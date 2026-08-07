package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.lang.base.TrieNode;
import com.tingfeng.util.java.base.lang.base.Tuple2;
import com.tingfeng.util.java.base.lang.inter.returnfunction.FunctionROne;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 字符串工具类（存量兼容门面）
 *
 * 全部方法委托至拆分后的实现类，签名与行为与拆分前完全一致；新增能力请直接使用实现类：
 * StringAppendOps（拼接/池）、StringParseOps（数值解析+类型判断）、StringFormatOps（格式化/宽度/全半角/裁剪/填充）、
 * StringConvertOps（命名/编码/大小写转换）、StringSearchOps（KMP/区间搜索/分割/正则提取）。
 *
 * @author huitoukest
 */
public class StringUtils {

    /**
     * null 对象的字符串表示，即 "null"
     */
    public static String STR_NULL_OBJ = StringAppendOps.STR_NULL_OBJ;

    private StringUtils() {

    }

    /** 将InputStream转换成String，同 {@link StringConvertOps#getStringByStream(InputStream)} */
    public static String getStringByStream(InputStream in) {
        return StringConvertOps.getStringByStream(in);
    }

    /** 将InputStream转换成某种字符编码的String，同 {@link StringConvertOps#getStringByStream(InputStream, String)} */
    public static String getStringByStream(InputStream in, String encoding) {
        return StringConvertOps.getStringByStream(in, encoding);
    }

    /** 将byte数组转换成String，同 {@link StringConvertOps#getStringByBytes(byte[], String)} */
    public static String getStringByBytes(byte[] in, String charEncode) throws UnsupportedEncodingException {
        return StringConvertOps.getStringByBytes(in, charEncode);
    }

    /** 将byte数组转换成String，同 {@link StringConvertOps#getStringByBytes(byte[])} */
    public static String getStringByBytes(byte[] in) throws UnsupportedEncodingException {
        return StringConvertOps.getStringByBytes(in);
    }

    /** 去掉首尾的空白字符和symbol字符串，同 {@link StringSearchOps#trimSymbol(String, String)} */
    public static String trimSymbol(String souceString, String symbol) {
        return StringSearchOps.trimSymbol(souceString, symbol);
    }

    /** 首字母小写，同 {@link StringConvertOps#toLowerFirstChar(String)} */
    public static String toLowerFirstChar(String srcString) {
        return StringConvertOps.toLowerFirstChar(srcString);
    }

    /** 基础类型数据转换，同 {@link StringParseOps#getValue(String, Object, Object, FunctionROne)} */
    public static <T> T getValue(String value, T emptyValue, T defaultValue, FunctionROne<T, String> convert) {
        return StringParseOps.getValue(value, emptyValue, defaultValue, convert);
    }

    /** 解析字符串，同 {@link StringParseOps#getInteger(String, Integer, Integer)} */
    public static Integer getInteger(String value, Integer emptyValue, Integer defaultValue) {
        return StringParseOps.getInteger(value, emptyValue, defaultValue);
    }

    /** 解析字符串，同 {@link StringParseOps#getInteger(String, Integer)} */
    public static Integer getInteger(String value, Integer defaultValue) {
        return StringParseOps.getInteger(value, defaultValue);
    }

    /** 解析字符串（默认字符串为null），同 {@link StringParseOps#getInteger(String)} */
    public static Integer getInteger(String value) {
        return StringParseOps.getInteger(value);
    }

    /** 解析字符串，同 {@link StringParseOps#getLong(String, Long, Long)} */
    public static Long getLong(String value, Long emptyValue, Long defaultValue) {
        return StringParseOps.getLong(value, emptyValue, defaultValue);
    }

    /** 解析字符串，同 {@link StringParseOps#getLong(String, Long)} */
    public static Long getLong(String value, Long defaultValue) {
        return StringParseOps.getLong(value, defaultValue);
    }

    /** 解析字符串，同 {@link StringParseOps#getLong(String)} */
    public static Long getLong(String value) {
        return StringParseOps.getLong(value);
    }

    /** 解析字符串，同 {@link StringParseOps#getDouble(String, Double, Double)} */
    public static Double getDouble(String value, Double emptyValue, Double defaultValue) {
        return StringParseOps.getDouble(value, emptyValue, defaultValue);
    }

    /** 解析字符串，同 {@link StringParseOps#getDouble(String, Double)} */
    public static Double getDouble(String value, Double defaultValue) {
        return StringParseOps.getDouble(value, defaultValue);
    }

