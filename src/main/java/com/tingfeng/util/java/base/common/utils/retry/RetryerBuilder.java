package com.tingfeng.util.java.base.common.utils.retry;

import com.tingfeng.util.java.base.common.utils.RetryUtils;
import com.tingfeng.util.java.base.common.utils.retry.strategies.ExponentialBackoffWaitStrategy;
import com.tingfeng.util.java.base.common.utils.retry.strategies.MaxAttemptsStopStrategy;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;

/**
 * Retryer 构造器。线程不安全（每个实例仅供单线程构建）。
 * <p>
 * 通过 {@link RetryUtils#builder()} 获取实例，支持链式调用。
 * </p>
 *
 * @param <T> 重试任务返回类型
 */
public class RetryerBuilder<T> {

    private int maxAttempts = RetryUtils.DEFAULT_MAX_ATTEMPTS;
    private WaitStrategy waitStrategy;
    private StopStrategy stopStrategy;
    private RetryCondition<T> retryCondition;
    private RecoveryCallback<T> recoveryCallback;
    private ScheduledExecutorService scheduledExecutor;

    /**
     * 构造器，通过 {@link RetryUtils#builder()} 创建。
     */
    public RetryerBuilder() {
    }

    // ---- 配置方法 ----

    /**
     * 设置最大尝试次数（包含首次执行）。
     *
     * @param maxAttempts 最大尝试次数，必须大于 0
     * @return 当前构造器实例
     */
    public RetryerBuilder<T> maxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    /**
     * 设置等待策略。
     *
     * @param waitStrategy 等待策略，不能为 null
     * @return 当前构造器实例
     * @throws NullPointerException waitStrategy 为 null
     */
    public RetryerBuilder<T> waitStrategy(WaitStrategy waitStrategy) {
        this.waitStrategy = Objects.requireNonNull(waitStrategy, "waitStrategy must not be null");
        return this;
    }

    /**
     * 设置停止策略。
     *
     * @param stopStrategy 停止策略，不能为 null
     * @return 当前构造器实例
     * @throws NullPointerException stopStrategy 为 null
     */
    public RetryerBuilder<T> stopStrategy(StopStrategy stopStrategy) {
        this.stopStrategy = Objects.requireNonNull(stopStrategy, "stopStrategy must not be null");
        return this;
    }

    /**
     * 设置重试条件。
     *
     * @param retryCondition 重试条件，不能为 null
     * @return 当前构造器实例
     * @throws NullPointerException retryCondition 为 null
     */
    public RetryerBuilder<T> retryCondition(RetryCondition<T> retryCondition) {
        this.retryCondition = Objects.requireNonNull(retryCondition, "retryCondition must not be null");
        return this;
    }

    /**
     * 设置按异常类型重试的便捷方法。
     * <p>
     * 内部构建 {@link RetryCondition}，当执行抛出的异常匹配指定类型时重试。
     * 注意：此方法会覆盖之前通过 {@link #retryCondition(RetryCondition)} 或
     * {@link #retryIf(Predicate)} 设置的条件。
     * </p>
     *
     * @param exceptionTypes 需要重试的异常类型
     * @return 当前构造器实例
     */
    @SafeVarargs
    public final RetryerBuilder<T> retryOn(Class<? extends Throwable>... exceptionTypes) {
        Objects.requireNonNull(exceptionTypes, "exceptionTypes must not be null");
        this.retryCondition = (result, ex) -> {
            if (ex == null) {
                return false;
            }
            for (Class<? extends Throwable> type : exceptionTypes) {
                if (type.isInstance(ex)) {
                    return true;
                }
            }
            return false;
        };
        return this;
    }

    /**
     * 设置按结果谓词重试的便捷方法。
     * <p>
     * 内部构建 {@link RetryCondition}，当无异常且结果满足谓词时重试。
     * 有异常时仍按默认行为重试。
     * 注意：此方法会覆盖之前通过 {@link #retryCondition(RetryCondition)} 或
     * {@link #retryOn(Class[])} 设置的条件。
     * </p>
     *
     * @param resultPredicate 结果谓词，不能为 null
     * @return 当前构造器实例
     * @throws NullPointerException resultPredicate 为 null
     */
    public RetryerBuilder<T> retryIf(Predicate<T> resultPredicate) {
        Objects.requireNonNull(resultPredicate, "resultPredicate must not be null");
        this.retryCondition = (result, ex) -> {
            if (ex != null) {
                return true;
            }
            return resultPredicate.test(result);
        };
        return this;
    }

    /**
     * 设置恢复回调，在重试耗尽时返回降级结果。
     *
     * @param recoveryCallback 恢复回调，不能为 null
     * @return 当前构造器实例
     * @throws NullPointerException recoveryCallback 为 null
     */
    public RetryerBuilder<T> recover(RecoveryCallback<T> recoveryCallback) {
        this.recoveryCallback = Objects.requireNonNull(recoveryCallback, "recoveryCallback must not be null");
        return this;
    }

    /**
     * 设置用于异步重试的调度器。
     * <p>
     * 异步重试（{@link Retryer#callAsync(java.util.function.Supplier)}）必需此参数，
     * 否则将抛出 {@link IllegalStateException}。
     * </p>
     *
     * @param executor 调度器服务，不能为 null
     * @return 当前构造器实例
     * @throws NullPointerException executor 为 null
     */
    public RetryerBuilder<T> withScheduledExecutor(ScheduledExecutorService executor) {
        this.scheduledExecutor = Objects.requireNonNull(executor, "executor must not be null");
        return this;
    }

    // ---- 构建 ----

    /**
     * 构建 {@link Retryer} 实例。
     * <p>
     * 如果未显式设置等待策略，默认使用 {@link ExponentialBackoffWaitStrategy}（初始 500ms，乘数 2.0，上限 10000ms）。
     * 如果未显式设置停止策略，默认使用 {@link MaxAttemptsStopStrategy}（基于当前 maxAttempts 值）。
     * 如果未显式设置重试条件，默认按异常重试（有异常即重试）。
     * </p>
     *
     * @return 不可变的 Retryer 实例
     * @throws IllegalArgumentException maxAttempts 小于等于 0
     */
    public Retryer<T> build() {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException(
                    "maxAttempts must be positive, but got: " + maxAttempts);
        }

        // 设置默认策略
        WaitStrategy ws = waitStrategy != null ? waitStrategy
                : new ExponentialBackoffWaitStrategy();
        StopStrategy ss = stopStrategy != null ? stopStrategy
                : new MaxAttemptsStopStrategy(maxAttempts);
        RetryCondition<T> rc = retryCondition != null ? retryCondition
                : (result, ex) -> ex != null;

        return new Retryer<>(maxAttempts, ws, ss, rc, recoveryCallback, scheduledExecutor);
    }
}
