package com.tingfeng.util.java.base.crypto;

import com.tingfeng.util.java.base.common.constant.EncryptionAlgorithmType;

/**
 * 加密策略接口。
 * <p>
 * 定义加密和解密操作的标准接口，采用策略模式。
 * 不同的加密算法（如 AES、MD5、SHA 系列）通过此接口统一调用。
 * 哈希算法（如 MD5、SHA）不支持解密操作，{@link #supportsDecrypt()} 返回 false。
 * </p>
 */
public interface EncryptionStrategy {

    /**
     * 加密数据。
     *
     * @param data 待加密的字节数组，为 null 时各实现可返回 null
     * @param key  密钥字节数组，对于哈希算法（MD5、SHA）会被忽略
     * @return 加密后的字节数组，data 为 null 时返回 null
     * @throws IllegalArgumentException 如果 key 不合法（如长度不符合要求）
     */
    byte[] encrypt(byte[] data, byte[] key);

    /**
     * 解密数据。
     *
     * @param data 待解密的字节数组，为 null 时返回 null
     * @param key  密钥字节数组
     * @return 解密后的字节数组，data 为 null 时返回 null
     * @throws UnsupportedOperationException 如果该策略不支持解密操作（如哈希算法）
     * @throws IllegalArgumentException 如果 key 不合法或数据格式不正确
     */
    byte[] decrypt(byte[] data, byte[] key);

    /**
     * 获取加密算法类型。
     *
     * @return 加密算法类型枚举值
     * @see EncryptionAlgorithmType
     */
    EncryptionAlgorithmType getType();

    /**
     * 判断该策略是否支持解密操作。
     *
     * @return true 表示支持解密，false 表示不支持（如哈希算法）
     */
    boolean supportsDecrypt();
}
