package com.tingfeng.util.java.base.function;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * 双参数消费者接口，与 JDK BiConsumer 命名对齐
 *
 * @param <T> 第一个输入类型
 * @param <U> 第二个输入类型
 */
@FunctionalInterface
public interface Consumer2<T, U> extends BiConsumer<T, U> {

    @Override
    void accept(T t, U u);

    default Consumer2<T, U> andThen(BiConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> {
            accept(t, u);
            after.accept(t, u);
        };
    }
}
