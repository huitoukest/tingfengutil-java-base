package com.tingfeng.util.java.base.common.inter;

/**
 * 处理一个对象并且发挥其值
 * @param <S> Source
 * @param <T> Target
 */
public interface ObjectDealReturnInter<S,T> {

    /**
     * 处理一个Collection
     * @param source
     * @param recursive 是否递归处理
     * @return
     */
    public abstract T dealCollection(S source,boolean recursive);

    /**
     * 处理一个Map
     * @param source
     * @param recursive 是否递归处理
     * @return
     */
    public abstract T dealMap(S source,boolean recursive);

    /**
     * 处理一个Array
     * @param source
     * @param recursive 是否递归处理
     * @return
     */
    public abstract T dealArray(S source,boolean recursive);
    /**
     * 处理一个字符串
     * @param source
     * @param isTrim 表示是否去掉字符串两边的空白字符
     * @return
     */
    public abstract T dealString(S source,boolean isTrim);

    /**
     * 处理普通的对象
     * @param source
     * @return
     */
    public abstract T dealCommonObject(S source);
    
    
}
