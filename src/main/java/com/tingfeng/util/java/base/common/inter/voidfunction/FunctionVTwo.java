package com.tingfeng.util.java.base.common.inter.voidfunction;

/**
 * 以v结尾表示void，没有返回值，以r结尾表示result，有返回值
 * @author huitoukest
 * 
 */
@FunctionalInterface
public interface FunctionVTwo<P1,P2> {

    public void run(P1 p1, P2 p2) throws Exception;

}
