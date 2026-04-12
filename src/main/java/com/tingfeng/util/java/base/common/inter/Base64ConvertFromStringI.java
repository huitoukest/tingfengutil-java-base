package com.tingfeng.util.java.base.common.inter;

/**
 * String 转为 base64
 */
@FunctionalInterface
public interface Base64ConvertFromStringI {
	/**
	 * String 转为 base64
	 * @param s 输入的字符串
	 * @return 返回base64字节数组
	 */
	byte[] convertToBase64(String s);
}
