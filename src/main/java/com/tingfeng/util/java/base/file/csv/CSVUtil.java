package  com.tingfeng.util.java.base.file.csv;

import com.tingfeng.util.java.base.common.bean.CSVBatchReadParam;
import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.inter.ConvertI;
import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVOne;
import com.tingfeng.util.java.base.common.utils.BeanUtils;
import com.tingfeng.util.java.base.common.utils.ObjectUtils;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * CSV读写工具类
 *
 */
public class CSVUtil {
    private static final Logger logger = LoggerFactory.getLogger(CSVUtil.class);
	
	public static final String  V_COMMON = ",";
	public static final String V_NEW_LINE = "\r";
	public static final String V_NULL_STRING = "";
    /**
     * 批量读写时，默认的批处理数量
     */
    private static final int DEFAULT_BATCH_SIZE = 5000;

    /**
     * 默认英文逗号分隔,
     * 当写入内容为空时，可以写入一个字符串""（表示csv的内容是空串），防止csv读取软件读取了csv编码内容
     * 错误会抛出异常
     * @param out 给定内容的输出流
     * @param csvWriter com.tingfeng.util.java.base.file.csv.CSVWriter 实例
     */
    public static void writeCsv(OutputStream out, CSVWriter csvWriter){
        try (OutputStreamWriter osw = new OutputStreamWriter(out);
             BufferedWriter bw = new BufferedWriter(osw)) {
            //UTF-8编码bom头，写入bw而非out，确保在内容之前
            bw.write('\uFEFF');
            csvWriter.write(bw);
        } catch (Throwable e){
           throw new BaseException(e);
        }
    }

