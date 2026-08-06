package com.tingfeng.util.java.base.io;

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * IO流处理工具类（存量兼容门面）
 *
 * 全部方法委托至拆分后的实现类，签名与行为与拆分前完全一致；新增能力请直接使用实现类：
 * StreamOps（流创建/转换/缓冲/按行读取/关闭）、StreamTransferOps（拷贝/管道/合并）、
 * StreamWriteOps（写入文件）、StreamAsyncOps（异步流处理/线程池适配）。
 *
 * @author huitoukest
 */
public class IOUtils {

    /**
     * 默认背压缓冲区大小：8MB
     */
    public static final int DEFAULT_BACK_PRESSURE_BUFFER_SIZE = 8 * 1024 * 1024;

    private IOUtils() {
    }

    // ==================== 流创建（输入） ====================

    /** 字符串转换为输入流，同 {@link StreamOps#toInputStream(String)} */
    public static InputStream toInputStream(String content) {
        return StreamOps.toInputStream(content);
    }

    /** 字符串转换为输入流，同 {@link StreamOps#toInputStream(String, Charset)} */
    public static InputStream toInputStream(String content, Charset charset) {
        return StreamOps.toInputStream(content, charset);
    }

    /** 字节数组转换为输入流，同 {@link StreamOps#toInputStream(byte[])} */
    public static InputStream toInputStream(byte[] bytes) {
        return StreamOps.toInputStream(bytes);
    }

    /** CharSequence转换为输入流，同 {@link StreamOps#toInputStream(CharSequence)} */
    public static InputStream toInputStream(CharSequence charSequence) {
        return StreamOps.toInputStream(charSequence);
    }

    /** CharSequence转换为输入流（指定编码），同 {@link StreamOps#toInputStream(CharSequence, Charset)} */
    public static InputStream toInputStream(CharSequence charSequence, Charset charset) {
        return StreamOps.toInputStream(charSequence, charset);
    }

    /** File转换为输入流，同 {@link StreamOps#toInputStream(File)} */
    public static InputStream toInputStream(File file) {
        return StreamOps.toInputStream(file);
    }

    /** Path转换为输入流，同 {@link StreamOps#toInputStream(Path)} */
    public static InputStream toInputStream(Path path) {
        return StreamOps.toInputStream(path);
    }

    /** URL转换为输入流，同 {@link StreamOps#toInputStream(URL)} */
    public static InputStream toInputStream(URL url) {
        return StreamOps.toInputStream(url);
    }

    /** URI转换为输入流，同 {@link StreamOps#toInputStream(URI)} */
    public static InputStream toInputStream(URI uri) {
        return StreamOps.toInputStream(uri);
    }

    // ==================== 流创建（输出） ====================

    /** 创建字节数组输出流，同 {@link StreamOps#createByteArrayOutputStream()} */
    public static ByteArrayOutputStream createByteArrayOutputStream() {
        return StreamOps.createByteArrayOutputStream();
    }

    // ==================== 流写入文件 ====================

    /** 将输入流写入文件，同 {@link StreamWriteOps#writeToFile(InputStream, File)} */
    public static void writeToFile(InputStream input, File file) {
        StreamWriteOps.writeToFile(input, file);
    }

    /** 将输入流写入文件，同 {@link StreamWriteOps#writeToFile(InputStream, Path)} */
    public static void writeToFile(InputStream input, Path path) {
        StreamWriteOps.writeToFile(input, path);
    }

    /** 将字节数组写入文件，同 {@link StreamWriteOps#writeToFile(byte[], File)} */
    public static void writeToFile(byte[] data, File file) {
        StreamWriteOps.writeToFile(data, file);
    }

    /** 将字节数组写入文件，同 {@link StreamWriteOps#writeToFile(byte[], Path)} */
    public static void writeToFile(byte[] data, Path path) {
        StreamWriteOps.writeToFile(data, path);
    }

    /** 将字符串写入文件，同 {@link StreamWriteOps#writeToFile(String, File)} */
    public static void writeToFile(String content, File file) {
        StreamWriteOps.writeToFile(content, file);
    }

    /** 将字符串写入文件，同 {@link StreamWriteOps#writeToFile(String, File, Charset)} */
    public static void writeToFile(String content, File file, Charset charset) {
        StreamWriteOps.writeToFile(content, file, charset);
    }

