package com.tingfeng.util.java.base.net.base;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HttpResponseInfo 单元测试
 */
public class HttpResponseInfoTest {

    /**
     * status 默认值为 0（传输失败语义），bodyBytes 为 null
     */
    @Test
    public void testDefaultStatusIsZero() {
        HttpResponseInfo info = new HttpResponseInfo();
        Assert.assertEquals(0, info.getStatus());
        Assert.assertNull(info.getBodyBytes());
        Assert.assertNull(info.getHeaders());
        Assert.assertNull(info.getBodyAsString());
        Assert.assertFalse(info.isSuccess());
    }

    /**
     * getBodyAsString 默认 UTF-8 解码
     */
    @Test
    public void testGetBodyAsStringUtf8() {
        HttpResponseInfo info = new HttpResponseInfo(200, null, "你好".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("你好", info.getBodyAsString());
    }

    /**
     * getBodyAsString 支持显式字符集（GBK 编解码闭环）
     */
    @Test
    public void testGetBodyAsStringWithGbk() {
        byte[] gbkBytes = "中文内容".getBytes(Charset.forName("GBK"));
        HttpResponseInfo info = new HttpResponseInfo(200, null, gbkBytes);
        Assert.assertEquals("中文内容", info.getBodyAsString("GBK"));
    }

    /**
     * bodyBytes 为 null 时 getBodyAsString 返回 null，不抛异常
     */
    @Test
    public void testGetBodyAsStringNullBody() {
        HttpResponseInfo info = new HttpResponseInfo(0, null, null);
        Assert.assertNull(info.getBodyAsString("GBK"));
    }

    /**
     * 非法字符集抛 IllegalArgumentException（编程错误快速失败）
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetBodyAsStringInvalidCharset() {
        HttpResponseInfo info = new HttpResponseInfo(200, null, new byte[]{1});
        info.getBodyAsString("no-such-charset-xyz");
    }

    /**
     * null 字符集抛 IllegalArgumentException（编程错误快速失败）
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetBodyAsStringNullCharset() {
        HttpResponseInfo info = new HttpResponseInfo(200, null, new byte[]{1});
        info.getBodyAsString(null);
    }

    /**
     * isSuccess 边界：199/200/299/300/0/500
     */
    @Test
    public void testIsSuccessBoundary() {
        Assert.assertFalse(new HttpResponseInfo(0, null, null).isSuccess());
        Assert.assertFalse(new HttpResponseInfo(199, null, null).isSuccess());
        Assert.assertTrue(new HttpResponseInfo(200, null, null).isSuccess());
        Assert.assertTrue(new HttpResponseInfo(299, null, null).isSuccess());
        Assert.assertFalse(new HttpResponseInfo(300, null, null).isSuccess());
        Assert.assertFalse(new HttpResponseInfo(500, null, null).isSuccess());
    }

    /**
     * equals：同字段相等，status/bodyBytes/headers 任一不同则不等
     */
    @Test
    public void testEquals() {
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Content-Type", Arrays.asList("text/plain"));
        HttpResponseInfo a = new HttpResponseInfo(200, headers, new byte[]{1, 2});
        HttpResponseInfo b = new HttpResponseInfo(200, headers, new byte[]{1, 2});
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);
        Assert.assertNotEquals(a, new HttpResponseInfo(201, headers, new byte[]{1, 2}));
        Assert.assertNotEquals(a, new HttpResponseInfo(200, headers, new byte[]{1, 3}));
        Assert.assertNotEquals(a, new HttpResponseInfo(200, null, new byte[]{1, 2}));
        Assert.assertNotEquals(a, null);
        Assert.assertNotEquals(a, "other");
    }

    /**
     * hashCode：相等对象 hashCode 一致
     */
    @Test
    public void testHashCode() {
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Content-Type", Arrays.asList("text/plain"));
        HttpResponseInfo a = new HttpResponseInfo(200, headers, new byte[]{1, 2});
        HttpResponseInfo b = new HttpResponseInfo(200, headers, new byte[]{1, 2});
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    /**
     * toString 基于三字段生成
     */
    @Test
    public void testToString() {
        HttpResponseInfo info = new HttpResponseInfo(200, null, new byte[]{1, 2});
        String text = info.toString();
        Assert.assertTrue(text.contains("status=200"));
        Assert.assertTrue(text.contains("bodyBytes.length=2"));
    }

    /**
     * fluent setter 返回当前对象
     */
    @Test
    public void testFluentSetter() {
        HttpResponseInfo info = new HttpResponseInfo();
        Assert.assertSame(info, info.setStatus(200));
        Assert.assertSame(info, info.setBodyBytes(new byte[]{1}));
        Assert.assertSame(info, info.setHeaders(null));
        Assert.assertEquals(200, info.getStatus());
    }
}
