package com.tingfeng.util.java.base.common.lang.fun;

import java.util.function.Supplier;

/**
 * 带有缓存功能的 Supplier
 * @param <T>
 */
public class CacheSupplier<T> implements Supplier<T> {
    private T t;
    private Supplier<T> supplier;

    public CacheSupplier(Supplier<T> supplier){
        this.supplier = supplier;
    }

    public synchronized T get() {
        if(t == null){
            t = supplier.get();
        }
        return t;
    }

    public static <T> CacheSupplier<T> build(Supplier<T> supplier){
        return new CacheSupplier<>(supplier);
    }
}
