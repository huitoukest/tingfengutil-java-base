package com.tingfeng.util.java.base.common.inter;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 *  即调用者需要区分对象可能的类型；实现者则处理具体的逻辑
 * 处理一个对象并且返回其值；
 * 依次处理并判断容器或者对象的值；符合条件则返回其值；
 * 否则根据条件继续
 * @author wanggang
 * T 返回的类型
 */
public interface ObjectDealReturnInter<T>{

    /**
     * 判断Collection是否符合条件
     * @param source
     * @return
     */
    T dealCollection(Collection<?> source);

    /**
     * 处理一个Map
     * @param source
     * @return
     */
    T dealMap(Map<?,?> source);

    /**
     * 处理一个Array
     * @param source
     * @return
     */
    T dealArray(Object[] source);

    /**
     * 处理一个字符序列 CharSequence
     * @param source
     * @return
     */
    T dealCharSequence(CharSequence source);

    /**
     * 处理一个字符序列 CharSequence
     * @param source
     * @return
     */
    T dealDate(Date source);

    /**
     * 处理普通的对象;非Collection、Map、Array、CharSequence、Date；
     * @param source
     * @return
     */
    T dealCommonObject(Object source);
    
}
