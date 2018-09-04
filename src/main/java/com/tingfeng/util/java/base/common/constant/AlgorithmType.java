package com.tingfeng.util.java.base.common.constant;

/**
 * 常见的数字签名sha和md系列的type
 */
public enum AlgorithmType  {
    SHA512("SHA-512"),SHAMAC512("HmacSHA512"),SHA256("SHA-256"),SHAMAC256("HmacSHA256"),
    SHA1("SHA-1");
    String value;
    AlgorithmType(String value){
        this.value = value;
    }
    public String getValue(){
        return this.value;
    }
}
