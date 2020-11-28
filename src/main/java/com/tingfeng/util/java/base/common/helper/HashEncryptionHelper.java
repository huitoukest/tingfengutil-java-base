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

    public HashEncryptionHelper(String salt){
        if(StringUtils.isEmpty(salt)){
            salt = "";
        }
        this.salt = salt + salt.length();
        hashChars();
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
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int index = BASE_CHARS_DICTIONARY[chars[i]];
            chars[i] = hashedChars[index];
        }
        return new String(chars);
    }

    /**
     * 将原始字符串做解码
     * @param str 需要解码的字符串
     * @return 解码后的字符串
     */
    public String decode(String str){
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int index = hashedCharsDictionary[chars[i]];
            chars[i] = BASE_CHARS[index];
        }
        return new String(chars);
    }

}