    /** 将字符串写入文件，同 {@link StreamWriteOps#writeToFile(String, Path)} */
    public static void writeToFile(String content, Path path) {
        StreamWriteOps.writeToFile(content, path);
    }

    /** 将字符串写入文件，同 {@link StreamWriteOps#writeToFile(String, Path, Charset)} */
    public static void writeToFile(String content, Path path, Charset charset) {
        StreamWriteOps.writeToFile(content, path, charset);
    }

    /** 将字符串列表写入文件（按行），同 {@link StreamWriteOps#writeToFile(List, File)} */
    public static void writeToFile(List<String> lines, File file) {
        StreamWriteOps.writeToFile(lines, file);
    }

    /** 将字符串列表写入文件（按行），同 {@link StreamWriteOps#writeToFile(List, File, Charset)} */
    public static void writeToFile(List<String> lines, File file, Charset charset) {
        StreamWriteOps.writeToFile(lines, file, charset);
    }

    /** 将字符串列表写入文件（按行），同 {@link StreamWriteOps#writeToFile(List, Path)} */
    public static void writeToFile(List<String> lines, Path path) {
        StreamWriteOps.writeToFile(lines, path);
    }

    /** 将字符串列表写入文件（按行），同 {@link StreamWriteOps#writeToFile(List, Path, Charset)} */
    public static void writeToFile(List<String> lines, Path path, Charset charset) {
        StreamWriteOps.writeToFile(lines, path, charset);
    }

    // ==================== 流转换 ====================

    /** 输入流转换为字节数组，同 {@link StreamOps#toByteArray(InputStream)} */
    public static byte[] toByteArray(InputStream inputStream) {
        return StreamOps.toByteArray(inputStream);
    }

    /** 输入流转换为字符串，同 {@link StreamOps#toString(InputStream)} */
    public static String toString(InputStream inputStream) {
        return StreamOps.toString(inputStream);
    }

    /** 输入流转换为字符串，同 {@link StreamOps#toString(InputStream, Charset)} */
    public static String toString(InputStream inputStream, Charset charset) {
        return StreamOps.toString(inputStream, charset);
    }

    /** InputStream 转换为 Reader，同 {@link StreamOps#toReader(InputStream, Charset)} */
    public static Reader toReader(InputStream inputStream, Charset charset) {
        return StreamOps.toReader(inputStream, charset);
    }

    /** OutputStream 转换为 Writer，同 {@link StreamOps#toWriter(OutputStream)} */
    public static Writer toWriter(OutputStream outputStream) {
        return StreamOps.toWriter(outputStream);
    }

    /** Reader 转换为字符串，同 {@link StreamOps#toString(Reader)} */
    public static String toString(Reader reader) {
        return StreamOps.toString(reader);
    }

    /** Reader 转换为字节数组，同 {@link StreamOps#toByteArray(Reader, Charset)} */
    public static byte[] toByteArray(Reader reader, Charset charset) {
        return StreamOps.toByteArray(reader, charset);
    }

    // ==================== 缓冲包装 ====================

    /** 包装为 BufferedReader，同 {@link StreamOps#toBufferedReader(InputStream, Charset)} */
    public static BufferedReader toBufferedReader(InputStream inputStream, Charset charset) {
        return StreamOps.toBufferedReader(inputStream, charset);
    }

    /** 包装为 BufferedOutputStream，同 {@link StreamOps#toBufferedOutputStream(OutputStream)} */
    public static BufferedOutputStream toBufferedOutputStream(OutputStream outputStream) {
        return StreamOps.toBufferedOutputStream(outputStream);
    }

    /** 包装为 BufferedInputStream，同 {@link StreamOps#toBufferedInputStream(InputStream)} */
    public static BufferedInputStream toBufferedInputStream(InputStream inputStream) {
        return StreamOps.toBufferedInputStream(inputStream);
    }

    // ==================== 流拷贝 ====================

    /** 流拷贝（自动关闭流），同 {@link StreamTransferOps#copy(OutputStream, InputStream)} */
    public static void copy(OutputStream output, InputStream input) {
        StreamTransferOps.copy(output, input);
    }

    /** 流拷贝，同 {@link StreamTransferOps#copy(OutputStream, InputStream, boolean)} */
    public static void copy(OutputStream output, InputStream input, boolean closeStream) {
        StreamTransferOps.copy(output, input, closeStream);
    }

