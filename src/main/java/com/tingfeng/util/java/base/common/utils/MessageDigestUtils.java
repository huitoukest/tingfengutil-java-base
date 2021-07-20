package com.tingfeng.util.java.base.common.utils;


import com.tingfeng.util.java.base.common.helper.FixedPoolHelper;
import com.tingfeng.util.java.base.common.helper.SimplePoolHelper;
import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionROne;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MessageDigestUtils {
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private static final int DEFAULT_MAX_MESSAGE_DIGEST_SIZE = 16;
    private static final char[] DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    /**
     * 用来缓存MessageDigest的资源
     */
    private static Map<String, FixedPoolHelper<MessageDigest>> messageDigestMap = new HashMap<>();
    @Deprecated
    public enum  SHAType{
        SHA512("SHA-512"),SHAMAC512("HmacSHA512"),SHA256("SHA-256"),SHAMAC256("HmacSHA256"),
        SHA1("SHA-1"),MD5("MD5");
        String value;
        SHAType(String value){
            this.value = value;
        }
        public String getValue(){
            return this.value;
        }
    }
    public enum  DigestType{
        SHA512("SHA-512"),SHAMAC512("HmacSHA512"),SHA256("SHA-256"),SHAMAC256("HmacSHA256"),
        SHA1("SHA-1"),MD5("MD5");
        String value;
        DigestType(String value){
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
    
    /**
     * 返回的MessageDigest是单实例复用的，是线程不安全的,需要手动同步
     * @param type
     * @return
     */
    private static FixedPoolHelper<MessageDigest> getMessageDigest(String type){
        FixedPoolHelper<MessageDigest> simplePoolHelper = messageDigestMap.get(type);
        if(simplePoolHelper == null) {
            synchronized (type.intern()) {
                simplePoolHelper = messageDigestMap.get(type);
                if (null == simplePoolHelper) {
                    simplePoolHelper = new FixedPoolHelper<>(DEFAULT_MAX_MESSAGE_DIGEST_SIZE, () -> MessageDigest.getInstance(type));
                    messageDigestMap.put(type, simplePoolHelper);
                }
            }
        }
        return simplePoolHelper;
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
        FixedPoolHelper<MessageDigest> simplePoolHelper = getMessageDigest(algorithmName);
        return simplePoolHelper.run(digest->{
            digest.reset();
            if (salt != null) {
                digest.update(salt);
            }

            byte[] hashed = digest.digest(bytes);
            int iterations = hashIterations - 1;
            for (int i = 0; i < iterations; ++i) {
                digest.reset();
                hashed = digest.digest(hashed);
            }
            return hashed;
        });
    }

    /**
     * 默认hash 1次数
     * @param algorithmName 加密的格式
     * @param content 加密的内容
     * @param salt salt
     * @return
     */
    public static byte[] hash(String algorithmName,String content, String salt){
        byte[] saltByte = null;
        if(null != salt){
            saltByte = salt.getBytes(Charset.forName("utf-8"));
        }
        return hash(algorithmName,content.getBytes(Charset.forName("utf-8")),saltByte,1);
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
    @Deprecated
    public static byte[] MD5(byte[] plainText) {
        return digest(SHAType.MD5.getValue(),plainText);
    }
    /**
     * 1.消息摘要算法，MD家族，有MD2 MD4 MD5，其中MD4 JDK不支持
     *
     * @param plainText
     * @return
     */
    public static byte[] md5(byte[] plainText) {
        return digest(SHAType.MD5.getValue(),plainText);
    }
    /**
     * 2.SHA Security Hash Algorithm 安全散列算法，固定长度摘要信息 SHA-1 SHA-2( SHA-224
     * SHA-256 SHA-384 SHA-512) 使用的依然是MessageDigest类，JDK不支持224
     *
     * @param plainText
     * @return
     */
    @Deprecated
    public static byte[] SHA(SHAType shaType,byte[] plainText) {
        return digest(shaType.getValue(),plainText);
    }

    /**
     * 2.SHA Security Hash Algorithm 安全散列算法，固定长度摘要信息 SHA-1 SHA-2( SHA-224
     * SHA-256 SHA-384 SHA-512) 使用的依然是MessageDigest类，JDK不支持224
     *
     * @param plainText
     * @return
     */
    public static byte[] sha(SHAType shaType,byte[] plainText) {
        return digest(shaType.getValue(),plainText);
    }

    /**
     * apply to small file
     * @param algorithmName
     * @param plainText
     * @return
     */
    private static byte[] digest(String algorithmName,byte[] plainText){
        FixedPoolHelper<MessageDigest> simplePoolHelper = getMessageDigest(algorithmName);
        return simplePoolHelper.run(digest->{
            digest.reset();
            return digest.digest(plainText);
        });
    }

    /**
     * apply to small file
     * @param shaType
     * @param plainText
     * @param salt
     * @return
     */
    @Deprecated
    public static byte[] SHA(SHAType shaType,byte[] plainText,String salt) {
        return hash(shaType.getValue(),plainText,salt.getBytes(),1);
    }

    /**
     * apply to small file
     * @param shaType
     * @param plainText
     * @param salt
     * @return
     */
    public static byte[] sha(SHAType shaType,byte[] plainText,String salt) {
        return hash(shaType.getValue(),plainText,salt.getBytes(),1);
    }

    /**
     * apply to small file
     * @param shaType
     * @param plainText
     * @param salt
     * @return
     */
    public static byte[] sha(SHAType shaType,String plainText,String salt) {
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
    @Deprecated
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
    public static byte[] macSha(SHAType shaType,byte[] plainText) {

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

    /**
     * apply to small file
     * @param shaType
     * @param plainText
     * @param secretBytes
     * @return
     */
    @Deprecated
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
     * apply to small file
     * @param shaType
     * @param plainText
     * @param secretBytes
     * @return
     */
    public static byte[] macSha(SHAType shaType,byte[] plainText,byte[] secretBytes) {
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
    @Deprecated
    private static SecretKey restoreMACSecretKey(SHAType shaType,byte[] secretBytes) {
        SecretKey key = new SecretKeySpec(secretBytes, shaType.getValue());
        return key;
    }

    /**
     * 还原密钥
     *
     * @param secretBytes
     * @return
     */
    private static SecretKey restoreMacSecretKey(SHAType shaType,byte[] secretBytes) {
        SecretKey key = new SecretKeySpec(secretBytes, shaType.getValue());
        return key;
    }

    /**
     * 流式处理
     * apply to big file, when end won't close the stream
     * @param algorithmName
     * @param contentFiller ,to use  digest.update method fill content
     * @return
     */
    public static byte[] digest(String algorithmName, FunctionROne<byte[],MessageDigest> contentFiller){
        FixedPoolHelper<MessageDigest> simplePoolHelper = getMessageDigest(algorithmName);
        return simplePoolHelper.run(digest->{
            digest.reset();
            contentFiller.run(digest);
            return digest.digest();
        });
    }

    /**
     * 流式处理
     * apply to big file, when end won't close the stream
     * @param algorithmName
     * @param inputStream
     * @return
     */
    public static byte[] digest(String algorithmName, InputStream inputStream){
        FixedPoolHelper<MessageDigest> simplePoolHelper = getMessageDigest(algorithmName);
        return simplePoolHelper.run(digest->{
            digest.reset();
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int length;
            try {
                while ((length = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, length);
                }
            }catch (IOException e){
                throw new com.tingfeng.util.java.base.common.exception.io.IOException (e);
            }
            return digest.digest();
        });
    }

    /**
     * apply to big file, when end won't close the stream
     * @param inputStream
     * @return
     */
    public static byte[] md5(InputStream inputStream){
        return digest(SHAType.MD5.getValue(), inputStream);
    }
}
