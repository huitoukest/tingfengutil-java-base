package com.tingfeng.util.java.base.common.utils;

import com.alibaba.fastjson.JSON;
import com.tingfeng.util.java.base.common.bean.User;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TreeUtilsTest {


    @Test
    public void treeUtilsTest(){
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

        List  k = TreeUtils.getTreeList(userList,(child,parent)->{
            if(parent.childList == null){
                parent.childList = new ArrayList<>();
            }
            parent.childList.add(child);
        },(child,parent) -> Objects.equals(child.parentUserName,parent.userName),(u) -> u.getAge());
        System.out.println(JSON.toJSONString(k));
    }

}
