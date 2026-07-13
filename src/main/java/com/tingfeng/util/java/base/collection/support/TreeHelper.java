package com.tingfeng.util.java.base.collection.support;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * 树操作助手 - 不可变设计，天然支持并发
 *
 * @param <T> 节点类型
 * @param <ID> ID 类型
 */
public final class TreeHelper<T, ID> {

    /** 节点列表（原始数据，不变） */
    private final List<T> allNodes;

    /** ID → 节点 索引（只读） */
    private final Map<ID, T> nodeIndex;

    /** ID → 父 ID 索引（只读） */
    private final Map<ID, ID> parentIndex;

    /** ID → 子 ID 列表 索引（只读） */
    private final Map<ID, List<ID>> childrenIndex;

    /** 根节点 ID 列表 */
    private final List<ID> rootIds;

    /** 节点拷贝函数 */
    private final Function<T, T> copier;

    /** ID 获取函数（保存以支持 getNodeId 动态获取） */
    private final Function<T, ID> idGetter;

    // ==================== 构造方法 ====================

    /**
     * 构建 TreeHelper
     *
     * @param nodes 节点列表
     * @param idGetter ID 获取函数
     * @param parentIdGetter 父 ID 获取函数
     * @param copier 节点拷贝函数（用于变换操作），可控制深拷贝或浅拷贝
     */
    public TreeHelper(List<T> nodes,
                      Function<T, ID> idGetter,
                      Function<T, ID> parentIdGetter,
                      Function<T, T> copier) {
        this.copier = copier;
        this.idGetter = idGetter;
        this.allNodes = nodes == null ? Collections.emptyList() : new ArrayList<>(nodes);

        // 建立索引
        Map<ID, T> tempNodeIndex = new HashMap<>();
        Map<ID, ID> tempParentIndex = new HashMap<>();
        Map<ID, List<ID>> tempChildrenIndex = new HashMap<>();
        List<ID> tempRootIds = new ArrayList<>();

        for (T node : this.allNodes) {
            ID id = idGetter.apply(node);
            if (id != null) {
                tempNodeIndex.put(id, node);

                ID parentId = parentIdGetter.apply(node);
                tempParentIndex.put(id, parentId);

                if (parentId == null) {
                    tempRootIds.add(id);
                } else {
                    tempChildrenIndex.computeIfAbsent(parentId, k -> new ArrayList<>()).add(id);
                }
            }
        }

        this.nodeIndex = Collections.unmodifiableMap(tempNodeIndex);
        this.parentIndex = Collections.unmodifiableMap(tempParentIndex);
        this.childrenIndex = Collections.unmodifiableMap(tempChildrenIndex);
        this.rootIds = Collections.unmodifiableList(tempRootIds);
    }

    /**
     * 私有构造器，用于内部创建新实例
     */
    private TreeHelper(List<T> allNodes,
                       Map<ID, T> nodeIndex,
                       Map<ID, ID> parentIndex,
                       Map<ID, List<ID>> childrenIndex,
                       List<ID> rootIds,
                       Function<T, T> copier,
                       Function<T, ID> idGetter) {
        this.allNodes = allNodes;
        this.nodeIndex = nodeIndex;
        this.parentIndex = parentIndex;
        this.childrenIndex = childrenIndex;
        this.rootIds = rootIds;
        this.copier = copier;
        this.idGetter = idGetter;
    }

    // ==================== 工厂方法 ====================

    /**
     * 创建 TreeHelper（使用默认浅拷贝）
     */
    public static <T, ID> TreeHelper<T, ID> of(List<T> nodes,
                                               Function<T, ID> idGetter,
                                               Function<T, ID> parentIdGetter) {
        return new TreeHelper<>(nodes, idGetter, parentIdGetter, Function.identity());
    }

    /**
     * 创建 TreeHelper（自定义拷贝函数）
     */
    public static <T, ID> TreeHelper<T, ID> of(List<T> nodes,
                                               Function<T, ID> idGetter,
                                               Function<T, ID> parentIdGetter,
                                               Function<T, T> copier) {
        return new TreeHelper<>(nodes, idGetter, parentIdGetter, copier);
    }

