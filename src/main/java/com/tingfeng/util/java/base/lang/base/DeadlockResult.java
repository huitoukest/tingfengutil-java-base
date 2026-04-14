package com.tingfeng.util.java.base.lang.base;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 死锁检测结果
 */
public class DeadlockResult {

    private final boolean deadlockDetected;
    private final List<Long> deadlockedThreadIds;
    private final Map<Long, String> threadStackTraces;

    public DeadlockResult(boolean deadlockDetected,
                         List<Long> deadlockedThreadIds,
                         Map<Long, String> threadStackTraces) {
        this.deadlockDetected = deadlockDetected;
        this.deadlockedThreadIds = deadlockedThreadIds;
        this.threadStackTraces = threadStackTraces;
    }

    public boolean isDeadlockDetected() {
        return deadlockDetected;
    }

    public List<Long> getDeadlockedThreadIds() {
        return deadlockedThreadIds;
    }

    public Map<Long, String> getThreadStackTraces() {
        return threadStackTraces;
    }

    public static DeadlockResult noDeadlock() {
        return new DeadlockResult(false, Collections.emptyList(), Collections.emptyMap());
    }

    public static DeadlockResult withDeadlock(List<Long> threadIds, Map<Long, String> traces) {
        return new DeadlockResult(true, threadIds, traces);
    }
}
