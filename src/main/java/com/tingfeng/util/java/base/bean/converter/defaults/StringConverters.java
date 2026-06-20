package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.array.ArrayUtils;
import com.tingfeng.util.java.base.bean.converter.ConverterException;
import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;
import com.tingfeng.util.java.base.bean.converter.StringConditionConverter;
import com.tingfeng.util.java.base.datetime.DateUtils;
import com.tingfeng.util.java.base.datetime.LocalDateUtils;
import com.tingfeng.util.java.base.lang.StringUtils;
import com.tingfeng.util.java.base.lang.base.Tuple2;
import com.tingfeng.util.java.base.text.RegExpUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * String 类型转换器注册
 * <p>
 * 所有 src=String 的转换器
 */
public final class StringConverters {

    private StringConverters() {}

    abstract static class TryCachedStringConditionConverter<T> extends StringConditionConverter<T>{
        Tuple2<Boolean,T> tuple2;

        public abstract Tuple2<Boolean,T> tryConvert(String source);

        @Override
        public boolean matches(String source) {
            if(null == tuple2){
                tuple2 = tryConvert(source);
            }
            return tuple2.get_1();
        }

        @Override
        public T convert(String source) {
            if(tuple2 == null){
                throw new UnsupportedOperationException("must call matches method before convert");
            }
            return tuple2.get_2();
        }
    }

    /**
     *
     * @param targetCls
     * @param tryConverter
     * @return
     * @param <T>
     */
    private static <T> TryCachedStringConditionConverter<T> createTryCachedConverter(Class<T> targetCls, Function<String,Tuple2<Boolean,T>> tryConverter){
        return new TryCachedStringConditionConverter<T>(){
            @Override
            public Class<T> getTargetType() {
                return targetCls;
            }

            @Override
            public Tuple2<Boolean,T> tryConvert(String source) {
                return tryConverter.apply(source);
            }
        };
    }

    public static void register(ConverterRegistry registry) {
        // ==================== 1. 数值包装类型（直接使用 StringUtils 正则匹配）====================

        // String -> Integer
        registry.register(createTryCachedConverter(Integer.class, StringUtils::safeParseInteger));

        // String -> Long (使用 safeParseLong)
        registry.register(createTryCachedConverter(Long.class, StringUtils::safeParseLong));

        // String -> Float (使用 safeParseFloat)
        registry.register(createTryCachedConverter(Float.class, StringUtils::safeParseFloat));

        // String -> Double (使用 safeParseDouble)
        registry.register(createTryCachedConverter(Double.class, StringUtils::safeParseDouble));

        // String -> Short (使用 safeParseShort)
        registry.register(createTryCachedConverter(Short.class, StringUtils::safeParseShort));

        // String -> Byte (使用 safeParseByte)
        registry.register(createTryCachedConverter(Byte.class, StringUtils::safeParseByte));

        // ==================== 2. Boolean / Char 类型 ====================

        // String -> Boolean
        registry.register(ConverterUtils.of(
                String.class, Boolean.class,
                StringUtils::isBoolean,
                s -> Boolean.parseBoolean(s.trim())
        ));

        // String -> Character
        registry.register(ConverterUtils.of(
                String.class, Character.class,
                StringConverters::isCharString,
                s -> s.charAt(0)
        ));

        // ==================== 3. byte[] / Byte[] 数组 ====================

        // String -> byte[]（UTF-8）
        registry.register(ConverterUtils.of(
                String.class, byte[].class,
                s -> s.getBytes(Charset.defaultCharset())
        ));

        // String -> Byte[]（UTF-8）
        registry.register(ConverterUtils.of(
                String.class, Byte[].class,
                s -> {
                    byte[] bytes = s.getBytes(Charset.defaultCharset());
                    return ArrayUtils.wrapper(bytes);
                }
        ));

        // ==================== 4. URL ====================

        // String -> URL
        registry.register(ConverterUtils.of(
                String.class, java.net.URL.class,
                StringUtils::isUrl,
                s -> {
                    try {
                        return new java.net.URL(s.trim());
                    } catch (MalformedURLException e) {
                        throw new ConverterException("Failed to parse URL: " + s.trim(), e);
                    }
                }
        ));

        // ==================== 5. 日期与日历类型（多种格式支持）====================

        // String -> java.util.Date
        registry.register(ConverterUtils.of(
                String.class, java.util.Date.class,
                StringConverters::isDateString,
                s -> DateUtils.getDate(s, true)
        ));

        // String -> Calendar
        registry.register(ConverterUtils.of(
                String.class, Calendar.class,
                StringConverters::isDateString,
                s -> DateUtils.toCalendar(DateUtils.getDate(s, true))
        ));

        // String -> LocalDateTime
        registry.register(ConverterUtils.of(
                String.class, java.time.LocalDateTime.class,
                StringConverters::isDateString,
                s -> LocalDateUtils.getLocalDateTime(s, true)
        ));

        // String -> LocalDate
        registry.register(ConverterUtils.of(
                String.class, java.time.LocalDate.class,
                StringConverters::isDateString,
                s -> LocalDateUtils.getLocalDate(s, true)
        ));

        // ==================== 7. BigDecimal / BigInteger ====================

        // String -> BigDecimal
        registry.register(ConverterUtils.of(
                String.class, BigDecimal.class,
                StringUtils::isBigDecimal,
                s -> new BigDecimal(s.trim())
        ));

        // String -> BigInteger
        registry.register(ConverterUtils.of(
                String.class, BigInteger.class,
                StringUtils::isBigInteger,
                StringConverters::parseBigInteger
        ));

        // ==================== 8. Character / CharSequence -> String ====================
        // Character -> String
        registry.register(ConverterUtils.of(
                Character.class, String.class,
                s -> String.valueOf(s)
        ));

        // CharSequence -> String
        registry.register(ConverterUtils.of(
                CharSequence.class, String.class,
                s -> s.toString()
        ));
    }

