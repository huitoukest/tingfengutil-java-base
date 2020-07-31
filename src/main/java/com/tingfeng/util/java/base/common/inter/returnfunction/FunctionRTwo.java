package com.tingfeng.util.java.base.common.inter.returnfunction;

/**
 * 以v结尾表示void，没有返回值，以r结尾表示result，有返回值
 * @author huitoukest
 *
 * @param <R> 返回类型
 * @param <P1> px表示第x个参数,即params x
 * @param <P2> px表示第x个参数,即params x
 */
@FunctionalInterface
public interface FunctionRTwo<R,P1,P2> {
    /**
     *
     * @param p1 px表示第x个参数,即params x
     * @param p2 px表示第x个参数,即params x
     * @return R类型对象
     */
    R run(P1 p1, P2 p2) ;

}
