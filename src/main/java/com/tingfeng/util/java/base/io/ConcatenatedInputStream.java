package com.tingfeng.util.java.base.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * 拼接输入流：将多个输入流按顺序拼接为单个逻辑输入流
 *
 * 与 JDK SequenceInputStream 的关键差异：
 * - close() 关闭全部子流（含未消费的子流）；SequenceInputStream 仅关闭自身，子流由调用方管理
 * - read(byte[], int, int) 内部自动切换子流，仅当全部子流耗尽才返回 -1，
 *   标准 while (read(buf) != -1) 消费模式可完整读取所有子流
 * - 子流中的 null 元素自动跳过；空源集合视为空流
 *
 * 子流惰性打开：仅在首次读取到该子流时才触发读取；
 * 子流读异常包装为项目 IOException 抛出。
 *
 * @author huitoukest
 */
public class ConcatenatedInputStream extends InputStream {

    private final Iterator<? extends InputStream> sources;
    private InputStream current;
    private boolean closed;

    /**
     * 创建拼接输入流
     * @param sources 子流迭代器，可为空集合（空流）；迭代器本身为 null → IllegalArgumentException
     */
    public ConcatenatedInputStream(Iterator<? extends InputStream> sources) {
        if (sources == null) {
            throw new IllegalArgumentException("sources must not be null");
        }
        this.sources = sources;
    }

    /**
     * 读取单个字节
     * 当前子流耗尽自动切换下一子流（null 元素跳过）；全部子流耗尽返回 -1
     * @return 读取的字节（0-255），全部子流耗尽返回 -1
     * @throws IOException 流已关闭或子流读取失败
     */
    @Override
    public int read() throws IOException {
        while (true) {
            ensureOpen();
            if (current == null) {
                current = nextSource();
                if (current == null) {
                    return -1;
                }
            }
            int b;
            try {
                b = current.read();
            } catch (IOException e) {
                throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
            }
            if (b != -1) {
                return b;
            }
            current = null;
        }
    }

    /**
     * 批量读取字节
     * 内部 do-while 子流切换：读到的 n > 0 直接返回；读到 -1/0 切换下一子流继续，
     * 仅当全部子流耗尽才返回 -1（SequenceInputStream 同款语义）
     * @param b 目标缓冲区
     * @param off 起始偏移
     * @param len 期望读取字节数
     * @return 实际读取字节数；全部子流耗尽返回 -1
     * @throws IOException 流已关闭或子流读取失败
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
        do {
            if (current == null) {
                current = nextSource();
                if (current == null) {
                    return -1;
                }
            }
            int n;
            try {
                n = current.read(b, off, len);
            } catch (IOException e) {
                throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
            }
            if (n > 0) {
                return n;
            }
            current = null;
        } while (true);
    }

    /**
     * 跳过指定字节数（尽力语义）
     * 优先代理当前子流，不足自动切换下一子流；skip() 返回 0 时 read 单字节尽力推进；
     * 返回实际跳过的总数（可小于 n，符合 InputStream.skip 契约）；
     * 全部子流耗尽即停止，不抛异常
     * @param n 期望跳过的字节数
     * @return 实际跳过的字节数
     * @throws IOException 流已关闭或子流读取失败
     */
    @Override
    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        ensureOpen();
        long total = 0;
        while (n > 0) {
            if (current == null) {
                current = nextSource();
                if (current == null) {
                    break;
                }
            }
            long skipped;
            try {
                skipped = current.skip(n);
            } catch (IOException e) {
                throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
            }
            if (skipped > 0) {
                total += skipped;
                n -= skipped;
                continue;
            }
            int b;
            try {
                b = current.read();
            } catch (IOException e) {
                throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
            }
            if (b == -1) {
                current = null;
                continue;
            }
            total += 1;
            n -= 1;
        }
        return total;
    }

    /**
     * 返回当前子流可读字节数
     * 当前子流已耗尽或全部子流耗尽返回 0
     * @return 可读字节数
     * @throws IOException 子流 available 失败
     */
    @Override
    public int available() throws IOException {
        if (closed || current == null) {
            return 0;
        }
        try {
            return current.available();
        } catch (IOException e) {
            throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
        }
    }

    /**
     * 关闭拼接流并关闭全部子流（含未消费的子流）
     * 子流关闭异常静默（closeQuietly 语义）；重复 close 无副作用
     * @throws IOException 不抛出（子流关闭异常静默）
     */
    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        StreamOps.closeQuietly(current);
        current = null;
        while (sources.hasNext()) {
            StreamOps.closeQuietly(sources.next());
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

    /**
     * 取下一个非 null 子流；全部子流耗尽返回 null
     * @return 下一个非 null 子流，耗尽返回 null
     */
    private InputStream nextSource() {
        while (sources.hasNext()) {
            InputStream next = sources.next();
            if (next != null) {
                return next;
            }
        }
        return null;
    }
}
