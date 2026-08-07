package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.lang.base.Tuple2;
import com.tingfeng.util.java.base.lang.inter.returnfunction.FunctionROne;
import com.tingfeng.util.java.base.text.RegExpUtils;

/**
 * 字符串数值解析与类型判断操作实现。
 *
 * 包级私有，不对外暴露。通过 {@link StringUtils} 对外提供统一 API。
 */
class StringParseOps {

    /**
     * Integer范围正则（用于预过滤）
     * - 支持 +/- 符号
     * - 忽略前导0
     * - 10位数字精确边界判断
     */
    private static final String INTEGER_RANGE_REGEX =
            "^[\\-\\+]?(0|[1-9][0-9]{0,9})$|" +
            "^[\\-\\+]?1[0-9]{9}$|" +
            "^[\\-\\+]?2[0-0][0-9]{8}$|" +
            "^[\\-\\+]?21[0-4][0-9]{7}$|" +
            "^[\\-\\+]?214[0-6][0-9]{6}$|" +
            "^[\\-\\+]?2147[0-3][0-9]{5}$|" +
            "^[\\-\\+]?21474[0-7][0-9]{4}$|" +
            "^[\\-\\+]?214748[0-2][0-9]{3}$|" +
            "^[\\-\\+]?2147483[0-5][0-9]{2}$|" +
            "^[\\-\\+]?21474836[0-3][0-9]$|" +
            "^\\+?214748364[0-7]$|" +
            "^-214748364[0-8]$";

    private StringParseOps() {

    }

