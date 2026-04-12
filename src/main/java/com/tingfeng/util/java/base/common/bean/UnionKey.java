package com.tingfeng.util.java.base.common.bean;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 多对象组成联合键
 * 适用于多个对象生成一个Map的Key的时候，重写了 hashCode 与 equals 方法
 * 支持任意数量的键，处理 null 值，优化哈希计算，支持序列化
 *
 * @author huitoukest
 */
public class UnionKey implements Serializable {
    private Object[] keys;

    public UnionKey(Object key1, Object key2){
        this.keys = new Object[]{key1, key2};
    }

    public UnionKey(Object key1, Object key2, Object key3){
        this.keys = new Object[]{key1, key2, key3};
    }

    public UnionKey(Object key1, Object key2, Object key3, Object key4){
        this.keys = new Object[]{key1, key2, key3, key4};
    }

    public UnionKey(Object key1, Object key2, Object key3, Object key4, Object key5){
        this.keys = new Object[]{key1, key2, key3, key4, key5};
    }

    /**
     * 可变参数构造方法，支持任意数量的键
     *
     * @param keys 键数组
     */
    public UnionKey(Object... keys){
        this.keys = keys;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (Object key : keys) {
            hashCode = 31 * hashCode + (key != null ? key.hashCode() : 0);
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if(obj instanceof UnionKey){
            if(obj == null && keys == null){
                return true;
            }
            return Arrays.equals(keys, ((UnionKey) obj).keys);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "UnionKey" + Arrays.toString(keys);
    }

    public <T> T getKey(int index){
        return (T) keys[index];
    }

    public Object[] getKeys(){
        return keys;
    }
}