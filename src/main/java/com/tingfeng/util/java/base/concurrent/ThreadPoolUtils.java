package com.tingfeng.util.java.base.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 线程池工具类
 * <p>
 * 提供线程池的创建、监控、优雅关闭等操作
 * </p>
 */
public final class ThreadPoolUtils {

    private ThreadPoolUtils() {
    }

    /**
     * 线程池配置 DTO
     * <p>
     * 封装线程池的完整配置参数，支持-builder 模式或直接构造
     * </p>
     */
    public static class ThreadPoolConfig {
        private int corePoolSize;
        private int maxPoolSize;
        private long keepAliveTime;
        private TimeUnit unit;
        private BlockingQueue<Runnable> workQueue;
        private ThreadFactory threadFactory;
        private RejectedExecutionHandler handler;

        public ThreadPoolConfig() {
        }

        public ThreadPoolConfig(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
                               RejectedExecutionHandler handler) {
            this.corePoolSize = corePoolSize;
            this.maxPoolSize = maxPoolSize;
            this.keepAliveTime = keepAliveTime;
            this.unit = unit;
            this.workQueue = workQueue;
            this.threadFactory = threadFactory;
            this.handler = handler;
        }

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public long getKeepAliveTime() {
            return keepAliveTime;
        }

        public void setKeepAliveTime(long keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
        }

        public TimeUnit getUnit() {
            return unit;
        }

        public void setUnit(TimeUnit unit) {
            this.unit = unit;
        }

        public BlockingQueue<Runnable> getWorkQueue() {
            return workQueue;
        }

        public void setWorkQueue(BlockingQueue<Runnable> workQueue) {
            this.workQueue = workQueue;
        }

        public ThreadFactory getThreadFactory() {
            return threadFactory;
        }

        public void setThreadFactory(ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
        }

        public RejectedExecutionHandler getHandler() {
            return handler;
        }

        public void setHandler(RejectedExecutionHandler handler) {
            this.handler = handler;
        }

        /**
         * 创建默认配置的 ThreadPoolExecutor
         *
         * @return ExecutorService 实例
         */
        public ExecutorService toExecutorService() {
            return new java.util.concurrent.ThreadPoolExecutor(
                    corePoolSize, maxPoolSize,
                    keepAliveTime, unit,
                    workQueue != null ? workQueue : new LinkedBlockingQueue<>(),
                    threadFactory != null ? threadFactory : ThreadFactoryUtils.newNamedThreadFactory("pool-", false),
                    handler != null ? handler : new java.util.concurrent.ThreadPoolExecutor.AbortPolicy()
            );
        }

        /**
         * Builder 模式开始构建
         *
         * @return Builder 实例
         */
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private int corePoolSize;
            private int maxPoolSize;
            private long keepAliveTime;
            private TimeUnit unit;
            private BlockingQueue<Runnable> workQueue;
            private ThreadFactory threadFactory;
            private RejectedExecutionHandler handler;

            public Builder corePoolSize(int corePoolSize) {
                this.corePoolSize = corePoolSize;
                return this;
            }

            public Builder maxPoolSize(int maxPoolSize) {
                this.maxPoolSize = maxPoolSize;
                return this;
            }

            public Builder keepAliveTime(long keepAliveTime) {
                this.keepAliveTime = keepAliveTime;
                return this;
            }

            public Builder unit(TimeUnit unit) {
                this.unit = unit;
                return this;
            }

            public Builder workQueue(BlockingQueue<Runnable> workQueue) {
                this.workQueue = workQueue;
                return this;
            }

            public Builder threadFactory(ThreadFactory threadFactory) {
                this.threadFactory = threadFactory;
                return this;
            }

            public Builder handler(RejectedExecutionHandler handler) {
                this.handler = handler;
                return this;
            }

            public ThreadPoolConfig build() {
                return new ThreadPoolConfig(corePoolSize, maxPoolSize, keepAliveTime, unit,
                        workQueue, threadFactory, handler);
            }
        }
    }

    // ==================== 基础线程池创建 ====================

    /**
     * 创建固定大小的命名线程池
     *
     * @param nThreads   线程数量
     * @param namePrefix 线程名称前缀
     * @return ExecutorService 实例
     */
    public static ExecutorService newFixedThreadPool(int nThreads, String namePrefix) {
        return new java.util.concurrent.ThreadPoolExecutor(
                nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                ThreadFactoryUtils.newNamedThreadFactory(namePrefix + "-", false)
        );
    }

    /**
     * 创建缓存线程池
     *
     * @param namePrefix 线程名称前缀
     * @return ExecutorService 实例
     */
    public static ExecutorService newCachedThreadPool(String namePrefix) {
        return new java.util.concurrent.ThreadPoolExecutor(
                0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                ThreadFactoryUtils.newNamedThreadFactory(namePrefix + "-", false)
        );
    }

    /**
     * 创建单线程池
     *
     * @param namePrefix 线程名称前缀
     * @return ExecutorService 实例
     */
    public static ExecutorService newSingleThreadPool(String namePrefix) {
        return java.util.concurrent.Executors.newSingleThreadExecutor(
                ThreadFactoryUtils.newNamedThreadFactory(namePrefix + "-", false)
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
     */
    public static ExecutorService newFixedThreadPool(int corePoolSize, int maxPoolSize,
                                                     long keepAliveTime, TimeUnit unit,
                                                     int queueSize, String namePrefix) {
        return new java.util.concurrent.ThreadPoolExecutor(
                corePoolSize, maxPoolSize,
                keepAliveTime, unit,
                queueSize <= 0 ? new SynchronousQueue<>() : new LinkedBlockingQueue<>(queueSize),
                ThreadFactoryUtils.newNamedThreadFactory(namePrefix + "-", false)
        );
    }

    /**
     * 创建调度线程池
     *
     * @param corePoolSize 核心线程数
     * @param namePrefix   线程名称前缀
     * @return ExecutorService 实例
     */
    public static ExecutorService newScheduledThreadPool(int corePoolSize, String namePrefix) {
        return java.util.concurrent.Executors.newScheduledThreadPool(
                corePoolSize,
                ThreadFactoryUtils.newNamedThreadFactory(namePrefix + "-", false)
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
     * <p>
     * 先停止接收新任务，再等待正在执行的任务完成
     * </p>
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
