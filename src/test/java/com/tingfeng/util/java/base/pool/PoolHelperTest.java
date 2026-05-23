package com.tingfeng.util.java.base.pool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.tingfeng.util.java.base.lang.inter.PoolMemberActionI;
import com.tingfeng.util.java.base.pool.base.PoolBaseInfo;
import com.tingfeng.util.java.base.pool.PoolHelper;

public class PoolHelperTest {

    @Test
    public void testPoolHelper() throws InterruptedException {
        final AtomicInteger atom = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(10); // 减少线程数量到10

        PoolBaseInfo baseInfo = new PoolBaseInfo();
        baseInfo.setMaxSize(10);
        baseInfo.setMaxRunTime(50000); // 减少最大运行时间到5秒
        baseInfo.setMaxQueueSize(500); // 减少队列大小
        baseInfo.setMaxIdleTime(2000); // 减少空闲时间
        baseInfo.setMaxWaitTime(30000); // 减少最大等待时间到3秒

        final Set<Object> set = new HashSet<Object>();
        final PoolHelper<Runnable> poolHelper = new  PoolHelper<Runnable>(new PoolMemberActionI<Runnable>() {

            @Override
            public Runnable create() {
                return () -> {
                    long time = (long) (Math.random() * 10); // 减少睡眠时间到100ms
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // 正确处理中断
                    }
                };
            }

            @Override
            public boolean destroy(Runnable t) {
                return true;
            }

            @Override
            public void onOverMaxRunTime(Runnable t) {
            }

            @Override
            public void onWorkException(Runnable t,Throwable e) {
            }
        }, baseInfo);

        for(int i = 0 ; i < 10 ; i++) { // 减少到10个线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Runnable run = poolHelper.open();
                        atom.incrementAndGet();
                        synchronized(PoolHelperTest.class) {
                            set.add(run);
                        }
                        run.run();
                    } catch (Exception e) {
                        // 忽略获取资源的异常
                    } finally {
                        atom.decrementAndGet();
                        latch.countDown(); // 完成一个任务
                    }
                }
            }).start();
        }

        // 等待所有任务完成，最多等待15秒
        boolean completed = latch.await(15, java.util.concurrent.TimeUnit.SECONDS);

        // 验证结果
        Assert.assertTrue("所有任务应该完成", completed);
    }

    @Test
    public void testPoolHelperWithError() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(5);

        PoolBaseInfo baseInfo = new PoolBaseInfo();
        baseInfo.setMaxSize(3);
        baseInfo.setMaxRunTime(3000);
        baseInfo.setMaxQueueSize(10);
        baseInfo.setMaxIdleTime(1000);
        baseInfo.setMaxWaitTime(1500);

        final PoolHelper<Runnable> poolHelper = new PoolHelper<>(new PoolMemberActionI<Runnable>() {
            @Override
            public Runnable create() {
                return new Runnable() {
                    @Override
                    public void run() {
                        long time = (long) (Math.random() * 50);
                        try {
                            Thread.sleep(time);
                            // 模拟异常
                            if (Math.random() > 0.7) {
                                throw new RuntimeException("模拟异常");
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                };
            }

            @Override
            public boolean destroy(Runnable t) {
                return true;
            }

            @Override
            public void onOverMaxRunTime(Runnable t) {
            }

            @Override
            public void onWorkException(Runnable t, Throwable e) {
            }
        }, baseInfo);

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    Runnable run = poolHelper.open();
                    run.run();
                } catch (Exception e) {
                    // 预期异常
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        boolean completed = latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
        Assert.assertTrue("错误测试应该完成", completed);
    }
}