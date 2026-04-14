package com.tingfeng.util.java.base.bean;

import com.tingfeng.util.java.base.lang.base.UnionKey;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * UnionKey 类的单元测试
 * 测试联合键的基本功能、哈希和相等性、null值处理、Map键使用、可变参数构造和序列化
 *
 * @author huitoukest
 */
public class UnionKeyTest {

    /**
     * 测试基本功能
     */
    @Test
    public void testBasicFunctionality() {
        // 测试两键构造
        UnionKey key1 = new UnionKey("a", 1);
        String key1_0 = key1.getKey(0);
        Integer key1_1 = key1.getKey(1);
        Assert.assertEquals("a", key1_0);
        Assert.assertEquals(1, key1_1.intValue());

        // 测试三键构造
        UnionKey key2 = new UnionKey("a", 1, true);
        String key2_0 = key2.getKey(0);
        Integer key2_1 = key2.getKey(1);
        Boolean key2_2 = key2.getKey(2);
        Assert.assertEquals("a", key2_0);
        Assert.assertEquals(1, key2_1.intValue());
        Assert.assertEquals(true, key2_2.booleanValue());

        // 测试四键构造
        UnionKey key3 = new UnionKey("a", 1, true, 3.14);
        String key3_0 = key3.getKey(0);
        Integer key3_1 = key3.getKey(1);
        Boolean key3_2 = key3.getKey(2);
        Double key3_3 = key3.getKey(3);
        Assert.assertEquals("a", key3_0);
        Assert.assertEquals(1, key3_1.intValue());
        Assert.assertEquals(true, key3_2.booleanValue());
        Assert.assertEquals(3.14, key3_3.doubleValue(), 0.001);

        // 测试五键构造
        UnionKey key4 = new UnionKey("a", 1, true, 3.14, 'c');
        String key4_0 = key4.getKey(0);
        Integer key4_1 = key4.getKey(1);
        Boolean key4_2 = key4.getKey(2);
        Double key4_3 = key4.getKey(3);
        Character key4_4 = key4.getKey(4);
        Assert.assertEquals("a", key4_0);
        Assert.assertEquals(1, key4_1.intValue());
        Assert.assertEquals(true, key4_2.booleanValue());
        Assert.assertEquals(3.14, key4_3.doubleValue(), 0.001);
        Assert.assertEquals('c', key4_4.charValue());

        // 测试可变参数构造
        UnionKey key5 = new UnionKey("a", 1, true, 3.14, 'c', "test");
        String key5_0 = key5.getKey(0);
        Integer key5_1 = key5.getKey(1);
        Boolean key5_2 = key5.getKey(2);
        Double key5_3 = key5.getKey(3);
        Character key5_4 = key5.getKey(4);
        String key5_5 = key5.getKey(5);
        Assert.assertEquals("a", key5_0);
        Assert.assertEquals(1, key5_1.intValue());
        Assert.assertEquals(true, key5_2.booleanValue());
        Assert.assertEquals(3.14, key5_3.doubleValue(), 0.001);
        Assert.assertEquals('c', key5_4.charValue());
        Assert.assertEquals("test", key5_5);
    }

    /**
     * 测试哈希和相等性
     */
    @Test
    public void testHashCodeAndEquals() {
        // 相同值的键应该相等
        UnionKey key1 = new UnionKey("a", 1);
        UnionKey key2 = new UnionKey("a", 1);
        Assert.assertEquals(key1, key2);
        Assert.assertEquals(key1.hashCode(), key2.hashCode());

        // 不同值的键应该不相等
        UnionKey key3 = new UnionKey("b", 1);
        Assert.assertNotEquals(key1, key3);
        Assert.assertNotEquals(key1.hashCode(), key3.hashCode());

        // 不同长度的键应该不相等
        UnionKey key4 = new UnionKey("a", 1, true);
        Assert.assertNotEquals(key1, key4);
    }

    /**
     * 测试 null 值
     */
    @Test
    public void testNullValues() {
        // 包含 null 值的键
        UnionKey key1 = new UnionKey("a", null);
        UnionKey key2 = new UnionKey("a", null);
        Assert.assertEquals(key1, key2);
        Assert.assertEquals(key1.hashCode(), key2.hashCode());

        // 全 null 值的键
        UnionKey key3 = new UnionKey(null, null);
        UnionKey key4 = new UnionKey(null, null);
        Assert.assertEquals(key3, key4);
        Assert.assertEquals(key3.hashCode(), key4.hashCode());

        // 部分 null 值的键
        UnionKey key5 = new UnionKey(null, 1);
        UnionKey key6 = new UnionKey("a", 1);
        Assert.assertNotEquals(key5, key6);
    }

    /**
     * 测试作为 Map 键
     */
    @Test
    public void testAsMapKey() {
        Map<UnionKey, String> map = new HashMap<>();

        // 添加键值对
        UnionKey key1 = new UnionKey("a", 1);
        map.put(key1, "value1");

        // 测试获取值
        UnionKey key2 = new UnionKey("a", 1);
        Assert.assertEquals("value1", map.get(key2));

        // 测试更新值
        map.put(key2, "value2");
        Assert.assertEquals("value2", map.get(key1));

        // 测试不同键
        UnionKey key3 = new UnionKey("b", 1);
        map.put(key3, "value3");
        Assert.assertEquals("value3", map.get(key3));
        Assert.assertEquals("value2", map.get(key1));
    }

    /**
     * 测试 toString 方法
     */
    @Test
    public void testToString() {
        UnionKey key = new UnionKey("a", 1, true);
        String toString = key.toString();
        Assert.assertTrue(toString.contains("a"));
        Assert.assertTrue(toString.contains("1"));
        Assert.assertTrue(toString.contains("true"));
        Assert.assertTrue(toString.startsWith("UnionKey"));
    }

    /**
     * 测试序列化和反序列化
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        UnionKey originalKey = new UnionKey("a", 1, true);

        // 序列化
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(originalKey);
        oos.close();

        // 反序列化
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        UnionKey deserializedKey = (UnionKey) ois.readObject();
        ois.close();

        // 验证相等性
        Assert.assertEquals(originalKey, deserializedKey);
        Assert.assertEquals(originalKey.hashCode(), deserializedKey.hashCode());
    }

    /**
     * 测试获取所有键
     */
    @Test
    public void testGetKeys() {
        UnionKey key = new UnionKey("a", 1, true);
        Object[] keys = key.getKeys();
        Assert.assertEquals(3, keys.length);
        Assert.assertEquals("a", keys[0]);
        Assert.assertEquals(1, ((Integer) keys[1]).intValue());
        Assert.assertEquals(true, ((Boolean) keys[2]).booleanValue());
    }

    /**
     * 测试边界情况
     */
    @Test
    public void testEdgeCases() {
        // 空键数组
        UnionKey emptyKey1 = new UnionKey();
        UnionKey emptyKey2 = new UnionKey();
        Assert.assertEquals(emptyKey1, emptyKey2);
        Assert.assertEquals(emptyKey1.hashCode(), emptyKey2.hashCode());

        // 单个键
        UnionKey singleKey1 = new UnionKey("test");
        UnionKey singleKey2 = new UnionKey("test");
        Assert.assertEquals(singleKey1, singleKey2);
        Assert.assertEquals(singleKey1.hashCode(), singleKey2.hashCode());
    }
}