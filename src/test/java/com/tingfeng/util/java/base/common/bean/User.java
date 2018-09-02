package com.tingfeng.util.java.base.common.bean;

import java.util.Date;

public class User {
    public String userName;
    public Date updateDateTime;
    private int age;
    public Long interval;
    public User user;
    private Long  c;
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
}
