package com.tingfeng.util.java.base.common.bean.tuple;

/**
 * 长度为3的元组
 * @param <A> 第一个元素, 对应属性 _1
 * @param <B> 第二个元素 ,对应属性 _2
 * @param <C> 第三个元素, 对应属性 _3
 */
public class Tuple3<A,B,C> extends Tuple2<A,B> {
    private C _3;

    public Tuple3(A _1, B _2, C _3) {
        super(_1, _2);
        this._3 = _3;
    }
    public Tuple3(){

    }

    public C get_3() {
        return _3;
    }

    public Tuple3<A, B, C> set_3(C _3) {
        this._3 = _3;
        return this;
    }
}
