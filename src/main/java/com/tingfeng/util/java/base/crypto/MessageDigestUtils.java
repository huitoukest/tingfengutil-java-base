package com.tingfeng.util.java.base.crypto;

import com.tingfeng.util.java.base.crypto.digest.DigestPoolHolder;
import com.tingfeng.util.java.base.pool.FixedPoolHelper;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.util.function.Consumer;

/**
 * 消息摘要与 MAC 加密工具类。
 * <p>
 * 支持 MD5、SHA-1、SHA-256、SHA-512 等消息摘要算法的字节数组和字符串摘要计算，
 * 支持 HmacSHA256、HmacSHA512 等 HMAC 系列算法的消息认证码计算。
 * 所有公开方法均对 null 入参进行校验并抛出 {@link IllegalArgumentException}。
 * </p>
 */
public class MessageDigestUtils {
    /** 流式处理默认缓冲区大小（4KB） */
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    /** 十六进制字符映射表 */
    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

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

    /**
     * 获取MessageDigest实例池（委托给DigestPoolHolder）
     * @param algorithm 算法名称，如"SHA-256"、"MD5"等
     * @return MessageDigest实例池
     */
    private static FixedPoolHelper<MessageDigest> getMessageDigestPool(String algorithm) {
        return DigestPoolHolder.getMessageDigestPool(algorithm);
    }

    /**
     * 获取Mac实例池（委托给DigestPoolHolder）
     * @param algorithm 算法名称，如"HmacSHA256"等
     * @return Mac实例池
     */
    private static FixedPoolHelper<Mac> getMacPool(String algorithm) {
        return DigestPoolHolder.getMacPool(algorithm);
    }

    /**
     * 获取KeyGenerator实例池（委托给DigestPoolHolder）
     * @param algorithm 算法名称，如"HmacSHA256"等
     * @return KeyGenerator实例池
     */
    private static FixedPoolHelper<KeyGenerator> getKeyGeneratorPool(String algorithm) {
        return DigestPoolHolder.getKeyGeneratorPool(algorithm);
    }

