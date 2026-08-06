package com.tingfeng.util.java.base.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 限速输出流：按设定速率节流写入目标流
 *
 * - 限速为平均上限语义（非硬实时）：实际写入速率不超过设定值（依赖 Thread.sleep 精度）
 * - write 先申请额度再写入（节流点前置）
 * - flush 不节流；close() 幂等，先 flush 后关闭目标流
 * - 速率单位支持 KB/MB/GB 每秒（1024 进制）；平滑间隔默认 100ms，可配置 1ms-1s
 * - 单实例非线程安全；目标流写异常包装为项目 IOException
 *
 * @author huitoukest
 */
public class RateLimitedOutputStream extends OutputStream {

    private final OutputStream target;
    private final RateLimiter limiter;
    private boolean closed;

    /**
     * 创建限速输出流（平滑间隔默认 100ms）
     * @param target 目标流
     * @param rate 限速值（大于 0）
     * @param unit 速率单位
     * @throws IllegalArgumentException target/unit 为 null、rate 不大于 0 或换算溢出
     */
    public RateLimitedOutputStream(OutputStream target, long rate, RateUnit unit) {
        this(target, rate, unit, 100L);
    }

    /**
     * 创建限速输出流（自定义平滑间隔）
     * @param target 目标流
     * @param rate 限速值（大于 0）
     * @param unit 速率单位
     * @param intervalMillis 平滑间隔毫秒数（1-1000）
     * @throws IllegalArgumentException target/unit 为 null、rate 不大于 0、
     *         intervalMillis 超出 [1,1000] 或 rate×unit 换算溢出
     */
    public RateLimitedOutputStream(OutputStream target, long rate, RateUnit unit, long intervalMillis) {
        if (target == null) {
            throw new IllegalArgumentException("target must not be null");
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
        this.target = target;
        this.limiter = new RateLimiter(ratePerSecond, intervalMillis);
    }

    /**
     * 写入单个字节（节流后写入）
     * @param b 字节值
     * @throws IOException 流已关闭或目标流写入失败
     */
    @Override
    public void write(int b) throws IOException {
        ensureOpen();
        limiter.acquire(1);
        try {
            target.write(b);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
    }

    /**
     * 批量写入字节（先申请 len 额度再写入，节流点前置）
     * @param b 数据缓冲区
     * @param off 起始偏移
     * @param len 写入字节数
     * @throws IOException 流已关闭或目标流写入失败
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        ensureOpen();
        limiter.acquire(len);
        try {
            target.write(b, off, len);
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
    }

    /**
     * 刷新目标流（不节流）
     * @throws IOException 流已关闭或目标流刷新失败
     */
    @Override
    public void flush() throws IOException {
        ensureOpen();
        try {
            target.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
    }

    /**
     * 关闭限速流：先刷新目标流再关闭（FilterOutputStream 惯例）
     * 刷新失败时仍尝试关闭目标流（try-with-resources 语义）；重复 close 无副作用
     * @throws IOException 目标流刷新失败
     */
    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        try {
            target.flush();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        } finally {
            StreamOps.closeQuietly(target);
        }
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
