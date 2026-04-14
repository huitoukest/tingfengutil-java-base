package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.common.constant.Constants;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;

/**
 * Base64编码解码工具类
 * 使用JDK自带的Base64工具实现，支持标准Base64和URL安全的Base64编码
 * 
 * 主要功能：
 * - 标准Base64编码解码
 * - URL安全的Base64编码解码（自动去除填充字符）
 * - 自动识别Base64类型进行解码
 * 
 * @author huitoukest
 */
public class Base64Utils {

	/**
	 * 使用指定字符集对字符串进行Base64编码
	 * @param content 要编码的字符串
	 * @param charset 字符集
	 * @return Base64编码后的字符串
	 */
	public static String enCode(String content, Charset charset) {
		if (content == null) {
			return null;
		}
		return enCode(content.getBytes(charset));
	}

	/**
	 * 使用UTF-8字符集对字符串进行Base64编码
	 * @param content 要编码的字符串
	 * @return Base64编码后的字符串
	 */
	public static String enCode(String content) {
		if (content == null) {
			return null;
		}
		return enCode(content.getBytes(Charset.forName(Constants.CharSet.UTF8)));
	}

	/**
	 * 对字节数组进行Base64编码
	 * @param content 要编码的字节数组
	 * @return Base64编码后的字符串
	 */
	public static String enCode(byte[] content) {
		if (content == null) {
			return null;
		}
		return Base64.getEncoder().encodeToString(content);
	}

	/**
	 * 对ByteBuffer进行Base64编码
	 * @param buffer 要编码的ByteBuffer
	 * @return 编码后的ByteBuffer
	 */
	public static ByteBuffer enCodeToByteBuffer(ByteBuffer buffer) {
		if (buffer == null) {
			return null;
		}
		return Base64.getEncoder().encode(buffer);
	}

	/**
	 * 对字节数组进行Base64编码，返回字节数组
	 * @param content 要编码的字节数组
	 * @return Base64编码后的字节数组
	 */
	public static byte[] enCodeToByte(byte[] content) {
		if (content == null) {
			return null;
		}
		return Base64.getEncoder().encode(content);
	}

	/**
	 * 自动判断是否为URL安全的Base64字符串，并进行解码
	 * @param content Base64编码的字节数组
	 * @return 解码后的原始字节数组
	 */
	public static byte[] deCode(byte[] content) {
		return autoDecodeBase64String(content);
	}

	/**
	 * 自动判断是否为URL安全的Base64字符串，并进行解码
	 * @param content Base64编码的字符串
	 * @return 解码后的原始字节数组
	 */
	public static byte[] deCode(String content) {
		if (content == null) {
			return null;
		}
		return deCode(content.getBytes(Charset.forName(Constants.CharSet.UTF8)));
	}

	/**
	 * 自动判断是否为URL安全的Base64字符串，并进行解码
	 * @param content Base64编码的字符串
	 * @return 解码后的原始字符串
	 */
	public static String deCodeToString(String content) {
		if (content == null) {
			return null;
		}
		return new String(deCode(content), Charset.forName(Constants.CharSet.UTF8));
	}

	/**
	 * 将字节数组编码为URL安全的Base64字符串
	 * URL安全的Base64会将'+'替换为'-'，'/'替换为'_'，并去除填充字符'='
	 * 
	 * @param bytes 要编码的字节数组
	 * @return URL安全的Base64字符串
	 */
	public static String enCodeBase64UrlSafeString(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		if (bytes.length == 0) {
			return "";
		}
		byte[] base64Bytes = Base64.getUrlEncoder().encode(bytes);
		// 把等号全部替换为空字符串，解码的时候再自行恢复
		int end = base64Bytes.length - 1;
		for (int i = end; i >= 0; i--) {
			if (base64Bytes[i] != '=') {
				end = i;
				break;
			}
		}
		return new String(base64Bytes, 0, end + 1);
	}

