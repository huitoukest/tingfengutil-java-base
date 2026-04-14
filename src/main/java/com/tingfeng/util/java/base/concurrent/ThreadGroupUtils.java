package com.tingfeng.util.java.base.concurrent;

import java.lang.reflect.Field;

/**
 * 线程组工具类
 * <p>
 * 提供系统线程组和根线程组的获取
 * </p>
 * <p>
 * 注意：ThreadGroup 是较过时的 API，现代 Java 应用中几乎不需要直接操作线程组
 * </p>
 */
public final class ThreadGroupUtils {

    private ThreadGroupUtils() {
    }

    /**
     * 获取系统线程组
     *
     * @return 系统线程组
     */
    public static ThreadGroup getSystemThreadGroup() {
        try {
            Field field = ThreadGroup.class.getDeclaredField("systemThreadGroup");
            field.setAccessible(true);
            return (ThreadGroup) field.get(null);
        } catch (Exception e) {
            // fallback: 遍历获取
            ThreadGroup root = getRootThreadGroup();
            ThreadGroup system = new ThreadGroup(root, "system");
            return system;
        }
    }

    /**
     * 获取根线程组
     *
     * @return 根线程组
     */
    public static ThreadGroup getRootThreadGroup() {
        ThreadGroup root = Thread.currentThread().getThreadGroup();
        while (root.getParent() != null) {
            root = root.getParent();
        }
        return root;
    }

    /**
     * 获取当前线程所属的线程组
     *
     * @return 当前线程的线程组
     */
    public static ThreadGroup getCurrentThreadGroup() {
        return Thread.currentThread().getThreadGroup();
    }

    /**
     * 获取线程组的基本信息
     *
     * @param group 线程组
     * @return 信息字符串，格式：name[activeCount:activeGroupCount, maxPriority:priority, daemon:isDaemon]
     */
    public static String getGroupInfo(ThreadGroup group) {
        return String.format("%s[activeCount=%d, activeGroupCount=%d, maxPriority=%d, daemon=%s]",
                group.getName(),
                group.activeCount(),
                group.activeGroupCount(),
                group.getMaxPriority(),
                group.isDaemon());
    }
}
