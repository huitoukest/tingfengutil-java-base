package com.tingfeng.util.java.base.io;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * RateLimitedInputStream 限速输入流测试
 * 统一参数 rate=100KB/s、interval=100ms → burst=10240B；断言按 D17 公式 max(0, N-burst)/rate 重算，
 * 耗时断言取理论值下限（×0.8 宽松容差，防 sleep 精度 flaky）
 */
public class RateLimitedInputStreamTest {

    private static final String TEST_CONTENT = "Hello, World! 你好，世界！";

    @Test
    public void testHighRateReadsConsistent() throws Exception {
        byte[] data = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        RateLimitedInputStream rl = new RateLimitedInputStream(new ByteArrayInputStream(data), 1, RateUnit.GB);
        Assert.assertArrayEquals(data, readAll(rl));
    }

    @Test
    public void testRateLimitedReadTiming() throws Exception {
        // 读 20480B = 2×burst @ 100KB/s → 期望 (20480-10240)/102400 = 100ms
        byte[] data = new byte[20480];
        fillSequential(data);
        RateLimitedInputStream rl = new RateLimitedInputStream(new ByteArrayInputStream(data), 100, RateUnit.KB);
        long start = System.currentTimeMillis();
        byte[] result = new byte[20480];
        StreamOps.readFully(rl, result);
        long elapsed = System.currentTimeMillis() - start;
        Assert.assertArrayEquals(data, result);
        Assert.assertTrue("expected >= 80ms but was " + elapsed + "ms", elapsed >= 80);
    }

    @Test
    public void testSmoothThrottling() throws Exception {
        // 预热读 10240B（恰好消耗满桶 burst，零等待）后分 5 块各读 2048B：每块期望 ≈ 20ms
        byte[] data = new byte[10240 + 5 * 2048];
        fillSequential(data);
        RateLimitedInputStream rl = new RateLimitedInputStream(new ByteArrayInputStream(data), 100, RateUnit.KB);
        byte[] warmup = new byte[10240];
        long warmStart = System.currentTimeMillis();
        StreamOps.readFully(rl, warmup);
        long warmElapsed = System.currentTimeMillis() - warmStart;
        Assert.assertTrue("warmup should consume burst without wait, but was " + warmElapsed + "ms", warmElapsed < 50);
        long total = 0;
        for (int i = 0; i < 5; i++) {
            byte[] chunk = new byte[2048];
            long blockStart = System.currentTimeMillis();
            StreamOps.readFully(rl, chunk);
            long blockElapsed = System.currentTimeMillis() - blockStart;
            total += blockElapsed;
            Assert.assertTrue("block " + i + " expected >= 5ms but was " + blockElapsed + "ms", blockElapsed >= 5);
        }
        Assert.assertTrue("5 blocks expected >= 80ms but was " + total + "ms", total >= 80);
    }

    @Test
    public void testUnitConversion() throws Exception {
        byte[] data = new byte[256];
        fillSequential(data);
        // 1KB/s：burst=102.4B，读 256B → 期望 (256-102.4)/1024 ≈ 150ms
        RateLimitedInputStream slow = new RateLimitedInputStream(new ByteArrayInputStream(data), 1, RateUnit.KB);
        long start = System.currentTimeMillis();
        StreamOps.readFully(slow, new byte[256]);
        long elapsed = System.currentTimeMillis() - start;
        Assert.assertTrue("1KB/s expected >= 75ms but was " + elapsed + "ms", elapsed >= 75);
        // 1MB/s：burst=102400B，读 256B 零等待
        RateLimitedInputStream fast = new RateLimitedInputStream(new ByteArrayInputStream(data), 1, RateUnit.MB);
        start = System.currentTimeMillis();
        StreamOps.readFully(fast, new byte[256]);
        elapsed = System.currentTimeMillis() - start;
        Assert.assertTrue("1MB/s expected < 50ms but was " + elapsed + "ms", elapsed < 50);
    }

    @Test
    public void testIntervalBoundaries() {
        new RateLimitedInputStream(new ByteArrayInputStream(new byte[0]), 100, RateUnit.KB, 1);
        new RateLimitedInputStream(new ByteArrayInputStream(new byte[0]), 100, RateUnit.KB, 1000);
        try {
            new RateLimitedInputStream(new ByteArrayInputStream(new byte[0]), 100, RateUnit.KB, 0);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        try {
            new RateLimitedInputStream(new ByteArrayInputStream(new byte[0]), 100, RateUnit.KB, 1001);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testConstructorValidation() {
        try {
            new RateLimitedInputStream(null, 100, RateUnit.KB);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        try {
            new RateLimitedInputStream(new ByteArrayInputStream(new byte[0]), 100, null);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        try {
            new RateLimitedInputStream(new ByteArrayInputStream(new byte[0]), 0, RateUnit.KB);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        try {
            new RateLimitedInputStream(new ByteArrayInputStream(new byte[0]), -1, RateUnit.KB);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testClosePropagates() throws Exception {
        TrackingInputStream source = new TrackingInputStream(new byte[]{1, 2, 3});
        RateLimitedInputStream rl = new RateLimitedInputStream(source, 1, RateUnit.GB);
        rl.close();
        Assert.assertTrue(source.closed);
        // 重复 close 幂等
        rl.close();
        Assert.assertTrue(source.closed);
    }

    @Test
    public void testSkipRateLimited() throws Exception {
        // skip 300B @ 1KB/s → 期望 (300-102.4)/1024 ≈ 193ms（严格限速防绕过）
        byte[] data = new byte[1024];
        fillSequential(data);
        RateLimitedInputStream rl = new RateLimitedInputStream(new ByteArrayInputStream(data), 1, RateUnit.KB);
        long start = System.currentTimeMillis();
        long skipped = rl.skip(300);
        long elapsed = System.currentTimeMillis() - start;
        Assert.assertEquals(300, skipped);
        Assert.assertTrue("skip expected >= 100ms but was " + elapsed + "ms", elapsed >= 100);
    }

    @Test
    public void testEofEmptySource() throws Exception {
        RateLimitedInputStream rl = new RateLimitedInputStream(new ByteArrayInputStream(new byte[0]), 100, RateUnit.KB);
        Assert.assertEquals(-1, rl.read());
        Assert.assertEquals(-1, rl.read(new byte[10], 0, 10));
    }

    @Test
    public void testNestedWithBoundedAndBuffered() throws Exception {
        // Buffered 包 Bounded 包限速流（高限速零等待），读内容与源一致
        byte[] data = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        InputStream nested = new BufferedInputStream(
            new BoundedInputStream(
                new RateLimitedInputStream(new ByteArrayInputStream(data), 1, RateUnit.GB), data.length), 16);
        Assert.assertArrayEquals(data, readAll(nested));
    }

    /**
     * 读取流全部字节
     * @param input 输入流
     * @return 全部字节
     * @throws IOException 读取失败
     */
    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = input.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        return bos.toByteArray();
    }

    /**
     * 顺序填充数据
     * @param data 目标数组
     */
    private static void fillSequential(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
    }

    /**
     * 记录 close 调用痕迹的输入流
     */
    private static class TrackingInputStream extends ByteArrayInputStream {

        private boolean closed;

        TrackingInputStream(byte[] buf) {
            super(buf);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }
}
