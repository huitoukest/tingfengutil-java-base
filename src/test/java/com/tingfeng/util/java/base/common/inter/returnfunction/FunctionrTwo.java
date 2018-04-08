package com.tingfeng.util.java.base.common.inter.returnfunction;

/**
 * 以v结尾表示void，没有返回值，以r结尾表示result，有返回值
 * @author huitoukest
 * 
 */
public interface FunctionrTwo<P1,P2> {

    public <T> T run(P1 p1,P2 p2);

}
