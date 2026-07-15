package com.tingfeng.util.java.base.common.utils;

import java.util.concurrent.TimeUnit;

/**
 * 简单性能计时器 — 测量代码执行耗时。
 * <p>
 * 支持启动/停止/暂停/恢复，基于 {@link System#nanoTime()} 实现单调递增计时，
 * 不受系统时钟调整（NTP 回拨、手动修改）影响。
 * 同时记录 {@link System#currentTimeMillis()} 用于获取启动时的挂钟时间戳。
 * </p>
 *
 * <p>快速用法:
 * <pre>{@code
 *   // 基础计时
 *   StopWatch sw = StopWatch.createStarted();
 *   // ... 待测代码 ...
 *   sw.stop();
 *   System.out.println(sw.prettyPrint());  // "1.234s"
 *
 *   // 暂停/恢复场景（排除中间处理时间）
 *   StopWatch sw2 = StopWatch.createStarted();
 *   // ... 第一阶段（计费） ...
 *   sw2.pause();
 *   // ... 中间处理（不计时） ...
 *   sw2.resume();
 *   // ... 第二阶段（计费） ...
 *   sw2.stop();
 *   System.out.println(sw2.getTime());  // 不含"中间处理"的耗时
 * }</pre>
 * </p>
 *
 * <p><b>线程安全:</b> 此类非线程安全。每个线程应使用独立的 StopWatch 实例。</p>
 *
 * <p>状态转换:
 * <pre>
 *   IDLE --start()--&gt; RUNNING
 *   RUNNING --pause()--&gt; PAUSED
 *   PAUSED --resume()--&gt; RUNNING
 *   RUNNING --stop()--&gt; STOPPED
 *   PAUSED --stop()--&gt; STOPPED
 *   STOPPED --reset()--&gt; IDLE
 *   任何状态 --reset()--&gt; IDLE
 * </pre>
 * </p>
 */
public class StopWatch {

    private enum State {
        IDLE, RUNNING, PAUSED, STOPPED
    }

    private State state = State.IDLE;
    private long startTimeNanos;
    private long startTimeMillis;
    private long accumulatedNanos;

    private StopWatch() {
    }

    // ==================== 工厂方法 ====================

    /**
     * 创建并启动计时器。
     * <p>等价于 {@code create().start()}。</p>
     *
     * @return 已启动的 StopWatch 实例
     */
    public static StopWatch createStarted() {
        StopWatch sw = new StopWatch();
        sw.start();
        return sw;
    }

    /**
     * 创建计时器（初始状态为 IDLE，需手动调用 {@link #start()}）。
     *
     * @return 新的 StopWatch 实例
     */
    public static StopWatch create() {
        return new StopWatch();
    }

    // ==================== 生命周期 ====================

    /**
     * 启动计时器。仅当状态为 IDLE 时可调用。
     *
     * @throws IllegalStateException 若状态不是 IDLE
     */
    public void start() {
        if (state != State.IDLE) {
            throw new IllegalStateException(
                    "StopWatch must be in IDLE state to start, but current state is " + state);
        }
        startTimeNanos = System.nanoTime();
        startTimeMillis = System.currentTimeMillis();
        accumulatedNanos = 0;
        state = State.RUNNING;
    }

    /**
     * 停止计时器，进入终态 STOPPED。停止后不可再 resume，需先 reset。
     *
     * @throws IllegalStateException 若状态为 IDLE 或 STOPPED
     */
    public void stop() {
        if (state == State.IDLE || state == State.STOPPED) {
            throw new IllegalStateException(
                    "StopWatch must be in RUNNING or PAUSED state to stop, but current state is " + state);
        }
        if (state == State.RUNNING) {
            accumulatedNanos += System.nanoTime() - startTimeNanos;
        }
        state = State.STOPPED;
    }

    /**
     * 暂停计时。累计当前运行段耗时，进入 PAUSED 状态。
     *
     * @throws IllegalStateException 若当前状态不是 RUNNING
     */
    public void pause() {
        if (state != State.RUNNING) {
            throw new IllegalStateException(
                    "StopWatch must be in RUNNING state to pause, but current state is " + state);
        }
        accumulatedNanos += System.nanoTime() - startTimeNanos;
        state = State.PAUSED;
    }

    /**
     * 恢复计时。从暂停点继续累积耗时，回到 RUNNING 状态。
     *
     * @throws IllegalStateException 若当前状态不是 PAUSED
     */
    public void resume() {
        if (state != State.PAUSED) {
            throw new IllegalStateException(
                    "StopWatch must be in PAUSED state to resume, but current state is " + state);
        }
        startTimeNanos = System.nanoTime();
        state = State.RUNNING;
    }

    /**
     * 重置计时器到初始 IDLE 状态。所有计时数据归零。
     * <p>可在任意状态调用，用于重复使用 StopWatch 实例。</p>
     */
    public void reset() {
        state = State.IDLE;
        accumulatedNanos = 0;
        startTimeNanos = 0;
        startTimeMillis = 0;
    }

    // ==================== 查询 ====================

