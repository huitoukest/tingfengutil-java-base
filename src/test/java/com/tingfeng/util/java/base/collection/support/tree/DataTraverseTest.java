package com.tingfeng.util.java.base.collection.support.tree;

import com.tingfeng.util.java.base.collection.base.TreeTraverseContext;
import com.tingfeng.util.java.base.collection.support.tree.DataNodeTraversePolicy;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.*;

/**
 * 树遍历策略框架综合测试
 *
 * <p>覆盖三种遍历策略（DataFirstTraverse / DataLastTraverse / DataAfterTraverse），
 * 两种方向（正序/逆序），以及自定义策略、遍历中断、空节点、边界场景等。</p>
 */
public class DataTraverseTest {

    // ==================== 测试辅助类型 ====================

    /**
     * 简单的树节点，用于测试
     */
    static class TNode {
        final String id;
        final List<TNode> children = new ArrayList<>();

        TNode(String id) {
            this.id = id;
        }

        TNode add(TNode child) {
            children.add(child);
            return this;
        }

        @Override
        public String toString() {
            return id;
        }
    }

    /**
     * 收集遍历结果的辅助类
     */
    static class TraverseResult {
        final List<String> order = new ArrayList<>();
        final Map<String, Integer> levels = new HashMap<>();

        Predicate<TreeTraverseContext<TNode>> collector() {
            return ctx -> {
                order.add(ctx.getNode().id);
                levels.put(ctx.getNode().id, ctx.getLevel());
                return true;
            };
        }
    }

    static final Function<TNode, List<TNode>> CHILDREN_GETTER = n -> n.children;

    // ==================== 树构建方法 ====================

    /**
     * 构建完整的二叉树（3层）：
     * <pre>
     *        A
     *      /   \
     *     B     C
     *    / \   / \
     *   D   E F   G
     * </pre>
     */
    static List<TNode> binaryTree() {
        TNode a = new TNode("A");
        TNode b = new TNode("B");
        TNode c = new TNode("C");
        TNode d = new TNode("D");
        TNode e = new TNode("E");
        TNode f = new TNode("F");
        TNode g = new TNode("G");
        a.add(b);
        a.add(c);
        b.add(d);
        b.add(e);
        c.add(f);
        c.add(g);
        return Collections.singletonList(a);
    }

    /**
     * 构建链表状树（4层）：
     * <pre>
     * A -&gt; B -&gt; C -&gt; D
     * </pre>
     */
    static List<TNode> linkedList() {
        TNode a = new TNode("A");
        TNode b = new TNode("B");
        TNode c = new TNode("C");
        TNode d = new TNode("D");
        a.add(b);
        b.add(c);
        c.add(d);
        return Collections.singletonList(a);
    }

    // ================================================================
    //  DataFirstTraverse - 先序遍历
    // ================================================================

    @Test
    public void first_forward_binaryTree() {
        DataFirstTraverse t = new DataFirstTraverse(false);
        TraverseResult r = new TraverseResult();
        t.traverse(binaryTree(), 0, null, CHILDREN_GETTER, r.collector());

        // 先序正序：根 -> 左 -> 右
        assertEquals(Arrays.asList("A", "B", "D", "E", "C", "F", "G"), r.order);
        // level 参数验证（H-2）：level=0 传入，节点上下文 level = level传入值 + 1
        assertEquals(1, (int) r.levels.get("A"));
        assertEquals(2, (int) r.levels.get("B"));
        assertEquals(3, (int) r.levels.get("D"));
        assertEquals(3, (int) r.levels.get("E"));
        assertEquals(2, (int) r.levels.get("C"));
        assertEquals(3, (int) r.levels.get("F"));
        assertEquals(3, (int) r.levels.get("G"));
    }

