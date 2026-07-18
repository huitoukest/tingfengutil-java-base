package com.tingfeng.util.java.base.file;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.tingfeng.util.java.base.file.strategy.FileCopyStrategy;
import com.tingfeng.util.java.base.file.strategy.ProgressCallback;
import com.tingfeng.util.java.base.file.strategy.ChannelCopyStrategy;
import com.tingfeng.util.java.base.lang.exception.BaseException;
import com.tingfeng.util.java.base.lang.exception.StreamCloseException;
import com.tingfeng.util.java.base.lang.inter.PercentActionCallBackI;
import com.tingfeng.util.java.base.lang.inter.RateCallBackI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 文件拷贝操作实现。
 * <p>包级私有，不对外暴露。通过 {@link FileUtils} 对外提供统一 API。</p>
 */
class FileCopyOps {

	private static final Log logger = LogFactory.getLog(FileCopyOps.class);

	// ==================== Channel 拷贝 ====================

	/**
	 * 文件拷贝, 通过channel方式 , 高效
	 * @param srcPath 源路径
	 * @param destPath 目标路径
	 */
	static void copyFile(String srcPath, String destPath) {
		copyFileByFileChannel(new File(srcPath), new File(destPath), null);
	}

	/**
	 * 带进度的文件拷贝
	 *
	 * @param srcPath 源路径
	 * @param destPath 目标路径
	 * @param fileCopyActionCallBack 当fileCopyActionCallBack为null的时候,将不会更新进度;
	 */
	static void copyFile(String srcPath, String destPath, RateCallBackI fileCopyActionCallBack) {
		copyFileByFileChannel(new File(srcPath), new File(destPath), fileCopyActionCallBack);
	}

	/**
	 * 带进度的文件拷贝,同步的
	 *
	 * @param source 源路径
	 * @param target 目标路径
	 * @param fileCopyActionCallBack 当fileCopyActionCallBack为null的时候,将不会更新进度;
	 */
	static void copyFileByFileChannel(File source, File target, RateCallBackI fileCopyActionCallBack) {
		FileChannel in = null;
		FileChannel out = null;
		FileInputStream inStream = null;
		FileOutputStream outStream = null;
		try {
			inStream = new FileInputStream(source);
			outStream = new FileOutputStream(target);
			in = inStream.getChannel();
			out = outStream.getChannel();

			if (fileCopyActionCallBack == null) {
				in.transferTo(0, in.size(), out);// 连接两个通道，并且从in通道读取，然后写入out通道
			} else {
				int lengthPerTime = 0;// 循环读写中,每一次读取的字节
				int lengthReadSum = 0;// 循环读写中,读取的字节的总数量
				Long fileSize = in.size();// 得到文件的总长度
				int countOfUpdate = (int) (fileSize / FileUtils.BUFFER_SIZE / 100);
				if (countOfUpdate == 0) {
					countOfUpdate = 1;
				}
				int countOfNowCycle = 0;// 当前循环的次数
				ByteBuffer buffer = ByteBuffer.allocate(FileUtils.BUFFER_SIZE);
				while ((lengthPerTime = in.read(buffer)) != -1) {
					buffer.flip();
					out.write(buffer);
					buffer.clear();
					lengthReadSum += lengthPerTime;
					if (countOfNowCycle >= countOfUpdate) {
						countOfNowCycle = 0;
						fileCopyActionCallBack.updateRate(100.0 * lengthReadSum / fileSize);
					}
					countOfNowCycle++;
				}
			}
		} catch (Throwable e) {
			throw new BaseException(e);
		} finally {
			try {
				if (null != inStream) {
					inStream.close();
				}
				if (null != outStream) {
					outStream.flush();
					outStream.close();
				}
				if (null != in) {
					in.close();
				}
				if (null != out) {
					out.close();
				}
			} catch (Throwable e) {
				throw new StreamCloseException(e);
			}
		}
	}

	/**
	 * 带进度的文件拷贝
	 *
	 * @param source 源路径
	 * @param target 目标路径
	 * @param fileCopyActionCallBack 当fileCopyActionCallBack为null的时候,将不会更新进度;
	 */
	static void copyFileByFileChannel(String source, String target, RateCallBackI fileCopyActionCallBack) {
		copyFileByFileChannel(new File(source), new File(target), fileCopyActionCallBack);
	}

	// ==================== 策略模式拷贝 ====================

	/**
	 * 使用策略模式拷贝文件（无进度回调）
	 *
	 * @param srcPath 源路径
	 * @param destPath 目标路径
	 * @param strategy 文件拷贝策略（StreamCopyStrategy/ChannelCopyStrategy/AsyncCopyStrategy）
	 *
	 * 设计原则：
	 * 1. 策略接口统一抽象，支持多种拷贝实现
	 * 2. 默认使用 ChannelCopyStrategy 高效拷贝
	 */
	static void copyFile(String srcPath, String destPath, FileCopyStrategy strategy) {
		copyFile(new File(srcPath), new File(destPath), strategy);
	}

