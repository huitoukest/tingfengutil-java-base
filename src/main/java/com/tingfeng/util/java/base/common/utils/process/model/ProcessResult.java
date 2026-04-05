package com.tingfeng.util.java.base.common.utils.process.model;

import com.tingfeng.util.java.base.common.utils.process.constant.ProcessExitCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 进程执行结果
 * @author huitoukest
 */
@Getter
@Setter
public class ProcessResult {
    /** 执行的命令 */
    private String command;

    /** 退出码 */
    private int exitCode;

    /** 标准输出 */
    private String standardOutput;

    /** 错误输出 */
    private String errorOutput;

    /** 是否超时 */
    private boolean timedOut;

    /** 执行耗时（毫秒） */
    private long durationMs;

    /** 进程是否成功启动 */
    private boolean started;

    /** 启动失败原因 */
    private String startFailureReason;

    /** 执行是否成功 */
    public boolean isSuccess() {
        return started && exitCode == ProcessExitCode.SUCCESS && !timedOut;
    }

    /** 获取错误类型 */
    public ErrorType getErrorType() {
        if (!started) {
            return ErrorType.START_FAILED;
        }
        if (timedOut) {
            return ErrorType.TIMEOUT;
        }
        if (exitCode != ProcessExitCode.SUCCESS) {
            return ErrorType.EXIT_CODE;
        }
        return ErrorType.NONE;
    }

    public enum ErrorType {
        NONE,
        START_FAILED,
        TIMEOUT,
        EXIT_CODE
    }
}
