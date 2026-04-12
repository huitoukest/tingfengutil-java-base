package com.tingfeng.util.java.base.common.utils;

import com.alibaba.fastjson.JSON;
import com.tingfeng.util.java.base.common.bean.User;
import com.tingfeng.util.java.base.common.bean.UserWechatServiceFansInfo;
import com.tingfeng.util.java.base.common.bean.WechatServiceUserInfo;
import com.tingfeng.util.java.base.common.bean.tuple.Tuple2;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

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

    @Test
    public void testCopyListProperties() {
        List<User> sourceList = new ArrayList<>();
        User user1 = new User();
        user1.setAge(20);
        user1.setC(1L);
        user1.userName = "User1";
        sourceList.add(user1);

        User user2 = new User();
        user2.setAge(30);
        user2.setC(2L);
        user2.userName = "User2";
        sourceList.add(user2);

        List<User> targetList = BeanUtils.copyListProperties(sourceList, User.class);
        Assert.assertEquals(2, targetList.size());
        Assert.assertEquals(user1.getAge(), targetList.get(0).getAge());
        Assert.assertEquals(user1.getC(), targetList.get(0).getC());
        Assert.assertEquals(user1.userName, targetList.get(0).userName);
        Assert.assertEquals(user2.getAge(), targetList.get(1).getAge());
        Assert.assertEquals(user2.getC(), targetList.get(1).getC());
        Assert.assertEquals(user2.userName, targetList.get(1).userName);
    }

    @Test
    public void testCopyPropertiesWithPredicateAndMapper() {
        User source = new User();
        source.setAge(20);
        source.setC(1L);
        source.userName = "Source";

        User target = new User();

        // 测试带predicate和mapper的复制
        Predicate<Tuple2<String, Object>> predicate = tuple -> {
            String fieldName = tuple.get_1();
            return !"userName".equals(fieldName);
        };

        Function<Tuple2<String, Object>, Object> mapper = tuple -> {
            String fieldName = tuple.get_1();
            Object value = tuple.get_2();
            if ("age".equals(fieldName)) {
                return (int) value + 10;
            }
            return value;
        };

        BeanUtils.copyProperties(target, source, predicate, mapper, Collections.emptyList());
        // 由于User类的getAge()方法直接返回age值，所以预期值是30
        Assert.assertEquals(30, target.getAge()); // 验证mapper生效
        Assert.assertEquals(source.getC(), target.getC());
        Assert.assertNull(target.userName); // 验证predicate生效
    }

    @Test
    public void testGetBeanCopyFieldMap() {
        Map<String, Tuple2<java.lang.reflect.Field, java.lang.reflect.Field>> fieldMap = BeanUtils.getBeanCopyFieldMap(User.class, User.class, true);
        Assert.assertNotNull(fieldMap);
        Assert.assertTrue(fieldMap.size() > 0);
    }

    @Test
    public void testGetBeanCopyFunMap() {
        Map<String, com.tingfeng.util.java.base.common.bean.BeanCopyFun> funMap = BeanUtils.getBeanCopyFunMap(User.class, User.class, true);
        Assert.assertNotNull(funMap);
        Assert.assertTrue(funMap.size() > 0);
    }

    @Test
    public void testGetBeanInfo() {
        BeanInfo beanInfo = BeanUtils.getBeanInfo(User.class);
        Assert.assertNotNull(beanInfo);
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        Assert.assertTrue(descriptors.length > 0);
    }

    @Test
    public void testGetBeanCopyMethodMap() {
        Map<String, Tuple2<Method, Method>> methodMap = BeanUtils.getBeanCopyMethodMap(User.class, User.class, true);
        Assert.assertNotNull(methodMap);
        Assert.assertTrue(methodMap.size() > 0);
    }

    @Test
    public void testGetFieldNameByGetter() {
        String fieldName1 = BeanUtils.getFieldNameByGetter("getAge");
        Assert.assertEquals("age", fieldName1);

        String fieldName2 = BeanUtils.getFieldNameByGetter("isOk");
        Assert.assertEquals("ok", fieldName2);
    }

    @Test
    public void testCreateBeanConverter() {
        String[] fieldNames = {"age", "userName", "c"};
        Function<String[], User> converter = BeanUtils.createBeanConverter(fieldNames, User.class, null);
        Assert.assertNotNull(converter);

        String[] values = {"25", "TestUser", "123"};
        User user = converter.apply(values);
        Assert.assertNotNull(user);
        Assert.assertEquals(25, user.getAge()); // 因为getAge()直接返回age值
        Assert.assertEquals("TestUser", user.userName);
        Assert.assertEquals(123L, user.getC().longValue());
    }

    @Test
    public void testCopyPropertiesNotStrict() {
        User source = new User();
        source.setAge(20);
        source.setC(1L);
        source.userName = "Source";
        source.updateDateTime = new Date();

        User target = new User();
        BeanUtils.copyPropertiesNotStrict(target, source, null, null, Collections.emptyList());
        Assert.assertEquals(source.getAge(), target.getAge());
        Assert.assertEquals(source.getC(), target.getC());
        Assert.assertEquals(source.userName, target.userName);
        Assert.assertEquals(source.updateDateTime, target.updateDateTime);
    }

    @Test
    public void testToMapWithIgnoreProperties() {
        User user = new User();
        user.setAge(20);
        user.setC(1L);
        user.userName = "Test";
        user.updateDateTime = new Date();

        // 测试忽略某些属性
        Map<String, Object> map = BeanUtils.toMap(user, User::getAge, User::getC);
        Assert.assertNotNull(map);
        // 输出map内容，查看实际包含的属性
        System.out.println("toMap result: " + JSON.toJSONString(map));
        // 由于toMap方法使用getter方法，所以age会被忽略，但userName应该存在
        // updateDateTime是公共字段，没有getter方法，所以不会被toMap方法获取
        Assert.assertTrue(map.containsKey("userName"));
    }
}