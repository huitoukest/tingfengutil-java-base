package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;

import java.nio.charset.StandardCharsets;

import static com.tingfeng.util.java.base.lang.StringUtils.encodeURL;
import static com.tingfeng.util.java.base.lang.StringUtils.getBoolean;
import static com.tingfeng.util.java.base.lang.StringUtils.getByte;
import static com.tingfeng.util.java.base.lang.StringUtils.getDouble;
import static com.tingfeng.util.java.base.lang.StringUtils.getFloat;
import static com.tingfeng.util.java.base.lang.StringUtils.getInteger;
import static com.tingfeng.util.java.base.lang.StringUtils.getLong;
import static com.tingfeng.util.java.base.lang.StringUtils.getShort;
import static com.tingfeng.util.java.base.lang.StringUtils.toDecodeStringUrl;

/**
 * String 类型转换器注册
 * <p>
 * 所有 src=String 的转换器
 */
public final class StringConverters {

    private StringConverters() {}

    public static void register(ConverterRegistry registry) {
        // String -> Number（带条件判断）
        registry.register(ConverterUtils.of(
                String.class, Integer.class,
                StringConverters::isIntegerString,
                s -> getInteger(s, null, null)
        ));
        registry.register(ConverterUtils.of(
                String.class, Long.class,
                StringConverters::isLongString,
                s -> getLong(s, null, null)
        ));
        registry.register(ConverterUtils.of(
                String.class, Double.class,
                StringConverters::isDoubleString,
                s -> getDouble(s, null, null)
        ));
        registry.register(ConverterUtils.of(
                String.class, Float.class,
                StringConverters::isFloatString,
                s -> getFloat(s, null, null)
        ));
        registry.register(ConverterUtils.of(
                String.class, Short.class,
                StringConverters::isShortString,
                s -> getShort(s, null, null)
        ));
        registry.register(ConverterUtils.of(
                String.class, Byte.class,
                StringConverters::isByteString,
                s -> getByte(s, null, null)
        ));

        // String -> Boolean
        registry.register(ConverterUtils.of(
                String.class, Boolean.class,
                s -> "true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s),
                s -> getBoolean(s, null, null)
        ));

        // String -> Character
        registry.register(ConverterUtils.of(
                String.class, Character.class,
                s -> s != null && !s.isEmpty(),
                s -> s.charAt(0)
        ));

        // Character/CharSequence -> String
        registry.register(ConverterUtils.of(
                Character.class, String.class,
                s -> String.valueOf(s)
        ));
        registry.register(ConverterUtils.of(
                CharSequence.class, String.class,
                s -> s.toString()
        ));

        // String -> Date/LocalDateTime/LocalDate
        registry.register(ConverterUtils.of(
                String.class, java.util.Date.class,
                StringConverters::isDateString,
                s -> com.tingfeng.util.java.base.datetime.DateUtils.getDate(s)
        ));
        registry.register(ConverterUtils.of(
                String.class, java.time.LocalDateTime.class,
                StringConverters::isDateString,
                s -> com.tingfeng.util.java.base.datetime.LocalDateUtils.getLocalDateTime(s)
        ));
        registry.register(ConverterUtils.of(
                String.class, java.time.LocalDate.class,
                StringConverters::isDateString,
                s -> com.tingfeng.util.java.base.datetime.LocalDateUtils.getLocalDate(s)
        ));

        // String <-> byte[]（UTF-8）
        registry.register(ConverterUtils.of(
                String.class, byte[].class,
                s -> s.getBytes(StandardCharsets.UTF_8)
        ));

        // String -> String（URL encode/decode）
        registry.register(ConverterUtils.of(
                String.class, String.class,
                s -> s != null,
                s -> encodeURL(s, "UTF-8")
        ));
        registry.register(ConverterUtils.of(
                String.class, String.class,
                s -> s != null,
                s -> toDecodeStringUrl(s, "UTF-8")
        ));
    }

    // ==================== 辅助判断方法 ====================

    public static boolean isIntegerString(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isLongString(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        String trimmed = s.trim();
        if (trimmed.endsWith("L") || trimmed.endsWith("l")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        try {
            Long.parseLong(trimmed);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDoubleString(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isFloatString(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        try {
            Float.parseFloat(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isShortString(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        try {
            Short.parseShort(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isByteString(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        try {
            Byte.parseByte(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDateString(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        return java.util.regex.Pattern.matches(".*\\d.*[-:/].*", s);
    }
}
