package com.tingfeng.util.java.base.common.utils.string;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern numericPattern = Pattern.compile("^[0-9\\-]+$");
    private static final Pattern numericStringPattern = Pattern.compile("^[0-9\\-\\-]+$");
    private static final Pattern floatNumericPattern = Pattern.compile("^[0-9\\-\\.]+$");
    private static final Pattern letterPattern = Pattern.compile("^[a-z|A-Z]+$");
	public static final String splitStrPattern = ",|，|;|；|、|\\.|。|-|_|\\(|\\)|\\[|\\]|\\{|\\}|\\\\|/| |　|\"";	
	public static final Pattern blankPatten = Pattern.compile("\\s*|\t|\r|\n");
	
	public StringUtils() {
	}
	
	protected static void StringUtilsLog(Object obj){
        System.out.print(obj.toString());
    }  

	/******************************************** 开始判断 *************************************************************/
	public static boolean  isEmpty(String value){
        if(value == null|| value.trim().length() < 1)
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
	
    public static boolean isUpperCase(String str) {
        return match("^[A-Z]+$", str);
    }

    public static boolean match(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
    
    public static boolean containsUpperCase(String word) {
        for(int i = 0; i < word.length(); ++i) {
            char c = word.charAt(i);
            if(Character.isUpperCase(c)) {
                return true;
            }
        }

        return false;
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
 
    /**
     * 判断两个字符串是否相等 如果都为null,或者为空串则判断为相等,否则如果s1=s2则相等
     * 
     * @param s1
     * @param s2
     * @return
     */
    public static boolean equals(String s1, String s2) {
        if (StringUtils.isEmpty(s1) && StringUtils.isEmpty(s2)) {
            return true;
        } else if (!StringUtils.isEmpty(s1) && !StringUtils.isEmpty(s2)) {
            return s1.equals(s2);
        }
        return false;
    }
    
    public static Boolean isCharSequence(Class<?> cls) {
        return Boolean.valueOf(cls != null && CharSequence.class.isAssignableFrom(cls));
    }
    
    public static Boolean isCharSequence(String propertyType) {
        try {
            return isCharSequence(Class.forName(propertyType));
        } catch (ClassNotFoundException var2) {
            return Boolean.valueOf(false);
        }
    }

    public static Boolean isBoolean(Class<?> propertyCls) {
        return Boolean.valueOf(propertyCls != null && (Boolean.TYPE.isAssignableFrom(propertyCls) || Boolean.class.isAssignableFrom(propertyCls)));
    }
    
    public static boolean containsLowerCase(String s) {
        char[] arr$ = s.toCharArray();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            char c = arr$[i$];
            if(Character.isLowerCase(c)) {
                return true;
            }
        }

        return false;
    }
    
	/******************************************** 结束判断 *************************************************************/
	
	
	/******************************************** 开始数据处理 *************************************************************/

    
    public static String firstCharToLower(String rawString) {
        return prefixToLower(rawString, 1);
    }

    public static String prefixToLower(String rawString, int index) {
        String beforeChar = rawString.substring(0, index).toLowerCase();
        String afterChar = rawString.substring(index, rawString.length());
        return beforeChar + afterChar;
    }

    public static String removePrefixAfterPrefixToLower(String rawString, int index) {
        return prefixToLower(rawString.substring(index, rawString.length()), 1);
    }
    
    public static String removeIsPrefixIfBoolean(String propertyName, Class<?> propertyType) {
        if(isBoolean(propertyType).booleanValue() && propertyName.startsWith("is")) {
            String property = propertyName.replaceFirst("is", "");
            if(isEmpty(property)) {
                return propertyName;
            } else {
                String firstCharToLowerStr = firstCharToLower(property);
                return property.equals(firstCharToLowerStr)?propertyName:firstCharToLowerStr;
            }
        } else {
            return propertyName;
        }
    }
    
    /**
     * 
     * @param prefix 属性名称的前缀,如"a."
     * @param nameString "a.b.c.d"这种的完整的类.属性的级联调用方式,默认以点号分割;
     * @return 返回一个字符串数组,0索引保存当前属性/类名称,1索引保存除开索引0和前缀的属性名称
     *            如果没有更多属性,索引0会为null;
     */
    public static String[] getFieldNames(String prefix,String nameString){
        String[] names=new String[2];
        if(nameString.indexOf(prefix)!=0||prefix.length()>=nameString.length())
            return names;
        String nameString2=null;
            nameString2=nameString.substring(prefix.length());
        String[] fieldNameStrings=nameString2.split("\\.");
        if(fieldNameStrings.length<2||StringUtils.isEmpty(fieldNameStrings[1])){
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
    

    /***************************************************************************
     * getHideEmailPrefix - 隐藏邮件地址前缀。
     * 
     * @param email
     *            - EMail邮箱地址 例如: linwenguo@koubei.com 等等...
     * @return 返回已隐藏前缀邮件地址, 如 *********@koubei.com.
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
     **************************************************************************/
    public static String repeat(String src, int num) {
        StringBuilder s = new StringBuilder();
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
     * 把 名=值 参数表转换成字符串 (url + ?a=1&b=2)
     * @param url  url可以为null，为null则返回参数组成的字符串a=1&b=2
     * @param map
     * @return
     */
    public static String getGetUrl(String url,Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        if(null != url) {
            sb.append(url);
        }
        if(null != params && !params.isEmpty()){
            int i = 0;
            for(String key :params.keySet()){
                Object  value =  params.get(key);
                if(value != null) {
                    if (i == 0 && null != url) {
                        sb.append("?");
                    } else {
                        sb.append("&");
                    }
                    sb.append(key);
                    sb.append("=");
                    sb.append(value);
                }
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * 解析字符串返回 名称=值的参数表 (a=1&b=2 => a=1,b=2)
     * 通过解析Get的参数url来得到参数
     * @param str
     * @return
     */
    public static HashMap<String, String> getParamsByGetUrl(String str) {
        if (str != null && !str.equals("") && str.indexOf("=") > 0) {
            HashMap<String,String> result = new HashMap<String,String>();

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
     * 货币转字符串
     * 
     * @param money
     * @param style
     *            样式 [default]要格式化成的格式 such as #.00, #.#
     * @return
     */

    public static String formateToMoneyString(double money, String style) {
        if (style != null) {
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
        return money + "";
    }

    /**
     * 页面中去除字符串中的空格、回车、换行符、制表符
     * @param str
     * @return
     */
    public static String replaceBlank(String str) {
        if (str != null) {
            Matcher m = blankPatten.matcher(str);
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
    public static String getStringByChangCoding(String s, String sourceEncoding, String targetEncoding) {
        String str;
        try {
            if (StringUtils.isNotEmpty(s)) {
                str = new String(s.getBytes(sourceEncoding), targetEncoding);
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
            StringBuilder buf = new StringBuilder(cSrc.length);
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
     * ************************************************************************* 用要通过URL传输的内容进行编码
     * 
     * @param 源字符串
     * @return 经过编码的内容
     ************************************************************************* 
     */
    public static String URLEncode(String src,String encoding) {
        String return_value = "";
        try {
            if (src != null) {
                return_value = URLEncoder.encode(src, encoding);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return_value = src;
        }
        return return_value;
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
        if (!StringUtils.isEmpty(query) && query.indexOf(split2) > 0) {
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
                    if (!StringUtils.isEmpty(name) && value != null) {
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

            if (!StringUtils.isEmpty(name) && value != null) {
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
    public static String replaceSubStr(String str, int start, int len) {
        if (StringUtils.isEmpty(str)) {
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
    
	/******************************************** 结束数据处理 *************************************************************/	
	
}
