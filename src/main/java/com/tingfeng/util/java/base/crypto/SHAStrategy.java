package com.tingfeng.util.java.base.crypto;

import com.tingfeng.util.java.base.common.constant.EncryptionAlgorithmType;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA系列加密策略实现
 * <p>
 * SHA是消息摘要算法家族，包括SHA-1、SHA-256、SHA-512等，均为不可逆，不支持解密操作
 * </p>
 */
public class SHAStrategy implements EncryptionStrategy {

    private final EncryptionAlgorithmType shaType;

    /**
     * 构造方法
     *
     * @param shaType SHA算法类型，支持SHA1、SHA256、SHA512
     */
    public SHAStrategy(EncryptionAlgorithmType shaType) {
        if (shaType == null || shaType == EncryptionAlgorithmType.MD5 || shaType == EncryptionAlgorithmType.AES) {
            throw new IllegalArgumentException("SHA strategy requires SHA1, SHA256, or SHA512 algorithm type");
        }
        this.shaType = shaType;
    }

    @Override
    public byte[] encrypt(byte[] data, byte[] key) {
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
     * 计算SHA摘要
     *
     * @param data 待计算的字节数组
     * @return SHA摘要，如果输入为null则返回null
     */
    private byte[] digest(byte[] data) {
        if (null == data) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance(shaType.getValue());
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm " + shaType.getValue() + " not available", e);
        }
    }
}
