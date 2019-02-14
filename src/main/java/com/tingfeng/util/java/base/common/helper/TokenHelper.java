package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.exception.InfoException;
import com.tingfeng.util.java.base.common.utils.Base64Utils;
import com.tingfeng.util.java.base.common.utils.MessageDigestUtils;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 一个自定义简单token工具类；
 * 工具的内容数据用,分隔；内容和签名用.分隔，如果内容中包含,号，则会自动转义。
 * 内容按照顺序保存和解析*/
public class TokenHelper {
    private static final Logger logger = LoggerFactory.getLogger(TokenHelper.class);
    private static final char CHAR_COMMA = ',';
    private static final String STR_DOT = ".";
    private static final String STR_SPLIT_DOT = "\\.";
    private static final char CHAR_SLASH = '\\';
    private static final int DEFAULT_MAX_SB_SIZE = 16;
    /**
     * StringBuilder默认的初始长度，如果不设置会导致StringBuilder在内存中一直保持最大长度
     */
    private static final int DEFAULT_MAX_SB_LENGTH = 128;
    private static final FixedPoolHelper<StringBuilder> tokenStringBuilderPool = new FixedPoolHelper<>(DEFAULT_MAX_SB_SIZE,()->new StringBuilder(),(sb)->{
        int len = sb.length();
        if(len > DEFAULT_MAX_SB_LENGTH){
            sb.delete(DEFAULT_MAX_SB_LENGTH,len);
        }
        sb.setLength(0);
    });
    private static final FixedPoolHelper<StringBuilder> escapeStringBuilderPool = new FixedPoolHelper<>(DEFAULT_MAX_SB_SIZE,()->new StringBuilder(),(sb)->{
        int len = sb.length();
        if(len > DEFAULT_MAX_SB_LENGTH){
            sb.delete(DEFAULT_MAX_SB_LENGTH,len);
        }
        sb.setLength(0);
    });
    private  Function<String,byte[]> encryptAction = null;
    private  Integer contentLength = null;
    private  RuntimeException emptyTokenException = null;
    private  RuntimeException errorTokenException = null;
    private  RuntimeException signErrorException = null;

    public TokenHelper(){
        this(MessageDigestUtils.SHAType.SHA256);
    }

    public TokenHelper(MessageDigestUtils.SHAType shaType){
           this(shaType.getValue());
    }
    /**
     * @param algorithmName 加密的数据格式，支持SHA-512,SHA-256,SHA-1,MD5
     */
    public TokenHelper(String algorithmName){
        this.encryptAction = (str) -> MessageDigestUtils.hash(algorithmName, str,null);
    }

    public TokenHelper(Function<String,byte[]> encryptAction){
        if(encryptAction != null){
            this.encryptAction = encryptAction;
        }else{
            this.encryptAction = (str) -> MessageDigestUtils.SHA(MessageDigestUtils.SHAType.SHA256, str.getBytes(Charset.forName("utf-8")));
        }
    }

    /**
     *token = 用户Id+过期时间+类型+签名
     *内容实用三个点号隔开的方式,分别是用户Id，过期时间和类型，三个数字
     * @param contentList 内容的List，要求有序
     * @param securityKey 安全验证码,通过secretPrefix和securityKey用来对内容签名
     * @return
     */
    public String getToken(ArrayList<String> contentList,String  securityKey){
        return tokenStringBuilderPool.run((tokenStringBuilder) -> {
            for (int i = 0; i < contentList.size(); i++) {
                if (i > 0) {
                    tokenStringBuilder.append(",");
                }
                tokenStringBuilder.append(escapeContent(contentList.get(i)));
            }
            byte[] sign = this.encryptAction.apply(tokenStringBuilder.toString() + securityKey);
            String signStr = Base64Utils.enCode(sign);
            String content = Base64Utils.enCode(tokenStringBuilder.toString());

            tokenStringBuilder.setLength(0);
            tokenStringBuilder.append(content);
            tokenStringBuilder.append('.');
            tokenStringBuilder.append(signStr);
            return tokenStringBuilder.toString();
        });
    }

