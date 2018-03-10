package com.tingfeng.util.java.base.common.utils;

import java.util.UUID;

/**
 * 生成随机数/随机字符串的一个工具类
 * @author huitoukest
 *
 */
public class RandomUtils {
	private static final double minPositionValue = 1E-308;

	public static double  getRandom(double min,double max){
		return  Math.random() * (max - min) + min;
	}

	public static int  getRandom(int min,int max){
		return  (int)( Math.random() * (max - min) + minPositionValue);
	}

	public static long  getRandom(long max){
		return  (long)( Math.random() * max + minPositionValue);
	}

	public static int  getRandom(int max){
		return  (int)( Math.random() * max + minPositionValue);
	}


	/**
	 * 产生一个Long范围内的随机正数
	 * @return
	 */
	public static Long getRandomLong(){
		return (long)(Math.random()*Long.MAX_VALUE);
	}
	
	/**
	 * 产生一个指定长度的数字字符串
	 * @return
	 */
	public static String getRandomNumberString(int length){
		StringBuffer sb=new StringBuffer();
		while(sb.length()<length){
			sb=sb.append(getRandomLong());
		}	
		return sb.substring(0, length);
	}
	
	/**
	 * 产生指定长度的一个随机字符串,利用UUid
	 * @param length
	 * @return
	 */
	public static String getRandomString(int length){
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<length;i++){
			sb.append(getUUidString());
			if(sb.length()>=length)
			{
				return sb.substring(0, length);
			}
		}
		return sb.toString();
	}
	/**
	 * 返回长度是128位的UUid随机字符串
	 * @return
	 */
	public static String getUUidString(){
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
	/**
	 * 返回没有连接符的的UUid随机字符串
	 * @return
	 */
	public static String getUUidStringNoSplite(){
		UUID uuid = UUID.randomUUID();
		return uuid.toString().replace("-", "");
	}
}
