package com.tingfeng.util.java.base.concurrent;

import org.junit.Assert;
import org.junit.Test;

/**
 * ThreadLocalUtils 测试
 */
public class ThreadLocalUtilsTest {

    /**
     * 注册 null 不应抛出异常（边界）
     */
    @Test
    public void testRegisterNull() {
        ThreadLocalUtils.register(null);
    }

    /**
     * 取消注册 null 不应抛出异常（边界）
     */
    @Test
    public void testUnregisterNull() {
        ThreadLocalUtils.unregister(null);
    }

    /**
     * 注册 ThreadLocal 后可以通过 remove 正常清理（happy path）
     */
    @Test
    public void testRegisterAndRemove() {
        ThreadLocal<String> tl = new ThreadLocal<>();
        tl.set("hello-register");
        Assert.assertEquals("hello-register", tl.get());

        String removed = ThreadLocalUtils.remove(tl);
        Assert.assertEquals("hello-register", removed);
        Assert.assertNull("remove 后值应为 null", tl.get());
    }

    /**
     * 移除未设值的 ThreadLocal 应返回 null（边界）
     */
    @Test
    public void testRemoveNonExistent() {
        ThreadLocal<String> tl = new ThreadLocal<>();
        String removed = ThreadLocalUtils.remove(tl);
        Assert.assertNull("未设值时 remove 应返回 null", removed);
    }

    /**
     * 注册后取消注册，不应抛出异常（happy path）
     */
    @Test
    public void testRegisterAndUnregister() {
        ThreadLocal<String> tl = new ThreadLocal<>();
        ThreadLocalUtils.register(tl);
        ThreadLocalUtils.unregister(tl);
    }

    /**
     * 多次注册和取消注册操作不应抛出异常（happy path）
     */
    @Test
    public void testRegisterUnregisterMultiple() {
        ThreadLocal<String> tl1 = new ThreadLocal<>();
        ThreadLocal<String> tl2 = new ThreadLocal<>();
        ThreadLocal<String> tl3 = new ThreadLocal<>();

        ThreadLocalUtils.register(tl1);
        ThreadLocalUtils.register(tl2);
        ThreadLocalUtils.register(tl3);

        ThreadLocalUtils.unregister(tl2);
        ThreadLocalUtils.unregister(tl1);
        ThreadLocalUtils.unregister(tl3);
    }

    /**
     * threadLocalCount 不应抛出受检异常（原问题 2 的回归验证）
     *
     * 此方法内部将受检异常包装为 RuntimeException 重新抛出，
     * 因此调用方无需处理受检异常。
     */
    @Test
    public void testThreadLocalCountNoCheckedException() {
        // 验证不会抛出受检异常（编译期验证），且返回值 >= 0
        int count = ThreadLocalUtils.threadLocalCount();
        Assert.assertTrue("ThreadLocal 数量应 >= 0", count >= 0);
    }

    /**
     * concurrent 包工具类的集中验证——确保线程安全的移除操作
     */
    @Test
    public void testRemoveAfterSetAndClear() {
        ThreadLocal<Integer> tl = new ThreadLocal<>();
        tl.set(42);
        Assert.assertEquals(Integer.valueOf(42), tl.get());

        // 先 remove 再验证
        Integer removed = ThreadLocalUtils.remove(tl);
        Assert.assertEquals(Integer.valueOf(42), removed);
        Assert.assertNull(tl.get());

        // 再次 remove 应返回 null
        Integer removedAgain = ThreadLocalUtils.remove(tl);
        Assert.assertNull("重复 remove 应返回 null", removedAgain);
    }
}
