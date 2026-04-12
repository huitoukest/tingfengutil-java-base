package com.tingfeng.util.java.base.common.helper;

import org.junit.Assert;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SimplePoolHelper 类的单元测试
 * 测试资源池的创建、使用、释放和关闭功能
 *
 * @author huitoukest
 */
public class SimplePoolHelperTest {

    /**
     * 测试基本功能
     */
    @Test
    public void test() throws InterruptedException {
           int threadSize = 20;
           SimplePoolHelper<StringBuilder> simplePoolHelper = new SimplePoolHelper<StringBuilder>(10,()->new StringBuilder());
           CountDownLatch latch = new CountDownLatch(threadSize);
           
           for(int i = 0;i < threadSize ;i++){
               new Thread(()->{
                   try {
                       StringBuilder sb = simplePoolHelper.get();
                       sb.setLength(0);
                       sb.append("1");
                       try {
                           Thread.sleep(2);
                       } catch (InterruptedException e) {
                           Thread.currentThread().interrupt();
                       }
                       sb.append("2");
                       System.out.println(sb.toString());
                       simplePoolHelper.release(sb);
                   } finally {
                       latch.countDown();
                   }
               }).start();
           }
           
           // 等待所有线程完成，最多等待10秒
           boolean completed = latch.await(10, TimeUnit.SECONDS);
           if (!completed) {
               System.out.println("测试超时，强制结束");
           }
           System.out.println("over");
           
           // 验证池状态
           Assert.assertEquals("使用中的资源数量应该为0", 0, simplePoolHelper.getUseSize());
           Assert.assertEquals("空闲资源数量应该为10", 10, simplePoolHelper.getIdleSize());
           
           // 关闭池
           simplePoolHelper.close();
    }

    /**
     * 测试 AutoCloseable 功能
     */
    @Test
    public void testAutoCloseable() {
        // 创建一个可关闭的资源
        class TestResource implements Closeable {
            private boolean closed = false;
            private int id;
            private AtomicInteger counter = new AtomicInteger(0);

            public TestResource() {
                this.id = counter.incrementAndGet();
            }

            public boolean isClosed() {
                return closed;
            }

            public int getId() {
                return id;
            }

            @Override
            public void close() throws IOException {
                closed = true;
                System.out.println("Resource " + id + " closed");
            }
        }

        // 使用 try-with-resources
        final TestResource testResource = new TestResource();
        try (SimplePoolHelper<TestResource> pool = new SimplePoolHelper<>(2, () -> new TestResource())) {
            // 获取资源
            TestResource resource1 = pool.get();
            TestResource resource2 = pool.get();
            
            // 验证资源不为 null
            Assert.assertNotNull("资源1应该不为null", resource1);
            Assert.assertNotNull("资源2应该不为null", resource2);
            
            // 验证资源未关闭
            Assert.assertFalse("资源1应该未关闭", resource1.isClosed());
            Assert.assertFalse("资源2应该未关闭", resource2.isClosed());
            
            // 释放资源
            pool.release(resource1);
            pool.release(resource2);
            
            // 验证池状态
            Assert.assertEquals("使用中的资源数量应该为0", 0, pool.getUseSize());
            Assert.assertEquals("空闲资源数量应该为2", 2, pool.getIdleSize());
        }
        // 这里 pool 会自动关闭
        // 资源应该被关闭
    }

