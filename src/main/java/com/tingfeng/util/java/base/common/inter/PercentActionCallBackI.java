package com.tingfeng.util.java.base.common.inter;

/**
 * 任何操作的百分比进度回调接口
 * @author huitoukest
 * @param <T> 操作成功后传入的对象
 */
public interface PercentActionCallBackI<T> extends RateCallBackI{
	/**
	 * 操作完毕之后的回调方法
	 * @param t T类型
	 */
	 void actionSuccess(T t);
	/**
	 * 操作失败后会调用此函数,传入失败的原因
	 * @param e Throwable
	 */
	 void actionFailed(Throwable e);
}
