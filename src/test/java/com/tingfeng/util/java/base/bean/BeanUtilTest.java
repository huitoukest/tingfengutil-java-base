package com.tingfeng.util.java.base.bean;

import com.tingfeng.util.java.base.bean.copier.CopyOptions;
import com.tingfeng.util.java.base.bean.copier.MapValueProvider;
import com.tingfeng.util.java.base.common.utils.TestUtils;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

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
public class BeanUtilTest {

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
        BeanUtil.copyProperties(source, target);

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
        BeanUtil.copyProperties(source, target);

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

        List<User> targets = BeanUtil.toList(sources, User.class);

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

        List<User> targets = BeanUtil.toList(sources, User.class,
                CopyOptions.create().setIgnoreProperties("age"));

        Assert.assertEquals(1, targets.size());
        Assert.assertEquals(0, targets.get(0).getAge()); // primitive int defaults to 0
        Assert.assertEquals(user1.getC(), targets.get(0).getC());
        Assert.assertEquals(user1.userName, targets.get(0).userName);
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
        BeanUtil.copyProperties(source, target, options);

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
        BeanUtil.copyProperties(source, target, options);

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

        Map<String, Object> map = BeanUtil.toMap(user);

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

        Map<String, Object> map = BeanUtil.toMap(user, "age", "c");

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
        User user = BeanUtil.toBean(provider, User.class);

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
        User user = BeanUtil.toBean(provider, User.class, options);

        Assert.assertNotNull(user);
        Assert.assertEquals(0, user.getAge()); // age被忽略，使用默认值
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

        BeanUtil.copyProperties(null, target);

        Assert.assertEquals(50, target.getAge()); // target保持不变
    }

    @Test
    public void testCopyPropertiesTargetNull() {
        User source = new User();
        source.setAge(25);

        BeanUtil.copyProperties(source, null);
        // 无异常即通过
    }

    @Test
    public void testToBeanSourceNull() {
        User user = BeanUtil.toBean(null, User.class);
        Assert.assertNull(user);
    }

    @Test
    public void testToBeanFromProviderNull() {
        User user = BeanUtil.toBean((MapValueProvider) null, User.class);
        Assert.assertNull(user);
    }

    @Test
    public void testToListSourceNull() {
        List<User> result = BeanUtil.toList(null, User.class);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testToListSourceEmpty() {
        List<User> sources = new ArrayList<>();
        List<User> result = BeanUtil.toList(sources, User.class);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testToMapBeanNull() {
        Map<String, Object> map = BeanUtil.toMap((Object) null);
        Assert.assertNotNull(map);
        Assert.assertTrue(map.isEmpty());
    }

    /**
     * 边界测试：targetClass无默认构造器时的异常
     */
    @Test(expected = com.tingfeng.util.java.base.lang.exception.BaseException.class)
    public void testToBeanNoDefaultConstructor() {
        NoDefaultConstructorBean source = new NoDefaultConstructorBean(100);

        BeanUtil.toBean(source, NoDefaultConstructorBean.class);
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
            BeanUtil.copyProperties(source, target);
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
}