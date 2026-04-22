package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterConstants;
import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;
import com.tingfeng.util.java.base.math.NumberUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * Number 类型转换器注册
 * <p>
 * 分类原则：源类型为 Number 或其子类时，归类到此。
 * </p>
 */
public final class NumberConverters {

    private NumberConverters() {}

    public static void register(ConverterRegistry registry) {
        // Number 子类之间互转（小范围到直接转换，大范围到小范围需溢出检查）
        registerNumberToNumber(registry);

        // Number -> BigDecimal / BigInteger
        registerNumberToBig(registry);

        // Integer -> 日期时间类型（秒级有效时间）
        registerIntegerToDateTime(registry);

        // Long -> 日期时间类型（毫秒级有效时间）
        registerLongToDateTime(registry);
    }

    // ==================== Number 子类之间互转 ====================

    private static void registerNumberToNumber(ConverterRegistry registry) {
        // 小范围 -> 大范围：直接转换（无精度丢失）
        // Byte -> Short, Integer, Long, Float, Double
        registerDirect(registry, Byte.class, Short.class, b -> b.shortValue());
        registerDirect(registry, Byte.class, Integer.class, b -> b.intValue());
        registerDirect(registry, Byte.class, Long.class, Byte::longValue);
        registerDirect(registry, Byte.class, Float.class, Byte::floatValue);
        registerDirect(registry, Byte.class, Double.class, Byte::doubleValue);

        // Short -> Integer, Long, Float, Double
        registerDirect(registry, Short.class, Integer.class, s -> s.intValue());
        registerDirect(registry, Short.class, Long.class, Short::longValue);
        registerDirect(registry, Short.class, Float.class, Short::floatValue);
        registerDirect(registry, Short.class, Double.class, Short::doubleValue);

        // Integer -> Long, Float, Double
        registerDirect(registry, Integer.class, Long.class, Integer::longValue);
        registerDirect(registry, Integer.class, Float.class, Integer::floatValue);
        registerDirect(registry, Integer.class, Double.class, Integer::doubleValue);

        // Long -> Float, Double
        registerDirect(registry, Long.class, Float.class, Long::floatValue);
        registerDirect(registry, Long.class, Double.class, Long::doubleValue);

        // Float -> Double
        registerDirect(registry, Float.class, Double.class, Float::doubleValue);

        // 大范围 -> 小范围：需要溢出检查
        // Long -> Byte, Short, Integer
        registerWithOverflowCheck(registry, Long.class, Byte.class,
                l -> l >= Byte.MIN_VALUE && l <= Byte.MAX_VALUE,
                l -> l.byteValue());
        registerWithOverflowCheck(registry, Long.class, Short.class,
                l -> l >= Short.MIN_VALUE && l <= Short.MAX_VALUE,
                l -> l.shortValue());
        registerWithOverflowCheck(registry, Long.class, Integer.class,
                l -> l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE,
                l -> l.intValue());

        // Double -> Float
        registerWithOverflowCheck(registry, Double.class, Float.class,
                d -> d >= -Float.MAX_VALUE && d <= Float.MAX_VALUE,
                d -> d.floatValue());

        // Integer -> Byte, Short
        registerWithOverflowCheck(registry, Integer.class, Byte.class,
                i -> i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE,
                Integer::byteValue);
        registerWithOverflowCheck(registry, Integer.class, Short.class,
                i -> i >= Short.MIN_VALUE && i <= Short.MAX_VALUE,
                Integer::shortValue);

        // Short -> Byte
        registerWithOverflowCheck(registry, Short.class, Byte.class,
                s -> s >= Byte.MIN_VALUE && s <= Byte.MAX_VALUE,
                Short::byteValue);
    }

    private static <S, T> void registerDirect(ConverterRegistry registry,
            Class<S> src, Class<T> target, java.util.function.Function<S, T> converter) {
        registry.register(ConverterUtils.of(src, target, converter));
    }

    private static <S, T> void registerWithOverflowCheck(ConverterRegistry registry,
            Class<S> src, Class<T> target,
            java.util.function.Predicate<S> condition,
            java.util.function.Function<S, T> converter) {
        registry.register(ConverterUtils.of(src, target, ConverterConstants.ORDER_DEFAULT, condition, converter));
    }

    // ==================== Number -> BigDecimal / BigInteger ====================

