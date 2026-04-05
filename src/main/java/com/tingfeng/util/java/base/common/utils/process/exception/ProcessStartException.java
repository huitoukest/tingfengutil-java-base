package com.tingfeng.util.java.base.common.utils.process.exception;

/**
 * 进程启动失败异常
 * @author huitoukest
 */
public class ProcessStartException extends ProcessException {
    private final String reason;

    public ProcessStartException(String message, String command, String reason) {
        super(message, command);
        this.reason = reason;
    }

    public ProcessStartException(String message, String command, String reason, Throwable cause) {
        super(message, command, cause);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
