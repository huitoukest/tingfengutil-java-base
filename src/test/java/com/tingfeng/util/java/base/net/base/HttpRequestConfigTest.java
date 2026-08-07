package com.tingfeng.util.java.base.net.base;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HttpRequestConfig 单元测试
 */
public class HttpRequestConfigTest {

    /**
     * 默认值：连接 10s / 读取 60s / 跟随重定向 / UTF-8 / gzip 开启 / ssl 工厂 null
     */
    @Test
    public void testDefaultValues() {
        HttpRequestConfig config = HttpRequestConfig.newDefault();
        Assert.assertEquals(10000, config.getConnectTimeout());
        Assert.assertEquals(60000, config.getReadTimeout());
        Assert.assertTrue(config.isFollowRedirects());
        Assert.assertEquals("UTF-8", config.getCharset());
        Assert.assertTrue(config.isUseGzip());
        Assert.assertNull(config.getSslSocketFactory());
        Assert.assertNull(config.getHeaders());
    }

    /**
     * fluent setter 链式调用生效
     */
    @Test
    public void testFluentSetterChain() {
        HttpRequestConfig config = HttpRequestConfig.newDefault()
                .setConnectTimeout(5000)
                .setReadTimeout(10000)
                .setFollowRedirects(false)
                .setCharset("GBK")
                .setUseGzip(false);
        Assert.assertEquals(5000, config.getConnectTimeout());
        Assert.assertEquals(10000, config.getReadTimeout());
        Assert.assertFalse(config.isFollowRedirects());
        Assert.assertEquals("GBK", config.getCharset());
        Assert.assertFalse(config.isUseGzip());
    }

    /**
     * addHeader 同名覆盖，返回当前对象
     */
    @Test
    public void testAddHeader() {
        HttpRequestConfig config = HttpRequestConfig.newDefault();
        Assert.assertSame(config, config.addHeader("X-Token", "abc"));
        Assert.assertSame(config, config.addHeader("X-Token", "def"));
        Assert.assertEquals(1, config.getHeaders().size());
        Assert.assertEquals("def", config.getHeaders().get("X-Token"));
    }

    /**
     * setHeaders 整体覆盖
     */
    @Test
    public void testSetHeaders() {
        HttpRequestConfig config = HttpRequestConfig.newDefault();
        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put("A", "1");
        config.setHeaders(headers);
        Assert.assertSame(headers, config.getHeaders());
        Assert.assertEquals("1", config.getHeaders().get("A"));
    }

    /**
     * 默认常量公开可断言
     */
    @Test
    public void testDefaultConstants() {
        Assert.assertEquals(10000, HttpRequestConfig.DEFAULT_CONNECT_TIMEOUT);
        Assert.assertEquals(60000, HttpRequestConfig.DEFAULT_READ_TIMEOUT);
    }
}
