package com.tingfeng.util.java.base.common.inter.voidfunction;

import java.util.function.Consumer;

/**
 * 以v结尾表示void，没有返回值，以r结尾表示result，有返回值
 * @author huitoukest
 *
 */
@FunctionalInterface
public interface FunctionVOne<P1> extends Consumer<P1> {

}