package com.tingfeng.util.java.base.common.inter.consumer;

/**
 * consumer function
 * @author huitoukest
 * @param <P1> Px表示第x个参数,即params x
 * @param <P2> Px表示第x个参数,即params x
 * @param <P3> Px表示第x个参数,即params x
 * @param <P4> Px表示第x个参数,即params x
 */
@FunctionalInterface
public interface ConsumerFour<P1,P2,P3,P4> {
    /**
     * consumer these data
     * @param p1 px表示第x个参数,即params x , 对应类型Px
     * @param p2 px表示第x个参数,即params x , 对应类型Px
     * @param p3 px表示第x个参数,即params x , 对应类型Px
     * @param p4 px表示第x个参数,即params x , 对应类型Px
     */
    void accept(P1 p1, P2 p2, P3 p3, P4 p4) ;

}
