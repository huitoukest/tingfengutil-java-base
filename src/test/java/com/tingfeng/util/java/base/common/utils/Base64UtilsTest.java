package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @Author wangGang
 * @Description //TODO
 * @Date 2019-06-05 14:01
 **/
public class Base64UtilsTest {

    @Test
    public void urlSafeUrlTest(){
        String content = RandomUtils.randomLetterString(RandomUtils.randomInt(15,100));
        String base64SafeUrl = Base64Utils.enCodeBase64UrlSafeString(content);
        Assert.assertTrue(base64SafeUrl.equals(Base64Utils.encodeBase64ToBase64UrlSafeString(Base64Utils.enCode(content))));

        String deCodeContent = Base64Utils.deCodeBase64UrlSafeString(base64SafeUrl);
        Assert.assertTrue(deCodeContent.equals(content));

        deCodeContent = Base64Utils.deCodeToString(base64SafeUrl);
        Assert.assertTrue(deCodeContent.equals(content));

        deCodeContent = Base64Utils.deCodeToString(Base64Utils.decodeBase64SafUrlStringToBase64(base64SafeUrl));
        Assert.assertTrue(deCodeContent.equals(content));

        base64SafeUrl = Base64Utils.enCode(content);
        deCodeContent = Base64Utils.deCodeBase64UrlSafeString(base64SafeUrl);
        Assert.assertTrue(deCodeContent.equals(content));
    }

    /**
     * 2s , 4 * 4 百万次
     */
    @Test
    public void urlSafeUrlBatchTest(){
        TestUtils.printTime(4,100000,(th,integer) -> {
            urlSafeUrlTest();
            if(integer % 50000 == 0){
                System.out.println("th:" + th + ",urlSafeUrlBatchTest:" + integer);
            }
        });
    }

}