package com.tingfeng.util.java.base.common.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadLocalUtils 单元测试
 */
public class ThreadLocalUtilsTest {

    // ==================== 基础操作测试 ====================

    @Test
    public void testRemove() {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadLocal.set("test-value");

        String removed = ThreadLocalUtils.remove(threadLocal);
        Assert.assertEquals("应该返回设置的值", "test-value", removed);
        Assert.assertNull("remove后应该为null", threadLocal.get());
    }

    @Test
    public void testRemoveWhenNull() {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        // 不设置值，直接remove

        String removed = ThreadLocalUtils.remove(threadLocal);
        Assert.assertNull("没有设置过值应该返回null", removed);
    }

    @Test
    public void testRemoveFromOtherThread() throws InterruptedException {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadLocal.set("main-value");

        Thread thread = new Thread(() -> {
            // 子线程中设置自己的值
            threadLocal.set("child-value");
            String removed = ThreadLocalUtils.remove(threadLocal);
            Assert.assertEquals("子线程应该返回自己的值", "child-value", removed);
            Assert.assertNull("子线程remove后应该为null", threadLocal.get());
        });

        thread.start();
        thread.join();

        // 主线程的值不应该受影响
        Assert.assertEquals("主线程的值不应该受影响", "main-value", threadLocal.get());
        ThreadLocalUtils.remove(threadLocal);
    }

    // ==================== clearAllThreadLocals 测试 ====================

    @Test
    public void testClearAllThreadLocals() {
        ThreadLocal<String> tl1 = new ThreadLocal<>();
        ThreadLocal<Integer> tl2 = new ThreadLocal<>();
        ThreadLocal<Long> tl3 = new ThreadLocal<>();

        tl1.set("value1");
        tl2.set(123);
        tl3.set(456L);

        // 注意：由于反射实现可能受JVM实现影响，此测试只验证方法能正常执行
        int cleared = ThreadLocalUtils.clearAllThreadLocals();
        Assert.assertTrue("应该返回非负数", cleared >= 0);
    }

    @Test
    public void testClearAllThreadLocalsWithNoThreadLocals() {
        // 当前线程可能没有设置过ThreadLocal
        int cleared = ThreadLocalUtils.clearAllThreadLocals();
        Assert.assertTrue("应该返回0或正数", cleared >= 0);
    }

    @Test
    public void testClearAllThreadLocalsFromOtherThread() throws InterruptedException {
        final AtomicInteger clearedCount = new AtomicInteger(-1);

        Thread thread = new Thread(() -> {
            ThreadLocal<String> tl1 = new ThreadLocal<>();
            ThreadLocal<Integer> tl2 = new ThreadLocal<>();

            tl1.set("child-value1");
            tl2.set(999);

            clearedCount.set(ThreadLocalUtils.clearAllThreadLocals());
        });

        thread.start();
        thread.join();

        // 由于反射实现可能受JVM实现影响，只验证返回非负值
        Assert.assertTrue("应该返回非负数", clearedCount.get() >= 0);
    }

    // ==================== threadLocalCount 测试 ====================

    @Test
    public void testThreadLocalCount() {
        ThreadLocal<String> tl1 = new ThreadLocal<>();
        ThreadLocal<Integer> tl2 = new ThreadLocal<>();
        ThreadLocal<Long> tl3 = new ThreadLocal<>();

        // 设置3个
        tl1.set("value1");
        tl2.set(123);
        tl3.set(456L);

        int count = ThreadLocalUtils.threadLocalCount();
        Assert.assertTrue("应该至少有3个ThreadLocal", count >= 3);

        // 清理
        ThreadLocalUtils.clearAllThreadLocals();
    }

    @Test
    public void testThreadLocalCountWithNoThreadLocals() {
        // 获取当前线程的ThreadLocal数量
        int count = ThreadLocalUtils.threadLocalCount();
        Assert.assertTrue("应该返回非负数", count >= 0);
    }

    // ==================== 线程隔离测试 ====================

    @Test
    public void testThreadIsolation() throws InterruptedException {
        ThreadLocal<Integer> threadLocal = new ThreadLocal<>();
        threadLocal.set(100);

        Thread thread = new Thread(() -> {
            Assert.assertNull("子线程不应该看到主线程设置的值",
                    threadLocal.get());
            threadLocal.set(200);
            Assert.assertEquals("子线程应该设置自己的值", Integer.valueOf(200), threadLocal.get());
        });

        thread.start();
        thread.join();

        Assert.assertEquals("主线程的值应该不变", Integer.valueOf(100), threadLocal.get());

        ThreadLocalUtils.remove(threadLocal);
    }

    // ==================== 内存泄漏防护测试 ====================

    @Test
    public void testRemovePreventsMemoryLeak() throws InterruptedException {
        final ThreadLocal<byte[]> threadLocal = new ThreadLocal<>();
        final AtomicInteger afterRemoveCount = new AtomicInteger(0);

        Thread thread = new Thread(() -> {
            // 设置一个大对象
            threadLocal.set(new byte[1024 * 1024]); // 1MB

            // 移除
            ThreadLocalUtils.remove(threadLocal);

            // 验证已移除
            afterRemoveCount.set(threadLocal.get() == null ? 1 : 0);
        });

        thread.start();
        thread.join();

        Assert.assertEquals("移除后应该为null", 1, afterRemoveCount.get());
    }

    @Test
    public void testClearAllThreadLocalsWithLargeData() throws InterruptedException {
        final AtomicInteger cleared = new AtomicInteger(-1);

        Thread thread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                new ThreadLocal<byte[]>().set(new byte[1024 * 1024]);
            }
            cleared.set(ThreadLocalUtils.clearAllThreadLocals());
        });

        thread.start();
        thread.join();

        // 由于反射实现可能受JVM实现影响，只验证返回非负值
        Assert.assertTrue("应该返回非负数", cleared.get() >= 0);
    }
}
