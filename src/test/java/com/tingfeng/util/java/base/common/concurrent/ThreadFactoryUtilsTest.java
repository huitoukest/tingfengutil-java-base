package com.tingfeng.util.java.base.common.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadFactoryUtils 单元测试
 */
public class ThreadFactoryUtilsTest {

    // ==================== ThreadFactory 创建测试 ====================

    @Test
    public void testNewNamedThreadFactoryNotDaemon() {
        ThreadFactory factory = ThreadFactoryUtils.newNamedThreadFactory("test-worker", false);
        Assert.assertNotNull("线程工厂不应为空", factory);

        Thread thread = factory.newThread(() -> {});
        Assert.assertNotNull("创建的线程不应为空", thread);
        Assert.assertFalse("不应是守护线程", thread.isDaemon());
        Assert.assertTrue("线程名应包含前缀", thread.getName().startsWith("test-worker"));
    }

    @Test
    public void testNewNamedThreadFactoryDaemon() {
        ThreadFactory factory = ThreadFactoryUtils.newNamedThreadFactory("daemon-worker", true);
        Assert.assertNotNull("线程工厂不应为空", factory);

        Thread thread = factory.newThread(() -> {});
        Assert.assertNotNull("创建的线程不应为空", thread);
        Assert.assertTrue("应是守护线程", thread.isDaemon());
        Assert.assertTrue("线程名应包含前缀", thread.getName().startsWith("daemon-worker"));
    }

    @Test
    public void testMultipleThreadsUniqueNames() {
        ThreadFactory factory = ThreadFactoryUtils.newNamedThreadFactory("unique-worker", false);

        Thread thread1 = factory.newThread(() -> {});
        Thread thread2 = factory.newThread(() -> {});
        Thread thread3 = factory.newThread(() -> {});

        Assert.assertNotEquals("线程名应该不同", thread1.getName(), thread2.getName());
        Assert.assertNotEquals("线程名应该不同", thread2.getName(), thread3.getName());
        Assert.assertNotEquals("线程名应该不同", thread1.getName(), thread3.getName());

        Assert.assertTrue("所有线程名都应包含前缀",
                thread1.getName().startsWith("unique-worker") &&
                thread2.getName().startsWith("unique-worker") &&
                thread3.getName().startsWith("unique-worker"));
    }

    @Test
    public void testNewNamedThreadFactoryWithStartIndex() {
        ThreadFactory factory = ThreadFactoryUtils.newNamedThreadFactory("indexed-worker", false, 100);

        Thread thread1 = factory.newThread(() -> {});
        Thread thread2 = factory.newThread(() -> {});

        Assert.assertTrue("线程名应包含起始序号100",
                thread1.getName().contains("100") || thread2.getName().contains("101"));
    }

    @Test
    public void testNewNamedThreadFactoryWithExceptionHandler() {
        final AtomicInteger exceptionCount = new AtomicInteger(0);
        Thread.UncaughtExceptionHandler handler = (t, e) -> {
            exceptionCount.incrementAndGet();
        };

        ThreadFactory factory = ThreadFactoryUtils.newNamedThreadFactory("handler-worker", false, handler);
        Thread thread = factory.newThread(() -> {
            throw new RuntimeException("Test exception");
        });

        thread.setUncaughtExceptionHandler(handler);
        thread.start();
        try {
            thread.join(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testThreadExecution() throws InterruptedException {
        ThreadFactory factory = ThreadFactoryUtils.newNamedThreadFactory("executor-worker", false);
        AtomicInteger result = new AtomicInteger(0);

        Thread thread = factory.newThread(() -> result.set(42));
        thread.start();
        thread.join(1000);

        Assert.assertEquals("线程应该正确执行任务", 42, result.get());
    }

    // ==================== 线程组创建测试 ====================

    @Test
    public void testNewThreadGroup() {
        ThreadGroup group = ThreadFactoryUtils.newThreadGroup("test-group");
        Assert.assertNotNull("线程组不应为空", group);
        Assert.assertEquals("线程组名应该正确", "test-group", group.getName());
        Assert.assertFalse("不应是守护线程组", group.isDaemon());
    }

    @Test
    public void testNewThreadGroupWithParent() {
        ThreadGroup parentGroup = new ThreadGroup("parent-group");
        ThreadGroup childGroup = ThreadFactoryUtils.newThreadGroup("child-group", parentGroup);

        Assert.assertNotNull("子线程组不应为空", childGroup);
        Assert.assertEquals("子线程组名应该正确", "child-group", childGroup.getName());
        Assert.assertSame("父线程组应该正确", parentGroup, childGroup.getParent());

        parentGroup.destroy();
    }
}