    /**
     * 多轮迭代哈希计算。
     *
     * @param algorithmName 算法名称（如 "MD5"、"SHA-256"），不能为 null
     * @param bytes         待哈希的字节数组，不能为 null
     * @param salt          盐值（可选，可以为 null）
     * @param hashIterations 迭代次数，必须大于 0
     * @return 哈希后的字节数组
     * @throws IllegalArgumentException 如果 algorithmName 或 bytes 为 null
     */
    public static byte[] hash(String algorithmName, byte[] bytes, byte[] salt, int hashIterations) {
        if (null == algorithmName) {
            throw new IllegalArgumentException("algorithmName must not be null");
        }
        if (null == bytes) {
            throw new IllegalArgumentException("bytes must not be null");
        }
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
     * 默认单轮迭代哈希计算。
     *
     * @param algorithmName 算法名称（如 "MD5"、"SHA-256"），不能为 null
     * @param content       待哈希的字符串内容，不能为 null
     * @param salt          盐值字符串（可选，可以为 null）
     * @return 哈希后的字节数组
     * @throws IllegalArgumentException 如果 algorithmName 或 content 为 null
     */
    public static byte[] hash(String algorithmName, String content, String salt) {
        if (null == algorithmName) {
            throw new IllegalArgumentException("algorithmName must not be null");
        }
        if (null == content) {
            throw new IllegalArgumentException("content must not be null");
        }
        byte[] saltBytes = null;
        if (null != salt) {
            saltBytes = salt.getBytes(StandardCharsets.UTF_8);
        }
        return hash(algorithmName, content.getBytes(StandardCharsets.UTF_8), saltBytes, 1);
    }

    /**
     * 多轮迭代哈希计算并转换为十六进制字符串。
     *
     * @param algorithmName  算法名称，不能为 null
     * @param bytes          待哈希的字节数组，不能为 null
     * @param salt           盐值（可选，可以为 null）
     * @param hashIterations 迭代次数
     * @return 十六进制字符串（小写）
     * @throws IllegalArgumentException 如果 algorithmName 或 bytes 为 null
     */
    public static String toHashHexString(String algorithmName, byte[] bytes, byte[] salt, int hashIterations) {
        if (null == algorithmName) {
            throw new IllegalArgumentException("algorithmName must not be null");
        }
        if (null == bytes) {
            throw new IllegalArgumentException("bytes must not be null");
        }
        byte[] hashBytes = hash(algorithmName, bytes, salt, hashIterations);
        return toHexString(hashBytes);
    }

    /**
     * 单轮迭代哈希计算并转换为十六进制字符串。
     *
     * @param algorithmName 算法名称，不能为 null
     * @param bytes         待哈希的字节数组，不能为 null
     * @param salt          盐值（可选，可以为 null）
     * @return 十六进制字符串（小写）
     * @throws IllegalArgumentException 如果 algorithmName 或 bytes 为 null
     */
    public static String toHashHexString(String algorithmName, byte[] bytes, byte[] salt) {
        if (null == algorithmName) {
            throw new IllegalArgumentException("algorithmName must not be null");
        }
        if (null == bytes) {
            throw new IllegalArgumentException("bytes must not be null");
        }
        return toHashHexString(algorithmName, bytes, salt, 1);
    }

    /**
     * 单轮哈希计算并将结果转换为十六进制字符串。
     *
     * @param algorithmName 算法名称，不能为 null
     * @param content       待哈希的字符串内容，不能为 null
     * @param salt          盐值字符串（可选，可以为 null）
     * @return 十六进制字符串（小写）
     * @throws IllegalArgumentException 如果 algorithmName 或 content 为 null
     */
    public static String toHashHexString(String algorithmName, String content, String salt) {
        if (null == algorithmName) {
            throw new IllegalArgumentException("algorithmName must not be null");
        }
        if (null == content) {
            throw new IllegalArgumentException("content must not be null");
        }
        byte[] saltBytes = salt != null ? salt.getBytes(StandardCharsets.UTF_8) : null;
        return toHashHexString(algorithmName, content.getBytes(StandardCharsets.UTF_8), saltBytes);
    }

    /**
     * 将字节数组转换为十六进制字符串（小写）。
     *
     * @param bytes 字节数组，不能为 null
     * @return 十六进制字符串（小写）
     * @throws IllegalArgumentException 如果 bytes 为 null
     */
    public static String toHexString(byte[] bytes) {
        if (null == bytes) {
            throw new IllegalArgumentException("bytes must not be null");
        }
        char[] encodedChars = toHex(bytes);
        return new String(encodedChars);
    }

    /**
     * 将字节数组转换为十六进制字符数组（小写）。
     *
     * @param data 字节数组，不能为 null
     * @return 十六进制字符数组（小写）
     * @throws IllegalArgumentException 如果 data 为 null
     */
    public static char[] toHex(byte[] data) {
        if (null == data) {
            throw new IllegalArgumentException("data must not be null");
        }
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
     * MD5 消息摘要计算。
     *
     * @param plainText 待计算的字节数组，不能为 null
     * @return 16 字节的 MD5 摘要结果
     * @throws IllegalArgumentException 如果 plainText 为 null
     */
    public static byte[] md5(byte[] plainText) {
        if (null == plainText) {
            throw new IllegalArgumentException("plainText must not be null");
        }
        return digest(DigestType.MD5.getValue(), plainText);
    }

    /**
     * SHA 系列消息摘要计算。
     *
     * @param digestType 摘要算法类型，不能为 null
     * @param plainText  待计算的字节数组，不能为 null
     * @return 摘要结果字节数组
     * @throws IllegalArgumentException 如果 digestType 或 plainText 为 null
     */
    public static byte[] sha(DigestType digestType, byte[] plainText) {
        if (null == digestType) {
            throw new IllegalArgumentException("digestType must not be null");
        }
        if (null == plainText) {
            throw new IllegalArgumentException("plainText must not be null");
        }
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
     * 带盐值的 SHA 摘要计算（字节数组输入）。
     *
     * @param digestType 摘要算法类型，不能为 null
     * @param plainText  待计算的字节数组，不能为 null
     * @param salt       盐值字符串，不能为 null
     * @return 摘要结果字节数组
     * @throws IllegalArgumentException 如果任一参数为 null
     */
    public static byte[] sha(DigestType digestType, byte[] plainText, String salt) {
        if (null == digestType) {
            throw new IllegalArgumentException("digestType must not be null");
        }
        if (null == plainText) {
            throw new IllegalArgumentException("plainText must not be null");
        }
        if (null == salt) {
            throw new IllegalArgumentException("salt must not be null");
        }
        return hash(digestType.getValue(), plainText, salt.getBytes(StandardCharsets.UTF_8), 1);
    }

    /**
     * 带盐值的 SHA 摘要计算（字符串输入）。
     *
     * @param digestType 摘要算法类型，不能为 null
     * @param plainText  明文字符串，不能为 null
     * @param salt       盐值字符串，不能为 null
     * @return 摘要结果字节数组
     * @throws IllegalArgumentException 如果任一参数为 null
     */
    public static byte[] sha(DigestType digestType, String plainText, String salt) {
        if (null == digestType) {
            throw new IllegalArgumentException("digestType must not be null");
        }
        if (null == plainText) {
            throw new IllegalArgumentException("plainText must not be null");
        }
        if (null == salt) {
            throw new IllegalArgumentException("salt must not be null");
        }
        return hash(digestType.getValue(), plainText.getBytes(StandardCharsets.UTF_8),
                     salt.getBytes(StandardCharsets.UTF_8), 1);
    }

    /**
     * MAC 消息认证码计算（自动生成密钥）。
     * <p>
     * MAC 算法是含有密钥的散列函数算法，兼容 MD 和 SHA 的特性。
     * 每次调用会生成一个新的随机密钥。
     * </p>
     *
     * @param digestType MAC 算法类型，不能为 null
     * @param plainText  待计算的字节数组，不能为 null
     * @return 认证码结果字节数组
     * @throws IllegalArgumentException 如果 digestType 或 plainText 为 null
     */
    public static byte[] macSha(DigestType digestType, byte[] plainText) {
        if (null == digestType) {
            throw new IllegalArgumentException("digestType must not be null");
        }
        if (null == plainText) {
            throw new IllegalArgumentException("plainText must not be null");
        }
        byte[] secretBytes = generatorMacSecretKey(digestType.getValue());
        return macSha(digestType.getValue(), plainText, secretBytes);
    }

    /**
     * MAC 消息认证码计算（使用指定密钥）。
     *
     * @param digestType  MAC 算法类型，不能为 null
     * @param plainText   待计算的字节数组，不能为 null
     * @param secretBytes 密钥字节数组，不能为 null
     * @return 认证码结果字节数组
     * @throws IllegalArgumentException 如果任一参数为 null
     */
    public static byte[] macSha(DigestType digestType, byte[] plainText, byte[] secretBytes) {
        if (null == digestType) {
            throw new IllegalArgumentException("digestType must not be null");
        }
        if (null == plainText) {
            throw new IllegalArgumentException("plainText must not be null");
        }
        if (null == secretBytes) {
            throw new IllegalArgumentException("secretBytes must not be null");
        }
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
     * 流式摘要计算（使用回调函数填充内容）。
     * <p>
     * 适用于大文件处理，调用者通过回调函数向 MessageDigest 实例填充数据，
     * 流结束时不会关闭输入流。调用者需自行管理输入流的关闭。
     * </p>
     *
     * @param algorithmName 算法名称（如 "MD5"、"SHA-256"），不能为 null
     * @param contentFiller 内容填充回调函数，用于调用 {@link MessageDigest#update(byte[])} 等方法填充数据，不能为 null
     * @return 摘要结果字节数组
     * @throws IllegalArgumentException 如果 algorithmName 或 contentFiller 为 null
     */
    public static byte[] digest(String algorithmName, Consumer<MessageDigest> contentFiller) {
        if (null == algorithmName) {
            throw new IllegalArgumentException("algorithmName must not be null");
        }
        if (null == contentFiller) {
            throw new IllegalArgumentException("contentFiller must not be null");
        }
        FixedPoolHelper<MessageDigest> pool = getMessageDigestPool(algorithmName);
        return pool.run(digest -> {
            digest.reset();
            contentFiller.accept(digest);
            return digest.digest();
        });
    }

    /**
     * 流式摘要计算（从输入流读取数据）。
     * <p>
     * 适用于大文件处理，流结束时不会关闭输入流。
     * 调用者需自行管理输入流的关闭。
     * </p>
     *
     * @param algorithmName 算法名称（如 "MD5"、"SHA-256"），不能为 null
     * @param inputStream   输入流，不能为 null
     * @return 摘要结果字节数组
     * @throws IllegalArgumentException 如果 algorithmName 或 inputStream 为 null
     */
    public static byte[] digest(String algorithmName, InputStream inputStream) {
        if (null == algorithmName) {
            throw new IllegalArgumentException("algorithmName must not be null");
        }
        if (null == inputStream) {
            throw new IllegalArgumentException("inputStream must not be null");
        }
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
                throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
            }
            return digest.digest();
        });
    }

    /**
     * MD5 流式摘要计算（从输入流读取数据）。
     * <p>
     * 适用于大文件处理，流结束时不会关闭输入流。
     * 调用者需自行管理输入流的关闭。
     * </p>
     *
     * @param inputStream 输入流，不能为 null
     * @return 16 字节的 MD5 摘要结果
     * @throws IllegalArgumentException 如果 inputStream 为 null
     */
    public static byte[] md5(InputStream inputStream) {
        if (null == inputStream) {
            throw new IllegalArgumentException("inputStream must not be null");
        }
        return digest(DigestType.MD5.getValue(), inputStream);
    }
}
