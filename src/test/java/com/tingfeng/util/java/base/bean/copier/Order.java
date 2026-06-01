package com.tingfeng.util.java.base.bean.copier;

/**
 * 测试用订单类
 */
public class Order {
    private Long id;
    private Double amount;

    public Order() {
    }

    public Order(Long id, Double amount) {
        this.id = id;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}