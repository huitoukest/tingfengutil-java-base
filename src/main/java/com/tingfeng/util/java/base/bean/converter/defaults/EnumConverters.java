package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterConstants;
import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;
import com.tingfeng.util.java.base.lang.base.IEnum;
import com.tingfeng.util.java.base.lang.support.GenericTypeUtils;

import java.util.Optional;

/**
 * 枚举类型转换器注册
 */
public final class EnumConverters {

    private EnumConverters() {}

    /**
     * 注册所有枚举转换器到指定注册中心
     *
     * @param registry 转换器注册中心
     */
    public static void register(ConverterRegistry registry) {
        if (registry == null) {
            return;
        }

        // ==================== Enum -> String ====================
        // IEnum 且泛型为 String：条件Converter，ORDER_DEFAULT 优先
        registry.register(ConverterUtils.of(
                Enum.class, String.class, ConverterConstants.ORDER_DEFAULT,
                e -> GenericTypeUtils.isImplGenericInterface(e.getClass(), IEnum.class, String.class),
                e -> ((IEnum<String>) e).getValue()
        ));
        // 普通枚举 -> String：非条件Converter，直接取 name()
        registry.register(ConverterUtils.of(
                Enum.class, String.class,
                Enum::name
        ));

        // ==================== Enum -> Integer ====================
        // IEnum<Number>：条件Converter，ORDER_DEFAULT
        registry.register(ConverterUtils.of(
                Enum.class, Integer.class, ConverterConstants.ORDER_DEFAULT,
                e -> GenericTypeUtils.isImplGenericInterface(e.getClass(), IEnum.class, Integer.class),
                e -> ((IEnum<Integer>) e).getValue()
        ));
        // 普通枚举 -> Integer：非条件Converter
        registry.register(ConverterUtils.of(
                Enum.class, Integer.class,
                Enum::ordinal
        ));

        // ==================== Enum -> Long ====================
        registry.register(ConverterUtils.of(
                Enum.class, Long.class, ConverterConstants.ORDER_DEFAULT,
                e -> GenericTypeUtils.isImplGenericInterface(e.getClass(), IEnum.class, Long.class),
                e -> ((IEnum<Long>) e).getValue()
        ));
        // ==================== Enum -> Long ====================
        registry.register(ConverterUtils.of(
                Enum.class, Long.class, ConverterConstants.ORDER_DEFAULT,
                e -> GenericTypeUtils.isImplGenericInterface(e.getClass(), IEnum.class, Integer.class),
                e -> Optional.ofNullable(((IEnum<Integer>) e).getValue()).map(Integer::longValue).orElse(null)
        ));
        registry.register(ConverterUtils.of(
                Enum.class, Long.class,
                e -> (long) e.ordinal()
        ));

        // ==================== Enum -> Byte ====================
        registry.register(ConverterUtils.of(
                Enum.class, Byte.class, ConverterConstants.ORDER_DEFAULT,
                e -> GenericTypeUtils.isImplGenericInterface(e.getClass(), IEnum.class, Short.class),
                e -> ((IEnum<Byte>) e).getValue()
        ));

        // ==================== Enum -> Short ====================
        registry.register(ConverterUtils.of(
                Enum.class, Short.class, ConverterConstants.ORDER_DEFAULT,
                e -> GenericTypeUtils.isImplGenericInterface(e.getClass(), IEnum.class, Short.class),
                e -> ((IEnum<Short>) e).getValue()
        ));

        // ==================== Enum -> int ====================
        registry.register(ConverterUtils.of(
                Enum.class, int.class,
                Enum::ordinal
        ));

        // ==================== Enum -> long ====================
        registry.register(ConverterUtils.of(
                Enum.class, long.class,
                e -> (long) e.ordinal()
        ));

        registry.register(ConverterUtils.of(
                Enum.class, short.class,
                e -> (short) e.ordinal()
        ));
    }
}
