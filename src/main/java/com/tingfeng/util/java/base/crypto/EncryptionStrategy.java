package com.tingfeng.util.java.base.crypto;

import com.tingfeng.util.java.base.common.constant.EncryptionAlgorithmType;

/**
 * 加密策略接口
 * 定义加密和解密操作的标准接口
 */
public interface EncryptionStrategy {

    /**
     * 加密数据
     *
     * @param data 待加密的字节数组
     * @param key  密钥（对于哈希算法会被忽略）
     * @return 加密后的字节数组
     */
    byte[] encrypt(byte[] data, byte[] key);

    /**
     * 解密数据
     *
     * @param data 待解密的字节数组
     * @param key  密钥（对于哈希算法会被忽略）
     * @return 解密后的字节数组
     * @throws UnsupportedOperationException 如果该策略不支持解密操作（哈希算法）
     */
    byte[] decrypt(byte[] data, byte[] key);

    /**
     * 获取加密算法类型
     *
     * @return 加密算法类型枚举
     */
    EncryptionAlgorithmType getType();

    /**
     * 判断该策略是否支持解密操作
     *
     * @return true 支持解密，false 不支持（哈希算法）
     */
    boolean supportsDecrypt();
}
