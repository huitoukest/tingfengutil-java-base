package com.tingfeng.util.java.base.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 异步流处理工具
 *
 * - 异步流拷贝：copyAsync（双线程管道模型，背压控制 + 取消令牌）
 * - 异步按行读取：readLinesAsync（支持暂停背压与错误回调）
 * - 背压感知批处理：readStreamWithBackpressure
 * - 流式传输：transmitStream（支持流水线处理）
 * - 线程池适配：toExecutorService / shutdownIfSelfManaged
 *
 * 本类为 IOUtils 拆分产物，IOUtils 门面委托本类方法，公开行为与拆分前完全一致。
 *
 * @author huitoukest
 */
public final class StreamAsyncOps {

    /**
     * 管道终止标记：生产者所有退出路径必须放入队列，消费者遇到即退出（P-INV-1）
     */
    private static final Object POISON = new Object();

    private StreamAsyncOps() {
    }

    // ==================== 异步流处理 ====================

    /**
     * 异步流拷贝（带进度回调和背压控制）
     *
     * 双线程管道模型：生产者=内部 daemon 线程（读取入队），消费者=executor 内唯一池任务（出队写出）。
     * 队列容量 = backPressureLimit / 4096（块，向下取整且不小于 1），队列满时生产者真实阻塞（背压生效）。
     * 回调返回 false 表示暂停：消费者停止消费，队列填满后生产者阻塞，直到 token 取消/超时收敛退出。
     * 生产者读异常经 POISON 传播，future 以项目自定义 IOException 异常完成。
     *
     * @param output 输出流
     * @param input 输入流
     * @param executor ExecutorService或Thread/Runnable，外部传入线程管理
     * @param progressCallback 进度回调，参数为已拷贝字节数，返回true继续，false暂停
     * @param backPressureLimit 背压缓冲区上限（字节），默认8MB
     * @param token 取消令牌，null表示不支持取消
     * @return CompletableFuture，任务完成后返回实际拷贝字节数
     */
    public static CompletableFuture<Long> copyAsync(OutputStream output, InputStream input,
                                                    Object executor,
                                                    Function<Long, Boolean> progressCallback,
                                                    int backPressureLimit,
                                                    IOUtils.CancellationToken token) {
        LinkedBlockingQueue<Object> queue =
            new LinkedBlockingQueue<>(Math.max(1, backPressureLimit / StreamOps.BUFFER_SIZE));
        AtomicBoolean done = new AtomicBoolean(false);
        AtomicReference<RuntimeException> producerError = new AtomicReference<>();

        // 生产者：内部 daemon 线程，生命周期与管道绑定（消费者退出即终止，不经 executor，单线程池下无死锁）
        Thread producer = new Thread(() -> {
            RuntimeException error = null;
            try {
                byte[] buffer = new byte[StreamOps.BUFFER_SIZE];
                int len;
                while (token == null || !token.shouldInterrupt()) {
                    len = input.read(buffer);
                    if (len == -1) {
                        break;
                    }
                    if (len == 0) {
                        continue;
                    }
                    if (!putItem(queue, Arrays.copyOf(buffer, len), done)) {
                        break;
                    }
                }
            } catch (IOException e) {
                error = new com.tingfeng.util.java.base.lang.exception.IOException(e);
            } catch (RuntimeException e) {
                error = e;
            } finally {
                if (error != null) {
                    producerError.set(error);
                }
                putItem(queue, POISON, done);
            }
        }, "IOUtils-pipe-producer");
        producer.setDaemon(true);
        producer.start();

        // 消费者：executor 内唯一池任务
        ExecutorService es = toExecutorService(executor);
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
            long total = 0;
            RuntimeException error = null;
            try {
                while (true) {
                    Object item = queue.take();
                    if (item == POISON) {
                        break;
                    }
                    byte[] chunk = (byte[]) item;
                    output.write(chunk);
                    total += chunk.length;
                    if (progressCallback != null) {
                        Boolean continueRead = progressCallback.apply(total);
                        if (continueRead != null && !continueRead) {
                            pauseUntilCancelOrDone(done, token);
                            break;
                        }
                    }
                }
                output.flush();
            } catch (IOException e) {
                error = new com.tingfeng.util.java.base.lang.exception.IOException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                error = new com.tingfeng.util.java.base.lang.exception.IOException("Copy operation interrupted", e);
            } catch (RuntimeException e) {
                error = e;
            } finally {
                done.set(true);
            }
            if (error != null) {
                throw error;
            }
            if (producerError.get() != null) {
                throw producerError.get();
            }
            return total;
        }, es);
        return future.whenComplete((r, ex) -> shutdownIfSelfManaged(es));
    }

    /**
     * 异步流拷贝（使用默认背压限制）
     */
    public static CompletableFuture<Long> copyAsync(OutputStream output, InputStream input,
                                                    Object executor,
                                                    Function<Long, Boolean> progressCallback,
                                                    IOUtils.CancellationToken token) {
        return copyAsync(output, input, executor, progressCallback, IOUtils.DEFAULT_BACK_PRESSURE_BUFFER_SIZE, token);
    }

    /**
     * 异步按行读取流
     *
     * @param input 输入流
     * @param charset 字符编码
     * @param executor ExecutorService或Thread/Runnable
     * @param lineConsumer 行处理回调，返回true继续，false暂停（背压）
     * @param errorHandler 错误处理回调
     * @param token 取消令牌
     */
    public static void readLinesAsync(InputStream input, Charset charset,
                                      Object executor,
                                      Function<String, Boolean> lineConsumer,
                                      Consumer<Throwable> errorHandler,
                                      IOUtils.CancellationToken token) {
        ExecutorService es = toExecutorService(executor);
        CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 检查取消令牌
                    if (token != null && token.shouldInterrupt()) {
                        break;
                    }

                    // 回调处理
                    if (lineConsumer != null) {
                        Boolean continueRead = lineConsumer.apply(line);
                        if (continueRead != null && !continueRead) {
                            // 暂停，sleep一小段时间
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                if (errorHandler != null) {
                    errorHandler.accept(e);
                }
            }
        }, es).whenComplete((r, ex) -> shutdownIfSelfManaged(es));
    }

    /**
     * 异步按行读取流（无背压控制）
     */
    public static void readLinesAsync(InputStream input, Charset charset,
                                      Object executor,
                                      Consumer<String> lineConsumer,
                                      IOUtils.CancellationToken token) {
        readLinesAsync(input, charset, executor,
            line -> {
                lineConsumer.accept(line);
                return true;
            },
            null, token);
    }

    /**
     * 背压感知的流式读取（批处理模式）
     *
     * 双线程管道模型：生产者=内部 daemon 线程（逐行读取入队），消费者=executor 内唯一池任务（出队攒批回调）。
     * 队列容量 = backPressureLimit（行），队列满时生产者真实阻塞（背压生效）。
     * 回调返回 false 表示暂停：消费者停止消费，队列填满后生产者阻塞，直到 token 取消/超时收敛退出。
     * EOF 后残留不足一批的行会作为最后一批回调（无死循环）。
     *
     * @param input 输入流
     * @param charset 字符编码
     * @param batchSize 每批行数
     * @param batchConsumer 批次回调，返回true继续，false暂停
     * @param executor ExecutorService或Thread/Runnable
     * @param backPressureLimit 背压缓冲区上限（行数）
     * @param token 取消令牌
     */
    public static void readStreamWithBackpressure(InputStream input, Charset charset,
                                                  int batchSize,
                                                  Function<List<String>, Boolean> batchConsumer,
                                                  Object executor,
                                                  int backPressureLimit,
                                                  IOUtils.CancellationToken token) {
        LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>(backPressureLimit);
        AtomicBoolean done = new AtomicBoolean(false);
        AtomicReference<RuntimeException> producerError = new AtomicReference<>();

        // 生产者：内部 daemon 线程，生命周期与管道绑定（消费者退出即终止，不经 executor，单线程池下无死锁）
        Thread producer = new Thread(() -> {
            RuntimeException error = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset))) {
                String line;
                while (token == null || !token.shouldInterrupt()) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    if (!putItem(queue, line, done)) {
                        break;
                    }
                }
            } catch (IOException e) {
                error = new com.tingfeng.util.java.base.lang.exception.IOException(e);
            } catch (RuntimeException e) {
                error = e;
            } finally {
                if (error != null) {
                    producerError.set(error);
                }
                putItem(queue, POISON, done);
            }
        }, "IOUtils-pipe-producer");
        producer.setDaemon(true);
        producer.start();

        // 消费者：executor 内唯一池任务
        ExecutorService es = toExecutorService(executor);
        CompletableFuture.runAsync(() -> {
            List<String> batch = new ArrayList<>(batchSize);
            RuntimeException error = null;
            boolean cancelled = false;
            try {
                while (true) {
                    Object item = queue.take();
                    if (item == POISON) {
                        break;
                    }
                    batch.add((String) item);
                    if (batch.size() >= batchSize) {
                        if (batchConsumer != null) {
                            Boolean continueRead = batchConsumer.apply(new ArrayList<>(batch));
                            if (continueRead != null && !continueRead) {
                                pauseUntilCancelOrDone(done, token);
                                cancelled = true;
                                break;
                            }
                        }
                        batch.clear();
                    }
                }
                // EOF 后处理残余批次（死循环修复点）
                if (!cancelled && !batch.isEmpty() && batchConsumer != null) {
                    batchConsumer.apply(batch);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                error = new com.tingfeng.util.java.base.lang.exception.IOException("Read operation interrupted", e);
            } catch (RuntimeException e) {
                error = e;
            } finally {
                done.set(true);
            }
            if (error != null) {
                throw error;
            }
            if (producerError.get() != null) {
                throw producerError.get();
            }
        }, es).whenComplete((r, ex) -> shutdownIfSelfManaged(es));
    }

    /**
     * 背压感知的流式读取（使用默认背压限制和批次大小）
     */
    public static void readStreamWithBackpressure(InputStream input, Charset charset,
                                                  Function<List<String>, Boolean> batchConsumer,
                                                  Object executor,
                                                  IOUtils.CancellationToken token) {
        readStreamWithBackpressure(input, charset, 100, batchConsumer, executor,
            IOUtils.DEFAULT_BACK_PRESSURE_BUFFER_SIZE, token);
    }

    /**
     * 流式传输（支持流水线处理）
     *
     * @param sourceSupplier 数据源供应器
     * @param output 输出流
     * @param executor ExecutorService或Thread/Runnable
     * @param flushCallback 每写入后的回调
     * @param token 取消令牌
     * @param <T> 数据类型
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<Long> transmitStream(Supplier<T> sourceSupplier,
                                                              OutputStream output,
                                                              Object executor,
                                                              Consumer<T> flushCallback,
                                                              IOUtils.CancellationToken token) {
        ExecutorService es = toExecutorService(executor);
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
            long total = 0;
            T data;
            try {
                while ((data = sourceSupplier.get()) != null) {
                    // 检查取消令牌
                    if (token != null && token.shouldInterrupt()) {
                        break;
                    }

                    if (data instanceof byte[]) {
                        byte[] bytes = (byte[]) data;
                        output.write(bytes);
                        total += bytes.length;
                    } else if (data instanceof String) {
                        String str = (String) data;
                        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
                        output.write(bytes);
                        total += bytes.length;
                    } else {
                        throw new IllegalArgumentException("Unsupported data type: " + data.getClass());
                    }

                    if (flushCallback != null) {
                        flushCallback.accept(data);
                    }
                }
                output.flush();
            } catch (IOException e) {
                throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
            }
            return total;
        }, es);
        return future.whenComplete((r, ex) -> shutdownIfSelfManaged(es));
    }

    // ==================== 线程池适配 ====================

    /**
     * 有界队列写入：1 秒超时轮询直到写入成功或管道终止（done 置位）
     *
     * 数据与 POISON 共用（P-INV-1/P-INV-2）：队列满时真实阻塞（背压生效）；
     * 消费者已终止（done=true）则放弃写入直接退出，避免写无人消费的队列。
     *
     * @param queue 管道队列
     * @param item 待写入元素（数据或 POISON）
     * @param done 管道终止标志（消费者置位，P-INV-2）
     * @return true=写入成功，false=管道已终止放弃写入
     */
    private static boolean putItem(LinkedBlockingQueue<Object> queue, Object item, AtomicBoolean done) {
        while (!done.get()) {
            try {
                if (queue.offer(item, 1, TimeUnit.SECONDS)) {
                    return true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }

    /**
     * 消费者暂停循环（回调返回 false 后调用，P-INV-4）
     *
     * 暂停期间每 100ms 检查一次取消令牌：取消/超时则置 done 并返回（消费者退出，不等 POISON）；
     * 无取消能力（token 为 null）时保持暂停，直到线程被中断（异常路径收敛）。
     *
     * @param done 管道终止标志（消费者置位，P-INV-2）
     * @param token 取消令牌，null 表示不支持取消
     * @throws InterruptedException 暂停期间线程被中断
     */
    private static void pauseUntilCancelOrDone(AtomicBoolean done, IOUtils.CancellationToken token)
        throws InterruptedException {
        while (!done.get()) {
            if (token != null && token.shouldInterrupt()) {
                done.set(true);
                return;
            }
            Thread.sleep(100);
        }
    }

    /**
     * 线程池工具方法：将Thread/Runnable转换为ExecutorService
     *
     * ExecutorService 原样返回（不自管，由调用方负责关闭）；
     * Thread / Runnable 返回自建单线程池（daemon 线程，异步方法完成后 whenComplete 自动 shutdown），
     * 传入对象本身不再被启动执行，仅表示"任务在新建单线程中执行"。
     *
     * @param executor Thread、Runnable或ExecutorService
     * @return ExecutorService
     */
    public static ExecutorService toExecutorService(Object executor) {
        if (executor instanceof ExecutorService) {
            return (ExecutorService) executor;
        } else if (executor instanceof Thread || executor instanceof Runnable) {
            return new SelfManagedExecutorService();
        }
        throw new IllegalArgumentException("Unsupported executor type: " +
            (executor == null ? "null" : executor.getClass().getName()));
    }

    /**
     * 自管线程池：Thread/Runnable 语义修正后的返回池
     *
     * 内部 ThreadPoolExecutor(1,1) + daemon 线程工厂 + 有界队列；
     * 由异步方法 whenComplete 自动 shutdown，即使未 shutdown 也不阻止 JVM 退出。
     * 线程名沿用 IOUtils-async-pool-* 前缀（兼容存量测试的线程名断言）。
     */
    private static final class SelfManagedExecutorService implements ExecutorService {
        private final ThreadPoolExecutor delegate;

        SelfManagedExecutorService() {
            ThreadFactory factory = runnable -> {
                Thread thread = new Thread(runnable, "IOUtils-async-pool-1");
                thread.setDaemon(true);
                return thread;
            };
            this.delegate = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024), factory, new ThreadPoolExecutor.CallerRunsPolicy());
        }

        /**
         * 自管池标记：true 表示由本类自建、生命周期由异步方法管理
         */
        boolean isSelfManaged() {
            return true;
        }

        @Override
        public void execute(Runnable command) {
            delegate.execute(command);
        }

        @Override
        public void shutdown() {
            delegate.shutdown();
        }

        @Override
        public List<Runnable> shutdownNow() {
            return delegate.shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return delegate.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return delegate.isTerminated();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.awaitTermination(timeout, unit);
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return delegate.submit(task);
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            return delegate.submit(task, result);
        }

        @Override
        public Future<?> submit(Runnable task) {
            return delegate.submit(task);
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            return delegate.invokeAll(tasks);
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
            return delegate.invokeAll(tasks, timeout, unit);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
            return delegate.invokeAny(tasks);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
            return delegate.invokeAny(tasks, timeout, unit);
        }
    }

    /**
     * 异步方法完成后关闭自管池（外部传入的 ExecutorService 不受影响）
     *
     * 供本类异步方法及外部透传方（如 file 包）在异步任务完成后联动关闭自建池，
     * 避免 Thread/Runnable 参数每次调用泄漏空闲池线程；对非自建池（外部注入的 ExecutorService）不执行任何操作。
     *
     * @param es 待检查的线程池
     */
    public static void shutdownIfSelfManaged(ExecutorService es) {
        if (es instanceof SelfManagedExecutorService && ((SelfManagedExecutorService) es).isSelfManaged()) {
            es.shutdown();
        }
    }
}
