package com.tingfeng.util.java.base.common.utils;

import com.alibaba.fastjson.JSON;
import com.tingfeng.util.java.base.common.bean.HttpResponseInfo;
import com.tingfeng.util.java.base.web.http.HttpUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Http工具类测试
 */
public class HttpUtilsTest {

    @Test
    public void testSendGet(){
        //https://www.baidu.com/s?wd=1&rsv_spt=1
        Map<String,Object> params = new HashMap<>();
        params.put("wd","1");
        params.put("rsv_spt",1);
        HttpResponseInfo responseInfo = HttpUtils.sendGet("https://www.baidu.com/s",params);
        System.out.println(JSON.toJSONString(responseInfo));
    }

}

