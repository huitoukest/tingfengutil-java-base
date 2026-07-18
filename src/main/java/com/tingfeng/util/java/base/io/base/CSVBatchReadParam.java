package com.tingfeng.util.java.base.io.base;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 请使用 {@link com.tingfeng.util.java.base.file.csv.CSVBatchReadParam}
 * @deprecated 迁移到 {@code file.csv} 包，此类保留仅用于兼容
 */
@Deprecated
public class CSVBatchReadParam<T> extends com.tingfeng.util.java.base.file.csv.CSVBatchReadParam<T> {

	public CSVBatchReadParam(){
		super();
	}

	public CSVBatchReadParam(int batchSize, Class<T> beanCls, Stream<String> contentStream, Consumer<List<T>> consumerContentF) {
		super(batchSize, beanCls, contentStream, consumerContentF);
	}

	public CSVBatchReadParam(int batchSize, Class<T> beanCls, String separator, boolean firstLineIsHeaders, Stream<String> contentStream, Consumer<List<T>> consumerContentF) {
		super(batchSize, beanCls, separator, firstLineIsHeaders, contentStream, consumerContentF);
	}
}
