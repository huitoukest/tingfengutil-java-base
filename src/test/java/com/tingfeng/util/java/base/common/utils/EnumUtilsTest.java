package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.inter.IEnum;
import org.junit.Assert;
import org.junit.Test;

public class EnumUtilsTest {

    enum Name implements IEnum<String>{
        A("A","123"),
        B("B","456"),
        C("C","789");

        private String value;
        private String code;

        Name(String value,String code){
            this.value = value;
            this.code = code;
        }

        @Override
        public String getValue() {
            return this.value;
        }

        public String getCode(){
            return this.code;
        }
    }

    @Test
    public void getEnumByValueTest(){
        Assert.assertTrue(Name.B.equals(EnumUtils.getEnum(Name.class,"456",Name::getCode)));
        Assert.assertTrue(Name.A.equals(EnumUtils.getEnumByValue(Name.class,"A")));
        Assert.assertTrue(Name.A.equals(EnumUtils.getEnum(Name.class,"123",Name::getCode)));
        Assert.assertTrue(Name.C.equals(EnumUtils.getEnumByValue(Name.class,"C")));
    }

}
