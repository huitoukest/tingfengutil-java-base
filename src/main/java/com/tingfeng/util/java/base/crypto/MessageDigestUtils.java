package com.tingfeng.util.java.base.common.utils;


import com.tingfeng.util.java.base.common.helper.FixedPoolHelper;
import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionROne;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息摘要与MAC加密工具类
 * 支持MD5、SHA系列算法的字节数组和字符串摘要计算
 * 支持HMAC系列算法的消息认证码计算
 */
public class MessageDigestUtils {
    /** 流式处理默认缓冲区大小（4KB） */
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    /** MessageDigest池最大容量 */
    private static final int DEFAULT_MAX_MESSAGE_DIGEST_SIZE = 16;
    /** Mac池最大容量 */
    private static final int DEFAULT_MAX_MAC_SIZE = 16;
    /** KeyGenerator池最大容量 */
    private static final int DEFAULT_MAX_KEY_GENERATOR_SIZE = 8;
    /** 十六进制字符映射表 */
    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    /** MessageDigest实例池缓存 */
    private static final Map<String, FixedPoolHelper<MessageDigest>> MESSAGE_DIGEST_POOL = new HashMap<>();
    /** Mac实例池缓存 */
    private static final Map<String, FixedPoolHelper<Mac>> MAC_POOL = new HashMap<>();
    /** KeyGenerator实例池缓存 */
    private static final Map<String, FixedPoolHelper<KeyGenerator>> KEY_GENERATOR_POOL = new HashMap<>();

    /**
     * 摘要算法类型枚举
     */
    public enum DigestType {
        SHA512("SHA-512"),
        SHAMAC512("HmacSHA512"),
        SHA256("SHA-256"),
        SHAMAC256("HmacSHA256"),
        SHA1("SHA-1"),
        MD5("MD5");

        private final String value;

