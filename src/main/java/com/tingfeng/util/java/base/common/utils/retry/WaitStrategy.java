package com.tingfeng.util.java.base.common.utils.retry;

/**
 * 等待策略 —— 计算每次重试前的等待时间。
 * <p>
 * 函数式接口，可与 lambda 表达式无缝配合：
 * <pre>{@code
 * WaitStrategy strategy = attempt -> attempt * 100L;
 * }</pre>
 * </p>
 */
@FunctionalInterface
public interface WaitStrategy {

    /**
     * 计算第 {@code attempt} 次重试前的等待时间。
     *
     * @param attempt 当前尝试次数（从 1 开始计数）
     * @return 等待毫秒数，必须大于等于 0
     */
    long computeWaitTime(int attempt);
}
