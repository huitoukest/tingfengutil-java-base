package com.tingfeng.util.java.base.crypto;

import com.tingfeng.util.java.base.common.constant.EncryptionAlgorithmType;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 加密策略实现。
 * <p>
 * MD5 是消息摘要算法，输出 128 位（16 字节）摘要，不可逆，不支持解密操作。
 * </p>
 */
public class MD5Strategy implements EncryptionStrategy {

    private static final String ALGORITHM = "MD5";

    @Override
    public byte[] encrypt(byte[] data, byte[] key) {
        if (null == data) {
            return null;
        }
        return digest(data);
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        throw new UnsupportedOperationException("MD5 is a hash algorithm and does not support decryption");
    }

    @Override
    public EncryptionAlgorithmType getType() {
        return EncryptionAlgorithmType.MD5;
    }

    @Override
    public boolean supportsDecrypt() {
        return false;
    }

    /**
     * 计算 MD5 摘要。
     *
     * @param data 待计算的字节数组，不能为 null（调用方保证）
     * @return 16 字节的 MD5 摘要
     */
    private byte[] digest(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm " + ALGORITHM + " not available", e);
        }
    }
}
