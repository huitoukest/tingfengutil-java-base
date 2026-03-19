package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.constant.Constants;
import com.tingfeng.util.java.base.common.inter.ConvertI;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 操作数组的一些工具类
 * 提供数组常用操作方法，包括反转、打乱、连接、转换等功能
 * @author huitoukest
 */
public class ArrayUtils {
	/**
	 * 输入流缓冲区大小
	 */
	private static final int BUFFER_SIZE = 1024;

	/**
	 * 判断数组是否包含指定元素
	 * 使用Objects.equals进行比较，支持null值判断
	 * @param array 要搜索的数组
	 * @param t 要查找的元素
	 * @param <T> 数组元素类型
	 * @return 如果数组包含指定元素返回true，否则返回false；数组为null时返回false
	 */
	public static <T> boolean isContain(T[] array, T t) {
		if (array == null) {
			return false;
		}
		return Arrays.stream(array).anyMatch(element -> Objects.equals(element, t));
	}

	/**
	 * 转换数组类型
	 * 将源数组中的每个元素通过转换函数转换为目标类型
	 * @param array 源数组
	 * @param cls 目标数组元素类型
	 * @param convertI 转换函数
	 * @param <S> 源数组元素类型
	 * @param <T> 目标数组元素类型
	 * @return 转换后的新数组
	 */
	@SuppressWarnings("unchecked")
	public static <S, T> T[] getArray(S[] array, Class<T> cls, ConvertI<S, T> convertI) {
		if (array == null) {
			return (T[]) Array.newInstance(cls, 0);
		}
		T[] objs = (T[]) Array.newInstance(cls, array.length);
		for (int i = 0; i < array.length; i++) {
			objs[i] = convertI.apply(array[i]);
		}
		return objs;
	}

