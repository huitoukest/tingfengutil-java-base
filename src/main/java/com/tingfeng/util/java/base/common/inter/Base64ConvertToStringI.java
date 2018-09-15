package com.tingfeng.util.java.base.common.inter;

/**
 * base64 转为 String
 */
@FunctionalInterface
public interface Base64ConvertToStringI {
	/**
	 * 将base64数组从startIndex开始翻译为String，默认UTF-8编码
	 * @param base64
	 * @param startIndex
	 * @return
	 */
	 String convertToString(byte[] base64,int startIndex);
}
