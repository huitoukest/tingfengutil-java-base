package com.tingfeng.util.java.base.file;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 目录遍历操作实现。
 * <p>包级私有，不对外暴露。通过 {@link FileUtils} 对外提供统一 API。</p>
 *
 * <p>核心能力：</p>
 * <ul>
 *   <li>基于 {@link WalkOption} 配置的目录递归遍历</li>
 *   <li>使用迭代 DFS（非递归），防止栈溢出</li>
 *   <li>符号链接循环检测（visited Set + canonical path）</li>
 *   <li>支持深度限制、包含/排除过滤器</li>
 * </ul>
 */
class FileWalkOps {

	/**
	 * 列出目录下所有直接子项（非递归）。
	 * <p>等效于 {@code dir.listFiles()}，但返回 List 而非数组。</p>
	 *
	 * @param dir 目录，非 null
	 * @return 子项列表，不会为 null（空目录或不存在时返回空列表）
	 * @throws IllegalArgumentException dir 为 null 时抛出
	 */
	static List<File> listFiles(File dir) {
		if (dir == null) {
			throw new IllegalArgumentException("dir must not be null");
		}
		if (!dir.exists() || !dir.isDirectory()) {
			return Collections.emptyList();
		}
		File[] children = dir.listFiles();
		if (children == null) {
			return Collections.emptyList();
		}
		List<File> result = new ArrayList<>(children.length);
		Collections.addAll(result, children);
		return result;
	}

	/**
	 * 列出目录下所有直接子项（非递归），使用过滤器筛选。
	 *
	 * @param dir    目录，非 null
	 * @param filter 文件过滤器，null 时返回全部子项
	 * @return 子项列表，不会为 null
	 * @throws IllegalArgumentException dir 为 null 时抛出
	 */
	static List<File> listFiles(File dir, FileFilter filter) {
		if (dir == null) {
			throw new IllegalArgumentException("dir must not be null");
		}
		if (filter == null) {
			return listFiles(dir);
		}
		if (!dir.exists() || !dir.isDirectory()) {
			return Collections.emptyList();
		}
		File[] children = dir.listFiles(filter);
		if (children == null) {
			return Collections.emptyList();
		}
		List<File> result = new ArrayList<>(children.length);
		Collections.addAll(result, children);
		return result;
	}

	private static final class WalkEntry {
		final File dir;
		final int depth;

		WalkEntry(File dir, int depth) {
			this.dir = dir;
			this.depth = depth;
		}
	}

	/**
	 * 遍历目录，返回匹配的文件/目录列表。
	 * <p>
	 * 使用迭代 DFS 算法，防止递归导致的栈溢出。
	 * 遍历结果顺序与 {@link File#listFiles()} 返回顺序相关。
	 * </p>
	 *
	 * <p>边界处理：</p>
	 * <ul>
	 *   <li>root 为 null → 抛出 {@link IllegalArgumentException}</li>
	 *   <li>root 不存在 → 返回空列表</li>
	 *   <li>root 是文件 → 若 {@code includeFiles=true} 返回单元素列表，否则空列表</li>
	 *   <li>空目录 → 返回空列表（若 {@code includeDirs=false}）或包含自身（若 {@code includeDirs=true}）</li>
	 *   <li>权限不足 → 跳过对应目录</li>
	 *   <li>符号链接 → visited Set 跟踪 canonical path，防止循环</li>
	 * </ul>
	 *
	 * @param root   遍历起始目录或文件，非 null
	 * @param option 遍历配置选项，非 null
	 * @return 匹配的文件/目录列表，不会为 null
	 * @throws IllegalArgumentException root 或 option 为 null 时抛出
	 */
	static List<File> walkFiles(File root, WalkOption option) {
		if (root == null) {
			throw new IllegalArgumentException("root must not be null");
		}
		if (option == null) {
			throw new IllegalArgumentException("option must not be null");
		}

		List<File> result = new ArrayList<>();

		if (!root.exists()) {
			return result;
		}

		// root 是文件：根据 includeFiles 决定是否返回
		if (root.isFile()) {
			if (option.isIncludeFiles()) {
				result.add(root);
			}
			return result;
		}

		// root 是目录：初始化栈与 visited Set
		Deque<WalkEntry> stack = new ArrayDeque<>();
		Set<String> visited = new HashSet<>();

		// 记录 root 的 canonical path 用于循环检测
		addVisited(root, visited);

		// 如果 includeDirs=true，将 root 自身加入结果
		if (option.isIncludeDirs()) {
			result.add(root);
		}

		stack.push(new WalkEntry(root, 0));

		while (!stack.isEmpty()) {
			WalkEntry entry = stack.pop();
			File dir = entry.dir;
			int depth = entry.depth;

			// 达到最大深度时不再深入，但当前目录如果是通过 includeDirs 已加入则已在结果中
			if (option.getMaxDepth() > 0 && depth >= option.getMaxDepth()) {
				continue;
			}

			File[] children = dir.listFiles();
			if (children == null) {
				// 权限不足或 IO 错误，跳过
				continue;
			}

			for (File child : children) {
				// 应用排除过滤器
				if (option.getExclude() != null && option.getExclude().accept(child)) {
					continue;
				}

				// 应用包含过滤器
				if (option.getFilter() != null && !option.getFilter().accept(child)) {
					continue;
				}

				if (child.isFile()) {
					if (option.isIncludeFiles()) {
						// exceptZeroFile：排除零长度文件
						if (option.isExceptZeroFile() && child.length() == 0) {
							continue;
						}
						result.add(child);
					}
				} else if (child.isDirectory()) {
					// 符号链接处理
					boolean isLink = isSymlink(child);
					if (isLink) {
						if (!option.isFollowLinks()) {
							// 不追踪符号链接：加入结果但不遍历
							if (option.isIncludeDirs()) {
								result.add(child);
							}
							continue;
						}
						// 追踪符号链接：循环检测
						String canonical = canonicalPath(child);
						if (canonical != null && !visited.add(canonical)) {
							// 已访问过，跳过遍历但仍可加入结果
							if (option.isIncludeDirs()) {
								result.add(child);
							}
							continue;
						}
					} else {
						addVisited(child, visited);
					}

					if (option.isIncludeDirs()) {
						result.add(child);
					}
					stack.push(new WalkEntry(child, depth + 1));
				}
				// 其他类型（特殊文件等）忽略
			}
		}

		return result;
	}

	/**
	 * 将文件的 canonical path 加入 visited Set。
	 *
	 * @param file    文件
	 * @param visited 已访问路径集合
	 */
	private static void addVisited(File file, Set<String> visited) {
		String path = canonicalPath(file);
		if (path != null) {
			visited.add(path);
		}
	}

	/**
	 * 获取文件的 canonical path，失败时返回绝对路径。
	 *
	 * @param file 文件
	 * @return canonical path，或绝对路径
	 */
	private static String canonicalPath(File file) {
		try {
			return file.getCanonicalPath();
		} catch (Exception e) {
			return file.getAbsolutePath();
		}
	}

	/**
	 * 判断文件是否为符号链接。
	 *
	 * @param file 文件
	 * @return true 表示为符号链接
	 */
	private static boolean isSymlink(File file) {
		return Files.isSymbolicLink(file.toPath());
	}
}
