package com.tingfeng.util.java.base.file;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.inter.Base64ConvertToStringI;
import com.tingfeng.util.java.base.common.inter.PercentActionCallBackI;
import com.tingfeng.util.java.base.common.inter.RateCallBackI;
import com.tingfeng.util.java.base.common.utils.Base64Utils;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 文件相关工具类
 */
@Slf4j
public class FileUtils {
	public static final int BUFFER_SIZE = 4096;
	public static  final String BASE64_IMG_HEADER_START = "data:image/";
	public static  final String BASE64_IMG_HEADER_END = ";base64";
	/**
	 * @param url
	 *            上传的url
	 * @param path
	 *            文件的路径
	 * @param params
	 *            参数
	 * @param callBack
	 *            回调 PercentActionCallBackI ，在文件操作完成之后回调成功或者失败的操作,以及上传文件过程中的百分比回调
	 */
	public static void uploadFile(final String url, final String path, final Map<String, String> params,
			final PercentActionCallBackI<File> callBack) {
		Thread thread = new Thread(()->{
				final String end = "/r/n";
				final String Hyphens = "--";
				final String boundary = "*****";
				DataOutputStream ds = null;
				HttpURLConnection conn = null;
				FileInputStream fStream = null;
				InputStream is = null;
				try {
					File uploadFile = new File(path);

					URL urlTemp = new URL(url);
					conn = (HttpURLConnection) urlTemp.openConnection();
					/* 允许Input、Output，不使用Cache */
					conn.setDoInput(true);
					conn.setDoOutput(true);
					conn.setConnectTimeout(10000);
					conn.setUseCaches(false);
					/* 设定传送的method=POST */
					conn.setRequestMethod("POST");
					/* setRequestProperty */
					conn.setRequestProperty("Connection", "Keep-Alive");
					conn.setRequestProperty("Charset", "UTF-8");
					conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
					if (params != null) {
						Set<String> keys = params.keySet();
						for (String s : keys) {
							conn.addRequestProperty(s, params.get(s));
						}
					}
					/* 设定DataOutputStream */
					ds = new DataOutputStream(conn.getOutputStream());
					ds.writeBytes(Hyphens + boundary + end);
					ds.writeBytes("Content-Disposition: form-data;" + "name=\"file1\";filename=\""
							+ uploadFile.getName() + "\"" + end);
					ds.writeBytes(end);
					/* 取得文件的FileInputStream */
					fStream = new FileInputStream(uploadFile);
					/* 设定每次写入2048bytes */
					int bufferSize = 2048;
					byte[] buffer = new byte[bufferSize];
					int lengthPerTime = 0;// 循环读写中,每一次读取的字节
					int lengthReadSum = 0;// 循环读写中,读取的字节的总数量
					Long fileSize = uploadFile.length();// 得到文件的总长度
					int countOfUpdate = (int) (fileSize / bufferSize / 100);
					if (countOfUpdate == 0) {
						countOfUpdate = 1;
					}
					int countOfNowCycle = 0;// 当前循环的次数
					/* 从文件读取数据到缓冲区 */
					while ((lengthPerTime = fStream.read(buffer)) != -1) {
						/* 将数据写入DataOutputStream中 */
						ds.write(buffer, 0, lengthPerTime);
						lengthReadSum += lengthPerTime;
						if (countOfNowCycle >= countOfUpdate) {
							countOfNowCycle = 0;
							callBack.updateRate(lengthReadSum / fileSize);
						}
						countOfNowCycle++;
					}
					ds.writeBytes(end);
					ds.writeBytes(Hyphens + boundary + Hyphens + end);
					ds.flush();
					/* 取得Response内容 */
					is = conn.getInputStream();
					int ch;
					StringBuffer b = new StringBuffer();
					while ((ch = is.read()) != -1) {
						b.append((char) ch);
					}
					callBack.actionSuccess(uploadFile);
				} catch (Exception e) {
					callBack.actionFailed(e);
				}finally {
					try {
						if(conn != null){
							conn.disconnect();
						}
						if (fStream != null) {
							fStream.close();
						}
						if (is != null) {
							is.close();
						}
						if (ds != null) {
							ds.close();
						}
					}catch (Throwable e){
						log.error("close stream error",e);
					}
				}
		});
		thread.start();
	}

