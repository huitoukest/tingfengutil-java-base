package com.tingfeng.util.java.base.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * 正则表达式的一些测试
 * @author huitoukest
 *
 */
public class RegExTest {
	public static void main(String[] args) {
	    // 要验证的字符串
	    String str = "service@xsoftlab.net";
	    // 邮箱验证规则
	    String regEx = "[a-zA-Z_]{1,}[0-9]{0,}@(([a-zA-z0-9]-*){1,}\\.){1,3}[a-zA-z\\-]{1,}";
	    // 编译正则表达式
	    Pattern pattern = Pattern.compile(regEx);
	    // 忽略大小写的写法
	    // Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(str);
	    // 字符串是否与正则表达式相匹配
	    boolean rs = matcher.matches();
	    System.out.println(rs);
	}
	@Test
	public void timeReplaceTest() {
		String time = "2017-10:20";
		String regEx = "^(\\d{4})\\D*(\\d{2})\\D*(\\d{2})\\D*"
			    + "((\\d{2})\\D*(\\d{2})\\D*(\\d{2})\\D*(\\d{3})){0,1}$";
		String content = time.replaceFirst(regEx, "$1@$2@$3@$5@$6@$7@$8");
		content = content.replaceAll("@", "\\\n");
		System.out.println(content);
	}
}
