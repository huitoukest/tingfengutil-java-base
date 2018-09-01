package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.utils.reflect.ReflectUtils;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

public class ReflectUtilsTest {

    class User{
        String userName;
        Date updateDateTime;
        int age;
        Long interval;
        User user;
    }

    @Test
    public void isJavaBaseDataFieldTest(){
        List<Field> fieldList = ReflectUtils.getFields(User.class);
        for(Field f:fieldList){
                System.out.println(f.getName() + ":"+ ReflectUtils.isJavaBaseDataField(f));
                System.out.println(f.getName() + ":"+ ReflectUtils.isJavaBaseDataClass(f.getDeclaringClass()));
        }
    }
}