	/**
	 * 从网络上下载一个文件,会自动开一个新的线程来下载
	 * 
	 * @param url
	 *            指定下载的url
	 * @param path
	 *            指定下载到本地的path,包括文件名
	 * @param params
	 *            此url的参数
	 * @param callBack
	 *            下载完毕之后的回调,实现DownFileFromServerCallBack接口
	 * @param connectTimeout 10000
	 * @return void
	 */
	public static void downFile(final String url, final String path, final Map<String, String> params,
			final PercentActionCallBackI<File> callBack,int connectTimeout) {
		Thread thread = new Thread(()-> {
				URL myFileUrl = null;
				File file = null;
			HttpURLConnection conn = null;
			FileOutputStream fStream = null;
			InputStream is = null;
			try {
					// 判断是否存在此文件夹，不存在创建
					file = new File(path);
					if (!file.exists()) {
						file.createNewFile();
					}
					myFileUrl = new URL(url);
					conn = (HttpURLConnection) myFileUrl.openConnection();
					if (params != null) {
						Set<String> keys = params.keySet();
						for (String s : keys) {
							conn.addRequestProperty(s, params.get(s));
						}
					}
					conn.setDefaultUseCaches(false);
					conn.setConnectTimeout(connectTimeout);
					conn.setDoInput(true);
					conn.connect();
					fStream = new FileOutputStream(file);
					is = conn.getInputStream();
					byte[] b = new byte[BUFFER_SIZE];
					int lengthPerTime = 0;// 循环读写中,每一次读取的字节
					int lengthReadSum = 0;// 循环读写中,读取的字节的总数量
					int fileSize = conn.getContentLength();// 得到文件的总长度
					int countOfUpdate = fileSize / BUFFER_SIZE / 100;
					if (countOfUpdate == 0) {
						countOfUpdate = 1;
					}
					int countOfNowCycle = 0;// 当前循环的次数
					while ((lengthPerTime = (is.read(b))) != -1) {
						fStream.write(b);
						lengthReadSum += lengthPerTime;
						if (countOfNowCycle >= countOfUpdate) {
							countOfNowCycle = 0;
							callBack.updateRate(lengthReadSum / fileSize);
						}
						countOfNowCycle++;
					}
					callBack.actionSuccess(file);
				} catch (Throwable e) {
					callBack.actionFailed(e);
				} finally {
					try {
						if(conn != null){
							conn.disconnect();
						}
						if (fStream != null) {
							fStream.close();
						}
						if (is != null) {
							is.close();
						}
					}catch (Throwable e){
						log.error("close stream error",e);
					}
				}
		});// end Thread
		thread.start();
	}

	/**
	 * 直接删除一个文件/文件夹,成功返回true,失败返回false
	 * 
	 * @param path
	 * @return
	 */
	public static boolean deleteFile(String path) {
		File file = new File(path);
		if (file.exists()) {
			return file.delete();
		}
		return true;
	}

	/**
	 * 删除文件,带有重试次数和时间参数
	 * 文件删除至少执行一次
	 * @param path
	 * @param tryCount 大于0
	 * @param intervalMillsSecond 单位毫秒
	 */
	public static void deleteFile(String path,int tryCount,int intervalMillsSecond) throws InterruptedException {
		do{
			File file =  new File(path);
			if(!file.exists()){
				break;
			}
			if(!file.canWrite()){
				Thread.sleep(intervalMillsSecond);
			}else{
				if(file.delete()) {
					break;
				}
			}
		}while(tryCount-- >= 0);
	}