    @Test
    public void first_reverse_binaryTree() {
        DataFirstTraverse t = new DataFirstTraverse(true);
        TraverseResult r = new TraverseResult();
        t.traverse(binaryTree(), 0, null, CHILDREN_GETTER, r.collector());

        // 先序逆序：根 -> 右 -> 左
        assertEquals(Arrays.asList("A", "C", "G", "F", "B", "E", "D"), r.order);
        // level 连续验证
        assertEquals(1, (int) r.levels.get("A"));
        assertEquals(2, (int) r.levels.get("B"));
        assertEquals(2, (int) r.levels.get("C"));
        assertEquals(3, (int) r.levels.get("D"));
        assertEquals(3, (int) r.levels.get("E"));
        assertEquals(3, (int) r.levels.get("F"));
        assertEquals(3, (int) r.levels.get("G"));
    }

    @Test
    public void first_forward_linkedList() {
        DataFirstTraverse t = new DataFirstTraverse(false);
        TraverseResult r = new TraverseResult();
        t.traverse(linkedList(), 0, null, CHILDREN_GETTER, r.collector());

        // 链表先序正序：A -> B -> C -> D
        assertEquals(Arrays.asList("A", "B", "C", "D"), r.order);
        assertEquals(1, (int) r.levels.get("A"));
        assertEquals(2, (int) r.levels.get("B"));
        assertEquals(3, (int) r.levels.get("C"));
        assertEquals(4, (int) r.levels.get("D"));
    }

    @Test
    public void first_reverse_linkedList() {
        DataFirstTraverse t = new DataFirstTraverse(true);
        TraverseResult r = new TraverseResult();
        t.traverse(linkedList(), 0, null, CHILDREN_GETTER, r.collector());

        // 链表每个节点只有一个子节点，正序逆序遍历顺序相同
        assertEquals(Arrays.asList("A", "B", "C", "D"), r.order);
    }

    @Test
    public void first_singleNode() {
        DataFirstTraverse t = new DataFirstTraverse(false);
        TraverseResult r = new TraverseResult();
        TNode single = new TNode("X");
        t.traverse(Collections.singletonList(single), 0, null, CHILDREN_GETTER, r.collector());

        assertEquals(Collections.singletonList("X"), r.order);
        assertEquals(1, (int) r.levels.get("X"));
    }

    @Test
    public void first_emptyList() {
        DataFirstTraverse t = new DataFirstTraverse(false);
        TraverseResult r = new TraverseResult();
        t.traverse(Collections.emptyList(), 0, null, CHILDREN_GETTER, r.collector());

        assertTrue("空列表不应触发任何遍历回调", r.order.isEmpty());
    }

    @Test
    public void first_reverse_emptyList() {
        DataFirstTraverse t = new DataFirstTraverse(true);
        TraverseResult r = new TraverseResult();
        t.traverse(Collections.emptyList(), 0, null, CHILDREN_GETTER, r.collector());

        assertTrue("空列表逆序遍历也不应触发任何回调", r.order.isEmpty());
    }

    @Test
    public void first_forward_nullChildrenGetter() {
        DataFirstTraverse t = new DataFirstTraverse(false);
        TraverseResult r = new TraverseResult();
        TNode node = new TNode("X");
        // childrenGetter 返回 null 时不应抛出异常
        t.traverse(Collections.singletonList(node), 0, null, n -> null, r.collector());

        assertEquals(Collections.singletonList("X"), r.order);
        assertEquals(1, (int) r.levels.get("X"));
    }

    // ================================================================
    //  DataLastTraverse - 后序遍历
    // ================================================================

    @Test
    public void last_forward_binaryTree() {
        DataLastTraverse t = new DataLastTraverse(false);
        TraverseResult r = new TraverseResult();
        t.traverse(binaryTree(), 0, null, CHILDREN_GETTER, r.collector());

        // 后序正序：左 -> 右 -> 根
        assertEquals(Arrays.asList("D", "E", "B", "F", "G", "C", "A"), r.order);
    }

    @Test
    public void last_reverse_binaryTree() {
        DataLastTraverse t = new DataLastTraverse(true);
        TraverseResult r = new TraverseResult();
        t.traverse(binaryTree(), 0, null, CHILDREN_GETTER, r.collector());

        // 后序逆序：右 -> 左 -> 根
        assertEquals(Arrays.asList("G", "F", "C", "E", "D", "B", "A"), r.order);
    }

