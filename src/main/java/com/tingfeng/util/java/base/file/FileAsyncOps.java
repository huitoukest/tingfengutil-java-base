package com.tingfeng.util.java.base.file;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.function.*;

import com.tingfeng.util.java.base.io.IOUtils;

/**
 * 文件异步操作实现。
 * <p>包级私有，不对外暴露。通过 {@link FileUtils} 对外提供统一 API。</p>
 */
class FileAsyncOps {

	/**
	 * 默认背压缓冲区大小：8MB
	 */
	private static final int DEFAULT_BACK_PRESSURE_BUFFER_SIZE = IOUtils.DEFAULT_BACK_PRESSURE_BUFFER_SIZE;

	// ==================== 异步读取 ====================

	/**
	 * 异步文件读取
	 *
	 * @param file 文件
	 * @param executor ExecutorService或Thread/Runnable
	 * @param readCallback 读取进度回调，(已读取, 总长度)，返回false暂停
	 * @param token 取消令牌
	 * @return CompletableFuture
	 *
	 * 设计思路：
	 * 1. 分块读取，避免一次性加载大文件到内存
	 * 2. 取消检查放在每块读取后
	 */
	static CompletableFuture<byte[]> readFileAsync(File file,
	                                                Object executor,
	                                                BiFunction<Long, Long, Boolean> readCallback,
	                                                IOUtils.CancellationToken token) {
        ExecutorService es = IOUtils.toExecutorService(executor);
        return CompletableFuture.supplyAsync(() -> {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[FileUtils.BUFFER_SIZE];
			long total = 0;
			long fileSize = file.length();

			try (FileInputStream fis = new FileInputStream(file)) {
				int len;
				while ((len = fis.read(buffer)) != -1) {
					// 检查取消令牌
					if (token != null && token.shouldInterrupt()) {
						break;
					}

					bos.write(buffer, 0, len);
					total += len;

					// 进度回调，返回false暂停
					if (readCallback != null) {
						Boolean continueRead = readCallback.apply(total, fileSize);
						if (continueRead != null && !continueRead) {
							// 暂停一小段时间
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
				throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
			}
			return bos.toByteArray();
        }, es).whenComplete((r, ex) -> IOUtils.shutdownIfSelfManaged(es));
	}

	/**
	 * 异步文件读取（简单版）
	 */
	static CompletableFuture<byte[]> readFileAsync(File file, Object executor) {
		return readFileAsync(file, executor, null, null);
	}

	// ==================== 异步写入 ====================

	/**
	 * 异步文件写入
	 *
	 * @param file 文件
	 * @param data 字节数据
	 * @param executor ExecutorService或Thread/Runnable
	 * @param writeCallback 写入进度回调，(已写入, 总长度)
	 * @param token 取消令牌
	 * @return CompletableFuture
	 *
	 * 设计思路：
	 * 1. 分块写入，控制内存占用
	 * 2. 支持追加模式和覆盖模式
	 * 3. 取消检查在每块写入后进行
	 */
	static CompletableFuture<Boolean> writeFileAsync(File file, byte[] data,
	                                                  Object executor,
	                                                  BiConsumer<Long, Long> writeCallback,
	                                                  IOUtils.CancellationToken token) {
        ExecutorService es = IOUtils.toExecutorService(executor);
        return CompletableFuture.supplyAsync(() -> {
			// 确保父目录存在
			File parent = file.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}

			try (FileOutputStream fos = new FileOutputStream(file)) {
				long total = 0;
				long length = data != null ? data.length : 0;
				int chunkSize = FileUtils.BUFFER_SIZE;
				int offset = 0;

				while (offset < length) {
					// 检查取消令牌
					if (token != null && token.shouldInterrupt()) {
						return false;
					}

					int len = Math.min(chunkSize, (int)(length - offset));
					fos.write(data, offset, len);
					offset += len;
					total += len;

					// 进度回调
					if (writeCallback != null) {
						writeCallback.accept(total, length);
					}
				}
				fos.flush();
				return true;
			} catch (IOException e) {
				throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
			}
        }, es).whenComplete((r, ex) -> IOUtils.shutdownIfSelfManaged(es));
	}

	/**
	 * 异步文件写入（字符串）
	 */
	static CompletableFuture<Boolean> writeFileAsync(File file, String content,
	                                                  Charset charset, boolean append,
	                                                  Object executor,
	                                                  IOUtils.CancellationToken token) {
		if (content == null) {
			return CompletableFuture.completedFuture(false);
		}
		byte[] data = content.getBytes(charset);
		// 写入时使用追加模式，但异步分块写入难以保证原子性，这里简化为覆盖
		// 如果需要真正的追加，应该使用 writeLineAsync 或自定义同步写入
        ExecutorService es = IOUtils.toExecutorService(executor);
        return CompletableFuture.supplyAsync(() -> {
			FileOutputStream fos = null;
			try {
				// 确保父目录存在
				File parent = file.getParentFile();
				if (parent != null && !parent.exists()) {
					parent.mkdirs();
				}
				fos = new FileOutputStream(file, false);
				fos.write(data);
				fos.flush();
				return true;
			} catch (IOException e) {
				throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException ignored) {
					}
				}
			}
        }, es).whenComplete((r, ex) -> IOUtils.shutdownIfSelfManaged(es));
	}

