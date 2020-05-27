package com.tingfeng.util.java.base.common.utils.string;

import java.io.UnsupportedEncodingException;

import com.tingfeng.util.java.base.common.constant.Constants;

/**
 * 字符转码工具类
 * java Properties默认以UNICODE编码读取,如果有中文需要转码
 * @author huitoukest
 *
 */
public class CharSetUtils {
	/**
	 * 支持iso8859-1和GBK,UTF-8自动转为UTF-8
	 * @param content
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String convertToUTF8(String content) throws UnsupportedEncodingException{
		byte[] newContent = null;
		String[] charset = new String[]{Constants.CharSet.UTF8,Constants.CharSet.ISO88591,Constants.CharSet.GBK,
				Constants.CharSet.UNICODE};
		
		for(String encode :charset){
			newContent = content.getBytes(encode);
			if(content.equals(new String(newContent,encode)))
			{
				String tmpContent = new String(newContent,Constants.CharSet.UTF8);
				return tmpContent;
			}
		}
		return content;
	}
	/**
	 * 转换为指定格式
	 * @param content
	 * @param chartType
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String convertToUTF8(String content,String chartType) throws UnsupportedEncodingException{
		byte[] newContent = content.getBytes(chartType);
		String tmpContent = new String(newContent,Constants.CharSet.UTF8);
		return tmpContent;
	}
	
}
