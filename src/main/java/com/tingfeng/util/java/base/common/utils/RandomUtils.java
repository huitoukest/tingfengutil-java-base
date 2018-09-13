package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.constant.RandomType;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 生成随机数/随机字符串的一个工具类
 * @author huitoukest
 * @version 20180912
 */
public class RandomUtils {
	/**
	 * 用来保存类型和char数组的对应关系
	 */
	private static final Map<String,char[]> RANDOM_TYPE_MAP = new HashMap<>();

	private static char[] getRandomValue(RandomType ... randomTypes){
		String key = ArrayUtils.join(randomTypes);
		synchronized (RANDOM_TYPE_MAP) {
			char[] value = RANDOM_TYPE_MAP.get(key);
			if (value == null) {
				List<String> strList = Stream.of(randomTypes).map(it -> it.getStrValue()).collect(Collectors.toList());
				String strValue = CollectionUtils.join(strList,"");
				value = strValue.toCharArray();
				RANDOM_TYPE_MAP.put(key,value);
			}
			return value;
		}
	}

	/**
	 * 返回一个定长的随机字符串(包含字母和数字)
	 * @param length 指定长度
	 * @return
	 */
	public static String randomString(int length){
		   return randomString(length,RandomType.number,RandomType.lowerChar,RandomType.upperChar);
	}

	/**
	 * 返回一个定长的随机字符串(包含RandomType中指定的字符)
	 *
	 * @param length
	 *            随机字符串长度
	 * @return 随机字符串
	 */
	public static String randomString(int length,RandomType ... randomTypes) {
		char[] chars = getRandomValue(randomTypes);
		int arrayLength = chars.length;
		return StringUtils.doAppend(sb->{
				for (int i = 0; i < length; i++) {
					sb.append(chars[ThreadLocalRandom.current().nextInt(arrayLength)]);
				}
				return sb.toString();
		});
	}

	/**
	 * 返回一个定长的随机纯字母字符串(只包含大小写字母)
	 *
	 * @param length
	 *            随机字符串长度
	 * @return 随机字符串
	 */
	public static String randomLetterString(int length) {
		return randomString(length,RandomType.lowerChar,RandomType.upperChar);
	}

	/**
	 * 返回一个定长的随机纯大写字母字符串(只包含大写字母)
	 *
	 * @param length
	 *            随机字符串长度
	 * @return 随机字符串
	 */
	public static String randomUpperString(int length) {
		return randomString(length,RandomType.upperChar);
	}

	/**
	 * 返回一个定长的随机纯小写字母字符串(只包含小写字母)
	 *
	 * @param length
	 *            随机字符串长度
	 * @return 随机字符串
	 */
	public static String randomLowerString(int length) {
		return randomString(length,RandomType.lowerChar);
	}

	/**
	 * 返回一个定长的随机数字字符串
	 *
	 * @param length
	 *            随机字符串长度
	 * @return 随机字符串
	 */
	public static String randomNumberString(int length) {
		return randomString(length,RandomType.number);
	}

	/**
	 * @param origin the least value returned
	 * @param bound the upper bound (exclusive)
	 * @return 返回大于等于origin，小于bound的值
	 */
	public static double  randomDouble(double origin,double bound){
		return  ThreadLocalRandom.current().nextDouble(origin,bound);
	}

	/**
	 * @param origin the least value returned
	 * @param bound the upper bound (exclusive)
	 * @return 返回大于等于origin，小于bound的值
	 */
	public static int randomInt(int origin,int bound){
		return  ThreadLocalRandom.current().nextInt(origin,bound);
	}

	/**
	 * @param bound the upper bound (exclusive)
	 * @return 返回小于bound的值
	 */
	public static long  randomLong(long bound){
		return  ThreadLocalRandom.current().nextLong(bound);
	}

	/**
	 * @param bound the upper bound (exclusive)
	 * @return 返回小于bound的值
	 */
	public static int  randomInt(int bound){
		return  ThreadLocalRandom.current().nextInt(bound);
	}
	/**
	 * @param origin the least value returned
	 * @param bound the upper bound (exclusive)
	 * @return 返回大于等于origin，小于bound的值
	 */
	public static long  randomLong(long origin,long bound){
		return ThreadLocalRandom.current().nextLong(origin,bound);
	}

	/**
	 * 产生一个Long范围内的随机正数
	 * @return
	 */
	public static long randomLong(){
		return ThreadLocalRandom.current().nextLong();
	}

	/**
	 * 产生一个int范围内的随机正数
	 * @return
	 */
	public static int randomInt(){
		return ThreadLocalRandom.current().nextInt();
	}

	/**
	 * 产生一个double范围内的随机正数
	 * @return
	 */
	public static double randomDouble(){
		return ThreadLocalRandom.current().nextDouble();
	}

	/**
	 * 产生一个float范围内的随机正数
	 * @return
	 */
	public static float randomFloat(){
		return ThreadLocalRandom.current().nextFloat();
	}

	/**
	 * 返回长度是128位的UUid随机字符串
	 * @return
	 */
	public static String randomUUid(){
		return randomUUid(false);
	}
	/**
	 * 返回没有连接符的的UUid随机字符串
	 * @param  removeSplit 是否去掉分隔符
	 * @return
	 */
	public static String randomUUid(boolean removeSplit){
		UUID uuid = UUID.randomUUID();
		String value = uuid.toString();
		if(removeSplit){
			value = value.replace("-", "");
		}
		return value;
	}
}
