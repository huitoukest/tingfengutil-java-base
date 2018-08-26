package com.tingfeng.util.java.base.common.bean;

/**
 * pool 成员的基本信息
 * @author huitoukest
 */
public class PoolMemberCommonInfo {
    /**
     * 成员的创建时间
     */
    protected long createTime = System.currentTimeMillis();
    /**
     * 成员的更新时间
     */
    protected long updateTime = createTime;
    /**
     * 当前成员数据是否正在被使用
     */
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