    /** 解析字符串，同 {@link StringParseOps#getDouble(String)} */
    public static Double getDouble(String value) {
        return StringParseOps.getDouble(value);
    }

    /** 解析字符串，同 {@link StringParseOps#getFloat(String, Float, Float)} */
    public static Float getFloat(String value, Float emptyValue, Float defaultValue) {
        return StringParseOps.getFloat(value, emptyValue, defaultValue);
    }

    /** 解析字符串，同 {@link StringParseOps#getFloat(String, Float)} */
    public static Float getFloat(String value, Float defaultValue) {
        return StringParseOps.getFloat(value, defaultValue);
    }

    /** 解析字符串，同 {@link StringParseOps#getFloat(String)} */
    public static Float getFloat(String value) {
        return StringParseOps.getFloat(value);
    }

    /** 解析字符串，同 {@link StringParseOps#getShort(String, Short, Short)} */
    public static Short getShort(String value, Short emptyValue, Short defaultValue) {
        return StringParseOps.getShort(value, emptyValue, defaultValue);
    }

    /** 解析字符串，同 {@link StringParseOps#getShort(String, Short)} */
    public static Short getShort(String value, Short defaultValue) {
        return StringParseOps.getShort(value, defaultValue);
    }

    /** 解析字符串，同 {@link StringParseOps#getShort(String)} */
    public static Short getShort(String value) {
        return StringParseOps.getShort(value);
    }

    /** 解析字符串，同 {@link StringParseOps#getByte(String, Byte, Byte)} */
    public static Byte getByte(String value, Byte emptyValue, Byte defaultValue) {
        return StringParseOps.getByte(value, emptyValue, defaultValue);
    }

    /** 解析字符串，同 {@link StringParseOps#getByte(String, Byte)} */
    public static Byte getByte(String value, Byte defaultValue) {
        return StringParseOps.getByte(value, defaultValue);
    }

    /** 解析字符串，同 {@link StringParseOps#getByte(String)} */
    public static Byte getByte(String value) {
        return StringParseOps.getByte(value);
    }

    /** 解析字符串，同 {@link StringParseOps#getBoolean(String, Boolean, Boolean)} */
    public static Boolean getBoolean(String value, Boolean emptyValue, Boolean defaultValue) {
        return StringParseOps.getBoolean(value, emptyValue, defaultValue);
    }

    /** 解析字符串，同 {@link StringParseOps#getBoolean(String, Boolean)} */
    public static Boolean getBoolean(String value, Boolean defaultValue) {
        return StringParseOps.getBoolean(value, defaultValue);
    }

    /** 解析字符串，同 {@link StringParseOps#getBoolean(String)} */
    public static Boolean getBoolean(String value) {
        return StringParseOps.getBoolean(value);
    }

    /** 全角字符变半角字符，同 {@link StringFormatOps#toSbcCaseByDbcCase(String)} */
    public static String toSbcCaseByDbcCase(String str) {
        return StringFormatOps.toSbcCaseByDbcCase(str);
    }

    /** 半角字符变全角字符，同 {@link StringFormatOps#toDbcCaseBySbcCase(String)} */
    public static String toDbcCaseBySbcCase(String str) {
        return StringFormatOps.toDbcCaseBySbcCase(str);
    }

    /** 解析前台encodeURIComponent编码后的参数，同 {@link StringConvertOps#toDecodeStringUrl(String, String)} */
    public static String toDecodeStringUrl(String url, String encoding) {
        return StringConvertOps.toDecodeStringUrl(url, encoding);
    }

    /** 数字转字符串，同 {@link StringAppendOps#toString(Number, double)} */
    public static String toString(Number num, double minValue) {
        return StringAppendOps.toString(num, minValue);
    }

    /** 根据传入的分割符号,把传入的字符串分割为List字符串，同 {@link StringSearchOps#split(String, String)} */
    public static String[] split(String src, String splitRegexSymbol) {
        return StringSearchOps.split(src, splitRegexSymbol);
    }

    /** 驼峰风格字符串转为下划线连接的小写字符串，同 {@link StringConvertOps#camelToUnderline(String)} */
    public static String camelToUnderline(String param) {
        return StringConvertOps.camelToUnderline(param);
    }

    /** 下划线风格的字符串转为驼峰原则，同 {@link StringConvertOps#underlineToCamel(String)} */
    public static String underlineToCamel(String param) {
        return StringConvertOps.underlineToCamel(param);
    }

