package com.tingfeng.util.java.base.common.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    // ==================== 流创建（输出） ====================

    /**
     * 创建字节数组输出流
     * @return ByteArrayOutputStream
     */
    public static ByteArrayOutputStream createByteArrayOutputStream() {
        return new ByteArrayOutputStream();
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
            throw new com.tingfeng.util.java.base.common.exception.io.IOException(e);
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
            throw new com.tingfeng.util.java.base.common.exception.io.IOException(e);
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
            throw new com.tingfeng.util.java.base.common.exception.io.IOException(e);
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
            throw new com.tingfeng.util.java.base.common.exception.io.IOException(e);
        }
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
            Semaphore semaphore = new Semaphore(backPressureLimit / BUFFER_SIZE);
            byte[] buffer = new byte[BUFFER_SIZE];
            long total = 0;
            int len;

            try {
                while ((len = input.read(buffer)) != -1) {
                    // 检查取消令牌
                    if (token != null && token.shouldInterrupt()) {
                        break;
                    }

                    // 背压控制
                    semaphore.acquire();

                    output.write(buffer, 0, len);
                    total += len;
                    semaphore.release();

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
                throw new com.tingfeng.util.java.base.common.exception.io.IOException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new com.tingfeng.util.java.base.common.exception.io.IOException("Copy operation interrupted", e);
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
                throw new com.tingfeng.util.java.base.common.exception.io.IOException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new com.tingfeng.util.java.base.common.exception.io.IOException("Read operation interrupted", e);
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
                throw new com.tingfeng.util.java.base.common.exception.io.IOException(e);
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
