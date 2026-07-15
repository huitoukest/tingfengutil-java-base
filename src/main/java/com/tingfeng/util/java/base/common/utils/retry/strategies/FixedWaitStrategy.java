package com.tingfeng.util.java.base.common.utils.retry.strategies;

import com.tingfeng.util.java.base.common.utils.retry.WaitStrategy;

/**
 * 固定延迟等待策略 —— 每次重试等待相同的毫秒数。
 * <p>
 * 当 {@code delayMs} 小于 0 时按 0 处理（不等待）。
 * </p>
 */
public class FixedWaitStrategy implements WaitStrategy {

    private final long delayMs;

    /**
     * 创建固定延迟等待策略。
     *
     * @param delayMs 每次重试的等待毫秒数（小于 0 时按 0 处理）
     */
    public FixedWaitStrategy(long delayMs) {
        this.delayMs = delayMs;
    }

    @Override
    public long computeWaitTime(int attempt) {
        return Math.max(0, delayMs);
    }
}
