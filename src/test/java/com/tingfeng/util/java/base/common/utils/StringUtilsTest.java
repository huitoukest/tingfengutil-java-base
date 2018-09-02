package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.utils.string.StringUtils;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void trimSymbolTest(){
        System.out.print(StringUtils.trimSymbol(",2,3,",","));
    }
}
