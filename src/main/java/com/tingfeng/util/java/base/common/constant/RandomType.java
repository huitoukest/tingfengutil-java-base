package com.tingfeng.util.java.base.common.constant;

/**
 * 产生随机数的一些类型
 * @author huitoukest
 */
public enum RandomType {
    NUMBER("0123456789"),
    UPPER_CHAR("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    LOWER_CHAR("abcdefghijklmnopqrstuvwxyz"),
    /**
     * 一些常见的符号
     */
    SYMBOL("!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~");
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
