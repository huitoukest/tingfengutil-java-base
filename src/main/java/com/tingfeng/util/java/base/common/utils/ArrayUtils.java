package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.constant.Constants;
import com.tingfeng.util.java.base.common.inter.ConvertI;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Objects;

/**
 * 操作数组的一些工具;
 * @author huitoukest
 */
public class ArrayUtils {
	final static int BUFFER_SIZE = 1024;

	/**
	 * 当前数组是否包含对象T
	 * @param array 数组
	 * @param t 对象实例
	 * @return 是否包含
	 * @param <T>
	 */
	public static <T> boolean isContain(T[] array, T t) {
		if(array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (Objects.equals(array[i],t)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 转换一个数组的类型
	 * @param array
	 * @param cls
	 * @param convertI
	 * @param <T>
	 * @return
	 */
	public static <T,S> T[] getArray(S[] array,Class<T> cls,ConvertI<S,T> convertI){
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
	 * 将多个srcArray 拷贝到target中
	 * @param target
	 * @param srcArray List[Object[]]
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

	/**
	 * 打乱一个array
	 * 使用洗牌算法
	 * @param array 来源数据
	 * @param shuffleCount 打乱的次数,默认为  array.length
	 */
	public static void shuffle(int[] array,int shuffleCount) {
		for (int i = 0; i < shuffleCount; i++) {
			int exchangeIndex = RandomUtils.randomInt(i, array.length);
			int tmp = array[i];
			array[i] = array[exchangeIndex];
			array[exchangeIndex] = tmp;
		}
	}

	/**
	 * 打乱一个array
	 * 使用洗牌算法
	 * @param array 来源数据
	 * @return 打乱顺序的新array
	 */
	public static void shuffle(int[] array) {
		 shuffle(array, array.length);
	}

	/**
	 * 打乱一个array
	 * 使用洗牌算法
	 * @param array 来源数据
	 * @param shuffleCount 打乱的次数,默认为  array.length
	 */
	public static void shuffle(long[] array,int shuffleCount) {
		for (int i = 0; i < shuffleCount; i++) {
			int exchangeIndex = RandomUtils.randomInt(i, array.length);
			long tmp = array[i];
			array[i] = array[exchangeIndex];
			array[exchangeIndex] = tmp;
		}
	}

	/**
	 * 打乱一个array
	 * 使用洗牌算法
	 * @param array 来源数据
	 * @return 打乱顺序的新array
	 */
	public static void shuffle(long[] array) {
		shuffle(array, array.length);
	}

	/**
	 * 打乱一个array
	 * 使用洗牌算法
	 * @param array 来源数据
	 * @param shuffleCount 打乱的次数,默认为  array.length
	 */
	public static void shuffle(char[] array,int shuffleCount) {
		for (int i = 0; i < shuffleCount; i++) {
			int exchangeIndex = RandomUtils.randomInt(i, array.length);
			char tmp = array[i];
			array[i] = array[exchangeIndex];
			array[exchangeIndex] = tmp;
		}
	}

	/**
	 * 打乱一个array
	 * 使用洗牌算法
	 * @param array 来源数据
	 * @return 打乱顺序的新array
	 */
	public static void shuffle(char[] array) {
		shuffle(array, array.length);
	}

	/**
	 * 打乱一个array
	 * 使用洗牌算法
	 * @param array 来源数据
	 * @param shuffleCount 打乱的次数,默认为  array.length
	 */
	public static void shuffle(boolean[] array,int shuffleCount) {
		for (int i = 0; i < shuffleCount; i++) {
			int exchangeIndex = RandomUtils.randomInt(i, array.length);
			boolean tmp = array[i];
			array[i] = array[exchangeIndex];
			array[exchangeIndex] = tmp;
		}
	}

	/**
	 * 打乱一个array
	 * 使用洗牌算法
	 * @param array 来源数据
	 * @return 打乱顺序的新array
	 */
	public static void shuffle(boolean[] array) {
		shuffle(array, array.length);
	}

	/**
	 * 打乱一个array
	 * 使用洗牌算法
	 * @param array 来源数据
	 * @param shuffleCount 打乱的次数,默认为  array.length
	 */
	public static void shuffle(byte[] array,int shuffleCount) {
		for (int i = 0; i < shuffleCount; i++) {
			int exchangeIndex = RandomUtils.randomInt(i, array.length);
			byte tmp = array[i];
			array[i] = array[exchangeIndex];
			array[exchangeIndex] = tmp;
		}
	}

	/**
	 * 打乱一个array
	 * 使用洗牌算法
	 * @param array 来源数据
	 * @return 打乱顺序的新array
	 */
	public static void shuffle(byte[] array) {
		shuffle(array, array.length);
	}

	/**
	 * 打乱一个array
	 * 使用洗牌算法
	 * @param array 来源数据
	 * @param shuffleCount 打乱的次数,默认为  array.length
	 */
	public static <T> void shuffle(T[] array,int shuffleCount) {
		for (int i = 0; i < shuffleCount; i++) {
			int exchangeIndex = RandomUtils.randomInt(i, array.length);
			T tmp = array[i];
			array[i] = array[exchangeIndex];
			array[exchangeIndex] = tmp;
		}
	}

	/**
	 * 打乱一个array
	 * 使用洗牌算法
	 * @param array 来源数据
	 * @return 打乱顺序的新array
	 */
	public static <T> void shuffle(T[] array) {
		shuffle(array, array.length);
	}

	/**
	 * 之用指定分隔符连接数组数据
	 * @param array
	 * @param splitStr
	 * @return 如果数组为 null则返回null,否则返回连接后的字符串
	 */
	public static String join(byte[] array,String splitStr) {
		if(array == null){
			return null;
		}
		return StringUtils.doAppend(sb -> {
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					sb.append(splitStr);
				}
				sb.append(array[i]);
			}
			return sb.toString();
		});
	}

	/**
	 * 之用指定分隔符连接数组数据
	 * @param array
	 * @return 如果数组为 null则返回null,否则返回连接后的字符串
	 */
	public static <T> String join(T[] array) {
		return join(array, Constants.Symbol.comma);
	}

	/**
	 * 之用指定分隔符连接数组数据
	 * @param array
	 * @param splitStr
	 * @return 如果数组为 null则返回null,否则返回连接后的字符串
	 */
	public static String join(int[] array,String splitStr) {
		if(array == null){
			return null;
		}
		return StringUtils.doAppend(sb -> {
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					sb.append(splitStr);
				}
				sb.append(array[i]);
			}
			return sb.toString();
		});
	}
	/**
	 * 之用指定分隔符连接数组数据
	 * @param array
	 * @param splitStr
	 * @return 如果数组为 null则返回null,否则返回连接后的字符串
	 */
	public static String join(long[] array,String splitStr) {
		if(array == null){
			return null;
		}
		return StringUtils.doAppend(sb -> {
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					sb.append(splitStr);
				}
				sb.append(array[i]);
			}
			return sb.toString();
		});
	}
	/**
	 * 之用指定分隔符连接数组数据
	 * @param array
	 * @param splitStr
	 * @return 如果数组为 null则返回null,否则返回连接后的字符串
	 */
	public static String join(boolean[] array,String splitStr) {
		if(array == null){
			return null;
		}
		return StringUtils.doAppend(sb -> {
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					sb.append(splitStr);
				}
				sb.append(array[i]);
			}
			return sb.toString();
		});
	}
	/**
	 * 之用指定分隔符连接数组数据
	 * @param array
	 * @param splitStr
	 * @return 如果数组为 null则返回null,否则返回连接后的字符串
	 */
	public static String join(char[] array,String splitStr) {
		if(array == null){
			return null;
		}
		return StringUtils.doAppend(sb -> {
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					sb.append(splitStr);
				}
				sb.append(array[i]);
			}
			return sb.toString();
		});
	}
	/**
	 * 之用指定分隔符连接数组数据
	 * @param array
	 * @param splitStr
	 * @return 如果数组为 null则返回null,否则返回连接后的字符串
	 */
	public static <T> String join(T[] array,String splitStr) {
		if(array == null){
			return null;
		}
		return StringUtils.doAppend(sb -> {
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					sb.append(splitStr);
				}
				sb.append(array[i]);
			}
			return sb.toString();
		});
	}

	/**
	 * 使用逗号连接字符串
	 * @param array 数组
	 * @return 如果数组为 null则返回null,否则逗号连接后的字符串
	 */
	public static String toString(byte[] array) {
		return join(array, Constants.Symbol.comma);
	}
	/**
	 * 使用逗号连接字符串
	 * @param array 数组
	 * @return 如果数组为 null则返回null,否则逗号连接后的字符串
	 */
	public static String toString(int[] array) {
		return join(array, Constants.Symbol.comma);
	}
	/**
	 * 使用逗号连接字符串
	 * @param array 数组
	 * @return 如果数组为 null则返回null,否则逗号连接后的字符串
	 */
	public static String toString(long[] array) {
		return join(array, Constants.Symbol.comma);
	}
	/**
	 * 使用逗号连接字符串
	 * @param array 数组
	 * @return 如果数组为 null则返回null,否则逗号连接后的字符串
	 */
	public static String toString(boolean[] array) {
		return join(array, Constants.Symbol.comma);
	}
	/**
	 * 使用逗号连接字符串
	 * @param array 数组
	 * @return 如果数组为 null则返回null,否则逗号连接后的字符串
	 */
	public static String toString(char[] array) {
		return join(array, Constants.Symbol.comma);
	}
	/**
	 * 使用逗号连接字符串
	 * @param array 数组
	 * @return 如果数组为 null则返回null,否则逗号连接后的字符串
	 */
	public static <T> String toString(T[] array) {
		return join(array, Constants.Symbol.comma);
	}


}
