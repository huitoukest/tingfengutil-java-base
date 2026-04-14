package com.tingfeng.util.java.base.lang.base;

/**
 * Entry的K,V值
 * @author huitoukest
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class EntryBean<K, V> extends Tuple2<K, V> {

    public EntryBean() {
    }

    public EntryBean(K key, V value) {
        super(key, value);
    }

    public K getKey() {
        return get_1();
    }

    public void setKey(K key) {
        set_1(key);
    }

    public V getValue() {
        return get_2();
    }

    public void setValue(V value) {
        set_2(value);
    }
}