	/**
	 * 将字符串编码为URL安全的Base64字符串
	 * 使用UTF-8字符集
	 * 
	 * @param originString 原始字符串
	 * @return URL安全的Base64字符串
	 */
	public static String enCodeBase64UrlSafeString(String originString) {
		if (originString == null) {
			return null;
		}
		return enCodeBase64UrlSafeString(originString.getBytes(Charset.forName(Constants.CharSet.UTF8)));
	}

	/**
	 * 自动判断是否为URL安全的Base64字符串，并进行解码
	 * @param base64UrlSafeString URL安全的Base64字符串
	 * @return 解码后的原始字符串
	 */
	public static String deCodeBase64UrlSafeString(String base64UrlSafeString) {
		if (base64UrlSafeString == null) {
			return null;
		}
		return deCodeBase64UrlSafeString(base64UrlSafeString.getBytes(Charset.forName(Constants.CharSet.UTF8)));
	}

	/**
	 * 自动判断是否为URL安全的Base64字符串，并进行解码
	 * @param bytes Base64编码的字节数组
	 * @return 解码后的原始字符串
	 */
	public static String deCodeBase64UrlSafeString(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		return new String(autoDecodeBase64String(bytes), Charset.forName(Constants.CharSet.UTF8));
	}

	/**
	 * 通过自动判断字符，来决定使用URL安全解码器还是普通Base64解码器
	 * 判断逻辑：
	 * 1. 如果字节数组长度不是4的倍数，则使用URL安全解码器
	 * 2. 如果字节数组中包含'_'或'-'字符，则使用URL安全解码器
	 * 3. 否则使用普通Base64解码器
	 * 
	 * @param bytes Base64编码的字节数组
	 * @return 解码后的原始字节数组
	 */
	private static byte[] autoDecodeBase64String(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		int mod4 = bytes.length % 4;
		boolean useSafeUrlDecoder = mod4 > 0;
		
		// 如果长度不是4的倍数，需要填充'='字符
		if (useSafeUrlDecoder) {
			int originLength = bytes.length;
			bytes = Arrays.copyOf(bytes, originLength + 4 - mod4);
			for (int i = originLength; i < bytes.length; i++) {
				bytes[i] = '=';
			}
		}
		
		// 检查是否包含URL安全字符
		if (!useSafeUrlDecoder) {
			for (int i = 0; i < bytes.length; i++) {
				byte value = bytes[i];
				if (value == '_' || value == '-') {
					useSafeUrlDecoder = true;
					break;
				}
			}
		}
		
		if (useSafeUrlDecoder) {
			return Base64.getUrlDecoder().decode(bytes);
		} else {
			return Base64.getDecoder().decode(bytes);
		}
	}

	/**
	 * 将标准Base64字符串转换为URL安全的Base64字符串
	 * 转换规则：
	 * - '+' 替换为 '-'
	 * - '/' 替换为 '_'
	 * - 去除所有 '=' 填充字符
	 * 
	 * @param base64String 标准Base64字符串
	 * @return URL安全的Base64字符串
	 */
	public static String encodeBase64ToBase64UrlSafeString(String base64String) {
		if (base64String == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(base64String.length());
		char[] chars = base64String.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			switch (c) {
				case '+':
					sb.append('-');
					break;
				case '/':
					sb.append('_');
					break;
				case '=':
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 将URL安全的Base64字符串转换为标准Base64字符串
	 * 转换规则：
	 * - '-' 替换为 '+'
	 * - '_' 替换为 '/'
	 * - 根据长度自动添加 '=' 填充字符
	 * 
	 * @param safeBase64UrlStr URL安全的Base64字符串
	 * @return 标准Base64字符串
	 */
	public static String decodeBase64SafUrlStringToBase64(String safeBase64UrlStr) {
		if (safeBase64UrlStr == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(safeBase64UrlStr.length() + 4);
		char[] chars = safeBase64UrlStr.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			switch (c) {
				case '-':
					sb.append('+');
					break;
				case '_':
					sb.append('/');
					break;
				default:
					sb.append(c);
			}
		}
		// 添加填充字符
		int mod4 = safeBase64UrlStr.length() % 4;
		if (mod4 > 0) {
			sb.append("====".substring(mod4));
		}
		return sb.toString();
	}
}