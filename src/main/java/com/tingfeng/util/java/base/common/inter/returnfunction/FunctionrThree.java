package com.tingfeng.util.java.base.common.inter.returnfunction;

/**
 * 以v结尾表示void，没有返回值，以r结尾表示result，有返回值
 * @author huitoukest
 * 
 */
@FunctionalInterface
public interface FunctionrThree<R,P1,P2,P3> {

    R run(P1 p1, P2 p2, P3 p3);

}
