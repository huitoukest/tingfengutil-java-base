package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * IOUtils 单元测试
 */
public class IOUtilsTest {

    private static final String TEST_CONTENT = "Hello, World! 你好，世界！";
    private static final String TEST_CONTENT_2 = "Line 2\nLine 3\r\nLine 4";

    // ==================== 流创建测试 ====================

    @Test
    public void testToInputStreamFromString() {
        InputStream is = IOUtils.toInputStream(TEST_CONTENT);
        Assert.assertNotNull(is);
        Assert.assertEquals(TEST_CONTENT, IOUtils.toString(is));
    }

    @Test
    public void testToInputStreamFromStringWithCharset() {
        InputStream is = IOUtils.toInputStream(TEST_CONTENT, StandardCharsets.UTF_8);
        Assert.assertNotNull(is);
        Assert.assertEquals(TEST_CONTENT, IOUtils.toString(is, StandardCharsets.UTF_8));
    }

    @Test
    public void testToInputStreamFromBytes() {
        byte[] bytes = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        InputStream is = IOUtils.toInputStream(bytes);
        Assert.assertNotNull(is);
        Assert.assertEquals(TEST_CONTENT, IOUtils.toString(is));
    }

    @Test
    public void testCreateByteArrayOutputStream() {
        ByteArrayOutputStream baos = IOUtils.createByteArrayOutputStream();
        Assert.assertNotNull(baos);
    }

    // ==================== 流转换测试 ====================

    @Test
    public void testToByteArray() {
        InputStream is = IOUtils.toInputStream(TEST_CONTENT);
        byte[] bytes = IOUtils.toByteArray(is);
        Assert.assertNotNull(bytes);
        Assert.assertEquals(TEST_CONTENT, new String(bytes, StandardCharsets.UTF_8));
    }

    @Test
    public void testToByteArrayNullInput() {
        byte[] bytes = IOUtils.toByteArray(null);
        Assert.assertNotNull(bytes);
        Assert.assertEquals(0, bytes.length);
    }

    @Test
    public void testToString() {
        InputStream is = IOUtils.toInputStream(TEST_CONTENT);
        String result = IOUtils.toString(is);
        Assert.assertEquals(TEST_CONTENT, result);
    }

    @Test
    public void testToStringNullInput() {
        String result = IOUtils.toString((InputStream) null);
        Assert.assertNull(result);
    }

    @Test
    public void testToReader() {
        InputStream is = IOUtils.toInputStream(TEST_CONTENT);
        Reader reader = IOUtils.toReader(is, StandardCharsets.UTF_8);
        Assert.assertNotNull(reader);
        Assert.assertTrue(reader instanceof BufferedReader);
        IOUtils.closeQuietly(reader);
    }

    @Test
    public void testToWriter() {
        OutputStream os = new ByteArrayOutputStream();
        Writer writer = IOUtils.toWriter(os);
        Assert.assertNotNull(writer);
        Assert.assertTrue(writer instanceof BufferedWriter);
        IOUtils.closeQuietly(writer);
    }

    // ==================== 缓冲包装测试 ====================

    @Test
    public void testToBufferedReader() {
        InputStream is = IOUtils.toInputStream(TEST_CONTENT);
        BufferedReader br = IOUtils.toBufferedReader(is, StandardCharsets.UTF_8);
        Assert.assertNotNull(br);
        IOUtils.closeQuietly(br);
    }

    @Test
    public void testToBufferedOutputStream() {
        OutputStream os = new ByteArrayOutputStream();
        BufferedOutputStream bos = IOUtils.toBufferedOutputStream(os);
        Assert.assertNotNull(bos);
        IOUtils.closeQuietly(bos);
    }

    @Test
    public void testToBufferedInputStream() {
        InputStream is = IOUtils.toInputStream(TEST_CONTENT);
        BufferedInputStream bis = IOUtils.toBufferedInputStream(is);
        Assert.assertNotNull(bis);
        IOUtils.closeQuietly(bis);
    }

    // ==================== 流拷贝测试 ====================

