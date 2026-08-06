package com.tingfeng.util.java.base.file;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.*;
import java.io.FileFilter;

import com.tingfeng.util.java.base.file.strategy.FileCopyStrategy;
import com.tingfeng.util.java.base.file.strategy.ProgressCallback;
import com.tingfeng.util.java.base.lang.base.Base64ConvertToStringI;
import com.tingfeng.util.java.base.lang.inter.PercentActionCallBackI;
import com.tingfeng.util.java.base.lang.inter.RateCallBackI;
import com.tingfeng.util.java.base.io.IOUtils;
import com.tingfeng.util.java.base.lang.StringUtils;

/**
 * 文件相关工具类
 * 1. 0.3 版本将文件和stream分离，提供单独的流拷贝
 * 2. 提供url到流的转换
 * 3. 提供文件和刘的转换
 * 4. 通过1、2、3形成流、url、文件三种的互相转换
 * @author huitoukest
 */
public class FileUtils {
	/**
	 * 默认的文件缓存字节数量
	 */
	public static final int BUFFER_SIZE = 4096;
	/**
	 * 直接删除一个文件/文件夹,成功返回true,失败返回false
	 * 
	 * @param path 文件路径
	 * @return 删除成功 = true or false
	 * @see FileDeleteOps
	 */
	public static boolean deleteFile(String path) {
		return FileDeleteOps.deleteFile(path);
	}

	/**
	 * 删除文件,带有重试次数和时间参数
	 * 文件删除至少执行一次
	 * @param path 文件路径
	 * @param tryCount 大于0
	 * @param intervalMillsSecond 单位毫秒
	 * @see FileDeleteOps
	 */
	public static void deleteFile(String path,int tryCount,int intervalMillsSecond){
		FileDeleteOps.deleteFile(path, tryCount, intervalMillsSecond);
	}

	/**
	 * 删除文件夹中内容,此文件夹本身;
	 * @param file 文件夹
	 * @param isDeleteChild
	 *            文件夹中存在内容的时候,是否删除子文件/文件夹
	 * @param isDeleteSelf
	 *            是否删除自身
	 * @return 删除成功 = true  or false
	 * @see FileDeleteOps
	 */
	public static boolean deleteFolder(File file, boolean isDeleteChild, boolean isDeleteSelf){
		return FileDeleteOps.deleteFolder(file, isDeleteChild, isDeleteSelf);
	}

	/**
	 * 删除文件夹
	 * @param path 路径
	 * @param isDeleteChild 是否删除子文件/子文件夹
	 * @param isDeleteSelf 是否删除自身
	 * @return 删除成功 = true or false
	 * @see FileDeleteOps
	 */
	public static boolean deleteFolder(String path, boolean isDeleteChild, boolean isDeleteSelf){
		return FileDeleteOps.deleteFolder(path, isDeleteChild, isDeleteSelf);
	}

	/**
	 * 删除文件
	 * @param file
	 * @return 删除成功 = true or false
	 * @see FileDeleteOps
	 */
	public static boolean deleteFile(File file) {
		return FileDeleteOps.deleteFile(file);
	}

	/**
	 * 创建指定path的文件夹,不能创建多级文件夹 如果不存在此文件夹,那么创建 如果此名称是个文件,删除后创建
	 * 
	 * @param path 文件路径
	 * @return 删除成功 = true or false
	 */
	public static boolean createFolder(String path) {
		File f = new File(path);
		if (!f.exists()) {
			f.mkdir();
			return true;
		} else if (f.isFile()) {
			f.delete();
			f.mkdir();
			return true;
		}
		return false;
	}

	/**
	 * 创建指定path的文件
	 * 
	 * @param path 创建的路径
	 * @throws IOException 创建路径错误/无权限时抛出异常
	 * @return 创建成功=true or false
	 */
	public static boolean createFile(String path) throws IOException {
		File f = new File(path);
		if (!f.exists()) {
			f.createNewFile();
			return true;
		} else if (f.isDirectory()) {
			f.delete();
			f.createNewFile();
			return true;
		}
		return false;
	}

	/**
	 * File(1)代表只加入文件,Folder(2)代表只加入文件夹,FileAndFolder(3)代表都加入
	 * 
	 * @author huitoukest
	 *
	 */
	public enum FileAddType {
		File(1), Folder(2), FileAndFolder(3);
		public int type = 1;

