package com.tingfeng.util.java.base.common.utils.support.tree;

import com.tingfeng.util.java.base.common.bean.TreeTraverseContext;
import com.tingfeng.util.java.base.common.constant.DataNodeTraversePolicy;
import com.tingfeng.util.java.base.common.utils.ObjectUtils;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 后序遍历
 * 优先遍历(每一个根节点)所有子节点,而后遍历这些子节点的父节点
 */
public class DataLastTraverse extends AbstractTraverse{
    public DataLastTraverse(boolean isReverse) {
        super(isReverse);
    }
    @Override
    public <T> void traverse(List<T> treeList, int level, T parent, Function<T, List<T>> childrenGetter,
                             Predicate<TreeTraverseContext<T>> traverseF, DataNodeTraversePolicy dataNodeTraversePolicy) {
        dataLastTraverse(treeList,level,parent,childrenGetter,traverseF);
    }

    /**
     *
     * @param treeList 当前节点列表
     * @param level 当前层级
     * @param parent treeList 对应的 parent 节点
     * @param childrenGetter 输入T类型数，返回其children 数据的方法
     * @param traverseF 遍历数据，输入当前节点/父节点的数据 并返回是否继续遍历
     * @return 是否继续遍历 true = 继续, false = 停止
     * @param <T>
     */
    protected  <T> boolean dataLastTraverse(List<T> treeList, int level, T parent, Function<T, List<T>> childrenGetter,Predicate<TreeTraverseContext<T>> traverseF) {
        if(ObjectUtils.isEmpty(treeList)){
            return true;
        }
        boolean isContinueTraverse = true;
        int size = treeList.size();
        if (isReverse()) {
            for (int i = size - 1; i >= 0 ; i--) {
                isContinueTraverse = isContinueTraverse && traverse(level,treeList,i,parent,childrenGetter,traverseF);
                if(!isContinueTraverse){
                    return false;
                }
            }
        }else {
            //遍历左侧
            for (int i = 0; i < size; i++) {
                isContinueTraverse = isContinueTraverse && traverse(level,treeList,i,parent,childrenGetter,traverseF);
                if(!isContinueTraverse){
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * 优先遍历子节点，再遍历当前即节点（父节点）
     * @param parentLevel 父节点的层级
     * @param brothers 当前节点的兄弟节点
     * @param indexInBrother  当前节点在兄弟节点中的索引值
     * @param parent treeList 对应的 parent 节点
     * @param childrenGetter 输入T类型数，返回其children 数据的方法
     * @param traverseF 遍历数据，输入当前节点/父节点的数据 并返回是否继续遍历
     * @return 是否继续遍历 true = 继续, false = 停止
     * @return
     * @param <T>
     */
    protected  <T> boolean traverse(int parentLevel,List<T> brothers,int indexInBrother,T parent, Function<T, List<T>> childrenGetter,Predicate<TreeTraverseContext<T>> traverseF) {
        boolean isContinueTraverse = true;
        T node = brothers.get(indexInBrother);
        List<T> children = childrenGetter.apply(node);
        isContinueTraverse = isContinueTraverse && dataLastTraverse(children,parentLevel + 1,node,childrenGetter,traverseF);
        if(!isContinueTraverse){
           return false;
        }
        //遍历当前节点
        TreeTraverseContext<T> currentContext = new TreeTraverseContext<>(isReverse(), node, parent, parentLevel, indexInBrother, brothers);
        isContinueTraverse = isContinueTraverse && traverseF.test(currentContext);

        return isContinueTraverse;
    }
}
