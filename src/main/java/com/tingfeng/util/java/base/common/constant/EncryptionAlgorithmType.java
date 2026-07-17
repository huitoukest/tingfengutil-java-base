package com.tingfeng.util.java.base.common.constant;

/**
 * 加密算法类型枚举
 * 用于标识支持的加密算法类型
 */
public enum EncryptionAlgorithmType {
    /**
     * MD5消息摘要算法（不可逆）
     */
    MD5("MD5", false),
    /**
     * SHA-1消息摘要算法（不可逆）
     */
    SHA1("SHA-1", false),
    /**
     * SHA-256消息摘要算法（不可逆）
     */
    SHA256("SHA-256", false),
    /**
     * SHA-512消息摘要算法（不可逆）
     */
    SHA512("SHA-512", false),
    /**
     * AES对称加密算法（可逆）
     */
    AES("AES", true),
    /**
     * 字符替换编码（非安全加密，用于ID混淆）
     */
    SUBSTITUTION("SUBSTITUTION", true);

    private final String value;
    private final boolean supportsDecrypt;

    EncryptionAlgorithmType(String value, boolean supportsDecrypt) {
        this.value = value;
        this.supportsDecrypt = supportsDecrypt;
    }

    /**
     * 获取算法值
     *
     * @return 算法对应的字符串值
     */
    public String getValue() {
        return value;
    }

    /**
     * 判断该算法是否支持解密
     *
     * @return true 支持解密，false 不支持（哈希算法）
     */
    public boolean supportsDecrypt() {
        return supportsDecrypt;
    }

    /**
     * 根据算法值查找枚举
     *
     * @param value 算法值
     * @return 对应的枚举值，未找到返回null
     */
    public static EncryptionAlgorithmType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (EncryptionAlgorithmType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}
