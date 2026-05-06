package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;

import java.util.Optional;

/**
 * Optional 类型转换器注册
 * <p>
 * 仅提供拆箱功能：Optional -> 具体类型
 */
public final class OptionalConverters {

    private OptionalConverters() {}

    public static void register(ConverterRegistry registry) {
        // Optional<T> -> T（非空时获取值）
        registry.register(ConverterUtils.of(
                Optional.class, Object.class,
                opt -> opt.orElse(null)
        ));
    }
}