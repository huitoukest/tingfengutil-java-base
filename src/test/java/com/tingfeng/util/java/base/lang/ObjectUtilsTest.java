package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.bean.User;
import com.tingfeng.util.java.base.common.constant.ObjectType;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * ObjectUtils单元测试
 */
public class ObjectUtilsTest {

    // ==================== getObjectType 系列测试 ====================

    @Test
    public void getObjectTypeByClass() {
        Assert.assertEquals(ObjectType.Boolean, ObjectUtils.getObjectType(Boolean.class));
        Assert.assertEquals(ObjectType.Integer, ObjectUtils.getObjectType(Integer.class));
        Assert.assertEquals(ObjectType.Long, ObjectUtils.getObjectType(Long.class));
        Assert.assertEquals(ObjectType.Double, ObjectUtils.getObjectType(Double.class));
        Assert.assertEquals(ObjectType.Float, ObjectUtils.getObjectType(Float.class));
        Assert.assertEquals(ObjectType.String, ObjectUtils.getObjectType(String.class));
        Assert.assertEquals(ObjectType.Short, ObjectUtils.getObjectType(Short.class));
        Assert.assertEquals(ObjectType.Byte, ObjectUtils.getObjectType(Byte.class));
        Assert.assertEquals(ObjectType.Date, ObjectUtils.getObjectType(Date.class));
        Assert.assertEquals(ObjectType.Other, ObjectUtils.getObjectType(Object.class));
        Assert.assertEquals(ObjectType.Other, ObjectUtils.getObjectType(List.class));
    }

    @Test
    public void getObjectTypeByClassName() {
        Assert.assertEquals(ObjectType.Boolean, ObjectUtils.getObjectType("java.lang.Boolean"));
        Assert.assertEquals(ObjectType.Boolean, ObjectUtils.getObjectType("boolean"));
        Assert.assertEquals(ObjectType.Integer, ObjectUtils.getObjectType("java.lang.Integer"));
        Assert.assertEquals(ObjectType.Integer, ObjectUtils.getObjectType("int"));
        Assert.assertEquals(ObjectType.String, ObjectUtils.getObjectType("java.lang.String"));
        Assert.assertEquals(ObjectType.Other, ObjectUtils.getObjectType("java.util.HashMap"));
    }

    @Test
    public void getObjectTypeByField() throws NoSuchFieldException {
        Field intField = User.class.getDeclaredField("age");
        Field stringField = User.class.getDeclaredField("userName");
        Field mapField = User.class.getDeclaredField("map");

        Assert.assertEquals(ObjectType.Integer, ObjectUtils.getObjectType(intField));
        Assert.assertEquals(ObjectType.String, ObjectUtils.getObjectType(stringField));
        Assert.assertEquals(ObjectType.Other, ObjectUtils.getObjectType(mapField));
    }

    // ==================== isBaseTypeObject 系列测试 ====================

    @Test
    public void isBaseTypeObjectByClassName() {
        Assert.assertTrue(ObjectUtils.isBaseTypeObject("java.lang.Integer"));
        Assert.assertTrue(ObjectUtils.isBaseTypeObject("int"));
        Assert.assertTrue(ObjectUtils.isBaseTypeObject("java.lang.String"));
        Assert.assertTrue(ObjectUtils.isBaseTypeObject("java.util.Date"));
        Assert.assertFalse(ObjectUtils.isBaseTypeObject("java.util.List"));
        Assert.assertFalse(ObjectUtils.isBaseTypeObject("com.example.CustomClass"));
    }

    @Test
    public void isBaseTypeObjectByClass() {
        Assert.assertTrue(ObjectUtils.isBaseTypeObject(Integer.class));
        Assert.assertTrue(ObjectUtils.isBaseTypeObject(int.class));
        Assert.assertTrue(ObjectUtils.isBaseTypeObject(String.class));
        Assert.assertFalse(ObjectUtils.isBaseTypeObject(List.class));
        Assert.assertFalse(ObjectUtils.isBaseTypeObject(Object.class));
    }

