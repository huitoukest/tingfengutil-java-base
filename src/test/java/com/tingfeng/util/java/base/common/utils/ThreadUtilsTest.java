package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工具类测试
 */
public class ThreadUtilsTest {

    /**
     * 测试sleep方法
     */
    @Test
    public void testSleep() {
        long startTime = System.currentTimeMillis();
        ThreadUtils.sleep(100);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        Assert.assertTrue("睡眠时间应该在100ms左右", duration >= 90 && duration < 150);
    }

    /**
     * 测试sleep方法，参数为0
     */
    @Test
    public void testSleepZero() {
        long startTime = System.currentTimeMillis();
        ThreadUtils.sleep(0);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        Assert.assertTrue("睡眠时间应该很短", duration < 50);
    }

    /**
     * 测试创建命名线程工厂 - 非守护线程
     */
    @Test
    public void testNewNamedThreadFactoryNotDaemon() {
        ThreadFactory factory = ThreadUtils.newNamedThreadFactory("test-thread", false);
        Assert.assertNotNull("线程工厂不能为空", factory);
        
        Thread thread = factory.newThread(() -> {
            // 空任务
        });
        
        Assert.assertNotNull("创建的线程不能为空", thread);
        Assert.assertFalse("线程不应该是守护线程", thread.isDaemon());
        Assert.assertTrue("线程名称应该包含前缀", thread.getName().startsWith("test-thread"));
    }

    /**
     * 测试创建命名线程工厂 - 守护线程
     */
    @Test
    public void testNewNamedThreadFactoryDaemon() {
        ThreadFactory factory = ThreadUtils.newNamedThreadFactory("daemon-thread", true);
        Assert.assertNotNull("线程工厂不能为空", factory);
        
        Thread thread = factory.newThread(() -> {
            // 空任务
        });
        
        Assert.assertNotNull("创建的线程不能为空", thread);
        Assert.assertTrue("线程应该是守护线程", thread.isDaemon());
        Assert.assertTrue("线程名称应该包含前缀", thread.getName().startsWith("daemon-thread"));
    }

    /**
     * 测试创建多个线程，验证线程名称的唯一性
     */
    @Test
    public void testMultipleThreadsUniqueNames() {
        ThreadFactory factory = ThreadUtils.newNamedThreadFactory("worker", false);
        AtomicInteger counter = new AtomicInteger(0);
        
        Thread thread1 = factory.newThread(() -> counter.incrementAndGet());
        Thread thread2 = factory.newThread(() -> counter.incrementAndGet());
        Thread thread3 = factory.newThread(() -> counter.incrementAndGet());
        
        Assert.assertNotEquals("线程名称应该不同", thread1.getName(), thread2.getName());
        Assert.assertNotEquals("线程名称应该不同", thread2.getName(), thread3.getName());
        Assert.assertNotEquals("线程名称应该不同", thread1.getName(), thread3.getName());
        
        Assert.assertTrue("所有线程名称都应该包含前缀", 
            thread1.getName().startsWith("worker") && 
            thread2.getName().startsWith("worker") && 
            thread3.getName().startsWith("worker"));
    }

    /**
     * 测试线程工厂创建的线程可以正常执行
     */
    @Test
    public void testThreadExecution() throws InterruptedException {
        ThreadFactory factory = ThreadUtils.newNamedThreadFactory("executor", false);
        AtomicInteger result = new AtomicInteger(0);
        
        Thread thread = factory.newThread(() -> {
            result.set(42);
        });
        
        thread.start();
        thread.join(1000); // 等待最多1秒
        
        Assert.assertEquals("线程应该正确执行任务", 42, result.get());
    }

    /**
     * 测试sleep方法被中断时的处理
     * 新实现静默处理中断，保持中断状态，不抛出异常
     */
    @Test
    public void testSleepInterrupted() {
        final AtomicBoolean interrupted = new AtomicBoolean(false);
        Thread sleepingThread = new Thread(() -> {
            ThreadUtils.sleep(1000);
            // sleep 返回后检查中断状态
            interrupted.set(Thread.currentThread().isInterrupted());
        });

        sleepingThread.start();

        // 等待线程开始睡眠
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 中断睡眠线程
        sleepingThread.interrupt();

        try {
            sleepingThread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 验证线程被中断且静默处理，没有抛出异常
        Assert.assertTrue("线程应该已被中断", interrupted.get());
    }
}