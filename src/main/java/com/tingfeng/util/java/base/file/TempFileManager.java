package com.tingfeng.util.java.base.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.tingfeng.util.java.base.lang.exception.BaseException;

/**
 * 临时文件/目录管理器。
 * <p>
 * 实现了 {@link AutoCloseable}，支持 try-with-resources 自动清理。
 * 通过实例方法创建临时资源，在 {@link #close()} 时清理所有已注册的临时资源。
 * 即使清理过程中部分文件删除失败，也不会阻断其他文件的清理。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 实例模式（推荐）
 * try (TempFileManager manager = new TempFileManager()) {
 *     File tmp = manager.createFile("app", ".tmp");
 *     File dir = manager.createDirectory("data");
 *     // 使用 tmp 和 dir ...
 * }
 *
 * // 静态快捷模式
 * File tmp = TempFileManager.createTempFile("app", ".tmp");
 * File dir = TempFileManager.createTempDirectory("data");
 * }</pre>
 *
 * <p>线程安全：此类非线程安全，调用方需自行同步。</p>
 *
 * @see FileUtils
 */
public final class TempFileManager implements AutoCloseable {

	private final List<File> registeredFiles = new ArrayList<>();
	private boolean closed;

	/**
	 * 创建空的临时文件管理器。
	 * <p>通过 {@link #createFile(String, String)} 和 {@link #createDirectory(String)} 添加资源。</p>
	 */
	public TempFileManager() {
	}

	// ==================== 实例方法 ====================

	/**
	 * 创建并注册一个临时文件。
	 *
	 * @param prefix 文件前缀，null 时使用 "temp"
	 * @param suffix 文件后缀，null 时使用 ".tmp"
	 * @return 创建的临时文件
	 * @throws BaseException 创建失败时抛出
	 */
	public File createFile(String prefix, String suffix) {
		File file = createTempFile0(prefix, suffix, null);
		register(file);
		return file;
	}

	/**
	 * 创建并注册一个临时文件，指定 deleteOnExit 兜底。
	 * <p>仅当 {@code deleteOnExit} 为 true 且创建的是文件（非目录）时，注册 JVM 退出时清理。</p>
	 *
	 * @param prefix      文件前缀，null 时使用 "temp"
	 * @param suffix      文件后缀，null 时使用 ".tmp"
	 * @param deleteOnExit 是否注册 JVM 退出时自动删除
	 * @return 创建的临时文件
	 * @throws BaseException 创建失败时抛出
	 */
	public File createFile(String prefix, String suffix, boolean deleteOnExit) {
		File file = createTempFile0(prefix, suffix, null);
		register(file);
		if (deleteOnExit) {
			file.deleteOnExit();
		}
		return file;
	}

	/**
	 * 创建并注册一个临时目录。
	 *
	 * @param prefix 目录前缀，null 时使用 "temp"
	 * @return 创建的临时目录
	 * @throws BaseException 创建失败时抛出
	 */
	public File createDirectory(String prefix) {
		File dir = createTempDir0(prefix);
		register(dir);
		return dir;
	}

	/**
	 * 获取管理的临时文件或目录（第一个注册的资源）。
	 * <p>
	 * 主要用于兼容已注册单资源的场景。
	 * 多资源场景建议自行持有 {@link #createFile(String, String)} 返回的引用。
	 * </p>
	 *
	 * @return 临时文件或目录，若管理器未注册任何资源则返回 null
	 */
	public File getFile() {
		return registeredFiles.isEmpty() ? null : registeredFiles.get(0);
	}

	/**
	 * 返回当前管理的临时资源数量。
	 *
	 * @return 资源数量
	 */
	public int size() {
		return registeredFiles.size();
	}

	/**
	 * 检查管理器是否已关闭。
	 *
	 * @return true 表示已关闭
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * 清理所有已注册的临时文件/目录。
	 * <p>
	 * 幂等性保证：首次调用执行完整清理，后续调用直接返回。
	 * 即使清理过程中部分文件删除失败，也不会阻断其他文件的清理。
	 * </p>
	 */
	@Override
	public void close() {
		if (closed) {
			return;
		}
		closed = true;
		cleanAll();
	}

