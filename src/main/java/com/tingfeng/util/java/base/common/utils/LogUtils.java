package com.tingfeng.util.java.base.common.utils;

import org.slf4j.Logger;

/**
 * 
 * @author huitoukest
 * 主要用于工具集的日志,进入sl4j作为日志的记录框架
 */
public class LogUtils {
	/**
	 * 输出info级别的日志
	 * @param logger
	 * @param runnable
	 */
	public static void info(Logger logger,Runnable runnable){
		log(logger.isInfoEnabled(),runnable);
	}

	/**
	 * 通过条件输出日志
	 * @param isLog
	 * @param runnable
	 */
	public static void log(boolean isLog,Runnable runnable){
		if(isLog){
			runnable.run();
		}
	}

	/**
	 * warn级别的日志
	 * @param logger
	 * @param runnable
	 */
	public static void warn(Logger logger,Runnable runnable){
		log(logger.isWarnEnabled(),runnable);
	}

	/**
	 * debug级别的日志
	 * @param logger
	 * @param runnable
	 */
	public static void debug(Logger logger,Runnable runnable){
		log(logger.isDebugEnabled(),runnable);
	}

	/**
	 * error级别的日志
	 * @param logger
	 * @param runnable
	 */
	public static void error(Logger logger,Runnable runnable){
		log(logger.isErrorEnabled(),runnable);
	}
}
