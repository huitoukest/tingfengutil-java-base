package com.tingfeng.util.java.base.function;

import java.util.Objects;

/**
 * 四参数消费者接口，与 JDK 命名对齐
 *
 * @param <T> 第一个输入类型
 * @param <U> 第二个输入类型
 * @param <V> 第三个输入类型
 * @param <W> 第四个输入类型
 */
@FunctionalInterface
public interface Consumer4<T, U, V, W> {

    /**
     * 接受四个参数
     */
    void accept(T t, U u, V v, W w);

    /**
     * 组合：先执行当前，再执行 after
     */
    default Consumer4<T, U, V, W> andThen(Consumer4<? super T, ? super U, ? super V, ? super W> after) {
        Objects.requireNonNull(after);
        return (T t, U u, V v, W w) -> {
            accept(t, u, v, w);
            after.accept(t, u, v, w);
        };
    }
}
