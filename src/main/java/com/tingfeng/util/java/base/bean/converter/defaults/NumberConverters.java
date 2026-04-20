package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;

/**
 * Number 类型转换器注册
 */
public final class NumberConverters {

    private NumberConverters() {}

    public static void register(ConverterRegistry registry) {
        registerNumberConverters(registry);
    }

    private static void registerNumberConverters(ConverterRegistry registry) {
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

    /**
     * 判断 Number 是否为目标类型
     */
    private static boolean isTargetType(Number s, Class<?> targetType) {
        return targetType.isInstance(s);
    }
}
