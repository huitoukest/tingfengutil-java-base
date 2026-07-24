package com.tingfeng.util.java.base.file;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

import com.tingfeng.util.java.base.io.IOUtils;
import com.tingfeng.util.java.base.lang.base.Base64ConvertToStringI;
import com.tingfeng.util.java.base.lang.exception.StreamCloseException;

/**
 * 文件同步读写与行写入操作实现。
 * <p>包级私有，不对外暴露。通过 {@link FileUtils} 对外提供统一 API。</p>
 */
class FileRWOps {

	/**
	 * 默认行分隔符
	 */
	private static final String DEFAULT_LINE_SEPARATOR = "\n";

	// ==================== 文件读取 ====================

	/**
	 * 读取文件为字节数组
	 * @param file 文件
	 * @return 字节数组
	 */
	static byte[] readFileToByteArray(File file) {
		if (file == null || !file.exists()) {
			throw new com.tingfeng.util.java.base.lang.exception.FileNotFoundException(
				"File not found: " + file);
		}
		try (FileInputStream fis = new FileInputStream(file)) {
			return IOUtils.toByteArray(fis);
		} catch (IOException e) {
			throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
		}
	}

	/**
	 * 读取文件为字符串
	 * @param file 文件
	 * @param charset 字符编码
	 * @return 字符串
	 */
	static String readFileToString(File file, Charset charset) {
		if (file == null || !file.exists()) {
			throw new com.tingfeng.util.java.base.lang.exception.FileNotFoundException(
				"File not found: " + file);
		}
		try (FileInputStream fis = new FileInputStream(file)) {
			return IOUtils.toString(fis, charset);
		} catch (IOException e) {
			throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
		}
	}

	/**
	 * 读取文件为字符串（UTF-8）
	 * @param file 文件
	 * @return 字符串
	 */
	static String readFileToString(File file) {
		return readFileToString(file, StandardCharsets.UTF_8);
	}

	// ==================== 文件写入 ====================

	/**
	 * 将字节数组写入文件
	 * @param file 文件
	 * @param data 字节数组
	 */
	static void writeByteArrayToFile(File file, byte[] data) {
		if (file == null) {
			throw new IllegalArgumentException("File must not be null");
		}
		// 确保父目录存在
		File parent = file.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		try (FileOutputStream fos = new FileOutputStream(file)) {
			if (data != null && data.length > 0) {
				fos.write(data);
				fos.flush();
			}
		} catch (IOException e) {
			throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
		}
	}

	/**
	 * 将字符串写入文件
	 * @param file 文件
	 * @param content 字符串内容
	 * @param charset 字符编码
	 * @param append 是否追加
	 */
	static void writeStringToFile(File file, String content, Charset charset, boolean append) {
		if (file == null) {
			throw new IllegalArgumentException("File must not be null");
		}
		// 确保父目录存在
		File parent = file.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		try (FileOutputStream fos = new FileOutputStream(file, append)) {
			if (content != null && !content.isEmpty()) {
				fos.write(content.getBytes(charset));
				fos.flush();
			}
		} catch (IOException e) {
			throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
		}
	}

	/**
	 * 将字符串写入文件（UTF-8，覆盖模式）
	 * @param file 文件
	 * @param content 字符串内容
	 */
	static void writeStringToFile(File file, String content) {
		writeStringToFile(file, content, StandardCharsets.UTF_8, false);
	}

	// ==================== 旧版内容转换 ====================

	/**
	 * 将一个文件的内容读取出来,并转换成为字符串的方式来返回相应的内容
	 * @param file
	 * @param base64ConvertToStringI
	 * @return 文件对应的base64字符串
	 */
	static String transFileToString(File file, Base64ConvertToStringI base64ConvertToStringI) {
		if (file == null || !file.exists()) {
			return null;
		}
		String content = "";
		byte[] bs = new byte[FileUtils.BUFFER_SIZE];
		InputStream is = null;
		BufferedInputStream br = null;
		ByteArrayOutputStream bos = null;
		try {
			is = new FileInputStream(file);
			br = new BufferedInputStream(is);
			bos = new ByteArrayOutputStream();
			int readLength = 0;
			while ((readLength = is.read(bs)) != -1) {
				bos.write(bs, 0, readLength);
			}
			content = base64ConvertToStringI.convertToString(bos.toByteArray(), 0);
		} catch (IOException e) {
			throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
		} finally {
			try {
				if (bos != null) {
					bos.close();
				}
				if (br != null) {
					br.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
				throw new StreamCloseException(e);
			}
		}
		return content;
	}

	/**
	 * 将一个输入流转换成指定编码的字符串
	 * @param inputStream 输入流
	 * @param encode 编码
	 * @return 字符串
	 */
	static String transInputStreamToStringByEncode(InputStream inputStream, String encode) {
		return IOUtils.toString(inputStream, Charset.forName(encode));
	}

	// ==================== 行写入操作 ====================

	/**
	 * 追加一行字符串到文件（自动换行）
	 *
	 * @param file 文件（追加模式）
	 * @param line 行内容
	 */
	static void writeLine(File file, String line) {
		writeLine(file, line, StandardCharsets.UTF_8, true);
	}

	/**
	 * 追加一行字符串到文件
	 *
	 * @param file 文件
	 * @param line 行内容
	 * @param charset 字符编码
	 * @param append 是否追加，false则覆盖
	 */
	static void writeLine(File file, String line, Charset charset, boolean append) {
		if (file == null) {
			throw new IllegalArgumentException("File must not be null");
		}
		// 确保父目录存在
		File parent = file.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file, append), charset))) {
			if (line != null) {
				writer.write(line);
			}
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
		}
	}

	/**
	 * 追加一行字符串到文件（指定编码）
	 *
	 * @param file 文件（追加模式）
	 * @param line 行内容
	 * @param charset 字符编码
	 */
	static void writeLine(File file, String line, Charset charset) {
		writeLine(file, line, charset, true);
	}

	/**
	 * 追加多行字符串到文件
	 *
	 * @param file 文件（追加模式）
	 * @param lines 行列表
	 */
	static void writeLines(File file, List<String> lines) {
		writeLines(file, lines, StandardCharsets.UTF_8, true);
	}

	/**
	 * 追加多行字符串到文件
	 *
	 * @param file 文件
	 * @param lines 行列表
	 * @param charset 字符编码
	 * @param append 是否追加
	 */
	static void writeLines(File file, List<String> lines, Charset charset, boolean append) {
		if (file == null) {
			throw new IllegalArgumentException("File must not be null");
		}
		if (lines == null || lines.isEmpty()) {
			return;
		}
		// 确保父目录存在
		File parent = file.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		// 使用JDK NIO Files.write()高效批量写入
		try {
			java.nio.file.OpenOption[] options = append
				? new java.nio.file.OpenOption[]{
					StandardOpenOption.CREATE,
					StandardOpenOption.APPEND}
				: new java.nio.file.OpenOption[]{
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING};
			Files.write(file.toPath(), lines, charset, options);
		} catch (IOException e) {
			throw new com.tingfeng.util.java.base.lang.exception.IOException(e);
		}
	}
}
