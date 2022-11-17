package com.tingfeng.util.java.base.common.utils;

import com.alibaba.fastjson.JSON;
import com.tingfeng.util.java.base.common.bean.DefaultTreeNode;
import com.tingfeng.util.java.base.common.bean.User;
import com.tingfeng.util.java.base.common.constant.TraversalPolicy;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class TreeUtilsTest {


    private List<User> getTree(){
        List<User>  userList  = new ArrayList<>();
        User user = new User();
        User user2 = new User();
        User userChild = new User();
        User userChild2 = new User();

        //init
        user.setAge(100);
        user.userName = "a";
        user2.setAge(50);
        user2.userName = "b";
        user2.parentUserName = "kk";

        userChild.userName = "bb1";
        userChild.setAge(20);
        userChild.parentUserName = "b";
        userChild2.userName = "bb2";
        userChild2.setAge(10);
        userChild2.parentUserName = "b";

        userList.add(user);
        userList.add(user2);
        userList.add(userChild);
        userList.add(userChild2);

        List<User>  k = TreeUtils.getTreeList(userList,(child,parent)->{
            if(parent.childList == null){
                parent.childList = new ArrayList<>();
            }
            parent.childList.add(child);
        },(child,parent) -> Objects.equals(child.parentUserName,parent.userName),(u) -> u.getAge());
        return k;
    }

    /**
     * 生成 深度等于 level, 总节点数为 maxNodeSize 的一个 tree
     * @param level
     * @param maxNodeSize
     * @return 一个 tree 的各个 Node,它们的层级关系是同级的，尚未建立父子层级关系
     */
    public List<DefaultTreeNode> generateTree(int level,int maxNodeSize){

        Function<Integer,String> levelNodeBaseRandomCharF = l -> {
            switch (l){
                case 1:
                case 2:{
                    return RandomUtils.randomLowerString(1);
                }
                default:{
                    return RandomUtils.randomUpperString(2);
                }
            }
        };
        List<DefaultTreeNode> trees = new ArrayList<>(maxNodeSize);
        Set<String> nodeIds = new HashSet<>(maxNodeSize);
        for (int i = 0; i < maxNodeSize; ) {
            int currentLevel = RandomUtils.randomInt(0, level);
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < currentLevel + 1 && i < maxNodeSize; j++) {
                String parentId = stringBuilder.toString();
                if (j > 0) {
                    stringBuilder.append("-");
                }
                String baseStr = levelNodeBaseRandomCharF.apply(j + 1);
                stringBuilder.append(baseStr);
                String id = stringBuilder.toString();
                if(nodeIds.contains(id)){
                    continue;
                }
                DefaultTreeNode node = new DefaultTreeNode();
                node.setId(id);
                node.setSortValue(RandomUtils.randomInt(0, 1000));
                if(currentLevel > 0){
                    node.setParentId(parentId);
                }
                trees.add(node);
                nodeIds.add(id);
                i++;
            }
        }
        return trees;
    }

    @Test
    public void treeUtilsTest(){
        List<User>  k = getTree();
        System.out.println(JSON.toJSONString(k));
    }

    @Test
    public void flatListTest(){
        List<User> users = TreeUtils.flatList(getTree(),it -> it.childList,it -> it);
        System.out.println(JSON.toJSONString(users));
    }


    @Test
    public void performanceTest(){
        List<DefaultTreeNode> treeNodes = generateTree(10,100000);
        TestUtils.printTime(1,10,index -> {
            List<DefaultTreeNode> treeList = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));
            System.out.println(treeList.size());
        });
    }

    @Test
    public void performanceTest2(){
        List<DefaultTreeNode> treeNodes = generateTree(10,100);
        TestUtils.printTime(1,10,index -> {
            List<DefaultTreeNode> treeList = TreeUtils.getTreeList(treeNodes,(child,parent) -> parent.getChildren().add(child),
                    (child,parent) -> parent.getId().equals(child.getParentId()),DefaultTreeNode::getSortValue);
            System.out.println(treeList.size());
        });
    }

    /**
     * 示例图：
     *                 1
     *      ---------------------------
     *      /      |       \         \
     *     21      22       23        24
     *    / \     |     /  |   \       |
     *   31 32    33   34  35  36     37
     *     /     |         |   |
     *    41     42        43  44
     * 期望的遍历的顺序为  1,21,31,32,41,22,33,42,23,34,35,43,36,44,24,37
     */
    @Test
    public void testTraverseByDLR() {
        List<DefaultTreeNode> treeNodes = getTreeNodesForTraverse();
        int[] traverseExpectValue = new int[]{1,21,31,32,41,22,33,42,23,34,35,43,36,44,24,37};
        List<DefaultTreeNode> treeList = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));
        AtomicInteger traverseCount = new AtomicInteger();
        TreeUtils.traverse(treeList, TraversalPolicy.DLR,DefaultTreeNode::getChildren,traverseContext -> {
            int currentIndex = traverseCount.getAndIncrement();
            Assert.assertEquals("" + traverseExpectValue[currentIndex],traverseContext.getNode().getId());
            return true;
        });
    }

    @Test
    public void testTraverseByDRL() {
        List<DefaultTreeNode> treeNodes = getTreeNodesForTraverse();
        int[] traverseExpectValue = new int[]{1,24,37,23,36,44,35,43,34,22,33,42,21,32,41,31};
        List<DefaultTreeNode> treeList = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));
        AtomicInteger traverseCount = new AtomicInteger();
        TreeUtils.traverse(treeList, TraversalPolicy.DRL,DefaultTreeNode::getChildren,traverseContext -> {
            int currentIndex = traverseCount.getAndIncrement();
            Assert.assertEquals("" + traverseExpectValue[currentIndex],traverseContext.getNode().getId());
            //System.out.print("," + traverseContext.getNode().getId());
            return true;
        });
    }

    /**
     * 数据图：
     *                 1
     *      ---------------------------
     *      /      |       \         \
     *     21      22       23        24
     *    / \     |     /  |   \       |
     *   31 32    33   34  35  36     37
     *     /     |         |   |
     *    41     42        43  44
     * @return 返回数据图中的 Tree结构的数据。
     */
    private static List<DefaultTreeNode> getTreeNodesForTraverse() {
        List<DefaultTreeNode> treeNodes = new ArrayList<>();
        treeNodes.add(new DefaultTreeNode("1",null,1));

        treeNodes.add(new DefaultTreeNode("21","1",1));
        treeNodes.add(new DefaultTreeNode("22","1",2));
        treeNodes.add(new DefaultTreeNode("23","1",3));
        treeNodes.add(new DefaultTreeNode("24","1",4));

        treeNodes.add(new DefaultTreeNode("31","21",1));
        treeNodes.add(new DefaultTreeNode("32","21",2));
        treeNodes.add(new DefaultTreeNode("33","22",3));
        treeNodes.add(new DefaultTreeNode("34","23",4));
        treeNodes.add(new DefaultTreeNode("35","23",5));
        treeNodes.add(new DefaultTreeNode("36","23",6));
        treeNodes.add(new DefaultTreeNode("37","24",7));

        treeNodes.add(new DefaultTreeNode("41","32",1));
        treeNodes.add(new DefaultTreeNode("42","33",2));
        treeNodes.add(new DefaultTreeNode("43","35",3));
        treeNodes.add(new DefaultTreeNode("44","36",4));
        return treeNodes;
    }

    /**
     * 中序遍历
     * <pre>
     * 当前遍历一个子节点后立即遍历父节点,然后遍历其它子节点
     * 则期望的遍历的顺序为   31,21,41,32,1,42,33,22,34,23,43,35,44,36,37,24
     * </pre>
     */
    @Test
    public void testTraverseByLDR() {
        List<DefaultTreeNode> treeNodes = getTreeNodesForTraverse();
        int[] traverseExpectValue = new int[]{31,21,41,32,1,42,33,22,34,23,43,35,44,36,37,24};
        List<DefaultTreeNode> treeList = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));
        AtomicInteger traverseCount = new AtomicInteger();
        TreeUtils.traverse(treeList, TraversalPolicy.LDR,DefaultTreeNode::getChildren,traverseContext -> {
            int currentIndex = traverseCount.getAndIncrement();
            Assert.assertEquals("" + traverseExpectValue[currentIndex],traverseContext.getNode().getId());
            //System.out.print("," + traverseContext.getNode().getId());
            //当前遍历一个子节点后,当前的索引为0 ,则接下来立即遍历父节点,然后遍历其它子节点
            return true;
        });
    }

    @Test
    public void traverseByRDL() {
        List<DefaultTreeNode> treeNodes = getTreeNodesForTraverse();
        int[] traverseExpectValue = new int[]{37,24,1,44,36,23,43,35,34,42,33,22,41,32,21,31};
        List<DefaultTreeNode> treeList = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));
        AtomicInteger traverseCount = new AtomicInteger();
        TreeUtils.traverse(treeList, TraversalPolicy.RDL,DefaultTreeNode::getChildren,traverseContext -> {
            int currentIndex = traverseCount.getAndIncrement();
            Assert.assertEquals("" + traverseExpectValue[currentIndex],traverseContext.getNode().getId());
            //System.out.print("," + traverseContext.getNode().getId());
            //当前遍历一个子节点后,当前的索引为0 ,则接下来立即遍历父节点,然后遍历其它子节点
            return true;
        });
    }

    @Test
    public void traverseByLRD() {
        List<DefaultTreeNode> treeNodes = getTreeNodesForTraverse();
        int[] traverseExpectValue = new int[]{31,41,32,21,42,33,22,34,43,35,44,36,23,37,24,1};
        List<DefaultTreeNode> treeList = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));
        AtomicInteger traverseCount = new AtomicInteger();
        TreeUtils.traverse(treeList, TraversalPolicy.LRD,DefaultTreeNode::getChildren,traverseContext -> {
            int currentIndex = traverseCount.getAndIncrement();
            Assert.assertEquals("" + traverseExpectValue[currentIndex],traverseContext.getNode().getId());
            //System.out.print("," + traverseContext.getNode().getId());
            return true;
        });
    }

    @Test
    public void traverseByRLD() {
        List<DefaultTreeNode> treeNodes = getTreeNodesForTraverse();
        int[] traverseExpectValue = new int[]{37,24,44,36,43,35,34,23,42,33,22,41,32,31,21,1};
        List<DefaultTreeNode> treeList = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));
        AtomicInteger traverseCount = new AtomicInteger();
        TreeUtils.traverse(treeList, TraversalPolicy.RLD,DefaultTreeNode::getChildren,traverseContext -> {
            int currentIndex = traverseCount.getAndIncrement();
            Assert.assertEquals("" + traverseExpectValue[currentIndex],traverseContext.getNode().getId());
            //System.out.print("," + traverseContext.getNode().getId());
            return true;
        });
    }
}
