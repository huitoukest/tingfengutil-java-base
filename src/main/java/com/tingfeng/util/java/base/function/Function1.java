package com.tingfeng.util.java.base.function;

import java.util.Objects;
import java.util.function.Function;

/**
 * 单参数函数接口，与 JDK Function 命名对齐
 *
 * @param <T> 输入类型
 * @param <R> 返回类型
 */
@FunctionalInterface
public interface Function1<T, R> extends Function<T, R> {

    @Override
    R apply(T t);

    default <V> Function1<V, R> compose(Function1<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    default <V> Function1<T, V> andThen(Function1<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    static <T> Function1<T, T> identity() {
        return t -> t;
    }
}
