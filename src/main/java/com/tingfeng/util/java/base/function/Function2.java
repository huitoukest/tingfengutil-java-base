package com.tingfeng.util.java.base.function;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * 双参数函数接口，与 JDK BiFunction 命名对齐
 *
 * @param <T> 第一个输入类型
 * @param <U> 第二个输入类型
 * @param <R> 返回类型
 */
@FunctionalInterface
public interface Function2<T, U, R> extends BiFunction<T, U, R> {

    @Override
    R apply(T t, U u);

    default <V> Function2<T, U, V> andThen(Function1<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> after.apply(apply(t, u));
    }
}
