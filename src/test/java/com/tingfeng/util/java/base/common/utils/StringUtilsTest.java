package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.utils.string.StringUtils;
import org.junit.Test;

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
           List<Integer> strList = Stream.generate(() -> ThreadLocalRandom.current().nextInt(5))
                            .limit(900000).collect(Collectors.toList());
           String str = CollectionUtils.join(strList,"");
           String subStr = str.substring(860000,860100);
           TestUtils.printTime(1,10,index ->{
               int position = str.indexOf(subStr);
               if(index == 0){
                   System.out.println(position);
               }
           });
        int[] next = StringUtils.getKmpNextArray(StringUtils.getCharArray(subStr));
        TestUtils.printTime(1,10,index ->{
            int position = StringUtils.indexOfByKMP(str,subStr,next);
            if(index == 0){
                System.out.println(position);
            }
        });
    }
}
