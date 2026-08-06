package com.tingfeng.util.java.base.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * 限速输入流：按设定速率节流读取源流
 *
 * - 限速为平均上限语义（非硬实时）：实际读取速率不超过设定值（依赖 Thread.sleep 精度）
 * - 节流点前置：read(byte[]) 先申请额度再读取，EOF 短读浪费额度为安全方向
 * - skip 先执行后按实际跳过数节流（严格限速，防绕过）
 * - available 不节流；close() 幂等并关闭源流
 * - 速率单位支持 KB/MB/GB 每秒（1024 进制）；平滑间隔默认 100ms，可配置 1ms-1s
 * - 单实例非线程安全；源流读异常包装为项目 IOException
 *
 * @author huitoukest
 */
public class RateLimitedInputStream extends InputStream {

    private final InputStream source;
    private final RateLimiter limiter;
    private boolean closed;

    /**
     * 创建限速输入流（平滑间隔默认 100ms）
     * @param source 源流
     * @param rate 限速值（大于 0）
     * @param unit 速率单位
     * @throws IllegalArgumentException source/unit 为 null、rate 不大于 0 或换算溢出
     */
    public RateLimitedInputStream(InputStream source, long rate, RateUnit unit) {
        this(source, rate, unit, 100L);
    }

    /**
     * 创建限速输入流（自定义平滑间隔）
     * @param source 源流
     * @param rate 限速值（大于 0）
     * @param unit 速率单位
     * @param intervalMillis 平滑间隔毫秒数（1-1000）
     * @throws IllegalArgumentException source/unit 为 null、rate 不大于 0、
     *         intervalMillis 超出 [1,1000] 或 rate×unit 换算溢出
     */
    public RateLimitedInputStream(InputStream source, long rate, RateUnit unit, long intervalMillis) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        if (unit == null) {
            throw new IllegalArgumentException("unit must not be null");
        }
        if (rate <= 0) {
            throw new IllegalArgumentException("rate must be positive");
        }
        if (intervalMillis < 1 || intervalMillis > 1000) {
            throw new IllegalArgumentException("intervalMillis must be between 1 and 1000");
        }
        long ratePerSecond;
        try {
            ratePerSecond = unit.toBytes(rate);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("rate out of range: " + rate + " " + unit, e);
        }
        this.source = source;
        this.limiter = new RateLimiter(ratePerSecond, intervalMillis);
    }

    /**
     * 读取单个字节（节流后读取）
     * @return 读取的字节（0-255），源流 EOF 返回 -1
     * @throws IOException 流已关闭或源流读取失败
     */
    @Override
    public int read() throws IOException {
        ensureOpen();
        limiter.acquire(1);
        try {
            return source.read();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
    }

    /**
     * 批量读取字节（先申请 len 额度再读取，节流点前置）
     * @param b 目标缓冲区
     * @param off 起始偏移
     * @param len 期望读取字节数
     * @return 实际读取字节数；源流 EOF 返回 -1
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
        limiter.acquire(len);
        try {
            return source.read(b, off, len);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
    }

    /**
     * 跳过指定字节数（先跳过再按实际跳过数节流，严格限速）
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
        long actual;
        try {
            actual = source.skip(n);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
        limiter.acquire(actual);
        return actual;
    }

    /**
     * 返回源流可读字节数（不节流）
     * @return 可读字节数；流已关闭返回 0
     * @throws IOException 源流 available 失败
     */
    @Override
    public int available() throws IOException {
        if (closed) {
            return 0;
        }
        try {
            return source.available();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
    }

    /**
     * 关闭限速流并关闭源流
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
