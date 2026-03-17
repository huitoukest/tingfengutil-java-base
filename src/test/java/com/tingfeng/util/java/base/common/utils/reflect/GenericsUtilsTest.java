package com.tingfeng.util.java.base.common.utils.reflect;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * 泛型工具类测试
 */
public class GenericsUtilsTest {

    /**
     * 测试泛型父类 - 基本功能
     */
    @Test
    public void testGetSuperClassGenericTypeBasic() {
        class TestClass extends GenericParent<String, Integer> {}
        
        Class<?> result = GenericsUtils.getSuperClassGenericType(TestClass.class, 0);
        Assert.assertEquals("第一个泛型参数应该是String", String.class, result);
        
        Class<?> result2 = GenericsUtils.getSuperClassGenericType(TestClass.class, 1);
        Assert.assertEquals("第二个泛型参数应该是Integer", Integer.class, result2);
    }

    /**
     * 测试泛型父类 - 默认索引
     */
    @Test
    public void testGetSuperClassGenericTypeDefaultIndex() {
        class TestClass extends GenericParent<String, Integer> {}
        
        Class<?> result = GenericsUtils.getSuperClassGenericType(TestClass.class);
        Assert.assertEquals("默认应该返回第一个泛型参数", String.class, result);
    }

    /**
     * 测试泛型父类 - 无泛型父类
     */
    @Test
    public void testGetSuperClassGenericTypeNoGeneric() {
        class TestClass extends NonGenericParent {}
        
        Class<?> result = GenericsUtils.getSuperClassGenericType(TestClass.class);
        Assert.assertEquals("无泛型父类应该返回Object.class", Object.class, result);
    }

    /**
     * 测试泛型父类 - Object父类
     */
    @Test
    public void testGetSuperClassGenericTypeObjectParent() {
        class TestClass {}
        
        Class<?> result = GenericsUtils.getSuperClassGenericType(TestClass.class);
        Assert.assertEquals("Object父类应该返回Object.class", Object.class, result);
    }

    /**
     * 测试泛型父类 - 负索引
     */
    @Test(expected = RuntimeException.class)
    public void testGetSuperClassGenericTypeNegativeIndex() {
        class TestClass extends GenericParent<String, Integer> {}
        
        GenericsUtils.getSuperClassGenericType(TestClass.class, -1);
    }

    /**
     * 测试泛型父类 - 超出范围的索引
     */
    @Test(expected = RuntimeException.class)
    public void testGetSuperClassGenericTypeOutOfRangeIndex() {
        class TestClass extends GenericParent<String, Integer> {}
        
        GenericsUtils.getSuperClassGenericType(TestClass.class, 2);
    }

    /**
     * 测试泛型父类 - 单个泛型参数
     */
    @Test
    public void testGetSuperClassGenericTypeSingleGeneric() {
        class TestClass extends SingleGenericParent<String> {}
        
        Class<?> result = GenericsUtils.getSuperClassGenericType(TestClass.class, 0);
        Assert.assertEquals("单个泛型参数应该是String", String.class, result);
    }

    /**
     * 测试泛型父类 - 多个泛型参数
     */
    @Test
    public void testGetSuperClassGenericTypeMultipleGenerics() {
        class TestClass extends MultipleGenericParent<String, Integer, Double, Boolean> {}
        
        Class<?> result1 = GenericsUtils.getSuperClassGenericType(TestClass.class, 0);
        Assert.assertEquals("第一个泛型参数应该是String", String.class, result1);
        
        Class<?> result2 = GenericsUtils.getSuperClassGenericType(TestClass.class, 1);
        Assert.assertEquals("第二个泛型参数应该是Integer", Integer.class, result2);
        
        Class<?> result3 = GenericsUtils.getSuperClassGenericType(TestClass.class, 2);
        Assert.assertEquals("第三个泛型参数应该是Double", Double.class, result3);
        
        Class<?> result4 = GenericsUtils.getSuperClassGenericType(TestClass.class, 3);
        Assert.assertEquals("第四个泛型参数应该是Boolean", Boolean.class, result4);
    }

    /**
     * 测试方法返回值泛型 - 基本功能
     */
    @Test
    public void testGetMethodGenericReturnTypeBasic() throws Exception {
        Method method = TestClass.class.getMethod("getMapMethod");
        
        Class<?> result = GenericsUtils.getMethodGenericReturnType(method, 0);
        Assert.assertEquals("方法返回值第一个泛型参数应该是String", String.class, result);
        
        Class<?> result2 = GenericsUtils.getMethodGenericReturnType(method, 1);
        Assert.assertEquals("方法返回值第二个泛型参数应该是Integer", Integer.class, result2);
    }

