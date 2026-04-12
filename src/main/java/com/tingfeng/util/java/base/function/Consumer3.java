package com.tingfeng.util.java.base.function;

import java.util.Objects;

/**
 * 三参数消费者接口，与 JDK 命名对齐
 *
 * @param <T> 第一个输入类型
 * @param <U> 第二个输入类型
 * @param <V> 第三个输入类型
 */
@FunctionalInterface
public interface Consumer3<T, U, V> {

    /**
     * 接受三个参数
     */
    void accept(T t, U u, V v);

    /**
     * 组合：先执行当前，再执行 after
     */
    default Consumer3<T, U, V> andThen(Consumer3<? super T, ? super U, ? super V> after) {
        Objects.requireNonNull(after);
        return (T t, U u, V v) -> {
            accept(t, u, v);
            after.accept(t, u, v);
        };
    }
}
