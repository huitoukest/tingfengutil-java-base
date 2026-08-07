package com.tingfeng.util.java.base.net.base;

import com.tingfeng.util.java.base.net.support.MockHttpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * HttpConnectionExecutor 执行器测试（基于本地 Mock 服务，零外网依赖）
 *
 * 覆盖：echo 场景（GET/POST body 一致、GET 忽略 body）、gzip 自动解压、302/307 重定向跟随与关闭、
 * 404/500/204 状态码透传、readTimeout 超时（status=0）、binary 字节响应、自定义 header 透传、
 * 参数校验与传输失败路径
 *
 * @author huitoukest
 */
public class HttpConnectionExecutorTest {

    private MockHttpServer server;

    @Before
    public void setUp() throws Exception {
        server = MockHttpServer.start();
    }

    @After
    public void tearDown() {
        server.close();
    }

    @Test
    public void testExecuteGetEcho() {
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/echo"), HttpMethod.GET, null, HttpRequestConfig.newDefault());
        assertEquals(200, info.getStatus());
        assertTrue(info.isSuccess());
        assertTrue(info.getBodyAsString().contains("method=GET"));
    }

    @Test
    public void testExecutePostBodyConsistent() {
        byte[] body = "hello-post-body".getBytes(StandardCharsets.UTF_8);
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/echo"), HttpMethod.POST, body, HttpRequestConfig.newDefault());
        assertEquals(200, info.getStatus());
        String responseBody = info.getBodyAsString();
        assertTrue(responseBody.contains("method=POST"));
        assertTrue(responseBody.contains("body=hello-post-body"));
    }

    @Test
    public void testExecuteGetWithBodyNotWritten() {
        byte[] body = "should-not-be-sent".getBytes(StandardCharsets.UTF_8);
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/echo"), HttpMethod.GET, body, HttpRequestConfig.newDefault());
        assertEquals(200, info.getStatus());
        String responseBody = info.getBodyAsString();
        assertTrue(responseBody.contains("method=GET"));
        assertTrue(responseBody.contains("body="));
        assertFalse(responseBody.contains("should-not-be-sent"));
    }

    @Test
    public void testExecuteGzipAutoDecompress() {
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/gzip"), HttpMethod.GET, null, HttpRequestConfig.newDefault());
        assertEquals(200, info.getStatus());
        assertEquals("gzip-response-content", info.getBodyAsString());
    }

    @Test
    public void testExecuteFollowRedirect302() {
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/redirect/302"), HttpMethod.GET, null, HttpRequestConfig.newDefault());
        assertEquals(200, info.getStatus());
        assertTrue(info.getBodyAsString().contains("method=GET"));
    }

    @Test
    public void testExecuteFollowRedirect307KeepMethodAndBody() {
        byte[] body = "redirect-body".getBytes(StandardCharsets.UTF_8);
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/redirect/307"), HttpMethod.POST, body, HttpRequestConfig.newDefault());
        assertEquals(200, info.getStatus());
        String responseBody = info.getBodyAsString();
        assertTrue(responseBody.contains("method=POST"));
        assertTrue(responseBody.contains("body=redirect-body"));
    }

    @Test
    public void testExecuteNotFollowRedirect302() {
        HttpRequestConfig config = HttpRequestConfig.newDefault().setFollowRedirects(false);
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/redirect/302"), HttpMethod.GET, null, config);
        assertEquals(302, info.getStatus());
        assertFalse(info.isSuccess());
        assertTrue(containsHeader(info, "Location"));
    }

    @Test
    public void testExecuteStatus404() {
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/status/404"), HttpMethod.GET, null, HttpRequestConfig.newDefault());
        assertEquals(404, info.getStatus());
        assertFalse(info.isSuccess());
        assertTrue(info.getBodyAsString().contains("status-404"));
    }

    @Test
    public void testExecuteStatus500() {
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/status/500"), HttpMethod.GET, null, HttpRequestConfig.newDefault());
        assertEquals(500, info.getStatus());
        assertFalse(info.isSuccess());
        assertTrue(info.getBodyAsString().contains("status-500"));
    }

    @Test
    public void testExecuteStatus204EmptyBody() {
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/status/204"), HttpMethod.GET, null, HttpRequestConfig.newDefault());
        assertEquals(204, info.getStatus());
        assertTrue(info.isSuccess());
        assertNotNull(info.getBodyBytes());
        assertEquals(0, info.getBodyBytes().length);
    }

    @Test
    public void testExecuteReadTimeoutReturnsStatusZero() {
        HttpRequestConfig config = HttpRequestConfig.newDefault().setReadTimeout(300);
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/slow?delay=2000"), HttpMethod.GET, null, config);
        assertEquals(0, info.getStatus());
        assertNull(info.getBodyBytes());
        assertFalse(info.isSuccess());
    }

    @Test
    public void testExecuteBinaryResponse() {
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/binary"), HttpMethod.GET, null, HttpRequestConfig.newDefault());
        assertEquals(200, info.getStatus());
        byte[] bytes = info.getBodyBytes();
        assertNotNull(bytes);
        assertEquals(256, bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            assertEquals((byte) i, bytes[i]);
        }
    }

