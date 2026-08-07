package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.array.ArrayUtils;
import com.tingfeng.util.java.base.common.constant.Constants;
import com.tingfeng.util.java.base.lang.base.TrieNode;
import com.tingfeng.util.java.base.lang.exception.BaseException;
import com.tingfeng.util.java.base.lang.support.ReflectUtils;
import com.tingfeng.util.java.base.text.StringTemplateHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 字符串命名转换、编码转换、大小写转换与内容变换操作实现。
 *
 * 包级私有，不对外暴露。通过 {@link StringUtils} 对外提供统一 API。
 */
class StringConvertOps {

    /**
     * 通过反射获取的 String 内部 char[] 字段，仅在 Java8 及之前可用
     */
    private static Field STRING_VALUE_FIELD = null;

    private final static int BUFFER_SIZE = 4096;

    static {
        //只在java8以及之前可用
        try {
            STRING_VALUE_FIELD = ReflectUtils.getField(String.class, "value", true);
            Class<?> type = STRING_VALUE_FIELD.getType();
            if (!type.isArray()){
                STRING_VALUE_FIELD = null;
            }else {
                Class<?> componentType = type.getComponentType();
                if (componentType != char.class) {
                    STRING_VALUE_FIELD = null;
                }
            }
        }catch (Exception e){
            //ignore
        }
    }

    private StringConvertOps() {

    }

    /**
     * 将InputStream转换成String
     *
     * @param in InputStream
     * @return String
     */
    static String getStringByStream(InputStream in) {
        return getStringByStream(in, Constants.CharSet.UTF8);
    }

    /**
     * 将InputStream转换成某种字符编码的String
     *
     * @param in
     * @param encoding
     * @return
     */
    static String getStringByStream(InputStream in, String encoding) {
        String string = null;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        try {
            while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
                outStream.write(data, 0, count);
            }
        } catch (IOException e) {
            throw new BaseException(e);
        }
        data = null; //显示数据回收
        try {
            string = new String(outStream.toByteArray(), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new BaseException(e);
        }
        return string;
    }

    /**
     * 将byte数组转换成String
     *
     * @param in
     * @param charEncode
     * @return
     */
    static String getStringByBytes(byte[] in, String charEncode) throws UnsupportedEncodingException {
        String string = new String(in, charEncode);
        return string;
    }

    static String getStringByBytes(byte[] in) throws UnsupportedEncodingException {
        return getStringByBytes(in, "UTF-8");
    }

    /**
     * 首字母小写
     *
     * @param srcString
     * @return
     */
    static String toLowerFirstChar(String srcString) {
        if (srcString == null || srcString.isEmpty()) {
            return srcString;
        }
        return StringUtils.doAppend((sb) -> {
            sb.append(Character.toLowerCase(srcString.charAt(0)));
            if (srcString.length() > 1) {
                sb.append(srcString.substring(1));
            }
            return sb.toString();
        });
    }

    /**
     * 首字母大写，null/空串原样返回
     *
     * @param rawString
     * @return
     */
    static String toUpperFirstChar(String rawString) {
        if (rawString == null || rawString.isEmpty()) {
            return rawString;
        }
        String beforeChar = rawString.substring(0, 1).toUpperCase();
        String afterChar = rawString.substring(1, rawString.length());
        return beforeChar + afterChar;
    }

    /**
     * 将rawString转换为小写并且替换index分隔第二种字符串的第一部分
     *
     * @param rawString
     * @param index
     * @return
     */
    static String toLowerByPrefix(String rawString, int index) {
        String beforeChar = rawString.substring(0, index).toLowerCase();
        String afterChar = rawString.substring(index, rawString.length());
        return beforeChar + afterChar;
    }

    static String removePrefixAfterPrefixToLower(String rawString, int index) {
        return toLowerByPrefix(rawString.substring(index, rawString.length()), 1);
    }

    /**
     * 驼峰风格字符串转为下划线连接的小写字符串
     *
     * @param param
     * @return
     */
    static String camelToUnderline(String param) {
        if (StringUtils.isEmpty(param)) {
            return "";
        } else {
            int len = param.length();
            return StringUtils.doAppend(sb -> {
                for (int i = 0; i < len; ++i) {
                    char c = param.charAt(i);
                    if (Character.isUpperCase(c) && i > 0) {//如果是大写则加入下划线，并且转为小写字符
                        sb.append('_');
                    }
                    sb.append(Character.toLowerCase(c));
                }

                return sb.toString();
            });

        }
    }

    /**
     * 下划线风格的字符串转为驼峰原则
     * 95代表下划线_
     *
     * @param param
     * @return
     */
    static String underlineToCamel(String param) {
        if (StringUtils.isEmpty(param)) {
            return "";
        } else {
            String temp = param.toLowerCase();
            int len = temp.length();
            return StringUtils.doAppend(sb -> {
                for (int i = 0; i < len; ++i) {
                    char c = temp.charAt(i);
                    if (c == 95) {
                        ++i;
                        if (i < len) {
                            sb.append(Character.toUpperCase(temp.charAt(i)));
                        }
                    } else {
                        sb.append(c);
                    }
                }
                return sb.toString();
            });
        }
    }

