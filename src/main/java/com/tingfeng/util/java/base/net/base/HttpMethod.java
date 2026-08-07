package com.tingfeng.util.java.base.net.base;

/**
 * HTTP 请求方法枚举
 *
 * - 仅含 JDK 8 HttpURLConnection.setRequestMethod 白名单支持的方法（GET/POST/HEAD/OPTIONS/PUT/DELETE/TRACE）
 * - PATCH 不在白名单内，直接调用将抛 ProtocolException；需要 PATCH 语义时请使用 POST + X-HTTP-Method-Override 约定
 *
 * @author huitoukest
 */
public enum HttpMethod {

    /** GET 请求，获取资源 */
    GET,
    /** POST 请求，提交资源 */
    POST,
    /** PUT 请求，整体替换资源 */
    PUT,
    /** DELETE 请求，删除资源 */
    DELETE,
    /** HEAD 请求，仅获取响应头 */
    HEAD,
    /** OPTIONS 请求，探测服务端能力 */
    OPTIONS
}
