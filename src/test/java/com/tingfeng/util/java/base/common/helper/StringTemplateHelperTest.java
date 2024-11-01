package com.tingfeng.util.java.base.common.helper;

import com.alibaba.fastjson.JSON;
import com.tingfeng.util.java.base.common.utils.CollectionUtils;
import com.tingfeng.util.java.base.common.utils.TestUtils;
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

    /**
     * 测试${${}嵌套以及结尾是}的情况；
     * 以及双重嵌套${${}${}}
     * 此时应该支持简单的嵌套即可
     */
    @Test
    public void generatorTest7(){
        String content = "${}}{${${a}},我们看${b}${c}好${c}${d}你}${${b}";
        Map<String,String> params = new HashMap<>();
        params.put("a","aaaa");
        params.put("b","bbbb");
        params.put("c","cccc");
        String expecStr = "${}}{${aaaa},我们看bbbbcccc好cccc${d}你}${bbbb";
        StringTemplateHelper helper = new StringTemplateHelper(content,CollectionUtils.createSet("a","b","c"));
        Assert.assertTrue(expecStr.equals(helper.generate(params)));
    }


    /**
     * 简单测试下性能 8线程8000w = 16.3s
     * 1kw = 2s;
     */
    @Test
    public void generatorSpeedTest(){
        String content = "${}}{${${a}},我们看${b}${c}好${c}${d}你}${${b}";
        Map<String,String> params = new HashMap<>();
        params.put("a","aaaa");
        params.put("b","bbbb");
        params.put("c","cccc");
        String expecStr = "${}}{${aaaa},我们看bbbbcccc好cccc${d}你}${bbbb";
        StringTemplateHelper helper = new StringTemplateHelper(content,CollectionUtils.createSet("a","b","c"));
        TestUtils.printTime(8,10000,(thread,index) -> {
            helper.generate(params);
        });
    }

    @Test
    public void parseParam() {
        String startFlag = "${";
        String endFlag = "}";
        String content = "亲,${a}你的花费余额为${b}元,月份${c}${d}元";
        Set<String> params = StringTemplateHelper.parseParam(startFlag, endFlag, content);
        System.out.println(JSON.toJSONString(params));
        Assert.assertTrue(CollectionUtils.eq(Arrays.asList("a","b","c","d").stream().collect(Collectors.toSet()), params));

        content = "${d}${a}你好,顾客${a}你的花费余额为${${b}}元,月份${{c}元";
        params = StringTemplateHelper.parseParam(startFlag, endFlag, content);
        System.out.println(JSON.toJSONString(params));
        Assert.assertTrue(CollectionUtils.eq(Arrays.asList("a","d","{c","${b").stream().collect(Collectors.toSet()), params));

        content = "${}}{${${a}},我们看${b}${c}好${c}${d}你}${${b}";
        params = StringTemplateHelper.parseParam(startFlag, endFlag, content);
        System.out.println(JSON.toJSONString(params));
        Assert.assertTrue(CollectionUtils.eq(Arrays.asList("b","c","d","${a","${b").stream().collect(Collectors.toSet()), params));
    }
}
