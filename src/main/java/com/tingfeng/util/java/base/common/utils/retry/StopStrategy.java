package com.tingfeng.util.java.base.common.utils.retry;

/**
 * 停止策略 —— 决定是否应停止重试。
 * <p>
 * 函数式接口，可与 lambda 表达式无缝配合。
 * </p>
 */
@FunctionalInterface
public interface StopStrategy {

    /**
     * 判断是否应停止重试。
     *
     * @param attempt   当前尝试次数（从 1 开始计数）
     * @param elapsedMs 自首次尝试以来的已耗毫秒数
     * @return true 表示应停止重试，false 表示继续
     */
    boolean shouldStop(int attempt, long elapsedMs);
}
