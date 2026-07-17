package com.tingfeng.util.java.base.crypto;

import com.tingfeng.util.java.base.common.constant.EncryptionAlgorithmType;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * AES 加密策略实现（AES-GCM 认证加密模式）。
 * <p>
 * 使用 GCM（Galois/Counter Mode）认证加密模式，同时提供机密性和完整性保护。
 * 加密时自动生成 12 字节随机 IV（nonce），并拼接到密文头部返回。
 * 解密时从密文头部提取 IV 后进行认证解密。
 * </p>
 * <p>
 * 密钥长度支持：
 * - 128 位（16 字节）
 * - 192 位（24 字节）
 * - 256 位（32 字节）- 需要 JCE Unlimited Strength Jurisdiction Policy（JDK 8u162+ 默认已包含）
 * 密钥不可为 null，长度必须为 16、24 或 32 字节。
 * </p>
 *
 * @see EncryptionStrategy
 */
public class AESStrategy implements EncryptionStrategy {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    /**
     * GCM 推荐 nonce 长度：12 字节（96 位）
     */
    private static final int GCM_IV_LENGTH = 12;

    /**
     * GCM 认证标签长度：128 位（16 字节）
     */
    private static final int GCM_TAG_LENGTH = 128;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public byte[] encrypt(byte[] data, byte[] key) {
        if (null == data) {
            return null;
        }
        validateKey(key);
        try {
            SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            byte[] ciphertext = cipher.doFinal(data);

            // 拼接格式：IV(12B) + ciphertext(含GCM tag)
            byte[] result = new byte[GCM_IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, GCM_IV_LENGTH);
            System.arraycopy(ciphertext, 0, result, GCM_IV_LENGTH, ciphertext.length);
            return result;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AES encryption failed: algorithm not available", e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException("AES encryption failed: padding not available", e);
        } catch (Exception e) {
            throw new RuntimeException("AES encryption failed", e);
        }
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        if (null == data) {
            return null;
        }
        validateKey(key);
        try {
            if (data.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException(
                    "Ciphertext too short, must contain at least " + GCM_IV_LENGTH + " bytes of IV");
            }

            SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            // 从密文头部提取 IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(data, 0, iv, 0, GCM_IV_LENGTH);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            // 剩余部分为密文 + GCM tag
            byte[] ciphertext = new byte[data.length - GCM_IV_LENGTH];
            System.arraycopy(data, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);
            return cipher.doFinal(ciphertext);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AES decryption failed: algorithm not available", e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException("AES decryption failed: padding not available", e);
        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed", e);
        }
    }

    /**
     * 校验 AES 密钥有效性。
     * <p>
     * 密钥不能为 null，长度必须为 16、24 或 32 字节。
     * </p>
     *
     * @param key AES 密钥字节数组
     * @throws IllegalArgumentException 如果 key 为 null 或长度不符合要求
     */
    private void validateKey(byte[] key) {
        if (null == key) {
            throw new IllegalArgumentException("AES key must not be null");
        }
        int len = key.length;
        if (len != 16 && len != 24 && len != 32) {
            throw new IllegalArgumentException(
                "AES key must be 16, 24, or 32 bytes (current: " + len + " bytes)");
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
}
