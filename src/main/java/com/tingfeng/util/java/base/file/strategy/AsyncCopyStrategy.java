package com.tingfeng.util.java.base.file.strategy;

import com.tingfeng.util.java.base.file.FileUtils;
import com.tingfeng.util.java.base.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * 异步拷贝策略
 * 使用 CompletableFuture 进行异步文件拷贝
 *
 * 设计原则：
 * 1. 使用工厂方法创建实例，非单例
 * 2. 内部使用 ChannelCopyStrategy 的分块拷贝逻辑
 * 3. 支持进度回调和取消令牌
 *
 * 使用示例：
 * <pre>
 * File src = new File("source.txt");
 * File dest = new File("dest.txt");
 *
 * AsyncCopyStrategy strategy = AsyncCopyStrategy.create();
 * CompletableFuture&lt;Long&gt; future = strategy.copyFileAsync(src, dest,
 *     progress -> System.out.println("Copied: " + progress + " bytes"),
 *     null, executor);
 *
 * future.thenAccept(total -> System.out.println("Done: " + total + " bytes"));
 * </pre>
 */
public class AsyncCopyStrategy implements FileCopyStrategy {

    /**
     * 默认缓冲区大小
     */
    private final int bufferSize;

    /**
     * 背压限制
     */
    private final int backPressureLimit;

    /**
     * 私有构造器，通过工厂方法创建
     */
    private AsyncCopyStrategy(int bufferSize, int backPressureLimit) {
        this.bufferSize = bufferSize > 0 ? bufferSize : FileUtils.BUFFER_SIZE;
        this.backPressureLimit = backPressureLimit > 0 ? backPressureLimit : FileUtils.DEFAULT_BACK_PRESSURE_BUFFER_SIZE;
    }

    /**
     * 工厂方法：创建异步拷贝策略（使用默认参数）
     * @return 新的 AsyncCopyStrategy 实例（非单例）
     */
    public static AsyncCopyStrategy create() {
        return new AsyncCopyStrategy(FileUtils.BUFFER_SIZE, FileUtils.DEFAULT_BACK_PRESSURE_BUFFER_SIZE);
    }

    /**
     * 工厂方法：创建异步拷贝策略（指定缓冲区大小）
     * @param bufferSize 缓冲区大小（字节）
     * @param backPressureLimit 背压限制（字节）
     * @return 新的 AsyncCopyStrategy 实例（非单例）
     */
    public static AsyncCopyStrategy create(int bufferSize, int backPressureLimit) {
        return new AsyncCopyStrategy(bufferSize, backPressureLimit);
    }

    @Override
    public void copyFile(File src, File dest) {
        copyFile(src, dest, ProgressCallback.NONE);
    }

    @Override
    public void copyFile(File src, File dest, ProgressCallback callback) {
        if (src == null || !src.exists()) {
            throw new IllegalArgumentException("Source file does not exist: " + src);
        }
        if (dest == null) {
            throw new IllegalArgumentException("Destination file must not be null");
        }

        copyFileAsync(src, dest, callback, null, null).join();
    }

    /**
     * 异步拷贝文件
     * @param src 源文件
     * @param dest 目标文件
     * @param callback 进度回调
     * @param executor 执行器
     * @param token 取消令牌
     * @return CompletableFuture，完成后返回实际拷贝的字节数
     */
    public CompletableFuture<Long> copyFileAsync(File src, File dest,
                                                  ProgressCallback callback,
                                                  ExecutorService executor,
                                                  IOUtils.CancellationToken token) {
        return CompletableFuture.supplyAsync(() -> {
            long total = 0;
            FileChannel in = null;
            FileChannel out = null;
            FileInputStream fis = null;
            FileOutputStream fos = null;

            try {
                // 确保目标父目录存在
                File parent = dest.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }

                fis = new FileInputStream(src);
                fos = new FileOutputStream(dest);
                in = fis.getChannel();
                out = fos.getChannel();

                long size = in.size();
                long remaining = size;
                int maxChunk = Math.min(backPressureLimit, 8 * 1024 * 1024); // 最大单次8MB

                while (remaining > 0) {
                    // 检查取消令牌
                    if (token != null && token.shouldInterrupt()) {
                        break;
                    }

                    long transferred = in.transferTo(total, Math.min(remaining, maxChunk), out);
                    if (transferred == 0) {
                        // 防止忙等待
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    total += transferred;
                    remaining -= transferred;

                    // 进度回调
                    if (callback != null && callback != ProgressCallback.NONE) {
                        callback.onProgress(total, size);
                    }
                }
                out.force(true);

                if (callback != null && callback != ProgressCallback.NONE) {
                    callback.onComplete(total);
                }

                return total;
            } catch (Exception e) {
                if (callback != null && callback != ProgressCallback.NONE) {
                    callback.onError(e);
                }
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
                IOUtils.closeQuietly(fis);
                IOUtils.closeQuietly(fos);
            }
        }, executor != null ? executor : ForkJoinPool.commonPool());
    }

    /**
     * 异步拷贝文件（简化版）
     * @param src 源文件
     * @param dest 目标文件
     * @param callback 进度回调
     * @param executor 执行器
     * @return CompletableFuture
     */
    public CompletableFuture<Long> copyFileAsync(File src, File dest,
                                                  ProgressCallback callback,
                                                  ExecutorService executor) {
        return copyFileAsync(src, dest, callback, executor, null);
    }
}
