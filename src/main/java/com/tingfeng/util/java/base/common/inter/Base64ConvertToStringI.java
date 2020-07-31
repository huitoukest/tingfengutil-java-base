package com.tingfeng.util.java.base.common.inter;

/**
 * base64 转为 String
 */
@FunctionalInterface
public interface Base64ConvertToStringI {
	/**
	 * 将base64数组从startIndex开始翻译为String，默认UTF-8编码
	 * @param base64 输入base64字节数组
	 * @param startIndex 读取数据时对应数组开始的索引
	 * @return base64还原的字符串
	 */
	 String convertToString(byte[] base64,int startIndex);
}
