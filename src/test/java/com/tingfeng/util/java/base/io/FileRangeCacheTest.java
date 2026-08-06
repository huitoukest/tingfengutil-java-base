package com.tingfeng.util.java.base.io;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * FileRangeCache 文件块缓存测试
 * 覆盖：块读写 / 损坏块 miss / 自定义后缀 / 构造校验 / 目录自动创建 /
 * cacheKey 子目录隔离 / clear / deleteBlock / 并发原子写
 */
public class FileRangeCacheTest {

    private Path tempDir;

    /** 创建新的测试缓存目录 */
    private Path newCacheDir() throws IOException {
        tempDir = Files.createTempDirectory("frc-test");
        return tempDir.resolve("cache");
    }

    @After
    public void cleanup() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            deleteRecursively(tempDir);
        }
    }

    @Test
    public void testWriteAndReadBlock() throws IOException {
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir, ".cache", 64);
        byte[] data = new byte[64];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i + 1);
        }
        cache.write(2, data);
        Assert.assertArrayEquals(data, cache.read(2));
        Assert.assertTrue(Files.exists(dir.resolve("2.cache")));
    }

    @Test
    public void testReadMissingBlockReturnsNull() throws IOException {
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir);
        Assert.assertNull(cache.read(0));
        Assert.assertNull(cache.read(100));
    }

    @Test
    public void testReadEmptyFileReturnsNull() throws IOException {
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir, ".cache", 64);
        Files.createFile(dir.resolve("0.cache"));
        Assert.assertNull(cache.read(0));
    }

    @Test
    public void testReadOversizedFileReturnsNull() throws IOException {
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir, ".cache", 64);
        Files.write(dir.resolve("0.cache"), new byte[128]);
        Assert.assertNull(cache.read(0));
    }

    @Test
    public void testCustomSuffix() throws IOException {
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir, ".blk", 64);
        byte[] data = new byte[64];
        Arrays.fill(data, (byte) 7);
        cache.write(0, data);
        Assert.assertArrayEquals(data, cache.read(0));
        Assert.assertTrue(Files.exists(dir.resolve("0.blk")));
        Assert.assertFalse(Files.exists(dir.resolve("0.cache")));
    }

    @Test
    public void testConstructorInvalidBlockSize() throws IOException {
        Path dir = newCacheDir();
        try {
            new FileRangeCache(dir, ".cache", 0);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
        try {
            new FileRangeCache(dir, ".cache", -1);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
    }

    @Test
    public void testConstructorNullArgs() throws IOException {
        try {
            new FileRangeCache(null);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
        try {
            new FileRangeCache(tempDir, null, 64);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
    }

    @Test
    public void testConstructorCreatesDirAutomatically() throws IOException {
        Path dir = newCacheDir();
        Assert.assertFalse(Files.exists(dir));
        FileRangeCache cache = new FileRangeCache(dir);
        Assert.assertTrue(Files.isDirectory(dir));
        cache.write(0, new byte[4]);
        Assert.assertTrue(Files.exists(dir.resolve("0.cache")));
    }

    @Test
    public void testCacheKeySubdirIsolation() throws IOException {
        Path dir = newCacheDir();
        FileRangeCache cacheA = new FileRangeCache(dir, ".cache", 64, "keyA");
        FileRangeCache cacheB = new FileRangeCache(dir, ".cache", 64, "keyB");
        byte[] dataA = new byte[64];
        Arrays.fill(dataA, (byte) 1);
        byte[] dataB = new byte[64];
        Arrays.fill(dataB, (byte) 2);
        cacheA.write(0, dataA);
        cacheB.write(0, dataB);
        // 同一块索引互不串数据（子目录隔离）
        Assert.assertArrayEquals(dataA, cacheA.read(0));
        Assert.assertArrayEquals(dataB, cacheB.read(0));
        Assert.assertTrue(Files.exists(dir.resolve("keyA").resolve("0.cache")));
        Assert.assertTrue(Files.exists(dir.resolve("keyB").resolve("0.cache")));
        Assert.assertFalse(Files.exists(dir.resolve("0.cache")));
        // 未绑定身份的同目录缓存互不可见
        FileRangeCache cachePlain = new FileRangeCache(dir, ".cache", 64);
        Assert.assertNull(cachePlain.read(0));
    }

    @Test
    public void testBindCacheKey() throws IOException {
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir, ".cache", 64);
        Assert.assertNull(cache.getCacheKey());
        cache.bindCacheKey("myKey");
        Assert.assertEquals("myKey", cache.getCacheKey());
        cache.bindCacheKey("myKey");
        try {
            cache.bindCacheKey("otherKey");
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
    }

    @Test
    public void testClearRemovesAllBlocks() throws IOException {
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir, ".cache", 64);
        cache.write(0, new byte[64]);
        cache.write(1, new byte[64]);
        cache.write(2, new byte[64]);
        cache.clear();
        Assert.assertNull(cache.read(0));
        Assert.assertNull(cache.read(1));
        Assert.assertNull(cache.read(2));
        Assert.assertTrue(Files.isDirectory(dir));
    }

    @Test
    public void testClearWithCacheKeyOnlyClearsSubdir() throws IOException {
        Path dir = newCacheDir();
        FileRangeCache cacheA = new FileRangeCache(dir, ".cache", 64, "keyA");
        FileRangeCache cacheB = new FileRangeCache(dir, ".cache", 64, "keyB");
        cacheA.write(0, new byte[64]);
        cacheB.write(0, new byte[64]);
        cacheA.clear();
        Assert.assertNull(cacheA.read(0));
        Assert.assertNotNull(cacheB.read(0));
    }

    @Test
    public void testDeleteBlockRemovesSingle() throws IOException {
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir, ".cache", 64);
        cache.write(0, new byte[64]);
        cache.write(1, new byte[64]);
        cache.deleteBlock(0);
        Assert.assertNull(cache.read(0));
        Assert.assertNotNull(cache.read(1));
        cache.deleteBlock(99);
    }

    @Test
    public void testWriteNullDataThrows() throws IOException {
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir);
        try {
            cache.write(0, null);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
    }

    @Test
    public void testConcurrentAtomicWrite() throws IOException, InterruptedException {
        Path dir = newCacheDir();
        FileRangeCache cache = new FileRangeCache(dir, ".cache", 64);
        int threads = 8;
        int rounds = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) {
            final byte fill = (byte) (t % 2 == 0 ? 0x41 : 0x42);
            pool.execute(() -> {
                try {
                    start.await();
                    byte[] data = new byte[64];
                    Arrays.fill(data, fill);
                    for (int i = 0; i < rounds; i++) {
                        try {
                            cache.write(0, data);
                        } catch (com.tingfeng.util.java.base.lang.exception.IOException e) {
                            // 并发写同一块竞争失败允许降级（设计内路径），数据完整性由最终断言保证
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        Assert.assertTrue(done.await(30, TimeUnit.SECONDS));
        pool.shutdown();
        byte[] result = cache.read(0);
        Assert.assertNotNull(result);
        for (byte b : result) {
            Assert.assertTrue("数据损坏（半写块）", b == 0x41 || b == 0x42);
        }
    }

    /** 递归删除目录 */
    private static void deleteRecursively(Path dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
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
