package com.tingfeng.util.java.base.lang.exception;

/**
 * 测试执行异常
 */
public class TestExecutionException extends TestException {

    public TestExecutionException(String message) {
        super(message);
    }

    public TestExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
