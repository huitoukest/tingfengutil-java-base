package com.tingfeng.util.java.base.common.inter.consumer;

/**
 * consumer function
 * @author huitoukest
 * 
 */
@FunctionalInterface
public interface ConsumerTwo<P1,P2> {
    /**
     * consumer these data
     * @param p1
     * @param p2
     */
    void accept(P1 p1, P2 p2);
}