package com.tingfeng.util.java.base.common.utils.reflect;

import org.junit.Assert;
import org.junit.Test;

/**
 * 方法工具类测试
 */
public class MethodUtilsTest {

    /**
     * 测试获取当前方法名 - 完整方法名
     */
    @Test
    public void testGetCurrentMethodName() {
        String methodName = MethodUtils.getCurrentMethodName();
        Assert.assertNotNull("方法名不能为空", methodName);
        Assert.assertTrue("方法名应该包含类名", methodName.contains("MethodUtilsTest"));
        Assert.assertTrue("方法名应该包含方法名", methodName.contains("testGetCurrentMethodName"));
    }

    /**
     * 测试获取当前方法名 - 简单方法名
     */
    @Test
    public void testGetCurrentSimpleMethodName() {
        String methodName = MethodUtils.getCurrentSimpleMethodName();
        Assert.assertNotNull("方法名不能为空", methodName);
        Assert.assertEquals("方法名应该是当前方法名", "testGetCurrentSimpleMethodName", methodName);
    }

    /**
     * 测试获取当前方法名 - 不同方法调用
     */
    @Test
    public void testGetCurrentMethodNameDifferentMethods() {
        String methodName1 = MethodUtils.getCurrentMethodName();
        String methodName2 = MethodUtils.getCurrentMethodName();
        
        Assert.assertEquals("同一个方法调用应该返回相同的方法名", methodName1, methodName2);
    }

    /**
     * 测试获取当前方法名 - 从辅助方法调用
     */
    @Test
    public void testGetCurrentMethodNameFromHelper() {
        String methodName = callHelperMethod();
        Assert.assertNotNull("方法名不能为空", methodName);
        Assert.assertTrue("方法名应该包含类名", methodName.contains("MethodUtilsTest"));
        Assert.assertTrue("方法名应该包含方法名", methodName.contains("testGetCurrentMethodNameFromHelper"));
    }

    /**
     * 辅助方法用于测试
     */
    private String callHelperMethod() {
        return MethodUtils.getCurrentMethodName(3);
    }

    /**
     * 测试获取当前简单方法名 - 从辅助方法调用
     */
    @Test
    public void testGetCurrentSimpleMethodNameFromHelper() {
        String methodName = callHelperMethodSimple();
        Assert.assertEquals("方法名应该是当前方法名", "testGetCurrentSimpleMethodNameFromHelper", methodName);
    }

    /**
     * 辅助方法用于测试简单方法名
     */
    private String callHelperMethodSimple() {
        return MethodUtils.getCurrentSimpleMethodName(3);
    }

    /**
     * 测试获取当前方法名 - 多次调用一致性
     */
    @Test
    public void testGetCurrentMethodNameConsistency() {
        String methodName1 = MethodUtils.getCurrentMethodName();
        String methodName2 = MethodUtils.getCurrentMethodName();
        String methodName3 = MethodUtils.getCurrentMethodName();
        
        Assert.assertEquals("多次调用应该返回相同的方法名", methodName1, methodName2);
        Assert.assertEquals("多次调用应该返回相同的方法名", methodName2, methodName3);
    }

    /**
     * 测试获取当前简单方法名 - 多次调用一致性
     */
    @Test
    public void testGetCurrentSimpleMethodNameConsistency() {
        String methodName1 = MethodUtils.getCurrentSimpleMethodName();
        String methodName2 = MethodUtils.getCurrentSimpleMethodName();
        String methodName3 = MethodUtils.getCurrentSimpleMethodName();
        
        Assert.assertEquals("多次调用应该返回相同的方法名", methodName1, methodName2);
        Assert.assertEquals("多次调用应该返回相同的方法名", methodName2, methodName3);
    }

    /**
     * 测试获取当前方法名 - 格式验证
     */
    @Test
    public void testGetCurrentMethodNameFormat() {
        String methodName = MethodUtils.getCurrentMethodName();
        Assert.assertTrue("方法名应该包含点号分隔符", methodName.contains("."));
        
        String[] parts = methodName.split("\\.");
        Assert.assertTrue("方法名应该至少包含类名和方法名两部分", parts.length >= 2);
        Assert.assertEquals("最后一部分应该是方法名", "testGetCurrentMethodNameFormat", parts[parts.length - 1]);
    }

    /**
     * 测试获取当前简单方法名 - 格式验证
     */
    @Test
    public void testGetCurrentSimpleMethodNameFormat() {
        String methodName = MethodUtils.getCurrentSimpleMethodName();
        Assert.assertFalse("简单方法名不应该包含点号", methodName.contains("."));
        Assert.assertTrue("简单方法名应该只包含字母和数字", methodName.matches("[a-zA-Z0-9_]+"));
    }

    /**
     * 测试获取当前方法名 - 不同测试方法
     */
    @Test
    public void testGetCurrentMethodNameDifferentTests() {
        String methodName1 = MethodUtils.getCurrentMethodName();
        
        // 调用另一个测试方法
        anotherTestMethod();
    }

    /**
     * 另一个测试方法
     */
    private void anotherTestMethod() {
        String methodName2 = MethodUtils.getCurrentSimpleMethodName();
        Assert.assertNotEquals("不同方法应该返回不同的方法名", 
            "testGetCurrentMethodNameDifferentTests", methodName2);
        Assert.assertEquals("方法名应该是当前方法名", "anotherTestMethod", methodName2);
    }

    /**
     * 测试获取当前方法名 - 静态方法调用
     */
    @Test
    public void testGetCurrentMethodNameStaticMethod() {
        String methodName = MethodUtils.getCurrentSimpleMethodName();
        Assert.assertEquals("静态方法调用应该返回正确的方法名", 
            "testGetCurrentMethodNameStaticMethod", methodName);
    }

    /**
     * 测试获取当前简单方法名 - 静态方法调用
     */
    @Test
    public void testGetCurrentSimpleMethodNameStaticMethod() {
        String methodName = MethodUtils.getCurrentSimpleMethodName();
        Assert.assertEquals("静态方法调用应该返回正确的方法名", 
            "testGetCurrentSimpleMethodNameStaticMethod", methodName);
    }

    /**
     * 测试获取当前方法名 - 性能测试
     */
    @Test
    public void testGetCurrentMethodNamePerformance() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            MethodUtils.getCurrentMethodName();
        }
        long endTime = System.currentTimeMillis();
        
        long duration = endTime - startTime;
        Assert.assertTrue("10000次调用应该在合理时间内完成", duration < 1000);
    }

    /**
     * 测试获取当前简单方法名 - 性能测试
     */
    @Test
    public void testGetCurrentSimpleMethodNamePerformance() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            MethodUtils.getCurrentSimpleMethodName();
        }
        long endTime = System.currentTimeMillis();
        
        long duration = endTime - startTime;
        Assert.assertTrue("10000次调用应该在合理时间内完成", duration < 1000);
    }
}