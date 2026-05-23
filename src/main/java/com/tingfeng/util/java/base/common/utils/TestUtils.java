package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.lang.base.*;
import com.tingfeng.util.java.base.lang.exception.test.TestExecutionException;
import com.tingfeng.util.java.base.lang.exception.test.TestInterruptedException;
import com.tingfeng.util.java.base.lang.exception.test.TestTimeoutException;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

/**
 * 测试工具类，支持并发测试、超时、死锁检测等功能
 */
public final class TestUtils {

    private static final long DEFAULT_TIMEOUT_MS = 30_000;
    private static final long DEFAULT_SLEEP_MS = 10;

    private TestUtils() {
    }

    // ==================== 核心并发测试 ====================

    /**
     * 并发执行测试任务
     */
    public static void runConcurrentTest(int thread, int cycleCountInThread,
                                         BiConsumer<Integer, Integer> action) {
        runConcurrentTest(thread, cycleCountInThread, action, DEFAULT_TIMEOUT_MS);
    }

    public static void runConcurrentTest(int thread, int cycleCountInThread,
                                         BiConsumer<Integer, Integer> action,
                                         long timeoutMs) {
        validateParams(thread, cycleCountInThread);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(thread);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicInteger activeCount = new AtomicInteger(thread);

        for (int i = 0; i < thread; i++) {
            int threadNo = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < cycleCountInThread && error.get() == null; j++) {
                        action.accept(threadNo, j);
                    }
                } catch (Throwable e) {
                    error.compareAndSet(null, e);
                } finally {
                    doneLatch.countDown();
                    activeCount.decrementAndGet();
                }
            }).start();
        }

        startLatch.countDown();
        boolean completed = awaitLatch(doneLatch, timeoutMs);

        if (!completed) {
            throw new TestTimeoutException("Test execution timeout after " + timeoutMs + "ms", timeoutMs);
        }
        if (error.get() != null) {
            throw new TestExecutionException("Test execution failed", error.get());
        }
    }

    public static void runConcurrentTest(int thread, int cycleCountInThread,
                                         BiConsumer<Integer, Integer> action,
                                         BiFunction<Integer, Integer, Boolean> progressCallback,
                                         long timeoutMs) {
        validateParams(thread, cycleCountInThread);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(thread);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicInteger activeCount = new AtomicInteger(thread);

        for (int i = 0; i < thread; i++) {
            int threadNo = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < cycleCountInThread && error.get() == null; j++) {
                        if (progressCallback != null) {
                            Boolean shouldContinue = progressCallback.apply(threadNo, j);
                            if (shouldContinue == null || !shouldContinue) {
                                break;
                            }
                        }
                        action.accept(threadNo, j);
                    }
                } catch (Throwable e) {
                    error.compareAndSet(null, e);
                } finally {
                    doneLatch.countDown();
                    activeCount.decrementAndGet();
                }
            }).start();
        }

        startLatch.countDown();
        boolean completed = awaitLatch(doneLatch, timeoutMs);

        if (!completed) {
            throw new TestTimeoutException("Test execution timeout after " + timeoutMs + "ms", timeoutMs);
        }
        if (error.get() != null) {
            throw new TestExecutionException("Test execution failed", error.get());
        }
    }

    // ==================== 结果收集 ====================

    public static <T> List<T> runConcurrentTestWithResults(
            int thread, int cycles,
            IntFunction<T> resultFactory,
            BiConsumer<Integer, T> action,
            long timeoutMs) {
        validateParams(thread, cycles);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(thread);
        List<T> results = Collections.synchronizedList(new ArrayList<>(thread));
        AtomicReference<Throwable> error = new AtomicReference<>();

        for (int i = 0; i < thread; i++) {
            int threadNo = i;
            T localResult = resultFactory.apply(i);
            results.add(localResult);
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < cycles && error.get() == null; j++) {
                        action.accept(threadNo, localResult);
                    }
                } catch (Throwable e) {
                    error.compareAndSet(null, e);
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        awaitLatch(doneLatch, timeoutMs);

        if (error.get() != null) {
            throw new TestExecutionException("Test execution failed", error.get());
        }
        return results;
    }

    // ==================== 栅栏同步 ====================

    public static void runConcurrentTestWithBarrier(
            int thread, int cycles,
            BiConsumer<Integer, Integer> action,
            long timeoutMs) {
        validateParams(thread, cycles);
        CyclicBarrier barrier = new CyclicBarrier(thread);
        CountDownLatch doneLatch = new CountDownLatch(thread);
        AtomicReference<Throwable> error = new AtomicReference<>();

        for (int i = 0; i < thread; i++) {
            int threadNo = i;
            new Thread(() -> {
                try {
                    barrier.await();
                    for (int j = 0; j < cycles && error.get() == null; j++) {
                        action.accept(threadNo, j);
                    }
                } catch (Throwable e) {
                    if (!(e instanceof BrokenBarrierException)) {
                        error.compareAndSet(null, e);
                    }
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        awaitLatch(doneLatch, timeoutMs);

        if (error.get() != null) {
            throw new TestExecutionException("Test execution failed", error.get());
        }
    }

    // ==================== 重复执行 ====================

    public static void runRepeated(int times, int thread, int cycles,
                                   BiConsumer<Integer, Integer> action) {
        for (int i = 0; i < times; i++) {
            runConcurrentTest(thread, cycles, action);
        }
    }

    public static void runRepeated(int times, int thread, int cycles,
                                   BiConsumer<Integer, Integer> action,
                                   BiConsumer<Integer, Integer> onFailure) {
        for (int i = 0; i < times; i++) {
            try {
                runConcurrentTest(thread, cycles, action);
            } catch (Throwable e) {
                onFailure.accept(i, i);
            }
        }
    }

    // ==================== 中断响应测试 ====================

    public static void testInterruptResponse(
            Runnable action,
            ExpectedInterruptBehavior expected) {
        Thread worker = new Thread(() -> {
            try {
                action.run();
            } catch (Throwable e) {
                throw e;
            }
        });

        worker.start();
        worker.interrupt();

        if (expected == ExpectedInterruptBehavior.SET_FLAG_ONLY) {
            try {
                worker.join(DEFAULT_TIMEOUT_MS);
            } catch (InterruptedException e) {
                throw new TestInterruptedException("Join interrupted", e);
            }
            if (worker.isAlive()) {
                worker.stop();
            }
        } else {
            try {
                worker.join(DEFAULT_TIMEOUT_MS);
            } catch (InterruptedException e) {
                throw new TestInterruptedException("Join interrupted", e);
            }
            if (worker.isAlive()) {
                worker.stop();
                throw new AssertionError("Thread did not terminate");
            }
        }
    }

    // ==================== ThreadLocal清理验证 ====================

    public static <T> boolean testThreadLocalCleanup(
            Supplier<T> threadLocalSupplier,
            Runnable action,
            long timeoutMs) throws Exception {
        AtomicReference<WeakReference<T>> refHolder = new AtomicReference<>(new WeakReference<>(null));
        CountDownLatch doneLatch = new CountDownLatch(1);

        Thread worker = new Thread(() -> {
            try {
                T value = threadLocalSupplier.get();
                refHolder.set(new WeakReference<>(value));
                action.run();
            } finally {
                doneLatch.countDown();
            }
        });

        worker.start();
        boolean completed = awaitLatch(doneLatch, timeoutMs);

        if (!completed) {
            worker.interrupt();
            throw new TestTimeoutException("Thread did not complete", timeoutMs);
        }

        System.gc();
        Thread.sleep(100);

        WeakReference<T> ref = refHolder.get();
        return ref == null || ref.get() == null;
    }

    // ==================== 死锁检测 ====================

    public static DeadlockResult testForDeadlock(
            int thread, int cycles,
            BiConsumer<Integer, Integer> action,
            long timeoutMs) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ExecutorService executor = Executors.newFixedThreadPool(thread);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        for (int i = 0; i < thread; i++) {
            int threadNo = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < cycles && error.get() == null; j++) {
                        action.accept(threadNo, j);
                    }
                } catch (Throwable e) {
                    error.compareAndSet(null, e);
                }
            });
        }

        startLatch.countDown();
        boolean completed;
        try {
            completed = executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            completed = false;
        }

        if (!completed) {
            executor.shutdownNow();
        }

        if (error.get() != null) {
            throw new TestExecutionException("Test execution failed", error.get());
        }

        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        if (deadlockedThreads != null && deadlockedThreads.length > 0) {
            Map<Long, String> traces = new HashMap<>();
            List<Long> ids = new ArrayList<>();
            for (long id : deadlockedThreads) {
                ThreadInfo info = threadMXBean.getThreadInfo(id);
                if (info != null) {
                    traces.put(id, info.toString());
                    ids.add(id);
                }
            }
            return DeadlockResult.withDeadlock(ids, traces);
        }

        return DeadlockResult.noDeadlock();
    }

    // ==================== 内存屏障测试 ====================

    public static <W> boolean testHappensBefore(
            Supplier<W> writerAction,
            Consumer<W> readerAction,
            Predicate<W> assertion,
            long timeoutMs) throws Exception {
        AtomicReference<W> sharedRef = new AtomicReference<>();
        AtomicBoolean writeCompleted = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(2);

        Thread writer = new Thread(() -> {
            try {
                W value = writerAction.get();
                sharedRef.set(value);
                writeCompleted.set(true);
            } finally {
                latch.countDown();
            }
        });

        Thread reader = new Thread(() -> {
            try {
                while (!writeCompleted.get()) {
                    Thread.sleep(DEFAULT_SLEEP_MS);
                }
                W value = sharedRef.get();
                readerAction.accept(value);
            } catch (Exception e) {
                // ignore
            } finally {
                latch.countDown();
            }
        });

        writer.start();
        reader.start();

        boolean completed = awaitLatch(latch, timeoutMs);
        if (!completed) {
            throw new TestTimeoutException("Happens-before test timeout", timeoutMs);
        }

        W result = sharedRef.get();
        if (assertion != null && result != null) {
            return assertion.test(result);
        }
        return result != null;
    }

    // ==================== 并发压力测试 ====================

    public static ConcurrencyLimitResult findConcurrencyLimit(
            IntUnaryOperator actionPerThread,
            int maxThreads,
            long timeoutPerLevel) {
        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= maxThreads; i++) {
            ExecutorService executor = Executors.newFixedThreadPool(i);
            CountDownLatch latch = new CountDownLatch(i);
            AtomicReference<Throwable> error = new AtomicReference<>();

            for (int j = 0; j < i; j++) {
                final int threadNo = j;
                executor.submit(() -> {
                    try {
                        actionPerThread.applyAsInt(threadNo);
                    } catch (Throwable e) {
                        error.compareAndSet(null, e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean completed;
            try {
                completed = latch.await(timeoutPerLevel, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            executor.shutdownNow();

            if (!completed || error.get() != null) {
                long duration = System.currentTimeMillis() - startTime;
                String reason = !completed ? "timeout" :
                    "exception: " + (error.get() != null ? error.get().getMessage() : "unknown");
                return new ConcurrencyLimitResult(i - 1, i, duration, reason);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        return new ConcurrencyLimitResult(maxThreads, 0, duration, null);
    }

    // ==================== 超时+返回值 ====================

    public static <T> TimedResult<T> runWithTimeout(
            Supplier<T> supplier,
            long timeoutMs,
            TimeUnit unit) {
        long startTime = System.currentTimeMillis();
        FutureTask<T> task = new FutureTask<>(supplier::get);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.execute(task);
            T result = task.get(timeoutMs, unit);
            long elapsed = System.currentTimeMillis() - startTime;
            return TimedResult.success(result, elapsed);
        } catch (TimeoutException e) {
            task.cancel(true);
            long elapsed = System.currentTimeMillis() - startTime;
            return TimedResult.timeout(elapsed);
        } catch (Exception e) {
            task.cancel(true);
            long elapsed = System.currentTimeMillis() - startTime;
            return new TimedResult<>(null, elapsed, false);
        } finally {
            executor.shutdownNow();
        }
    }

    public static <T> TimedResult<T> runWithTimeout(
            Supplier<T> supplier,
            long timeoutMs) {
        return runWithTimeout(supplier, timeoutMs, TimeUnit.MILLISECONDS);
    }

    // ==================== 异常收集 ====================

    public static List<Throwable> runAndCollectAllErrors(
            int thread, int cycles,
            BiConsumer<Integer, Integer> action) {
        validateParams(thread, cycles);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(thread);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < thread; i++) {
            int threadNo = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < cycles; j++) {
                        try {
                            action.accept(threadNo, j);
                        } catch (Throwable e) {
                            errors.add(e);
                        }
                    }
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        try {
            doneLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return errors;
    }

    // ==================== 工具方法 ====================

    private static void validateParams(int thread, int cycles) {
        if (thread <= 0) {
            throw new IllegalArgumentException("thread must be positive");
        }
        if (cycles <= 0) {
            throw new IllegalArgumentException("cycleCountInThread must be positive");
        }
    }

    private static boolean awaitLatch(CountDownLatch latch, long timeoutMs) {
        try {
            return latch.await(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    // ==================== 兼容性保留方法 ====================

    /**
     * @deprecated Use {@link #runConcurrentTest(int, int, BiConsumer)} instead
     */
    @Deprecated
    public static void printTime(int thread, int cycleCountInThread,
                                  java.util.function.BiConsumer<Integer, Integer> functionVTwo) {
        runConcurrentTest(thread, cycleCountInThread, functionVTwo);
    }

    /**
     * @deprecated Use {@link #runConcurrentTest(int, int, BiConsumer)} instead
     */
    @Deprecated
    public static void printTime(int thread, int cycleCountInThread,
                                  java.util.function.Consumer<Integer> functionVOne) {
        runConcurrentTest(thread, cycleCountInThread, (t, c) -> functionVOne.accept(c));
    }
}
