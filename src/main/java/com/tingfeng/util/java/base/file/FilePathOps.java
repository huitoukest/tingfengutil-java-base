package com.tingfeng.util.java.base.file;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 路径归一化操作实现。
 * <p>包级私有，不对外暴露。通过 {@link FileUtils} 对外提供统一 API。</p>
 *
 * 核心能力：
 * - 统一分隔符为 '/'（跨平台，含 UNC 路径保护）
 * - 解析并去除 '.'（当前目录）和 '..'（上级目录）冗余片段
 * - 根目录保护（超出根的 '..' 被忽略）
 * - Windows 盘符（C:）正确处理
 * - 支持尾部斜杠保留控制
 */
class FilePathOps {

	/**
	 * 归一化路径字符串。
	 * <p>
	 * 统一分隔符为 '/'，解析并去除 '.' 和 '..' 冗余片段。
	 * 默认不保留尾部分隔符。
	 * </p>
	 *
	 * @param path 原始路径，非 null
	 * @return 归一化后的路径
	 * @throws IllegalArgumentException path 为 null 时抛出
	 */
	static String normalize(String path) {
		return normalize(path, false);
	}

	/**
	 * 归一化路径字符串，支持控制是否保留尾部分隔符。
	 * <p>
	 * 处理流程：
	 * <ol>
	 *   <li>null 校验 → 抛 IllegalArgumentException</li>
	 *   <li>空串 → 返回空串 ""</li>
	 *   <li>统一分隔符：'\\' → '/'，UNC 路径（\\server\share）前缀受保护</li>
	 *   <li>按 '/' 切分为 tokens</li>
	 *   <li>遍历 tokens：'.' 忽略；'..' 回溯一级（根目录下不回溯）；其余保留</li>
	 *   <li>按 '/' 重新拼接，保留尾部斜杠（由参数控制）</li>
	 * </ol>
	 * </p>
	 *
	 * <p>边界场景：</p>
	 * <ul>
	 *   <li>null → IllegalArgumentException</li>
	 *   <li>空串 "" → 返回空串</li>
	 *   <li>纯 "." → 返回 "."</li>
	 *   <li>纯 "/" → 返回 "/"</li>
	 *   <li>UNC 路径 "\\server\share\file" → "//server/share/file"</li>
	 *   <li>Linux 绝对路径 "/a/../b" → "/b"</li>
	 *   <li>Windows 绝对路径 "C:\a\..\b" → "C:/b"</li>
	 *   <li>超出根的 ".."（"/a/../../b"）→ "/b"（根目录不回溯）</li>
	 * </ul>
	 *
	 * @param path                  原始路径，非 null
	 * @param keepTrailingSeparator 是否保留尾部分隔符
	 * @return 归一化后的路径
	 * @throws IllegalArgumentException path 为 null 时抛出
	 */
	static String normalize(String path, boolean keepTrailingSeparator) {
		if (path == null) {
			throw new IllegalArgumentException("Path must not be null");
		}
		if (path.isEmpty()) {
			return "";
		}

		boolean hasTrailingSep = path.charAt(path.length() - 1) == '/'
				|| path.charAt(path.length() - 1) == '\\';

		// 统一分隔符：保留 UNC 前缀 "\\"
		String normalized;
		boolean isUnc = path.startsWith("\\\\") && path.length() > 2 && path.charAt(2) != '\\';
		if (isUnc) {
			normalized = "\\\\" + path.substring(2).replace('\\', '/');
		} else {
			normalized = path.replace('\\', '/');
		}

		// 按 '/' 切分为 tokens
		String[] tokens = normalized.split("/", -1);

		// 检测路径前缀类型
		String prefix = null;
		int startIdx = 0;

		if (tokens.length > 0 && tokens[0].isEmpty()) {
			// 潜在 UNC 或 Unix 绝对路径
			if (tokens.length > 2 && tokens[1].isEmpty() && !tokens[2].isEmpty()) {
				// UNC: ["", "", "server", "share", ...]
				prefix = "//";
				startIdx = 2;
			} else {
				// Unix 绝对: ["", "a", "b", ...]
				prefix = "/";
				startIdx = 1;
			}
		} else if (tokens.length > 0 && tokens[0].length() == 2
				&& tokens[0].charAt(1) == ':') {
			// Windows 盘符: ["C:", "a", "b", ...]
			prefix = tokens[0].substring(0, 1).toUpperCase() + ":";
			startIdx = 1;
		}

		// 遍历处理 tokens
		Deque<String> result = new ArrayDeque<>();

		for (int i = startIdx; i < tokens.length; i++) {
			String token = tokens[i];
			if (token.isEmpty() || token.equals(".")) {
				continue;
			} else if (token.equals("..")) {
				if (prefix != null) {
					// 绝对路径：不能超出根目录
					if (!result.isEmpty()) {
						result.pollLast();
					}
					// 根目录下直接丢弃 ".."
				} else {
					// 相对路径
					if (!result.isEmpty() && !"..".equals(result.peekLast())) {
						result.pollLast();
					} else {
						result.addLast(token);
					}
				}
			} else {
				result.addLast(token);
			}
		}

		// 处理空结果
		if (result.isEmpty()) {
			if (prefix != null) {
				if ("/".equals(prefix) || "//".equals(prefix)) {
					return prefix;
				}
				// Windows 盘符：检查原始路径中盘符后是否有分隔符
				int prefixLen = prefix.length(); // 2 for "C:"
				if (normalized.length() > prefixLen && normalized.charAt(prefixLen) == '/') {
					return prefix + "/";
				}
				return prefix;
			}
			// 相对路径解析为空
			if (normalized.equals(".")) {
				return ".";
			}
			if (normalized.equals("..")) {
				return "..";
			}
			return "";
		}

		// 重新拼接路径
		StringBuilder sb = new StringBuilder();
		if (prefix != null) {
			sb.append(prefix);
			if (prefix.length() == 2 && prefix.charAt(1) == ':') {
				sb.append('/');
			}
		}

		boolean first = true;
		for (String s : result) {
			if (first) {
				first = false;
			} else {
				sb.append('/');
			}
			sb.append(s);
		}

		// 保留尾部分隔符
		if (keepTrailingSeparator && hasTrailingSep
				&& sb.charAt(sb.length() - 1) != '/') {
			sb.append('/');
		}

		return sb.toString();
	}

