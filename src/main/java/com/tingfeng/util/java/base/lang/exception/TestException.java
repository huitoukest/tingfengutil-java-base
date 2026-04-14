package com.tingfeng.util.java.base.lang.exception.test;

/**
 * 测试相关异常基类
 */
public class TestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TestException() {
        super();
    }

    public TestException(String message) {
        super(message);
    }

    public TestException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestException(Throwable cause) {
        super(cause);
    }
}
