package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.lang.base.RuntimeStatus;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 内存状态相关操作实现。
 *
 * 包级私有，不对外暴露。通过 {@link RuntimeUtils} 对外提供统一 API。
 */
class RuntimeMemOps {

    /**
     * 获取当前运行时状态
     */
    static RuntimeStatus getRuntimeStatus() {
        Runtime runtime = Runtime.getRuntime();
        RuntimeStatus status = new RuntimeStatus();
        status.setMaxMemory(runtime.maxMemory());
        status.setTotalMemory(runtime.totalMemory());
        status.setFreeMemory(runtime.freeMemory());
        status.setAvailableProcessors(runtime.availableProcessors());
        return status;
    }

    /**
     * 检查内存使用率是否超过阈值
     * @param threshold 阈值（0.0 ~ 1.0），例如0.8表示80%
     */
    static boolean isMemoryOverThreshold(double threshold) {
        RuntimeStatus status = getRuntimeStatus();
        return status.getMemoryUsageRatio() > threshold;
    }

    /**
     * 建议JVM进行垃圾回收
     * 注意：只是建议，实际是否执行由JVM决定
     */
    static void gc() {
        Runtime.getRuntime().gc();
    }

    /**
     * 获取详细内存信息
     * @return 内存信息Map，包含max/total/free/used等
     */
    static Map<String, Long> getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Long> memoryInfo = new LinkedHashMap<>();
        memoryInfo.put("maxMemory", runtime.maxMemory());
        memoryInfo.put("totalMemory", runtime.totalMemory());
        memoryInfo.put("freeMemory", runtime.freeMemory());
        memoryInfo.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        memoryInfo.put("availableProcessors", (long) runtime.availableProcessors());
        return memoryInfo;
    }

    /**
     * 获取各内存池信息
     * @return 内存池信息列表
     */
    static List<Map<String, Object>> getMemoryPoolInfo() {
        List<Map<String, Object>> poolInfoList = new ArrayList<>();
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            Map<String, Object> poolInfo = new LinkedHashMap<>();
            poolInfo.put("name", pool.getName());
            poolInfo.put("type", pool.getType().name());
            MemoryUsage usage = pool.getUsage();
            if (usage != null) {
                poolInfo.put("used", usage.getUsed());
                poolInfo.put("committed", usage.getCommitted());
                poolInfo.put("max", usage.getMax());
            }
            poolInfoList.add(poolInfo);
        }
        return poolInfoList;
    }

    /**
     * 检查指定内存池使用率是否超过阈值
     * @param poolName 内存池名称（如"Tenured Gen", "Metaspace"）
     * @param threshold 阈值（0.0 ~ 1.0）
     * @return 是否超过阈值
     */
    static boolean isMemoryPoolOverThreshold(String poolName, double threshold) {
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getName().equals(poolName)) {
                MemoryUsage usage = pool.getUsage();
                if (usage != null && usage.getMax() > 0) {
                    return (double) usage.getUsed() / usage.getMax() > threshold;
                }
            }
        }
        return false;
    }

}