	/**
	 * 使用策略模式拷贝文件（无进度回调）
	 *
	 * @param src 源文件
	 * @param dest 目标文件
	 * @param strategy 文件拷贝策略（StreamCopyStrategy/ChannelCopyStrategy/AsyncCopyStrategy）
	 */
	static void copyFile(File src, File dest, FileCopyStrategy strategy) {
		if (strategy == null) {
			// 默认使用 ChannelCopyStrategy
			strategy = new ChannelCopyStrategy();
		}
		strategy.copyFile(src, dest);
	}

	/**
	 * 使用策略模式拷贝文件（带进度回调）
	 *
	 * @param srcPath 源路径
	 * @param destPath 目标路径
	 * @param strategy 文件拷贝策略
	 * @param callback 进度回调
	 */
	static void copyFile(String srcPath, String destPath, FileCopyStrategy strategy, ProgressCallback callback) {
		copyFile(new File(srcPath), new File(destPath), strategy, callback);
	}

	/**
	 * 使用策略模式拷贝文件（带进度回调）
	 *
	 * @param src 源文件
	 * @param dest 目标文件
	 * @param strategy 文件拷贝策略
	 * @param callback 进度回调
	 */
	static void copyFile(File src, File dest, FileCopyStrategy strategy, ProgressCallback callback) {
		if (strategy == null) {
			strategy = new ChannelCopyStrategy();
		}
		strategy.copyFile(src, dest, callback);
	}

	// ==================== 流式拷贝与目录拷贝 ====================

