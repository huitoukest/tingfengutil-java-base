package com.tingfeng.util.java.base.text;

import com.alibaba.fastjson.JSON;
import com.tingfeng.util.java.base.lang.exception.InfoException;
import com.tingfeng.util.java.base.crypto.MessageDigestUtils;
import com.tingfeng.util.java.base.text.TokenHelper;
import com.tingfeng.util.java.base.common.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TokenHelperTest {

    static TokenHelper tokenHelper = new TokenHelper();

@Test
    public void test(){
        ArrayList<String> list = new ArrayList<>();

        list.add("\\,");
        list.add("\\");
        list.add("123456");
        list.add("\\,");
        this.test(false,list);
        list.clear();

        list.add("\\");
        list.add("\\");
        list.add("123456");
        list.add("\\");
        this.test(false,list);
        list.clear();

        list.add("");
        list.add("\\");
        list.add("123456");
        list.add("\\,");
        list.add("");
        list.add("");
        list.add("");
        this.test(false,list);
        list.clear();
        //分别存入空串，用户id，过期时间，token类型，空串
        list.add("");
        list.add("");
        list.add("123456");
        list.add("\\,");
        list.add("123456");
        list.add(String.valueOf(System.currentTimeMillis()));
        list.add("2");
        list.add(",,5,6,7,,");
        list.add("");
        list.add("\\");
        list.add("");

        this.test(false,list);
    }

    private void test(boolean  isPrint){
        ArrayList<String> list = new ArrayList<>();

        list.add("\\,");
        list.add("\\");
        list.add("123456");
        list.add("\\,");
        test(isPrint,list);
        list.clear();

        list.add("\\");
        list.add("\\");
        list.add("123456");
        list.add("\\");
        test(isPrint,list);
        list.clear();

        list.add("");
        list.add("\\");
        list.add("123456");
        list.add("\\,");
        list.add("");
        list.add("");
        list.add("");
        test(isPrint,list);
        list.clear();
        //分别存入空串，用户id，过期时间，token类型，空串
        list.add("");
        list.add("");
        list.add("123456");
        list.add("\\,");
        list.add("123456");
        list.add(String.valueOf(System.currentTimeMillis()));
        list.add("2");
        list.add(",,5,6,7,,");
        list.add("");
        list.add("\\");
        list.add("");

        test(false,list);
    }

    public void test(boolean isPrint,ArrayList<String> list) {
        String token  = tokenHelper.getToken(list,"123456");
        List<String> obj = tokenHelper.parseToken(token, (contents) -> contents.get(2) ,(contents) -> contents);
        Assert.assertEquals(JSON.toJSONString(obj),JSON.toJSONString(list));
    }

    /**
     * 测试签名验证
     */
    @Test
    public void testSignatureCheck() {
        ArrayList<String> list = new ArrayList<>();
        list.add("123");
        list.add("456");
        list.add("789");

        String securityKey = "testKey";
        String token = tokenHelper.getToken(list, securityKey);

        // 测试正确的签名验证
        String sign = tokenHelper.checkSignature(token, securityKey);
        Assert.assertNotNull(sign);

        // 测试错误的签名验证
        try {
            tokenHelper.checkSignature(token, "wrongKey");
            Assert.fail("应该抛出异常");
        } catch (InfoException e) {
            // 预期异常
        }
    }

    /**
     * 测试内容长度验证
     */
    @Test
    public void testContentLengthCheck() {
        ArrayList<String> list = new ArrayList<>();
        list.add("123");
        list.add("456");
        list.add("789");

        String securityKey = "testKey";
        String token = tokenHelper.getToken(list, securityKey);

        // 设置内容长度为3，应该验证通过
        tokenHelper.setContentLength(3);
        List<String> result = tokenHelper.parseTokenWithNoSignatureCheck(token);
        Assert.assertEquals(3, result.size());

        // 设置内容长度为4，应该验证失败
        tokenHelper.setContentLength(4);
        try {
            tokenHelper.parseTokenWithNoSignatureCheck(token);
            Assert.fail("应该抛出异常");
        } catch (InfoException e) {
            // 预期异常
        }

        // 重置内容长度
        tokenHelper.setContentLength(null);
    }

    /**
     * 测试异常处理
     */
    @Test
    public void testExceptionHandling() {
        // 测试空token
        try {
            tokenHelper.parseTokenWithNoSignatureCheck("");
            Assert.fail("应该抛出异常");
        } catch (InfoException e) {
            // 预期异常
        }

        // 测试格式错误的token
        try {
            tokenHelper.parseTokenWithNoSignatureCheck("invalidToken");
            Assert.fail("应该抛出异常");
        } catch (InfoException e) {
            // 预期异常
        }
    }

    /**
     * 测试不同的加密算法
     */
    @Test
    public void testDifferentEncryptionAlgorithms() {
        ArrayList<String> list = new ArrayList<>();
        list.add("123");
        list.add("456");

        String securityKey = "testKey";

        // 测试SHA-256
        TokenHelper sha256Helper = new TokenHelper(MessageDigestUtils.DigestType.SHA256);
        String token256 = sha256Helper.getToken(list, securityKey);
        List<String> result256 = sha256Helper.parseToken(token256, (contents) -> securityKey, (contents) -> contents);
        Assert.assertEquals(list, result256);

        // 测试SHA-512
        TokenHelper sha512Helper = new TokenHelper(MessageDigestUtils.DigestType.SHA512);
        String token512 = sha512Helper.getToken(list, securityKey);
        List<String> result512 = sha512Helper.parseToken(token512, (contents) -> securityKey, (contents) -> contents);
        Assert.assertEquals(list, result512);

        // 测试MD5
        TokenHelper md5Helper = new TokenHelper("MD5");
        String tokenMd5 = md5Helper.getToken(list, securityKey);
        List<String> resultMd5 = md5Helper.parseToken(tokenMd5, (contents) -> securityKey, (contents) -> contents);
        Assert.assertEquals(list, resultMd5);
    }

    /**
     * 测试速度 - 保持多线程但减少循环次数到10w
     */
    @Test
    public void testSpeed(){
        ArrayList<String> list = new ArrayList<>();
        list.add("\\,");
        list.add("\\");
        list.add("123456");
        list.add("\\,");
        long startTime = System.currentTimeMillis();
        for(int i = 0;i < 100000 ;i++){
            test(false,list);
        }
        long duration = System.currentTimeMillis() - startTime;
        Assert.assertTrue("执行时间应该在合理范围内", duration < 30000);
    }

    /**
     * 测试速度 - 保持5线程但减少循环次数到10w
     */
    @Test
    public void testSpeed2() throws InterruptedException {
        ArrayList<String> list = new ArrayList<>();
        list.add("\\,");
        list.add("\\");
        list.add("123456");
        list.add("\\,");
        TestUtils.printTime(5,100000,integer -> {
            test(false,list);
        });
    }
}