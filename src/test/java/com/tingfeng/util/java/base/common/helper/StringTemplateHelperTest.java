package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.utils.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

public class StringTemplateHelperTest {
    

    /*
     * 测试生成模板字符串的功能
     */
    @Test
    public void generatorTest1(){
        String content = "${a},我们看好你}${";
        Map<String,String> params = new HashMap<>();
        params.put("a","asdf");
        String expecStr = "asdf,我们看好你}${";
        StringTemplateHelper helper = new StringTemplateHelper(content, CollectionUtils.createSet("a"));
        Assert.assertTrue(expecStr.equals(helper.generate(params)));
    }

    @Test
    public void generatorTest2(){
        String content = "${a},我们看${b}${c}好${c}${d}你}${";
        Map<String,String> params = new HashMap<>();
        params.put("a","aaaa");
        params.put("b","bbbb");
        params.put("c","cccc");
        String expecStr = "aaaa,我们看bbbbcccc好cccc${d}你}${";
        StringTemplateHelper helper = new StringTemplateHelper(content,CollectionUtils.createSet("a","b","c"));
        Assert.assertTrue(expecStr.equals(helper.generate(params)));
    }

    @Test
    public void generatorTest3(){
        String content = "${}}{${a},我们看${b}${c}好${c}${d}你}${";
        Map<String,String> params = new HashMap<>();
        params.put("a","aaaa");
        params.put("b","bbbb");
        params.put("c","cccc");
        String expecStr = "${}}{aaaa,我们看bbbbcccc好cccc${d}你}${";
        StringTemplateHelper helper = new StringTemplateHelper(content,CollectionUtils.createSet("a","b","c"));
        Assert.assertTrue(expecStr.equals(helper.generate(params)));
    }

    @Test
    public void generatorTest4(){
        String content = "${}{";
        Map<String,String> params = new HashMap<>();
        params.put("a","asdf");
        String expecStr = "${}{";
        StringTemplateHelper helper = new StringTemplateHelper(content,CollectionUtils.createSet("a"));
        Assert.assertTrue(expecStr.equals(helper.generate(params)));
    }

    @Test
    public void generatorTest5(){
        String content = "";
        Map<String,String> params = new HashMap<>();
        params.put("a","asdf");
        String expecStr = "";
        StringTemplateHelper helper = new StringTemplateHelper(content,CollectionUtils.createSet("a"));
        Assert.assertTrue(expecStr.equals(helper.generate(params)));
    }

    @Test
    public void generatorTest6(){
        String content = "";
        Map<String,String> params = new HashMap<>();
        String expecStr = "";
        StringTemplateHelper helper = new StringTemplateHelper(content,CollectionUtils.createSet("a"));
        Assert.assertTrue(expecStr.equals(helper.generate(params)));
    }


}
