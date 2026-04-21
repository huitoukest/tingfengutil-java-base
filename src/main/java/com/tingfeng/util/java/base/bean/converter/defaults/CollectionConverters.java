package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;

import java.util.List;

/**
 * 集合与数组类型转换器注册
 * <p>
 * Object[] ↔ List 转换
 */
public final class CollectionConverters {

    private CollectionConverters() {}

    public static void register(ConverterRegistry registry) {
        registry.register(ConverterUtils.of(
                Object[].class, List.class,
                arr -> java.util.Arrays.asList(arr)
        ));

        registry.register(ConverterUtils.of(
                List.class, Object[].class,
                list -> list.toArray()
        ));
    }
}