    /**
     * 解析前台encodeURIComponent编码后的参数
     *
     * @param url      前端用urldecoder，编码后的url
     * @param encoding 编码方式，如"UTF-8"
     * @return
     */
    static String toDecodeStringUrl(String url, String encoding) {
        String trem = "";
        if (StringUtils.isNotEmpty(url)) {
            try {
                trem = URLDecoder.decode(url, encoding);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return trem;
    }

    /**
     * 转换编码
     *
     * @param s              源字符串
     * @param sourceEncoding 源编码格式
     * @param targetEncoding 目标编码格式
     * @return 目标编码
     */
    static String getStringByChangCoding(String s, String sourceEncoding, String targetEncoding) {
        String str;
        try {
            if (StringUtils.isNotEmpty(s)) {
                str = new String(s.getBytes(sourceEncoding), targetEncoding);
            } else {
                str = "";
            }
            return str;
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    /**
     * 用要通过URL传输的内容进行编码
     *
     * @param src 源字符串
     * @return 经过编码的内容
     */
    static String encodeURL(String src, String encoding) {
        String return_value = "";
        try {
            if (src != null) {
                return_value = URLEncoder.encode(src, encoding);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return_value = src;
        }
        return return_value;
    }

    /**
     * 全角括号转为半角
     *
     * @param str
     * @return
     */
    static String replaceBracketStr(String str) {
        if (str != null && str.length() > 0) {
            str = str.replaceAll("（", "(");
            str = str.replaceAll("）", ")");
        }
        return str;
    }

    /**
     * 返回当前str的char[]数组，而不是创建一个新的char[]
     *
     * @param str 字符串
     * @return 字符数组
     */
    static char[] getCharArray(String str) {
        if (str == null) {
            return null;
        }
        try {
            if (STRING_VALUE_FIELD == null) {
                return str.toCharArray();
            }
            return (char[]) STRING_VALUE_FIELD.get(str);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static String reverse(Object str) {
        char[] chars = str.toString().toCharArray();
        ArrayUtils.reverse(chars);
        return new String(chars);
    }

    /**
     * 首字母小写（与 toLowerFirstChar 功能一致，保留签名委托）
     *
     * @param srcString
     * @return
     */
    static String firstLetterToLower(String srcString) {
        return toLowerFirstChar(srcString);
    }

    /**
     * 一次性的模板替换，重复且多次的替换，请使用@See com.tingfeng.util.java.base.text.StringTemplateHelper
     *
     * @param startFlag 替换开始的标记,empty String = 使用默认值${
     * @param endFlag 替换结束的标记,empty String = 使用默认值}
     * @param content 模板内容
     * @param params  模板中的参数有哪些，参数无需包含开始结束标记
     * @param paramsData 模板参数对应的值
     * @return
     */
    static String replaceByTemplate(String startFlag, String endFlag, String content, Set<String> params, Map<String, String> paramsData) {
        if (paramsData == null) {
            return content;
        }
        StringTemplateHelper helper = new StringTemplateHelper(startFlag, endFlag, content, params);
        return helper.generate(paramsData);
    }

    /**
     * 一次性的模板替换，重复且多次的替换，请使用@See com.tingfeng.util.java.base.text.StringTemplateHelper
     * 默认替换${A}中A的值，默认paramsData中的keySet作为可用的模板参数
     *
     * @param content 模板内容
     * @param paramsData 模板参数对应的值
     * @return
     */
    static String replaceByTemplate(String content, Map<String, String> paramsData) {
        if (paramsData == null) {
            return content;
        }
        Set<String> params = paramsData.keySet();
        StringTemplateHelper helper = new StringTemplateHelper(null, null, content, params);
        return helper.generate(paramsData);
    }

    /**
     * 反转义字符串
     *
     * @param str 字符串
     * @return 反转后的义字符串
     */
    static String unescape(String str) {
        if (str == null) {
            return null;
        }
        boolean unescape = false;
        if (str.startsWith("\"") && str.endsWith("\"")) {
            unescape = true;
        }
        if (str.startsWith("\'") && str.endsWith("\'")) {
            unescape = true;
        }
        if (unescape && str.length() > 1) {
            str = str.substring(1, str.length() - 1);
            str = str.replaceAll("(\\\\(?!\\\\)(.))", "$2")
                    .replaceAll("\\\\\\\\", "\\\\");
        }
        return str;
    }

    /**
     * 构建签字字典树
     *
     * @param collections
     * @return
     */
    static TrieNode build(Collection<String> collections) {
        if (collections == null) {
            return new TrieNode();
        }
        TrieNode trieNode = new TrieNode();
        for (String collection : collections) {
            trieNode.insert(collection);
        }
        return trieNode;
    }
}
