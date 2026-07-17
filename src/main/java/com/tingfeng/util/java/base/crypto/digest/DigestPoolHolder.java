package com.tingfeng.util.java.base.crypto.digest;

import com.tingfeng.util.java.base.pool.FixedPoolHelper;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一摘要池持有者。
 * <p>
 * 统一管理 MessageDigest、Mac、KeyGenerator 三类对象池，
 * 使用 ConcurrentHashMap.computeIfAbsent 保证线程安全的原子初始化。
 * 同一算法名称始终返回相同的池实例。
 * </p>
 * <p>
 * 本类旨在消除多处重复的池管理逻辑，确保同一算法使用相同的池实例，
 * 减少资源浪费并统一同步策略。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * FixedPoolHelper<MessageDigest> pool = DigestPoolHolder.getMessageDigestPool("MD5");
 * byte[] result = pool.run(digest -> digest.digest(data));
 * }</pre>
 * </p>
 */
public final class DigestPoolHolder {

    /** MessageDigest 池默认最大容量 */
    private static final int DEFAULT_MAX_MESSAGE_DIGEST_SIZE = 16;
    /** Mac 池默认最大容量 */
    private static final int DEFAULT_MAX_MAC_SIZE = 16;
    /** KeyGenerator 池默认最大容量 */
    private static final int DEFAULT_MAX_KEY_GENERATOR_SIZE = 8;

    /** MessageDigest 实例池缓存 */
    private static final Map<String, FixedPoolHelper<MessageDigest>> MESSAGE_DIGEST_POOL = new ConcurrentHashMap<>();
    /** Mac 实例池缓存 */
    private static final Map<String, FixedPoolHelper<Mac>> MAC_POOL = new ConcurrentHashMap<>();
    /** KeyGenerator 实例池缓存 */
    private static final Map<String, FixedPoolHelper<KeyGenerator>> KEY_GENERATOR_POOL = new ConcurrentHashMap<>();

    private DigestPoolHolder() {
        // 私有构造器，禁止实例化
    }

    /**
     * 获取 MessageDigest 实例池。
     * <p>
     * 使用 ConcurrentHashMap.computeIfAbsent 实现线程安全的原子初始化，
     * 同一算法名称始终返回相同的池实例。
     * </p>
     *
     * @param algorithm 算法名称，如 "SHA-256"、"MD5" 等，不能为 null
     * @return MessageDigest 实例池
     * @throws RuntimeException 如果算法不可用，包装 {@link java.security.NoSuchAlgorithmException}
     */
    public static FixedPoolHelper<MessageDigest> getMessageDigestPool(String algorithm) {
        return MESSAGE_DIGEST_POOL.computeIfAbsent(algorithm, key ->
                new FixedPoolHelper<>(DEFAULT_MAX_MESSAGE_DIGEST_SIZE,
                        () -> MessageDigest.getInstance(key)));
    }

    /**
     * 获取 Mac 实例池。
     * <p>
     * 使用 ConcurrentHashMap.computeIfAbsent 实现线程安全的原子初始化，
     * 同一算法名称始终返回相同的池实例。
     * </p>
     *
     * @param algorithm 算法名称，如 "HmacSHA256"、"HmacSHA512" 等，不能为 null
     * @return Mac 实例池
     * @throws RuntimeException 如果算法不可用，包装 {@link java.security.NoSuchAlgorithmException}
     */
    public static FixedPoolHelper<Mac> getMacPool(String algorithm) {
        return MAC_POOL.computeIfAbsent(algorithm, key ->
                new FixedPoolHelper<>(DEFAULT_MAX_MAC_SIZE,
                        () -> Mac.getInstance(key)));
    }

    /**
     * 获取 KeyGenerator 实例池。
     * <p>
     * 使用 ConcurrentHashMap.computeIfAbsent 实现线程安全的原子初始化，
     * 同一算法名称始终返回相同的池实例。
     * </p>
     *
     * @param algorithm 算法名称，如 "HmacSHA256"、"HmacSHA512" 等，不能为 null
     * @return KeyGenerator 实例池
     * @throws RuntimeException 如果算法不可用，包装 {@link java.security.NoSuchAlgorithmException}
     */
    public static FixedPoolHelper<KeyGenerator> getKeyGeneratorPool(String algorithm) {
        return KEY_GENERATOR_POOL.computeIfAbsent(algorithm, key ->
                new FixedPoolHelper<>(DEFAULT_MAX_KEY_GENERATOR_SIZE,
                        () -> KeyGenerator.getInstance(key)));
    }
}