    /** 判断一个字符串是null或者是空白字符串，同 {@link StringParseOps#isEmpty(String, boolean)} */
    public static boolean isEmpty(String value, boolean isTrim) {
        return StringParseOps.isEmpty(value, isTrim);
    }

    /** 判断一个字符串是null或者是空白字符串，同 {@link StringParseOps#isEmpty(String)} */
    public static boolean isEmpty(String value) {
        return StringParseOps.isEmpty(value);
    }

    /** 判断对象是否为空，同 {@link StringParseOps#isNotEmpty(String, boolean)} */
    public static boolean isNotEmpty(String str, boolean isTrim) {
        return StringParseOps.isNotEmpty(str, isTrim);
    }

    /** 判断对象是否为空，同 {@link StringParseOps#isNotEmpty(String)} */
    public static boolean isNotEmpty(String str) {
        return StringParseOps.isNotEmpty(str);
    }

    /** 是否是大写字符串，同 {@link StringParseOps#isUpperCase(String)} */
    public static boolean isUpperCase(String str) {
        return StringParseOps.isUpperCase(str);
    }

    /** 判断字符串是否为合法 BigInteger 格式，同 {@link StringParseOps#isBigInteger(String)} */
    public static boolean isBigInteger(String s) {
        return StringParseOps.isBigInteger(s);
    }

    /** 判断字符串是否为合法 Integer 格式（十进制），同 {@link StringParseOps#safeParseInteger(String)} */
    public static Tuple2<Boolean,Integer> safeParseInteger(String s) {
        return StringParseOps.safeParseInteger(s);
    }

    /** 判断字符串是否为合法 Float 格式，同 {@link StringParseOps#safeParseFloat(String)} */
    public static Tuple2<Boolean, Float> safeParseFloat(String s) {
        return StringParseOps.safeParseFloat(s);
    }

    /** 判断字符串是否为合法 Double 格式，同 {@link StringParseOps#safeParseDouble(String)} */
    public static Tuple2<Boolean, Double> safeParseDouble(String s) {
        return StringParseOps.safeParseDouble(s);
    }

    /** 判断字符串是否为合法 Byte 格式，同 {@link StringParseOps#safeParseByte(String)} */
    public static Tuple2<Boolean, Byte> safeParseByte(String s) {
        return StringParseOps.safeParseByte(s);
    }

    /** 判断字符串是否为合法 Short 格式，同 {@link StringParseOps#safeParseShort(String)} */
    public static Tuple2<Boolean, Short> safeParseShort(String s) {
        return StringParseOps.safeParseShort(s);
    }

    /** 判断字符串是否为合法 Long 格式，同 {@link StringParseOps#safeParseLong(String)} */
    public static Tuple2<Boolean, Long> safeParseLong(String s) {
        return StringParseOps.safeParseLong(s);
    }

    /** 判断字符串是否为合法 Boolean 格式，同 {@link StringParseOps#isBoolean(String)} */
    public static boolean isBoolean(String s) {
        return StringParseOps.isBoolean(s);
    }

    /** 判断字符串是否为合法十六进制整数格式，同 {@link StringParseOps#isHex(String)} */
    public static boolean isHex(String s) {
        return StringParseOps.isHex(s);
    }

    /** 判断字符串是否为 URL 格式，同 {@link StringParseOps#isUrl(String)} */
    public static boolean isUrl(String s) {
        return StringParseOps.isUrl(s);
    }

    /** 判断字符串是否为合法 BigInteger 格式，同 {@link StringParseOps#isBigDecimal(String)} */
    public static boolean isBigDecimal(String s) {
        return StringParseOps.isBigDecimal(s);
    }

    /** word是否包含大写字符串，同 {@link StringSearchOps#isContainUpperCase(String)} */
    public static boolean isContainUpperCase(String word) {
        return StringSearchOps.isContainUpperCase(word);
    }

    /** 判断某个字符串是否是存在于数组中某一个子串的subStr，同 {@link StringSearchOps#isAnyItemContainsSource(String[], String)} */
    public static boolean isAnyItemContainsSource(String[] stringArray, String source) {
        return StringSearchOps.isAnyItemContainsSource(stringArray, source);
    }

