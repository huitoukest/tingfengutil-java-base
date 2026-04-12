package com.tingfeng.util.java.base.common.bean;

import java.util.List;

/**
 * 遍历 Tree 结构数据的时候上下文信息（遍历所属的节点的相关信息）
 * 当判断紧接着是否遍历 data节点（父节点）时，传入的context信息是当前这个层级的子节点信息
 */
public class TreeTraverseContext<T> {
    /**
     * 是正序还是逆序
     */
    private boolean isReverse;
    /**
     * 当前节点
     */
    private T node;
    /**
     * 其中第一次父节点的值为传入的parent值，默认为 为null
     * 在 中序或者后续遍历时,遍历到父节点则此值为 null
     */
    private T parent;
    /**
     * 遍历的当前节点的层级信息,默认从 1开始
     */
    private int level = 1;
    /**
     * 当前节点在当前层级的List(兄弟节点)中的索引值
     */
    private int indexInBrother;
    /**
     * 当前节点的所有兄弟节点（包含自身）
     */
    private List<T> brothers;

    public TreeTraverseContext(boolean isReverse,T node, T parent, int level,int indexInBrother,List<T> brothers) {
        this.isReverse = isReverse;
        this.node = node;
        this.parent = parent;
        this.level = level;
        this.indexInBrother = indexInBrother;
        this.brothers = brothers;
    }

    public TreeTraverseContext() {
    }

    public T getNode() {
        return node;
    }

    public boolean isReverse() {
        return isReverse;
    }

    public void setReverse(boolean reverse) {
        isReverse = reverse;
    }

    public List<T> getBrothers() {
        return brothers;
    }

    public void setBrothers(List<T> brothers) {
        this.brothers = brothers;
    }

    public void setNode(T node) {
        this.node = node;
    }

    public T getParent() {
        return parent;
    }

    public void setParent(T parent) {
        this.parent = parent;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getIndexInBrother() {
        return indexInBrother;
    }

    public void setIndexInBrother(int indexInBrother) {
        this.indexInBrother = indexInBrother;
    }
}
