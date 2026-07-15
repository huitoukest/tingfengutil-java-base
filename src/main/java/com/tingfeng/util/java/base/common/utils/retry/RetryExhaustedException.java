package com.tingfeng.util.java.base.common.utils.retry;

import com.tingfeng.util.java.base.lang.exception.BaseException;

/**
 * 重试耗尽异常 —— 所有重试均失败且未设置 RecoveryCallback 时抛出。
 * <p>
 * 继承 {@link BaseException}，符合项目异常层级规范。
 * </p>
 */
public class RetryExhaustedException extends BaseException {
    private static final long serialVersionUID = 1L;

    /**
     * 创建重试耗尽异常。
     *
     * @param message 异常描述
     * @param cause   导致重试耗尽的最后一个异常
     */
    public RetryExhaustedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建重试耗尽异常。
     *
     * @param message 异常描述
     */
    public RetryExhaustedException(String message) {
        super(message);
    }
}