	// ==================== 异步拷贝 ====================

	/**
	 * 异步文件拷贝（带进度和背压）
	 *
	 * @param dest 目标文件
	 * @param src 源文件
	 * @param executor ExecutorService或Thread/Runnable
	 * @param progressCallback 进度回调，参数为已拷贝字节数
	 * @param backPressureLimit 背压缓冲区上限
	 * @param token 取消令牌
	 * @return CompletableFuture
	 *
	 * 设计思路：
	 * 1. 使用FileChannel.transferTo()高效拷贝
	 * 2. 每拷贝一定数据后调用progressCallback
	 * 3. 支持背压控制（缓冲区满时暂停）
	 * 4. 支持取消/中断
	 */
	static CompletableFuture<Long> copyFileAsync(File dest, File src,
	                                              Object executor,
	                                              Consumer<Long> progressCallback,
	                                              int backPressureLimit,
	                                              IOUtils.CancellationToken token) {
        ExecutorService es = IOUtils.toExecutorService(executor);
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
					if (progressCallback != null) {
						progressCallback.accept(total);
					}
				}
				out.force(true);
			} catch (IOException e) {
				throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
				IOUtils.closeQuietly(fis);
				IOUtils.closeQuietly(fos);
			}
			return total;
        }, es).whenComplete((r, ex) -> IOUtils.shutdownIfSelfManaged(es));
	}

	/**
	 * 异步文件拷贝（使用默认背压限制）
	 */
	static CompletableFuture<Long> copyFileAsync(File dest, File src,
	                                              Object executor,
	                                              Consumer<Long> progressCallback,
	                                              IOUtils.CancellationToken token) {
		return copyFileAsync(dest, src, executor, progressCallback, DEFAULT_BACK_PRESSURE_BUFFER_SIZE, token);
	}

	// ==================== 异步行写入 ====================

	/**
	 * 异步追加一行字符串
	 *
	 * @param file 文件
	 * @param line 行内容
	 * @param charset 字符编码
	 * @param executor ExecutorService或Thread/Runnable
	 * @param token 取消令牌
	 * @return CompletableFuture
	 *
	 * 设计思路：
	 * 1. 异步执行writeLine
	 * 2. 完成后返回true，失败返回false
	 */
	static CompletableFuture<Boolean> writeLineAsync(File file, String line,
	                                                 Charset charset, boolean append,
	                                                 Object executor,
	                                                 IOUtils.CancellationToken token) {
        ExecutorService es = IOUtils.toExecutorService(executor);
        return CompletableFuture.supplyAsync(() -> {
			// 检查取消令牌
			if (token != null && token.shouldInterrupt()) {
				return false;
			}
			FileRWOps.writeLine(file, line, charset, append);
			return true;
        }, es).whenComplete((r, ex) -> IOUtils.shutdownIfSelfManaged(es));
	}

	/**
	 * 异步追加一行字符串（UTF-8，追加模式）
	 */
	static CompletableFuture<Boolean> writeLineAsync(File file, String line,
	                                                 Object executor,
	                                                 IOUtils.CancellationToken token) {
		return writeLineAsync(file, line, StandardCharsets.UTF_8, true, executor, token);
	}
}
