package com.tingfeng.util.java.base.common.utils;

import com.alibaba.fastjson.JSON;
import com.tingfeng.util.java.base.common.bean.User;
import com.tingfeng.util.java.base.common.bean.UserWechatServiceFansInfo;
import com.tingfeng.util.java.base.common.bean.WechatServiceUserInfo;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BeanUtilsTest {

    @Test
    public void copyTest(){
        User user = new User();
        User b = new User();
        user.setAge(100);
        user.setC(123L);
        user.updateDateTime = new Date();
        user.setMap(new HashMap<>());


        b.user = user;
        b.setAge(50);
        b.setParentFiled("parent");

        User target = new User();
        //10w次的时间应该在300ms左右，主流的BeanUtils性能；getFields需要优化；
        TestUtils.printTime(1,100000,i -> {
            BeanUtils.copyProperties(target,b,"age");
        });
        System.out.println(JSON.toJSONString(target));
    }

    @Test
    public void mapToBeanTest(){
        Map<String,Object> map = new HashMap<>();
        map.put("age",10);
        map.put("user.c","ccc");
        map.put("user.updateDateTime",new Date());
        map.put("user.interval",110);
        User user = BeanUtils.getBeanByMap(User.class,map);
        System.out.println(JSON.toJSONString(user));
    }

    @Test
    public void testCopy(){
        UserWechatServiceFansInfo wechatServiceFansInfo = new UserWechatServiceFansInfo();
        WechatServiceUserInfo wechatServiceUserInfo = new WechatServiceUserInfo();
        wechatServiceUserInfo.setUnionid("1231");
        wechatServiceUserInfo.setGroupid(123456);
        wechatServiceUserInfo.setHeadimgurl("http://sdsds/dsdd");
        wechatServiceUserInfo.setNickname("dddd");
        wechatServiceUserInfo.setQr_scene_str("xxxxx");
        BeanUtils.copyProperties(wechatServiceFansInfo,wechatServiceUserInfo);

        System.out.println(JSON.toJSONString(wechatServiceFansInfo));
    }

    @Test
    public void toMapTest(){
        User user = new User();
        User b = new User();
        user.setAge(100);
        user.setC(123L);
        user.updateDateTime = new Date();
        user.setMap(new HashMap<>());

        b.user = user;
        b.setAge(50);
        b.setParentFiled("parent");
        Map<String,Object> map = BeanUtils.toMap(b);
        String str = JSON.toJSONString(map);
        System.out.println(str);
        User parseB = JSON.parseObject(str,User.class);
        Assert.assertEquals( b.getUser().getC() , parseB.user.getC());
    }

    @Getter
    @Setter
    public static class SaveParentDTO{
        private Long parent_id;
        private Long student_id;
        private String student_name;
        private String class_name;
        private Long classId;
        private Long school_id;
        private String updated;
        private String relationship;
        private String parentMobilePhone;
        private String mobileCountryCode;
    }

    @Getter
    @Setter
    public static class SaveParentDTO2{
        private Long parent_id;
        private Long student_id;
        private String student_name;
        private String class_name;
        private Long classId;
        private Long school_id;
        private String updated;
        private String relationship;
        private String parentMobilePhone;
        private String mobileCountryCode;
    }

    @Test
    public void copyTest2(){
        int count = 10000000;
        SaveParentDTO saveParentDTO = new SaveParentDTO();
        saveParentDTO.setParent_id(111L);
        saveParentDTO.setStudent_name(RandomUtils.randomString(25));
        saveParentDTO.setRelationship(RandomUtils.randomString(30));
        saveParentDTO.setClassId(RandomUtils.randomLong());
        TestUtils.printTime(1,1,index -> {
            for (int i = 0; i < count; i++) {
                BeanUtils.copyProperties(new SaveParentDTO2(),saveParentDTO);
            }
        });
    }

    @Test
    public void copyTest3(){
        int count = 10000000;
        SaveParentDTO saveParentDTO = new SaveParentDTO();
        saveParentDTO.setParent_id(111L);
        saveParentDTO.setStudent_name(RandomUtils.randomString(25));
        saveParentDTO.setRelationship(RandomUtils.randomString(30));
        saveParentDTO.setClassId(RandomUtils.randomLong());
        TestUtils.printTime(1,1,index -> {
            for (int i = 0; i < count; i++) {
                BeanUtils.copyProperties(new SaveParentDTO2(),saveParentDTO,false,"relationship");
            }
        });
    }
}
