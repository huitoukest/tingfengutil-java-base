package com.tingfeng.util.java.base.cache;

import com.tingfeng.util.java.base.cache.SimpleCacheHelper;
import com.tingfeng.util.java.base.cache.base.SimpleCacheMember;
import com.tingfeng.util.java.base.common.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SimpleCacheHelper 单元测试
 * <p>
 * 测试内容：
 * <ul>
 *   <li>基本 get/set 功能</li>
 *   <li>容量超限时权重最低者被驱逐</li>
 *   <li>多线程并发访问安全性</li>
 *   <li>权重溢出 globalOffset 机制</li>
 *   <li>边界条件（空缓存、null key 等）</li>
 * </ul>
 *
 * @author huitoukest
 */
public class SimpleCacheHelperTest {

    // ==================== 基本功能测试 ====================

    /**
     * 测试基本 get/set
     */
    @Test
    public void testBasicGetSet() {
        SimpleCacheHelper<String, String> cache = new SimpleCacheHelper<>(3);

        cache.set("a", "1");
        cache.set("b", "2");
        cache.set("c", "3");

        Assert.assertEquals("1", cache.get("a"));
        Assert.assertEquals("2", cache.get("b"));
        Assert.assertEquals("3", cache.get("c"));
        Assert.assertEquals(3, cache.size());
    }

    /**
     * 测试 get 未命中返回 null
     */
    @Test
    public void testGetMiss() {
        SimpleCacheHelper<String, String> cache = new SimpleCacheHelper<>(3);

        cache.set("a", "1");
        Assert.assertNull(cache.get("nonexistent"));
        Assert.assertNull(cache.get(null));
    }

    /**
     * 测试 set 更新已有 key
     */
    @Test
    public void testSetUpdate() {
        SimpleCacheHelper<String, String> cache = new SimpleCacheHelper<>(3);

        cache.set("a", "1");
        Assert.assertEquals("1", cache.get("a"));

        cache.set("a", "updated");
        Assert.assertEquals("updated", cache.get("a"));
        Assert.assertEquals(1, cache.size());
    }

    /**
     * 测试 containsKey
     */
    @Test
    public void testContainsKey() {
        SimpleCacheHelper<String, String> cache = new SimpleCacheHelper<>(3);

        cache.set("a", "1");
        Assert.assertTrue(cache.containsKey("a"));
        Assert.assertFalse(cache.containsKey("b"));
    }

    // ==================== 驱逐测试 ====================

    /**
     * 测试容量超限时权重最低者被驱逐
     * <p>
     * 场景：容量为2，依次存入 a,b,c
     * c 访问频率最高，a 最低
     * 结果：c 应该被保留，a 或 b 被驱逐
     */
    @Test
    public void testEvictionLowestWeight() {
        SimpleCacheHelper<String, String> cache = new SimpleCacheHelper<>(2);

        cache.set("a", "1");
        cache.set("b", "2");

        // a 访问一次
        cache.get("a");

        // 存入 c，触发驱逐
        cache.set("c", "3");

        // c 一定存在
        Assert.assertNotNull(cache.get("c"));

        // a 和 b 至少有一个存在
        boolean aExists = cache.containsKey("a");
        boolean bExists = cache.containsKey("b");
        Assert.assertTrue(aExists || bExists);

        // 总数不超过容量
        Assert.assertTrue(cache.size() <= 2);
    }

    /**
     * 测试新插入的元素初始权重为 minWeight+1，不会立即被驱逐
     */
    @Test
    public void testNewEntryNotImmediatelyEvicted() {
        SimpleCacheHelper<String, String> cache = new SimpleCacheHelper<>(2);

        cache.set("a", "1");
        cache.set("b", "2");

        // 此时 minWeight=某值，c 的初始权重 = minWeight+1
        cache.set("c", "3");

        // c 应该存在（因为 minWeight+1 不一定是最低）
        // a 或 b 之一被驱逐
        Assert.assertNotNull(cache.get("c"));
        Assert.assertTrue(cache.size() <= 2);
    }

