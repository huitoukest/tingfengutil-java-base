package com.tingfeng.util.java.base.common.bean.test;

/**
 * 并发极限测试结果
 */
public class ConcurrencyLimitResult {

    private final int maxStableConcurrency;
    private final int failedConcurrency;
    private final long durationMs;
    private final String failureReason;

    public ConcurrencyLimitResult(int maxStableConcurrency, int failedConcurrency,
                                  long durationMs, String failureReason) {
        this.maxStableConcurrency = maxStableConcurrency;
        this.failedConcurrency = failedConcurrency;
        this.durationMs = durationMs;
        this.failureReason = failureReason;
    }

    public int getMaxStableConcurrency() {
        return maxStableConcurrency;
    }

    public int getFailedConcurrency() {
        return failedConcurrency;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public boolean hasFailed() {
        return failedConcurrency > 0;
    }
}