    @Test
    public void isBaseTypeObjectByField() throws NoSuchFieldException {
        Field intField = User.class.getDeclaredField("age");
        Field listField = User.class.getDeclaredField("homeNames");

        Assert.assertTrue(ObjectUtils.isBaseTypeObject(intField));
        Assert.assertFalse(ObjectUtils.isBaseTypeObject(listField));
    }

    // ==================== isBoolean 测试 ====================

    @Test
    public void isBooleanTest() {
        Assert.assertTrue(ObjectUtils.isBoolean(Boolean.class));
        Assert.assertTrue(ObjectUtils.isBoolean(boolean.class));
        Assert.assertFalse(ObjectUtils.isBoolean(Integer.class));
        Assert.assertFalse(ObjectUtils.isBoolean(String.class));
        Assert.assertFalse(ObjectUtils.isBoolean(null));
    }

    // ==================== getObject 测试 ====================

    @Test
    public void getObjectTest() {
        Assert.assertEquals(Integer.valueOf(123), ObjectUtils.getObject(Integer.class, "123"));
        Assert.assertEquals(Long.valueOf(123), ObjectUtils.getObject(Long.class, "123"));
        Assert.assertEquals("abc", ObjectUtils.getObject(String.class, "abc"));
        Assert.assertEquals(Double.valueOf(1.5), ObjectUtils.getObject(Double.class, "1.5"));
        Assert.assertEquals(Float.valueOf(1.5f), ObjectUtils.getObject(Float.class, "1.5"));
        Assert.assertEquals(Byte.valueOf((byte) 1), ObjectUtils.getObject(Byte.class, "1"));
        Assert.assertEquals(Short.valueOf((short) 1), ObjectUtils.getObject(Short.class, "1"));
    }

    @Test
    public void getObjectWithSameType() {
        Integer original = 100;
        Assert.assertSame(original, ObjectUtils.getObject(Integer.class, original));
    }

    @Test
    public void getObjectWithNull() {
        Assert.assertNull(ObjectUtils.getObject(Integer.class, null));
        Assert.assertEquals("default", ObjectUtils.getObject(String.class, "default"));
    }

    // ==================== getObjectByXml 测试 ====================

