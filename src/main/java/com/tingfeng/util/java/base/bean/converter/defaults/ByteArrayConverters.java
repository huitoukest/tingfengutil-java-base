package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.array.ArrayUtils;
import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;
import com.tingfeng.util.java.base.file.FileUtils;
import com.tingfeng.util.java.base.io.IOUtils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * 字节数组类型转换器注册
 * <p>
 * src=byte[] 或 src=Byte[] 的转换器
 */
public final class ByteArrayConverters {

    private ByteArrayConverters() {}

    public static void register(ConverterRegistry registry) {
        // ========== byte[] 简单转换器 ==========

        // byte[] -> String（默认编码）
        registry.register(ConverterUtils.of(
                byte[].class, String.class,
                s -> new String(s, Charset.defaultCharset())
        ));

        // byte[] -> InputStream
        registry.register(ConverterUtils.of(
                byte[].class, InputStream.class,
                IOUtils::toInputStream
        ));

        // byte[] -> ByteBuffer
        registry.register(ConverterUtils.of(
                byte[].class, ByteBuffer.class,
                ByteBuffer::wrap
        ));

        // byte[] -> File（写入临时文件）
        registry.register(ConverterUtils.of(
                byte[].class, java.io.File.class,
                s -> {
                    java.io.File tempFile = createTempFile();
                    FileUtils.writeByteArrayToFile(tempFile, s);
                    return tempFile;
                }
        ));

        // byte[] -> byte[]（拷贝）
        registry.register(ConverterUtils.of(
                byte[].class, Byte[].class,
                ByteArrayConverters::wrap
        ));

        // ========== Byte[] 条件转换器（仅当所有元素不为 null） ==========

        // Byte[] -> String
        registry.register(ConverterUtils.of(
                Byte[].class, String.class,
                ByteArrayConverters::allNonNull,
                s -> new String(unwrap(s), Charset.defaultCharset())
        ));

        // Byte[] -> InputStream
        registry.register(ConverterUtils.of(
                Byte[].class, InputStream.class,
                ByteArrayConverters::allNonNull,
                s -> IOUtils.toInputStream(unwrap(s))
        ));

        // Byte[] -> File
        registry.register(ConverterUtils.of(
                Byte[].class, java.io.File.class,
                ByteArrayConverters::allNonNull,
                s -> {
                    java.io.File tempFile = createTempFile();
                    FileUtils.writeByteArrayToFile(tempFile, unwrap(s));
                    return tempFile;
                }
        ));

        // Byte[] -> byte[]
        registry.register(ConverterUtils.of(
                Byte[].class, byte[].class,
                ByteArrayConverters::allNonNull,
                ByteArrayConverters::unwrap
        ));
    }

    // ========== 辅助方法 ==========

    /**
     * 检查 Byte[] 数组所有元素是否都不为 null
     */
    private static boolean allNonNull(Byte[] array) {
        if (array == null) return false;
        for (Byte b : array) {
            if (b == null) return false;
        }
        return true;
    }

    /**
     * Byte[] -> byte[]（解包装）
     */
    private static Byte[] wrap(byte[] array) {
        Byte[] result = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    /**
     * Byte[] -> byte[]（解包装）
     */
    private static byte[] unwrap(Byte[] array) {
        byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    /**
     * 创建临时文件
     */
    private static java.io.File createTempFile() {
        try {
            return java.io.File.createTempFile("converter_", ".tmp");
        } catch (java.io.IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
    }
}