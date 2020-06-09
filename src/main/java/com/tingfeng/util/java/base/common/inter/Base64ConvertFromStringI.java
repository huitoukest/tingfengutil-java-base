package com.tingfeng.util.java.base.common.inter;

/**
 * String 转为 base64
 */
@FunctionalInterface
public interface Base64ConvertFromStringI {
	byte[] convertToBase64(String s);
}