    /** source 中是否包含items中的某个item的字符串，同 {@link StringSearchOps#isContainsAnyItem(String, List)} */
    public static boolean isContainsAnyItem(String source, List<String> items) {
        return StringSearchOps.isContainsAnyItem(source, items);
    }

    /** source中是否包含items中所有的字符串，同 {@link StringSearchOps#isContainsAllItem(String, List)} */
    public static boolean isContainsAllItem(String source, List<String> items) {
        return StringSearchOps.isContainsAllItem(source, items);
    }

    /** 判断两个字符串是否相等，同 {@link StringParseOps#equals(String, String)} */
    public static boolean equals(String s1, String s2) {
        return StringParseOps.equals(s1, s2);
    }

    /** cls 是否是CharSequence，同 {@link StringParseOps#isCharSequence(Class)} */
    public static Boolean isCharSequence(Class<?> cls) {
        return StringParseOps.isCharSequence(cls);
    }

    /** 是否是CharSequence的子类，同 {@link StringParseOps#isCharSequence(String)} */
    public static Boolean isCharSequence(String className) {
        return StringParseOps.isCharSequence(className);
    }

    /** 是否包含小写字符串，同 {@link StringSearchOps#isContainLowerCase(String)} */
    public static boolean isContainLowerCase(String s) {
        return StringSearchOps.isContainLowerCase(s);
    }

    /** 首字母大写，null/空串原样返回，同 {@link StringConvertOps#toUpperFirstChar(String)} */
    public static String toUpperFirstChar(String rawString) {
        return StringConvertOps.toUpperFirstChar(rawString);
    }

    /** 将rawString转换为小写并且替换index分隔第二种字符串的第一部分，同 {@link StringConvertOps#toLowerByPrefix(String, int)} */
    public static String toLowerByPrefix(String rawString, int index) {
        return StringConvertOps.toLowerByPrefix(rawString, index);
    }

    /** 去除前缀后将剩余部分首字母小写，同 {@link StringConvertOps#removePrefixAfterPrefixToLower(String, int)} */
    public static String removePrefixAfterPrefixToLower(String rawString, int index) {
        return StringConvertOps.removePrefixAfterPrefixToLower(rawString, index);
    }

    /** 获取subString，自动判空，同 {@link StringFormatOps#getSubString(String, int)} */
    public static String getSubString(String src, int size) {
        return StringFormatOps.getSubString(src, size);
    }

    /** 截取字符串　超出的字符用symbol代替，同 {@link StringFormatOps#getStringByLimitLength(String, int, String, String)} */
    public static String getStringByLimitLength(String str, int len, String symbol, String charset) {
        return StringFormatOps.getStringByLimitLength(str, len, symbol, charset);
    }

    /** 截取字符串　超出的字符用...代替，同 {@link StringFormatOps#getStringByLimitLength(String, int)} */
    public static String getStringByLimitLength(String str, int len) {
        return StringFormatOps.getStringByLimitLength(str, len);
    }

    /** 取得字符串的实际长度（考虑了汉字的情况），同 {@link StringFormatOps#getStringLength(String)} */
    public static int getStringLength(String srcStr) {
        return StringFormatOps.getStringLength(srcStr);
    }

    /** 获得隐藏邮件地址前缀的邮箱地址，同 {@link StringFormatOps#toHideEmailPrefix(String)} */
    public static String toHideEmailPrefix(String email) {
        return StringFormatOps.toHideEmailPrefix(email);
    }

    /** 通过源字符串重复生成N次组成新的字符串，同 {@link StringAppendOps#repeat(String, int)} */
    public static String repeat(String src, int num) {
        return StringAppendOps.repeat(src, num);
    }

    /** 格式化一个float，同 {@link StringFormatOps#formatFloat(float, String)} */
    public static String formatFloat(float f, String format) {
        return StringFormatOps.formatFloat(f, format);
    }

    /** 页面中去除字符串中的空格、回车、换行符、制表符，同 {@link StringSearchOps#replaceBlank(String)} */
    public static String replaceBlank(String str) {
        return StringSearchOps.replaceBlank(str);
    }

    /** 转换编码，同 {@link StringConvertOps#getStringByChangCoding(String, String, String)} */
    public static String getStringByChangCoding(String s, String sourceEncoding, String targetEncoding) {
        return StringConvertOps.getStringByChangCoding(s, sourceEncoding, targetEncoding);
    }

    /** 字符串替换，同 {@link StringSearchOps#replaceByReg(String, String, String)} */
    public static String replaceByReg(String str, String regEx, String insteadStr) {
        return StringSearchOps.replaceByReg(str, regEx, insteadStr);
    }

