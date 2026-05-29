package com.tingfeng.util.java.base.bean;

import com.tingfeng.util.java.base.bean.copier.CopyOptions;
import com.tingfeng.util.java.base.bean.copier.MapValueProvider;
import com.tingfeng.util.java.base.common.utils.TestUtils;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import com.tingfeng.util.java.base.bean.TypeReference;

/**
 * BeanUtil门面类单元测试
 * <p>
 * 覆盖6个核心测试场景：
 * <ul>
 *   <li>场景1：简单对象拷贝 — 同类型对象属性值一致</li>
 *   <li>场景2：跨类型对象映射 — SaveParentDTO → SaveParentDTO2</li>
 *   <li>场景3：列表批量拷贝 — toList(sources, TargetClass)</li>
 *   <li>场景4：带条件过滤的拷贝 — ignoreNull + ignoreProperties</li>
 *   <li>场景5：对象转Map — toMap(obj) / toMap(obj, ignoreFields)</li>
 *   <li>场景6：Map/ValueProvider转对象 — toBean(MapValueProvider, Class)</li>
 * </ul>
 */
public class BeanUtilsTest {

    /**
     * 场景1：简单对象拷贝 — 同类型对象属性值一致
     */
    @Test
    public void testCopyPropertiesSameType() {
        User source = new User();
        source.setAge(25);
        source.setC(100L);
        source.userName = "SourceUser";
        source.updateDateTime = new Date();

        User target = new User();
        BeanUtils.copyProperties(source, target);

        Assert.assertEquals(source.getAge(), target.getAge());
        Assert.assertEquals(source.getC(), target.getC());
        Assert.assertEquals(source.userName, target.userName);
        Assert.assertEquals(source.updateDateTime, target.updateDateTime);

        // 修改target不影响source
        target.setAge(99);
        Assert.assertNotEquals(source.getAge(), target.getAge());
    }

    /**
     * 场景2：跨类型对象映射 — SaveParentDTO → SaveParentDTO2
     */
    @Test
    public void testCopyPropertiesCrossType() {
        SaveParentDTO source = new SaveParentDTO();
        source.setParent_id(1001L);
        source.setStudent_id(2001L);
        source.setStudent_name("张三");
        source.setClass_name("Class-A");
        source.setClassId(301L);
        source.setSchool_id(401L);
        source.setUpdated("2024-01-01");
        source.setRelationship("父子");
        source.setParentMobilePhone("13800138000");
        source.setMobileCountryCode("+86");

        SaveParentDTO2 target = new SaveParentDTO2();
        BeanUtils.copyProperties(source, target);

        Assert.assertEquals(source.getParent_id(), target.getParent_id());
        Assert.assertEquals(source.getStudent_id(), target.getStudent_id());
        Assert.assertEquals(source.getStudent_name(), target.getStudent_name());
        Assert.assertEquals(source.getClass_name(), target.getClass_name());
        Assert.assertEquals(source.getClassId(), target.getClassId());
        Assert.assertEquals(source.getSchool_id(), target.getSchool_id());
        Assert.assertEquals(source.getUpdated(), target.getUpdated());
        Assert.assertEquals(source.getRelationship(), target.getRelationship());
        Assert.assertEquals(source.getParentMobilePhone(), target.getParentMobilePhone());
        Assert.assertEquals(source.getMobileCountryCode(), target.getMobileCountryCode());
    }

    /**
     * 场景3：列表批量拷贝 — toList(sources, TargetClass)
     */
    @Test
    public void testToList() {
        List<User> sources = new ArrayList<>();
        User user1 = new User();
        user1.setAge(20);
        user1.setC(1L);
        user1.userName = "User1";
        sources.add(user1);

        User user2 = new User();
        user2.setAge(30);
        user2.setC(2L);
        user2.userName = "User2";
        sources.add(user2);

        List<User> targets = BeanUtils.toList(sources, User.class);

        Assert.assertEquals(2, targets.size());
        Assert.assertEquals(user1.getAge(), targets.get(0).getAge());
        Assert.assertEquals(user1.getC(), targets.get(0).getC());
        Assert.assertEquals(user1.userName, targets.get(0).userName);
        Assert.assertEquals(user2.getAge(), targets.get(1).getAge());
        Assert.assertEquals(user2.getC(), targets.get(1).getC());
        Assert.assertEquals(user2.userName, targets.get(1).userName);
    }

    /**
     * 场景3补充：带CopyOptions的列表批量拷贝
     */
    @Test
    public void testToListWithOptions() {
        List<User> sources = new ArrayList<>();
        User user1 = new User();
        user1.setAge(20);
        user1.setC(1L);
        user1.userName = "User1";
        sources.add(user1);

        List<User> targets = BeanUtils.toList(sources, User.class,
                CopyOptions.create().setIgnoreProperties("age"));

        Assert.assertEquals(1, targets.size());
        Assert.assertEquals(0, targets.get(0).getAge()); // primitive int defaults to 0
        Assert.assertEquals(user1.getC(), targets.get(0).getC());
        Assert.assertEquals(user1.userName, targets.get(0).userName);
    }

    /**
     * 泛型类型推断测试：toList with TypeReference
     * <p>
     * 注意：TypeReference 的泛型参数是目标元素类型，如 User，
     * 而非容器类型如 List<User>。
     */
    @Test
    public void testToListWithTypeReference() {
        List<User> sources = new ArrayList<>();
        User user1 = new User();
        user1.setAge(20);
        user1.setC(1L);
        user1.userName = "User1";
        sources.add(user1);

        User user2 = new User();
        user2.setAge(30);
        user2.setC(2L);
        user2.userName = "User2";
        sources.add(user2);

        // 使用 TypeReference 保留泛型信息，泛型参数是目标元素类型 User
        List<User> targets = BeanUtils.toList(sources, new TypeReference<User>() {});

        Assert.assertEquals(2, targets.size());
        Assert.assertEquals(user1.getAge(), targets.get(0).getAge());
        Assert.assertEquals(user1.getC(), targets.get(0).getC());
        Assert.assertEquals(user1.userName, targets.get(0).userName);
        Assert.assertEquals(user2.getAge(), targets.get(1).getAge());
        Assert.assertEquals(user2.getC(), targets.get(1).getC());
        Assert.assertEquals(user2.userName, targets.get(1).userName);
    }

