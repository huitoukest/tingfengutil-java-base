package com.tingfeng.util.java.base.io;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * StreamOps 冒烟测试
 * 验证拆分后实现归属：IOUtils 门面委托的方法逻辑已迁移至本类，行为与拆分前一致
 */
public class StreamOpsTest {

    private static final String TEST_CONTENT = "Hello, World! 你好，世界！";

    @Test
    public void testToInputStreamFromString() {
        InputStream is = StreamOps.toInputStream(TEST_CONTENT);
        Assert.assertNotNull(is);
        Assert.assertEquals(TEST_CONTENT, StreamOps.toString(is));
    }

    @Test
    public void testToInputStreamFromStringNull() {
        InputStream is = StreamOps.toInputStream((String) null, StandardCharsets.UTF_8);
        Assert.assertNotNull(is);
        Assert.assertEquals(0, StreamOps.toByteArray(is).length);
    }

    @Test
    public void testToInputStreamFromBytes() {
        byte[] bytes = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        InputStream is = StreamOps.toInputStream(bytes);
        Assert.assertNotNull(is);
        Assert.assertEquals(TEST_CONTENT, StreamOps.toString(is));
    }

    @Test
    public void testToInputStreamFromCharSequence() {
        StringBuilder sb = new StringBuilder(TEST_CONTENT);
        InputStream is = StreamOps.toInputStream(sb);
        Assert.assertNotNull(is);
        Assert.assertEquals(TEST_CONTENT, StreamOps.toString(is));
    }

    @Test
    public void testToInputStreamFromList() {
        List<String> lines = new ArrayList<>();
        lines.add("line1");
        lines.add("line2");
        InputStream is = StreamOps.toInputStream(lines);
        Assert.assertNotNull(is);
        Assert.assertTrue(StreamOps.toString(is).contains("line1"));
    }

    @Test
    public void testCreateByteArrayOutputStream() {
        ByteArrayOutputStream baos = StreamOps.createByteArrayOutputStream();
        Assert.assertNotNull(baos);
    }

    @Test
    public void testToByteArray() {
        InputStream is = StreamOps.toInputStream(TEST_CONTENT);
        byte[] bytes = StreamOps.toByteArray(is);
        Assert.assertEquals(TEST_CONTENT, new String(bytes, StandardCharsets.UTF_8));
    }

    @Test
    public void testToByteArrayNullInput() {
        byte[] bytes = StreamOps.toByteArray((InputStream) null);
        Assert.assertNotNull(bytes);
        Assert.assertEquals(0, bytes.length);
    }

    @Test
    public void testToStringNullInput() {
        Assert.assertNull(StreamOps.toString((InputStream) null));
    }

    @Test
    public void testToReaderAndToWriter() {
        Reader reader = StreamOps.toReader(StreamOps.toInputStream(TEST_CONTENT), StandardCharsets.UTF_8);
        Assert.assertNotNull(reader);
        StreamOps.closeQuietly(reader);

        Writer writer = StreamOps.toWriter(new ByteArrayOutputStream());
        Assert.assertNotNull(writer);
        StreamOps.closeQuietly(writer);
    }

    @Test
    public void testToStringFromReader() {
        String result = StreamOps.toString(new StringReader(TEST_CONTENT));
        Assert.assertEquals(TEST_CONTENT, result);
    }

    @Test
    public void testToByteArrayFromReader() {
        byte[] bytes = StreamOps.toByteArray(new StringReader(TEST_CONTENT), StandardCharsets.UTF_8);
        Assert.assertEquals(TEST_CONTENT, new String(bytes, StandardCharsets.UTF_8));
    }

    @Test
    public void testReadLines() {
        InputStream is = StreamOps.toInputStream("line1\nline2\nline3");
        List<String> lines = new ArrayList<>();
        StreamOps.readLines(is, lines::add);
        Assert.assertEquals(3, lines.size());
        Assert.assertEquals("line1", lines.get(0));
    }

    @Test
    public void testReadLinesNullInput() {
        List<String> lines = new ArrayList<>();
        StreamOps.readLines(null, lines::add);
        Assert.assertTrue(lines.isEmpty());
    }

    @Test
    public void testReadLinesToList() {
        InputStream is = StreamOps.toInputStream("line1\nline2");
        List<String> lines = StreamOps.readLinesToList(is);
        Assert.assertEquals(2, lines.size());
    }

    @Test
    public void testCloseQuietly() {
        InputStream is = StreamOps.toInputStream(TEST_CONTENT);
        StreamOps.closeQuietly((Closeable) is);
        StreamOps.closeQuietly((Closeable) null);
    }

    // ==================== 便捷方法测试（SubStory-2） ====================

    @Test
    public void testToByteArrayFromReaderSingleArg() {
        byte[] bytes = StreamOps.toByteArray(new StringReader(TEST_CONTENT));
        Assert.assertEquals(TEST_CONTENT, new String(bytes, StandardCharsets.UTF_8));
    }

