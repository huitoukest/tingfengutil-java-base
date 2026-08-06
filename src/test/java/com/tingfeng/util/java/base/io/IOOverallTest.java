package com.tingfeng.util.java.base.io;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * IO 总体测试类：自定义流相互嵌套 + JDK 装饰器包装自定义流的功能回归
 *
 * 覆盖场景（用例组 A：JDK 装饰器包装自定义流）：
 * - BufferedInputStream 包装 ConcatenatedInputStream / RangeCachingInputStream：
 *   字节一致性、EOF、mark/reset、skip 跨块
 * - PushbackInputStream 包装 ConcatenatedInputStream：unread 回退
 * - DataInputStream 包装 ConcatenatedInputStream：readInt/readUTF 跨子流边界
 * - StreamOps.toReader 包装 BufferedInputStream + RangeCachingInputStream：多字节字符跨块解码
 * - JDK 装饰器 close 传播到自定义流及其底层源
 *
 * 覆盖场景（用例组 B：自定义流互为底层）：
 * - CIS 含 RCIS 子流：拼接字节一致、跨子流边界单字节读切换
 * - RCIS.source=CIS：跨拼接边界 range 读取
 * - 深链 Buffered → Pushback → CIS → RCIS：字节一致、unread 回退、close 全传播
 * - 嵌套 skip 语义（RCIS 块内纯内存推进）/ 嵌套 available 保守断言
 *
 * 覆盖场景（用例组 C：工具方法消费嵌套流）：
 * - toByteArray/toString、copy、readLinesToList、skipFully、isEmpty、contentEquals、
 *   writeToFile、joinStreams、copyAsync 消费嵌套流
 *
 * 覆盖场景（用例组 D：BoundedInputStream 嵌套矩阵）：
 * - Bounded 包 CIS/RCIS：限长截断（跨子流边界）、limit=0 空流、skip 计入额度、available 下界
 * - CIS 含 Bounded 子流：定长段截断后自动切换下一子流
 * - RCIS over Bounded：构造 skipFully 经 Bounded 额度截断三态（正常 / 构造异常 / 提前 EOF）
 * - 深链 Buffered → Bounded → CIS：close 全传播 + 重复 close 幂等
 *
 * 覆盖场景（用例组 E：RateLimitedInputStream 嵌套矩阵）：
 * - RateLimited 包 CIS / Bounded 包 RateLimited / 深链：限速读嵌套 + 耗时断言（请求量≥实际量安全方向）
 * - CIS 含 RateLimited 子流：close 链传播（RateLimited 关闭其 source）
 * - RateLimited 与 RCIS 双向：高限速零等待字节一致（短读过度计费，不做耗时断言）
 * - Buffered 包 RateLimited：Buffered fill 批次粒度不敏感
 * - E3 时序冻结：预热耗满 burst → 中途 skip 节流 → 读剩余（总请求量=实际量）
 *
 * 覆盖场景（用例组 F：RateLimitedOutputStream 输出侧嵌套）：
 * - copy/copyCount 消费嵌套输入写限速输出（写粒度=实际读长 → 请求量=实际量）
 * - joinStreams 合并写限速输出 / BufferedOutputStream 包限速输出
 * - close 先 flush 后关 target 传播 + 重复 close 幂等（不重复 flush）
 *
 * 断言基准：以各方法 javadoc 声明语义为准，不套 JDK 默认语义
 * （如 RangeCachingInputStream.skip 返回实际跳过数、available 为当前块内可读数；
 * 限速耗时断言取 D17 理论值 ×0.8 宽松下限，防 Thread.sleep 精度 flaky）。
 */
public class IOOverallTest {

    /** 默认测试块大小 */
    private static final long BS = 64;

    /** 记录关闭状态的测试流 */
    private static class TrackingInputStream extends ByteArrayInputStream {
        boolean closed;

        TrackingInputStream(byte[] data) {
            super(data);
        }

        @Override
        public void close() throws IOException {
            this.closed = true;
            super.close();
        }
    }

    /** 记录关闭与刷新痕迹的输出流（验证 close 先 flush 后关与幂等） */
    private static class TrackingOutputStream extends ByteArrayOutputStream {
        boolean closed;
        int flushCount;

        @Override
        public void flush() throws IOException {
            this.flushCount++;
            super.flush();
        }

        @Override
        public void close() throws IOException {
            this.closed = true;
            super.close();
        }
    }

    /** 记录 read 调用次数的测试流（验证 skip 是否实际消费底层） */
    private static final class ReadTrackingInputStream extends ByteArrayInputStream {
        int readCalls;

        ReadTrackingInputStream(byte[] data) {
            super(data);
        }

        @Override
        public int read() {
            readCalls++;
            return super.read();
        }

        @Override
        public int read(byte[] b, int off, int len) {
            readCalls++;
            return super.read(b, off, len);
        }
    }

    /** 内存缓存：避免文件系统依赖 */
    private static final class MemoryCache implements CacheReader, CacheWriter {
        final Map<Long, byte[]> blocks = new HashMap<>();

        @Override
        public byte[] read(long blockIndex) {
            byte[] data = blocks.get(blockIndex);
            return data == null ? null : data.clone();
        }

        @Override
        public void write(long blockIndex, byte[] data) {
            blocks.put(blockIndex, data.clone());
        }
    }

    /** 字符串转 UTF-8 字节数组 */
    private static byte[] bytes(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }

