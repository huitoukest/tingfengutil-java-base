package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.constant.Constants;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.IntStream;

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

    /**
     * 自动判断是否urlSafe 的base64字符串，并解码
     * @param content
     * @return 原始字符串
     */
    public static byte[] deCode(byte[] content){
        return autoDecodeBase64String(content);
    }
    /**
     * 自动判断是否urlSafe 的base64字符串，并解码
     * @param content
     * @return 原始字符串
     */
    public static byte[] deCode(String content){
        return deCode(content.getBytes(Charset.forName("utf-8")));
    }
    /**
     * 自动判断是否urlSafe 的base64字符串，并解码
     * @param content
     * @return 原始字符串
     */
    public static String deCodeToString(String content){
        return new String(deCode(content),Charset.forName("utf-8"));
    }

    /**
     * 原始字符串编码为 base64SafeUrl字符串，并编码
     * @param bytes
     * @return base64SafeUrl 字符串
     */
    public static String enCodeBase64UrlSafeString(byte[] bytes){
        if(null == bytes){
            return null;
        }
        if(bytes.length == 0){
            return  "";
        }
        byte[] base64Bytes = Base64.getUrlEncoder().encode(bytes);
        //把等号全部替换为空字符串，解码的时候再自行恢复
        int end = base64Bytes.length - 1;
        for(int  i = end ; i >= 0 ; i --){
            if(base64Bytes[i] != '='){
                end = i;
                break;
            }
        }
        return new String(base64Bytes,0,end + 1);
    }

    /**
     * 原始字符串编码为 base64SafeUrl字符串，并编码
     * @param originString
     * @return base64SafeUrl 字符串
     */
    public static String enCodeBase64UrlSafeString(String originString){
        return new String(enCodeBase64UrlSafeString(originString.getBytes(Charset.forName("utf-8"))));
    }

    /**
     * 自动判断是否urlSafe 的base64字符串，并解码
     * @param base64UrlSafeString
     * @return 原始字符串
     */
    public static String deCodeBase64UrlSafeString(String base64UrlSafeString){
        return deCodeBase64UrlSafeString(base64UrlSafeString.getBytes(Charset.forName("utf-8")));
    }


    public static String deCodeBase64UrlSafeString(byte[] bytes){
        return new String(autoDecodeBase64String(bytes));
    }

    /**
     * 通过自动判断字符，来决定使用urlSafe还是普通的base64解码
     * @param bytes
     * @return
     */
    private static byte[] autoDecodeBase64String(byte[] bytes){
        if(bytes == null){
            return null;
        }
        int mod4 = bytes.length % 4;
        boolean useSafeUrlDecoder = mod4 > 0;
        if(useSafeUrlDecoder){
            int originLength = bytes.length;
            bytes = Arrays.copyOf(bytes,originLength + 4 - mod4);
            for(int i = originLength ; i < bytes.length ; i ++){
                bytes[i] = '=';
            }
        }
        if(!useSafeUrlDecoder) {
            for (int i = 0; i < bytes.length; i++) {
                byte value = bytes[i];
                if (value == '_' || value == '-') {
                    useSafeUrlDecoder = true;
                    break;
                }
            }
        }
        if(useSafeUrlDecoder){
            return Base64.getUrlDecoder().decode(bytes);
        }else{
            return Base64.getDecoder().decode(bytes);
        }
    }

    /**
     * base64 字符串加密为geturl安全的字符串
     * @param base64String
     * @return
     */
    public static String encodeBase64ToBase64UrlSafeString(String base64String){
        StringBuilder sb = new StringBuilder((int) (base64String.length() * 1.5));
        char[] chars = base64String.toCharArray();
        IntStream.range(0,chars.length).forEach(it ->{
            char c = chars[it];
            switch (c){
                case '+' : sb.append('-');
                    break;
                case '/' : sb.append('_');
                    break;
                case '=' :
                    break;
                default:
                    sb.append(c);
            }
        });
        return sb.toString();
    }

    /**
     * 解密安全的base64 url 字符串为普通的base64字符串
     * @param safeBase64UrlStr
     * @return
     */
    public static String decodeBase64SafUrlStringToBase64(String safeBase64UrlStr){
        StringBuilder sb = new StringBuilder((int) (safeBase64UrlStr.length() * 1.5));
        char[] chars = safeBase64UrlStr.toCharArray();
        IntStream.range(0,chars.length).forEach(it ->{
            char c = chars[it];
            switch (c){
                case '-' : sb.append('+');
                    break;
                case '_' : sb.append('/');
                    break;
                default:
                    sb.append(c);
            }
        });
        int mod4 = safeBase64UrlStr.length() % 4;
        if( mod4 > 0){
            sb.append("====".substring(mod4));
        }
        return sb.toString();
    }
}
