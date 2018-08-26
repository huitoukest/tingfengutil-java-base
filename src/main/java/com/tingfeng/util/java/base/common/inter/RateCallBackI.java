package com.tingfeng.util.java.base.common.inter;

/**
 * 任何操作的进度回调接口
 */
public interface RateCallBackI {
	/**
	 * @param rate 当前操作的进度回调用;
	 *        rate从0~100,表示百分比进度;
	 */
	public void updateRate(double rate);
}
