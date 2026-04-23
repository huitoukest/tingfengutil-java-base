package com.tingfeng.util.java.base.bean.converter;

public abstract class StringConditionConverter<T>  implements ConditionConverter<String,T>{

    @Override
    public Class<String> getSourceType() {
        return String.class;
    }
}
