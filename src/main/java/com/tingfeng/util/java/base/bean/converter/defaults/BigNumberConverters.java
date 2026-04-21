package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConditionConverter;
import com.tingfeng.util.java.base.bean.converter.ConverterConstants;
import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;
import com.tingfeng.util.java.base.common.constant.BigNumberConstants;
import com.tingfeng.util.java.base.math.base.BigFraction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * BigDecimal / BigInteger 类型转换器注册
 * <p>
 * 分类原则：仅当源类型为 BigDecimal、BigInteger 或 BigFraction 时，才归类到此。
 * </p>
 */
public final class BigNumberConverters {

    private BigNumberConverters() {}

    public static void register(ConverterRegistry registry) {
        registerBigIntegerToPrimitiveAndWrapper(registry);
        registerBigIntegerToAtomic(registry);
        registerBigIntegerToTimestamp(registry);
        registerBigDecimalToPrimitiveAndWrapper(registry);
        registerBigDecimalToAtomic(registry);
        registerBigFraction(registry);
        registerBigNumberInterchange(registry);
    }

    // ==================== BigInteger -> 基础类型 & 包装类型 ====================

    private static boolean isInteger(BigDecimal bd) {
        return bd.remainder(BigDecimal.ONE).signum() == 0;
    }

    private static void registerBigIntegerToPrimitiveAndWrapper(ConverterRegistry registry) {
        // byte / Byte
        registry.register(ConverterUtils.of(
                BigInteger.class, byte.class,
                ConverterConstants.ORDER_DEFAULT,
                bi -> bi.compareTo(BigNumberConstants.BIG_INTEGER_BYTE_MIN) >= 0
                        && bi.compareTo(BigNumberConstants.BIG_INTEGER_BYTE_MAX) <= 0,
                bi -> bi.byteValue()
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, Byte.class,
                ConverterConstants.ORDER_DEFAULT,
                bi -> bi.compareTo(BigNumberConstants.BIG_INTEGER_BYTE_MIN) >= 0
                        && bi.compareTo(BigNumberConstants.BIG_INTEGER_BYTE_MAX) <= 0,
                bi -> bi.byteValue()
        ));
        
        // short / Short
        registry.register(ConverterUtils.of(
                BigInteger.class, short.class,
                ConverterConstants.ORDER_DEFAULT,
                bi -> bi.compareTo(BigNumberConstants.BIG_INTEGER_SHORT_MIN) >= 0
                        && bi.compareTo(BigNumberConstants.BIG_INTEGER_SHORT_MAX) <= 0,
                bi -> bi.shortValue()
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, Short.class,
                ConverterConstants.ORDER_DEFAULT,
                bi -> bi.compareTo(BigNumberConstants.BIG_INTEGER_SHORT_MIN) >= 0
                        && bi.compareTo(BigNumberConstants.BIG_INTEGER_SHORT_MAX) <= 0,
                bi -> bi.shortValue()
        ));

        // int / Integer
        registry.register(ConverterUtils.of(
                BigInteger.class, int.class,
                ConverterConstants.ORDER_DEFAULT,
                bi -> bi.compareTo(BigNumberConstants.BIG_INTEGER_INT_MIN) >= 0
                        && bi.compareTo(BigNumberConstants.BIG_INTEGER_INT_MAX) <= 0,
                bi -> bi.intValue()
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, Integer.class,
                ConverterConstants.ORDER_DEFAULT,
                bi -> bi.compareTo(BigNumberConstants.BIG_INTEGER_INT_MIN) >= 0
                        && bi.compareTo(BigNumberConstants.BIG_INTEGER_INT_MAX) <= 0,
                bi -> bi.intValue()
        ));

        // long / Long
        registry.register(ConverterUtils.of(
                BigInteger.class, long.class,
                ConverterConstants.ORDER_DEFAULT,
                bi -> bi.compareTo(BigNumberConstants.BIG_INTEGER_LONG_MIN) >= 0
                        && bi.compareTo(BigNumberConstants.BIG_INTEGER_LONG_MAX) <= 0,
                bi -> bi.longValue()
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, Long.class,
                ConverterConstants.ORDER_DEFAULT,
                bi -> bi.compareTo(BigNumberConstants.BIG_INTEGER_LONG_MIN) >= 0
                        && bi.compareTo(BigNumberConstants.BIG_INTEGER_LONG_MAX) <= 0,
                bi -> Long.valueOf(bi.longValue())
        ));

        // boolean / Boolean
        registry.register(ConverterUtils.of(
                BigInteger.class, boolean.class,
                ConverterConstants.ORDER_DEFAULT,
                bi -> (bi.equals(BigInteger.ZERO) || bi.equals(BigInteger.ONE)),
                bi -> BigInteger.ONE.equals(bi)
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, Boolean.class,
                ConverterConstants.ORDER_DEFAULT,
                bi -> (bi.equals(BigInteger.ZERO) || bi.equals(BigInteger.ONE)),
                bi -> BigInteger.ONE.equals(bi)
        ));
    }

