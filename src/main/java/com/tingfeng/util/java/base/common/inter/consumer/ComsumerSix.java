package com.tingfeng.util.java.base.common.inter.consumer;

/**
 * consumer function
 * @author huitoukest
 * 
 */
@FunctionalInterface
public interface ComsumerSix<P1,P2,P3,P4,P5,P6> {
    /**
     * consumer these data
     * @param p1
     * @param p2
     * @param p3
     * @param p4
     * @param p5
     * @param p6
     */
    void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6) ;

}