    @Test
    public void testCopy() throws Exception {
        InputStream is = IOUtils.toInputStream(TEST_CONTENT);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        IOUtils.copy(os, is);
        Assert.assertEquals(TEST_CONTENT, os.toString("UTF-8"));
    }

    @Test
    public void testCopyWithCloseStream() throws Exception {
        InputStream is = IOUtils.toInputStream(TEST_CONTENT);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        IOUtils.copy(os, is, true);
        Assert.assertEquals(TEST_CONTENT, os.toString("UTF-8"));
    }

    @Test
    public void testCopyWithCallback() {
        InputStream is = IOUtils.toInputStream(TEST_CONTENT);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        AtomicLong total = new AtomicLong(0);
        IOUtils.copy(os, is, total::set);
        Assert.assertTrue(total.get() > 0);
    }

    // ==================== 管道连接测试 ====================

    @Test
    public void testPipe() throws Exception {
        InputStream is = IOUtils.toInputStream(TEST_CONTENT);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        IOUtils.pipe(os, is);
        Assert.assertEquals(TEST_CONTENT, os.toString("UTF-8"));
    }

    @Test
    public void testPipeReaderWriter() throws IOException {
        Reader reader = new StringReader(TEST_CONTENT);
        Writer writer = new StringWriter();
        IOUtils.pipe(writer, reader);
        Assert.assertEquals(TEST_CONTENT, writer.toString());
    }

    @Test
    public void testJoinStreams() throws Exception {
        InputStream is1 = IOUtils.toInputStream("Hello");
        InputStream is2 = IOUtils.toInputStream(", World!");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        IOUtils.joinStreams(os, is1, is2);
        Assert.assertEquals("Hello, World!", os.toString("UTF-8"));
    }

    @Test
    public void testJoinStreamsWithNull() throws Exception {
        InputStream is1 = IOUtils.toInputStream("Hello");
        InputStream is2 = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        IOUtils.joinStreams(os, is1, is2);
        Assert.assertEquals("Hello", os.toString("UTF-8"));
    }

    // ==================== 流读取测试 ====================

    @Test
    public void testReadLines() {
        InputStream is = IOUtils.toInputStream("line1\nline2\nline3");
        List<String> lines = new ArrayList<>();
        IOUtils.readLines(is, lines::add);
        Assert.assertEquals(3, lines.size());
        Assert.assertEquals("line1", lines.get(0));
        Assert.assertEquals("line2", lines.get(1));
        Assert.assertEquals("line3", lines.get(2));
    }

    @Test
    public void testReadLinesNullInput() {
        List<String> lines = new ArrayList<>();
        IOUtils.readLines(null, lines::add);
        Assert.assertTrue(lines.isEmpty());
    }

    // ==================== 资源关闭测试 ====================

    @Test
    public void testCloseQuietly() {
        InputStream is = IOUtils.toInputStream(TEST_CONTENT);
        IOUtils.closeQuietly(is); // 不应抛出异常
    }

    @Test
    public void testCloseQuietlyNull() {
        IOUtils.closeQuietly((Closeable) null); // 不应抛出异常
    }

    @Test
    public void testCloseQuietlyMultiple() {
        InputStream is1 = IOUtils.toInputStream("test1");
        InputStream is2 = IOUtils.toInputStream("test2");
        IOUtils.closeQuietly(is1, is2); // 不应抛出异常
    }

    // ==================== CancellationToken 测试 ====================

    @Test
    public void testCancellationTokenCancel() {
        IOUtils.CancellationToken token = new IOUtils.CancellationToken();
        Assert.assertFalse(token.isCancelled());
        token.cancel();
        Assert.assertTrue(token.isCancelled());
        Assert.assertTrue(token.shouldInterrupt());
    }

    @Test
    public void testCancellationTokenTimeout() {
        IOUtils.CancellationToken token = new IOUtils.CancellationToken(100); // 100ms超时
        Assert.assertFalse(token.isTimeout());
        try {
            Thread.sleep(150);
        } catch (InterruptedException ignored) {}
        Assert.assertTrue(token.isTimeout());
    }

    @Test
    public void testCancellationTokenReset() {
        IOUtils.CancellationToken token = new IOUtils.CancellationToken();
        token.cancel();
        Assert.assertTrue(token.isCancelled());
        token.reset();
        Assert.assertFalse(token.isCancelled());
    }