    // ==================== BigInteger -> 原子类型 ====================

    private static void registerBigIntegerToAtomic(ConverterRegistry registry) {
        registry.register(ConverterUtils.of(
                BigInteger.class, AtomicLong.class,
                ConverterConstants.ORDER_DEFAULT,
                bi -> bi.compareTo(BigNumberConstants.BIG_INTEGER_LONG_MIN) >= 0
                        && bi.compareTo(BigNumberConstants.BIG_INTEGER_LONG_MAX) <= 0,
                bi -> new AtomicLong(bi.longValue())
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, AtomicInteger.class,
                ConverterConstants.ORDER_DEFAULT,
                bi -> bi.compareTo(BigNumberConstants.BIG_INTEGER_INT_MIN) >= 0
                        && bi.compareTo(BigNumberConstants.BIG_INTEGER_INT_MAX) <= 0,
                bi -> new AtomicInteger(bi.intValue())
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, AtomicBoolean.class,
                ConverterConstants.ORDER_DEFAULT,
                bi -> (bi.equals(BigInteger.ZERO) || bi.equals(BigInteger.ONE)),
                bi -> new AtomicBoolean(BigInteger.ONE.equals(bi))
        ));
    }

    // ==================== BigInteger 时间戳 -> Date 类型 ====================

