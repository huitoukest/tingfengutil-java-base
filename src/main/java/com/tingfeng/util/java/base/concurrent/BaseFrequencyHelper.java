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
    private Integer secondMaxCount;

    public BaseFrequencyHelper() {

    }

    public BaseFrequencyHelper(Integer secondMaxCount) {
        this.secondMaxCount = secondMaxCount;
    }

    /**
     * 检查频率是否超出限制（非阻塞）
     * @return true 表示超出频率限制，调用方应适当暂停；false 表示未超出，可继续执行
     */
    public boolean isRateLimited() {
        long currentSecond = System.currentTimeMillis() / 1000;
        long last = lastSecond.get();

        if (currentSecond != last) {
            // CAS 循环：只有一个线程能成功将 lastSecond 更新为 currentSecond
            lastSecond.compareAndSet(last, currentSecond);
            incrementalCount.set(0);
            return false;
        }

        long count = incrementalCount.incrementAndGet();
        return count > secondMaxCount;
    }

    /**
     * 获取达到条件后的随眠时间，在每一次有访问时调用
     * @param incrementalCount 当期秒内的访问次数
     * @param secondMaxCount 每秒的最大访问次数
     * @return the sleepTime ,unit  millisecond 毫秒
     */
    public abstract long getSleepTimeWhileOverMaxCount(int incrementalCount, int secondMaxCount);
}
