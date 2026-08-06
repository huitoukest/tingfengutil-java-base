package com.tingfeng.util.java.base.io;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * RateLimitedOutputStream 限速输出流测试
 * 统一参数 rate=100KB/s、interval=100ms → burst=10240B；耗时断言按 D17 公式宽松下限
 */
public class RateLimitedOutputStreamTest {

    @Test
    public void testWriteConsistent() throws Exception {
        byte[] data = new byte[1024];
        fillSequential(data);
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        RateLimitedOutputStream rl = new RateLimitedOutputStream(target, 1, RateUnit.GB);
        rl.write(data);
        rl.flush();
        Assert.assertArrayEquals(data, target.toByteArray());
    }

    @Test
    public void testWriteRateLimited() throws Exception {
        // 写 20480B = 2×burst @ 100KB/s → 期望 (20480-10240)/102400 = 100ms
        byte[] data = new byte[20480];
        fillSequential(data);
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        RateLimitedOutputStream rl = new RateLimitedOutputStream(target, 100, RateUnit.KB);
        long start = System.currentTimeMillis();
        rl.write(data);
        long elapsed = System.currentTimeMillis() - start;
        Assert.assertEquals(20480, target.size());
        Assert.assertTrue("expected >= 80ms but was " + elapsed + "ms", elapsed >= 80);
    }

    @Test
    public void testFlushNotThrottled() throws Exception {
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        RateLimitedOutputStream rl = new RateLimitedOutputStream(target, 100, RateUnit.KB);
        long start = System.currentTimeMillis();
        rl.write(new byte[10240]); // 恰好消耗满桶（burst=10240），零等待
        rl.flush();
        rl.flush();
        long elapsed = System.currentTimeMillis() - start;
        Assert.assertTrue("flush should not throttle, but was " + elapsed + "ms", elapsed < 50);
    }

    @Test
    public void testClosePropagates() throws Exception {
        TrackingOutputStream target = new TrackingOutputStream();
        RateLimitedOutputStream rl = new RateLimitedOutputStream(target, 1, RateUnit.GB);
        rl.close();
        Assert.assertTrue(target.closed);
        // 重复 close 幂等
        rl.close();
        Assert.assertTrue(target.closed);
    }

    @Test
    public void testConstructorValidation() {
        try {
            new RateLimitedOutputStream(null, 100, RateUnit.KB);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        try {
            new RateLimitedOutputStream(new ByteArrayOutputStream(), 100, null);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        try {
            new RateLimitedOutputStream(new ByteArrayOutputStream(), 0, RateUnit.KB);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        try {
            new RateLimitedOutputStream(new ByteArrayOutputStream(), 100, RateUnit.KB, 0);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        try {
            new RateLimitedOutputStream(new ByteArrayOutputStream(), 100, RateUnit.KB, 1001);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
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
     * 记录 close 调用痕迹的输出流
     */
    private static class TrackingOutputStream extends ByteArrayOutputStream {

        private boolean closed;

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }
}
