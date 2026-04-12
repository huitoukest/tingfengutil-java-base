package com.tingfeng.util.java.base.common.constant;

/**
 * 产生随机数的一些类型
 * @author huitoukest
 */
public enum RandomType {
    number("0123456789"),
    upperChar("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    lowerChar("abcdefghijklmnopqrstuvwxyz"),
    /**
     * 一些常见的符号
     */
    symbol("!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~");
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
