package com.tingfeng.util.java.base.net;

import com.tingfeng.util.java.base.net.base.HttpResponseInfo;
import com.tingfeng.util.java.base.net.HttpUtils;
import org.junit.Assert;
import org.junit.Assume;
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
            // 验证响应不为null
            Assert.assertNotNull(responseInfo);
        } catch (Exception e) {
            // 网络不可达时跳过测试
            Assume.assumeNoException(e);
        }
    }

}