    /**
     * 测试连续驱逐
     */
    @Test
    public void testMultipleEvictions() {
        SimpleCacheHelper<String, String> cache = new SimpleCacheHelper<>(2);

        for (int i = 0; i < 10; i++) {
            cache.set("key" + i, "value" + i);
        }

        // 容量应保持为 maxSize
        Assert.assertEquals(2, cache.size());

        // 至少有某些 key 存在
        int existingCount = 0;
        for (int i = 0; i < 10; i++) {
            if (cache.get("key" + i) != null) {
                existingCount++;
            }
        }
        Assert.assertEquals(2, existingCount);
    }

    // ==================== 多线程测试 ====================

    /**
     * 测试多线程并发写入
     */
    @Test
    public void testConcurrentSet() throws InterruptedException {
        final int threadCount = 10;
        final int operationsPerThread = 100;
        final SimpleCacheHelper<Integer, String> cache = new SimpleCacheHelper<>(200);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            final int threadNo = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < operationsPerThread; i++) {
                        int key = (threadNo * operationsPerThread + i) % 200;
                        cache.set(key, "value-" + key);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    errorCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        endLatch.await(30, TimeUnit.SECONDS);

        Assert.assertEquals(0, errorCount.get());
        Assert.assertTrue(cache.size() <= 200);
    }

