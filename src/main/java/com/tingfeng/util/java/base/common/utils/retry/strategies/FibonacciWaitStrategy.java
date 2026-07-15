package com.tingfeng.util.java.base.common.utils.retry.strategies;

import com.tingfeng.util.java.base.common.utils.retry.WaitStrategy;

/**
 * 斐波那契退避等待策略 —— 等待时间按斐波那契数列增长。
 * <p>
 * 计算公式：{@code delay = fib(attempt) * baseDelayMs}，
 * 其中 {@code fib(1) = 1, fib(2) = 1, fib(3) = 2, fib(4) = 3, fib(5) = 5, ...}，
 * 结果上限为 {@code maxDelayMs}，下限为 0。
 * </p>
 */
public class FibonacciWaitStrategy implements WaitStrategy {

    private final long baseDelayMs;
    private final long maxDelayMs;

    /**
     * 创建斐波那契退避等待策略。
     *
     * @param baseDelayMs 基础延迟毫秒数（与斐波那契数的乘数）
     * @param maxDelayMs  最大延迟毫秒数（上限）
     */
    public FibonacciWaitStrategy(long baseDelayMs, long maxDelayMs) {
        this.baseDelayMs = baseDelayMs;
        this.maxDelayMs = maxDelayMs;
    }

    @Override
    public long computeWaitTime(int attempt) {
        long fib = fibonacci(attempt);
        return Math.min(Math.max(0, fib * baseDelayMs), maxDelayMs);
    }

    /**
     * 迭代计算第 n 个斐波那契数（从 1 开始计数）。
     *
     * @param n 斐波那契数列的索引（从 1 开始）
     * @return 第 n 个斐波那契数
     */
    private static long fibonacci(int n) {
        if (n <= 0) {
            return 0;
        }
        if (n == 1 || n == 2) {
            return 1;
        }
        long a = 1;
        long b = 1;
        for (int i = 3; i <= n; i++) {
            long c = a + b;
            a = b;
            b = c;
        }
        return b;
    }
}
