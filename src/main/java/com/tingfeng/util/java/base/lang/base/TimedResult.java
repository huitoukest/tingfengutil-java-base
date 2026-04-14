package com.tingfeng.util.java.base.lang.base;

/**
 * 计时结果
 */
public class TimedResult<T> {

    private final T result;
    private final long elapsedMs;
    private final boolean timedOut;

    public TimedResult(T result, long elapsedMs, boolean timedOut) {
        this.result = result;
        this.elapsedMs = elapsedMs;
        this.timedOut = timedOut;
    }

    public T getResult() {
        return result;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public static <T> TimedResult<T> success(T result, long elapsedMs) {
        return new TimedResult<>(result, elapsedMs, false);
    }

    public static <T> TimedResult<T> timeout(long elapsedMs) {
        return new TimedResult<>(null, elapsedMs, true);
    }
}
