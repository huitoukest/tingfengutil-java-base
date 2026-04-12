package com.tingfeng.util.java.base.common.utils.process.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 进程信息
 * @author huitoukest
 */
@Getter
@Setter
public class ProcessInfo {
    /** 进程ID */
    private long pid;

    /** 执行的命令 */
    private String command;

    /** 是否存活 */
    private boolean alive;

    /** 原始Process对象 */
    private transient java.lang.Process process;
}
