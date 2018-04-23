package com.tingfeng.util.java.base.common.inter;

public interface ObjectDealReturnInter<S,T> {

    
    public abstract T dealCollection(S source,boolean recursive);
    public abstract T dealMap(S source,boolean recursive);
    public abstract T dealArray(S source,boolean recursive);
    /**
     * recursive
     * @param source
     * @param recursive recursive表示是否trim
     * @return
     */
    public abstract T dealString(S source,boolean isTrim);
    public abstract T dealCommonObject(S source);
    
    
}