	/**
	 * 复制单个文件
	 *
	 * @param srcFileName
	 *            待复制的文件名
	 * @param destFileName
	 *            目标文件名
	 * @param overlay
	 *            如果目标文件存在，是否覆盖
	 * @return 如果复制成功返回true，否则返回false
	 */
	static boolean copyFileByStream(String srcFileName, String destFileName, boolean overlay) {
		File srcFile = new File(srcFileName);
		String msg = "";
		// 判断源文件是否存在
		if (!srcFile.exists()) {
			msg = "源文件：" + srcFileName + "不存在！";
			if (logger.isInfoEnabled()) {
				logger.info(msg);
			}
			return false;
		} else if (!srcFile.isFile()) {
			msg = "复制文件失败，源文件：" + srcFileName + "不是一个文件！";
			if (logger.isInfoEnabled()) {
				logger.info(msg);
			}
			return false;
		}
		// 判断目标文件是否存在
		File destFile = new File(destFileName);
		if (destFile.exists()) {
			// 如果目标文件存在并允许覆盖
			if (overlay) {
				// 删除已经存在的目标文件，无论目标文件是目录还是单个文件
				new File(destFileName).delete();
			}
		} else {
			// 如果目标文件所在目录不存在，则创建目录
			if (!destFile.getParentFile().exists()) {
				// 目标文件所在目录不存在
				if (!destFile.getParentFile().mkdirs()) {
					// 复制文件失败：创建目标文件所在目录失败
					return false;
				}
			}
		}
		// 复制文件 读取的字节数
		int byteRead = 0;
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(srcFile);
			out = new FileOutputStream(destFile);
			byte[] buffer = new byte[FileUtils.BUFFER_SIZE];

			while ((byteRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, byteRead);
			}
			System.gc();
			return true;
		} catch (Throwable e) {
			logger.error(e);
			return false;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (Throwable e) {
				logger.error(e);
			}
		}
	}

	/**
	 * 复制整个目录的内容,包括目录中包含的文件和子目录
	 *
	 * @param srcDirName
	 *            待复制目录的目录名
	 * @param destDirName
	 *            目标目录名
	 * @param overlay
	 *            如果目标目录存在，是否覆盖
	 * @return 如果复制成功返回true，否则返回false
	 */
	static boolean copyDirectory(String srcDirName, String destDirName, boolean overlay) {
		String msg = "";
		// 判断源目录是否存在
		File srcDir = new File(srcDirName);
		if (!srcDir.exists()) {
			msg = "复制目录失败：源目录" + srcDirName + "不存在！";
			logger.info(msg);
			return false;
		} else if (!srcDir.isDirectory()) {
			msg = "复制目录失败：" + srcDirName + "不是目录！";
			logger.info(msg);
			return false;
		}

		// 如果目标目录名不是以文件分隔符结尾，则加上文件分隔符
		if (!destDirName.endsWith(File.separator)) {
			destDirName = destDirName + File.separator;
		}
		File destDir = new File(destDirName);
		// 如果目标文件夹存在
		if (destDir.exists()) {
			// 如果允许覆盖则删除已存在的目标目录
			if (overlay) {
				new File(destDirName).delete();
			} else {
				msg = "复制目录失败：目的目录" + destDirName + "已存在！";
				if (logger.isInfoEnabled()) {
					logger.info(msg);
				}
				return false;
			}
		} else {
			// 创建目的目录
			if (!destDir.mkdirs()) {
				if (logger.isInfoEnabled()) {
					logger.info("复制目录失败：创建目的目录失败！");
				}
				return false;
			}
		}
		boolean flag = true;
		File[] files = srcDir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				// 复制文件
				if (files[i].isFile()) {
					flag = copyFileByStream(files[i].getAbsolutePath(), destDirName + files[i].getName(), overlay);
					if (!flag)
						break;
				} else if (files[i].isDirectory()) {
					flag = copyDirectory(files[i].getAbsolutePath(), destDirName + files[i].getName(), overlay);
					if (!flag)
						break;
				}
			}
		}
		if (!flag) {
			msg = "复制目录" + srcDirName + "至" + destDirName + "失败！";
			if (logger.isInfoEnabled()) {
				logger.info(msg);
			}
			return false;
		} else {
			return true;
		}
	}

	// ==================== 拷贝并重命名 / 输入流保存 ====================

	/**
	 * 拷贝并重命名文件
	 * @param file
	 * @param destPath
	 * @param destFileName
	 */
	static void copyFile(File file, String destPath, String destFileName) {
		try {
			copyFile(new FileInputStream(file), destPath, destFileName);
		} catch (FileNotFoundException e) {
			throw new com.tingfeng.util.java.base.lang.exception.FileNotFoundException(e);
		}
	}

	/**
	 * 保存文件。（保留原有文件名）
	 * @param inputStream 输入文件流
	 * @param destPath 保存路径
	 * @param destFileName 文件名
	 */
	static void copyFile(InputStream inputStream, String destPath, String destFileName) {
		int byteRead;
		FileOutputStream fs = null;
		try {
			fs = new FileOutputStream(destPath + destFileName);
			byte[] buffer = new byte[FileUtils.BUFFER_SIZE];
			while ((byteRead = inputStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteRead);
			}
			System.gc();
		} catch (IOException e) {
			throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
		} finally {
			try {
				if (fs != null) {
					fs.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				throw new StreamCloseException(e);
			}
		}
	}

	// ==================== 文件写入输出流（拷贝相关） ====================

	/**
	 * 用指定的写出文件流来写出文件;
	 * @param file 目标文件
	 * @param os 输出流
	 * @param callBack callBack 回调 PercentActionCallBackI ，在文件操作完成之后回调成功或者失败的操作,以及上传文件过程中的百分比回调
	 */
	static void writeFile(File file, OutputStream os, PercentActionCallBackI<File> callBack) {
		FileInputStream fStream = null;
		try {
			/* 取得文件的FileInputStream */
			fStream = new FileInputStream(file);
			/* 设定每次写入4096bytes */
			int bufferSize = FileUtils.BUFFER_SIZE;
			byte[] buffer = new byte[bufferSize];
			int lengthPerTime = 0;// 循环读写中,每一次读取的字节
			int lengthReadSum = 0;// 循环读写中,读取的字节的总数量
			Long fileSize = file.length();// 得到文件的总长度
			int countOfUpdate = (int) (fileSize / bufferSize / 100);
			if (countOfUpdate == 0) {
				countOfUpdate = 1;
			}
			int countOfNowCycle = 0;// 当前循环的次数
			/* 从文件读取数据到缓冲区 */
			while ((lengthPerTime = fStream.read(buffer)) != -1) {
				/* 将数据写入DataOutputStream中 */
				os.write(buffer, 0, lengthPerTime);
				if (null != callBack) {
					lengthReadSum += lengthPerTime;
					if (countOfNowCycle >= countOfUpdate) {
						countOfNowCycle = 0;
						if (callBack != null)
							callBack.updateRate(lengthReadSum / fileSize);
					}
					countOfNowCycle++;
				}
			}
			if (null != callBack) {
				callBack.actionSuccess(file);
			}
		} catch (Throwable e) {
			if (null != callBack) {
				callBack.actionFailed(e);
			} else {
				throw new BaseException(e);
			}
		} finally {
			try {
				if (fStream != null) {
					fStream.close();
				}
				if (os != null) {
					os.flush();
					os.close();
				}
			} catch (Throwable e) {
				throw new StreamCloseException(e);
			}
		}
	}
}
