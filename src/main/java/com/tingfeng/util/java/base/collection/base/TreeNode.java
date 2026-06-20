package com.tingfeng.util.java.base.collection.base;

import java.util.List;

/**
 * 树节点定义
 *
 * @param <T> 节点类型
 * @param <ID> ID 类型
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
