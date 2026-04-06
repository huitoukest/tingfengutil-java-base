package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.bean.DefaultTreeNode;
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

    // ==================== UnsupportedOperationException 测试 ====================

    @Test(expected = UnsupportedOperationException.class)
    public void testMapThrows() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();
        helper.map(node -> node, children -> children);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFilterThrows() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();
        helper.filter(node -> true, (parent, children) -> children);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testMergeThrows() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();
        helper.merge(helper, (a, b) -> true, (a, b) -> a, (a, b) -> a);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSubtractThrows() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();
        helper.subtract(helper, (a, b) -> true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddThrows() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();
        helper.add(helper);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIntersectThrows() {
        TreeHelper<DefaultTreeNode, String> helper = buildHelper();
        helper.intersect(helper, (a, b) -> true, (a, b) -> a);
    }
}
