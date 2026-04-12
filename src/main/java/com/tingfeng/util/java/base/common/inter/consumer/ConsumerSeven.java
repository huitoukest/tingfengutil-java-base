package com.tingfeng.util.java.base.common.inter.consumer;

/**
 * consumer function
 * @author huitoukest
 * @param <P1> Px表示第x个参数,即params x
 * @param <P2> Px表示第x个参数,即params x
 * @param <P3> Px表示第x个参数,即params x
 * @param <P4> Px表示第x个参数,即params x
 * @param <P5> Px表示第x个参数,即params x
 * @param <P6> Px表示第x个参数,即params x
 * @param <P7> Px表示第x个参数,即params x
 */
@FunctionalInterface
public interface ConsumerSeven<P1,P2,P3,P4,P5,P6,P7> {
    /**
     * consumer these data
     * @param p1 px表示第x个参数,即params x , 对应类型Px
     * @param p2 px表示第x个参数,即params x , 对应类型Px
     * @param p3 px表示第x个参数,即params x , 对应类型Px
     * @param p4 px表示第x个参数,即params x , 对应类型Px
     * @param p5 px表示第x个参数,即params x , 对应类型Px
     * @param p6 px表示第x个参数,即params x , 对应类型Px
     * @param p7 px表示第x个参数,即params x , 对应类型Px
     */
    void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7) ;

}
