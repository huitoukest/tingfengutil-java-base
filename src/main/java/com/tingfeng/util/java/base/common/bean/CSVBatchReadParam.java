package com.tingfeng.util.java.base.common.bean;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
    private Supplier<Stream<String>> contentSupplier;
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

    public CSVBatchReadParam(int batchSize,Class<T> beanCls,Supplier<Stream<String>> contentSupplier,Consumer<List<T>> consumerContentF) {
        this.beanCls = beanCls;
        this.batchSize = batchSize;
        this.consumerContentF = consumerContentF;
        this.contentSupplier = contentSupplier;
    }

    public CSVBatchReadParam(int batchSize,Class<T> beanCls,String separator,boolean firstLineIsHeaders,Supplier<Stream<String>> contentSupplier,Consumer<List<T>> consumerContentF) {
        this.separator = separator;
        this.beanCls = beanCls;
        this.batchSize = batchSize;
        this.firstLineIsHeaders = firstLineIsHeaders;
        this.consumerContentF = consumerContentF;
        this.contentSupplier = contentSupplier;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String[] getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    public boolean isFirstLineIsHeaders() {
        return firstLineIsHeaders;
    }

    public void setFirstLineIsHeaders(boolean firstLineIsHeaders) {
        this.firstLineIsHeaders = firstLineIsHeaders;
    }

    public Supplier<Stream<String>> getContentSupplier() {
        return contentSupplier;
    }

    public void setContentSupplier(Supplier<Stream<String>> contentSupplier) {
        this.contentSupplier = contentSupplier;
    }

    public Consumer<String[]> getContentHandler() {
        return contentHandler;
    }

    public void setContentHandler(Consumer<String[]> contentHandler) {
        this.contentHandler = contentHandler;
    }

    public Consumer<String[]> getHeaderHandler() {
        return headerHandler;
    }

    public void setHeaderHandler(Consumer<String[]> headerHandler) {
        this.headerHandler = headerHandler;
    }

    public void setBeanCls(Class<T> beanCls) {
        this.beanCls = beanCls;
    }

    public Function<String[], T> getBeanConverter() {
        return beanConverter;
    }

    public void setBeanConverter(Function<String[], T> beanConverter) {
        this.beanConverter = beanConverter;
    }

    public Class<T> getBeanCls() {
        return beanCls;
    }

    public Consumer<List<T>> getConsumerContentF() {
        return consumerContentF;
    }

    public void setConsumerContentF(Consumer<List<T>> consumerContentF) {
        this.consumerContentF = consumerContentF;
    }
}
