package com.tingfeng.util.java.base.common.exception.test;

/**
 * 测试中断异常
 */
public class TestInterruptedException extends TestException {

    public TestInterruptedException(String message) {
        super(message);
    }

    public TestInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
