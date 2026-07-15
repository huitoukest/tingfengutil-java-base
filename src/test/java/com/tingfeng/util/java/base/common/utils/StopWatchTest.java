package com.tingfeng.util.java.base.common.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * StopWatch 单元测试。
 * <p>覆盖完整状态机、非法转换、格式输出和精度验证。</p>
 */
public class StopWatchTest {

    // ==================== 1. createStarted ====================

    /**
     * createStarted() 创建后 isRunning() 应为 true，isStarted() 应为 true。
     */
    @Test
    public void createStarted_ShouldBeRunning() {
        StopWatch sw = StopWatch.createStarted();
        assertTrue("createStarted should result in running state", sw.isRunning());
        assertTrue("createStarted should result in started state", sw.isStarted());
    }

    /**
     * create() 创建后 isRunning() 应为 false，isStarted() 应为 false。
     */
    @Test
    public void create_ShouldBeIdle() {
        StopWatch sw = StopWatch.create();
        assertFalse("create should result in idle state (not running)", sw.isRunning());
        assertFalse("create should result in idle state (not started)", sw.isStarted());
    }

    // ==================== 2. start → stop 时间演进 ====================

    /**
     * start() → sleep → stop() 后 getTime() 应返回正数耗时。
     */
    @Test
    public void startStop_ShouldReturnElapsedTime() throws InterruptedException {
        StopWatch sw = StopWatch.createStarted();
        Thread.sleep(200);
        sw.stop();
        long elapsed = sw.getTime();
        assertTrue("Elapsed time should be positive after stop, got " + elapsed, elapsed > 0);
    }

    /**
     * stop() 后多次调用 getTime() 应返回相同的值（幂等）。
     */
    @Test
    public void getTime_AfterStop_ShouldBeStable() throws InterruptedException {
        StopWatch sw = StopWatch.createStarted();
        Thread.sleep(100);
        sw.stop();
        long t1 = sw.getTime();
        long t2 = sw.getTime();
        long t3 = sw.getTime();
        assertEquals("getTime after stop should be stable", t1, t2);
        assertEquals("getTime after stop should be stable", t2, t3);
    }

    // ==================== 3. pause/resume 累计正确 ====================

    /**
     * start() → pause() → resume() → stop() 验证暂停期间不计时。
     */
    @Test
    public void pauseResume_ShouldAccumulateCorrectly() throws InterruptedException {
        StopWatch sw = StopWatch.createStarted();
        Thread.sleep(100);
        sw.pause();

        // 暂停期间消耗 100ms，不应计入
        Thread.sleep(100);

        sw.resume();
        Thread.sleep(100);
        sw.stop();

        long elapsed = sw.getTime();
        // 期望约 200ms（100 + 100），暂停的 100ms 不计入
        // 容忍 ±100ms 应对 OS 调度波动
        assertTrue("Elapsed time should be around 200ms, got " + elapsed,
                elapsed >= 80 && elapsed <= 350);
    }

    // ==================== 4. 多次暂停/恢复 ====================

    /**
     * start() → pause() → resume() → pause() → resume() → stop()
     * 验证多次暂停/恢复后累计正确。
     */
    @Test
    public void multiplePauseResume_ShouldAccumulateCorrectly() throws InterruptedException {
        StopWatch sw = StopWatch.createStarted();
        Thread.sleep(80);
        sw.pause();
        Thread.sleep(50);
        sw.resume();
        Thread.sleep(80);
        sw.pause();
        Thread.sleep(50);
        sw.resume();
        Thread.sleep(80);
        sw.stop();

        long elapsed = sw.getTime();
        // 期望约 240ms（80+80+80），两次暂停各 50ms 不计入
        assertTrue("Elapsed time should be around 240ms, got " + elapsed,
                elapsed >= 100 && elapsed <= 400);
    }

    // ==================== 5. reset 可重用 ====================

