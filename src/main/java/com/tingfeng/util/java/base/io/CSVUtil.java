package com.tingfeng.util.java.base.io;

import com.tingfeng.util.java.base.io.base.CSVBatchReadParam;

import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 请使用 {@link com.tingfeng.util.java.base.file.csv.CSVUtil}
 * @deprecated 迁移到 {@code file.csv} 包，此类保留仅用于兼容
 */
@Deprecated
public class CSVUtil {

	public static final String  V_COMMON = ",";
	public static final String V_NEW_LINE = "\r";
	public static final String V_NULL_STRING = "";

    public static void writeCsv(OutputStream out, CSVWriter csvWriter){
        com.tingfeng.util.java.base.file.csv.CSVUtil.writeCsv(out, csvWriter);
    }

    public static <T> void readCsv(Reader reader, com.tingfeng.util.java.base.lang.base.ConvertI<String,T> converter, com.tingfeng.util.java.base.lang.inter.voidfunction.FunctionVOne<T> functionVOne){
        com.tingfeng.util.java.base.file.csv.CSVUtil.readCsv(reader, converter, functionVOne);
    }

    public static String escapeCsv(String line){
        return com.tingfeng.util.java.base.file.csv.CSVUtil.escapeCsv(line);
    }

    public static <T> void writeCsvByPage(OutputStream out, com.tingfeng.util.java.base.io.CSVPageWriter<T> csvPageWriter, Object... params){
        com.tingfeng.util.java.base.file.csv.CSVUtil.writeCsvByPage(out, csvPageWriter, params);
    }

    public static <T> void readCSVInBatch(CSVBatchReadParam<T> csvBatchReadParam){
        com.tingfeng.util.java.base.file.csv.CSVUtil.readCSVInBatch(csvBatchReadParam);
    }

    public static <T> void readCSVInBatch(int batchSize, Class<T> beanCls, Stream<String> contentStream, Consumer<List<T>> consumerContentF){
        com.tingfeng.util.java.base.file.csv.CSVUtil.readCSVInBatch(batchSize, beanCls, contentStream, consumerContentF);
    }

    public static void readCSVInBatchToMap(int batchSize, Stream<String> contentStream, Consumer<List<Map>> consumerContentF){
        com.tingfeng.util.java.base.file.csv.CSVUtil.readCSVInBatchToMap(batchSize, contentStream, consumerContentF);
    }

    public static <T> List<T> readToBean(Class<T> beanCls, Stream<String> stream, Charset charset){
        return com.tingfeng.util.java.base.file.csv.CSVUtil.readToBean(beanCls, stream, charset);
    }

    public static <T> List<T> readToBean(Class<T> beanCls, Path path, Charset charset){
        return com.tingfeng.util.java.base.file.csv.CSVUtil.readToBean(beanCls, path, charset);
    }
}
