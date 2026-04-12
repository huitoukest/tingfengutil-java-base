package com.tingfeng.util.java.base.common.bean;

import java.util.List;

/**
 * 树节点定义
 * @param <ID>
 */
public interface TreeNode<T extends TreeNode,ID> {
    ID getId();
    ID getParentId();

    /**
     * 子节点
     * @return
     */
    List<T> getChildren();
}
