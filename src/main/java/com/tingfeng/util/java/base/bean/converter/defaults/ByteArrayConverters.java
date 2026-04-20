package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;

import java.nio.charset.StandardCharsets;

/**
 * 字节数组类型转换器注册
 * <p>
 * src=byte[] 的转换器，使用 UTF-8 编码
 */
public final class ByteArrayConverters {

    private ByteArrayConverters() {}

    public static void register(ConverterRegistry registry) {
        // byte[] -> String（UTF-8）
        registry.register(ConverterUtils.of(
                byte[].class, String.class,
                s -> new String(s, StandardCharsets.UTF_8)
        ));
    }
}
