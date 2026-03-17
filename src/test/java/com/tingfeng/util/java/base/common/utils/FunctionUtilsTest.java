package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 函数工具类测试
 */
public class FunctionUtilsTest {

    /**
     * 测试distinctByKey方法 - 基本功能
     */
    @Test
    public void testDistinctByKeyBasic() {
        List<String> list = Arrays.asList("apple", "banana", "apple", "orange", "banana", "grape");
        
        List<String> distinctList = list.stream()
                .filter(FunctionUtils.distinctByKey(s -> s))
                .collect(Collectors.toList());
        
        Assert.assertEquals("去重后应该有4个元素", 4, distinctList.size());
        Assert.assertTrue("应该包含apple", distinctList.contains("apple"));
        Assert.assertTrue("应该包含banana", distinctList.contains("banana"));
        Assert.assertTrue("应该包含orange", distinctList.contains("orange"));
        Assert.assertTrue("应该包含grape", distinctList.contains("grape"));
    }

    /**
     * 测试distinctByKey方法 - 使用对象属性
     */
    @Test
    public void testDistinctByKeyWithProperty() {
        class Person {
            String name;
            int age;
            
            Person(String name, int age) {
                this.name = name;
                this.age = age;
            }
            
            public String getName() {
                return name;
            }
            
            public int getAge() {
                return age;
            }
        }
        
        List<Person> people = Arrays.asList(
                new Person("Alice", 25),
                new Person("Bob", 30),
                new Person("Alice", 28),
                new Person("Charlie", 25),
                new Person("Bob", 35)
        );
        
        List<Person> distinctByName = people.stream()
                .filter(FunctionUtils.distinctByKey(Person::getName))
                .collect(Collectors.toList());
        
        Assert.assertEquals("按姓名去重后应该有3个元素", 3, distinctByName.size());
        Assert.assertEquals("第一个应该是Alice", "Alice", distinctByName.get(0).getName());
        Assert.assertEquals("第二个应该是Bob", "Bob", distinctByName.get(1).getName());
        Assert.assertEquals("第三个应该是Charlie", "Charlie", distinctByName.get(2).getName());
    }

    /**
     * 测试distinctByKey方法 - 使用多个属性组合
     */
    @Test
    public void testDistinctByKeyWithCompositeKey() {
        class Product {
            String category;
            String name;
            
            Product(String category, String name) {
                this.category = category;
                this.name = name;
            }
            
            public String getCategory() {
                return category;
            }
            
            public String getName() {
                return name;
            }
        }
        
        List<Product> products = Arrays.asList(
                new Product("Electronics", "Laptop"),
                new Product("Electronics", "Phone"),
                new Product("Clothing", "Shirt"),
                new Product("Electronics", "Laptop"),
                new Product("Clothing", "Pants"),
                new Product("Electronics", "Phone")
        );
        
        List<Product> distinctByCategory = products.stream()
                .filter(FunctionUtils.distinctByKey(Product::getCategory))
                .collect(Collectors.toList());
        
        Assert.assertEquals("按类别去重后应该有2个元素", 2, distinctByCategory.size());
        Assert.assertEquals("第一个应该是Electronics", "Electronics", distinctByCategory.get(0).getCategory());
        Assert.assertEquals("第二个应该是Clothing", "Clothing", distinctByCategory.get(1).getCategory());
    }

    /**
     * 测试distinctByKey方法 - 空列表
     */
    @Test
    public void testDistinctByKeyEmptyList() {
        List<String> emptyList = Arrays.asList();
        
        List<String> distinctList = emptyList.stream()
                .filter(FunctionUtils.distinctByKey(s -> s))
                .collect(Collectors.toList());
        
        Assert.assertTrue("空列表去重后应该仍然为空", distinctList.isEmpty());
    }

    /**
     * 测试distinctByKey方法 - 所有元素都相同
     */
    @Test
    public void testDistinctByKeyAllSame() {
        List<String> list = Arrays.asList("same", "same", "same", "same");
        
        List<String> distinctList = list.stream()
                .filter(FunctionUtils.distinctByKey(s -> s))
                .collect(Collectors.toList());
        
        Assert.assertEquals("所有元素相同时，去重后应该只有1个元素", 1, distinctList.size());
        Assert.assertEquals("元素应该是same", "same", distinctList.get(0));
    }

