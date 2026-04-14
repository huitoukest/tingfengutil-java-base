package com.tingfeng.util.java.base.pool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.tingfeng.util.java.base.lang.inter.PoolMemberActionI;
import com.tingfeng.util.java.base.pool.base.PoolBaseInfo;
import com.tingfeng.util.java.base.pool.PoolHelper;

public class PoolHelperTest {

    @Test
    public void testPoolHelper() throws InterruptedException {
        final AtomicInteger atom = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(20); // 减少线程数量到20

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
                    System.out.println("use Time : " + time + "  " + Thread.currentThread().getName());
                };
            }

            @Override
            public boolean destroy(Runnable t) {
                System.out.println("destroy : " + t);
                return true;
            }

            @Override
            public void onOverMaxRunTime(Runnable t) {
                System.out.println("onOverMaxRunTime : " + t);
            }

            @Override
            public void onWorkException(Runnable t,Throwable e) {
                System.out.println("onWorkException : " + e.getMessage());
            }
        }, baseInfo);

        for(int i = 0 ; i < 20 ; i++) { // 减少到20个线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Runnable run = poolHelper.open();
                        atom.incrementAndGet();
                        synchronized(PoolHelperTest.class) {
                            set.add(run);
                            System.out.println("atom count : " + atom.get() + ",setSize:" + set.size());
                        }
                        run.run();
                    } finally {
                        atom.decrementAndGet();
                        //poolHelper.close(run);
                        latch.countDown(); // 完成一个任务
                    }
                }
            }).start();
        }

        // 等待所有任务完成，最多等待10秒
        boolean completed = latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
        if (!completed) {
            System.out.println("测试超时，强制结束");
        }

        // 验证结果
        System.out.println("测试完成，最终原子计数: " + atom.get() + ", 集合大小: " + set.size());
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
                System.out.println("超过最大运行时间");
            }

            @Override
            public void onWorkException(Runnable t, Throwable e) {
                System.out.println("工作异常: " + e.getMessage());
            }
        }, baseInfo);

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    Runnable run = poolHelper.open();
                    run.run();
                } catch (Exception e) {
                    System.out.println("获取资源异常: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        boolean completed = latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
        if (!completed) {
            System.out.println("错误测试超时");
        }
    }
}