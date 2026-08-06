package com.tingfeng.util.java.base.io;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

/**
 * StreamTransferOps 冒烟测试
 * 验证拆分后实现归属：IOUtils 门面委托的方法逻辑已迁移至本类，行为与拆分前一致
 */
public class StreamTransferOpsTest {

    private static final String TEST_CONTENT = "Hello, World! 你好，世界！";

    @Test
    public void testCopy() throws Exception {
        InputStream is = StreamOps.toInputStream(TEST_CONTENT);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamTransferOps.copy(os, is);
        Assert.assertEquals(TEST_CONTENT, os.toString("UTF-8"));
    }

    @Test
    public void testCopyWithCloseStream() throws Exception {
        InputStream is = StreamOps.toInputStream(TEST_CONTENT);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamTransferOps.copy(os, is, true);
        Assert.assertEquals(TEST_CONTENT, os.toString("UTF-8"));
    }

    @Test
    public void testCopyWithCallback() {
        InputStream is = StreamOps.toInputStream(TEST_CONTENT);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        AtomicLong total = new AtomicLong(0);
        StreamTransferOps.copy(os, is, total::set);
        Assert.assertTrue(total.get() > 0);
    }

    @Test
    public void testPipe() throws Exception {
        InputStream is = StreamOps.toInputStream(TEST_CONTENT);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamTransferOps.pipe(os, is);
        Assert.assertEquals(TEST_CONTENT, os.toString("UTF-8"));
    }

    @Test
    public void testPipeReaderWriter() throws Exception {
        Reader reader = new StringReader(TEST_CONTENT);
        Writer writer = new StringWriter();
        StreamTransferOps.pipe(writer, reader);
        Assert.assertEquals(TEST_CONTENT, writer.toString());
    }

    @Test
    public void testJoinStreams() throws Exception {
        InputStream is1 = StreamOps.toInputStream("Hello");
        InputStream is2 = StreamOps.toInputStream(", World!");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamTransferOps.joinStreams(os, is1, is2);
        Assert.assertEquals("Hello, World!", os.toString("UTF-8"));
    }

    @Test
    public void testJoinStreamsWithNull() throws Exception {
        InputStream is1 = StreamOps.toInputStream("Hello");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamTransferOps.joinStreams(os, is1, null);
        Assert.assertEquals("Hello", os.toString("UTF-8"));
    }

    // ==================== 跨类型拷贝测试（SubStory-2） ====================

    @Test
    public void testCopyWriterReader() throws Exception {
        StringWriter writer = new StringWriter();
        long count = StreamTransferOps.copy(writer, new StringReader(TEST_CONTENT));
        Assert.assertEquals(TEST_CONTENT, writer.toString());
        Assert.assertEquals(TEST_CONTENT.length(), count);
    }

    @Test
    public void testCopyWriterReaderNull() {
        try {
            StreamTransferOps.copy((Writer) null, new StringReader("x"));
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        try {
            StreamTransferOps.copy(new StringWriter(), (Reader) null);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testCopyOutputStreamReaderCharset() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        long count = StreamTransferOps.copy(output, new StringReader(TEST_CONTENT), StandardCharsets.UTF_8);
        Assert.assertEquals(TEST_CONTENT, output.toString("UTF-8"));
        Assert.assertEquals(TEST_CONTENT.length(), count);
    }

    @Test
    public void testCopyWriterInputStreamCharset() throws Exception {
        StringWriter writer = new StringWriter();
        byte[] bytes = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        long count = StreamTransferOps.copy(writer, new ByteArrayInputStream(bytes), StandardCharsets.UTF_8);
        Assert.assertEquals(TEST_CONTENT, writer.toString());
        // 返回解码后的字符数（与 Reader → Writer 版本统计口径一致）
        Assert.assertEquals(TEST_CONTENT.length(), count);
    }

    @Test
    public void testCopyCrossTypeNull() {
        try {
            StreamTransferOps.copy((OutputStream) null, new StringReader("x"), StandardCharsets.UTF_8);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        try {
            StreamTransferOps.copy(new StringWriter(), (InputStream) null, StandardCharsets.UTF_8);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testCopyCrossTypeDoesNotClose() throws Exception {
        TrackingOutputStream output = new TrackingOutputStream();
        StreamTransferOps.copy(output, new StringReader(TEST_CONTENT), StandardCharsets.UTF_8);
        Assert.assertFalse(output.closed);

        TrackingInputStream input = new TrackingInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8));
        StringWriter writer = new StringWriter();
        StreamTransferOps.copy(writer, input, StandardCharsets.UTF_8);
        Assert.assertFalse(input.closed);
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
