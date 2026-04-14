package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.lang.base.IEnum;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * EnumUtils功能测试类
 */
public class EnumUtilsTest {

    /**
     * 测试枚举类
     */
    enum TestEnum implements IEnum<String> {
        ONE("1", "第一个"),
        TWO("2", "第二个"),
        THREE("3", "第三个");

        private final String value;
        private final String desc;

        TestEnum(String value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        @Override
        public String getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 测试枚举类 - 用于缓存测试
     */
    enum Name implements IEnum<String> {
        A("A", "123"),
        B("B", "456"),
        C("C", "789");

        private String value;
        private String code;

        Name(String value, String code) {
            this.value = value;
            this.code = code;
        }

        @Override
        public String getValue() {
            return this.value;
        }

        public String getCode() {
            return code;
        }
    }

    /**
     * 空枚举类 - 用于边界测试
     */
    enum EmptyEnum {
    }

    /**
     * 单枚举值类 - 用于边界测试
     */
    enum SingleEnum implements IEnum<String> {
        ONLY("only");

        private final String value;

        SingleEnum(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    /**
     * 测试getEnumByValue方法
     */
    @Test
    public void getEnumByValueTest() {
        Assert.assertTrue(Name.B.equals(EnumUtils.getEnum(Name.class, "456", Name::getCode)));
        Assert.assertTrue(Name.A.equals(EnumUtils.getEnumByValue(Name.class, "A")));
        Assert.assertTrue(Name.A.equals(EnumUtils.getEnum(Name.class, "123", Name::getCode)));
        Assert.assertTrue(Name.C.equals(EnumUtils.getEnumByValue(Name.class, "C")));
    }

    /**
     * 测试getEnumByValue方法 - 不使用缓存
     */
    @Test
    public void getEnumByValueWithoutCacheTest() {
        Assert.assertTrue(Name.B.equals(EnumUtils.getEnumByValue(Name.class, "B", false)));
        Assert.assertTrue(Name.A.equals(EnumUtils.getEnumByValue(Name.class, "A", false)));
    }

    /**
     * 测试getEnumByValue方法 - 使用缓存
     */
    @Test
    public void getEnumByValueWithCacheTest() {
        Assert.assertTrue(Name.B.equals(EnumUtils.getEnumByValue(Name.class, "B", true)));
        Assert.assertTrue(Name.A.equals(EnumUtils.getEnumByValue(Name.class, "A", true)));
    }

    /**
     * 测试getEnum方法
     */
    @Test
    public void getEnumTest() {
        TestEnum result = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue);
        Assert.assertEquals(TestEnum.ONE, result);

        result = EnumUtils.getEnum(TestEnum.class, "第二个", TestEnum::getDesc);
        Assert.assertEquals(TestEnum.TWO, result);

        result = EnumUtils.getEnum(TestEnum.class, "999", TestEnum::getValue);
        Assert.assertNull(result);
    }

    /**
     * 测试getEnum方法 - 不使用缓存
     */
    @Test
    public void getEnumWithoutCacheTest() {
        TestEnum result = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, false);
        Assert.assertEquals(TestEnum.ONE, result);

        result = EnumUtils.getEnum(TestEnum.class, "999", TestEnum::getValue, false);
        Assert.assertNull(result);
    }

    /**
     * 测试getEnum方法 - 使用缓存
     */
    @Test
    public void getEnumWithCacheTest() {
        TestEnum result = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, true);
        Assert.assertEquals(TestEnum.ONE, result);

        result = EnumUtils.getEnum(TestEnum.class, "999", TestEnum::getValue, true);
        Assert.assertNull(result);
    }

    /**
     * 测试getEnumByName方法
     */
    @Test
    public void getEnumByNameTest() {
        TestEnum result = EnumUtils.getEnumByName(TestEnum.class, "ONE");
        Assert.assertEquals(TestEnum.ONE, result);

        result = EnumUtils.getEnumByName(TestEnum.class, "TWO");
        Assert.assertEquals(TestEnum.TWO, result);

        result = EnumUtils.getEnumByName(TestEnum.class, "不存在的名称");
        Assert.assertNull(result);

        result = EnumUtils.getEnumByName(TestEnum.class, null);
        Assert.assertNull(result);

        result = EnumUtils.getEnumByName(TestEnum.class, "");
        Assert.assertNull(result);
    }

    /**
     * 测试getEnumByName方法 - 不使用缓存
     */
    @Test
    public void getEnumByNameWithoutCacheTest() {
        TestEnum result = EnumUtils.getEnumByName(TestEnum.class, "ONE", false);
        Assert.assertEquals(TestEnum.ONE, result);

        result = EnumUtils.getEnumByName(TestEnum.class, "不存在的名称", false);
        Assert.assertNull(result);
    }

