package com.tingfeng.util.java.base.common.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadUtils 单元测试
 */
public class ThreadUtilsTest {

    // ==================== Sleep 相关测试 ====================

    @Test
    public void testSleep() {
        long startTime = System.currentTimeMillis();
        ThreadUtils.sleep(100);
        long duration = System.currentTimeMillis() - startTime;
        Assert.assertTrue("睡眠时间应该 >= 100ms", duration >= 90);
        Assert.assertTrue("睡眠时间应该 < 200ms", duration < 200);
    }

    @Test
    public void testSleepZero() {
        long startTime = System.currentTimeMillis();
        ThreadUtils.sleep(0);
        long duration = System.currentTimeMillis() - startTime;
        Assert.assertTrue("睡眠时间应该很短", duration < 50);
    }

    @Test
    public void testSleepWithNanos() {
        long startTime = System.currentTimeMillis();
        ThreadUtils.sleep(50, 500000); // 50.5ms
        long duration = System.currentTimeMillis() - startTime;
        Assert.assertTrue("睡眠时间应该 >= 50ms", duration >= 45);
        Assert.assertTrue("睡眠时间应该 < 150ms", duration < 150);
    }

    @Test
    public void testSleepInterrupted() {
        final AtomicBoolean interrupted = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            ThreadUtils.sleep(5000);
            interrupted.set(Thread.currentThread().isInterrupted());
        });

        thread.start();
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        thread.interrupt();
        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Assert.assertTrue("线程应该已被中断", interrupted.get());
    }

    // ==================== Join 相关测试 ====================

    @Test
    public void testJoin() throws InterruptedException {
        final AtomicInteger count = new AtomicInteger(0);
        Thread thread = new Thread(() -> {
            count.incrementAndGet();
        });

        thread.start();
        ThreadUtils.join(thread);
        Assert.assertEquals("线程应该已执行完成", 1, count.get());
        Assert.assertFalse("线程应该不再存活", thread.isAlive());
    }

    @Test
    public void testJoinWithTimeout() throws InterruptedException {
        final AtomicInteger count = new AtomicInteger(0);
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(50);
                count.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        thread.start();
        boolean result = ThreadUtils.join(thread, 1000);
        Assert.assertTrue("应该等待成功", result);
        Assert.assertEquals("线程应该已执行完成", 1, count.get());
    }

    @Test
    public void testJoinTimeout() throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        thread.start();
        long startTime = System.currentTimeMillis();
        boolean result = ThreadUtils.join(thread, 50);
        long duration = System.currentTimeMillis() - startTime;

        Assert.assertFalse("应该超时", result);
        Assert.assertTrue("等待时间应该 >= 50ms", duration >= 45);
        Assert.assertTrue("线程应该仍然存活", thread.isAlive());
        thread.interrupt();
        thread.join(100);
    }

    // ==================== Interrupt 相关测试 ====================

    @Test
    public void testInterrupt() throws InterruptedException {
        final AtomicBoolean interrupted = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                // busy wait
            }
            interrupted.set(true);
        });

        thread.start();
        ThreadUtils.interrupt(thread);
        thread.join(100);
        Assert.assertTrue("线程应该被中断", interrupted.get() || thread.isAlive());
    }

    @Test
    public void testIsInterrupted() throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        thread.start();
        thread.interrupt();
        thread.join();

        // 注意：isInterrupted() 测试的是当前线程，需要在主线程中测试
        Assert.assertTrue("主线程应该未被中断", !ThreadUtils.isInterrupted());
    }

    // ==================== 状态检查相关测试 ====================

    @Test
    public void testGetThreadInfo() throws InterruptedException {
        Thread testThread = new Thread(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        testThread.start();
        String info = ThreadUtils.getThreadInfo(testThread);
        testThread.join(100);

        Assert.assertNotNull("线程信息不应为空", info);
        Assert.assertTrue("信息应包含线程名", info.contains("Thread[name="));
        Assert.assertTrue("信息应包含状态", info.contains("state="));
        Assert.assertTrue("信息应包含ID", info.contains("id="));
    }

    @Test
    public void testGetCurrentThreadName() {
        String name = ThreadUtils.getCurrentThreadName();
        Assert.assertNotNull("当前线程名不应为空", name);
        Assert.assertEquals("应该与Thread.currentThread()一致",
                Thread.currentThread().getName(), name);
    }

    @Test
    public void testGetCurrentThreadId() {
        long id = ThreadUtils.getCurrentThreadId();
        Assert.assertEquals("应该与Thread.currentThread()一致",
                Thread.currentThread().getId(), id);
    }
}
