package com.tingfeng.util.java.base.concurrent;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadLocal 工具类
 * <p>
 * 提供 ThreadLocal 的安全操作和清理
 * </p>
 */
public final class ThreadLocalUtils {

    private ThreadLocalUtils() {
    }

    // ==================== 基础操作 ====================

    /**
     * 安全移除当前线程的 ThreadLocal 值
     *
     * @param threadLocal ThreadLocal 实例
     * @param <T>        值类型
     * @return 被移除的值，不存在返回 null
     */
    public static <T> T remove(ThreadLocal<T> threadLocal) {
        T value = threadLocal.get();
        threadLocal.remove();
        return value; // 如果原本就没有值，get() 返回 null，remove() 后仍返回 null，这是正确的
    }

    /**
     * 清理当前线程的所有 ThreadLocal
     * <p>
     * 适用于线程复用场景（如线程池中的线程），防止 ThreadLocal 泄露
     * </p>
     *
     * @return 清理的 ThreadLocal 数量
     * @note 此方法使用反射实现，性能较低，仅在必要时使用
     */
    public static int clearAllThreadLocals() {
        return clearAllThreadLocals(Thread.currentThread());
    }

    /**
     * 清理指定线程的所有 ThreadLocal
     * <p>
     * 此方法通过反射访问线程的 threadLocals Map，
     * 在某些安全 manager 下可能失效
     * </p>
     *
     * @param thread 目标线程
     * @return 清理的 ThreadLocal 数量
     * @throws SecurityException 如果无法访问线程的私有字段
     */
    public static int clearAllThreadLocals(Thread thread) {
        try {
            Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
            threadLocalsField.setAccessible(true);

            Object threadLocals = threadLocalsField.get(thread);
            if (threadLocals == null) {
                return 0;
            }

            // ThreadLocal.ThreadLocalMap
            Class<?> threadLocalMapClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
            Field tableField = threadLocalMapClass.getDeclaredField("table");
            tableField.setAccessible(true);

            Object table = tableField.get(threadLocals);
            if (table == null) {
                return 0;
            }

            // 获取数组长度
            int length = java.lang.reflect.Array.getLength(table);
            AtomicInteger count = new AtomicInteger(0);

            for (int i = 0; i < length; i++) {
                Object entry = java.lang.reflect.Array.get(table, i);
                if (entry != null) {
                    try {
                        // Entry 继承自 WeakReference，value 在父类
                        Field valueField = entry.getClass().getSuperclass().getDeclaredField("value");
                        valueField.setAccessible(true);
                        Object value = valueField.get(entry);
                        if (value != null) {
                            valueField.set(entry, null);
                            count.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // 忽略无法处理的 entry
                    }
                }
            }
            return count.get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear thread locals", e);
        }
    }

    // ==================== 信息获取 ====================

    /**
     * 估算当前线程 ThreadLocal 的数量
     * <p>
     * 此方法通过反射实现，可能在某些环境下失效
     * </p>
     *
     * @return ThreadLocal 数量
     * @throws RuntimeException 如果反射访问失败
     */
    public static int threadLocalCount() {
        try {
            Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
            threadLocalsField.setAccessible(true);

            Object threadLocals = threadLocalsField.get(Thread.currentThread());
            if (threadLocals == null) {
                return 0;
            }

            Class<?> threadLocalMapClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
            Field tableField = threadLocalMapClass.getDeclaredField("table");
            tableField.setAccessible(true);

            Object table = tableField.get(threadLocals);
            if (table == null) {
                return 0;
            }

            int length = java.lang.reflect.Array.getLength(table);
            int count = 0;

            for (int i = 0; i < length; i++) {
                Object entry = java.lang.reflect.Array.get(table, i);
                if (entry != null) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get thread local count", e);
        }
    }
}
