package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.bean.TreeTraverseContext;
import com.tingfeng.util.java.base.common.constant.TraversalPolicy;
import com.tingfeng.util.java.base.common.helper.TreeHelper;
import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionROne;
import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionRThree;
import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionRTwo;
import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVTwo;
import com.tingfeng.util.java.base.common.utils.support.tree.Traverse;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * 树结构操作工具类
 * <p>
 * 提供无状态的树遍历、转换等操作。复杂操作建议使用 {@link TreeHelper}
 * </p>
 */
public final class TreeUtils {

    private TreeUtils() {
    }

    // ==================== 原有遍历方法（保留） ====================

    /**
     * 遍历一个 tree结构的数据
     *
     * @param treeList 当前 tree的根节点数据
     * @param traversalPolicy 遍历的策略 {@link TraversalPolicy}
     * @param childrenGetter 获取当前节点的children的方法
     * @param traverseF 用于遍历数据，输入节点信息，返回是否继续遍历
     */
    public static <T> void traverse(List<T> treeList, TraversalPolicy traversalPolicy,
                                   Function<T, List<T>> childrenGetter,
                                   Predicate<TreeTraverseContext<T>> traverseF) {
        traverse(treeList, 0, traversalPolicy, null, childrenGetter, traverseF);
    }

    /**
     * 遍历一个 tree结构的数据
     *
     * @param treeList 当前 tree的根节点数据
     * @param traversalPolicy 遍历的策略 {@link TraversalPolicy}
     * @param parent 当前 treeList 的父节点
     * @param childrenGetter 获取当前节点的children的方法
     * @param traverseF 用于遍历数据，输入节点信息，返回是否继续遍历
     */
    public static <T> void traverse(List<T> treeList, TraversalPolicy traversalPolicy, T parent,
                                   Function<T, List<T>> childrenGetter,
                                   Predicate<TreeTraverseContext<T>> traverseF) {
        traverse(treeList, 0, traversalPolicy, parent, childrenGetter, traverseF);
    }

    /**
     * 遍历一个 tree结构的数据
     *
     * @param treeList 当前 tree的根节点数据
     * @param level 指定当前层级信息,默认值为 0
     * @param traversalPolicy 遍历的策略 {@link TraversalPolicy}
     * @param parent 当前 treeList 的父节点
     * @param childrenGetter 获取当前节点的children的方法
     * @param traverseF 用于遍历数据，输入节点信息，返回是否继续遍历
     */
    public static <T> void traverse(List<T> treeList, int level, TraversalPolicy traversalPolicy, T parent,
                                   Function<T, List<T>> childrenGetter,
                                   Predicate<TreeTraverseContext<T>> traverseF) {
        Traverse traverse = traversalPolicy.getTraverse();
        traverse.traverse(treeList, level, parent, childrenGetter, traverseF);
    }

    // ==================== 新增：遍历并收集结果 ====================

    /**
     * 遍历并收集结果
     *
     * @param roots 根节点列表
     * @param childrenGetter 获取子节点的方法
     * @param collector 收集函数: (节点, 层级) → 结果
     * @return 收集结果列表
     */
    public static <T, R> List<R> traverseAndCollect(List<T> roots,
                                                    Function<T, List<T>> childrenGetter,
                                                    BiFunction<T, Integer, R> collector) {
        List<R> result = new ArrayList<>();
        traverseWithCollector(roots, 0, childrenGetter, (node, level, hasParent) -> {
            result.add(collector.apply(node, level));
            return true;
        });
        return result;
    }

    /**
     * 遍历直到找到目标
     *
     * @param roots 根节点列表
     * @param childrenGetter 获取子节点的方法
     * @param stopPredicate 停止条件
     * @return 找到的目标节点，若未找到返回 Optional.empty()
     */
    public static <T> Optional<T> traverseUntil(List<T> roots,
                                               Function<T, List<T>> childrenGetter,
                                               Predicate<T> stopPredicate) {
        AtomicReference<T> found = new AtomicReference<>();
        traverseWithCollector(roots, 0, childrenGetter, (node, level, hasParent) -> {
            if (stopPredicate.test(node)) {
                found.set(node);
                return false;
            }
            return true;
        });
        return Optional.ofNullable(found.get());
    }

