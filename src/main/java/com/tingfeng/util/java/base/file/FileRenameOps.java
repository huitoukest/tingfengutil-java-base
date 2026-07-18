package com.tingfeng.util.java.base.file;

import java.io.File;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.*;

import com.tingfeng.util.java.base.lang.exception.FileNotFoundException;
import com.tingfeng.util.java.base.lang.exception.IOException;

/**
 * 文件重命名操作实现。
 * <p>包级私有，不对外暴露。通过 {@link FileUtils} 对外提供统一 API。</p>
 */
class FileRenameOps {

	/**
	 * 单文件重命名。
	 *
	 * @param source  源文件，不能为 null
	 * @param target  目标文件，不能为 null
	 * @param options 重命名策略选项（默认 {@link FileUtils.RenameOption#FAIL_IF_EXISTS}）
	 * @return 重命名成功返回 true
	 * @throws IllegalArgumentException                                   参数为 null 时抛出
	 * @throws FileNotFoundException 源文件不存在时抛出
	 * @throws IOException           文件系统操作失败时抛出
	 */
	static boolean rename(File source, File target, FileUtils.RenameOption... options) {
		// 参数校验
		if (source == null) {
			throw new IllegalArgumentException("source must not be null");
		}
		if (target == null) {
			throw new IllegalArgumentException("target must not be null");
		}
		if (!source.exists()) {
			throw new FileNotFoundException(
				"source not found: " + source.getAbsolutePath());
		}

		// 源 == 目标（同名同路径）→ 短路返回
		if (sameFile(source, target)) {
			return true;
		}

		// 解析策略
		FileUtils.RenameOption option = resolveOption(options);

		// 目标文件已存在处理
		if (target.exists()) {
			if (option == FileUtils.RenameOption.FAIL_IF_EXISTS) {
				throw new IOException(
					"target already exists: " + target.getAbsolutePath());
			}
			// OVERWRITE / ATOMIC_MOVE：继续执行（通过 REPLACE_EXISTING 覆盖）
		} else {
			// 确保目标父目录存在
			File parent = target.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
		}

		try {
			java.nio.file.Path sourcePath = source.toPath();
			java.nio.file.Path targetPath = target.toPath();

			if (option == FileUtils.RenameOption.ATOMIC_MOVE) {
				try {
					Files.move(sourcePath, targetPath,
						StandardCopyOption.ATOMIC_MOVE,
						StandardCopyOption.REPLACE_EXISTING);
				} catch (AtomicMoveNotSupportedException e) {
					// 跨文件系统降级为 copy + delete
					Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
					Files.delete(sourcePath);
				}
			} else if (option == FileUtils.RenameOption.OVERWRITE) {
				Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
			} else {
				// FAIL_IF_EXISTS：前置检查已处理目标存在情况，此处安全
				Files.move(sourcePath, targetPath);
			}
			return true;
		} catch (java.io.IOException e) {
			throw new IOException(
				"rename failed: " + source.getAbsolutePath() + " -> " + target.getAbsolutePath(), e);
		}
	}

	/**
	 * 解析重命名策略选项。
	 * <p>未指定或为空时默认返回 {@link FileUtils.RenameOption#FAIL_IF_EXISTS}。</p>
	 */
	private static FileUtils.RenameOption resolveOption(FileUtils.RenameOption... options) {
		if (options != null && options.length > 0 && options[0] != null) {
			return options[0];
		}
		return FileUtils.RenameOption.FAIL_IF_EXISTS;
	}

	/**
	 * 判断源文件和目标文件是否为同一文件（基于规范路径比较）。
	 * <p>使用 {@link File#getCanonicalPath()} 比较，能识别等效路径（如 {@code ./foo} 与 {@code foo/bar/../foo}）。
	 * 获取规范路径失败时回退到绝对路径比较。</p>
	 *
	 * @param source 源文件
	 * @param target 目标文件
	 * @return true 表示同一文件
	 */
	private static boolean sameFile(File source, File target) {
		try {
			return source.getCanonicalPath().equals(target.getCanonicalPath());
		} catch (java.io.IOException e) {
			return source.getAbsolutePath().equals(target.getAbsolutePath());
		}
	}

	/**
	 * 批量重命名文件。
	 * <p>默认开启回滚（失败时回滚已重命名的文件），使用 {@link FileUtils.RenameOption#FAIL_IF_EXISTS} 策略。</p>
	 *
	 * @param files  文件数组，不能为 null
	 * @param filter 过滤条件，保留满足条件的文件（null 时抛出异常）
	 * @param namer  命名函数，输入源文件返回新文件名（不含路径，null/空时抛出异常）
	 * @return 重命名结果列表，不会为 null
	 */
	static List<FileUtils.RenameResult> batchRename(File[] files,
			Predicate<File> filter, Function<File, String> namer) {
		return batchRenameInternal(files, filter, namer, FileUtils.RenameOption.FAIL_IF_EXISTS, true);
	}

