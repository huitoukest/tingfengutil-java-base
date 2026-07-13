package com.tingfeng.util.java.base.collection.base;

import org.junit.Assert;
import org.junit.Test;

/**
 * FastMap 单元测试
 */
public class FastMapTest {

    /**
     * 验证 add 返回 FastMap 自身，支持链式连续调用
     */
    @Test
    public void testAddChain() {
        FastMap<String, Integer> map = new FastMap<>();
        FastMap<String, Integer> result = map.add("a", 1).add("b", 2).add("c", 3);

        Assert.assertSame("add 应返回当前 FastMap 实例", map, result);
        Assert.assertEquals(3, map.size());
        Assert.assertEquals(Integer.valueOf(1), map.get("a"));
        Assert.assertEquals(Integer.valueOf(2), map.get("b"));
        Assert.assertEquals(Integer.valueOf(3), map.get("c"));
    }

    /**
     * H-1 回归：验证 instance(K,V) 构造的 Map 包含传入的 KV 对
     */
    @Test
    public void testInstanceWithKV() {
        FastMap<String, String> map = FastMap.instance("name", "test");

        Assert.assertEquals(1, map.size());
        Assert.assertEquals("test", map.get("name"));
        Assert.assertTrue(map.containsKey("name"));
    }

    /**
     * 验证 instance() 返回的空 Map
     */
    @Test
    public void testInstanceEmpty() {
        FastMap<String, Integer> map = FastMap.instance();

        Assert.assertTrue("空 Map 应 isEmpty", map.isEmpty());
        Assert.assertEquals(0, map.size());
    }

    /**
     * 验证继承自 HashMap 的方法正常
     */
    @Test
    public void testInheritedBehaviors() {
        FastMap<String, String> map = new FastMap<>();

        // put / size / get
        map.put("k1", "v1");
        map.put("k2", "v2");
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("v1", map.get("k1"));
        Assert.assertNull("不存在的 key 返回 null", map.get("nonexistent"));

        // containsKey
        Assert.assertTrue(map.containsKey("k1"));
        Assert.assertFalse(map.containsKey("nonexistent"));

        // containsValue
        Assert.assertTrue(map.containsValue("v2"));
        Assert.assertFalse(map.containsValue("nonexistent"));

        // remove
        map.remove("k1");
        Assert.assertEquals(1, map.size());
        Assert.assertNull(map.get("k1"));

        // clear
        map.clear();
        Assert.assertTrue(map.isEmpty());
    }
}
