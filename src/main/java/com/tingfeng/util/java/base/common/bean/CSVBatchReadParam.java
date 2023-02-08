package com.tingfeng.util.java.base.common.bean;

import lombok.Data;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
@Data
public class CSVBatchReadParam<T> {
    /**
     * 默认的分隔符
     */
    private String separator = "\t";
    private int batchSize = 1000;
    private String[] headers;
    private boolean firstLineIsHeaders = true;
    private Class<T> beanCls;
    /**
     * 提供原始的每行数据来源
     */
    private Stream<String> contentStream;
    /**
     * 自定义处理content handler, 在转换为bean 对象之前触发执行， 且此值不为 null 时方使用
     */
    private Consumer<String[]> contentHandler;
    /**
     * 自定义处理header handler；仅仅在使用系统自动解析方式时， 且此值不为 null 时方使用
     */
    private Consumer<String[]> headerHandler;
    /**
     * 提供自定义的转换器，值为 null 时 不使用
     */
    private Function<String[], T> beanConverter;
    /**
     * 对读取到的已经转换后的内容做处理
     */
    private Consumer<List<T>> consumerContentF;

    public CSVBatchReadParam(){}

    public CSVBatchReadParam(int batchSize,Class<T> beanCls,Stream<String> contentStream,Consumer<List<T>> consumerContentF) {
        this.beanCls = beanCls;
        this.batchSize = batchSize;
        this.consumerContentF = consumerContentF;
        this.contentStream = contentStream;
    }

    public CSVBatchReadParam(int batchSize,Class<T> beanCls,String separator,boolean firstLineIsHeaders,Stream<String> contentStream,Consumer<List<T>> consumerContentF) {
        this.separator = separator;
        this.beanCls = beanCls;
        this.batchSize = batchSize;
        this.firstLineIsHeaders = firstLineIsHeaders;
        this.consumerContentF = consumerContentF;
        this.contentStream = contentStream;
    }
}
