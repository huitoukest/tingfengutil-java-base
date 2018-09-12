package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.utils.string.StringUtils;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void trimSymbolTest(){
        System.out.print(StringUtils.trimSymbol(",2,3,",","));
    }

    @Test
    public void testAppend(){
        System.out.println(StringUtils.append(false,null));
        System.out.println(StringUtils.append(true,null));
        System.out.println(StringUtils.append(false,null,null,"123123"));

        System.out.println(StringUtils.append(true,null,"123123",null));
    }
}