    private static void registerBigIntegerToTimestamp(ConverterRegistry registry) {
        // 10位秒时间戳 -> Date 类型
        registry.register(ConverterUtils.of(
                BigInteger.class, Date.class,
                ConverterConstants.ORDER_DEFAULT,
                BigNumberConverters::isValid10DigitTimestamp,
                bi -> Date.from(Instant.ofEpochSecond(bi.longValue()))
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, java.sql.Date.class,
                ConverterConstants.ORDER_DEFAULT,
                BigNumberConverters::isValid10DigitTimestamp,
                bi -> new java.sql.Date(bi.longValue() * 1000)
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, LocalDateTime.class,
                ConverterConstants.ORDER_DEFAULT,
                BigNumberConverters::isValid10DigitTimestamp,
                bi -> LocalDateTime.ofInstant(Instant.ofEpochSecond(bi.longValue()), ZoneId.systemDefault())
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, LocalDate.class,
                ConverterConstants.ORDER_DEFAULT,
                BigNumberConverters::isValid10DigitTimestamp,
                bi -> Instant.ofEpochSecond(bi.longValue()).atZone(ZoneId.systemDefault()).toLocalDate()
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, Calendar.class,
                ConverterConstants.ORDER_DEFAULT,
                BigNumberConverters::isValid10DigitTimestamp,
                bi -> {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(bi.longValue() * 1000);
                    return c;
                }
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, ZonedDateTime.class,
                ConverterConstants.ORDER_DEFAULT,
                BigNumberConverters::isValid10DigitTimestamp,
                bi -> Instant.ofEpochSecond(bi.longValue()).atZone(ZoneId.systemDefault())
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, Instant.class,
                ConverterConstants.ORDER_DEFAULT,
                BigNumberConverters::isValid10DigitTimestamp,
                bi -> Instant.ofEpochSecond(bi.longValue())
        ));

        // 13位毫秒时间戳 -> Date 类型
        registry.register(ConverterUtils.of(
                BigInteger.class, Date.class,
                ConverterConstants.ORDER_DEFAULT,
                BigNumberConverters::isValid13DigitTimestamp,
                bi -> Date.from(Instant.ofEpochMilli(bi.longValue()))
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, java.sql.Date.class,
                ConverterConstants.ORDER_DEFAULT,
                BigNumberConverters::isValid13DigitTimestamp,
                bi -> new java.sql.Date(bi.longValue())
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, LocalDateTime.class,
                ConverterConstants.ORDER_DEFAULT,
                BigNumberConverters::isValid13DigitTimestamp,
                bi -> LocalDateTime.ofInstant(Instant.ofEpochMilli(bi.longValue()), ZoneId.systemDefault())
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, LocalDate.class,
                ConverterConstants.ORDER_DEFAULT,
                BigNumberConverters::isValid13DigitTimestamp,
                bi -> Instant.ofEpochMilli(bi.longValue()).atZone(ZoneId.systemDefault()).toLocalDate()
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, Calendar.class,
                ConverterConstants.ORDER_DEFAULT,
                BigNumberConverters::isValid13DigitTimestamp,
                bi -> {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(bi.longValue());
                    return c;
                }
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, ZonedDateTime.class,
                ConverterConstants.ORDER_DEFAULT,
                BigNumberConverters::isValid13DigitTimestamp,
                bi -> Instant.ofEpochMilli(bi.longValue()).atZone(ZoneId.systemDefault())
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, Instant.class,
                ConverterConstants.ORDER_DEFAULT,
                BigNumberConverters::isValid13DigitTimestamp,
                bi -> Instant.ofEpochMilli(bi.longValue())
        ));
    }

