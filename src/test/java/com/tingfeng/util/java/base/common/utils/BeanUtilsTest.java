package com.tingfeng.util.java.base.common.utils;

import com.alibaba.fastjson.JSON;
import com.tingfeng.util.java.base.common.bean.User;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BeanUtilsTest {

    @Test
    public void copyTest(){
        User user = new User();
        User b = new User();
        user.setAge(100);
        user.setC(123L);
        user.updateDateTime = new Date();
        user.setMap(new HashMap<>());

        b.user = user;
        b.setAge(50);

        User target = new User();

        BeanUtils.copyProperties(target,b,"age");
        System.out.println(JSON.toJSONString(target));
    }

    @Test
    public void mapToBeanTest(){
        Map<String,Object> map = new HashMap<>();
        map.put("age",10);
        map.put("user.c","ccc");
        map.put("user.updateDateTime",new Date());
        map.put("user.interval",110);
        User user = BeanUtils.getBeanByMap(User.class,map);
        System.out.println(JSON.toJSONString(user));
    }
}
