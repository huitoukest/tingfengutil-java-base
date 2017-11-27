package com.tingfeng.util.java.base.common.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * JDK代理类
 *
 */
public class ProxyByJDKUtils{	 
	 private ProxyByJDKUtils(){	
	 }
	 /**
	  * 
	  * @param interfaceClass 接口类
	  * @param proxyedObj 实现接口的的实例
	  * @return (实现接口的)代理方法
	  */
	 @SuppressWarnings("unchecked")
	public static <T,E extends T> T getProxy(Class<T> interfaceClass,final E proxyedObj,InvocationHandler invocationHandler){
		 Object proxy=Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
	                new Class[] { interfaceClass },invocationHandler); 
		 return (T) proxy;
	 }
}
