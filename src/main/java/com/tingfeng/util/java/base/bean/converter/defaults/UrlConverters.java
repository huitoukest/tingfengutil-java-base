package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;

import static com.tingfeng.util.java.base.lang.StringUtils.encodeURL;
import static com.tingfeng.util.java.base.lang.StringUtils.toDecodeStringUrl;

/**
 * URL 编码转换器注册
 */
public final class UrlConverters {

    private UrlConverters() {}

    public static void register(ConverterRegistry registry) {
        registry.register(ConverterUtils.of(
                String.class, String.class, 30,
                s -> s != null,
                s -> encodeURL(s, "UTF-8")
        ));
        registry.register(ConverterUtils.of(
                String.class, String.class, 30,
                s -> s != null,
                s -> toDecodeStringUrl(s, "UTF-8")
        ));
    }
}
