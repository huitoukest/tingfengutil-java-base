package com.tingfeng.util.java.base.net.base;

import com.tingfeng.util.java.base.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HttpsURLConnection;

/**
 * HTTP 统一连接执行器（静态方法模式）
 *
 * 负责 HttpURLConnection 的完整请求执行链路：URL 解析、请求头设置（默认头 + 自定义头覆盖）、
 * 超时控制、重定向跟随、请求体写入、状态码获取、gzip/deflate 自动解压与响应体字节收集。
 *
 * 失败语义（status=0）：任何传输层失败（超时/DNS/IO/协议错误）统一返回 status=0 且
 * bodyBytes 为 null 的响应对象，不抛异常；仅 null/空 URL、null 方法等参数错误抛
 * IllegalArgumentException（编程错误快速失败）。
 *
 * 重定向：默认自动跟随（由 JDK 内部限制跳数防环），301/302/303 跟随时方法可能变为 GET
 * （JDK 8 行为），307 保持方法与请求体；关闭自动跟随时 3xx 原样返回（含 Location 头），
 * 手动跟随循环由调用方实现并自控跳数。
 *
 * gzip：默认发送 Accept-Encoding: gzip, deflate，响应按 Content-Encoding 头自动解压；
 * 服务端不支持 gzip 时无该头则普通读取，双向兼容。
 *
 * @author huitoukest
 */
public final class HttpConnectionExecutor {

    /** 默认 User-Agent（调用方可经 config.headers 覆盖） */
    private static final String DEFAULT_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)";
    /** 读取缓冲大小 */
    private static final int BUFFER_SIZE = 8192;

    private HttpConnectionExecutor() {
    }

    /**
     * 执行一次 HTTP 请求
     *
     * @param url    请求地址，null、空串、无 host（如 http://）或非 http(s) 协议抛 IllegalArgumentException
     * @param method 请求方法，null 抛 IllegalArgumentException；PATCH 不支持（JDK 8 白名单限制），
     *               需要 PATCH 语义时请使用 POST + X-HTTP-Method-Override 约定
     * @param body   请求体字节，仅 POST/PUT/DELETE 且 body 非 null 时写入；GET/HEAD/OPTIONS 忽略
     * @param config 请求配置，null 时使用默认配置（连接超时 10s / 读取超时 60s / 跟随重定向 / gzip）
     * @return 标准响应对象；传输失败（超时/DNS/IO/协议错误）时 status=0 且 bodyBytes 为 null，不抛异常
     */
    public static HttpResponseInfo execute(String url, HttpMethod method, byte[] body, HttpRequestConfig config) {
        if (url == null || url.trim().length() == 0) {
            throw new IllegalArgumentException("url must not be null or empty");
        }
        if (method == null) {
            throw new IllegalArgumentException("method must not be null");
        }
        HttpRequestConfig cfg = config == null ? HttpRequestConfig.newDefault() : config;

        HttpURLConnection connection = null;
        try {
            URL realUrl = new URL(url);
            if (realUrl.getHost() == null || realUrl.getHost().length() == 0) {
                throw new IllegalArgumentException("url must contain a host: " + url);
            }
            URLConnection urlConnection = realUrl.openConnection();
            if (!(urlConnection instanceof HttpURLConnection)) {
                throw new IllegalArgumentException("url must use http or https protocol: " + url);
            }
            connection = (HttpURLConnection) urlConnection;
            setupConnection(connection, method, body, cfg);

            // 原生状态码获取（替代正则解析）；-1 表示无有效响应，归入传输失败
            int status = connection.getResponseCode();
            if (status < 0) {
                return failure(url, null);
            }
            byte[] bodyBytes = readBody(connection, status);
            return new HttpResponseInfo(status, connection.getHeaderFields(), bodyBytes);
        } catch (IOException e) {
            return failure(url, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 配置连接：默认请求头（自定义头后设置可覆盖）、超时、重定向、SSL 工厂、方法、请求体写入
     */
    private static void setupConnection(HttpURLConnection connection, HttpMethod method, byte[] body,
                                        HttpRequestConfig config) throws IOException {
        // 默认请求头先设置，config.headers 后设置同名覆盖，调用方自定义优先
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
        connection.setRequestProperty("Content-Type", "text/plain; charset=" + config.getCharset());
        if (config.isUseGzip()) {
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        }
        Map<String, String> customHeaders = config.getHeaders();
        if (customHeaders != null && !customHeaders.isEmpty()) {
            for (Map.Entry<String, String> header : customHeaders.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        connection.setConnectTimeout(config.getConnectTimeout());
        connection.setReadTimeout(config.getReadTimeout());
        // 实例级重定向开关，不污染全局配置
        connection.setInstanceFollowRedirects(config.isFollowRedirects());
        if (connection instanceof HttpsURLConnection && config.getSslSocketFactory() != null) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(config.getSslSocketFactory());
        }

        connection.setRequestMethod(method.name());
        // 仅 POST/PUT/DELETE 且 body 非 null 时写请求体；GET/HEAD/OPTIONS 不写
        if (body != null
                && (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.DELETE)) {
            connection.setDoOutput(true);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(body);
                output.flush();
            }
        }
    }

    /**
     * 读取响应体：4xx/5xx 走错误流，其余走输入流；按 Content-Encoding 自动解压 gzip/deflate
     */
    private static byte[] readBody(HttpURLConnection connection, int status) throws IOException {
        InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (input == null) {
            return new byte[0];
        }
        String contentEncoding = connection.getHeaderField("Content-Encoding");
        if ("gzip".equalsIgnoreCase(contentEncoding)) {
            try (InputStream decoded = new GZIPInputStream(input)) {
                return readAll(decoded);
            }
        }
        if ("deflate".equalsIgnoreCase(contentEncoding)) {
            try (InputStream decoded = new InflaterInputStream(input)) {
                return readAll(decoded);
            }
        }
        try (InputStream plain = input) {
            return readAll(plain);
        }
    }

    /**
     * 循环 read 收集全部字节（消除 readLine 丢换行与字符串 += 的 O(n²)）
     */
    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    /**
     * 传输失败统一出口：status=0、bodyBytes=null、不抛异常
     */
    private static HttpResponseInfo failure(String url, IOException cause) {
        LogUtils.error("Http execute failed, url=%s", url);
        if (cause != null) {
            // 显式 toString 避免匹配 error(String, Throwable) 重载导致 %s 不替换
            LogUtils.error("Http execute failed cause: %s", cause.toString());
        }
        return new HttpResponseInfo(0, null, null);
    }
}
