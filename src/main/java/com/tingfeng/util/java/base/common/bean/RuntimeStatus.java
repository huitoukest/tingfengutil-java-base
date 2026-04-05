package com.tingfeng.util.java.base.common.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * JVM运行时状态信息
 */
@Getter
@Setter
public class RuntimeStatus {
    /** JVM最大堆内存（字节） */
    private long maxMemory;

    /** JVM当前申请的总堆内存（字节） */
    private long totalMemory;

    /** 当前空闲堆内存（字节） */
    private long freeMemory;

    /** 可用处理器数量 */
    private int availableProcessors;

    /** 获取已使用内存（字节） */
    public long getUsedMemory() {
        return totalMemory - freeMemory;
    }

    /** 获取内存使用率 */
    public double getMemoryUsageRatio() {
        if (maxMemory == 0) {
            return 0.0;
        }
        return (double) getUsedMemory() / maxMemory;
    }

    /** 获取可用内存（字节） */
    public long getUsableMemory() {
        return maxMemory - getUsedMemory();
    }
}
