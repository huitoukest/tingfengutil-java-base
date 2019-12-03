package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.inter.IEnum;
import org.junit.Assert;
import org.junit.Test;

public class EnumUtilsTest {

    enum Name implements IEnum<String>{
        A("A"),
        B("B"),
        C("C");

        private String value;

        Name(String value){
            this.value = value;
        }

        @Override
        public String getValue() {
            return this.value;
        }
    }

    @Test
    public void getEnumByValueTest(){
        Assert.assertTrue(Name.A.equals(EnumUtils.getEnumByValue(Name.class,"A")));
    }

}
