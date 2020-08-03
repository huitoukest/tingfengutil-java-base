package com.tingfeng.util.java.base.common.bean.fraction;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class AbstractFraction {
    /**
     * 分数值转为小数, 默认使用四舍五入,可能精度丢失.
     * @return 计算得到的结果值
     */
    abstract public  double toDouble();

    /**
     * 转为BigDecimal来查看结果
     * @param newScale 保留的小数位数
     * @param roundingMode  小数的末位收尾机制, 比如归零或四舍五入
     * @return 当前数值结果
     */
    abstract BigDecimal toBigDecimal(int newScale, RoundingMode roundingMode);

    /**
     * 转为字符串展示值
     * @param newScale 缩放的位数,默认四舍五入
     * @return 分数对应的字符串结果
     */
    abstract String toString(int newScale);

    /**
     * 标准的A/B的分数形式 , 以英文斜杠隔开
     * @return
     */
    abstract String getValue();

    /**
     * 是否是正数
     */
    abstract boolean isPositive();

    /**
     * 是否值 = 0
     * @return
     */
    abstract boolean isZero();

    /***
     *
     * @return 是否是负数
     */
    abstract boolean isNegative();

    /**
     *
     * @return 是否是最简分数
     */
    abstract boolean isSimpleFraction();
}