    /** 流拷贝（带回调），同 {@link StreamTransferOps#copy(OutputStream, InputStream, Consumer)} */
    public static void copy(OutputStream output, InputStream input, Consumer<Long> readSizeCallBack) {
        StreamTransferOps.copy(output, input, readSizeCallBack);
    }

    /** 流拷贝（完整参数），同 {@link StreamTransferOps#copy(OutputStream, InputStream, int, boolean, Consumer)} */
    public static void copy(OutputStream output, InputStream input, int bufferSize,
                            boolean closeStream, Consumer<Long> readSizeCallBack) {
        StreamTransferOps.copy(output, input, bufferSize, closeStream, readSizeCallBack);
    }

    // ==================== 管道连接 ====================

    /** 同步管道连接：InputStream → OutputStream，同 {@link StreamTransferOps#pipe(OutputStream, InputStream)} */
    public static void pipe(OutputStream output, InputStream input) {
        StreamTransferOps.pipe(output, input);
    }

    /** 同步管道连接（指定缓冲区），同 {@link StreamTransferOps#pipe(OutputStream, InputStream, int)} */
    public static void pipe(OutputStream output, InputStream input, int bufferSize) {
        StreamTransferOps.pipe(output, input, bufferSize);
    }

    /** 同步管道连接：Reader → Writer，同 {@link StreamTransferOps#pipe(Writer, Reader)} */
    public static void pipe(Writer writer, Reader reader) {
        StreamTransferOps.pipe(writer, reader);
    }

    /** 同步管道连接（指定缓冲区），同 {@link StreamTransferOps#pipe(Writer, Reader, int)} */
    public static void pipe(Writer writer, Reader reader, int bufferSize) {
        StreamTransferOps.pipe(writer, reader, bufferSize);
    }

    /** 合并多个输入流到单一输出流，同 {@link StreamTransferOps#joinStreams(OutputStream, InputStream[])} */
    public static void joinStreams(OutputStream output, InputStream... inputs) {
        StreamTransferOps.joinStreams(output, inputs);
    }

    // ==================== 流读取（按行） ====================

    /** 按行读取流，同 {@link StreamOps#readLines(InputStream, Consumer)} */
    public static void readLines(InputStream input, Consumer<String> lineConsumer) {
        StreamOps.readLines(input, lineConsumer);
    }

    /** 按行读取流，同 {@link StreamOps#readLines(InputStream, Charset, Consumer)} */
    public static void readLines(InputStream input, Charset charset, Consumer<String> lineConsumer) {
        StreamOps.readLines(input, charset, lineConsumer);
    }

    /** 按行读取流并返回列表，同 {@link StreamOps#readLinesToList(InputStream)} */
    public static List<String> readLinesToList(InputStream input) {
        return StreamOps.readLinesToList(input);
    }

    /** 按行读取流并返回列表，同 {@link StreamOps#readLinesToList(InputStream, Charset)} */
    public static List<String> readLinesToList(InputStream input, Charset charset) {
        return StreamOps.readLinesToList(input, charset);
    }

    /** 字符串列表合并为输入流（按行），同 {@link StreamOps#toInputStream(List)} */
    public static InputStream toInputStream(List<String> lines) {
        return StreamOps.toInputStream(lines);
    }

    /** 字符串列表合并为输入流（按行），同 {@link StreamOps#toInputStream(List, Charset)} */
    public static InputStream toInputStream(List<String> lines, Charset charset) {
        return StreamOps.toInputStream(lines, charset);
    }

    // ==================== 资源关闭 ====================

    /** 安全关闭流（安静模式），同 {@link StreamOps#closeQuietly(Closeable)} */
    public static void closeQuietly(Closeable closeable) {
        StreamOps.closeQuietly(closeable);
    }

    /** 安全关闭多个流，同 {@link StreamOps#closeQuietly(Closeable[])} */
    public static void closeQuietly(Closeable... closeables) {
        StreamOps.closeQuietly(closeables);
    }

    // ==================== 取消令牌 ====================

    /**
     * 取消令牌，用于异步任务的取消和中断控制
     */
    public static class CancellationToken {
        private volatile boolean cancelled = false;
        private volatile long timeoutMillis = 0;
        private volatile long timeoutDurationMillis = 0;

        public CancellationToken() {}

