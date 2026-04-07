package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.bean.DefaultTreeNode;
import com.tingfeng.util.java.base.common.helper.TreeHelper;
import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionROne;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;

/**
 * TreeUtils 单元测试
 */
public class TreeUtilsTest {

    // ==================== 测试辅助方法 ====================

    /**
     * 构建简单的树结构（单根）:
     *       1
     *      / \
     *     2   3
     *    / \
     *   4   5
     */
    private List<DefaultTreeNode> buildSimpleTree() {
        List<DefaultTreeNode> nodes = new ArrayList<>();
        nodes.add(new DefaultTreeNode("1", null, 1));
        nodes.add(new DefaultTreeNode("2", "1", 2));
        nodes.add(new DefaultTreeNode("3", "1", 3));
        nodes.add(new DefaultTreeNode("4", "2", 4));
        nodes.add(new DefaultTreeNode("5", "2", 5));
        return nodes;
    }

    /**
     * 构建复杂树结构（多根 / 森林）:
     *     1     6
     *    / \   / \
     *   2   3 7   8
     *  / \
     * 4   5
     *
     * 业界处理多根的方式：
     * 1. 虚拟根节点 - 添加一个假根作为所有真实根的父节点（DOM/XML 常用）
     * 2. 根节点列表 API - 直接接受 List<T> 而非单一根（当前 TreeUtils 的方式）
     * 3. 严格单根约束 - 检测到多根时抛出异常
     * 4. 无父节点即根 - 把没有父节点的节点视为独立树的根
     *
     * TreeUtils 采用方式2：所有方法接受 List<T> roots，自动处理森林场景
     */
    private List<DefaultTreeNode> buildComplexTree() {
        List<DefaultTreeNode> nodes = new ArrayList<>();
        // 第一棵树
        nodes.add(new DefaultTreeNode("1", null, 1));
        nodes.add(new DefaultTreeNode("2", "1", 2));
        nodes.add(new DefaultTreeNode("3", "1", 3));
        nodes.add(new DefaultTreeNode("4", "2", 4));
        nodes.add(new DefaultTreeNode("5", "2", 5));
        // 第二棵树
        nodes.add(new DefaultTreeNode("6", null, 6));
        nodes.add(new DefaultTreeNode("7", "6", 7));
        nodes.add(new DefaultTreeNode("8", "6", 8));
        return nodes;
    }

    private final Function<DefaultTreeNode, List<DefaultTreeNode>> childrenGetter = DefaultTreeNode::getChildren;

    // ==================== depthFirst 测试 ====================

    @Test
    public void testDepthFirstSimple() {
        List<DefaultTreeNode> treeNodes = buildSimpleTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        List<DefaultTreeNode> result = TreeUtils.depthFirst(tree, childrenGetter);

        Assert.assertEquals(5, result.size());
        // 深度优先: 1 -> 2 -> 4 -> 5 -> 3
        Assert.assertEquals("1", result.get(0).getId());
        Assert.assertEquals("2", result.get(1).getId());
        Assert.assertEquals("4", result.get(2).getId());
        Assert.assertEquals("5", result.get(3).getId());
        Assert.assertEquals("3", result.get(4).getId());
    }

