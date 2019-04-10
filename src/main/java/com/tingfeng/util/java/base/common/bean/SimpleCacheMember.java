package com.tingfeng.util.java.base.common.bean;


public class SimpleCacheMember<T>{
    private int weight = 0;
    private T value ;

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public int getWeight() {
        return weight;
    }

    public SimpleCacheMember<T> setWeight(int weight) {
        this.weight = weight;
        return this;
    }

    public T getValue() {
        return value;
    }

    public SimpleCacheMember<T> setValue(T value) {
        this.value = value;
        return this;
    }
}