    /** 得到字符串的regExp匹配的位置序列，同 {@link StringSearchOps#getSubStringPositions(String, String)} */
    public static List<Integer> getSubStringPositions(String str, String regExp) {
        return StringSearchOps.getSubStringPositions(str, regExp);
    }

    /** 根据正则表达式提取字符串,相同的字符串只返回一个，同 {@link StringSearchOps#getStringsByPattern(String, String)} */
    public static Set<String> getStringsByPattern(String str, String regExp) {
        return StringSearchOps.getStringsByPattern(str, regExp);
    }

    /** 用要通过URL传输的内容进行编码，同 {@link StringConvertOps#encodeURL(String, String)} */
    public static String encodeURL(String src, String encoding) {
        return StringConvertOps.encodeURL(src, encoding);
    }

    /** 全角括号转为半角，同 {@link StringConvertOps#replaceBracketStr(String)} */
    public static String replaceBracketStr(String str) {
        return StringConvertOps.replaceBracketStr(str);
    }

    /** 一个高效的支持多线程的字符串append工具，同 {@link StringAppendOps#doAppend(FunctionROne)} */
    public static String doAppend(FunctionROne<String, StringBuilder> functionROne) {
        return StringAppendOps.doAppend(functionROne);
    }

    /** 将objects中的对象按照顺序依次append到StringBuilder中并且返回，同 {@link StringAppendOps#appendValue(boolean, boolean, Object[])} */
    public static String appendValue(boolean freeMemoryThen, boolean isAppendNull, Object[] objects) {
        return StringAppendOps.appendValue(freeMemoryThen, isAppendNull, objects);
    }

    /** 将objects中的对象按照顺序依次append到StringBuilder中并且返回，同 {@link StringAppendOps#appendValue(boolean, Object[])} */
    public static String appendValue(boolean isAppendNull, Object... objects) {
        return StringAppendOps.appendValue(isAppendNull, objects);
    }

    /** 将objects中的对象按照顺序依次append到StringBuilder中并且返回，同 {@link StringAppendOps#append(Object[])} */
    public static String append(Object... objects) {
        return StringAppendOps.append(objects);
    }

    /** KMP搜索（跳过前skipLength个字符），同 {@link StringSearchOps#indexOfByKMP(char[], char[], int[], int)} */
    public static int indexOfByKMP(char[] sArray, char[] pArray, int[] kmpNextArray, int skipLength) {
        return StringSearchOps.indexOfByKMP(sArray, pArray, kmpNextArray, skipLength);
    }

    /** KMP搜索，同 {@link StringSearchOps#indexOfByKMP(char[], char[], int[])} */
    public static int indexOfByKMP(char[] sArray, char[] pArray, int[] kmpNextArray) {
        return StringSearchOps.indexOfByKMP(sArray, pArray, kmpNextArray);
    }

    /** KMP搜索，同 {@link StringSearchOps#indexOfByKMP(String, String, int[])} */
    public static int indexOfByKMP(String str, String subStr, int[] kmpNextArray) {
        return StringSearchOps.indexOfByKMP(str, subStr, kmpNextArray);
    }

    /** 求出KMP算法中的next数组，同 {@link StringSearchOps#getKmpNextArray(char[])} */
    public static int[] getKmpNextArray(char[] pArray) {
        return StringSearchOps.getKmpNextArray(pArray);
    }

    /** KMP搜索，同 {@link StringSearchOps#indexOfByKMP(String, String)} */
    public static int indexOfByKMP(String str, String subStr) {
        return StringSearchOps.indexOfByKMP(str, subStr);
    }

    /** 返回当前str的char[]数组，而不是创建一个新的char[]，同 {@link StringConvertOps#getCharArray(String)} */
    public static char[] getCharArray(String str) {
        return StringConvertOps.getCharArray(str);
    }

    /** 左填充字符串到指定长度，同 {@link StringFormatOps#leftPad(Object, int, Object)} */
    public static String leftPad(Object value, int length, Object c) {
        return StringFormatOps.leftPad(value, length, c);
    }

    /** 右填充字符串到指定长度，同 {@link StringFormatOps#rightPad(Object, int, Object)} */
    public static String rightPad(Object value, int length, Object c) {
        return StringFormatOps.rightPad(value, length, c);
    }

