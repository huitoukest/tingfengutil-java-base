package com.tingfeng.util.java.base.common.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimpleCacheMember<T>{
    private int weight = 0;
    private T value ;

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
