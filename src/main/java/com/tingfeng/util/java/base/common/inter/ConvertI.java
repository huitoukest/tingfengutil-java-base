package com.tingfeng.util.java.base.common.inter;

import java.util.function.Function;

/**
 *  ConvertI<T,S> 将传入的一个对象E转换为指定对象T
 * @author huitoukest
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface ConvertI<T,R> extends Function<T,R> {
}
