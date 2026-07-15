package com.tingfeng.util.java.base.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * 线程池工具类
 *
 * 提供线程池的创建、监控、优雅关闭等操作
 */
public final class ThreadPoolUtils {

    public static final int DEFAULT_POOL_MAX_THREADS = 2048;
    public static final int MAX_RECOMMENDED_THREADS = DEFAULT_POOL_MAX_THREADS;

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
     * 如果 maxThreads 超过 {@link #MAX_RECOMMENDED_THREADS}（2048），
     * 实际最大线程数会被截断为 {@link #MAX_RECOMMENDED_THREADS}。
     * 这是为了防止无限制创建线程导致资源耗尽。
     *
     * @param namePrefix  线程名称前缀
     * @param maxThreads  最大线程数，建议不超过 256，上限受 {@link #MAX_RECOMMENDED_THREADS} 约束
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
     * 创建固定大小线程池，namePrefix 在前
     *
     * @param namePrefix 线程名称前缀
     * @param nThreads 线程数量
     * @param queueSize 队列容量
     * @return ExecutorService 实例
     * @see ThreadPoolExecutor.CallerRunsPolicy 拒绝策略：任务由调用线程同步执行，确保无任务丢失
     */
    public static ExecutorService newFixedThreadPool(String namePrefix, int nThreads, int queueSize) {
        return newFixedThreadPool(nThreads, queueSize, namePrefix);
    }

    /**
     * 创建调度线程池
     *
     * 内部使用 ScheduledThreadPoolExecutor，支持 schedule / scheduleAtFixedRate 等定时任务
     *
     * @param corePoolSize 核心线程数
     * @param namePrefix   线程名称前缀
     * @return ScheduledExecutorService 实例
     * @see ScheduledThreadPoolExecutor
     * @see ThreadPoolExecutor.CallerRunsPolicy 拒绝策略：任务由调用线程同步执行，确保无任务丢失
     */
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, String namePrefix) {
        return new ScheduledThreadPoolExecutor(
                corePoolSize,
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

    // ==================== 线程池监控 ====================

    /**
     * 获取当前线程池大小
     *
     * @param executor 线程池
     * @return 当前线程数，非 ThreadPoolExecutor 返回 -1
     * @deprecated 使用 {@link #getPoolSizeOpt(ExecutorService)} 替代，返回 OptionalInt 避免 sentinel 值
     */
    @Deprecated
    public static int getPoolSize(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executor).getPoolSize();
        }
        return -1;
    }

    /**
     * 获取当前线程池大小，返回 OptionalInt
     *
     * 空值表示无法获取（非 ThreadPoolExecutor 实例）
     *
     * @param executor 线程池
     * @return OptionalInt，包含当前线程数；空表示无法获取
     */
    public static OptionalInt getPoolSizeOpt(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor) {
            return OptionalInt.of(((ThreadPoolExecutor) executor).getPoolSize());
        }
        return OptionalInt.empty();
    }

    /**
     * 获取活跃线程数
     *
     * @param executor 线程池
     * @return 活跃线程数，非 ThreadPoolExecutor 返回 -1
     * @deprecated 使用 {@link #getActiveCountOpt(ExecutorService)} 替代，返回 OptionalInt 避免 sentinel 值
     */
    @Deprecated
    public static int getActiveCount(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executor).getActiveCount();
        }
        return -1;
    }

    /**
     * 获取活跃线程数，返回 OptionalInt
     *
     * 空值表示无法获取（非 ThreadPoolExecutor 实例）
     *
     * @param executor 线程池
     * @return OptionalInt，包含活跃线程数；空表示无法获取
     */
    public static OptionalInt getActiveCountOpt(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor) {
            return OptionalInt.of(((ThreadPoolExecutor) executor).getActiveCount());
        }
        return OptionalInt.empty();
    }

    /**
     * 获取队列长度
     *
     * @param executor 线程池
     * @return 队列长度，非 ThreadPoolExecutor 返回 -1
     * @deprecated 使用 {@link #getQueueSizeOpt(ExecutorService)} 替代，返回 OptionalInt 避免 sentinel 值
     */
    @Deprecated
    public static int getQueueSize(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executor).getQueue().size();
        }
        return -1;
    }

    /**
     * 获取队列长度，返回 OptionalInt
     *
     * 空值表示无法获取（非 ThreadPoolExecutor 实例）
     *
     * @param executor 线程池
     * @return OptionalInt，包含队列长度；空表示无法获取
     */
    public static OptionalInt getQueueSizeOpt(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor) {
            return OptionalInt.of(((ThreadPoolExecutor) executor).getQueue().size());
        }
        return OptionalInt.empty();
    }

    /**
     * 获取已完成任务数
     *
     * @param executor 线程池
     * @return 已完成任务数，非 ThreadPoolExecutor 返回 -1L
     * @deprecated 使用 {@link #getCompletedTaskCountOpt(ExecutorService)} 替代，返回 OptionalLong 避免 sentinel 值
     */
    @Deprecated
    public static long getCompletedTaskCount(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executor).getCompletedTaskCount();
        }
        return -1L;
    }

    /**
     * 获取已完成任务数，返回 OptionalLong
     *
     * 空值表示无法获取（非 ThreadPoolExecutor 实例）
     *
     * @param executor 线程池
     * @return OptionalLong，包含已完成任务数；空表示无法获取
     */
    public static OptionalLong getCompletedTaskCountOpt(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor) {
            return OptionalLong.of(((ThreadPoolExecutor) executor).getCompletedTaskCount());
        }
        return OptionalLong.empty();
    }

    /**
     * 获取任务总数
     *
     * @param executor 线程池
     * @return 任务总数，非 ThreadPoolExecutor 返回 -1L
     * @deprecated 使用 {@link #getTaskCountOpt(ExecutorService)} 替代，返回 OptionalLong 避免 sentinel 值
     */
    @Deprecated
    public static long getTaskCount(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executor).getTaskCount();
        }
        return -1L;
    }

    /**
     * 获取任务总数，返回 OptionalLong
     *
     * 空值表示无法获取（非 ThreadPoolExecutor 实例）
     *
     * @param executor 线程池
     * @return OptionalLong，包含任务总数；空表示无法获取
     */
    public static OptionalLong getTaskCountOpt(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor) {
            return OptionalLong.of(((ThreadPoolExecutor) executor).getTaskCount());
        }
        return OptionalLong.empty();
    }

    /**
     * 获取核心线程数
     *
     * @param executor 线程池
     * @return 核心线程数，非 ThreadPoolExecutor 返回 -1
     */
    public static int getCorePoolSize(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executor).getCorePoolSize();
        }
        return -1;
    }

    /**
     * 获取最大线程数
     *
     * @param executor 线程池
     * @return 最大线程数，非 ThreadPoolExecutor 返回 -1
     */
    public static int getMaximumPoolSize(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executor).getMaximumPoolSize();
        }
        return -1;
    }
}