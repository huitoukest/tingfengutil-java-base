package com.tingfeng.util.java.base.net;

import com.tingfeng.util.java.base.common.constant.Constants;
import com.tingfeng.util.java.base.lang.StringUtils;
import com.tingfeng.util.java.base.net.base.HttpConnectionExecutor;
import com.tingfeng.util.java.base.net.base.HttpMethod;
import com.tingfeng.util.java.base.net.base.HttpRequestConfig;
import com.tingfeng.util.java.base.net.base.HttpResponseInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 请求工具门面（静态方法模式，统一委托 {@link HttpConnectionExecutor} 执行）
 *
 * 全部请求 API 返回标准响应对象 {@link HttpResponseInfo}，不抛传输异常：
 * - status = 0 表示传输失败（超时/DNS/IO/协议错误），此时 bodyBytes 为 null
 * - status &gt; 0 为服务端返回的真实状态码，2xx 可经 {@link HttpResponseInfo#isSuccess()} 判断
 *
 * 默认配置（可经 {@link HttpRequestConfig} 覆盖）：连接超时 10s / 读取超时 60s、自动跟随重定向、
 * gzip 请求头、UTF-8 字符集；null URL 等参数错误抛 IllegalArgumentException（编程错误快速失败）。
 *
 * @author huitoukest
 */
public final class HttpUtils {

    private HttpUtils() {
        // 私有构造器，禁止实例化
    }

    /**
     * 向指定 URL 发送 GET 请求
     *
     * @param url 请求地址，null/空串抛 IllegalArgumentException
     * @return 标准响应对象，传输失败时 status=0
     */
    public static HttpResponseInfo sendGet(String url) {
        return HttpConnectionExecutor.execute(url, HttpMethod.GET, null, null);
    }

    /**
     * 向指定 URL 发送 GET 请求，参数以 name=value 形式拼接为 query（不进行 URL 编码）
     *
     * @param url    请求地址，null/空串抛 IllegalArgumentException
     * @param params 请求参数，值为 null 时转为空串
     * @return 标准响应对象，传输失败时 status=0
     */
    public static HttpResponseInfo sendGet(String url, Map<String, Object> params) {
        return sendGet(toGetUrl(url, params));
    }

    /**
     * 向指定 URL 发送 GET 请求，参数以 name=value 形式拼接为 query（不进行 URL 编码）
     *
     * @param url    请求地址，null/空串抛 IllegalArgumentException
     * @param params 请求参数，值为 null 时转为空串
     * @param config 请求配置，null 时使用默认配置（超时/重定向/gzip/自定义头）
     * @return 标准响应对象，传输失败时 status=0
     */
    public static HttpResponseInfo sendGet(String url, Map<String, Object> params, HttpRequestConfig config) {
        return HttpConnectionExecutor.execute(toGetUrl(url, params), HttpMethod.GET, null, config);
    }

    /**
     * 向指定 URL 发送 POST 请求，请求体按 UTF-8 编码
     *
     * @param url         请求地址，null/空串抛 IllegalArgumentException
     * @param body        请求体字符串，null 时发送空请求体
     * @param contentType Content-Type 请求头（如 application/json），null/空串时使用默认 text/plain
     * @return 标准响应对象，传输失败时 status=0
     */
    public static HttpResponseInfo sendPost(String url, String body, String contentType) {
        HttpRequestConfig config = HttpRequestConfig.newDefault();
        if (contentType != null && contentType.length() > 0) {
            config.addHeader("Content-Type", contentType);
        }
        return sendRequest(url, HttpMethod.POST, utf8Bytes(body), config);
    }

    /**
     * 向指定 URL 发送表单 POST 请求，参数按 application/x-www-form-urlencoded 编码（UTF-8）
     *
     * @param url        请求地址，null/空串抛 IllegalArgumentException
     * @param formParams 表单参数，值为 null 时转为空串
     * @return 标准响应对象，传输失败时 status=0
     */
    public static HttpResponseInfo sendPost(String url, Map<String, Object> formParams) {
        String body = toGetUrl(null, formParams, true, null);
        return sendPost(url, body, "application/x-www-form-urlencoded; charset=" + Constants.CharSet.UTF8);
    }

    /**
     * 向指定 URL 发送 JSON POST 请求（Content-Type: application/json; charset=UTF-8）
     *
     * @param url 请求地址，null/空串抛 IllegalArgumentException
     * @param json JSON 请求体，null 时发送空请求体
     * @return 标准响应对象，传输失败时 status=0
     */
    public static HttpResponseInfo sendPostJson(String url, String json) {
        return sendPost(url, json, "application/json; charset=" + Constants.CharSet.UTF8);
    }

    /**
     * 统一请求入口，支持 PUT/DELETE/HEAD/OPTIONS 等方法（PATCH 不支持，JDK 8 白名单限制，
     * 需要 PATCH 语义时请使用 POST + X-HTTP-Method-Override 约定）
     *
     * @param url    请求地址，null/空串抛 IllegalArgumentException
     * @param method 请求方法，null 抛 IllegalArgumentException
     * @param body   请求体字节，仅 POST/PUT/DELETE 且非 null 时写入；GET/HEAD/OPTIONS 忽略
     * @param config 请求配置，null 时使用默认配置
     * @return 标准响应对象，传输失败时 status=0
     */
    public static HttpResponseInfo sendRequest(String url, HttpMethod method, byte[] body, HttpRequestConfig config) {
        return HttpConnectionExecutor.execute(url, method, body, config);
    }

    /**
     * 把 名=值 参数表转换成字符串（url + ?a=1&amp;b=2）
     * 1. 存在但是只为 null 时，值转为空串
     * 2. 如果 url 已带参数（含 ?），则追加参数到 url 上
     * 3. url 为 null 时返回参数组成的字符串 a=1&amp;b=2（不带分隔符）
     *
     * @param url    url 可以为 null，为 null 则返回参数组成的字符串 a=1&amp;b=2
     * @param params url 中的参数
     * @return 拼接后的 URL
     */
    public static String toGetUrl(String url, Map<String, ? extends Object> params) {
        return toGetUrl(url, params, false, null);
    }

    /**
     * 把 名=值 参数表转换成字符串（url + ?a=1&amp;b=2）
     * 1. 存在但是只为 null 时，值转为空串
     * 2. 如果 url 已带参数（含 ?），则追加参数到 url 上
     * 3. url 为 null 时返回参数组成的字符串 a=1&amp;b=2（不带分隔符）
     *
     * @param url          url 可以为 null，为 null 则返回参数组成的字符串 a=1&amp;b=2
     * @param params       url 中的参数
     * @param encodeParam  对参数的键值做 URL 编码
     * @param encodeCharSet 编码格式，仅当 encodeParam = true 时生效；null 时默认 UTF-8，非法字符集抛 IllegalArgumentException
     * @return 拼接后的 URL
     */
    public static String toGetUrl(String url, Map<String, ? extends Object> params, boolean encodeParam, String encodeCharSet) {
        if (encodeParam) {
            // 提前校验字符集：doAppend 池内抛出的异常会被包装，必须在 lambda 外快速失败（非法字符集抛 IllegalArgumentException）
            Charset.forName(null == encodeCharSet ? Constants.CharSet.UTF8 : encodeCharSet);
        }
        boolean urlHasParam = url != null && url.indexOf('?') >= 0;
        return StringUtils.doAppend(sb -> {
            if (null != url) {
                sb.append(url);
            }
            if (null != params && !params.isEmpty()) {
                int i = 0;
                for (String key : params.keySet()) {
                    Object value = params.get(key);
                    if (i > 0 || urlHasParam) {
                        sb.append("&");
                    } else {
                        if (null != url) {
                            sb.append("?");
                        }
                    }
                    if (encodeParam) {
                        key = encode(key, encodeCharSet);
                        if (null != value) {
                            value = encode(value.toString(), encodeCharSet);
                        }
                    }
                    sb.append(key);
                    sb.append("=");
                    sb.append(null == value ? "" : value);
                    ++i;
                }
            }
            return sb.toString();
        });
    }

    /**
     * 解析字符串返回 名称=值的参数表（a=1 &amp; b=2 to {a=1, b=2}）
     * <p>
     * 单次扫描 + StringBuilder 实现，时间复杂度 O(n)。
     * 注意：返回值不进行 URL 解码（与 {@link #toGetUrl(String, Map, boolean, String)} 的编码不对称是有意设计，
     * 避免双重解码；需要原始值时请自行 URLDecoder 解码）。
     *
     * @param str 参数字符串，null 或空串返回空 Map
     * @return 参数表，无法解析出任何键值对时返回空 Map
     */
    public static HashMap<String, String> parseGetParams(String str) {
        HashMap<String, String> result = new HashMap<String, String>();
        if (str == null || str.length() == 0 || str.indexOf('=') < 0) {
            return result;
        }
        StringBuilder name = new StringBuilder();
        StringBuilder value = new StringBuilder();
        boolean hasValue = false;
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if (c == '=') {
                hasValue = true;
                value.setLength(0);
            } else if (c == '&') {
                if (name.length() > 0 && hasValue) {
                    result.put(name.toString(), value.toString());
                }
                name.setLength(0);
                value.setLength(0);
                hasValue = false;
            } else if (hasValue) {
                value.append(c);
            } else {
                name.append(c);
            }
        }
        if (name.length() > 0 && hasValue) {
            result.put(name.toString(), value.toString());
        }
        return result;
    }

    /**
     * 得到处理后的可用 url，如果 relativeUrl 是 http 或 https 开头的连接
     * 那么不处理，否则返回 urlPrefix + relativeUrl.trim() 的值
     *
     * @param urlPrefix    url 前缀
     * @param relativeUrl  相对地址
     * @return 处理后的 url；relativeUrl 为空返回空串，urlPrefix 为空返回相对地址本身
     */
    public static String getFileUrl(String urlPrefix, String relativeUrl) {
        if (StringUtils.isEmpty(relativeUrl)) {
            return "";
        }
        if (StringUtils.isEmpty(urlPrefix)) {
            return relativeUrl;
        }
        relativeUrl = relativeUrl.trim();
        if (relativeUrl.startsWith(Constants.HttpConfig.KEY_HTTPS) || relativeUrl.startsWith(Constants.HttpConfig.KEY_HTTP)) {
            return relativeUrl;
        }
        return urlPrefix + relativeUrl;
    }

    /**
     * URL 编码（null 字符集默认 UTF-8，非法字符集抛 IllegalArgumentException）
     */
    private static String encode(String value, String encodeCharSet) {
        try {
            return URLEncoder.encode(value, null == encodeCharSet ? Constants.CharSet.UTF8 : encodeCharSet);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("unsupported charset: " + encodeCharSet, e);
        }
    }

    /**
     * 字符串转 UTF-8 字节（null 原样返回）
     */
    private static byte[] utf8Bytes(String value) {
        return null == value ? null : value.getBytes(StandardCharsets.UTF_8);
    }
}
