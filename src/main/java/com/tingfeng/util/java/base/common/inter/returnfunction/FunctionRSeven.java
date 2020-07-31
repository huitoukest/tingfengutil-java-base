package com.tingfeng.util.java.base.common.inter.returnfunction;

/**
 * 以v结尾表示void，没有返回值，以r结尾表示result，有返回值
 * @author huitoukest
 * @param <R> 返回类型
 * @param <P1> Px表示第x个参数,即params x
 * @param <P2> Px表示第x个参数,即params x
 * @param <P3> Px表示第x个参数,即params x
 * @param <P4> Px表示第x个参数,即params x
 * @param <P5> Px表示第x个参数,即params x
 * @param <P6> Px表示第x个参数,即params x
 * @param <P7> Px表示第x个参数,即params x
 */
@FunctionalInterface
public interface FunctionRSeven<R,P1,P2,P3,P4,P5,P6,P7> {
    /**
     *
     * @param p1 px表示第x个参数,即params x
     * @param p2 px表示第x个参数,即params x
     * @param p3 px表示第x个参数,即params x
     * @param p4 px表示第x个参数,即params x
     * @param p5 px表示第x个参数,即params x
     * @param p6 px表示第x个参数,即params x
     * @param p7 px表示第x个参数,即params x
     * @return R类型对象
     */
    R run(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7)  ;

}
