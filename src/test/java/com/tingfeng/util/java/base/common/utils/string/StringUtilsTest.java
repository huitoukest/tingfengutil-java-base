package com.tingfeng.util.java.base.common.utils.string;

import com.tingfeng.util.java.base.common.utils.CollectionUtils;
import com.tingfeng.util.java.base.common.utils.TestUtils;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtilsTest {

    @Test
    public void trimSymbolTest(){
        System.out.print(StringUtils.trimSymbol(",2,3,",","));
    }

    @Test
    public void testAppend(){
        System.out.println(StringUtils.appendValue(false,null));
        System.out.println(StringUtils.appendValue(true,null));
        System.out.println(StringUtils.appendValue(false,new Object[]{null,null,"123123"}));

        System.out.println(StringUtils.appendValue(true,new Object[]{null,"123123",null}));
    }


    @Test
    public void testKMPIndexOf(){
           //String str = RandomUtils.randomLetterString(5000);
           //String str = RandomUtils.randomNumberString(5000);
           List<Integer> strList = Stream.generate(() -> ThreadLocalRandom.current().nextInt(2))
                            .limit(900000).collect(Collectors.toList());
           String str = CollectionUtils.join(strList,"");
           String subStr = str.substring(860000,860100);
            int[] next = StringUtils.getKmpNextArray(StringUtils.getCharArray(subStr));
           StringUtils.indexOfByKMP(str,subStr,next);
           System.out.println("start ...");
           TestUtils.printTime(10,1, index ->{
               int position = str.indexOf(subStr);
               if(index == 0){
                   System.out.println(position);
               }
           });
        TestUtils.printTime(10,1,index ->{
            int position = StringUtils.indexOfByKMP(str,subStr,next);
            if(index == 0){
                System.out.println(position);
            }
        });
    }

    @Test
    public void indexOfTest(){
        String a = "abbccddeeff";
        String target = "cc";
        Assert.assertEquals(-1,StringUtils.indexOf(a,target,1,3));
        Assert.assertEquals(3,StringUtils.indexOf(a,target,1,4));
        Assert.assertEquals(3,StringUtils.indexOf(a,target,1,5));
    }

    @Test
    public void lastIndexOfTest(){
        String a = "abbccaccddeeff";
        String target = "cc";
        Assert.assertEquals(-1,StringUtils.lastIndexOf(a,target,1,3));
        Assert.assertEquals(3,StringUtils.lastIndexOf(a,target,1,6));
        Assert.assertEquals(6,StringUtils.lastIndexOf(a,target,1,7));
        Assert.assertEquals(6,StringUtils.lastIndexOf(a,target,6,7));
        Assert.assertEquals(6,StringUtils.lastIndexOf(a,target,4,9));
        Assert.assertEquals(-1,StringUtils.lastIndexOf(a,target,7,10));
    }

    @Test
    public void testSplitByStr() {
        Assert.assertEquals(Collections.emptyList(),StringUtils.splitByStr(null,"a"));
        Assert.assertEquals(Collections.emptyList(),StringUtils.splitByStr("a,b,c",",",0));
        Assert.assertEquals(Collections.emptyList(),StringUtils.splitByStr("a,b,c",",",-1));
        Assert.assertEquals("a",StringUtils.splitByStr("a,b,c",",",1).stream().collect(Collectors.joining("@")));
        Assert.assertEquals("a",StringUtils.splitByStr("a,",",",1).stream().collect(Collectors.joining("@")));
        Assert.assertEquals("a@",StringUtils.splitByStr("a,",",",2).stream().collect(Collectors.joining("@")));
        Assert.assertEquals("a",StringUtils.splitByStr("a",",",1).stream().collect(Collectors.joining("@")));
        Assert.assertEquals("a",StringUtils.splitByStr("a",",",2).stream().collect(Collectors.joining("@")));
        Assert.assertEquals("a",StringUtils.splitByStr("a",",",3).stream().collect(Collectors.joining("@")));
        Assert.assertEquals("a@b@c@d",StringUtils.splitByStr("a,b,c,d",",",4).stream().collect(Collectors.joining("@")));
        Assert.assertEquals("a@b@c@d@e@@",StringUtils.splitByStr("a,b,c,d,e,,",",").stream().collect(Collectors.joining("@")));
        Assert.assertEquals("a@b@c@d@e@",StringUtils.splitByStr("a,b,c,d,e,",",").stream().collect(Collectors.joining("@")));
    }
}
