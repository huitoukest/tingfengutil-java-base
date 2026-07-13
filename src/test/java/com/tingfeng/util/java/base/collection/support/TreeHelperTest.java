package com.tingfeng.util.java.base.collection.support;

import com.tingfeng.util.java.base.collection.support.TreeHelper;
import com.tingfeng.util.java.base.collection.base.DefaultTreeNode;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TreeHelper 单元测试
 */
public class TreeHelperTest {

    // ==================== 测试辅助方法 ====================

    /**
     * 构建简单的树结构:
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

    private TreeHelper<DefaultTreeNode, String> buildHelper() {
        List<DefaultTreeNode> nodes = buildSimpleTree();
        return TreeHelper.of(nodes, DefaultTreeNode::getId, DefaultTreeNode::getParentId);
    }

    // ==================== 工厂方法测试 ====================

    @Test
    public void testOf() {
        List<DefaultTreeNode> nodes = buildSimpleTree();
        TreeHelper<DefaultTreeNode, String> helper = TreeHelper.of(nodes,
                DefaultTreeNode::getId, DefaultTreeNode::getParentId);

        Assert.assertNotNull(helper);
        Assert.assertEquals(5, helper.size());
    }

    @Test
    public void testEmpty() {
        TreeHelper<DefaultTreeNode, String> helper = TreeHelper.empty();

        Assert.assertNotNull(helper);
        Assert.assertTrue(helper.isEmpty());
        Assert.assertEquals(0, helper.size());
    }

    @Test
    public void testOfWithNullNodes() {
        TreeHelper<DefaultTreeNode, String> helper = TreeHelper.of(null,
                DefaultTreeNode::getId, DefaultTreeNode::getParentId);

        Assert.assertNotNull(helper);
        Assert.assertTrue(helper.isEmpty());
    }

    // ==================== 查询操作测试 ====================

    @Test
    public void testFindById() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        Optional<DefaultTreeNode> found = helper.findById("3");
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals("3", found.get().getId());
    }

    @Test
    public void testFindByIdNotFound() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        Optional<DefaultTreeNode> found = helper.findById("not-exist");
        Assert.assertFalse(found.isPresent());
    }

    @Test
    public void testFindOne() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        Optional<DefaultTreeNode> found = helper.findOne(node -> node.getSortValue() == 3);
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals("3", found.get().getId());
    }

    @Test
    public void testFindAll() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        List<DefaultTreeNode> found = helper.findAll(node -> node.getSortValue() >= 3);
        Assert.assertEquals(3, found.size());
    }

    @Test
    public void testGetRoots() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        List<DefaultTreeNode> roots = helper.getRoots();
        Assert.assertEquals(1, roots.size());
        Assert.assertEquals("1", roots.get(0).getId());
    }

    @Test
    public void testGetChildren() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();
        DefaultTreeNode node2 = helper.findById("2").get();

        List<DefaultTreeNode> children = helper.getChildren(node2);
        Assert.assertEquals(2, children.size());
        Assert.assertTrue(children.stream().anyMatch(c -> "4".equals(c.getId())));
        Assert.assertTrue(children.stream().anyMatch(c -> "5".equals(c.getId())));
    }

    @Test
    public void testGetParent() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();
        DefaultTreeNode node4 = helper.findById("4").get();

        Optional<DefaultTreeNode> parent = helper.getParent(node4);
        Assert.assertTrue(parent.isPresent());
        Assert.assertEquals("2", parent.get().getId());
    }

    @Test
    public void testGetParentOfRoot() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();
        DefaultTreeNode root = helper.findById("1").get();

        Optional<DefaultTreeNode> parent = helper.getParent(root);
        Assert.assertFalse(parent.isPresent());
    }

    @Test
    public void testGetLeaves() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        List<DefaultTreeNode> leaves = helper.getLeaves();
        Assert.assertEquals(3, leaves.size());
        Assert.assertTrue(leaves.stream().anyMatch(l -> "4".equals(l.getId())));
        Assert.assertTrue(leaves.stream().anyMatch(l -> "5".equals(l.getId())));
        Assert.assertTrue(leaves.stream().anyMatch(l -> "3".equals(l.getId())));
    }

    @Test
    public void testGetPathToRoot() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();
        DefaultTreeNode node4 = helper.findById("4").get();

        List<DefaultTreeNode> path = helper.getPathToRoot(node4);
        Assert.assertEquals(3, path.size());
        Assert.assertEquals("1", path.get(0).getId());
        Assert.assertEquals("2", path.get(1).getId());
        Assert.assertEquals("4", path.get(2).getId());
    }

    @Test
    public void testGetLevel() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        Assert.assertEquals(0, helper.getLevel(helper.findById("1").get()));
        Assert.assertEquals(1, helper.getLevel(helper.findById("2").get()));
        Assert.assertEquals(2, helper.getLevel(helper.findById("4").get()));
    }

    @Test
    public void testGetDepth() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        Assert.assertEquals(3, helper.getDepth()); // 1 -> 2 -> 4
    }

    @Test
    public void testSort() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        // 按 sortValue 降序排序 (b.sortValue - a.sortValue)
        TreeHelper<DefaultTreeNode, String> sorted = helper.sort(
                (a, b) -> Integer.compare(b.getSortValue(), a.getSortValue()));

        // 原 helper 不受影响
        Assert.assertEquals(3, helper.getDepth());
        Assert.assertEquals("1", helper.getRoots().get(0).getId());

        // 排序后：根节点 1 的直接子节点应该按 sortValue 降序排列
        // buildSimpleTree: node2(sv=2), node3(sv=3) -> 降序: node3, node2
        List<DefaultTreeNode> children = sorted.getChildren(sorted.getRoots().get(0));
        Assert.assertEquals(2, children.size());
        Assert.assertEquals("3", children.get(0).getId()); // sortValue=3 > sortValue=2
        Assert.assertEquals("2", children.get(1).getId()); // sortValue=2 < sortValue=3
    }

    @Test
    public void testSortImmutable() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        // 排序返回新实例
        TreeHelper<DefaultTreeNode, String> sorted = helper.sort(
                Comparator.comparingInt(DefaultTreeNode::getSortValue));

        // 原实例不变（返回的是新实例）
        Assert.assertNotSame(helper, sorted);
        // 原有根节点顺序不变
        Assert.assertEquals("1", helper.getRoots().get(0).getId());
    }

    // ==================== 展平方法测试 ====================

    @Test
    public void testFlattenDepthFirst() {
        // buildSimpleTree:
        //       1
        //      / \
        //     2   3
        //    / \
        //   4   5
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        List<DefaultTreeNode> result = helper.flattenDepthFirst();

        Assert.assertEquals(5, result.size());
        // 深度优先: 1 -> 2 -> 4 -> 5 -> 3
        Assert.assertEquals("1", result.get(0).getId());
        Assert.assertEquals("2", result.get(1).getId());
        Assert.assertEquals("4", result.get(2).getId());
        Assert.assertEquals("5", result.get(3).getId());
        Assert.assertEquals("3", result.get(4).getId());
    }

    @Test
    public void testFlattenBreadthFirst() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        List<DefaultTreeNode> result = helper.flattenBreadthFirst();

        Assert.assertEquals(5, result.size());
        // 广度优先: 1 -> 2 -> 3 -> 4 -> 5
        Assert.assertEquals("1", result.get(0).getId());
        Assert.assertEquals("2", result.get(1).getId());
        Assert.assertEquals("3", result.get(2).getId());
        Assert.assertEquals("4", result.get(3).getId());
        Assert.assertEquals("5", result.get(4).getId());
    }

    @Test
    public void testFlattenSorted() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        // 按 sortValue 降序排序展开
        List<DefaultTreeNode> result = helper.flattenSorted(
                (a, b) -> Integer.compare(b.getSortValue(), a.getSortValue()));

        Assert.assertEquals(5, result.size());
        // 深度优先 + 降序: 1 -> 3(sv=3) -> 2(sv=2) -> 5(sv=5) -> 4(sv=4)
        // 子节点内部排序: 3 在前，2 的子节点 5,4 排序后 5,4
        Assert.assertEquals("1", result.get(0).getId());
    }

    @Test
    public void testGetNodesByLevel() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        // Level 0: root (1)
        List<DefaultTreeNode> level0 = helper.getNodesByLevel(0);
        Assert.assertEquals(1, level0.size());
        Assert.assertEquals("1", level0.get(0).getId());

        // Level 1: 2, 3
        List<DefaultTreeNode> level1 = helper.getNodesByLevel(1);
        Assert.assertEquals(2, level1.size());

        // Level 2: 4, 5
        List<DefaultTreeNode> level2 = helper.getNodesByLevel(2);
        Assert.assertEquals(2, level2.size());

        // Level 3: 不存在
        List<DefaultTreeNode> level3 = helper.getNodesByLevel(3);
        Assert.assertEquals(0, level3.size());
    }

    @Test
    public void testFilterByLevelAndPredicate() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        // Level 1 中 sortValue > 2 的节点
        List<DefaultTreeNode> result = helper.filterByLevelAndPredicate(
                1, node -> node.getSortValue() > 2);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("3", result.get(0).getId()); // sortValue=3
    }

    @Test
    public void testGetSiblings() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        // node 2 的兄弟节点是 node 3
        DefaultTreeNode node2 = helper.findById("2").get();
        List<DefaultTreeNode> siblings = helper.getSiblings(node2);

        Assert.assertEquals(1, siblings.size());
        Assert.assertEquals("3", siblings.get(0).getId());
    }

    @Test
    public void testGetSiblingsRootNode() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        // 根节点没有兄弟节点
        DefaultTreeNode root = helper.getRoots().get(0);
        List<DefaultTreeNode> siblings = helper.getSiblings(root);

        Assert.assertEquals(0, siblings.size());
    }

    @Test
    public void testGetSameLevelNodes() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        // node 2 在 level 1，同级节点有 2 和 3
        DefaultTreeNode node2 = helper.findById("2").get();
        List<DefaultTreeNode> sameLevel = helper.getSameLevelNodes(node2);

        Assert.assertEquals(2, sameLevel.size());
    }

    // ==================== 转换操作测试 ====================

    @Test
    public void testToList() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        List<DefaultTreeNode> list = helper.toList();
        Assert.assertEquals(5, list.size());
    }

    @Test
    public void testFlatten() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        List<DefaultTreeNode> flat = helper.flatten();
        Assert.assertEquals(5, flat.size());
        // Root should be first
        Assert.assertEquals("1", flat.get(0).getId());
    }

    @Test
    public void testToMap() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        Map<String, DefaultTreeNode> map = helper.toMap();
        Assert.assertEquals(5, map.size());
        Assert.assertTrue(map.containsKey("1"));
        Assert.assertTrue(map.containsKey("2"));
        Assert.assertTrue(map.containsKey("3"));
    }

    // ==================== Getters 测试 ====================

    @Test
    public void testGetAllNodes() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        List<DefaultTreeNode> allNodes = helper.getAllNodes();
        Assert.assertEquals(5, allNodes.size());
        // Should be unmodifiable
        try {
            allNodes.add(new DefaultTreeNode());
            Assert.fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testGetNodeIndex() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        Map<String, DefaultTreeNode> index = helper.getNodeIndex();
        Assert.assertEquals(5, index.size());
        // Should be unmodifiable
        try {
            index.put("new", new DefaultTreeNode());
            Assert.fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testGetRootIds() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();

        List<String> rootIds = helper.getRootIds();
        Assert.assertEquals(1, rootIds.size());
        Assert.assertEquals("1", rootIds.get(0));
    }

}