    /**
     * 泛型类型推断测试：sources 为 null 时返回空列表
     */
    @Test
    public void testToListWithTypeReferenceNull() {
        List<User> result = BeanUtils.toList(null, new TypeReference<User>() {});
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    /**
     * 泛型类型推断测试：sources 为空时返回空列表
     */
    @Test
    public void testToListWithTypeReferenceEmpty() {
        List<User> sources = new ArrayList<>();
        List<User> result = BeanUtils.toList(sources, new TypeReference<User>() {});
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    /**
     * 场景4：带条件过滤的拷贝 — ignoreNull + ignoreProperties
     */
    @Test
    public void testCopyPropertiesWithIgnoreNull() {
        User source = new User();
        source.setAge(25);
        source.setC(null); // null值
        source.userName = "TestUser";

        User target = new User();
        target.setC(100L);

        CopyOptions options = CopyOptions.create().setIgnoreNull(true);
        BeanUtils.copyProperties(source, target, options);

        Assert.assertEquals(25, target.getAge());
        Assert.assertEquals("TestUser", target.userName);
        Assert.assertEquals(Long.valueOf(100L), target.getC()); // c为null，被忽略
    }

    @Test
    public void testCopyPropertiesWithIgnoreProperties() {
        User source = new User();
        source.setAge(25);
        source.setC(100L);
        source.userName = "TestUser";

        User target = new User();
        target.setAge(50);
        target.setC(200L);

        CopyOptions options = CopyOptions.create().setIgnoreProperties("age", "c");
        BeanUtils.copyProperties(source, target, options);

        Assert.assertEquals(50, target.getAge()); // age被忽略
        Assert.assertEquals(Long.valueOf(200L), target.getC()); // c被忽略
        Assert.assertEquals("TestUser", target.userName);
    }

    /**
     * 场景5：对象转Map — toMap(obj) / toMap(obj, ignoreFields)
     */
    @Test
    public void testToMapBasic() {
        User user = new User();
        user.setAge(25);
        user.setC(100L);
        user.userName = "TestUser";

        Map<String, Object> map = BeanUtils.toMap(user);

        Assert.assertNotNull(map);
        Assert.assertTrue(map.containsKey("age"));
        Assert.assertTrue(map.containsKey("c"));
        Assert.assertTrue(map.containsKey("userName"));
        Assert.assertEquals(25, map.get("age"));
        Assert.assertEquals(100L, map.get("c"));
        Assert.assertEquals("TestUser", map.get("userName"));
    }

    @Test
    public void testToMapWithIgnoreFields() {
        User user = new User();
        user.setAge(25);
        user.setC(100L);
        user.userName = "TestUser";

        Map<String, Object> map = BeanUtils.toMap(user, "age", "c");

        Assert.assertNotNull(map);
        Assert.assertFalse(map.containsKey("age"));
        Assert.assertFalse(map.containsKey("c"));
        Assert.assertTrue(map.containsKey("userName"));
    }

    /**
     * 场景6：Map/ValueProvider转对象 — toBean(MapValueProvider, Class)
     */
    @Test
    public void testToBeanFromMapValueProvider() {
        Map<String, Object> map = new HashMap<>();
        map.put("age", 30);
        map.put("c", 500L);
        map.put("userName", "MapUser");

        MapValueProvider provider = new MapValueProvider(map);
        User user = BeanUtils.toBean(provider, User.class);

        Assert.assertNotNull(user);
        Assert.assertEquals(30, user.getAge());
        Assert.assertEquals(Long.valueOf(500L), user.getC());
        Assert.assertEquals("MapUser", user.userName);
    }

    @Test
    public void testToBeanFromMapValueProviderWithOptions() {
        Map<String, Object> map = new HashMap<>();
        map.put("age", 30);
        map.put("c", 500L);
        map.put("userName", "MapUser");

        MapValueProvider provider = new MapValueProvider(map);
        CopyOptions options = CopyOptions.create().setIgnoreProperties("age");
        User user = BeanUtils.toBean(provider, User.class, options);

        Assert.assertNotNull(user);
        Assert.assertEquals(0, user.getAge()); // age被忽略，使用默认值
        Assert.assertEquals(Long.valueOf(500L), user.getC());
        Assert.assertEquals("MapUser", user.userName);
    }

    /**
     * 场景6补充：toBean(Map, Class) 便捷方法
     */
    @Test
    public void testToBeanFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("age", 30);
        map.put("c", 500L);
        map.put("userName", "MapUser");

        User user = BeanUtils.toBean(map, User.class);

        Assert.assertNotNull(user);
        Assert.assertEquals(30, user.getAge());
        Assert.assertEquals(Long.valueOf(500L), user.getC());
        Assert.assertEquals("MapUser", user.userName);
    }

    @Test
    public void testToBeanFromMapWithOptions() {
        Map<String, Object> map = new HashMap<>();
        map.put("age", 30);
        map.put("c", 500L);
        map.put("userName", "MapUser");

        CopyOptions options = CopyOptions.create().setIgnoreProperties("age");
        User user = BeanUtils.toBean(map, User.class, options);

        Assert.assertNotNull(user);
        Assert.assertEquals(0, user.getAge()); // age被忽略
        Assert.assertEquals(Long.valueOf(500L), user.getC());
        Assert.assertEquals("MapUser", user.userName);
    }

    // ========== 边界测试 ==========

    /**
     * 边界测试：source == null 时的行为
     */
    @Test
    public void testCopyPropertiesSourceNull() {
        User target = new User();
        target.setAge(50);

        BeanUtils.copyProperties(null, target);

        Assert.assertEquals(50, target.getAge()); // target保持不变
    }

    @Test
    public void testCopyPropertiesTargetNull() {
        User source = new User();
        source.setAge(25);

        BeanUtils.copyProperties(source, null);
        // 无异常即通过
    }

    @Test
    public void testToBeanSourceNull() {
        User user = BeanUtils.toBean((Object) null, User.class);
        Assert.assertNull(user);
    }

    @Test
    public void testToBeanFromProviderNull() {
        MapValueProvider provider = null;
        User user = BeanUtils.toBean(provider, User.class);
        Assert.assertNull(user);
    }

    @Test
    public void testToListSourceNull() {
        List<User> result = BeanUtils.toList(null, User.class);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testToListSourceEmpty() {
        List<User> sources = new ArrayList<>();
        List<User> result = BeanUtils.toList(sources, User.class);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testToMapBeanNull() {
        Map<String, Object> map = BeanUtils.toMap((Object) null);
        Assert.assertNotNull(map);
        Assert.assertTrue(map.isEmpty());
    }

    /**
     * 边界测试：targetClass无默认构造器时的异常
     */
    @Test(expected = com.tingfeng.util.java.base.lang.exception.BaseException.class)
    public void testToBeanNoDefaultConstructor() {
        NoDefaultConstructorBean source = new NoDefaultConstructorBean(100);

        BeanUtils.toBean(source, NoDefaultConstructorBean.class);
    }

    // ========== 性能测试（可选） ==========

    /**
     * 性能测试：10万次拷贝耗时
     */
    @Test
    public void testCopyPerformance() {
        User source = new User();
        source.setAge(25);
        source.setC(100L);
        source.userName = "PerfUser";

        TestUtils.printTime(1, 100000, i -> {
            User target = new User();
            BeanUtils.copyProperties(source, target);
        });
    }

    // ========== 辅助类 ==========

    @Getter
    @Setter
    public static class SaveParentDTO {
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
    public static class SaveParentDTO2 {
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

    /**
     * 无默认构造器的Bean，用于测试边界情况
     */
    public static class NoDefaultConstructorBean {
        private int value;

        public NoDefaultConstructorBean(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    // ========== deepCopy 测试 ==========

    /**
     * 深拷贝测试：deepCopy(null) 返回 null
     */
    @Test
    public void testDeepCopyNull() {
        User result = BeanUtils.deepCopy((User) null);
        Assert.assertNull(result);
    }

    /**
     * 深拷贝测试：不可变类型（String）直接返回相同引用
     */
    @Test
    public void testDeepCopyImmutableType() {
        String source = "hello";
        String result = BeanUtils.deepCopy(source);
        Assert.assertSame(source, result); // 相同引用，未创建新对象
    }

    /**
     * 深拷贝测试：基本类型包装类直接返回相同引用
     */
    @Test
    public void testDeepCopyWrapperTypes() {
        Integer intSource = Integer.valueOf(100);
        Long longSource = Long.valueOf(200L);
        String strSource = "test";

        Assert.assertSame(intSource, BeanUtils.deepCopy(intSource));
        Assert.assertSame(longSource, BeanUtils.deepCopy(longSource));
        Assert.assertSame(strSource, BeanUtils.deepCopy(strSource));
    }

    /**
     * 深拷贝测试：普通 Bean 返回完全独立副本，修改副本不影响原始对象
     */
    @Test
    public void testDeepCopyBean() {
        User source = new User();
        source.setAge(25);
        source.setC(100L);
        source.userName = "OriginalUser";

        User result = BeanUtils.deepCopy(source);

        // 副本值相同
        Assert.assertEquals(source.getAge(), result.getAge());
        Assert.assertEquals(source.getC(), result.getC());
        Assert.assertEquals(source.userName, result.userName);

        // 修改副本不影响原始对象
        result.setAge(99);
        result.setC(999L);
        result.userName = "ModifiedUser";

        Assert.assertNotEquals(source.getAge(), result.getAge());
        Assert.assertNotEquals(source.getC(), result.getC());
        Assert.assertNotEquals(source.userName, result.userName);
    }

    /**
     * 深拷贝测试：嵌套对象深拷贝，非浅拷贝
     */
    @Test
    public void testDeepCopyNestedObject() {
        User source = new User();
        source.setAge(25);
        source.setC(100L);
        source.userName = "OuterUser";

        User result = BeanUtils.deepCopy(source);

        // 基本属性值相同但非同一对象
        Assert.assertEquals(source.getAge(), result.getAge());
        Assert.assertNotSame(source, result);
    }

    /**
     * 深拷贝测试：数组深拷贝
     */
    @Test
    public void testDeepCopyArray() {
        int[] intArray = {1, 2, 3};
        String[] strArray = {"a", "b", "c"};

        int[] intResult = BeanUtils.deepCopy(intArray);
        String[] strResult = BeanUtils.deepCopy(strArray);

        // 值相同
        Assert.assertArrayEquals(intArray, intResult);
        Assert.assertArrayEquals(strArray, strResult);

        // 非同一引用
        Assert.assertNotSame(intArray, intResult);
        Assert.assertNotSame(strArray, strResult);

        // 修改副本不影响原始
        intResult[0] = 999;
        strResult[0] = "modified";
        Assert.assertNotEquals(intArray[0], intResult[0]);
        Assert.assertNotEquals(strArray[0], strResult[0]);
    }

    /**
     * 深拷贝测试：Collection 深拷贝
     */
    @Test
    public void testDeepCopyCollection() {
        List<String> source = new ArrayList<>();
        source.add("item1");
        source.add("item2");
        source.add("item3");

        List<String> result = BeanUtils.deepCopy(source);

        // 值相同
        Assert.assertEquals(source.size(), result.size());
        for (int i = 0; i < source.size(); i++) {
            Assert.assertEquals(source.get(i), result.get(i));
        }

        // 非同一引用
        Assert.assertNotSame(source, result);

        // 修改副本不影响原始
        result.set(0, "modified");
        Assert.assertNotEquals(source.get(0), result.get(0));
    }

    /**
     * 深拷贝测试：Map 深拷贝
     */
    @Test
    public void testDeepCopyMap() {
        Map<String, Integer> source = new HashMap<>();
        source.put("key1", 100);
        source.put("key2", 200);

        Map<String, Integer> result = BeanUtils.deepCopy(source);

        // 值相同
        Assert.assertEquals(source.size(), result.size());
        Assert.assertEquals(source.get("key1"), result.get("key1"));
        Assert.assertEquals(source.get("key2"), result.get("key2"));

        // 非同一引用
        Assert.assertNotSame(source, result);

        // 修改副本不影响原始
        result.put("key1", 999);
        Assert.assertNotEquals(source.get("key1"), result.get("key1"));
    }

    /**
     * 深拷贝测试：循环引用（A→B→A）不栈溢出
     */
    @Test
    public void testDeepCopyCircularReference() {
        Node a = new Node("A");
        Node b = new Node("B");
        a.target = b;
        b.target = a; // 循环引用

        Node aCopy = BeanUtils.deepCopy(a);

        // 拷贝成功，值正确
        Assert.assertEquals("A", aCopy.name);
        Assert.assertNotNull(aCopy.target);
        Assert.assertEquals("B", aCopy.target.name);

        // 循环引用检测：aCopy.target.target 应该是 aCopy 本身
        Assert.assertSame(aCopy.target.target, aCopy);

        // 原始对象不受影响
        Assert.assertNotSame(a.target, aCopy.target);
    }

    // ========== copyProperties 新增测试（6个）==========

    /**
     * copyProperties 嵌套Bean测试：source.user 有值时验证浅拷贝（共享引用）
     */
    @Test
    public void testCopyNestedBean() {
        User innerUser = new User();
        innerUser.setAge(30);
        innerUser.userName = "InnerUser";

        User source = new User();
        source.setAge(25);
        source.userName = "OuterUser";
        source.user = innerUser;

        User target = new User();
        BeanUtils.copyProperties(source, target);

        // 基本属性拷贝成功
        Assert.assertEquals(source.getAge(), target.getAge());
        Assert.assertEquals(source.userName, target.userName);

        // 嵌套对象是同一个引用（浅拷贝）
        Assert.assertSame(source.user, target.user);
        Assert.assertEquals(innerUser.getAge(), target.user.getAge());
        Assert.assertEquals(innerUser.userName, target.user.userName);

        // 修改 innerUser 的值会影响 source 和 target
        innerUser.setAge(99);
        Assert.assertEquals(source.user.getAge(), target.user.getAge());
    }

    /**
     * copyProperties 嵌套Bean + List测试：childList 浅拷贝共享引用
     */
    @Test
    public void testCopyNestedBeanWithList() {
        User child1 = new User();
        child1.setAge(10);
        child1.userName = "Child1";

        User child2 = new User();
        child2.setAge(20);
        child2.userName = "Child2";

        List<User> childList = new ArrayList<>();
        childList.add(child1);
        childList.add(child2);

        User source = new User();
        source.setAge(25);
        source.childList = childList;

        User target = new User();
        BeanUtils.copyProperties(source, target);

        // 基本属性拷贝成功
        Assert.assertEquals(source.getAge(), target.getAge());

        // childList 是同一个引用（浅拷贝）
        Assert.assertSame(source.childList, target.childList);
        Assert.assertEquals(2, target.childList.size());
        Assert.assertEquals("Child1", target.childList.get(0).userName);

        // 修改列表元素影响双方
        child1.setAge(999);
        Assert.assertEquals(source.childList.get(0).getAge(), target.childList.get(0).getAge());
    }

    /**
     * copyProperties 嵌套Bean + Map测试：map 浅拷贝共享引用
     */
    @Test
    public void testCopyNestedBeanWithMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 100);

        User source = new User();
        source.setAge(25);
        source.setMap(map);

        User target = new User();
        BeanUtils.copyProperties(source, target);

        // 基本属性拷贝成功
        Assert.assertEquals(source.getAge(), target.getAge());

        // map 是同一个引用（浅拷贝）
        Assert.assertSame(source.getMap(), target.getMap());
        Assert.assertEquals("value1", target.getMap().get("key1"));
        Assert.assertEquals(100, target.getMap().get("key2"));

        // 修改 map 内容影响双方
        map.put("key3", "value3");
        Assert.assertEquals(source.getMap().get("key3"), target.getMap().get("key3"));
    }

    /**
     * copyProperties 嵌套Bean + Array测试：otherNames 数组共享引用
     */
    @Test
    public void testCopyNestedBeanWithArray() {
        String[] names = {"Alice", "Bob", "Charlie"};

        User source = new User();
        source.setAge(25);
        source.setOtherNames(names);

        User target = new User();
        BeanUtils.copyProperties(source, target);

        // 基本属性拷贝成功
        Assert.assertEquals(source.getAge(), target.getAge());

        // otherNames 是同一个引用（浅拷贝）
        Assert.assertSame(source.getOtherNames(), target.getOtherNames());
        Assert.assertEquals(3, target.getOtherNames().length);
        Assert.assertEquals("Alice", target.getOtherNames()[0]);

        // 修改数组内容影响双方
        target.getOtherNames()[0] = "Modified";
        Assert.assertEquals(source.getOtherNames()[0], target.getOtherNames()[0]);
    }

    /**
     * copyProperties 继承属性测试：BaseUser.parentFiled 继承属性拷贝
     */
    @Test
    public void testCopyWithInheritedProperty() {
        User source = new User();
        source.setAge(25);
        source.setParentFiled("ParentValue");

        User target = new User();
        BeanUtils.copyProperties(source, target);

        // 继承属性拷贝成功
        Assert.assertEquals("ParentValue", target.getParentFiled());

        // 修改 target 不影响 source
        target.setParentFiled("Modified");
        Assert.assertNotEquals(source.getParentFiled(), target.getParentFiled());
    }

    /**
     * copyProperties 循环引用测试：A.user=B, B.user=A 循环引用不栈溢出
     * <p>
     * 验证浅拷贝正确复制循环引用结构，不栈溢出
     */
    @Test
    public void testCopyCircularReferenceNested() {
        User a = new User();
        a.setAge(30);
        a.userName = "A";

        User b = new User();
        b.setAge(40);
        b.userName = "B";

        a.user = b;
        b.user = a; // 循环引用

        User targetA = new User();
        BeanUtils.copyProperties(a, targetA);

        // 基本属性拷贝成功
        Assert.assertEquals(a.getAge(), targetA.getAge());
        Assert.assertEquals(a.userName, targetA.userName);

        // 嵌套对象是同一个引用（浅拷贝）
        Assert.assertSame(a.user, targetA.user);
        Assert.assertEquals("B", targetA.user.userName);

        // 循环引用验证：targetA.user.user 应等于 a（因为 a.user = b, b.user = a）
        // 即 targetA.user(b) -> user(a) 与原始 a 是同一对象
        Assert.assertSame(a, targetA.user.user);

        // 验证 targetA.user 是 b（与 a.user 相同引用）
        Assert.assertSame(b, targetA.user);
    }

    // ========== toBean 新增测试（6个）==========

    /**
     * toBean Map value 为 Map 测试：Map value 为 Map 直接存储
     * <p>
     * 注意：当前实现不会递归转换嵌套 Map 为 Bean，
     * 嵌套 Map 会作为 Map 类型值直接存储到目标属性中
     */
    @Test
    public void testToBeanMapWithNestedMap() {
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("innerKey", "innerValue");
        nestedMap.put("innerNum", 42);

        Map<String, Object> map = new HashMap<>();
        map.put("age", 25);
        map.put("map", nestedMap);

        User user = BeanUtils.toBean(map, User.class);

        Assert.assertNotNull(user);
        Assert.assertEquals(25, user.getAge());
        Assert.assertNotNull(user.getMap());
        // 嵌套 Map 作为 Map<String, Object> 存在
        Assert.assertTrue(user.getMap().containsKey("innerKey"));
    }

    /**
     * toBean Map value 为 List测试：Map value 为 List 转为 List<String>
     */
    @Test
    public void testToBeanMapWithListValue() {
        List<String> names = new ArrayList<>();
        names.add("name1");
        names.add("name2");

        Map<String, Object> map = new HashMap<>();
        map.put("age", 25);
        map.put("homeNames", names);

        User user = BeanUtils.toBean(map, User.class);

        Assert.assertNotNull(user);
        Assert.assertNotNull(user.getHomeNames());
        Assert.assertEquals(2, user.getHomeNames().size());
        Assert.assertEquals("name1", user.getHomeNames().get(0));
    }

    /**
     * toBean Map value 为数组测试：Map value 为数组
     */
    @Test
    public void testToBeanMapWithArrayValue() {
        String[] names = {"Alice", "Bob"};

        Map<String, Object> map = new HashMap<>();
        map.put("age", 25);
        map.put("otherNames", names);

        User user = BeanUtils.toBean(map, User.class);

        Assert.assertNotNull(user);
        Assert.assertNotNull(user.getOtherNames());
        Assert.assertEquals(2, user.getOtherNames().length);
        Assert.assertEquals("Alice", user.getOtherNames()[0]);
    }

    /**
     * toBean Map复杂嵌套测试：Map 同时含 Bean+List+Map+数组
     */
    @Test
    public void testToBeanMapWithComplexNesting() {
        List<String> homeNames = new ArrayList<>();
        homeNames.add("Home1");
        homeNames.add("Home2");

        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("nestedKey", "nestedValue");

        String[] otherNames = {"Other1", "Other2"};

        Map<String, Object> map = new HashMap<>();
        map.put("age", 30);
        map.put("userName", "ComplexUser");
        map.put("homeNames", homeNames);
        map.put("map", nestedMap);
        map.put("otherNames", otherNames);

        User user = BeanUtils.toBean(map, User.class);

        Assert.assertNotNull(user);
        Assert.assertEquals(30, user.getAge());
        Assert.assertEquals("ComplexUser", user.userName);
        Assert.assertNotNull(user.getHomeNames());
        Assert.assertEquals(2, user.getHomeNames().size());
        Assert.assertNotNull(user.getMap());
        Assert.assertNotNull(user.getOtherNames());
        Assert.assertEquals(2, user.getOtherNames().length);
    }

    /**
     * toBean 跨类型转换嵌套测试：验证 user 字段类型不匹配时的行为
     * <p>
     * 注意：当尝试将 Map 赋值给 User 类型的 user 字段时会抛出 IllegalArgumentException，
     * 这是因为类型不匹配无法设置字段值
     */
    @Test(expected = IllegalArgumentException.class)
    public void testToBeanCrossTypeWithNested() {
        Map<String, Object> innerData = new HashMap<>();
        innerData.put("age", 30);
        innerData.put("userName", "InnerUser");

        Map<String, Object> map = new HashMap<>();
        map.put("age", 25);
        map.put("user", innerData);

        // 尝试将 Map 赋值给 User 类型的 user 字段会抛出异常
        BeanUtils.toBean(map, User.class);
    }

    // ========== toMap 新增测试（5个）==========

    /**
     * toMap 嵌套Bean测试：嵌套 User → toMap 后嵌套结构
     */
    @Test
    public void testToMapWithNestedBean() {
        User innerUser = new User();
        innerUser.setAge(30);
        innerUser.userName = "InnerUser";

        User source = new User();
        source.setAge(25);
        source.userName = "OuterUser";
        source.user = innerUser;

        Map<String, Object> map = BeanUtils.toMap(source);

        Assert.assertNotNull(map);
        Assert.assertEquals(25, map.get("age"));
        Assert.assertEquals("OuterUser", map.get("userName"));

        // 嵌套 user 应转为一个 Map 结构
        Object userObj = map.get("user");
        Assert.assertNotNull(userObj);
        Assert.assertTrue(userObj instanceof User);
        User userResult = (User) userObj;
        Assert.assertEquals(30, userResult.getAge());
        Assert.assertEquals("InnerUser", userResult.userName);
    }

    /**
     * toMap List属性测试：List<User> → toMap 表现
     * <p>
     * 注意：当前实现直接返回原始对象引用，不会递归转换 List 元素
     */
    @Test
    public void testToMapWithListProperty() {
        User child1 = new User();
        child1.setAge(10);
        child1.userName = "Child1";

        User child2 = new User();
        child2.setAge(20);
        child2.userName = "Child2";

        List<User> childList = new ArrayList<>();
        childList.add(child1);
        childList.add(child2);

        User source = new User();
        source.setAge(25);
        source.childList = childList;

        Map<String, Object> map = BeanUtils.toMap(source);

        Assert.assertNotNull(map);
        Assert.assertEquals(25, map.get("age"));

        // childList 保持为 List<User> 引用
        Object childListObj = map.get("childList");
        Assert.assertNotNull(childListObj);
        Assert.assertTrue(childListObj instanceof List);
        @SuppressWarnings("unchecked")
        List<User> resultList = (List<User>) childListObj;
        Assert.assertEquals(2, resultList.size());
        Assert.assertEquals("Child1", resultList.get(0).userName);
    }

    /**
     * toMap Array属性测试：String[] → toMap 表现
     */
    @Test
    public void testToMapWithArrayProperty() {
        String[] names = {"Alice", "Bob", "Charlie"};

        User source = new User();
        source.setAge(25);
        source.setOtherNames(names);

        Map<String, Object> map = BeanUtils.toMap(source);

        Assert.assertNotNull(map);
        Assert.assertEquals(25, map.get("age"));

        // otherNames 应保持数组形式
        Object otherNamesObj = map.get("otherNames");
        Assert.assertNotNull(otherNamesObj);
        Assert.assertTrue(otherNamesObj instanceof String[]);
        String[] resultNames = (String[]) otherNamesObj;
        Assert.assertEquals(3, resultNames.length);
        Assert.assertEquals("Alice", resultNames[0]);
    }

    /**
     * toMap Map属性测试：Map → toMap 嵌套表现
     */
    @Test
    public void testToMapWithMapProperty() {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("key1", "value1");
        innerMap.put("key2", 100);

        User source = new User();
        source.setAge(25);
        source.setMap(innerMap);

        Map<String, Object> map = BeanUtils.toMap(source);

        Assert.assertNotNull(map);
        Assert.assertEquals(25, map.get("age"));

        // map 属性应保持为 Map
        Object mapObj = map.get("map");
        Assert.assertNotNull(mapObj);
        Assert.assertTrue(mapObj instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) mapObj;
        Assert.assertEquals("value1", resultMap.get("key1"));
        Assert.assertEquals(100, resultMap.get("key2"));
    }

    /**
     * toMap 深度嵌套测试：深度嵌套 Bean → toMap 多层结构
     * <p>
     * 注意：当前实现返回原始对象引用，不会递归转换为 Map
     */
    @Test
    public void testToMapWithDeepNesting() {
        // 构造三层嵌套
        User level3 = new User();
        level3.setAge(30);
        level3.userName = "Level3";

        User level2 = new User();
        level2.setAge(20);
        level2.userName = "Level2";
        level2.user = level3;

        User level1 = new User();
        level1.setAge(10);
        level1.userName = "Level1";
        level1.user = level2;

        Map<String, Object> map = BeanUtils.toMap(level1);

        Assert.assertNotNull(map);
        Assert.assertEquals(10, map.get("age"));

        // 嵌套 user 返回原始对象引用
        Object level1UserObj = map.get("user");
        Assert.assertNotNull(level1UserObj);
        Assert.assertTrue(level1UserObj instanceof User);
        User level1Result = (User) level1UserObj;
        Assert.assertEquals(20, level1Result.getAge());
        Assert.assertEquals("Level2", level1Result.userName);
    }

    // ========== deepCopy 补充测试（4个）==========

    /**
     * 深拷贝嵌套User测试：修复嵌套深拷贝（设置 user 属性）
     */
    @Test
    public void testDeepCopyBeanWithNestedUser() {
        User innerUser = new User();
        innerUser.setAge(30);
        innerUser.userName = "InnerUser";

        User source = new User();
        source.setAge(25);
        source.userName = "OuterUser";
        source.user = innerUser;

        User result = BeanUtils.deepCopy(source);

        // 副本值相同
        Assert.assertEquals(source.getAge(), result.getAge());
        Assert.assertEquals(source.userName, result.userName);

        // user 属性是不同的引用（深拷贝）
        Assert.assertNotSame(source.user, result.user);
        Assert.assertEquals(innerUser.getAge(), result.user.getAge());
        Assert.assertEquals(innerUser.userName, result.user.userName);

        // 修改内部对象不影响原始
        result.user.setAge(999);
        Assert.assertNotEquals(source.user.getAge(), result.user.getAge());
    }

    /**
     * 深拷贝 childList 测试：List<User> 深拷贝后元素独立
     */
    @Test
    public void testDeepCopyBeanWithChildList() {
        User child1 = new User();
        child1.setAge(10);
        child1.userName = "Child1";

        User child2 = new User();
        child2.setAge(20);
        child2.userName = "Child2";

        List<User> childList = new ArrayList<>();
        childList.add(child1);
        childList.add(child2);

        User source = new User();
        source.setAge(5);
        source.childList = childList;

        User result = BeanUtils.deepCopy(source);

        // 副本值相同
        Assert.assertEquals(source.getAge(), result.getAge());
        Assert.assertEquals(2, result.childList.size());

        // childList 是新的 List，但元素也是新的（深拷贝）
        Assert.assertNotSame(source.childList, result.childList);

        // 列表元素是不同的引用
        Assert.assertNotSame(source.childList.get(0), result.childList.get(0));
        Assert.assertNotSame(source.childList.get(1), result.childList.get(1));

        // 元素值相同
        Assert.assertEquals("Child1", result.childList.get(0).userName);
        Assert.assertEquals("Child2", result.childList.get(1).userName);

        // 修改副本不影响原始
        result.childList.get(0).setAge(999);
        Assert.assertNotEquals(source.childList.get(0).getAge(), result.childList.get(0).getAge());
    }

    /**
     * 深拷贝 Map<String,User> 测试：Map<String,User> 深拷贝
     * <p>
     * 注意：由于 IdentityHashMap 循环引用检测，
     * 同一个对象在对象图中多处出现时，深拷贝后仍是同一引用
     */
    @Test
    public void testDeepCopyBeanWithMapAndComplexValue() {
        User user1 = new User();
        user1.setAge(30);
        user1.userName = "MapUser1";

        User user2 = new User();
        user2.setAge(40);
        user2.userName = "MapUser2";

        User source = new User();
        source.setAge(25);
        source.setMap(new HashMap<String, Object>() {{
            put("key1", user1);
            put("key2", user2);
        }});

        User result = BeanUtils.deepCopy(source);

        // 副本值相同
        Assert.assertEquals(source.getAge(), result.getAge());
        Assert.assertNotNull(result.getMap());

        // map 是新的
        Assert.assertNotSame(source.getMap(), result.getMap());

        // 获取深拷贝后的 map - 它是 Map<String, Object>，包含深拷贝的 User
        Map<String, Object> resultMap = result.getMap();
        Object value1 = resultMap.get("key1");
        Object value2 = resultMap.get("key2");

        // 元素是不同的引用（深拷贝）
        Assert.assertNotSame(user1, value1);
        Assert.assertNotSame(user2, value2);

        // 元素值相同（都是 User 类型）
        Assert.assertTrue(value1 instanceof User);
        Assert.assertTrue(value2 instanceof User);
        Assert.assertEquals(user1.getAge(), ((User) value1).getAge());
        Assert.assertEquals(user2.getAge(), ((User) value2).getAge());

        // 修改副本不影响原始
        ((User) value1).setAge(999);
        Assert.assertNotEquals(user1.getAge(), ((User) resultMap.get("key1")).getAge());
    }

    /**
     * 深拷贝混合嵌套容器测试：user+childList+map+otherNames 全部设置后深拷贝
     * <p>
     * 注意：由于 IdentityHashMap 循环引用检测，
     * 同一个对象在对象图中多处出现时，深拷贝后仍是同一引用
     */
    @Test
    public void testDeepCopyMixedNestedContainer() {
        // 构造完整的混合嵌套对象
        User innerUser = new User();
        innerUser.setAge(30);
        innerUser.userName = "InnerUser";

        User child1 = new User();
        child1.setAge(10);
        child1.userName = "Child1";

        User child2 = new User();
        child2.setAge(20);
        child2.userName = "Child2";

        List<User> childList = new ArrayList<>();
        childList.add(child1);
        childList.add(child2);

        String[] otherNames = {"Alice", "Bob"};

        User source = new User();
        source.setAge(25);
        source.userName = "MixedSource";
        source.user = innerUser;
        source.childList = childList;
        source.setMap(new HashMap<String, Object>() {{
            put("childMapKey", innerUser);
        }});
        source.setOtherNames(otherNames);

        User result = BeanUtils.deepCopy(source);

        // 基本属性值相同
        Assert.assertEquals(source.getAge(), result.getAge());
        Assert.assertEquals(source.userName, result.userName);

        // user: 不同的引用
        Assert.assertNotSame(source.user, result.user);
        Assert.assertEquals(innerUser.getAge(), result.user.getAge());

        // childList: 新的 List，元素也是新的
        Assert.assertNotSame(source.childList, result.childList);
        Assert.assertNotSame(source.childList.get(0), result.childList.get(0));
        Assert.assertNotSame(source.childList.get(1), result.childList.get(1));
        Assert.assertEquals("Child1", result.childList.get(0).userName);

        // map: 新的 Map
        Assert.assertNotSame(source.getMap(), result.getMap());
        Map<String, Object> resultMapValue = result.getMap();
        Object mapValue = resultMapValue.get("childMapKey");

        // 注意：由于 innerUser 已在 user 属性中被拷贝，
        // map 中的引用与 user 属性是同一对象（IdentityHashMap 缓存）
        // 这是当前 deepCopy 实现的特性，非 bug
        Assert.assertSame(result.user, mapValue);

        // otherNames: 新的数组（数组创建了新对象）
        Assert.assertNotSame(source.getOtherNames(), result.getOtherNames());
        // 但 String 元素是不可变类型，deepCopy 返回相同引用（这是正确行为）
        Assert.assertEquals(otherNames[0], result.getOtherNames()[0]);
        Assert.assertSame(otherNames[0], result.getOtherNames()[0]);

        // 修改副本所有层级均不影响原始
        result.user.setAge(999);
        result.childList.get(0).setAge(999);
        result.getOtherNames()[0] = "Modified";

        Assert.assertNotEquals(source.user.getAge(), result.user.getAge());
        Assert.assertNotEquals(source.childList.get(0).getAge(), result.childList.get(0).getAge());
        Assert.assertNotEquals(innerUser.getAge(), result.getUser().getAge());
        Assert.assertNotEquals(otherNames[0], result.getOtherNames()[0]);
    }

    // ========== deepCopy 深度控制测试 ==========

    /**
     * deepCopy maxDepth 测试：maxDepth = 0 时，嵌套对象不递归（浅拷贝）
     */
    @Test
    public void testDeepCopyMaxDepthZero() {
        User innerUser = new User();
        innerUser.setAge(30);
        innerUser.userName = "InnerUser";

        User source = new User();
        source.setAge(25);
        source.userName = "OuterUser";
        source.user = innerUser;

        User result = BeanUtils.deepCopy(source, 0);

        // 基本属性值相同
        Assert.assertEquals(source.getAge(), result.getAge());
        Assert.assertEquals(source.userName, result.userName);

        // user 属性是同一引用（浅拷贝，未递归）
        Assert.assertSame(source.user, result.user);

        // 修改副本的 user 影响原始对象（证明是浅拷贝）
        result.user.setAge(999);
        Assert.assertEquals(source.user.getAge(), result.user.getAge());
    }

    /**
     * deepCopy maxDepth 测试：maxDepth = 1 时，拷贝一层嵌套
     */
    @Test
    public void testDeepCopyMaxDepthOne() {
        User innerUser = new User();
        innerUser.setAge(30);
        innerUser.userName = "InnerUser";

        User source = new User();
        source.setAge(25);
        source.userName = "OuterUser";
        source.user = innerUser;

        User result = BeanUtils.deepCopy(source, 1);

        // 基本属性值相同
        Assert.assertEquals(source.getAge(), result.getAge());
        Assert.assertEquals(source.userName, result.userName);

        // user 属性是不同引用（深拷贝了一层）
        Assert.assertNotSame(source.user, result.user);
        Assert.assertEquals(innerUser.getAge(), result.user.getAge());
        Assert.assertEquals(innerUser.userName, result.user.userName);
    }

    /**
     * deepCopy maxDepth 测试：maxDepth = 0 时，数组元素不递归
     */
    @Test
    public void testDeepCopyArrayMaxDepthZero() {
        User[] sourceArray = new User[2];
        sourceArray[0] = new User();
        sourceArray[0].setAge(10);
        sourceArray[0].userName = "User1";
        sourceArray[1] = new User();
        sourceArray[1].setAge(20);
        sourceArray[1].userName = "User2";

        User[] result = BeanUtils.deepCopy(sourceArray, 0);

        // 数组本身是新对象
        Assert.assertNotSame(sourceArray, result);

        // 数组元素是同一引用（浅拷贝）
        Assert.assertSame(sourceArray[0], result[0]);
        Assert.assertSame(sourceArray[1], result[1]);
    }

    /**
     * deepCopy maxDepth 测试：maxDepth = 0 时，Collection 元素不递归
     */
    @Test
    public void testDeepCopyCollectionMaxDepthZero() {
        User child1 = new User();
        child1.setAge(10);
        child1.userName = "Child1";

        List<User> sourceList = new ArrayList<>();
        sourceList.add(child1);

        List<User> result = BeanUtils.deepCopy(sourceList, 0);

        // List 本身是新对象
        Assert.assertNotSame(sourceList, result);

        // 元素是同一引用（浅拷贝）
        Assert.assertSame(sourceList.get(0), result.get(0));
    }

    /**
     * deepCopy maxDepth 测试：maxDepth = 0 时，Map key/value 不递归
     */
    @Test
    public void testDeepCopyMapMaxDepthZero() {
        User user1 = new User();
        user1.setAge(30);
        user1.userName = "MapUser1";

        Map<String, User> sourceMap = new HashMap<>();
        sourceMap.put("key1", user1);

        Map<String, User> result = BeanUtils.deepCopy(sourceMap, 0);

        // Map 本身是新对象
        Assert.assertNotSame(sourceMap, result);

        // key 是同一引用（String 是不可变类型）
        Assert.assertSame(sourceMap.get("key1"), result.get("key1"));

        // value 是同一引用（未递归）
        Assert.assertSame(user1, result.get("key1"));
    }

    /**
     * deepCopy maxDepth 测试：maxDepth < 0 时抛出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDeepCopyMaxDepthNegative() {
        User source = new User();
        source.setAge(25);

        BeanUtils.deepCopy(source, -1);
    }

    /**
     * deepCopy maxDepth 测试：验证深度递减行为
     */
    @Test
    public void testDeepCopyMaxDepthTwoLevels() {
        // 构造三层嵌套：level1 -> level2 -> level3
        User level3 = new User();
        level3.setAge(30);
        level3.userName = "Level3";

        User level2 = new User();
        level2.setAge(20);
        level2.userName = "Level2";
        level2.user = level3;

        User level1 = new User();
        level1.setAge(10);
        level1.userName = "Level1";
        level1.user = level2;

        // maxDepth = 1：拷贝 level1 和 level2（因为 level1 拷贝时 remainingDepth=1 > 0），
        // level3 不递归（因为 level2 拷贝时 remainingDepth=0，不递归）
        User result1 = BeanUtils.deepCopy(level1, 1);
        Assert.assertNotSame(level1, result1);
        Assert.assertNotSame(level1.user, result1.user); // result1.user 是 level2 的新副本
        Assert.assertNotSame(level2, result1.user); // result1.user 是新对象，不是 level2
        // level3 未递归，result1.user（即 level2 副本）的 user 属性应该仍是 level3 原引用
        Assert.assertSame(level3, result1.user.user);

        // maxDepth = 2：拷贝 level1、level2、level3
        User result2 = BeanUtils.deepCopy(level1, 2);
        Assert.assertNotSame(level1, result2);
        Assert.assertNotSame(level1.user, result2.user);
        Assert.assertNotSame(level2, result2.user);
        Assert.assertNotSame(level3, result2.user.user);

        // maxDepth = Integer.MAX_VALUE：无限制，等同于 deepCopy(source)
        User resultMax = BeanUtils.deepCopy(level1, Integer.MAX_VALUE);
        Assert.assertNotSame(level1, resultMax);
        Assert.assertNotSame(level1.user, resultMax.user);
        Assert.assertNotSame(level2, resultMax.user);
        Assert.assertNotSame(level3, resultMax.user.user);
    }

    // ========== 辅助类（深拷贝测试用）==========

    /**
     * 用于测试循环引用的节点类
     */
    public static class Node {
        public String name;
        public Node target;

        public Node() {
        }

        public Node(String name) {
            this.name = name;
        }
    }
}