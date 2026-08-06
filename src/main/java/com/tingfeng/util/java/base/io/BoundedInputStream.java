package com.tingfeng.util.java.base.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * 限长输入流：包装源流并限制最多可读取的字节数
 *
 * - 读取超过 limit 字节后视为 EOF（read 返回 -1）；skip 跳过的字节计入已读额度
 * - available 返回源流剩余与剩余额度的较小值
 * - close() 传播关闭源流（幂等，重复 close 无副作用）
 * - 源流读异常包装为项目 IOException；单实例非线程安全
 * - limit 为 0 → 空流（read 立即返回 -1）
 *
 * 与 RangeCachingInputStream 职责不同：本类仅做字节数上限限制，不做区间缓存；
 * 与 JDK 装饰器可嵌套（如 BufferedInputStream 包装本类）。
 *
 * @author huitoukest
 */
public class BoundedInputStream extends InputStream {

    private final InputStream source;
    private final long limit;
    private long count;
    private boolean closed;

    /**
     * 创建限长输入流
     * @param source 源流
     * @param limit 字节数上限（非负）；0 → 空流
     * @throws IllegalArgumentException source 为 null 或 limit 为负数
     */
    public BoundedInputStream(InputStream source, long limit) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit must not be negative");
        }
        this.source = source;
        this.limit = limit;
    }

    /**
     * 读取单个字节
     * 已读额度达到 limit 或源流 EOF 返回 -1
     * @return 读取的字节（0-255），额度耗尽或源流 EOF 返回 -1
     * @throws IOException 流已关闭或源流读取失败
     */
    @Override
    public int read() throws IOException {
        ensureOpen();
        if (count >= limit) {
            return -1;
        }
        int b;
        try {
            b = source.read();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
        if (b == -1) {
            return -1;
        }
        count++;
        return b;
    }

    /**
     * 批量读取字节
     * 读取长度受剩余额度 limit - count 约束；额度耗尽或源流 EOF 返回 -1
     * @param b 目标缓冲区
     * @param off 起始偏移
     * @param len 期望读取字节数
     * @return 实际读取字节数；额度耗尽或源流 EOF 返回 -1
     * @throws IOException 流已关闭或源流读取失败
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        ensureOpen();
        long remaining = limit - count;
        if (remaining <= 0) {
            return -1;
        }
        int toRead = (int) Math.min(len, remaining);
        int n;
        try {
            n = source.read(b, off, toRead);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
        if (n > 0) {
            count += n;
        }
        return n;
    }

    /**
     * 跳过指定字节数（尽力语义）
     * 跳过长度受剩余额度 limit - count 约束，跳过的字节计入已读额度；
     * 返回实际跳过的字节数（可小于 n，符合 InputStream.skip 契约）
     * @param n 期望跳过的字节数
     * @return 实际跳过的字节数
     * @throws IOException 流已关闭或源流读取失败
     */
    @Override
    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        ensureOpen();
        long remaining = limit - count;
        if (remaining <= 0) {
            return 0;
        }
        long toSkip = Math.min(n, remaining);
        long skipped;
        try {
            skipped = source.skip(toSkip);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
        if (skipped > 0) {
            count += skipped;
        }
        return skipped;
    }

    /**
     * 返回剩余可读字节数
     * 取源流 available 与剩余额度 limit - count 的较小值（不小于 0）；流已关闭返回 0
     * @return 剩余可读字节数
     * @throws IOException 源流 available 失败
     */
    @Override
    public int available() throws IOException {
        if (closed) {
            return 0;
        }
        int avail;
        try {
            avail = source.available();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
        if (avail < 0) {
            avail = 0;
        }
        return (int) Math.min(avail, limit - count);
    }

    /**
     * 关闭限长流并关闭源流
     * 源流关闭异常静默（closeQuietly 语义）；重复 close 无副作用
     * @throws IOException 不抛出（源流关闭异常静默）
     */
    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        StreamOps.closeQuietly(source);
    }

    /**
     * 校验流未关闭
     * @throws IOException 流已关闭
     */
    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }
}
