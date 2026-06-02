package com.tingfeng.util.java.base.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 每秒的频率控制类，非阻塞设计
 */
public abstract class BaseFrequencyHelper {
    /**
     * 每秒内的增量
     */
    private final AtomicInteger incrementalCount = new AtomicInteger(0);
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
            // 时间戳变化，重置计数
            lastSecond.set(currentSecond);
            incrementalCount.set(0);
            return false;
        }

        int count = incrementalCount.incrementAndGet();
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
