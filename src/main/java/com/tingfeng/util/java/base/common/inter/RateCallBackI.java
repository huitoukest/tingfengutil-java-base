package com.tingfeng.util.java.base.common.inter;

/**
 * 任何操作的进度回调接口
 */
@FunctionalInterface
public interface RateCallBackI {
	/**
	 * @param rate 当前操作的进度回调用;
	 *        rate从0~100,表示百分比进度;
	 */
	void updateRate(double rate);
}
