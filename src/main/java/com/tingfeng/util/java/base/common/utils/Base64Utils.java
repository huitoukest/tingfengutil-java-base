package com.tingfeng.util.java.base.common.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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

    public static byte[] deCode(byte content[]){
        return Base64.getDecoder().decode(content);
    }

    public static byte[] deCode(String content){
        return deCode(content.getBytes(Charset.forName("utf-8")));
    }

    public static String deCodeToString(String content){
        return new String(deCode(content),Charset.forName("utf-8"));
    }

    /**
     * base64 字符串加密为geturl安全的字符串
     * @param base64String
     * @return
     */
    public static String enCodeBase64ToSafeUrl(String base64String){
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
    public static String decodeSafUrlToBase64(String safeBase64UrlStr){
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
