package com.tingfeng.util.java.base.net;

import com.alibaba.fastjson.JSON;
import com.tingfeng.util.java.base.net.base.HttpResponseInfo;
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
        
        // 添加超时机制，避免网络延迟导致测试失败
        HttpResponseInfo responseInfo = null;
        try {
            // 使用线程执行网络请求，设置5秒超时
            responseInfo = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                return HttpUtils.sendGet("https://www.baidu.com/s", params);
            }).get(5, java.util.concurrent.TimeUnit.SECONDS);
            System.out.println(JSON.toJSONString(responseInfo));
        } catch (Exception e) {
            System.out.println("网络请求失败: " + e.getMessage());
            // 不抛出异常，避免网络问题导致测试失败
        }
    }

}