    @Test
    public void last_forward_linkedList() {
        DataLastTraverse t = new DataLastTraverse(false);
        TraverseResult r = new TraverseResult();
        t.traverse(linkedList(), 0, null, CHILDREN_GETTER, r.collector());

        // 后序正序：D -> C -> B -> A
        assertEquals(Arrays.asList("D", "C", "B", "A"), r.order);
    }

    @Test
    public void last_reverse_linkedList() {
        DataLastTraverse t = new DataLastTraverse(true);
        TraverseResult r = new TraverseResult();
        t.traverse(linkedList(), 0, null, CHILDREN_GETTER, r.collector());

        // 链表每个节点只有一个子节点，后序正逆序相同
        assertEquals(Arrays.asList("D", "C", "B", "A"), r.order);
    }

    @Test
    public void last_singleNode() {
        DataLastTraverse t = new DataLastTraverse(false);
        TraverseResult r = new TraverseResult();
        TNode single = new TNode("X");
        t.traverse(Collections.singletonList(single), 0, null, CHILDREN_GETTER, r.collector());

        assertEquals(Collections.singletonList("X"), r.order);
    }

    @Test
    public void last_emptyList() {
        DataLastTraverse t = new DataLastTraverse(false);
        TraverseResult r = new TraverseResult();
        t.traverse(Collections.emptyList(), 0, null, CHILDREN_GETTER, r.collector());

        assertTrue("空列表不应触发任何遍历回调", r.order.isEmpty());
    }

    // ================================================================
    //  M-4 回归：后序遍历中途停止，验证返回值反映实际终止状态
    // ================================================================

    @Test
    public void last_stopMidTraversal_linkedList() {
        DataLastTraverse t = new DataLastTraverse(false);
        List<String> visited = new ArrayList<>();
        final int[] counter = {0};
        final int stopAfter = 2;

        Predicate<TreeTraverseContext<TNode>> stopper = ctx -> {
            visited.add(ctx.getNode().id);
            counter[0]++;
            return counter[0] < stopAfter;
        };

        t.traverse(linkedList(), 0, null, CHILDREN_GETTER, stopper);

        // 后序正序 D -> C -> B -> A, stopAfter=2 则只会访问 D, C
        assertEquals("M-4: 中途停止后不应继续遍历", Arrays.asList("D", "C"), visited);
    }

    @Test
    public void last_stopMidTraversal_binaryTree() {
        DataLastTraverse t = new DataLastTraverse(false);
        List<String> visited = new ArrayList<>();
        final int[] counter = {0};
        final int stopAfter = 3;

        Predicate<TreeTraverseContext<TNode>> stopper = ctx -> {
            visited.add(ctx.getNode().id);
            counter[0]++;
            return counter[0] < stopAfter;
        };

        t.traverse(binaryTree(), 0, null, CHILDREN_GETTER, stopper);

        // 后序正序 D -> E -> B -> ...，stopAfter=3 则只会访问 D, E, B
        assertEquals("M-4: 后序二叉树中途停止只访问前3个节点",
                Arrays.asList("D", "E", "B"), visited);
    }

    @Test
    public void last_stopOnFirstNode() {
        DataLastTraverse t = new DataLastTraverse(false);
        List<String> visited = new ArrayList<>();

        Predicate<TreeTraverseContext<TNode>> stopper = ctx -> {
            visited.add(ctx.getNode().id);
            return false; // 第一个节点就停止
        };

        t.traverse(binaryTree(), 0, null, CHILDREN_GETTER, stopper);

        assertEquals("第一个节点即停止，只能访问D", Collections.singletonList("D"), visited);
    }

