package com.tingfeng.util.java.base.common.constant;

import com.tingfeng.util.java.base.common.utils.support.tree.DataAfterTraverse;
import com.tingfeng.util.java.base.common.utils.support.tree.DataFirstTraverse;
import com.tingfeng.util.java.base.common.utils.support.tree.DataLastTraverse;
import com.tingfeng.util.java.base.common.utils.support.tree.Traverse;

/**
 * tree遍历的策略 <br/>
 * <pre>
 * 示例,转换器的Tree：
 *                 1
 *      ---------------------------
 *      /      |       \         \
 *     21     22       23        24
 *   /  \     |     /  |   \       |
 *  31  32   33   34  35  36     37
 *     /     |         |   |
 *    41    42        43  44
 * 选择不同的遍历策略 会得到不同的结果，具体参见枚举值对应的策略说明
 * </pre>
 */
public enum TraversalPolicy{
    /**
     * 前序遍历(Preorder Traversal 亦称先序遍历)
     * <pre>
     * 顺序为：访问根结点—>根的左节点-中间节点—>根的右节点(Data -> Left --  -> Right)
     * 期望的遍历的顺序为  1,21,31,32,41,22,33,42,23,34,35,43,36,44,24,37
     * 对于有多个根节点的情况来说，这里会优先遍历第一个根节点所有节点；而后再依次遍历其它节点
     * </pre>
     */
    DLR(new DataFirstTraverse(false)),
    /**
     * 前序遍历(Preorder Traversal 亦称先序遍历)
     * <pre>
     * 顺序为：访问根结点—>根的右节点-中间节点—>根的左节点(Data -> --> Right -- -Left )
     * 期望的遍历的顺序为  1,24,37,23,36,44,35,43,34,22,33,42,21,32,41,31
     * 对于有多个根节点的情况来说，这里会优先遍历第一个根节点所有节点；而后再依次遍历其它节点
     * </pre>
     */
    DRL(new DataFirstTraverse(true)),
    /**
     * 中序遍历
     * <pre>
     * 顺序为：Left --> Data  -- -> Right
     * 当前遍历一个子节点后立即遍历父节点,然后遍历其它子节点
     * 则期望的遍历的顺序为   31,21,41,32,1,42,33,22,34,23,43,35,44,36,37,24
     * </pre>
     */
    LDR(new DataAfterTraverse(false)),
    /**
     * 中序遍历
     * <pre>
     * 顺序为： Right --> Data  -- -> Left
     * 当前遍历一个子节点后立即遍历父节点,然后遍历其它子节点
     * 则期望的遍历的顺序为   31,21,41,32,1,42,33,22,34,23,43,35,44,36,37,24
     * </pre>
     */
    RDL(new DataAfterTraverse(true)),
    /**
     * 后序遍历
     * <pre>
     * 顺序为： Left --> -- -> Right --> Data
     * 即优先遍历子节点,而后遍历父节点
     * 对于有多个根节点的情况，会依次遍历每个根节点的所有节点后再遍历下一个根节点的数据
     * 则期望的遍历的顺序为   31,41,32,21,42,33,22,34,43,35,44,36,23,37,24,1
     * </pre>
     */
    LRD(new DataLastTraverse(false)),
    /**
     * 后序遍历
     * <pre>
     * 顺序为： Right --> -- -> Left --> Data
     * 即优先遍历子节点,而后遍历父节点
     * 对于有多个根节点的情况，会依次遍历每个根节点的所有节点后再遍历下一个根节点的数据
     * 则期望的遍历的顺序为 37,24,44,36,43,35,34,23,42,33,22,41,32,31,21,1
     * </pre>
     */
    RLD(new DataLastTraverse(true)),
     /*,LEVEL_ORDER_ASC,
    LEVEL_ORDER_DESC,*/
    ;

    private Traverse traverse;
    TraversalPolicy(Traverse traverse){
        this.traverse = traverse;
    }

    public Traverse getTraverse(){
        return this.traverse;
    }
}
