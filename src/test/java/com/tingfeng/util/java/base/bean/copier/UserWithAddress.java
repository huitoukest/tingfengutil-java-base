package com.tingfeng.util.java.base.bean.copier;

/**
 * 测试用包含嵌套Address的类
 */
public class UserWithAddress {
    private String name;
    private Address address;

    public UserWithAddress() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}