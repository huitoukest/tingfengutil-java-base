package com.tingfeng.util.java.base.common.utils.retry.strategies;

import com.tingfeng.util.java.base.common.utils.retry.StopStrategy;

/**
 * 永不停止策略 —— 永不主动停止重试。
 * <p>
 * 通常需配合 {@link com.tingfeng.util.java.base.common.utils.retry.RetryCondition} 使用，
 * 由条件决定何时停止重试。若单独使用此策略且无有效停止条件，将导致无限重试。
 * </p>
 */
public class NeverStopStrategy implements StopStrategy {

    @Override
    public boolean shouldStop(int attempt, long elapsedMs) {
        return false;
    }
}
