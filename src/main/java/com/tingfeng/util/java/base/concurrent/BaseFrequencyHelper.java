package com.tingfeng.util.java.base.concurrent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 每秒的频率控制类，非阻塞设计
 */
public abstract class BaseFrequencyHelper {
    /**
     * 每秒内的增量计数器，使用 AtomicLong 提供线程安全
     */
    private final AtomicLong incrementalCount = new AtomicLong(0);
    private final AtomicLong lastSecond = new AtomicLong(System.currentTimeMillis() / 1000);

    /**
     * 每秒的最大访问次数
     */
    private int secondMaxCount;

    public BaseFrequencyHelper() {
        this.secondMaxCount = Integer.MAX_VALUE;
    }

    public BaseFrequencyHelper(int secondMaxCount) {
        this.secondMaxCount = secondMaxCount;
    }

    /**
     * 检查频率是否超出限制。
     * 
     * 此方法为非阻塞设计，仅返回布尔值表示当前是否超出频率限制。
     * 如果需要阻塞等待直到频率限制解除，请使用 {@link #waitIfRateLimited()}。
     *
     * @return true 表示超出频率限制，调用方应适当暂停；false 表示未超出，可继续执行
     */
    public boolean isRateLimited() {
        long currentSecond = System.currentTimeMillis() / 1000;
        long lastSecondValue = lastSecond.get();

        if (currentSecond != lastSecondValue) {
            synchronized (this) {
                // double-check：防止多个线程同时进入导致竞态
                if (currentSecond != lastSecond.get()) {
                    lastSecond.set(currentSecond);
                    incrementalCount.set(0);
                }
            }
        }
        // 同秒内无锁（CAS 路径）
        int count = (int) incrementalCount.incrementAndGet();
        if (secondMaxCount < 0) {
            return false; // 无限制
        }
        return count > secondMaxCount;
    }

    /**
     * 获取超出最大次数后的休眠时间。
     * 
     * 此方法不应阻塞线程，仅返回休眠毫秒数；阻塞行为由 {@link #waitIfRateLimited()} 处理。
     *
     * @param incrementalCount 当前秒内的访问次数
     * @param secondMaxCount 每秒的最大访问次数
     * @return 休眠时间，单位毫秒
     */
    public abstract long getSleepTimeWhileOverMaxCount(int incrementalCount, int secondMaxCount);

    /**
     * 阻塞等待直到速率限制解除。
     *
     * 当 {@link #isRateLimited()} 返回 true 时，调用 {@link #getSleepTimeWhileOverMaxCount(int, int)}
     * 获取等待时长并执行 {@link Thread#sleep(long)}，循环直到速率限制解除。
     * 如果返回的休眠时间小于等于 0，将默认使用 1 毫秒以防止 CPU 空转。
     *
     * @throws InterruptedException 如果当前线程被中断，恢复中断标志后抛出
     */
    public void waitIfRateLimited() throws InterruptedException {
        while (isRateLimited()) {
            long sleepTime = getSleepTimeWhileOverMaxCount((int) incrementalCount.get(), secondMaxCount);
            if (sleepTime <= 0) {
                sleepTime = 1; // 防止 CPU 忙等
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 恢复中断标志
                throw e;
            }
        }
    }
}
