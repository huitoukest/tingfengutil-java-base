package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Number 类型转换器注册
 * <p>
 * 分类原则：源类型为 Number 或其子类时，归类到此。
 * </p>
 */
public final class NumberConverters {

    private NumberConverters() {}

    public static void register(ConverterRegistry registry) {
        // Number 子类之间互转
        registerNumberToNumber(registry);

        // Number -> BigDecimal / BigInteger
        registerNumberToBig(registry);
    }

    private static void registerNumberToNumber(ConverterRegistry registry) {
        registry.register(ConverterUtils.of(
                Number.class, Integer.class, 20,
                s -> s != null && !isTargetType(s, Integer.class),
                Number::intValue
        ));
        registry.register(ConverterUtils.of(
                Number.class, Long.class, 20,
                s -> s != null && !isTargetType(s, Long.class),
                Number::longValue
        ));
        registry.register(ConverterUtils.of(
                Number.class, Double.class, 20,
                s -> s != null && !isTargetType(s, Double.class),
                Number::doubleValue
        ));
        registry.register(ConverterUtils.of(
                Number.class, Float.class, 20,
                s -> s != null && !isTargetType(s, Float.class),
                Number::floatValue
        ));
        registry.register(ConverterUtils.of(
                Number.class, Short.class, 20,
                s -> s != null && !isTargetType(s, Short.class),
                Number::shortValue
        ));
        registry.register(ConverterUtils.of(
                Number.class, Byte.class, 20,
                s -> s != null && !isTargetType(s, Byte.class),
                Number::byteValue
        ));
    }

    private static void registerNumberToBig(ConverterRegistry registry) {
        // Long -> BigDecimal
        registry.register(ConverterUtils.of(
                Long.class, BigDecimal.class,
                BigDecimal::valueOf
        ));

        // Long -> BigInteger
        registry.register(ConverterUtils.of(
                Long.class, BigInteger.class,
                BigInteger::valueOf
        ));

        // Integer -> BigDecimal
        registry.register(ConverterUtils.of(
                Integer.class, BigDecimal.class,
                i -> BigDecimal.valueOf(i.longValue())
        ));

        // Integer -> BigInteger
        registry.register(ConverterUtils.of(
                Integer.class, BigInteger.class,
                i -> BigInteger.valueOf(i.longValue())
        ));

        // Short -> BigDecimal
        registry.register(ConverterUtils.of(
                Short.class, BigDecimal.class,
                s -> BigDecimal.valueOf(s.longValue())
        ));

        // Short -> BigInteger
        registry.register(ConverterUtils.of(
                Short.class, BigInteger.class,
                s -> BigInteger.valueOf(s.longValue())
        ));

        // Byte -> BigDecimal
        registry.register(ConverterUtils.of(
                Byte.class, BigDecimal.class,
                b -> BigDecimal.valueOf(b.longValue())
        ));

        // Byte -> BigInteger
        registry.register(ConverterUtils.of(
                Byte.class, BigInteger.class,
                b -> BigInteger.valueOf(b.longValue())
        ));

        // Double -> BigDecimal
        registry.register(ConverterUtils.of(
                Double.class, BigDecimal.class,
                BigDecimal::valueOf
        ));

        // Double -> BigInteger
        registry.register(ConverterUtils.of(
                Double.class, BigInteger.class,
                d -> BigDecimal.valueOf(d).toBigInteger()
        ));

        // Float -> BigDecimal
        registry.register(ConverterUtils.of(
                Float.class, BigDecimal.class,
                f -> BigDecimal.valueOf(f.doubleValue())
        ));

        // Float -> BigInteger
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

        // Number -> BigInteger（泛型擦除兜底）
        registry.register(ConverterUtils.of(
                Number.class, BigInteger.class,
                n -> n instanceof BigInteger ? (BigInteger) n
                    : n instanceof BigDecimal ? ((BigDecimal) n).toBigInteger()
                    : BigInteger.valueOf(n.longValue())
        ));
    }

    private static boolean isTargetType(Number s, Class<?> targetType) {
        return targetType.isInstance(s);
    }
}
