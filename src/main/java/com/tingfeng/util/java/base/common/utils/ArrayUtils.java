package com.tingfeng.util.java.base.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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
		if(array==null) {
			return false;
		}
		// 转换为list
		List<T> tempList = Arrays.asList(array);
		// 利用list的包含方法,进行判断
		if (tempList.contains(t)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 转换一个数组的类型
	 * @param array
	 * @param cls
	 * @param convertI
	 * @param <T>
	 * @return
	 */
	public static <T> T[] getArray(Object[] array,Class<T> cls,ConvertI<Object,T> convertI){
		T[] objs = (T[]) Array.newInstance(cls, array.length);
		for(int i = 0 ; i < array.length ; i++){
			objs[i] = convertI.apply(array[i]);
		}
		return objs;
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
		if (a == null) {
			return "";
		}
		int iMax = a.length - 1;
		if (iMax == -1) {
			return "";
		}
		StringBuilder b = new StringBuilder();
		for (int i = 0; ; i++) {
			b.append(a[i]);
			if (i == iMax) {
				return b.toString();
			}
			b.append(symbol);
		}
	}

	/**
	 * 数组a中的内容 用英语逗号分隔连接为字符串
	 * @param a
	 * @return
	 */
	public static String join(Object[] a) {
		return join(a, Constants.Symbol.comma);
	}

	/**
	 * 将多个srcArray 拷贝到target中
	 * @param target
	 * @param srcArray List<Object[]>
	 */
    public static void concatArray(Object[] target,List<Object[]> srcArray) {
		int startPosition = 0;
		for(int i = 0 ; i < srcArray.size() ; i++){
			System.arraycopy(target,startPosition,srcArray.get(i),0,srcArray.get(i).length);
			startPosition += srcArray.get(i).length;
		}
	}

    public static void reverse(Object[] arrays) {
    	if(arrays != null && arrays.length > 1){
    		int flag = arrays.length / 2;
    		int maxIndex = arrays.length -  1;
    		for(int i = 0 ; i <  flag; i ++){
    			Object arr = arrays[i];
    			int tmpIndex = maxIndex - i;
				arrays[i] = arrays[tmpIndex];
				arrays[tmpIndex] = arr;
			}
		}
    }

	public static void reverse(char[] arrays) {
		if(arrays != null && arrays.length > 1){
			int flag = arrays.length / 2;
			int maxIndex = arrays.length -  1;
			for(int i = 0 ; i <  flag; i ++){
				char arr = arrays[i];
				int tmpIndex = maxIndex - i;
				arrays[i] = arrays[tmpIndex];
				arrays[tmpIndex] = arr;
			}
		}
	}

    public static void reverse(int[] arrays) {
		if(arrays != null && arrays.length > 1){
			int flag = arrays.length / 2;
			int maxIndex = arrays.length -  1;
			for(int i = 0 ; i <  flag; i ++){
				int arr = arrays[i];
				int tmpIndex = maxIndex - i;
				arrays[i] = arrays[tmpIndex];
				arrays[tmpIndex] = arr;
			}
		}
	}

	public static void reverse(long[] arrays) {
		if(arrays != null && arrays.length > 1){
			int flag = arrays.length / 2;
			int maxIndex = arrays.length -  1;
			for(int i = 0 ; i <  flag; i ++){
				long arr = arrays[i];
				int tmpIndex = maxIndex - i;
				arrays[i] = arrays[tmpIndex];
				arrays[tmpIndex] = arr;
			}
		}
	}

	public static void reverse(float[] arrays) {
		if(arrays != null && arrays.length > 1){
			int flag = arrays.length / 2;
			int maxIndex = arrays.length -  1;
			for(int i = 0 ; i <  flag; i ++){
				float arr = arrays[i];
				int tmpIndex = maxIndex - i;
				arrays[i] = arrays[tmpIndex];
				arrays[tmpIndex] = arr;
			}
		}
	}

	public static void reverse(double[] arrays) {
		if(arrays != null && arrays.length > 1){
			int flag = arrays.length / 2;
			int maxIndex = arrays.length -  1;
			for(int i = 0 ; i <  flag; i ++){
				double arr = arrays[i];
				int tmpIndex = maxIndex - i;
				arrays[i] = arrays[tmpIndex];
				arrays[tmpIndex] = arr;
			}
		}
	}

	/**
	 * 获取数组的第一个值，如果没有则返回null
	 * @param array
	 * @param <T>
	 * @return
	 */
	public static <T> T getFirst(T[] array){
    	if(array == null || array.length < 1){
    		return null;
		}
		return array[0];
	}

	/**
	 * 获取数组的最后一个个值，如果没有则返回null
	 * @param array
	 * @param <T>
	 * @return
	 */
	public static <T> T getLast(T[] array){
		if(array == null || array.length < 1){
			return null;
		}
		return array[array.length - 1];
	}
}
