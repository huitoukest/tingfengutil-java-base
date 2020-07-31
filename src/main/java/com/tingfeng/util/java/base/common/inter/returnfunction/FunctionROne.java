package com.tingfeng.util.java.base.common.inter.returnfunction;

/**
 * 以v结尾表示void，没有返回值，以r结尾表示result，有返回值
 * @author huitoukest
 * @param <R> 返回的结果类型
 * @param <P1> 所有以p开头的表示是参数，是params的简写
 */
@FunctionalInterface
public interface FunctionROne<R,P1> {
    /**
     *
     * @param p1  px表示第x个参数,即params x
     * @return R类型对象
     */
    R run(P1 p1);

}