    /**
     * 获取已耗时间（毫秒）。
     * <ul>
     *   <li>RUNNING 状态：返回当前实时耗时（含当前运行段）</li>
     *   <li>PAUSED / STOPPED 状态：返回截至暂停/停止时的累计耗时</li>
     *   <li>IDLE 状态：返回 0</li>
     * </ul>
     * 调用此方法不会改变计时器状态。
     *
     * @return 已耗毫秒数，IDLE 状态返回 0
     */
    public long getTime() {
        return getTime(TimeUnit.MILLISECONDS);
    }

    /**
     * 获取已耗时间，指定时间单位。
     *
     * @param unit 时间单位
     * @return 已耗时间（按指定单位转换），IDLE 状态返回 0
     * @see #getTime()
     */
    public long getTime(TimeUnit unit) {
        return unit.convert(getTimeNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * 获取启动时的挂钟时间戳（{@link System#currentTimeMillis()}）。
     * <p>未启动时返回 0。</p>
     *
     * @return 启动时的毫秒时间戳，未启动返回 0
     */
    public long getStartTime() {
        return startTimeMillis;
    }

    /**
     * 计时器是否正在运行。
     * <p>仅 RUNNING 状态返回 true，PAUSED/STOPPED/IDLE 返回 false。</p>
     *
     * @return 是否正在运行
     */
    public boolean isRunning() {
        return state == State.RUNNING;
    }

    /**
     * 计时器是否已启动（非 IDLE 状态）。
     *
     * @return 是否已启动
     */
    public boolean isStarted() {
        return state != State.IDLE;
    }

    // ==================== 格式化 ====================

    /**
     * 友好格式输出耗时，始终以秒为基准并保留毫秒精度。
     * <p>格式化规则:
     * <pre>
     *   &lt; 1ms     → "0.001s"
     *   &lt; 1s      → "0.123s"
     *   &lt; 1min    → "12.345s"
     *   &lt; 1h      → "1m 23.456s"
     *   ≥ 1h      → "1h 2m 03.456s"
     * </pre>
     * </p>
     *
     * @return 友好格式的耗时字符串
     */
    public String prettyPrint() {
        long nanos = getTimeNanos();
        long totalMs = nanos / 1_000_000;
        long totalSec = totalMs / 1000;
        long remainMs = totalMs % 1000;

        if (totalMs < 1) {
            if (nanos == 0) {
                return "0.000s";
            }
            return "0.001s";
        }
        if (totalSec < 1) {
            return String.format("%.3fs", totalMs / 1000.0);
        }
        if (totalSec < 60) {
            return String.format("%.3fs", totalMs / 1000.0);
        }

        long minutes = totalSec / 60;
        long sec = totalSec % 60;

        if (minutes < 60) {
            return String.format("%dm %.3fs", minutes, sec + remainMs / 1000.0);
        }

        // >= 1h
        long hours = minutes / 60;
        minutes = minutes % 60;
        return String.format("%dh %dm %06.3fs", hours, minutes, sec + remainMs / 1000.0);
    }

    /**
     * 友好格式输出耗时，指定时间单位。
     * <p>输出格式为数值 + 单位缩写：</p>
     * <ul>
     *   <li>NANOSECONDS → "123456789ns"</li>
     *   <li>MICROSECONDS → "123456us"</li>
     *   <li>MILLISECONDS → "1234ms"</li>
     *   <li>SECONDS → "1.234s"</li>
     *   <li>MINUTES → "2m 3s"</li>
     *   <li>HOURS → "1h 2m 3s"</li>
     * </ul>
     *
     * @param unit 时间单位
     * @return 友好格式的耗时字符串
     */
    public String prettyPrint(TimeUnit unit) {
        long nanos = getTimeNanos();
        switch (unit) {
            case NANOSECONDS:
                return nanos + "ns";
            case MICROSECONDS:
                return (nanos / 1000) + "us";
            case MILLISECONDS:
                return (nanos / 1_000_000) + "ms";
            case SECONDS:
                return String.format("%.3fs", nanos / 1_000_000_000.0);
            case MINUTES: {
                long totalSec = nanos / 1_000_000_000;
                long mins = totalSec / 60;
                long secs = totalSec % 60;
                return String.format("%dm %ds", mins, secs);
            }
            case HOURS: {
                long totalSec = nanos / 1_000_000_000;
                long hrs = totalSec / 3600;
                long mins = (totalSec % 3600) / 60;
                long secs = totalSec % 60;
                return String.format("%dh %dm %ds", hrs, mins, secs);
            }
            default:
                throw new IllegalArgumentException("Unsupported TimeUnit: " + unit);
        }
    }

    // ==================== 内部方法 ====================

    /**
     * 获取当前纳秒级已耗时间。
     * <p>IDLE 状态返回 0。</p>
     *
     * @return 已耗纳秒数，IDLE 状态返回 0
     */
    private long getTimeNanos() {
        if (state == State.IDLE) {
            return 0;
        }
        long total = accumulatedNanos;
        if (state == State.RUNNING) {
            total += System.nanoTime() - startTimeNanos;
        }
        return total;
    }
}
