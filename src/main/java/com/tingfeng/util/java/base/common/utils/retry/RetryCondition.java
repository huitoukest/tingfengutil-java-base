package com.tingfeng.util.java.base.common.utils.retry;

/**
 * 重试条件 —— 判断是否应对当前执行结果进行重试。
 * <p>
 * 函数式接口，可与 lambda 表达式无缝配合。
 * </p>
 *
 * @param <T> 重试任务的返回类型
 */
@FunctionalInterface
public interface RetryCondition<T> {

    /**
     * 判断是否应对当前结果进行重试。
     *
     * @param result    执行结果（可能为 null）
     * @param exception 执行抛出的异常（无异常时为 null）
     * @return true 表示应重试，false 表示不重试
     */
    boolean shouldRetry(T result, Throwable exception);
}
