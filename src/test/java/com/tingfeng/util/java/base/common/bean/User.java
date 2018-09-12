package com.tingfeng.util.java.base.common.bean;

import com.tingfeng.util.java.base.common.utils.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用于测试使用的User类
 */
public class User {

    private  final String a = "a";
    private static String b = "b";

    public String userName;
    public Date updateDateTime;
    private int age;
    public Long interval;
    public User user;
    private Long  c;
    private String[] otherNames;
    private List<String> homeNames;
    private Map<String,Object> map;

    public User(){
        this.homeNames = CollectionUtils.getList(new String[]{"1","2","3"});
    }

    public int getAge() {
        return age + 1000;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Long getC() {
        return c;
    }

    public void setC(Long c) {
        this.c = c;
    }


    public String getA() {
        return a;
    }

    public static String getB() {
        return b;
    }

    public static void setB(String b) {
        User.b = b;
    }


    public String[] getOtherNames() {
        return otherNames;
    }

    public void setOtherNames(String[] otherNames) {
        this.otherNames = otherNames;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }
}