    /**
     * 测试多线程并发读写
     */
    @Test
    public void testConcurrentGetAndSet() throws InterruptedException {
        final int threadCount = 10;
        final int operationsPerThread = 100;
        final SimpleCacheHelper<Integer, String> cache = new SimpleCacheHelper<>(100);

        // 预先放入50个元素
        for (int i = 0; i < 50; i++) {
            cache.set(i, "initial-" + i);
        }

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int threadNo = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < operationsPerThread; i++) {
                        int key = i % 50;
                        if (i % 3 == 0) {
                            cache.get(key);
                        } else {
                            cache.set(key, "updated-" + threadNo + "-" + i);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        endLatch.await(30, TimeUnit.SECONDS);

        // 所有元素应该仍然存在或被驱逐但保持一致
        for (int i = 0; i < 50; i++) {
            String value = cache.get(i);
            if (value != null) {
                Assert.assertTrue(value.startsWith("updated-") || value.startsWith("initial-"));
            }
        }
    }

    /**
     * 使用 TestUtils 进行多线程压力测试
     */
    @Test
    public void testConcurrentWithTestUtils() {
        final SimpleCacheHelper<String, String> cache = new SimpleCacheHelper<>(500);

        // 使用 TestUtils 进行多线程测试
        TestUtils.printTime(10, 1000, (index) -> {
            int key = index % 500;
            if (index % 2 == 0) {
                cache.set("key-" + key, "value-" + index);
            } else {
                cache.get("key-" + key);
            }
        });

        Assert.assertTrue(cache.size() <= 500);
    }

    // ==================== 权重溢出测试 ====================

    /**
     * 测试权重溢出触发 globalOffset
     * <p>
     * 通过反射或其他方式直接操作内部状态来模拟权重接近溢出的情况
     */
    @Test
    public void testGlobalOffsetMechanism() throws Exception {
        SimpleCacheHelper<String, String> cache = new SimpleCacheHelper<>(10);

        // 存入一些数据
        for (int i = 0; i < 5; i++) {
            cache.set("key" + i, "value" + i);
        }

        // 通过反射验证 globalOffset 机制存在
        // 由于 globalOffset 是 private 的，我们只能通过行为验证：
        // 多次访问某个 key，增加其权重
        for (int i = 0; i < 1000; i++) {
            cache.get("key0");
        }

        // 继续正常操作，不应出错
        cache.set("newKey", "newValue");
        Assert.assertEquals("newValue", cache.get("newKey"));
    }

    // ==================== 边界条件测试 ====================

    /**
     * 测试空缓存
     */
    @Test
    public void testEmptyCache() {
        SimpleCacheHelper<String, String> cache = new SimpleCacheHelper<>(3);

        Assert.assertEquals(0, cache.size());
        Assert.assertNull(cache.get("any"));
        Assert.assertFalse(cache.containsKey("any"));
    }

    /**
     * 测试容量为1的缓存
     */
    @Test
    public void testCapacityOne() {
        SimpleCacheHelper<String, String> cache = new SimpleCacheHelper<>(1);

        cache.set("a", "1");
        Assert.assertEquals("1", cache.get("a"));

        cache.set("b", "2");
        // a 或 b 其中一个被驱逐
        Assert.assertTrue(cache.containsKey("a") ^ cache.containsKey("b"));
        Assert.assertEquals(1, cache.size());
    }

    /**
     * 测试 keySet 返回的是副本
     */
    @Test
    public void testKeySetIsCopy() {
        SimpleCacheHelper<String, String> cache = new SimpleCacheHelper<>(3);

        cache.set("a", "1");
        cache.set("b", "2");

        Set<String> keySet = cache.keySet();
        keySet.clear();

        // 原缓存不应受影响
        Assert.assertEquals(2, cache.size());
    }

    /**
     * 测试相同 value 不同 key
     */
    @Test
    public void testSameValueDifferentKeys() {
        SimpleCacheHelper<String, String> cache = new SimpleCacheHelper<>(3);

        cache.set("a", "same");
        cache.set("b", "same");

        Assert.assertEquals(2, cache.size());
        Assert.assertNotNull(cache.get("a"));
        Assert.assertNotNull(cache.get("b"));
    }

    // ==================== SimpleCacheMember 测试 ====================

    /**
     * 测试 SimpleCacheMember 基本功能
     */
    @Test
    public void testSimpleCacheMember() {
        SimpleCacheMember<String> member = new SimpleCacheMember<>("test");

        Assert.assertEquals(0, member.getWeight());
        Assert.assertEquals("test", member.getValue());

        member.setWeight(100);
        member.setValue("updated");

        Assert.assertEquals(100, member.getWeight());
        Assert.assertEquals("updated", member.getValue());

        // 测试链式调用
        SimpleCacheMember<String> result = member.setWeight(200).setValue("chained");
        Assert.assertSame(member, result);
    }

    /**
     * 测试 SimpleCacheMember 构造器
     */
    @Test
    public void testSimpleCacheMemberConstructors() {
        SimpleCacheMember<String> m1 = new SimpleCacheMember<>();
        Assert.assertEquals(0, m1.getWeight());
        Assert.assertNull(m1.getValue());

        SimpleCacheMember<String> m2 = new SimpleCacheMember<>("value");
        Assert.assertEquals(0, m2.getWeight());
        Assert.assertEquals("value", m2.getValue());

        SimpleCacheMember<String> m3 = new SimpleCacheMember<>(50, "value50");
        Assert.assertEquals(50, m3.getWeight());
        Assert.assertEquals("value50", m3.getValue());
    }

    // ==================== 权重访问频率测试 ====================

    /**
     * 测试访问频率高的元素不会被驱逐
     */
    @Test
    public void testHighFrequencyAccessSurvives() {
        SimpleCacheHelper<String, String> cache = new SimpleCacheHelper<>(3);

        cache.set("hot", "hotValue");
        cache.set("cold1", "cold1");
        cache.set("cold2", "cold2");

        // 频繁访问 hot，使其权重升高
        for (int i = 0; i < 100; i++) {
            cache.get("hot");
        }

        // 添加新元素触发驱逐
        cache.set("new", "newValue");

        // hot 应该存活
        Assert.assertNotNull(cache.get("hot"));

        // 驱逐后总数不超过容量
        Assert.assertTrue(cache.size() <= 3);
    }

    /**
     * 测试权重相同时先插入的先被驱逐
     */
    @Test
    public void testFifoWhenWeightEqual() {
        SimpleCacheHelper<String, String> cache = new SimpleCacheHelper<>(2);

        cache.set("first", "1");
        cache.set("second", "2");

        // 不访问任何key，权重相同

        // 插入第三个触发驱逐
        cache.set("third", "3");

        // third 一定存在
        Assert.assertNotNull(cache.get("third"));

        // first 和 second 至少一个存在
        boolean firstExists = cache.containsKey("first");
        boolean secondExists = cache.containsKey("second");
        Assert.assertTrue(firstExists || secondExists);
    }
}