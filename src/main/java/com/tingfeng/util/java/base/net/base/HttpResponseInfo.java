package com.tingfeng.util.java.base.net.base;

import com.tingfeng.util.java.base.common.constant.Constants;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * HTTP 响应信息（标准响应对象）
 *
 * - status = 0 表示传输失败，未获得有效 HTTP 响应（如超时/DNS/IO 错误），此时 bodyBytes 为 null
 * - status > 0 时为服务端返回的真实状态码
 * - 响应体以原始字节保存（bodyBytes），按需通过 getBodyAsString 解码，二进制数据无损
 *
 * @author huitoukest
 */
public class HttpResponseInfo {

    private int status;
    private Map<String, List<String>> headers;
    private byte[] bodyBytes;

    /**
     * 默认构造，status 为 0（传输失败语义），bodyBytes/headers 为 null
     */
    public HttpResponseInfo() {
    }

    /**
     * 全参构造
     *
     * @param status   状态码，0 表示传输失败未获得 HTTP 响应
     * @param headers  响应头
     * @param bodyBytes 响应体原始字节
     */
    public HttpResponseInfo(int status, Map<String, List<String>> headers, byte[] bodyBytes) {
        this.status = status;
        this.headers = headers;
        this.bodyBytes = bodyBytes;
    }

    /**
     * 获取响应状态码，0 表示传输失败未获得 HTTP 响应
     *
     * @return 状态码
     */
    public int getStatus() {
        return status;
    }

    /**
     * 设置响应状态码，0 表示传输失败未获得 HTTP 响应
     *
     * @param status 状态码
     * @return 当前对象
     */
    public HttpResponseInfo setStatus(int status) {
        this.status = status;
        return this;
    }

    /**
     * 获取全部响应头，key 为头名（null 表示状态行），value 为同名头的值列表
     *
     * @return 响应头
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * 设置响应头
     *
     * @param headers 响应头
     * @return 当前对象
     */
    public HttpResponseInfo setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * 获取响应体原始字节，传输失败时为 null
     *
     * @return 响应体字节
     */
    public byte[] getBodyBytes() {
        return bodyBytes;
    }

    /**
     * 设置响应体原始字节
     *
     * @param bodyBytes 响应体字节
     * @return 当前对象
     */
    public HttpResponseInfo setBodyBytes(byte[] bodyBytes) {
        this.bodyBytes = bodyBytes;
        return this;
    }

    /**
     * 以 UTF-8 解码响应体为字符串，响应体为 null 时返回 null
     *
     * @return 解码后的字符串
     */
    public String getBodyAsString() {
        return getBodyAsString(Constants.CharSet.UTF8);
    }

    /**
     * 以指定字符集解码响应体为字符串，响应体为 null 时返回 null
     *
     * @param charset 字符集名称，null 或非法字符集抛 IllegalArgumentException（编程错误快速失败）
     * @return 解码后的字符串
     */
    public String getBodyAsString(String charset) {
        if (bodyBytes == null) {
            return null;
        }
        if (charset == null) {
            throw new IllegalArgumentException("charset must not be null");
        }
        return new String(bodyBytes, Charset.forName(charset));
    }

    /**
     * 是否为成功响应（状态码 2xx 区间）
     *
     * @return 2xx 返回 true，否则 false（含 status=0 传输失败）
     */
    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }

    @Override
    public String toString() {
        return "HttpResponseInfo{status=" + status
                + ", headers=" + headers
                + ", bodyBytes.length=" + (bodyBytes == null ? 0 : bodyBytes.length) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HttpResponseInfo that = (HttpResponseInfo) o;
        return status == that.status
                && Objects.equals(headers, that.headers)
                && Arrays.equals(bodyBytes, that.bodyBytes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(status, headers);
        result = 31 * result + Arrays.hashCode(bodyBytes);
        return result;
    }
}
