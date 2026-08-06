package com.tingfeng.util.java.base.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * 带缓存的 range 读取输入流
 *
 * 语义：
 * - 半开区间 [start, end)：start 为输出起点（相对 source 起点），end 为输出终点；
 *   end <= 0 表示读到 source 末尾（EOF）
 * - 块索引：blockIndex = position / blockSize（整除）
 * - 块内消费偏移：blockOffset = position - blockIndex * blockSize
 * - 缓存命中校验：精确期望块长（end>0 时 min(blockSize, end-blockIndex*blockSize)；
 *   end<=0 时 blockSize），长度不符视为 miss 重读（陈旧部分块永不过校验）
 * - EOF 截断的不满块不写缓存（避免陈旧部分块与 miss→write 死循环）；
 *   end>0 时末块不满块（读满期望块长）正常写入并可命中
 * - 缓存写失败降级：catch 后继续从 source 读，读路径不受影响
 *
 * 状态模型（两态不变量）：
 * - 已加载态（currentData != null）：恒有 position <= sourcePos，且
 *   sourcePos - position = currentData.length - blockOffset（source 超前消费，超前量=块内未读部分）
 * - 未同步态（currentData == null 且 position > sourcePos，仅 skip 跨块后）：
 *   输出位置领先 source，由下次 loadBlock 步骤 1 尽力 skip 补齐（EOF 容错）
 *
 * loadBlock 五步：1 同步 source 至块起点（尽力 skip + EOF 容错，
 * skip()==0 时 read-discard 丢弃间隙字节）→ 2 读缓存 → 3 精确期望块长命中校验 →
 * 4 miss 从 source 读块（块起点即 EOF 时空读分支直接返回 EOF，不产生空数组；
 * 命中则物理推进至块末尾）→ 5 更新状态
 *
 * 构造定位使用 StreamOps.skipFully（fully 语义，source 短于定位目标抛项目 IOException）。
 * 线程安全：单实例非线程安全；多实例共享 FileRangeCache 安全（原子写）。
 *
 * @author huitoukest
 */
public class RangeCachingInputStream extends InputStream {

    private final InputStream source;
    private final long start;
    private final long end;
    private final CacheReader cacheReader;
    private final CacheWriter cacheWriter;
    private final long blockSize;

    /** 对外输出位置（绝对位置，相对 source 起点）；构造后初始值 = start */
    private long position;
    /** source 已消耗位置（物理消费进度） */
    private long sourcePos;
    /** 当前块数据；null = 未加载 */
    private byte[] currentData;
    /** 当前块索引 */
    private long currentBlockIndex;
    /** 块内消费偏移 = position - currentBlockIndex * blockSize */
    private int blockOffset;
    private boolean closed;

    /**
     * 创建缓存 range 读取流（默认块大小 1MB、无 cacheKey）
     * @param source 来源输入流
     * @param start 输出起点（相对 source 起点），不允许为负
     * @param end 输出终点（不含，半开区间）；end <= 0 表示读到 source 末尾
     * @param reader 缓存读取器
     * @param writer 缓存写入器
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 构造定位失败（source 短于定位目标）
     */
    public RangeCachingInputStream(InputStream source, long start, long end,
            CacheReader reader, CacheWriter writer) {
        this(source, start, end, reader, writer, FileRangeCache.DEFAULT_BLOCK_SIZE, null);
    }

    /**
     * 创建缓存 range 读取流（无 cacheKey）
     * @param source 来源输入流
     * @param start 输出起点（相对 source 起点），不允许为负
     * @param end 输出终点（不含，半开区间）；end <= 0 表示读到 source 末尾
     * @param reader 缓存读取器
     * @param writer 缓存写入器
     * @param blockSize 块大小（字节），必须为正数
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 构造定位失败（source 短于定位目标）
     */
    public RangeCachingInputStream(InputStream source, long start, long end,
            CacheReader reader, CacheWriter writer, long blockSize) {
        this(source, start, end, reader, writer, blockSize, null);
    }

