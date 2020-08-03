package com.tingfeng.util.java.base.common.inter;

import com.tingfeng.util.java.base.common.bean.fraction.AbstractFraction;

/**
 * 抽象的一个基本分数数值计算行为.
 * @author huitoukest
 */
public interface IFractionOperation<T extends AbstractFraction> extends Comparable<T>{
    /**
     * 当前对象加上 other值
     * @param other 需要加的对象
     * @return 相加后的值
     */
    T add(T other);

    /**
     * 当前对象减去other对象
     * @param other 需要减去的值
     * @return 相减后的值.
     */
    T sub(T other);

    /**
     * 当前对象乘以other对象
     * @param other 需要乘以的数值
     * @return 相乘后的值
     */
    T multiply(T other);

    /**
     * 当前对象除以other对象
     * @param other 需要除的数值
     * @return 当前对象除以other对象的结果
     */
    T div(T other);

    /**
     * 判断当前对象和other是否相等
     * @param other 需要判断的对象
     * @return 是否相等的结果
     */
    default boolean eq(T other){
        return this.compareTo(other) == 0;
    }

    /**
     * 当前值是否大于 other 值
     * @param other 需要必须的对象
     * @return 是否大于
     */
    default boolean greatThan(T other){
        return this.compareTo(other) > 0;
    }

    /**
     * 当前值是否小于 other 值
     * @param other 需要必须的对象
     * @return 是否小于
     */
    default boolean letterThan(T other){
        return this.compareTo(other) < 0;
    }

    /**
     * 求自身的最简分数;
     * 当分母为零时,分子默认为1;
     * @return
     */
    T simpleFraction();
}
