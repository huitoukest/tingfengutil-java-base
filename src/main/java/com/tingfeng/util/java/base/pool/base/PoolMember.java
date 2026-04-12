package com.tingfeng.util.java.base.common.bean;

/**
 * PoolMember的数据
 * @author huitoukest
 * @param <T> T 就是pool实际打开或者释放的资源本身
 */
public class PoolMember<T> extends PoolMemberCommonInfo {
    /**
     * member 是pool中打开或者释放的资源实例
     */
    protected T member;

    public T getMember() {
        return member;
    }

    public void setMember(T member) {
        this.member = member;
    }
    
    
}
