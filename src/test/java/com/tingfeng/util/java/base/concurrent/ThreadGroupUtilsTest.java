package com.tingfeng.util.java.base.concurrent;

import org.junit.Assert;
import org.junit.Test;

/**
 * 线程组工具类测试
 */
public class ThreadGroupUtilsTest {

    /**
     * 测试 getSystemThreadGroup 正常返回
     */
    @Test
    public void testGetSystemThreadGroup() {
        ThreadGroup group = ThreadGroupUtils.getSystemThreadGroup();
        Assert.assertNotNull("系统线程组不能为空", group);
    }

    /**
     * 测试 getSystemThreadGroup 返回的线程组是根线程组（无父组）
     */
    @Test
    public void testGetSystemThreadGroupIsRoot() {
        ThreadGroup systemGroup = ThreadGroupUtils.getSystemThreadGroup();
        ThreadGroup rootGroup = ThreadGroupUtils.getRootThreadGroup();
        // 在标准 JVM 中，系统线程组就是根线程组
        Assert.assertSame("系统线程组应与根线程组是同一个对象", rootGroup, systemGroup);
    }

    /**
     * 测试 getRootThreadGroup 返回的线程组没有父组（已经是根）
     */
    @Test
    public void testGetRootThreadGroupHasNoParent() {
        ThreadGroup root = ThreadGroupUtils.getRootThreadGroup();
        Assert.assertNotNull("根线程组不能为空", root);
        Assert.assertNull("根线程组不应有父组", root.getParent());
    }

    /**
     * 测试 getCurrentThreadGroup 返回非空
     */
    @Test
    public void testGetCurrentThreadGroup() {
        ThreadGroup current = ThreadGroupUtils.getCurrentThreadGroup();
        Assert.assertNotNull("当前线程组不能为空", current);
        Assert.assertEquals("当前线程组应与 Thread.currentThread() 一致",
                Thread.currentThread().getThreadGroup(), current);
    }

    /**
     * 测试 getGroupInfo 返回格式正确
     */
    @Test
    public void testGetGroupInfo() {
        ThreadGroup group = ThreadGroupUtils.getRootThreadGroup();
        String info = ThreadGroupUtils.getGroupInfo(group);
        Assert.assertNotNull("信息不能为空", info);
        Assert.assertTrue("信息应包含线程组名称", info.startsWith(group.getName()));
        Assert.assertTrue("信息应包含 activeCount", info.contains("activeCount="));
        Assert.assertTrue("信息应包含 maxPriority", info.contains("maxPriority="));
    }
}