        public CancellationToken(long timeoutMillis) {
            // 保存原始超时时长（reset 时用于重建截止时间点，保留超时配置）
            this.timeoutDurationMillis = timeoutMillis;
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
         * 恢复 cancelled=false；若构造时指定了超时时长，则按原始时长重建截止时间点（超时配置保留）
         */
        public void reset() {
            this.cancelled = false;
            this.timeoutMillis = timeoutDurationMillis > 0
                ? System.currentTimeMillis() + timeoutDurationMillis : 0;
        }
    }

    // ==================== 异步流处理 ====================

    /**
     * 异步流拷贝（带进度回调和背压控制）
     * 同 {@link StreamAsyncOps#copyAsync(OutputStream, InputStream, Object, Function, int, CancellationToken)}
     */
    public static CompletableFuture<Long> copyAsync(OutputStream output, InputStream input,
                                                    Object executor,
                                                    Function<Long, Boolean> progressCallback,
                                                    int backPressureLimit,
                                                    CancellationToken token) {
        return StreamAsyncOps.copyAsync(output, input, executor, progressCallback, backPressureLimit, token);
    }

    /**
     * 异步流拷贝（使用默认背压限制）
     * 同 {@link StreamAsyncOps#copyAsync(OutputStream, InputStream, Object, Function, CancellationToken)}
     */
    public static CompletableFuture<Long> copyAsync(OutputStream output, InputStream input,
                                                    Object executor,
                                                    Function<Long, Boolean> progressCallback,
                                                    CancellationToken token) {
        return StreamAsyncOps.copyAsync(output, input, executor, progressCallback, token);
    }

    /**
     * 异步按行读取流
     * 同 {@link StreamAsyncOps#readLinesAsync(InputStream, Charset, Object, Function, Consumer, CancellationToken)}
     */
    public static void readLinesAsync(InputStream input, Charset charset,
                                      Object executor,
                                      Function<String, Boolean> lineConsumer,
                                      Consumer<Throwable> errorHandler,
                                      CancellationToken token) {
        StreamAsyncOps.readLinesAsync(input, charset, executor, lineConsumer, errorHandler, token);
    }

    /**
     * 异步按行读取流（无背压控制）
     * 同 {@link StreamAsyncOps#readLinesAsync(InputStream, Charset, Object, Consumer, CancellationToken)}
     */
    public static void readLinesAsync(InputStream input, Charset charset,
                                      Object executor,
                                      Consumer<String> lineConsumer,
                                      CancellationToken token) {
        StreamAsyncOps.readLinesAsync(input, charset, executor, lineConsumer, token);
    }

    /** 背压感知的流式读取（批处理模式），同 {@link StreamAsyncOps#readStreamWithBackpressure} */
    public static void readStreamWithBackpressure(InputStream input, Charset charset,
                                                  int batchSize,
                                                  Function<List<String>, Boolean> batchConsumer,
                                                  Object executor,
                                                  int backPressureLimit,
                                                  CancellationToken token) {
        StreamAsyncOps.readStreamWithBackpressure(input, charset, batchSize, batchConsumer, executor,
            backPressureLimit, token);
    }

    /** 背压感知的流式读取（使用默认背压限制和批次大小），同 {@link StreamAsyncOps#readStreamWithBackpressure} */
    public static void readStreamWithBackpressure(InputStream input, Charset charset,
                                                  Function<List<String>, Boolean> batchConsumer,
                                                  Object executor,
                                                  CancellationToken token) {
        StreamAsyncOps.readStreamWithBackpressure(input, charset, batchConsumer, executor, token);
    }

    /**
     * 流式传输（支持流水线处理）
     * 同 {@link StreamAsyncOps#transmitStream(Supplier, OutputStream, Object, Consumer, CancellationToken)}
     */
    public static <T> CompletableFuture<Long> transmitStream(Supplier<T> sourceSupplier,
                                                              OutputStream output,
                                                              Object executor,
                                                              Consumer<T> flushCallback,
                                                              CancellationToken token) {
        return StreamAsyncOps.transmitStream(sourceSupplier, output, executor, flushCallback, token);
    }

    /** 线程池工具方法：将Thread/Runnable转换为ExecutorService，同 {@link StreamAsyncOps#toExecutorService(Object)} */
    public static ExecutorService toExecutorService(Object executor) {
        return StreamAsyncOps.toExecutorService(executor);
    }

    /** 异步方法完成后关闭自管池，同 {@link StreamAsyncOps#shutdownIfSelfManaged(ExecutorService)} */
    public static void shutdownIfSelfManaged(ExecutorService es) {
        StreamAsyncOps.shutdownIfSelfManaged(es);
    }
}
