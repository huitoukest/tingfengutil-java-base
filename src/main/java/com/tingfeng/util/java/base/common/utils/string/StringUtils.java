package com.tingfeng.util.java.base.common.utils.string;

import com.tingfeng.util.java.base.common.constant.Constants;
import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.helper.FixedPoolHelper;
import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionROne;
import com.tingfeng.util.java.base.common.utils.RegExpUtils;
import com.tingfeng.util.java.base.common.utils.reflect.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author huitoukest
 * String工具类
 */
public class StringUtils {
    private final static Field STRING_VALUE_FIELD = ReflectUtils.getField(String.class,"value",true);
    private final static Logger logger = LoggerFactory.getLogger(StringUtils.class);
    private final static int BUFFER_SIZE = 4096;
    /**
     * 默认的StringBuilder的数量
     */
    private static final int DEFAULT_MAX_SB_SIZE = 16;
    private static final int DEFAULT_MAX_SB_LENGTH = 32;
    /**
     * 公共的StringBuilder的资源，用于多线程时复用对象提高效率
     */
    private static final FixedPoolHelper<StringBuilder> stringBuilderPool = new FixedPoolHelper<>(DEFAULT_MAX_SB_SIZE, () -> new StringBuilder(), (sb) -> {
        int len = sb.length();
        if (len > DEFAULT_MAX_SB_LENGTH) {
            sb.delete(DEFAULT_MAX_SB_LENGTH, len);
        }
        sb.setLength(0);
    });

    private StringUtils() {

    }

    /**
     * 将InputStream转换成String
     *
     * @param in InputStream
     * @return String
     * @throws Exception
     */
    public static String getStringByStream(InputStream in) {
        return getStringByStream(in, Constants.CharSet.UTF8);
    }

    /**
     * 将InputStream转换成某种字符编码的String
     *
     * @param in
     * @param encoding
     * @return
     * @throws Exception
     */
    public static String getStringByStream(InputStream in, String encoding) {
        String string = null;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        try {
            while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
                outStream.write(data, 0, count);
            }
        } catch (IOException e) {
            throw new BaseException(e);
        }
        data = null; //显示数据回收
        try {
            string = new String(outStream.toByteArray(), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new BaseException(e);
        }
        return string;
    }

    /**
     * 将byte数组转换成String
     *
     * @param in
     * @param charEncode
     * @return
     * @throws UnsupportedEncodingException
     * @throws Exception
     */
    public static String getStringByBytes(byte[] in, String charEncode) throws UnsupportedEncodingException {
        String string = new String(in, charEncode);
        return string;
    }

    public static String getStringByBytes(byte[] in) throws UnsupportedEncodingException {
        return getStringByBytes(in, "UTF-8");
    }

    /**
     * souceString默认使用的是逗号作为分隔符号
     * 去掉首尾的空白字符和symbol字符串
     *
     * @param souceString
     * @param symbol
     * @return
     */
    public static String trimSymbol(String souceString, String symbol) {
        souceString = souceString.trim();
        if (souceString.length() < 1) return souceString;
        if (souceString.indexOf(symbol) == 0) {
            souceString = souceString.substring(symbol.length());
        }
        if (souceString.lastIndexOf(symbol) == souceString.length() - symbol.length()) {
            souceString = souceString.substring(0, souceString.length() - symbol.length());
        }
        return souceString;
    }

    /**
     * 首字母小写
     *
     * @param srcString
     * @return
     */
    public static String toLowerFirstChar(String srcString) {
        return stringBuilderPool.run((sb) -> {
            sb.append(Character.toLowerCase(srcString.charAt(0)));
            sb.append(srcString.substring(1));
            return sb.toString();
        });
    }


    /*******************************************************************************
     * 一些基础类型数据的转换
     *******************************************************************************/