    static <T> T getValue(String value, T emptyValue, T defaultValue, FunctionROne<T, String> convert) {
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
    static Integer getInteger(String value, Integer emptyValue, Integer defaultValue) {
        return getValue(value, emptyValue, defaultValue, (str) -> Integer.parseInt(str));
    }

    /**
     * 解析字符串
     *
     * @param value
     * @param defaultValue 解析出错后返回的默认字符串
     * @return
     */
    static Integer getInteger(String value, Integer defaultValue) {
        return getInteger(value, null, defaultValue);
    }

    /**
     * 解析出错后返回的默认字符串是null
     *
     * @param value
     * @return
     */
    static Integer getInteger(String value) {
        return getInteger(value, null);
    }

    static Long getLong(String value, Long emptyValue, Long defaultValue) {
        return getValue(value, emptyValue, defaultValue, (str) -> Long.parseLong(str));
    }

    static Long getLong(String value, Long defaultValue) {
        return getLong(value, null, defaultValue);
    }

    static Long getLong(String value) {
        return getLong(value, null);
    }

    static Double getDouble(String value, Double emptyValue, Double defaultValue) {
        return getValue(value, emptyValue, defaultValue, (str) -> Double.parseDouble(str));
    }

    static Double getDouble(String value, Double defaultValue) {
        return getDouble(value, null, defaultValue);
    }

    static Double getDouble(String value) {
        return getDouble(value, null);
    }

    static Float getFloat(String value, Float emptyValue, Float defaultValue) {
        return getValue(value, emptyValue, defaultValue, (str) -> Float.parseFloat(str));
    }

    static Float getFloat(String value, Float defaultValue) {
        return getFloat(value, null, defaultValue);
    }

    static Float getFloat(String value) {
        return getFloat(value, null);
    }

    static Short getShort(String value, Short emptyValue, Short defaultValue) {
        return getValue(value, emptyValue, defaultValue, (str) -> Short.parseShort(str));
    }

    static Short getShort(String value, Short defaultValue) {
        return getShort(value, null, defaultValue);
    }

    static Short getShort(String value) {
        return getShort(value, null);
    }

    static Byte getByte(String value, Byte emptyValue, Byte defaultValue) {
        return getValue(value, emptyValue, defaultValue, (str) -> Byte.parseByte(str));
    }

    static Byte getByte(String value, Byte defaultValue) {
        return getByte(value, null, defaultValue);
    }

    static Byte getByte(String value) {
        return getByte(value, null);
    }

    static Boolean getBoolean(String value, Boolean emptyValue, Boolean defaultValue) {
        if (isEmpty(value)) {
            return emptyValue;
        }
        try {
            if ("true".equalsIgnoreCase(value)) {
                return Boolean.TRUE;
            } else if ("false".equalsIgnoreCase(value)) {
                return Boolean.FALSE;
            } else {
                return defaultValue;
            }
        } catch (Exception e) {
            return defaultValue;
        }
    }

    static Boolean getBoolean(String value, Boolean defaultValue) {
        return getBoolean(value, null, defaultValue);
    }

    static Boolean getBoolean(String value) {
        return getBoolean(value, null);
    }

    /**
     * 判断字符串是否为合法 BigInteger 格式
     *
     * 支持：整数（含正负）、十六进制(0x/0X前缀)、八进制(0前缀)
     *
     * @param s 待判断字符串
     * @return 是否为合法 BigInteger 格式
     */
    static boolean isBigInteger(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        return RegExpUtils.isMatch(s, RegExpUtils.PatternStr.BIGINTEGER, true);
    }

    /**
     * 判断字符串是否为合法 Integer 格式（十进制）
     *
     * 使用正则匹配，性能优化：避免 parse 后的字符串比较
     *
     * @param s 待判断字符串
     * @return Tuple2[是否可转为Integer,前一个值为true的时候这里可能返回有效值或者null]
     */
    static Tuple2<Boolean,Integer> safeParseInteger(String s) {
        if (s == null || s.isEmpty()) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        String trimmed = s.trim();
        //可能是Integer长度范围之内的正则判断，考虑正负号支持
        String intNumberRegex = INTEGER_RANGE_REGEX;
        if (!RegExpUtils.isMatch(trimmed, intNumberRegex, true)) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        Integer value = ObjectUtils.tryDo(() -> Integer.valueOf(trimmed));
        if (value == null) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        String valueStr = value.toString();
        // 正数需要同时比较 "100" 和 "+100" 两种格式
        boolean matches = valueStr.equals(trimmed) || (value > 0 && ("+" + valueStr).equals(trimmed));
        if (!matches) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        return new Tuple2<>(Boolean.TRUE, value);
    }

    /**
     * 判断字符串是否为合法 Float 格式
     *
     * 使用正则预过滤 + 范围验证
     *
     * @param s 待判断字符串
     * @return Tuple2[是否可转为Float, 前一个值为true的时候这里可能返回有效值或者null]
     */
    static Tuple2<Boolean, Float> safeParseFloat(String s) {
        if (s == null || s.isEmpty()) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        String trimmed = s.trim();
        // Float 范围预过滤正则：支持正负号、小数点、科学计数法前缀
        String floatRegex = "^[\\-\\+]?(0|[1-9][0-9]{0,38})(\\.\\d+)?([eE][\\-\\+]?[0-9]+)?$";
        if (!RegExpUtils.isMatch(trimmed, floatRegex, true)) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        Float value = ObjectUtils.tryDo(() -> Float.valueOf(trimmed));
        if (value == null) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        String valueStr = value.toString().replace("E", "e");
        String trimmedStr = trimmed.replace("E", "e");
        boolean matches = valueStr.equals(trimmedStr) || (value > 0 && ("+" + valueStr).equals(trimmedStr));
        if (!matches) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        return new Tuple2<>(Boolean.TRUE, value);
    }

    /**
     * 判断字符串是否为合法 Double 格式
     *
     * 使用正则预过滤 + 范围验证
     *
     * @param s 待判断字符串
     * @return Tuple2[是否可转为Double, 前一个值为true的时候这里可能返回有效值或者null]
     */
    static Tuple2<Boolean, Double> safeParseDouble(String s) {
        if (s == null || s.isEmpty()) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        String trimmed = s.trim();
        // Double 范围预过滤正则
        String doubleRegex = "^[\\-\\+]?(0|[1-9][0-9]{0,308})(\\.\\d+)?([eE][\\-\\+]?[0-9]+)?$";
        if (!RegExpUtils.isMatch(trimmed, doubleRegex, true)) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        Double value = ObjectUtils.tryDo(() -> Double.valueOf(trimmed));
        if (value == null) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        String valueStr = value.toString().replace("E", "e");
        String trimmedStr = trimmed.replace("E", "e");
        boolean matches = valueStr.equals(trimmedStr) || (value > 0 && ("+" + valueStr).equals(trimmedStr));
        if (!matches) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        return new Tuple2<>(Boolean.TRUE, value);
    }

    /**
     * 判断字符串是否为合法 Byte 格式
     *
     * 使用正则预过滤 + 范围验证
     *
     * @param s 待判断字符串
     * @return Tuple2[是否可转为Byte, 前一个值为true的时候这里可能返回有效值或者null]
     */
    static Tuple2<Boolean, Byte> safeParseByte(String s) {
        if (s == null || s.isEmpty()) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        String trimmed = s.trim();
        // Byte 范围预过滤正则：支持 +/- 符号，3位数字
        String byteRegex = "^[\\-\\+]?(0|[1-9][0-9]{0,2})$";
        if (!RegExpUtils.isMatch(trimmed, byteRegex, true)) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        Byte value = ObjectUtils.tryDo(() -> Byte.valueOf(trimmed));
        if (value == null) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        String valueStr = value.toString();
        boolean matches = valueStr.equals(trimmed) || (value > 0 && ("+" + valueStr).equals(trimmed));
        if (!matches) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        return new Tuple2<>(Boolean.TRUE, value);
    }

    /**
     * 判断字符串是否为合法 Short 格式
     *
     * 使用正则预过滤 + 范围验证
     *
     * @param s 待判断字符串
     * @return Tuple2[是否可转为Short, 前一个值为true的时候这里可能返回有效值或者null]
     */
    static Tuple2<Boolean, Short> safeParseShort(String s) {
        if (s == null || s.isEmpty()) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        String trimmed = s.trim();
        // Short 范围预过滤正则：支持 +/- 符号，5位数字
        String shortRegex = "^[\\-\\+]?(0|[1-9][0-9]{0,4})$";
        if (!RegExpUtils.isMatch(trimmed, shortRegex, true)) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        Short value = ObjectUtils.tryDo(() -> Short.valueOf(trimmed));
        if (value == null) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        String valueStr = value.toString();
        boolean matches = valueStr.equals(trimmed) || (value > 0 && ("+" + valueStr).equals(trimmed));
        if (!matches) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        return new Tuple2<>(Boolean.TRUE, value);
    }

    /**
     * 判断字符串是否为合法 Long 格式
     *
     * 使用正则预过滤 + 范围验证
     *
     * @param s 待判断字符串
     * @return Tuple2[是否可转为Long, 前一个值为true的时候这里可能返回有效值或者null]
     */
    static Tuple2<Boolean, Long> safeParseLong(String s) {
        if (s == null || s.isEmpty()) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        String trimmed = s.trim();
        // 去除可能的后缀 L/l
        boolean hasSuffix = trimmed.endsWith("L") || trimmed.endsWith("l");
        String numPart = hasSuffix ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
        // Long 范围预过滤正则：支持 +/- 符号，19位数字
        String longRegex = "^[\\-\\+]?(0|[1-9][0-9]{0,18})$";
        if (!RegExpUtils.isMatch(numPart, longRegex, true)) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        Long value = ObjectUtils.tryDo(() -> Long.valueOf(hasSuffix ? numPart : trimmed));
        if (value == null) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        String valueStr = value.toString();
        String toCompare = hasSuffix ? numPart : trimmed;
        boolean matches = valueStr.equals(toCompare) || (value > 0 && ("+" + valueStr).equals(toCompare));
        if (!matches) {
            return new Tuple2<>(Boolean.FALSE, null);
        }
        return new Tuple2<>(Boolean.TRUE, value);
    }

    /**
     * 判断字符串是否为合法 Boolean 格式
     *
     * 只匹配 "true" 或 "false"（不区分大小写）
     *
     * @param s 待判断字符串
     * @return 是否为合法 Boolean 格式
     */
    static boolean isBoolean(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        String trimmed = s.trim();
        return "true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed);
    }

    /**
     * 判断字符串是否为合法十六进制整数格式
     *
     * 十六进制前缀 0x/0X，字符范围 0-9a-fA-F
     *
     * @param s 待判断字符串
     * @return 是否为合法十六进制格式
     */
    static boolean isHex(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        String trimmed = s.trim();
        boolean negative = trimmed.startsWith("-");
        boolean positive = trimmed.startsWith("+");
        String numPart = (negative || positive) ? trimmed.substring(1) : trimmed;
        if (!numPart.startsWith("0x") && !numPart.startsWith("0X")) {
            return false;
        }
        String hexPart = numPart.substring(2);
        if (hexPart.isEmpty()) {
            return false;
        }
        for (int i = 0; i < hexPart.length(); i++) {
            char c = hexPart.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否为 URL 格式
     *
     * 使用正则匹配
     *
     * @param s 待判断字符串
     * @return 是否为合法 URL 格式
     */
    static boolean isUrl(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        return RegExpUtils.isMatch(s.trim(), RegExpUtils.PatternStr.URL, true);
    }

    /**
     * 判断字符串是否为合法 BigInteger 格式
     *
     * 支持：整数、浮点数（含正负）、科学计数法
     *
     * @param s 待判断字符串
     * @return 是否为合法 BigDecimal 格式
     */
    static boolean isBigDecimal(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        return RegExpUtils.isMatch(s, RegExpUtils.PatternStr.BIGDECIMAL, true);
    }

    /**
     * 判断一个字符串是null或者是空白字符串
     *
     * @param value
     * @param isTrim 是否trim
     * @return
     */
    static boolean isEmpty(String value, boolean isTrim) {
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
    static boolean isEmpty(String value) {
        return isEmpty(value, true);
    }

    /**
     * 判断对象是否为空
     *
     * @param str
     * @return
     */
    static boolean isNotEmpty(String str, boolean isTrim) {
        return !isEmpty(str, isTrim);
    }

    /**
     * 判断对象是否为空
     *
     * @param str
     * @return
     */
    static boolean isNotEmpty(String str) {
        return isNotEmpty(str, true);
    }

    /**
     * 是否是大写字符串
     *
     * @param str
     * @return
     */
    static boolean isUpperCase(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isUpperCase(str.charAt(i))) {
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
    static boolean equals(String s1, String s2) {
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
    static Boolean isCharSequence(Class<?> cls) {
        return Boolean.valueOf(cls != null && CharSequence.class.isAssignableFrom(cls));
    }

    /**
     * 是否是CharSequence的子类
     *
     * @param className 类的名称
     * @return
     */
    static Boolean isCharSequence(String className) {
        try {
            return isCharSequence(Class.forName(className));
        } catch (ClassNotFoundException var2) {
            return Boolean.FALSE;
        }
    }

    /**
     * 判断 str 中的内容是否与 content中每一项的toString 连接后的内容相等
     *
     * @param str 字符串
     * @param content 内容
     * @return 是否值相等
     */
    static boolean equals(String str, Object... content) {
        if (str == null) {
            return false;
        }
        if (content == null) {
            return false;
        }
        if (content.length == 0 && "".equals(str)) {
            return true;
        }
        int compareIndex = 0;
        for (int i = 0; i < content.length; i++) {
            String item = String.valueOf(content[i]);
            int maxCompareLength = compareIndex + item.length();
            if (maxCompareLength > str.length()) {
                return false;
            }
            if (!StringUtils.equals(str, compareIndex, maxCompareLength, item, 0, item.length())) {
                return false;
            }
            compareIndex = maxCompareLength;
        }
        if (compareIndex != str.length()) {
            return false;
        }
        return true;
    }
}
