package com.tingfeng.util.java.base.io;

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * 流创建、转换、缓冲包装与按行读取工具
 *
 * - 流创建：String/byte[]/CharSequence/File/Path/URL/URI/List 转换为 InputStream
 * - 流转换：InputStream 转 byte[]/String/Reader，OutputStream 转 Writer，Reader 转 String/byte[]
 * - 缓冲包装：BufferedReader/BufferedOutputStream/BufferedInputStream
 * - 按行读取：readLines/readLinesToList
 * - 资源关闭：closeQuietly
 *
 * 本类为 IOUtils 拆分产物，IOUtils 门面委托本类方法，公开行为与拆分前完全一致。
 *
 * @author huitoukest
 */
public final class StreamOps {

    /**
     * 默认缓冲区大小：4KB
     */
    static final int BUFFER_SIZE = 4096;

    private StreamOps() {
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
        // 判空语义与 CharSequence 版本统一：null 返回空流
        return toInputStream((CharSequence) content, charset);
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
            throw new com.tingfeng.util.java.base.lang.exception.IOException(
                "Failed to open file: " + file.getPath(), e);
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

    // ==================== 流转换 ====================

    /**
     * 输入流转换为字节数组
     * 注意：不关闭传入的 inputStream，由调用方负责管理
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
     * 注意：不关闭传入的 inputStream，由调用方负责管理
     * @param inputStream 输入流
     * @return 字符串
     */
    public static String toString(InputStream inputStream) {
        return toString(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * 输入流转换为字符串
     * 注意：不关闭传入的 inputStream，由调用方负责管理
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

    /**
     * Reader 转换为字节数组（默认 UTF-8 编码）
     * @param reader Reader
     * @return 字节数组
     */
    public static byte[] toByteArray(Reader reader) {
        return toByteArray(reader, StandardCharsets.UTF_8);
    }

    /**
     * URL 资源读取为字节数组
     * 读取完成后自动关闭连接；null → IllegalArgumentException
     * @param url URL 资源
     * @return 字节数组
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果读取失败
     */
    public static byte[] toByteArray(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("url must not be null");
        }
        try (InputStream input = url.openStream()) {
            return toByteArray(input);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to read from URL: " + url, e);
        }
    }

    /**
     * URI 资源读取为字节数组
     * 读取完成后自动关闭连接；null → IllegalArgumentException
     * @param uri URI 资源
     * @return 字节数组
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果读取失败
     */
    public static byte[] toByteArray(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null");
        }
        try (InputStream input = uri.toURL().openStream()) {
            return toByteArray(input);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to read from URI: " + uri, e);
        }
    }

    /**
     * 文件读取为字节数组
     * 读取完成后自动关闭文件；null → IllegalArgumentException
     * @param file 文件
     * @return 字节数组
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果读取失败
     */
    public static byte[] toByteArray(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        try (InputStream input = new FileInputStream(file)) {
            return toByteArray(input);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(
                "Failed to read file: " + file.getPath(), e);
        }
    }

    /**
     * 路径读取为字节数组
     * 读取完成后自动关闭文件；null → IllegalArgumentException
     * @param path 文件路径
     * @return 字节数组
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果读取失败
     */
    public static byte[] toByteArray(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        try (InputStream input = Files.newInputStream(path)) {
            return toByteArray(input);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException("Failed to read path: " + path, e);
        }
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

    // ==================== 便捷判断与跳过 ====================

    /**
     * 判断输入流是否为空（无任何字节可读）
     * null → true；探测 1 字节不消费（peek 语义）：
     * - 支持 mark/reset 的流（如 ByteArrayInputStream/BufferedInputStream）无损探测；
     * - 不可标记流经 PushbackInputStream 探测并回退，但字节回退在包装缓冲内，
     *   调用方继续使用原始流读取会丢失该字节，建议先 BufferedInputStream 包装。
     * @param input 输入流
     * @return true 表示空流或 null
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果读取失败
     */
    public static boolean isEmpty(InputStream input) {
        if (input == null) {
            return true;
        }
        try {
            if (input.markSupported()) {
                input.mark(1);
                int b = input.read();
                input.reset();
                return b == -1;
            }
            PushbackInputStream pushback = new PushbackInputStream(input, 1);
            int b = pushback.read();
            if (b == -1) {
                return true;
            }
            pushback.unread(b);
            return false;
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
    }

    /**
     * 比较两个输入流内容是否完全一致
     * 任一为 null → false；逐块比较直至任一 EOF，长度或字节不等 → false；
     * 注意：比较过程会消费两个流
     * @param input1 输入流 1
     * @param input2 输入流 2
     * @return true 表示内容完全一致
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果读取失败
     */
    public static boolean contentEquals(InputStream input1, InputStream input2) {
        if (input1 == null || input2 == null) {
            return false;
        }
        try {
            byte[] buffer1 = new byte[BUFFER_SIZE];
            byte[] buffer2 = new byte[BUFFER_SIZE];
            while (true) {
                int n1 = readUpTo(input1, buffer1);
                int n2 = readUpTo(input2, buffer2);
                if (n1 != n2) {
                    return false;
                }
                if (n1 < 0) {
                    return true;
                }
                for (int i = 0; i < n1; i++) {
                    if (buffer1[i] != buffer2[i]) {
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
    }

    /**
     * 完全跳过指定字节数（fully 语义）
     * 跳过 n 字节，不足抛项目 IOException；n <= 0 → 返回 0；
     * skip() 返回 0 时采用 read-discard 逐字节回退（read() 返回 -1 → 不足抛异常），
     * 杜绝 skip() == 0 死循环；跳过的字节属于跳过区间，直接丢弃不计入业务数据
     * @param input 输入流
     * @param n 需要跳过的字节数
     * @return 实际跳过的字节数（成功时为 n）
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果不足 n 字节或读取失败
     */
    public static long skipFully(InputStream input, long n) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        if (n <= 0) {
            return 0;
        }
        long remaining = n;
        try {
            while (remaining > 0) {
                long skipped = input.skip(remaining);
                if (skipped > 0) {
                    remaining -= skipped;
                    continue;
                }
                // skip() 返回 0：read-discard 逐字节推进，防止死循环
                int b = input.read();
                if (b == -1) {
                    throw new com.tingfeng.util.java.base.lang.exception.IOException(
                        "Unable to skip " + n + " bytes, EOF reached after skipping " + (n - remaining) + " bytes");
                }
                remaining--;
            }
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
        return n;
    }

    /**
     * 从输入流尽力读取缓冲并填满（EOF 提前返回）
     * @param input 输入流
     * @param buffer 缓冲区
     * @return 实际读取字节数；立即 EOF 返回 -1
     * @throws IOException 如果读取失败
     */
    private static int readUpTo(InputStream input, byte[] buffer) throws IOException {
        int total = 0;
        while (total < buffer.length) {
            int len = input.read(buffer, total, buffer.length - total);
            if (len == -1) {
                break;
            }
            total += len;
        }
        return total == 0 ? -1 : total;
    }

    // ==================== 多流拼接 ====================

    /**
     * 拼接多个输入流为一个逻辑输入流
     * 返回的拼接流关闭时自动关闭全部子流（含未消费的子流，见 ConcatenatedInputStream）；
     * 空数组 → 空流；数组中的 null 元素自动跳过；数组本身为 null → IllegalArgumentException；
     * 与 joinStreams（多个流写入单一输出流）语义不同：本方法返回可继续消费的拼接流
     * @param sources 输入流数组
     * @return 拼接流
     */
    public static ConcatenatedInputStream concatenate(InputStream... sources) {
        if (sources == null) {
            throw new IllegalArgumentException("sources must not be null");
        }
        return new ConcatenatedInputStream(Arrays.asList(sources).iterator());
    }

    /**
     * 拼接输入流列表为一个逻辑输入流
     * 空列表 → 空流；列表中的 null 元素自动跳过；列表本身为 null → IllegalArgumentException
     * @param sources 输入流列表
     * @return 拼接流
     */
    public static ConcatenatedInputStream concatenate(List<InputStream> sources) {
        if (sources == null) {
            throw new IllegalArgumentException("sources must not be null");
        }
        return new ConcatenatedInputStream(sources.iterator());
    }

    /**
     * 拼接枚举输入流为一个逻辑输入流
     * 空枚举 → 空流；枚举中的 null 元素自动跳过；枚举本身为 null → IllegalArgumentException
     * @param sources 输入流枚举
     * @return 拼接流
     */
    public static ConcatenatedInputStream concatenate(Enumeration<? extends InputStream> sources) {
        if (sources == null) {
            throw new IllegalArgumentException("sources must not be null");
        }
        return new ConcatenatedInputStream(new Iterator<InputStream>() {
            @Override
            public boolean hasNext() {
                return sources.hasMoreElements();
            }

            @Override
            public InputStream next() {
                return sources.nextElement();
            }
        });
    }

    // ==================== 流读取（按行） ====================

    /**
     * 按行读取流
     * 方法内部自动关闭传入的 input
     * @param input 输入流
     * @param lineConsumer 行处理回调
     */
    public static void readLines(InputStream input, Consumer<String> lineConsumer) {
        readLines(input, StandardCharsets.UTF_8, lineConsumer);
    }

    /**
     * 按行读取流
     * 方法内部自动关闭传入的 input
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
     * 按行读取流并追加到指定列表（追加模式）
     * 方法内部自动关闭传入的 input（与 readLines 关闭语义一致）；
     * lines 为 null 时内部新建（追加结果不返回，属防御语义）
     * @param input 输入流
     * @param charset 字符编码
     * @param lines 追加目标列表
     */
    public static void readLines(InputStream input, Charset charset, List<String> lines) {
        if (input == null) {
            return;
        }
        List<String> target = lines != null ? lines : new ArrayList<>();
        readLines(input, charset, target::add);
    }

    /**
     * 从输入流读取最多 n 个字节
     * 读满 n 字节或到达 EOF 时返回；null → 空数组；n <= 0 → 空数组；
     * 不关闭传入的 input，由调用方负责管理
     * @param input 输入流
     * @param n 期望读取字节数
     * @return 实际读取的字节数组（EOF 提前结束时少于 n）
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 如果读取失败
     */
    public static byte[] readNBytes(InputStream input, int n) {
        if (input == null || n <= 0) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(Math.min(n, 8192));
        byte[] buffer = new byte[Math.min(n, BUFFER_SIZE)];
        int remaining = n;
        try {
            while (remaining > 0) {
                int len = input.read(buffer, 0, Math.min(buffer.length, remaining));
                if (len == -1) {
                    break;
                }
                out.write(buffer, 0, len);
                remaining -= len;
            }
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
        return out.toByteArray();
    }

    /**
     * 惰性按行迭代器
     * hasNext() 时才读取下一行；close() 关闭底层流（经 BufferedReader 链传播至原始输入流）；
     * 中途放弃迭代时请显式 close() 释放资源，未消费的行不丢失（底层流未读部分仍在）
     * @param input 输入流
     * @param charset 字符编码
     * @return 行迭代器
     */
    public static LineIterator lineIterator(InputStream input, Charset charset) {
        return new LineIterator(input, charset);
    }

    /**
     * 按行读取流并返回列表
     * 方法内部自动关闭传入的 input（经 readLines 委托）
     * @param input 输入流
     * @return 行列表
     */
    public static List<String> readLinesToList(InputStream input) {
        return readLinesToList(input, StandardCharsets.UTF_8);
    }

    /**
     * 按行读取流并返回列表
     * 方法内部自动关闭传入的 input（经 readLines 委托）
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

    /**
     * 惰性行迭代器
     * - 惰性读取：hasNext() 时才从底层流读取下一行
     * - close() 关闭底层流（经 BufferedReader 链传播至原始输入流）
     * - 与 try-with-resources 兼容（实现 AutoCloseable）
     */
    public static class LineIterator implements Iterator<String>, AutoCloseable {

        private final BufferedReader reader;
        private String nextLine;
        private boolean exhausted;

        private LineIterator(InputStream input, Charset charset) {
            this.reader = new BufferedReader(new InputStreamReader(input, charset), BUFFER_SIZE);
        }

        /**
         * 是否有下一行
         * @return true 表示还有未消费的行
         */
        @Override
        public boolean hasNext() {
            if (nextLine != null) {
                return true;
            }
            if (exhausted) {
                return false;
            }
            try {
                nextLine = reader.readLine();
                if (nextLine == null) {
                    exhausted = true;
                    return false;
                }
                return true;
            } catch (IOException e) {
                throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
            }
        }

        /**
         * 返回下一行
         * @return 下一行内容
         * @throws java.util.NoSuchElementException 无更多行时
         */
        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more lines");
            }
            String line = nextLine;
            nextLine = null;
            return line;
        }

        /**
         * 关闭迭代器并释放底层流（安静模式，异常不抛出）
         */
        @Override
        public void close() {
            StreamOps.closeQuietly(reader);
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
}