    @Test
    public void first_stopMidTraversal() {
        DataFirstTraverse t = new DataFirstTraverse(false);
        List<String> visited = new ArrayList<>();
        final int[] counter = {0};
        final int stopAfter = 3;

        Predicate<TreeTraverseContext<TNode>> stopper = ctx -> {
            visited.add(ctx.getNode().id);
            counter[0]++;
            return counter[0] < stopAfter;
        };

        t.traverse(binaryTree(), 0, null, CHILDREN_GETTER, stopper);

        // 先序正序 A -> B -> D -> ...，stopAfter=3 则只会访问 A, B, D
        assertEquals("先序二叉树中途停止只访问前3个节点",
                Arrays.asList("A", "B", "D"), visited);
    }

    // ================================================================
    //  DataAfterTraverse - 中序/策略遍历（H-3 回归）
    //  默认策略 AFTER_ONE_CHILD_NODE
    // ================================================================

    @Test
    public void after_forward_binaryTree_defaultPolicy() {
        DataAfterTraverse t = new DataAfterTraverse(false);
        TraverseResult r = new TraverseResult();
        // 使用 6 参数版本（默认 AFTER_ONE_CHILD_NODE）
        t.traverse(binaryTree(), 0, null, CHILDREN_GETTER, r.collector(), null);

        // AFTER_ONE_CHILD_NODE 正序（!isReverse && indexInBrother==0）：
        // 遍历第一个子节点后立即访问父节点。
        // 预期顺序：D, B, E, A, F, C, G
        assertEquals(Arrays.asList("D", "B", "E", "A", "F", "C", "G"), r.order);

        // H-3 回归：验证 parentLevel+1 对节点 level 的影响
        // level=0 传入，上下文 level = parentLevel + 1
        assertEquals(1, (int) r.levels.get("A"));
        assertEquals(2, (int) r.levels.get("B"));
        assertEquals(2, (int) r.levels.get("C"));
        assertEquals(3, (int) r.levels.get("D"));
        assertEquals(3, (int) r.levels.get("E"));
        assertEquals(3, (int) r.levels.get("F"));
        assertEquals(3, (int) r.levels.get("G"));
    }

    @Test
    public void after_reverse_binaryTree_defaultPolicy() {
        DataAfterTraverse t = new DataAfterTraverse(true);
        TraverseResult r = new TraverseResult();
        t.traverse(binaryTree(), 0, null, CHILDREN_GETTER, r.collector(), null);

        // AFTER_ONE_CHILD_NODE 逆序（isReverse && indexInBrother==size-1）：
        // 遍历最后一个子节点后立即访问父节点
        // 预期顺序：G, C, F, A, E, B, D
        assertEquals(Arrays.asList("G", "C", "F", "A", "E", "B", "D"), r.order);
    }

    @Test
    public void after_forward_linkedList_defaultPolicy() {
        DataAfterTraverse t = new DataAfterTraverse(false);
        TraverseResult r = new TraverseResult();
        t.traverse(linkedList(), 0, null, CHILDREN_GETTER, r.collector(), null);

        // 链表，每个父节点只有一个子节点（index=0）
        // D 叶子 -> 策略触发父 C -> C 的 index=0 -> 触发父 B -> B 的 index=0 -> 触发父 A
        // 预期顺序：D, C, B, A
        assertEquals(Arrays.asList("D", "C", "B", "A"), r.order);
    }

    @Test
    public void after_forward_binaryTree_explicitPolicy() {
        DataAfterTraverse t = new DataAfterTraverse(false);
        TraverseResult r = new TraverseResult();

        // 显式传递 AFTER_ONE_CHILD_NODE，行为应与 null 默认值一致
        t.traverse(binaryTree(), 0, null, CHILDREN_GETTER, r.collector(),
                DataNodeTraversePolicy.AFTER_ONE_CHILD_NODE);

        assertEquals(Arrays.asList("D", "B", "E", "A", "F", "C", "G"), r.order);
    }

    // ================================================================
    //  DataAfterTraverse - 自定义策略（DataNodeTraversePolicy.buildPredicate）
    // ================================================================

