package com.tingfeng.util.java.base.bean.base;

import java.lang.reflect.InvocationTargetException;

public interface BeanCopyFun {
    String getName();
    Object read(Object srcObj) throws IllegalAccessException, InvocationTargetException, IllegalArgumentException;

    Object write(Object targetObj,Object value) throws IllegalAccessException, InvocationTargetException, IllegalArgumentException;
}
