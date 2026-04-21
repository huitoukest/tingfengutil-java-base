package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;
import com.tingfeng.util.java.base.lang.EnumUtils;

/**
 * 枚举类型转换器注册
 * <p>
 * 由于 Java 泛型擦除，无法通过单一通用转换器覆盖所有 Enum 类型。
 * 提供 {@link #registerEnum(Class, ConverterRegistry)} 方法注册特定枚举类的转换器。
 * </p>
 * <p>
 * 每个枚举类注册以下转换器：
 * <ul>
 *   <li>String -> Enum（大小写敏感，order=10，先于忽略大小写匹配）</li>
 *   <li>String -> Enum（忽略大小写，order=20）</li>
 *   <li>Integer -> Enum（通过 ordinal，order=10）</li>
 *   <li>Enum -> String（通过 name）</li>
 *   <li>Enum -> Integer（通过 ordinal）</li>
 * </ul>
 * </p>
 * <p>
 * 示例：
 * <pre>
 * ConverterRegistry registry = ConverterUtils.getInstance();
 * EnumConverters.registerEnum(MyEnum.class, registry);
 * MyEnum result = ConverterUtils.convert("VALUE_NAME", MyEnum.class);
 * </pre>
 */
public final class EnumConverters {

    private EnumConverters() {}

    /**
     * 注册指定枚举类型的转换器到注册中心
     *
     * @param enumClass 枚举类，非 null
     * @param registry  转换器注册中心，非 null
     * @param <E>       枚举类型
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> void registerEnum(Class<E> enumClass, ConverterRegistry registry) {
        if (enumClass == null || registry == null) {
            return;
        }

        // String -> Enum（大小写敏感，order=10，先于忽略大小写匹配）
        registry.register(ConverterUtils.of(
                String.class, enumClass,
                10,
                s -> {
                    if (s == null || s.isEmpty()) {
                        return false;
                    }
                    try {
                        Enum.valueOf(enumClass, s);
                        return true;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                },
                s -> {
                    if (s == null) {
                        return null;
                    }
                    return Enum.valueOf(enumClass, s);
                }
        ));

        // String -> Enum（忽略大小写，order=20）
        registry.register(ConverterUtils.of(
                String.class, enumClass,
                20,
                s -> s != null && !s.isEmpty(),
                s -> {
                    if (s == null) {
                        return null;
                    }
                    return EnumUtils.getEnumByName(enumClass, s);
                }
        ));

        // Integer -> Enum（通过 ordinal，order=10）
        registry.register(ConverterUtils.of(
                Integer.class, enumClass,
                10,
                ordinal -> {
                    if (ordinal == null) {
                        return false;
                    }
                    E[] constants = enumClass.getEnumConstants();
                    return ordinal >= 0 && ordinal < constants.length;
                },
                ordinal -> {
                    if (ordinal == null) {
                        return null;
                    }
                    return EnumUtils.getByOrdinal(enumClass, ordinal);
                }
        ));

        // Enum -> String
        registry.register(ConverterUtils.of(
                enumClass, String.class,
                e -> e == null ? null : e.name()
        ));

        // Enum -> Integer
        registry.register(ConverterUtils.of(
                enumClass, Integer.class,
                e -> e == null ? null : e.ordinal()
        ));
    }
}
