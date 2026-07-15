package com.tingfeng.util.java.base.common.utils.retry.strategies;

import com.tingfeng.util.java.base.common.utils.retry.WaitStrategy;

/**
 * 指数退避等待策略 —— 等待时间随尝试次数呈指数增长。
 * <p>
 * 计算公式：{@code delay = initialDelayMs * multiplier^(attempt - 1)}，
 * 结果上限为 {@code maxDelayMs}，下限为 0。
 * </p>
 */
public class ExponentialBackoffWaitStrategy implements WaitStrategy {

    private final long initialDelayMs;
    private final double multiplier;
    private final long maxDelayMs;

    /** 默认初始延迟 500ms */
    private static final long DEFAULT_INITIAL_DELAY_MS = 500L;
    /** 默认退避乘数 2.0 */
    private static final double DEFAULT_MULTIPLIER = 2.0;
    /** 默认最大延迟 10000ms */
    private static final long DEFAULT_MAX_DELAY_MS = 10000L;

    /**
     * 使用默认参数创建指数退避等待策略：初始 500ms，乘数 2.0，上限 10000ms。
     */
    public ExponentialBackoffWaitStrategy() {
        this(DEFAULT_INITIAL_DELAY_MS, DEFAULT_MULTIPLIER, DEFAULT_MAX_DELAY_MS);
    }

    /**
     * 创建指数退避等待策略。
     *
     * @param initialDelayMs 初始延迟毫秒数
     * @param multiplier     退避乘数（每次尝试的延迟倍数）
     * @param maxDelayMs     最大延迟毫秒数（上限）
     */
    public ExponentialBackoffWaitStrategy(long initialDelayMs, double multiplier, long maxDelayMs) {
        this.initialDelayMs = initialDelayMs;
        this.multiplier = multiplier;
        this.maxDelayMs = maxDelayMs;
    }

    @Override
    public long computeWaitTime(int attempt) {
        long delay = (long) (initialDelayMs * Math.pow(multiplier, attempt - 1));
        return Math.min(Math.max(0, delay), maxDelayMs);
    }
}