    private static void registerNumberToBig(ConverterRegistry registry) {
        // Long -> BigDecimal / BigInteger
        registry.register(ConverterUtils.of(
                Long.class, BigDecimal.class,
                BigDecimal::valueOf
        ));
        registry.register(ConverterUtils.of(
                Long.class, BigInteger.class,
                BigInteger::valueOf
        ));

        // Integer -> BigDecimal / BigInteger
        registry.register(ConverterUtils.of(
                Integer.class, BigDecimal.class,
                i -> BigDecimal.valueOf(i.longValue())
        ));
        registry.register(ConverterUtils.of(
                Integer.class, BigInteger.class,
                i -> BigInteger.valueOf(i.longValue())
        ));

        // Short -> BigDecimal / BigInteger
        registry.register(ConverterUtils.of(
                Short.class, BigDecimal.class,
                s -> BigDecimal.valueOf(s.longValue())
        ));
        registry.register(ConverterUtils.of(
                Short.class, BigInteger.class,
                s -> BigInteger.valueOf(s.longValue())
        ));

        // Byte -> BigDecimal / BigInteger
        registry.register(ConverterUtils.of(
                Byte.class, BigDecimal.class,
                b -> BigDecimal.valueOf(b.longValue())
        ));
        registry.register(ConverterUtils.of(
                Byte.class, BigInteger.class,
                b -> BigInteger.valueOf(b.longValue())
        ));

        // Double -> BigDecimal / BigInteger
        registry.register(ConverterUtils.of(
                Double.class, BigDecimal.class,
                BigDecimal::valueOf
        ));
        registry.register(ConverterUtils.of(
                Double.class, BigInteger.class,
                d -> BigDecimal.valueOf(d).toBigInteger()
        ));

        // Float -> BigDecimal / BigInteger
        registry.register(ConverterUtils.of(
                Float.class, BigDecimal.class,
                f -> BigDecimal.valueOf(f.doubleValue())
        ));
        registry.register(ConverterUtils.of(
                Float.class, BigInteger.class,
                f -> BigDecimal.valueOf(f.doubleValue()).toBigInteger()
        ));

        // Number -> BigDecimal（泛型擦除兜底）
        registry.register(ConverterUtils.of(
                Number.class, BigDecimal.class,
                n -> n instanceof BigDecimal ? (BigDecimal) n
                    : n instanceof BigInteger ? new BigDecimal((BigInteger) n)
                    : BigDecimal.valueOf(n.doubleValue())
        ));
    }

    // ==================== Integer -> 日期时间类型（秒级有效时间） ====================

    private static void registerIntegerToDateTime(ConverterRegistry registry) {
        // Integer -> Date（秒转毫秒）
        registry.register(ConverterUtils.of(
                Integer.class, Date.class, ConverterConstants.ORDER_DEFAULT,
                NumberUtils::isValidSecond,
                i -> new Date(i * 1000L)
        ));

        // Integer -> LocalDateTime
        registry.register(ConverterUtils.of(
                Integer.class, LocalDateTime.class, ConverterConstants.ORDER_DEFAULT,
                NumberUtils::isValidSecond,
                i -> LocalDateTime.ofInstant(Instant.ofEpochSecond(i), ZoneId.systemDefault())
        ));

        // Integer -> LocalDate
        registry.register(ConverterUtils.of(
                Integer.class, LocalDate.class, ConverterConstants.ORDER_DEFAULT,
                NumberUtils::isValidSecond,
                i -> LocalDateTime.ofInstant(Instant.ofEpochSecond(i), ZoneId.systemDefault()).toLocalDate()
        ));

        // Integer -> Calendar
        registry.register(ConverterUtils.of(
                Integer.class, Calendar.class, ConverterConstants.ORDER_DEFAULT,
                NumberUtils::isValidSecond,
                i -> {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(i * 1000L);
                    return c;
                }
        ));

        // Integer -> Instant
        registry.register(ConverterUtils.of(
                Integer.class, Instant.class, ConverterConstants.ORDER_DEFAULT,
                NumberUtils::isValidSecond,
                Instant::ofEpochSecond
        ));
    }

    // ==================== Long -> 日期时间类型（毫秒级有效时间） ====================

    private static void registerLongToDateTime(ConverterRegistry registry) {
        // Long -> Date
        registry.register(ConverterUtils.of(
                Long.class, Date.class, ConverterConstants.ORDER_DEFAULT,
                NumberUtils::isValidMillis,
                Date::new
        ));

        // Long -> LocalDateTime
        registry.register(ConverterUtils.of(
                Long.class, LocalDateTime.class, ConverterConstants.ORDER_DEFAULT,
                NumberUtils::isValidMillis,
                l -> LocalDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneId.systemDefault())
        ));

        // Long -> LocalDate
        registry.register(ConverterUtils.of(
                Long.class, LocalDate.class, ConverterConstants.ORDER_DEFAULT,
                NumberUtils::isValidMillis,
                l -> LocalDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneId.systemDefault()).toLocalDate()
        ));

        // Long -> Calendar
        registry.register(ConverterUtils.of(
                Long.class, Calendar.class, ConverterConstants.ORDER_DEFAULT,
                NumberUtils::isValidMillis,
                l -> {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(l);
                    return c;
                }
        ));

        // Long -> Instant
        registry.register(ConverterUtils.of(
                Long.class, Instant.class, ConverterConstants.ORDER_DEFAULT,
                NumberUtils::isValidMillis,
                Instant::ofEpochMilli
        ));
    }
}