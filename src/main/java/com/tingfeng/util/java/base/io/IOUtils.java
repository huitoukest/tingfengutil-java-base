package com.tingfeng.util.java.base.io;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * IO流处理工具类
 * <p>
 * 设计原则：
 * <ul>
 *   <li>统一运行时异常：所有方法抛出项目自定义 IOException，不抛检查型异常</li>
 *   <li>try-with-resources：所有资源管理使用自动资源关闭</li>
 *   <li>职责分离：流创建、流操作、流转换分类管理</li>
 *   <li>命名规范：方法名遵循 Java 8 风格，如 toInputStream(), copy() 等</li>
 *   <li>线程池外部注入：异步方法支持 ExecutorService / Thread / Runnable</li>
 * </ul>
 *
 * @author huitoukest
 */
public class IOUtils {

    private static final int BUFFER_SIZE = 4096;

    /**
     * 背压许可单位：1KB（每permit代表1024字节）
     */
    private static final int PERMIT_UNIT = 1024;

    /**
     * 默认背压缓冲区大小：8MB
     */
    public static final int DEFAULT_BACK_PRESSURE_BUFFER_SIZE = 8 * 1024 * 1024;

    private IOUtils() {
    }

    // ==================== 流创建（输入） ====================

    /**
     * 字符串转换为输入流
     * @param content 字符串内容
     * @return InputStream
     */
    public static InputStream toInputStream(String content) {
        return toInputStream(content, StandardCharsets.UTF_8);
    }

    /**
     * 字符串转换为输入流
     * @param content 字符串内容
     * @param charset 字符编码
     * @return InputStream
     */
    public static InputStream toInputStream(String content, Charset charset) {
        return new ByteArrayInputStream(content.getBytes(charset));
    }

    /**
     * 字节数组转换为输入流
     * @param bytes 字节数组
     * @return InputStream
     */
    public static InputStream toInputStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    /**
     * CharSequence转换为输入流（支持StringBuilder、StringBuffer等）
     * @param charSequence 字符序列
     * @return InputStream
     */
    public static InputStream toInputStream(CharSequence charSequence) {
        return toInputStream(charSequence, StandardCharsets.UTF_8);
    }

    /**
     * CharSequence转换为输入流（指定编码）
     * @param charSequence 字符序列
     * @param charset 字符编码
     * @return InputStream
     */
    public static InputStream toInputStream(CharSequence charSequence, Charset charset) {
        if (charSequence == null) {
            return new ByteArrayInputStream(new byte[0]);
        }
        return new ByteArrayInputStream(charSequence.toString().getBytes(charset));
    }

    /**
     * File转换为输入流
     * @param file 文件
     * @return InputStream
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果读取失败
     */
    public static InputStream toInputStream(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        try {
            return new BufferedInputStream(new FileInputStream(file));
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to open file: " + file.getPath(), e);
        }
    }

    /**
     * Path转换为输入流
     * @param path 文件路径
     * @return InputStream
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果读取失败
     */
    public static InputStream toInputStream(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        try {
            return new BufferedInputStream(Files.newInputStream(path));
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to open path: " + path, e);
        }
    }

