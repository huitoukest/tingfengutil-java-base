package com.tingfeng.util.java.base.io;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Test
    public void testCancellationTokenResetKeepTimeout() throws InterruptedException {
        IOUtils.CancellationToken token = new IOUtils.CancellationToken(100); // 100ms超时
        Assert.assertFalse(token.isTimeout());
        token.reset(); // 重置后超时配置应保留
        Assert.assertFalse(token.isTimeout()); // 重建截止时间点，重置瞬间未超时
        Thread.sleep(150);
        Assert.assertTrue(token.isTimeout()); // 原超时时长生效
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
                total -> { progress.set(total); return true; }, IOUtils.DEFAULT_BACK_PRESSURE_BUFFER_SIZE, token)
                .get();

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

            // 等待异步任务完成（5行，每批2行 → 3批：2+2+1）
            long deadline = System.currentTimeMillis() + 5000;
            while (callCount.get() < 3 && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }

            Assert.assertEquals(3, batches.size());
            Assert.assertEquals(2, batches.get(0).size());
            Assert.assertEquals(2, batches.get(1).size());
            Assert.assertEquals(1, batches.get(2).size());

            // 死循环根治验证：管道任务已退出，池线程可被复用
            CountDownLatch latch = new CountDownLatch(1);
            executor.execute(latch::countDown);
            Assert.assertTrue("管道任务未退出（存在死循环）", latch.await(2, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testCopyAsyncSingleThreadPoolNoDeadlock() throws Exception {
        // 单线程池 + 小背压上限 + 大数据量：队列满时消费者仍可 take 腾空（生产者=内部 daemon，不占池线程）
        byte[] largeData = new byte[1024 * 1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 251);
        }
        InputStream is = IOUtils.toInputStream(largeData);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Long result = IOUtils.copyAsync(os, is, executor, total -> true, 4096,
                new IOUtils.CancellationToken()).get(10, TimeUnit.SECONDS);
            Assert.assertEquals(largeData.length, result.longValue());
            Assert.assertArrayEquals(largeData, os.toByteArray());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testCopyAsyncCancellationConvergesWithin2s() throws Exception {
        byte[] largeData = new byte[1024 * 1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 251);
        }
        InputStream is = IOUtils.toInputStream(largeData);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            IOUtils.CancellationToken token = new IOUtils.CancellationToken();
            token.cancel();

            long start = System.currentTimeMillis();
            Long result = IOUtils.copyAsync(os, is, executor, total -> true, 4096, token)
                .get(2, TimeUnit.SECONDS);
            long elapsed = System.currentTimeMillis() - start;

            Assert.assertTrue("取消收敛耗时超过 2s: " + elapsed + "ms", elapsed < 2000);
            Assert.assertTrue(result < largeData.length);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testCopyAsyncBackpressurePause() throws Exception {
        byte[] largeData = new byte[512 * 1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 251);
        }
        InputStream is = IOUtils.toInputStream(largeData);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            IOUtils.CancellationToken token = new IOUtils.CancellationToken();
            AtomicInteger callbackCount = new AtomicInteger(0);

            CompletableFuture<Long> future = IOUtils.copyAsync(os, is, executor, total -> {
                callbackCount.incrementAndGet();
                return false;
            }, 4096, token);

            // 等待回调至少执行一次（确认管道已启动并进入暂停）
            long deadline = System.currentTimeMillis() + 3000;
            while (callbackCount.get() < 1 && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            Assert.assertTrue("进度回调未执行", callbackCount.get() >= 1);

            // 暂停中：future 不应完成（真背压，生产者阻塞在队列满）
            Thread.sleep(300);
            Assert.assertFalse("暂停未生效，future 提前完成", future.isDone());

            // 取消 → 收敛退出
            token.cancel();
            Long result = future.get(2, TimeUnit.SECONDS);
            Assert.assertTrue("取消后应只处理暂停前已写入的少量数据", result < largeData.length);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testCopyAsyncProducerErrorPropagation() throws Exception {
        InputStream failing = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("simulated read failure");
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new IOException("simulated read failure");
            }
        };
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            CompletableFuture<Long> future = IOUtils.copyAsync(os, failing, executor, total -> true, 4096,
                new IOUtils.CancellationToken());
            try {
                future.get(5, TimeUnit.SECONDS);
                Assert.fail("预期生产者读异常应传播到 future");
            } catch (ExecutionException e) {
                Assert.assertTrue("异常类型不匹配: " + e.getCause(),
                    e.getCause() instanceof com.tingfeng.util.java.base.lang.exception.IOException);
            }
        } finally {
            executor.shutdownNow();
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

            // 非 ASCII 内容：total 统计真实写出字节数（UTF-8，"你好" = 6 字节）
            ByteArrayOutputStream nonAsciiOs = new ByteArrayOutputStream();
            AtomicLong nonAsciiIndex = new AtomicLong(0);
            Supplier<String> nonAsciiSupplier = () -> nonAsciiIndex.getAndIncrement() == 0 ? "你好" : null;
            Long nonAsciiResult = IOUtils.transmitStream(nonAsciiSupplier, nonAsciiOs, executor, null, token).get();
            Assert.assertEquals(6, nonAsciiResult.intValue());
            Assert.assertEquals("你好", nonAsciiOs.toString(StandardCharsets.UTF_8.name()));
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
        Thread.sleep(100); // 语义修正后 Thread 不再被启动执行

        Assert.assertFalse("Thread 对象不应再被启动执行", executed.get());
        Assert.assertNotNull(result);
        result.shutdown();
        Assert.assertTrue(result.isShutdown());
        Assert.assertTrue("自建池 shutdown 后应能正常终止", result.awaitTermination(2, TimeUnit.SECONDS));
    }

    @Test
    public void testToExecutorServiceWithRunnable() throws InterruptedException {
        AtomicBoolean executed = new AtomicBoolean(false);

        ExecutorService result = IOUtils.toExecutorService((Runnable) () -> executed.set(true));
        Thread.sleep(100); // 语义修正后 Runnable 不再被启动执行

        Assert.assertFalse("Runnable 对象不应再被启动执行", executed.get());
        Assert.assertNotNull(result);
        result.shutdown();
    }

    @Test
    public void testToExecutorServiceSelfManagedDaemonAndShutdown() throws Exception {
        // 自建池：任务可正常执行、线程为 daemon、shutdown 后可终止（无泄漏）
        ExecutorService result = IOUtils.toExecutorService(new Thread(() -> {}));
        Assert.assertNotNull(result);

        AtomicBoolean daemon = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        result.execute(() -> {
            daemon.set(Thread.currentThread().isDaemon());
            latch.countDown();
        });
        Assert.assertTrue("自建池任务应正常执行", latch.await(2, TimeUnit.SECONDS));
        Assert.assertTrue("自建池线程应为 daemon", daemon.get());

        result.shutdown();
        Assert.assertTrue(result.isShutdown());
        Assert.assertTrue("shutdown 后应能正常终止", result.awaitTermination(2, TimeUnit.SECONDS));
    }

    @Test
    public void testAsyncMethodsShutdownSelfManagedPool() throws Exception {
        // 异步方法传 Thread 时：任务完成后自建池自动 shutdown（线程名 IOUtils-async-pool-* 不再存活）
        for (int i = 0; i < 3; i++) {
            InputStream is = IOUtils.toInputStream(TEST_CONTENT);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Long result = IOUtils.copyAsync(os, is, new Thread(() -> {}), total -> true,
                IOUtils.DEFAULT_BACK_PRESSURE_BUFFER_SIZE, new IOUtils.CancellationToken())
                .get(5, TimeUnit.SECONDS);
            Assert.assertTrue(result > 0);
        }
        long deadline = System.currentTimeMillis() + 3000;
        while (countPoolThreads() > 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        Assert.assertEquals("自建池线程应在任务完成后被 shutdown 回收", 0, countPoolThreads());
    }

    @Test
    public void testShutdownIfSelfManaged() throws Exception {
        // 外部传入的 ExecutorService：no-op，不应被关闭
        ExecutorService external = Executors.newSingleThreadExecutor();
        try {
            IOUtils.shutdownIfSelfManaged(external);
            Assert.assertFalse("外部传入的 ExecutorService 不应被关闭", external.isShutdown());
        } finally {
            external.shutdown();
        }

        // 自建池：shutdown 生效并正常终止
        ExecutorService selfManaged = IOUtils.toExecutorService(new Thread(() -> {}));
        IOUtils.shutdownIfSelfManaged(selfManaged);
        Assert.assertTrue("自建池应被 shutdown", selfManaged.isShutdown());
        Assert.assertTrue("自建池 shutdown 后应能正常终止", selfManaged.awaitTermination(2, TimeUnit.SECONDS));
    }

    private static int countPoolThreads() {
        int count = 0;
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.isAlive() && t.getName().startsWith("IOUtils-async-pool-")) {
                count++;
            }
        }
        return count;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToExecutorServiceWithInvalidType() {
        IOUtils.toExecutorService("invalid");
    }

    // ==================== 新增类型扩展测试 ====================

    @Test
    public void testToInputStreamFromCharSequence() {
        StringBuilder sb = new StringBuilder(TEST_CONTENT);
        InputStream is = IOUtils.toInputStream(sb);
        Assert.assertNotNull(is);
        Assert.assertEquals(TEST_CONTENT, IOUtils.toString(is));
    }

    @Test
    public void testToInputStreamFromCharSequenceWithCharset() {
        StringBuffer sb = new StringBuffer(TEST_CONTENT);
        InputStream is = IOUtils.toInputStream(sb, StandardCharsets.UTF_8);
        Assert.assertNotNull(is);
        Assert.assertEquals(TEST_CONTENT, IOUtils.toString(is, StandardCharsets.UTF_8));
    }

    @Test
    public void testToInputStreamFromCharSequenceNull() {
        StringBuilder sb = null;
        InputStream is = IOUtils.toInputStream(sb, StandardCharsets.UTF_8);
        Assert.assertNotNull(is);
        Assert.assertEquals(0, IOUtils.toByteArray(is).length);
    }

    @Test
    public void testToInputStreamFromStringNull() {
        String content = null;
        InputStream is = IOUtils.toInputStream(content, StandardCharsets.UTF_8);
        Assert.assertNotNull(is);
        Assert.assertEquals(0, IOUtils.toByteArray(is).length);
    }

    @Test
    public void testToInputStreamFromFile() throws IOException {
        File tempFile = File.createTempFile("ioutils_test_", ".tmp");
        tempFile.deleteOnExit();
        IOUtils.writeToFile(TEST_CONTENT, tempFile);

        InputStream is = IOUtils.toInputStream(tempFile);
        Assert.assertNotNull(is);
        Assert.assertEquals(TEST_CONTENT, IOUtils.toString(is));
        IOUtils.closeQuietly(is);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToInputStreamFromNullFile() {
        IOUtils.toInputStream((File) null);
    }

    @Test
    public void testToInputStreamFromPath() throws IOException {
        Path tempPath = Files.createTempFile("ioutils_test_", ".tmp");
        Files.deleteIfExists(tempPath);
        IOUtils.writeToFile(TEST_CONTENT, tempPath);

        InputStream is = IOUtils.toInputStream(tempPath);
        Assert.assertNotNull(is);
        Assert.assertEquals(TEST_CONTENT, IOUtils.toString(is));
        IOUtils.closeQuietly(is);
        Files.deleteIfExists(tempPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToInputStreamFromNullPath() {
        IOUtils.toInputStream((Path) null);
    }

    @Test
    public void testToInputStreamFromURL() throws IOException {
        // 使用 file:// URL 协议测试本地文件
        File tempFile = File.createTempFile("ioutils_test_", ".tmp");
        tempFile.deleteOnExit();
        IOUtils.writeToFile(TEST_CONTENT, tempFile);

        URL url = tempFile.toURI().toURL();
        InputStream is = IOUtils.toInputStream(url);
        Assert.assertNotNull(is);
        Assert.assertEquals(TEST_CONTENT, IOUtils.toString(is));
        IOUtils.closeQuietly(is);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToInputStreamFromNullURL() {
        IOUtils.toInputStream((URL) null);
    }

    @Test
    public void testToInputStreamFromURI() throws IOException {
        File tempFile = File.createTempFile("ioutils_test_", ".tmp");
        tempFile.deleteOnExit();
        IOUtils.writeToFile(TEST_CONTENT, tempFile);

        URI uri = tempFile.toURI();
        InputStream is = IOUtils.toInputStream(uri);
        Assert.assertNotNull(is);
        Assert.assertEquals(TEST_CONTENT, IOUtils.toString(is));
        IOUtils.closeQuietly(is);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToInputStreamFromNullURI() {
        IOUtils.toInputStream((URI) null);
    }

    @Test
    public void testToStringFromReader() {
        Reader reader = new StringReader(TEST_CONTENT);
        String result = IOUtils.toString(reader);
        Assert.assertEquals(TEST_CONTENT, result);
    }

    @Test
    public void testToStringFromNullReader() {
        String result = IOUtils.toString((Reader) null);
        Assert.assertNull(result);
    }

    @Test
    public void testToByteArrayFromReader() {
        Reader reader = new StringReader(TEST_CONTENT);
        byte[] bytes = IOUtils.toByteArray(reader, StandardCharsets.UTF_8);
        Assert.assertNotNull(bytes);
        Assert.assertEquals(TEST_CONTENT, new String(bytes, StandardCharsets.UTF_8));
    }

    @Test
    public void testToByteArrayFromNullReader() {
        byte[] bytes = IOUtils.toByteArray(null, StandardCharsets.UTF_8);
        Assert.assertNotNull(bytes);
        Assert.assertEquals(0, bytes.length);
    }

    @Test
    public void testReadLinesToList() {
        InputStream is = IOUtils.toInputStream("line1\nline2\nline3");
        List<String> lines = IOUtils.readLinesToList(is);
        Assert.assertEquals(3, lines.size());
        Assert.assertEquals("line1", lines.get(0));
        Assert.assertEquals("line2", lines.get(1));
        Assert.assertEquals("line3", lines.get(2));
    }

    @Test
    public void testReadLinesToListWithCharset() {
        InputStream is = IOUtils.toInputStream("line1\nline2\nline3");
        List<String> lines = IOUtils.readLinesToList(is, StandardCharsets.UTF_8);
        Assert.assertEquals(3, lines.size());
    }

    @Test
    public void testReadLinesToListNullInput() {
        List<String> lines = IOUtils.readLinesToList(null);
        Assert.assertNotNull(lines);
        Assert.assertTrue(lines.isEmpty());
    }

    @Test
    public void testToInputStreamFromList() {
        List<String> lines = new ArrayList<>();
        lines.add("line1");
        lines.add("line2");
        lines.add("line3");

        InputStream is = IOUtils.toInputStream(lines);
        Assert.assertNotNull(is);
        String content = IOUtils.toString(is);
        Assert.assertTrue(content.contains("line1"));
        Assert.assertTrue(content.contains("line2"));
        Assert.assertTrue(content.contains("line3"));
    }

    @Test
    public void testToInputStreamFromEmptyList() {
        List<String> lines = new ArrayList<>();
        InputStream is = IOUtils.toInputStream(lines);
        Assert.assertNotNull(is);
        Assert.assertEquals(0, IOUtils.toByteArray(is).length);
    }

    @Test
    public void testToInputStreamFromNullList() {
        InputStream is = IOUtils.toInputStream((List<String>) null);
        Assert.assertNotNull(is);
        Assert.assertEquals(0, IOUtils.toByteArray(is).length);
    }

    @Test
    public void testWriteToFileFromInputStream() throws IOException {
        File tempFile = File.createTempFile("ioutils_test_", ".tmp");
        tempFile.deleteOnExit();

        InputStream is = IOUtils.toInputStream(TEST_CONTENT);
        IOUtils.writeToFile(is, tempFile);

        Assert.assertEquals(TEST_CONTENT, IOUtils.toString(IOUtils.toInputStream(tempFile)));
    }

    @Test
    public void testWriteToFileFromInputStreamToPath() throws IOException {
        Path tempPath = Files.createTempFile("ioutils_test_", ".tmp");
        Files.deleteIfExists(tempPath);

        InputStream is = IOUtils.toInputStream(TEST_CONTENT);
        IOUtils.writeToFile(is, tempPath);

        Assert.assertEquals(TEST_CONTENT, IOUtils.toString(IOUtils.toInputStream(tempPath)));
        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testWriteToFileFromByteArray() throws IOException {
        File tempFile = File.createTempFile("ioutils_test_", ".tmp");
        tempFile.deleteOnExit();

        byte[] data = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        IOUtils.writeToFile(data, tempFile);

        Assert.assertEquals(TEST_CONTENT, IOUtils.toString(IOUtils.toInputStream(tempFile)));
    }

    @Test
    public void testWriteToFileFromByteArrayToPath() throws IOException {
        Path tempPath = Files.createTempFile("ioutils_test_", ".tmp");
        Files.deleteIfExists(tempPath);

        byte[] data = TEST_CONTENT.getBytes(StandardCharsets.UTF_8);
        IOUtils.writeToFile(data, tempPath);

        Assert.assertEquals(TEST_CONTENT, IOUtils.toString(IOUtils.toInputStream(tempPath)));
        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testWriteToFileFromString() throws IOException {
        File tempFile = File.createTempFile("ioutils_test_", ".tmp");
        tempFile.deleteOnExit();

        IOUtils.writeToFile(TEST_CONTENT, tempFile);

        Assert.assertEquals(TEST_CONTENT, IOUtils.toString(IOUtils.toInputStream(tempFile)));
    }

    @Test
    public void testWriteToFileFromStringToPath() throws IOException {
        Path tempPath = Files.createTempFile("ioutils_test_", ".tmp");
        Files.deleteIfExists(tempPath);

        IOUtils.writeToFile(TEST_CONTENT, tempPath);

        Assert.assertEquals(TEST_CONTENT, IOUtils.toString(IOUtils.toInputStream(tempPath)));
        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testWriteToFileFromStringWithCharset() throws IOException {
        File tempFile = File.createTempFile("ioutils_test_", ".tmp");
        tempFile.deleteOnExit();

        IOUtils.writeToFile(TEST_CONTENT, tempFile, StandardCharsets.UTF_8);

        Assert.assertEquals(TEST_CONTENT, IOUtils.toString(IOUtils.toInputStream(tempFile)));
    }

    @Test
    public void testWriteToFileFromList() throws IOException {
        File tempFile = File.createTempFile("ioutils_test_", ".tmp");
        tempFile.deleteOnExit();

        List<String> lines = new ArrayList<>();
        lines.add("line1");
        lines.add("line2");
        lines.add("line3");
        IOUtils.writeToFile(lines, tempFile);

        List<String> result = IOUtils.readLinesToList(IOUtils.toInputStream(tempFile));
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("line1", result.get(0));
        Assert.assertEquals("line2", result.get(1));
        Assert.assertEquals("line3", result.get(2));
    }

    @Test
    public void testWriteToFileFromListToPath() throws IOException {
        Path tempPath = Files.createTempFile("ioutils_test_", ".tmp");
        Files.deleteIfExists(tempPath);

        List<String> lines = new ArrayList<>();
        lines.add("line1");
        lines.add("line2");
        lines.add("line3");
        IOUtils.writeToFile(lines, tempPath);

        List<String> result = IOUtils.readLinesToList(IOUtils.toInputStream(tempPath));
        Assert.assertEquals(3, result.size());
        Files.deleteIfExists(tempPath);
    }
}
