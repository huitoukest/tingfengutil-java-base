package com.tingfeng.util.java.base.common.utils.retry.strategies;

import com.tingfeng.util.java.base.common.utils.retry.WaitStrategy;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机化等待策略 —— 在基础延迟上增加随机偏移量。
 * <p>
 * 计算公式：{@code delay = baseDelayMs + random(0, rangeMs)}，
 * 结果下限为 0。
 * 使用 {@link ThreadLocalRandom} 保证线程安全。
 * </p>
 */
public class RandomizedWaitStrategy implements WaitStrategy {

    private final long baseDelayMs;
    private final long rangeMs;

    /**
     * 创建随机化等待策略。
     *
     * @param baseDelayMs 基础延迟毫秒数
     * @param rangeMs     随机偏移范围（实际偏移为 [0, rangeMs] 之间的随机值）
     */
    public RandomizedWaitStrategy(long baseDelayMs, long rangeMs) {
        this.baseDelayMs = baseDelayMs;
        this.rangeMs = rangeMs;
    }

    @Override
    public long computeWaitTime(int attempt) {
        return Math.max(0, baseDelayMs + ThreadLocalRandom.current().nextLong(rangeMs + 1));
    }
}