    @Test
    public void after_forward_binaryTree_customPolicy_alwaysTrue() {
        DataAfterTraverse t = new DataAfterTraverse(false);
        final List<String> visited = new ArrayList<>();

        // 自定义策略：每遍历一个子节点后都访问父节点
        DataNodeTraversePolicy alwaysPolicy = DataNodeTraversePolicy.AFTER_ONE_CHILD_NODE
                .buildPredicate(ctx -> true);

        Predicate<TreeTraverseContext<TNode>> collector = ctx -> {
            visited.add(ctx.getNode().id);
            return true;
        };

        t.traverse(binaryTree(), 1, null, CHILDREN_GETTER, collector, alwaysPolicy);

        // 始终策略：每个子节点都触发父节点访问
        assertTrue("A 应被访问", visited.contains("A"));
        assertTrue("B 应被访问", visited.contains("B"));
        assertTrue("C 应被访问", visited.contains("C"));
        assertTrue("D 应被访问", visited.contains("D"));
        assertTrue("E 应被访问", visited.contains("E"));
        assertTrue("F 应被访问", visited.contains("F"));
        assertTrue("G 应被访问", visited.contains("G"));
        // B 应当被多次访问（D 和 E 后各一次）
        assertTrue("自定义策略下 B 应被多次触发", countOccurrences(visited, "B") >= 2);
    }

    @Test
    public void after_forward_binaryTree_customPolicy_never() {
        DataAfterTraverse t = new DataAfterTraverse(false);
        final List<String> visited = new ArrayList<>();

        // 自定义策略：从不访问父节点
        DataNodeTraversePolicy neverPolicy = DataNodeTraversePolicy.AFTER_ONE_CHILD_NODE
                .buildPredicate(ctx -> false);

        Predicate<TreeTraverseContext<TNode>> collector = ctx -> {
            visited.add(ctx.getNode().id);
            return true;
        };

        t.traverse(binaryTree(), 1, null, CHILDREN_GETTER, collector, neverPolicy);

        // 永不策略：没有父节点会被策略触发，只有叶子节点被访问
        assertEquals("永不策略下只访问叶子节点", Arrays.asList("D", "E", "F", "G"), visited);
    }

    // ================================================================
    //  DataAfterTraverse - 停止测试
    // ================================================================

    @Test
    public void after_stopMidTraversal() {
        DataAfterTraverse t = new DataAfterTraverse(false);
        List<String> visited = new ArrayList<>();
        final int[] counter = {0};
        final int stopAfter = 2;

        Predicate<TreeTraverseContext<TNode>> stopper = ctx -> {
            visited.add(ctx.getNode().id);
            counter[0]++;
            return counter[0] < stopAfter;
        };

        t.traverse(binaryTree(), 0, null, CHILDREN_GETTER, stopper, null);

        // 默认策略正序：D(1) -> B(2)，B 为第2个，stopAfter=2 时第2个返回 false 停止
        assertEquals(Arrays.asList("D", "B"), visited);
    }

    // ================================================================
    //  三种策略统一 level 连续性验证（深度3的二叉树）
    // ================================================================

    @Test
    public void levelContinuity_firstTraverse() {
        DataFirstTraverse t = new DataFirstTraverse(false);
        TraverseResult r = new TraverseResult();
        t.traverse(binaryTree(), 0, null, CHILDREN_GETTER, r.collector());

        // level 应随深度递增：根=1，深度2=2，深度3=3
        assertEquals(1, (int) r.levels.get("A"));
        assertEquals(2, (int) r.levels.get("B"));
        assertEquals(2, (int) r.levels.get("C"));
        assertEquals(3, (int) r.levels.get("D"));
        assertEquals(3, (int) r.levels.get("E"));
        assertEquals(3, (int) r.levels.get("F"));
        assertEquals(3, (int) r.levels.get("G"));
    }

