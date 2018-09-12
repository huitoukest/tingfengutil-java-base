package com.tingfeng.util.java.base.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.tingfeng.util.java.base.common.constant.Constants;
import com.tingfeng.util.java.base.common.inter.ConvertI;

/**
 * 操作数组的一些工具;
 * @author huitoukest
 *@version20160517
 */
public class ArrayUtils {
	final static int BUFFER_SIZE = 1024;
	
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

	public static <T> T[] getArray(Object[] array,ConvertI<T,Object> convertI){
		Object[] objs = new Object[array.length];
		for(int i = 0 ; i < array.length ; i++){
			objs[i] = convertI.convert(array[i]);
		}
		return (T[])objs;
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
		byte[] data = new byte[BUFFER_SIZE];
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		try {
			int count = -1;
			while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
				outStream.write(data, 0, count);
			}
			return outStream.toByteArray();
		}finally {
			outStream.close();
		}
	}

	/**
	 * 把整形数组转换成以“,”相隔的字符串
	 *
	 * @param a 数组a
	 * @return 以“,”相隔的字符串
	 * @author fengliang
	 * @serialData 2008-01-08
	 */
	public static String join(Object[] a, String symbol) {
		if (a == null)
			return "";
		int iMax = a.length - 1;
		if (iMax == -1)
			return "";
		StringBuilder b = new StringBuilder();
		for (int i = 0; ; i++) {
			b.append(a[i]);
			if (i == iMax)
				return b.toString();
			b.append(symbol);
		}
	}

	public static String join(Object[] a) {
		return join(a, Constants.Symbol.comma);
	}
	
}
