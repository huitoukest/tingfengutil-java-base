package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.utils.retry.Retryer;
import com.tingfeng.util.java.base.common.utils.retry.RetryerBuilder;
import com.tingfeng.util.java.base.common.utils.retry.strategies.ExponentialBackoffWaitStrategy;
import com.tingfeng.util.java.base.common.utils.retry.strategies.FixedWaitStrategy;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/**
 * 重试工具门面 —— 推门即用的重试操作入口。
 * <p>
 * 快速用法：
 * <pre>{@code
 * // 简单重试 3 次（默认指数退避等待）
 * String result = RetryUtils.retry(3, () -> httpClient.get("/api"));
 *
 * // 固定间隔重试
 * String result = RetryUtils.retry(3, 1000L, () -> httpClient.get("/api"));
 *
 * // 指数退避
 * String result = RetryUtils.retryWithBackoff(5, 200L, () -> httpClient.get("/api"));
 *
 * // 异步重试
 * CompletableFuture<String> future = RetryUtils.retryAsync(3, 500L,
 *     () -> CompletableFuture.supplyAsync(() -> httpClient.get("/api")), executor);
 *
 * // 自定义策略
 * Retryer<String> retryer = RetryUtils.<String>builder()
 *     .maxAttempts(5)
 *     .waitStrategy(new ExponentialBackoffWaitStrategy(200, 2.0, 5000))
 *     .retryOn(IOException.class)
 *     .build();
 * String result = retryer.call(() -> httpClient.get("/api"));
 * }</pre>
 *
 * 注意：异步重试需要传入 {@link ScheduledExecutorService}，工具类不自建线程池。
 */
public final class RetryUtils {

    /** 默认最大尝试次数（包含首次执行） */
    public static final int DEFAULT_MAX_ATTEMPTS = 3;

    private RetryUtils() {
    }

    // ---- 简单重试 ----

    /**
     * 简单重试，使用默认指数退避等待策略（初始 500ms，乘数 2.0，上限 10000ms）。
     * <p>
     * 重试耗尽且未设置恢复回调时将抛出 {@link com.tingfeng.util.java.base.common.utils.retry.RetryExhaustedException}。
     * </p>
     *
     * @param maxAttempts 最大尝试次数（包含首次执行），必须大于 0
     * @param task        待执行的任务，不能为 null
     * @param <T>         任务返回类型
     * @return 任务执行结果
     * @throws NullPointerException     task 为 null
     * @throws IllegalArgumentException maxAttempts 小于等于 0
     */
    public static <T> T retry(int maxAttempts, Callable<T> task) {
        Objects.requireNonNull(task, "task must not be null");
        return RetryUtils.<T>builder()
                .maxAttempts(maxAttempts)
                .build()
                .call(task);
    }

    /**
     * 固定间隔重试。
     * <p>
     * 每次重试前等待固定毫秒数。
     * </p>
     *
     * @param maxAttempts 最大尝试次数（包含首次执行），必须大于 0
     * @param delayMs     每次重试前的等待毫秒数
     * @param task        待执行的任务，不能为 null
     * @param <T>         任务返回类型
     * @return 任务执行结果
     * @throws NullPointerException     task 为 null
     * @throws IllegalArgumentException maxAttempts 小于等于 0
     */
    public static <T> T retry(int maxAttempts, long delayMs, Callable<T> task) {
        Objects.requireNonNull(task, "task must not be null");
        return RetryUtils.<T>builder()
                .maxAttempts(maxAttempts)
                .waitStrategy(new FixedWaitStrategy(delayMs))
                .build()
                .call(task);
    }

    /**
     * 指数退避重试。
     * <p>
     * 每次重试的等待时间按指数增长：initialDelayMs * 2^(attempt-1)。
     * </p>
     *
     * @param maxAttempts    最大尝试次数（包含首次执行），必须大于 0
     * @param initialDelayMs 初始等待毫秒数
     * @param task           待执行的任务，不能为 null
     * @param <T>            任务返回类型
     * @return 任务执行结果
     * @throws NullPointerException     task 为 null
     * @throws IllegalArgumentException maxAttempts 小于等于 0
     */
    public static <T> T retryWithBackoff(int maxAttempts, long initialDelayMs, Callable<T> task) {
        Objects.requireNonNull(task, "task must not be null");
        return RetryUtils.<T>builder()
                .maxAttempts(maxAttempts)
                .waitStrategy(new ExponentialBackoffWaitStrategy(initialDelayMs, 2.0, 10000L))
                .build()
                .call(task);
    }

    // ---- 异步重试 ----

    /**
     * 异步重试，使用固定间隔等待策略。
     * <p>
     * 非阻塞，返回 {@link CompletableFuture} 表示异步结果。
     * 注意：传入的 {@link ScheduledExecutorService} 需由调用方管理生命周期。
     * </p>
     *
     * @param maxAttempts 最大尝试次数（包含首次执行），必须大于 0
     * @param delayMs     每次重试前的等待毫秒数
     * @param asyncTask   返回 {@link CompletableFuture} 的任务供应器，不能为 null
     * @param executor    用于延迟调度的调度器，不能为 null
     * @param <T>         任务返回类型
     * @return 表示异步结果的 CompletableFuture
     * @throws NullPointerException     asyncTask 或 executor 为 null
     * @throws IllegalArgumentException maxAttempts 小于等于 0
     */
    public static <T> CompletableFuture<T> retryAsync(int maxAttempts, long delayMs,
                                                       Supplier<CompletableFuture<T>> asyncTask,
                                                       ScheduledExecutorService executor) {
        Objects.requireNonNull(asyncTask, "asyncTask must not be null");
        Objects.requireNonNull(executor, "executor must not be null");
        return RetryUtils.<T>builder()
                .maxAttempts(maxAttempts)
                .waitStrategy(new FixedWaitStrategy(delayMs))
                .withScheduledExecutor(executor)
                .build()
                .callAsync(asyncTask);
    }

    // ---- Builder 入口 ----

    /**
     * 获取 {@link RetryerBuilder} 实例，用于自定义重试策略。
     *
     * @param <T> 重试任务返回类型
     * @return 新的 RetryerBuilder 实例
     */
    public static <T> RetryerBuilder<T> builder() {
        return new RetryerBuilder<>();
    }
}
