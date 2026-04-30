package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;
import lombok.SneakyThrows;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;

/**
 * URL 编码转换器注册
 */
public final class UrlConverters {

    private UrlConverters() {}

    @SneakyThrows
    public static void register(ConverterRegistry registry) {
        registry.register(ConverterUtils.of(
                URL.class, String.class, URL::toString
        ));
        registry.register(ConverterUtils.of(
                URI.class, String.class, URI::toString
        ));
        registry.register(ConverterUtils.of(
                URI.class, URL.class, URI::toURL
        ));
        registry.register(ConverterUtils.of(
                URL.class, URI.class, URL::toURI
        ));
        registry.register(ConverterUtils.of(
                URI.class, File.class, uri -> Paths.get(uri).toFile()
        ));
    }
}