    /**
     * 测试最大并发数
     */
    @Test
    public void testMaxThreadCount() throws InterruptedException {
        final int maxThreads = 3;
        SimplePoolHelper<StringBuilder> pool = new SimplePoolHelper<>(maxThreads, () -> new StringBuilder());
        
        // 获取最大数量的资源
        List<StringBuilder> resources = new ArrayList<>();
        for (int i = 0; i < maxThreads; i++) {
            resources.add(pool.get());
        }
        
        // 验证池状态
        Assert.assertEquals("使用中的资源数量应该为3", 3, pool.getUseSize());
        Assert.assertEquals("空闲资源数量应该为0", 0, pool.getIdleSize());
        Assert.assertEquals("最大线程数应该为3", 3, pool.getMaxThreadCount());
        
        // 尝试获取更多资源（应该等待）
        final CountDownLatch latch = new CountDownLatch(1);
        final StringBuilder[] extraResource = new StringBuilder[1];
        
        new Thread(() -> {
            extraResource[0] = pool.get();
            latch.countDown();
        }).start();
        
        // 等待一段时间，验证资源还未获取到
        Assert.assertFalse("资源获取应该等待", latch.await(100, TimeUnit.MILLISECONDS));
        
        // 释放一个资源
        pool.release(resources.get(0));
        
        // 等待资源获取成功
        Assert.assertTrue("资源获取应该成功", latch.await(1000, TimeUnit.MILLISECONDS));
        Assert.assertNotNull("额外资源应该不为null", extraResource[0]);
        
        // 验证池状态
        Assert.assertEquals("使用中的资源数量应该为3", 3, pool.getUseSize());
        Assert.assertEquals("空闲资源数量应该为0", 0, pool.getIdleSize());
        
        // 关闭池
        pool.close();
    }

    /**
     * 测试资源释放
     */
    @Test
    public void testResourceRelease() {
        SimplePoolHelper<StringBuilder> pool = new SimplePoolHelper<>(2, () -> new StringBuilder());
        
        // 获取资源
        StringBuilder sb1 = pool.get();
        StringBuilder sb2 = pool.get();
        
        // 验证池状态
        Assert.assertEquals("使用中的资源数量应该为2", 2, pool.getUseSize());
        Assert.assertEquals("空闲资源数量应该为0", 0, pool.getIdleSize());
        
        // 释放资源
        pool.release(sb1);
        
        // 验证池状态
        Assert.assertEquals("使用中的资源数量应该为1", 1, pool.getUseSize());
        Assert.assertEquals("空闲资源数量应该为1", 1, pool.getIdleSize());
        
        // 释放另一个资源
        pool.release(sb2);
        
        // 验证池状态
        Assert.assertEquals("使用中的资源数量应该为0", 0, pool.getUseSize());
        Assert.assertEquals("空闲资源数量应该为2", 2, pool.getIdleSize());
        
        // 关闭池
        pool.close();
    }

    /**
     * 测试 get() 方法的循环等待逻辑
     * 确保不会发生栈溢出
     */
    @Test
    public void testGetMethodLoop() throws InterruptedException {
        final int maxThreads = 2;
        SimplePoolHelper<StringBuilder> pool = new SimplePoolHelper<>(maxThreads, () -> new StringBuilder());
        
        // 获取所有资源
        StringBuilder sb1 = pool.get();
        StringBuilder sb2 = pool.get();
        
        // 验证池状态
        Assert.assertEquals("使用中的资源数量应该为2", 2, pool.getUseSize());
        Assert.assertEquals("空闲资源数量应该为0", 0, pool.getIdleSize());
        
        // 尝试获取更多资源（应该等待）
        final CountDownLatch latch = new CountDownLatch(1);
        final StringBuilder[] extraResource = new StringBuilder[1];
        
        new Thread(() -> {
            // 这个调用会等待，直到有资源释放
            extraResource[0] = pool.get();
            latch.countDown();
        }).start();
        
        // 等待一段时间，确保线程已经进入等待状态
        Thread.sleep(200);
        
        // 释放一个资源
        pool.release(sb1);
        
        // 等待资源获取成功
        Assert.assertTrue("资源获取应该成功", latch.await(1000, TimeUnit.MILLISECONDS));
        Assert.assertNotNull("额外资源应该不为null", extraResource[0]);
        
        // 验证池状态
        Assert.assertEquals("使用中的资源数量应该为2", 2, pool.getUseSize());
        Assert.assertEquals("空闲资源数量应该为0", 0, pool.getIdleSize());
        
        // 释放所有资源
        pool.release(sb2);
        pool.release(extraResource[0]);
        
        // 验证池状态
        Assert.assertEquals("使用中的资源数量应该为0", 0, pool.getUseSize());
        Assert.assertEquals("空闲资源数量应该为2", 2, pool.getIdleSize());
        
        // 关闭池
        pool.close();
    }
}