    /**
     * 测试getEnumByName方法 - 使用缓存
     */
    @Test
    public void getEnumByNameWithCacheTest() {
        TestEnum result = EnumUtils.getEnumByName(TestEnum.class, "ONE", true);
        Assert.assertEquals(TestEnum.ONE, result);

        result = EnumUtils.getEnumByName(TestEnum.class, "不存在的名称", true);
        Assert.assertNull(result);
    }

    /**
     * 测试getEnumByOrdinal方法
     */
    @Test
    public void getEnumByOrdinalTest() {
        TestEnum result = EnumUtils.getEnumByOrdinal(TestEnum.class, 0);
        Assert.assertEquals(TestEnum.ONE, result);

        result = EnumUtils.getEnumByOrdinal(TestEnum.class, 1);
        Assert.assertEquals(TestEnum.TWO, result);

        result = EnumUtils.getEnumByOrdinal(TestEnum.class, 2);
        Assert.assertEquals(TestEnum.THREE, result);

        result = EnumUtils.getEnumByOrdinal(TestEnum.class, -1);
        Assert.assertNull(result);

        result = EnumUtils.getEnumByOrdinal(TestEnum.class, 999);
        Assert.assertNull(result);
    }

    /**
     * 测试getEnumList方法
     */
    @Test
    public void getEnumListTest() {
        List<TestEnum> list = EnumUtils.getEnumList(TestEnum.class);
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains(TestEnum.ONE));
        Assert.assertTrue(list.contains(TestEnum.TWO));
        Assert.assertTrue(list.contains(TestEnum.THREE));
    }

    /**
     * 测试contains方法
     */
    @Test
    public void containsTest() {
        boolean contains = EnumUtils.contains(TestEnum.class, "1", TestEnum::getValue);
        Assert.assertTrue(contains);

        contains = EnumUtils.contains(TestEnum.class, "999", TestEnum::getValue);
        Assert.assertFalse(contains);
    }

    /**
     * 测试contains方法 - 不使用缓存
     */
    @Test
    public void containsWithoutCacheTest() {
        boolean contains = EnumUtils.contains(TestEnum.class, "1", TestEnum::getValue, false);
        Assert.assertTrue(contains);

        contains = EnumUtils.contains(TestEnum.class, "999", TestEnum::getValue, false);
        Assert.assertFalse(contains);
    }

    /**
     * 测试contains方法 - 使用缓存
     */
    @Test
    public void containsWithCacheTest() {
        boolean contains = EnumUtils.contains(TestEnum.class, "1", TestEnum::getValue, true);
        Assert.assertTrue(contains);

        contains = EnumUtils.contains(TestEnum.class, "999", TestEnum::getValue, true);
        Assert.assertFalse(contains);
    }

    /**
     * 测试containsByValue方法
     */
    @Test
    public void containsByValueTest() {
        boolean contains = EnumUtils.containsByValue(TestEnum.class, "1");
        Assert.assertTrue(contains);

        contains = EnumUtils.containsByValue(TestEnum.class, "999");
        Assert.assertFalse(contains);
    }

    /**
     * 测试containsByValue方法 - 不使用缓存
     */
    @Test
    public void containsByValueWithoutCacheTest() {
        boolean contains = EnumUtils.containsByValue(TestEnum.class, "1", false);
        Assert.assertTrue(contains);

        contains = EnumUtils.containsByValue(TestEnum.class, "999", false);
        Assert.assertFalse(contains);
    }

    /**
     * 测试containsByValue方法 - 使用缓存
     */
    @Test
    public void containsByValueWithCacheTest() {
        boolean contains = EnumUtils.containsByValue(TestEnum.class, "1", true);
        Assert.assertTrue(contains);

        contains = EnumUtils.containsByValue(TestEnum.class, "999", true);
        Assert.assertFalse(contains);
    }

    /**
     * 测试containsName方法
     */
    @Test
    public void containsNameTest() {
        boolean contains = EnumUtils.containsName(TestEnum.class, "ONE");
        Assert.assertTrue(contains);

        contains = EnumUtils.containsName(TestEnum.class, "不存在的名称");
        Assert.assertFalse(contains);

        contains = EnumUtils.containsName(TestEnum.class, null);
        Assert.assertFalse(contains);

        contains = EnumUtils.containsName(TestEnum.class, "");
        Assert.assertFalse(contains);
    }

    /**
     * 测试containsName方法 - 不使用缓存
     */
    @Test
    public void containsNameWithoutCacheTest() {
        boolean contains = EnumUtils.containsName(TestEnum.class, "ONE", false);
        Assert.assertTrue(contains);

        contains = EnumUtils.containsName(TestEnum.class, "不存在的名称", false);
        Assert.assertFalse(contains);
    }

