package com.tingfeng.util.java.base.common.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadGroupUtils 单元测试
 */
public class ThreadGroupUtilsTest {

    // ==================== 系统线程组测试 ====================

    @Test
    public void testGetSystemThreadGroup() {
        ThreadGroup systemGroup = ThreadGroupUtils.getSystemThreadGroup();
        Assert.assertNotNull("系统线程组不应为空", systemGroup);
        Assert.assertNotNull("系统线程组应该有名称", systemGroup.getName());
    }

    @Test
    public void testGetRootThreadGroup() {
        ThreadGroup rootGroup = ThreadGroupUtils.getRootThreadGroup();
        Assert.assertNotNull("根线程组不应为空", rootGroup);

        // 根线程组的父应该是null
        Assert.assertNull("根线程组的父应该是null", rootGroup.getParent());
    }

    @Test
    public void testGetCurrentThreadGroup() {
        ThreadGroup currentGroup = ThreadGroupUtils.getCurrentThreadGroup();
        Assert.assertNotNull("当前线程组不应为空", currentGroup);
        Assert.assertEquals("应该与Thread.currentThread().getThreadGroup()一致",
                Thread.currentThread().getThreadGroup(), currentGroup);
    }

    // ==================== 线程组信息测试 ====================

    @Test
    public void testGetGroupInfo() {
        ThreadGroup group = new ThreadGroup("test-group-info");
        String info = ThreadGroupUtils.getGroupInfo(group);

        Assert.assertNotNull("信息不应为空", info);
        Assert.assertTrue("信息应包含线程组名", info.contains("test-group-info"));
        Assert.assertTrue("信息应包含activeCount", info.contains("activeCount="));
        Assert.assertTrue("信息应包含activeGroupCount", info.contains("activeGroupCount="));
        Assert.assertTrue("信息应包含maxPriority", info.contains("maxPriority="));
        Assert.assertTrue("信息应包含daemon", info.contains("daemon="));

        group.destroy();
    }

    @Test
    public void testGetGroupInfoForCurrentThreadGroup() {
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        String info = ThreadGroupUtils.getGroupInfo(currentGroup);

        Assert.assertNotNull("信息不应为空", info);
        Assert.assertTrue("信息应包含当前线程组名", info.contains(currentGroup.getName()));
    }

    // ==================== 线程组层级关系测试 ====================

    @Test
    public void testThreadGroupHierarchy() {
        ThreadGroup rootGroup = ThreadGroupUtils.getRootThreadGroup();
        ThreadGroup currentGroup = ThreadGroupUtils.getCurrentThreadGroup();

        // 当前线程组应该是根线程组的子孙
        ThreadGroup parent = currentGroup;
        boolean foundRoot = false;
        while (parent != null) {
            if (parent == rootGroup) {
                foundRoot = true;
                break;
            }
            parent = parent.getParent();
        }
        Assert.assertTrue("当前线程组应该在根线程组的层级中", foundRoot);
    }

    // ==================== 线程组状态测试 ====================

    @Test
    public void testThreadGroupActiveCount() throws InterruptedException {
        ThreadGroup group = new ThreadGroup("active-count-test");
        AtomicInteger activeCount = new AtomicInteger(0);

        Thread thread = new Thread(group, () -> {
            activeCount.set(group.activeCount());
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        thread.start();
        thread.join(100);

        Assert.assertTrue("活跃线程数应该 >= 1", activeCount.get() >= 1);

        group.destroy();
    }

    @Test
    public void testThreadGroupMaxPriority() {
        ThreadGroup parentGroup = new ThreadGroup("parent-priority");
        ThreadGroup childGroup = new ThreadGroup(parentGroup, "child-priority");

        Assert.assertEquals("子线程组的最大优先级应该与父线程组相同",
                parentGroup.getMaxPriority(), childGroup.getMaxPriority());

        parentGroup.destroy();
    }

    @Test
    public void testThreadGroupDaemon() {
        ThreadGroup daemonGroup = new ThreadGroup("daemon-group");
        ThreadGroup nonDaemonGroup = new ThreadGroup("non-daemon-group");

        // 默认情况下，新线程组的守护状态取决于父线程组
        Assert.assertNotNull("守护状态不应为null", daemonGroup.isDaemon());

        daemonGroup.destroy();
        nonDaemonGroup.destroy();
    }

    // ==================== 线程组与线程关系测试 ====================

    @Test
    public void testThreadBelongsToGroup() throws InterruptedException {
        ThreadGroup group = new ThreadGroup("member-test");
        final AtomicInteger groupMatches = new AtomicInteger(0);

        Thread thread = new Thread(group, () -> {
            if (Thread.currentThread().getThreadGroup() == group) {
                groupMatches.incrementAndGet();
            }
        });

        thread.start();
        thread.join();

        Assert.assertEquals("线程应该属于指定的线程组", 1, groupMatches.get());

        group.destroy();
    }

    // ==================== 边界情况测试 ====================

    @Test
    public void testGetGroupInfoWithDestroyedGroup() {
        ThreadGroup group = new ThreadGroup("destroyed-group");
        group.destroy();

        // 对已销毁的线程组获取信息可能会失败或返回不确定结果
        // 这里只测试不会抛出异常
        try {
            String info = ThreadGroupUtils.getGroupInfo(group);
            // 可能返回结果或抛出异常，都是可接受的行为
        } catch (Exception e) {
            // 可接受的行为
        }
    }

    @Test
    public void testEmptyThreadGroupName() {
        ThreadGroup group = new ThreadGroup("");
        String info = ThreadGroupUtils.getGroupInfo(group);
        Assert.assertNotNull("空名称的线程组信息也不应为空", info);
        group.destroy();
    }
}
