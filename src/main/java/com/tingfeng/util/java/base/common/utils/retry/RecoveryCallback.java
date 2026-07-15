package com.tingfeng.util.java.base.common.utils.retry;

/**
 * 恢复回调 —— 所有重试均耗尽时提供降级返回值。
 * <p>
 * 函数式接口，可与 lambda 表达式无缝配合。
 * </p>
 *
 * @param <T> 重试任务的返回类型
 */
@FunctionalInterface
public interface RecoveryCallback<T> {

    /**
     * 在重试耗尽时返回降级结果。
     *
     * @param throwable 导致重试耗尽的最后一个异常
     * @return 降级返回值
     */
    T recover(Throwable throwable);
}