	// ==================== 静态快捷方法 ====================

	/**
	 * 创建临时文件（快捷方法，不绑定管理器）。
	 * <p>等效于 {@code File.createTempFile(prefix, suffix)}。</p>
	 *
	 * @param prefix 文件前缀，null 时使用 "temp"
	 * @param suffix 文件后缀，null 时使用 ".tmp"
	 * @return 创建的临时文件
	 * @throws BaseException 创建失败时抛出
	 */
	public static File createTempFile(String prefix, String suffix) {
		return createTempFile0(prefix, suffix, null);
	}

	/**
	 * 创建临时目录（快捷方法，不绑定管理器）。
	 * <p>等效于 {@code Files.createTempDirectory(prefix).toFile()}。</p>
	 *
	 * @param prefix 目录前缀，null 时使用 "temp"
	 * @return 创建的临时目录
	 * @throws BaseException 创建失败时抛出
	 */
	public static File createTempDirectory(String prefix) {
		return createTempDir0(prefix);
	}

	// ==================== 私有方法 ====================

	/**
	 * 注册一个临时文件到管理器中。
	 * <p>对于文件（非目录）自动调用 {@link File#deleteOnExit()} 作为兜底清理机制。
	 * 目录的清理由 {@link #close()} 中的递归删除保证。</p>
	 *
	 * @param file 临时文件或目录
	 */
	private void register(File file) {
		registeredFiles.add(file);
		// deleteOnExit 仅对文件有效（目录不会递归删除子项）
		if (file.isFile()) {
			file.deleteOnExit();
		}
	}

	/**
	 * 清理所有已注册的文件和目录。
	 */
	private void cleanAll() {
		for (File file : registeredFiles) {
			cleanOne(file);
		}
		registeredFiles.clear();
	}

	/**
	 * 清理单个文件或目录。
	 * <p>
	 * 文件直接删除；目录递归删除全部子项。
	 * 如果文件已被外部删除则忽略；删除失败时捕获异常继续清理其余文件。
	 * </p>
	 *
	 * @param file 要清理的文件或目录
	 */
	private static void cleanOne(File file) {
		if (!file.exists()) {
			return;
		}
		try {
			if (file.isDirectory()) {
				deleteDirectory(file);
			}
			file.delete();
		} catch (Exception e) {
			// 不阻断其他文件的清理
		}
	}

	/**
	 * 递归删除目录及其全部子项。
	 *
	 * @param dir 目录
	 */
	private static void deleteDirectory(File dir) {
		File[] children = dir.listFiles();
		if (children != null) {
			for (File child : children) {
				if (child.isDirectory()) {
					deleteDirectory(child);
				}
				child.delete();
			}
		}
	}

	/**
	 * 创建临时文件。
	 *
	 * @param prefix    前缀
	 * @param suffix    后缀
	 * @param directory 目录，null 时使用系统临时目录
	 * @return 创建的临时文件
	 * @throws BaseException 创建失败时抛出
	 */
	private static File createTempFile0(String prefix, String suffix, File directory) {
		String p = (prefix != null) ? prefix : "temp";
		String s = (suffix != null) ? suffix : ".tmp";
		try {
			if (directory != null) {
				return File.createTempFile(p, s, directory);
			} else {
				return File.createTempFile(p, s);
			}
		} catch (IOException e) {
			throw new BaseException("Failed to create temp file: " + e.getMessage(), e);
		}
	}

	/**
	 * 创建临时目录。
	 *
	 * @param prefix 目录前缀
	 * @return 创建的临时目录
	 * @throws BaseException 创建失败时抛出
	 */
	private static File createTempDir0(String prefix) {
		String p = (prefix != null) ? prefix : "temp";
		try {
			return Files.createTempDirectory(p).toFile();
		} catch (IOException e) {
			throw new BaseException("Failed to create temp directory: " + e.getMessage(), e);
		}
	}
}
