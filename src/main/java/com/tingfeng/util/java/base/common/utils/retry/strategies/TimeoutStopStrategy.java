package com.tingfeng.util.java.base.common.utils.retry.strategies;

import com.tingfeng.util.java.base.common.utils.retry.StopStrategy;

/**
 * 超时停止策略 —— 当已耗时间达到超时上限时停止重试。
 * <p>
 * 判断逻辑：{@code elapsedMs >= timeoutMs} 时停止。
 * </p>
 */
public class TimeoutStopStrategy implements StopStrategy {

    private final long timeoutMs;

    /**
     * 创建超时停止策略。
     *
     * @param timeoutMs 超时毫秒数（必须大于 0）
     */
    public TimeoutStopStrategy(long timeoutMs) {
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException(
                    "timeoutMs must be > 0, but got: " + timeoutMs);
        }
        this.timeoutMs = timeoutMs;
    }

    @Override
    public boolean shouldStop(int attempt, long elapsedMs) {
        return elapsedMs >= timeoutMs;
    }
}
