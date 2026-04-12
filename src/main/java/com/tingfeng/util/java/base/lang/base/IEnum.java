package com.tingfeng.util.java.base.common.inter;

/**
 * @author huitoukest
 * @param <E> 枚举对应的关键值
 */
public interface IEnum<E> {

    /**
     * 枚举数据库存储值
     */
    E getValue();
}
