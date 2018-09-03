package com.tingfeng.util.java.base.common.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Base64;

/**
 * base64的工具类，使用jdk自带的base64工具
 */
public class Base64Utils {

    public static String enCode(String content, Charset charset){
        return enCode(content.getBytes(charset));
    }

    public static String enCode(String content){
        return enCode(content.getBytes(Charset.forName("utf-8")));
    }

    public static String enCode(byte[] content){
        return Base64.getEncoder().encodeToString(content);
    }

    public static ByteBuffer enCodeToByteBuffer(ByteBuffer buffer){
        return Base64.getEncoder().encode(buffer);
    }

    public static byte[] enCodeToByte(byte[] content){
        return Base64.getEncoder().encode(content);
    }

    public static byte[] deCode(byte content[]){
        return Base64.getDecoder().decode(content);
    }

    public static byte[] deCode(String content){
        return deCode(content.getBytes(Charset.forName("utf-8")));
    }

    public static String deCodeToString(String content){
        return new String(deCode(content),Charset.forName("utf-8"));
    }

}
