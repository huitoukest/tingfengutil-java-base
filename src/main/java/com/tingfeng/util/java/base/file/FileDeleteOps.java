package com.tingfeng.util.java.base.file;

import java.io.File;

import com.tingfeng.util.java.base.lang.exception.BaseException;
import com.tingfeng.util.java.base.lang.exception.IOException;

/**
 * 文件/目录删除操作实现。
 * <p>包级私有，不对外暴露。通过 {@link FileUtils} 对外提供统一 API。</p>
 */
class FileDeleteOps {

	// ==================== 文件删除 ====================

	/**
	 * 直接删除一个文件/文件夹,成功返回true,失败返回false
	 * 
	 * @param path 文件路径
	 * @return 删除成功 = true or false
	 */
	static boolean deleteFile(String path) {
		File file = new File(path);
		return deleteFile(file);
	}

	/**
	 * 删除文件,带有重试次数和时间参数
	 * 文件删除至少执行一次
	 * @param path 文件路径
	 * @param tryCount 大于0
	 * @param intervalMillsSecond 单位毫秒
	 */
	static void deleteFile(String path, int tryCount, int intervalMillsSecond) {
		do {
			File file = new File(path);
			if (!file.exists()) {
				break;
			}
			if (!file.canWrite()) {
				try {
					Thread.sleep(intervalMillsSecond);
				} catch (InterruptedException e) {
					throw new BaseException(e);
				}
			} else {
				if (file.delete()) {
					break;
				}
			}
		} while (tryCount-- >= 0);
	}

	/**
	 * 删除文件
	 * @param file
	 * @return 删除成功 = true or false
	 */
	static boolean deleteFile(File file) {
		if (file != null && file.exists()) {
			file.delete();
			return true;
		}
		return false;
	}

	// ==================== 文件夹删除 ====================

	/**
	 * 删除文件夹中内容,此文件夹本身;
	 * @param file 文件夹
	 * @param isDeleteChild
	 *            文件夹中存在内容的时候,是否删除子文件/文件夹
	 * @param isDeleteSelf
	 *            是否删除自身
	 * @return 删除成功 = true  or false
	 */
	static boolean deleteFolder(File file, boolean isDeleteChild, boolean isDeleteSelf) {
		if (file == null || !file.exists()) {
			return false;
		}
		// 列出当前目录中所有子目录
		File[] childs = file.listFiles();
		if (!isDeleteChild && childs.length > 0) {
			throw new IOException("Folder is not empty!");
		}
		if (childs != null) {
			for (int i = 0; i < childs.length; i++) {
				if (childs[i].isDirectory()) {
					if (!deleteFolder(childs[i], isDeleteChild, true))
						return false;
				} else {
					if (!deleteFile(childs[i]))
						return false;
				}
			}
		}
		if (isDeleteSelf) {
			file.delete();
		}
		return true;
	}

	/**
	 * 删除文件夹
	 * @param path 路径
	 * @param isDeleteChild 是否删除子文件/子文件夹
	 * @param isDeleteSelf 是否删除自身
	 * @return 删除成功 = true or false
	 */
	static boolean deleteFolder(String path, boolean isDeleteChild, boolean isDeleteSelf) {
		return deleteFolder(new File(path), isDeleteChild, isDeleteSelf);
	}
}
