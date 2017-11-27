package com.tingfeng.util.java.base.common.utils.string;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	
	public static final String splitStrPattern = ",|，|;|；|、|\\.|。|-|_|\\(|\\)|\\[|\\]|\\{|\\}|\\\\|/| |　|\"";	
	
	public StringUtils() {
	}

	 protected static void StringUtilsLog(Object obj){
		 System.out.print(obj.toString());
	 }	
		/**
		 * 
		 * @param prefix 属性名称的前缀,如"a."
		 * @param nameString "a.b.c.d"这种的完整的类.属性的级联调用方式,默认以点号分割;
		 * @return 返回一个字符串数组,0索引保存当前属性/类名称,1索引保存除开索引0和前缀的属性名称
		 * 		      如果没有更多属性,索引0会为null;
		 */
		public static String[] getFieldNames(String prefix,String nameString){
			String[] names=new String[2];
			if(nameString.indexOf(prefix)!=0||prefix.length()>=nameString.length())
				return names;
			String nameString2=null;
				nameString2=nameString.substring(prefix.length());
			String[] fieldNameStrings=nameString2.split("\\.");
			if(fieldNameStrings.length<2||StringJudgeUtils.isEmpty(fieldNameStrings[1])){
				names[1]=null;
			}else{
				names[1]=nameString2.substring(fieldNameStrings[0].length()+1);
			}
			names[0]=fieldNameStrings[0];
			return names;
		}
	 public static String getSubString(String subject, int size) {
			if (subject != null && subject.length() > size) {
				subject = subject.substring(0, size);
			}
			return subject;
		}
		/**
		 * 截取字符串　超出的字符用symbol代替 　　
		 * 
		 * @param len
		 *            　字符串长度　长度计量单位为一个GBK汉字　　两个英文字母计算为一个单位长度
		 * @param str
		 * @param symbol
		 * @param charset字符编码
		 * @return
		 */
		public static String getLimitLengthString(String str, int len, String symbol,String charset) {
			int iLen = len * 2;
			int counterOfDoubleByte = 0;
			String strRet = "";
			try {
				if (str != null) {
					byte[] b = str.getBytes(charset);
					if (b.length <= iLen) {
						return str;
					}
					for (int i = 0; i < iLen; i++) {
						if (b[i] < 0) {
							counterOfDoubleByte++;
						}
					}
					if (counterOfDoubleByte % 2 == 0) {
						strRet = new String(b, 0, iLen,charset) + symbol;
						return strRet;
					} else {
						strRet = new String(b, 0, iLen - 1, charset) + symbol;
						return strRet;
					}
				} else {
					return "";
				}
			} catch (Exception ex) {
				return str.substring(0, len);
			} finally {
				strRet = null;
			}
		}

		/**
		 * 截取字符串　超出的字符用symbol代替 　　
		 * 
		 * @param len
		 *            　字符串长度　长度计量单位为一个GBK汉字　　两个英文字母计算为一个单位长度
		 * @param str
		 * @param symbol
		 * @return12
		 */
		public static String getLimitLengthString(String str, int len) {
			return getLimitLengthString(str, len, "...","UTF-8");
		}	

		/**
		 * 取得字符串的实际长度（考虑了汉字的情况）
		 * 
		 * @param SrcStr
		 *            源字符串
		 * @return 字符串的实际长度
		 */
		public static int getStringLength(String SrcStr) {
			int return_value = 0;
			if (SrcStr != null) {
				char[] theChars = SrcStr.toCharArray();
				for (int i = 0; i < theChars.length; i++) {
					return_value += (theChars[i] <= 255) ? 1 : 2;
				}
			}
			return return_value;
		}

		/**
		 * 检查数据串中是否包含非法字符集
		 * 
		 * @param str
		 * @return [true]|[false] 包含|不包含
		 */
		public static boolean check(String str) {
			String sIllegal = "'\"";
			int len = sIllegal.length();
			if (null == str)
				return false;
			for (int i = 0; i < len; i++) {
				if (str.indexOf(sIllegal.charAt(i)) != -1)
					return true;
			}

			return false;
		}

		/***************************************************************************
		 * getHideEmailPrefix - 隐藏邮件地址前缀。
		 * 
		 * @param email
		 *            - EMail邮箱地址 例如: linwenguo@koubei.com 等等...
		 * @return 返回已隐藏前缀邮件地址, 如 *********@koubei.com.
		 * @version 1.0 (2006.11.27) Wilson Lin
		 **************************************************************************/
		public static String getHideEmailPrefix(String email) {
			if (null != email) {
				int index = email.lastIndexOf('@');
				if (index > 0) {
					email = repeat("*", index).concat(email.substring(index));
				}
			}
			return email;
		}

		/***************************************************************************
		 * repeat - 通过源字符串重复生成N次组成新的字符串。
		 * 
		 * @param src
		 *            - 源字符串 例如: 空格(" "), 星号("*"), "浙江" 等等...
		 * @param num
		 *            - 重复生成次数
		 * @return 返回已生成的重复字符串
		 * @version 1.0 (2006.10.10) Wilson Lin
		 **************************************************************************/
		public static String repeat(String src, int num) {
			StringBuffer s = new StringBuffer();
			for (int i = 0; i < num; i++)
				s.append(src);
			return s.toString();
		}
		/**
		 * 格式化一个float
		 * 
		 * @param format
		 *            要格式化成的格式 such as #.00, #.#
		 */

		public static String formatFloat(float f, String format) {
			DecimalFormat df = new DecimalFormat(format);
			return df.format(f);
		}
		/**
		 * 把 名=值 参数表转换成字符串 (a=1,b=2 =>a=1&b=2)
		 * 
		 * @param map
		 * @return
		 */
		public static String linkedHashMapToString(LinkedHashMap<String, String> map) {
			if (map != null && map.size() > 0) {
				String result = "";
				Iterator<String> it = map.keySet().iterator();
				while (it.hasNext()) {
					String name = (String) it.next();
					String value = (String) map.get(name);
					result += (result.equals("")) ? "" : "&";
					result += String.format("%s=%s", name, value);
				}
				return result;
			}
			return null;
		}

		/**
		 * 解析字符串返回 名称=值的参数表 (a=1&b=2 => a=1,b=2)
		 * 
		 * @see test.koubei.util.StringUtilTest#testParseStr()
		 * @param str
		 * @return
		 */
		public static LinkedHashMap<String, String> toLinkedHashMap(String str) {
			if (str != null && !str.equals("") && str.indexOf("=") > 0) {
				LinkedHashMap<String,String> result = new LinkedHashMap<String,String>();

				String name = null;
				String value = null;
				int i = 0;
				while (i < str.length()) {
					char c = str.charAt(i);
					switch (c) {
					case 61: // =
						value = "";
						break;
					case 38: // &
						if (name != null && value != null && !name.equals("")) {
							result.put(name, value);
						}
						name = null;
						value = null;
						break;
					default:
						if (value != null) {
							value = (value != null) ? (value + c) : "" + c;
						} else {
							name = (name != null) ? (name + c) : "" + c;
						}
					}
					i++;

				}

				if (name != null && value != null && !name.equals("")) {
					result.put(name, value);
				}

				return result;

			}
			return null;
		}

		/**
		 * 根据输入的多个解释和下标返回一个值
		 * 
		 * @param captions
		 *            例如:"无,爱干净,一般,比较乱"
		 * @param index
		 *            1
		 * @return 一般
		 */
		public static String getCaption(String captions, int index) {
			if (index > 0 && captions != null && !captions.equals("")) {
				String[] ss = captions.split(",");
				if (ss != null && ss.length > 0 && index < ss.length) {
					return ss[index];
				}
			}
			return null;
		}

		/**
		 * 数字转字符串,如果num<=0 则输出"";
		 * 
		 * @param num
		 * @return
		 */
		public static String numberToString(Object num) {
			if (num == null) {
				return null;
			} else if (num instanceof Integer && (Integer) num > 0) {
				return Integer.toString((Integer) num);
			} else if (num instanceof Long && (Long) num > 0) {
				return Long.toString((Long) num);
			} else if (num instanceof Float && (Float) num > 0) {
				return Float.toString((Float) num);
			} else if (num instanceof Double && (Double) num > 0) {
				return Double.toString((Double) num);
			} else {
				return "";
			}
		}

		/**
		 * 货币转字符串
		 * 
		 * @param money
		 * @param style
		 *            样式 [default]要格式化成的格式 such as #.00, #.#
		 * @return
		 */

		public static String moneyToString(Object money, String style) {
			if (money != null && style != null && (money instanceof Double || money instanceof Float)) {
				Double num = (Double) money;

				if (style.equalsIgnoreCase("default")) {
					// 缺省样式 0 不输出 ,如果没有输出小数位则不输出.0
					if (num == 0) {
						// 不输出0
						return "";
					} else if ((num * 10 % 10) == 0) {
						// 没有小数
						return Integer.toString((int) num.intValue());
					} else {
						// 有小数
						return num.toString();
					}

				} else {
					DecimalFormat df = new DecimalFormat(style);
					return df.format(num);
				}
			}
			return null;
		}

		/**
		 * 判断两个字符串是否相等 如果都为null,或者为空串则判断为相等,否则如果s1=s2则相等
		 * 
		 * @param s1
		 * @param s2
		 * @return
		 */
		public static boolean equals(String s1, String s2) {
			if (StringJudgeUtils.isEmpty(s1) && StringJudgeUtils.isEmpty(s2)) {
				return true;
			} else if (!StringJudgeUtils.isEmpty(s1) && !StringJudgeUtils.isEmpty(s2)) {
				return s1.equals(s2);
			}
			return false;
		}

		/**
		 * 页面中去除字符串中的空格、回车、换行符、制表符
		 * 
		 * @author shazao
		 * @date 2007-08-17
		 * @param str
		 * @return
		 */
		public static String replaceBlank(String str) {
			if (str != null) {
				Pattern p = Pattern.compile("\\s*|\t|\r|\n");
				Matcher m = p.matcher(str);
				str = m.replaceAll("");
			}
			return str;
		}
		
		/**
		 * 
		 * 转换编码
		 * 
		 * @param s
		 *            源字符串
		 * @param fencode
		 *            源编码格式
		 * @param bencode
		 *            目标编码格式
		 * @return 目标编码
		 */
		public static String getStringByChangCoding(String s, String fencode, String bencode) {
			String str;
			try {
				if (StringJudgeUtils.isNotEmpty(s)) {
					str = new String(s.getBytes(fencode), bencode);
				} else {
					str = "";
				}
				return str;
			} catch (UnsupportedEncodingException e) {
				return s;
			}
		}
	    /**
		 * 
		 * 字符串替换
		 * 
		 * @param str
		 *            源字符串
		 * @param regEx
		 *            正则表达式样式
		 * @param sd
		 *            替换文本
		 * @return 结果串
		 */
		public static String getStringByReplaceByReg(String str, String regEx, String sd) {
			Pattern p = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(str);
			str = m.replaceAll(sd);
			return str;
		}
		
		/**
		 * 
		 * 得到字符串的子串位置序列
		 * 
		 * @param str
		 *            字符串
		 * @param sub
		 *            子串
		 * @return 字符串的子串位置序列
		 */
		public static int[] getPositionBySubString(String str, String sub) {
			String[] sp = null;
			int l = sub.length();
			sp = splitString(str, sub);
			if (sp == null) {
				return null;
			}
			int[] ip = new int[sp.length - 1];
			for (int i = 0; i < sp.length - 1; i++) {
				ip[i] = sp[i].length() + l;
				if (i != 0) {
					ip[i] += ip[i - 1];
				}
			}
			return ip;
		}

		/**
		 * 
		 * 根据正则表达式分割字符串
		 * 
		 * @param str
		 *            源字符串
		 * @param regEx
		 *            正则表达式
		 * @return 目标字符串组
		 */
		public static String[] splitString(String str, String regEx) {
			Pattern p = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
			String[] sp = p.split(str);
			return sp;
		}

		/**
		 * 根据正则表达式提取字符串,相同的字符串只返回一个
		 * 
		 * @param str源字符串
		 * @param pattern
		 *            正则表达式
		 * @return 目标字符串数据组
		 ************************************************************************* 
		 */

		// ★传入一个字符串，把符合pattern格式的字符串放入字符串数组
		// java.util.regex是一个用正则表达式所订制的模式来对字符串进行匹配工作的类库包
		public static String[] getStringArrayByPattern(String str, String pattern) {
			Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			Matcher matcher = p.matcher(str);
			// 范型
			Set<String> result = new HashSet<String>();// 目的是：相同的字符串只返回一个。。。 不重复元素
			// boolean find() 尝试在目标字符串里查找下一个匹配子串。
			while (matcher.find()) {
				for (int i = 0; i < matcher.groupCount(); i++) { // int groupCount()
																	// 返回当前查找所获得的匹配组的数量。
					// org.jeecgframework.core.util.LogUtil.info(matcher.group(i));
					result.add(matcher.group(i));

				}
			}
			String[] resultStr = null;
			if (result.size() > 0) {
				resultStr = new String[result.size()];
				return result.toArray(resultStr);// 将Set result转化为String[] resultStr
			}
			return resultStr;

		}

		/**
		 * 得到第一个b,e之间的字符串,并返回e后的子串
		 * 
		 * @param s
		 *            源字符串
		 * @param b
		 *            标志开始
		 * @param e
		 *            标志结束
		 * @return b,e之间的字符串
		 */

		/*
		 * String aaa="abcdefghijklmn"; String[] bbb=StringProcessor.midString(aaa, "b","l"); org.jeecgframework.core.util.LogUtil.info("bbb[0]:"+bbb[0]);//cdefghijk org.jeecgframework.core.util.LogUtil.info("bbb[1]:"+bbb[1]);//lmn ★这个方法是得到第二个参数和第三个参数之间的字符串,赋给元素0;然后把元素0代表的字符串之后的,赋给元素1
		 */

		/*
		 * String aaa="abcdefgllhijklmn5465"; String[] bbb=StringProcessor.midString(aaa, "b","l"); //ab cdefg llhijklmn5465 // 元素0 元素1
		 */
		public static String[] midString(String s, String b, String e) {
			int i = s.indexOf(b) + b.length();
			int j = s.indexOf(e, i);
			String[] sa = new String[2];
			if (i < b.length() || j < i + 1 || i > j) {
				sa[1] = s;
				sa[0] = null;
				return sa;
			} else {
				sa[0] = s.substring(i, j);
				sa[1] = s.substring(j);
				return sa;
			}
		}

		/**
		 * 带有前一次替代序列的正则表达式替代
		 * 
		 * @param s
		 * @param pf
		 * @param pb
		 * @param start
		 * @return
		 */
		public static String stringReplace(String s, String pf, String pb, int start) {
			Pattern pattern_hand = Pattern.compile(pf);
			Matcher matcher_hand = pattern_hand.matcher(s);
			int gc = matcher_hand.groupCount();
			int pos = start;
			String sf1 = "";
			String sf2 = "";
			String sf3 = "";
			int if1 = 0;
			String strr = "";
			while (matcher_hand.find(pos)) {
				sf1 = matcher_hand.group();
				if1 = s.indexOf(sf1, pos);
				if (if1 >= pos) {
					strr += s.substring(pos, if1);
					pos = if1 + sf1.length();
					sf2 = pb;
					for (int i = 1; i <= gc; i++) {
						sf3 = "\\" + i;
						sf2 = replaceAll(sf2, sf3, matcher_hand.group(i));
					}
					strr += sf2;
				} else {
					return s;
				}
			}
			strr = s.substring(0, start) + strr;
			return strr;
		}

		/**
		 * 存文本替换
		 * 
		 * @param s
		 *            源字符串
		 * @param sf
		 *            子字符串
		 * @param sb
		 *            替换字符串
		 * @return 替换后的字符串
		 */
		public static String replaceAll(String s, String sf, String sb) {
			int i = 0, j = 0;
			int l = sf.length();
			boolean b = true;
			boolean o = true;
			String str = "";
			do {
				j = i;
				i = s.indexOf(sf, j);
				if (i > j) {
					str += s.substring(j, i);
					str += sb;
					i += l;
					o = false;
				} else {
					str += s.substring(j);
					b = false;
				}
			} while (b);
			if (o) {
				str = s;
			}
			return str;
		}
		
		// 字符串的替换
		public static String replace(String strSource, String strOld, String strNew) {
			if (strSource == null) {
				return null;
			}
			int i = 0;
			if ((i = strSource.indexOf(strOld, i)) >= 0) {
				char[] cSrc = strSource.toCharArray();
				char[] cTo = strNew.toCharArray();
				int len = strOld.length();
				StringBuffer buf = new StringBuffer(cSrc.length);
				buf.append(cSrc, 0, i).append(cTo);
				i += len;
				int j = i;
				while ((i = strSource.indexOf(strOld, i)) > 0) {
					buf.append(cSrc, j, i - j).append(cTo);
					i += len;
					j = i;
				}
				buf.append(cSrc, j, cSrc.length - j);
				return buf.toString();
			}
			return strSource;
		}

		/**
		 * 判断是否与给定字符串样式匹配
		 * 
		 * @param str
		 *            字符串
		 * @param pattern
		 *            正则表达式样式
		 * @return 是否匹配是true,否false
		 */
		public static boolean isMatch(String str, String pattern) {
			Pattern pattern_hand = Pattern.compile(pattern);
			Matcher matcher_hand = pattern_hand.matcher(str);
			boolean b = matcher_hand.matches();
			return b;
		}

		/**
		 * 截取字符串
		 * 
		 * @param s
		 *            源字符串
		 * @param jmp
		 *            跳过jmp
		 * @param sb
		 *            取在sb
		 * @param se
		 *            于se
		 * @return 之间的字符串
		 */
		public static String getSubStringExe(String s, String jmp, String sb, String se) {
			if (StringJudgeUtils.isEmpty(s)) {
				return "";
			}
			int i = s.indexOf(jmp);
			if (i >= 0 && i < s.length()) {
				s = s.substring(i + 1);
			}
			i = s.indexOf(sb);
			if (i >= 0 && i < s.length()) {
				s = s.substring(i + 1);
			}
			if ("".equals(se)) {
				return s;
			} else {
				i = s.indexOf(se);
				if (i >= 0 && i < s.length()) {
					s = s.substring(i + 1);
				}
				return s;
			}
		}

		/**
		 * ************************************************************************* 用要通过URL传输的内容进行编码
		 * 
		 * @param 源字符串
		 * @return 经过编码的内容
		 ************************************************************************* 
		 */
		public static String URLEncode(String src) {
			String return_value = "";
			try {
				if (src != null) {
					return_value = URLEncoder.encode(src, "GBK");

				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return_value = src;
			}

			return return_value;
		}

		/**
		 * *************************************************************************
		 * 
		 * @author 
		 * @param 传入
		 * @return 经过解码的内容
		 ************************************************************************* 
		 */
		public static String getGBK(String str) {

			return transfer(str);
		}

		public static String transfer(String str) {
			Pattern p = Pattern.compile("&#\\d+;");
			Matcher m = p.matcher(str);
			while (m.find()) {
				String old = m.group();
				str = str.replaceAll(old, getChar(old));
			}
			return str;
		}

		public static String getChar(String str) {
			String dest = str.substring(2, str.length() - 1);
			char ch = (char) Integer.parseInt(dest);
			return "" + ch;
		}
		
		/**
		 * 判断文字内容重复
		 */
		public static boolean isContentRepeat(String content) {
			int similarNum = 0;
			int forNum = 0;
			int subNum = 0;
			int thousandNum = 0;
			String startStr = "";
			String nextStr = "";
			boolean result = false;
			float endNum = (float) 0.0;
			if (content != null && content.length() > 0) {
				if (content.length() % 1000 > 0)
					thousandNum = (int) Math.floor(content.length() / 1000) + 1;
				else
					thousandNum = (int) Math.floor(content.length() / 1000);
				if (thousandNum < 3)
					subNum = 100 * thousandNum;
				else if (thousandNum < 6)
					subNum = 200 * thousandNum;
				else if (thousandNum < 9)
					subNum = 300 * thousandNum;
				else
					subNum = 3000;
				for (int j = 1; j < subNum; j++) {
					if (content.length() % j > 0)
						forNum = (int) Math.floor(content.length() / j) + 1;
					else
						forNum = (int) Math.floor(content.length() / j);
					if (result || j >= content.length())
						break;
					else {
						for (int m = 0; m < forNum; m++) {
							if (m * j > content.length() || (m + 1) * j > content.length() || (m + 2) * j > content.length())
								break;
							startStr = content.substring(m * j, (m + 1) * j);
							nextStr = content.substring((m + 1) * j, (m + 2) * j);
							if (startStr.equals(nextStr)) {
								similarNum = similarNum + 1;
								endNum = (float) similarNum / forNum;
								if (endNum > 0.4) {
									result = true;
									break;
								}
							} else
								similarNum = 0;
						}
					}
				}
			}
			return result;
		}


		/**
		

		/**
		 * 全角括号转为半角
		 * @param str
		 * @return
		 */
		public static String replaceBracketStr(String str) {
			if (str != null && str.length() > 0) {
				str = str.replaceAll("（", "(");
				str = str.replaceAll("）", ")");
			}
			return str;
		}

		/**
		 * 解析字符串返回map键值对(例：a=1&b=2 => a=1,b=2)
		 * 
		 * @param query
		 *            源参数字符串
		 * @param split1
		 *            键值对之间的分隔符（例：&）
		 * @param split2
		 *            key与value之间的分隔符（例：=）
		 * @param dupLink
		 *            重复参数名的参数值之间的连接符，连接后的字符串作为该参数的参数值，可为null null：不允许重复参数名出现，则靠后的参数值会覆盖掉靠前的参数值。
		 * @return map
		 * @author sky
		 */
		public static Map<String, String> parseQuery(String query, char split1, char split2, String dupLink) {
			if (!StringJudgeUtils.isEmpty(query) && query.indexOf(split2) > 0) {
				Map<String, String> result = new HashMap<String,String>();

				String name = null;
				String value = null;
				String tempValue = "";
				int len = query.length();
				for (int i = 0; i < len; i++) {
					char c = query.charAt(i);
					if (c == split2) {
						value = "";
					} else if (c == split1) {
						if (!StringJudgeUtils.isEmpty(name) && value != null) {
							if (dupLink != null) {
								tempValue = result.get(name);
								if (tempValue != null) {
									value += dupLink + tempValue;
								}
							}
							result.put(name, value);
						}
						name = null;
						value = null;
					} else if (value != null) {
						value += c;
					} else {
						name = (name != null) ? (name + c) : "" + c;
					}
				}

				if (!StringJudgeUtils.isEmpty(name) && value != null) {
					if (dupLink != null) {
						tempValue = result.get(name);
						if (tempValue != null) {
							value += dupLink + tempValue;
						}
					}
					result.put(name, value);
				}

				return result;
			}
			return null;
		}
		/**
		 * 获取从start开始用*替换len个长度后的字符串
		 * 
		 * @param str
		 *            要替换的字符串
		 * @param start
		 *            开始位置
		 * @param len
		 *            长度
		 * @return 替换后的字符串
		 */
		public static String getMaskStr(String str, int start, int len) {
			if (StringJudgeUtils.isEmpty(str)) {
				return str;
			}
			if (str.length() < start) {
				return str;
			}

			// 获取*之前的字符串
			String ret = str.substring(0, start);

			// 获取最多能打的*个数
			int strLen = str.length();
			if (strLen < start + len) {
				len = strLen - start;
			}

			// 替换成*
			for (int i = 0; i < len; i++) {
				ret += "*";
			}

			// 加上*之后的字符串
			if (strLen > start + len) {
				ret += str.substring(start + len);
			}

			return ret;
		}

		/**
		 * 根据传入的分割符号,把传入的字符串分割为List字符串
		 * 
		 * @param slipStr
		 *            分隔的字符串
		 * @param src
		 *            字符串
		 * @return 列表
		 */
		public static List<String> stringToStringListBySlipStr(String slipStr, String src) {

			if (src == null)
				return null;
			List<String> list = new ArrayList<String>();
			String[] result = src.split(slipStr);
			for (int i = 0; i < result.length; i++) {
				list.add(result[i]);
			}
			return list;
		}

		
		public static String getProperty(String property) {
			if (property.contains("_")) {
				return property.replaceAll("_", "\\.");
			}
			return property;
		}	
}
