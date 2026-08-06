package com.tingfeng.util.java.base.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 流写入文件工具
 *
 * 支持 InputStream/byte[]/String/List(String) 四种数据来源 × File/Path 两种目标，
 * 方法内部创建的输出流自动关闭（try-with-resources）。
 *
 * 本类为 IOUtils 拆分产物，IOUtils 门面委托本类方法，公开行为与拆分前完全一致。
 *
 * @author huitoukest
 */
public final class StreamWriteOps {

    private StreamWriteOps() {
    }

    // ==================== 流写入文件 ====================

    /**
     * 将输入流写入文件
     * 方法内部创建的输出流自动关闭（try-with-resources）
     * @param input 输入流
     * @param file 目标文件
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(InputStream input, File file) {
        if (input == null || file == null) {
            throw new IllegalArgumentException("input and file must not be null");
        }
        try (OutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
            StreamTransferOps.copy(output, input);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(
                "Failed to write to file: " + file.getPath(), e);
        }
    }

    /**
     * 将输入流写入文件
     * 方法内部创建的输出流自动关闭（try-with-resources）
     * @param input 输入流
     * @param path 目标路径
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(InputStream input, Path path) {
        if (input == null || path == null) {
            throw new IllegalArgumentException("input and path must not be null");
        }
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(path))) {
            StreamTransferOps.copy(output, input);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to write to path: " + path, e);
        }
    }

    /**
     * 将字节数组写入文件
     * 方法内部创建的输出流自动关闭（try-with-resources）
     * @param data 字节数据
     * @param file 目标文件
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(byte[] data, File file) {
        if (data == null || file == null) {
            throw new IllegalArgumentException("data and file must not be null");
        }
        try (OutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
            output.write(data);
            output.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(
                "Failed to write to file: " + file.getPath(), e);
        }
    }

    /**
     * 将字节数组写入文件
     * 方法内部创建的输出流自动关闭（try-with-resources）
     * @param data 字节数据
     * @param path 目标路径
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(byte[] data, Path path) {
        if (data == null || path == null) {
            throw new IllegalArgumentException("data and path must not be null");
        }
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(path))) {
            output.write(data);
            output.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to write to path: " + path, e);
        }
    }

    /**
     * 将字符串写入文件
     * 方法内部创建的输出流自动关闭（try-with-resources）
     * @param content 字符串内容
     * @param file 目标文件
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(String content, File file) {
        writeToFile(content, file, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串写入文件
     * 方法内部创建的输出流自动关闭（try-with-resources）
     * @param content 字符串内容
     * @param file 目标文件
     * @param charset 字符编码
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(String content, File file, Charset charset) {
        if (content == null || file == null) {
            throw new IllegalArgumentException("content and file must not be null");
        }
        try (OutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
            output.write(content.getBytes(charset));
            output.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(
                "Failed to write to file: " + file.getPath(), e);
        }
    }

    /**
     * 将字符串写入文件
     * 方法内部创建的输出流自动关闭（try-with-resources）
     * @param content 字符串内容
     * @param path 目标路径
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(String content, Path path) {
        writeToFile(content, path, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串写入文件
     * 方法内部创建的输出流自动关闭（try-with-resources）
     * @param content 字符串内容
     * @param path 目标路径
     * @param charset 字符编码
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(String content, Path path, Charset charset) {
        if (content == null || path == null) {
            throw new IllegalArgumentException("content and path must not be null");
        }
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(path))) {
            output.write(content.getBytes(charset));
            output.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to write to path: " + path, e);
        }
    }

    /**
     * 将字符串列表写入文件（按行）
     * 方法内部创建的输出流自动关闭（try-with-resources）
     * @param lines 字符串列表
     * @param file 目标文件
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(List<String> lines, File file) {
        writeToFile(lines, file, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串列表写入文件（按行）
     * 方法内部创建的输出流自动关闭（try-with-resources）
     * @param lines 字符串列表
     * @param file 目标文件
     * @param charset 字符编码
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(List<String> lines, File file, Charset charset) {
        if (lines == null || file == null) {
            throw new IllegalArgumentException("lines and file must not be null");
        }
        try (OutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
            // 预计算分隔符字节，避免循环内重复编码（性能优化）
            byte[] separator = System.lineSeparator().getBytes(charset);
            for (int i = 0; i < lines.size(); i++) {
                output.write(lines.get(i).getBytes(charset));
                if (i < lines.size() - 1) {
                    output.write(separator);
                }
            }
            output.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(
                "Failed to write to file: " + file.getPath(), e);
        }
    }

    /**
     * 将字符串列表写入文件（按行）
     * 方法内部创建的输出流自动关闭（try-with-resources）
     * @param lines 字符串列表
     * @param path 目标路径
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(List<String> lines, Path path) {
        writeToFile(lines, path, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串列表写入文件（按行）
     * 方法内部创建的输出流自动关闭（try-with-resources）
     * @param lines 字符串列表
     * @param path 目标路径
     * @param charset 字符编码
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(List<String> lines, Path path, Charset charset) {
        if (lines == null || path == null) {
            throw new IllegalArgumentException("lines and path must not be null");
        }
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(path))) {
            // 预计算分隔符字节，避免循环内重复编码（性能优化）
            byte[] separator = System.lineSeparator().getBytes(charset);
            for (int i = 0; i < lines.size(); i++) {
                output.write(lines.get(i).getBytes(charset));
                if (i < lines.size() - 1) {
                    output.write(separator);
                }
            }
            output.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to write to path: " + path, e);
        }
    }

    // ==================== 便捷写入 ====================

    /**
     * 便捷写入字符串到输出流（默认 UTF-8 编码）
     * 注意：不关闭传入的 output，由调用方负责管理；null 内容不写入
     * @param content 字符串内容
     * @param output 输出流
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void write(String content, OutputStream output) {
        write(content, output, StandardCharsets.UTF_8);
    }

    /**
     * 便捷写入字符串到输出流
     * 注意：不关闭传入的 output，由调用方负责管理；null 内容不写入
     * @param content 字符串内容
     * @param output 输出流
     * @param charset 字符编码
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void write(String content, OutputStream output, Charset charset) {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null");
        }
        if (content == null) {
            return;
        }
        try {
            output.write(content.getBytes(charset));
            output.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
    }

    /**
     * 便捷写入字节数组到输出流
     * 注意：不关闭传入的 output，由调用方负责管理；null 数据不写入
     * @param data 字节数据
     * @param output 输出流
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void write(byte[] data, OutputStream output) {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null");
        }
        if (data == null) {
            return;
        }
        try {
            output.write(data);
            output.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
    }
}
