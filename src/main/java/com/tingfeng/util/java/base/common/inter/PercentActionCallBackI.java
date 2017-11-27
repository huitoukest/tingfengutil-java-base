package com.tingfeng.util.java.base.common.inter;

/**
 * 任何操作的百分经度回调接口
 * @author huitoukest
 *
 */
	public interface PercentActionCallBackI<T> extends RateCallBackI{
	/**
	 * 文件操作完毕之后的回调方法
	 * @param file
	 */
	public void actionSuccess(T t);
	/**
	 *操作失败后会调用此函数,传入下载失败的原因
	 * @param s
	 */
	public void actionFailed(Object o);
}