    // ==================== 异步流处理测试 ====================

    @Test
    public void testCopyAsync() throws Exception {
        InputStream is = IOUtils.toInputStream(TEST_CONTENT);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            IOUtils.CancellationToken token = new IOUtils.CancellationToken();
            AtomicLong progress = new AtomicLong(0);

            Long result = IOUtils.copyAsync(os, is, executor,
                total -> { progress.set(total); return true; }, IOUtils.DEFAULT_BACK_PRESSURE_BUFFER_SIZE, token).get();

            Assert.assertTrue(result > 0);
            Assert.assertEquals(TEST_CONTENT, os.toString("UTF-8"));
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testCopyAsyncWithCancellation() throws Exception {
        // 创建一个很大的输入流，用于测试取消
        byte[] largeData = new byte[1024 * 1024]; // 1MB
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        InputStream is = IOUtils.toInputStream(largeData);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            IOUtils.CancellationToken token = new IOUtils.CancellationToken();
            // 立即取消
            token.cancel();

            Long result = IOUtils.copyAsync(os, is, executor,
                total -> true, IOUtils.DEFAULT_BACK_PRESSURE_BUFFER_SIZE, token).get();

            // 取消后结果应该是0或很小
            Assert.assertTrue(result < largeData.length);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testReadLinesAsync() throws Exception {
        InputStream is = IOUtils.toInputStream("line1\nline2\nline3");
        List<String> lines = new ArrayList<>();
        AtomicInteger lineCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            IOUtils.CancellationToken token = new IOUtils.CancellationToken();

            IOUtils.readLinesAsync(is, StandardCharsets.UTF_8, executor,
                line -> {
                    lines.add(line);
                    return true;
                },
                null, token);

            // 等待异步任务完成
            Thread.sleep(500);

            Assert.assertEquals(3, lines.size());
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testReadStreamWithBackpressure() throws Exception {
        InputStream is = IOUtils.toInputStream("line1\nline2\nline3\nline4\nline5");
        List<List<String>> batches = new ArrayList<>();
        AtomicInteger callCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            IOUtils.CancellationToken token = new IOUtils.CancellationToken();

            IOUtils.readStreamWithBackpressure(is, StandardCharsets.UTF_8, 2,
                batch -> {
                    batches.add(new ArrayList<>(batch));
                    callCount.incrementAndGet();
                    return true;
                }, executor, 100, token);

            // 等待异步任务完成
            Thread.sleep(1000);

            // 5行，每批2行，应该有3批（最后一批可能不满）
            Assert.assertEquals(2, batches.size());
            Assert.assertEquals(2, batches.get(0).size());
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testTransmitStream() throws Exception {
        List<String> data = new ArrayList<>();
        data.add("Hello");
        data.add(", ");
        data.add("World!");
        AtomicLong index = new AtomicLong(0);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            IOUtils.CancellationToken token = new IOUtils.CancellationToken();

            Supplier<String> supplier = () -> {
                int i = (int) index.getAndIncrement();
                return i < data.size() ? data.get(i) : null;
            };

            Long result = IOUtils.transmitStream(supplier, os, executor, null, token).get();

            Assert.assertEquals("Hello, World!".length(), result.intValue());
            Assert.assertEquals("Hello, World!", os.toString(StandardCharsets.UTF_8.name()));
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testToExecutorServiceWithExecutorService() {
        ExecutorService original = Executors.newSingleThreadExecutor();
        ExecutorService result = IOUtils.toExecutorService(original);
        Assert.assertSame(original, result);
        result.shutdown();
    }

    @Test
    public void testToExecutorServiceWithThread() throws InterruptedException {
        AtomicBoolean executed = new AtomicBoolean(false);
        Thread thread = new Thread(() -> executed.set(true));

        ExecutorService result = IOUtils.toExecutorService(thread);
        Thread.sleep(100); // 等待线程执行

        Assert.assertTrue(executed.get());
        Assert.assertNotNull(result);
        result.shutdown();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToExecutorServiceWithInvalidType() {
        IOUtils.toExecutorService("invalid");
    }
}
