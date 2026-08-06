package com.tingfeng.util.java.base.io;

/**
 * 预支令牌桶限速器（package-private，供限速流共享）
 *
 * V2 修订设计（消除 V1 死循环与批次效应）：
 * - 先记账后等待：acquire 先扣减 tokens（允许为负 = 预支欠账），不足时单次 sleep，无 while 循环，任意 n 必然返回
 * - 连续比例补充：refill 按真实 elapsed 比例补充令牌，消除整间隔 batch 效应
 * - 初始满桶：tokens 初始 = burst（一个 interval 配额），创建后前 burst 字节零等待
 * - burst 仅截断正方向：空闲积累不超过一个配额，抑制突发
 *
 * 限速为平均上限语义（依赖 Thread.sleep 毫秒精度）：sleep 提前返回时欠账残留为负，
 * 后续 refill 按真实 elapsed 偿还，长期平均速率不超过设定值；非硬实时。
 *
 * @author huitoukest
 */
final class RateLimiter {

    private final double ratePerSecond;
    private final double burst;
    private double tokens;
    private long lastRefillNanos;

    /**
     * 创建限速器
     * @param ratePerSecond 每秒字节数（大于 0，由调用方校验）
     * @param intervalMillis 平滑间隔毫秒数（1-1000，由调用方校验）
     */
    RateLimiter(long ratePerSecond, long intervalMillis) {
        this.ratePerSecond = ratePerSecond;
        this.burst = ratePerSecond * intervalMillis / 1000.0;
        this.tokens = this.burst;
        this.lastRefillNanos = System.nanoTime();
    }

    /**
     * 按真实流逝时间补充令牌（连续比例）
     * elapsed 小于等于 0（nanoTime 回绕防御）不补充；补充后不超过 burst 上限
     */
    private void refill() {
        long now = System.nanoTime();
        long elapsed = now - lastRefillNanos;
        if (elapsed <= 0) {
            return;
        }
        tokens = Math.min(burst, tokens + elapsed / 1e9 * ratePerSecond);
        lastRefillNanos = now;
    }

    /**
     * 申请 n 字节的放行额度（先记账后等待，无循环）
     * 令牌不足时按缺口计算等待毫秒数并单次 sleep（下限 1ms 防忙等）；
     * 等待被中断 → 恢复中断标志并抛项目 IOException
     * @param n 申请字节数（非负，由调用方保证）
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 等待被中断
     */
    void acquire(long n) {
        refill();
        tokens -= n;
        if (tokens < 0) {
            long waitMillis = (long) (-tokens / ratePerSecond * 1000.0);
            if (waitMillis <= 0) {
                waitMillis = 1; // 防忙等（BaseFrequencyHelper 先例）
            }
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 恢复中断标志
                throw new com.tingfeng.util.java.base.lang.exception.IOException(
                    "Interrupted while rate limiting", e);
            }
        }
    }
}
