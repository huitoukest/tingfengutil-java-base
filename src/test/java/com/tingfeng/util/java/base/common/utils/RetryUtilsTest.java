package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.utils.retry.RecoveryCallback;
import com.tingfeng.util.java.base.common.utils.retry.RetryCondition;
import com.tingfeng.util.java.base.common.utils.retry.RetryExhaustedException;
import com.tingfeng.util.java.base.common.utils.retry.Retryer;
import com.tingfeng.util.java.base.common.utils.retry.StopStrategy;
import com.tingfeng.util.java.base.common.utils.retry.WaitStrategy;
import com.tingfeng.util.java.base.common.utils.retry.strategies.ExponentialBackoffWaitStrategy;
import com.tingfeng.util.java.base.common.utils.retry.strategies.FibonacciWaitStrategy;
import com.tingfeng.util.java.base.common.utils.retry.strategies.FixedWaitStrategy;
import com.tingfeng.util.java.base.common.utils.retry.strategies.MaxAttemptsStopStrategy;
import com.tingfeng.util.java.base.common.utils.retry.strategies.NeverStopStrategy;
import com.tingfeng.util.java.base.common.utils.retry.strategies.RandomizedWaitStrategy;
import com.tingfeng.util.java.base.common.utils.retry.strategies.TimeoutStopStrategy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * RetryUtils 单元测试 —— 覆盖门面静态方法、Builder + Retryer 核心执行、
 * 策略实现、边界条件及异常路径。
 */
public class RetryUtilsTest {

    private ScheduledExecutorService executor;

    @Before
    public void setUp() {
        executor = Executors.newScheduledThreadPool(2);
    }

    @After
    public void tearDown() {
        executor.shutdownNow();
    }

    // ==================== 1. 静态 retry 成功/失败 ====================

    @Test
    public void testStaticRetry_success() throws Exception {
        String result = RetryUtils.retry(3, () -> "ok");
        assertEquals("ok", result);
    }

    @Test(expected = RetryExhaustedException.class)
    public void testStaticRetry_failure() {
        RetryUtils.retry(3, () -> {
            throw new RuntimeException("always fail");
        });
    }

    // ==================== 2. 静态 retryWithBackoff ====================

    @Test
    public void testStaticRetryWithBackoff_success() throws Exception {
        String result = RetryUtils.retryWithBackoff(3, 10L, () -> "backoff_ok");
        assertEquals("backoff_ok", result);
    }

    @Test(expected = RetryExhaustedException.class)
    public void testStaticRetryWithBackoff_failure() {
        RetryUtils.retryWithBackoff(2, 5L, () -> {
            throw new RuntimeException("always fail");
        });
    }

    // ==================== 3. 静态 retryAsync 成功/失败 ====================

    @Test
    public void testStaticRetryAsync_success() throws Exception {
        CompletableFuture<String> future = RetryUtils.retryAsync(
                3, 1L,
                () -> CompletableFuture.completedFuture("async_ok"),
                executor);
        assertEquals("async_ok", future.get(5, TimeUnit.SECONDS));
    }

    @Test(expected = ExecutionException.class)
    public void testStaticRetryAsync_failure() throws Exception {
        CompletableFuture<String> future = RetryUtils.retryAsync(
                2, 1L,
                () -> {
                    CompletableFuture<String> cf = new CompletableFuture<>();
                    cf.completeExceptionally(new RuntimeException("async fail"));
                    return cf;
                },
                executor);
        future.get(5, TimeUnit.SECONDS);
    }

    // ==================== 4. Builder + Retryer.call ====================

    @Test
    public void testBuilderRetryer_call_normal() throws Exception {
        Retryer<String> retryer = RetryUtils.<String>builder()
                .maxAttempts(3)
                .build();
        String result = retryer.call(() -> "normal");
        assertEquals("normal", result);
    }

    @Test(expected = RetryExhaustedException.class)
    public void testBuilderRetryer_call_retryExhausted() {
        Retryer<String> retryer = RetryUtils.<String>builder()
                .maxAttempts(2)
                .build();
        retryer.call(() -> {
            throw new RuntimeException("exhausted");
        });
    }

