package com.tingfeng.util.java.base.bean.base;

import com.tingfeng.util.java.base.lang.base.UnionKey;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.Function;

@Data
@AllArgsConstructor
public class ConverterInfo<S,T> {
    private Class<S> source;
    private Class<T> target;
    private Function<S,T> convertMethod;

    public UnionKey getMatchKey(){
        return new UnionKey(source, target);
    }
}