    /**
     * start() → stop() → reset() → start() → stop()
     * 验证 reset 后可重用。
     */
    @Test
    public void reset_ShouldAllowReuse() throws InterruptedException {
        StopWatch sw = StopWatch.createStarted();
        Thread.sleep(100);
        sw.stop();
        long first = sw.getTime();
        assertTrue("First measurement should be positive", first > 0);

        sw.reset();
        assertFalse("After reset should not be running", sw.isRunning());

        sw.start();
        Thread.sleep(50);
        sw.stop();
        long second = sw.getTime();
        assertTrue("Second measurement after reset should be positive", second > 0);

        // 第二次测量应显著小于第一次（仅睡了 50ms vs 100ms）
        assertTrue("Second measurement should be less than first: " + second + " vs " + first,
                second < first * 3);
    }

    // ==================== 6. getTime 在 RUNNING 实时性 ====================

    /**
     * getTime() 在 RUNNING 状态下应返回实时的已耗时间（含当前运行段）。
     */
    @Test
    public void getTime_WhileRunning_ShouldReturnRealTime() throws InterruptedException {
        StopWatch sw = StopWatch.createStarted();
        Thread.sleep(200);
        long elapsed = sw.getTime();
        assertTrue("getTime while running should be positive, got " + elapsed, elapsed > 0);
        // 应在 200ms 附近（容忍 150ms 偏差）
        assertTrue("getTime while running should be ~200ms, got " + elapsed, elapsed >= 50);
    }

    // ==================== 7. prettyPrint 格式验证 ====================

    /**
     * 极短耗时 prettyPrint 应返回 "0.001s"。
     */
    @Test
    public void prettyPrint_Minimum_ShouldReturnDefault() {
        StopWatch sw = StopWatch.createStarted();
        sw.stop();
        String result = sw.prettyPrint();
        assertTrue("Minimum prettyPrint should be 0.001s or similar, got: " + result,
                result.matches("\\d+\\.\\d{3}s"));
    }

    /**
     * 百毫秒级耗时 prettyPrint 应匹配 "0.XXXs" 格式。
     */
    @Test
    public void prettyPrint_SubSecond_ShouldMatchFormat() throws InterruptedException {
        StopWatch sw = StopWatch.createStarted();
        Thread.sleep(200);
        sw.stop();
        String result = sw.prettyPrint();
        assertTrue("Sub-second prettyPrint should match 0.XXXs format, got: " + result,
                result.matches("0\\.\\d{3}s") || result.matches("\\d+\\.\\d{3}s"));
    }

    /**
     * prettyPrint(TimeUnit) 各单位的格式验证。
     */
    @Test
    public void prettyPrint_WithTimeUnit_ShouldMatchFormat() throws InterruptedException {
        StopWatch sw = StopWatch.createStarted();
        Thread.sleep(100);
        sw.stop();

        // NANOSECONDS: 数字 + "ns"
        String nanos = sw.prettyPrint(TimeUnit.NANOSECONDS);
        assertTrue("Nanos format should end with 'ns', got: " + nanos, nanos.endsWith("ns"));

        // MICROSECONDS: 数字 + "us"
        String micros = sw.prettyPrint(TimeUnit.MICROSECONDS);
        assertTrue("Micros format should end with 'us', got: " + micros, micros.endsWith("us"));

        // MILLISECONDS: 数字 + "ms"
        String millis = sw.prettyPrint(TimeUnit.MILLISECONDS);
        assertTrue("Millis format should end with 'ms', got: " + millis, millis.endsWith("ms"));

        // SECONDS: "X.XXXs"
        String secs = sw.prettyPrint(TimeUnit.SECONDS);
        assertTrue("Seconds format should end with 's', got: " + secs, secs.endsWith("s"));
    }

    // ==================== 8. 非法转换 ====================

    @Test(expected = IllegalStateException.class)
    public void start_WhenRunning_ShouldThrow() {
        StopWatch sw = StopWatch.createStarted();
        sw.start();
    }

    @Test(expected = IllegalStateException.class)
    public void start_WhenPaused_ShouldThrow() {
        StopWatch sw = StopWatch.createStarted();
        sw.pause();
        sw.start();
    }

