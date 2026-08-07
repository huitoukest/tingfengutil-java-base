package com.tingfeng.util.java.base.net;

import com.tingfeng.util.java.base.net.base.HttpMethod;
import com.tingfeng.util.java.base.net.base.HttpRequestConfig;
import com.tingfeng.util.java.base.net.base.HttpResponseInfo;
import com.tingfeng.util.java.base.net.support.MockHttpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * HttpUtils 门面测试（纯函数边界 + 性能断言 + Mock 全场景委托，零外网依赖）
 *
 * 覆盖：toGetUrl（? 第 0 位边界、中文 encode、默认 UTF-8、非法 charset、null url）、
 * parseGetParams（O(n) 1MB 性能断言、= 在首位边界、空串/null、不解码保持）、
 * getFileUrl（http/https 前缀跳过、空前缀）、门面委托（GET/POST 表单/JSON/PUT/DELETE/HEAD/OPTIONS、
 * 404/500/204 状态码、gzip 解压、302 跟随与关闭）
 *
 * @author huitoukest
 */
public class HttpUtilsTest {

    private MockHttpServer server;

    @Before
    public void setUp() throws Exception {
        server = MockHttpServer.start();
    }

    @After
    public void tearDown() {
        server.close();
    }

    // ==================== toGetUrl 纯函数 ====================

    @Test
    public void testToGetUrlQuestionMarkAtFirst() {
        // ? 在第 0 位时仍应识别为已带参数，追加 & 而非重复 ?
        assertEquals("?a=1&b=2", HttpUtils.toGetUrl("?a=1", singleParam("b", "2")));
    }

    @Test
    public void testToGetUrlAppendWhenHasParam() {
        assertEquals("http://x.com/p?a=1&b=2",
                HttpUtils.toGetUrl("http://x.com/p?a=1", singleParam("b", "2")));
    }

    @Test
    public void testToGetUrlFirstParam() {
        assertEquals("http://x.com/p?a=1", HttpUtils.toGetUrl("http://x.com/p", singleParam("a", "1")));
    }

    @Test
    public void testToGetUrlNullUrl() {
        assertEquals("a=1&b=2", HttpUtils.toGetUrl(null, paramsOf("a", "1", "b", "2")));
    }

