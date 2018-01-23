package com.tingfeng.util.java.base.common.bean;

public class PoolMember<T> extends PoolMemberCommonInfo {
    protected T member;

    public T getMember() {
        return member;
    }

    public void setMember(T member) {
        this.member = member;
    }
    
    
}