    /**
     * 创建缓存 range 读取流
     * cacheKey 非空时透传给 FileRangeCache 绑定源身份（子目录隔离）；
     * 非 FileRangeCache 实现由调用方自行保证隔离
     * @param source 来源输入流
     * @param start 输出起点（相对 source 起点），不允许为负
     * @param end 输出终点（不含，半开区间）；end <= 0 表示读到 source 末尾
     * @param reader 缓存读取器
     * @param writer 缓存写入器
     * @param blockSize 块大小（字节），必须为正数
     * @param cacheKey 源身份标识，可为 null
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 构造定位失败（source 短于定位目标）
     */
    public RangeCachingInputStream(InputStream source, long start, long end,
            CacheReader reader, CacheWriter writer, long blockSize, String cacheKey) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        if (reader == null) {
            throw new IllegalArgumentException("reader must not be null");
        }
        if (writer == null) {
            throw new IllegalArgumentException("writer must not be null");
        }
        if (blockSize <= 0) {
            throw new IllegalArgumentException("blockSize must be positive");
        }
        if (blockSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("blockSize too large, must be <= " + Integer.MAX_VALUE);
        }
        if (start < 0) {
            throw new IllegalArgumentException("start must not be negative");
        }
        if (end > 0 && end <= start) {
            throw new IllegalArgumentException("end must be greater than start");
        }
        this.source = source;
        this.start = start;
        this.end = end;
        this.cacheReader = reader;
        this.cacheWriter = writer;
        this.blockSize = blockSize;
        if (cacheKey != null && !cacheKey.isEmpty()) {
            if (reader instanceof FileRangeCache) {
                ((FileRangeCache) reader).bindCacheKey(cacheKey);
            }
            if (writer != reader && writer instanceof FileRangeCache) {
                ((FileRangeCache) writer).bindCacheKey(cacheKey);
            }
        }
        // 构造定位：skipFully 至首个块起点（依赖 skipFully 的 read-discard 回退，skip()==0 不死循环）
        long firstBlockIndex = start / blockSize;
        StreamOps.skipFully(source, firstBlockIndex * blockSize);
        this.position = start;
        this.sourcePos = firstBlockIndex * blockSize;
    }

    /**
     * 读取单个字节
     * @return 读取的字节（0-255）；达到 end 或 source EOF 返回 -1
     * @throws IOException 流已关闭或读取失败
     */
    @Override
    public int read() throws IOException {
        ensureOpen();
        if (end > 0 && position >= end) {
            return -1;
        }
        if (currentData == null || blockOffset >= currentData.length) {
            if (!loadBlock(position / blockSize)) {
                return -1;
            }
        }
        int b = currentData[blockOffset] & 0xFF;
        blockOffset++;
        position++;
        return b;
    }

    /**
     * 批量读取字节
     * 返回数据不超过 end 截断范围；EOF 空读分支由 loadBlock 兜底（返回 -1，不产生空数组）
     * @param b 目标缓冲区
     * @param off 起始偏移
     * @param len 期望读取字节数
     * @return 实际读取字节数；达到 end 或 source EOF 返回 -1
     * @throws IOException 流已关闭或读取失败
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
        if (end > 0 && position >= end) {
            return -1;
        }
        if (currentData == null || blockOffset >= currentData.length) {
            if (!loadBlock(position / blockSize)) {
                return -1;
            }
        }
        int toCopy = Math.min(len, currentData.length - blockOffset);
        if (end > 0) {
            long remaining = end - position;
            if (remaining < toCopy) {
                toCopy = (int) remaining;
            }
        }
        System.arraycopy(currentData, blockOffset, b, off, toCopy);
        blockOffset += toCopy;
        position += toCopy;
        return toCopy;
    }

    /**
     * 跳过指定字节数
     * end>0 时跳过数截断至 end-position；
     * 当前块内可跳则纯内存推进（不触碰 source）；跨块/未加载则丢弃缓存进入未同步态，
     * 由下次 loadBlock 步骤 1 尽力 skip 补齐（EOF 容错）
     * @param n 期望跳过的字节数
     * @return 实际跳过的字节数（end<=0 且 source 提前 EOF 时后续 read 返回 -1 兜底）
     * @throws IOException 流已关闭
     */
    @Override
    public long skip(long n) throws IOException {
        ensureOpen();
        if (n <= 0) {
            return 0;
        }
        if (end > 0) {
            n = Math.min(n, end - position);
        }
        if (n <= 0) {
            return 0;
        }
        if (currentData != null && blockOffset + n <= currentData.length) {
            blockOffset += (int) n;
            position += n;
            return n;
        }
        currentData = null;
        position += n;
        return n;
    }

    /**
     * 返回当前块内可读字节数
     * 块未加载或已耗尽返回 0
     * @return 可读字节数
     * @throws IOException 流已关闭
     */
    @Override
    public int available() throws IOException {
        ensureOpen();
        if (currentData == null || blockOffset >= currentData.length) {
            return 0;
        }
        return currentData.length - blockOffset;
    }

    /**
     * 关闭流并关闭 source
     * 重复 close 无副作用
     * @throws IOException 关闭失败
     */
    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        currentData = null;
        source.close();
    }

    /**
     * 加载指定索引块（loadBlock 五步）
     * @param blockIndex 块索引
     * @return true 加载成功；false 遇到 EOF（read() 应返回 -1）
     * @throws IOException source 读取失败
     */
    private boolean loadBlock(long blockIndex) throws IOException {
        // 步骤 1：同步 source 至块起点（尽力 skip + EOF 容错；skip()==0 时 read-discard 丢弃间隙字节）
        long need = blockIndex * blockSize - sourcePos;
        while (need > 0) {
            long skipped = source.skip(need);
            if (skipped > 0) {
                need -= skipped;
                sourcePos += skipped;
                continue;
            }
            int b = source.read();
            if (b == -1) {
                return false;
            }
            need--;
            sourcePos++;
        }
        // 步骤 2：尝试从缓存读取（读异常降级为 miss）
        byte[] data = null;
        try {
            data = cacheReader.read(blockIndex);
        } catch (com.tingfeng.util.java.base.lang.exception.IOException e) {
            data = null;
        }
        // 步骤 3：命中校验（精确期望块长；end<=0 仅满块命中，陈旧部分块永不过校验）
        long expectedLen = end > 0 ? Math.min(blockSize, end - blockIndex * blockSize) : blockSize;
        if (data != null && data.length != expectedLen) {
            data = null;
        }
        if (data != null) {
            // 命中：source 物理推进至块末尾（保持物理消费与逻辑位置一致，满足已加载态不变量）
            long toSkip = data.length;
            while (toSkip > 0) {
                long skipped = source.skip(toSkip);
                if (skipped > 0) {
                    toSkip -= skipped;
                    sourcePos += skipped;
                    continue;
                }
                int b = source.read();
                if (b == -1) {
                    break;
                }
                toSkip--;
                sourcePos++;
            }
        } else {
            // 步骤 4：miss → 从 source 读取 [块起点, min(块末尾, EOF)) 范围
            byte[] buf = new byte[(int) expectedLen];
            int first = source.read(buf);
            if (first == -1) {
                return false;
            }
            int total = first;
            while (total < buf.length) {
                int n = source.read(buf, total, buf.length - total);
                if (n == -1) {
                    break;
                }
                total += n;
            }
            sourcePos += total;
            data = total == buf.length ? buf : Arrays.copyOf(buf, total);
            // 仅当读满期望块长才写缓存（EOF 截断的不满块不写，消除陈旧部分块与 miss→write 死循环）
            if (total == expectedLen) {
                try {
                    cacheWriter.write(blockIndex, data);
                } catch (com.tingfeng.util.java.base.lang.exception.IOException e) {
                    // 写失败降级：缓存未生效但读路径不受影响
                }
            }
        }
        // 步骤 5：更新状态
        currentData = data;
        currentBlockIndex = blockIndex;
        blockOffset = (int) (position - blockIndex * blockSize);
        return true;
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
