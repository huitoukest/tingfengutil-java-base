package com.tingfeng.util.java.base.lang.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 并发测试上下文，用于收集和验证线程安全测试结果
 */
public class ConcurrentTestContext<T> {

    private final List<T> results = new ArrayList<>();
    private final AtomicInteger successCount = new AtomicInteger();
    private final AtomicInteger failCount = new AtomicInteger();
    private final Object lock = new Object();

    public void addResult(T result) {
        synchronized (lock) {
            results.add(result);
        }
    }

    public void incrementSuccess() {
        successCount.incrementAndGet();
    }

    public void incrementFail() {
        failCount.incrementAndGet();
    }

    public List<T> getResults() {
        synchronized (lock) {
            return new ArrayList<>(results);
        }
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public int getFailCount() {
        return failCount.get();
    }

    public int getTotalCount() {
        return successCount.get() + failCount.get();
    }

    public void clear() {
        synchronized (lock) {
            results.clear();
        }
        successCount.set(0);
        failCount.set(0);
    }

    public boolean assertAll(java.util.function.Predicate<T> predicate, String message) {
        List<T> snapshot;
        synchronized (lock) {
            snapshot = new ArrayList<>(results);
        }
        for (T result : snapshot) {
            if (!predicate.test(result)) {
                throw new AssertionError(message + " - failed on: " + result);
            }
        }
        return true;
    }

    public boolean assertNone(java.util.function.Predicate<T> predicate, String message) {
        List<T> snapshot;
        synchronized (lock) {
            snapshot = new ArrayList<>(results);
        }
        for (T result : snapshot) {
            if (predicate.test(result)) {
                throw new AssertionError(message + " - found unexpected: " + result);
            }
        }
        return true;
    }
}