    /**
     * 深度优先遍历
     *
     * @param roots 根节点列表
     * @param childrenGetter 获取子节点的方法
     * @return 按深度优先顺序排列的节点列表
     */
    public static <T> List<T> depthFirst(List<T> roots, Function<T, List<T>> childrenGetter) {
        List<T> result = new ArrayList<>();
        depthFirstCollect(roots, childrenGetter, result);
        return result;
    }

    private static <T> void depthFirstCollect(List<T> nodes, Function<T, List<T>> childrenGetter, List<T> result) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        for (T node : nodes) {
            result.add(node);
            List<T> children = childrenGetter.apply(node);
            depthFirstCollect(children, childrenGetter, result);
        }
    }

    /**
     * 广度优先遍历
     *
     * @param roots 根节点列表
     * @param childrenGetter 获取子节点的方法
     * @return 按广度优先顺序排列的节点列表
     */
    public static <T> List<T> breadthFirst(List<T> roots, Function<T, List<T>> childrenGetter) {
        List<T> result = new ArrayList<>();
        if (roots == null || roots.isEmpty()) {
            return result;
        }

        Queue<T> queue = new LinkedList<>(roots);
        while (!queue.isEmpty()) {
            T node = queue.poll();
            result.add(node);
            List<T> children = childrenGetter.apply(node);
            if (children != null && !children.isEmpty()) {
                queue.addAll(children);
            }
        }
        return result;
    }

    // ==================== 遍历内部实现 ====================

    /**
     * 带回调的遍历实现
     */
    private static <T> void traverseWithCollector(List<T> treeList, int level,
                                                   Function<T, List<T>> childrenGetter,
                                                   FunctionRThree<Boolean, T, Integer, Boolean> callback) {
        if (treeList == null || treeList.isEmpty()) {
            return;
        }

        for (T node : treeList) {
            boolean continueTraverse = callback.run(node, level, true);
            if (continueTraverse) {
                List<T> children = childrenGetter.apply(node);
                if (children != null && !children.isEmpty()) {
                    traverseWithCollector(children, level + 1, childrenGetter, callback);
                }
            }
        }
    }

    // ==================== 简单转换（返回 List，非 TreeHelper） ====================

    /**
     * 简单 Map
     *
     * @param nodes 节点列表
     * @param childrenGetter 获取子节点的方法
     * @param mapper 节点转换函数
     * @return 转换后的节点列表（保持树结构）
     */
    public static <T, R> List<R> map(List<T> nodes,
                                     Function<T, List<T>> childrenGetter,
                                     Function<T, R> mapper) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<R> result = new ArrayList<>();
        mapRecursively(nodes, childrenGetter, mapper, result);
        return result;
    }

    private static <T, R> void mapRecursively(List<T> nodes,
                                              Function<T, List<T>> childrenGetter,
                                              Function<T, R> mapper,
                                              List<R> result) {
        for (T node : nodes) {
            result.add(mapper.apply(node));
            List<T> children = childrenGetter.apply(node);
            if (children != null && !children.isEmpty()) {
                mapRecursively(children, childrenGetter, mapper, result);
            }
        }
    }

    /**
     * 简单 Filter
     *
     * @param nodes 节点列表
     * @param childrenGetter 获取子节点的方法
     * @param predicate 过滤条件
     * @return 过滤后的节点列表（保持树结构）
     */
    public static <T> List<T> filter(List<T> nodes,
                                     Function<T, List<T>> childrenGetter,
                                     Predicate<T> predicate) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>();
        filterRecursively(nodes, childrenGetter, predicate, result);
        return result;
    }

    private static <T> void filterRecursively(List<T> nodes,
                                              Function<T, List<T>> childrenGetter,
                                              Predicate<T> predicate,
                                              List<T> result) {
        for (T node : nodes) {
            if (predicate.test(node)) {
                result.add(node);
                List<T> children = childrenGetter.apply(node);
                if (children != null && !children.isEmpty()) {
                    filterRecursively(children, childrenGetter, predicate, result);
                }
            }
        }
    }

    // ==================== 排序 ====================

    /**
     * 对树进行排序
     *
     * @param roots 根节点列表
     * @param childrenGetter 获取子节点的方法
     * @param comparator 比较器
     * @return 排序后的根节点列表（不影响原结构）
     */
    public static <T> List<T> sort(List<T> roots,
                                   Function<T, List<T>> childrenGetter,
                                   Comparator<T> comparator) {
        if (roots == null || roots.isEmpty()) {
            return Collections.emptyList();
        }
        return roots.stream()
                .sorted(comparator)
                .peek(root -> sortChildrenRecursively(root, childrenGetter, comparator))
                .collect(Collectors.toList());
    }

    private static <T> void sortChildrenRecursively(T node,
                                                    Function<T, List<T>> childrenGetter,
                                                    Comparator<T> comparator) {
        List<T> children = childrenGetter.apply(node);
        if (children != null && children.size() > 1) {
            children.sort(comparator);
            for (T child : children) {
                sortChildrenRecursively(child, childrenGetter, comparator);
            }
        }
    }

    // ==================== 统计 ====================

    /**
     * 统计节点总数
     *
     * @param roots 根节点列表
     * @param childrenGetter 获取子节点的方法
     * @return 节点总数
     */
    public static <T> int count(List<T> roots, Function<T, List<T>> childrenGetter) {
        if (roots == null || roots.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (T root : roots) {
            count += countRecursively(root, childrenGetter);
        }
        return count;
    }

    private static <T> int countRecursively(T node, Function<T, List<T>> childrenGetter) {
        int count = 1;
        List<T> children = childrenGetter.apply(node);
        if (children != null) {
            for (T child : children) {
                count += countRecursively(child, childrenGetter);
            }
        }
        return count;
    }

    /**
     * 计算树深度
     *
     * @param root 根节点
     * @param childrenGetter 获取子节点的方法
     * @return 树深度（根节点深度为 1）
     */
    public static <T> int depth(T root, Function<T, List<T>> childrenGetter) {
        if (root == null) {
            return 0;
        }
        return depthRecursively(root, childrenGetter, 1);
    }

    private static <T> int depthRecursively(T node, Function<T, List<T>> childrenGetter, int currentDepth) {
        List<T> children = childrenGetter.apply(node);
        if (children == null || children.isEmpty()) {
            return currentDepth;
        }
        int maxChildDepth = currentDepth;
        for (T child : children) {
            int childDepth = depthRecursively(child, childrenGetter, currentDepth + 1);
            maxChildDepth = Math.max(maxChildDepth, childDepth);
        }
        return maxChildDepth;
    }

    // ==================== 工厂方法 ====================

    /**
     * 创建 TreeHelper
     *
     * @param nodes 节点列表
     * @param idGetter ID 获取函数
     * @param parentIdGetter 父 ID 获取函数
     * @return TreeHelper 实例
     */
    public static <T, ID> TreeHelper<T, ID> helper(List<T> nodes,
                                                   Function<T, ID> idGetter,
                                                   Function<T, ID> parentIdGetter) {
        return TreeHelper.of(nodes, idGetter, parentIdGetter);
    }

    /**
     * 创建 TreeHelper（带拷贝函数）
     *
     * @param nodes 节点列表
     * @param idGetter ID 获取函数
     * @param parentIdGetter 父 ID 获取函数
     * @param copier 节点拷贝函数
     * @return TreeHelper 实例
     */
    public static <T, ID> TreeHelper<T, ID> helper(List<T> nodes,
                                                   Function<T, ID> idGetter,
                                                   Function<T, ID> parentIdGetter,
                                                   Function<T, T> copier) {
        return TreeHelper.of(nodes, idGetter, parentIdGetter, copier);
    }

    // ==================== 原有方法（保持向后兼容） ====================

    /**
     * 将列表构建为树结构
     *
     * @param list 节点列表
     * @param setChildren 设置子节点函数 (child, parent) -> {}
     * @param isParent 判断是否为父节点的函数 (child, parent) -> boolean
     * @param getRootValue 获取根节点值的函数
     * @return 树的根节点列表
     */
    public static <T> List<T> getTreeList(List<T> list,
                                          FunctionVTwo<T, T> setChildren,
                                          FunctionRTwo<Boolean, T, T> isParent,
                                          FunctionROne<Object, T> getRootValue) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> roots = new ArrayList<>();
        Map<Object, List<T>> parentMap = new HashMap<>();

        for (T node : list) {
            Object parentValue = null;
            boolean isRoot = true;
            for (T other : list) {
                if (isParent.run(node, other)) {
                    isRoot = false;
                    parentValue = getRootValue.run(other);
                    break;
                }
            }
            if (isRoot) {
                roots.add(node);
            } else {
                parentMap.computeIfAbsent(parentValue, k -> new ArrayList<>()).add(node);
            }
        }

        for (Map.Entry<Object, List<T>> entry : parentMap.entrySet()) {
            Object parentValue = entry.getKey();
            List<T> children = entry.getValue();
            for (T node : list) {
                if (getRootValue.run(node).equals(parentValue)) {
                    for (T child : children) {
                        setChildren.run(child, node);
                    }
                    break;
                }
            }
        }

        return roots;
    }

    /**
     * 将列表构建为树结构（使用 Comparator 获取排序值）
     *
     * @param list 节点列表
     * @param comparator 比较器
     * @return 排序后的根节点列表
     */
    public static <T> List<T> getTreeList(List<T> list, Comparator<T> comparator) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> sortedList = new ArrayList<>(list);
        sortedList.sort(comparator);
        return getTreeList(sortedList,
                (child, parent) -> {
                    if (parent instanceof com.tingfeng.util.java.base.common.bean.TreeNode) {
                        ((com.tingfeng.util.java.base.common.bean.TreeNode) parent).getChildren().add(child);
                    }
                },
                (child, parent) -> {
                    if (child instanceof com.tingfeng.util.java.base.common.bean.TreeNode
                            && parent instanceof com.tingfeng.util.java.base.common.bean.TreeNode) {
                        Object childParentId = ((com.tingfeng.util.java.base.common.bean.TreeNode) child).getParentId();
                        Object parentId = ((com.tingfeng.util.java.base.common.bean.TreeNode) parent).getId();
                        return childParentId != null && childParentId.equals(parentId);
                    }
                    return false;
                },
                node -> {
                    if (node instanceof com.tingfeng.util.java.base.common.bean.TreeNode) {
                        return ((com.tingfeng.util.java.base.common.bean.TreeNode) node).getId();
                    }
                    return node.hashCode();
                });
    }

    /**
     * 将树结构展平为列表
     *
     * @param list 树节点列表
     * @param getChildren 获取子节点列表的函数
     * @param getSelf 获取自身的函数
     * @return 展平后的列表
     */
    public static <T> List<T> flatList(List<T> list,
                                       FunctionROne<List<T>, T> getChildren,
                                       FunctionROne<T, T> getSelf) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>();
        flatListRecursively(list, getChildren, getSelf, result);
        return result;
    }

    private static <T> void flatListRecursively(List<T> list,
                                                FunctionROne<List<T>, T> getChildren,
                                                FunctionROne<T, T> getSelf,
                                                List<T> result) {
        for (T node : list) {
            result.add(getSelf.run(node));
            List<T> children = getChildren.run(node);
            if (children != null && !children.isEmpty()) {
                flatListRecursively(children, getChildren, getSelf, result);
            }
        }
    }
}
