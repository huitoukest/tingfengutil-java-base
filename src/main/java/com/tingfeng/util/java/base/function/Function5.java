package com.tingfeng.util.java.base.function;

import java.util.Objects;

/**
 * 五参数函数接口，与 JDK 命名对齐
 *
 * @param <T> 第一个输入类型
 * @param <U> 第二个输入类型
 * @param <V> 第三个输入类型
 * @param <W> 第四个输入类型
 * @param <X> 第五个输入类型
 * @param <R> 返回类型
 */
@FunctionalInterface
public interface Function5<T, U, V, W, X, R> {

    /**
     * 应用五个参数
     */
    R apply(T t, U u, V v, W w, X x);

    /**
     * 组合：先执行当前函数，再执行 after 函数
     */
    default <R2> Function5<T, U, V, W, X, R2> andThen(Function1<? super R, ? extends R2> after) {
        Objects.requireNonNull(after);
        return (T t, U u, V v, W w, X x) -> after.apply(apply(t, u, v, w, x));
    }
}