    // ==================== 解析方法（已知可转换）====================

    /**
     * 解析 BigInteger，支持十六进制(0x)、八进制(0)和十进制
     */
    private static BigInteger parseBigInteger(String s) {
        String trimmed = s.trim();
        if (trimmed.startsWith("0x") || trimmed.startsWith("0X")) {
            return new BigInteger(trimmed.substring(2), 16);
        }
        if (trimmed.length() > 1 && trimmed.startsWith("0")) {
            return new BigInteger(trimmed.substring(1), 8);
        }
        return new BigInteger(trimmed);
    }

    // ==================== Character / Date 格式判断（StringConverters 私有）====================

    /**
     * 判断是否是可以转换为 Character 的字符串（非空字符串，取首字符）
     */
    public static boolean isCharString(String s) {
        return s != null && s.length() == 1;
    }

    /**
     * 判断是否是日期字符串（支持多种格式）
     * <p>
     * 支持格式：
     * - 年月: yyyy-MM
     * - 年月日: yyyy-MM-dd
     * - 年月日时分秒: yyyy-MM-dd HH:mm:ss
     * - 年月日时分秒毫秒: yyyy-MM-dd HH:mm:ss.SSS
     * - 以及其他常见变体
     */
    public static boolean isDateString(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        String trimmed = s.trim();
        // 至少包含数字和分隔符
        if (!Pattern.matches(".*\\d.*[-:/].*", trimmed)) {
            return false;
        }
        // 尝试用正则匹配常见日期格式
        return isMatchDateFormat(trimmed);
    }

    /**
     * 匹配常见日期格式的正则
     */
    private static boolean isMatchDateFormat(String s) {
        // yyyy-MM-dd HH:mm:ss.SSS
        if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} [0-2]\\d:[0-5]\\d:[0-5]\\d\\.\\d{1,3}$", s)) {
            return true;
        }
        // yyyy-MM-dd HH:mm:ss
        if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} [0-2]\\d:[0-5]\\d:[0-5]\\d$", s)) {
            return true;
        }
        // yyyy-MM-dd
        if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2}$", s)) {
            return true;
        }
        // yyyy-MM
        if (Pattern.matches("^\\d{4}-\\d{2}$", s)) {
            return true;
        }
        // HH:mm:ss
        if (Pattern.matches("^[0-2]\\d:[0-5]\\d:[0-5]\\d$", s)) {
            return true;
        }
        // 时间戳（13位毫秒或10位秒）
        if (RegExpUtils.isMatch(s, RegExpUtils.PatternStr.NATURAL_NUMBER, true)) {
            int len = s.length();
            return len == 10 || len == 13;
        }
        return false;
    }
}
