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
 * BoundedInputStream 限长流测试
 * 覆盖：限长内读取一致、超限截断、limit=0 空流、skip/available 截断、close 传播、参数校验、JDK 嵌套
 */
public class BoundedInputStreamTest {

    private static final String TEST_CONTENT = "Hello, World! 你好，世界！";

    @Test
    public void testReadWithinLimit() throws Exception {
        byte[] data = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), data.length);
        Assert.assertArrayEquals(data, readAll(bounded));
    }

    @Test
    public void testReadLimitTruncates() throws Exception {
        byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), 5);
        Assert.assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, readAll(bounded));
    }

    @Test
    public void testReadZeroLimit() throws Exception {
        BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3}), 0);
        Assert.assertEquals(-1, bounded.read());
        Assert.assertEquals(-1, bounded.read(new byte[10], 0, 10));
    }

    @Test
    public void testSkipTruncated() throws Exception {
        byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), 5);
        Assert.assertEquals(5, bounded.skip(10));
        Assert.assertEquals(-1, bounded.read());
    }

    @Test
    public void testAvailableTruncated() throws Exception {
        byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), 5);
        Assert.assertEquals(5, bounded.available());
        bounded.read();
        Assert.assertEquals(4, bounded.available());
    }

    @Test
    public void testClosePropagates() throws Exception {
        TrackingInputStream source = new TrackingInputStream(new byte[]{1, 2, 3});
        BoundedInputStream bounded = new BoundedInputStream(source, 10);
        bounded.close();
        Assert.assertTrue(source.closed);
        // 重复 close 幂等
        bounded.close();
        Assert.assertTrue(source.closed);
    }

    @Test
    public void testConstructorValidation() {
        try {
            new BoundedInputStream(null, 10);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        try {
            new BoundedInputStream(new ByteArrayInputStream(new byte[0]), -1);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testNestedWithBufferedInputStream() throws Exception {
        byte[] data = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        InputStream nested = new BufferedInputStream(
            new BoundedInputStream(new ByteArrayInputStream(data), data.length), 16);
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
        byte[] buffer = new byte[16];
        int len;
        while ((len = input.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        return bos.toByteArray();
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
