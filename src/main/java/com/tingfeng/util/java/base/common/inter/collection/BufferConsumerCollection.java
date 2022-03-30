package com.tingfeng.util.java.base.common.inter.collection;

public interface BufferConsumerCollection<T> {
    /**
     * 添加一个元素
     * @param t
     */
    void add(T t);

    /**
     * 判断，若符合条件则匹配，否则不消费数据
     * @return
     */
    void consumerIfMatch();
}