    // ==================== BigDecimal -> 基础类型 & 包装类型 ====================
    private static void registerBigDecimalToPrimitiveAndWrapper(ConverterRegistry registry) {
        // byte / Byte - 整数时委托给 BigInteger 转换器
        registry.register(ConverterUtils.of(
                BigDecimal.class, byte.class,
                ConverterConstants.ORDER_DEFAULT,
                bd -> isInteger(bd) && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_BYTE_MIN) >= 0
                        && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_BYTE_MAX) <= 0,
                bd -> bd.toBigInteger().byteValue()
        ));
        registry.register(ConverterUtils.of(
                BigDecimal.class, Byte.class,
                ConverterConstants.ORDER_DEFAULT,
                bd -> isInteger(bd) && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_BYTE_MIN) >= 0
                        && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_BYTE_MAX) <= 0,
                bd -> bd.toBigInteger().byteValue()
        ));

        // short / Short - 整数时委托给 BigInteger 转换器
        registry.register(ConverterUtils.of(
                BigDecimal.class, short.class,
                ConverterConstants.ORDER_DEFAULT,
                bd -> isInteger(bd) && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_SHORT_MIN) >= 0
                        && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_SHORT_MAX) <= 0,
                bd -> bd.toBigInteger().shortValue()
        ));
        registry.register(ConverterUtils.of(
                BigDecimal.class, Short.class,
                ConverterConstants.ORDER_DEFAULT,
                bd -> isInteger(bd) && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_SHORT_MIN) >= 0
                        && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_SHORT_MAX) <= 0,
                bd -> bd.toBigInteger().shortValue()
        ));

        // int / Integer - 整数时委托给 BigInteger 转换器
        registry.register(ConverterUtils.of(
                BigDecimal.class, int.class,
                ConverterConstants.ORDER_DEFAULT,
                bd -> isInteger(bd) && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_INT_MIN) >= 0
                        && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_INT_MAX) <= 0,
                bd -> bd.toBigInteger().intValue()
        ));
        registry.register(ConverterUtils.of(
                BigDecimal.class, Integer.class,
                ConverterConstants.ORDER_DEFAULT,
                bd -> isInteger(bd) && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_INT_MIN) >= 0
                        && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_INT_MAX) <= 0,
                bd -> bd.toBigInteger().intValue()
        ));

        // long / Long - 整数时委托给 BigInteger 转换器
        registry.register(ConverterUtils.of(
                BigDecimal.class, long.class,
                ConverterConstants.ORDER_DEFAULT,
                bd -> isInteger(bd) && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_LONG_MIN) >= 0
                        && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_LONG_MAX) <= 0,
                bd -> bd.toBigInteger().longValue()
        ));
        registry.register(ConverterUtils.of(
                BigDecimal.class, Long.class,
                ConverterConstants.ORDER_DEFAULT,
                bd -> isInteger(bd) && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_LONG_MIN) >= 0
                        && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_LONG_MAX) <= 0,
                bd -> bd.toBigInteger().longValue()
        ));

        // float / Float
        registry.register(ConverterUtils.of(
                BigDecimal.class, float.class,
                ConverterConstants.ORDER_DEFAULT,
                bd -> bd.compareTo(BigNumberConstants.BIG_DECIMAL_FLOAT_MIN) >= 0
                        && bd.compareTo(BigNumberConstants.BIG_DECIMAL_FLOAT_MAX) <= 0,
                bd -> bd.floatValue()
        ));
        registry.register(ConverterUtils.of(
                BigDecimal.class, Float.class,
                ConverterConstants.ORDER_DEFAULT,
                bd -> bd.compareTo(BigNumberConstants.BIG_DECIMAL_FLOAT_MIN) >= 0
                        && bd.compareTo(BigNumberConstants.BIG_DECIMAL_FLOAT_MAX) <= 0,
                bd -> bd.floatValue()
        ));

        // double / Double
        registry.register(ConverterUtils.of(
                BigDecimal.class, double.class,
                ConverterConstants.ORDER_DEFAULT,
                bd -> bd.compareTo(BigNumberConstants.BIG_DECIMAL_DOUBLE_MIN) >= 0
                        && bd.compareTo(BigNumberConstants.BIG_DECIMAL_DOUBLE_MAX) <= 0,
                bd -> bd.doubleValue()
        ));
        registry.register(ConverterUtils.of(
                BigDecimal.class, Double.class,
                ConverterConstants.ORDER_DEFAULT,
                bd -> bd.compareTo(BigNumberConstants.BIG_DECIMAL_DOUBLE_MIN) >= 0
                        && bd.compareTo(BigNumberConstants.BIG_DECIMAL_DOUBLE_MAX) <= 0,
                bd -> bd.doubleValue()
        ));
    }

    // ==================== BigDecimal -> 原子类型 ====================

    private static void registerBigDecimalToAtomic(ConverterRegistry registry) {
        registry.register(ConverterUtils.of(
                BigDecimal.class, AtomicLong.class,
                ConverterConstants.ORDER_DEFAULT,
                bd -> isInteger(bd) && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_LONG_MIN) >= 0
                        && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_LONG_MAX) <= 0,
                bd -> new AtomicLong(bd.toBigInteger().longValue())
        ));
        registry.register(ConverterUtils.of(
                BigDecimal.class, AtomicInteger.class,
                ConverterConstants.ORDER_DEFAULT,
                bd -> isInteger(bd) && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_INT_MIN) >= 0
                        && bd.toBigInteger().compareTo(BigNumberConstants.BIG_INTEGER_INT_MAX) <= 0,
                bd -> new AtomicInteger(bd.toBigInteger().intValue())
        ));
        registry.register(ConverterUtils.of(
                BigDecimal.class, AtomicBoolean.class,
                ConverterConstants.ORDER_DEFAULT,
                bd -> bd.equals(BigDecimal.ONE) || bd.equals(BigDecimal.ZERO),
                bd -> new AtomicBoolean(BigDecimal.ONE.equals(bd))
        ));
    }

    // ==================== BigFraction 源 ====================

    private static void registerBigFraction(ConverterRegistry registry) {
        registry.register(new ConditionConverter<BigFraction, BigInteger>() {
            BigFraction simpleBigFraction;
            @Override
            public BigInteger convert(BigFraction source) {
                if(simpleBigFraction != null) {
                    return simpleBigFraction.getNumerator();
                }
                throw new IllegalArgumentException("BigFraction must be simple fraction,you must call matches first.");
            }

            @Override
            public Class<BigFraction> getSourceType() {
                return BigFraction.class;
            }

            @Override
            public Class<BigInteger> getTargetType() {
                return BigInteger.class;
            }

            @Override
            public boolean matches(BigFraction source) {
                if(source.isSimpleFraction()){
                    simpleBigFraction = source;
                }
                source =  simpleBigFraction;
                return source.getDenominator().equals(BigDecimal.ONE);
            }
        });
        registry.register(ConverterUtils.of(
                BigFraction.class, BigDecimal.class,
                bf -> new BigDecimal(bf.getNumerator())
                        .divide(new BigDecimal(bf.getDenominator()), BigNumberConstants.BIG_FRACTION_DIVIDE_SCALE, BigNumberConstants.BIG_FRACTION_DIVIDE_ROUNDING_MODE)
        ));
        registry.register(ConverterUtils.of(
                BigFraction.class, double.class,
                bf -> bf.toDouble()
        ));
        registry.register(ConverterUtils.of(
                BigFraction.class, Double.class,
                bf -> bf.toDouble()
        ));
        registry.register(ConverterUtils.of(
                BigFraction.class, float.class,
                bf -> (float) bf.toDouble()
        ));
        registry.register(ConverterUtils.of(
                BigFraction.class, Float.class,
                bf -> (float) bf.toDouble()
        ));
    }

    // ==================== BigNumber 互转 ====================

    private static void registerBigNumberInterchange(ConverterRegistry registry) {
        // BigDecimal -> BigInteger: 仅整数可转换
        registry.register(ConverterUtils.of(
                BigDecimal.class, BigInteger.class,
                ConverterConstants.ORDER_DEFAULT,
                BigNumberConverters::isInteger,
                BigDecimal::toBigInteger
        ));
        // BigInteger -> BigDecimal: 始终可转换
        registry.register(ConverterUtils.of(
                BigInteger.class, BigDecimal.class,
                bi -> new BigDecimal(bi.toString())
        ));
        registry.register(ConverterUtils.of(
                BigDecimal.class, String.class,
                BigDecimal::toPlainString
        ));
        registry.register(ConverterUtils.of(
                BigInteger.class, String.class,
                BigInteger::toString
        ));
        registry.register(ConverterUtils.of(
                BigFraction.class, String.class,
                BigFraction::toString
        ));
    }

    // ==================== 时间戳判断辅助 ====================

    private static boolean isValid10DigitTimestamp(BigInteger bi) {
        if (bi == null) return false;
        if (bi.bitLength() > 36) return false;
        long val = bi.longValue();
        return val >= 0 && val <= BigNumberConstants.BIG_INTEGER_MAX_10_DIGIT_SECONDS;
    }

    private static boolean isValid13DigitTimestamp(BigInteger bi) {
        if (bi == null) return false;
        if (bi.bitLength() > 50) return false;
        long val = bi.longValue();
        return val >= 0 && val <= BigNumberConstants.BIG_INTEGER_MAX_13_DIGIT_MILLIS;
    }
}