    /**
     * 导入读取CSV文件
     * @param reader 内容输入的Reader对象
     * @param converter 将读取的csv单行数据转换为T类型
     * @param functionVOne 将读取到的数据消费
     * @param <T> 读取到的数据转后后的对应的java bean对象
     */
    public static <T> void readCsv(Reader reader, ConvertI<String,T> converter, FunctionVOne<T> functionVOne){
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                T t = converter.apply(line);
                functionVOne.accept(t);
            }
        } catch (Throwable e){
            throw new BaseException(e);
        }
    }
    /**
     * 默认英文逗号分隔,转意双引号和逗号
     * @param line csv行数据
     * @return 转义后的字符串
     */
    public static String escapeCsv(String line){
    	if(null == line)
    		return "";
    	StringBuilder sb = new StringBuilder();
    	sb.append("\"");
    	sb.append(line.replace("\"","\"\""));
    	sb.append("\"");
    	return sb.toString();
    }
    
   /**
    * 
    * @param out 输出流 ,此方法关闭输出流
    * @param csvPageWriter
    * @param params 获取数据传入的参数,在每次取得数据后因该对参数内容做调整以做到分批得到数据
    */
    public static <T> void writeCsvByPage(OutputStream out, final CSVPageWriter<T> csvPageWriter, final Object... params){
    		final long count = csvPageWriter.getTotalCount();
            if (count > csvPageWriter.getMaxExportCount()) {
    			throw csvPageWriter.getOverMaxExportCountException();
    		}    		
    		CSVWriter csvWriter = new CSVWriter() {
    			StringBuilder sb = new StringBuilder(150);
    			@Override
    			public boolean write(BufferedWriter bufferedWriter){
    				try {
                        List<T> susbs = csvPageWriter.getList(params);
                        String content = null;
                        Object[] data = null;
                        for (int i = 0; i < susbs.size(); i++) {
                            if (i == 0) {
                                data = csvPageWriter.getTableHeader();
                                content = getCSVLineData(data, sb);
                                bufferedWriter.append(content).append(V_NEW_LINE);
                            } else if (i > 0) {
                                bufferedWriter.append(V_NEW_LINE);
                            }
                            T lineData = susbs.get(i);
                            data = csvPageWriter.getTableLineData(lineData);
                            content = getCSVLineData(data, sb);
                            bufferedWriter.append(content);
                        }
                        if (content == null || content.length() <= 0) {
                            bufferedWriter.append(" ");
                        }
                        if (susbs.size() >= csvPageWriter.getCountOfPerExport()) {
                            return this.write(bufferedWriter);
                        }
                        return true;
                    }catch (Throwable e){
    				    throw new BaseException(e);
                    }
    			}
    		};
            CSVUtil.writeCsv(out, csvWriter);
    }
    /**
     * 将对应数组中的数据转为CSV单行字符串
     * @param csvData
     * @param sb 此StringBuilder中原数据会被清空
     * @return 返回csv中单行数据
     */
    private static <T> String  getCSVLineData(T[] csvData,StringBuilder sb){
    	sb.setLength(0);
    	if(csvData != null){
    		for(int i = 0; i < csvData.length; i++){
    			if(i > 0){
    				sb.append(V_COMMON);
    			}
    			if(null == csvData[i]){
    				sb.append(V_NULL_STRING);
    			}else{
    				sb.append(csvData[i]);
    			}
    			
    		}
    	}
       return sb.toString();
    }

    /**
     * 分批读取CSV内容并转为指定对象
     * @param csvBatchReadParam
     * @param <T>
     */
    public static <T> void readCSVInBatch(CSVBatchReadParam<T> csvBatchReadParam){
        //获取字段
        boolean firstLineIsHeaders = csvBatchReadParam.isFirstLineIsHeaders();
        Class<T> beanCls = csvBatchReadParam.getBeanCls();
        Stream<String> contentStream = csvBatchReadParam.getContentStream();
       if(csvBatchReadParam.getBatchSize() <= 0){
           throw new IllegalArgumentException("batch size must over than 0");
       }
       if(ObjectUtils.isAnyEmpty(csvBatchReadParam.getConsumerContentF(),csvBatchReadParam.getContentStream(),csvBatchReadParam.getBeanCls())){
           throw new IllegalArgumentException("contentSupplier,consumerContentF,beanCls must not be null");
       }
       final String[][] headers = {csvBatchReadParam.getHeaders()};
       AtomicLong index = new AtomicLong(0);
       String separator = csvBatchReadParam.getSeparator();
       int batchSize = csvBatchReadParam.getBatchSize();
       List<T> contentList = new ArrayList<>(Math.max(16,Math.min(DEFAULT_BATCH_SIZE,csvBatchReadParam.getBatchSize())));
       contentStream.forEach(line -> {
           long lineNumber = index.incrementAndGet();
           if(headers[0] == null || headers[0].length == 0){
               if(!firstLineIsHeaders){
                   throw new IllegalArgumentException("can not read used headers! first line is headers or specified the headers");
               }
               if(lineNumber == 1){
                   headers[0] = Optional.ofNullable(line)
                           .map(str -> str.split(separator))
                           .orElse(new String[]{});
               }
               if(csvBatchReadParam.getHeaderHandler() != null){
                   csvBatchReadParam.getHeaderHandler().accept(headers[0]);
               }
           }
           if(csvBatchReadParam.isUnescapeWhenAroundQuotationMarks()) {
               for (int i = 0; i < headers[0].length; i++) {
                   String header = headers[0][i];
                   if(header.length() > 1){
                       headers[0][i] = StringUtils.unescape(header);
                   }
               }
           }
           // 剥离 header 首尾引号
           Character headerQuoteChar = csvBatchReadParam.getHeaderQuoteChar();
           if (headerQuoteChar != null) {
               char qc = headerQuoteChar;
               for (int i = 0; i < headers[0].length; i++) {
                   headers[0][i] = trimQuote(headers[0][i], qc);
               }
           }
           Function<String[], T> beanConverter = csvBatchReadParam.getBeanConverter();
           if(beanConverter == null){
               if(beanCls.isAssignableFrom(Map.class)){
                   beanConverter = (Function<String[], T>) createMapConverter(headers[0]);
               }else {
                   beanConverter = BeanUtils.createBeanConverter(headers[0], beanCls, null);
               }
           }
           int contentOffset = csvBatchReadParam.isFirstLineIsHeaders() ? 1 : 0;
           if(lineNumber > contentOffset){
               String[] contentStr = line.split(separator);
               if(csvBatchReadParam.isUnescapeWhenAroundQuotationMarks()) {
                   for (int i = 0; i < contentStr.length; i++) {
                       contentStr[i] = StringUtils.unescape(contentStr[i]);
                   }
               }
               // 剥离 content 首尾引号
               Character contentQuoteChar = csvBatchReadParam.getContentQuoteChar();
               if (contentQuoteChar != null) {
                   char qc = contentQuoteChar;
                   for (int i = 0; i < contentStr.length; i++) {
                       contentStr[i] = trimQuote(contentStr[i], qc);
                   }
               }
               if(csvBatchReadParam.getContentHandler() != null){
                   csvBatchReadParam.getContentHandler().accept(contentStr);
               }
               T content = beanConverter.apply(contentStr);
               contentList.add(content);
           }
           if(lineNumber > contentOffset && (lineNumber - contentOffset) % batchSize == 0){
               csvBatchReadParam.getConsumerContentF().accept(contentList);
               contentList.clear();
           }
       });
        if(!contentList.isEmpty()){
            csvBatchReadParam.getConsumerContentF().accept(contentList);
            contentList.clear();
        }
    }

    /**
     * 剥离字符串首尾的指定引号字符
     * @param str 原始字符串
     * @param quoteChar 引号字符
     * @return 剥离后的字符串
     */
    private static String trimQuote(String str, char quoteChar) {
        if (str == null || str.length() < 2) return str;
        if (str.charAt(0) == quoteChar && str.charAt(str.length() - 1) == quoteChar) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    private static <T> Function<String[],Map<String,String>> createMapConverter(String[] headers) {
        return contents -> {
            Map<String,String> map = new HashMap<>();
            IntStream.range(0, contents.length)
                    .forEach(index -> {
                        map.put(headers[index],contents[index]);
                    });
            return map;
        };
    }

    /**
     * 分批读取CSV内容并转为指定对象
     * @param batchSize
     * @param beanCls
     * @param contentStream
     * @param consumerContentF
     * @param <T>
     */
    public static <T> void readCSVInBatch(int batchSize,Class<T> beanCls,Stream<String> contentStream, Consumer<List<T>> consumerContentF){
        readCSVInBatch(new CSVBatchReadParam<>(batchSize,beanCls,contentStream,consumerContentF));
    }

    /**
     * 分批读取CSV内容并转为指定对象,通过map方式读取内容
     * @param batchSize
     * @param contentStream
     * @param consumerContentF
     */
    public static void readCSVInBatchToMap(int batchSize,Stream<String> contentStream, Consumer<List<Map>> consumerContentF){
        readCSVInBatch(new CSVBatchReadParam<>(batchSize, Map.class, contentStream, consumerContentF));
    }

    /**
     * 读取数据 并转为指定 Bean; 如果传入 Map.class 则默认返回数据为 List[Map[String,String]]
     * @param beanCls bean对象的class 或者 Map.class
     * @param stream 每一行数据的Stream 流
     * @param charset 编码
     * @return
     * @param <T>
     */
    public static <T> List<T> readToBean(Class<T> beanCls, Stream<String> stream, Charset charset){
        List<T> resultList = new ArrayList<>(64);
        CSVUtil.readCSVInBatch(DEFAULT_BATCH_SIZE,beanCls,stream,list -> resultList.addAll(list));
        return resultList;
    }
    /**
     * 读取数据 并转为指定 Bean; 如果传入 Map.class 则默认返回数据为 List[Map[String,String]]
     * @param beanCls bean对象的class 或者 Map.class
     * @param path 文件路径
     * @param charset 编码
     * @return
     * @param <T>
     */
    public static <T> List<T> readToBean(Class<T> beanCls,Path path, Charset charset){
        try (Stream<String> lines = Files.lines(path, charset)) {
            return readToBean(beanCls, lines, charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