	/**
	 * 批量重命名文件（使用指定策略，默认开启回滚）。
	 *
	 * @param files   文件数组，不能为 null
	 * @param filter  过滤条件
	 * @param namer   命名函数
	 * @param options 重命名策略选项（默认 FAIL_IF_EXISTS）
	 * @return 重命名结果列表
	 */
	static List<FileUtils.RenameResult> batchRename(File[] files,
			Predicate<File> filter, Function<File, String> namer,
			FileUtils.RenameOption... options) {
		return batchRenameInternal(files, filter, namer, resolveOption(options), true);
	}

	/**
	 * 批量重命名文件（控制是否回滚）。
	 *
	 * @param files             文件数组，不能为 null
	 * @param filter            过滤条件
	 * @param namer             命名函数
	 * @param rollbackOnFailure 失败时是否回滚已重命名的文件
	 * @return 重命名结果列表
	 */
	static List<FileUtils.RenameResult> batchRename(File[] files,
			Predicate<File> filter, Function<File, String> namer,
			boolean rollbackOnFailure) {
		return batchRenameInternal(files, filter, namer, FileUtils.RenameOption.FAIL_IF_EXISTS, rollbackOnFailure);
	}

	/**
	 * 批量重命名内部实现。
	 */
	private static List<FileUtils.RenameResult> batchRenameInternal(File[] files,
			Predicate<File> filter, Function<File, String> namer,
			FileUtils.RenameOption option, boolean rollbackOnFailure) {
		// 参数校验
		if (files == null) {
			throw new IllegalArgumentException("files must not be null");
		}
		if (filter == null) {
			throw new IllegalArgumentException("filter must not be null");
		}
		if (namer == null) {
			throw new IllegalArgumentException("namer must not be null");
		}

		// Step 1: 过滤 + 命名
		List<File> filteredList = new ArrayList<>();
		List<String> nameList = new ArrayList<>();
		for (File f : files) {
			if (f != null && filter.test(f)) {
				String newName = namer.apply(f);
				if (newName == null || newName.trim().isEmpty()) {
					throw new IllegalArgumentException(
						"namer returned null/empty name for: " + f.getAbsolutePath());
				}
				filteredList.add(f);
				nameList.add(newName);
			}
		}

		if (filteredList.isEmpty()) {
			return Collections.emptyList();
		}

		// Step 2: 构建目标路径 + 冲突检测（同名冲突 → 抛异常失败）
		List<File> targetList = new ArrayList<>();
		Set<String> seenPaths = new HashSet<>();
		for (int i = 0; i < filteredList.size(); i++) {
			File source = filteredList.get(i);
			String newName = nameList.get(i);
			File target = new File(source.getParentFile(), newName);
			String absPath = target.getAbsolutePath();

			if (seenPaths.contains(absPath)) {
				throw new IOException(
					"batch rename conflict: multiple files map to the same target: " + absPath);
			}
			seenPaths.add(absPath);
			targetList.add(target);
		}

		// Step 3: 逐一执行
		List<FileUtils.RenameResult> results = new ArrayList<>();
		List<Integer> completedIndices = new ArrayList<>();

		for (int i = 0; i < filteredList.size(); i++) {
			File source = filteredList.get(i);
			File target = targetList.get(i);

			try {
				rename(source, target, option);
				results.add(new FileUtils.RenameResult(source, target, true, null));
				completedIndices.add(i);
			} catch (Exception e) {
				results.add(new FileUtils.RenameResult(
					source, target, false, e.getMessage()));

				if (rollbackOnFailure) {
					rollbackCompleted(filteredList, targetList, completedIndices);
				}

				break; // 第一个失败后停止
			}
		}

		return results;
	}

	/**
	 * 回滚已成功重命名的文件（逆序恢复原名）。
	 */
	private static void rollbackCompleted(List<File> sourceList,
			List<File> targetList, List<Integer> completedIndices) {
		for (int j = completedIndices.size() - 1; j >= 0; j--) {
			int idx = completedIndices.get(j);
			File original = sourceList.get(idx);
			File renamed = targetList.get(idx);
			try {
				if (renamed.exists()) {
					Files.move(renamed.toPath(), original.toPath(),
						StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (java.io.IOException e) {
				// 回滚失败：不抛异常，保持已部分回滚状态
			}
		}
	}
}
