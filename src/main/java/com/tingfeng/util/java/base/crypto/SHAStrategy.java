package com.tingfeng.util.java.base.crypto;

import com.tingfeng.util.java.base.common.constant.EncryptionAlgorithmType;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.Set;

/**
 * SHA 系列加密策略实现。
 * <p>
 * SHA 是消息摘要算法家族，包括 SHA-1、SHA-256、SHA-512 等，均为不可逆，不支持解密操作。
 * 仅接受 {@link EncryptionAlgorithmType#SHA1}、{@link EncryptionAlgorithmType#SHA256}、
 * {@link EncryptionAlgorithmType#SHA512} 三种算法类型，其他算法类型将在构造时拒绝。
 * </p>
 */
public class SHAStrategy implements EncryptionStrategy {

    /** 支持的 SHA 算法类型白名单 */
    private static final Set<EncryptionAlgorithmType> SUPPORTED_TYPES = EnumSet.of(
            EncryptionAlgorithmType.SHA1,
            EncryptionAlgorithmType.SHA256,
            EncryptionAlgorithmType.SHA512
    );

    private final EncryptionAlgorithmType shaType;

    /**
     * 构造方法。
     *
     * @param shaType SHA 算法类型，仅支持 {@link EncryptionAlgorithmType#SHA1}、
     *                {@link EncryptionAlgorithmType#SHA256}、{@link EncryptionAlgorithmType#SHA512}
     * @throws IllegalArgumentException 如果 shaType 为 null 或不是支持的 SHA 算法类型
     */
    public SHAStrategy(EncryptionAlgorithmType shaType) {
        if (shaType == null || !SUPPORTED_TYPES.contains(shaType)) {
            throw new IllegalArgumentException("SHA strategy requires SHA1, SHA256, or SHA512 algorithm type, got: " + shaType);
        }
        this.shaType = shaType;
    }

    @Override
    public byte[] encrypt(byte[] data, byte[] key) {
        if (null == data) {
            return null;
        }
        return digest(data);
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        throw new UnsupportedOperationException("SHA is a hash algorithm and does not support decryption");
    }

    @Override
    public EncryptionAlgorithmType getType() {
        return shaType;
    }

    @Override
    public boolean supportsDecrypt() {
        return false;
    }

    /**
     * 计算 SHA 摘要。
     *
     * @param data 待计算的字节数组，不能为 null（调用方保证）
     * @return SHA 摘要字节数组
     */
    private byte[] digest(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance(shaType.getValue());
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm " + shaType.getValue() + " not available", e);
        }
    }
}
