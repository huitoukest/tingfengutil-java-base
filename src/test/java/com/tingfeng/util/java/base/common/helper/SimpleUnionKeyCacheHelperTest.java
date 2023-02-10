package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.bean.UnionKey;
import com.tingfeng.util.java.base.common.utils.TestUtils;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SimpleUnionKeyCacheHelperTest {

    @Test
    public void get() {
    }

    /**
     * 用于测试 多种Key联合 存储转Map 的读写性能
     */
    @Test
    public void test(){
        String key1 = SimpleUnionKeyCacheHelperTest.class.getSimpleName();
        String key2 = "abcfdwf";
        String key3 = "@";
        SimpleUnionKeyCacheHelperTest value = new SimpleUnionKeyCacheHelperTest();
        int testCount = 5000000;
        //手动字符串链接Key的方式
        Map<String,Object> map = new HashMap<>();
        TestUtils.printTime(1,testCount,index -> {
            String key = StringUtils.append(key1, key2, key3);
            map.put(key,value);
            Object o = map.get(key);
            Assert.assertEquals(value,o);
        });
        //使用普通Map + UnionKey 方式
        Map<UnionKey,Object> map2 = new HashMap<>();
        TestUtils.printTime(1,testCount,index -> {
            map2.put(new UnionKey(key1,key2,key3),value);
            Object o = map2.get(new UnionKey(key1,key2,key3));
            Assert.assertEquals(value,o);
        });
        //使用多重嵌套Map的方式
        Map<String,Object> mapTmp = new HashMap<>();
        mapTmp.put(key3,value);
        TestUtils.printTime(1,testCount,index -> {
            putWithBeanCopyByMultiKey(key1,key2,mapTmp);
            Object o =getWithBeanCopyByMultiKey(key1,key2).get(key3);
            Assert.assertEquals(value,o);
        });
    }

    private static SimpleCacheHelper<String, Map<String,Map<String, Object>>> BEAN_COPY_METHOD_CACHE = new SimpleCacheHelper<>(512);
    private static Map<String, Object> getWithBeanCopyByMultiKey(String targetClass,String sourceClass){
        Map<String, Map<String, Object>> classMapMap = BEAN_COPY_METHOD_CACHE.get(targetClass);
        if(classMapMap != null){
            return classMapMap.get(sourceClass);
        }
        return null;
    }

    private static synchronized void putWithBeanCopyByMultiKey(String targetClass,String sourceClass,Map<String, Object> methodMap){
        Map<String, Map<String, Object>> classMapMap = BEAN_COPY_METHOD_CACHE.get(targetClass);
        if(classMapMap == null){
            classMapMap = new HashMap<>();
            BEAN_COPY_METHOD_CACHE.set(targetClass,classMapMap);
        }
        classMapMap.put(sourceClass,methodMap);
    }


}