    @Test
    public void testBuilderRetryer_call_withRecovery() throws Exception {
        Retryer<String> retryer = RetryUtils.<String>builder()
                .maxAttempts(2)
                .recover((Throwable throwable) -> "recovered")
                .build();
        String result = retryer.call(() -> {
            throw new RuntimeException("fail");
        });
        assertEquals("recovered", result);
    }

    // ==================== 5. Builder + Retryer.callAsync ====================

    @Test
    public void testBuilderRetryer_callAsync_normal() throws Exception {
        Retryer<String> retryer = RetryUtils.<String>builder()
                .maxAttempts(3)
                .withScheduledExecutor(executor)
                .build();
        CompletableFuture<String> future = retryer.callAsync(
                () -> CompletableFuture.completedFuture("async_normal"));
        assertEquals("async_normal", future.get(5, TimeUnit.SECONDS));
    }

    @Test(expected = ExecutionException.class)
    public void testBuilderRetryer_callAsync_retryExhausted() throws Exception {
        Retryer<String> retryer = RetryUtils.<String>builder()
                .maxAttempts(2)
                .withScheduledExecutor(executor)
                .build();
        CompletableFuture<String> future = retryer.callAsync(
                () -> {
                    CompletableFuture<String> cf = new CompletableFuture<>();
                    cf.completeExceptionally(new RuntimeException("async exhausted"));
                    return cf;
                });
        future.get(5, TimeUnit.SECONDS);
    }

    @Test
    public void testBuilderRetryer_callAsync_withRecovery() throws Exception {
        Retryer<String> retryer = RetryUtils.<String>builder()
                .maxAttempts(2)
                .withScheduledExecutor(executor)
                .recover((Throwable t) -> "async_recovered")
                .build();
        CompletableFuture<String> future = retryer.callAsync(
                () -> {
                    CompletableFuture<String> cf = new CompletableFuture<>();
                    cf.completeExceptionally(new RuntimeException("fail"));
                    return cf;
                });
        assertEquals("async_recovered", future.get(5, TimeUnit.SECONDS));
    }

    // ==================== 6. 边界：null 入参、非法参数、线程中断 ====================

    @Test(expected = NullPointerException.class)
    public void testRetry_nullTask() {
        RetryUtils.retry(3, null);
    }

    @Test(expected = NullPointerException.class)
    public void testRetryWithDelay_nullTask() {
        RetryUtils.retry(3, 100L, null);
    }

    @Test(expected = NullPointerException.class)
    public void testRetryWithBackoff_nullTask() {
        RetryUtils.retryWithBackoff(3, 100L, null);
    }

    @Test(expected = NullPointerException.class)
    public void testRetryAsync_nullTask() {
        RetryUtils.retryAsync(3, 1L, null, executor);
    }

    @Test(expected = NullPointerException.class)
    public void testRetryAsync_nullExecutor() {
        RetryUtils.retryAsync(3, 1L,
                () -> CompletableFuture.completedFuture("x"), null);
    }

    @Test(expected = NullPointerException.class)
    public void testBuilderRetry_call_nullTask() {
        Retryer<String> retryer = RetryUtils.<String>builder().maxAttempts(3).build();
        retryer.call(null);
    }

