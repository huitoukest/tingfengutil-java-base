package com.tingfeng.util.java.base.common.inter.consumer;

/**
 * consumer function
 * @author huitoukest
 * 
 */
@FunctionalInterface
public interface ComsumerThree<P1,P2,P3> {
    /**
     * consumer these data
     * @param p1
     * @param p2
     * @param p3
     */
    void run(P1 p1, P2 p2, P3 p3) ;

}
