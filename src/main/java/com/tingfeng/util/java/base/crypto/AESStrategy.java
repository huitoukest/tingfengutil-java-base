package com.tingfeng.util.java.base.crypto;

import com.tingfeng.util.java.base.common.constant.EncryptionAlgorithmType;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * AES加密策略实现
 * <p>
 * AES是对称加密算法，支持加密和解密操作
 * </p>
 */
public class AESStrategy implements EncryptionStrategy {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    @Override
    public byte[] encrypt(byte[] data, byte[] key) {
        if (null == data) {
            return null;
        }
        try {
            SecretKey secretKey = generateKey(key);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("AES encryption failed", e);
        }
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        if (null == data) {
            return null;
        }
        try {
            SecretKey secretKey = generateKey(key);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid AES key", e);
        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed", e);
        }
    }

    @Override
    public EncryptionAlgorithmType getType() {
        return EncryptionAlgorithmType.AES;
    }

    @Override
    public boolean supportsDecrypt() {
        return true;
    }

    /**
     * 生成AES密钥
     * <p>
     * 支持128位、192位和256位密钥
     * 密钥长度不足16字节时使用PKCS5Padding方式补足
     * </p>
     *
     * @param key 原始密钥字节数组
     * @return AES SecretKey对象
     */
    private SecretKey generateKey(byte[] key) {
        byte[] keyBytes;
        if (key == null) {
            keyBytes = new byte[16];
        } else if (key.length < 16) {
            keyBytes = new byte[16];
            System.arraycopy(key, 0, keyBytes, 0, key.length);
        } else if (key.length == 16 || key.length == 24 || key.length == 32) {
            keyBytes = key;
        } else {
            keyBytes = new byte[32];
            System.arraycopy(key, 0, keyBytes, 0, 32);
        }
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}
