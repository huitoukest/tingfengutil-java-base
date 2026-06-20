package com.tingfeng.util.java.base.concurrent;

import com.tingfeng.util.java.base.common.constant.EncryptionAlgorithmType;
import com.tingfeng.util.java.base.crypto.EncryptionHelper;

import java.nio.charset.StandardCharsets;

/**
 * 加密工具类（线程安全门面）
 * <p>
 * 提供统一的加密和解密接口，内部委托给EncryptionHelper处理
 * </p>
 * <p>
 * 设计说明：
 * <ul>
 *   <li>对于哈希算法（MD5、SHA-1、SHA-256、SHA-512），key参数会被忽略</li>
 *   <li>对于AES算法，key参数用于加密和解密</li>
 *   <li>哈希算法是不可逆的，调用decrypt会抛出UnsupportedOperationException</li>
 * </ul>
 * </p>
 */
public final class EncryptionUtils {

    private EncryptionUtils() {
        // 私有构造器，禁止实例化
    }

    // ==================== 字节数组加密/解密 ====================

    /**
     * 加密数据（字节数组）
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
        return EncryptionHelper.encrypt(data, algorithm, key);
    }

    /**
     * 解密数据（字节数组）
     * <p>
     * 注意：哈希算法（MD5、SHA系列）不支持解密，会抛出UnsupportedOperationException
     * </p>
     *
     * @param data     待解密的字节数组
     * @param algorithm 算法类型
     * @param key      密钥（对于哈希算法会被忽略）
     * @return 解密后的字节数组
     * @throws UnsupportedOperationException 如果算法不支持解密（哈希算法）
     */
    public static byte[] decrypt(byte[] data, EncryptionAlgorithmType algorithm, byte[] key) {
        return EncryptionHelper.decrypt(data, algorithm, key);
    }

    // ==================== 字符串加密/解密（UTF-8） ====================

    /**
     * 加密字符串（UTF-8编码）
     * <p>
     * 对于哈希算法（MD5、SHA系列），key参数会被忽略
     * </p>
     *
     * @param data     待加密的字符串
     * @param algorithm 算法类型
     * @param key      密钥（对于哈希算法会被忽略）
     * @return 加密后的字节数组
     */
    public static byte[] encrypt(String data, EncryptionAlgorithmType algorithm, byte[] key) {
        if (data == null) {
            return null;
        }
        return encrypt(data.getBytes(StandardCharsets.UTF_8), algorithm, key);
    }

    /**
     * 解密字符串（UTF-8编码）
     * <p>
     * 注意：哈希算法不支持解密，会抛出UnsupportedOperationException
     * </p>
     *
     * @param data     待解密的字节数组
     * @param algorithm 算法类型
     * @param key      密钥（对于哈希算法会被忽略）
     * @return 解密后的字符串
     * @throws UnsupportedOperationException 如果算法不支持解密（哈希算法）
     */
    public static String decryptToString(byte[] data, EncryptionAlgorithmType algorithm, byte[] key) {
        byte[] result = decrypt(data, algorithm, key);
        return result != null ? new String(result, StandardCharsets.UTF_8) : null;
    }

    // ==================== 十六进制字符串加密 ====================

    /**
     * 加密并转换为十六进制字符串
     * <p>
     * 对于哈希算法（MD5、SHA系列），key参数会被忽略
     * </p>
     *
     * @param data     待加密的字符串
     * @param algorithm 算法类型
     * @param key      密钥（对于哈希算法会被忽略）
     * @return 十六进制格式的加密结果
     */
    public static String encryptToHex(String data, EncryptionAlgorithmType algorithm, byte[] key) {
        if (data == null) {
            return null;
        }
        byte[] encrypted = encrypt(data.getBytes(StandardCharsets.UTF_8), algorithm, key);
        return bytesToHex(encrypted);
    }

    /**
     * 解密十六进制字符串
     * <p>
     * 注意：哈希算法不支持解密，会抛出UnsupportedOperationException
     * </p>
     *
     * @param hexData  十六进制格式的加密数据
     * @param algorithm 算法类型
     * @param key      密钥（对于哈希算法会被忽略）
     * @return 解密后的字符串
     * @throws UnsupportedOperationException 如果算法不支持解密（哈希算法）
     */
    public static String decryptFromHex(String hexData, EncryptionAlgorithmType algorithm, byte[] key) {
        if (hexData == null) {
            return null;
        }
        byte[] data = hexToBytes(hexData);
        byte[] decrypted = decrypt(data, algorithm, key);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    // ==================== 便捷方法（无密钥） ====================

    /**
     * 使用MD5加密字符串
     *
     * @param data 待加密的字符串
     * @return MD5加密后的十六进制字符串
     */
    public static String md5(String data) {
        return encryptToHex(data, EncryptionAlgorithmType.MD5, null);
    }

    /**
     * 使用SHA-256加密字符串
     *
     * @param data 待加密的字符串
     * @return SHA-256加密后的十六进制字符串
     */
    public static String sha256(String data) {
        return encryptToHex(data, EncryptionAlgorithmType.SHA256, null);
    }

    /**
     * 使用SHA-512加密字符串
     *
     * @param data 待加密的字符串
     * @return SHA-512加密后的十六进制字符串
     */
    public static String sha512(String data) {
        return encryptToHex(data, EncryptionAlgorithmType.SHA512, null);
    }

    // ==================== 辅助方法 ====================

    /**
     * 字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = Character.forDigit(v >>> 4, 16);
            hexChars[i * 2 + 1] = Character.forDigit(v & 0x0F, 16);
        }
        return new String(hexChars).toLowerCase();
    }

    /**
     * 十六进制字符串转换为字节数组
     *
     * @param hex 十六进制字符串
     * @return 字节数组
     */
    private static byte[] hexToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string");
        }
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
