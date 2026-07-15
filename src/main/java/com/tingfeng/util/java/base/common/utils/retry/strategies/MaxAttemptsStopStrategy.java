package com.tingfeng.util.java.base.common.utils.retry.strategies;

import com.tingfeng.util.java.base.common.utils.retry.StopStrategy;

/**
 * 最大尝试次数停止策略 —— 当尝试次数达到上限时停止重试。
 * <p>
 * 判断逻辑：{@code attempt >= maxAttempts} 时停止。
 * </p>
 */
public class MaxAttemptsStopStrategy implements StopStrategy {

    private final int maxAttempts;

    /**
     * 创建最大尝试次数停止策略。
     *
     * @param maxAttempts 最大尝试次数（必须大于 0）
     */
    public MaxAttemptsStopStrategy(int maxAttempts) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException(
                    "maxAttempts must be > 0, but got: " + maxAttempts);
        }
        this.maxAttempts = maxAttempts;
    }

    @Override
    public boolean shouldStop(int attempt, long elapsedMs) {
        return attempt >= maxAttempts;
    }
}
