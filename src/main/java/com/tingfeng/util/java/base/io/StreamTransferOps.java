package com.tingfeng.util.java.base.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.function.Consumer;

/**
 * 流传输工具：流拷贝、管道连接与多流合并
 *
 * - 流拷贝：copy 系列（OutputStream ← InputStream），支持自定义缓冲、关闭语义与进度回调
 * - 管道连接：pipe 系列（InputStream → OutputStream / Reader → Writer）
 * - 多流合并：joinStreams（多个输入流依次写入单一输出流）
 *
 * 本类为 IOUtils 拆分产物，IOUtils 门面委托本类方法，公开行为与拆分前完全一致。
 *
 * @author huitoukest
 */
public final class StreamTransferOps {

    private StreamTransferOps() {
    }

    // ==================== 流拷贝 ====================

    /**
     * 流拷贝（自动关闭流）
     * 默认关闭传入的 input 和 output（closeStream=true）
     * @param output 输出流
     * @param input 输入流
     */
    public static void copy(OutputStream output, InputStream input) {
        copy(output, input, StreamOps.BUFFER_SIZE, true, null);
    }

    /**
     * 流拷贝
     * closeStream=true 时关闭传入的 input 和 output；false 时由调用方负责关闭
     * @param output 输出流
     * @param input 输入流
     * @param closeStream 是否关闭流
     */
    public static void copy(OutputStream output, InputStream input, boolean closeStream) {
        copy(output, input, StreamOps.BUFFER_SIZE, closeStream, null);
    }

    /**
     * 流拷贝（带回调）
     * 默认关闭传入的 input 和 output（closeStream=true）
     * @param output 输出流
     * @param input 输入流
     * @param readSizeCallBack 读取进度回调，参数为已读取字节数
     */
    public static void copy(OutputStream output, InputStream input, Consumer<Long> readSizeCallBack) {
        copy(output, input, StreamOps.BUFFER_SIZE, true, readSizeCallBack);
    }

    /**
     * 流拷贝（完整参数）
     * closeStream=true 时关闭传入的 input 和 output；false 时由调用方负责关闭
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
                StreamOps.closeQuietly(input);
                StreamOps.closeQuietly(output);
            }
        }
    }

    // ==================== 跨类型拷贝 ====================

    /**
     * 跨类型拷贝：Reader → Writer
     * 参数顺序与存量 copy(OutputStream, InputStream) 一致（输出在前）；
     * 返回拷贝的字符数（与存量 copy 返回 void 的差异，调用方按需使用）；
     * 注意：不关闭传入的 writer 和 reader，由调用方负责管理
     * @param writer 输出
     * @param reader 输入
     * @return 拷贝的字符数
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果读写失败
     */
    public static long copy(Writer writer, Reader reader) {
        if (writer == null || reader == null) {
            throw new IllegalArgumentException("writer and reader must not be null");
        }
        char[] buffer = new char[StreamOps.BUFFER_SIZE];
        long total = 0;
        int len;
        try {
            while ((len = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, len);
                total += len;
            }
            writer.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
        return total;
    }

    /**
     * 跨类型拷贝：Reader → OutputStream（按 charset 编码）
     * 参数顺序与存量 copy(OutputStream, InputStream) 一致（输出在前）；
     * 返回拷贝的字符数（与存量 copy 返回 void 的差异，调用方按需使用）；
     * 注意：不关闭传入的 output 和 reader，由调用方负责管理
     * @param output 输出流
     * @param reader 输入
     * @param charset 字符编码
     * @return 拷贝的字符数
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果读写失败
     */
    public static long copy(OutputStream output, Reader reader, Charset charset) {
        if (output == null || reader == null) {
            throw new IllegalArgumentException("output and reader must not be null");
        }
        char[] buffer = new char[StreamOps.BUFFER_SIZE];
        long total = 0;
        int len;
        try {
            Writer writer = new OutputStreamWriter(output, charset);
            while ((len = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, len);
                total += len;
            }
            writer.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
        return total;
    }

    /**
     * 跨类型拷贝：InputStream → Writer（按 charset 解码）
     * 参数顺序与存量 copy(OutputStream, InputStream) 一致（输出在前）；
     * 返回拷贝的字符数（解码后的字符数量；与存量 copy 返回 void 的差异，调用方按需使用）；
     * 注意：不关闭传入的 writer 和 input，由调用方负责管理
     * @param writer 输出
     * @param input 输入流
     * @param charset 字符编码
     * @return 拷贝的字符数
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果读写失败
     */
    public static long copy(Writer writer, InputStream input, Charset charset) {
        if (writer == null || input == null) {
            throw new IllegalArgumentException("writer and input must not be null");
        }
        char[] buffer = new char[StreamOps.BUFFER_SIZE];
        long total = 0;
        int len;
        try {
            Reader reader = new InputStreamReader(input, charset);
            while ((len = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, len);
                total += len;
            }
            writer.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
        return total;
    }

    // ==================== 管道连接 ====================

    /**
     * 同步管道连接：InputStream → OutputStream
     * 自动关闭传入的 input 和 output（委托 copy 默认关闭语义）
     * @param output 输出流
     * @param input 输入流
     */
    public static void pipe(OutputStream output, InputStream input) {
        pipe(output, input, StreamOps.BUFFER_SIZE);
    }

    /**
     * 同步管道连接：InputStream → OutputStream（指定缓冲区）
     * 自动关闭传入的 input 和 output（委托 copy 默认关闭语义）
     * @param output 输出流
     * @param input 输入流
     * @param bufferSize 缓冲区大小
     */
    public static void pipe(OutputStream output, InputStream input, int bufferSize) {
        copy(output, input, bufferSize, true, null);
    }

    /**
     * 同步管道连接：Reader → Writer
     * 注意：本重载不关闭任何流，由调用方负责关闭 writer 和 reader
     * @param writer 输出
     * @param reader 输入
     */
    public static void pipe(Writer writer, Reader reader) {
        pipe(writer, reader, StreamOps.BUFFER_SIZE);
    }

    /**
     * 同步管道连接：Reader → Writer（指定缓冲区）
     * 注意：本重载不关闭任何流，由调用方负责关闭 writer 和 reader
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

    // ==================== 多流合并 ====================

    /**
     * 合并多个输入流到单一输出流
     * 关闭全部传入的 input 和 output（output 由 finally 兜底关闭，input 经 pipe 自动关闭）
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
            StreamOps.closeQuietly(output);
        }
    }
}
