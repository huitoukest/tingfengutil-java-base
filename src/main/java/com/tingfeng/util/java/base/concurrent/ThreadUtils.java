package com.tingfeng.util.java.base.concurrent;

/**
 * 线程操作工具类
 *
 * 聚焦于线程级别的操作：sleep、join、interrupt
 * 内部统一处理 InterruptedException，不向上抛出
 */
public final class ThreadUtils {

    private ThreadUtils() {
    }

    // ==================== Sleep 相关 ====================

    /**
     * 睡眠指定毫秒数
     *
     * @param mills 毫秒数，必须大于等于 0
     * @throws IllegalArgumentException mills 为负数时抛出
     */
    public static void sleep(long mills) {
        if (mills < 0) {
            throw new IllegalArgumentException("mills must not be negative");
        }
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            // 静默处理，保持中断状态
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 睡眠指定毫秒数和纳秒数
     *
     * @param mills 毫秒数，必须大于等于 0
     * @param nanos 纳秒数 (0-999999)
     * @throws IllegalArgumentException mills 为负数或 nanos 超出范围时抛出
     */
    public static void sleep(long mills, int nanos) {
        if (mills < 0) {
            throw new IllegalArgumentException("mills must not be negative");
        }
        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException("nanos must be between 0 and 999999");
        }
        try {
            Thread.sleep(mills, nanos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ==================== Join 相关 ====================

    /**
     * 等待指定线程终止
     *
     * @param thread 待等待的线程
     */
    public static void join(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 等待指定线程终止，带超时
     *
     * @param thread    待等待的线程
     * @param timeoutMs 超时毫秒数
     * @return true 表示线程正常结束，false 表示超时或中断
     */
    public static boolean join(Thread thread, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        long remaining = timeoutMs;
        while (thread.isAlive()) {
            if (remaining <= 0) {
                return false;
            }
            try {
                thread.join(remaining);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            remaining = deadline - System.currentTimeMillis();
        }
        return true;
    }

    // ==================== Interrupt 相关 ====================

    /**
     * 中断指定线程
     *
     * @param thread 待中断的线程
     */
    public static void interrupt(Thread thread) {
        thread.interrupt();
    }

    /**
     * 获取当前线程是否被中断
     *
     * @return true 表示当前线程已被中断
     */
    public static boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    // ==================== 状态检查 ====================

    /**
     * 获取线程详细信息
     *
     * @param thread 待检查的线程
     * @return 包含线程名、状态、ID、优先级、栈信息的字符串
     */
    public static String getThreadInfo(Thread thread) {
        StringBuilder sb = new StringBuilder();
        sb.append("Thread[name=").append(thread.getName());
        sb.append(", id=").append(thread.getId());
        sb.append(", state=").append(thread.getState());
        sb.append(", priority=").append(thread.getPriority());
        sb.append(", daemon=").append(thread.isDaemon());
        sb.append("]");

        StackTraceElement[] stackTrace = thread.getStackTrace();
        if (stackTrace.length > 0) {
            sb.append("\n  Stack:");
            for (StackTraceElement element : stackTrace) {
                sb.append("\n    at ").append(element);
            }
        }
        return sb.toString();
    }

    /**
     * 获取当前线程的名称
     *
     * @return 当前线程名称
     */
    public static String getCurrentThreadName() {
        return Thread.currentThread().getName();
    }

    /**
     * 获取当前线程的 ID
     *
     * @return 当前线程 ID
     */
    public static long getCurrentThreadId() {
        return Thread.currentThread().getId();
    }
}
