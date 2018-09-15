package com.tingfeng.util.java.base.common.bean;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Http的相应信息
 */
@Data
public class HttpResponseInfo {

    private Integer status;
    private String body;
    private Map<String, List<String>> headers;
}
