package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;

import java.util.Optional;

/**
 * Object 类型转换器注册
 * <p>
 * Object -> Optional 转换
 */
public final class ObjectConverters {

    private ObjectConverters() {}

    public static void register(ConverterRegistry registry) {
        registry.register(ConverterUtils.of(
                Object.class, Optional.class,
                obj -> Optional.ofNullable(obj)
        ));
    }
}