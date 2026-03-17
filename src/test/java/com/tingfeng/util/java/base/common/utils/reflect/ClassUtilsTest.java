package com.tingfeng.util.java.base.common.utils.reflect;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * 类工具类测试
 */
public class ClassUtilsTest {

    /**
     * 测试接口 - 基本功能
     */
    @Test
    public void testGetAllClassByInterfaceBasic() {
        List<Class<?>> result = ClassUtils.getAllClassByInterface(TestInterface.class);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertTrue("应该包含实现类", result.size() > 0);
        Assert.assertTrue("应该包含TestClass1", result.contains(TestClass1.class));
        Assert.assertTrue("应该包含TestClass2", result.contains(TestClass2.class));
    }

    /**
     * 测试接口 - 非接口类
     */
    @Test
    public void testGetAllClassByInterfaceNotInterface() {
        List<Class<?>> result = ClassUtils.getAllClassByInterface(TestClass1.class);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertTrue("非接口类应该返回空列表", result.isEmpty());
    }

    /**
     * 测试接口 - null输入
     */
    @Test(expected = NullPointerException.class)
    public void testGetAllClassByInterfaceNull() {
        ClassUtils.getAllClassByInterface(null);
    }

    /**
     * 测试获取类 - 基本功能
     */
    @Test
    public void testGetClassesBasic() {
        String packageName = ClassUtilsTest.class.getPackage().getName();
        List<Class<?>> result = ClassUtils.getClasses(packageName);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertTrue("应该包含当前测试类", result.contains(ClassUtilsTest.class));
    }

    /**
     * 测试获取类 - 递归查找
     */
    @Test
    public void testGetClassesRecursive() {
        String packageName = "com.tingfeng.util.java.base.common.utils";
        List<Class<?>> result = ClassUtils.getClasses(packageName, true);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertTrue("递归查找应该找到多个类", result.size() > 10);
    }

    /**
     * 测试获取类 - 非递归查找
     */
    @Test
    public void testGetClassesNonRecursive() {
        String packageName = "com.tingfeng.util.java.base.common.utils";
        List<Class<?>> result = ClassUtils.getClasses(packageName, false);
        Assert.assertNotNull("结果不能为空", result);
    }

    /**
     * 测试获取类 - 不存在的包
     */
    @Test
    public void testGetClassesNonExistentPackage() {
        String packageName = "com.nonexistent.package";
        List<Class<?>> result = ClassUtils.getClasses(packageName);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertTrue("不存在的包应该返回空列表", result.isEmpty());
    }

    /**
     * 测试获取类 - 空包名
     */
    @Test
    public void testGetClassesEmptyPackage() {
        String packageName = "";
        List<Class<?>> result = ClassUtils.getClasses(packageName);
        Assert.assertNotNull("结果不能为空", result);
    }

    /**
     * 测试通过Jar文件查找类
     */
    @Test
    public void testFindClassesByJar() {
        // 这个测试依赖于运行环境，可能在不同环境下表现不同
        String packageName = "org.junit";
        List<Class<?>> result = ClassUtils.getClasses(packageName);
        Assert.assertNotNull("结果不能为空", result);
    }

    /**
     * 测试通过路径查找类
     */
    @Test
    public void testFindClassesByPath() {
        String packageName = ClassUtilsTest.class.getPackage().getName();
        String packagePath = packageName.replace('.', '/');
        
        List<Class<?>> result = ClassUtils.getClasses(packageName);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertTrue("应该找到测试类", result.contains(ClassUtilsTest.class));
    }

    /**
     * 测试获取类 - 多次调用一致性
     */
    @Test
    public void testGetClassesConsistency() {
        String packageName = ClassUtilsTest.class.getPackage().getName();
        
        List<Class<?>> result1 = ClassUtils.getClasses(packageName);
        List<Class<?>> result2 = ClassUtils.getClasses(packageName);
        
        Assert.assertEquals("多次调用应该返回相同的结果", result1.size(), result2.size());
    }

    /**
     * 测试获取类 - 性能测试
     */
    @Test
    public void testGetClassesPerformance() {
        String packageName = "com.tingfeng.util.java.base.common.utils";
        
        long startTime = System.currentTimeMillis();
        List<Class<?>> result = ClassUtils.getClasses(packageName);
        long endTime = System.currentTimeMillis();
        
        Assert.assertNotNull("结果不能为空", result);
        long duration = endTime - startTime;
        Assert.assertTrue("类查找应该在合理时间内完成", duration < 5000);
    }

    /**
     * 测试接口实现类 - 多个实现
     */
    @Test
    public void testGetAllClassByInterfaceMultipleImplementations() {
        List<Class<?>> result = ClassUtils.getAllClassByInterface(TestInterface.class);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertTrue("应该包含多个实现类", result.size() >= 2);
    }

    /**
     * 测试接口实现类 - 不包含接口本身
     */
    @Test
    public void testGetAllClassByInterfaceNotIncludeInterface() {
        List<Class<?>> result = ClassUtils.getAllClassByInterface(TestInterface.class);
        Assert.assertFalse("结果不应该包含接口本身", result.contains(TestInterface.class));
    }

    /**
     * 测试接口实现类 - 排除抽象类
     */
    @Test
    public void testGetAllClassByInterfaceExcludeAbstract() {
        List<Class<?>> result = ClassUtils.getAllClassByInterface(TestInterface.class);
        Assert.assertNotNull("结果不能为空", result);
        for (Class<?> clazz : result) {
            Assert.assertFalse("结果不应该包含抽象类", java.lang.reflect.Modifier.isAbstract(clazz.getModifiers()));
        }
    }

    /**
     * 测试接口实现类 - 验证实现关系
     */
    @Test
    public void testGetAllClassByInterfaceVerifyImplementation() {
        List<Class<?>> result = ClassUtils.getAllClassByInterface(TestInterface.class);
        Assert.assertNotNull("结果不能为空", result);
        
        for (Class<?> clazz : result) {
            Assert.assertTrue("所有结果都应该实现TestInterface", TestInterface.class.isAssignableFrom(clazz));
        }
    }

    /**
     * 测试用到的接口和类
     */
    interface TestInterface {
        void testMethod();
    }

    static class TestClass1 implements TestInterface {
        @Override
        public void testMethod() {}
    }

    static class TestClass2 implements TestInterface {
        @Override
        public void testMethod() {}
    }

    static class TestClass3 {
        public void otherMethod() {}
    }
}