		FileAddType(int addType) {
			type = addType;
		}

	}

	/**
	 * 文件重命名策略枚举。
	 */
	public enum RenameOption {
		/** 目标存在时覆盖 */
		OVERWRITE,
		/** 目标存在时失败（抛异常） */
		FAIL_IF_EXISTS,
		/** 原子移动（跨文件系统时降级为 copy + delete） */
		ATOMIC_MOVE
	}

	/**
	 * 重命名操作结果。
	 */
	public static class RenameResult {
		/** 源文件 */
		public final File source;
		/** 目标文件 */
		public final File target;
		/** 是否成功 */
		public final boolean success;
		/** 错误消息（成功时为 null） */
		public final String errorMessage;

		/**
		 * 包级私有构造器，由 Ops 层创建。
		 */
		RenameResult(File source, File target, boolean success, String errorMessage) {
			this.source = source;
			this.target = target;
			this.success = success;
			this.errorMessage = errorMessage;
		}
	}

	/**
	 * 加入当前文件/文件夹下面的(包含自己的)所有子文件夹或者文件
	 * 
	 * @param f
	 *            一个父文件或者文件夹
	 * @param fList
	 *            List,一个文件的List,如果传入的list为null,会自动创建一个List
	 * @param addType
	 *            FileAddType,可以在FileUtils_wg.xxx的静态变量中找到,1代表只加入文件,2代表只加入文件夹,3代表都加入
	 * @param exceptZeroFile
	 *            是否除去大小为0的文件,即exceptZeroFile为true的时候,大小为0的文件不会被加入;
	 */
	protected static void getAllFilesByAFolders(File f, List<File> fList, FileAddType addType, boolean exceptZeroFile) {
		if (fList == null) {
			fList = new ArrayList<File>();
		}
		if (f.exists() && f.isFile() && ((addType.type - 1) == 0 || (addType.type - 3 == 0))) {
			if (exceptZeroFile && f.length() < 1)
				return;
			fList.add(f);
		}
		if (f.exists() && f.isDirectory() && ((addType.type - 2) == 0 || (addType.type - 3 == 0))) {
			fList.add(f);
			File[] childs = f.listFiles();// 列出当前目录中所有子目录
			if (null != childs) {
				for (int i = 0; i < childs.length; i++) {
					getAllFilesByAFolders(childs[i], fList, addType, exceptZeroFile);
				}
			}
		}
	}

	/**
	 * 基于path的字符串分析截取获得无扩展名的文件名称
	 * @param filePath 文件路径
	 * @return 返回不带扩展名的文件名称
	 */
	public static String getFileNoExtensionName(String filePath) {
		if (StringUtils.isEmpty(filePath)) {
			return filePath;
		}
		String fileNameString = getFileNameByPath(filePath);
		return fileNameString.substring(0, fileNameString.indexOf("." + FileUtils.getFileExtension(fileNameString)));
	}

	/**
	 * 获取文件扩展名 , 基于字符串截取方式
	 * @param filePath
	 * @return 返回文件的扩展名,如果扩展名不存在返回"",否则返回原值; 返回的扩展名不包含小点；
	 */
	public static String getFileExtension(String filePath) {
		if (StringUtils.isEmpty(filePath)) {
			return filePath;
		}
		filePath = filePath.toLowerCase();
		int dotIndex = filePath.lastIndexOf(".");
		if (dotIndex <= 0 || (dotIndex + 1 == filePath.length())) {
			return "";
		} else {
			return filePath.substring(dotIndex + 1, filePath.length());
		}
	}

	/**
	 * 通过一个路径或者url来获得到文件名称
	 * 
	 * @param filePath 文件路径
	 * @return 文件名称
	 */
	public static String getFileNameByPath(String filePath) {
		if (StringUtils.isEmpty(filePath)) {
			return filePath;
		}
		String path = filePath.replaceAll("\\\\", "/");
		int index1 = path.lastIndexOf("/");
		int index2 = path.lastIndexOf(":");
		if (index1 < index2) {
			index1 = index2;
		}
		if (index1 < 0) {
			return filePath;
		}
		return filePath.substring(index1 + 1);
	}

	/**
	 * 将一个文件的内容读取出来,并转换成为字符串的方式来返回相应的内容
	 * @param file
	 * @param base64ConvertToStringI
	 * @return 文件对应的base64字符串
	 * @see FileRWOps
	 */
	public static String transFileToString(File file, Base64ConvertToStringI base64ConvertToStringI){
		return FileRWOps.transFileToString(file, base64ConvertToStringI);
	}




