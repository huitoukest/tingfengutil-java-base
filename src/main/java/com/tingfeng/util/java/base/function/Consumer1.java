package com.tingfeng.util.java.base.function;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * 单参数消费者接口，与 JDK Consumer 命名对齐
 *
 * @param <T> 输入类型
 */
@FunctionalInterface
public interface Consumer1<T> extends Consumer<T> {

    @Override
    void accept(T t);

    default Consumer1<T> andThen(Consumer1<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
