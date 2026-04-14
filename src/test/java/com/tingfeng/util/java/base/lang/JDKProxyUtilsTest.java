package com.tingfeng.util.java.base.lang;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * JDK代理工具类测试
 */
public class JDKProxyUtilsTest {

    /**
     * 测试接口定义
     */
    interface TestInterface {
        String sayHello(String name);
        int add(int a, int b);
        void doSomething();
    }

    /**
     * 另一个测试接口
     */
    interface AnotherInterface {
        String greet();
    }

    /**
     * 带默认方法的接口
     */
    interface InterfaceWithDefault {
        default String defaultMethod() {
            return "Default implementation";
        }
        
        String customMethod();
    }

    /**
     * 测试创建代理对象 - 基本功能
     */
    @Test
    public void testGetProxyBasic() {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("sayHello")) {
                return "Hello, " + args[0];
            }
            return null;
        };

        TestInterface proxy = JDKProxyUtils.getProxy(TestInterface.class, handler);
        Assert.assertNotNull("代理对象不能为空", proxy);
        String result = proxy.sayHello("World");
        Assert.assertEquals("代理方法应该正确执行", "Hello, World", result);
    }

    /**
     * 测试创建代理对象 - 多个方法
     */
    @Test
    public void testGetProxyMultipleMethods() {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("sayHello")) {
                return "Hello, " + args[0];
            } else if (method.getName().equals("add")) {
                return (int) args[0] + (int) args[1];
            } else if (method.getName().equals("doSomething")) {
                return null;
            }
            return null;
        };

        TestInterface proxy = JDKProxyUtils.getProxy(TestInterface.class, handler);
        
        String helloResult = proxy.sayHello("Test");
        Assert.assertEquals("sayHello方法应该正确执行", "Hello, Test", helloResult);
        
        int addResult = proxy.add(3, 5);
        Assert.assertEquals("add方法应该正确执行", 8, addResult);
        
        proxy.doSomething(); // void方法，不抛异常即可
    }

    /**
     * 测试创建代理对象 - 方法抛出异常
     */
    @Test(expected = RuntimeException.class)
    public void testGetProxyWithException() {
        InvocationHandler handler = (proxy, method, args) -> {
            throw new RuntimeException("Test exception");
        };

        TestInterface proxy = JDKProxyUtils.getProxy(TestInterface.class, handler);
        proxy.sayHello("World");
    }

    /**
     * 测试创建代理对象 - 返回不同类型
     */
    @Test
    public void testGetProxyDifferentReturnTypes() {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("sayHello")) {
                return "Hello";
            } else if (method.getName().equals("add")) {
                return 42;
            } else {
                return null;
            }
        };

        TestInterface proxy = JDKProxyUtils.getProxy(TestInterface.class, handler);
        
        String stringResult = proxy.sayHello("Test");
        Assert.assertEquals("应该返回String类型", "Hello", stringResult);
        
        int intResult = proxy.add(1, 2);
        Assert.assertEquals("应该返回int类型", 42, intResult);
    }

    /**
     * 测试创建代理对象 - 方法参数验证
     */
    @Test
    public void testGetProxyParameterValidation() {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("sayHello")) {
                return "Hello, " + args[0] + ", param count: " + args.length;
            } else if (method.getName().equals("add")) {
                return (int) args[0] + (int) args[1];
            }
            return null;
        };

        TestInterface proxy = JDKProxyUtils.getProxy(TestInterface.class, handler);
        
        String helloResult = proxy.sayHello("Test");
        Assert.assertEquals("参数应该正确传递", "Hello, Test, param count: 1", helloResult);
        
        int addResult = proxy.add(10, 20);
        Assert.assertEquals("参数应该正确传递", 30, addResult);
    }

    /**
     * 测试创建代理对象 - 方法信息获取
     */
    @Test
    public void testGetProxyMethodInfo() {
        InvocationHandler handler = (proxy, method, args) -> {
            return "Method: " + method.getName() + ", Declaring class: " + method.getDeclaringClass().getSimpleName();
        };

        TestInterface proxy = JDKProxyUtils.getProxy(TestInterface.class, handler);
        
        String result = proxy.sayHello("Test");
        Assert.assertTrue("应该包含方法名", result.contains("sayHello"));
        Assert.assertTrue("应该包含声明类", result.contains("TestInterface"));
    }

    /**
     * 测试创建代理对象 - null返回值
     */
    @Test
    public void testGetProxyNullReturn() {
        InvocationHandler handler = (proxy, method, args) -> null;

        TestInterface proxy = JDKProxyUtils.getProxy(TestInterface.class, handler);
        
        String result = proxy.sayHello("Test");
        Assert.assertNull("应该返回null", result);
    }

    /**
     * 测试创建代理对象 - 多次调用
     */
    @Test
    public void testGetProxyMultipleCalls() {
        final int[] callCount = {0};
        InvocationHandler handler = (proxy, method, args) -> {
            callCount[0]++;
            return "Call #" + callCount[0];
        };

        TestInterface proxy = JDKProxyUtils.getProxy(TestInterface.class, handler);
        
        String result1 = proxy.sayHello("First");
        String result2 = proxy.sayHello("Second");
        String result3 = proxy.sayHello("Third");
        
        Assert.assertEquals("第一次调用", "Call #1", result1);
        Assert.assertEquals("第二次调用", "Call #2", result2);
        Assert.assertEquals("第三次调用", "Call #3", result3);
        Assert.assertEquals("总共应该调用3次", 3, callCount[0]);
    }

    /**
     * 测试创建代理对象 - 不同接口
     */
    @Test
    public void testGetProxyDifferentInterfaces() {
        InvocationHandler handler = (proxy, method, args) -> "Greeting from " + method.getDeclaringClass().getSimpleName();

        TestInterface proxy1 = JDKProxyUtils.getProxy(TestInterface.class, handler);
        AnotherInterface proxy2 = JDKProxyUtils.getProxy(AnotherInterface.class, handler);
        
        String result1 = proxy1.sayHello("Test");
        String result2 = proxy2.greet();
        
        Assert.assertTrue("TestInterface代理应该正确工作", result1.contains("TestInterface"));
        Assert.assertTrue("AnotherInterface代理应该正确工作", result2.contains("AnotherInterface"));
    }

    /**
     * 测试创建代理对象 - 默认方法（如果接口有默认方法）
     */
    @Test
    public void testGetProxyDefaultMethod() {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.isDefault()) {
                return "Handler intercepted default method";
            } else {
                return "Handler intercepted custom method";
            }
        };

        InterfaceWithDefault proxy = JDKProxyUtils.getProxy(InterfaceWithDefault.class, handler);
        
        String result = proxy.customMethod();
        Assert.assertEquals("应该拦截自定义方法", "Handler intercepted custom method", result);
    }

    /**
     * 测试创建代理对象 - Object类方法
     */
    @Test
    public void testGetProxyObjectMethods() {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("toString")) {
                return "Proxy toString";
            } else if (method.getName().equals("hashCode")) {
                return 12345;
            } else if (method.getName().equals("equals")) {
                return args[0] != null && args[0].toString().contains("Proxy");
            }
            return null;
        };

        TestInterface proxy = JDKProxyUtils.getProxy(TestInterface.class, handler);
        
        String toStringResult = proxy.toString();
        Assert.assertEquals("toString应该被拦截", "Proxy toString", toStringResult);
        
        int hashCodeResult = proxy.hashCode();
        Assert.assertEquals("hashCode应该被拦截", 12345, hashCodeResult);
        
        boolean equalsResult = proxy.equals(proxy);
        Assert.assertTrue("equals应该被拦截", equalsResult);
    }

    /**
     * 测试创建代理对象 - 性能测试
     */
    @Test
    public void testGetProxyPerformance() {
        InvocationHandler handler = (proxy, method, args) -> "Result";
        TestInterface proxy = JDKProxyUtils.getProxy(TestInterface.class, handler);
        
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            proxy.sayHello("Test");
        }
        long endTime = System.currentTimeMillis();
        
        long duration = endTime - startTime;
        Assert.assertTrue("10000次调用应该在合理时间内完成", duration < 1000);
    }
}