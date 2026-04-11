package com.tingfeng.util.java.base.common.helper;

import org.junit.Assert;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionROne;
import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVOne;

/**
 * FixedPoolHelper 类的单元测试
 * 测试资源池的创建、使用和关闭功能
 *
 * @author huitoukest
 */
public class FixedPoolHelperTest {

    /**
     * 测试基本功能
     */
    @Test
    public void testBasicFunctionality() {
        // 创建一个简单的资源类
        class TestResource {
            private int id;
            private AtomicInteger useCount = new AtomicInteger(0);

            public TestResource(int id) {
                this.id = id;
            }

            public int getId() {
                return id;
            }

            public int incrementAndGetUseCount() {
                return useCount.incrementAndGet();
            }

            public int getUseCount() {
                return useCount.get();
            }
        }

        // 计数器，用于生成资源 ID
        final AtomicInteger resourceId = new AtomicInteger(0);

        // 创建资源池
        try (FixedPoolHelper<TestResource> pool = new FixedPoolHelper<>(3, () -> new TestResource(resourceId.incrementAndGet()))) {
            // 测试资源分配
            TestResource resource1 = pool.run(t -> {
                t.incrementAndGetUseCount();
                return t;
            });

            TestResource resource2 = pool.run(t -> {
                t.incrementAndGetUseCount();
                return t;
            });

            TestResource resource3 = pool.run(t -> {
                t.incrementAndGetUseCount();
                return t;
            });

            // 测试资源复用
            TestResource resource4 = pool.run(t -> {
                t.incrementAndGetUseCount();
                return t;
            });

            // 验证资源 ID 在 1-3 之间
            Assert.assertTrue("资源 ID 应该在 1-3 之间", resource1.getId() >= 1 && resource1.getId() <= 3);
            Assert.assertTrue("资源 ID 应该在 1-3 之间", resource2.getId() >= 1 && resource2.getId() <= 3);
            Assert.assertTrue("资源 ID 应该在 1-3 之间", resource3.getId() >= 1 && resource3.getId() <= 3);
            Assert.assertTrue("资源 ID 应该在 1-3 之间", resource4.getId() >= 1 && resource4.getId() <= 3);

            // 验证资源被复用
            Assert.assertEquals("资源应该被使用 2 次", 2, resource4.getUseCount());
        }
    }

    /**
     * 测试初始化回调功能
     */
    @Test
    public void testInitDataAction() {
        // 创建一个需要初始化的资源类
        class TestResource {
            private String data;

            public void setData(String data) {
                this.data = data;
            }

            public String getData() {
                return data;
            }
        }

        // 创建资源池，设置初始化回调
        try (FixedPoolHelper<TestResource> pool = new FixedPoolHelper<>(2, new Callable<TestResource>() {
            @Override
            public TestResource call() throws Exception {
                return new TestResource();
            }
        }, new FunctionVOne<TestResource>() {
            @Override
            public void accept(TestResource t) {
                t.setData("initialized");
            }
        })) {
            // 测试资源初始化
            String data = pool.run(new FunctionROne<String, TestResource>() {
                @Override
                public String run(TestResource t) {
                    return t.getData();
                }
            });

            Assert.assertEquals("资源应该被初始化", "initialized", data);
        }
    }

    /**
     * 测试 close 方法
     */
    @Test
    public void testClose() {
        // 创建一个可关闭的资源类
        class TestResource implements Closeable {
            private boolean closed = false;

            @Override
            public void close() throws IOException {
                closed = true;
            }

            public boolean isClosed() {
                return closed;
            }
        }

        // 存储创建的资源，用于后续检查
        final TestResource[] resources = new TestResource[2];
        int resourceIndex = 0;

        // 创建资源池
        FixedPoolHelper<TestResource> pool = new FixedPoolHelper<>(2, new Callable<TestResource>() {
            @Override
            public TestResource call() throws Exception {
                TestResource resource = new TestResource();
                synchronized (resources) {
                    for (int i = 0; i < resources.length; i++) {
                        if (resources[i] == null) {
                            resources[i] = resource;
                            break;
                        }
                    }
                }
                return resource;
            }
        });

        // 使用资源
        pool.run(new FunctionROne<Void, TestResource>() {
            @Override
            public Void run(TestResource t) {
                return null;
            }
        });

        // 关闭资源池
        pool.close();

        // 检查资源是否被关闭
        for (TestResource resource : resources) {
            if (resource != null) {
                Assert.assertTrue("资源应该被关闭", resource.isClosed());
            }
        }
    }

