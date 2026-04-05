package com.tingfeng.util.java.base.common.utils.process.exception;

/**
 * 进程执行超时异常
 * @author huitoukest
 */
public class ProcessTimeoutException extends ProcessException {
    private final long timeoutMs;

    public ProcessTimeoutException(String message, String command, long timeoutMs) {
        super(message, command);
        this.timeoutMs = timeoutMs;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }
}