    /**
     * 对单个内容加密,如果内容中包含,号，则会自动转义。
     * @param content
     * @return
     */
    private String escapeContent(String content){
        return escapeStringBuilderPool.run(escapeStringBuilder->{
            char[] chars = content.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == CHAR_COMMA || chars[i] == CHAR_SLASH) {
                    escapeStringBuilder.append(CHAR_SLASH);
                }
                escapeStringBuilder.append(chars[i]);
            }
            return escapeStringBuilder.toString();
        });
    }

    /**
     * 对整个内容解密为List<Object> Object可以是数字或者字符串,有序List
     * 解析的时候必须都是从前往后顺序解析。
     * @param content
     * @return
     */
    private List<String> parseContent(String content){
        if(null == content){
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        char[] chars = content.toCharArray();
        escapeStringBuilderPool.run(escapeStringBuilder ->{
            for (int i = 0; i < chars.length; i++) {
                if(chars[i] == CHAR_SLASH && i + 1 < chars.length ){
                    if(chars[i + 1] == CHAR_SLASH){//如果是两个\\，则表示这可能是，的转意；
                        escapeStringBuilder.append(CHAR_SLASH);
                    }else if(chars[i + 1] == CHAR_COMMA){
                        escapeStringBuilder.append(CHAR_COMMA);
                    }
                    i = i + 1;
                    if(i == chars.length - 1){
                        list.add(escapeStringBuilder.toString());
                    }
                }else if (i == 0) {
                    if (chars[i] != CHAR_SLASH && chars[i] == CHAR_COMMA) {//判断以，开头的情况
                        list.add(escapeStringBuilder.toString());
                        escapeStringBuilder.setLength(0);
                    } else {
                        escapeStringBuilder.append(chars[i]);
                    }
                }else if (chars[i] == CHAR_COMMA) {
                    list.add(escapeStringBuilder.toString());
                    escapeStringBuilder.setLength(0);
                    if(i + 1 == chars.length){//判断以，结尾的情况
                        list.add("");
                    }
                } else {
                    escapeStringBuilder.append(chars[i]);
                    if(i == chars.length - 1){//如果是最后一个字母
                        list.add(escapeStringBuilder.toString());
                        escapeStringBuilder.setLength(0);
                    }
                }
            }
            return "";
        });
        return list;
    }

    /**
     * 检查token的基本类容和格式
     * @param token
     * @return 返回内容和签名两部分组成的数组
     * */
    private String[] tokenBaseParseAndCheck(String token){
        if (StringUtils.isEmpty(token)) {
            if(this.emptyTokenException != null){
                throw  emptyTokenException;
            }else {
                throw new InfoException("token 不能为空！");
            }
        }
        String[] strArray = token.split(STR_SPLIT_DOT);
        if(contentLength != null && contentLength != strArray.length){
            if(this.errorTokenException != null){
                throw errorTokenException;
            }else{
                throw new InfoException("token 错误！");
            }
        }
        return strArray;
    }

    /**
     * 如果是get方式传入的token，其中url中的“ + ”字符会被转换为空格，需要前端手动将+字符转意为%2B
     * 这样后端服务器接收到之后会自动转为“ + ”。
     * 解密出UserId，如果有问题会直接抛出异常
     * @param token
     * @param securityKey 传入解析的内容List<String>，返回securityKey ，自动校验签名，如果获取securityKey为空着不见好擦
     * @param checkParseContent 传入解析后的内容List<String>，检查内容，如果有问题直接抛出异常，否则转换为需要的类型并且返回结果
     * @param <T>
     * @return
     */
    public <T> T parseToken(String token,Function<List<String>,String>  securityKey,Function<List<String>,T> checkParseContent){
        T t = null;
        if(null != checkParseContent) {
            String[] strArray = tokenBaseParseAndCheck(token);
            String sign = strArray[1];//数据校验
            String content = Base64Utils.deCodeToString(strArray[0]);
            List<String> contentList = parseContent(content);
            if (null != securityKey) {
                byte[] expectSignBytes = this.encryptAction.apply(content + securityKey.apply(contentList));
                String expectSign = Base64Utils.enCode(expectSignBytes);
                if (!expectSign.equals(sign)) {
                    if(null != signErrorException){
                        throw signErrorException;
                    }else {
                        throw new InfoException("token 错误！");
                    }
                }
            }
            t = checkParseContent.apply(contentList);
        }
        return t;
    }

    /**
     * 返回解析的原始token，不做签名校验
     * @param token
     * @return
     */
    public List<String> parseTokenWithNoSignatureCheck(String token){
        String[] strArray = tokenBaseParseAndCheck(token);
        String sign = strArray[1];//数据校验
        String content = Base64Utils.deCodeToString(strArray[0]);
        return parseContent(content);
    }

    /**
     * 检查并且做签名校验
     * @param token
     * @pram securityKey 加密字符串
     * @return 返回token中的签名:base64字符串
     */
    public String checkSignature(String token,String  securityKey){
        String[] strArray = tokenBaseParseAndCheck(token);
        String sign = strArray[1];//数据校验
        String content = Base64Utils.deCodeToString(strArray[0]);
        byte[] expectSignBytes = this.encryptAction.apply(content + securityKey);
        String expectSign = Base64Utils.enCode(expectSignBytes);
        if (!expectSign.equals(sign)) {
            if(null != signErrorException){
                throw signErrorException;
            }else {
                throw new InfoException("token 错误！");
            }
        }
        return sign;
    }

    public Exception getEmptyTokenException() {
        return emptyTokenException;
    }

    public void setEmptyTokenException(RuntimeException emptyTokenException) {
        this.emptyTokenException = emptyTokenException;
    }

    public RuntimeException getErrorTokenException() {
        return errorTokenException;
    }

    public void setErrorTokenException(RuntimeException errorTokenException) {
        this.errorTokenException = errorTokenException;
    }

    public RuntimeException getSignErrorException() {
        return signErrorException;
    }

    public void setSignErrorException(RuntimeException signErrorException) {
        this.signErrorException = signErrorException;
    }

    public Integer getContentLength() {
        return contentLength;
    }

    public void setContentLength(Integer contentLength) {
        this.contentLength = contentLength;
    }
}
