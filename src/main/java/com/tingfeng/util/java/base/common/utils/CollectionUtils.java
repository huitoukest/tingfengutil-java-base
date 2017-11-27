package com.tingfeng.util.java.base.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.FormatFlagsConversionMismatchException;
import java.util.List;

import com.tingfeng.util.java.base.common.inter.ConvertI;

public class CollectionUtils {
	
	public static <T> List<T> getListByArray(T[] array){
		if(array==null) return null;
		return Arrays.asList(array);
	}
	
	public static <T> List<T> getObjectList(String souceString,String regex,ConvertI<T,String> convert){
		List<T> list=new ArrayList<T>();
		souceString=souceString.trim();
		if(souceString.length()<1) return list;
		try {
			String[] ss=souceString.split(regex);
			for(String s:ss){
				T t=null;
				try {
					t=convert.convert(s);
				} catch (FormatFlagsConversionMismatchException e) {					
				}								
				list.add(t);
			}
		} catch (Exception e) {			
		}		
		return list;	
	}
	
	/**
	 * @param souceString
	 * @param regex 分隔标志
	 * @return
	 */
	public static List<String> getStringList(String souceString,String regex){
		return getObjectList(souceString, regex, new ConvertI<String, String>() {
			@Override
			public String convert(String e) {
				return e;
			}
		});		
	}
	/**
	 * 默认以逗号作为分割
	 * @param souceString
	 * @return
	 */
	public static List<String> getStringList(String souceString){
		return getStringList(souceString, ",");
	}
	
	/**
	 * @param souceString
	 * @param regex 分隔标志
	 * @return
	 */
	public static List<Integer> getIntegerList(String souceString,String regex){
		return getObjectList(souceString, regex, new ConvertI<Integer, String>() {
			@Override
			public Integer convert(String e) {
				return Integer.parseInt(e);
			}
		});	
	}
	public static List<Integer> getIntegerList(String souceString){
		return getIntegerList(souceString, ",");
	}
	
	/**
	 * @param souceString
	 * @param symbol 分隔标志
	 * @return
	 */
	public static List<Long> getLongList(String souceString,String regex){
		return getObjectList(souceString, regex, new ConvertI<Long, String>() {
			@Override
			public Long convert(String e) {
				return Long.parseLong(e);
			}
		});	
	}
	
	/**默认以逗号作为分隔符
	 * @param souceString
	 * @return 
	 */
	public static List<Long> getLongList(String souceString){
		return getLongList(souceString, ",");
	}

}
