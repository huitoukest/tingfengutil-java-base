package com.tingfeng.util.java.base.common.inter.returnfunction;

/**
 * 以v结尾表示void，没有返回值，以r结尾表示result，有返回值
 * @author huitoukest
 * @param <R> 返回类型
 * @param <P1> px表示第x个参数,即params x
 * @param <P2> px表示第x个参数,即params x
 * @param <P3> px表示第x个参数,即params x
 * @param <P4> px表示第x个参数,即params x
 * @param <P5> px表示第x个参数,即params x
 * @param <P6> px表示第x个参数,即params x
 * @param <P7> px表示第x个参数,即params x
 * @param <P8> px表示第x个参数,即params x
 */
@FunctionalInterface
public interface FunctionREight<R,P1,P2,P3,P4,P5,P6,P7,P8> {
     /**
      *
      * @param p1 px表示第x个参数,即params x
      * @param p2 px表示第x个参数,即params x
      * @param p3 px表示第x个参数,即params x
      * @param p4 px表示第x个参数,即params x
      * @param p5 px表示第x个参数,即params x
      * @param p6 px表示第x个参数,即params x
      * @param p7 px表示第x个参数,即params x
      * @param p8 px表示第x个参数,即params x
      * @return 返回R类型数据
      */
     R run(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8);

}
