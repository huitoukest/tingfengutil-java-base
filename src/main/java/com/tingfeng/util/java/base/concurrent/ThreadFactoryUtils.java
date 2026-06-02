package com.tingfeng.util.java.base.concurrent;

import com.tingfeng.util.java.base.concurrent.base.NamedThreadFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂工具类
 *
 * 提供 ThreadFactory 的便捷创建
 */
public final class ThreadFactoryUtils {

    private ThreadFactoryUtils() {
    }

    // ==================== ThreadFactory 创建 ====================

    /**
     * 创建命名线程工厂
     *
     * @param namePrefix 线程名称前缀
     * @param daemon     是否守护线程
     * @return ThreadFactory 实例
     */
    public static ThreadFactory newNamedThreadFactory(String namePrefix, boolean daemon) {
        return new NamedThreadFactory(namePrefix, daemon);
    }

    /**
     * 创建带序号的命名线程工厂
     *
     * @param namePrefix  线程名称前缀
     * @param daemon     是否守护线程
     * @param startIndex 起始序号
     * @return ThreadFactory 实例
     */
    public static ThreadFactory newNamedThreadFactory(String namePrefix, boolean daemon, int startIndex) {
        return new NamedThreadFactory(namePrefix, daemon) {
            private final AtomicInteger index = new AtomicInteger(startIndex);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = super.newThread(r);
                // 替换线程名称为带序号的版本
                thread.setName(namePrefix + index.getAndIncrement());
                return thread;
            }
        };
    }

    /**
     * 创建带异常处理的命名线程工厂
     *
     * @param namePrefix 线程名称前缀
     * @param daemon    是否守护线程
     * @param handler   未捕获异常处理器
     * @return ThreadFactory 实例
     */
    public static ThreadFactory newNamedThreadFactory(String namePrefix, boolean daemon,
                                                       Thread.UncaughtExceptionHandler handler) {
        return new NamedThreadFactory(namePrefix, daemon) {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = super.newThread(r);
                thread.setUncaughtExceptionHandler(handler);
                return thread;
            }
        };
    }

    // ==================== 线程组创建 ====================

    /**
     * 创建线程组
     *
     * @param name 线程组名称
     * @return 新线程组实例
     */
    public static ThreadGroup newThreadGroup(String name) {
        return new ThreadGroup(name);
    }

    /**
     * 创建带父线程组的线程组
     *
     * @param name   线程组名称
     * @param parent 父线程组
     * @return 新线程组实例
     */
    public static ThreadGroup newThreadGroup(String name, ThreadGroup parent) {
        return new ThreadGroup(parent, name);
    }
}
