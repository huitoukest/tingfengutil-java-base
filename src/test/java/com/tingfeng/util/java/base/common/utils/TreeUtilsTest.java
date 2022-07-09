package com.tingfeng.util.java.base.common.utils;

import com.alibaba.fastjson.JSON;
import com.tingfeng.util.java.base.common.bean.DefaultTreeNode;
import com.tingfeng.util.java.base.common.bean.User;
import org.junit.Test;

import java.util.*;
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

   /* @Test
    public void traverseByDLR() {
        int testCount = 5000;
        List<DefaultTreeNode> treeNodes = generateTree(10,testCount);
        TestUtils.printTime(1,10,index -> {
            List<DefaultTreeNode> treeList = TreeUtils.getTreeList(treeNodes, Comparator.comparingInt(DefaultTreeNode::getSortValue));
            AtomicInteger traverseCount = new AtomicInteger();
            TreeUtils.traverseByDLR(treeList,DefaultTreeNode::getChildren,(node,parent) -> traverseCount.incrementAndGet());
            Assert.assertEquals(testCount,traverseCount.intValue());
        });
    }

    @Test
    public void testTraverseByDLR() {
    }

    @Test
    public void traverseByDRL() {
    }

    @Test
    public void testTraverseByDRL() {
    }

    @Test
    public void traverseByLDR() {
    }

    @Test
    public void testTraverseByLDR() {
    }

    @Test
    public void traverseByRDL() {
    }

    @Test
    public void testTraverseByRDL() {
    }

    @Test
    public void traverseByLRD() {
    }

    @Test
    public void traverseByRLD() {
    }*/
}
