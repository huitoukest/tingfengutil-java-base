package com.tingfeng.util.java.base.array;

import com.tingfeng.util.java.base.common.constant.Constants;
import com.tingfeng.util.java.base.lang.base.ConvertI;
import com.tingfeng.util.java.base.lang.StringUtils;
import com.tingfeng.util.java.base.math.RandomUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

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
	 * 使用循环遍历进行比较，支持null值判断
	 * @param array 要搜索的数组
	 * @param t 要查找的元素
	 * @param <T> 数组元素类型
	 * @return 如果数组包含指定元素返回true，否则返回false；数组为null时返回false
	 */
	public static <T> boolean contains(T[] array, T t) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (Objects.equals(array[i], t)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断数组是否包含指定元素（byte类型）
	 * @param array 要搜索的数组
	 * @param t 要查找的元素
	 * @return 如果数组包含指定元素返回true，否则返回false
	 */
	public static boolean contains(byte[] array, byte t) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] == t) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断数组是否包含指定元素（int类型）
	 * @param array 要搜索的数组
	 * @param t 要查找的元素
	 * @return 如果数组包含指定元素返回true，否则返回false
	 */
	public static boolean contains(int[] array, int t) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] == t) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断数组是否包含指定元素（long类型）
	 * @param array 要搜索的数组
	 * @param t 要查找的元素
	 * @return 如果数组包含指定元素返回true，否则返回false
	 */
	public static boolean contains(long[] array, long t) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] == t) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断数组是否包含指定元素（char类型）
	 * @param array 要搜索的数组
	 * @param t 要查找的元素
	 * @return 如果数组包含指定元素返回true，否则返回false
	 */
	public static boolean contains(char[] array, char t) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] == t) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断数组是否包含指定元素（short类型）
	 * @param array 要搜索的数组
	 * @param t 要查找的元素
	 * @return 如果数组包含指定元素返回true，否则返回false
	 */
	public static boolean contains(short[] array, short t) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] == t) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断数组是否包含指定元素（boolean类型）
	 * @param array 要搜索的数组
	 * @param t 要查找的元素
	 * @return 如果数组包含指定元素返回true，否则返回false
	 */
	public static boolean contains(boolean[] array, boolean t) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] == t) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断数组是否包含指定元素（double类型）
	 * @param array 要搜索的数组
	 * @param t 要查找的元素
	 * @return 如果数组包含指定元素返回true，否则返回false
	 */
	public static boolean contains(double[] array, double t) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (Double.compare(array[i], t) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断数组是否包含指定元素（float类型）
	 * @param array 要搜索的数组
	 * @param t 要查找的元素
	 * @return 如果数组包含指定元素返回true，否则返回false
	 */
	public static boolean contains(float[] array, float t) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (Float.compare(array[i], t) == 0) {
				return true;
			}
		}
		return false;
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
	 * 反转数组 - 使用双指针法进行原地反转
	 * 通用内部实现
	 */
	private static void reverseInternal(Object array) {
		int length = java.lang.reflect.Array.getLength(array);
		if (length <= 1) {
			return;
		}
		int left = 0;
		int right = length - 1;
		while (left < right) {
			Object temp = java.lang.reflect.Array.get(array, left);
			java.lang.reflect.Array.set(array, left, java.lang.reflect.Array.get(array, right));
			java.lang.reflect.Array.set(array, right, temp);
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
	 * 反转泛型数组
	 * 使用双指针法进行原地反转
	 * @param arrays 要反转的数组
	 * @param <T> 数组元素类型
	 */
	public static <T> void reverse(T[] arrays) {
		if (arrays == null || arrays.length <= 1) {
			return;
		}
		int left = 0;
		int right = arrays.length - 1;
		while (left < right) {
			T temp = arrays[left];
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
	 * 使用指定分隔符连接double数组
	 * @param array double数组
	 * @param splitStr 分隔符
	 * @return 连接后的字符串，数组为null时返回null
	 */
	public static String join(double[] array, String splitStr) {
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
	 * 使用指定分隔符连接float数组
	 * @param array float数组
	 * @param splitStr 分隔符
	 * @return 连接后的字符串，数组为null时返回null
	 */
	public static String join(float[] array, String splitStr) {
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
	 * 使用指定分隔符连接short数组
	 * @param array short数组
	 * @param splitStr 分隔符
	 * @return 连接后的字符串，数组为null时返回null
	 */
	public static String join(short[] array, String splitStr) {
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

	// ==================== 新增方法 ====================

	/**
	 * 判断数组是否为空（null或长度为0）
	 * @param array 要检查的数组
	 * @param <T> 数组元素类型
	 * @return 如果数组为null或长度为0返回true，否则返回false
	 */
	public static <T> boolean isEmpty(T[] array) {
		return array == null || array.length == 0;
	}

	/**
	 * 判断数组是否非空
	 * @param array 要检查的数组
	 * @param <T> 数组元素类型
	 * @return 如果数组不为null且长度大于0返回true，否则返回false
	 */
	public static <T> boolean isNotEmpty(T[] array) {
		return !isEmpty(array);
	}

	/**
	 * 查找元素在数组中的索引位置
	 * @param array 要搜索的数组
	 * @param t 要查找的元素
	 * @param <T> 数组元素类型
	 * @return 元素所在索引，未找到返回-1；数组为null时返回-1
	 */
	public static <T> int indexOf(T[] array, T t) {
		if (array == null) {
			return -1;
		}
		for (int i = 0; i < array.length; i++) {
			if (Objects.equals(array[i], t)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 从后向前查找元素在数组中的索引位置
	 * @param array 要搜索的数组
	 * @param t 要查找的元素
	 * @param <T> 数组元素类型
	 * @return 元素所在索引，未找到返回-1；数组为null时返回-1
	 */
	public static <T> int lastIndexOf(T[] array, T t) {
		if (array == null) {
			return -1;
		}
		for (int i = array.length - 1; i >= 0; i--) {
			if (Objects.equals(array[i], t)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 查找元素在数组中的索引位置（byte类型）
	 */
	public static int indexOf(byte[] array, byte t) {
		if (array == null) {
			return -1;
		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] == t) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 查找元素在数组中的索引位置（int类型）
	 */
	public static int indexOf(int[] array, int t) {
		if (array == null) {
			return -1;
		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] == t) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 查找元素在数组中的索引位置（long类型）
	 */
	public static int indexOf(long[] array, long t) {
		if (array == null) {
			return -1;
		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] == t) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 查找元素在数组中的索引位置（char类型）
	 */
	public static int indexOf(char[] array, char t) {
		if (array == null) {
			return -1;
		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] == t) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 查找元素在数组中的索引位置（short类型）
	 */
	public static int indexOf(short[] array, short t) {
		if (array == null) {
			return -1;
		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] == t) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 查找元素在数组中的索引位置（double类型）
	 */
	public static int indexOf(double[] array, double t) {
		if (array == null) {
			return -1;
		}
		for (int i = 0; i < array.length; i++) {
			if (Double.compare(array[i], t) == 0) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 查找元素在数组中的索引位置（float类型）
	 */
	public static int indexOf(float[] array, float t) {
		if (array == null) {
			return -1;
		}
		for (int i = 0; i < array.length; i++) {
			if (Float.compare(array[i], t) == 0) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 从后向前查找元素在数组中的索引位置（byte类型）
	 */
	public static int lastIndexOf(byte[] array, byte t) {
		if (array == null) {
			return -1;
		}
		for (int i = array.length - 1; i >= 0; i--) {
			if (array[i] == t) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 从后向前查找元素在数组中的索引位置（int类型）
	 */
	public static int lastIndexOf(int[] array, int t) {
		if (array == null) {
			return -1;
		}
		for (int i = array.length - 1; i >= 0; i--) {
			if (array[i] == t) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 从后向前查找元素在数组中的索引位置（long类型）
	 */
	public static int lastIndexOf(long[] array, long t) {
		if (array == null) {
			return -1;
		}
		for (int i = array.length - 1; i >= 0; i--) {
			if (array[i] == t) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 从后向前查找元素在数组中的索引位置（char类型）
	 */
	public static int lastIndexOf(char[] array, char t) {
		if (array == null) {
			return -1;
		}
		for (int i = array.length - 1; i >= 0; i--) {
			if (array[i] == t) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 从后向前查找元素在数组中的索引位置（short类型）
	 */
	public static int lastIndexOf(short[] array, short t) {
		if (array == null) {
			return -1;
		}
		for (int i = array.length - 1; i >= 0; i--) {
			if (array[i] == t) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 从后向前查找元素在数组中的索引位置（double类型）
	 */
	public static int lastIndexOf(double[] array, double t) {
		if (array == null) {
			return -1;
		}
		for (int i = array.length - 1; i >= 0; i--) {
			if (Double.compare(array[i], t) == 0) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 从后向前查找元素在数组中的索引位置（float类型）
	 */
	public static int lastIndexOf(float[] array, float t) {
		if (array == null) {
			return -1;
		}
		for (int i = array.length - 1; i >= 0; i--) {
			if (Float.compare(array[i], t) == 0) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 按条件过滤数组元素
	 * @param array 源数组
	 * @param predicate 过滤条件
	 * @param <T> 数组元素类型
	 * @return 过滤后的新数组，数组为null时返回null
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] filter(T[] array, Predicate<? super T> predicate) {
		if (array == null || predicate == null) {
			return null;
		}
		return Arrays.stream(array).filter(predicate).toArray(i -> (T[]) Array.newInstance(array.getClass().getComponentType(), i));
	}

	/**
	 * 是否有任意元素满足条件
	 * @param array 源数组
	 * @param predicate 条件
	 * @param <T> 数组元素类型
	 * @return 如果有任意元素满足条件返回true，数组为null或空时返回false
	 */
	public static <T> boolean anyMatch(T[] array, Predicate<? super T> predicate) {
		if (array == null || predicate == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (predicate.test(array[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 所有元素是否都满足条件
	 * @param array 源数组
	 * @param predicate 条件
	 * @param <T> 数组元素类型
	 * @return 如果所有元素都满足条件返回true，数组为null或空时返回true
	 */
	public static <T> boolean allMatch(T[] array, Predicate<? super T> predicate) {
		if (array == null || predicate == null) {
			return true;
		}
		for (int i = 0; i < array.length; i++) {
			if (!predicate.test(array[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否有任意元素满足条件（int类型）
	 */
	public static boolean anyMatch(int[] array, IntPredicate predicate) {
		if (array == null || predicate == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (predicate.test(array[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 所有元素是否都满足条件（int类型）
	 */
	public static boolean allMatch(int[] array, IntPredicate predicate) {
		if (array == null || predicate == null) {
			return true;
		}
		for (int i = 0; i < array.length; i++) {
			if (!predicate.test(array[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否有任意元素满足条件（long类型）
	 */
	public static boolean anyMatch(long[] array, LongPredicate predicate) {
		if (array == null || predicate == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (predicate.test(array[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 所有元素是否都满足条件（long类型）
	 */
	public static boolean allMatch(long[] array, LongPredicate predicate) {
		if (array == null || predicate == null) {
			return true;
		}
		for (int i = 0; i < array.length; i++) {
			if (!predicate.test(array[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否有任意元素满足条件（double类型）
	 */
	public static boolean anyMatch(double[] array, DoublePredicate predicate) {
		if (array == null || predicate == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (predicate.test(array[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 所有元素是否都满足条件（double类型）
	 */
	public static boolean allMatch(double[] array, DoublePredicate predicate) {
		if (array == null || predicate == null) {
			return true;
		}
		for (int i = 0; i < array.length; i++) {
			if (!predicate.test(array[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 对数组元素进行映射转换
	 * @param array 源数组
	 * @param mapper 转换函数
	 * @param <S> 源数组元素类型
	 * @param <T> 目标数组元素类型
	 * @return 转换后的新数组，源数组为null时返回null
	 */
	public static <S, T> T[] map(S[] array, Function<? super S, ? extends T> mapper, IntFunction<T[]> arrayFactory) {
		if (array == null || mapper == null) {
			return null;
		}
		return Arrays.stream(array).map(mapper).toArray(arrayFactory);
	}

	/**
	 * 对数组元素进行映射转换（使用组件类型创建目标数组）
	 * @param array 源数组
	 * @param mapper 转换函数
	 * @param targetComponentType 目标数组的组件类型
	 * @param <S> 源数组元素类型
	 * @param <T> 目标数组元素类型
	 * @return 转换后的新数组，源数组为null时返回null
	 */
	@SuppressWarnings("unchecked")
	public static <S, T> T[] map(S[] array, Function<? super S, ? extends T> mapper, Class<T> targetComponentType) {
		if (array == null || mapper == null) {
			return null;
		}
		return Arrays.stream(array).map(mapper).toArray(i -> (T[]) Array.newInstance(targetComponentType, i));
	}

	/**
	 * 规范化子数组边界
	 * @param length 数组长度
	 * @param start 起始索引（包含）
	 * @param end 结束索引（不包含）
	 * @return int[]{规范化后的start, 规范化后的end}，如果start>=end则返回null
	 */
	private static int[] normalizeSubArrayRange(int length, int start, int end) {
		if (start < 0) {
			start = 0;
		}
		if (end > length) {
			end = length;
		}
		if (start >= end) {
			return null;
		}
		return new int[]{start, end};
	}

	/**
	 * 切分子数组
	 * @param array 源数组
	 * @param start 起始索引（包含）
	 * @param end 结束索引（不包含）
	 * @param <T> 数组元素类型
	 * @return 子数组，参数非法时返回null
	 */
	public static <T> T[] subArray(T[] array, int start, int end) {
		if (array == null) {
			return null;
		}
		int[] range = normalizeSubArrayRange(array.length, start, end);
		if (range == null) {
			return (T[]) Array.newInstance(array.getClass().getComponentType(), 0);
		}
		return Arrays.copyOfRange(array, range[0], range[1]);
	}

	/**
	 * 切分int数组
	 */
	public static int[] subArray(int[] array, int start, int end) {
		if (array == null) {
			return null;
		}
		int[] range = normalizeSubArrayRange(array.length, start, end);
		if (range == null) {
			return new int[0];
		}
		return Arrays.copyOfRange(array, range[0], range[1]);
	}

	/**
	 * 切分long数组
	 */
	public static long[] subArray(long[] array, int start, int end) {
		if (array == null) {
			return null;
		}
		int[] range = normalizeSubArrayRange(array.length, start, end);
		if (range == null) {
			return new long[0];
		}
		return Arrays.copyOfRange(array, range[0], range[1]);
	}

	/**
	 * 切分byte数组
	 */
	public static byte[] subArray(byte[] array, int start, int end) {
		if (array == null) {
			return null;
		}
		int[] range = normalizeSubArrayRange(array.length, start, end);
		if (range == null) {
			return new byte[0];
		}
		return Arrays.copyOfRange(array, range[0], range[1]);
	}

	/**
	 * 切分char数组
	 */
	public static char[] subArray(char[] array, int start, int end) {
		if (array == null) {
			return null;
		}
		int[] range = normalizeSubArrayRange(array.length, start, end);
		if (range == null) {
			return new char[0];
		}
		return Arrays.copyOfRange(array, range[0], range[1]);
	}

	/**
	 * 切分double数组
	 */
	public static double[] subArray(double[] array, int start, int end) {
		if (array == null) {
			return null;
		}
		int[] range = normalizeSubArrayRange(array.length, start, end);
		if (range == null) {
			return new double[0];
		}
		return Arrays.copyOfRange(array, range[0], range[1]);
	}

	/**
	 * 切分float数组
	 */
	public static float[] subArray(float[] array, int start, int end) {
		if (array == null) {
			return null;
		}
		int[] range = normalizeSubArrayRange(array.length, start, end);
		if (range == null) {
			return new float[0];
		}
		return Arrays.copyOfRange(array, range[0], range[1]);
	}

	/**
	 * 切分boolean数组
	 */
	public static boolean[] subArray(boolean[] array, int start, int end) {
		if (array == null) {
			return null;
		}
		int[] range = normalizeSubArrayRange(array.length, start, end);
		if (range == null) {
			return new boolean[0];
		}
		return Arrays.copyOfRange(array, range[0], range[1]);
	}

	/**
	 * 切分short数组
	 */
	public static short[] subArray(short[] array, int start, int end) {
		if (array == null) {
			return null;
		}
		int[] range = normalizeSubArrayRange(array.length, start, end);
		if (range == null) {
			return new short[0];
		}
		return Arrays.copyOfRange(array, range[0], range[1]);
	}

	/**
	 * 包装类型数组转基础类型数组
	 */
	public static boolean[] unwrapper(Boolean[] array) {
		if (array == null) {
			return null;
		}
		boolean[] result = new boolean[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i] != null ? array[i] : false;
		}
		return result;
	}

	/**
	 * 包装类型数组转基础类型数组
	 */
	public static char[] unwrapper(Character[] array) {
		if (array == null) {
			return null;
		}
		char[] result = new char[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i] != null ? array[i] : '\0';
		}
		return result;
	}

	/**
	 * 包装类型数组转基础类型数组
	 */
	public static byte[] unwrapper(Byte[] array) {
		if (array == null) {
			return null;
		}
		byte[] result = new byte[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i] != null ? array[i] : 0;
		}
		return result;
	}

	/**
	 * 包装类型数组转基础类型数组
	 */
	public static short[] unwrapper(Short[] array) {
		if (array == null) {
			return null;
		}
		short[] result = new short[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i] != null ? array[i] : 0;
		}
		return result;
	}

	/**
	 * 包装类型数组转基础类型数组
	 */
	public static int[] unwrapper(Integer[] array) {
		if (array == null) {
			return null;
		}
		int[] result = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i] != null ? array[i] : 0;
		}
		return result;
	}

	/**
	 * 包装类型数组转基础类型数组
	 */
	public static long[] unwrapper(Long[] array) {
		if (array == null) {
			return null;
		}
		long[] result = new long[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i] != null ? array[i] : 0L;
		}
		return result;
	}

	/**
	 * 包装类型数组转基础类型数组
	 */
	public static float[] unwrapper(Float[] array) {
		if (array == null) {
			return null;
		}
		float[] result = new float[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i] != null ? array[i] : 0f;
		}
		return result;
	}

	/**
	 * 包装类型数组转基础类型数组
	 */
	public static double[] unwrapper(Double[] array) {
		if (array == null) {
			return null;
		}
		double[] result = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i] != null ? array[i] : 0d;
		}
		return result;
	}

	/**
	 * 基础类型数组转包装类型数组
	 */
	public static Boolean[] wrapper(boolean[] array) {
		if (array == null) {
			return null;
		}
		Boolean[] result = new Boolean[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * 基础类型数组转包装类型数组
	 */
	public static Character[] wrapper(char[] array) {
		if (array == null) {
			return null;
		}
		Character[] result = new Character[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * 基础类型数组转包装类型数组
	 */
	public static Byte[] wrapper(byte[] array) {
		if (array == null) {
			return null;
		}
		Byte[] result = new Byte[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * 基础类型数组转包装类型数组
	 */
	public static Short[] wrapper(short[] array) {
		if (array == null) {
			return null;
		}
		Short[] result = new Short[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * 基础类型数组转包装类型数组
	 */
	public static Integer[] wrapper(int[] array) {
		if (array == null) {
			return null;
		}
		Integer[] result = new Integer[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * 基础类型数组转包装类型数组
	 */
	public static Long[] wrapper(long[] array) {
		if (array == null) {
			return null;
		}
		Long[] result = new Long[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * 基础类型数组转包装类型数组
	 */
	public static Float[] wrapper(float[] array) {
		if (array == null) {
			return null;
		}
		Float[] result = new Float[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * 基础类型数组转包装类型数组
	 */
	public static Double[] wrapper(double[] array) {
		if (array == null) {
			return null;
		}
		Double[] result = new Double[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	// ==================== 统计方法 ====================

	/**
	 * int数组求和
	 */
	public static long sum(int[] array) {
		if (array == null || array.length == 0) {
			return 0L;
		}
		long sum = 0;
		for (int i = 0; i < array.length; i++) {
			sum += array[i];
		}
		return sum;
	}

	/**
	 * long数组求和
	 */
	public static long sum(long[] array) {
		if (array == null || array.length == 0) {
			return 0L;
		}
		long sum = 0;
		for (int i = 0; i < array.length; i++) {
			sum += array[i];
		}
		return sum;
	}

	/**
	 * double数组求和
	 */
	public static double sum(double[] array) {
		if (array == null || array.length == 0) {
			return 0d;
		}
		double sum = 0d;
		for (int i = 0; i < array.length; i++) {
			sum += array[i];
		}
		return sum;
	}

	/**
	 * float数组求和
	 */
	public static double sum(float[] array) {
		if (array == null || array.length == 0) {
			return 0d;
		}
		double sum = 0d;
		for (int i = 0; i < array.length; i++) {
			sum += array[i];
		}
		return sum;
	}

	/**
	 * int数组求平均值
	 */
	public static double average(int[] array) {
		if (array == null || array.length == 0) {
			return 0d;
		}
		return (double) sum(array) / array.length;
	}

	/**
	 * long数组求平均值
	 */
	public static double average(long[] array) {
		if (array == null || array.length == 0) {
			return 0d;
		}
		return (double) sum(array) / array.length;
	}

	/**
	 * double数组求平均值
	 */
	public static double average(double[] array) {
		if (array == null || array.length == 0) {
			return 0d;
		}
		return sum(array) / array.length;
	}

	/**
	 * float数组求平均值
	 */
	public static double average(float[] array) {
		if (array == null || array.length == 0) {
			return 0d;
		}
		return sum(array) / array.length;
	}

	/**
	 * 检查数组所有元素是否都不为 null
	 */
    public static  <T>  boolean allNonNull(T[] array) {
        if (array == null) return false;
        for (T t : array) {
            if (t == null) return false;
        }
        return true;
    }

    // ==================== 数组转 List/Set ====================

    /**
     * 泛型数组转 List
     */
    public static <T> List<T> toList(T[] array) {
        if (array == null) {
            return null;
        }
        List<T> list = new java.util.ArrayList<>(array.length);
        for (T t : array) {
            list.add(t);
        }
        return list;
    }

    /**
     * 泛型数组转 Set
     */
    public static <T> Set<T> toSet(T[] array) {
        if (array == null) {
            return null;
        }
        Set<T> set = new HashSet<>(array.length);
        for (T t : array) {
            set.add(t);
        }
        return set;
    }

    /**
     * int 数组转 List
     */
    public static List<Integer> toList(int[] array) {
        if (array == null) {
            return null;
        }
        List<Integer> list = new java.util.ArrayList<>(array.length);
        for (int v : array) {
            list.add(v);
        }
        return list;
    }

    /**
     * int 数组转 Set
     */
    public static Set<Integer> toSet(int[] array) {
        if (array == null) {
            return null;
        }
        Set<Integer> set = new HashSet<>(array.length);
        for (int v : array) {
            set.add(v);
        }
        return set;
    }

    /**
     * long 数组转 List
     */
    public static List<Long> toList(long[] array) {
        if (array == null) {
            return null;
        }
        List<Long> list = new java.util.ArrayList<>(array.length);
        for (long v : array) {
            list.add(v);
        }
        return list;
    }

    /**
     * long 数组转 Set
     */
    public static Set<Long> toSet(long[] array) {
        if (array == null) {
            return null;
        }
        Set<Long> set = new HashSet<>(array.length);
        for (long v : array) {
            set.add(v);
        }
        return set;
    }

    /**
     * double 数组转 List
     */
    public static List<Double> toList(double[] array) {
        if (array == null) {
            return null;
        }
        List<Double> list = new java.util.ArrayList<>(array.length);
        for (double v : array) {
            list.add(v);
        }
        return list;
    }

    /**
     * double 数组转 Set
     */
    public static Set<Double> toSet(double[] array) {
        if (array == null) {
            return null;
        }
        Set<Double> set = new HashSet<>(array.length);
        for (double v : array) {
            set.add(v);
        }
        return set;
    }

    /**
     * float 数组转 List
     */
    public static List<Float> toList(float[] array) {
        if (array == null) {
            return null;
        }
        List<Float> list = new java.util.ArrayList<>(array.length);
        for (float v : array) {
            list.add(v);
        }
        return list;
    }

    /**
     * float 数组转 Set
     */
    public static Set<Float> toSet(float[] array) {
        if (array == null) {
            return null;
        }
        Set<Float> set = new HashSet<>(array.length);
        for (float v : array) {
            set.add(v);
        }
        return set;
    }

    /**
     * short 数组转 List
     */
    public static List<Short> toList(short[] array) {
        if (array == null) {
            return null;
        }
        List<Short> list = new java.util.ArrayList<>(array.length);
        for (short v : array) {
            list.add(v);
        }
        return list;
    }

    /**
     * short 数组转 Set
     */
    public static Set<Short> toSet(short[] array) {
        if (array == null) {
            return null;
        }
        Set<Short> set = new HashSet<>(array.length);
        for (short v : array) {
            set.add(v);
        }
        return set;
    }

    /**
     * byte 数组转 List
     */
    public static List<Byte> toList(byte[] array) {
        if (array == null) {
            return null;
        }
        List<Byte> list = new java.util.ArrayList<>(array.length);
        for (byte v : array) {
            list.add(v);
        }
        return list;
    }

    /**
     * byte 数组转 Set
     */
    public static Set<Byte> toSet(byte[] array) {
        if (array == null) {
            return null;
        }
        Set<Byte> set = new HashSet<>(array.length);
        for (byte v : array) {
            set.add(v);
        }
        return set;
    }

    /**
     * boolean 数组转 List
     */
    public static List<Boolean> toList(boolean[] array) {
        if (array == null) {
            return null;
        }
        List<Boolean> list = new java.util.ArrayList<>(array.length);
        for (boolean v : array) {
            list.add(v);
        }
        return list;
    }

    /**
     * boolean 数组转 Set
     */
    public static Set<Boolean> toSet(boolean[] array) {
        if (array == null) {
            return null;
        }
        Set<Boolean> set = new HashSet<>(array.length);
        for (boolean v : array) {
            set.add(v);
        }
        return set;
    }

    /**
     * char 数组转 List
     */
    public static List<Character> toList(char[] array) {
        if (array == null) {
            return null;
        }
        List<Character> list = new java.util.ArrayList<>(array.length);
        for (char v : array) {
            list.add(v);
        }
        return list;
    }

    /**
     * char 数组转 Set
     */
    public static Set<Character> toSet(char[] array) {
        if (array == null) {
            return null;
        }
        Set<Character> set = new HashSet<>(array.length);
        for (char v : array) {
            set.add(v);
        }
        return set;
    }
}