package com.tingfeng.util.java.base.common.inter;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 作为读取bean 属性名称的一个 用途的 Function
 * @param <T>
 * @param <R>
 */
public interface PropertyFunction<T, R> extends Function<T, R>, Serializable {
}