    /** 构造递增值字节数组：data[i] = startValue + i */
    private static byte[] bytes(int length, int startValue) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) (startValue + i);
        }
        return data;
    }

    /** 拼接多个字节数组 */
    private static byte[] concat(byte[]... arrays) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] array : arrays) {
            out.write(array, 0, array.length);
        }
        return out.toByteArray();
    }

    /** 截取字节数组 [from, length) */
    private static byte[] slice(byte[] data, long from) {
        byte[] result = new byte[data.length - (int) from];
        System.arraycopy(data, (int) from, result, 0, result.length);
        return result;
    }

    /** 截取字节数组 [from, to) */
    private static byte[] slice(byte[] data, long from, long to) {
        byte[] result = new byte[(int) (to - from)];
        System.arraycopy(data, (int) from, result, 0, result.length);
        return result;
    }

    // ==================== 用例组 A：JDK 装饰器包装自定义流 ====================

    /**
     * BufferedInputStream 包装 ConcatenatedInputStream：字节一致、EOF、空源嵌套
     * mark/reset 由 BufferedInputStream 自身缓冲实现，跨自定义流边界可用
     */
    @Test
    public void testBufferedWrapsConcatenated() throws IOException {
        byte[] a = bytes("abc");
        byte[] b = bytes("defg");
        byte[] c = bytes("hi");
        byte[] expected = concat(a, b, c);
        ConcatenatedInputStream cis = StreamOps.concatenate(
            new ByteArrayInputStream(a), new ByteArrayInputStream(b), new ByteArrayInputStream(c));
        try (BufferedInputStream buffered = new BufferedInputStream(cis)) {
            Assert.assertArrayEquals(expected, StreamOps.toByteArray(buffered));
            Assert.assertEquals(-1, buffered.read());
        }
        // mark/reset 可用：回退后重读字节一致
        try (BufferedInputStream buffered = new BufferedInputStream(
            StreamOps.concatenate(new ByteArrayInputStream(bytes("ab")), new ByteArrayInputStream(bytes("cdefg"))))) {
            Assert.assertEquals('a', buffered.read());
            buffered.mark(16);
            Assert.assertEquals('b', buffered.read());
            Assert.assertEquals('c', buffered.read());
            buffered.reset();
            Assert.assertEquals('b', buffered.read());
            Assert.assertEquals('c', buffered.read());
            Assert.assertEquals('d', buffered.read());
        }
        // 空源嵌套：CIS 空 → Buffered 读 EOF
        try (BufferedInputStream buffered = new BufferedInputStream(StreamOps.concatenate())) {
            Assert.assertEquals(-1, buffered.read());
            Assert.assertEquals(0, StreamOps.toByteArray(buffered).length);
        }
    }

    /**
     * BufferedInputStream 包装 RangeCachingInputStream：字节一致（2.5 块非对齐）、EOF
     */
    @Test
    public void testBufferedWrapsRangeCaching() throws IOException {
        byte[] src = bytes(160, 0);
        MemoryCache cache = new MemoryCache();
        try (BufferedInputStream buffered = new BufferedInputStream(
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, cache, BS))) {
            Assert.assertArrayEquals(src, StreamOps.toByteArray(buffered));
            Assert.assertEquals(-1, buffered.read());
        }
    }

    /**
     * PushbackInputStream 包装 ConcatenatedInputStream：unread 回退后读取一致
     */
    @Test
    public void testPushbackWrapsConcatenated() throws IOException {
        byte[] a = bytes("abc");
        byte[] b = bytes("defg");
        try (PushbackInputStream pushback = new PushbackInputStream(
            StreamOps.concatenate(new ByteArrayInputStream(a), new ByteArrayInputStream(b)))) {
            Assert.assertEquals('a', pushback.read());
            pushback.unread('a');
            Assert.assertEquals('a', pushback.read());
            Assert.assertArrayEquals(bytes("bcdefg"), StreamOps.toByteArray(pushback));
            Assert.assertEquals(-1, pushback.read());
        }
    }

    /**
     * DataInputStream 包装 ConcatenatedInputStream：readInt/readUTF 跨子流边界
     * 构造字节：int + UTF 分段写入后再拼接，UTF 字节再拆两段制造跨流读取
     */
    @Test
    public void testDataInputWrapsConcatenated() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(bos)) {
            dos.writeInt(42);
            dos.writeUTF("嵌套测试");
        }
        byte[] raw = bos.toByteArray();
        byte[] intBytes = new byte[4];
        System.arraycopy(raw, 0, intBytes, 0, 4);
        byte[] utfBytes = new byte[raw.length - 4];
        System.arraycopy(raw, 4, utfBytes, 0, utfBytes.length);
        int mid = utfBytes.length / 2;
        byte[] utf1 = new byte[mid];
        byte[] utf2 = new byte[utfBytes.length - mid];
        System.arraycopy(utfBytes, 0, utf1, 0, mid);
        System.arraycopy(utfBytes, mid, utf2, 0, utf2.length);
        try (DataInputStream dis = new DataInputStream(StreamOps.concatenate(
            new ByteArrayInputStream(intBytes), new ByteArrayInputStream(utf1), new ByteArrayInputStream(utf2)))) {
            Assert.assertEquals(42, dis.readInt());
            Assert.assertEquals("嵌套测试", dis.readUTF());
            Assert.assertEquals(-1, dis.read());
        }
    }

    /**
     * BufferedInputStream 内层 RCIS skip 跨块：skip 后读剩余与源对应位置一致
     */
    @Test
    public void testBufferedWrapsRangeCachingSkip() throws IOException {
        byte[] src = bytes(640, 0);
        MemoryCache cache = new MemoryCache();
        try (BufferedInputStream buffered = new BufferedInputStream(
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, cache, BS))) {
            Assert.assertEquals(160, buffered.skip(160));
            Assert.assertArrayEquals(slice(src, 160), StreamOps.toByteArray(buffered));
        }
    }

    /**
     * JDK 装饰器 close 传播：Buffered 关闭 → CIS 全部子流 / RCIS 底层 source 均 closed
     */
    @Test
    public void testJdkWrapperClosePropagatesToCustomStream() throws IOException {
        TrackingInputStream t1 = new TrackingInputStream(bytes("abc"));
        TrackingInputStream t2 = new TrackingInputStream(bytes("def"));
        BufferedInputStream bufferedCis = new BufferedInputStream(StreamOps.concatenate(t1, t2));
        bufferedCis.close();
        Assert.assertTrue(t1.closed);
        Assert.assertTrue(t2.closed);

        TrackingInputStream tracking = new TrackingInputStream(bytes(64, 0));
        MemoryCache cache = new MemoryCache();
        BufferedInputStream bufferedRcis = new BufferedInputStream(
            new RangeCachingInputStream(tracking, 0, -1, cache, cache, BS));
        bufferedRcis.close();
        Assert.assertTrue(tracking.closed);
    }

    /**
     * StreamOps.toReader 包装 BufferedInputStream + RangeCachingInputStream：
     * 多字节字符跨块边界解码一致（内容 150 字节跨 2 个 64 字节块）
     */
    @Test
    public void testReaderWrapsNested() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append("测");
        }
        String content = sb.toString();
        byte[] src = content.getBytes(StandardCharsets.UTF_8);
        Assert.assertTrue(src.length > BS * 2);
        MemoryCache cache = new MemoryCache();
        try (Reader reader = StreamOps.toReader(new BufferedInputStream(
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, cache, BS)),
            StandardCharsets.UTF_8)) {
            Assert.assertEquals(content, StreamOps.toString(reader));
        }
    }

    // ==================== 用例组 B：自定义流互为底层 ====================

    /**
     * B1 CIS 含 RCIS 子流：拼接字节一致，跨子流边界单字节读切换
     * 两个 RCIS 子流各自独立缓存（共享缓存会因块索引相同而串扰）
     */
    @Test
    public void testConcatenatedContainsRangeCaching() throws IOException {
        MemoryCache cacheA = new MemoryCache();
        MemoryCache cacheB = new MemoryCache();
        RangeCachingInputStream rcisA = new RangeCachingInputStream(
            new ByteArrayInputStream(bytes(160, 0)), 0, 64, cacheA, cacheA, BS);
        RangeCachingInputStream rcisB = new RangeCachingInputStream(
            new ByteArrayInputStream(bytes(160, 100)), 0, 64, cacheB, cacheB, BS);
        byte[] expected = concat(bytes(64, 0), bytes(64, 100));
        try (ConcatenatedInputStream stream = StreamOps.concatenate(rcisA, rcisB)) {
            Assert.assertArrayEquals(expected, StreamOps.toByteArray(stream));
            Assert.assertEquals(-1, stream.read());
        }
        // 跨子流边界单字节读切换：前 64 字节来自 rcisA，随后切换 rcisB
        MemoryCache cacheA2 = new MemoryCache();
        MemoryCache cacheB2 = new MemoryCache();
        RangeCachingInputStream ra = new RangeCachingInputStream(
            new ByteArrayInputStream(bytes(64, 0)), 0, -1, cacheA2, cacheA2, BS);
        RangeCachingInputStream rb = new RangeCachingInputStream(
            new ByteArrayInputStream(bytes(64, 30)), 0, -1, cacheB2, cacheB2, BS);
        try (ConcatenatedInputStream stream = StreamOps.concatenate(ra, rb)) {
            for (int i = 0; i < 64; i++) {
                Assert.assertEquals(i, stream.read());
            }
            for (int i = 0; i < 64; i++) {
                Assert.assertEquals(30 + i, stream.read());
            }
            Assert.assertEquals(-1, stream.read());
        }
    }

    /**
     * B2 RCIS.source=CIS：跨拼接边界 range 读取 [32,96) 双向闭包
     * 正向构造 64+64 拼接流 → 逆向 RCIS 读 [32,96) 与 slice 期望比对；
     * end=96 触发精确期望块长（块 1 不满块 32 字节）写缓存并可命中
     */
    @Test
    public void testRangeCachingOverConcatenated() throws IOException {
        byte[] s1 = bytes(64, 0);
        byte[] s2 = bytes(64, 64);
        byte[] full = concat(s1, s2);
        byte[] expected = slice(full, 32, 96);
        MemoryCache cache = new MemoryCache();
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            StreamOps.concatenate(new ByteArrayInputStream(s1), new ByteArrayInputStream(s2)),
            32, 96, cache, cache, BS)) {
            Assert.assertArrayEquals(expected, StreamOps.toByteArray(stream));
            Assert.assertEquals(-1, stream.read());
            Assert.assertEquals(-1, stream.read());
        }
        // end>0 末块不满块（32 字节）写入缓存
        Assert.assertEquals(32, cache.blocks.get(1L).length);
    }

    /**
     * B3 三层深链 Buffered→Pushback→CIS→RCIS：字节一致、unread 回退、EOF
     * unread 需在 Pushback 层操作，回退验证用 Pushback 外层变体（Buffered 内层）
     */
    @Test
    public void testDeepChain() throws IOException {
        byte[] expected = concat(bytes(64, 0), bytes(64, 100));
        // plan 原链形态：Buffered 外层 → 字节一致 + EOF
        MemoryCache cacheA = new MemoryCache();
        MemoryCache cacheB = new MemoryCache();
        RangeCachingInputStream rcisA = new RangeCachingInputStream(
            new ByteArrayInputStream(bytes(160, 0)), 0, 64, cacheA, cacheA, BS);
        RangeCachingInputStream rcisB = new RangeCachingInputStream(
            new ByteArrayInputStream(bytes(160, 100)), 0, 64, cacheB, cacheB, BS);
        try (BufferedInputStream buffered = new BufferedInputStream(new PushbackInputStream(
            StreamOps.concatenate(rcisA, rcisB)))) {
            Assert.assertArrayEquals(expected, StreamOps.toByteArray(buffered));
            Assert.assertEquals(-1, buffered.read());
        }
        // Pushback 外层变体：unread 回退后读取一致
        MemoryCache cacheC = new MemoryCache();
        MemoryCache cacheD = new MemoryCache();
        RangeCachingInputStream rcisC = new RangeCachingInputStream(
            new ByteArrayInputStream(bytes(160, 0)), 0, 64, cacheC, cacheC, BS);
        RangeCachingInputStream rcisD = new RangeCachingInputStream(
            new ByteArrayInputStream(bytes(160, 100)), 0, 64, cacheD, cacheD, BS);
        try (PushbackInputStream pushback = new PushbackInputStream(new BufferedInputStream(
            StreamOps.concatenate(rcisC, rcisD)))) {
            Assert.assertEquals(0, pushback.read());
            pushback.unread(0);
            Assert.assertEquals(0, pushback.read());
            Assert.assertArrayEquals(slice(expected, 1), StreamOps.toByteArray(pushback));
            Assert.assertEquals(-1, pushback.read());
        }
    }

    /**
     * B4 嵌套 close 全传播：深链最外层 close → 最内层源全部 closed
     * 链：Buffered → Pushback → CIS → RCIS → TrackingInputStream
     */
    @Test
    public void testNestedClosePropagation() throws IOException {
        TrackingInputStream t1 = new TrackingInputStream(bytes(160, 0));
        TrackingInputStream t2 = new TrackingInputStream(bytes(160, 100));
        MemoryCache cacheA = new MemoryCache();
        MemoryCache cacheB = new MemoryCache();
        RangeCachingInputStream rcisA = new RangeCachingInputStream(t1, 0, 64, cacheA, cacheA, BS);
        RangeCachingInputStream rcisB = new RangeCachingInputStream(t2, 0, 64, cacheB, cacheB, BS);
        BufferedInputStream buffered = new BufferedInputStream(new PushbackInputStream(
            StreamOps.concatenate(rcisA, rcisB)));
        buffered.close();
        Assert.assertTrue(t1.closed);
        Assert.assertTrue(t2.closed);
    }

    /**
     * B5 嵌套 skip 语义：RCIS 块内 skip 纯内存推进，不触发底层 read
     * 缓存命中场景全程零 read；无缓存场景 skip 后首次读仅触发一次底层批量读
     */
    @Test
    public void testNestedSkipSemantics() throws IOException {
        byte[] src = bytes(160, 0);
        // 缓存命中：skip 与读均不触发底层 read（source 仅物理 skip 推进）
        ReadTrackingInputStream t1 = new ReadTrackingInputStream(slice(src, 0, 80));
        ReadTrackingInputStream t2 = new ReadTrackingInputStream(slice(src, 80, 160));
        MemoryCache cache = new MemoryCache();
        cache.write(0, slice(src, 0, 64));
        try (BufferedInputStream buffered = new BufferedInputStream(new RangeCachingInputStream(
            StreamOps.concatenate(t1, t2), 0, -1, cache, cache, BS))) {
            Assert.assertEquals(10, buffered.skip(10));
            Assert.assertEquals(0, t1.readCalls);
            Assert.assertEquals(0, t2.readCalls);
            Assert.assertEquals(10, buffered.read());
            Assert.assertEquals(0, t1.readCalls);
            Assert.assertEquals(0, t2.readCalls);
        }
        // 无缓存：skip 不触发 read，后续首次读触发一次底层批量读
        ReadTrackingInputStream t3 = new ReadTrackingInputStream(slice(src, 0, 80));
        ReadTrackingInputStream t4 = new ReadTrackingInputStream(slice(src, 80, 160));
        MemoryCache cache2 = new MemoryCache();
        try (BufferedInputStream buffered = new BufferedInputStream(new RangeCachingInputStream(
            StreamOps.concatenate(t3, t4), 0, -1, cache2, cache2, BS))) {
            Assert.assertEquals(10, buffered.skip(10));
            Assert.assertEquals(0, t3.readCalls);
            Assert.assertEquals(0, t4.readCalls);
            Assert.assertEquals(10, buffered.read());
            Assert.assertEquals(1, t3.readCalls);
            Assert.assertEquals(0, t4.readCalls);
        }
    }

    /**
     * B6 嵌套 available 保守断言：按各层 javadoc 语义叠加
     * CIS.available=当前子流可读、RCIS.available=当前块内可读、Buffered 含缓冲区内数据；
     * 只断言下界（缓冲区内确定可读量），不套 JDK 默认语义
     */
    @Test
    public void testNestedAvailable() throws IOException {
        MemoryCache cacheA = new MemoryCache();
        MemoryCache cacheB = new MemoryCache();
        RangeCachingInputStream rcisA = new RangeCachingInputStream(
            new ByteArrayInputStream(bytes(160, 0)), 0, 64, cacheA, cacheA, BS);
        RangeCachingInputStream rcisB = new RangeCachingInputStream(
            new ByteArrayInputStream(bytes(160, 100)), 0, 64, cacheB, cacheB, BS);
        try (BufferedInputStream buffered = new BufferedInputStream(new PushbackInputStream(
            StreamOps.concatenate(rcisA, rcisB)))) {
            // 未读：各层未激活（CIS 惰性取流 + RCIS 块未加载），保守断言下界
            Assert.assertTrue(buffered.available() >= 0);
            buffered.read();
            // 读 1 字节后 Buffered 缓冲区内至少 63 字节可读
            Assert.assertTrue(buffered.available() >= 63);
            buffered.read();
            Assert.assertTrue(buffered.available() >= 62);
        }
    }

    // ==================== 用例组 C：工具方法消费嵌套流 ====================

    /**
     * C1 工具方法消费嵌套流：toByteArray / toString
     */
    @Test
    public void testToByteArrayOnNested() throws IOException {
        byte[] src = bytes(160, 0);
        MemoryCache cache = new MemoryCache();
        try (BufferedInputStream buffered = new BufferedInputStream(
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, cache, BS))) {
            Assert.assertArrayEquals(src, StreamOps.toByteArray(buffered));
        }
        try (ConcatenatedInputStream cis = StreamOps.concatenate(
            new ByteArrayInputStream(bytes("abc")), new ByteArrayInputStream(bytes("defg")),
            new ByteArrayInputStream(bytes("hi")))) {
            Assert.assertEquals("abcdefghi", StreamOps.toString(cis));
        }
    }

    /**
     * C2 copy 消费嵌套流（copy 默认 closeStream=true，独立构造流）
     */
    @Test
    public void testCopyNestedToOutput() throws IOException {
        byte[] src = bytes(160, 0);
        MemoryCache cache = new MemoryCache();
        BufferedInputStream buffered = new BufferedInputStream(
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, cache, BS));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamTransferOps.copy(out, buffered);
        Assert.assertArrayEquals(src, out.toByteArray());
    }

    /**
     * C3 readLinesToList 消费嵌套流：按行读取跨子流边界（行尾无换行也计入）
     */
    @Test
    public void testReadLinesOnNestedConcatenated() throws IOException {
        // "line2" 拆到两个子流，验证按行读取跨子流边界
        ConcatenatedInputStream cis = StreamOps.concatenate(
            new ByteArrayInputStream(bytes("line1\nli")), new ByteArrayInputStream(bytes("ne2\nline3")));
        List<String> lines = StreamOps.readLinesToList(cis);
        Assert.assertEquals(3, lines.size());
        Assert.assertEquals("line1", lines.get(0));
        Assert.assertEquals("line2", lines.get(1));
        Assert.assertEquals("line3", lines.get(2));
    }

    /**
     * C4 skipFully 跨嵌套链：跳过后后续读取位置正确
     */
    @Test
    public void testSkipFullyOnNested() throws IOException {
        ConcatenatedInputStream cis = StreamOps.concatenate(
            new ByteArrayInputStream(bytes("abcdef")), new ByteArrayInputStream(bytes("ghijkl")));
        try (BufferedInputStream buffered = new BufferedInputStream(cis)) {
            StreamOps.skipFully(buffered, 3);
            Assert.assertEquals('d', buffered.read());
        }
        byte[] src = bytes(640, 0);
        MemoryCache cache = new MemoryCache();
        try (BufferedInputStream buffered = new BufferedInputStream(
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, cache, BS))) {
            StreamOps.skipFully(buffered, 160);
            Assert.assertArrayEquals(slice(src, 160), StreamOps.toByteArray(buffered));
        }
    }

    /**
     * C5 isEmpty 消费嵌套流（peek 语义按 javadoc 声明）
     * mark 支持的 Buffered 包装无损探测，首字节保留；
     * 裸 CIS/RCIS 不支持 mark，探测经 Pushback 回退、字节落在包装缓冲内，
     * 原始流继续读会丢失首字节 —— 只断言布尔值
     */
    @Test
    public void testIsEmptyOnNested() throws IOException {
        // 空嵌套流 → true
        try (BufferedInputStream empty = new BufferedInputStream(StreamOps.concatenate())) {
            Assert.assertTrue(StreamOps.isEmpty(empty));
            Assert.assertEquals(-1, empty.read());
        }
        // Buffered 包装（mark 支持）：无损探测，首字节保留
        try (BufferedInputStream buffered = new BufferedInputStream(
            StreamOps.concatenate(new ByteArrayInputStream(bytes("ab")), new ByteArrayInputStream(bytes("cdef"))))) {
            Assert.assertFalse(StreamOps.isEmpty(buffered));
            Assert.assertEquals('a', buffered.read());
        }
        // 裸 CIS（不支持 mark）：只断言布尔值
        ConcatenatedInputStream cis = StreamOps.concatenate(new ByteArrayInputStream(bytes("xyz")));
        Assert.assertFalse(StreamOps.isEmpty(cis));
        // 裸 RCIS（不支持 mark）：只断言布尔值
        MemoryCache cache = new MemoryCache();
        RangeCachingInputStream rcis = new RangeCachingInputStream(
            new ByteArrayInputStream(bytes(160, 0)), 0, -1, cache, cache, BS);
        Assert.assertFalse(StreamOps.isEmpty(rcis));
    }

    /**
     * C6 contentEquals 嵌套流比对（逆向基准：普通流 vs 嵌套流；比较消费两流）
     */
    @Test
    public void testContentEqualsNestedVsPlain() throws IOException {
        byte[] src = bytes(160, 0);
        MemoryCache cache = new MemoryCache();
        BufferedInputStream buffered = new BufferedInputStream(
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, cache, BS));
        Assert.assertTrue(StreamOps.contentEquals(buffered, new ByteArrayInputStream(src)));
        Assert.assertTrue(StreamOps.contentEquals(new ByteArrayInputStream(src),
            new BufferedInputStream(new RangeCachingInputStream(
                new ByteArrayInputStream(src), 0, -1, new MemoryCache(), new MemoryCache(), BS))));
        Assert.assertFalse(StreamOps.contentEquals(
            new ByteArrayInputStream(src), new ByteArrayInputStream(bytes(64, 0))));
    }

    /**
     * C7 writeToFile 消费嵌套流：临时文件字节与期望一致
     */
    @Test
    public void testWriteToFileFromNested() throws IOException {
        byte[] src = bytes(160, 0);
        MemoryCache cache = new MemoryCache();
        BufferedInputStream buffered = new BufferedInputStream(
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, cache, BS));
        File tempFile = File.createTempFile("io-overall-", ".tmp");
        tempFile.deleteOnExit();
        try {
            StreamWriteOps.writeToFile(buffered, tempFile);
            Assert.assertArrayEquals(src, Files.readAllBytes(tempFile.toPath()));
        } finally {
            Files.deleteIfExists(tempFile.toPath());
        }
    }

    /**
     * C8 joinStreams 嵌套流合并（joinStreams 关闭全部 input 与 output，独立构造流）
     */
    @Test
    public void testJoinStreamsWithNested() throws IOException {
        byte[] src = bytes(160, 0);
        MemoryCache cache = new MemoryCache();
        BufferedInputStream nested = new BufferedInputStream(
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, cache, BS));
        byte[] tail = bytes("tail");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamTransferOps.joinStreams(out, nested, new ByteArrayInputStream(tail));
        Assert.assertArrayEquals(concat(src, tail), out.toByteArray());
    }

    /**
     * C9 copyAsync 消费嵌套流：get(10, SECONDS) 限时防挂起
     */
    @Test
    public void testCopyAsyncNested() throws Exception {
        byte[] src = bytes(160, 0);
        MemoryCache cache = new MemoryCache();
        BufferedInputStream buffered = new BufferedInputStream(
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, cache, BS));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            IOUtils.CancellationToken token = new IOUtils.CancellationToken();
            Long result = StreamAsyncOps.copyAsync(out, buffered, executor, null, token)
                .get(10, TimeUnit.SECONDS);
            Assert.assertEquals(src.length, result.longValue());
            Assert.assertArrayEquals(src, out.toByteArray());
        } finally {
            executor.shutdown();
        }
    }

    // ==================== 用例组 D：BoundedInputStream 嵌套矩阵 ====================

    /**
     * D1 Bounded 包 CIS：限长截断拼接流（截断跨子流边界）、limit=0 空流、skip 计入额度、available 下界
     * 链：Bounded → CIS(s1, s2, s3)；s1="abc"(3) + s2="defg"(4) 使 limit=5 截断跨子流边界
     */
    @Test
    public void testBoundedWrapsConcatenated() throws IOException {
        byte[] s1 = bytes("abc");
        byte[] s2 = bytes("defg");
        byte[] s3 = bytes("hi");
        // limit 前字节一致（截断跨子流边界），超限 EOF
        BoundedInputStream bounded = new BoundedInputStream(
            StreamOps.concatenate(new ByteArrayInputStream(s1), new ByteArrayInputStream(s2),
                new ByteArrayInputStream(s3)), 5);
        Assert.assertArrayEquals(bytes("abcde"), StreamOps.toByteArray(bounded));
        Assert.assertEquals(-1, bounded.read());
        // limit=0 空流：read 立即 EOF
        BoundedInputStream empty = new BoundedInputStream(
            StreamOps.concatenate(new ByteArrayInputStream(s1)), 0);
        Assert.assertEquals(-1, empty.read());
        Assert.assertEquals(-1, empty.read(new byte[8], 0, 8));
        // skip 计入额度：skip 跨子流边界后读到正确位置；skip 满额度后 EOF
        BoundedInputStream skipped = new BoundedInputStream(
            StreamOps.concatenate(new ByteArrayInputStream(s1), new ByteArrayInputStream(s2),
                new ByteArrayInputStream(s3)), 9);
        Assert.assertEquals(5, skipped.skip(5));
        Assert.assertEquals('f', skipped.read());
        // read 已消耗 s2 1 字节，剩余可跳 s2 1 字节 + s3 2 字节 = 3（skip 尽力契约，可小于 n）
        Assert.assertEquals(3, skipped.skip(4));
        Assert.assertEquals(-1, skipped.read());
        // available 下界断言：未读（CIS 惰性未激活）为 0；读后 min(子流可读, 剩余额度) ≥ 1；额度耗尽为 0
        BoundedInputStream avail = new BoundedInputStream(
            StreamOps.concatenate(new ByteArrayInputStream(s1), new ByteArrayInputStream(s2)), 7);
        Assert.assertEquals(0, avail.available());
        Assert.assertEquals(7, avail.skip(7));
        Assert.assertEquals(0, avail.available());
        Assert.assertEquals(-1, avail.read());
        BoundedInputStream avail2 = new BoundedInputStream(
            StreamOps.concatenate(new ByteArrayInputStream(s1), new ByteArrayInputStream(s2)), 9);
        Assert.assertEquals('a', avail2.read());
        Assert.assertTrue(avail2.available() >= 1);
    }

    /**
     * D2 Bounded 包 RCIS：限长截断 range 读取
     * 链：Bounded → RCIS(src, 0, -1)；limit ≥ 源长 → 全量一致；limit < 源长 → 提前 EOF
     */
    @Test
    public void testBoundedWrapsRangeCaching() throws IOException {
        byte[] src = bytes(160, 0);
        MemoryCache cache = new MemoryCache();
        BoundedInputStream full = new BoundedInputStream(
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, cache, BS), 200);
        Assert.assertArrayEquals(src, StreamOps.toByteArray(full));
        Assert.assertEquals(-1, full.read());
        MemoryCache cache2 = new MemoryCache();
        BoundedInputStream truncated = new BoundedInputStream(
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache2, cache2, BS), 100);
        Assert.assertArrayEquals(slice(src, 0, 100), StreamOps.toByteArray(truncated));
        Assert.assertEquals(-1, truncated.read());
    }

    /**
     * D3 CIS 含 Bounded 子流：定长段截断后自动切换下一子流，拼接字节一致
     * 链：CIS(Bounded(s1, 5), s2, s3)
     */
    @Test
    public void testConcatenatedContainsBounded() throws IOException {
        byte[] s1 = bytes("abcdefghij");
        byte[] s2 = bytes("xyz");
        byte[] s3 = bytes("12345");
        byte[] expected = concat(bytes("abcde"), s2, s3);
        try (ConcatenatedInputStream stream = StreamOps.concatenate(
            new BoundedInputStream(new ByteArrayInputStream(s1), 5),
            new ByteArrayInputStream(s2), new ByteArrayInputStream(s3))) {
            Assert.assertArrayEquals(expected, StreamOps.toByteArray(stream));
            Assert.assertEquals(-1, stream.read());
        }
    }

    /**
     * D4 RCIS over Bounded：构造 skipFully 经 Bounded 额度截断的三态（start=64 = BS，确保 skipFully 真实执行）
     * 链：RCIS(Bounded(src, limit), 64, 128)
     * 变体 1 limit=128（≥end）：构造通过 → 输出 [64,128) 与 slice 一致 + EOF
     * 变体 2 limit=32（<start）：构造 skipFully(64) 被 Bounded 截断为 32，read-discard 遇 EOF → 项目 IOException
     * 变体 3 limit=96（start≤limit<end）：构造通过 → 读到 limit-start=32 字节后提前 EOF
     */
    @Test
    public void testRangeCachingOverBounded() throws IOException {
        byte[] src = bytes(160, 0);
        // 变体 1：limit ≥ end，正常输出 [64,128)
        MemoryCache cache1 = new MemoryCache();
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new BoundedInputStream(new ByteArrayInputStream(src), 128), 64, 128, cache1, cache1, BS)) {
            Assert.assertArrayEquals(slice(src, 64, 128), StreamOps.toByteArray(stream));
            Assert.assertEquals(-1, stream.read());
        }
        // 变体 2：limit < start，构造定位不足 → 项目 IOException
        MemoryCache cache2 = new MemoryCache();
        try {
            new RangeCachingInputStream(new BoundedInputStream(new ByteArrayInputStream(src), 32),
                64, 128, cache2, cache2, BS);
            Assert.fail("expected project IOException");
        } catch (com.tingfeng.util.java.base.lang.exception.IOException expected) {
        }
        // 变体 3：start ≤ limit < end，读到 limit-start 字节后提前 EOF
        MemoryCache cache3 = new MemoryCache();
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new BoundedInputStream(new ByteArrayInputStream(src), 96), 64, 128, cache3, cache3, BS)) {
            Assert.assertArrayEquals(slice(src, 64, 96), StreamOps.toByteArray(stream));
            Assert.assertEquals(-1, stream.read());
        }
    }

    /**
     * D5 深链 Bounded 中层：Buffered → Bounded → CIS
     * 字节一致（limit 无截断）+ 最外层 close 全传播（t1/t2 closed）+ 重复 close 幂等；
     * close 传播链构造后立即 close（CIS 惰性取流，close 遍历关闭全部子流）
     */
    @Test
    public void testBoundedDeepChainClose() throws IOException {
        byte[] s1 = bytes(64, 0);
        byte[] s2 = bytes(64, 100);
        byte[] expected = concat(s1, s2);
        // 字节一致链（普通源）
        BufferedInputStream buffered = new BufferedInputStream(new BoundedInputStream(
            StreamOps.concatenate(new ByteArrayInputStream(s1), new ByteArrayInputStream(s2)), 128));
        Assert.assertArrayEquals(expected, StreamOps.toByteArray(buffered));
        Assert.assertEquals(-1, buffered.read());
        buffered.close();
        // close 全传播链（TrackingInputStream 子流）
        TrackingInputStream t1 = new TrackingInputStream(s1);
        TrackingInputStream t2 = new TrackingInputStream(s2);
        BufferedInputStream closing = new BufferedInputStream(new BoundedInputStream(
            StreamOps.concatenate(t1, t2), 128));
        closing.close();
        Assert.assertTrue(t1.closed);
        Assert.assertTrue(t2.closed);
        // 重复 close 幂等
        closing.close();
        Assert.assertTrue(t1.closed);
        Assert.assertTrue(t2.closed);
    }

    // ==================== 用例组 E：RateLimitedInputStream 嵌套矩阵 ====================

    /**
     * E1 RateLimited 包 CIS：限速读拼接流，子流按 4096 对齐（20480=5×4096）
     * 链：RateLimited → CIS(s1, s2, s3)；CIS.read 为"n>0 直接返回"非读满，
     * 末次 EOF 读仍前置 acquire → 请求量≥实际量（安全方向），断言 ≥80ms 成立
     */
    @Test
    public void testRateLimitedWrapsConcatenated() throws IOException {
        byte[] s1 = bytes(4096, 0);
        byte[] s2 = bytes(8192, 100);
        byte[] s3 = bytes(8192, 200);
        byte[] expected = concat(s1, s2, s3);
        RateLimitedInputStream rl = new RateLimitedInputStream(
            StreamOps.concatenate(new ByteArrayInputStream(s1), new ByteArrayInputStream(s2),
                new ByteArrayInputStream(s3)), 100, RateUnit.KB);
        long start = System.currentTimeMillis();
        byte[] actual = StreamOps.toByteArray(rl);
        long elapsed = System.currentTimeMillis() - start;
        Assert.assertArrayEquals(expected, actual);
        Assert.assertEquals(-1, rl.read());
        Assert.assertTrue("expected >= 80ms but was " + elapsed + "ms", elapsed >= 80);
    }

    /**
     * E2 Bounded 包 RateLimited：限长截断限速流
     * 链：Bounded(limit=20480) → RateLimited(src 24576B, 100KB/s)；
     * limit 截断先于源 EOF（remaining=0 不触底层）；Bounded 精确截断 → 请求量=实际量
     */
    @Test
    public void testBoundedWrapsRateLimited() throws IOException {
        byte[] src = bytes(24576, 0);
        BoundedInputStream bounded = new BoundedInputStream(
            new RateLimitedInputStream(new ByteArrayInputStream(src), 100, RateUnit.KB), 20480);
        long start = System.currentTimeMillis();
        byte[] actual = StreamOps.toByteArray(bounded);
        long elapsed = System.currentTimeMillis() - start;
        Assert.assertArrayEquals(slice(src, 0, 20480), actual);
        Assert.assertEquals(-1, bounded.read());
        Assert.assertTrue("expected >= 80ms but was " + elapsed + "ms", elapsed >= 80);
    }

    /**
     * E3 RateLimited 包 Bounded：时序冻结三步（禁止先读满再 skip / 先 skip 后读两种必败顺序）
     * 1. 预热读 10240B：acquire 恰好耗满 burst，断言 <50ms 验证预热有效
     * 2. 中途 skip 2048B：burst 已耗尽 → tokens 为负 → wait≈20ms（Bounded 额度 remaining=10240 充足）
     * 3. 读剩余 8192B：总请求量 10240+2048+8192=20480=实际量，D17 成立 → 总断言 ≥80ms
     */
    @Test
    public void testRateLimitedWrapsBounded() throws IOException {
        byte[] src = bytes(20480, 0);
        RateLimitedInputStream rl = new RateLimitedInputStream(
            new BoundedInputStream(new ByteArrayInputStream(src), 20480), 100, RateUnit.KB);
        // 1. 预热读：单次 acquire(10240) → tokens=0 零等待
        byte[] warmup = new byte[10240];
        long warmStart = System.currentTimeMillis();
        StreamOps.readFully(rl, warmup);
        long warmElapsed = System.currentTimeMillis() - warmStart;
        Assert.assertTrue("warmup should consume burst without wait, but was " + warmElapsed + "ms",
            warmElapsed < 50);
        // 2. 中途 skip：先执行后节流，按实际跳过数 acquire(2048)
        // 总计时从 skip 前开始：skip wait≈20ms + 读剩余 wait≈80ms ≈ 100ms（读剩余单段理论 80ms，
        // 受 refill 连续比例精度影响实测可能 78-79ms，含 skip 段后断言 ≥80ms 余量充足）
        long totalStart = System.currentTimeMillis();
        long skipped = rl.skip(2048);
        long skipElapsed = System.currentTimeMillis() - totalStart;
        Assert.assertEquals(2048, skipped);
        Assert.assertTrue("skip expected >= 5ms but was " + skipElapsed + "ms", skipElapsed >= 5);
        // 3. 读剩余 8192B
        byte[] rest = new byte[8192];
        StreamOps.readFully(rl, rest);
        long totalElapsed = System.currentTimeMillis() - totalStart;
        byte[] expected = concat(slice(src, 0, 10240), slice(src, 12288));
        Assert.assertArrayEquals(expected, concat(warmup, rest));
        Assert.assertEquals(-1, rl.read());
        Assert.assertTrue("total expected >= 80ms but was " + totalElapsed + "ms", totalElapsed >= 80);
    }

    /**
     * E4 CIS 含 RateLimited 子流：拼接内限速段（1GB/s 高限速零等待）
     * 链：CIS(RateLimited(s1, 1GB), s2)；close 链传播（RateLimited 子流关闭其 source）
     */
    @Test
    public void testConcatenatedContainsRateLimited() throws IOException {
        byte[] s1 = bytes(64, 0);
        byte[] s2 = bytes(64, 100);
        byte[] expected = concat(s1, s2);
        // 字节一致链
        ConcatenatedInputStream stream = StreamOps.concatenate(
            new RateLimitedInputStream(new ByteArrayInputStream(s1), 1, RateUnit.GB),
            new ByteArrayInputStream(s2));
        Assert.assertArrayEquals(expected, StreamOps.toByteArray(stream));
        Assert.assertEquals(-1, stream.read());
        // close 传播链：构造后立即 close（CIS 遍历关闭全部子流 → RateLimited 关闭其 source）
        TrackingInputStream t1 = new TrackingInputStream(s1);
        TrackingInputStream t2 = new TrackingInputStream(s2);
        ConcatenatedInputStream closing = StreamOps.concatenate(
            new RateLimitedInputStream(t1, 1, RateUnit.GB), t2);
        closing.close();
        Assert.assertTrue(t1.closed);
        Assert.assertTrue(t2.closed);
        closing.close();
        Assert.assertTrue(t1.closed);
    }

    /**
     * E5 深链 Bounded → RateLimited → CIS：三流互嵌套一层包一层
     * 子流 4096 对齐（8192+12288=20480）；Bounded 精确截断 → 请求量=实际量 → 耗时 ≥80ms；
     * close 全传播（Tracking 子流链构造后立即 close）
     */
    @Test
    public void testBoundedRateLimitedConcatenatedDeepChain() throws IOException {
        byte[] s1 = bytes(8192, 0);
        byte[] s2 = bytes(12288, 100);
        byte[] expected = concat(s1, s2);
        // 字节一致 + 耗时链
        BoundedInputStream bounded = new BoundedInputStream(
            new RateLimitedInputStream(StreamOps.concatenate(new ByteArrayInputStream(s1),
                new ByteArrayInputStream(s2)), 100, RateUnit.KB), 20480);
        long start = System.currentTimeMillis();
        Assert.assertArrayEquals(expected, StreamOps.toByteArray(bounded));
        long elapsed = System.currentTimeMillis() - start;
        Assert.assertEquals(-1, bounded.read());
        Assert.assertTrue("expected >= 80ms but was " + elapsed + "ms", elapsed >= 80);
        // close 传播链
        TrackingInputStream t1 = new TrackingInputStream(s1);
        TrackingInputStream t2 = new TrackingInputStream(s2);
        BoundedInputStream closing = new BoundedInputStream(
            new RateLimitedInputStream(StreamOps.concatenate(t1, t2), 1, RateUnit.GB), 20480);
        closing.close();
        Assert.assertTrue(t1.closed);
        Assert.assertTrue(t2.closed);
        closing.close();
        Assert.assertTrue(t1.closed);
    }

    /**
     * S1 RateLimited 包 RCIS：高限速零等待字节一致
     * 链：RateLimited(1GB/s) → RCIS(src, 0, -1)；RCIS 短读 ≤块内剩余 → 请求量≫实际量
     * （过度计费为 javadoc 安全方向语义），禁用耗时断言
     */
    @Test
    public void testRateLimitedWrapsRangeCaching() throws IOException {
        byte[] src = bytes(160, 0);
        MemoryCache cache = new MemoryCache();
        RateLimitedInputStream rl = new RateLimitedInputStream(
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, cache, BS),
            1, RateUnit.GB);
        Assert.assertArrayEquals(src, StreamOps.toByteArray(rl));
        Assert.assertEquals(-1, rl.read());
    }

    /**
     * S2 RCIS over RateLimited：构造 skipFully(0) 直接返回，高限速零等待 range 读取
     * 链：RCIS(RateLimited(src, 1GB), 0, 64)
     */
    @Test
    public void testRangeCachingOverRateLimited() throws IOException {
        byte[] src = bytes(160, 0);
        MemoryCache cache = new MemoryCache();
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new RateLimitedInputStream(new ByteArrayInputStream(src), 1, RateUnit.GB),
            0, 64, cache, cache, BS)) {
            Assert.assertArrayEquals(slice(src, 0, 64), StreamOps.toByteArray(stream));
            Assert.assertEquals(-1, stream.read());
        }
    }

    /**
     * S3 Buffered 包 RateLimited：Buffered fill 读满 8192 → 总请求量≥总消费量
     * 链：Buffered(8192) → RateLimited(src 20480B, 100KB/s)；D17 对批次粒度不敏感 → 耗时 ≥80ms
     */
    @Test
    public void testBufferedWrapsRateLimited() throws IOException {
        byte[] src = bytes(20480, 0);
        BufferedInputStream buffered = new BufferedInputStream(
            new RateLimitedInputStream(new ByteArrayInputStream(src), 100, RateUnit.KB));
        long start = System.currentTimeMillis();
        Assert.assertArrayEquals(src, StreamOps.toByteArray(buffered));
        long elapsed = System.currentTimeMillis() - start;
        Assert.assertEquals(-1, buffered.read());
        Assert.assertTrue("expected >= 80ms but was " + elapsed + "ms", elapsed >= 80);
    }

    // ==================== 用例组 F：RateLimitedOutputStream 输出侧嵌套 ====================

    /**
     * F1 copy 消费嵌套输入写限速输出：copy 4096 写循环 → 写粒度=实际读长 → 请求量=实际量
     * 链：copy(RLOutputStream(100KB/s) → BAOS, Buffered → RCIS)；copy 默认 closeStream=true
     */
    @Test
    public void testCopyNestedInputToRateLimitedOutput() throws IOException {
        byte[] src = bytes(20480, 0);
        MemoryCache cache = new MemoryCache();
        BufferedInputStream buffered = new BufferedInputStream(
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, cache, BS));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RateLimitedOutputStream rl = new RateLimitedOutputStream(out, 100, RateUnit.KB);
        long start = System.currentTimeMillis();
        StreamTransferOps.copy(rl, buffered);
        long elapsed = System.currentTimeMillis() - start;
        Assert.assertArrayEquals(src, out.toByteArray());
        Assert.assertTrue("expected >= 80ms but was " + elapsed + "ms", elapsed >= 80);
    }

    /**
     * F2 close 先 flush 后关 target 传播：close → target.flush() + target.close()
     * 重复 close 幂等（closed 标志直接返回，不重复 flush）
     */
    @Test
    public void testRateLimitedOutputCloseFlushPropagates() throws IOException {
        TrackingOutputStream target = new TrackingOutputStream();
        RateLimitedOutputStream rl = new RateLimitedOutputStream(target, 1, RateUnit.GB);
        rl.write(new byte[]{1, 2, 3});
        rl.close();
        Assert.assertTrue(target.closed);
        Assert.assertEquals(1, target.flushCount);
        // 重复 close 幂等：不重复 flush、不重复关闭
        rl.close();
        Assert.assertEquals(1, target.flushCount);
        Assert.assertTrue(target.closed);
    }

    /**
     * F3 joinStreams 写限速输出：嵌套输入经 joinStreams 路径写入（1GB/s 高限速零等待）
     * joinStreams 关闭全部 input 与 output（output 由 finally 兜底 close）；
     * 注意：多 input 场景下 pipe 每次 copy(closeStream=true) 会提前关闭 output
     * （StreamTransferOps:305-318），RLOutputStream close 后禁止写入 → 本用例以单 input 验证 joinStreams 路径
     */
    @Test
    public void testJoinStreamsToRateLimitedOutput() throws IOException {
        byte[] src = bytes(160, 0);
        MemoryCache cache = new MemoryCache();
        BufferedInputStream nested = new BufferedInputStream(
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, cache, BS));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RateLimitedOutputStream rl = new RateLimitedOutputStream(out, 1, RateUnit.GB);
        StreamTransferOps.joinStreams(rl, nested);
        Assert.assertArrayEquals(src, out.toByteArray());
    }

    /**
     * F4 BufferedOutputStream 包限速输出：写后 flush 字节一致 + close 链 flush 传播
     * 链：Buffered(8192) → RLOutputStream(1GB/s) → BAOS / TrackingOutputStream
     * Buffered.close → RL.close → flush + close target
     */
    @Test
    public void testBufferedOutputWrapsRateLimited() throws IOException {
        byte[] src = bytes(20480, 0);
        // 字节一致链：大数组直写底层，flush 不节流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedOutputStream buffered = new BufferedOutputStream(
            new RateLimitedOutputStream(out, 1, RateUnit.GB));
        buffered.write(src);
        buffered.flush();
        Assert.assertArrayEquals(src, out.toByteArray());
        // close 链传播：Buffered.close（FilterOutputStream.close → 动态分发 flush）
        // → RL.flush + RL.close → target.flush 至少 1 次 + target.close
        TrackingOutputStream track = new TrackingOutputStream();
        BufferedOutputStream closing = new BufferedOutputStream(
            new RateLimitedOutputStream(track, 1, RateUnit.GB));
        closing.write(new byte[]{1, 2, 3});
        closing.close();
        Assert.assertTrue(track.closed);
        Assert.assertTrue(track.flushCount >= 1);
    }

    /**
     * F5 copyCount 消费嵌套输入写限速输出：返回字节数=源长度 + 输出一致（高限速零等待）
     * 链：copyCount(RLOutputStream(1GB/s) → BAOS, Buffered → RCIS)
     */
    @Test
    public void testCopyCountNestedToRateLimitedOutput() throws IOException {
        byte[] src = bytes(160, 0);
        MemoryCache cache = new MemoryCache();
        BufferedInputStream buffered = new BufferedInputStream(
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, cache, BS));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RateLimitedOutputStream rl = new RateLimitedOutputStream(out, 1, RateUnit.GB);
        long count = StreamTransferOps.copyCount(rl, buffered);
        Assert.assertEquals(src.length, count);
        Assert.assertArrayEquals(src, out.toByteArray());
    }
}
