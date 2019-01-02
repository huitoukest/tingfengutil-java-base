package com.tingfeng.util.java.base.common.bean.tuple;

/**
 * 长度为4的元组
 * @param <A>
 * @param <B>
 * @param <C>
 * @param <D>
 */
public class Tuple4<A,B,C,D> extends Tuple3<A,B,C> {
    private D _4;

    public Tuple4(A _1, B _2, C _3, D _4) {
        super(_1, _2, _3);
        this._4 = _4;
    }

    public Tuple4(){}

    public D get_4() {
        return _4;
    }

    public Tuple4<A, B, C, D> set_4(D _4) {
        this._4 = _4;
        return this;
    }
}
