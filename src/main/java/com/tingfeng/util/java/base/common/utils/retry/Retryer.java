package com.tingfeng.util.java.base.common.utils.retry;

import com.tingfeng.util.java.base.concurrent.ThreadUtils;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * 重试执行器。构建后不可变，线程安全。
 * <p>
 * 通过 {@link RetryerBuilder} 构建，支持同步（{@link #call(Callable)}）
 * 和异步（{@link #callAsync(Supplier)}）两种执行方式。
 * </p>
 *
 * @param <T> 重试任务返回类型
 */
public class Retryer<T> {

    private final int maxAttempts;
    private final WaitStrategy waitStrategy;
    private final StopStrategy stopStrategy;
    private final RetryCondition<T> retryCondition;
    private final RecoveryCallback<T> recoveryCallback;
    private final ScheduledExecutorService scheduledExecutor;

    /**
     * 包私有构造器，由 {@link RetryerBuilder#build()} 调用。
     */
    Retryer(int maxAttempts, WaitStrategy waitStrategy, StopStrategy stopStrategy,
            RetryCondition<T> retryCondition, RecoveryCallback<T> recoveryCallback,
            ScheduledExecutorService scheduledExecutor) {
        this.maxAttempts = maxAttempts;
        this.waitStrategy = waitStrategy;
        this.stopStrategy = stopStrategy;
        this.retryCondition = retryCondition;
        this.recoveryCallback = recoveryCallback;
        this.scheduledExecutor = scheduledExecutor;
    }

    /**
     * 同步执行重试任务。
     * <p>
     * 循环执行流程：执行任务 → 判断是否停止 → 判断是否重试 → 等待 → 继续。
     * 响应线程中断（通过 {@link ThreadUtils#sleep(long)} 的中断处理机制）。
     * </p>
     *
     * @param task 待执行的任务，不能为 null
     * @return 任务执行结果
     * @throws NullPointerException        task 为 null
     * @throws RetryExhaustedException     所有重试耗尽且未设置 RecoveryCallback
     * @throws RuntimeException            任务执行过程中抛出的非受检异常
     */
    public T call(Callable<T> task) {
        Objects.requireNonNull(task, "task must not be null");

        int attempt = 0;
        long startTime = System.nanoTime();

        while (true) {
            attempt++;
            T result = null;
            Throwable exception = null;

            try {
                result = task.call();
            } catch (Exception e) {
                exception = e;
            }

            long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;

            // 先判断停止策略
            if (stopStrategy.shouldStop(attempt, elapsedMs)) {
                if (exception != null) {
                    if (recoveryCallback != null) {
                        return recoveryCallback.recover(exception);
                    }
                    throw new RetryExhaustedException(
                            "Retry exhausted after " + attempt + " attempts", exception);
                }
                return result;
            }

            // 再判断重试条件
            if (!retryCondition.shouldRetry(result, exception)) {
                if (exception != null) {
                    if (exception instanceof RuntimeException) {
                        throw (RuntimeException) exception;
                    }
                    if (exception instanceof Error) {
                        throw (Error) exception;
                    }
                    throw new RuntimeException(exception);
                }
                return result;
            }

            // 进入下一次重试前等待
            long delayMs = waitStrategy.computeWaitTime(attempt);
            if (delayMs > 0) {
                ThreadUtils.sleep(delayMs);
                // 如果线程在 sleep 中被中断，检查中断标志并停止重试
                if (Thread.currentThread().isInterrupted()) {
                    if (exception != null) {
                        if (recoveryCallback != null) {
                            return recoveryCallback.recover(exception);
                        }
                        throw new RetryExhaustedException(
                                "Retry interrupted after " + attempt + " attempts", exception);
                    }
                    return result;
                }
            }
        }
    }

    /**
     * 异步执行重试任务。
     * <p>
     * 使用构造时传入的 {@link ScheduledExecutorService} 进行延迟调度。
     * 非阻塞，返回 {@link CompletableFuture} 表示异步结果。
     * </p>
     *
     * @param asyncTask 返回 {@link CompletableFuture} 的任务供应器，不能为 null
     * @return 表示异步结果的 CompletableFuture
     * @throws NullPointerException     asyncTask 为 null
     * @throws IllegalStateException    未配置 ScheduledExecutorService
     */
    public CompletableFuture<T> callAsync(Supplier<CompletableFuture<T>> asyncTask) {
        Objects.requireNonNull(asyncTask, "asyncTask must not be null");
        if (scheduledExecutor == null) {
            throw new IllegalStateException(
                    "ScheduledExecutorService required for async retry. "
                    + "Call withScheduledExecutor() on the builder.");
        }

        long startTime = System.nanoTime();
        return doCallAsync(asyncTask, 0, startTime);
    }

    /**
     * 异步重试的内部递归执行。
     *
     * @param asyncTask 任务供应器
     * @param attempt   已完成的尝试次数
     * @param startTime 首次尝试的开始时间（纳秒）
     * @return 表示异步结果的 CompletableFuture
     */
    private CompletableFuture<T> doCallAsync(Supplier<CompletableFuture<T>> asyncTask,
                                              int attempt, long startTime) {
        CompletableFuture<T> cf = new CompletableFuture<>();
        int currentAttempt = attempt + 1;

        try {
            CompletableFuture<T> taskFuture = asyncTask.get();
            if (taskFuture == null) {
                cf.completeExceptionally(new NullPointerException(
                        "asyncTask returned null CompletableFuture"));
                return cf;
            }

            taskFuture.whenComplete((result, ex) -> {
                long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;

                // 先判断停止策略
                if (stopStrategy.shouldStop(currentAttempt, elapsedMs)) {
                    if (ex != null) {
                        if (recoveryCallback != null) {
                            cf.complete(recoveryCallback.recover(ex));
                        } else {
                            cf.completeExceptionally(new RetryExhaustedException(
                                    "Retry exhausted after " + currentAttempt + " attempts", ex));
                        }
                    } else {
                        cf.complete(result);
                    }
                    return;
                }

                // 将 Throwable 转为 Exception 用于 retryCondition 判断
                Throwable retryException = (ex instanceof Exception || ex == null) ? ex
                        : new RuntimeException(ex);

                // 再判断重试条件
                if (!retryCondition.shouldRetry(result, retryException)) {
                    if (ex != null) {
                        cf.completeExceptionally(ex);
                    } else {
                        cf.complete(result);
                    }
                    return;
                }

                // 进入下一次重试
                long delayMs = waitStrategy.computeWaitTime(currentAttempt);
                scheduledExecutor.schedule(() -> {
                    doCallAsync(asyncTask, currentAttempt, startTime)
                            .whenComplete((r, e) -> {
                                if (e != null) {
                                    cf.completeExceptionally(e);
                                } else {
                                    cf.complete(r);
                                }
                            });
                }, delayMs, MILLISECONDS);
            });
        } catch (Exception e) {
            cf.completeExceptionally(e);
        }

        return cf;
    }
}
