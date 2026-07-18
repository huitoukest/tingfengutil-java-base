package com.tingfeng.util.java.base.file;

import java.io.FileFilter;

/**
 * 目录遍历配置选项。
 * <p>
 * 使用 Builder 模式创建实例：
 * <pre>{@code
 * WalkOption opt = WalkOption.builder()
 *     .maxDepth(50)
 *     .includeFiles(true)
 *     .includeDirs(false)
 *     .filter(f -> f.getName().endsWith(".txt"))
 *     .exclude(f -> f.getName().startsWith("."))
 *     .build();
 * }</pre>
 * </p>
 *
 * <p>默认值：</p>
 * <ul>
 *   <li>{@code maxDepth = 50}（≤0 表示无限深度）</li>
 *   <li>{@code includeFiles = true}</li>
 *   <li>{@code includeDirs = false}</li>
 *   <li>{@code followLinks = false}</li>
 *   <li>{@code exceptZeroFile = false}</li>
 *   <li>{@code filter = null}（不过滤）</li>
 *   <li>{@code exclude = null}（不排除）</li>
 * </ul>
 *
 * @see FileWalkOps
 */
public final class WalkOption {

	private final int maxDepth;
	private final boolean includeFiles;
	private final boolean includeDirs;
	private final boolean followLinks;
	private final boolean exceptZeroFile;
	private final FileFilter filter;
	private final FileFilter exclude;

	private WalkOption(Builder builder) {
		this.maxDepth = builder.maxDepth;
		this.includeFiles = builder.includeFiles;
		this.includeDirs = builder.includeDirs;
		this.followLinks = builder.followLinks;
		this.exceptZeroFile = builder.exceptZeroFile;
		this.filter = builder.filter;
		this.exclude = builder.exclude;
	}

	/**
	 * 创建新的 Builder 实例。
	 *
	 * @return Builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * 返回最大遍历深度。
	 * <p>≤0 表示无限深度。默认 50。</p>
	 *
	 * @return 最大深度
	 */
	public int getMaxDepth() {
		return maxDepth;
	}

	/**
	 * 是否包含文件。
	 *
	 * @return true 表示结果中包含文件
	 */
	public boolean isIncludeFiles() {
		return includeFiles;
	}

	/**
	 * 是否包含目录。
	 *
	 * @return true 表示结果中包含目录
	 */
	public boolean isIncludeDirs() {
		return includeDirs;
	}

	/**
	 * 获取添加类型（文件/目录/全部）。
	 * <p>根据 {@link #isIncludeFiles()} 和 {@link #isIncludeDirs()} 推导。</p>
	 *
	 * @return 添加类型，不会为 null
	 */
	public FileUtils.FileAddType getAddType() {
		if (includeFiles && includeDirs) {
			return FileUtils.FileAddType.FileAndFolder;
		} else if (includeDirs) {
			return FileUtils.FileAddType.Folder;
		} else {
			return FileUtils.FileAddType.File;
		}
	}

	/**
	 * 是否追踪符号链接。
	 * <p>false（默认）表示遇到符号链接时跳过遍历；true 表示跟随符号链接（带循环检测）。</p>
	 *
	 * @return true 表示追踪符号链接
	 */
	public boolean isFollowLinks() {
		return followLinks;
	}

	/**
	 * 是否排除大小为零的文件。
	 *
	 * @return true 表示排除零长度文件
	 */
	public boolean isExceptZeroFile() {
		return exceptZeroFile;
	}

	/**
	 * 返回包含过滤器。
	 *
	 * @return 包含过滤器，可能为 null
	 */
	public FileFilter getFilter() {
		return filter;
	}

	/**
	 * 返回排除过滤器。
	 *
	 * @return 排除过滤器，可能为 null
	 */
	public FileFilter getExclude() {
		return exclude;
	}

	/**
	 * WalkOption 构建器。
	 */
	public static final class Builder {
		private int maxDepth = 50;
		private boolean includeFiles = true;
		private boolean includeDirs = false;
		private boolean followLinks = false;
		private boolean exceptZeroFile = false;
		private FileFilter filter;
		private FileFilter exclude;

		private Builder() {
		}

		/**
		 * 设置最大遍历深度。
		 *
		 * @param maxDepth 最大深度，≤0 表示无限
		 * @return this
		 */
		public Builder maxDepth(int maxDepth) {
			this.maxDepth = maxDepth;
			return this;
		}

		/**
		 * 设置是否包含文件。
		 *
		 * @param includeFiles true 表示结果中包含文件
		 * @return this
		 */
		public Builder includeFiles(boolean includeFiles) {
			this.includeFiles = includeFiles;
			return this;
		}

		/**
		 * 设置是否包含目录。
		 *
		 * @param includeDirs true 表示结果中包含目录
		 * @return this
		 */
		public Builder includeDirs(boolean includeDirs) {
			this.includeDirs = includeDirs;
			return this;
		}

		/**
		 * 设置添加类型。
		 * <p>设置此值会同步更新 {@link #includeFiles(boolean)} 和 {@link #includeDirs(boolean)}：</p>
		 * <ul>
		 *   <li>{@link FileUtils.FileAddType#File} → includeFiles=true, includeDirs=false</li>
		 *   <li>{@link FileUtils.FileAddType#Folder} → includeFiles=false, includeDirs=true</li>
		 *   <li>{@link FileUtils.FileAddType#FileAndFolder} → includeFiles=true, includeDirs=true</li>
		 * </ul>
		 *
		 * @param addType 添加类型，非 null
		 * @return this
		 */
		public Builder addType(FileUtils.FileAddType addType) {
			if (addType == null) {
				throw new IllegalArgumentException("addType must not be null");
			}
			switch (addType) {
				case File:
					this.includeFiles = true;
					this.includeDirs = false;
					break;
				case Folder:
					this.includeFiles = false;
					this.includeDirs = true;
					break;
				case FileAndFolder:
					this.includeFiles = true;
					this.includeDirs = true;
					break;
			}
			return this;
		}

		/**
		 * 设置是否追踪符号链接。
		 *
		 * @param followLinks true 表示跟随符号链接（带循环检测）
		 * @return this
		 */
		public Builder followLinks(boolean followLinks) {
			this.followLinks = followLinks;
			return this;
		}

		/**
		 * 设置是否排除大小为零的文件。
		 *
		 * @param exceptZeroFile true 表示排除零长度文件
		 * @return this
		 */
		public Builder exceptZeroFile(boolean exceptZeroFile) {
			this.exceptZeroFile = exceptZeroFile;
			return this;
		}

		/**
		 * 设置包含过滤器。
		 * <p>只有通过此过滤器的文件/目录才会被包含在结果中。</p>
		 *
		 * @param filter 包含过滤器，null 表示不过滤
		 * @return this
		 */
		public Builder filter(FileFilter filter) {
			this.filter = filter;
			return this;
		}

		/**
		 * 设置排除过滤器。
		 * <p>通过此过滤器的文件/目录将被排除在结果之外。</p>
		 *
		 * @param exclude 排除过滤器，null 表示不排除
		 * @return this
		 */
		public Builder exclude(FileFilter exclude) {
			this.exclude = exclude;
			return this;
		}

		/**
		 * 构建 WalkOption 实例。
		 *
		 * @return WalkOption 实例
		 */
		public WalkOption build() {
			return new WalkOption(this);
		}
	}
}
