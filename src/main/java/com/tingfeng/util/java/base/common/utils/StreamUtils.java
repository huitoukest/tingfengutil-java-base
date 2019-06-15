package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.exception.io.StreamCloseException;
import com.tingfeng.util.java.base.common.inter.PercentActionCallBackI;

import java.io.*;
import java.util.function.Consumer;

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

	/**
	 * 将输出流中的内容outputStream拷贝到inputStream中，分批拷贝，默认每次4096 字节
	 * @param outputStream
	 * @param inputStream
	 */
	public static void copy(OutputStream outputStream, InputStream inputStream){
		copy(outputStream,inputStream,BUFFER_SIZE,true,null);
	}

	/**
	 * 将输出流中的内容outputStream拷贝到inputStream中，分批拷贝，缓存的size是
	 * @param outputStream
	 * @param inputStream
	 * @param bufferSize
	 */
	public static void copy(OutputStream outputStream, InputStream inputStream, int bufferSize){
		copy(outputStream,inputStream,bufferSize,true,null);
	}
	/**
	 * 将输出流中的内容outputStream拷贝到inputStream中，分批拷贝，缓存的size是
	 * @param outputStream
	 * @param inputStream
	 * @param bufferSize
	 * @param closeStream 使用完毕之后是否关闭流
	 * @param readSizeCallBack 回调，并传入当前已经读取的字节数
	 */
	public static void copy(OutputStream outputStream, InputStream inputStream, int bufferSize,boolean closeStream, Consumer<Integer> readSizeCallBack){
		try{
			int perReadLength = 0;
			byte[] buffer = new byte[bufferSize];
			// 循环读写中,读取的字节的总数量
			int sumReadSize = 0;
			/* 从文件读取数据到缓冲区 */
			while ((perReadLength = inputStream.read(buffer)) != -1) {
				/* 将数据写入DataOutputStream中 */
				outputStream.write(buffer, 0, perReadLength);
				if(null != readSizeCallBack) {
					sumReadSize += perReadLength;
					readSizeCallBack.accept(sumReadSize);
				}
			}
		}catch (IOException e){
			throw new com.tingfeng.util.java.base.common.exception.io.IOException(e);
		}finally {
			try {
				if (closeStream && inputStream != null) {
					inputStream.close();
				}
				if (closeStream && outputStream != null) {
					outputStream.flush();
					outputStream.close();
				}
			}catch (Throwable e){
				throw new StreamCloseException("close stream error",e);
			}
		}
	}
}