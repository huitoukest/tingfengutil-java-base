package com.tingfeng.util.java.base.common.inter.voidfunction;

/**
 * 以v结尾表示void，没有返回值，以r结尾表示result，有返回值
 * @author huitoukest
 * @param <P1> px表示第x个参数,即params x
 * @param <P2> px表示第x个参数,即params x
 * @param <P3> px表示第x个参数,即params x
 * @param <P4> px表示第x个参数,即params x
 */
@FunctionalInterface
public interface FunctionVFour<P1,P2,P3,P4> {
    /**
     * 运行指定逻辑
     * @param p1 px表示第x个参数,即params x , 对应类型Px
     * @param p2 px表示第x个参数,即params x , 对应类型Px
     * @param p3 px表示第x个参数,即params x , 对应类型Px
     * @param p4 px表示第x个参数,即params x , 对应类型Px
     */
    void run(P1 p1, P2 p2, P3 p3, P4 p4) ;

}