        DigestType(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public static void main(String[] args) {
        System.out.println("MD5: " + toHexString(md5("i m a sample".getBytes())));
        System.out.println("SHA-256: " + toHexString(sha(DigestType.SHA256, "i m a sample".getBytes())));
        System.out.println("HmacSHA256：" + toHexString(macSha(DigestType.SHAMAC256, "i m a sample".getBytes())));
        System.out.println("HmacSHA256：" + toHexString(macSha(DigestType.SHAMAC256, "i m a sample".getBytes(), "123456".getBytes(StandardCharsets.UTF_8))));
    }

    /**
     * 获取MessageDigest实例池（线程不安全，需外部同步）
     * @param algorithm 算法名称，如"SHA-256"、"MD5"等
     * @return MessageDigest实例池
     */
    private static FixedPoolHelper<MessageDigest> getMessageDigestPool(String algorithm) {
        FixedPoolHelper<MessageDigest> pool = MESSAGE_DIGEST_POOL.get(algorithm);
        if (pool == null) {
            synchronized (algorithm.intern()) {
                pool = MESSAGE_DIGEST_POOL.get(algorithm);
                if (null == pool) {
                    pool = new FixedPoolHelper<>(DEFAULT_MAX_MESSAGE_DIGEST_SIZE,
                            () -> MessageDigest.getInstance(algorithm));
                    MESSAGE_DIGEST_POOL.put(algorithm, pool);
                }
            }
        }
        return pool;
    }

    /**
     * 获取Mac实例池（线程不安全，需外部同步）
     * @param algorithm 算法名称，如"HmacSHA256"等
     * @return Mac实例池
     */
    private static FixedPoolHelper<Mac> getMacPool(String algorithm) {
        FixedPoolHelper<Mac> pool = MAC_POOL.get(algorithm);
        if (pool == null) {
            synchronized (algorithm.intern()) {
                pool = MAC_POOL.get(algorithm);
                if (null == pool) {
                    pool = new FixedPoolHelper<>(DEFAULT_MAX_MAC_SIZE,
                            () -> Mac.getInstance(algorithm));
                    MAC_POOL.put(algorithm, pool);
                }
            }
        }
        return pool;
    }

    /**
     * 获取KeyGenerator实例池（线程不安全，需外部同步）
     * @param algorithm 算法名称，如"HmacSHA256"等
     * @return KeyGenerator实例池
     */
    private static FixedPoolHelper<KeyGenerator> getKeyGeneratorPool(String algorithm) {
        FixedPoolHelper<KeyGenerator> pool = KEY_GENERATOR_POOL.get(algorithm);
        if (pool == null) {
            synchronized (algorithm.intern()) {
                pool = KEY_GENERATOR_POOL.get(algorithm);
                if (null == pool) {
                    pool = new FixedPoolHelper<>(DEFAULT_MAX_KEY_GENERATOR_SIZE,
                            () -> KeyGenerator.getInstance(algorithm));
                    KEY_GENERATOR_POOL.put(algorithm, pool);
                }
            }
        }
        return pool;
    }

    /**
     * 多轮迭代哈希计算
     * @param algorithmName 算法名称
     * @param bytes 待哈希的字节数组
     * @param salt 盐值（可选）
     * @param hashIterations 迭代次数
     * @return 哈希后的字节数组
     */
    public static byte[] hash(String algorithmName, byte[] bytes, byte[] salt, int hashIterations) {
        FixedPoolHelper<MessageDigest> pool = getMessageDigestPool(algorithmName);
        return pool.run(digest -> {
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
     * 默认单轮迭代哈希计算
     * @param algorithmName 算法名称
     * @param content 待哈希的字符串内容
     * @param salt 盐值字符串（可选）
     * @return 哈希后的字节数组
     */
    public static byte[] hash(String algorithmName, String content, String salt) {
        byte[] saltBytes = null;
        if (null != salt) {
            saltBytes = salt.getBytes(StandardCharsets.UTF_8);
        }
        return hash(algorithmName, content.getBytes(StandardCharsets.UTF_8), saltBytes, 1);
    }

    /**
     * 多轮迭代哈希计算并转换为十六进制字符串
     * @param algorithmName 算法名称
     * @param bytes 待哈希的字节数组
     * @param salt 盐值（可选）
     * @param hashIterations 迭代次数
     * @return 十六进制字符串
     */
    public static String toHashHexString(String algorithmName, byte[] bytes, byte[] salt, int hashIterations) {
        byte[] hashBytes = hash(algorithmName, bytes, salt, hashIterations);
        return toHexString(hashBytes);
    }

    /**
     * 单轮迭代哈希计算并转换为十六进制字符串
     * @param algorithmName 算法名称
     * @param bytes 待哈希的字节数组
     * @param salt 盐值（可选）
     * @return 十六进制字符串
     */
    public static String toHashHexString(String algorithmName, byte[] bytes, byte[] salt) {
        return toHashHexString(algorithmName, bytes, salt, 1);
    }

    /**
     * 单轮迭代哈希计算并转换为十六进制字符串
     * @param algorithmName 算法名称
     * @param content 待哈希的字符串内容
     * @param salt 盐值字符串（可选）
     * @return 十六进制字符串
     */
    public static String toHashHexString(String algorithmName, String content, String salt) {
        byte[] saltBytes = salt != null ? salt.getBytes(StandardCharsets.UTF_8) : null;
        return toHashHexString(algorithmName, content.getBytes(StandardCharsets.UTF_8), saltBytes);
    }

    /**
     * 字节数组转换为十六进制字符串
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String toHexString(byte[] bytes) {
        char[] encodedChars = toHex(bytes);
        return new String(encodedChars);
    }

    /**
     * 字节数组转换为十六进制字符数组（摘录自Apache Shiro）
     * @param data 字节数组
     * @return 十六进制字符数组
     */
    public static char[] toHex(byte[] data) {
        int len = data.length;
        char[] out = new char[len << 1];
        int j = 0;

        for (int i = 0; i < len; i++) {
            out[j++] = DIGITS[(data[i] >> 4) & 0x0F];
            out[j++] = DIGITS[data[i] & 0x0F];
        }

        return out;
    }

    /**
     * MD5消息摘要计算（小文件）
     * @param plainText 待计算的字节数组
     * @return 摘要结果字节数组
     */
    public static byte[] md5(byte[] plainText) {
        return digest(DigestType.MD5.getValue(), plainText);
    }

    /**
     * SHA系列消息摘要计算（小文件）
     * @param digestType 摘要算法类型
     * @param plainText 待计算的字节数组
     * @return 摘要结果字节数组
     */
    public static byte[] sha(DigestType digestType, byte[] plainText) {
        return digest(digestType.getValue(), plainText);
    }

    /**
     * 私有的摘要计算方法（小文件）
     * @param algorithmName 算法名称
     * @param plainText 待计算的字节数组
     * @return 摘要结果字节数组
     */
    private static byte[] digest(String algorithmName, byte[] plainText) {
        FixedPoolHelper<MessageDigest> pool = getMessageDigestPool(algorithmName);
        return pool.run(digest -> {
            digest.reset();
            return digest.digest(plainText);
        });
    }

    /**
     * 带盐值的SHA摘要计算（小文件）
     * @param digestType 摘要算法类型
     * @param plainText 待计算的字节数组
     * @param salt 盐值字符串
     * @return 摘要结果字节数组
     */
    public static byte[] sha(DigestType digestType, byte[] plainText, String salt) {
        return hash(digestType.getValue(), plainText, salt.getBytes(StandardCharsets.UTF_8), 1);
    }

    /**
     * 带盐值的SHA摘要计算（小文件）
     * @param digestType 摘要算法类型
     * @param plainText 明文字符串
     * @param salt 盐值字符串
     * @return 摘要结果字节数组
     */
    public static byte[] sha(DigestType digestType, String plainText, String salt) {
        return hash(digestType.getValue(), plainText.getBytes(StandardCharsets.UTF_8),
                     salt.getBytes(StandardCharsets.UTF_8), 1);
    }

    /**
     * MAC消息认证码计算（自动生成密钥）
     * MAC算法是含有密钥的散列函数算法，兼容MD和SHA的特性
     * @param digestType MAC算法类型
     * @param plainText 待计算的字节数组
     * @return 认证码结果字节数组
     */
    public static byte[] macSha(DigestType digestType, byte[] plainText) {
        byte[] secretBytes = generatorMacSecretKey(digestType.getValue());
        return macSha(digestType.getValue(), plainText, secretBytes);
    }

    /**
     * MAC消息认证码计算（使用指定密钥）
     * @param digestType MAC算法类型
     * @param plainText 待计算的字节数组
     * @param secretBytes 密钥字节数组
     * @return 认证码结果字节数组
     */
    public static byte[] macSha(DigestType digestType, byte[] plainText, byte[] secretBytes) {
        return macSha(digestType.getValue(), plainText, secretBytes);
    }

    /**
     * MAC消息认证码计算（内部方法，使用算法名称）
     * @param algorithm 算法名称
     * @param plainText 待计算的字节数组
     * @param secretBytes 密钥字节数组
     * @return 认证码结果字节数组
     */
    private static byte[] macSha(String algorithm, byte[] plainText, byte[] secretBytes) {
        FixedPoolHelper<Mac> pool = getMacPool(algorithm);
        return pool.run(mac -> {
            try {
                SecretKey key = new SecretKeySpec(secretBytes, algorithm);
                mac.init(key);
                return mac.doFinal(plainText);
            } catch (InvalidKeyException e) {
                throw new RuntimeException("Invalid key for MAC algorithm: " + algorithm, e);
            }
        });
    }

    /**
     * 生成MAC算法的随机密钥
     * @param algorithm MAC算法名称
     * @return 密钥字节数组
     */
    private static byte[] generatorMacSecretKey(String algorithm) {
        FixedPoolHelper<KeyGenerator> pool = getKeyGeneratorPool(algorithm);
        return pool.run(keyGenerator -> keyGenerator.generateKey().getEncoded());
    }

    /**
     * 流式摘要计算（使用回调函数填充内容）
     * 适用于大文件处理，流结束时不会关闭输入流
     * @param algorithmName 算法名称
     * @param contentFiller 内容填充回调函数，用于调用 digest.update 方法填充数据
     * @return 摘要结果字节数组
     */
    public static byte[] digest(String algorithmName, FunctionROne<byte[], MessageDigest> contentFiller) {
        FixedPoolHelper<MessageDigest> pool = getMessageDigestPool(algorithmName);
        return pool.run(digest -> {
            digest.reset();
            contentFiller.run(digest);
            return digest.digest();
        });
    }

    /**
     * 流式摘要计算（从输入流读取数据）
     * 适用于大文件处理，流结束时不会关闭输入流
     * @param algorithmName 算法名称
     * @param inputStream 输入流
     * @return 摘要结果字节数组
     */
    public static byte[] digest(String algorithmName, InputStream inputStream) {
        FixedPoolHelper<MessageDigest> pool = getMessageDigestPool(algorithmName);
        return pool.run(digest -> {
            digest.reset();
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int length;
            try {
                while ((length = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, length);
                }
            } catch (IOException e) {
                throw new com.tingfeng.util.java.base.common.exception.io.IOException(e);
            }
            return digest.digest();
        });
    }

    /**
     * MD5流式摘要计算（从输入流读取数据）
     * 适用于大文件处理，流结束时不会关闭输入流
     * @param inputStream 输入流
     * @return 摘要结果字节数组
     */
    public static byte[] md5(InputStream inputStream) {
        return digest(DigestType.MD5.getValue(), inputStream);
    }
}