    public static <T> T getValue(String value, T emptyValue, T defaultValue, FunctionROne<T, String> convert) {
        if (isEmpty(value)) {
            return emptyValue;
        }
        try {
            return convert.run(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 解析字符串
     *
     * @param value
     * @param emptyValue   字符串是null或者空串时返回的值
     * @param defaultValue 解析出错后返回的默认字符串
     * @return
     */
    public static Integer getInteger(String value, Integer emptyValue, Integer defaultValue) {
        return getValue(value, emptyValue, defaultValue, (str) -> Integer.parseInt(str));
    }

    /**
     * 解析字符串
     *
     * @param value
     * @param defaultValue 解析出错后返回的默认字符串
     * @return
     */
    public static Integer getInteger(String value, Integer defaultValue) {
        return getInteger(value, null, defaultValue);
    }

    /**
     * 解析出错后返回的默认字符串是null
     *
     * @param value
     * @return
     */
    public static Integer getInteger(String value) {
        return getInteger(value, null);
    }

    public static Long getLong(String value, Long emptyValue, Long defaultValue) {
        return getValue(value, emptyValue, defaultValue, (str) -> Long.parseLong(str));
    }

    public static Long getLong(String value, Long defaultValue) {
        return getLong(value, null, defaultValue);
    }

    public static Long getLong(String value) {
        return getLong(value, null);
    }

    public static Double getDouble(String value, Double emptyValue, Double defaultValue) {
        return getValue(value, emptyValue, defaultValue, (str) -> Double.parseDouble(str));
    }

    public static Double getDouble(String value, Double defaultValue) {
        return getDouble(value, null, defaultValue);
    }

    public static Double getDouble(String value) {
        return getDouble(value, null);
    }

    public static Float getFloat(String value, Float emptyValue, Float defaultValue) {
        return getValue(value, emptyValue, defaultValue, (str) -> Float.parseFloat(str));
    }

    public static Float getFloat(String value, Float defaultValue) {
        return getFloat(value, null, defaultValue);
    }

    public static Float getFloat(String value) {
        return getFloat(value, null);
    }

    public static Short getShort(String value, Short emptyValue, Short defaultValue) {
        return getValue(value, emptyValue, defaultValue, (str) -> Short.parseShort(str));
    }

    public static Short getShort(String value, Short defaultValue) {
        return getShort(value, null, defaultValue);
    }

    public static Short getShort(String value) {
        return getShort(value, null);
    }

    public static Byte getByte(String value, Byte emptyValue, Byte defaultValue) {
        return getValue(value, emptyValue, defaultValue, (str) -> Byte.parseByte(str));
    }

    public static Byte getByte(String value, Byte defaultValue) {
        return getByte(value, null, defaultValue);
    }

    public static Byte getByte(String value) {
        return getByte(value, null);
    }

    public static Boolean getBoolean(String value, Boolean emptyValue, Boolean defaultValue) {
        return getValue(value, emptyValue, defaultValue, (str) -> Boolean.parseBoolean(str));
    }

    public static Boolean getBoolean(String value, Boolean defaultValue) {
        return getBoolean(value, null, defaultValue);
    }

    public static Boolean getBoolean(String value) {
        return getBoolean(value, null);
    }

    /**
     * 全角字符变半角字符
     *
     * @param str
     * @return
     * @date
     */
    public static String toSbcCaseByDbcCase(String str) {
        if (str == null || "".equals(str))
            return "";
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c >= 65281 && c < 65373)
                sb.append((char) (c - 65248));
            else
                sb.append(str.charAt(i));
        }

        return sb.toString();

    }

    /**
     * 全角生成半角
     */
    public static String toDbcCaseBySbcCase(String QJstr) {
        String outStr = "";
        String Tstr = "";
        byte[] b = null;
        for (int i = 0; i < QJstr.length(); i++) {
            try {
                Tstr = QJstr.substring(i, i + 1);
                b = Tstr.getBytes("unicode");
            } catch (java.io.UnsupportedEncodingException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("UnsupportedEncodingException", e);
                }
            }
            if (b[3] == -1) {
                b[2] = (byte) (b[2] + 32);
                b[3] = 0;
                try {
                    outStr = outStr + new String(b, "unicode");
                } catch (java.io.UnsupportedEncodingException ex) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("UnsupportedEncodingException", ex);
                    }
                }
            } else {
                outStr = outStr + Tstr;
            }
        }
        return outStr;
    }

    /**
     * 解析前台encodeURIComponent编码后的参数
     *
     * @param url      前端用urldecoder，编码后的url
     * @param encoding 编码方式，如"UTF-8"
     * @return
     */
    public static String toDecodeStringUrl(String url, String encoding) {
        String trem = "";
        if (StringUtils.isNotEmpty(url)) {
            try {
                trem = URLDecoder.decode(url, encoding);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return trem;
    }

    /**
     * 数字转字符串
     *
     * @param num
     * @param minValue 如果小于minValue，则输出""
     * @return
     */
    public static String toString(Number num, double minValue) {
        if (num == null) {
            return null;
        } else if (num instanceof Integer && (Integer) num > minValue) {
            return Integer.toString((Integer) num);
        } else if (num instanceof Long && (Long) num > minValue) {
            return Long.toString((Long) num);
        } else if (num instanceof Float && (Float) num > minValue) {
            return Float.toString((Float) num);
        } else if (num instanceof Double && (Double) num > minValue) {
            return Double.toString((Double) num);
        } else {
            return "";
        }
    }

    /**
     * 根据传入的分割符号,把传入的字符串分割为List字符串
     *
     * @param src              字符串
     * @param splitRegexSymbol 分隔的正则表达式字符串,
     * @return 列表
     */
    public static String[] split(String src, String splitRegexSymbol) {
        if (src == null) {
            return null;
        }
        return src.split(splitRegexSymbol);
    }

    /**
     * 驼峰风格字符串转为下划线连接的小写字符串
     *
     * @param param
     * @return
     */
    public static String camelToUnderline(String param) {
        if (StringUtils.isEmpty(param)) {
            return "";
        } else {
            int len = param.length();
            return stringBuilderPool.run(sb -> {
                for (int i = 0; i < len; ++i) {
                    char c = param.charAt(i);
                    if (Character.isUpperCase(c) && i > 0) {//如果是大写则加入下划线，并且转为小写字符
                        sb.append('_');
                    }
                    sb.append(Character.toLowerCase(c));
                }

                return sb.toString();
            });

        }
    }

    /**
     * 下划线风格的字符串转为驼峰原则
     * 95代表下划线_
     *
     * @param param
     * @return
     */
    public static String underlineToCamel(String param) {
        if (StringUtils.isEmpty(param)) {
            return "";
        } else {
            String temp = param.toLowerCase();
            int len = temp.length();
            return stringBuilderPool.run(sb -> {
                for (int i = 0; i < len; ++i) {
                    char c = temp.charAt(i);
                    if (c == 95) {
                        ++i;
                        if (i < len) {
                            sb.append(Character.toUpperCase(temp.charAt(i)));
                        }
                    } else {
                        sb.append(c);
                    }
                }
                return sb.toString();
            });
        }
    }

    /******************************************** 开始判断 *************************************************************/

    /**
     * 判断一个字符串是null或者是空白字符串
     *
     * @param value
     * @param isTrim 是否trim
     * @return
     */
    public static boolean isEmpty(String value, boolean isTrim) {
        if (value == null) {
            return true;
        }
        String str = value;
        if (isTrim) {
            str = value.trim();
        }
        if (str.length() < 1) {
            return true;
        }
        return false;
    }

    /**
     * 判断一个字符串是null或者是空白字符串
     *
     * @param value
     * @return
     */
    public static boolean isEmpty(String value) {
        return isEmpty(value, true);
    }

    /**
     * 判断对象是否为空
     *
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str, boolean isTrim) {
        return !isEmpty(str, isTrim);
    }

    /**
     * 判断对象是否为空
     *
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return isNotEmpty(str, true);
    }

    /**
     * 是否是大写字符串
     *
     * @param str
     * @return
     */
    public static boolean isUpperCase(String str) {
        return isMatch("^[A-Z]+$", str);
    }

    /**
     * str的一部分是否 是否匹配某个正则表达式
     *
     * @param str   字符串内容
     * @param regex 正则表达式
     * @return
     */
    public static boolean isMatch(String str, String regex) {
        return RegExpUtils.isMatch(str, regex);
    }

    /**
     * word是否包含大写字符串
     *
     * @param word
     * @return
     */
    public static boolean isContainUpperCase(String word) {
        for (int i = 0; i < word.length(); ++i) {
            char c = word.charAt(i);
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断某个字符串是否是存在于数组中某一个子串的subStr
     *
     * @param stringArray 原数组
     * @param source      查找的字符串
     * @return 是否找到
     */
    public static boolean isAnyItemContainsSource(String[] stringArray, String source) {
        // 转换为list
        List<String> tempList = Arrays.asList(stringArray);
        for (String s : tempList) {
            if (s != null && s.indexOf(source) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * source 中是否包含items中的某个item的字符串
     *
     * @param source 如果source is null，返回false
     * @param items
     * @return
     */
    public static boolean isContainsAnyItem(String source, List<String> items) {
        if (null == source) {
            return false;
        }
        for (String s : items) {
            if (s != null && source.indexOf(s) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * source中是否包含items中所有的字符串
     *
     * @param source 如果source is null，返回false
     * @param items
     * @return
     */
    public static boolean isContainsAllItem(String source, List<String> items) {
        if (null == source) {
            return false;
        }
        for (String s : items) {
            if (s != null && source.indexOf(s) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断两个字符串是否相等 如果都为null,或者为空串则判断为相等,否则如果s1=s2则相等
     *
     * @param s1
     * @param s2
     * @return
     */
    public static boolean equals(String s1, String s2) {
        if (StringUtils.isEmpty(s1, false) && StringUtils.isEmpty(s2, false)) {
            return true;
        } else if (null != s1 && null != s2) {
            return s1.equals(s2);
        }
        return false;
    }

    /**
     * cls 是否是CharSequence
     *
     * @param cls
     * @return
     */
    public static Boolean isCharSequence(Class<?> cls) {
        return Boolean.valueOf(cls != null && CharSequence.class.isAssignableFrom(cls));
    }

    /**
     * 是否是CharSequence的子类
     *
     * @param className 类的名称
     * @return
     */
    public static Boolean isCharSequence(String className) {
        try {
            return isCharSequence(Class.forName(className));
        } catch (ClassNotFoundException var2) {
            return Boolean.FALSE;
        }
    }

    /**
     * 是否包含小写字符串
     *
     * @param s
     * @return
     */
    public static boolean isContainLowerCase(String s) {
        char[] arr = getCharArray(s);
        int len = arr.length;
        for (int i = 0; i < len; ++i) {
            char c = arr[i];
            if (Character.isLowerCase(c)) {
                return true;
            }
        }

        return false;
    }

    /******************************************** 结束判断 *************************************************************/


    /******************************************** 开始数据处理 *************************************************************/

    public static String toUpperFirstChar(String rawString) {
        String beforeChar = rawString.substring(0, 1).toUpperCase();
        String afterChar = rawString.substring(1, rawString.length());
        return beforeChar + afterChar;
    }

    /**
     * 将rawString转换为小写并且替换index分隔第二种字符串的第一部分
     *
     * @param rawString
     * @param index
     * @return
     */
    public static String toLowerByPrefix(String rawString, int index) {
        String beforeChar = rawString.substring(0, index).toLowerCase();
        String afterChar = rawString.substring(index, rawString.length());
        return beforeChar + afterChar;
    }

    public static String removePrefixAfterPrefixToLower(String rawString, int index) {
        return toLowerByPrefix(rawString.substring(index, rawString.length()), 1);
    }

    /**
     * 获取subString，自动判空
     *
     * @param src
     * @param size
     * @return
     */
    public static String getSubString(String src, int size) {
        if (src != null && src.length() > size) {
            src = src.substring(0, size);
        }
        return src;
    }

    /**
     * 截取字符串　超出的字符用symbol代替
     *
     * @param len     　字符串长度　长度计量单位为一个GBK汉字　　两个英文字母计算为一个单位长度
     * @param str
     * @param symbol  超出的字符用symbol代替
     * @param charset 字符编码
     * @return
     */
    public static String getStringByLimitLength(String str, int len, String symbol, String charset) {
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
                    strRet = new String(b, 0, iLen, charset) + symbol;
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
     * 截取字符串　超出的字符用...代替
     *
     * @param len 　字符串长度　长度计量单位为一个GBK汉字　　两个英文字母计算为一个单位长度
     * @param str
     * @return12
     */
    public static String getStringByLimitLength(String str, int len) {
        return getStringByLimitLength(str, len, "...", "UTF-8");
    }

    /**
     * 取得字符串的实际长度（考虑了汉字的情况）
     *
     * @param srcStr 源字符串
     * @return 字符串的实际长度
     */
    public static int getStringLength(String srcStr) {
        int return_value = 0;
        if (srcStr != null) {
            char[] theChars = getCharArray(srcStr);
            for (int i = 0; i < theChars.length; i++) {
                return_value += (theChars[i] <= 255) ? 1 : 2;
            }
        }
        return return_value;
    }


    /***************************************************************************
     * toHideEmailPrefix - 获得隐藏邮件地址前缀的邮箱地址。
     *
     * @param email
     *            - EMail邮箱地址 例如: linwenguo@koubei.com 等等...
     * @return 返回已隐藏前缀邮件地址, 如 *********@koubei.com.
     **************************************************************************/
    public static String toHideEmailPrefix(String email) {
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
        return stringBuilderPool.run(s -> {
            for (int i = 0; i < num; i++) {
                s.append(src);
            }
            return s.toString();
        });
    }

    /**
     * 格式化一个float
     *
     * @param format 要格式化成的格式 such as #.00, #.#
     */

    public static String formatFloat(float f, String format) {
        DecimalFormat df = new DecimalFormat(format);
        return df.format(f);
    }


    /**
     * 页面中去除字符串中的空格、回车、换行符、制表符
     *
     * @param str
     * @return
     */
    public static String replaceBlank(String str) {
        if (str != null) {
            Pattern pattern = RegExpUtils.getPattern(RegExpUtils.PatternStr.blank);
            Matcher m = pattern.matcher(str);
            str = m.replaceAll("");
        }
        return str;
    }

    /**
     * 转换编码
     *
     * @param s              源字符串
     * @param sourceEncoding 源编码格式
     * @param targetEncoding 目标编码格式
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
     * 字符串替换
     *
     * @param str        源字符串
     * @param regEx      正则表达式样式
     * @param insteadStr 替换文本
     * @return 结果串
     */
    public static String replaceByReg(String str, String regEx, String insteadStr) {
        Pattern p = RegExpUtils.getPattern(regEx);
        Matcher m = p.matcher(str);
        str = m.replaceAll(insteadStr);
        return str;
    }

    /**
     * 得到字符串的regExp匹配的位置序列
     *
     * @param str    字符串
     * @param regExp 正则表达式
     * @return 字符串的子串位置序列
     */
    public static List<Integer> getSubStringPositions(String str, String regExp) {
        String[] sp = split(str, regExp);
        if (sp == null || sp.length < 1) {
            return Collections.EMPTY_LIST;
        }
        int spIndex = 0;
        int lastIndex = 0;
        if (str.indexOf(sp[0]) == 0) {
            lastIndex = sp[0].length();
            ++spIndex;
        }
        List<Integer> positions = Collections.EMPTY_LIST;
        Pattern p = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(str);
        while (matcher.find()) {
            positions.add(lastIndex + 1);
            lastIndex = lastIndex + matcher.group(0).length() + sp[++spIndex].length();
        }
        return positions;
    }

    /**
     * 根据正则表达式提取字符串,相同的字符串只返回一个
     *
     * @param str    源字符串
     * @param regExp 正则表达式
     * @return 目标字符串数据组
     * ************************************************************************
     */

    // ★传入一个字符串，把符合pattern格式的字符串放入字符串Set
    // java.util.regex是一个用正则表达式所订制的模式来对字符串进行匹配工作的类库包
    public static Set<String> getStringsByPattern(String str, String regExp) {
        Pattern p = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
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
        return result;

    }

    /**
     * ************************************************************************* 用要通过URL传输的内容进行编码
     *
     * @param src 源字符串
     * @return 经过编码的内容
     * ************************************************************************
     */
    public static String encodeURL(String src, String encoding) {
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
     * /**
     * 全角括号转为半角
     *
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

    /******************************************** 结束数据处理 *************************************************************/
    /**
     * 一个高效的支持多线程的字符串append工具，传入StringBuilder用于自定义append
     *
     * @param functionROne
     * @return
     */
    public static String doAppend(FunctionROne<String, StringBuilder> functionROne) {
        return stringBuilderPool.run(functionROne);
    }

    /**
     * 将objects中的对象按照顺序依次append到StringBuilder中并且返回
     *
     * @param freeMemoryThen 使用完毕后，当内存占用大于64字节的时候，删除超过64字节的部分，即最低保留64字节
     * @param objects        Object[]
     * @param isAppendNull   是否将null值也append到字符串中，默认为false
     * @return
     */
    public static String appendValue(boolean freeMemoryThen, boolean isAppendNull, Object[] objects) {
        if (objects == null) {
            if (isAppendNull) {
                return String.valueOf("null");
            } else {
                return "";
            }
        }
        return stringBuilderPool.run(sb -> {
            Stream.of(objects).forEach(it -> {
                if (isAppendNull) {
                    sb.append(it);
                } else if (!isAppendNull && it != null) {
                    sb.append(it);
                }
            });
            if (freeMemoryThen && sb.capacity() > 64) {
                sb.delete(0, sb.capacity() - 64);
            }
            return sb.toString();
        });
    }

    /**
     * 将objects中的对象按照顺序依次append到StringBuilder中并且返回
     *
     * @param objects      Object[]
     * @param isAppendNull 是否将null值也append到字符串中，默认为false
     * @return
     */
    public static String appendValue(boolean isAppendNull, Object[] objects) {
        return appendValue(false, false, objects);
    }

    /**
     * 将objects中的对象按照顺序依次append到StringBuilder中并且返回
     * 默认将null对象忽略，不会append到字符串中
     *
     * @param objects
     * @return
     */
    public static String append(Objects... objects) {
        return appendValue(false, objects);
    }

    /**
     * 如果str或者subStr为null，返回-1 （-1 表示不存在匹配的子串），否则返回其子串在主串中的索引值
     * 一般情况下，朴素indexOf的速度更快，前缀的重复率越高，KMP的速度越快
     *
     * @param sArray          主串
     * @param pArray       子串、模式串
     * @param kmpNextArray KMP的next数组，传入null时会自动获取
     * @param skipLength 跳过主串中前面skipLength个长度的字符
     * @return
     */
    public static int indexOfByKMP(char[] sArray, char[] pArray, int[] kmpNextArray,int skipLength) {
        if (kmpNextArray == null) {
            kmpNextArray = getKmpNextArray(pArray);
        }

        int i = skipLength;
        int maxLength = sArray.length - skipLength;
        int pLength = pArray.length;
        int j = 0;
        while (j < pLength && i < maxLength) {
            //①如果j = -1，或者当前字符匹配成功（即S[i] == P[j]），都令i++，j++
            if (j == -1 || sArray[i] == pArray[j]) {
                //①如果当前字符匹配成功（即S[i] == P[j]），则i++，j++
                i++;
                j++;
            } else {
                //②如果j != -1，且当前字符匹配失败（即S[i] != P[j]），则令 i 不变，j = next[j]
                //next[j]即为j所对应的next值
                j = kmpNextArray[j];
            }
        }
        //匹配成功，返回模式串p在文本串s中的位置，否则返回-1
        if (j == pArray.length) {
            return i - j;
        }
        return -1;

    }

    /**
     * 如果str或者subStr为null，返回-1 （-1 表示不存在匹配的子串），否则返回其子串在主串中的索引值
     * 一般情况下，朴素indexOf的速度更快，前缀的重复率越高，KMP的速度越快
     *
     * @param sArray          主串
     * @param pArray       子串、模式串
     * @param kmpNextArray KMP的next数组，传入null时会自动获取
     * @return
     */
    public static int indexOfByKMP(char[] sArray, char[] pArray, int[] kmpNextArray) {
        return indexOfByKMP(sArray,pArray,kmpNextArray,0);
    }

    /**
     * 如果str或者subStr为null，返回-1 （-1 表示不存在匹配的子串），否则返回其子串在主串中的索引值
     * 一般情况下，朴素indexOf的速度更快，前缀的重复率越高，KMP的速度越快
     * @param str          主串
     * @param subStr       子串、模式串
     * @param kmpNextArray KMP的next数组，传入null时会自动获取
     * @return
     */
    public static int indexOfByKMP(String str,String subStr, int[] kmpNextArray) {
        return indexOfByKMP(getCharArray(str),getCharArray(subStr),kmpNextArray);
    }

    /**
     * 求出KMP算法中的next数组，优化过后的next 数组求法
     *
     * @param pArray 模式串的char数组内容
     * @return
     */
    public static int[] getKmpNextArray(char[] pArray) {
        int[] next = new int[pArray.length];
        next[0] = -1;
        int k = -1;
        int j = 0;
        int maxIndex = pArray.length - 1;
        while (j < maxIndex) {
            //p[k]表示前缀，p[j]表示后缀
            if (k == -1 || pArray[j] == pArray[k]) {
                ++j;
                ++k;
                //较之前next数组求法，改动在下面4行
                if (pArray[j] != pArray[k]) {
                    next[j] = k;   //之前只有这一行
                } else {
                    //因为不能出现p[j] = p[ next[j ]]，所以当出现时需要继续递归，k = next[k] = next[next[k]]
                    //优化之前这里是next[j] = k; 但是因为当前 pArray[j] = pArray[k]的时候，由于前后缀相等，
                    //所以在第一次失配并移动后，移动的位置的当前字符其实仍然是失配的，所以只要值相等，这里就递归前移
                    next[j] = next[k];
                }
            } else {
                k = next[k];
            }
        }
        return next;
    }

    /**
     * 如果str或者subStr为null，返回-1 （-1 表示不存在匹配的子串），否则返回其子串在主串中的索引值
     * 使用KMP算法搜索字符串
     *
     * @param str    主串
     * @param subStr 子串
     * @return
     */
    public static int indexOfByKMP(String str, String subStr) {
        return indexOfByKMP(str, subStr, null);
    }

    /**
     * 返回当前str的char[]数组，而不是创建一个新的char[]
     * @param str
     * @return
     */
    public static char[] getCharArray(String str){
        try {
            return (char[]) STRING_VALUE_FIELD.get(str);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
