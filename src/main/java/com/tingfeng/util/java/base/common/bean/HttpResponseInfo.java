package com.tingfeng.util.java.base.common.bean;


import java.util.List;
import java.util.Map;

/**
 * Http的相应信息
 */
public class HttpResponseInfo {

    private Integer status;
    private String body;
    private Map<String, List<String>> headers;

    public Integer getStatus() {
        return status;
    }

    public HttpResponseInfo setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public String getBody() {
        return body;
    }

    public HttpResponseInfo setBody(String body) {
        this.body = body;
        return this;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public HttpResponseInfo setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
        return this;
    }
}
