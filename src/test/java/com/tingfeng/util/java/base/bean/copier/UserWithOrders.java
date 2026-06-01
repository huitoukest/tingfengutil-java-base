package com.tingfeng.util.java.base.bean.copier;

import java.util.List;

/**
 * 测试用包含嵌套List的类
 */
public class UserWithOrders {
    private String name;
    private List<Order> orders;

    public UserWithOrders() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}