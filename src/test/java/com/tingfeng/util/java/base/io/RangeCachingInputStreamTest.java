package com.tingfeng.util.java.base.io;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * RangeCachingInputStream 缓存 range 流测试
 * 覆盖八路径：命中 / miss / 部分命中 / 写失败降级 / 损坏块 / 越界 / skip 跨块 / EOF 截断
 * 补充：EOF 截断两场景（end<=0 非块对齐 / end>0 source 短于 end）、skip 跨块逐字节一致性、
 * r3 块对齐 EOF 用例 C/D、末块缓存策略双场景、陈旧缓存、cacheKey 隔离
 */
public class RangeCachingInputStreamTest {

    /** 默认测试块大小 */
    private static final long BS = 64;

    private Path tempDir;

    private static final class MemoryCache implements CacheReader, CacheWriter {
        final Map<Long, byte[]> blocks = new HashMap<>();
        boolean writeFails;

        @Override
        public byte[] read(long blockIndex) {
            byte[] data = blocks.get(blockIndex);
            return data == null ? null : data.clone();
        }

        @Override
        public void write(long blockIndex, byte[] data) {
            if (writeFails) {
                throw new com.tingfeng.util.java.base.lang.exception.IOException("write failed");
            }
            blocks.put(blockIndex, data.clone());
        }
    }

    /** 记录 read 调用次数的测试流（验证命中时不读 source） */
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

    /** 记录关闭状态的测试流 */
    private static final class TrackingInputStream extends ByteArrayInputStream {
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