    /**
     * 创建空 TreeHelper
     */
    public static <T, ID> TreeHelper<T, ID> empty() {
        return new TreeHelper<>(Collections.emptyList(), t -> null, t -> null, Function.identity());
    }

    // ==================== 查询操作 ====================

    /**
     * 按 ID 查找节点
     */
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(nodeIndex.get(id));
    }

    /**
     * 按条件查找第一个匹配节点
     */
    public Optional<T> findOne(Predicate<T> predicate) {
        return allNodes.stream().filter(predicate).findFirst();
    }

    /**
     * 按条件查找所有匹配节点
     */
    public List<T> findAll(Predicate<T> predicate) {
        return allNodes.stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * 获取所有根节点
     */
    public List<T> getRoots() {
        return rootIds.stream()
                .map(nodeIndex::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取节点的父节点
     */
    public Optional<T> getParent(T node) {
        ID parentId = parentIndex.get(getNodeId(node));
        return Optional.ofNullable(nodeIndex.get(parentId));
    }

    /**
     * 获取节点的直接子节点
     */
    public List<T> getChildren(T node) {
        ID nodeId = getNodeId(node);
        return getChildrenById(nodeId);
    }

    /**
     * 根据节点ID获取子节点列表（内部使用，避免重复转换）
     */
    private List<T> getChildrenById(ID nodeId) {
        return Optional.ofNullable(childrenIndex.get(nodeId))
                .orElse(Collections.emptyList())
                .stream()
                .map(nodeIndex::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有叶子节点
     */
    public List<T> getLeaves() {
        return allNodes.stream()
                .filter(node -> {
                    ID id = getNodeId(node);
                    List<ID> children = childrenIndex.get(id);
                    return children == null || children.isEmpty();
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取到根节点的路径
     */
    public List<T> getPathToRoot(T node) {
        List<T> path = new ArrayList<>();
        path.add(node);
        ID currentId = getNodeId(node);

        ID parentId;
        while ((parentId = parentIndex.get(currentId)) != null) {
            T parent = nodeIndex.get(parentId);
            if (parent != null) {
                path.add(0, parent);
                currentId = parentId;
            } else {
                break;
            }
        }
        return path;
    }

    /**
     * 获取节点层级（根=0）- 优化：直接遍历父节点链，避免构建完整路径
     */
    public int getLevel(T node) {
        int level = 0;
        ID currentId = getNodeId(node);

        while ((currentId = parentIndex.get(currentId)) != null) {
            level++;
        }
        return level;
    }

    /**
     * 计算树深度
     */
    public int getDepth() {
        return getRoots().stream()
                .mapToInt(root -> calculateDepth(root, 0))
                .max()
                .orElse(0);
    }

    /**
     * 计算以 node 为根的子树深度 - 优化：直接使用 childrenIndex 避免 List 创建
     */
    private int calculateDepth(T node, int currentDepth) {
        ID nodeId = getNodeId(node);
        List<ID> childIds = childrenIndex.get(nodeId);
        if (childIds == null || childIds.isEmpty()) {
            return currentDepth + 1; // 叶子节点本身需要被计数
        }
        int maxChildDepth = currentDepth + 1;
        for (ID childId : childIds) {
            T child = nodeIndex.get(childId);
            if (child != null) {
                int childDepth = calculateDepth(child, currentDepth + 1);
                maxChildDepth = Math.max(maxChildDepth, childDepth);
            }
        }
        return maxChildDepth;
    }

    /**
     * 节点总数
     */
    public int size() {
        return allNodes.size();
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return allNodes.isEmpty();
    }

    // ==================== 排序操作（返回新实例，保持不可变性）=================

    /**
     * 对树的子节点进行排序（递归排序所有层级）
     *
     * 返回一个新的 TreeHelper，所有层级的子节点都按 comparator 排序。
     * 本实例不受影响，保持不可变性。
     *
     * @param comparator 比较器
     * @return 排序后的新 TreeHelper
     */
    public TreeHelper<T, ID> sort(Comparator<T> comparator) {
        // 复制所有节点列表
        List<T> copiedNodes = allNodes.stream()
                .map(node -> copier.apply(node))
                .collect(Collectors.toList());

        // 重建索引结构
        Map<ID, T> newNodeIndex = new HashMap<>(copiedNodes.size());
        Map<ID, ID> newParentIndex = new HashMap<>(copiedNodes.size());
        Map<ID, List<ID>> newChildrenIndex = new HashMap<>();

        // 重建 ID → 节点映射（用于排序查找）
        Map<ID, T> tempIndex = new HashMap<>();
        for (T node : copiedNodes) {
            ID id = getNodeIdFromCopiedNode(node);
            tempIndex.put(id, node);
        }

        // 重建 parentIndex 和 childrenIndex（原始顺序）
        for (T node : copiedNodes) {
            ID id = getNodeIdFromCopiedNode(node);
            T original = nodeIndex.get(id);
            ID parentId = original != null ? parentIndex.get(id) : null;
            newParentIndex.put(id, parentId);

            if (parentId == null) {
                // 根节点不入 childrenIndex
            } else {
                newChildrenIndex.computeIfAbsent(parentId, k -> new ArrayList<>()).add(id);
            }
            newNodeIndex.put(id, node);
        }

        // 递归排序每个根节点的子树
        for (ID rootId : rootIds) {
            sortChildrenRecursively(rootId, newChildrenIndex, tempIndex, comparator);
        }

        return new TreeHelper<>(copiedNodes, newNodeIndex, newParentIndex,
                newChildrenIndex, rootIds, copier, idGetter);
    }

    /**
     * 递归排序子节点
     */
    private void sortChildrenRecursively(ID nodeId,
                                          Map<ID, List<ID>> childrenIndex,
                                          Map<ID, T> nodeMap,
                                          Comparator<T> comparator) {
        List<ID> childIds = childrenIndex.get(nodeId);
        if (childIds == null || childIds.size() <= 1) {
            return;
        }

        // 转换为节点列表并排序
        List<T> children = childIds.stream()
                .map(nodeMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        children.sort(comparator);

        // 更新排序后的子节点 ID 列表
        List<ID> sortedChildIds = children.stream()
                .map(node -> getNodeIdFromCopiedNode(node))
                .collect(Collectors.toList());
        childrenIndex.put(nodeId, sortedChildIds);

        // 递归排序每个子节点的子节点
        for (ID childId : sortedChildIds) {
            sortChildrenRecursively(childId, childrenIndex, nodeMap, comparator);
        }
    }

    /**
     * 从复制的节点中获取 ID（使用 idGetter 支持泛型）
     */
    private ID getNodeIdFromCopiedNode(T node) {
        return idGetter.apply(node);
    }

    // ==================== 转换操作 ====================

    /**
     * 转换为 List
     */
    public List<T> toList() {
        return new ArrayList<>(allNodes);
    }

    /**
     * 展平为单层 List（深度优先）
     */
    public List<T> flatten() {
        return flattenDepthFirst();
    }

    /**
     * 深度优先展平为单层 List
     */
    public List<T> flattenDepthFirst() {
        List<T> result = new ArrayList<>();
        for (T root : getRoots()) {
            flattenDepthFirstNode(root, result);
        }
        return result;
    }

    /**
     * 广度优先展平为单层 List
     */
    public List<T> flattenBreadthFirst() {
        List<T> result = new ArrayList<>();
        Queue<T> queue = new LinkedList<>(getRoots());

        while (!queue.isEmpty()) {
            T node = queue.poll();
            result.add(node);
            List<T> children = getChildren(node);
            if (!children.isEmpty()) {
                queue.addAll(children);
            }
        }
        return result;
    }

    /**
     * 按 comparator 排序后深度优先展平
     */
    public List<T> flattenSorted(Comparator<T> comparator) {
        List<T> result = new ArrayList<>();
        for (T root : getRoots()) {
            flattenSortedNode(root, result, comparator);
        }
        return result;
    }

    /**
     * 按 comparator 排序后广度优先展平
     */
    public List<T> flattenBreadthFirstSorted(Comparator<T> comparator) {
        List<T> result = new ArrayList<>();
        Queue<T> queue = new LinkedList<>(getRoots());

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            List<T> levelNodes = new ArrayList<>();
            for (int i = 0; i < levelSize; i++) {
                T node = queue.poll();
                levelNodes.add(node);
                queue.addAll(getChildren(node));
            }
            // 对同一层节点排序（可选）
            if (comparator != null) {
                levelNodes.sort(comparator);
            }
            result.addAll(levelNodes);
        }
        return result;
    }

    private void flattenDepthFirstNode(T node, List<T> result) {
        result.add(node);
        List<T> children = getChildren(node);
        for (T child : children) {
            flattenDepthFirstNode(child, result);
        }
    }

    private void flattenSortedNode(T node, List<T> result, Comparator<T> comparator) {
        result.add(node);
        List<T> children = getChildren(node);
        if (children.size() > 1) {
            children.sort(comparator);
        }
        for (T child : children) {
            flattenSortedNode(child, result, comparator);
        }
    }

    // ==================== 层级与条件过滤 ====================

    /**
     * 按层级和条件双重过滤节点
     *
     * @param level 目标层级（根节点为 0）
     * @param predicate 过滤条件，返回 true 保留
     * @return 满足条件的节点列表
     */
    public List<T> filterByLevelAndPredicate(int level, Predicate<T> predicate) {
        if (level < 0) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>();
        Queue<T> queue = new LinkedList<>(getRoots());
        int currentLevel = 0;

        while (!queue.isEmpty() && currentLevel <= level) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                T node = queue.poll();
                if (currentLevel == level && predicate.test(node)) {
                    result.add(node);
                }
                queue.addAll(getChildren(node));
            }
            currentLevel++;
        }
        return result;
    }

    /**
     * 获取指定层级的所有节点
     *
     * @param level 目标层级（根节点为 0）
     * @return 该层级的所有节点
     */
    public List<T> getNodesByLevel(int level) {
        return filterByLevelAndPredicate(level, node -> true);
    }

    // ==================== 兄弟节点与同级节点 ====================

    /**
     * 获取节点的兄弟节点（不包括自身）
     *
     * @param node 目标节点
     * @return 兄弟节点列表
     */
    public List<T> getSiblings(T node) {
        ID nodeId = getNodeId(node);
        ID parentId = parentIndex.get(nodeId);

        if (parentId == null) {
            // 根节点没有兄弟
            return Collections.emptyList();
        }

        List<ID> siblingIds = childrenIndex.get(parentId);
        if (siblingIds == null || siblingIds.isEmpty()) {
            return Collections.emptyList();
        }

        return siblingIds.stream()
                .filter(id -> !id.equals(nodeId))
                .map(nodeIndex::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取与节点同层级的所有节点
     *
     * @param node 目标节点
     * @return 同层级的所有节点（包括自身）
     */
    public List<T> getSameLevelNodes(T node) {
        int level = getLevel(node);
        return getNodesByLevel(level);
    }

    /**
     * 转换为 Map
     */
    public Map<ID, T> toMap() {
        return new HashMap<>(nodeIndex);
    }

    // ==================== 工具方法 ====================

    private ID getNodeId(T node) {
        return idGetter.apply(node);
    }

    // ==================== Getters ====================

    public List<T> getAllNodes() {
        return Collections.unmodifiableList(allNodes);
    }

    public Map<ID, T> getNodeIndex() {
        return nodeIndex;
    }

    public Map<ID, ID> getParentIndex() {
        return parentIndex;
    }

    public Map<ID, List<ID>> getChildrenIndex() {
        return childrenIndex;
    }

    public List<ID> getRootIds() {
        return rootIds;
    }

    public Function<T, T> getCopier() {
        return copier;
    }
}
