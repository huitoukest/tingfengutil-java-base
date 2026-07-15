package com.tingfeng.util.java.base.concurrent;

import java.lang.reflect.Field;

/**
 * 线程组工具类
 *
 * 提供系统线程组和根线程组的获取
 *
 * 注意：ThreadGroup 是较过时的 API，现代 Java 应用中几乎不需要直接操作线程组
 */
public final class ThreadGroupUtils {

    private ThreadGroupUtils() {
    }

    /**
     * 获取系统线程组
     *
     * 优先通过反射访问 systemThreadGroup 私有字段获取真实的系统线程组。
     * 若反射失败（如安全权限不足），保底返回 getRootThreadGroup() 即根线程组。
     *
     * 注意：保底返回的根线程组虽然不是系统线程组，但是是真实存在的线程组，
     * 而非伪造的，可避免调用者收到误导性结果。
     *
     * @return 系统线程组（反射成功）或根线程组（反射失败时保底）
     */
    public static ThreadGroup getSystemThreadGroup() {
        try {
            Field field = ThreadGroup.class.getDeclaredField("systemThreadGroup");
            field.setAccessible(true);
            return (ThreadGroup) field.get(null);
        } catch (Exception e) {
            // fallback: 反射失败时返回根线程组（真实存在的线程组）
            return getRootThreadGroup();
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
