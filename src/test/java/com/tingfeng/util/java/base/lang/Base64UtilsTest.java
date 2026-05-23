package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.common.constant.Constants;
import com.tingfeng.util.java.base.common.utils.TestUtils;
import com.tingfeng.util.java.base.crypto.MessageDigestUtils;
import com.tingfeng.util.java.base.math.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;

/**
 * @Author huitoukest
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
     * 2s , 4 * 4 百万次 - 减少到 4 * 1000
     */
    @Test
    public void urlSafeUrlBatchTest(){
        TestUtils.printTime(4,1000,(th,integer) -> {
            urlSafeUrlTest();
        });
    }

    @Test
    public void urlSafeUrlUseTest(){
        // SHA256 编码结果
        String shaResult = Base64Utils.enCodeBase64UrlSafeString(MessageDigestUtils.sha(MessageDigestUtils.DigestType.SHA256,"1577934671000,2s5d4c1d0d3g6x91234".getBytes(Charset.forName(Constants.CharSet.UTF8))));
        Assert.assertNotNull(shaResult);
        Assert.assertTrue(shaResult.length() > 0);

        // 字符串编码并验证 round-trip
        String original = "1577934671000,2s5d4c1d0d3g6x9";
        String encoded = Base64Utils.enCodeBase64UrlSafeString(original);
        Assert.assertNotNull(encoded);
        Assert.assertEquals(original, Base64Utils.deCodeBase64UrlSafeString(encoded));

        // 解码验证
        String decoded = Base64Utils.deCodeBase64UrlSafeString("MTU3NzkzNDY3MTAwMA==");
        Assert.assertEquals("1577934671000", decoded);
    }
}