    @Test
    public void testToByteArrayFromFile() throws Exception {
        File tempFile = File.createTempFile("streamops_file_", ".tmp");
        tempFile.deleteOnExit();
        StreamWriteOps.writeToFile(TEST_CONTENT, tempFile);
        byte[] bytes = StreamOps.toByteArray(tempFile);
        Assert.assertEquals(TEST_CONTENT, new String(bytes, StandardCharsets.UTF_8));
    }

    @Test
    public void testToByteArrayFromPath() throws Exception {
        Path tempPath = Files.createTempFile("streamops_path_", ".tmp");
        Files.deleteIfExists(tempPath);
        StreamWriteOps.writeToFile(TEST_CONTENT, tempPath);
        byte[] bytes = StreamOps.toByteArray(tempPath);
        Assert.assertEquals(TEST_CONTENT, new String(bytes, StandardCharsets.UTF_8));
        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testToByteArrayFromFileNull() {
        try {
            StreamOps.toByteArray((File) null);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testToByteArrayFromPathNull() {
        try {
            StreamOps.toByteArray((Path) null);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testToByteArrayFromURL() throws Exception {
        File tempFile = File.createTempFile("streamops_url_", ".tmp");
        tempFile.deleteOnExit();
        StreamWriteOps.writeToFile(TEST_CONTENT, tempFile);
        byte[] bytes = StreamOps.toByteArray(tempFile.toURI().toURL());
        Assert.assertEquals(TEST_CONTENT, new String(bytes, StandardCharsets.UTF_8));
    }

    @Test
    public void testToByteArrayFromURI() throws Exception {
        File tempFile = File.createTempFile("streamops_uri_", ".tmp");
        tempFile.deleteOnExit();
        StreamWriteOps.writeToFile(TEST_CONTENT, tempFile);
        byte[] bytes = StreamOps.toByteArray(tempFile.toURI());
        Assert.assertEquals(TEST_CONTENT, new String(bytes, StandardCharsets.UTF_8));
    }

    @Test
    public void testToByteArrayFromURLNull() {
        try {
            StreamOps.toByteArray((java.net.URL) null);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testIsEmptyEmpty() {
        Assert.assertTrue(StreamOps.isEmpty(new ByteArrayInputStream(new byte[0])));
    }

    @Test
    public void testIsEmptyNotEmpty() {
        Assert.assertFalse(StreamOps.isEmpty(new ByteArrayInputStream(new byte[]{1})));
    }

    @Test
    public void testIsEmptyNull() {
        Assert.assertTrue(StreamOps.isEmpty(null));
    }

    @Test
    public void testIsEmptyNoConsume() {
        byte[] data = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        Assert.assertFalse(StreamOps.isEmpty(is));
        // 无损探测：原始流继续读取内容完整
        Assert.assertEquals(TEST_CONTENT, StreamOps.toString(is));
    }

    @Test
    public void testContentEqualsEqual() {
        Assert.assertTrue(StreamOps.contentEquals(
            new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)),
            new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void testContentEqualsDifferent() {
        Assert.assertFalse(StreamOps.contentEquals(
            new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)),
            new ByteArrayInputStream("abd".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void testContentEqualsNull() {
        Assert.assertFalse(StreamOps.contentEquals(null, new ByteArrayInputStream(new byte[0])));
        Assert.assertFalse(StreamOps.contentEquals(new ByteArrayInputStream(new byte[0]), null));
    }

    @Test
    public void testContentEqualsEmptyVsNonEmpty() {
        Assert.assertFalse(StreamOps.contentEquals(
            new ByteArrayInputStream(new byte[0]),
            new ByteArrayInputStream(new byte[]{1})));
    }

    @Test
    public void testContentEqualsMultiBlock() {
        byte[] big1 = new byte[10000];
        byte[] big2 = new byte[10000];
        Random random = new Random(42);
        random.nextBytes(big1);
        System.arraycopy(big1, 0, big2, 0, big1.length);
        Assert.assertTrue(StreamOps.contentEquals(new ByteArrayInputStream(big1), new ByteArrayInputStream(big2)));
        big2[9999] = (byte) (big2[9999] ^ 1);
        Assert.assertFalse(StreamOps.contentEquals(new ByteArrayInputStream(big1), new ByteArrayInputStream(big2)));
    }

    @Test
    public void testSkipFully() {
        InputStream is = StreamOps.toInputStream("HelloWorld");
        long skipped = StreamOps.skipFully(is, 5);
        Assert.assertEquals(5, skipped);
        Assert.assertEquals("World", StreamOps.toString(is));
    }

    @Test
    public void testSkipFullyMoreThanAvailable() {
        try {
            StreamOps.skipFully(StreamOps.toInputStream("abc"), 10);
            Assert.fail("expected IOException");
        } catch (com.tingfeng.util.java.base.lang.exception.IOException expected) {
        }
    }

    @Test
    public void testSkipFullyZeroOrNegative() {
        InputStream is = StreamOps.toInputStream("abc");
        Assert.assertEquals(0, StreamOps.skipFully(is, 0));
        Assert.assertEquals(0, StreamOps.skipFully(is, -5));
        Assert.assertEquals("abc", StreamOps.toString(is));
    }

    @Test
    public void testSkipFullyZeroSkipStream() {
        // skip() 恒返回 0 的流：read-discard 逐字节回退，防死循环
        byte[] data = "HelloWorld".getBytes(StandardCharsets.UTF_8);
        ZeroSkipInputStream is = new ZeroSkipInputStream(data);
        long skipped = StreamOps.skipFully(is, 5);
        Assert.assertEquals(5, skipped);
        Assert.assertEquals("World", StreamOps.toString(is));
    }

    @Test
    public void testSkipFullyZeroSkipStreamEof() {
        try {
            StreamOps.skipFully(new ZeroSkipInputStream("abc".getBytes(StandardCharsets.UTF_8)), 10);
            Assert.fail("expected IOException");
        } catch (com.tingfeng.util.java.base.lang.exception.IOException expected) {
        }
    }

    @Test
    public void testReadLinesAppend() {
        InputStream is = StreamOps.toInputStream("line1\nline2\nline3");
        List<String> lines = new ArrayList<>();
        lines.add("prefix");
        StreamOps.readLines(is, StandardCharsets.UTF_8, lines);
        Assert.assertEquals(4, lines.size());
        Assert.assertEquals("prefix", lines.get(0));
        Assert.assertEquals("line3", lines.get(3));
    }

    @Test
    public void testReadLinesAppendNullList() {
        InputStream is = StreamOps.toInputStream("line1\nline2");
        StreamOps.readLines(is, StandardCharsets.UTF_8, (List<String>) null);
    }

    @Test
    public void testReadLinesAppendClosesInput() {
        TrackingInputStream is = new TrackingInputStream("line1\nline2".getBytes(StandardCharsets.UTF_8));
        List<String> lines = new ArrayList<>();
        StreamOps.readLines(is, StandardCharsets.UTF_8, lines);
        Assert.assertEquals(2, lines.size());
        Assert.assertTrue(is.closed);
    }

    @Test
    public void testReadNBytes() {
        InputStream is = StreamOps.toInputStream("Hello World");
        Assert.assertEquals("Hello", new String(StreamOps.readNBytes(is, 5), StandardCharsets.UTF_8));
        // 剩余读取
        Assert.assertEquals(" World", new String(StreamOps.readNBytes(is, 100), StandardCharsets.UTF_8));
    }

    @Test
    public void testReadNBytesLessThanAvailable() {
        InputStream is = StreamOps.toInputStream("abc");
        byte[] bytes = StreamOps.readNBytes(is, 10);
        Assert.assertEquals("abc", new String(bytes, StandardCharsets.UTF_8));
    }

    @Test
    public void testReadNBytesZeroOrNegative() {
        Assert.assertEquals(0, StreamOps.readNBytes(StreamOps.toInputStream("abc"), 0).length);
        Assert.assertEquals(0, StreamOps.readNBytes(StreamOps.toInputStream("abc"), -1).length);
    }

    @Test
    public void testReadNBytesNull() {
        Assert.assertEquals(0, StreamOps.readNBytes(null, 5).length);
    }

    @Test
    public void testLineIterator() {
        List<String> result = new ArrayList<>();
        try (StreamOps.LineIterator it = StreamOps.lineIterator(StreamOps.toInputStream("a\nb\nc"), StandardCharsets.UTF_8)) {
            while (it.hasNext()) {
                result.add(it.next());
            }
        }
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("a", result.get(0));
        Assert.assertEquals("c", result.get(2));
    }

    @Test
    public void testLineIteratorClose() {
        TrackingInputStream is = new TrackingInputStream("line1\nline2".getBytes(StandardCharsets.UTF_8));
        StreamOps.LineIterator it = StreamOps.lineIterator(is, StandardCharsets.UTF_8);
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("line1", it.next());
        it.close();
        Assert.assertTrue(is.closed);
    }

    @Test
    public void testLineIteratorNoSuchElement() {
        StreamOps.LineIterator it = StreamOps.lineIterator(StreamOps.toInputStream("only"), StandardCharsets.UTF_8);
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("only", it.next());
        Assert.assertFalse(it.hasNext());
        try {
            it.next();
            Assert.fail("expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }
        it.close();
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

    /**
     * skip() 恒返回 0 的输入流，用于验证 read-discard 回退
     */
    private static class ZeroSkipInputStream extends ByteArrayInputStream {

        ZeroSkipInputStream(byte[] buf) {
            super(buf);
        }

        @Override
        public long skip(long n) {
            return 0;
        }
    }
}
