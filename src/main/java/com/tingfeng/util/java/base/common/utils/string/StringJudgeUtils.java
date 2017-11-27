package com.tingfeng.util.java.base.common.utils.string;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 此工具类中均返回boolean类型,对字符串做判断
 * @author huitoukest
 *
 */
public class StringJudgeUtils {
	private static Pattern numericPattern = Pattern.compile("^[0-9\\-]+$");
	private static Pattern numericStringPattern = Pattern.compile("^[0-9\\-\\-]+$");
	private static Pattern floatNumericPattern = Pattern.compile("^[0-9\\-\\.]+$");
	private static Pattern letterPattern = Pattern.compile("^[a-z|A-Z]+$");
	
	public static boolean  isEmpty(String value){
		if(value==null||value.trim().length()<1)
			return true;
		return false;
	}

	/**
	 * 判断对象是否为空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}
	/**
	 * 判断是否数字表示
	 * 
	 * @param src
	 *            源字符串
	 * @return 是否数字的标志
	 */
	public static boolean isNumer(String src) {
		boolean return_value = false;
		if (src != null && src.length() > 0) {
			Matcher m = numericPattern.matcher(src);
			if (m.find()) {
				return_value = true;
			}
		}
		return return_value;
	}

	/**
	 * 判断是否数字表示
	 * 
	 * @param src
	 *            源字符串
	 * @return 是否数字的标志
	 */
	public static boolean isNumericString(String src) {
		boolean return_value = false;
		if (src != null && src.length() > 0) {
			Matcher m = numericStringPattern.matcher(src);
			if (m.find()) {
				return_value = true;
			}
		}
		return return_value;
	}

	/**
	 * 判断是否纯字母组合
	 * 
	 * @param src
	 *            源字符串
	 * @return 是否纯字母组合的标志
	 */
	public static boolean isLetter(String src) {
		boolean return_value = false;
		if (src != null && src.length() > 0) {
			Matcher m = letterPattern.matcher(src);
			if (m.find()) {
				return_value = true;
			}
		}
		return return_value;
	}

	/**
	 * 判断是否浮点数字表示
	 * 
	 * @param src
	 *            源字符串
	 * @return 是否数字的标志
	 */
	public static boolean isFloatNumer(String src) {
		boolean return_value = false;
		if (src != null && src.length() > 0) {
			Matcher m = floatNumericPattern.matcher(src);
			if (m.find()) {
				return_value = true;
			}
		}
		return return_value;
	}
	
	/**
	 * 判断一个字符串是否都为数字
	 * @param strNum
	 * @return
	 */
	public boolean isDigit(String strNum) {
		Pattern pattern = Pattern.compile("[0-9]{1,}");
		Matcher matcher = pattern.matcher((CharSequence) strNum);
		return matcher.matches();
	}
	
	/**
	 * 判断某个字符串是否存在于数组中
	 * 
	 * @param stringArray
	 *            原数组
	 * @param source
	 *            查找的字符串
	 * @return 是否找到
	 */
	public static boolean contains(String[] stringArray, String source) {
		// 转换为list
		List<String> tempList = Arrays.asList(stringArray);

		// 利用list的包含方法,进行判断
		if (tempList.contains(source)) {
			return true;
		} else {
			return false;
		}
	}
	
	
}