    /**
     * 测试方法返回值泛型 - 默认索引
     */
    @Test
    public void testGetMethodGenericReturnTypeDefaultIndex() throws Exception {
        Method method = TestClass.class.getMethod("getListMethod");
        
        Class<?> result = GenericsUtils.getMethodGenericReturnType(method);
        Assert.assertEquals("默认应该返回第一个泛型参数", String.class, result);
    }

    /**
     * 测试方法返回值泛型 - 无泛型返回值
     */
    @Test
    public void testGetMethodGenericReturnTypeNoGeneric() throws Exception {
        Method method = TestClass.class.getMethod("getNonGenericMethod");
        
        Class<?> result = GenericsUtils.getMethodGenericReturnType(method);
        Assert.assertEquals("无泛型返回值应该返回Object.class", Object.class, result);
    }

    /**
     * 测试方法返回值泛型 - 负索引
     */
    @Test(expected = RuntimeException.class)
    public void testGetMethodGenericReturnTypeNegativeIndex() throws Exception {
        Method method = TestClass.class.getMethod("getMapMethod");
        
        GenericsUtils.getMethodGenericReturnType(method, -1);
    }

    /**
     * 测试方法返回值泛型 - 超出范围的索引
     */
    @Test(expected = RuntimeException.class)
    public void testGetMethodGenericReturnTypeOutOfRangeIndex() throws Exception {
        Method method = TestClass.class.getMethod("getMapMethod");
        
        GenericsUtils.getMethodGenericReturnType(method, 2);
    }

    /**
     * 测试方法参数泛型 - 基本功能
     */
    @Test
    public void testGetMethodGenericParameterTypesBasic() throws Exception {
        Method method = TestClass.class.getMethod("mapParameterMethod", Map.class, List.class);
        
        List<Class<?>> result = GenericsUtils.getMethodGenericParameterTypes(method, 0);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertEquals("第一个参数应该有2个泛型参数", 2, result.size());
        Assert.assertEquals("第一个泛型参数应该是String", String.class, result.get(0));
        Assert.assertEquals("第二个泛型参数应该是Integer", Integer.class, result.get(1));
    }

    /**
     * 测试方法参数泛型 - 默认索引
     */
    @Test
    public void testGetMethodGenericParameterTypesDefaultIndex() throws Exception {
        Method method = TestClass.class.getMethod("listParameterMethod", List.class);
        
        List<Class<?>> result = GenericsUtils.getMethodGenericParameterTypes(method);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertEquals("第一个参数应该有1个泛型参数", 1, result.size());
        Assert.assertEquals("泛型参数应该是String", String.class, result.get(0));
    }

    /**
     * 测试方法参数泛型 - 无泛型参数
     */
    @Test
    public void testGetMethodGenericParameterTypesNoGeneric() throws Exception {
        Method method = TestClass.class.getMethod("nonGenericParameterMethod", String.class);
        
        List<Class<?>> result = GenericsUtils.getMethodGenericParameterTypes(method, 0);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertTrue("无泛型参数应该返回空列表", result.isEmpty());
    }

    /**
     * 测试方法参数泛型 - 负索引
     */
    @Test(expected = RuntimeException.class)
    public void testGetMethodGenericParameterTypesNegativeIndex() throws Exception {
        Method method = TestClass.class.getMethod("mapParameterMethod", Map.class, List.class);
        
        GenericsUtils.getMethodGenericParameterTypes(method, -1);
    }

    /**
     * 测试方法参数泛型 - 超出范围的索引
     */
    @Test(expected = RuntimeException.class)
    public void testGetMethodGenericParameterTypesOutOfRangeIndex() throws Exception {
        Method method = TestClass.class.getMethod("mapParameterMethod", Map.class, List.class);
        
        GenericsUtils.getMethodGenericParameterTypes(method, 2);
    }

    /**
     * 测试字段泛型 - 基本功能
     */
    @Test
    public void testGetFieldGenericTypeBasic() throws Exception {
        Field field = TestClass.class.getDeclaredField("mapField");
        
        Class<?> result = GenericsUtils.getFieldGenericType(field, 0);
        Assert.assertEquals("字段第一个泛型参数应该是String", String.class, result);
        
        Class<?> result2 = GenericsUtils.getFieldGenericType(field, 1);
        Assert.assertEquals("字段第二个泛型参数应该是Integer", Integer.class, result2);
    }