	/**
	 * 用指定的写出文件流来写出文件;
	 * @param file 目标文件
	 * @param os 输出流
	 * @param callBack callBack 回调 PercentActionCallBackI ，在文件操作完成之后回调成功或者失败的操作,以及上传文件过程中的百分比回调
	 * @see FileCopyOps
	 */
	public static void writeFile(File file, OutputStream os, PercentActionCallBackI<File> callBack){
		FileCopyOps.writeFile(file, os, callBack);
	}

	/**
	 * 将一个输入流转换成指定编码的字符串
	 * @param inputStream 输入流
	 * @param encode 编码
	 * @return 字符串
	 * @see FileRWOps
	 */
	public static String transInputStreamToStringByEncode(InputStream inputStream, String encode) {
		return FileRWOps.transInputStreamToStringByEncode(inputStream, encode);
	}

	/**
	 * 文件拷贝, 通过channel方式 , 高效
	 * @param srcPath 源路径
	 * @param destPath 目标路径
	 * @see FileCopyOps
	 */
	public static void copyFile(String srcPath,String destPath ){
		FileCopyOps.copyFile(srcPath, destPath);
	}

	/**
	 * 带进度的文件拷贝
	 *
	 * @param srcPath 源路径
	 * @param destPath 目标路径
	 * @param fileCopyActionCallBack 当fileCopyActionCallBack为null的时候,将不会更新进度;
	 * @see FileCopyOps
	 */
	public static void copyFile(String srcPath,String destPath, RateCallBackI fileCopyActionCallBack){
		FileCopyOps.copyFile(srcPath, destPath, fileCopyActionCallBack);
	}

	/**
	 * 带进度的文件拷贝,同步的
	 * 
	 * @param source 源路径
	 * @param target 目标路径
	 * @param fileCopyActionCallBack 当fileCopyActionCallBack为null的时候,将不会更新进度;
	 * @see FileCopyOps
	 */
	public static void copyFileByFileChannel(File source, File target, RateCallBackI fileCopyActionCallBack) {
		FileCopyOps.copyFileByFileChannel(source, target, fileCopyActionCallBack);
	}

	/**
	 * 带进度的文件拷贝
	 *
	 * @param source 源路径
	 * @param target 目标路径
	 * @param fileCopyActionCallBack 当fileCopyActionCallBack为null的时候,将不会更新进度;
	 * @see FileCopyOps
	 */
	public static void copyFileByFileChannel(String source, String target, RateCallBackI fileCopyActionCallBack){
		FileCopyOps.copyFileByFileChannel(source, target, fileCopyActionCallBack);
	}

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
	 * @see FileCopyOps
	 */
	public static void copyFile(String srcPath, String destPath, FileCopyStrategy strategy) {
		FileCopyOps.copyFile(srcPath, destPath, strategy);
	}

	/**
	 * 使用策略模式拷贝文件（无进度回调）
	 *
	 * @param src 源文件
	 * @param dest 目标文件
	 * @param strategy 文件拷贝策略（StreamCopyStrategy/ChannelCopyStrategy/AsyncCopyStrategy）
	 * @see FileCopyOps
	 */
	public static void copyFile(File src, File dest, FileCopyStrategy strategy) {
		FileCopyOps.copyFile(src, dest, strategy);
	}

	/**
	 * 使用策略模式拷贝文件（带进度回调）
	 *
	 * @param srcPath 源路径
	 * @param destPath 目标路径
	 * @param strategy 文件拷贝策略
	 * @param callback 进度回调
	 * @see FileCopyOps
	 */
	public static void copyFile(String srcPath, String destPath, FileCopyStrategy strategy, ProgressCallback callback) {
		FileCopyOps.copyFile(srcPath, destPath, strategy, callback);
	}

