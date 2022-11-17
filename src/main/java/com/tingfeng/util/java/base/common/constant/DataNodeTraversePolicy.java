package com.tingfeng.util.java.base.common.constant;

import com.tingfeng.util.java.base.common.bean.TreeTraverseContext;

import java.util.function.Predicate;

/**
 * 数据节点（中序遍历下的父节点遍历策略）
 */
public class DataNodeTraversePolicy {
    /**
     * 遍历完一个子节点后，立即遍历父节点
     */
    public static final DataNodeTraversePolicy AFTER_ONE_CHILD_NODE;
    static {
        AFTER_ONE_CHILD_NODE = new DataNodeTraversePolicy(context -> {
            if (context.isReverse() && context.getIndexInBrother() == context.getBrothers().size() - 1) {
                return true;
            }
            if(!context.isReverse() && context.getIndexInBrother() == 0){
                return true;
            }
            return false;
        });
    }
    ;
    private Predicate<TreeTraverseContext> predicate;

    private DataNodeTraversePolicy(Predicate<TreeTraverseContext> predicate){
        this.predicate = predicate;
    }

    public DataNodeTraversePolicy buildPredicate(Predicate<TreeTraverseContext> predicate){
        return new DataNodeTraversePolicy(predicate);
    }

    public Predicate<TreeTraverseContext> getPredicate() {
        return predicate;
    }
}
