package com.tingfeng.util.java.base.common.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadPoolUtils 单元测试
 */
public class ThreadPoolUtilsTest {

    // ==================== 线程池创建测试 ====================

    @Test
    public void testNewFixedThreadPool() throws InterruptedException {
        ExecutorService pool = ThreadPoolUtils.newFixedThreadPool(4, "fixed-pool");

        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 10; i++) {
            pool.submit(() -> counter.incrementAndGet());
        }

        pool.shutdown();
        boolean terminated = pool.awaitTermination(5, TimeUnit.SECONDS);
        Assert.assertTrue("线程池应该终止", terminated);
        Assert.assertEquals("应该执行了10个任务", 10, counter.get());
    }

    @Test
    public void testNewCachedThreadPool() throws InterruptedException {
        ExecutorService pool = ThreadPoolUtils.newCachedThreadPool("cached-pool");

        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 10; i++) {
            pool.submit(() -> counter.incrementAndGet());
        }

        pool.shutdown();
        boolean terminated = pool.awaitTermination(5, TimeUnit.SECONDS);
        Assert.assertTrue("线程池应该终止", terminated);
        Assert.assertEquals("应该执行了10个任务", 10, counter.get());
    }

    @Test
    public void testNewSingleThreadPool() throws InterruptedException {
        ExecutorService pool = ThreadPoolUtils.newSingleThreadPool("single-pool");

        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 5; i++) {
            pool.submit(() -> counter.incrementAndGet());
        }

        pool.shutdown();
        boolean terminated = pool.awaitTermination(5, TimeUnit.SECONDS);
        Assert.assertTrue("线程池应该终止", terminated);
        Assert.assertEquals("应该执行了5个任务", 5, counter.get());
    }

    @Test
    public void testNewScheduledThreadPool() throws InterruptedException {
        ExecutorService pool = ThreadPoolUtils.newScheduledThreadPool(2, "scheduled-pool");

        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 5; i++) {
            pool.submit(() -> counter.incrementAndGet());
        }

        pool.shutdown();
        boolean terminated = pool.awaitTermination(5, TimeUnit.SECONDS);
        Assert.assertTrue("线程池应该终止", terminated);
        Assert.assertEquals("应该执行了5个任务", 5, counter.get());
    }

    @Test
    public void testNewFixedThreadPoolWithCustomParams() throws InterruptedException {
        ExecutorService pool = ThreadPoolUtils.newFixedThreadPool(
                2, 4, 60, TimeUnit.SECONDS, 100, "custom-pool");

        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 10; i++) {
            pool.submit(() -> counter.incrementAndGet());
        }

        pool.shutdown();
        boolean terminated = pool.awaitTermination(5, TimeUnit.SECONDS);
        Assert.assertTrue("线程池应该终止", terminated);
        Assert.assertEquals("应该执行了10个任务", 10, counter.get());
    }

    // ==================== ForkJoinPool 测试 ====================

    @Test
    public void testNewForkJoinPool() {
        ForkJoinPool pool = ThreadPoolUtils.newForkJoinPool(4);
        Assert.assertNotNull("ForkJoinPool不应为空", pool);
        Assert.assertEquals("并行度应该是4", 4, pool.getParallelism());
        pool.shutdown();
    }

    @Test
    public void testNewForkJoinPoolDefault() {
        ForkJoinPool pool = ThreadPoolUtils.newForkJoinPool();
        Assert.assertNotNull("ForkJoinPool不应为空", pool);
        Assert.assertEquals("并行度应该是CPU核心数",
                Runtime.getRuntime().availableProcessors(), pool.getParallelism());
        pool.shutdown();
    }

    // ==================== WorkStealingPool 测试 ====================

    @Test
    public void testNewWorkStealingPool() throws InterruptedException {
        ExecutorService pool = ThreadPoolUtils.newWorkStealingPool(4);

        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 10; i++) {
            pool.submit(() -> counter.incrementAndGet());
        }

        pool.shutdown();
        boolean terminated = pool.awaitTermination(5, TimeUnit.SECONDS);
        Assert.assertTrue("线程池应该终止", terminated);
        Assert.assertEquals("应该执行了10个任务", 10, counter.get());
    }

    @Test
    public void testNewWorkStealingPoolDefault() throws InterruptedException {
        ExecutorService pool = ThreadPoolUtils.newWorkStealingPool();

        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 10; i++) {
            pool.submit(() -> counter.incrementAndGet());
        }

        pool.shutdown();
        boolean terminated = pool.awaitTermination(5, TimeUnit.SECONDS);
        Assert.assertTrue("线程池应该终止", terminated);
        Assert.assertEquals("应该执行了10个任务", 10, counter.get());
    }

    // ==================== 线程池关闭测试 ====================

    @Test
    public void testShutdownAndAwait() throws InterruptedException {
        ExecutorService pool = ThreadPoolUtils.newFixedThreadPool(2, "shutdown-pool");

        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 5; i++) {
            pool.submit(() -> {
                try {
                    Thread.sleep(20);
                    counter.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        boolean result = ThreadPoolUtils.shutdownAndAwait(pool, 5, TimeUnit.SECONDS);
        Assert.assertTrue("应该成功关闭", result);
        Assert.assertEquals("应该完成所有任务", 5, counter.get());
    }

    @Test
    public void testShutdownAndAwaitDefault() throws InterruptedException {
        ExecutorService pool = ThreadPoolUtils.newFixedThreadPool(2, "shutdown-default-pool");

        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 3; i++) {
            pool.submit(() -> counter.incrementAndGet());
        }

        boolean result = ThreadPoolUtils.shutdownAndAwait(pool);
        Assert.assertTrue("应该成功关闭", result);
        Assert.assertEquals("应该完成所有任务", 3, counter.get());
    }

    @Test
    public void testShutdownNow() throws InterruptedException {
        ExecutorService pool = ThreadPoolUtils.newFixedThreadPool(2, "shutdownnow-pool");

        for (int i = 0; i < 100; i++) {
            pool.submit(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        ThreadPoolUtils.shutdownNow(pool);
        Assert.assertTrue("应该已shutdown", pool.isShutdown());
    }

    // ==================== 线程池状态测试 ====================

    @Test
    public void testIsShutdown() {
        ExecutorService pool = ThreadPoolUtils.newFixedThreadPool(2, "status-pool");
        Assert.assertFalse("创建时不应已shutdown", ThreadPoolUtils.isShutdown(pool));
        pool.shutdown();
        Assert.assertTrue("shutdown后应该已shutdown", ThreadPoolUtils.isShutdown(pool));
    }

    @Test
    public void testIsTerminated() throws InterruptedException {
        ExecutorService pool = ThreadPoolUtils.newFixedThreadPool(2, "terminated-pool");

        pool.submit(() -> {});
        pool.shutdown();
        boolean terminated = pool.awaitTermination(5, TimeUnit.SECONDS);

        Assert.assertTrue("应该已终止", terminated);
        Assert.assertTrue("应该已终止", ThreadPoolUtils.isTerminated(pool));
    }

    // ==================== 任务提交测试 ====================

    @Test
    public void testSubmitWithResult() throws Exception {
        ExecutorService pool = ThreadPoolUtils.newFixedThreadPool(2, "submit-pool");

        Future<Integer> future = pool.submit(() -> 42);
        Integer result = future.get(5, TimeUnit.SECONDS);

        Assert.assertEquals("应该返回正确结果", Integer.valueOf(42), result);

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
    }
}
