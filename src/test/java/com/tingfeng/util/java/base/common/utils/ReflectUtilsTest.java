package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.bean.User;
import com.tingfeng.util.java.base.common.utils.reflect.ReflectUtils;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

public class ReflectUtilsTest {

    @Test
    public void isJavaBaseDataFieldTest(){
        List<Field> fieldList = ReflectUtils.getFields(User.class);
        for(Field f:fieldList){
                System.out.println(f.getName() + ":"+ ReflectUtils.isJavaBaseDataField(f));
                System.out.println(f.getName() + ":"+ ReflectUtils.isJavaBaseDataClass(f.getDeclaringClass()));
        }
    }

    @Test
    public void setFiledValueTest(){
        User user  = new User();
        ReflectUtils.setFieldValue(user,"user.userName","abc");
        System.out.print(user.user.userName);

        //ReflectUtils.setFieldValue(user,"user.age",100);
        ReflectUtils.setFieldValue(true,user,"user.age",new Object[]{100},Integer.TYPE);
        System.out.print(user.user.getAge());
    }
}
