package com.tingfeng.util.java.base.function;

import java.util.Objects;

/**
 * 三参数函数接口，与 JDK 命名对齐
 * <p>
 * 注意：JDK 9+ 已引入 java.util.function.TriFunction，此接口与其兼容
 *
 * @param <T> 第一个输入类型
 * @param <U> 第二个输入类型
 * @param <V> 第三个输入类型
 * @param <R> 返回类型
 */
@FunctionalInterface
public interface Fun3<T, U, V, R> {

    /**
     * 应用三个参数
     */
    R apply(T t, U u, V v);

    /**
     * 组合：先执行当前函数，再执行 after 函数
     */
    default <R2> Fun3<T, U, V, R2> andThen(Fun1<? super R, ? extends R2> after) {
        Objects.requireNonNull(after);
        return (T t, U u, V v) -> after.apply(apply(t, u, v));
    }
}
