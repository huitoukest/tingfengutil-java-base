package com.tingfeng.util.java.base.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.tingfeng.util.java.base.common.inter.ConvertI;

/**
 * 操作数组的一些工具;
 * @author huitoukest
 *@version20160517
 */
public class ArrayUtils {
	final static int BUFFER_SIZE=1024;
	
	public static <T> boolean isContain(T[] array, T t) {
		if(array==null) return false;
		// 转换为list
		List<T> tempList = Arrays.asList(array);
		// 利用list的包含方法,进行判断
		if (tempList.contains(t)) {
			return true;
		} else {
			return false;
		}
	}			
	@SuppressWarnings("unchecked")
	public static <T> T[] getArray(Object[] array,ConvertI<T,Object> convertI){
		Object[] objs=new Object[array.length];
		for(int i=0;i<array.length;i++){
			objs[i]=convertI.convert(array[i]);
		}
		return (T[])objs;
	}
	/**
	 * 判断一个值是否存在于一个数组;
	 * @param key
	 * @param values
	 * @return
	 */
	protected static boolean isExitedInArray(String key,String...values){
		if(key==null)
			return false;
		if(values==null)
			return false;
		for(String s:values){
			if(s.equals(key))
				return true;
		}
		return false;
	}
	
	/**
	 * 将InputStream转换成byte数组
	 * 
	 * @param in
	 *            InputStream
	 * @return byte[]
	 * @throws IOException
	 */
	public static byte[] getBytesByInputStream(InputStream in) throws IOException {

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] data = new byte[BUFFER_SIZE];
		int count = -1;
		while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
			outStream.write(data, 0, count);

		data = null;
		return outStream.toByteArray();
	}
	
}