	/**
	 * 判断路径是否为绝对路径（跨平台兼容）。
	 * <p>
	 * 绝对路径判定规则：
	 * <ul>
	 *   <li>Unix/Linux：以 '/' 开头</li>
	 *   <li>Windows：以盘符（如 "C:"）开头，或以 '\\' UNC 前缀开头</li>
	 * </ul>
	 * </p>
	 *
	 * @param path 路径字符串，非 null
	 * @return 绝对路径返回 true；null 或空串返回 false
	 * @throws IllegalArgumentException path 为 null 时抛出
	 */
	static boolean isAbsolute(String path) {
		if (path == null) {
			throw new IllegalArgumentException("Path must not be null");
		}
		if (path.isEmpty()) {
			return false;
		}
		String normalized = path.replace('\\', '/');
		// Unix 绝对：以 '/' 开头
		if (normalized.startsWith("/")) {
			return true;
		}
		// Windows 盘符：字母 + ':'
		if (normalized.length() >= 2
				&& Character.isLetter(normalized.charAt(0))
				&& normalized.charAt(1) == ':') {
			return true;
		}
		return false;
	}

	/**
	 * 将路径中的分隔符统一转换为 Unix 风格（'/'）。
	 *
	 * @param path 路径字符串
	 * @return 转换后的路径，null 时返回 null
	 */
	static String separatorsToUnix(String path) {
		if (path == null) {
			return null;
		}
		return path.replace('\\', '/');
	}

	/**
	 * 将路径中的分隔符统一转换为 Windows 风格（'\\'）。
	 *
	 * @param path 路径字符串
	 * @return 转换后的路径，null 时返回 null
	 */
	static String separatorsToWindows(String path) {
		if (path == null) {
			return null;
		}
		return path.replace('/', '\\');
	}
}
