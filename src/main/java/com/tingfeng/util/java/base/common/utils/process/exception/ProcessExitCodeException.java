package com.tingfeng.util.java.base.common.utils.process.exception;

import com.tingfeng.util.java.base.common.utils.process.constant.ProcessExitCode;

/**
 * 进程非0退出码异常
 * @author huitoukest
 */
public class ProcessExitCodeException extends ProcessException {
    private final int exitCode;

    public ProcessExitCodeException(String message, String command, int exitCode) {
        super(message, command);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }

    /**
     * 是否被信号终止
     * @return true if process was killed by a signal
     */
    public boolean isSignaled() {
        return exitCode > ProcessExitCode.SIGNAL_TERMINATE;
    }

    /**
     * 获取信号编号（如果是信号终止）
     * @return signal number, or -1 if not signaled
     */
    public int getSignal() {
        return isSignaled() ? exitCode - ProcessExitCode.SIGNAL_TERMINATE : -1;
    }
}
