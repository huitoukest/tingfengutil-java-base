package com.tingfeng.util.java.base.common.utils.process.constant;

/**
 * 进程退出码常量接口
 * @author huitoukest
 */
public interface ProcessExitCode {
    /** 正常退出 */
    int SUCCESS = 0;

    /** 一般错误 */
    int GENERAL_ERROR = 1;

    /** 命令使用错误 */
    int MISUSE_OF_SHELL = 2;

    /** 命令不可执行（权限问题） */
    int EXECUTE_ERROR = 126;

    /** 命令未找到 */
    int NOT_FOUND = 127;

    /** 被信号终止的基准码（实际信号 = exitCode - 128） */
    int SIGNAL_TERMINATE = 128;

    /** SIGKILL - 强制终止 */
    int SIGKILL = 9;

    /** SIGTERM - 优雅终止 */
    int SIGTERM = 15;
}
