package com.tingfeng.util.java.base.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.regex.Pattern;

/**
 * 数值相关的计算和判断
 */
public class NumberUtils {

    /** 9999年12月31日 23:59:59.999（UTC）对应的毫秒数 */
    public static final long MAX_MILLI_SECONDS = 253402300799999L;

    /** 数字字符串匹配模式：可选正负号 + 整数部分（必填）+ 可选小数部分 */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[+-]?\\d+(\\.\\d+)?$");

    private NumberUtils() {}

    /**
     * 判断 Integer 值是否可作为有效秒数转换为日期
     * <p>
     * 有效范围：0 ~ Integer.MAX_VALUE 秒
     * </p>
     */
    public static boolean isValidSecond(Integer value) {
        return value != null && value >= 0;
    }

    /**
     * 判断 BigInteger 值是否可作为有效秒数转换为日期
     * <p>
     * 有效范围：0 ~ Integer.MAX_VALUE 秒
     * </p>
     */
    public static boolean isValidSecond(BigInteger bi) {
        if (bi == null) {
            return false;
        }
        if (bi.signum() < 0) {
            return false;
        }
        return bi.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0;
    }

    /**
     * 判断 Long 值是否可作为有效毫秒数转换为日期
     * <p>
     * 有效范围：0 ~ Max_Milli_Seconds
     * </p>
     */
    public static boolean isValidMillis(Long millis) {
        if (millis == null) {
            return false;
        }
        return millis >= 0 && millis <= MAX_MILLI_SECONDS;
    }

    /**
     * 判断 BigInteger 值是否可作为有效毫秒数转换为日期
     * <p>
     * 有效范围：0 ~ Max_Milli_Seconds
     * </p>
     */
    public static boolean isValidMillis(BigInteger millis) {
        if (millis == null) {
            return false;
        }
        if (millis.signum() < 0) {
            return false;
        }
        return millis.compareTo(BigInteger.valueOf(MAX_MILLI_SECONDS)) <= 0;
    }

    /**
     * 判断字符串是否为合法的十进制数字
     *
     * 格式：可选正负号 + 至少一位整数 + 可选小数部分（如 123、-12.34、+0.5）。
     *
     * @param value 待判断的字符串
     * @return 匹配返回 true；null、空串或不匹配返回 false
     */
    public static boolean isNumber(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return NUMBER_PATTERN.matcher(value).matches();
    }

    /**
     * 将 double 值四舍五入到指定小数位
     *
     * @param value 待舍入的数值
     * @param scale 保留的小数位数
     * @return 舍入后的 double 值
     * @throws IllegalArgumentException scale 小于 0 时抛出
     */
    public static double round(double value, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("scale must be >= 0: " + scale);
        }
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 判断两个 double 值在给定绝对误差范围内是否近似相等
     *
     * 语义约定：
     * 1. 两个 NaN 视为相等，返回 true
     * 2. 无穷值：a 与 b 相等时直接短路返回 true（如正无穷与正无穷）
     * 3. 其余情况：|a - b| ≤ epsilon 时返回 true
     *
     * 相对误差场景可通过 approxEquals(a / b, 1.0, eps) 表达。
     *
     * @param a 第一个数值
     * @param b 第二个数值
     * @param epsilon 允许的绝对误差，必须大于 0 且不能为 NaN
     * @return 近似相等返回 true，否则 false
     * @throws IllegalArgumentException epsilon 小于等于 0 或为 NaN 时抛出
     */
    public static boolean approxEquals(double a, double b, double epsilon) {
        if (epsilon <= 0 || Double.isNaN(epsilon)) {
            throw new IllegalArgumentException("epsilon must be > 0 and not NaN: " + epsilon);
        }
        if (a == b) {
            return true;
        }
        if (Double.isNaN(a) && Double.isNaN(b)) {
            return true;
        }
        return Math.abs(a - b) <= epsilon;
    }

    /**
     * 计算 part 占 total 的百分比
     *
     * total 为 0 时返回 Double.NaN（除零语义显式、温和，由调用方处理）。
     *
     * @param part 部分值
     * @param total 总值
     * @return part / total * 100 的百分数值；total 为 0 时返回 Double.NaN
     */
    public static double percentOf(double part, double total) {
        if (total == 0) {
            return Double.NaN;
        }
        return part / total * 100;
    }
}