    @Test
    public void testDepthFirstEmpty() {
        List<DefaultTreeNode> result = TreeUtils.depthFirst(Collections.emptyList(), childrenGetter);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testDepthFirstNull() {
        List<DefaultTreeNode> result = TreeUtils.depthFirst(null, childrenGetter);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testDepthFirstSingleNode() {
        List<DefaultTreeNode> treeNodes = Collections.singletonList(new DefaultTreeNode("1", null, 1));
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        List<DefaultTreeNode> result = TreeUtils.depthFirst(tree, childrenGetter);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("1", result.get(0).getId());
    }

    // ==================== breadthFirst 测试 ====================

    @Test
    public void testBreadthFirstSimple() {
        List<DefaultTreeNode> treeNodes = buildSimpleTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        List<DefaultTreeNode> result = TreeUtils.breadthFirst(tree, childrenGetter);

        Assert.assertEquals(5, result.size());
        // 广度优先: 1 -> 2 -> 3 -> 4 -> 5
        Assert.assertEquals("1", result.get(0).getId());
        Assert.assertEquals("2", result.get(1).getId());
        Assert.assertEquals("3", result.get(2).getId());
        Assert.assertEquals("4", result.get(3).getId());
        Assert.assertEquals("5", result.get(4).getId());
    }

    @Test
    public void testBreadthFirstEmpty() {
        List<DefaultTreeNode> result = TreeUtils.breadthFirst(Collections.emptyList(), childrenGetter);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testBreadthFirstNull() {
        List<DefaultTreeNode> result = TreeUtils.breadthFirst(null, childrenGetter);
        Assert.assertTrue(result.isEmpty());
    }

    // ==================== traverseAndCollect 测试 ====================

    @Test
    public void testTraverseAndCollect() {
        List<DefaultTreeNode> treeNodes = buildSimpleTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        List<String> result = TreeUtils.traverseAndCollect(tree, childrenGetter,
                (node, level) -> node.getId() + "-" + level);

        Assert.assertEquals(5, result.size());
        Assert.assertEquals("1-0", result.get(0));
        Assert.assertEquals("2-1", result.get(1));
        Assert.assertEquals("3-1", result.get(2));
        Assert.assertEquals("4-2", result.get(3));
        Assert.assertEquals("5-2", result.get(4));
    }

    @Test
    public void testTraverseAndCollectEmpty() {
        List<String> result = TreeUtils.traverseAndCollect(Collections.emptyList(), childrenGetter,
                (node, level) -> node.getId());
        Assert.assertTrue(result.isEmpty());
    }

    // ==================== traverseUntil 测试 ====================

    @Test
    public void testTraverseUntilFound() {
        List<DefaultTreeNode> treeNodes = buildSimpleTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        Optional<DefaultTreeNode> result = TreeUtils.traverseUntil(tree, childrenGetter,
                node -> "5".equals(node.getId()));

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals("5", result.get().getId());
    }

    @Test
    public void testTraverseUntilNotFound() {
        List<DefaultTreeNode> treeNodes = buildSimpleTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        Optional<DefaultTreeNode> result = TreeUtils.traverseUntil(tree, childrenGetter,
                node -> "not-exist".equals(node.getId()));

        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void testTraverseUntilEmpty() {
        Optional<DefaultTreeNode> result = TreeUtils.traverseUntil(Collections.emptyList(), childrenGetter,
                node -> true);
        Assert.assertFalse(result.isPresent());
    }

    // ==================== count 测试 ====================

    @Test
    public void testCountSimple() {
        List<DefaultTreeNode> treeNodes = buildSimpleTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        int count = TreeUtils.count(tree, childrenGetter);

        Assert.assertEquals(5, count);
    }

    @Test
    public void testCountEmpty() {
        int count = TreeUtils.count(Collections.emptyList(), childrenGetter);
        Assert.assertEquals(0, count);
    }

    @Test
    public void testCountNull() {
        int count = TreeUtils.count(null, childrenGetter);
        Assert.assertEquals(0, count);
    }

    @Test
    public void testCountSingleNode() {
        List<DefaultTreeNode> treeNodes = Collections.singletonList(new DefaultTreeNode("1", null, 1));
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        int count = TreeUtils.count(tree, childrenGetter);

        Assert.assertEquals(1, count);
    }

    // ==================== depth 测试 ====================

    @Test
    public void testDepthSimple() {
        List<DefaultTreeNode> treeNodes = buildSimpleTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        int depth = TreeUtils.depth(tree.get(0), childrenGetter);

        Assert.assertEquals(3, depth); // 1 -> 2 -> 4 (depth=3)
    }

    @Test
    public void testDepthEmpty() {
        int depth = TreeUtils.depth(null, childrenGetter);
        Assert.assertEquals(0, depth);
    }

    @Test
    public void testDepthSingleNode() {
        DefaultTreeNode single = new DefaultTreeNode("1", null, 1);
        int depth = TreeUtils.depth(single, childrenGetter);
        Assert.assertEquals(1, depth);
    }

    // ==================== map 测试 ====================

    @Test
    public void testMapSimple() {
        List<DefaultTreeNode> treeNodes = buildSimpleTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        List<String> result = TreeUtils.map(tree, childrenGetter, node -> node.getId() + "-mapped");

        Assert.assertEquals(5, result.size());
        Assert.assertTrue(result.stream().allMatch(s -> s.endsWith("-mapped")));
    }

    @Test
    public void testMapEmpty() {
        List<String> result = TreeUtils.map(Collections.emptyList(), childrenGetter, DefaultTreeNode::getId);
        Assert.assertTrue(result.isEmpty());
    }

    // ==================== filter 测试 ====================

    @Test
    public void testFilterSimple() {
        List<DefaultTreeNode> treeNodes = buildSimpleTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        List<DefaultTreeNode> result = TreeUtils.filter(tree, childrenGetter,
                node -> node.getSortValue() <= 3);

        Assert.assertEquals(3, result.size());
    }

    @Test
    public void testFilterEmpty() {
        List<DefaultTreeNode> result = TreeUtils.filter(Collections.emptyList(), childrenGetter, node -> true);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testFilterNoneMatch() {
        List<DefaultTreeNode> treeNodes = buildSimpleTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        List<DefaultTreeNode> result = TreeUtils.filter(tree, childrenGetter, node -> false);

        Assert.assertEquals(0, result.size());
    }

    // ==================== sort 测试 ====================

    @Test
    public void testSortSimple() {
        List<DefaultTreeNode> treeNodes = buildSimpleTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        List<DefaultTreeNode> sorted = TreeUtils.sort(tree, childrenGetter,
                (a, b) -> Integer.compare(b.getSortValue(), a.getSortValue())); // 降序

        // buildSimpleTree 只有一个根节点 (node 1)，排序后不变
        Assert.assertEquals("1", sorted.get(0).getId());
    }

    // ==================== getTreeList (backward compatible) 测试 ====================

    @Test
    public void testGetTreeListWithComparator() {
        List<DefaultTreeNode> treeNodes = buildSimpleTree();

        List<DefaultTreeNode> tree = TreeUtils.getTreeList(treeNodes,
                Comparator.comparingInt(DefaultTreeNode::getSortValue));

        Assert.assertEquals(1, tree.size());
        Assert.assertEquals("1", tree.get(0).getId());
        Assert.assertEquals(2, tree.get(0).getChildren().size());
    }

    @Test
    public void testGetTreeListWithCallbacks() {
        List<DefaultTreeNode> treeNodes = buildSimpleTree();

        List<DefaultTreeNode> tree = TreeUtils.getTreeList(treeNodes,
                (child, parent) -> parent.getChildren().add(child),
                (child, parent) -> Objects.equals(child.getParentId(), parent.getId()),
                DefaultTreeNode::getId);

        Assert.assertEquals(1, tree.size());
        Assert.assertEquals("1", tree.get(0).getId());
    }

    @Test
    public void testGetTreeListEmpty() {
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(Collections.emptyList(),
                Comparator.comparingInt(DefaultTreeNode::getSortValue));
        Assert.assertTrue(tree.isEmpty());
    }

    @Test
    public void testGetTreeListNull() {
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(null,
                Comparator.comparingInt(DefaultTreeNode::getSortValue));
        Assert.assertTrue(tree.isEmpty());
    }

    // ==================== flatList 测试 ====================

    @Test
    public void testFlatListSimple() {
        List<DefaultTreeNode> treeNodes = buildSimpleTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        FunctionROne<List<DefaultTreeNode>, DefaultTreeNode> getChildren = DefaultTreeNode::getChildren;
        FunctionROne<DefaultTreeNode, DefaultTreeNode> getSelf = n -> n;

        List<DefaultTreeNode> flat = TreeUtils.flatList(tree, getChildren, getSelf);

        Assert.assertEquals(5, flat.size());
    }

    @Test
    public void testFlatListEmpty() {
        FunctionROne<List<DefaultTreeNode>, DefaultTreeNode> getChildren = DefaultTreeNode::getChildren;
        FunctionROne<DefaultTreeNode, DefaultTreeNode> getSelf = n -> n;

        List<DefaultTreeNode> flat = TreeUtils.flatList(Collections.emptyList(), getChildren, getSelf);
        Assert.assertTrue(flat.isEmpty());
    }

    // ==================== helper 工厂方法测试 ====================

    @Test
    public void testHelperFactory() {
        List<DefaultTreeNode> nodes = buildSimpleTree();

        TreeHelper<DefaultTreeNode, String> helper = TreeUtils.helper(nodes, DefaultTreeNode::getId, DefaultTreeNode::getParentId);

        Assert.assertNotNull(helper);
        Assert.assertEquals(5, helper.size());
    }

    @Test
    public void testHelperFactoryWithCopier() {
        List<DefaultTreeNode> nodes = buildSimpleTree();

        TreeHelper<DefaultTreeNode, String> helper = TreeUtils.helper(nodes, DefaultTreeNode::getId, DefaultTreeNode::getParentId,
                node -> {
                    DefaultTreeNode copy = new DefaultTreeNode();
                    copy.setId(node.getId());
                    copy.setParentId(node.getParentId());
                    copy.setSortValue(node.getSortValue());
                    return copy;
                });

        Assert.assertNotNull(helper);
        Assert.assertEquals(5, helper.size());
    }

    // ==================== 复杂树（多根/森林）测试 ====================

    @Test
    public void testComplexTreeGetTreeList() {
        List<DefaultTreeNode> nodes = buildComplexTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(nodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        // 多根场景：应该有 2 个根节点 (node 1 和 node 6)
        Assert.assertEquals(2, tree.size());
        Assert.assertEquals("1", tree.get(0).getId());
        Assert.assertEquals("6", tree.get(1).getId());
    }

    @Test
    public void testComplexTreeDepthFirst() {
        List<DefaultTreeNode> nodes = buildComplexTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(nodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        List<DefaultTreeNode> result = TreeUtils.depthFirst(tree, childrenGetter);

        Assert.assertEquals(8, result.size());
        // 深度优先遍历两棵树: 1 -> 2 -> 4 -> 5 -> 3 -> 6 -> 7 -> 8
        Assert.assertEquals("1", result.get(0).getId());
        Assert.assertEquals("2", result.get(1).getId());
        Assert.assertEquals("4", result.get(2).getId());
        Assert.assertEquals("5", result.get(3).getId());
        Assert.assertEquals("3", result.get(4).getId());
        Assert.assertEquals("6", result.get(5).getId());
        Assert.assertEquals("7", result.get(6).getId());
        Assert.assertEquals("8", result.get(7).getId());
    }

    @Test
    public void testComplexTreeBreadthFirst() {
        List<DefaultTreeNode> nodes = buildComplexTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(nodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        List<DefaultTreeNode> result = TreeUtils.breadthFirst(tree, childrenGetter);

        Assert.assertEquals(8, result.size());
        // 广度优先遍历森林:
        // Level 0: 1, 6
        // Level 1: 2, 3, 7, 8
        // Level 2: 4
        // BFS顺序: 1 -> 6 -> 2 -> 3 -> 7 -> 8 -> 4
        Assert.assertEquals("1", result.get(0).getId());
        Assert.assertEquals("6", result.get(1).getId());
        Assert.assertEquals("2", result.get(2).getId());
        Assert.assertEquals("3", result.get(3).getId());
        Assert.assertEquals("7", result.get(4).getId());
        Assert.assertEquals("8", result.get(5).getId());
        Assert.assertEquals("4", result.get(6).getId());
    }

    @Test
    public void testComplexTreeSort() {
        List<DefaultTreeNode> nodes = buildComplexTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(nodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        // 按 sortValue 降序排序，根节点排序后 node6 (sortValue=6) 应该在前面
        List<DefaultTreeNode> sorted = TreeUtils.sort(tree, childrenGetter,
                (a, b) -> Integer.compare(b.getSortValue(), a.getSortValue()));

        Assert.assertEquals("6", sorted.get(0).getId()); // sortValue=6 最大
        Assert.assertEquals("1", sorted.get(1).getId()); // sortValue=1 最小
    }

    @Test
    public void testComplexTreeTraverseAndCollect() {
        List<DefaultTreeNode> nodes = buildComplexTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(nodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        List<String> result = TreeUtils.traverseAndCollect(tree, childrenGetter,
                (node, level) -> node.getId() + "-" + level);

        Assert.assertEquals(8, result.size());
        // 广度优先遍历森林:
        // Level 0: 1, 6 -> 1-0, 6-0
        // Level 1: 2, 3, 7, 8 -> 2-1, 3-1, 7-1, 8-1
        // Level 2: 4 -> 4-2
        Assert.assertEquals("1-0", result.get(0));
        Assert.assertEquals("6-0", result.get(1));
        Assert.assertEquals("2-1", result.get(2));
        Assert.assertEquals("3-1", result.get(3));
        Assert.assertEquals("7-1", result.get(4));
        Assert.assertEquals("8-1", result.get(5));
        Assert.assertEquals("4-2", result.get(6));
    }

    @Test
    public void testComplexTreeCount() {
        List<DefaultTreeNode> nodes = buildComplexTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(nodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        int count = TreeUtils.count(tree, childrenGetter);

        Assert.assertEquals(8, count);
    }

    @Test
    public void testComplexTreeDepth() {
        List<DefaultTreeNode> nodes = buildComplexTree();
        List<DefaultTreeNode> tree = TreeUtils.getTreeList(nodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));

        // 第一棵树深度: 1 -> 2 -> 4 (depth=3)
        Assert.assertEquals(3, TreeUtils.depth(tree.get(0), childrenGetter));
        // 第二棵树深度: 6 -> 7 (depth=2)
        Assert.assertEquals(2, TreeUtils.depth(tree.get(1), childrenGetter));
    }
}