    @Test(expected = NullPointerException.class)
    public void testBuilderRetry_callAsync_nullTask() {
        Retryer<String> retryer = RetryUtils.<String>builder()
                .maxAttempts(3)
                .withScheduledExecutor(executor)
                .build();
        retryer.callAsync(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxAttemptsZero() {
        RetryUtils.retry(0, () -> "x");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxAttemptsNegative() {
        RetryUtils.retry(-1, () -> "x");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderMaxAttemptsZero() {
        RetryUtils.builder().maxAttempts(0).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderMaxAttemptsNegative() {
        RetryUtils.builder().maxAttempts(-5).build();
    }

    @Test
    public void testThreadInterrupt_stopsRetry() throws Exception {
        // 使用一个很长的固定等待，确保有足够时间中断
        Retryer<String> retryer = RetryUtils.<String>builder()
                .maxAttempts(5)
                .waitStrategy(new FixedWaitStrategy(100000L))
                .build();

        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch started = new CountDownLatch(1);

        Thread worker = new Thread(() -> {
            try {
                started.countDown();
                retryer.call(() -> {
                    throw new RuntimeException("fail");
                });
            } catch (Throwable e) {
                error.set(e);
            }
        });
        worker.start();
        started.await();
        // 等待第一个 task 执行完并进入 sleep
        Thread.sleep(100);
        worker.interrupt();
        worker.join(5000);

        assertNotNull("Should have thrown an exception after interrupt", error.get());
        assertTrue("Exception should be RetryExhaustedException",
                error.get() instanceof RetryExhaustedException);
    }

    // ==================== 7. WaitStrategy 验证 ====================

    @Test
    public void testFixedWaitStrategy() {
        FixedWaitStrategy ws = new FixedWaitStrategy(100L);
        assertEquals(100L, ws.computeWaitTime(1));
        assertEquals(100L, ws.computeWaitTime(10));
        assertEquals(100L, ws.computeWaitTime(100));

        // 负数按 0 处理
        FixedWaitStrategy ws2 = new FixedWaitStrategy(-50L);
        assertEquals(0L, ws2.computeWaitTime(1));
    }

    @Test
    public void testExponentialBackoffWaitStrategy() {
        // 初始 100ms，乘数 2.0，上限 10000ms
        ExponentialBackoffWaitStrategy ws = new ExponentialBackoffWaitStrategy(100, 2.0, 10000);
        // attempt=1: 100 * 2^0 = 100
        assertEquals(100L, ws.computeWaitTime(1));
        // attempt=2: 100 * 2^1 = 200
        assertEquals(200L, ws.computeWaitTime(2));
        // attempt=3: 100 * 2^2 = 400
        assertEquals(400L, ws.computeWaitTime(3));
        // attempt=4: 100 * 2^3 = 800
        assertEquals(800L, ws.computeWaitTime(4));

        // 上限测试
        ExponentialBackoffWaitStrategy ws2 = new ExponentialBackoffWaitStrategy(1000, 2.0, 3000);
        // attempt=1: 1000
        assertEquals(1000L, ws2.computeWaitTime(1));
        // attempt=2: 2000
        assertEquals(2000L, ws2.computeWaitTime(2));
        // attempt=3: 4000 但上限 3000
        assertEquals(3000L, ws2.computeWaitTime(3));

        // 默认构造器
        ExponentialBackoffWaitStrategy ws3 = new ExponentialBackoffWaitStrategy();
        assertEquals(500L, ws3.computeWaitTime(1));
        assertEquals(1000L, ws3.computeWaitTime(2));
        assertEquals(2000L, ws3.computeWaitTime(3));
    }

    @Test
    public void testRandomizedWaitStrategy() {
        RandomizedWaitStrategy ws = new RandomizedWaitStrategy(100, 50);
        for (int i = 0; i < 100; i++) {
            long delay = ws.computeWaitTime(i + 1);
            assertTrue("Delay should be >= 100", delay >= 100);
            assertTrue("Delay should be <= 150", delay <= 150);
        }
    }

    @Test
    public void testFibonacciWaitStrategy() {
        // base=10, max=10000
        FibonacciWaitStrategy ws = new FibonacciWaitStrategy(10, 10000);
        // fib(1)=1 → 10
        assertEquals(10L, ws.computeWaitTime(1));
        // fib(2)=1 → 10
        assertEquals(10L, ws.computeWaitTime(2));
        // fib(3)=2 → 20
        assertEquals(20L, ws.computeWaitTime(3));
        // fib(4)=3 → 30
        assertEquals(30L, ws.computeWaitTime(4));
        // fib(5)=5 → 50
        assertEquals(50L, ws.computeWaitTime(5));
        // fib(6)=8 → 80
        assertEquals(80L, ws.computeWaitTime(6));

        // 上限测试
        FibonacciWaitStrategy ws2 = new FibonacciWaitStrategy(1000, 5000);
        // fib(1)=1 → 1000
        assertEquals(1000L, ws2.computeWaitTime(1));
        // fib(5)=5 → 5000
        assertEquals(5000L, ws2.computeWaitTime(5));
        // fib(6)=8 → 8000 但上限 5000
        assertEquals(5000L, ws2.computeWaitTime(6));
    }

    // ==================== 8. StopStrategy 验证 ====================

    @Test
    public void testMaxAttemptsStopStrategy() {
        MaxAttemptsStopStrategy ss = new MaxAttemptsStopStrategy(3);
        // attempt=1, elapsed 任意
        assertFalse(ss.shouldStop(1, 0));
        assertFalse(ss.shouldStop(2, 0));
        // attempt=3 → stop
        assertTrue(ss.shouldStop(3, 0));
        assertTrue(ss.shouldStop(4, 0));
    }

    @Test
    public void testTimeoutStopStrategy() {
        TimeoutStopStrategy ss = new TimeoutStopStrategy(1000);
        assertFalse(ss.shouldStop(1, 500));
        assertFalse(ss.shouldStop(2, 999));
        // elapsed=1000 → stop
        assertTrue(ss.shouldStop(1, 1000));
        assertTrue(ss.shouldStop(2, 1500));
    }

    @Test
    public void testNeverStopStrategy() {
        NeverStopStrategy ss = new NeverStopStrategy();
        assertFalse(ss.shouldStop(1, 0));
        assertFalse(ss.shouldStop(100, 100000));
        assertFalse(ss.shouldStop(Integer.MAX_VALUE, Long.MAX_VALUE));
    }

    // ==================== 9. RetryCondition ====================

    @Test
    public void testRetryCondition_byExceptionType() {
        // 只重试 IOException
        Retryer<String> retryer = RetryUtils.<String>builder()
                .maxAttempts(3)
                .waitStrategy(new FixedWaitStrategy(1))
                .retryOn(IOException.class)
                .build();

        // 抛出 IOException 应重试，最终耗尽
        try {
            retryer.call(() -> {
                throw new IOException("io error");
            });
            fail("Should throw RetryExhaustedException");
        } catch (RetryExhaustedException e) {
            // expected
        }

        // 抛出 RuntimeException（非 IOException）不应重试，直接抛出
        Retryer<String> retryer2 = RetryUtils.<String>builder()
                .maxAttempts(3)
                .retryOn(IOException.class)
                .build();
        try {
            retryer2.call(() -> {
                throw new RuntimeException("non-io");
            });
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("non-io", e.getMessage());
        }
    }

    @Test
    public void testRetryCondition_byResultPredicate() {
        // 当结果小于 0 时重试
        AtomicInteger counter = new AtomicInteger(0);
        Retryer<Integer> retryer = RetryUtils.<Integer>builder()
                .maxAttempts(4)
                .waitStrategy(new FixedWaitStrategy(1))
                .retryIf(result -> result < 0)
                .build();

        Integer result = retryer.call(() -> {
            int val = counter.incrementAndGet();
            if (val < 3) {
                return -1; // 触发重试
            }
            return val;
        });
        assertEquals(Integer.valueOf(3), result);
        assertEquals(3, counter.get());
    }

    // ==================== 10. callAsync 未传 executor ====================

    @Test(expected = IllegalStateException.class)
    public void testCallAsyncWithoutExecutor() {
        Retryer<String> retryer = RetryUtils.<String>builder()
                .maxAttempts(3)
                .build();
        retryer.callAsync(() -> CompletableFuture.completedFuture("x"));
    }

    // ==================== 额外边界测试 ====================

    @Test
    public void testRetryExhaustedException_creation() {
        RetryExhaustedException ex1 = new RetryExhaustedException("msg");
        assertEquals("msg", ex1.getMessage());

        Throwable cause = new RuntimeException("cause");
        RetryExhaustedException ex2 = new RetryExhaustedException("msg2", cause);
        assertEquals("msg2", ex2.getMessage());
        assertSame(cause, ex2.getCause());
    }

    @Test
    public void testRetry_zeroDelay_shouldNotBlock() throws Exception {
        // delay=0 时，FixedWaitStrategy 返回 0，不应调用 sleep
        String result = RetryUtils.retry(3, 0L, () -> "zero_delay");
        assertEquals("zero_delay", result);
    }

    @Test
    public void testRetry_successAfterRetries() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        String result = RetryUtils.retry(3, () -> {
            if (counter.incrementAndGet() < 3) {
                throw new RuntimeException("not yet");
            }
            return "finally_ok";
        });
        assertEquals("finally_ok", result);
        assertEquals(3, counter.get());
    }

    @Test
    public void testAsyncRetry_successAfterRetries() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        Retryer<String> retryer = RetryUtils.<String>builder()
                .maxAttempts(4)
                .waitStrategy(new FixedWaitStrategy(1))
                .withScheduledExecutor(executor)
                .build();

        CompletableFuture<String> future = retryer.callAsync(() -> {
            if (counter.incrementAndGet() < 3) {
                CompletableFuture<String> cf = new CompletableFuture<>();
                cf.completeExceptionally(new RuntimeException("not yet"));
                return cf;
            }
            return CompletableFuture.completedFuture("async_finally_ok");
        });

        assertEquals("async_finally_ok", future.get(10, TimeUnit.SECONDS));
        assertEquals(3, counter.get());
    }

    @Test
    public void testRetryCondition_default_retryOnException() {
        // 默认 RetryCondition：有异常就重试
        AtomicInteger counter = new AtomicInteger(0);
        try {
            RetryUtils.retry(3, () -> {
                counter.incrementAndGet();
                throw new RuntimeException("fail");
            });
        } catch (RetryExhaustedException e) {
            // expected
        }
        // 应该有 3 次尝试
        assertEquals(3, counter.get());
    }

    @Test
    public void testBuilder_defaultValues() {
        Retryer<String> retryer = RetryUtils.<String>builder().build();
        assertNotNull(retryer);
        // 默认 maxAttempts=3，默认使用 ExponentialBackoff，默认按异常重试
        String result = retryer.call(() -> "default_ok");
        assertEquals("default_ok", result);
    }

    @Test
    public void testExponentialBackoffWaitStrategy_defaultConstructor() {
        ExponentialBackoffWaitStrategy ws = new ExponentialBackoffWaitStrategy();
        assertEquals(500L, ws.computeWaitTime(1));
        assertEquals(1000L, ws.computeWaitTime(2));
        assertEquals(2000L, ws.computeWaitTime(3));
        assertEquals(4000L, ws.computeWaitTime(4));
        assertEquals(8000L, ws.computeWaitTime(5));
        // 上限 10000
        assertEquals(10000L, ws.computeWaitTime(6));
        assertEquals(10000L, ws.computeWaitTime(7));
    }

    @Test
    public void testRetryCondition_retryOnMultipleExceptionTypes() {
        AtomicInteger counter = new AtomicInteger(0);
        Retryer<String> retryer = RetryUtils.<String>builder()
                .maxAttempts(3)
                .waitStrategy(new FixedWaitStrategy(1))
                .retryOn(IOException.class, IllegalArgumentException.class)
                .build();

        try {
            retryer.call(() -> {
                counter.incrementAndGet();
                throw new IllegalArgumentException("bad arg");
            });
        } catch (RetryExhaustedException e) {
            // expected - all 3 attempts exhausted
        }
        assertEquals(3, counter.get());
    }

    @Test
    public void testAsyncRetry_recoveryCallback() throws Exception {
        Retryer<String> retryer = RetryUtils.<String>builder()
                .maxAttempts(2)
                .waitStrategy(new FixedWaitStrategy(1))
                .withScheduledExecutor(executor)
                .recover((Throwable t) -> "async_recover_value")
                .build();

        CompletableFuture<String> future = retryer.callAsync(
                () -> {
                    CompletableFuture<String> cf = new CompletableFuture<>();
                    cf.completeExceptionally(new RuntimeException("async fail"));
                    return cf;
                });

        assertEquals("async_recover_value", future.get(5, TimeUnit.SECONDS));
    }
}
