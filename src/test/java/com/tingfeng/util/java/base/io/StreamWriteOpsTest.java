package com.tingfeng.util.java.base.io;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * StreamWriteOps 冒烟测试
 * 验证拆分后实现归属：IOUtils 门面委托的方法逻辑已迁移至本类，行为与拆分前一致
 */
public class StreamWriteOpsTest {

    private static final String TEST_CONTENT = "Hello, World! 你好，世界！";

    @Test
    public void testWriteToFileFromInputStream() throws Exception {
        File tempFile = File.createTempFile("streamwrite_test_", ".tmp");
        tempFile.deleteOnExit();

        InputStream is = StreamOps.toInputStream(TEST_CONTENT);
        StreamWriteOps.writeToFile(is, tempFile);

        Assert.assertEquals(TEST_CONTENT, StreamOps.toString(StreamOps.toInputStream(tempFile)));
    }

    @Test
    public void testWriteToFileFromInputStreamToPath() throws Exception {
        Path tempPath = Files.createTempFile("streamwrite_test_", ".tmp");
        Files.deleteIfExists(tempPath);

        InputStream is = StreamOps.toInputStream(TEST_CONTENT);
        StreamWriteOps.writeToFile(is, tempPath);

        Assert.assertEquals(TEST_CONTENT, StreamOps.toString(StreamOps.toInputStream(tempPath)));
        Files.deleteIfExists(tempPath);
    }

    // ==================== InputStream 源 append 测试（SubStory-4） ====================

    @Test
    public void testWriteToFileAppendFile() throws Exception {
        File tempFile = File.createTempFile("streamwrite_append_", ".tmp");
        tempFile.deleteOnExit();

        StreamWriteOps.writeToFile(TEST_CONTENT, tempFile);
        StreamWriteOps.writeToFile(StreamOps.toInputStream(TEST_CONTENT), tempFile, true);

        Assert.assertEquals(TEST_CONTENT + TEST_CONTENT, StreamOps.toString(StreamOps.toInputStream(tempFile)));
    }

    @Test
    public void testWriteToFileAppendPath() throws Exception {
        Path tempPath = Files.createTempFile("streamwrite_append_", ".tmp");
        Files.deleteIfExists(tempPath);

        StreamWriteOps.writeToFile(TEST_CONTENT, tempPath);
        StreamWriteOps.writeToFile(StreamOps.toInputStream(TEST_CONTENT), tempPath, true);

        Assert.assertEquals(TEST_CONTENT + TEST_CONTENT, StreamOps.toString(StreamOps.toInputStream(tempPath)));
        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testWriteToFileAppendFalseOverwrites() throws Exception {
        File tempFile = File.createTempFile("streamwrite_append_", ".tmp");
        tempFile.deleteOnExit();
        StreamWriteOps.writeToFile("old content", tempFile);
        StreamWriteOps.writeToFile(StreamOps.toInputStream(TEST_CONTENT), tempFile, false);
        Assert.assertEquals(TEST_CONTENT, StreamOps.toString(StreamOps.toInputStream(tempFile)));

        Path tempPath = Files.createTempFile("streamwrite_append_", ".tmp");
        Files.deleteIfExists(tempPath);
        StreamWriteOps.writeToFile("old content", tempPath);
        StreamWriteOps.writeToFile(StreamOps.toInputStream(TEST_CONTENT), tempPath, false);
        Assert.assertEquals(TEST_CONTENT, StreamOps.toString(StreamOps.toInputStream(tempPath)));
        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testWriteToFileFromByteArray() throws Exception {
        File tempFile = File.createTempFile("streamwrite_test_", ".tmp");
        tempFile.deleteOnExit();

        byte[] data = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        StreamWriteOps.writeToFile(data, tempFile);

        Assert.assertEquals(TEST_CONTENT, StreamOps.toString(StreamOps.toInputStream(tempFile)));
    }

    @Test
    public void testWriteToFileFromString() throws Exception {
        File tempFile = File.createTempFile("streamwrite_test_", ".tmp");
        tempFile.deleteOnExit();

        StreamWriteOps.writeToFile(TEST_CONTENT, tempFile);

        Assert.assertEquals(TEST_CONTENT, StreamOps.toString(StreamOps.toInputStream(tempFile)));
    }

    @Test
    public void testWriteToFileFromStringToPath() throws Exception {
        Path tempPath = Files.createTempFile("streamwrite_test_", ".tmp");
        Files.deleteIfExists(tempPath);

        StreamWriteOps.writeToFile(TEST_CONTENT, tempPath);

        Assert.assertEquals(TEST_CONTENT, StreamOps.toString(StreamOps.toInputStream(tempPath)));
        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testWriteToFileFromList() throws Exception {
        File tempFile = File.createTempFile("streamwrite_test_", ".tmp");
        tempFile.deleteOnExit();

        List<String> lines = new ArrayList<>();
        lines.add("line1");
        lines.add("line2");
        lines.add("line3");
        StreamWriteOps.writeToFile(lines, tempFile);

        List<String> result = StreamOps.readLinesToList(StreamOps.toInputStream(tempFile));
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("line1", result.get(0));
    }

    @Test
    public void testWriteToFileFromListToPath() throws Exception {
        Path tempPath = Files.createTempFile("streamwrite_test_", ".tmp");
        Files.deleteIfExists(tempPath);

        List<String> lines = new ArrayList<>();
        lines.add("line1");
        lines.add("line2");
        StreamWriteOps.writeToFile(lines, tempPath);

        List<String> result = StreamOps.readLinesToList(StreamOps.toInputStream(tempPath));
        Assert.assertEquals(2, result.size());
        Files.deleteIfExists(tempPath);
    }

    // ==================== 便捷写入测试（SubStory-2） ====================

    @Test
    public void testWriteString() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        StreamWriteOps.write(TEST_CONTENT, output);
        Assert.assertEquals(TEST_CONTENT, output.toString("UTF-8"));
    }

    @Test
    public void testWriteStringCharset() {
        Charset gbk = Charset.forName("GBK");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        StreamWriteOps.write(TEST_CONTENT, output, gbk);
        Assert.assertEquals(TEST_CONTENT, new String(output.toByteArray(), gbk));
    }

    @Test
    public void testWriteBytes() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        StreamWriteOps.write(TEST_CONTENT.getBytes(StandardCharsets.UTF_8), output);
        Assert.assertEquals(TEST_CONTENT, output.toString("UTF-8"));
    }

    @Test
    public void testWriteNullContent() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        StreamWriteOps.write((String) null, output);
        Assert.assertEquals(0, output.size());
        StreamWriteOps.write((byte[]) null, output);
        Assert.assertEquals(0, output.size());
    }

    @Test
    public void testWriteNullOutput() {
        try {
            StreamWriteOps.write("content", (OutputStream) null);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        try {
            StreamWriteOps.write("content", (OutputStream) null, StandardCharsets.UTF_8);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        try {
            StreamWriteOps.write(new byte[]{1}, (OutputStream) null);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testWriteDoesNotCloseStream() throws Exception {
        TrackingOutputStream output = new TrackingOutputStream();
        StreamWriteOps.write(TEST_CONTENT, output);
        Assert.assertFalse(output.closed);
        Assert.assertEquals(TEST_CONTENT, output.toString("UTF-8"));
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
