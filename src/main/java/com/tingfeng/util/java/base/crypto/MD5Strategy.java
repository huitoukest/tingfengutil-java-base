package com.tingfeng.util.java.base.crypto;

import com.tingfeng.util.java.base.common.constant.EncryptionAlgorithmType;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密策略实现
 * <p>
 * MD5是消息摘要算法，不可逆，不支持解密操作
 * </p>
 */
public class MD5Strategy implements EncryptionStrategy {

    private static final String ALGORITHM = "MD5";

    @Override
    public byte[] encrypt(byte[] data, byte[] key) {
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
     * 计算MD5摘要
     *
     * @param data 待计算的字节数组
     * @return 16字节的MD5摘要，如果输入为null则返回null
     */
    private byte[] digest(byte[] data) {
        if (null == data) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm " + ALGORITHM + " not available", e);
        }
    }
}
