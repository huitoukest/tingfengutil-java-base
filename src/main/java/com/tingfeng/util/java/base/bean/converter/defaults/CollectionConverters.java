package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterConstants;
import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;
import com.tingfeng.util.java.base.collection.CollectionUtils;

import java.util.*;

/**
 * 集合与数组类型转换器注册
 * <p>
 * Object[] ↔ List/Set 转换；基础类型数组与集合间条件转换
 */
public final class CollectionConverters {

    private CollectionConverters() {}

    public static void register(ConverterRegistry registry) {
        // ========== Object[] <-> List（简单转换器）==========
        registry.register(ConverterUtils.of(
                Object[].class, List.class,
                arr -> Arrays.asList(arr)
        ));

        registry.register(ConverterUtils.of(
                List.class, Object[].class,
                List::toArray
        ));

        // ========== Object[] <-> Set（简单转换器）==========
        registry.register(ConverterUtils.of(
                Object[].class, Set.class,
                arr -> new HashSet<>(Arrays.asList(arr))
        ));

        registry.register(ConverterUtils.of(
                Set.class, Object[].class,
                Set::toArray
        ));

        // ========== List <-> Set（简单转换器）==========
        registry.register(ConverterUtils.of(
                List.class, Set.class,
                list -> new HashSet<>(list)
        ));

        registry.register(ConverterUtils.of(
                Set.class, List.class,
                set -> new ArrayList<>(set)
        ));

        // ========== List -> 基础类型数组（条件转换器）==========
        registry.register(ConverterUtils.of(
                List.class, int[].class, ConverterConstants.ORDER_DEFAULT,
                CollectionUtils::allNonNull,
                CollectionUtils::toIntArray
        ));

        registry.register(ConverterUtils.of(
                List.class, long[].class, ConverterConstants.ORDER_DEFAULT,
                CollectionUtils::allNonNull,
                CollectionUtils::toLongArray
        ));

        registry.register(ConverterUtils.of(
                List.class, double[].class, ConverterConstants.ORDER_DEFAULT,
                CollectionUtils::allNonNull,
                CollectionUtils::toDoubleArray
        ));

        registry.register(ConverterUtils.of(
                List.class, float[].class, ConverterConstants.ORDER_DEFAULT,
                CollectionUtils::allNonNull,
                CollectionUtils::toFloatArray
        ));

        registry.register(ConverterUtils.of(
                List.class, short[].class, ConverterConstants.ORDER_DEFAULT,
                CollectionUtils::allNonNull,
                CollectionUtils::toShortArray
        ));

        registry.register(ConverterUtils.of(
                List.class, byte[].class, ConverterConstants.ORDER_DEFAULT,
                CollectionUtils::allNonNull,
                CollectionUtils::toByteArray
        ));

        registry.register(ConverterUtils.of(
                List.class, boolean[].class, ConverterConstants.ORDER_DEFAULT,
                CollectionUtils::allNonNull,
                CollectionUtils::toBooleanArray
        ));

        registry.register(ConverterUtils.of(
                List.class, char[].class, ConverterConstants.ORDER_DEFAULT,
                CollectionUtils::allNonNull,
                CollectionUtils::toCharArray
        ));

        // ========== Set -> 基础类型数组（条件转换器）==========
        registry.register(ConverterUtils.of(
                Set.class, int[].class, ConverterConstants.ORDER_DEFAULT,
                CollectionUtils::allNonNull,
                set -> CollectionUtils.toIntArray(new ArrayList<>(set))
        ));

        registry.register(ConverterUtils.of(
                Set.class, long[].class, ConverterConstants.ORDER_DEFAULT,
                CollectionUtils::allNonNull,
                set -> CollectionUtils.toLongArray(new ArrayList<>(set))
        ));

        registry.register(ConverterUtils.of(
                Set.class, double[].class, ConverterConstants.ORDER_DEFAULT,
                CollectionUtils::allNonNull,
                set -> CollectionUtils.toDoubleArray(new ArrayList<>(set))
        ));

        registry.register(ConverterUtils.of(
                Set.class, float[].class, ConverterConstants.ORDER_DEFAULT,
                CollectionUtils::allNonNull,
                set -> CollectionUtils.toFloatArray(new ArrayList<>(set))
        ));

        registry.register(ConverterUtils.of(
                Set.class, short[].class, ConverterConstants.ORDER_DEFAULT,
                CollectionUtils::allNonNull,
                set -> CollectionUtils.toShortArray(new ArrayList<>(set))
        ));

        registry.register(ConverterUtils.of(
                Set.class, byte[].class, ConverterConstants.ORDER_DEFAULT,
                CollectionUtils::allNonNull,
                set -> CollectionUtils.toByteArray(new ArrayList<>(set))
        ));

        registry.register(ConverterUtils.of(
                Set.class, boolean[].class, ConverterConstants.ORDER_DEFAULT,
                CollectionUtils::allNonNull,
                set -> CollectionUtils.toBooleanArray(new ArrayList<>(set))
        ));

        registry.register(ConverterUtils.of(
                Set.class, char[].class, ConverterConstants.ORDER_DEFAULT,
                CollectionUtils::allNonNull,
                set -> CollectionUtils.toCharArray(new ArrayList<>(set))
        ));
    }
}