    /** 反转字符串，同 {@link StringConvertOps#reverse(Object)} */
    public static String reverse(Object str) {
        return StringConvertOps.reverse(str);
    }

    /** 首字母小写（与 toLowerFirstChar 功能一致，保留签名委托），同 {@link StringConvertOps#firstLetterToLower(String)} */
    public static String firstLetterToLower(String srcString) {
        return StringConvertOps.firstLetterToLower(srcString);
    }

    /** 一次性的模板替换，同 {@link StringConvertOps#replaceByTemplate(String, String, String, Set, Map)} */
    public static String replaceByTemplate(String startFlag, String endFlag, String content, Set<String> params, Map<String, String> paramsData) {
        return StringConvertOps.replaceByTemplate(startFlag, endFlag, content, params, paramsData);
    }

    /** 一次性的模板替换（默认${A}占位符），同 {@link StringConvertOps#replaceByTemplate(String, Map)} */
    public static String replaceByTemplate(String content, Map<String, String> paramsData) {
        return StringConvertOps.replaceByTemplate(content, paramsData);
    }

    /** 从jdk拷贝出来的工具，同 {@link StringSearchOps#indexOf(char[], int, int, char[], int, int, int)} */
    public static int indexOf(char[] source, int sourceOffset, int sourceCount,
                       char[] target, int targetOffset, int targetCount,
                       int fromIndex) {
        return StringSearchOps.indexOf(source, sourceOffset, sourceCount,
                target, targetOffset, targetCount, fromIndex);
    }

    /** 在source的指定区间查找source，同 {@link StringSearchOps#indexOf(String, String, int, int)} */
    public static int indexOf(String source, String target, int fromIndex, int endIndex) {
        return StringSearchOps.indexOf(source, target, fromIndex, endIndex);
    }

    /** 从jdk中拷贝出来的工具，同 {@link StringSearchOps#lastIndexOf(char[], int, int, char[], int, int, int)} */
    public static int lastIndexOf(char[] source, int sourceOffset, int sourceCount,
                           char[] target, int targetOffset, int targetCount,
                           int fromIndex) {
        return StringSearchOps.lastIndexOf(source, sourceOffset, sourceCount,
                target, targetOffset, targetCount, fromIndex);
    }

    /** 从jdk中拷贝出来的工具，做了点调整，同 {@link StringSearchOps#lastIndexOf(char[], int, int, String, int)} */
    public static int lastIndexOf(char[] source, int sourceOffset, int sourceCount,
                           String target, int fromIndex) {
        return StringSearchOps.lastIndexOf(source, sourceOffset, sourceCount, target, fromIndex);
    }

    /** 从指定区间查询最后一个匹配的索引，同 {@link StringSearchOps#lastIndexOf(String, String, int, int)} */
    public static int lastIndexOf(String source, String target, int startIndex, int endIndex) {
        return StringSearchOps.lastIndexOf(source, target, startIndex, endIndex);
    }

    /** 自定义分割函数，返回全部，同 {@link StringSearchOps#splitByStr(String, String, int)} */
    public static List<String> splitByStr(String str, String delimitString, int limit) {
        return StringSearchOps.splitByStr(str, delimitString, limit);
    }

    /** 自定义分割函数，返回全部，同 {@link StringSearchOps#splitByStr(String, String)} */
    public static List<String> splitByStr(String str, String delimitString) {
        return StringSearchOps.splitByStr(str, delimitString);
    }

    /** 判断 str 中的内容是否与 content中每一项的toString 连接后的内容相等，同 {@link StringParseOps#equals(String, Object[])} */
    public static boolean equals(String str, Object... content) {
        return StringParseOps.equals(str, content);
    }

    /** 判断两个字符串指定开始结束位置的 子串是否相等，同 {@link StringSearchOps#equals(String, int, int, String, int, int)} */
    public static boolean equals(String sourceStr, int sourceStart, int sourceEnd, String targetStr, int targetStart, int targetEnd) {
        return StringSearchOps.equals(sourceStr, sourceStart, sourceEnd, targetStr, targetStart, targetEnd);
    }

    /** 反转义字符串，同 {@link StringConvertOps#unescape(String)} */
    public static String unescape(String str) {
        return StringConvertOps.unescape(str);
    }

    /** 构建签字字典树，同 {@link StringConvertOps#build(Collection)} */
    public static TrieNode build(Collection<String> collections) {
        return StringConvertOps.build(collections);
    }
}