    private static byte[] bytes(int length, int startValue) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) (startValue + i);
        }
        return data;
    }

    private static byte[] slice(byte[] data, long from) {
        byte[] result = new byte[data.length - (int) from];
        System.arraycopy(data, (int) from, result, 0, result.length);
        return result;
    }

    private static byte[] slice(byte[] data, long from, long to) {
        byte[] result = new byte[(int) (to - from)];
        System.arraycopy(data, (int) from, result, 0, result.length);
        return result;
    }

    private Path newCacheDir() throws IOException {
        tempDir = Files.createTempDirectory("rcis-test");
        return tempDir.resolve("cache");
    }

    @After
    public void cleanup() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            try (Stream<Path> walk = Files.walk(tempDir)) {
                walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    @Test
    public void testConstructorValidation() throws IOException {
        byte[] src = bytes(256, 0);
        MemoryCache cache = new MemoryCache();
        try {
            new RangeCachingInputStream(null, 0, -1, cache, cache, BS);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
        try {
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, null, cache, BS);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
        try {
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, null, BS);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
        try {
            new RangeCachingInputStream(new ByteArrayInputStream(src), 0, -1, cache, cache, 0);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
        try {
            new RangeCachingInputStream(new ByteArrayInputStream(src), -1, -1, cache, cache, BS);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
        try {
            new RangeCachingInputStream(new ByteArrayInputStream(src), 100, 100, cache, cache, BS);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
        try {
            new RangeCachingInputStream(new ByteArrayInputStream(src), 100, 50, cache, cache, BS);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
    }

    @Test
    public void testMissPathOutputMatchesSource() throws IOException {
        byte[] src = bytes(256, 0);
        MemoryCache cache = new MemoryCache();
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, -1, cache, cache, BS)) {
            Assert.assertArrayEquals(src, StreamOps.toByteArray(stream));
        }
        // miss 路径写满块缓存
        Assert.assertEquals(64, cache.blocks.get(0L).length);
        Assert.assertEquals(64, cache.blocks.get(1L).length);
        Assert.assertEquals(64, cache.blocks.get(2L).length);
        Assert.assertEquals(64, cache.blocks.get(3L).length);
    }

    @Test
    public void testHitPathOutputMatchesSource() throws IOException {
        byte[] src = bytes(256, 0);
        MemoryCache cache = new MemoryCache();
        cache.write(0, slice(src, 0, 64));
        cache.write(1, slice(src, 64, 128));
        ReadTrackingInputStream tracking = new ReadTrackingInputStream(src);
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            tracking, 0, 128, cache, cache, BS)) {
            Assert.assertArrayEquals(slice(src, 0, 128), StreamOps.toByteArray(stream));
        }
        // 命中路径完全不读 source（仅 skip 物理推进）
        Assert.assertEquals(0, tracking.readCalls);
    }

    @Test
    public void testPartialHitFirstBlock() throws IOException {
        byte[] src = bytes(256, 0);
        MemoryCache cache = new MemoryCache();
        // 预填块 1（部分命中：start 落在块 1 中间）
        cache.write(1, slice(src, 64, 128));
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 96, -1, cache, cache, BS)) {
            Assert.assertArrayEquals(slice(src, 96), StreamOps.toByteArray(stream));
        }
    }

    @Test
    public void testStartOffset() throws IOException {
        byte[] src = bytes(256, 0);
        MemoryCache cache = new MemoryCache();
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 96, -1, cache, cache, BS)) {
            Assert.assertArrayEquals(slice(src, 96), StreamOps.toByteArray(stream));
        }
    }

    @Test
    public void testStartBeyondSourceThrows() {
        byte[] src = bytes(64, 0);
        MemoryCache cache = new MemoryCache();
        try {
            new RangeCachingInputStream(new ByteArrayInputStream(src), 128, -1, cache, cache, BS);
            Assert.fail("expected project IOException");
        } catch (com.tingfeng.util.java.base.lang.exception.IOException expected) {
            // 构造定位 skipFully 不足
        }
    }

    @Test
    public void testEndCap() throws IOException {
        byte[] src = bytes(256, 0);
        MemoryCache cache = new MemoryCache();
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, 160, cache, cache, BS)) {
            Assert.assertArrayEquals(slice(src, 0, 160), StreamOps.toByteArray(stream));
            Assert.assertEquals(-1, stream.read());
        }
    }

    @Test
    public void testWriteFailureDegrades() throws IOException {
        byte[] src = bytes(256, 0);
        MemoryCache cache = new MemoryCache();
        cache.writeFails = true;
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, -1, cache, cache, BS)) {
            // 写失败降级：读路径不受影响，输出仍与源一致
            Assert.assertArrayEquals(src, StreamOps.toByteArray(stream));
        }
        Assert.assertTrue(cache.blocks.isEmpty());
    }

    @Test
    public void testCorruptedCacheBlockMiss() throws IOException {
        byte[] src = bytes(256, 0);
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir, ".cache", BS);
        // 块 0 文件超长（损坏）→ 文件层校验 miss → 从 source 重读
        Files.createDirectories(dir);
        Files.write(dir.resolve("0.cache"), new byte[(int) BS * 2]);
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, -1, cache, cache, BS)) {
            Assert.assertArrayEquals(src, StreamOps.toByteArray(stream));
        }
        // 重读后块 0 被正确数据覆盖
        Assert.assertArrayEquals(slice(src, 0, 64), cache.read(0));
    }

    @Test
    public void testEndBeyondSourceEofNonBlockAligned() throws IOException {
        // 场景 B：end>0 且 source 实际短于 end（非块对齐），读越过 source EOF → -1
        byte[] src = bytes(160, 0);
        MemoryCache cache = new MemoryCache();
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, 300, cache, cache, BS)) {
            Assert.assertArrayEquals(src, StreamOps.toByteArray(stream));
            Assert.assertEquals(-1, stream.read());
            Assert.assertEquals(-1, stream.read());
        }
    }

    @Test
    public void testSkipWithinBlock() throws IOException {
        byte[] src = bytes(256, 0);
        MemoryCache cache = new MemoryCache();
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, -1, cache, cache, BS)) {
            Assert.assertEquals(10, stream.skip(10));
            Assert.assertEquals(10, stream.read());
            Assert.assertEquals(20, stream.skip(20));
            Assert.assertEquals(31, stream.read());
        }
    }

    @Test
    public void testSkipAcrossBlocksConsistency() throws IOException {
        byte[] src = bytes(640, 0);
        MemoryCache cache = new MemoryCache();
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, -1, cache, cache, BS)) {
            // 跨 2 个块边界 skip，再逐字节读，断言输出与源数据对应位置一致
            Assert.assertEquals(160, stream.skip(160));
            byte[] actual = StreamOps.toByteArray(stream);
            Assert.assertArrayEquals(slice(src, 160), actual);
        }
    }

    @Test
    public void testSkipAcrossBlocksNonSkippingSource() throws IOException {
        byte[] src = bytes(640, 0);
        MemoryCache cache = new MemoryCache();
        InputStream nonSkipping = new ByteArrayInputStream(src) {
            @Override
            public long skip(long n) {
                return 0;
            }
        };
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            nonSkipping, 0, -1, cache, cache, BS)) {
            // 不可 skip 源：跨块 skip 后 read-discard 逐字节补齐，数据不错位
            Assert.assertEquals(160, stream.skip(160));
            Assert.assertArrayEquals(slice(src, 160), StreamOps.toByteArray(stream));
        }
    }

    @Test
    public void testEofNonBlockAligned() throws IOException {
        // 场景 A：end<=0 且 source 长度非块对齐（2.5×bs），顺序读至 EOF 后继续 read → -1
        byte[] src = bytes(160, 0);
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir, ".cache", BS);
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, -1, cache, cache, BS)) {
            Assert.assertArrayEquals(src, StreamOps.toByteArray(stream));
            Assert.assertEquals(-1, stream.read());
        }
        // EOF 截断的不满块（块 2 仅 32 字节）不写缓存
        Assert.assertFalse(Files.exists(dir.resolve("2.cache")));
        Assert.assertTrue(Files.exists(dir.resolve("0.cache")));
        Assert.assertTrue(Files.exists(dir.resolve("1.cache")));
    }

    @Test
    public void testEofBlockAlignedNoWrite() throws IOException {
        // 场景 C：end<=0 且 source 长度恰为 blockSize 整数倍（2×bs）——最后一块起点即 EOF，
        // 顺序读完全部块后继续 read → -1 且不抛 AIOOBE；EOF 空读分支不写缓存
        byte[] src = bytes(128, 0);
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir, ".cache", BS);
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, -1, cache, cache, BS)) {
            Assert.assertArrayEquals(src, StreamOps.toByteArray(stream));
            Assert.assertEquals(-1, stream.read());
            Assert.assertEquals(-1, stream.read());
        }
        Assert.assertTrue(Files.exists(dir.resolve("0.cache")));
        Assert.assertTrue(Files.exists(dir.resolve("1.cache")));
        Assert.assertFalse(Files.exists(dir.resolve("2.cache")));
    }

    @Test
    public void testEofBlockAlignedShorterEnd() throws IOException {
        // 场景 D：end>0 且 end > source 长度、source 长度恰为 blockSize 整数倍——
        // 越过 source EOF 后 read → -1 且不抛 AIOOBE（步骤 4 空读分支）
        byte[] src = bytes(128, 0);
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir, ".cache", BS);
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, 300, cache, cache, BS)) {
            Assert.assertArrayEquals(src, StreamOps.toByteArray(stream));
            Assert.assertEquals(-1, stream.read());
            Assert.assertEquals(-1, stream.read());
        }
        Assert.assertFalse(Files.exists(dir.resolve("2.cache")));
    }

    @Test
    public void testLastBlockCachePolicyEndPositive() throws IOException {
        // 末块缓存策略：end>0 时末块不满块（expectedLen < blockSize）写入缓存并可命中
        byte[] src = bytes(160, 0);
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir, ".cache", BS);
        byte[] first;
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, 96, cache, cache, BS)) {
            first = StreamOps.toByteArray(stream);
        }
        Assert.assertArrayEquals(slice(src, 0, 96), first);
        // 末块（块 1，32 字节不满块）写入缓存
        Assert.assertTrue(Files.exists(dir.resolve("1.cache")));
        Assert.assertEquals(32, Files.size(dir.resolve("1.cache")));
        // 重开流：末块命中
        ReadTrackingInputStream tracking = new ReadTrackingInputStream(src);
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            tracking, 0, 96, cache, cache, BS)) {
            Assert.assertArrayEquals(slice(src, 0, 96), StreamOps.toByteArray(stream));
        }
        Assert.assertEquals(0, tracking.readCalls);
    }

    @Test
    public void testLastBlockCachePolicyEndNonPositive() throws IOException {
        // 末块缓存策略：end<=0 时 EOF 截断不满块不写缓存、重复读仍 miss 重读
        byte[] src = bytes(160, 0);
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir, ".cache", BS);
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, -1, cache, cache, BS)) {
            Assert.assertArrayEquals(src, StreamOps.toByteArray(stream));
        }
        Assert.assertFalse(Files.exists(dir.resolve("2.cache")));
        // 重开流：块 2 仍 miss 重读，输出与源一致
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, -1, cache, cache, BS)) {
            Assert.assertArrayEquals(src, StreamOps.toByteArray(stream));
        }
        Assert.assertFalse(Files.exists(dir.resolve("2.cache")));
    }

    @Test
    public void testStalePartialBlockNotHit() throws IOException {
        // 陈旧缓存：先以 end=1.5×blockSize 跑一次（块 1 产生 32 字节不满块缓存），
        // 再以 end<=0 重开流 → 陈旧部分块不命中（expectedLen==blockSize 校验失败）→ 从 source 重读
        byte[] src = bytes(160, 0);
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir, ".cache", BS);
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, 96, cache, cache, BS)) {
            StreamOps.toByteArray(stream);
        }
        Assert.assertEquals(32, Files.size(dir.resolve("1.cache")));
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, -1, cache, cache, BS)) {
            Assert.assertArrayEquals(src, StreamOps.toByteArray(stream));
        }
        // 重读后块 1 被满块覆盖
        Assert.assertEquals(64, Files.size(dir.resolve("1.cache")));
    }

    @Test
    public void testCacheKeyIsolation() throws IOException {
        byte[] src = bytes(256, 0);
        Path dir = newCacheDir();
        FileRangeCache cacheA = new FileRangeCache(dir, ".cache", BS);
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, 128, cacheA, cacheA, BS, "keyA")) {
            Assert.assertArrayEquals(slice(src, 0, 128), StreamOps.toByteArray(stream));
        }
        // cacheKey 透传绑定：块文件写入 cacheDir/keyA/ 子目录，与无身份路径隔离
        Assert.assertTrue(Files.exists(dir.resolve("keyA").resolve("0.cache")));
        Assert.assertFalse(Files.exists(dir.resolve("0.cache")));
        // 另一身份 keyB 读不到 keyA 的块
        FileRangeCache cacheB = new FileRangeCache(dir, ".cache", BS);
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, 128, cacheB, cacheB, BS, "keyB")) {
            Assert.assertArrayEquals(slice(src, 0, 128), StreamOps.toByteArray(stream));
        }
        Assert.assertTrue(Files.exists(dir.resolve("keyB").resolve("0.cache")));
    }

    @Test
    public void testHitThenSkipAcrossBlocksConsistency() throws IOException {
        // 命中缓存后 skip 跨块：物理推进与 blockOffset 组合验证，输出与源对应位置一致
        byte[] src = bytes(640, 0);
        MemoryCache cache = new MemoryCache();
        cache.write(0, slice(src, 0, 64));
        cache.write(1, slice(src, 64, 128));
        cache.write(2, slice(src, 128, 192));
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, -1, cache, cache, BS)) {
            Assert.assertEquals(10, stream.skip(10));
            Assert.assertEquals(10, stream.read());
            // 从块 0 内 11 处跨块 skip 至块 1 中间
            Assert.assertEquals(100, stream.skip(100));
            byte[] actual = StreamOps.toByteArray(stream);
            Assert.assertArrayEquals(slice(src, 111), actual);
        }
    }

    @Test
    public void testSourceReadErrorPropagates() throws IOException {
        MemoryCache cache = new MemoryCache();
        InputStream throwing = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("boom");
            }
        };
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            throwing, 0, -1, cache, cache, BS)) {
            try {
                stream.read();
                Assert.fail("expected IOException");
            } catch (IOException expected) {
                // 预期异常
            }
        }
    }

    @Test
    public void testAvailable() throws IOException {
        byte[] src = bytes(256, 0);
        MemoryCache cache = new MemoryCache();
        try (RangeCachingInputStream stream = new RangeCachingInputStream(
            new ByteArrayInputStream(src), 0, -1, cache, cache, BS)) {
            Assert.assertEquals(0, stream.available());
            Assert.assertEquals(0, stream.read());
            Assert.assertEquals(63, stream.available());
            byte[] buf = new byte[32];
            Assert.assertEquals(32, stream.read(buf, 0, 32));
            Assert.assertEquals(31, stream.available());
        }
    }

    @Test
    public void testCloseClosesSource() throws IOException {
        byte[] src = bytes(64, 0);
        MemoryCache cache = new MemoryCache();
        TrackingInputStream tracking = new TrackingInputStream(src);
        RangeCachingInputStream stream = new RangeCachingInputStream(tracking, 0, -1, cache, cache, BS);
        stream.close();
        stream.close();
        Assert.assertTrue(tracking.closed);
    }
}
