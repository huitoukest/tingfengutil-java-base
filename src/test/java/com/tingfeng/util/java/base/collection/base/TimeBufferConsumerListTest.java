package com.tingfeng.util.java.base.collection.base;

import com.tingfeng.util.java.base.math.RandomUtils;
import com.tingfeng.util.java.base.common.utils.TestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeBufferConsumerListTest {

    private final List<TimeBufferConsumerList<?>> buffers = new ArrayList<>();

    @After
    public void tearDown() {
        for (TimeBufferConsumerList<?> b : buffers) {
            try {
                b.shutdown();
            } catch (Exception e) {
                // ignore
            }
        }
        buffers.clear();
    }

    private <T> TimeBufferConsumerList<T> trackedList(TimeBufferConsumerList<T> buffer) {
        buffers.add(buffer);
        return buffer;
    }

    // ======================== 原有测试 ========================

    @Test
    public void test() throws InterruptedException {
        AtomicInteger consumerSize = new AtomicInteger(0);
        TimeBufferConsumerList<Integer> timeBufferConsumerList = trackedList(new TimeBufferConsumerList<>(10, 64, 100, list -> {
            int size = list.size();
            consumerSize.addAndGet(size);
        }));
        TimeBufferConsumerList<Integer> timeBufferConsumerList2 = trackedList(new TimeBufferConsumerList<>(10, 64, 100, list -> {
            int size = list.size();
            consumerSize.addAndGet(size);
        }));
        int thread = 10;
        int cycle = 50;
        int total = thread * cycle;
        CountDownLatch latch = new CountDownLatch(1);

        TestUtils.printTime(thread, cycle, index -> {
            try {
                Thread.sleep(RandomUtils.randomInt(2, 10));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            timeBufferConsumerList.add(1);
            timeBufferConsumerList2.add(1);

            // 当所有元素都添加完成后，通知主线程
            if (index == thread * cycle - 1) {
                latch.countDown();
            }
        });

        // 等待所有元素添加完成
        latch.await(5, TimeUnit.SECONDS);

        // 等待消费者处理完成，最多等待10秒
        long startTime = System.currentTimeMillis();
        while (consumerSize.intValue() < total * 2 && System.currentTimeMillis() - startTime < 10000) {
            Thread.sleep(10);
        }

        Thread.sleep(500); // 给消费者一点额外时间完成处理
        Assert.assertEquals(total * 2, consumerSize.intValue());
    }

    // ==================== 1. 构造参数校验（H-5 回归） ====================

    /**
     * H-5 回归：验证 checkInterval == maxHoldMs 时抛出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWhenCheckIntervalEqualsMaxHoldMs() {
        trackedList(new TimeBufferConsumerList<>(100, 10, 100, list -> {}));
    }

    /**
     * H-5 回归：验证 checkInterval > maxHoldMs 时抛出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWhenCheckIntervalGreaterThanMaxHoldMs() {
        trackedList(new TimeBufferConsumerList<>(200, 10, 100, list -> {}));
    }

    /**
     * H-5 回归：验证 checkInterval >= maxHoldMs 时的异常消息内容
     */
    @Test
    public void testConstructorCheckIntervalGteMaxHoldMsErrorMessage() {
        try {
            trackedList(new TimeBufferConsumerList<>(100, 10, 50, list -> {}));
            Assert.fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("checkInterval must be less than maxHoldMs", e.getMessage());
        }
    }

    /**
     * 验证基类约束：checkInterval < 1 时抛出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWhenCheckIntervalZero() {
        trackedList(new TimeBufferConsumerList<>(0, 10, 100, list -> {}));
    }

    // ==================== 2. Shutdown 生命周期 ====================

    /**
     * 验证 shutdown 可重复调用而不抛异常
     */
    @Test
    public void testShutdownCanBeCalledMultipleTimes() {
        TimeBufferConsumerList<Integer> buffer = trackedList(
                new TimeBufferConsumerList<>(100, 64, 5000, list -> {}));
        buffer.shutdown();
        buffer.shutdown();
        buffer.shutdown();
    }

    /**
     * 验证 shutdown 后后台线程停止工作，非直通模式下 add 不会再被消费
     */
    @Test(timeout = 5000)
    public void testShutdownStopsBackgroundProcessing() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);
        TimeBufferConsumerList<Integer> buffer = trackedList(
                new TimeBufferConsumerList<>(10, 5, 5000, false, list -> count.addAndGet(list.size())));
        // 等待后台线程至少完成一次检查
        Thread.sleep(50);
        buffer.shutdown();
        // 给后台线程足够时间退出
        Thread.sleep(100);
        // 非直通模式，add 不会调用 consumerIfMatch
        buffer.add(1);
        buffer.add(2);
        Thread.sleep(300);
        Assert.assertEquals("shutdown 后不应有元素被消费", 0, count.get());
    }

    /**
     * 验证 try-with-resources 支持（AutoCloseable）
     */
    @Test(timeout = 5000)
    public void testAutoCloseable() {
        AtomicInteger count = new AtomicInteger(0);
        try (TimeBufferConsumerList<Integer> buffer = new TimeBufferConsumerList<>(10, 5, 5000, false,
                list -> count.addAndGet(list.size()))) {
            buffer.add(1);
            // 正常使用，close 由 try-with-resources 自动调用
        }
        // close 已调用，验证不抛异常即可
    }

    // ==================== 3. Passthrough 模式 ====================

    /**
     * 验证 passthrough 模式下，累积到 batchSize 时立即触发消费
     */
    @Test(timeout = 5000)
    public void testPassthroughBatchFullConsumesImmediately() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        // checkInterval < maxHoldMs 以满足构造约束，且 maxHoldMs 足够大以避免超时干扰
        TimeBufferConsumerList<Integer> buffer = trackedList(
                new TimeBufferConsumerList<>(100, 3, 10000, true, list -> latch.countDown()));
        buffer.add(1);
        buffer.add(2);
        // 未满 batchSize，不应消费
        Assert.assertFalse("batchSize 未满不应触发消费", latch.await(200, TimeUnit.MILLISECONDS));
        buffer.add(3);
        // 填满 batchSize，应触发消费
        Assert.assertTrue("填满 batchSize 应触发消费", latch.await(1000, TimeUnit.MILLISECONDS));
    }

    /**
     * 验证 passthrough 模式下超时也能触发消费（后台线程的 timeMatch 机制）
     */
    @Test(timeout = 5000)
    public void testPassthroughTimeoutConsumes() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        TimeBufferConsumerList<Integer> buffer = trackedList(
                new TimeBufferConsumerList<>(50, 100, 200, true, list -> latch.countDown()));
        // 添加少量元素（远小于 batchSize），后台线程会在超时后通过 timeMatch 消费
        buffer.add(1);
        Assert.assertTrue("超时应触发消费", latch.await(2000, TimeUnit.MILLISECONDS));
    }

    // ==================== 4. 并发边界 ====================

    /**
     * 验证并发 add 期间调用 shutdown 不抛异常
     */
    @Test(timeout = 10000)
    public void testConcurrentAddDuringShutdownNoException() throws InterruptedException {
        AtomicInteger errorCount = new AtomicInteger(0);
        TimeBufferConsumerList<Integer> buffer = trackedList(
                new TimeBufferConsumerList<>(10, 50, 5000, false, list -> {}));
        int threadCount = 5;
        int addsPerThread = 50;
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                try {
                    for (int i = 0; i < addsPerThread; i++) {
                        buffer.add(1);
                        Thread.yield();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        // 在并发 add 中途关闭
        Thread.sleep(30);
        buffer.shutdown();
        latch.await(5, TimeUnit.SECONDS);
        Assert.assertEquals("并发 add 期间 shutdown 不应抛异常", 0, errorCount.get());
    }

    /**
     * 验证 passthrough 模式下并发 add 不会丢元素
     */
    @Test(timeout = 15000)
    public void testConcurrentAddInPassthroughMode() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);
        int batchSize = 20;
        int maxHoldMs = 200;
        TimeBufferConsumerList<Integer> buffer = trackedList(
                new TimeBufferConsumerList<>(10, batchSize, maxHoldMs, true,
                        list -> count.addAndGet(list.size())));
        int threadCount = 5;
        int addsPerThread = 30;
        int totalAdded = threadCount * addsPerThread;
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                for (int i = 0; i < addsPerThread; i++) {
                    buffer.add(1);
                }
                latch.countDown();
            }).start();
        }
        latch.await(5, TimeUnit.SECONDS);
        // 等待后台线程消费残余元素（partial batch + buffer 中未消费的完整 batch）
        long deadline = System.currentTimeMillis() + 5000;
        while (count.get() < totalAdded && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        Assert.assertEquals("passthrough 模式并发 add 不应丢元素", totalAdded, count.get());
    }

    // ==================== 5. batchSize 边界 ====================

    /**
     * 验证 batchSize=1 时每个元素独立成批，全部被消费
     */
    @Test(timeout = 5000)
    public void testBatchSizeOne() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);
        TimeBufferConsumerList<Integer> buffer = trackedList(
                new TimeBufferConsumerList<>(10, 1, 5000, false,
                        list -> count.addAndGet(list.size())));
        buffer.add(1);
        buffer.add(2);
        buffer.add(3);
        long deadline = System.currentTimeMillis() + 3000;
        while (count.get() < 3 && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        Assert.assertEquals("batchSize=1 应逐批消费全部元素", 3, count.get());
    }

    /**
     * 验证 batchSize=Integer.MAX_VALUE 时通过超时触发消费
     */
    @Test(timeout = 5000)
    public void testBatchSizeLargeValue() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        TimeBufferConsumerList<Integer> buffer = trackedList(
                new TimeBufferConsumerList<>(10, Integer.MAX_VALUE, 100, false,
                        list -> latch.countDown()));
        buffer.add(1);
        Assert.assertTrue("batchSize=MAX_VALUE 应通过超时消费", latch.await(2000, TimeUnit.MILLISECONDS));
    }
}