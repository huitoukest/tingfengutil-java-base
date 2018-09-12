package com.tingfeng.util.java.base.common.constant;

/**
 * 产生随机数的一些类型
 */
public enum RandomType {
    number("0123456789"),
    upperChar("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    lowerChar("abcdefghijklmnopqrstuvwxyz"),
    symbol("!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~");//一些常见的符号
    private char[] value;
    private String strValue;
    RandomType(String str){
        this.value = str.toCharArray();
        this.strValue = str;
    }
    public char[] getValue(){
        return this.value;
    }

    public String getStrValue() {
        return this.strValue;
    }
}
