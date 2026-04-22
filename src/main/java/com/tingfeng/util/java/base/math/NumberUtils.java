package com.tingfeng.util.java.base.math;

import java.math.BigInteger;

/**
 * 数值相关的计算和判断
 */
public class NumberUtils {

    /** 9999年12月31日 23:59:59.999（UTC）对应的毫秒数 */
    public static final long MAX_MILLI_SECONDS = 253402300799999L;

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
}