	/**
	 * 使用策略模式拷贝文件（带进度回调）
	 *
	 * @param src 源文件
	 * @param dest 目标文件
	 * @param strategy 文件拷贝策略
	 * @param callback 进度回调
	 * @see FileCopyOps
	 */
	public static void copyFile(File src, File dest, FileCopyStrategy strategy, ProgressCallback callback) {
		FileCopyOps.copyFile(src, dest, strategy, callback);
	}

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
	 * @see FileCopyOps
	 */
	public static boolean copyFileByStream(String srcFileName, String destFileName, boolean overlay) {
		return FileCopyOps.copyFileByStream(srcFileName, destFileName, overlay);
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
	 * @see FileCopyOps
	 */
	public static boolean copyDirectory(String srcDirName, String destDirName, boolean overlay) {
		return FileCopyOps.copyDirectory(srcDirName, destDirName, overlay);
	}


	/**
	 * 拷贝并重命名文件
	 * @param file
	 * @param destPath
	 * @param destFileName
	 * @see FileCopyOps
	 */
	public static void copyFile(File file, String destPath, String destFileName){
		FileCopyOps.copyFile(file, destPath, destFileName);
	}

	/**
	 * 保存文件。（保留原有文件名）
	 * @param inputStream 输入文件流
	 * @param destPath 保存路径
	 * @param destFileName 文件名
	 * @see FileCopyOps
	 */
	public static void copyFile(InputStream inputStream, String destPath, String destFileName){
		FileCopyOps.copyFile(inputStream, destPath, destFileName);
	}




	/**
	 * 通过base64的字符串来获取文件名data:image/png;
	 * @param imgStr 如果没有获取到或者内容是空则返回空串
	 * @return 文件扩展名称
	 * @see FileCodecOps
	 */
	public static String getExtensionNameByBase64Img(String imgStr){
		return FileCodecOps.getExtensionNameByBase64Img(imgStr);
	}

	/**
	 * 上传成功返回true，否则返回false;并且会自动关闭输出流
	 * @param fileStr base64编码字符串
	 * @param out 输出流
	 * @see FileCodecOps
	 */
	public static void saveBase64File(String fileStr,OutputStream out){
		FileCodecOps.saveBase64File(fileStr, out);
	}

	/**
	 * 获取base64的图片的内容信息
	 * @param fileStr base64编码字符串
	 * @return 去除base64文件名称和扩展名信息后的内容
	 * @see FileCodecOps
	 */
	public static String getBase64ImgFileContent(String fileStr){
		return FileCodecOps.getBase64ImgFileContent(fileStr);
	}

	// ==================== 文件读写简洁封装 ====================
	// 设计原则：
	// 1. 文件直接操作放在此类，IOUtils只处理流
	// 2. 底层调用IOUtils的流操作方法

	/**
	 * 读取文件为字节数组
	 * @param file 文件
	 * @return 字节数组
	 *
	 * 设计思路：底层调用IOUtils.toByteArray()，由其内部实现流拷贝
	 * @see FileRWOps
	 */
	public static byte[] readFileToByteArray(File file) {
		return FileRWOps.readFileToByteArray(file);
	}

	/**
	 * 读取文件为字符串
	 * @param file 文件
	 * @param charset 字符编码
	 * @return 字符串
	 * @see FileRWOps
	 */
	public static String readFileToString(File file, Charset charset) {
		return FileRWOps.readFileToString(file, charset);
	}

	/**
	 * 读取文件为字符串（UTF-8）
	 * @param file 文件
	 * @return 字符串
	 * @see FileRWOps
	 */
	public static String readFileToString(File file) {
		return FileRWOps.readFileToString(file);
	}

	/**
	 * 将字节数组写入文件
	 * @param file 文件
	 * @param data 字节数组
	 * @see FileRWOps
	 */
	public static void writeByteArrayToFile(File file, byte[] data) {
		FileRWOps.writeByteArrayToFile(file, data);
	}

	/**
	 * 将字符串写入文件
	 * @param file 文件
	 * @param content 字符串内容
	 * @param charset 字符编码
	 * @param append 是否追加
	 * @see FileRWOps
	 */
	public static void writeStringToFile(File file, String content, Charset charset, boolean append) {
		FileRWOps.writeStringToFile(file, content, charset, append);
	}

	/**
	 * 将字符串写入文件（UTF-8，覆盖模式）
	 * @param file 文件
	 * @param content 字符串内容
	 * @see FileRWOps
	 */
	public static void writeStringToFile(File file, String content) {
		FileRWOps.writeStringToFile(file, content);
	}

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
	public static CompletableFuture<byte[]> readFileAsync(File file,
	                                                      Object executor,
	                                                      BiFunction<Long, Long, Boolean> readCallback,
	                                                      IOUtils.CancellationToken token) {
		return FileAsyncOps.readFileAsync(file, executor, readCallback, token);
	}

	/**
	 * 异步文件读取（简单版）
	 */
	public static CompletableFuture<byte[]> readFileAsync(File file, Object executor) {
		return FileAsyncOps.readFileAsync(file, executor);
	}

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
	public static CompletableFuture<Boolean> writeFileAsync(File file, byte[] data,
	                                                        Object executor,
	                                                        BiConsumer<Long, Long> writeCallback,
	                                                        IOUtils.CancellationToken token) {
		return FileAsyncOps.writeFileAsync(file, data, executor, writeCallback, token);
	}

	/**
	 * 异步文件写入（字符串）
	 */
	public static CompletableFuture<Boolean> writeFileAsync(File file, String content,
	                                                        Charset charset, boolean append,
	                                                        Object executor,
	                                                        IOUtils.CancellationToken token) {
		return FileAsyncOps.writeFileAsync(file, content, charset, append, executor, token);
	}

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
	public static CompletableFuture<Long> copyFileAsync(File dest, File src,
	                                                     Object executor,
	                                                     Consumer<Long> progressCallback,
	                                                     int backPressureLimit,
	                                                     IOUtils.CancellationToken token) {
		return FileAsyncOps.copyFileAsync(dest, src, executor, progressCallback, backPressureLimit, token);
	}

	/**
	 * 异步文件拷贝（使用默认背压限制）
	 */
	public static CompletableFuture<Long> copyFileAsync(File dest, File src,
	                                                     Object executor,
	                                                     Consumer<Long> progressCallback,
	                                                     IOUtils.CancellationToken token) {
		return FileAsyncOps.copyFileAsync(dest, src, executor, progressCallback, token);
	}

	// ==================== 行写入操作 ====================
	// 设计原则：
	// 1. 追加模式：自动添加换行符\n
	// 2. 使用BufferedWriter缓存，避免频繁IO
	// 3. 支持指定字符编码
	// 4. 关闭时自动flush

	/**
	 * 追加一行字符串到文件（自动换行）
	 *
	 * @param file 文件（追加模式）
	 * @param line 行内容
	 * @see FileRWOps
	 */
	public static void writeLine(File file, String line) {
		FileRWOps.writeLine(file, line);
	}

	/**
	 * 追加一行字符串到文件
	 *
	 * @param file 文件
	 * @param line 行内容
	 * @param charset 字符编码
	 * @param append 是否追加，false则覆盖
	 * @see FileRWOps
	 */
	public static void writeLine(File file, String line, Charset charset, boolean append) {
		FileRWOps.writeLine(file, line, charset, append);
	}

	/**
	 * 追加一行字符串到文件（指定编码）
	 *
	 * @param file 文件（追加模式）
	 * @param line 行内容
	 * @param charset 字符编码
	 * @see FileRWOps
	 */
	public static void writeLine(File file, String line, Charset charset) {
		FileRWOps.writeLine(file, line, charset);
	}

	/**
	 * 追加多行字符串到文件
	 *
	 * @param file 文件（追加模式）
	 * @param lines 行列表
	 * @see FileRWOps
	 */
	public static void writeLines(File file, List<String> lines) {
		FileRWOps.writeLines(file, lines);
	}

	/**
	 * 追加多行字符串到文件
	 *
	 * @param file 文件
	 * @param lines 行列表
	 * @param charset 字符编码
	 * @param append 是否追加
	 * @see FileRWOps
	 */
	public static void writeLines(File file, List<String> lines, Charset charset, boolean append) {
		FileRWOps.writeLines(file, lines, charset, append);
	}

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
	public static CompletableFuture<Boolean> writeLineAsync(File file, String line,
	                                                       Charset charset, boolean append,
	                                                       Object executor,
	                                                       IOUtils.CancellationToken token) {
		return FileAsyncOps.writeLineAsync(file, line, charset, append, executor, token);
	}

	/**
	 * 异步追加一行字符串（UTF-8，追加模式）
	 */
	public static CompletableFuture<Boolean> writeLineAsync(File file, String line,
	                                                       Object executor,
	                                                       IOUtils.CancellationToken token) {
		return FileAsyncOps.writeLineAsync(file, line, executor, token);
	}

	// ==================== 文件重命名 ====================

	/**
	 * 单文件重命名（默认策略 {@link RenameOption#FAIL_IF_EXISTS}）。
	 *
	 * @param source 源文件
	 * @param target 目标文件
	 * @return 重命名成功返回 true
	 * @see FileRenameOps
	 */
	public static boolean renameFile(File source, File target) {
		return FileRenameOps.rename(source, target);
	}

	/**
	 * 单文件重命名（指定策略）。
	 *
	 * @param source  源文件
	 * @param target  目标文件
	 * @param options 重命名策略选项
	 * @return 重命名成功返回 true
	 * @see FileRenameOps
	 */
	public static boolean renameFile(File source, File target, RenameOption... options) {
		return FileRenameOps.rename(source, target, options);
	}

	/**
	 * 批量重命名文件（默认开启回滚）。
	 *
	 * @param files  文件数组
	 * @param filter 过滤条件
	 * @param namer  命名函数
	 * @return 重命名结果列表
	 * @see FileRenameOps
	 */
	public static List<RenameResult> batchRenameFiles(File[] files,
			Predicate<File> filter, Function<File, String> namer) {
		return FileRenameOps.batchRename(files, filter, namer);
	}

	/**
	 * 批量重命名文件（使用指定策略，默认开启回滚）。
	 *
	 * @param files   文件数组
	 * @param filter  过滤条件
	 * @param namer   命名函数
	 * @param options 重命名策略选项
	 * @return 重命名结果列表
	 * @see FileRenameOps
	 */
	public static List<RenameResult> batchRenameFiles(File[] files,
			Predicate<File> filter, Function<File, String> namer,
			RenameOption... options) {
		return FileRenameOps.batchRename(files, filter, namer, options);
	}

	/**
	 * 批量重命名文件（控制是否回滚）。
	 *
	 * @param files             文件数组
	 * @param filter            过滤条件
	 * @param namer             命名函数
	 * @param rollbackOnFailure 失败时是否回滚已重命名的文件
	 * @return 重命名结果列表
	 * @see FileRenameOps
	 */
	public static List<RenameResult> batchRenameFiles(File[] files,
			Predicate<File> filter, Function<File, String> namer,
			boolean rollbackOnFailure) {
		return FileRenameOps.batchRename(files, filter, namer, rollbackOnFailure);
	}

	// ==================== 临时文件管理 ====================

	/**
	 * 创建临时文件（快捷方法）。
	 * <p>
	 * 等效于 {@link TempFileManager#createTempFile(String, String)}。
	 * 不绑定管理器，文件由 JVM 在退出时自动删除。
	 * </p>
	 *
	 * @param prefix 文件前缀，null 时使用 "temp"
	 * @param suffix 文件后缀，null 时使用 ".tmp"
	 * @return 创建的临时文件
	 * @throws com.tingfeng.util.java.base.lang.exception.BaseException 创建失败时抛出
	 * @see TempFileManager
	 */
	public static File createTempFile(String prefix, String suffix) {
		return TempFileManager.createTempFile(prefix, suffix);
	}

	/**
	 * 创建临时目录（快捷方法）。
	 * <p>
	 * 等效于 {@link TempFileManager#createTempDirectory(String)}。
	 * </p>
	 *
	 * @param prefix 目录前缀，null 时使用 "temp"
	 * @return 创建的临时目录
	 * @throws com.tingfeng.util.java.base.lang.exception.BaseException 创建失败时抛出
	 * @see TempFileManager
	 */
	public static File createTempDirectory(String prefix) {
		return TempFileManager.createTempDirectory(prefix);
	}

	/**
	 * 创建空的临时文件管理器。
	 * <p>
	 * 等效于 {@code new TempFileManager()}。
	 * 通过 {@link TempFileManager#createFile(String, String)} 和
	 * {@link TempFileManager#createDirectory(String)} 添加资源。
	 * </p>
	 *
	 * @return TempFileManager 实例
	 * @see TempFileManager
	 */
	public static TempFileManager createTempManager() {
		return new TempFileManager();
	}

	// ==================== 路径归一化 ====================

	/**
	 * 归一化路径字符串。
	 * 统一分隔符为 '/'，解析并去除 '.' 和 '..' 冗余片段。
	 *
	 * @param path 原始路径，非 null
	 * @return 归一化后的路径
	 * @throws IllegalArgumentException path 为 null 时抛出
	 * @see FilePathOps
	 */
	public static String normalizePath(String path) {
		return FilePathOps.normalize(path);
	}

	/**
	 * 归一化路径字符串，支持控制是否保留尾部分隔符。
	 *
	 * @param path                  原始路径，非 null
	 * @param keepTrailingSeparator 是否保留尾部分隔符
	 * @return 归一化后的路径
	 * @throws IllegalArgumentException path 为 null 时抛出
	 * @see FilePathOps
	 */
	public static String normalizePath(String path, boolean keepTrailingSeparator) {
		return FilePathOps.normalize(path, keepTrailingSeparator);
	}

	/**
	 * 判断路径是否为绝对路径（跨平台兼容）。
	 *
	 * @param path 路径字符串，非 null
	 * @return true 表示为绝对路径；null 或空串返回 false
	 * @throws IllegalArgumentException path 为 null 时抛出
	 * @see FilePathOps
	 */
	public static boolean isAbsolutePath(String path) {
		return FilePathOps.isAbsolute(path);
	}

	/**
	 * 将路径中的分隔符统一转换为 Unix 风格（'/'）。
	 *
	 * @param path 路径字符串
	 * @return 转换后的路径，null 时返回 null
	 * @see FilePathOps
	 */
	public static String separatorsToUnix(String path) {
		return FilePathOps.separatorsToUnix(path);
	}

	/**
	 * 将路径中的分隔符统一转换为 Windows 风格（'\\'）。
	 *
	 * @param path 路径字符串
	 * @return 转换后的路径，null 时返回 null
	 * @see FilePathOps
	 */
	public static String separatorsToWindows(String path) {
		return FilePathOps.separatorsToWindows(path);
	}

	// ==================== 目录列表（非递归） ====================

	/**
	 * 列出指定目录下的所有直接子项（非递归）。
	 * <p>返回目录下所有文件/目录的列表，不递归遍历子目录。</p>
	 *
	 * @param dir 目标目录，非 null
	 * @return 子项列表，不会为 null（空目录或不存在时返回空列表）
	 * @throws IllegalArgumentException dir 为 null 时抛出
	 * @see FileWalkOps
	 */
	public static List<File> listFiles(File dir) {
		return FileWalkOps.listFiles(dir);
	}

	/**
	 * 列出指定目录下的直接子项，使用过滤器筛选（非递归）。
	 *
	 * @param dir    目标目录，非 null
	 * @param filter 文件过滤器，null 时返回全部子项
	 * @return 子项列表，不会为 null
	 * @throws IllegalArgumentException dir 为 null 时抛出
	 * @see FileWalkOps
	 */
	public static List<File> listFiles(File dir, FileFilter filter) {
		return FileWalkOps.listFiles(dir, filter);
	}

	// ==================== 目录遍历 ====================

	/**
	 * 遍历目录，返回所有匹配的文件列表（默认配置）。
	 * <p>
	 * 等效于 {@code walkFiles(root, WalkOption.builder().build())}，
	 * 默认只返回文件（不包含目录），最大深度 50。
	 * </p>
	 *
	 * @param root 遍历起始目录或文件，非 null
	 * @return 匹配的文件列表，不会为 null
	 * @throws IllegalArgumentException root 为 null 时抛出
	 * @see FileWalkOps
	 * @see WalkOption
	 */
	public static List<File> walkFiles(File root) {
		return FileWalkOps.walkFiles(root, WalkOption.builder().build());
	}

	/**
	 * 遍历目录，返回根据指定配置匹配的文件/目录列表。
	 * <p>
	 * 使用迭代 DFS 算法，防止递归导致的栈溢出。
	 * 支持深度限制、包含/排除过滤、文件/目录筛选。
	 * </p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * WalkOption opt = WalkOption.builder()
	 *     .maxDepth(10)
	 *     .includeFiles(true)
	 *     .includeDirs(true)
	 *     .filter(f -> f.getName().endsWith(".java"))
	 *     .build();
	 * List<File> files = FileUtils.walkFiles(root, opt);
	 * }</pre>
	 *
	 * @param root   遍历起始目录或文件，非 null
	 * @param option 遍历配置选项，非 null
	 * @return 匹配的文件/目录列表，不会为 null
	 * @throws IllegalArgumentException root 或 option 为 null 时抛出
	 * @see FileWalkOps
	 * @see WalkOption
	 */
	public static List<File> walkFiles(File root, WalkOption option) {
		return FileWalkOps.walkFiles(root, option);
	}
}
