package com.tingfeng.util.java.base.common.bean;

public class PoolMemberCommonInfo {
    protected long createTime = System.currentTimeMillis();
    protected long updateTime = System.currentTimeMillis();
    protected boolean isUse = false;
    
    public long getCreateTime() {
        return createTime;
    }
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    public long getUpdateTime() {
        return updateTime;
    }
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
    public boolean isUse() {
        return isUse;
    }
    public void setUse(boolean isUse) {
        this.isUse = isUse;
    }
    
}
