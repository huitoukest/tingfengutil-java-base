package com.tingfeng.util.java.base.common.inter;

/**
 *  ConvertI<T,S> 将传入的一个对象E转换为指定对象T
 * @author huitoukest
 *
 * @param  <T> Target 第一个参数，得到的对象和类型
 * @param <S>  Source 第二个参数，传入的对象和类型
 */
public interface ConvertI<T,S>{
	public T convert(S e);
}