	/**
	 * 删除文件夹中内容,此文件夹本身;
	 * 
	 * @param isDeleteChild
	 *            文件夹中存在内容的时候,是否删除子文件/文件夹
	 * @param isDeleteSelf
	 *            是否删除自身
	 * @return
	 * @throws Exception
	 */
	public static boolean deleteFolder(File file, boolean isDeleteChild, boolean isDeleteSelf) throws Exception {
		if (file == null || !file.exists()) {
			return false;
		}
		File[] childs = file.listFiles();// 列出当前目录中所有子目录
		if (!isDeleteChild && childs.length > 0) {
			throw new Exception("Folder is not empty!");
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

	public static boolean deleteFolder(String path, boolean isDeleteChild, boolean isDeleteSelf) throws Exception {
		return deleteFolder(new File(path), isDeleteChild, isDeleteSelf);
	}

	public static boolean deleteFile(File file) {
		if (file != null && file.exists()) {
			file.delete();
			return true;
		}
		return false;
	}

	/**
	 * 创建指定path的文件夹,不能创建多级文件夹 如果不存在此文件夹,那么创建 如果此名称是个文件,删除后创建
	 * 
	 * @param path
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
	 * @param path
	 * @throws IOException
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

		private FileAddType(int addType) {
			type = addType;
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
	 * 
	 * @param filePath
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
	 * 
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
	 * @param filePath
	 * @return
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
	 * 
	 * @return
	 * @throws IOException
	 */
	public static String transFileToString(File file, Base64ConvertToStringI base64ConvertToStringI)
			throws IOException {
		if (file == null || !file.exists()) {
			return null;
		}
		String content = "";
		byte[] bs = new byte[BUFFER_SIZE];
		InputStream is = new FileInputStream(file);
		BufferedInputStream br = new BufferedInputStream(is);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try{
			int readLength = 0;
			while ((readLength = is.read(bs)) != -1) {
				bos.write(bs, 0, readLength);

			}
			content = base64ConvertToStringI.convertToString(bos.toByteArray(), 0);
		}finally {
			bos.close();
			br.close();
			is.close();
		}
		return content;
	}

	/**
	 * 用指定的写出文件流来写出文件;
	 * 
	 * @param file
	 * @param os
	 * @param callBack callBack 回调 PercentActionCallBackI ，在文件操作完成之后回调成功或者失败的操作,以及上传文件过程中的百分比回调
	 * @throws
	 */
	public static void writeFile(File file, OutputStream os, PercentActionCallBackI<File> callBack){
		new Thread(()->{
			FileInputStream fStream = null;
			try {
				/* 取得文件的FileInputStream */
				fStream = new FileInputStream(file);
				/* 设定每次写入4096bytes */
				int bufferSize = 4096;
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
					lengthReadSum += lengthPerTime;
					if (countOfNowCycle >= countOfUpdate) {
						countOfNowCycle = 0;
						if (callBack != null)
							callBack.updateRate(lengthReadSum / fileSize);
					}
					countOfNowCycle++;
				}
				callBack.actionSuccess(file);
			}catch (Throwable e){
				callBack.actionFailed(e);
			}finally {
				try {
					if (fStream != null) {
						fStream.close();
					}
					if (os != null) {
						os.flush();
						os.close();
					}
				}catch (Throwable e){
					log.error("close stream error",e);
				}
			}
		}).start();
	}

	/**
	 * 将一个输入流转换成指定编码的字符串
	 * 
	 */
	public static String transInputStreamToStringByEncode(InputStream inputStream, String encode) {
		// 内存流
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] data = new byte[4096];
		int len = 0;
		String result = null;
		if (inputStream != null) {
			try {
				while ((len = inputStream.read(data)) != -1) {
					byteArrayOutputStream.write(data, 0, len);
				}
				result = new String(byteArrayOutputStream.toByteArray(), encode);
			} catch (IOException e) {
				throw new BaseException(e);
			}finally {
				try {
					inputStream.close();
					byteArrayOutputStream.close();
				}catch (Throwable e){
					log.error("close stream error",e);
				}
			}
		}
		return result;
	}

	/**
	 * 文件拷贝
	 *
	 * @param srcPath
	 * @param destPath
	 * @throws IOException
	 */
	public static void copyFile(String srcPath,String destPath ) throws IOException {
		copyFileByFileChannel(new File(srcPath),new File(destPath),null);
	}

	/**
	 * 带进度的文件拷贝
	 *
	 * @param srcPath
	 * @param destPath
	 * @param fileCopyActionCallBack
	 *            当fileCopyActionCallBack为null的时候,将不会更新进度;
	 * @throws IOException
	 */
	public static void copyFile(String srcPath,String destPath, RateCallBackI fileCopyActionCallBack) throws IOException {
		copyFileByFileChannel(new File(srcPath),new File(destPath),fileCopyActionCallBack);
	}

