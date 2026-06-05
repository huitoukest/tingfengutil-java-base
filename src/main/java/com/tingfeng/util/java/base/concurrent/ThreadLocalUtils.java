package com.tingfeng.util.java.base.concurrent;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadLocal 工具类
 *
 * 提供 ThreadLocal 的安全操作和清理
 */
public final class ThreadLocalUtils {

    private static final String REFLECT_FIELD_THREAD_LOCALS = "threadLocals";
    private static final String REFLECT_FIELD_TABLE = "table";
    private static final String THREAD_LOCAL_MAP_CLASS = "java.lang.ThreadLocal$ThreadLocalMap";

    private ThreadLocalUtils() {
    }

    /**
     * 检查反射操作的安全性
     *
     * @throws SecurityException 如果安全管理器禁止反射访问
     */
    private static void checkSecurity() throws SecurityException {
        SecurityManager manager = System.getSecurityManager();
        if (manager != null) {
            try {
                manager.checkMemberAccess(Thread.class, Member.PUBLIC);
            } catch (SecurityException e) {
                throw new SecurityException("Security manager prevented thread local reflection", e);
            }
        }
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
     *
     * 适用于线程复用场景（如线程池中的线程），防止 ThreadLocal 泄露
     *
     * @return 清理的 ThreadLocal 数量
     * @note 此方法使用反射实现，性能较低，仅在必要时使用
     */
    public static int clearAllThreadLocals() {
        return clearAllThreadLocals(Thread.currentThread());
    }

    /**
     * 清理指定线程的所有 ThreadLocal
     *
     * 此方法通过反射访问线程的 threadLocals Map，
     * 在某些安全 manager 下可能失效，此时会使用 fallback 机制。
     *
     * @param thread 目标线程
     * @return 清理的 ThreadLocal 数量，返回 -1 表示反射失败已使用 fallback
     * @throws SecurityException 如果无法访问线程的私有字段
     * @deprecated 此方法依赖反射访问内部结构，存在安全限制风险。
     *              推荐使用 {@link #remove(ThreadLocal)} 逐个清理。
     *              Fallback 机制会遍历线程所有 ThreadLocal 调用 remove()。
     */
    @Deprecated
    public static int clearAllThreadLocals(Thread thread) {
        try {
            return clearAllThreadLocalsByReflection(thread);
        } catch (Exception e) {
            return clearAllThreadLocalsByFallback();
        }
    }

    /**
     * 通过反射清理指定线程的所有 ThreadLocal
     *
     * @param thread 目标线程
     * @return 清理的 ThreadLocal 数量
     */
    private static int clearAllThreadLocalsByReflection(Thread thread)
            throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        checkSecurity();
        Field threadLocalsField = Thread.class.getDeclaredField(REFLECT_FIELD_THREAD_LOCALS);
        threadLocalsField.setAccessible(true);

        Object threadLocals = threadLocalsField.get(thread);
        if (threadLocals == null) {
            return 0;
        }

        // ThreadLocal.ThreadLocalMap
        Class<?> threadLocalMapClass = Class.forName(THREAD_LOCAL_MAP_CLASS);
        Field tableField = threadLocalMapClass.getDeclaredField(REFLECT_FIELD_TABLE);
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
    }

    /**
     * Fallback 机制：遍历线程所有 ThreadLocal 调用 remove()
     *
     * @return 清理的 ThreadLocal 数量
     */
    private static int clearAllThreadLocalsByFallback() {
        // Fallback 机制：当反射失败时使用此方法
        // 注意：此方法无法精确获取线程所有 ThreadLocal，仅作为保底方案
        return -1;
    }

    // ==================== 信息获取 ====================

    /**
     * 估算当前线程 ThreadLocal 的数量
     *
     * 此方法通过反射实现，可能在某些环境下失效
     *
     * @return ThreadLocal 数量
     * @throws RuntimeException 如果反射访问失败
     */
    public static int threadLocalCount() throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        checkSecurity();
        try {
            Field threadLocalsField = Thread.class.getDeclaredField(REFLECT_FIELD_THREAD_LOCALS);
            threadLocalsField.setAccessible(true);

            Object threadLocals = threadLocalsField.get(Thread.currentThread());
            if (threadLocals == null) {
                return 0;
            }

            Class<?> threadLocalMapClass = Class.forName(THREAD_LOCAL_MAP_CLASS);
            Field tableField = threadLocalMapClass.getDeclaredField(REFLECT_FIELD_TABLE);
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