    @Test
    public void testToGetUrlNullValue() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("a", null);
        assertEquals("http://x.com/p?a=", HttpUtils.toGetUrl("http://x.com/p", params));
    }

    @Test
    public void testToGetUrlNullParams() {
        assertEquals("http://x.com/p", HttpUtils.toGetUrl("http://x.com/p", null));
    }

    @Test
    public void testToGetUrlEncodeChinese() {
        // URLEncoder 对空格编码为 +，中文按 UTF-8 百分号编码
        assertEquals("http://x.com/p?k=%E4%B8%AD%E6%96%87+%E5%80%BC",
                HttpUtils.toGetUrl("http://x.com/p", singleParam("k", "中文 值"), true, null));
    }

    @Test
    public void testToGetUrlEncodeDefaultCharset() {
        // encodeCharSet 为 null 时默认 UTF-8，中文正常编码不抛异常
        String result = HttpUtils.toGetUrl("http://x.com/p", singleParam("k", "中文"), true, null);
        assertEquals("http://x.com/p?k=%E4%B8%AD%E6%96%87", result);
    }

    @Test
    public void testToGetUrlIllegalCharset() {
        try {
            HttpUtils.toGetUrl("http://x.com/p", singleParam("k", "v"), true, "not-a-charset");
            fail("expected IllegalArgumentException for illegal charset");
        } catch (IllegalArgumentException expected) {
            // 编程错误快速失败
        }
    }

    // ==================== parseGetParams 纯函数 ====================

    @Test
    public void testParseGetParamsBasic() {
        Map<String, String> result = HttpUtils.parseGetParams("a=1&b=2");
        assertEquals("1", result.get("a"));
        assertEquals("2", result.get("b"));
        assertEquals(2, result.size());
    }

    @Test
    public void testParseGetParamsEqualsAtFirst() {
        // =x=1 场景：= 在首位仍进入完整解析（修复前直接返回空 Map），无有效键值对则空 Map
        assertTrue(HttpUtils.parseGetParams("=x=1").isEmpty());
    }

    @Test
    public void testParseGetParamsEqualsAtFirstWithValidPair() {
        // 修复入口边界后，= 开头的串仍能解析出后续有效键值对
        Map<String, String> result = HttpUtils.parseGetParams("=1&b=2");
        assertEquals(1, result.size());
        assertEquals("2", result.get("b"));
    }

    @Test
    public void testParseGetParamsEmpty() {
        assertTrue(HttpUtils.parseGetParams("").isEmpty());
    }

    @Test
    public void testParseGetParamsNull() {
        assertTrue(HttpUtils.parseGetParams(null).isEmpty());
    }

    @Test
    public void testParseGetParamsNoEquals() {
        assertTrue(HttpUtils.parseGetParams("abc&def").isEmpty());
    }

    @Test
    public void testParseGetParamsNoValueDropped() {
        // 无 = 的段（b）不产出键值对
        Map<String, String> result = HttpUtils.parseGetParams("a=1&b");
        assertEquals("1", result.get("a"));
        assertNull(result.get("b"));
        assertEquals(1, result.size());
    }

    @Test
    public void testParseGetParamsNoDecode() {
        // 保持不解码（与 toGetUrl 编码不对称是有意设计，防双重解码）
        Map<String, String> result = HttpUtils.parseGetParams("a=%E4%B8%AD&b=1");
        assertEquals("%E4%B8%AD", result.get("a"));
        assertEquals("1", result.get("b"));
    }

    @Test
    public void testParseGetParamsPerformanceLargeInput() {
        // 1MB 大输入性能断言：单次扫描 O(n)，修复前字符串 += 为 O(n²)
        StringBuilder input = new StringBuilder();
        for (int i = 0; i < 50000; i++) {
            input.append("key").append(i).append('=').append("value").append(i).append('&');
        }
        long start = System.nanoTime();
        Map<String, String> result = HttpUtils.parseGetParams(input.toString());
        long costMs = (System.nanoTime() - start) / 1000000;
        assertEquals(50000, result.size());
        assertEquals("value49999", result.get("key49999"));
        assertTrue("parseGetParams cost " + costMs + "ms, expected < 1000ms", costMs < 1000);
    }

    // ==================== getFileUrl 纯函数 ====================

    @Test
    public void testGetFileUrlHttpPrefixSkipped() {
        assertEquals("http://other.com/a.png",
                HttpUtils.getFileUrl("http://cdn.com/", "http://other.com/a.png"));
    }

    @Test
    public void testGetFileUrlHttpsPrefixSkipped() {
        assertEquals("https://other.com/a.png",
                HttpUtils.getFileUrl("http://cdn.com/", "https://other.com/a.png"));
    }

    @Test
    public void testGetFileUrlEmptyRelative() {
        assertEquals("", HttpUtils.getFileUrl("http://cdn.com/", ""));
        assertEquals("", HttpUtils.getFileUrl("http://cdn.com/", null));
    }

    @Test
    public void testGetFileUrlEmptyPrefix() {
        assertEquals("a.png", HttpUtils.getFileUrl("", "a.png"));
        assertEquals("a.png", HttpUtils.getFileUrl(null, "a.png"));
    }

    @Test
    public void testGetFileUrlConcatenate() {
        assertEquals("http://cdn.com/a.png", HttpUtils.getFileUrl("http://cdn.com/", "a.png"));
    }

    @Test
    public void testGetFileUrlTrim() {
        assertEquals("http://cdn.com/a.png", HttpUtils.getFileUrl("http://cdn.com/", "  a.png  "));
    }

    // ==================== 门面委托 Mock 场景 ====================

    @Test
    public void testSendGetEcho() {
        HttpResponseInfo info = HttpUtils.sendGet(server.getUrl("/echo"));
        assertEquals(200, info.getStatus());
        assertTrue(info.isSuccess());
        assertTrue(info.getBodyAsString().contains("method=GET"));
    }

    @Test
    public void testSendGetWithParams() {
        HttpResponseInfo info = HttpUtils.sendGet(server.getUrl("/echo"), singleParam("a", "1"));
        assertEquals(200, info.getStatus());
        assertTrue(info.getBodyAsString().contains("method=GET"));
    }

    @Test
    public void testSendGetWithConfig() {
        HttpRequestConfig config = HttpRequestConfig.newDefault()
                .setConnectTimeout(3000)
                .setReadTimeout(3000)
                .addHeader("X-Custom", "custom-value");
        HttpResponseInfo info = HttpUtils.sendGet(server.getUrl("/echo"), null, config);
        assertEquals(200, info.getStatus());
        // JDK HttpServer 将请求头名统一小写化存储，echo 输出为 header-X-custom
        assertTrue(info.getBodyAsString().contains("header-X-custom=[custom-value]"));
    }

    @Test
    public void testSendPostBody() {
        HttpResponseInfo info = HttpUtils.sendPost(server.getUrl("/echo"), "hello-post", "text/plain");
        assertEquals(200, info.getStatus());
        String body = info.getBodyAsString();
        assertTrue(body.contains("method=POST"));
        assertTrue(body.contains("body=hello-post"));
    }

    @Test
    public void testSendPostForm() {
        HttpResponseInfo info = HttpUtils.sendPost(server.getUrl("/echo"), singleParam("k", "v"));
        assertEquals(200, info.getStatus());
        String body = info.getBodyAsString();
        assertTrue(body.contains("method=POST"));
        assertTrue(body.contains("body=k=v"));
        assertTrue(body.contains("application/x-www-form-urlencoded"));
    }

    @Test
    public void testSendPostJson() {
        HttpResponseInfo info = HttpUtils.sendPostJson(server.getUrl("/echo"), "{\"a\":1}");
        assertEquals(200, info.getStatus());
        String body = info.getBodyAsString();
        assertTrue(body.contains("method=POST"));
        assertTrue(body.contains("body={\"a\":1}"));
        assertTrue(body.contains("application/json"));
    }

    @Test
    public void testSendRequestPut() {
        HttpRequestConfig config = HttpRequestConfig.newDefault();
        HttpResponseInfo info = HttpUtils.sendRequest(server.getUrl("/echo"), HttpMethod.PUT,
                "put-body".getBytes(StandardCharsets.UTF_8), config);
        assertEquals(200, info.getStatus());
        String body = info.getBodyAsString();
        assertTrue(body.contains("method=PUT"));
        assertTrue(body.contains("body=put-body"));
    }

    @Test
    public void testSendRequestDelete() {
        HttpResponseInfo info = HttpUtils.sendRequest(server.getUrl("/echo"), HttpMethod.DELETE,
                "del-body".getBytes(StandardCharsets.UTF_8), HttpRequestConfig.newDefault());
        assertEquals(200, info.getStatus());
        assertTrue(info.getBodyAsString().contains("method=DELETE"));
    }

    @Test
    public void testSendRequestHead() {
        HttpResponseInfo info = HttpUtils.sendRequest(server.getUrl("/echo"), HttpMethod.HEAD,
                null, HttpRequestConfig.newDefault());
        assertEquals(200, info.getStatus());
    }

    @Test
    public void testSendRequestOptions() {
        HttpResponseInfo info = HttpUtils.sendRequest(server.getUrl("/echo"), HttpMethod.OPTIONS,
                null, HttpRequestConfig.newDefault());
        assertEquals(200, info.getStatus());
        assertTrue(info.getBodyAsString().contains("method=OPTIONS"));
    }

    @Test
    public void testSendGetStatus404() {
        HttpResponseInfo info = HttpUtils.sendGet(server.getUrl("/status/404"));
        assertEquals(404, info.getStatus());
        assertFalse(info.isSuccess());
    }

    @Test
    public void testSendGetStatus500() {
        HttpResponseInfo info = HttpUtils.sendGet(server.getUrl("/status/500"));
        assertEquals(500, info.getStatus());
        assertFalse(info.isSuccess());
        assertTrue(info.getBodyAsString().contains("status-500"));
    }

    @Test
    public void testSendGetStatus204() {
        HttpResponseInfo info = HttpUtils.sendGet(server.getUrl("/status/204"));
        assertEquals(204, info.getStatus());
        assertTrue(info.isSuccess());
    }

    @Test
    public void testSendGetGzipAutoDecompress() {
        HttpResponseInfo info = HttpUtils.sendGet(server.getUrl("/gzip"));
        assertEquals(200, info.getStatus());
        assertEquals("gzip-response-content", info.getBodyAsString());
    }

    @Test
    public void testSendGetRedirectFollowed() {
        // 默认自动跟随 302 → /echo，最终 200（JDK 8 跟随 302 时方法变 GET）
        HttpResponseInfo info = HttpUtils.sendGet(server.getUrl("/redirect/302"));
        assertEquals(200, info.getStatus());
        assertTrue(info.getBodyAsString().contains("method=GET"));
    }

    @Test
    public void testSendGetRedirectDisabled() {
        // 关闭自动跟随时 3xx 原样返回，Location 头可读
        HttpRequestConfig config = HttpRequestConfig.newDefault().setFollowRedirects(false);
        HttpResponseInfo info = HttpUtils.sendGet(server.getUrl("/redirect/302"), null, config);
        assertEquals(302, info.getStatus());
        assertTrue(info.getHeaders().get("Location").get(0).endsWith("/echo?from=redirect"));
    }

    @Test
    public void testSendGetGzipDisabled() {
        // 门面级 gzip 开关关闭：config 透传执行器，请求头不携带 Accept-Encoding
        HttpRequestConfig config = HttpRequestConfig.newDefault().setUseGzip(false);
        HttpResponseInfo info = HttpUtils.sendGet(server.getUrl("/echo"), null, config);
        assertEquals(200, info.getStatus());
        assertFalse(info.getBodyAsString().toLowerCase().contains("accept-encoding"));
    }

    @Test
    public void testSendGetInvalidUrl() {
        try {
            HttpUtils.sendGet(null);
            fail("expected IllegalArgumentException for null url");
        } catch (IllegalArgumentException expected) {
            // 编程错误快速失败
        }
    }

    // ==================== 辅助 ====================

    private static Map<String, Object> singleParam(String key, String value) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(key, value);
        return params;
    }

    private static Map<String, Object> paramsOf(String k1, String v1, String k2, String v2) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(k1, v1);
        params.put(k2, v2);
        return params;
    }
}
