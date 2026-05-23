package com.tingfeng.util.java.base.lang.exception.test;

/**
 * 测试执行超时异常
 */
public class TestTimeoutException extends TestException {

    private final long timeoutMs;

    public TestTimeoutException(String message) {
        super(message);
        this.timeoutMs = 0;
    }

    public TestTimeoutException(String message, long timeoutMs) {
        super(message);
        this.timeoutMs = timeoutMs;
    }

    public TestTimeoutException(String message, long timeoutMs, Throwable cause) {
        super(message, cause);
        this.timeoutMs = timeoutMs;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }
}
