package com.tingfeng.util.java.base.concurrent;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * 基于频率限制的辅助类
 * <p>
 * 继承 BaseFrequencyHelper，通过构造器参数提供睡眠时间计算策略，
 * 无需子类实现抽象方法
 * </p>
 *
 * @param <T> 上下文参数类型（未使用时可传 null）
 */
public class FrequencyLimitHelper<T> extends BaseFrequencyHelper {

    /**
     * 睡眠时间计算函数
     * <p>
     * 参数1：当前秒内的访问次数
     * 参数2：每秒的最大访问次数
     * 返回：需要睡眠的毫秒数
     */
    private final BiFunction<Integer, Integer, Long> sleepTimeCalculator;

    /**
     * 构造频率限制辅助类
     *
     * @param secondMaxCount        每秒的最大访问次数
     * @param sleepTimeCalculator   睡眠时间计算函数，不能为 null
     * @throws NullPointerException if sleepTimeCalculator is null
     */
    public FrequencyLimitHelper(Integer secondMaxCount,
                                 BiFunction<Integer, Integer, Long> sleepTimeCalculator) {
        super(secondMaxCount);
        this.sleepTimeCalculator = Objects.requireNonNull(sleepTimeCalculator,
                "sleepTimeCalculator cannot be null");
    }

    /**
     * 构造频率限制辅助类（使用默认最大访问次数 1000）
     *
     * @param sleepTimeCalculator 睡眠时间计算函数，不能为 null
     * @throws NullPointerException if sleepTimeCalculator is null
     */
    public FrequencyLimitHelper(BiFunction<Integer, Integer, Long> sleepTimeCalculator) {
        this(1000, sleepTimeCalculator);
    }

    /**
     * 获取达到条件后的睡眠时间
     *
     * @param incrementalCount 当期秒内的访问次数
     * @param secondMaxCount   每秒的最大访问次数
     * @return 睡眠时间，单位毫秒
     */
    @Override
    public long getSleepTimeWhileOverMaxCount(int incrementalCount, int secondMaxCount) {
        Long result = sleepTimeCalculator.apply(incrementalCount, secondMaxCount);
        return result != null ? result : 0L;
    }
}