    /**
     * 测试字段泛型 - 默认索引
     */
    @Test
    public void testGetFieldGenericTypeDefaultIndex() throws Exception {
        Field field = TestClass.class.getDeclaredField("listField");
        
        Class<?> result = GenericsUtils.getFieldGenericType(field);
        Assert.assertEquals("默认应该返回第一个泛型参数", String.class, result);
    }

    /**
     * 测试字段泛型 - 无泛型字段
     */
    @Test
    public void testGetFieldGenericTypeNoGeneric() throws Exception {
        Field field = TestClass.class.getDeclaredField("nonGenericField");
        
        Class<?> result = GenericsUtils.getFieldGenericType(field);
        Assert.assertEquals("无泛型字段应该返回Object.class", Object.class, result);
    }

    /**
     * 测试字段泛型 - 负索引
     */
    @Test(expected = RuntimeException.class)
    public void testGetFieldGenericTypeNegativeIndex() throws Exception {
        Field field = TestClass.class.getDeclaredField("mapField");
        
        GenericsUtils.getFieldGenericType(field, -1);
    }

    /**
     * 测试字段泛型 - 超出范围的索引
     */
    @Test(expected = RuntimeException.class)
    public void testGetFieldGenericTypeOutOfRangeIndex() throws Exception {
        Field field = TestClass.class.getDeclaredField("mapField");
        
        GenericsUtils.getFieldGenericType(field, 2);
    }

    /**
     * 测试嵌套泛型
     */
    @Test
    public void testNestedGeneric() throws Exception {
        Method method = TestClass.class.getMethod("nestedGenericMethod");
        
        Class<?> result = GenericsUtils.getMethodGenericReturnType(method, 0);
        Assert.assertEquals("嵌套泛型应该返回String.class", String.class, result);
    }

    /**
     * 测试通配符泛型
     */
    @Test
    public void testWildcardGeneric() throws Exception {
        Method method = TestClass.class.getMethod("wildcardGenericMethod");
        
        Class<?> result = GenericsUtils.getMethodGenericReturnType(method);
        Assert.assertEquals("通配符泛型应该返回Object.class", Object.class, result);
    }

    /**
     * 测试泛型父类 - 复杂继承层次
     */
    @Test
    public void testComplexInheritance() {
        class ChildClass extends GenericParent<String, Integer> {}
        class GrandChildClass extends ChildClass {}
        
        Class<?> result = GenericsUtils.getSuperClassGenericType(GrandChildClass.class);
        Assert.assertEquals("应该获取到父类的泛型参数", String.class, result);
    }

    /**
     * 测试方法参数泛型 - 多个参数
     */
    @Test
    public void testMultipleMethodParameters() throws Exception {
        Method method = TestClass.class.getMethod("multipleParametersMethod", Map.class, List.class, String.class);
        
        List<Class<?>> result1 = GenericsUtils.getMethodGenericParameterTypes(method, 0);
        Assert.assertEquals("第一个参数应该有2个泛型参数", 2, result1.size());
        
        List<Class<?>> result2 = GenericsUtils.getMethodGenericParameterTypes(method, 1);
        Assert.assertEquals("第二个参数应该有1个泛型参数", 1, result2.size());
        
        List<Class<?>> result3 = GenericsUtils.getMethodGenericParameterTypes(method, 2);
        Assert.assertTrue("第三个参数应该没有泛型参数", result3.isEmpty());
    }

    /**
     * 测试用到的测试类
     */
    static class GenericParent<T, U> {}
    static class NonGenericParent {}
    static class SingleGenericParent<T> {}
    static class MultipleGenericParent<T, U, V, W> {}

    static class TestClass {
        public Map<String, Integer> getMapMethod() { return null; }
        public List<String> getListMethod() { return null; }
        public String getNonGenericMethod() { return null; }
        
        public void mapParameterMethod(Map<String, Integer> map, List<String> list) {}
        public void listParameterMethod(List<String> list) {}
        public void nonGenericParameterMethod(String str) {}
        
        public List<String> nestedGenericMethod() { return null; }
        public List<?> wildcardGenericMethod() { return null; }
        
        public void multipleParametersMethod(Map<String, Integer> map, List<String> list, String str) {}
        
        public Map<String, Integer> mapField;
        public List<String> listField;
        public String nonGenericField;
    }
}