    /**
     * URL转换为输入流
     * @param url URL资源
     * @return InputStream
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果打开连接失败
     */
    public static InputStream toInputStream(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("url must not be null");
        }
        try {
            return new BufferedInputStream(url.openStream());
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to open URL: " + url, e);
        }
    }

    /**
     * URI转换为输入流
     * @param uri URI资源
     * @return InputStream
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果打开连接失败
     */
    public static InputStream toInputStream(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null");
        }
        try {
            return new BufferedInputStream(uri.toURL().openStream());
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to open URI: " + uri, e);
        }
    }

    // ==================== 流创建（输出） ====================

    /**
     * 创建字节数组输出流
     * @return ByteArrayOutputStream
     */
    public static ByteArrayOutputStream createByteArrayOutputStream() {
        return new ByteArrayOutputStream();
    }

    // ==================== 流写入文件 ====================

    /**
     * 将输入流写入文件
     * @param input 输入流
     * @param file 目标文件
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(InputStream input, File file) {
        if (input == null || file == null) {
            throw new IllegalArgumentException("input and file must not be null");
        }
        try (OutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
            copy(output, input);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to write to file: " + file.getPath(), e);
        }
    }

    /**
     * 将输入流写入文件
     * @param input 输入流
     * @param path 目标路径
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(InputStream input, Path path) {
        if (input == null || path == null) {
            throw new IllegalArgumentException("input and path must not be null");
        }
        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(path))) {
            copy(output, input);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to write to path: " + path, e);
        }
    }

    /**
     * 将字节数组写入文件
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
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to write to file: " + file.getPath(), e);
        }
    }

    /**
     * 将字节数组写入文件
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
     * @param content 字符串内容
     * @param file 目标文件
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(String content, File file) {
        writeToFile(content, file, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串写入文件
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
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to write to file: " + file.getPath(), e);
        }
    }

    /**
     * 将字符串写入文件
     * @param content 字符串内容
     * @param path 目标路径
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(String content, Path path) {
        writeToFile(content, path, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串写入文件
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
     * @param lines 字符串列表
     * @param file 目标文件
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(List<String> lines, File file) {
        writeToFile(lines, file, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串列表写入文件（按行）
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
            for (int i = 0; i < lines.size(); i++) {
                output.write(lines.get(i).getBytes(charset));
                if (i < lines.size() - 1) {
                    output.write(System.lineSeparator().getBytes(charset));
                }
            }
            output.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to write to file: " + file.getPath(), e);
        }
    }

    /**
     * 将字符串列表写入文件（按行）
     * @param lines 字符串列表
     * @param path 目标路径
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果写入失败
     */
    public static void writeToFile(List<String> lines, Path path) {
        writeToFile(lines, path, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串列表写入文件（按行）
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
            for (int i = 0; i < lines.size(); i++) {
                output.write(lines.get(i).getBytes(charset));
                if (i < lines.size() - 1) {
                    output.write(System.lineSeparator().getBytes(charset));
                }
            }
            output.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to write to path: " + path, e);
        }
    }

    // ==================== 流转换 ====================

    /**
     * 输入流转换为字节数组
     * @param inputStream 输入流
     * @return 字节数组
     */
    public static byte[] toByteArray(InputStream inputStream) {
        if (inputStream == null) {
            return new byte[0];
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
    }

    /**
     * 输入流转换为字符串
     * @param inputStream 输入流
     * @return 字符串
     */
    public static String toString(InputStream inputStream) {
        return toString(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * 输入流转换为字符串
     * @param inputStream 输入流
     * @param charset 字符编码
     * @return 字符串
     */
    public static String toString(InputStream inputStream, Charset charset) {
        if (inputStream == null) {
            return null;
        }
        byte[] bytes = toByteArray(inputStream);
        return new String(bytes, charset);
    }

    /**
     * InputStream 转换为 Reader
     * @param inputStream 输入流
     * @param charset 字符编码
     * @return BufferedReader
     */
    public static Reader toReader(InputStream inputStream, Charset charset) {
        return new BufferedReader(new InputStreamReader(inputStream, charset));
    }

    /**
     * OutputStream 转换为 Writer
     * @param outputStream 输出流
     * @return BufferedWriter
     */
    public static Writer toWriter(OutputStream outputStream) {
        return new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    /**
     * Reader 转换为字符串
     * @param reader Reader
     * @return 字符串
     */
    public static String toString(Reader reader) {
        if (reader == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[BUFFER_SIZE];
        int len;
        try {
            while ((len = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, len);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
    }

    /**
     * Reader 转换为字节数组
     * @param reader Reader
     * @param charset 字符编码
     * @return 字节数组
     */
    public static byte[] toByteArray(Reader reader, Charset charset) {
        if (reader == null) {
            return new byte[0];
        }
        String str = toString(reader);
        return str.getBytes(charset);
    }

    // ==================== 缓冲包装 ====================

    /**
     * 包装为 BufferedReader
     * @param inputStream 输入流
     * @param charset 字符编码
     * @return BufferedReader
     */
    public static BufferedReader toBufferedReader(InputStream inputStream, Charset charset) {
        return new BufferedReader(new InputStreamReader(inputStream, charset), BUFFER_SIZE);
    }

    /**
     * 包装为 BufferedOutputStream
     * @param outputStream 输出流
     * @return BufferedOutputStream
     */
    public static BufferedOutputStream toBufferedOutputStream(OutputStream outputStream) {
        return new BufferedOutputStream(outputStream, BUFFER_SIZE);
    }

    /**
     * 包装为 BufferedInputStream
     * @param inputStream 输入流
     * @return BufferedInputStream
     */
    public static BufferedInputStream toBufferedInputStream(InputStream inputStream) {
        return new BufferedInputStream(inputStream, BUFFER_SIZE);
    }

    // ==================== 流拷贝 ====================

    /**
     * 流拷贝（自动关闭流）
     * @param output 输出流
     * @param input 输入流
     */
    public static void copy(OutputStream output, InputStream input) {
        copy(output, input, BUFFER_SIZE, true, null);
    }

    /**
     * 流拷贝
     * @param output 输出流
     * @param input 输入流
     * @param closeStream 是否关闭流
     */
    public static void copy(OutputStream output, InputStream input, boolean closeStream) {
        copy(output, input, BUFFER_SIZE, closeStream, null);
    }

    /**
     * 流拷贝（带回调）
     * @param output 输出流
     * @param input 输入流
     * @param readSizeCallBack 读取进度回调，参数为已读取字节数
     */
    public static void copy(OutputStream output, InputStream input, Consumer<Long> readSizeCallBack) {
        copy(output, input, BUFFER_SIZE, true, readSizeCallBack);
    }

    /**
     * 流拷贝（完整参数）
     * @param output 输出流
     * @param input 输入流
     * @param bufferSize 缓冲区大小
     * @param closeStream 是否关闭流
     * @param readSizeCallBack 读取进度回调，参数为已读取字节数
     */
    public static void copy(OutputStream output, InputStream input, int bufferSize,
                            boolean closeStream, Consumer<Long> readSizeCallBack) {
        if (output == null || input == null) {
            throw new IllegalArgumentException("output and input must not be null");
        }
        byte[] buffer = new byte[bufferSize];
        long total = 0;
        int len;
        try {
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
                total += len;
                if (readSizeCallBack != null) {
                    readSizeCallBack.accept(total);
                }
            }
            output.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        } finally {
            if (closeStream) {
                closeQuietly(input);
                closeQuietly(output);
            }
        }
    }

    // ==================== 管道连接 ====================

    /**
     * 同步管道连接：InputStream → OutputStream
     * @param output 输出流
     * @param input 输入流
     */
    public static void pipe(OutputStream output, InputStream input) {
        pipe(output, input, BUFFER_SIZE);
    }

    /**
     * 同步管道连接：InputStream → OutputStream（指定缓冲区）
     * @param output 输出流
     * @param input 输入流
     * @param bufferSize 缓冲区大小
     */
    public static void pipe(OutputStream output, InputStream input, int bufferSize) {
        copy(output, input, bufferSize, true, null);
    }

    /**
     * 同步管道连接：Reader → Writer
     * @param writer 输出
     * @param reader 输入
     */
    public static void pipe(Writer writer, Reader reader) {
        pipe(writer, reader, BUFFER_SIZE);
    }

    /**
     * 同步管道连接：Reader → Writer（指定缓冲区）
     * @param writer 输出
     * @param reader 输入
     * @param bufferSize 缓冲区大小
     */
    public static void pipe(Writer writer, Reader reader, int bufferSize) {
        if (writer == null || reader == null) {
            throw new IllegalArgumentException("writer and reader must not be null");
        }
        char[] buffer = new char[bufferSize];
        int len;
        try {
            while ((len = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, len);
            }
            writer.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
    }

    /**
     * 合并多个输入流到单一输出流
     * @param output 输出流
     * @param inputs 输入流数组
     */
    public static void joinStreams(OutputStream output, InputStream... inputs) {
        if (output == null || inputs == null) {
            throw new IllegalArgumentException("output and inputs must not be null");
        }
        try {
            for (InputStream input : inputs) {
                if (input != null) {
                    pipe(output, input);
                }
            }
        } finally {
            closeQuietly(output);
        }
    }

    // ==================== 流读取（按行） ====================

    /**
     * 按行读取流
     * @param input 输入流
     * @param lineConsumer 行处理回调
     */
    public static void readLines(InputStream input, Consumer<String> lineConsumer) {
        readLines(input, StandardCharsets.UTF_8, lineConsumer);
    }

    /**
     * 按行读取流
     * @param input 输入流
     * @param charset 字符编码
     * @param lineConsumer 行处理回调
     */
    public static void readLines(InputStream input, Charset charset, Consumer<String> lineConsumer) {
        if (input == null || lineConsumer == null) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineConsumer.accept(line);
            }
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
    }

    /**
     * 按行读取流并返回列表
     * @param input 输入流
     * @return 行列表
     */
    public static List<String> readLinesToList(InputStream input) {
        return readLinesToList(input, StandardCharsets.UTF_8);
    }

    /**
     * 按行读取流并返回列表
     * @param input 输入流
     * @param charset 字符编码
     * @return 行列表
     */
    public static List<String> readLinesToList(InputStream input, Charset charset) {
        if (input == null) {
            return new ArrayList<>();
        }
        List<String> lines = new ArrayList<>();
        readLines(input, charset, lines::add);
        return lines;
    }

    /**
     * 字符串列表合并为输入流（按行）
     * @param lines 字符串列表
     * @return InputStream
     */
    public static InputStream toInputStream(List<String> lines) {
        return toInputStream(lines, StandardCharsets.UTF_8);
    }

    /**
     * 字符串列表合并为输入流（按行）
     * @param lines 字符串列表
     * @param charset 字符编码
     * @return InputStream
     */
    public static InputStream toInputStream(List<String> lines, Charset charset) {
        if (lines == null || lines.isEmpty()) {
            return new ByteArrayInputStream(new byte[0]);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            sb.append(lines.get(i));
            if (i < lines.size() - 1) {
                sb.append(System.lineSeparator());
            }
        }
        return new ByteArrayInputStream(sb.toString().getBytes(charset));
    }

    // ==================== 资源关闭 ====================

    /**
     * 安全关闭流（安静模式，异常不抛出）
     * @param closeable 可关闭资源
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
                // 安静模式，不抛出异常
            }
        }
    }

    /**
     * 安全关闭多个流
     * @param closeables 可关闭资源数组
     */
    public static void closeQuietly(Closeable... closeables) {
        if (closeables != null) {
            for (Closeable closeable : closeables) {
                closeQuietly(closeable);
            }
        }
    }

    // ==================== 取消令牌 ====================

    /**
     * 取消令牌，用于异步任务的取消和中断控制
     */
    public static class CancellationToken {
        private volatile boolean cancelled = false;
        private volatile long timeoutMillis = 0;

        public CancellationToken() {}

        public CancellationToken(long timeoutMillis) {
            // 存储截止时间点 = 当前时间 + 超时时长
            this.timeoutMillis = System.currentTimeMillis() + timeoutMillis;
        }

        /**
         * 请求取消
         */
        public void cancel() {
            this.cancelled = true;
        }

        /**
         * 检查是否已取消
         */
        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * 检查是否超时
         */
        public boolean isTimeout() {
            return timeoutMillis > 0 && System.currentTimeMillis() > timeoutMillis;
        }

        /**
         * 检查是否应中断（取消或超时）
         */
        public boolean shouldInterrupt() {
            return cancelled || isTimeout();
        }

        /**
         * 重置取消状态
         */
        public void reset() {
            this.cancelled = false;
            this.timeoutMillis = 0;
        }
    }

    // ==================== 异步流处理 ====================

    /**
     * 异步流拷贝（带进度回调和背压控制）
     *
     * @param output 输出流
     * @param input 输入流
     * @param executor ExecutorService或Thread/Runnable，外部传入线程管理
     * @param progressCallback 进度回调，参数为已拷贝字节数，返回true继续，false暂停
     * @param backPressureLimit 背压缓冲区上限（字节），默认8MB
     * @param token 取消令牌，null表示不支持取消
     * @return CompletableFuture，任务完成后返回实际拷贝字节数
     */
    public static CompletableFuture<Long> copyAsync(OutputStream output, InputStream input,
                                                    Object executor,
                                                    Function<Long, Boolean> progressCallback,
                                                    int backPressureLimit,
                                                    CancellationToken token) {
        return CompletableFuture.supplyAsync(() -> {
            Semaphore semaphore = new Semaphore(backPressureLimit / PERMIT_UNIT);
            byte[] buffer = new byte[BUFFER_SIZE];
            long total = 0;
            int len;

            try {
                while ((len = input.read(buffer)) != -1) {
                    // 检查取消令牌
                    if (token != null && token.shouldInterrupt()) {
                        break;
                    }

                    // 背压控制：按实际读取字节数获取许可（向上取整）
                    semaphore.acquire((len + PERMIT_UNIT - 1) / PERMIT_UNIT);

                    output.write(buffer, 0, len);
                    total += len;
                    semaphore.release((len + PERMIT_UNIT - 1) / PERMIT_UNIT);

                    // 进度回调，返回false时暂停
                    if (progressCallback != null) {
                        Boolean continueRead = progressCallback.apply(total);
                        if (continueRead != null && !continueRead) {
                            // 暂停读取，等待信号
                            semaphore.acquire();
                            semaphore.release();
                        }
                    }
                }
                output.flush();
            } catch (IOException e) {
                throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new com.tingfeng.util.java.base.lang.exception.IOException("Copy operation interrupted", e);
            }
            return total;
        }, toExecutorService(executor));
    }

    /**
     * 异步流拷贝（使用默认背压限制）
     */
    public static CompletableFuture<Long> copyAsync(OutputStream output, InputStream input,
                                                    Object executor,
                                                    Function<Long, Boolean> progressCallback,
                                                    CancellationToken token) {
        return copyAsync(output, input, executor, progressCallback, DEFAULT_BACK_PRESSURE_BUFFER_SIZE, token);
    }

    /**
     * 异步按行读取流
     *
     * @param input 输入流
     * @param charset 字符编码
     * @param executor ExecutorService或Thread/Runnable
     * @param lineConsumer 行处理回调，返回true继续，false暂停（背压）
     * @param errorHandler 错误处理回调
     * @param token 取消令牌
     */
    public static void readLinesAsync(InputStream input, Charset charset,
                                     Object executor,
                                     Function<String, Boolean> lineConsumer,
                                     Consumer<Throwable> errorHandler,
                                     CancellationToken token) {
        CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 检查取消令牌
                    if (token != null && token.shouldInterrupt()) {
                        break;
                    }

                    // 回调处理
                    if (lineConsumer != null) {
                        Boolean continueRead = lineConsumer.apply(line);
                        if (continueRead != null && !continueRead) {
                            // 暂停，sleep一小段时间
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                if (errorHandler != null) {
                    errorHandler.accept(e);
                }
            }
        }, toExecutorService(executor));
    }

    /**
     * 异步按行读取流（无背压控制）
     */
    public static void readLinesAsync(InputStream input, Charset charset,
                                     Object executor,
                                     Consumer<String> lineConsumer,
                                     CancellationToken token) {
        readLinesAsync(input, charset, executor,
            line -> {
                lineConsumer.accept(line);
                return true;
            },
            null, token);
    }

    /**
     * 背压感知的流式读取（批处理模式）
     *
     * @param input 输入流
     * @param charset 字符编码
     * @param batchSize 每批行数
     * @param batchConsumer 批次回调，返回true继续，false暂停
     * @param executor ExecutorService或Thread/Runnable
     * @param backPressureLimit 背压缓冲区上限（行数）
     * @param token 取消令牌
     */
    public static void readStreamWithBackpressure(InputStream input, Charset charset,
                                                  int batchSize,
                                                  Function<List<String>, Boolean> batchConsumer,
                                                  Object executor,
                                                  int backPressureLimit,
                                                  CancellationToken token) {
        CompletableFuture.runAsync(() -> {
            LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(backPressureLimit);
            List<String> batch = new ArrayList<>(batchSize);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset))) {
                String line;
                while ((line = reader.readLine()) != null || !batch.isEmpty()) {
                    // 检查取消令牌
                    if (token != null && token.shouldInterrupt()) {
                        break;
                    }

                    if (line != null) {
                        // 尝试加入队列，背压时阻塞
                        queue.put(line);
                    }

                    // 累积到 batchSize
                    queue.drainTo(batch, batchSize - batch.size());
                    if (batch.size() >= batchSize) {
                        Boolean continueRead = batchConsumer.apply(new ArrayList<>(batch));
                        batch.clear();
                        if (continueRead != null && !continueRead) {
                            // 暂停，sleep一小段时间
                            Thread.sleep(100);
                        }
                    }
                }

                // 处理剩余行
                if (!batch.isEmpty()) {
                    batchConsumer.apply(batch);
                }
            } catch (IOException e) {
                throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new com.tingfeng.util.java.base.lang.exception.IOException("Read operation interrupted", e);
            }
        }, toExecutorService(executor));
    }

    /**
     * 背压感知的流式读取（使用默认背压限制和批次大小）
     */
    public static void readStreamWithBackpressure(InputStream input, Charset charset,
                                                  Function<List<String>, Boolean> batchConsumer,
                                                  Object executor,
                                                  CancellationToken token) {
        readStreamWithBackpressure(input, charset, 100, batchConsumer, executor,
            DEFAULT_BACK_PRESSURE_BUFFER_SIZE, token);
    }

    /**
     * 流式传输（支持流水线处理）
     *
     * @param sourceSupplier 数据源供应器
     * @param output 输出流
     * @param executor ExecutorService或Thread/Runnable
     * @param flushCallback 每写入后的回调
     * @param token 取消令牌
     * @param <T> 数据类型
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<Long> transmitStream(Supplier<T> sourceSupplier,
                                                              OutputStream output,
                                                              Object executor,
                                                              Consumer<T> flushCallback,
                                                              CancellationToken token) {
        return CompletableFuture.supplyAsync(() -> {
            long total = 0;
            T data;
            try {
                while ((data = sourceSupplier.get()) != null) {
                    // 检查取消令牌
                    if (token != null && token.shouldInterrupt()) {
                        break;
                    }

                    if (data instanceof byte[]) {
                        byte[] bytes = (byte[]) data;
                        output.write(bytes);
                        total += bytes.length;
                    } else if (data instanceof String) {
                        String str = (String) data;
                        output.write(str.getBytes(StandardCharsets.UTF_8));
                        total += str.length();
                    } else {
                        throw new IllegalArgumentException("Unsupported data type: " + data.getClass());
                    }

                    if (flushCallback != null) {
                        flushCallback.accept(data);
                    }
                }
                output.flush();
            } catch (IOException e) {
                throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
            }
            return total;
        }, toExecutorService(executor));
    }

    /**
     * 线程池工具方法：将Thread/Runnable转换为ExecutorService
     *
     * @param executor Thread、Runnable或ExecutorService
     * @return ExecutorService
     */
    public static ExecutorService toExecutorService(Object executor) {
        if (executor instanceof ExecutorService) {
            return (ExecutorService) executor;
        } else if (executor instanceof Thread) {
            Thread t = (Thread) executor;
            t.start();
            return Executors.newSingleThreadExecutor();
        } else if (executor instanceof Runnable) {
            Thread t = new Thread((Runnable) executor);
            t.start();
            return Executors.newSingleThreadExecutor();
        }
        throw new IllegalArgumentException("Unsupported executor type: " +
            (executor == null ? "null" : executor.getClass().getName()));
    }
}
