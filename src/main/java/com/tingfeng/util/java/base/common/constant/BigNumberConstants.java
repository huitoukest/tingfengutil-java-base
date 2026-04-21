package com.tingfeng.util.java.base.common.constant;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * BigNumber 相关常量
 */
public interface BigNumberConstants {

    // ==================== BigInteger 常量 ====================

    BigInteger BIG_INTEGER_BYTE_MIN = BigInteger.valueOf(Byte.MIN_VALUE);
    BigInteger BIG_INTEGER_BYTE_MAX = BigInteger.valueOf(Byte.MAX_VALUE);

    BigInteger BIG_INTEGER_SHORT_MIN = BigInteger.valueOf(Short.MIN_VALUE);
    BigInteger BIG_INTEGER_SHORT_MAX = BigInteger.valueOf(Short.MAX_VALUE);

    BigInteger BIG_INTEGER_INT_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
    BigInteger BIG_INTEGER_INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);

    BigInteger BIG_INTEGER_LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    BigInteger BIG_INTEGER_LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    long BIG_INTEGER_MAX_10_DIGIT_SECONDS = 32503680000L;
    long BIG_INTEGER_MAX_13_DIGIT_MILLIS = 32503680000000L;

    // ==================== BigDecimal 常量 ====================

    BigDecimal BIG_DECIMAL_BYTE_MIN = BigDecimal.valueOf(Byte.MIN_VALUE);
    BigDecimal BIG_DECIMAL_BYTE_MAX = BigDecimal.valueOf(Byte.MAX_VALUE);

    BigDecimal BIG_DECIMAL_SHORT_MIN = BigDecimal.valueOf(Short.MIN_VALUE);
    BigDecimal BIG_DECIMAL_SHORT_MAX = BigDecimal.valueOf(Short.MAX_VALUE);

    BigDecimal BIG_DECIMAL_INT_MIN = BigDecimal.valueOf(Integer.MIN_VALUE);
    BigDecimal BIG_DECIMAL_INT_MAX = BigDecimal.valueOf(Integer.MAX_VALUE);

    BigDecimal BIG_DECIMAL_LONG_MIN = BigDecimal.valueOf(Long.MIN_VALUE);
    BigDecimal BIG_DECIMAL_LONG_MAX = BigDecimal.valueOf(Long.MAX_VALUE);

    BigDecimal BIG_DECIMAL_FLOAT_MIN = BigDecimal.valueOf(-Float.MAX_VALUE);
    BigDecimal BIG_DECIMAL_FLOAT_MAX = BigDecimal.valueOf(Float.MAX_VALUE);

    BigDecimal BIG_DECIMAL_DOUBLE_MIN = BigDecimal.valueOf(-Double.MAX_VALUE);
    BigDecimal BIG_DECIMAL_DOUBLE_MAX = BigDecimal.valueOf(Double.MAX_VALUE);

    // ==================== BigFraction 常量 ====================

    int BIG_FRACTION_DIVIDE_SCALE = 15;
    RoundingMode BIG_FRACTION_DIVIDE_ROUNDING_MODE = RoundingMode.HALF_UP;
}