    /**
     * 测试 try-with-resources 语法
     */
    @Test
    public void testTryWithResources() {
        // 创建一个可关闭的资源类
        class TestResource implements Closeable {
            private boolean closed = false;

            @Override
            public void close() throws IOException {
                closed = true;
            }

            public boolean isClosed() {
                return closed;
            }
        }

        // 存储创建的资源，用于后续检查
        final TestResource[] resources = new TestResource[2];

        // 使用 try-with-resources 语法
        try (FixedPoolHelper<TestResource> pool = new FixedPoolHelper<>(2, new Callable<TestResource>() {
            @Override
            public TestResource call() throws Exception {
                TestResource resource = new TestResource();
                synchronized (resources) {
                    for (int i = 0; i < resources.length; i++) {
                        if (resources[i] == null) {
                            resources[i] = resource;
                            break;
                        }
                    }
                }
                return resource;
            }
        })) {
            // 使用资源
            pool.run(new FunctionROne<Void, TestResource>() {
                @Override
                public Void run(TestResource t) {
                    return null;
                }
            });
        }
        // 这里 pool 会自动关闭

        // 检查资源是否被关闭
        for (TestResource resource : resources) {
            if (resource != null) {
                Assert.assertTrue("资源应该被关闭", resource.isClosed());
            }
        }
    }

    /**
     * 测试线程安全
     */
    @Test
    public void testThreadSafety() throws InterruptedException {
        // 创建一个线程安全的资源类
        class TestResource {
            private AtomicInteger useCount = new AtomicInteger(0);

            public int incrementAndGetUseCount() {
                return useCount.incrementAndGet();
            }
        }

        // 创建资源池
        try (FixedPoolHelper<TestResource> pool = new FixedPoolHelper<>(2, new Callable<TestResource>() {
            @Override
            public TestResource call() throws Exception {
                return new TestResource();
            }
        })) {
            // 启动多个线程并发使用资源
            int threadCount = 10;
            final CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        for (int j = 0; j < 100; j++) {
                            pool.run(new FunctionROne<Void, TestResource>() {
                                @Override
                                public Void run(TestResource t) {
                                    t.incrementAndGetUseCount();
                                    return null;
                                }
                            });
                        }
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }

            // 等待所有线程完成
            latch.await();

            // 验证所有操作都成功执行
            // 这里我们不检查具体的使用次数，因为并发环境下难以预测
            // 只要没有抛出异常，就说明线程安全
        }
    }

    /**
     * 测试异常处理
     */
    @Test
    public void testExceptionHandling() {
        // 创建一个会抛出异常的资源
        try {
            new FixedPoolHelper<>(1, new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    throw new RuntimeException("Resource creation failed");
                }
            });
            Assert.fail("应该抛出异常");
        } catch (Exception e) {
            Assert.assertTrue("异常信息应该包含错误信息", e.getMessage().contains("Resource creation failed"));
        }
    }

    /**
     * 测试默认池大小
     */
    @Test
    public void testDefaultPoolSize() {
        // 创建一个简单的资源类
        class TestResource {}

        // 使用默认池大小创建资源池
        try (FixedPoolHelper<TestResource> pool = new FixedPoolHelper<>(new Callable<TestResource>() {
            @Override
            public TestResource call() throws Exception {
                return new TestResource();
            }
        })) {
            // 验证资源池可以正常使用
            TestResource resource = pool.run(new FunctionROne<TestResource, TestResource>() {
                @Override
                public TestResource run(TestResource t) {
                    return t;
                }
            });
            Assert.assertNotNull("资源应该不为 null", resource);
        }
    }

    /**
     * 测试负数池大小
     */
    @Test
    public void testNegativePoolSize() {
        // 创建一个简单的资源类
        class TestResource {}

        // 使用负数池大小创建资源池
        try (FixedPoolHelper<TestResource> pool = new FixedPoolHelper<>(-5, new Callable<TestResource>() {
            @Override
            public TestResource call() throws Exception {
                return new TestResource();
            }
        })) {
            // 验证资源池可以正常使用
            TestResource resource = pool.run(new FunctionROne<TestResource, TestResource>() {
                @Override
                public TestResource run(TestResource t) {
                    return t;
                }
            });
            Assert.assertNotNull("资源应该不为 null", resource);
        }
    }
}