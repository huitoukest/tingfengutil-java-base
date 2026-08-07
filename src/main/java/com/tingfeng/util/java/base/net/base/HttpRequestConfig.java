package com.tingfeng.util.java.base.net.base;

import com.tingfeng.util.java.base.common.constant.Constants;

import javax.net.ssl.SSLSocketFactory;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HTTP 请求配置（可变对象，fluent setter 链式调用）
 *
 * - 每次请求应使用独立配置对象，禁止跨线程共享并同时修改
 * - 默认值：连接超时 10s / 读取超时 60s，默认跟随重定向，默认 gzip 请求头，默认 UTF-8 字符集
 * - sslSocketFactory 默认 null 走系统信任链；自签证书场景可注入自定义工厂（不内置 TrustManager 策略）
 *
 * @author huitoukest
 */
public class HttpRequestConfig {

    /** 默认连接超时毫秒数 */
    public static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    /** 默认读取超时毫秒数 */
    public static final int DEFAULT_READ_TIMEOUT = 60000;

    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;
    private Map<String, String> headers;
    private boolean followRedirects = true;
    private String charset = Constants.CharSet.UTF8;
    private boolean useGzip = true;
    private SSLSocketFactory sslSocketFactory;

    private HttpRequestConfig() {
    }

    /**
     * 创建默认配置实例
     *
     * @return 默认配置
     */
    public static HttpRequestConfig newDefault() {
        return new HttpRequestConfig();
    }

    /**
     * 获取连接超时毫秒数
     *
     * @return 连接超时毫秒数
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * 设置连接超时毫秒数
     *
     * @param connectTimeout 连接超时毫秒数
     * @return 当前对象
     */
    public HttpRequestConfig setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * 获取读取超时毫秒数
     *
     * @return 读取超时毫秒数
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * 设置读取超时毫秒数
     *
     * @param readTimeout 读取超时毫秒数
     * @return 当前对象
     */
    public HttpRequestConfig setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * 获取自定义请求头
     *
     * @return 自定义请求头，未设置时为 null
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 设置自定义请求头（整体覆盖）
     *
     * @param headers 自定义请求头
     * @return 当前对象
     */
    public HttpRequestConfig setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * 追加单个自定义请求头，同名覆盖
     *
     * @param name  请求头名称
     * @param value 请求头值
     * @return 当前对象
     */
    public HttpRequestConfig addHeader(String name, String value) {
        if (headers == null) {
            headers = new LinkedHashMap<String, String>();
        }
        headers.put(name, value);
        return this;
    }

    /**
     * 是否自动跟随重定向
     *
     * @return true 自动跟随（JDK 内部限制跳数），false 时 3xx 原样返回
     */
    public boolean isFollowRedirects() {
        return followRedirects;
    }

    /**
     * 设置是否自动跟随重定向
     *
     * @param followRedirects true 自动跟随，false 时 3xx 原样返回
     * @return 当前对象
     */
    public HttpRequestConfig setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    /**
     * 获取请求体字符集
     *
     * @return 请求体字符集
     */
    public String getCharset() {
        return charset;
    }

    /**
     * 设置请求体字符集
     *
     * @param charset 请求体字符集
     * @return 当前对象
     */
    public HttpRequestConfig setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 是否启用 gzip 请求头（Accept-Encoding: gzip, deflate）
     *
     * @return true 默认请求 gzip, deflate
     */
    public boolean isUseGzip() {
        return useGzip;
    }

    /**
     * 设置是否启用 gzip 请求头
     *
     * @param useGzip true 请求 Accept-Encoding: gzip, deflate
     * @return 当前对象
     */
    public HttpRequestConfig setUseGzip(boolean useGzip) {
        this.useGzip = useGzip;
        return this;
    }

    /**
     * 获取 HTTPS 自定义 SSL 工厂，null 表示走系统信任链
     *
     * @return SSL 工厂
     */
    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    /**
     * 设置 HTTPS 自定义 SSL 工厂（自签证书场景），null 表示走系统信任链
     *
     * @param sslSocketFactory SSL 工厂
     * @return 当前对象
     */
    public HttpRequestConfig setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }
}
