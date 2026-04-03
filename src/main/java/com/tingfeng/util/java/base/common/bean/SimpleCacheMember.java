package com.tingfeng.util.java.base.common.bean;

/**
 * 缓存成员，包含权重值和实际对象
 *
 * @param <T> 对象类型
 */
public class SimpleCacheMember<T> {
    /**
     * 权重增量，相对于 globalBase 的偏移量
     * actualWeight = globalBase + weight
     */
    private long weight;
    private T value;

    public SimpleCacheMember() {
        this.weight = 0;
    }

    public SimpleCacheMember(T value) {
        this.value = value;
        this.weight = 0;
    }

    public SimpleCacheMember(long weight, T value) {
        this.weight = weight;
        this.value = value;
    }

    public long getWeight() {
        return weight;
    }

    public SimpleCacheMember<T> setWeight(long weight) {
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