    @Test
    public void levelContinuity_afterTraverse() {
        DataAfterTraverse t = new DataAfterTraverse(false);
        TraverseResult r = new TraverseResult();
        t.traverse(binaryTree(), 0, null, CHILDREN_GETTER, r.collector(), null);

        // parentLevel+1：根=1，深度2=2，深度3=3
        assertEquals(1, (int) r.levels.get("A"));
        assertEquals(2, (int) r.levels.get("B"));
        assertEquals(2, (int) r.levels.get("C"));
        assertEquals(3, (int) r.levels.get("D"));
        assertEquals(3, (int) r.levels.get("E"));
        assertEquals(3, (int) r.levels.get("F"));
        assertEquals(3, (int) r.levels.get("G"));
    }

    // ================================================================
    //  多根节点测试
    // ================================================================

    @Test
    public void first_forward_multipleRoots() {
        DataFirstTraverse t = new DataFirstTraverse(false);
        // 两个独立的树：[A->B, C->D]
        TNode a = new TNode("A");
        TNode b = new TNode("B");
        a.add(b);
        TNode c = new TNode("C");
        TNode d = new TNode("D");
        c.add(d);

        TraverseResult r = new TraverseResult();
        t.traverse(Arrays.asList(a, c), 0, null, CHILDREN_GETTER, r.collector());

        // 注意：遍历一个 size=N 的根节点列表，每元素会被处理 N 次
        // （外层 traverse 迭代 size 次，每次对全列表调用 dataFirstTraverse）
        assertEquals(Arrays.asList("A", "B", "C", "D", "A", "B", "C", "D"), r.order);
        assertEquals(1, (int) r.levels.get("A"));
        assertEquals(2, (int) r.levels.get("B"));
        assertEquals(1, (int) r.levels.get("C"));
        assertEquals(2, (int) r.levels.get("D"));
    }

    @Test
    public void last_forward_multipleRoots() {
        DataLastTraverse t = new DataLastTraverse(false);
        TNode a = new TNode("A");
        TNode b = new TNode("B");
        a.add(b);
        TNode c = new TNode("C");
        TNode d = new TNode("D");
        c.add(d);

        TraverseResult r = new TraverseResult();
        t.traverse(Arrays.asList(a, c), 0, null, CHILDREN_GETTER, r.collector());

        // 后序：B, A, D, C
        assertEquals(Arrays.asList("B", "A", "D", "C"), r.order);
    }

    // ================================================================
    //  6参数接口兼容性测试
    // ================================================================

    @Test
    public void first_withPolicyParam_traverseSuccessfully() {
        // 验证 6 参数 traverse() 修复后正常工作（不再无限递归）
        // 原 bug: DataFirstTraverse.java:17-19 自调用自身导致 StackOverflowError
        DataFirstTraverse t = new DataFirstTraverse(false);
        Traverse iface = t;
        TraverseResult r = new TraverseResult();
        iface.traverse(binaryTree(), 0, null, CHILDREN_GETTER, ctx -> {
            r.order.add(ctx.getNode().id);
            return true;
        }, DataNodeTraversePolicy.AFTER_ONE_CHILD_NODE);
        assertEquals(Arrays.asList("A", "B", "D", "E", "C", "F", "G"), r.order);
    }

    @Test
    public void last_withPolicyParam_ignoresPolicy() {
        DataLastTraverse t = new DataLastTraverse(false);
        TraverseResult r1 = new TraverseResult();
        TraverseResult r2 = new TraverseResult();

        t.traverse(binaryTree(), 0, null, CHILDREN_GETTER, r1.collector());
        Traverse iface = t;
        iface.traverse(binaryTree(), 0, null, CHILDREN_GETTER, r2.collector(),
                DataNodeTraversePolicy.AFTER_ONE_CHILD_NODE);

        assertEquals("后序遍历的5参数和6参数版本结果应一致", r1.order, r2.order);
    }

    // ================================================================
    //  辅助方法
    // ================================================================

    private static int countOccurrences(List<String> list, String target) {
        int count = 0;
        for (String s : list) {
            if (target.equals(s)) {
                count++;
            }
        }
        return count;
    }
}