    /**
     * 测试containsName方法 - 使用缓存
     */
    @Test
    public void containsNameWithCacheTest() {
        boolean contains = EnumUtils.containsName(TestEnum.class, "ONE", true);
        Assert.assertTrue(contains);

        contains = EnumUtils.containsName(TestEnum.class, "不存在的名称", true);
        Assert.assertFalse(contains);
    }

    /**
     * 测试getValueList方法
     */
    @Test
    public void getValueListTest() {
        List<String> list = EnumUtils.getValueList(TestEnum.class, TestEnum::getValue);
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains("1"));
        Assert.assertTrue(list.contains("2"));
        Assert.assertTrue(list.contains("3"));
    }

    /**
     * 测试getValueListByValue方法
     */
    @Test
    public void getValueListByValueTest() {
        List<String> list = EnumUtils.getValueListByValue(TestEnum.class);
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains("1"));
        Assert.assertTrue(list.contains("2"));
        Assert.assertTrue(list.contains("3"));
    }

    /**
     * 测试getNameList方法
     */
    @Test
    public void getNameListTest() {
        List<String> list = EnumUtils.getNameList(TestEnum.class);
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains("ONE"));
        Assert.assertTrue(list.contains("TWO"));
        Assert.assertTrue(list.contains("THREE"));
    }

    /**
     * 测试getOrdinalList方法
     */
    @Test
    public void getOrdinalListTest() {
        List<Integer> list = EnumUtils.getOrdinalList(TestEnum.class);
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains(0));
        Assert.assertTrue(list.contains(1));
        Assert.assertTrue(list.contains(2));
    }

    /**
     * 测试clearCache方法
     */
    @Test
    public void clearCacheTest() {
        TestEnum result = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, true);
        Assert.assertEquals(TestEnum.ONE, result);

        EnumUtils.clearCache(TestEnum.class, TestEnum::getValue);

        result = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, true);
        Assert.assertEquals(TestEnum.ONE, result);
    }

    /**
     * 测试clearAllCache方法
     */
    @Test
    public void clearAllCacheTest() {
        TestEnum result = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, true);
        Assert.assertEquals(TestEnum.ONE, result);

        EnumUtils.clearAllCache(TestEnum.class);

        result = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, true);
        Assert.assertEquals(TestEnum.ONE, result);
    }

    /**
     * 测试clearAllCache方法 - 清除所有缓存
     */
    @Test
    public void clearAllCacheAllTest() {
        TestEnum result = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, true);
        Assert.assertEquals(TestEnum.ONE, result);

        EnumUtils.clearAllCache();

        result = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, true);
        Assert.assertEquals(TestEnum.ONE, result);
    }

    /**
     * 测试getBy方法 - 带类参数
     */
    @Test
    public void getByWithClassTest() {
        TestEnum result = EnumUtils.getBy(TestEnum.class, TestEnum::getValue, "1");
        Assert.assertEquals(TestEnum.ONE, result);

        result = EnumUtils.getBy(TestEnum.class, TestEnum::getDesc, "第二个");
        Assert.assertEquals(TestEnum.TWO, result);

        result = EnumUtils.getBy(TestEnum.class, TestEnum::getValue, "999");
        Assert.assertNull(result);
    }

    /**
     * 测试getByOrDefault方法 - 带类参数
     */
    @Test
    public void getByOrDefaultWithClassTest() {
        TestEnum result = EnumUtils.getByOrDefault(TestEnum.class, TestEnum::getValue, "1", TestEnum.THREE);
        Assert.assertEquals(TestEnum.ONE, result);

        result = EnumUtils.getByOrDefault(TestEnum.class, TestEnum::getValue, "999", TestEnum.THREE);
        Assert.assertEquals(TestEnum.THREE, result);
    }

    /**
     * 测试getByOrThrow方法 - 带类参数
     */
    @Test
    public void getByOrThrowWithClassTest() {
        TestEnum result = EnumUtils.getByOrThrow(TestEnum.class, TestEnum::getValue, "2");
        Assert.assertEquals(TestEnum.TWO, result);

        try {
            EnumUtils.getByOrThrow(TestEnum.class, TestEnum::getValue, "999");
            Assert.fail("应该抛出IllegalArgumentException异常");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("with value: 999"));
        }
    }

    /**
     * 测试getByPredicate方法
     */
    @Test
    public void getByPredicateTest() {
        TestEnum result = EnumUtils.getByPredicate(TestEnum.class, e -> e.getDesc().equals("第二个"));
        Assert.assertEquals(TestEnum.TWO, result);

        result = EnumUtils.getByPredicate(TestEnum.class, e -> e.getDesc().equals("不存在的描述"));
        Assert.assertNull(result);
    }

    /**
     * 测试getByPredicateOrDefault方法
     */
    @Test
    public void getByPredicateOrDefaultTest() {
        TestEnum result = EnumUtils.getByPredicateOrDefault(TestEnum.class, e -> e.getDesc().equals("第二个"), TestEnum.ONE);
        Assert.assertEquals(TestEnum.TWO, result);

        result = EnumUtils.getByPredicateOrDefault(TestEnum.class, e -> e.getDesc().equals("不存在的描述"), TestEnum.ONE);
        Assert.assertEquals(TestEnum.ONE, result);
    }

    /**
     * 测试getByPredicateOrThrow方法
     */
    @Test
    public void getByPredicateOrThrowTest() {
        TestEnum result = EnumUtils.getByPredicateOrThrow(TestEnum.class, e -> e.getDesc().equals("第二个"));
        Assert.assertEquals(TestEnum.TWO, result);

        try {
            EnumUtils.getByPredicateOrThrow(TestEnum.class, e -> e.getDesc().equals("不存在的描述"));
            Assert.fail("应该抛出IllegalArgumentException异常");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("matching the predicate"));
        }
    }

    /**
     * 测试getByList方法 - 带类参数
     */
    @Test
    public void getByListWithClassTest() {
        List<TestEnum> result = EnumUtils.getByList(TestEnum.class, TestEnum::getValue, "1");
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(TestEnum.ONE, result.get(0));

        result = EnumUtils.getByList(TestEnum.class, TestEnum::getValue, "999");
        Assert.assertTrue(result.isEmpty());
    }

    /**
     * 测试getByList方法 - 不使用缓存
     */
    @Test
    public void getByListWithoutCacheTest() {
        List<TestEnum> result = EnumUtils.getByList(TestEnum.class, TestEnum::getValue, "1", false);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(TestEnum.ONE, result.get(0));

        result = EnumUtils.getByList(TestEnum.class, TestEnum::getValue, "999", false);
        Assert.assertTrue(result.isEmpty());
    }

    /**
     * 测试getByList方法 - 使用缓存
     */
    @Test
    public void getByListWithCacheTest() {
        List<TestEnum> result = EnumUtils.getByList(TestEnum.class, TestEnum::getValue, "1", true);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(TestEnum.ONE, result.get(0));

        result = EnumUtils.getByList(TestEnum.class, TestEnum::getValue, "999", true);
        Assert.assertTrue(result.isEmpty());
    }

    /**
     * 测试getByPredicateList方法
     */
    @Test
    public void getByPredicateListTest() {
        List<TestEnum> result = EnumUtils.getByPredicateList(TestEnum.class, e -> e.getValue().startsWith("1"));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(TestEnum.ONE, result.get(0));

        result = EnumUtils.getByPredicateList(TestEnum.class, e -> e.getDesc().startsWith("第"));
        Assert.assertEquals(3, result.size());
    }

    /**
     * 测试exists方法 - 带类参数
     */
    @Test
    public void existsWithClassTest() {
        boolean exists = EnumUtils.exists(TestEnum.class, TestEnum::getValue, "2");
        Assert.assertTrue(exists);

        exists = EnumUtils.exists(TestEnum.class, TestEnum::getValue, "999");
        Assert.assertFalse(exists);
    }

    /**
     * 测试exists方法 - 不使用缓存
     */
    @Test
    public void existsWithoutCacheTest() {
        boolean exists = EnumUtils.exists(TestEnum.class, TestEnum::getValue, "2", false);
        Assert.assertTrue(exists);

        exists = EnumUtils.exists(TestEnum.class, TestEnum::getValue, "999", false);
        Assert.assertFalse(exists);
    }

    /**
     * 测试exists方法 - 使用缓存
     */
    @Test
    public void existsWithCacheTest() {
        boolean exists = EnumUtils.exists(TestEnum.class, TestEnum::getValue, "2", true);
        Assert.assertTrue(exists);

        exists = EnumUtils.exists(TestEnum.class, TestEnum::getValue, "999", true);
        Assert.assertFalse(exists);
    }

    /**
     * 测试existsPredicate方法
     */
    @Test
    public void existsPredicateTest() {
        boolean exists = EnumUtils.existsPredicate(TestEnum.class, e -> e.getDesc().equals("第二个"));
        Assert.assertTrue(exists);

        exists = EnumUtils.existsPredicate(TestEnum.class, e -> e.getDesc().equals("不存在的描述"));
        Assert.assertFalse(exists);
    }

    /**
     * 测试getFirst方法
     */
    @Test
    public void getFirstTest() {
        TestEnum result = EnumUtils.getFirst(TestEnum.class);
        Assert.assertEquals(TestEnum.ONE, result);
    }

    /**
     * 测试getLast方法
     */
    @Test
    public void getLastTest() {
        TestEnum result = EnumUtils.getLast(TestEnum.class);
        Assert.assertEquals(TestEnum.THREE, result);
    }

    /**
     * 测试getCount方法
     */
    @Test
    public void getCountTest() {
        int count = EnumUtils.getCount(TestEnum.class);
        Assert.assertEquals(3, count);
    }

    /**
     * 测试isEmpty方法
     */
    @Test
    public void isEmptyTest() {
        boolean isEmpty = EnumUtils.isEmpty(TestEnum.class);
        Assert.assertFalse(isEmpty);
    }

    /**
     * 测试isEnum方法
     */
    @Test
    public void isEnumTest() {
        Assert.assertTrue(EnumUtils.isEnum(TestEnum.class));
        Assert.assertFalse(EnumUtils.isEnum(String.class));
        Assert.assertFalse(EnumUtils.isEnum(null));
    }

    /**
     * 测试getValues方法
     */
    @Test
    public void getValuesTest() {
        TestEnum[] values = EnumUtils.getValues(TestEnum.class);
        Assert.assertEquals(3, values.length);
        Assert.assertEquals(TestEnum.ONE, values[0]);
    }

    /**
     * 测试getNames方法
     */
    @Test
    public void getNamesTest() {
        String[] names = EnumUtils.getNames(TestEnum.class);
        Assert.assertEquals(3, names.length);
        Assert.assertEquals("ONE", names[0]);
        Assert.assertEquals("TWO", names[1]);
        Assert.assertEquals("THREE", names[2]);
    }

    /**
     * 测试getOrdinals方法
     */
    @Test
    public void getOrdinalsTest() {
        int[] ordinals = EnumUtils.getOrdinals(TestEnum.class);
        Assert.assertEquals(3, ordinals.length);
        Assert.assertEquals(0, ordinals[0]);
        Assert.assertEquals(1, ordinals[1]);
        Assert.assertEquals(2, ordinals[2]);
    }

    /**
     * 测试getByOrdinal方法
     */
    @Test
    public void getByOrdinalTest() {
        TestEnum result = EnumUtils.getByOrdinal(TestEnum.class, 0);
        Assert.assertEquals(TestEnum.ONE, result);

        result = EnumUtils.getByOrdinal(TestEnum.class, 1);
        Assert.assertEquals(TestEnum.TWO, result);

        result = EnumUtils.getByOrdinal(TestEnum.class, 999);
        Assert.assertNull(result);
    }

    /**
     * 测试getByName方法
     */
    @Test
    public void getByNameTest() {
        TestEnum result = EnumUtils.getByName(TestEnum.class, "ONE");
        Assert.assertEquals(TestEnum.ONE, result);

        result = EnumUtils.getByName(TestEnum.class, "TWO");
        Assert.assertEquals(TestEnum.TWO, result);

        result = EnumUtils.getByName(TestEnum.class, "不存在的名称");
        Assert.assertNull(result);
    }

    /**
     * 测试list方法
     */
    @Test
    public void listTest() {
        List<TestEnum> list = EnumUtils.list(TestEnum.class);
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains(TestEnum.ONE));
        Assert.assertTrue(list.contains(TestEnum.TWO));
        Assert.assertTrue(list.contains(TestEnum.THREE));
    }

    /**
     * 测试valueList方法
     */
    @Test
    public void valueListTest() {
        List<String> list = EnumUtils.valueList(TestEnum.class, TestEnum::getValue);
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains("1"));
        Assert.assertTrue(list.contains("2"));
        Assert.assertTrue(list.contains("3"));
    }

    /**
     * 测试nameList方法
     */
    @Test
    public void nameListTest() {
        List<String> list = EnumUtils.nameList(TestEnum.class);
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains("ONE"));
        Assert.assertTrue(list.contains("TWO"));
        Assert.assertTrue(list.contains("THREE"));
    }

    /**
     * 测试ordinalList方法
     */
    @Test
    public void ordinalListTest() {
        List<Integer> list = EnumUtils.ordinalList(TestEnum.class);
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains(0));
        Assert.assertTrue(list.contains(1));
        Assert.assertTrue(list.contains(2));
    }

    /**
     * 测试缓存性能 - 多次查询同一值
     */
    @Test
    public void cachePerformanceTest() {
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            TestEnum result = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, true);
            Assert.assertEquals(TestEnum.ONE, result);
        }
        long endTime = System.nanoTime();
        long cachedTime = endTime - startTime;

        startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            TestEnum result = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, false);
            Assert.assertEquals(TestEnum.ONE, result);
        }
        endTime = System.nanoTime();
        long uncachedTime = endTime - startTime;

        Assert.assertTrue("缓存查询应该更快", cachedTime < uncachedTime);
    }

    /**
     * 测试缓存一致性 - 清除缓存后重新查询
     */
    @Test
    public void cacheConsistencyTest() {
        TestEnum result1 = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, true);
        Assert.assertEquals(TestEnum.ONE, result1);

        EnumUtils.clearCache(TestEnum.class, TestEnum::getValue);

        TestEnum result2 = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, true);
        Assert.assertEquals(TestEnum.ONE, result2);

        Assert.assertEquals(result1, result2);
    }

    /**
     * 测试边界情况 - 空枚举类
     */
    @Test
    public void emptyEnumTest() {
        List<EmptyEnum> list = EnumUtils.getEnumList(EmptyEnum.class);
        Assert.assertTrue(list.isEmpty());

        int count = EnumUtils.getCount(EmptyEnum.class);
        Assert.assertEquals(0, count);

        boolean isEmpty = EnumUtils.isEmpty(EmptyEnum.class);
        Assert.assertTrue(isEmpty);
    }

    /**
     * 测试边界情况 - 单个枚举值
     */
    @Test
    public void singleEnumTest() {
        SingleEnum result = EnumUtils.getEnumByValue(SingleEnum.class, "only");
        Assert.assertEquals(SingleEnum.ONLY, result);

        result = EnumUtils.getEnumByOrdinal(SingleEnum.class, 0);
        Assert.assertEquals(SingleEnum.ONLY, result);

        result = EnumUtils.getFirst(SingleEnum.class);
        Assert.assertEquals(SingleEnum.ONLY, result);

        result = EnumUtils.getLast(SingleEnum.class);
        Assert.assertEquals(SingleEnum.ONLY, result);
    }

    /**
     * 测试isEnum方法
     */
    @Test
    public void testIsEnum() {
        Assert.assertTrue(EnumUtils.isEnum(TestEnum.class));
        Assert.assertTrue(EnumUtils.isEnum(Name.class));
        Assert.assertTrue(EnumUtils.isEnum(EmptyEnum.class));
        Assert.assertTrue(EnumUtils.isEnum(SingleEnum.class));
        Assert.assertFalse(EnumUtils.isEnum(String.class));
        Assert.assertFalse(EnumUtils.isEnum(null));
        Assert.assertFalse(EnumUtils.isEnum(Integer.class));
    }

    /**
     * 测试getValues方法
     */
    @Test
    public void testGetValues() {
        TestEnum[] values = EnumUtils.getValues(TestEnum.class);
        Assert.assertNotNull(values);
        Assert.assertEquals(3, values.length);
        Assert.assertEquals(TestEnum.ONE, values[0]);
        Assert.assertEquals(TestEnum.TWO, values[1]);
        Assert.assertEquals(TestEnum.THREE, values[2]);

        // 测试空枚举
        EmptyEnum[] emptyValues = EnumUtils.getValues(EmptyEnum.class);
        Assert.assertNotNull(emptyValues);
        Assert.assertEquals(0, emptyValues.length);

        // 测试null
        Assert.assertNull(EnumUtils.getValues(null));
    }

    /**
     * 测试getNames方法
     */
    @Test
    public void testGetNames() {
        String[] names = EnumUtils.getNames(TestEnum.class);
        Assert.assertNotNull(names);
        Assert.assertEquals(3, names.length);
        Assert.assertEquals("ONE", names[0]);
        Assert.assertEquals("TWO", names[1]);
        Assert.assertEquals("THREE", names[2]);

        // 测试空枚举
        String[] emptyNames = EnumUtils.getNames(EmptyEnum.class);
        Assert.assertNotNull(emptyNames);
        Assert.assertEquals(0, emptyNames.length);

        // 测试null
        Assert.assertNull(EnumUtils.getNames(null));
    }

    /**
     * 测试getOrdinals方法
     */
    @Test
    public void testGetOrdinals() {
        int[] ordinals = EnumUtils.getOrdinals(TestEnum.class);
        Assert.assertNotNull(ordinals);
        Assert.assertEquals(3, ordinals.length);
        Assert.assertEquals(0, ordinals[0]);
        Assert.assertEquals(1, ordinals[1]);
        Assert.assertEquals(2, ordinals[2]);

        // 测试空枚举
        int[] emptyOrdinals = EnumUtils.getOrdinals(EmptyEnum.class);
        Assert.assertNotNull(emptyOrdinals);
        Assert.assertEquals(0, emptyOrdinals.length);

        // 测试null
        Assert.assertNull(EnumUtils.getOrdinals(null));
    }

    /**
     * 测试getByOrdinal方法（等同于getEnumByOrdinal）
     */
    @Test
    public void testGetByOrdinal() {
        TestEnum result = EnumUtils.getByOrdinal(TestEnum.class, 0);
        Assert.assertEquals(TestEnum.ONE, result);

        result = EnumUtils.getByOrdinal(TestEnum.class, 1);
        Assert.assertEquals(TestEnum.TWO, result);

        result = EnumUtils.getByOrdinal(TestEnum.class, 2);
        Assert.assertEquals(TestEnum.THREE, result);

        result = EnumUtils.getByOrdinal(TestEnum.class, -1);
        Assert.assertNull(result);

        result = EnumUtils.getByOrdinal(TestEnum.class, 999);
        Assert.assertNull(result);
    }

    /**
     * 测试getByName方法（等同于getEnumByName）
     */
    @Test
    public void testGetByName() {
        TestEnum result = EnumUtils.getByName(TestEnum.class, "ONE");
        Assert.assertEquals(TestEnum.ONE, result);

        result = EnumUtils.getByName(TestEnum.class, "TWO");
        Assert.assertEquals(TestEnum.TWO, result);

        result = EnumUtils.getByName(TestEnum.class, "THREE");
        Assert.assertEquals(TestEnum.THREE, result);

        result = EnumUtils.getByName(TestEnum.class, "不存在的名称");
        Assert.assertNull(result);

        result = EnumUtils.getByName(TestEnum.class, null);
        Assert.assertNull(result);

        result = EnumUtils.getByName(TestEnum.class, "");
        Assert.assertNull(result);
    }

    /**
     * 测试list方法（等同于getEnumList）
     */
    @Test
    public void testList() {
        List<TestEnum> list = EnumUtils.list(TestEnum.class);
        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains(TestEnum.ONE));
        Assert.assertTrue(list.contains(TestEnum.TWO));
        Assert.assertTrue(list.contains(TestEnum.THREE));

        // 测试空枚举
        List<EmptyEnum> emptyList = EnumUtils.list(EmptyEnum.class);
        Assert.assertNotNull(emptyList);
        Assert.assertTrue(emptyList.isEmpty());
    }

    /**
     * 测试valueList方法（等同于getValueList）
     */
    @Test
    public void testValueList() {
        List<String> list = EnumUtils.valueList(TestEnum.class, TestEnum::getValue);
        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains("1"));
        Assert.assertTrue(list.contains("2"));
        Assert.assertTrue(list.contains("3"));

        List<String> descList = EnumUtils.valueList(TestEnum.class, TestEnum::getDesc);
        Assert.assertNotNull(descList);
        Assert.assertEquals(3, descList.size());
        Assert.assertTrue(descList.contains("第一个"));
        Assert.assertTrue(descList.contains("第二个"));
        Assert.assertTrue(descList.contains("第三个"));
    }

    /**
     * 测试nameList方法（等同于getNameList）
     */
    @Test
    public void testNameList() {
        List<String> list = EnumUtils.nameList(TestEnum.class);
        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains("ONE"));
        Assert.assertTrue(list.contains("TWO"));
        Assert.assertTrue(list.contains("THREE"));
    }

    /**
     * 测试ordinalList方法（等同于getOrdinalList）
     */
    @Test
    public void testOrdinalList() {
        List<Integer> list = EnumUtils.ordinalList(TestEnum.class);
        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains(0));
        Assert.assertTrue(list.contains(1));
        Assert.assertTrue(list.contains(2));
    }

    /**
     * 测试边界情况 - 空枚举的各种方法
     */
    @Test
    public void testEmptyEnumMethods() {
        // 测试getFirst
        Assert.assertNull(EnumUtils.getFirst(EmptyEnum.class));

        // 测试getLast
        Assert.assertNull(EnumUtils.getLast(EmptyEnum.class));

        // 测试getCount
        Assert.assertEquals(0, EnumUtils.getCount(EmptyEnum.class));

        // 测试isEmpty
        Assert.assertTrue(EnumUtils.isEmpty(EmptyEnum.class));

        // 测试getValueList
        List<Object> valueList = EnumUtils.getValueList(EmptyEnum.class, e -> e);
        Assert.assertNotNull(valueList);
        Assert.assertTrue(valueList.isEmpty());

        // 测试getNameList
        List<String> nameList = EnumUtils.getNameList(EmptyEnum.class);
        Assert.assertNotNull(nameList);
        Assert.assertTrue(nameList.isEmpty());

        // 测试getOrdinalList
        List<Integer> ordinalList = EnumUtils.getOrdinalList(EmptyEnum.class);
        Assert.assertNotNull(ordinalList);
        Assert.assertTrue(ordinalList.isEmpty());
    }

    /**
     * 测试边界情况 - 单枚举值的各种方法
     */
    @Test
    public void testSingleEnumMethods() {
        // 测试getFirst
        Assert.assertEquals(SingleEnum.ONLY, EnumUtils.getFirst(SingleEnum.class));

        // 测试getLast
        Assert.assertEquals(SingleEnum.ONLY, EnumUtils.getLast(SingleEnum.class));

        // 测试getCount
        Assert.assertEquals(1, EnumUtils.getCount(SingleEnum.class));

        // 测试isEmpty
        Assert.assertFalse(EnumUtils.isEmpty(SingleEnum.class));

        // 测试getValueList
        List<String> valueList = EnumUtils.getValueList(SingleEnum.class, SingleEnum::getValue);
        Assert.assertNotNull(valueList);
        Assert.assertEquals(1, valueList.size());
        Assert.assertTrue(valueList.contains("only"));

        // 测试getNameList
        List<String> nameList = EnumUtils.getNameList(SingleEnum.class);
        Assert.assertNotNull(nameList);
        Assert.assertEquals(1, nameList.size());
        Assert.assertTrue(nameList.contains("ONLY"));

        // 测试getOrdinalList
        List<Integer> ordinalList = EnumUtils.getOrdinalList(SingleEnum.class);
        Assert.assertNotNull(ordinalList);
        Assert.assertEquals(1, ordinalList.size());
        Assert.assertTrue(ordinalList.contains(0));
    }

    /**
     * 测试缓存清除功能的完整性
     */
    @Test
    public void testCacheClear() {
        // 先构建缓存
        TestEnum result1 = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, true);
        Assert.assertEquals(TestEnum.ONE, result1);

        TestEnum result2 = EnumUtils.getEnum(TestEnum.class, "2", TestEnum::getValue, true);
        Assert.assertEquals(TestEnum.TWO, result2);

        // 清除指定缓存
        EnumUtils.clearCache(TestEnum.class, TestEnum::getValue);

        // 重新获取，应该重新构建缓存
        TestEnum result3 = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, true);
        Assert.assertEquals(TestEnum.ONE, result3);

        // 构建多个缓存
        TestEnum result4 = EnumUtils.getEnum(TestEnum.class, "ONE", Enum::name, true);
        Assert.assertEquals(TestEnum.ONE, result4);

        // 清除所有缓存
        EnumUtils.clearAllCache(TestEnum.class);

        // 重新获取，应该重新构建缓存
        TestEnum result5 = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, true);
        Assert.assertEquals(TestEnum.ONE, result5);

        // 构建缓存
        TestEnum result6 = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, true);
        Assert.assertEquals(TestEnum.ONE, result6);

        // 清除所有枚举缓存
        EnumUtils.clearAllCache();

        // 重新获取，应该重新构建缓存
        TestEnum result7 = EnumUtils.getEnum(TestEnum.class, "1", TestEnum::getValue, true);
        Assert.assertEquals(TestEnum.ONE, result7);
    }

    /**
     * 测试getByPredicate方法的各种场景
     */
    @Test
    public void testgetByPredicate() {
        // 测试精确匹配
        TestEnum result = EnumUtils.getByPredicate(TestEnum.class, e -> e.getDesc().equals("第二个"));
        Assert.assertEquals(TestEnum.TWO, result);

        // 测试模糊匹配
        result = EnumUtils.getByPredicate(TestEnum.class, e -> e.getDesc().contains("个"));
        Assert.assertEquals(TestEnum.ONE, result); // 应该返回第一个匹配的

        // 测试不匹配
        result = EnumUtils.getByPredicate(TestEnum.class, e -> e.getDesc().equals("不存在的描述"));
        Assert.assertNull(result);

        // 测试空枚举
        EmptyEnum emptyResult = EnumUtils.getByPredicate(EmptyEnum.class, e -> true);
        Assert.assertNull(emptyResult);
    }

    /**
     * 测试getByPredicateList方法的各种场景
     */
    @Test
    public void testgetByPredicateList() {
        // 测试精确匹配
        List<TestEnum> result = EnumUtils.getByPredicateList(TestEnum.class, e -> e.getDesc().equals("第二个"));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(TestEnum.TWO, result.get(0));

        // 测试模糊匹配
        result = EnumUtils.getByPredicateList(TestEnum.class, e -> e.getDesc().contains("个"));
        Assert.assertEquals(3, result.size()); // 所有枚举都包含"个"

        // 测试不匹配
        result = EnumUtils.getByPredicateList(TestEnum.class, e -> e.getDesc().equals("不存在的描述"));
        Assert.assertTrue(result.isEmpty());

        // 测试空枚举
        List<EmptyEnum> emptyResult = EnumUtils.getByPredicateList(EmptyEnum.class, e -> true);
        Assert.assertTrue(emptyResult.isEmpty());
    }

    /**
     * 测试existsPredicate方法
     */
    @Test
    public void testexistsPredicate() {
        // 测试存在
        boolean exists = EnumUtils.existsPredicate(TestEnum.class, e -> e.getDesc().equals("第二个"));
        Assert.assertTrue(exists);

        // 测试不存在
        exists = EnumUtils.existsPredicate(TestEnum.class, e -> e.getDesc().equals("不存在的描述"));
        Assert.assertFalse(exists);

        // 测试空枚举
        exists = EnumUtils.existsPredicate(EmptyEnum.class, e -> true);
        Assert.assertFalse(exists);
    }
}