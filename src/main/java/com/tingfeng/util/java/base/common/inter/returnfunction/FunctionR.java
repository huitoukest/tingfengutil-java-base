package com.tingfeng.util.java.base.common.inter.returnfunction;

import java.util.function.Supplier;

/**
 * 以v结尾表示void，没有返回值，以r结尾表示result，有返回值
 * @author huitoukest
 *
 */
@FunctionalInterface
public interface FunctionR<T> extends Supplier<T>  {
}