package com.tingfeng.util.java.base.net.support;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * MockHttpServer 测试基座冒烟验证（6 场景基本行为）
 */
public class MockHttpServerTest {

    private MockHttpServer mockServer;

    @Before
    public void setUp() throws IOException {
        mockServer = MockHttpServer.start();
    }

    @After
    public void tearDown() {
        mockServer.close();
    }

    /**
     * echo 场景：GET 请求回显 method
     */
    @Test
    public void testEchoGet() throws IOException {
        HttpURLConnection connection = open("/echo?name=value");
        Assert.assertEquals(200, connection.getResponseCode());
        String body = readBody(connection);
        Assert.assertTrue(body.contains("method=GET"));
        Assert.assertTrue(body.contains("body="));
        connection.disconnect();
    }

    /**
     * echo 场景：POST body 原样回显
     */
    @Test
    public void testEchoPost() throws IOException {
        HttpURLConnection connection = open("/echo");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.getOutputStream().write("hello-body".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(200, connection.getResponseCode());
        String body = readBody(connection);
        Assert.assertTrue(body.contains("method=POST"));
        Assert.assertTrue(body.contains("body=hello-body"));
        connection.disconnect();
    }

    /**
     * echo 场景：自定义 header 回显
     */
    @Test
    public void testEchoHeader() throws IOException {
        HttpURLConnection connection = open("/echo");
        connection.setRequestProperty("X-Custom-Header", "abc");
        Assert.assertEquals(200, connection.getResponseCode());
        String body = readBody(connection);
        // JDK 8 HttpURLConnection 会将自定义头名规范化为 X-custom-header（capitalize 规则）后发送
        Assert.assertTrue(body.contains("header-X-custom-header=[abc]"));
        connection.disconnect();
    }

    /**
     * redirect 场景：302 带 Location
     */
    @Test
    public void testRedirect302() throws IOException {
        HttpURLConnection connection = open("/redirect/302");
        connection.setInstanceFollowRedirects(false);
        Assert.assertEquals(302, connection.getResponseCode());
        Assert.assertNotNull(connection.getHeaderField("Location"));
        Assert.assertTrue(connection.getHeaderField("Location").contains("/echo?from=redirect"));
        connection.disconnect();
    }

    /**
     * redirect 场景：307 带 Location
     */
    @Test
    public void testRedirect307() throws IOException {
        HttpURLConnection connection = open("/redirect/307");
        connection.setInstanceFollowRedirects(false);
        Assert.assertEquals(307, connection.getResponseCode());
        Assert.assertNotNull(connection.getHeaderField("Location"));
        connection.disconnect();
    }

    /**
     * gzip 场景：Content-Encoding: gzip，解压后内容正确
     */
    @Test
    public void testGzip() throws IOException {
        HttpURLConnection connection = open("/gzip");
        Assert.assertEquals(200, connection.getResponseCode());
        Assert.assertEquals("gzip", connection.getHeaderField("Content-Encoding"));
        byte[] raw = readAll(connection.getInputStream());
        GZIPInputStream gzipIn = new GZIPInputStream(new java.io.ByteArrayInputStream(raw));
        String content = new String(readAll(gzipIn), StandardCharsets.UTF_8);
        Assert.assertEquals("gzip-response-content", content);
        connection.disconnect();
    }

    /**
     * status 场景：500/404/204
     */
    @Test
    public void testStatusCodes() throws IOException {
        Assert.assertEquals(500, open("/status/500").getResponseCode());
        Assert.assertEquals(404, open("/status/404").getResponseCode());
        Assert.assertEquals(204, open("/status/204").getResponseCode());
    }

    /**
     * slow 场景：延迟后正常响应
     */
    @Test
    public void testSlow() throws IOException {
        HttpURLConnection connection = open("/slow?delay=100");
        Assert.assertEquals(200, connection.getResponseCode());
        Assert.assertTrue(readBody(connection).contains("slow-done"));
        connection.disconnect();
    }

    /**
     * binary 场景：0-255 字节序列往返一致
     */
    @Test
    public void testBinary() throws IOException {
        HttpURLConnection connection = open("/binary");
        Assert.assertEquals(200, connection.getResponseCode());
        byte[] bytes = readAll(connection.getInputStream());
        Assert.assertEquals(256, bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            Assert.assertEquals((byte) i, bytes[i]);
        }
        connection.disconnect();
    }

    private HttpURLConnection open(String path) throws IOException {
        return (HttpURLConnection) new URL(mockServer.getUrl(path)).openConnection();
    }

    private String readBody(HttpURLConnection connection) throws IOException {
        return new String(readAll(connection.getInputStream()), StandardCharsets.UTF_8);
    }

    private byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }
}
