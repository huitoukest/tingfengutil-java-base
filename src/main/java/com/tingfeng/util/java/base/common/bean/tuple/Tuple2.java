package com.tingfeng.util.java.base.common.bean.tuple;

import java.util.Map;

/**
 * 长度为2的元组
 * @param <A>
 * @param <B>
 */
public class Tuple2<A,B> {
    private A _1;
    private B _2;

    public Tuple2(A _1, B _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public Tuple2(){}
    public static <K,V> Map.Entry<K,V> toEntry(Tuple2<K,V> tuple2){
        Map.Entry<K,V> entry = new Map.Entry<K,V>() {

            @Override
            public K getKey() {
                return tuple2._1;
            }

            @Override
            public V getValue() {
                return tuple2._2;
            }

            @Override
            public V setValue(V value) {
                tuple2._2 = value;
                return tuple2._2;
            }
        };
        return entry;
    }

    public A get_1() {
        return _1;
    }

    public Tuple2<A, B> set_1(A _1) {
        this._1 = _1;
        return this;
    }

    public B get_2() {
        return _2;
    }

    public Tuple2<A, B> set_2(B _2) {
        this._2 = _2;
        return this;
    }
}
