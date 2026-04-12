package com.tingfeng.util.java.base.common.inter.voidfunction;

/**
 * 以v结尾表示void，没有返回值，以r结尾表示result，有返回值
 * @author huitoukest
 * @param <P1> px表示第x个参数,即params x
 * @param <P2> px表示第x个参数,即params x
 */
@FunctionalInterface
public interface FunctionVTwo<P1,P2> {
    /**
     * 运行指定逻辑
     * @param p1 px表示第x个参数,即params x , 对应类型Px
     * @param p2 px表示第x个参数,即params x , 对应类型Px
     */
    void run(P1 p1, P2 p2);
}
