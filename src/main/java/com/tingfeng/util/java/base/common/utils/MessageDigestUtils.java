package com.tingfeng.util.java.base.common.utils;


import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestUtils {

    private static final char[] DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public enum  SHAType{
        SHA512("SHA-512"),SHAMAC512("HmacSHA512"),SHA256("SHA-256"),SHAMAC256("HmacSHA256"),
        SHA1("SHA-1");
        String value;
        SHAType(String value){
            this.value = value;
        }
        public String getValue(){
            return this.value;
        }
    }

    public static void main(String[] args) {
        System.out.println("MD5: " + toHexString(MD5("i m a sample".getBytes())));
        System.out.println("SHA-512: " + toHexString(SHA(SHAType.SHA256,"i m a sample".getBytes())));
        System.out.println("HmacSHA512：" + toHexString(MACSHA(SHAType.SHAMAC256,"i m a sample".getBytes())));
        System.out.println("HmacSHA512：" + toHexString(MACSHA(SHAType.SHAMAC256,"i m a sample".getBytes(),"123456".getBytes())));
    }

    private static MessageDigest getMessageDigest(String type){
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(type);
            return messageDigest;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param algorithmName 加密的格式
     * @param bytes 加密的内容
     * @param salt salt
     * @param hashIterations hash的次数
     * @return
     */
    public static byte[] hash(String algorithmName,byte[] bytes, byte[] salt, int hashIterations){
        MessageDigest digest = getMessageDigest(algorithmName);
        if (salt != null) {
            digest.reset();
            digest.update(salt);
        }

        byte[] hashed = digest.digest(bytes);
        int iterations = hashIterations - 1;

        for(int i = 0; i < iterations; ++i) {
            digest.reset();
            hashed = digest.digest(hashed);
        }

        return hashed;
    }

    /**
     * 默认hash 1次数
     * @param algorithmName 加密的格式
     * @param content 加密的内容
     * @param salt salt
     * @return
     */
    public static byte[] hash(String algorithmName,String content, String salt){
        return hash(algorithmName,content.getBytes(Charset.forName("utf-8")),salt.getBytes(Charset.forName("utf-8")),1);
    }

    /**
     *
     * @param algorithmName 加密的格式
     * @param bytes 加密的内容
     * @param salt salt
     * @param hashIterations hash的次数
     * @return
     */
    public static String toHashHexString(String algorithmName,byte[] bytes, byte[] salt, int hashIterations){
           byte[] hashBytes = hash(algorithmName,bytes,salt,hashIterations);
           return toHexString(hashBytes);
    }

    /**
     *
     * @param algorithmName 加密的格式
     * @param bytes 加密的内容
     * @param salt salt
     * @return
     */
    public static String toHashHexString(String algorithmName,byte[] bytes, byte[] salt){
        return toHashHexString(algorithmName,bytes,salt);
    }

    /**
     *
     * @param algorithmName 加密的格式
     * @param content 加密的内容
     * @param salt salt
     * @return
     */
    public static String toHashHexString(String algorithmName,String content, String salt){
        return toHashHexString(algorithmName,content.getBytes(Charset.forName("utf-8")),salt.getBytes(Charset.forName("utf-8")));
    }

    /**
     * 转义为16进制字符串
     * @return
     */
    public static String toHexString(byte[] bytes) {
        char[] encodedChars = toHex(bytes);
        return new String(encodedChars);
    }

    /**
     * 摘录自shiro中的转16精制的算法
     * @param data
     * @return
     */
    public static char[] toHex(byte[] data) {
        int l = data.length;
        char[] out = new char[l << 1];
        int i = 0;

        for(int var4 = 0; i < l; ++i) {
            out[var4++] = DIGITS[(240 & data[i]) >>> 4];
            out[var4++] = DIGITS[15 & data[i]];
        }

        return out;
    }
    /**
     * 1.消息摘要算法，MD家族，有MD2 MD4 MD5，其中MD4 JDK不支持
     *
     * @param plainText
     * @return
     */
    public static byte[] MD5(byte[] plainText) {
        MessageDigest messageDigest = getMessageDigest("md5");
        return messageDigest.digest(plainText);
    }

    /**
     * 2.SHA Security Hash Algorithm 安全散列算法，固定长度摘要信息 SHA-1 SHA-2( SHA-224
     * SHA-256 SHA-384 SHA-512) 使用的依然是MessageDigest类，JDK不支持224
     *
     * @param plainText
     * @return
     */
    public static byte[] SHA(SHAType shaType,byte[] plainText) {
        MessageDigest messageDigest = getMessageDigest(shaType.getValue());
        return messageDigest.digest(plainText);
    }

    public static byte[] SHA(SHAType shaType,byte[] plainText,String salt) {
        return hash(shaType.getValue(),plainText,salt.getBytes(),1);
    }

    public static byte[] SHA(SHAType shaType,String plainText,String salt) {
        return hash(shaType.getValue(),plainText.getBytes(),salt.getBytes(),1);
    }

    /**
     * 3.MAC(Message Authentication Code) 消息认证码算法，是含有密钥散列函数算法。
     * 兼容了MD和SHA的特性。
     * 加密过程三步走，与后面要介绍的对称加密和非对称加密是相似的
     * 1) 传入算法，实例化一个加密器
     * 2) 传入密钥，初始化加密器
     * 3) 调用doFinal方法进行加密
     * @param plainText
     * @return
     */
    public static byte[] MACSHA(SHAType shaType,byte[] plainText) {

        try {
            byte[] secretBytes = generatorMACSecretKey(shaType);
            SecretKey key = restoreMACSecretKey(shaType,secretBytes);
            Mac mac = Mac.getInstance(shaType.getValue());
            mac.init(key);
            return mac.doFinal(plainText);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }


    public static byte[] MACSHA(SHAType shaType,byte[] plainText,byte[] secretBytes) {
        try {
            SecretKey key = restoreMACSecretKey(shaType,secretBytes);
            Mac mac = Mac.getInstance(shaType.getValue());
            mac.init(key);
            return mac.doFinal(plainText);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * MAC生成随机密钥 两步走 1.创建一个KeyGenerator 2.调用KeyGenerator.generateKey方法
     *
     * @return
     */
    private static byte[] generatorMACSecretKey(SHAType shaType) {
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(shaType.getValue());
            SecretKey key = keyGenerator.generateKey();
            return key.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 还原密钥
     *
     * @param secretBytes
     * @return
     */
    private static SecretKey restoreMACSecretKey(SHAType shaType,byte[] secretBytes) {
        SecretKey key = new SecretKeySpec(secretBytes, shaType.getValue());
        return key;
    }
}