    @Test
    public void testExecuteCustomHeaderPassThrough() {
        HttpRequestConfig config = HttpRequestConfig.newDefault()
                .addHeader("X-Custom-Header", "custom-value");
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/echo"), HttpMethod.GET, null, config);
        assertEquals(200, info.getStatus());
        String responseBody = info.getBodyAsString();
        assertTrue(responseBody.contains("custom-value"));
        // 头名大小写不敏感断言：JDK 8 会规范化自定义头名（capitalize 形式），Mock 端回显 wire 名，统一小写后比较
        assertTrue(responseBody.toLowerCase().contains("header-x-custom-header"));
    }

    @Test
    public void testExecuteNullConfigUsesDefault() {
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/echo"), HttpMethod.GET, null, null);
        assertEquals(200, info.getStatus());
    }

    @Test
    public void testExecuteNullUrlThrows() {
        try {
            HttpConnectionExecutor.execute(null, HttpMethod.GET, null, HttpRequestConfig.newDefault());
            fail("null url should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void testExecuteEmptyUrlThrows() {
        try {
            HttpConnectionExecutor.execute("   ", HttpMethod.GET, null, HttpRequestConfig.newDefault());
            fail("empty url should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void testExecuteNullMethodThrows() {
        try {
            HttpConnectionExecutor.execute(server.getUrl("/echo"), null, null, HttpRequestConfig.newDefault());
            fail("null method should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void testExecuteUrlWithoutHostThrows() {
        try {
            HttpConnectionExecutor.execute("http://", HttpMethod.GET, null, HttpRequestConfig.newDefault());
            fail("url without host should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void testExecuteNonHttpProtocolThrows() {
        try {
            HttpConnectionExecutor.execute("ftp://127.0.0.1:21/x", HttpMethod.GET, null, HttpRequestConfig.newDefault());
            fail("non http(s) url should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void testExecuteConnectionRefusedReturnsStatusZero() throws Exception {
        int port = server.getPort();
        server.close();
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                "http://127.0.0.1:" + port + "/echo", HttpMethod.GET, null, HttpRequestConfig.newDefault());
        assertEquals(0, info.getStatus());
        assertNull(info.getBodyBytes());
    }

    @Test
    public void testExecuteStatus101PassThrough() {
        // 1xx 边界：JDK 8 HttpURLConnection 对 101 原生透传（实验确认），非 2xx 故 isSuccess=false；
        // HttpServer 对 1xx 强制无响应体（contentLen=-1），body 为空字节数组
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/status/101"), HttpMethod.GET, null, HttpRequestConfig.newDefault());
        assertEquals(101, info.getStatus());
        assertFalse(info.isSuccess());
        assertNotNull(info.getBodyBytes());
        assertEquals(0, info.getBodyBytes().length);
    }

    @Test
    public void testExecuteStatus100ReturnsStatusZero() {
        // 1xx 边界：JDK 8 客户端对 100 Continue 特殊处理，Mock 回 100 后关闭连接 → EOF IOException → status=0
        // （Mock 场景实测行为，JDK 8 环境稳定；符合"协议错误归入传输失败"设计）
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/status/100"), HttpMethod.GET, null, HttpRequestConfig.newDefault());
        assertEquals(0, info.getStatus());
        assertNull(info.getBodyBytes());
    }

    @Test
    public void testExecuteGzipDisabledOmitsAcceptEncoding() {
        // gzip 开关关闭：请求头不携带 Accept-Encoding（echo 无该头回显）
        HttpRequestConfig config = HttpRequestConfig.newDefault().setUseGzip(false);
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/echo"), HttpMethod.GET, null, config);
        assertEquals(200, info.getStatus());
        assertFalse(info.getBodyAsString().toLowerCase().contains("accept-encoding"));
    }

    @Test
    public void testExecuteGzipEnabledSendsAcceptEncoding() {
        // gzip 开关默认开启：请求头携带 Accept-Encoding: gzip, deflate
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/echo"), HttpMethod.GET, null, HttpRequestConfig.newDefault());
        assertEquals(200, info.getStatus());
        assertTrue(info.getBodyAsString().toLowerCase().contains("accept-encoding"));
    }

    @Test
    public void testExecuteNotFollowRedirect307() {
        // 关闭自动跟随时 307 原样返回，Location 头可读
        HttpRequestConfig config = HttpRequestConfig.newDefault().setFollowRedirects(false);
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/redirect/307"), HttpMethod.GET, null, config);
        assertEquals(307, info.getStatus());
        assertFalse(info.isSuccess());
        assertTrue(containsHeader(info, "Location"));
    }

    @Test
    public void testExecuteReadTimeoutNotTriggeredWithinLimit() {
        // timeout 边界正例：readTimeout=1000 大于 slow delay=100，正常完成不触发超时
        HttpRequestConfig config = HttpRequestConfig.newDefault().setReadTimeout(1000);
        HttpResponseInfo info = HttpConnectionExecutor.execute(
                server.getUrl("/slow?delay=100"), HttpMethod.GET, null, config);
        assertEquals(200, info.getStatus());
        assertTrue(info.getBodyAsString().contains("slow-done"));
    }

    /**
     * 响应头是否包含指定头名（大小写不敏感）
     */
    private static boolean containsHeader(HttpResponseInfo info, String headerName) {
        if (info.getHeaders() == null) {
            return false;
        }
        for (String key : info.getHeaders().keySet()) {
            if (key != null && key.equalsIgnoreCase(headerName)) {
                return true;
            }
        }
        return false;
    }
}
