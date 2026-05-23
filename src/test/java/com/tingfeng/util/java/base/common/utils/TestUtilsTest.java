package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.lang.exception.test.TestTimeoutException;
import com.tingfeng.util.java.base.lang.base.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TestUtils 单元测试
 */
public class TestUtilsTest {

    private static final long TIMEOUT_MS = 5000;

    // ==================== 核心并发测试 ====================

    @Test
    public void testRunConcurrentTest_normal_success() {
        AtomicInteger counter = new AtomicInteger(0);

        TestUtils.runConcurrentTest(5, 100, (t, c) -> counter.incrementAndGet());

        Assert.assertEquals(500, counter.get());
    }

    @Test(expected = TestTimeoutException.class)
    public void testRunConcurrentTest_timeout_throwsException() {
        TestUtils.runConcurrentTest(2, 1000, (t, c) -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 500);
    }

    @Test
    public void testRunConcurrentTest_withProgressCallback() {
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger progressCount = new AtomicInteger(0);

        TestUtils.runConcurrentTest(2, 100,
            (t, c) -> counter.incrementAndGet(),
            (t, c) -> {
                progressCount.incrementAndGet();
                return true;
            },
            TIMEOUT_MS);

        Assert.assertEquals(200, counter.get());
        Assert.assertTrue(progressCount.get() > 0);
    }

    // ==================== 结果收集 ====================

    @Test
    public void testRunConcurrentTestWithResults() {
        List<AtomicInteger> results = TestUtils.runConcurrentTestWithResults(
            3, 10,
            i -> new AtomicInteger(0),
            (threadNo, localResult) -> localResult.incrementAndGet(),
            TIMEOUT_MS
        );

        Assert.assertEquals(3, results.size());
        for (AtomicInteger ai : results) {
            Assert.assertEquals(10, ai.get());
        }
    }

    // ==================== 栅栏同步 ====================

    @Test
    public void testRunConcurrentTestWithBarrier() {
        AtomicInteger startTime = new AtomicInteger(0);
        AtomicInteger firstStart = new AtomicInteger(Integer.MAX_VALUE);

        TestUtils.runConcurrentTestWithBarrier(5, 10, (t, c) -> {
            if (firstStart.get() == Integer.MAX_VALUE) {
                firstStart.set((int) System.currentTimeMillis());
            }
            startTime.incrementAndGet();
        }, TIMEOUT_MS);

        Assert.assertEquals(50, startTime.get());
    }

    // ==================== 重复执行 ====================

    @Test
    public void testRunRepeated() {
        AtomicInteger counter = new AtomicInteger(0);

        TestUtils.runRepeated(3, 2, 10, (t, c) -> counter.incrementAndGet());

        Assert.assertEquals(60, counter.get());
    }

    @Test
    public void testRunRepeated_withFailureCallback() {
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger failCounter = new AtomicInteger(0);

        TestUtils.runRepeated(3, 2, 10,
            (t, c) -> {
                if (t == 1 && c == 5) {
                    throw new RuntimeException("Test exception");
                }
                counter.incrementAndGet();
            },
            (iteration, thread) -> failCounter.incrementAndGet()
        );

        Assert.assertTrue(failCounter.get() > 0);
    }

    // ==================== 异常收集 ====================

    @Test
    public void testRunAndCollectAllErrors() {
        List<Throwable> errors = TestUtils.runAndCollectAllErrors(3, 10, (t, c) -> {
            if (c == 5) {
                throw new RuntimeException("Error at c=5");
            }
        });

        Assert.assertTrue(errors.size() > 0);
    }

    @Test
    public void testRunAndCollectAllErrors_noErrors() {
        List<Throwable> errors = TestUtils.runAndCollectAllErrors(3, 10, (t, c) -> {});

        Assert.assertEquals(0, errors.size());
    }

    // ==================== 超时+返回值 ====================

    @Test
    public void testRunWithTimeout_success() {
        TimedResult<String> result = TestUtils.runWithTimeout(
            () -> "success",
            1000,
            TimeUnit.MILLISECONDS
        );

        Assert.assertFalse(result.isTimedOut());
        Assert.assertEquals("success", result.getResult());
        Assert.assertTrue(result.getElapsedMs() < 1000);
    }

    @Test
    public void testRunWithTimeout_timeout() {
        TimedResult<String> result = TestUtils.runWithTimeout(
            () -> {
                try {
                    Thread.sleep(2000);
                    return "done";
                } catch (InterruptedException e) {
                    return "interrupted";
                }
            },
            100,
            TimeUnit.MILLISECONDS
        );

        Assert.assertTrue(result.isTimedOut());
        Assert.assertNull(result.getResult());
    }

    // ==================== 死锁检测 ====================

    @Test
    public void testTestForDeadlock_noDeadlock() {
        DeadlockResult result = TestUtils.testForDeadlock(
            4, 100, (t, c) -> {
                // 正常操作不会死锁
            }, TIMEOUT_MS
        );

        Assert.assertFalse(result.isDeadlockDetected());
    }

    // ==================== 内存屏障测试 ====================

    @Test
    public void testTestHappensBefore() throws Exception {
        AtomicInteger sharedValue = new AtomicInteger(0);

        boolean success = TestUtils.testHappensBefore(
            () -> {
                sharedValue.set(42);
                return sharedValue;
            },
            value -> {
                Assert.assertEquals(42, value.get());
            },
            value -> value.get() == 42,
            TIMEOUT_MS
        );

        Assert.assertTrue(success);
    }

    // ==================== 并发压力测试 ====================

    @Test
    public void testFindConcurrencyLimit() {
        ConcurrencyLimitResult result = TestUtils.findConcurrencyLimit(
            threadNo -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return 0;
            },
            10,
            1000
        );

        Assert.assertTrue(result.getMaxStableConcurrency() > 0);
        Assert.assertTrue(result.getDurationMs() > 0);
    }

    // ==================== ThreadLocal清理验证 ====================

    @Test
    public void testTestThreadLocalCleanup() throws Exception {
        ThreadLocal<byte[]> threadLocal = new ThreadLocal<>();
        threadLocal.set(new byte[1024]);

        boolean cleaned = TestUtils.testThreadLocalCleanup(
            () -> {
                byte[] bytes = new byte[1024];
                threadLocal.set(bytes);
                return bytes;
            },
            () -> threadLocal.remove(),
            TIMEOUT_MS
        );

        Assert.assertTrue(cleaned);
    }

    // ==================== ConcurrentTestContext ====================

    @Test
    public void testConcurrentTestContext() {
        ConcurrentTestContext<Integer> context = new ConcurrentTestContext<>();

        for (int i = 0; i < 100; i++) {
            context.addResult(i);
        }
        context.incrementSuccess();
        context.incrementFail();

        Assert.assertEquals(100, context.getResults().size());
        Assert.assertEquals(1, context.getSuccessCount());
        Assert.assertEquals(1, context.getFailCount());
        Assert.assertEquals(2, context.getTotalCount());

        context.assertAll(n -> n >= 0, "All should be non-negative");
        context.assertNone(n -> n < 0, "None should be negative");
    }

    // ==================== 兼容性方法 ====================

    @Test
    @SuppressWarnings("deprecation")
    public void testPrintTimeDeprecated() {
        AtomicInteger counter = new AtomicInteger(0);

        TestUtils.printTime(2, 10, (t, c) -> counter.incrementAndGet());

        Assert.assertEquals(20, counter.get());
    }
}
