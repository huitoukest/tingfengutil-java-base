package com.tingfeng.util.java.base.net.support;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * 本地 Mock HTTP 服务测试基座（基于 JDK 自带 com.sun.net.httpserver，零第三方依赖）
 *
 * 场景路由（前缀匹配）：
 * - /echo              回显请求 method/headers/body，响应格式 method={m}、body={b}、header-{k}={v} 逐行
 * - /redirect/302、/redirect/307  返回 302/307 并携带 Location=/echo?from=redirect
 * - /gzip              返回 GZIP 压缩响应（Content-Encoding: gzip）
 * - /status/{code}     返回指定状态码（支持 500/404/204 等，204 无响应体）
 * - /slow?delay={ms}   延迟指定毫秒后响应，用于超时测试，默认 5000ms
 * - /binary            返回固定二进制响应（0-255 递增字节序列）
 *
 * 随机端口绑定（port=0），close 时释放，测试离线可跑。
 *
 * @author huitoukest
 */
public class MockHttpServer implements Closeable {

    private final HttpServer server;

    private MockHttpServer(HttpServer server) {
        this.server = server;
    }

    /**
     * 启动 Mock 服务（随机端口）
     *
     * @return Mock 服务实例
     * @throws IOException 端口绑定失败
     */
    public static MockHttpServer start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/echo", MockHttpServer::handleEcho);
        server.createContext("/redirect", MockHttpServer::handleRedirect);
        server.createContext("/gzip", MockHttpServer::handleGzip);
        server.createContext("/status", MockHttpServer::handleStatus);
        server.createContext("/slow", MockHttpServer::handleSlow);
        server.createContext("/binary", MockHttpServer::handleBinary);
        server.start();
        return new MockHttpServer(server);
    }

    /**
     * 获取监听端口
     *
     * @return 端口号
     */
    public int getPort() {
        return server.getAddress().getPort();
    }

    /**
     * 获取基础地址，形如 http://127.0.0.1:{port}
     *
     * @return 基础地址
     */
    public String getBaseUrl() {
        return "http://127.0.0.1:" + getPort();
    }

    /**
     * 获取指定路径的完整地址
     *
     * @param path 以 / 开头的路径
     * @return 完整地址
     */
    public String getUrl(String path) {
        return getBaseUrl() + path;
    }

    @Override
    public void close() {
        server.stop(0);
    }

    /**
     * 回显场景：响应 method/body/headers，供请求参数透传断言
     */
    private static void handleEcho(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        byte[] requestBody = readAll(exchange.getRequestBody());
        StringBuilder response = new StringBuilder();
        response.append("method=").append(method).append('\n');
        response.append("body=").append(new String(requestBody, StandardCharsets.UTF_8)).append('\n');
        for (Map.Entry<String, List<String>> header : exchange.getRequestHeaders().entrySet()) {
            response.append("header-").append(header.getKey()).append('=').append(header.getValue()).append('\n');
        }
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        writeResponse(exchange, 200, response.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 重定向场景：/redirect/302 返回 302，/redirect/307 返回 307，Location 指向 /echo
     */
    private static void handleRedirect(HttpExchange exchange) throws IOException {
        String host = exchange.getRequestHeaders().getFirst("Host");
        String location = "http://" + host + "/echo?from=redirect";
        exchange.getResponseHeaders().set("Location", location);
        int code = exchange.getRequestURI().getPath().endsWith("/307") ? 307 : 302;
        exchange.sendResponseHeaders(code, -1);
        exchange.close();
    }

    /**
     * gzip 场景：返回 GZIP 压缩的固定内容
     */
    private static void handleGzip(HttpExchange exchange) throws IOException {
        byte[] content = "gzip-response-content".getBytes(StandardCharsets.UTF_8);
        byte[] gzipBytes = gzip(content);
        exchange.getResponseHeaders().set("Content-Encoding", "gzip");
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        writeResponse(exchange, 200, gzipBytes);
    }

    /**
     * 状态码场景：/status/{code} 返回指定状态码，204 无响应体
     */
    private static void handleStatus(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        int code = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
        if (code == 204) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }
        byte[] content = ("status-" + code).getBytes(StandardCharsets.UTF_8);
        writeResponse(exchange, code, content);
    }

    /**
     * 慢响应场景：/slow?delay={ms} 延迟后返回，用于超时测试
     */
    private static void handleSlow(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        int delay = 5000;
        if (query != null && query.startsWith("delay=")) {
            delay = Integer.parseInt(query.substring("delay=".length()));
        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        byte[] content = "slow-done".getBytes(StandardCharsets.UTF_8);
        writeResponse(exchange, 200, content);
    }

    /**
     * 二进制场景：返回 0-255 递增字节序列，供二进制往返断言
     */
    private static void handleBinary(HttpExchange exchange) throws IOException {
        byte[] content = new byte[256];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) i;
        }
        exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
        writeResponse(exchange, 200, content);
    }

    private static void writeResponse(HttpExchange exchange, int code, byte[] body) throws IOException {
        exchange.sendResponseHeaders(code, body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }

    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private static byte[] gzip(byte[] data) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
            gzip.write(data);
        }
        return output.toByteArray();
    }
}
