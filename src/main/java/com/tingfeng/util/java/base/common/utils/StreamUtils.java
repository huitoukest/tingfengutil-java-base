package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.exception.BaseException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class StreamUtils {

	final static int BUFFER_SIZE = 4096;
	/**
	 * 将String转换成InputStream
	 * 默认UTF-8编码
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public static InputStream getInputStreamByStream(String in) throws Exception {
		return getInputStreamByStream(in, "UTF-8");
	}
	
	public static InputStream getInputStreamByStream(String in,String charEncode) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes(charEncode));
		return is;
	}
	/**
	 * 将byte数组转换成InputStream
	 * 
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public static InputStream getInputStreamByBytes(byte[] in) throws Exception {

		ByteArrayInputStream is = new ByteArrayInputStream(in);
		return is;
	}
	/**
	 * 根据文件路径创建文件输入流处理
	 * 以字节为单位（非 unicode ）
	 * @param filepath
	 * @return
	 */
	public static FileInputStream getFileInputStream(String filepath) {
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(filepath);
		} catch (FileNotFoundException e) {
			throw new BaseException(e);
		}
		return fileInputStream;
	}
	/**
	 * 根据文件对象创建文件输入流处理
	 * 以字节为单位（非 unicode ）
	 * @param file
	 * @return
	 */
	public static FileInputStream getFileInputStream(File file) {
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new BaseException(e);
		}
		return fileInputStream;
	}
	/**
	  * 根据文件对象创建文件输出流处理
	 * 以字节为单位（非 unicode ）
	 * @param file
	 * @param append true:文件以追加方式打开,false:则覆盖原文件的内容
	 * @return
	 */
	public static FileOutputStream getFileOutputStream(File file,boolean append) {
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file,append);
		} catch (FileNotFoundException e) {
			throw new BaseException(e);
		}
		return fileOutputStream;
	}
	/**
	 * 根据文件路径创建文件输出流处理
	 * 以字节为单位（非 unicode ）
	 * @param filepath
	 * @param append true:文件以追加方式打开,false:则覆盖原文件的内容
	 * @return
	 */
	public static FileOutputStream getFileOutputStream(String filepath,boolean append) {
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(filepath,append);
		} catch (FileNotFoundException e) {
			throw new BaseException(e);
		}
		return fileOutputStream;
	}
	public static ByteArrayOutputStream getByteArrayOutputStream() {
		return new ByteArrayOutputStream();
	}

}