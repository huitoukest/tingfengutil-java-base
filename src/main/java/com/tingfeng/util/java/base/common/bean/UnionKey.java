package com.tingfeng.util.java.base.common.bean;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 多对象组成联合键
 * 适用于多个对象生成一个Map的Key的时候，重写了 hashCode 与 equals 方法
 */
@AllArgsConstructor
public class UnionKey {
    private Object[] keys;

    public UnionKey(Object key1,Object key2){
        this.keys = new Object[]{key1,key2};
    }
    public UnionKey(Object key1,Object key2,Object key3){
        this.keys = new Object[]{key1,key2,key3};
    }
    public UnionKey(Object key1,Object key2,Object key3,Object key4){
        this.keys = new Object[]{key1,key2,key3,key4};
    }

    public UnionKey(Object key1,Object key2,Object key3,Object key4,Object key5){
        this.keys = new Object[]{key1,key2,key3,key4,key5};
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (int i = 0; i < keys.length; i++) {
            hashCode += keys[i].hashCode();
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if(obj instanceof UnionKey){
            return Arrays.equals(keys, ((UnionKey) obj).keys);
        }
        return false;
    }
}
