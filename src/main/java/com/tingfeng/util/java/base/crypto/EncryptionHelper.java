package com.tingfeng.util.java.base.crypto;

import com.tingfeng.util.java.base.common.constant.EncryptionAlgorithmType;
import com.tingfeng.util.java.base.pool.FixedPoolHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加密辅助类
 * <p>
 * 负责策略注册表管理和MessageDigest实例缓存
 * 提供加密和解密操作的统一入口
 * </p>
 */
public class EncryptionHelper {

    /**
     * MessageDigest池最大容量
     */
    private static final int DEFAULT_MAX_MESSAGE_DIGEST_SIZE = 16;

    /**
     * 策略注册表（线程安全）
     */
    private static final Map<EncryptionAlgorithmType, EncryptionStrategy> STRATEGY_REGISTRY = new ConcurrentHashMap<>();

    /**
     * MessageDigest实例池缓存（线程安全）
     */
    private static final Map<String, FixedPoolHelper<MessageDigest>> MESSAGE_DIGEST_POOL = new ConcurrentHashMap<>();

    static {
        // 注册默认策略
        registerStrategy(new MD5Strategy());
        registerStrategy(new SHAStrategy(EncryptionAlgorithmType.SHA1));
        registerStrategy(new SHAStrategy(EncryptionAlgorithmType.SHA256));
        registerStrategy(new SHAStrategy(EncryptionAlgorithmType.SHA512));
        registerStrategy(new AESStrategy());
    }

    /**
     * 注册加密策略
     *
     * @param strategy 加密策略实例
     */
    public static void registerStrategy(EncryptionStrategy strategy) {
        if (strategy != null) {
            STRATEGY_REGISTRY.put(strategy.getType(), strategy);
        }
    }

    /**
     * 加密数据
     * <p>
     * 对于哈希算法（MD5、SHA系列），key参数会被忽略
     * </p>
     *
     * @param data     待加密的字节数组
     * @param algorithm 算法类型
     * @param key      密钥（对于哈希算法会被忽略）
     * @return 加密后的字节数组
     */
    public static byte[] encrypt(byte[] data, EncryptionAlgorithmType algorithm, byte[] key) {
        EncryptionStrategy strategy = getStrategy(algorithm);
        return strategy.encrypt(data, key);
    }

    /**
     * 解密数据
     * <p>
     * 对于哈希算法（MD5、SHA系列），此方法会抛出UnsupportedOperationException
     * </p>
     *
     * @param data     待解密的字节数组
     * @param algorithm 算法类型
     * @param key      密钥（对于哈希算法会被忽略）
     * @return 解密后的字节数组
     * @throws UnsupportedOperationException 如果算法不支持解密（哈希算法）
     */
    public static byte[] decrypt(byte[] data, EncryptionAlgorithmType algorithm, byte[] key) {
        EncryptionStrategy strategy = getStrategy(algorithm);
        return strategy.decrypt(data, key);
    }

    /**
     * 获取对应的加密策略
     *
     * @param algorithm 算法类型
     * @return 加密策略实例
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
     * 获取MessageDigest实例池（线程安全）
     *
     * @param algorithm 算法名称，如"SHA-256"、"MD5"等
     * @return MessageDigest实例池
     */
    public static FixedPoolHelper<MessageDigest> getMessageDigestPool(String algorithm) {
        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm cannot be null");
        }
        FixedPoolHelper<MessageDigest> pool = MESSAGE_DIGEST_POOL.get(algorithm);
        if (pool != null) {
            return pool;
        }
        synchronized (MESSAGE_DIGEST_POOL) {
            pool = MESSAGE_DIGEST_POOL.get(algorithm);
            if (pool != null) {
                return pool;
            }
            // 预验证算法有效性
            try {
                MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Algorithm " + algorithm + " not available", e);
            }
            // 创建池，Callable内部不抛出检查异常
            pool = new FixedPoolHelper<>(DEFAULT_MAX_MESSAGE_DIGEST_SIZE, () -> MessageDigest.getInstance(algorithm));
            MESSAGE_DIGEST_POOL.put(algorithm, pool);
            return pool;
        }
    }

    /**
     * 检查算法是否支持解密
     *
     * @param algorithm 算法类型
     * @return true 支持解密，false 不支持
     */
    public static boolean supportsDecrypt(EncryptionAlgorithmType algorithm) {
        if (algorithm == null) {
            return false;
        }
        EncryptionStrategy strategy = STRATEGY_REGISTRY.get(algorithm);
        return strategy != null && strategy.supportsDecrypt();
    }
}
