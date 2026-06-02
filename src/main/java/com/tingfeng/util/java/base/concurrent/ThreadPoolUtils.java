package com.tingfeng.util.java.base.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池工具类
 *
 * 提供线程池的创建、监控、优雅关闭等操作
 */
public final class ThreadPoolUtils {

    public static final int DEFAULT_POOL_MAX_THREADS = 2048;

    private ThreadPoolUtils() {
    }

    // ==================== 基础线程池创建 ====================

    /**
     * 创建固定大小的命名线程池
     *
     * @param nThreads   线程数量
     * @param namePrefix 线程名称前缀
     * @return ExecutorService 实例
     * @see ThreadPoolExecutor.CallerRunsPolicy 拒绝策略：任务由调用线程同步执行，确保无任务丢失
     */
    public static ExecutorService newFixedThreadPool(int nThreads, String namePrefix) {
        return new ThreadPoolExecutor(
                nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                ThreadFactoryUtils.newNamedThreadFactory(namePrefix + "-", false),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 创建缓存线程池
     *
     * @param namePrefix 线程名称前缀
     * @return ExecutorService 实例
     * @deprecated 请使用 {@link #newCachedThreadPool(String, int)} 并指定最大线程数
     */
    @Deprecated
    public static ExecutorService newCachedThreadPool(String namePrefix) {
        return newCachedThreadPool(namePrefix, DEFAULT_POOL_MAX_THREADS);
    }

    /**
     * 创建缓存线程池，支持指定最大线程数
     *
     * @param namePrefix  线程名称前缀
     * @param maxThreads  最大线程数，建议不超过 256
     * @return ExecutorService 实例
     * @see ThreadPoolExecutor.CallerRunsPolicy 拒绝策略：任务由调用线程同步执行，确保无任务丢失
     */
    public static ExecutorService newCachedThreadPool(String namePrefix, int maxThreads) {
        int effectiveMaxThreads = Math.min(maxThreads, DEFAULT_POOL_MAX_THREADS);
        return new ThreadPoolExecutor(
                0, effectiveMaxThreads,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                ThreadFactoryUtils.newNamedThreadFactory(namePrefix + "-", false),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 创建单线程池
     *
     * @param namePrefix 线程名称前缀
     * @return ExecutorService 实例
     * @see ThreadPoolExecutor.CallerRunsPolicy 拒绝策略：任务由调用线程同步执行，确保无任务丢失
     */
    public static ExecutorService newSingleThreadPool(String namePrefix) {
        return new ThreadPoolExecutor(
                1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                ThreadFactoryUtils.newNamedThreadFactory(namePrefix + "-", false),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    // ==================== 扩展线程池创建 ====================

    /**
     * 创建固定大小线程池，支持自定义参数
     *
     * @param corePoolSize    核心线程数
     * @param maxPoolSize     最大线程数
     * @param keepAliveTime   空闲线程存活时间
     * @param unit            时间单位
     * @param queueSize       队列容量，0 或负数使用 SynchronousQueue
     * @param namePrefix      线程名称前缀
     * @return ExecutorService 实例
     * @see ThreadPoolExecutor.CallerRunsPolicy 拒绝策略：任务由调用线程同步执行，确保无任务丢失
     */
    public static ExecutorService newFixedThreadPool(int corePoolSize, int maxPoolSize,
                                                     long keepAliveTime, TimeUnit unit,
                                                     int queueSize, String namePrefix) {
        return new ThreadPoolExecutor(
                corePoolSize, maxPoolSize,
                keepAliveTime, unit,
                queueSize <= 0 ? new SynchronousQueue<>() : new LinkedBlockingQueue<>(queueSize),
                ThreadFactoryUtils.newNamedThreadFactory(namePrefix + "-", false),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 创建固定大小的命名线程池，必须指定队列容量
     *
     * @param nThreads   线程数量
     * @param queueSize  队列容量，必须大于 0
     * @param namePrefix 线程名称前缀
     * @return ExecutorService 实例
     * @see ThreadPoolExecutor.CallerRunsPolicy 拒绝策略：任务由调用线程同步执行，确保无任务丢失
     */
    public static ExecutorService newFixedThreadPool(int nThreads, int queueSize, String namePrefix) {
        return new ThreadPoolExecutor(
                nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueSize),
                ThreadFactoryUtils.newNamedThreadFactory(namePrefix + "-", false),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 创建调度线程池
     *
     * @param corePoolSize 核心线程数
     * @param namePrefix   线程名称前缀
     * @return ExecutorService 实例
     * @see ThreadPoolExecutor.CallerRunsPolicy 拒绝策略：任务由调用线程同步执行，确保无任务丢失
     */
    public static ExecutorService newScheduledThreadPool(int corePoolSize, String namePrefix) {
        return new ThreadPoolExecutor(
                corePoolSize, corePoolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                ThreadFactoryUtils.newNamedThreadFactory(namePrefix + "-", false),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    // ==================== ForkJoinPool ====================

    /**
     * 创建 ForkJoinPool（Work-Stealing 模式）
     *
     * @param parallelism 并行度，默认使用 CPU 核心数
     * @return ForkJoinPool 实例
     */
    public static ForkJoinPool newForkJoinPool(int parallelism) {
        return new ForkJoinPool(parallelism);
    }

    /**
     * 创建默认并行度的 ForkJoinPool
     *
     * @return ForkJoinPool 实例
     */
    public static ForkJoinPool newForkJoinPool() {
        return new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }

    // ==================== WorkStealingPool ====================

    /**
     * 创建 Work-Stealing 线程池（JDK 8+）
     *
     * @param parallelism 并行度，默认使用 CPU 核心数
     * @return ExecutorService 实例
     */
    public static ExecutorService newWorkStealingPool(int parallelism) {
        return new ForkJoinPool(parallelism);
    }

    /**
     * 创建默认并行度的 Work-Stealing 线程池
     *
     * @return ExecutorService 实例
     */
    public static ExecutorService newWorkStealingPool() {
        return new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }

    // ==================== 线程池关闭 ====================

    /**
     * 优雅关闭线程池
     *
     * 先停止接收新任务，再等待正在执行的任务完成
     *
     * @param executor  线程池
     * @param timeout  等待超时时间
     * @param unit     时间单位
     * @return true 表示所有任务完成，false 表示超时
     */
    public static boolean shutdownAndAwait(ExecutorService executor, long timeout, TimeUnit unit) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout, unit)) {
                executor.shutdownNow();
                return executor.awaitTermination(timeout, unit);
            }
            return true;
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 优雅关闭线程池（默认 30 秒超时）
     *
     * @param executor 线程池
     * @return true 表示所有任务完成，false 表示超时
     */
    public static boolean shutdownAndAwait(ExecutorService executor) {
        return shutdownAndAwait(executor, 30, TimeUnit.SECONDS);
    }

    /**
     * 强制关闭线程池
     *
     * @param executor 线程池
     */
    public static void shutdownNow(ExecutorService executor) {
        executor.shutdownNow();
    }

    // ==================== 线程池状态 ====================

    /**
     * 检查线程池是否已 shutdown
     *
     * @param executor 线程池
     * @return true 表示已 shutdown
     */
    public static boolean isShutdown(ExecutorService executor) {
        return executor.isShutdown();
    }

    /**
     * 检查线程池是否已终止
     *
     * @param executor 线程池
     * @return true 表示已终止
     */
    public static boolean isTerminated(ExecutorService executor) {
        return executor.isTerminated();
    }
}