package com.tingfeng.util.java.base.common.utils;

/**
 * 
 * @author huitoukest
 * 主要用于工具集的日志
 */
public class LogUtils {
	
	public static void info(Class<?> cls,Throwable throwable){
		System.out.print("info:\t"+cls.getName()+":");
		throwable.printStackTrace();
	}
	
	public static void info(Class<?> cls,String s){
		System.out.print("info:\t"+cls.getName()+": "+s);
	}
	
	public static void error(Class<?> cls,Throwable throwable){
		System.out.print("error:\t"+cls.getName()+":");
		throwable.printStackTrace();
	}
	
	public static void error(Class<?> cls,String s){
		System.out.print("error:\t"+cls.getName()+": "+s);
	}
}
