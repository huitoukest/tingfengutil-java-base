package com.tingfeng.util.java.base.common.inter;

public interface RateCallBackI {
	/**
	 * @param radio 当前操作的进度回调用;
	 *        radio从0~100,表示百分比进度;
	 */
	public void updateRate(double rate);
}