    /**
     * 测试distinctByKey方法 - 所有元素都不同
     */
    @Test
    public void testDistinctByKeyAllDifferent() {
        List<String> list = Arrays.asList("a", "b", "c", "d", "e");
        
        List<String> distinctList = list.stream()
                .filter(FunctionUtils.distinctByKey(s -> s))
                .collect(Collectors.toList());
        
        Assert.assertEquals("所有元素都不同时，去重后应该保持原样", 5, distinctList.size());
    }

    /**
     * 测试distinctByKey方法 - 使用null值
     */
    @Test
    public void testDistinctByKeyWithNullValues() {
        List<String> list = Arrays.asList("a", null, "b", null, "c");
        
        List<String> distinctList = list.stream()
                .filter(FunctionUtils.distinctByKey(s -> s))
                .collect(Collectors.toList());
        
        Assert.assertEquals("包含null值时，去重后应该有4个元素", 4, distinctList.size());
        Assert.assertTrue("应该包含null", distinctList.contains(null));
    }

    /**
     * 测试distinctByKey方法 - 使用整数
     */
    @Test
    public void testDistinctByKeyWithIntegers() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 2, 4, 1, 5);
        
        List<Integer> distinctNumbers = numbers.stream()
                .filter(FunctionUtils.distinctByKey(n -> n))
                .collect(Collectors.toList());
        
        Assert.assertEquals("整数去重后应该有5个元素", 5, distinctNumbers.size());
        Assert.assertTrue("应该包含1", distinctNumbers.contains(1));
        Assert.assertTrue("应该包含2", distinctNumbers.contains(2));
        Assert.assertTrue("应该包含3", distinctNumbers.contains(3));
        Assert.assertTrue("应该包含4", distinctNumbers.contains(4));
        Assert.assertTrue("应该包含5", distinctNumbers.contains(5));
    }

    /**
     * 测试distinctByKey方法 - 保持顺序
     */
    @Test
    public void testDistinctByKeyPreserveOrder() {
        List<String> list = Arrays.asList("first", "second", "first", "third", "second");
        
        List<String> distinctList = list.stream()
                .filter(FunctionUtils.distinctByKey(s -> s))
                .collect(Collectors.toList());
        
        Assert.assertEquals("去重后应该有3个元素", 3, distinctList.size());
        Assert.assertEquals("第一个元素应该是first", "first", distinctList.get(0));
        Assert.assertEquals("第二个元素应该是second", "second", distinctList.get(1));
        Assert.assertEquals("第三个元素应该是third", "third", distinctList.get(2));
    }

    /**
     * 测试distinctByKey方法 - 使用复杂对象
     */
    @Test
    public void testDistinctByKeyWithComplexObject() {
        class Order {
            String orderId;
            String customerId;
            double amount;
            
            Order(String orderId, String customerId, double amount) {
                this.orderId = orderId;
                this.customerId = customerId;
                this.amount = amount;
            }
            
            public String getOrderId() {
                return orderId;
            }
            
            public String getCustomerId() {
                return customerId;
            }
            
            public double getAmount() {
                return amount;
            }
        }
        
        List<Order> orders = Arrays.asList(
                new Order("ORD001", "CUST001", 100.0),
                new Order("ORD002", "CUST001", 200.0),
                new Order("ORD003", "CUST002", 150.0),
                new Order("ORD004", "CUST001", 300.0),
                new Order("ORD005", "CUST002", 250.0)
        );
        
        List<Order> distinctByCustomer = orders.stream()
                .filter(FunctionUtils.distinctByKey(Order::getCustomerId))
                .collect(Collectors.toList());
        
        Assert.assertEquals("按客户去重后应该有2个订单", 2, distinctByCustomer.size());
        Assert.assertEquals("第一个订单的客户应该是CUST001", "CUST001", distinctByCustomer.get(0).getCustomerId());
        Assert.assertEquals("第二个订单的客户应该是CUST002", "CUST002", distinctByCustomer.get(1).getCustomerId());
    }
}