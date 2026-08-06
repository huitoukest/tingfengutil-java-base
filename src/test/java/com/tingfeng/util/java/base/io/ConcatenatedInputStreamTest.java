package com.tingfeng.util.java.base.io;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * ConcatenatedInputStream 拼接流测试
 * 覆盖：顺序拼接字节一致性 / 跨流边界 / 空源 / null 元素 / 参数校验 /
 * close 关闭全部子流（含未消费）/ 跨流 skip 尽力语义 / 与 copy 衔接
 */
public class ConcatenatedInputStreamTest {

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

    /** skip 恒返回 0 的测试流（验证尽力 read 回退） */
    private static class NonSkippingInputStream extends ByteArrayInputStream {
        NonSkippingInputStream(byte[] data) {
            super(data);
        }

        @Override
        public long skip(long n) {
            return 0;
        }
    }

    /** read 恒抛 JDK IOException 的测试流（验证异常包装） */
    private static class ThrowingInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            throw new IOException("boom");
        }
    }

    private static byte[] bytes(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] concat(byte[]... arrays) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] array : arrays) {
            out.write(array, 0, array.length);
        }
        return out.toByteArray();
    }

    @Test
    public void testConcatenateOrderAndBytesConsistent() throws IOException {
        byte[] a = bytes("abc");
        byte[] b = bytes("defg");
        byte[] c = bytes("hi");
        try (ConcatenatedInputStream stream = StreamOps.concatenate(
            new ByteArrayInputStream(a), new ByteArrayInputStream(b), new ByteArrayInputStream(c))) {
            byte[] actual = StreamOps.toByteArray(stream);
            Assert.assertArrayEquals(concat(a, b, c), actual);
        }
    }

    @Test
    public void testConcatenateSingleByteAcrossBoundary() throws IOException {
        InputStream s1 = new ByteArrayInputStream(bytes("a"));
        InputStream s2 = new ByteArrayInputStream(bytes("b"));
        InputStream s3 = new ByteArrayInputStream(bytes("c"));
        try (ConcatenatedInputStream stream = StreamOps.concatenate(s1, s2, s3)) {
            Assert.assertEquals('a', stream.read());
            Assert.assertEquals('b', stream.read());
            Assert.assertEquals('c', stream.read());
            Assert.assertEquals(-1, stream.read());
        }
    }

    @Test
    public void testConcatenateBatchReadCrossBoundary() throws IOException {
        InputStream s1 = new ByteArrayInputStream(bytes("ab"));
        InputStream s2 = new ByteArrayInputStream(bytes("cdef"));
        try (ConcatenatedInputStream stream = StreamOps.concatenate(s1, s2)) {
            byte[] buf = new byte[5];
            int n1 = stream.read(buf, 0, 5);
            Assert.assertEquals(2, n1);
            Assert.assertEquals("ab", new String(buf, 0, n1, StandardCharsets.UTF_8));
            int n2 = stream.read(buf, 0, 5);
            Assert.assertEquals(4, n2);
            Assert.assertEquals("cdef", new String(buf, 0, n2, StandardCharsets.UTF_8));
            Assert.assertEquals(-1, stream.read(buf, 0, 5));
        }
    }

    @Test
    public void testConcatenateReadBufferLenZeroReturnsZero() throws IOException {
        try (ConcatenatedInputStream stream = StreamOps.concatenate(new ByteArrayInputStream(bytes("ab")))) {
            Assert.assertEquals(0, stream.read(new byte[1], 0, 0));
        }
    }

    @Test
    public void testConcatenateReadBufferNullThrowsNpe() throws IOException {
        try (ConcatenatedInputStream stream = StreamOps.concatenate(new ByteArrayInputStream(bytes("ab")))) {
            try {
                stream.read(null, 0, 1);
                Assert.fail("expected NullPointerException");
            } catch (NullPointerException expected) {
                // 预期异常
            }
        }
    }

    @Test
    public void testConcatenateReadBufferOutOfBounds() throws IOException {
        try (ConcatenatedInputStream stream = StreamOps.concatenate(new ByteArrayInputStream(bytes("ab")))) {
            byte[] buf = new byte[4];
            try {
                stream.read(buf, -1, 2);
                Assert.fail("expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException expected) {
                // 预期异常
            }
            try {
                stream.read(buf, 0, -1);
                Assert.fail("expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException expected) {
                // 预期异常
            }
            try {
                stream.read(buf, 2, 3);
                Assert.fail("expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException expected) {
                // 预期异常
            }
        }
    }

    @Test
    public void testConcatenateEmptyVarargs() throws IOException {
        try (ConcatenatedInputStream stream = StreamOps.concatenate()) {
            Assert.assertEquals(-1, stream.read());
            Assert.assertEquals(0, StreamOps.toByteArray(stream).length);
        }
    }

    @Test
    public void testConcatenateEmptyList() throws IOException {
        try (ConcatenatedInputStream stream = StreamOps.concatenate(Collections.<InputStream>emptyList())) {
            Assert.assertEquals(-1, stream.read());
            Assert.assertEquals(0, StreamOps.toByteArray(stream).length);
        }
    }

    @Test
    public void testConcatenateNullElementSkipped() throws IOException {
        byte[] a = bytes("abc");
        byte[] b = bytes("def");
        try (ConcatenatedInputStream stream = StreamOps.concatenate(
            new ByteArrayInputStream(a), null, new ByteArrayInputStream(b))) {
            Assert.assertArrayEquals(concat(a, b), StreamOps.toByteArray(stream));
        }
    }

    @Test
    public void testConcatenateAllNullElements() throws IOException {
        try (ConcatenatedInputStream stream = StreamOps.concatenate(null, null)) {
            Assert.assertEquals(-1, stream.read());
        }
    }

    @Test
    public void testConcatenateNullVarargsThrows() {
        try {
            StreamOps.concatenate((InputStream[]) null);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
    }

    @Test
    public void testConcatenateNullListThrows() {
        try {
            StreamOps.concatenate((List<InputStream>) null);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
    }

    @Test
    public void testConcatenateNullEnumerationThrows() {
        try {
            StreamOps.concatenate((Enumeration<InputStream>) null);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
    }

    @Test
    public void testConcatenateListEntry() throws IOException {
        List<InputStream> sources = new ArrayList<>();
        sources.add(new ByteArrayInputStream(bytes("ab")));
        sources.add(null);
        sources.add(new ByteArrayInputStream(bytes("cd")));
        try (ConcatenatedInputStream stream = StreamOps.concatenate(sources)) {
            Assert.assertArrayEquals(bytes("abcd"), StreamOps.toByteArray(stream));
        }
    }

    @Test
    public void testConcatenateEnumerationEntry() throws IOException {
        Vector<InputStream> vector = new Vector<>();
        vector.add(new ByteArrayInputStream(bytes("abc")));
        vector.add(new ByteArrayInputStream(bytes("de")));
        try (ConcatenatedInputStream stream = StreamOps.concatenate(vector.elements())) {
            Assert.assertArrayEquals(bytes("abcde"), StreamOps.toByteArray(stream));
        }
    }

    @Test
    public void testConcatenateCloseClosesAllIncludingUnconsumed() throws IOException {
        TrackingInputStream t1 = new TrackingInputStream(bytes("abc"));
        TrackingInputStream t2 = new TrackingInputStream(bytes("def"));
        TrackingInputStream t3 = new TrackingInputStream(bytes("ghi"));
        ConcatenatedInputStream stream = StreamOps.concatenate(t1, t2, t3);
        Assert.assertEquals('a', stream.read());
        stream.close();
        Assert.assertTrue(t1.closed);
        Assert.assertTrue(t2.closed);
        Assert.assertTrue(t3.closed);
    }

    @Test
    public void testConcatenateCloseIdempotent() throws IOException {
        TrackingInputStream t1 = new TrackingInputStream(bytes("abc"));
        ConcatenatedInputStream stream = StreamOps.concatenate(t1);
        stream.close();
        stream.close();
        Assert.assertTrue(t1.closed);
    }

    @Test
    public void testConcatenateReadAfterCloseThrows() throws IOException {
        ConcatenatedInputStream stream = StreamOps.concatenate(new ByteArrayInputStream(bytes("ab")));
        stream.close();
        try {
            stream.read();
            Assert.fail("expected IOException");
        } catch (java.io.IOException expected) {
            // 预期异常
        }
        try {
            stream.read(new byte[4], 0, 2);
            Assert.fail("expected IOException");
        } catch (java.io.IOException expected) {
            // 预期异常
        }
        try {
            stream.skip(1);
            Assert.fail("expected IOException");
        } catch (java.io.IOException expected) {
            // 预期异常
        }
    }

    @Test
    public void testConcatenateSkipAcrossStreams() throws IOException {
        InputStream s1 = new ByteArrayInputStream(bytes("abcde"));
        InputStream s2 = new ByteArrayInputStream(bytes("fghij"));
        try (ConcatenatedInputStream stream = StreamOps.concatenate(s1, s2)) {
            Assert.assertEquals(7, stream.skip(7));
            Assert.assertEquals('h', stream.read());
            Assert.assertEquals("ij", StreamOps.toString(stream));
        }
    }

    @Test
    public void testConcatenateSkipBeyondTotal() throws IOException {
        InputStream s1 = new ByteArrayInputStream(bytes("abc"));
        InputStream s2 = new ByteArrayInputStream(bytes("de"));
        try (ConcatenatedInputStream stream = StreamOps.concatenate(s1, s2)) {
            Assert.assertEquals(5, stream.skip(100));
            Assert.assertEquals(-1, stream.read());
        }
    }

    @Test
    public void testConcatenateSkipWithNonSkippingStream() throws IOException {
        InputStream s = new NonSkippingInputStream(bytes("abcdefghij"));
        try (ConcatenatedInputStream stream = StreamOps.concatenate(s)) {
            Assert.assertEquals(3, stream.skip(3));
            Assert.assertEquals('d', stream.read());
        }
    }

    @Test
    public void testConcatenateCopyIntegration() throws IOException {
        byte[] a = bytes("line1\n");
        byte[] b = bytes("line2\n");
        byte[] c = bytes("line3");
        ConcatenatedInputStream stream = StreamOps.concatenate(
            new ByteArrayInputStream(a), new ByteArrayInputStream(b), new ByteArrayInputStream(c));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamTransferOps.copy(out, stream);
        Assert.assertArrayEquals(concat(a, b, c), out.toByteArray());
    }

    @Test
    public void testConcatenateAvailable() throws IOException {
        InputStream s1 = new ByteArrayInputStream(bytes("abc"));
        InputStream s2 = new ByteArrayInputStream(bytes("de"));
        try (ConcatenatedInputStream stream = StreamOps.concatenate(s1, s2)) {
            // 惰性取流：构造后尚无当前子流 → 0
            Assert.assertEquals(0, stream.available());
            Assert.assertEquals('a', stream.read());
            Assert.assertEquals(2, stream.available());
            Assert.assertEquals('b', stream.read());
            Assert.assertEquals('c', stream.read());
            Assert.assertEquals(0, stream.available());
            Assert.assertEquals('d', stream.read());
            Assert.assertEquals(1, stream.available());
            Assert.assertEquals('e', stream.read());
            Assert.assertEquals(0, stream.available());
            Assert.assertEquals(-1, stream.read());
            Assert.assertEquals(0, stream.available());
        }
    }

    @Test
    public void testConcatenateDirectConstructor() throws IOException {
        List<InputStream> sources = Arrays.asList(
            new ByteArrayInputStream(bytes("abc")), new ByteArrayInputStream(bytes("def")));
        try (ConcatenatedInputStream stream = new ConcatenatedInputStream(sources.iterator())) {
            Assert.assertArrayEquals(bytes("abcdef"), StreamOps.toByteArray(stream));
        }
    }

    @Test
    public void testConcatenateSubStreamReadErrorWrapped() throws IOException {
        try (ConcatenatedInputStream stream = StreamOps.concatenate(new ThrowingInputStream())) {
            try {
                stream.read();
                Assert.fail("expected project IOException");
            } catch (com.tingfeng.util.java.base.lang.exception.IOException expected) {
                // 预期异常
            }
        }
    }
}
