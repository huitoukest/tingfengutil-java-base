package com.tingfeng.util.java.base.common.utils.process.exception;

/**
 * 进程操作基础异常
 * @author huitoukest
 */
public class ProcessException extends RuntimeException {
    private final String command;

    public ProcessException(String message, String command) {
        super(message);
        this.command = command;
    }

    public ProcessException(String message, String command, Throwable cause) {
        super(message, cause);
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
