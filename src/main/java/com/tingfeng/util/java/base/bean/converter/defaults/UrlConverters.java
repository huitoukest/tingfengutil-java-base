package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;

/**
 * URL 编码转换器注册
 */
public final class UrlConverters {

    private UrlConverters() {}

    public static void register(ConverterRegistry registry) {
        registry.register(ConverterUtils.of(
                URL.class, String.class, URL::toString
        ));
        registry.register(ConverterUtils.of(
                URI.class, String.class, URI::toString
        ));
        registry.register(ConverterUtils.of(
                URI.class, URL.class, uri -> {
                    try { return uri.toURL(); } catch (java.net.MalformedURLException e) { throw new RuntimeException(e); }
                }
        ));
        registry.register(ConverterUtils.of(
                URL.class, URI.class, url -> {
                    try { return url.toURI(); } catch (java.net.URISyntaxException e) { throw new RuntimeException(e); }
                }
        ));
        registry.register(ConverterUtils.of(
                URI.class, File.class, uri -> Paths.get(uri).toFile()
        ));
    }
}
