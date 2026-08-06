package com.tingfeng.util.java.base.io;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * StreamAsyncOps 冒烟测试
 * 验证拆分后实现归属：IOUtils 门面委托的方法逻辑已迁移至本类，行为与拆分前一致
 */
public class StreamAsyncOpsTest {

    private static final String TEST_CONTENT = "Hello, World! 你好，世界！";

    @Test
    public void testCopyAsync() throws Exception {
        InputStream is = StreamOps.toInputStream(TEST_CONTENT);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            IOUtils.CancellationToken token = new IOUtils.CancellationToken();
            AtomicLong progress = new AtomicLong(0);

            Long result = StreamAsyncOps.copyAsync(os, is, executor,
                total -> { progress.set(total); return true; }, IOUtils.DEFAULT_BACK_PRESSURE_BUFFER_SIZE, token)
                .get();

            Assert.assertTrue(result > 0);
            Assert.assertEquals(TEST_CONTENT, os.toString("UTF-8"));
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testCopyAsyncWithDefaultBackPressure() throws Exception {
        InputStream is = StreamOps.toInputStream(TEST_CONTENT);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Long result = StreamAsyncOps.copyAsync(os, is, executor, total -> true,
                new IOUtils.CancellationToken()).get();
            Assert.assertEquals(TEST_CONTENT.getBytes(StandardCharsets.UTF_8).length, result.longValue());
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testReadLinesAsync() throws Exception {
        InputStream is = StreamOps.toInputStream("line1\nline2\nline3");
        List<String> lines = new ArrayList<>();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            StreamAsyncOps.readLinesAsync(is, StandardCharsets.UTF_8, executor,
                line -> {
                    lines.add(line);
                    return true;
                },
                null, new IOUtils.CancellationToken());

            Thread.sleep(500);
            Assert.assertEquals(3, lines.size());
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testReadStreamWithBackpressure() throws Exception {
        InputStream is = StreamOps.toInputStream("line1\nline2\nline3\nline4\nline5");
        List<List<String>> batches = new ArrayList<>();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            StreamAsyncOps.readStreamWithBackpressure(is, StandardCharsets.UTF_8, 2,
                batch -> {
                    batches.add(new ArrayList<>(batch));
                    return true;
                }, executor, 100, new IOUtils.CancellationToken());

            long deadline = System.currentTimeMillis() + 5000;
            while (batches.size() < 3 && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            Assert.assertEquals(3, batches.size());
            Assert.assertEquals(2, batches.get(0).size());
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
            Supplier<String> supplier = () -> {
                int i = (int) index.getAndIncrement();
                return i < data.size() ? data.get(i) : null;
            };

            Long result = StreamAsyncOps.transmitStream(supplier, os, executor, null,
                new IOUtils.CancellationToken()).get();
            Assert.assertEquals("Hello, World!".length(), result.intValue());
            Assert.assertEquals("Hello, World!", os.toString(StandardCharsets.UTF_8.name()));
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testToExecutorService() {
        ExecutorService original = Executors.newSingleThreadExecutor();
        ExecutorService result = StreamAsyncOps.toExecutorService(original);
        Assert.assertSame(original, result);
        result.shutdown();

        ExecutorService selfManaged = StreamAsyncOps.toExecutorService(new Thread(() -> {}));
        Assert.assertNotNull(selfManaged);
        selfManaged.shutdown();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToExecutorServiceWithInvalidType() {
        StreamAsyncOps.toExecutorService("invalid");
    }

    @Test
    public void testShutdownIfSelfManaged() throws Exception {
        ExecutorService external = Executors.newSingleThreadExecutor();
        try {
            StreamAsyncOps.shutdownIfSelfManaged(external);
            Assert.assertFalse("外部传入的 ExecutorService 不应被关闭", external.isShutdown());
        } finally {
            external.shutdown();
        }

        ExecutorService selfManaged = StreamAsyncOps.toExecutorService(new Thread(() -> {}));
        StreamAsyncOps.shutdownIfSelfManaged(selfManaged);
        Assert.assertTrue("自建池应被 shutdown", selfManaged.isShutdown());
        Assert.assertTrue("自建池 shutdown 后应能正常终止",
            selfManaged.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    public void testAsyncMethodsShutdownSelfManagedPool() throws Exception {
        for (int i = 0; i < 2; i++) {
            InputStream is = StreamOps.toInputStream(TEST_CONTENT);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Long result = StreamAsyncOps.copyAsync(os, is, new Thread(() -> {}), total -> true,
                IOUtils.DEFAULT_BACK_PRESSURE_BUFFER_SIZE, new IOUtils.CancellationToken())
                .get(5, java.util.concurrent.TimeUnit.SECONDS);
            Assert.assertTrue(result > 0);
        }
        long deadline = System.currentTimeMillis() + 3000;
        while (countPoolThreads() > 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        Assert.assertEquals("自建池线程应在任务完成后被 shutdown 回收", 0, countPoolThreads());
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
}
