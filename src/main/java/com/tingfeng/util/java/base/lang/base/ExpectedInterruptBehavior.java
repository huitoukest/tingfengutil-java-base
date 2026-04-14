package com.tingfeng.util.java.base.lang.base;

/**
 * 中断响应行为枚举
 */
public enum ExpectedInterruptBehavior {

    /**
     * 忽略中断，继续执行
     */
    IGNORE,

    /**
     * 抛出 InterruptedException
     */
    THROW_INTERRUPTED,

    /**
     * 清理后抛出 InterruptedException
     */
    CLEANUP_THEN_THROW,

    /**
     * 设置中断标志但不抛异常
     */
    SET_FLAG_ONLY
}
