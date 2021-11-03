package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.utils.string.StringUtils;

/**
 * 利用hash映射生成的加解密工具
 * hash涉及的字符暂且内置, 线程安全
 * @author huitoukest
 */
public class HashEncryptionHelper {
    /**
     * 64个基础字符
     */
    private static final char[] BASE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_-".toCharArray();
    /**
     * 索引 = BASE_CHARS中字符的ASCII码 , 值 = BASE_CHARS 中字符对应的索引
     */
    private static final int[] BASE_CHARS_DICTIONARY = new int[256];
    static {
        for (int i = 0; i < BASE_CHARS.length; i++) {
            BASE_CHARS_DICTIONARY[BASE_CHARS[i]] = i;
        }
    }
    /**
     * 映射之后的字符串, hashedChars 和 BASE_CHARS 索引值相同的一一映射.
     */
    private char[] hashedChars = null;

    /**
     * 索引 = hashedChars中字符的ASCII码 , 值 = hashedChars 中字符对应的索引
     */
    private final int[] hashedCharsDictionary = new int[256];

    /**
     * 映射加盐的字符串
     */
    private String salt;
    /**
     * hash映射时原始值根据位置来做的值映射的字典
     */
    private int[] hashedPositionValueOffsetDictionary;

    public HashEncryptionHelper(String salt){
        if(StringUtils.isEmpty(salt)){
            salt = "";
        }
        this.salt = salt + salt.length();
        hashChars();
    }

    /**
     * 位置映射的初始化，
     * @param length
     */
    public void hashPositionDictionary(int length){
        assert length > 0;
        hashedPositionValueOffsetDictionary = new int[length];
        int saltLength = this.salt.length();
        long seedValue = saltLength;
        char[] saltArr = salt.toCharArray();
        for (int i = 0; i < saltArr.length; i++) {
            int v = ((int) saltArr[i]);
            seedValue +=  v * 13;
        }
        seedValue = Math.abs(seedValue);
        for (int i = 0; i < hashedPositionValueOffsetDictionary.length; i++) {
            long modValue = saltLength > i ? seedValue + saltArr[i] : seedValue + saltArr[i % saltLength] + i;
            int offsetValue = (int) (modValue % BASE_CHARS.length);
            hashedPositionValueOffsetDictionary[i] = offsetValue;
        }
    }

    /**
     * 根据salt,将初始数据打乱,做映射.
     */
    private void hashChars(){
        long seedValue = 0;
        char[] saltArr = salt.toCharArray();
        for (int i = 0; i < saltArr.length; i++) {
            int v = ((int) saltArr[i]);
            seedValue +=  v * 31;
        }
        seedValue += saltArr.length;
        seedValue = Math.abs(seedValue);

        hashedChars = new char[BASE_CHARS.length];
        System.arraycopy(BASE_CHARS,0,hashedChars,0, BASE_CHARS.length);

        //洗牌打乱顺序
        for (int i = 0; i < BASE_CHARS.length; i++) {
            long modValue = saltArr.length > i ? seedValue + saltArr[i] : seedValue + i;
            int exChangeIdx = (int) (modValue % BASE_CHARS.length);
            char tmp = hashedChars[i];
            hashedChars[i] = hashedChars[exChangeIdx];
            hashedChars[exChangeIdx] = tmp;
        }
        for (int i = 0; i < hashedChars.length; i++) {
            hashedCharsDictionary[hashedChars[i]] = i;
        }
    }

    /**
     * 将原始字符串做编码
     * @param str 需要编码的字符串
     * @return 编码后的字符串
     */
    public String encode(String str){
        return encode(str,false);
    }

    /**
     * 将原始字符串做解码
     * @param str 需要解码的字符串
     * @return 解码后的字符串
     */
    public String decode(String str){
        return decode(str,false);
    }

    /**
     * 将原始字符串做编码
     * @param str 需要编码的字符串
     * @return 编码后的字符串
     */
    public String encode(String str,boolean withPositionEncode){
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int index = BASE_CHARS_DICTIONARY[chars[i]];
            if(withPositionEncode) {
                int offsetValue = hashedPositionValueOffsetDictionary[i % hashedPositionValueOffsetDictionary.length];
                index = index + offsetValue;
                index = index % BASE_CHARS.length;
            }
            chars[i] = hashedChars[index];
        }
        return new String(chars);
    }

    /**
     * 将原始字符串做解码
     * @param str 需要解码的字符串
     * @return 解码后的字符串
     */
    public String decode(String str,boolean withPositionDecode){
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int index = hashedCharsDictionary[chars[i]];
            if(withPositionDecode) {
                int offsetValue = hashedPositionValueOffsetDictionary[i % hashedPositionValueOffsetDictionary.length];
                index = index - offsetValue;
                if (index < 0) {
                    index = index % BASE_CHARS.length + BASE_CHARS.length;
                }
            }
            chars[i] = BASE_CHARS[index];
        }
        return new String(chars);
    }

}
