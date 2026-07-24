package com.tingfeng.util.java.base.file;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import com.tingfeng.util.java.base.lang.exception.BaseException;

/**
 * Properties 属性文件读写工具类。
 *
 * 提供 Properties 文件的加载、存储、便捷取值和追加合并功能，
 * 默认编码为 UTF-8（不同于 JDK Properties 的 ISO 8859-1 默认编码），
 * 可通过 Charset 参数指定其他编码。
 *
 * 注意：本类不保证线程安全，并发场景下需由调用方自行控制。
 *
 * @author huitoukest
 */
public final class PropertiesUtils {

    private PropertiesUtils() {
    }

    // ==================== 加载 ====================

    /**
     * 从文件加载 Properties（默认 UTF-8 编码）。
     *
     * @param file .properties 文件
     * @return Properties 对象（文件不存在时返回空 Properties）
     * @throws IllegalArgumentException file 为 null 时抛出
     * @throws BaseException           IO 异常时抛出
     */
    public static Properties load(File file) {
        return load(file, StandardCharsets.UTF_8);
    }

    /**
     * 从文件加载 Properties（指定编码）。
     *
     * @param file    .properties 文件
     * @param charset 字符编码
     * @return Properties 对象（文件不存在时返回空 Properties）
     * @throws IllegalArgumentException file 为 null 或 charset 为 null 时抛出
     * @throws BaseException           IO 异常时抛出
     */
    public static Properties load(File file, Charset charset) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (charset == null) {
            throw new IllegalArgumentException("Charset must not be null");
        }
        if (!file.exists()) {
            return new Properties();
        }
        try (InputStream in = new FileInputStream(file)) {
            return load(in, charset);
        } catch (IOException e) {
            throw new BaseException("Failed to load properties from file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 从 classpath 资源加载 Properties（默认 UTF-8 编码）。
     *
     * @param resourcePath classpath 资源路径（如 /config/app.properties）
     * @return Properties 对象
     * @throws IllegalArgumentException resourcePath 为 null 或空时抛出
     * @throws BaseException           资源不存在或 IO 异常时抛出
     */
    public static Properties loadFromClasspath(String resourcePath) {
        return loadFromClasspath(resourcePath, StandardCharsets.UTF_8);
    }

    /**
     * 从 classpath 资源加载 Properties（指定编码）。
     *
     * @param resourcePath classpath 资源路径
     * @param charset      字符编码
     * @return Properties 对象
     * @throws IllegalArgumentException resourcePath 为 null 或空，或 charset 为 null 时抛出
     * @throws BaseException           资源不存在或 IO 异常时抛出
     */
    public static Properties loadFromClasspath(String resourcePath, Charset charset) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("Resource path must not be null or empty");
        }
        if (charset == null) {
            throw new IllegalArgumentException("Charset must not be null");
        }
        InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resourcePath);
        if (input == null) {
            // Also try the class's own classloader
            input = PropertiesUtils.class.getResourceAsStream(resourcePath);
        }
        if (input == null) {
            throw new BaseException("Resource not found: " + resourcePath);
        }
        try {
            return load(input, charset);
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                throw new BaseException("Failed to close stream for resource: " + resourcePath, e);
            }
        }
    }

    /**
     * 从 InputStream 加载 Properties（默认 UTF-8 编码，不关闭流）。
     *
     * @param input 输入流
     * @return Properties 对象
     * @throws IllegalArgumentException input 为 null 时抛出
     * @throws BaseException            IO 异常时抛出
     */
    public static Properties load(InputStream input) {
        return load(input, StandardCharsets.UTF_8);
    }

    /**
     * 从 InputStream 加载 Properties（指定编码，不关闭流）。
     *
     * @param input   输入流
     * @param charset 字符编码
     * @return Properties 对象
     * @throws IllegalArgumentException input 为 null 或 charset 为 null 时抛出
     * @throws BaseException            IO 异常时抛出
     */
    public static Properties load(InputStream input, Charset charset) {
        if (input == null) {
            throw new IllegalArgumentException("InputStream must not be null");
        }
        if (charset == null) {
            throw new IllegalArgumentException("Charset must not be null");
        }
        Properties props = new Properties();
        try {
            Reader reader = new InputStreamReader(input, charset);
            props.load(reader);
        } catch (IOException e) {
            throw new BaseException("Failed to load properties from input stream", e);
        }
        return props;
    }

    // ==================== 存储 ====================

    /**
     * 将 Properties 写入文件（默认 UTF-8 编码，自动创建父目录）。
     *
     * @param properties 属性集合
     * @param file       目标文件
     * @param comments   注释（可选，传 null 则无注释）
     * @throws IllegalArgumentException properties 为 null 或 file 为 null 时抛出
     * @throws BaseException            IO 异常时抛出
     */
    public static void store(Properties properties, File file, String comments) {
        store(properties, file, comments, StandardCharsets.UTF_8);
    }

    /**
     * 将 Properties 写入文件（指定编码，自动创建父目录）。
     *
     * @param properties 属性集合
     * @param file       目标文件
     * @param comments   注释（可选，传 null 则无注释）
     * @param charset    字符编码
     * @throws IllegalArgumentException properties 为 null、file 为 null 或 charset 为 null 时抛出
     * @throws BaseException            IO 异常时抛出
     */
    public static void store(Properties properties, File file, String comments, Charset charset) {
        if (properties == null) {
            throw new IllegalArgumentException("Properties must not be null");
        }
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (charset == null) {
            throw new IllegalArgumentException("Charset must not be null");
        }
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (OutputStream out = new FileOutputStream(file)) {
            store(properties, out, comments);
        } catch (IOException e) {
            throw new BaseException("Failed to store properties to file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 将 Properties 写入 OutputStream（默认 UTF-8 编码，不关闭流）。
     *
     * @param properties 属性集合
     * @param output     输出流
     * @param comments   注释（可选，传 null 则无注释）
     * @throws IllegalArgumentException properties 为 null 或 output 为 null 时抛出
     * @throws BaseException            IO 异常时抛出
     */
    public static void store(Properties properties, OutputStream output, String comments) {
        if (properties == null) {
            throw new IllegalArgumentException("Properties must not be null");
        }
        if (output == null) {
            throw new IllegalArgumentException("OutputStream must not be null");
        }
        try {
            Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
            properties.store(writer, comments);
            writer.flush();
        } catch (IOException e) {
            throw new BaseException("Failed to store properties to output stream", e);
        }
    }

    // ==================== 便捷取值 ====================

    /**
     * 获取字符串属性值。
     *
     * @param props        Properties 对象
     * @param key          属性键
     * @param defaultValue 默认值（props 为 null 或键不存在时返回）
     * @return 属性值，若键不存在或 props 为 null 则返回 defaultValue
     * @throws IllegalArgumentException key 为 null 时抛出
     */
    public static String getString(Properties props, String key, String defaultValue) {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        if (props == null) {
            return defaultValue;
        }
        return props.getProperty(key, defaultValue);
    }

    /**
     * 获取整型属性值。
     *
     * @param props        Properties 对象
     * @param key          属性键
     * @param defaultValue 默认值（props 为 null、键不存在或值格式异常时返回）
     * @return 整型属性值
     * @throws IllegalArgumentException key 为 null 时抛出
     */
    public static int getInt(Properties props, String key, int defaultValue) {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        if (props == null) {
            return defaultValue;
        }
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取布尔型属性值。
     *
     * @param props        Properties 对象
     * @param key          属性键
     * @param defaultValue 默认值（props 为 null 或键不存在时返回）
     * @return 布尔型属性值（"true"/"yes"/"on"/"1" 视为 true，忽略大小写）
     * @throws IllegalArgumentException key 为 null 时抛出
     */
    public static boolean getBoolean(Properties props, String key, boolean defaultValue) {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        if (props == null) {
            return defaultValue;
        }
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        value = value.trim().toLowerCase();
        if ("true".equals(value) || "yes".equals(value)
                || "on".equals(value) || "1".equals(value)) {
            return true;
        }
        if ("false".equals(value) || "no".equals(value)
                || "off".equals(value) || "0".equals(value)) {
            return false;
        }
        return defaultValue;
    }

    // ==================== 追加/合并 ====================

    /**
     * 追加属性到现有 Properties 文件（不覆盖已有 key，默认 UTF-8 编码）。
     *
     * 实现方式：先读原文件，将传入的 props 中不在原文件中的 key 合并进去，再覆写回文件。
     *
     * @param file  目标文件
     * @param props 待追加的属性集合
     * @throws IllegalArgumentException file 为 null 或 props 为 null 时抛出
     * @throws BaseException           IO 异常时抛出
     */
    public static void append(File file, Properties props) {
        append(file, props, StandardCharsets.UTF_8);
    }

    /**
     * 追加属性到现有 Properties 文件（不覆盖已有 key，指定编码）。
     *
     * 实现方式：先读原文件，将传入的 props 中不在原文件中的 key 合并进去，再覆写回文件。
     *
     * @param file    目标文件
     * @param props   待追加的属性集合
     * @param charset 字符编码
     * @throws IllegalArgumentException file 为 null、props 为 null 或 charset 为 null 时抛出
     * @throws BaseException            IO 异常时抛出
     */
    public static void append(File file, Properties props, Charset charset) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (props == null) {
            throw new IllegalArgumentException("Properties must not be null");
        }
        if (charset == null) {
            throw new IllegalArgumentException("Charset must not be null");
        }
        // 读取现有文件
        Properties existing = load(file, charset);
        // 合并：只添加不在 existing 中的 key（不覆盖已有 key）
        for (String key : props.stringPropertyNames()) {
            if (!existing.containsKey(key)) {
                existing.setProperty(key, props.getProperty(key));
            }
        }
        // 覆写回文件
        store(existing, file, null, charset);
    }
}