	/**
	 * 将InputStream转换为byte数组
	 * 使用try-with-resources确保资源正确关闭
	 * @param in 输入流
	 * @return 读取到的字节数组
	 * @throws IOException 读取过程中发生IO异常
	 */
	public static byte[] getBytesByInputStream(InputStream in) throws IOException {
		if (in == null) {
			return new byte[0];
		}
		try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
			byte[] data = new byte[BUFFER_SIZE];
			int count;
			while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
				outStream.write(data, 0, count);
			}
			return outStream.toByteArray();
		}
	}

	/**
	 * 将多个源数组合并到目标数组中
	 * 按照源数组的顺序依次拷贝到目标数组中
	 * @param target 目标数组，必须足够大以容纳所有源数组的元素
	 * @param srcArray 源数组列表
	 */
	public static void concatArray(Object[] target, List<Object[]> srcArray) {
		if (target == null || srcArray == null) {
			return;
		}
		int startPosition = 0;
		for (Object[] src : srcArray) {
			if (src != null && src.length > 0) {
				System.arraycopy(src, 0, target, startPosition, src.length);
				startPosition += src.length;
			}
		}
	}

	/**
	 * 反转Object数组
	 * 使用双指针法进行原地反转
	 * @param arrays 要反转的数组
	 */
	public static void reverse(Object[] arrays) {
		if (arrays == null || arrays.length <= 1) {
			return;
		}
		int left = 0;
		int right = arrays.length - 1;
		while (left < right) {
			Object temp = arrays[left];
			arrays[left] = arrays[right];
			arrays[right] = temp;
			left++;
			right--;
		}
	}

	/**
	 * 反转char数组
	 * 使用双指针法进行原地反转
	 * @param arrays 要反转的数组
	 */
	public static void reverse(char[] arrays) {
		if (arrays == null || arrays.length <= 1) {
			return;
		}
		int left = 0;
		int right = arrays.length - 1;
		while (left < right) {
			char temp = arrays[left];
			arrays[left] = arrays[right];
			arrays[right] = temp;
			left++;
			right--;
		}
	}

	/**
	 * 反转int数组
	 * 使用双指针法进行原地反转
	 * @param arrays 要反转的数组
	 */
	public static void reverse(int[] arrays) {
		if (arrays == null || arrays.length <= 1) {
			return;
		}
		int left = 0;
		int right = arrays.length - 1;
		while (left < right) {
			int temp = arrays[left];
			arrays[left] = arrays[right];
			arrays[right] = temp;
			left++;
			right--;
		}
	}

	/**
	 * 反转long数组
	 * 使用双指针法进行原地反转
	 * @param arrays 要反转的数组
	 */
	public static void reverse(long[] arrays) {
		if (arrays == null || arrays.length <= 1) {
			return;
		}
		int left = 0;
		int right = arrays.length - 1;
		while (left < right) {
			long temp = arrays[left];
			arrays[left] = arrays[right];
			arrays[right] = temp;
			left++;
			right--;
		}
	}

	/**
	 * 反转float数组
	 * 使用双指针法进行原地反转
	 * @param arrays 要反转的数组
	 */
	public static void reverse(float[] arrays) {
		if (arrays == null || arrays.length <= 1) {
			return;
		}
		int left = 0;
		int right = arrays.length - 1;
		while (left < right) {
			float temp = arrays[left];
			arrays[left] = arrays[right];
			arrays[right] = temp;
			left++;
			right--;
		}
	}

	/**
	 * 反转double数组
	 * 使用双指针法进行原地反转
	 * @param arrays 要反转的数组
	 */
	public static void reverse(double[] arrays) {
		if (arrays == null || arrays.length <= 1) {
			return;
		}
		int left = 0;
		int right = arrays.length - 1;
		while (left < right) {
			double temp = arrays[left];
			arrays[left] = arrays[right];
			arrays[right] = temp;
			left++;
			right--;
		}
	}

	/**
	 * 获取数组的第一个元素
	 * @param array 数组
	 * @param <T> 数组元素类型
	 * @return 数组第一个元素，如果数组为null或空则返回null
	 */
	public static <T> T getFirst(T[] array) {
		if (array == null || array.length < 1) {
			return null;
		}
		return array[0];
	}

	/**
	 * 获取数组的最后一个元素
	 * @param array 数组
	 * @param <T> 数组元素类型
	 * @return 数组最后一个元素，如果数组为null或空则返回null
	 */
	public static <T> T getLast(T[] array) {
		if (array == null || array.length < 1) {
			return null;
		}
		return array[array.length - 1];
	}

	/**
	 * 打乱int数组
	 * 使用洗牌算法
	 * @param array 要打乱的数组
	 * @param shuffleCount 打乱的次数,默认为array.length
	 */
	public static void shuffle(int[] array, int shuffleCount) {
		if (array == null || array.length <= 1) {
			return;
		}
		for (int i = 0; i < shuffleCount; i++) {
			int exchangeIndex = RandomUtils.randomInt(i, array.length);
			int tmp = array[i];
			array[i] = array[exchangeIndex];
			array[exchangeIndex] = tmp;
		}
	}

	/**
	 * 打乱int数组
	 * 使用洗牌算法
	 * @param array 要打乱的数组
	 */
	public static void shuffle(int[] array) {
		shuffle(array, array.length);
	}

	/**
	 * 打乱long数组
	 * 使用洗牌算法
	 * @param array 要打乱的数组
	 * @param shuffleCount 打乱的次数,默认为array.length
	 */
	public static void shuffle(long[] array, int shuffleCount) {
		if (array == null || array.length <= 1) {
			return;
		}
		for (int i = 0; i < shuffleCount; i++) {
			int exchangeIndex = RandomUtils.randomInt(i, array.length);
			long tmp = array[i];
			array[i] = array[exchangeIndex];
			array[exchangeIndex] = tmp;
		}
	}

	/**
	 * 打乱long数组
	 * 使用洗牌算法
	 * @param array 要打乱的数组
	 */
	public static void shuffle(long[] array) {
		shuffle(array, array.length);
	}

	/**
	 * 打乱char数组
	 * 使用洗牌算法
	 * @param array 要打乱的数组
	 * @param shuffleCount 打乱的次数,默认为array.length
	 */
	public static void shuffle(char[] array, int shuffleCount) {
		if (array == null || array.length <= 1) {
			return;
		}
		for (int i = 0; i < shuffleCount; i++) {
			int exchangeIndex = RandomUtils.randomInt(i, array.length);
			char tmp = array[i];
			array[i] = array[exchangeIndex];
			array[exchangeIndex] = tmp;
		}
	}

	/**
	 * 打乱char数组
	 * 使用洗牌算法
	 * @param array 要打乱的数组
	 */
	public static void shuffle(char[] array) {
		shuffle(array, array.length);
	}

	/**
	 * 打乱boolean数组
	 * 使用洗牌算法
	 * @param array 要打乱的数组
	 * @param shuffleCount 打乱的次数,默认为array.length
	 */
	public static void shuffle(boolean[] array, int shuffleCount) {
		if (array == null || array.length <= 1) {
			return;
		}
		for (int i = 0; i < shuffleCount; i++) {
			int exchangeIndex = RandomUtils.randomInt(i, array.length);
			boolean tmp = array[i];
			array[i] = array[exchangeIndex];
			array[exchangeIndex] = tmp;
		}
	}

	/**
	 * 打乱boolean数组
	 * 使用洗牌算法
	 * @param array 要打乱的数组
	 */
	public static void shuffle(boolean[] array) {
		shuffle(array, array.length);
	}

	/**
	 * 打乱byte数组
	 * 使用洗牌算法
	 * @param array 要打乱的数组
	 * @param shuffleCount 打乱的次数,默认为array.length
	 */
	public static void shuffle(byte[] array, int shuffleCount) {
		if (array == null || array.length <= 1) {
			return;
		}
		for (int i = 0; i < shuffleCount; i++) {
			int exchangeIndex = RandomUtils.randomInt(i, array.length);
			byte tmp = array[i];
			array[i] = array[exchangeIndex];
			array[exchangeIndex] = tmp;
		}
	}

	/**
	 * 打乱byte数组
	 * 使用洗牌算法
	 * @param array 要打乱的数组
	 */
	public static void shuffle(byte[] array) {
		shuffle(array, array.length);
	}

	/**
	 * 打乱泛型数组
	 * 使用洗牌算法
	 * @param array 要打乱的数组
	 * @param shuffleCount 打乱的次数,默认为array.length
	 * @param <T> 数组元素类型
	 */
	public static <T> void shuffle(T[] array, int shuffleCount) {
		if (array == null || array.length <= 1) {
			return;
		}
		for (int i = 0; i < shuffleCount; i++) {
			int exchangeIndex = RandomUtils.randomInt(i, array.length);
			T tmp = array[i];
			array[i] = array[exchangeIndex];
			array[exchangeIndex] = tmp;
		}
	}

	/**
	 * 打乱泛型数组
	 * 使用洗牌算法
	 * @param array 要打乱的数组
	 * @param <T> 数组元素类型
	 */
	public static <T> void shuffle(T[] array) {
		shuffle(array, array.length);
	}

	/**
	 * 使用指定分隔符连接byte数组
	 * @param array byte数组
	 * @param splitStr 分隔符
	 * @return 连接后的字符串，数组为null时返回null
	 */
	public static String join(byte[] array, String splitStr) {
		if (array == null) {
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
	 * 使用默认分隔符（逗号）连接泛型数组
	 * @param array 泛型数组
	 * @param <T> 数组元素类型
	 * @return 连接后的字符串，数组为null时返回null
	 */
	public static <T> String join(T[] array) {
		return join(array, Constants.Symbol.comma);
	}

	/**
	 * 使用指定分隔符连接int数组
	 * @param array int数组
	 * @param splitStr 分隔符
	 * @return 连接后的字符串，数组为null时返回null
	 */
	public static String join(int[] array, String splitStr) {
		if (array == null) {
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
	 * 使用指定分隔符连接long数组
	 * @param array long数组
	 * @param splitStr 分隔符
	 * @return 连接后的字符串，数组为null时返回null
	 */
	public static String join(long[] array, String splitStr) {
		if (array == null) {
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
	 * 使用指定分隔符连接boolean数组
	 * @param array boolean数组
	 * @param splitStr 分隔符
	 * @return 连接后的字符串，数组为null时返回null
	 */
	public static String join(boolean[] array, String splitStr) {
		if (array == null) {
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
	 * 使用指定分隔符连接char数组
	 * @param array char数组
	 * @param splitStr 分隔符
	 * @return 连接后的字符串，数组为null时返回null
	 */
	public static String join(char[] array, String splitStr) {
		if (array == null) {
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
	 * 使用指定分隔符连接泛型数组
	 * @param array 泛型数组
	 * @param splitStr 分隔符
	 * @param <T> 数组元素类型
	 * @return 连接后的字符串，数组为null时返回null
	 */
	public static <T> String join(T[] array, String splitStr) {
		if (array == null) {
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
	 * 使用逗号连接byte数组
	 * @param array byte数组
	 * @return 逗号连接后的字符串，数组为null时返回null
	 */
	public static String toString(byte[] array) {
		return join(array, Constants.Symbol.comma);
	}

	/**
	 * 使用逗号连接int数组
	 * @param array int数组
	 * @return 逗号连接后的字符串，数组为null时返回null
	 */
	public static String toString(int[] array) {
		return join(array, Constants.Symbol.comma);
	}

	/**
	 * 使用逗号连接long数组
	 * @param array long数组
	 * @return 逗号连接后的字符串，数组为null时返回null
	 */
	public static String toString(long[] array) {
		return join(array, Constants.Symbol.comma);
	}

	/**
	 * 使用逗号连接boolean数组
	 * @param array boolean数组
	 * @return 逗号连接后的字符串，数组为null时返回null
	 */
	public static String toString(boolean[] array) {
		return join(array, Constants.Symbol.comma);
	}

	/**
	 * 使用逗号连接char数组
	 * @param array char数组
	 * @return 逗号连接后的字符串，数组为null时返回null
	 */
	public static String toString(char[] array) {
		return join(array, Constants.Symbol.comma);
	}

	/**
	 * 使用逗号连接泛型数组
	 * @param array 泛型数组
	 * @param <T> 数组元素类型
	 * @return 逗号连接后的字符串，数组为null时返回null
	 */
	public static <T> String toString(T[] array) {
		return join(array, Constants.Symbol.comma);
	}


}