	/**
	 * 带进度的文件拷贝,同步的
	 * 
	 * @param source
	 * @param target
	 * @param fileCopyActionCallBack
	 *            当fileCopyActionCallBack为null的时候,将不会更新进度;
	 * @throws IOException
	 */
	public static void copyFileByFileChannel(File source, File target, RateCallBackI fileCopyActionCallBack) {
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
				int countOfUpdate = (int) (fileSize / BUFFER_SIZE / 100);
				if (countOfUpdate == 0) {
					countOfUpdate = 1;
				}
				int countOfNowCycle = 0;// 当前循环的次数
				ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
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
			}catch (Throwable e){
				log.error("close stream error",e);
			}
		}
	}

	/**
	 * 带进度的文件拷贝
	 * 
	 * @param source
	 * @param target
	 * @param fileCopyActionCallBack
	 *            当fileCopyActionCallBack为null的时候,将不会更新进度;
	 * @throws IOException
	 */
	public static void copyFileByFileChannel(String source, String target, RateCallBackI fileCopyActionCallBack)
			throws IOException {
		copyFileByFileChannel(new File(source), new File(target), fileCopyActionCallBack);
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
	 */
	public static boolean copyFileByStream(String srcFileName, String destFileName, boolean overlay) {
		File srcFile = new File(srcFileName);
		String msg = "";
		// 判断源文件是否存在
		if (!srcFile.exists()) {
			msg = "源文件：" + srcFileName + "不存在！";
			return false;
		} else if (!srcFile.isFile()) {
			msg = "复制文件失败，源文件：" + srcFileName + "不是一个文件！";
			if(log.isInfoEnabled()) {
				log.info(msg);
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
		// 复制文件
		int byteread = 0; // 读取的字节数
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(srcFile);
			out = new FileOutputStream(destFile);
			byte[] buffer = new byte[4096];

			while ((byteread = in.read(buffer)) != -1) {
				out.write(buffer, 0, byteread);
			}
			return true;
		} catch (Throwable e) {
			return false;
		}finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (Throwable e) {
				log.error("close stream error",e);
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
	public static boolean copyDirectory(String srcDirName, String destDirName, boolean overlay) {
		String msg = "";
		// 判断源目录是否存在
		File srcDir = new File(srcDirName);
		if (!srcDir.exists()) {
			msg = "复制目录失败：源目录" + srcDirName + "不存在！";
			log.info(msg);
			return false;
		} else if (!srcDir.isDirectory()) {
			msg = "复制目录失败：" + srcDirName + "不是目录！";
			log.info(msg);
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
				if(log.isInfoEnabled()) {
					log.info(msg);
				}
				return false;
			}
		} else {
			// 创建目的目录
			if (!destDir.mkdirs()) {
				if(log.isInfoEnabled()){
					log.info("复制目录失败：创建目的目录失败！");
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
			if(log.isInfoEnabled()) {
				log.info(msg);
			}
			return false;
		} else {
			return true;
		}
	}


	/**
	 * 拷贝并重命名文件
	 * @param file
	 * @param destPath
	 * @param destFileName
	 * @throws IOException
	 */
	public static void copyFile(File file, String destPath, String destFileName) throws IOException {
		copyFile(new FileInputStream(file),destPath,destFileName);
	}

	/**
	 * 保存文件。（保留原有文件名）
	 * @param inputStream 输入文件流
	 * @param destPath 保存路径
	 * @param destFileName 文件名
	 */
	public static void copyFile(InputStream inputStream, String destPath, String destFileName) throws IOException {
		int bytesum = 0;
		int byteread;
		FileOutputStream fs = null;
		try {
			fs = new FileOutputStream(destPath + destFileName);
			byte[] buffer = new byte[1444];
			while ((byteread = inputStream.read(buffer)) != -1) {
				bytesum += byteread;
				System.out.println(bytesum);
				fs.write(buffer, 0, byteread);
			}
		}finally {
			if (fs != null){
				try {
					fs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}




	/**
	 * 通过base64的字符串来获取文件名data:image/png;
	 * @param imgStr 如果没有获取到或者内容是空则返回空串
	 * @return
	 */
	public static String getExtensionNameByBase64Img(String imgStr){
		if(StringUtils.isNotEmpty(imgStr)){
			int start = imgStr.indexOf(BASE64_IMG_HEADER_START) + 11;
			int end = imgStr.indexOf(BASE64_IMG_HEADER_END);
			if(start <= end){
				return imgStr.substring(start,end);
			}
		}
		return "";
	}

	/**
	 * 上传成功返回true，否则返回false;并且会自动关闭输出流
	 * @param fileStr
	 * @param out
	 * @return
	 */
	public static void saveBase64File(String fileStr,OutputStream out) throws IOException {
		//对字节数组字符串进行Base64解码并生成图片
		if (fileStr == null) {
			//图像数据为空
			throw new BaseException("文件内容不能为空！");
		}
		try {
			fileStr = getBase64ImgFileContent(fileStr);
			//Base64解码
			byte[] content = Base64Utils.deCode(fileStr);
			for(int i = 0 ;i < content.length;++i){
				if(content[i] < 0) {
					content[i] += 256;//调整异常数据
				}
			}
			//生成jpeg图片
			out.write(content);
			out.flush();
		}finally {
			if(out!=null){
				out.close();
			}
		}
	}

	/**
	 * 获取base64的图片的内容信息
	 * @param fileStr
	 * @return
	 */
	public static String getBase64ImgFileContent(String fileStr){
		int flag = fileStr.indexOf(",");
		if(flag <= 0){
			throw new BaseException("不是base64的图片文件");
		}
		return fileStr.substring(flag + 1);
	}
}