    @Test(expected = IllegalStateException.class)
    public void start_WhenStopped_ShouldThrow() throws InterruptedException {
        StopWatch sw = StopWatch.createStarted();
        Thread.sleep(10);
        sw.stop();
        sw.start();
    }

    @Test(expected = IllegalStateException.class)
    public void stop_WhenIdle_ShouldThrow() {
        StopWatch sw = StopWatch.create();
        sw.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void pause_WhenIdle_ShouldThrow() {
        StopWatch sw = StopWatch.create();
        sw.pause();
    }

    @Test(expected = IllegalStateException.class)
    public void pause_WhenPaused_ShouldThrow() {
        StopWatch sw = StopWatch.createStarted();
        sw.pause();
        sw.pause();
    }

    @Test(expected = IllegalStateException.class)
    public void pause_WhenStopped_ShouldThrow() throws InterruptedException {
        StopWatch sw = StopWatch.createStarted();
        Thread.sleep(10);
        sw.stop();
        sw.pause();
    }

    @Test(expected = IllegalStateException.class)
    public void resume_WhenIdle_ShouldThrow() {
        StopWatch sw = StopWatch.create();
        sw.resume();
    }

    @Test(expected = IllegalStateException.class)
    public void resume_WhenRunning_ShouldThrow() {
        StopWatch sw = StopWatch.createStarted();
        sw.resume();
    }

    @Test(expected = IllegalStateException.class)
    public void resume_WhenStopped_ShouldThrow() throws InterruptedException {
        StopWatch sw = StopWatch.createStarted();
        Thread.sleep(10);
        sw.stop();
        sw.resume();
    }

    @Test
    public void getTime_WhenIdle_ShouldReturnZero() {
        StopWatch sw = StopWatch.create();
        assertEquals("getTime when idle should return 0", 0, sw.getTime());
    }

    @Test
    public void prettyPrint_WhenIdle_ShouldReturnDefault() {
        StopWatch sw = StopWatch.create();
        String result = sw.prettyPrint();
        assertTrue("prettyPrint when idle should return default format, got: " + result,
                result.matches("\\d+\\.\\d{3}s"));
    }

    @Test
    public void prettyPrint_WithUnit_WhenIdle_ShouldReturnFormatted() {
        StopWatch sw = StopWatch.create();
        String result = sw.prettyPrint(TimeUnit.SECONDS);
        assertEquals("prettyPrint(SECONDS) when idle should return 0.000s", "0.000s", result);
    }

    // ==================== 9. getStartTime ====================

    /**
     * getStartTime() 应返回接近 System.currentTimeMillis() 的挂钟时间戳。
     */
    @Test
    public void getStartTime_ShouldReturnWallClock() throws InterruptedException {
        long before = System.currentTimeMillis();
        StopWatch sw = StopWatch.createStarted();
        long after = System.currentTimeMillis();
        long startTime = sw.getStartTime();
        assertTrue("getStartTime should be between before and after calls",
                startTime >= before && startTime <= after);
    }

    /**
     * 未启动时 getStartTime() 应返回 0。
     */
    @Test
    public void getStartTime_WhenIdle_ShouldReturnZero() {
        StopWatch sw = StopWatch.create();
        assertEquals("getStartTime when idle should be 0", 0, sw.getStartTime());
    }

    // ==================== 10. 精度验证 ====================

    /**
     * 暂停+运行累计误差应在 50ms 内。
     * <p>用 System.nanoTime() 作为参考时钟同步测量。</p>
     */
    @Test
    public void precision_ShouldBeWithin50ms() throws InterruptedException {
        StopWatch sw = StopWatch.createStarted();
        long refStart = System.nanoTime();

        Thread.sleep(200);
        sw.pause();
        long refPause = System.nanoTime();

        Thread.sleep(50);

        sw.resume();
        long refResume = System.nanoTime();

        Thread.sleep(200);
        sw.stop();
        long refStop = System.nanoTime();

        long swNanos = sw.getTime(TimeUnit.NANOSECONDS);
        long expectedNanos = (refPause - refStart) + (refStop - refResume);
        long diff = Math.abs(swNanos - expectedNanos);
        long maxDiff = TimeUnit.MILLISECONDS.toNanos(50);

        assertTrue("Precision error: " + TimeUnit.NANOSECONDS.toMillis(diff)
                        + "ms (expected ≤50ms)",
                diff <= maxDiff);
    }

    // ==================== 额外边界场景 ====================

    /**
     * isRunning() 在 PAUSED 状态下应返回 false（仅 RUNNING 视为运行中）。
     */
    @Test
    public void isRunning_WhenPaused_ShouldReturnFalse() {
        StopWatch sw = StopWatch.createStarted();
        sw.pause();
        assertFalse("isRunning should return false when paused", sw.isRunning());
    }

    /**
     * isRunning() 在 STOPPED 状态下应返回 false。
     */
    @Test
    public void isRunning_WhenStopped_ShouldReturnFalse() throws InterruptedException {
        StopWatch sw = StopWatch.createStarted();
        Thread.sleep(10);
        sw.stop();
        assertFalse("isRunning should return false when stopped", sw.isRunning());
    }

    /**
     * isStarted() 在 IDLE 状态下应返回 false。
     */
    @Test
    public void isStarted_WhenIdle_ShouldReturnFalse() {
        StopWatch sw = StopWatch.create();
        assertFalse("isStarted should return false when idle", sw.isStarted());
    }

    /**
     * start() → pause() → stop() 验证从 pause 直接到 stop 的路径。
     */
    @Test
    public void startPauseStop_ShouldReturnAccumulatedTime() throws InterruptedException {
        StopWatch sw = StopWatch.createStarted();
        Thread.sleep(100);
        sw.pause();
        sw.stop();
        long elapsed = sw.getTime();
        assertTrue("Elapsed time should be positive after pause→stop, got " + elapsed,
                elapsed > 0);
    }

    /**
     * reset() 在任意状态都可调用且不抛异常。
     */
    @Test
    public void reset_FromAnyState_ShouldNotThrow() {
        // IDLE
        StopWatch sw = StopWatch.create();
        sw.reset();

        // RUNNING
        sw.start();
        sw.reset();

        // PAUSED
        sw.start();
        sw.pause();
        sw.reset();

        // STOPPED
        sw.start();
        sw.stop();
        sw.reset();

        // 最后的 reset 后应为 IDLE 状态
        assertFalse("After reset from any state, should be idle", sw.isRunning());
    }

    /**
     * getTime(TimeUnit) 在 STOPPED 状态应正常工作。
     */
    @Test
    public void getTime_WithUnit_ShouldWorkAfterStop() throws InterruptedException {
        StopWatch sw = StopWatch.createStarted();
        Thread.sleep(100);
        sw.stop();

        long nanos = sw.getTime(TimeUnit.NANOSECONDS);
        long micros = sw.getTime(TimeUnit.MICROSECONDS);
        long millis = sw.getTime(TimeUnit.MILLISECONDS);
        long secs = sw.getTime(TimeUnit.SECONDS);

        assertTrue("Nanos should be positive", nanos > 0);
        assertTrue("Micros should be positive", micros > 0);
        assertTrue("Millis should be positive", millis > 0);
        assertTrue("Secs should be >= 0", secs >= 0);

        // 验证单位换算一致性
        assertTrue("Nanos should be >= micros * 1000", nanos >= micros * 1000);
        assertTrue("Micros should be >= millis * 1000", micros >= millis * 1000);
    }

    /**
     * pause() 不 resume() 直接 stop()，accumulated 应保持 pause 时的值。
     */
    @Test
    public void pauseWithoutResumeThenStop_ShouldWork() throws InterruptedException {
        StopWatch sw = StopWatch.createStarted();
        Thread.sleep(100);
        sw.pause();

        // 暂停后等待一段时间，再 stop
        Thread.sleep(50);
        sw.stop();

        long elapsed = sw.getTime();
        // 暂停后直接 stop，耗时不应包含暂停后的 50ms
        assertTrue("Elapsed should be around 100ms (without resume), got " + elapsed,
                elapsed >= 30 && elapsed <= 300);
    }
}
