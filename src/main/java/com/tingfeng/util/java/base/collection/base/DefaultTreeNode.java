package com.tingfeng.util.java.base.common.bean;

public class DefaultTreeNode extends GenericTreeNode<DefaultTreeNode,String>{
    private String id;
    private String parentId;
    private int sortValue;

    public DefaultTreeNode(String id, String parentId, int sortValue) {
        this.id = id;
        this.parentId = parentId;
        this.sortValue = sortValue;
    }

    public DefaultTreeNode(String id, String parentId) {
       this(id,parentId,0);
    }

    public DefaultTreeNode() {
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getParentId() {
        return this.parentId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getSortValue() {
        return sortValue;
    }

    public void setSortValue(int sortValue) {
        this.sortValue = sortValue;
    }
}
