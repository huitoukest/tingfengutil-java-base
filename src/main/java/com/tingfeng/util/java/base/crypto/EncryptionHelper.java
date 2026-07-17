package com.tingfeng.util.java.base.crypto;

import com.tingfeng.util.java.base.common.constant.EncryptionAlgorithmType;
import com.tingfeng.util.java.base.crypto.digest.DigestPoolHolder;
import com.tingfeng.util.java.base.pool.FixedPoolHelper;

import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加密辅助类。
 * <p>
 * 负责策略注册表管理和 MessageDigest 实例缓存，
 * 提供加密和解密操作的统一入口。
 * 默认注册 MD5、SHA-1、SHA-256、SHA-512 和 AES 策略，
 * 可通过 {@link #registerStrategy(EncryptionStrategy)} 扩展。
 * </p>
 */
public class EncryptionHelper {

    /**
     * 策略注册表（线程安全）
     */
    private static final Map<EncryptionAlgorithmType, EncryptionStrategy> STRATEGY_REGISTRY = new ConcurrentHashMap<>();

    static {
        // 注册默认策略
        registerStrategy(new MD5Strategy());
        registerStrategy(new SHAStrategy(EncryptionAlgorithmType.SHA1));
        registerStrategy(new SHAStrategy(EncryptionAlgorithmType.SHA256));
        registerStrategy(new SHAStrategy(EncryptionAlgorithmType.SHA512));
        registerStrategy(new AESStrategy());
        registerStrategy(new HashStrategy(""));
    }

    /**
     * 注册加密策略。
     * <p>
     * 如果 strategy 为 null，则静默忽略。
     * 如果已有同类型策略，将被覆盖。
     * </p>
     *
     * @param strategy 加密策略实例，为 null 时静默忽略
     */
    public static void registerStrategy(EncryptionStrategy strategy) {
        if (strategy != null) {
            STRATEGY_REGISTRY.put(strategy.getType(), strategy);
        }
    }

    /**
     * 加密数据。
     * <p>
     * 对于哈希算法（MD5、SHA 系列），key 参数会被忽略。
     * </p>
     *
     * @param data      待加密的字节数组
     * @param algorithm 算法类型，不能为 null
     * @param key       密钥字节数组（对于哈希算法会被忽略）
     * @return 加密后的字节数组
     * @throws IllegalArgumentException 如果 algorithm 为 null 或未注册
     */
    public static byte[] encrypt(byte[] data, EncryptionAlgorithmType algorithm, byte[] key) {
        EncryptionStrategy strategy = getStrategy(algorithm);
        return strategy.encrypt(data, key);
    }

    /**
     * 解密数据。
     * <p>
     * 对于哈希算法（MD5、SHA 系列），此方法会抛出 UnsupportedOperationException。
     * </p>
     *
     * @param data      待解密的字节数组
     * @param algorithm 算法类型，不能为 null
     * @param key       密钥字节数组
     * @return 解密后的字节数组
      * @throws IllegalArgumentException 如果 algorithm 为 null 或未注册
     * @throws UnsupportedOperationException 如果算法不支持解密（如哈希算法）
     */
    public static byte[] decrypt(byte[] data, EncryptionAlgorithmType algorithm, byte[] key) {
        EncryptionStrategy strategy = getStrategy(algorithm);
        return strategy.decrypt(data, key);
    }

    /**
     * 获取对应的加密策略。
     *
     * @param algorithm 算法类型，不能为 null
     * @return 加密策略实例
     * @throws IllegalArgumentException 如果 algorithm 为 null 或未注册
     */
    private static EncryptionStrategy getStrategy(EncryptionAlgorithmType algorithm) {
        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm cannot be null");
        }
        EncryptionStrategy strategy = STRATEGY_REGISTRY.get(algorithm);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
        return strategy;
    }

    /**
     * 获取 MessageDigest 实例池。
     * <p>
     * 委托给 {@link com.tingfeng.util.java.base.crypto.digest.DigestPoolHolder#getMessageDigestPool(String)}。
     * </p>
     *
     * @param algorithm 算法名称，如 "SHA-256"、"MD5" 等，不能为 null
     * @return MessageDigest 实例池
     * @throws IllegalArgumentException 如果 algorithm 为 null
     */
    public static FixedPoolHelper<MessageDigest> getMessageDigestPool(String algorithm) {
        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm cannot be null");
        }
        return DigestPoolHolder.getMessageDigestPool(algorithm);
    }

    /**
     * 检查算法是否支持解密操作。
     *
     * @param algorithm 算法类型
     * @return true 表示支持解密，false 表示不支持或 algorithm 为 null
     */
    public static boolean supportsDecrypt(EncryptionAlgorithmType algorithm) {
        if (algorithm == null) {
            return false;
        }
        EncryptionStrategy strategy = STRATEGY_REGISTRY.get(algorithm);
        return strategy != null && strategy.supportsDecrypt();
    }
}
