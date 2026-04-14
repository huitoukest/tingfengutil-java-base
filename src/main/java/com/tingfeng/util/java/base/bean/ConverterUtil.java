package com.tingfeng.util.java.base.bean;

import com.tingfeng.util.java.base.bean.base.ConverterInfo;
import com.tingfeng.util.java.base.lang.base.UnionKey;
import com.tingfeng.util.java.base.datetime.DateUtils;
import com.tingfeng.util.java.base.datetime.LocalDateUtils;
import com.tingfeng.util.java.base.lang.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 转换工具
 * 默认不处理 null转换的情况
 * 1. 将会在bean属性copy使用
 * 2. 将会在csv的读取与写的时候使用
 */
public class ConverterUtil {

    private static List<ConverterInfo> DEFAULT_CONVERT_INFOS = new ArrayList<>();

    private static Map<UnionKey, Function> DEFAULT_CONVERT_METHOD_MAP;

    static {
        /*************** 其它转字符串 ************************************/
        DEFAULT_CONVERT_INFOS.addAll(getDefaultDateToStringConverter());
        DEFAULT_CONVERT_INFOS.addAll(getDefaultNumberToStringConverter());
        DEFAULT_CONVERT_INFOS.addAll(getDefaultCharToStringConverter());

        /************** 字符串转其它 **************************************/
        DEFAULT_CONVERT_INFOS.addAll(getDefaultStringToDateConverter());
        DEFAULT_CONVERT_INFOS.addAll(getDefaultStringToNumberConverter());
        DEFAULT_CONVERT_INFOS.addAll(getDefaultStringToCharConverter());

        buildDefaultConvertMethodMap();
    }

    public static void register(ConverterInfo converterInfo){
        DEFAULT_CONVERT_INFOS.add(converterInfo);
        buildDefaultConvertMethodMap();
    }

    private static void buildDefaultConvertMethodMap(){
        DEFAULT_CONVERT_METHOD_MAP = DEFAULT_CONVERT_INFOS.stream()
                .collect(Collectors.toMap(ConverterInfo::getMatchKey,ConverterInfo::getConvertMethod, (a,b) -> b));
    }

    public static Map<UnionKey, Function> getDefaultConvertMethodMap(){
        return DEFAULT_CONVERT_METHOD_MAP;
    }

    public static <S,T> Function<S,T> getConverter(Class<S> src,Class<T> target){
        return DEFAULT_CONVERT_METHOD_MAP.get(new UnionKey(src,target));
    }

    public static <T>  Function<String,T> convertWhenNotBlank(Function<String,T> converter){
        return src -> {
            if(StringUtils.isEmpty(src, true)){
                return null;
            }
            return converter.apply(src);
        };
    }

    public static List<ConverterInfo> getDefaultStringToDateConverter(){
        return Arrays.asList(new ConverterInfo(String.class, Date.class, convertWhenNotBlank(s -> DateUtils.getDate(s))),
                new ConverterInfo(String.class, LocalDateTime.class, convertWhenNotBlank(s -> LocalDateUtils.getLocalDateTime(s))),
                new ConverterInfo(String.class, LocalDate.class, convertWhenNotBlank(s -> LocalDateUtils.getLocalDate(s)))
        );
    }

    public static List<ConverterInfo> getDefaultStringToNumberConverter(){
        return Arrays.asList(new ConverterInfo(String.class, Byte.class, convertWhenNotBlank(s -> Byte.parseByte(s))),
                new ConverterInfo(String.class, Short.class, convertWhenNotBlank(s -> Short.parseShort(s))),
                new ConverterInfo(String.class, Integer.class, convertWhenNotBlank(s -> Integer.parseInt(s))),
                new ConverterInfo(String.class, Long.class, convertWhenNotBlank(s -> Long.parseLong(s))),
                new ConverterInfo(String.class, BigDecimal.class, convertWhenNotBlank(s -> new BigDecimal(s))),
                new ConverterInfo(String.class, Float.class, convertWhenNotBlank(s -> Float.parseFloat(s))),
                new ConverterInfo(String.class, Double.class, convertWhenNotBlank(s -> Double.parseDouble(s)))
        );
    }

    public static List<ConverterInfo> getDefaultStringToCharConverter(){
        return Arrays.asList(new ConverterInfo(String.class, Character.class, convertWhenNotBlank(s -> s.charAt(0))));
    }

    public static List<ConverterInfo> getDefaultDateToStringConverter(){
        return Arrays.asList(new ConverterInfo(Date.class, String.class, s -> DateUtils.getDateString((Date) s)),
                new ConverterInfo(LocalDateTime.class, String.class, s -> LocalDateUtils.getDateString((LocalDateTime) s)),
                new ConverterInfo(LocalDate.class,String.class, s -> LocalDateUtils.getDateString((LocalDate) s))
        );
    }

    public static List<ConverterInfo> getDefaultNumberToStringConverter(){
        return Arrays.asList(new ConverterInfo(Byte.class, String.class, s -> s.toString()),
                new ConverterInfo(Short.class, String.class,  s -> s.toString()),
                new ConverterInfo(Integer.class, String.class, s -> s.toString()),
                new ConverterInfo(Long.class, String.class, s -> s.toString()),
                new ConverterInfo(BigDecimal.class, String.class, s -> s.toString()),
                new ConverterInfo(Float.class, String.class,  s -> s.toString()),
                new ConverterInfo(Double.class, String.class, s -> s.toString())
        );
    }

    public static List<ConverterInfo> getDefaultCharToStringConverter(){
        return Arrays.asList(new ConverterInfo(Character.class, String.class, s -> s.toString()));
    }

}