    @Test
    public void getObjectByXmlTest() throws UnsupportedEncodingException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><java version=\"1.8\" class=\"java.beans.XMLDecoder\"><object class=\"java.lang.String\"><string>test</string></object></java>";
        Object result = ObjectUtils.getObjectByXml(xml);
        Assert.assertEquals("test", result);
    }

    @Test(expected = Exception.class)
    public void getObjectByXmlInvalidTest() throws UnsupportedEncodingException {
        ObjectUtils.getObjectByXml("invalid xml");
    }

    // ==================== isNull / isNotNull 系列测试 ====================

    @Test
    public void isNullTest() {
        Assert.assertTrue(ObjectUtils.isNull(null));
        Assert.assertFalse(ObjectUtils.isNull(""));
        Assert.assertFalse(ObjectUtils.isNull(new Object()));
    }

    @Test
    public void isNotNullTest() {
        Assert.assertFalse(ObjectUtils.isNotNull(null));
        Assert.assertTrue(ObjectUtils.isNotNull(""));
        Assert.assertTrue(ObjectUtils.isNotNull(new Object()));
    }

    @Test
    public void isAnyNullTest() {
        Assert.assertTrue(ObjectUtils.isAnyNull(null, "a", "b"));
        Assert.assertTrue(ObjectUtils.isAnyNull("a", null, "b"));
        Assert.assertFalse(ObjectUtils.isAnyNull("a", "b", "c"));
        Assert.assertTrue(ObjectUtils.isAnyNull((Object[]) null));
        Assert.assertFalse(ObjectUtils.isAnyNull());
    }

    @Test
    public void isAllNullTest() {
        Assert.assertTrue(ObjectUtils.isAllNull(null, null, null));
        Assert.assertFalse(ObjectUtils.isAllNull(null, "a", null));
        Assert.assertFalse(ObjectUtils.isAllNull("a", "b", "c"));
        Assert.assertTrue(ObjectUtils.isAllNull());
    }

    @Test
    public void isAnyNotNullTest() {
        Assert.assertTrue(ObjectUtils.isAnyNotNull(null, "a", null));
        Assert.assertTrue(ObjectUtils.isAnyNotNull("a", "b", "c"));
        Assert.assertFalse(ObjectUtils.isAnyNotNull(null, null, null));
        Assert.assertFalse(ObjectUtils.isAnyNotNull());
    }

    @Test
    public void isAllNotNullTest() {
        Assert.assertTrue(ObjectUtils.isAllNotNull("a", "b", "c"));
        Assert.assertFalse(ObjectUtils.isAllNotNull("a", null, "c"));
        Assert.assertFalse(ObjectUtils.isAllNotNull(null, null, null));
    }

    // ==================== isEmpty 系列测试 ====================

    @Test
    public void isEmptyWithNull() {
        Assert.assertTrue(ObjectUtils.isEmpty(null));
        Assert.assertTrue(ObjectUtils.isEmpty(null, true));
        Assert.assertTrue(ObjectUtils.isEmpty(null, true, true));
    }

    @Test
    public void isEmptyWithString() {
        Assert.assertTrue(ObjectUtils.isEmpty(""));
        Assert.assertTrue(ObjectUtils.isEmpty("", true, true));
        Assert.assertTrue(ObjectUtils.isEmpty("   ", true, true));
        Assert.assertFalse(ObjectUtils.isEmpty("   ", false, false));
        Assert.assertFalse(ObjectUtils.isEmpty("abc"));
    }

    @Test
    public void isEmptyWithCollection() {
        Assert.assertTrue(ObjectUtils.isEmpty(Collections.emptyList()));
        Assert.assertFalse(ObjectUtils.isEmpty(Arrays.asList("a", "b")));
    }

    @Test
    public void isEmptyWithMap() {
        Assert.assertTrue(ObjectUtils.isEmpty(Collections.emptyMap()));
        Assert.assertFalse(ObjectUtils.isEmpty(Collections.singletonMap("key", "value")));
    }

    @Test
    public void isEmptyWithArray() {
        Assert.assertTrue(ObjectUtils.isEmpty(new int[]{}));
        Assert.assertTrue(ObjectUtils.isEmpty(new String[]{}));

        int[][] nestedEmpty = new int[][]{{}};
        Assert.assertTrue(ObjectUtils.isEmpty(nestedEmpty, true, false));
    }

    @Test
    public void isEmptyWithRecursive() {
        List<Object> nested = Arrays.asList("", Collections.emptyList());
        Assert.assertTrue(ObjectUtils.isEmpty(nested, true, true));
        Assert.assertFalse(ObjectUtils.isEmpty(nested, false, true));
    }

    @Test
    public void isEmptyWithOptional() {
        Assert.assertTrue(ObjectUtils.isEmpty(Optional.empty()));
        Assert.assertFalse(ObjectUtils.isEmpty(Optional.of("value")));
    }

    @Test
    public void isAllEmptyTest() {
        Assert.assertTrue(ObjectUtils.isAllEmpty(false, null, new String[]{}));
        Assert.assertTrue(ObjectUtils.isAllEmpty(true, null, new String[]{}, "   "));
        Assert.assertFalse(ObjectUtils.isAllEmpty(false, "a", new String[]{}));
    }

    @Test
    public void isAnyEmptyTest() {
        Assert.assertTrue(ObjectUtils.isAnyEmpty(null, "a", new String[]{}));
        Assert.assertFalse(ObjectUtils.isAnyEmpty("a", "b", "c"));
    }

    @Test
    public void isNotEmptyTest() {
        Assert.assertFalse(ObjectUtils.isNotEmpty(null));
        Assert.assertFalse(ObjectUtils.isNotEmpty(""));
        Assert.assertFalse(ObjectUtils.isNotEmpty(Collections.emptyList()));
        Assert.assertTrue(ObjectUtils.isNotEmpty("abc"));
        Assert.assertTrue(ObjectUtils.isNotEmpty(Arrays.asList("a")));
    }

    // ==================== equals(String, String) 测试 ====================

    @Test
    public void equalsStringTest() {
        Assert.assertTrue(ObjectUtils.equals(null, null));
        Assert.assertTrue(ObjectUtils.equals("abc", "abc"));
        Assert.assertFalse(ObjectUtils.equals("abc", "def"));
        Assert.assertFalse(ObjectUtils.equals(null, "abc"));
        Assert.assertFalse(ObjectUtils.equals("abc", null));
    }

    // ==================== equals(Object, Object) 测试 ====================

    @Test
    public void equalsObjectTest() {
        Assert.assertTrue(ObjectUtils.equals(null, null));
        Assert.assertTrue(ObjectUtils.equals("abc", "abc"));
        Assert.assertFalse(ObjectUtils.equals("abc", "def"));
        Assert.assertFalse(ObjectUtils.equals(null, "abc"));
    }

    // ==================== deepEquals 测试 ====================

    @Test
    public void deepEqualsTest() {
        Assert.assertTrue(ObjectUtils.deepEquals(null, null));
        Assert.assertTrue(ObjectUtils.deepEquals("abc", "abc"));
        Assert.assertTrue(ObjectUtils.deepEquals(new int[]{1, 2}, new int[]{1, 2}));
        Assert.assertFalse(ObjectUtils.deepEquals(new int[]{1}, new int[]{1, 2}));
        Assert.assertFalse(ObjectUtils.deepEquals("abc", "def"));
    }

    // ==================== getValue 系列测试 ====================

    @Test
    public void getValueWithVarargs() {
        Assert.assertEquals("default", ObjectUtils.getValue("default"));
        Assert.assertEquals("first", ObjectUtils.getValue("default", "first", "second"));
        Assert.assertEquals("second", ObjectUtils.getValue("default", null, "second"));
        Assert.assertEquals("default", ObjectUtils.getValue("default", null, null));
    }

    @Test
    public void getValueWithSourceAndConvert() {
        Assert.assertEquals(Integer.valueOf(100), ObjectUtils.getValue(0, "100", Integer::parseInt));
        Assert.assertEquals(Integer.valueOf(0), ObjectUtils.getValue(0, ((String) null), Integer::parseInt));
        Assert.assertEquals(Integer.valueOf(0), ObjectUtils.getValue(0, "abc", Integer::parseInt));
    }

    @Test
    public void getValueWithCallable() {
        Assert.assertEquals("value", ObjectUtils.getValue("default", () -> "value"));
        Assert.assertEquals("default", ObjectUtils.getValue("default", () -> {
            throw new NullPointerException();
        }));
    }

    // ==================== clone / cloneArray 测试 ====================

    @Test
    public void cloneArrayTest() {
        String[] src = {"a", "b", "c"};
        String[] dest = new String[3];
        ObjectUtils.cloneArray(src, dest);

        Assert.assertArrayEquals(src, dest);
        Assert.assertNotSame(src, dest);
    }

    @Test
    public void cloneArrayWithNull() {
        String[] dest = new String[3];
        ObjectUtils.cloneArray(null, dest);
        Assert.assertNull(dest[0]);
    }

    @Test
    public void cloneTest() {
        List<String> original = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> cloned = ObjectUtils.clone(original);

        Assert.assertEquals(original, cloned);
        Assert.assertNotSame(original, cloned);
    }

    // ==================== isBaseJavaType 测试 ====================

    @Test
    public void isBaseJavaTypeTest() {
        Assert.assertFalse(ObjectUtils.isBaseJavaType(null));
        Assert.assertTrue(ObjectUtils.isBaseJavaType(1));
        Assert.assertTrue(ObjectUtils.isBaseJavaType(1L));
        Assert.assertTrue(ObjectUtils.isBaseJavaType(1.0));
        Assert.assertTrue(ObjectUtils.isBaseJavaType(1.0f));
        Assert.assertTrue(ObjectUtils.isBaseJavaType(true));
        Assert.assertTrue(ObjectUtils.isBaseJavaType((byte) 1));
        Assert.assertTrue(ObjectUtils.isBaseJavaType((short) 1));
        Assert.assertTrue(ObjectUtils.isBaseJavaType("string"));
        Assert.assertTrue(ObjectUtils.isBaseJavaType(new Date()));
        Assert.assertTrue(ObjectUtils.isBaseJavaType(new Integer[]{1, 2}));
        Assert.assertTrue(ObjectUtils.isBaseJavaType(Arrays.asList("a")));
        Assert.assertFalse(ObjectUtils.isBaseJavaType(new User()));
    }

    // ==================== isInteger / isFloat 测试 ====================

    @Test
    public void isIntegerTest() {
        Assert.assertTrue(ObjectUtils.isInteger((byte) 1));
        Assert.assertTrue(ObjectUtils.isInteger((short) 1));
        Assert.assertTrue(ObjectUtils.isInteger(1));
        Assert.assertTrue(ObjectUtils.isInteger(1L));
        Assert.assertFalse(ObjectUtils.isInteger(1.0));
        Assert.assertFalse(ObjectUtils.isInteger("1"));
        Assert.assertFalse(ObjectUtils.isInteger(null));
    }

    @Test
    public void isFloatTest() {
        Assert.assertTrue(ObjectUtils.isFloat(1.0));
        Assert.assertTrue(ObjectUtils.isFloat(1.0f));
        Assert.assertFalse(ObjectUtils.isFloat(1));
        Assert.assertFalse(ObjectUtils.isFloat("1.0"));
        Assert.assertFalse(ObjectUtils.isFloat(null));
    }

    // ==================== valueEquals 测试 ====================

    @Test
    public void valueEqualsIntegerTest() {
        Assert.assertTrue(ObjectUtils.valueEquals(1, 1L));
        Assert.assertTrue(ObjectUtils.valueEquals(2, 2L));
        Assert.assertFalse(ObjectUtils.valueEquals(1, 2L));
    }

    @Test
    public void valueEqualsFloatTest() {
        Assert.assertTrue(ObjectUtils.valueEquals(1.0f, 1.0f));
        Assert.assertTrue(ObjectUtils.valueEquals(2.0f, 2.0));
        Assert.assertFalse(ObjectUtils.valueEquals(1.0f, 2.0));
    }

    @Test
    public void valueEqualsStringTest() {
        Assert.assertTrue(ObjectUtils.valueEquals(2.0f, "2.0"));
        Assert.assertTrue(ObjectUtils.valueEquals(2.0, "2.0"));
    }

    @Test
    public void valueEqualsNullTest() {
        Assert.assertTrue(ObjectUtils.valueEquals(null, null));
        Assert.assertFalse(ObjectUtils.valueEquals(2.0, null));
        Assert.assertFalse(ObjectUtils.valueEquals(null, "2.0"));
    }

    // ==================== arrayEquals 测试 ====================

    @Test
    public void arrayEqualsTest() {
        Assert.assertTrue(ObjectUtils.arrayEquals(null, null));
        Assert.assertTrue(ObjectUtils.arrayEquals(new String[]{"a", "b"}, new String[]{"a", "b"}));
        Assert.assertFalse(ObjectUtils.arrayEquals(new String[]{"a"}, new String[]{"a", "b"}));
        Assert.assertFalse(ObjectUtils.arrayEquals(null, new String[]{"a"}));
    }

    @Test
    public void arrayEqualsWithValueEq() {
        Assert.assertTrue(ObjectUtils.arrayEquals(new Object[]{1, 2L}, new Object[]{1, 2L}, true));
        Assert.assertTrue(ObjectUtils.arrayEquals(new Object[]{1.0, 2.0}, new Object[]{1.0, 2.0}, true));
        Assert.assertFalse(ObjectUtils.arrayEquals(new Object[]{1, 2.0}, new Object[]{1, 3.0}, true));
    }

    @Test
    public void arrayEqualsWithObjectEquals() {
        Assert.assertTrue(ObjectUtils.arrayEquals(
                new Object[]{"a", "b"},
                new Object[]{"a", "b"}
        ));
    }
}