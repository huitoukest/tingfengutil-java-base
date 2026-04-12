package com.tingfeng.util.java.base.common.bean;

import java.util.ArrayList;
import java.util.List;

public abstract class GenericTreeNode<T extends GenericTreeNode,ID> implements TreeNode<T,ID>{

    private List<T> children = new ArrayList<>();

    @Override
    public List<T> getChildren() {
        return children;
    }

    public void setChildren(List<T> children) {
        this.children = children;
    }
}