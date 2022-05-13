package  com.tingfeng.util.java.base.file.csv;

import com.tingfeng.util.java.base.common.bean.CSVBatchReadParam;
import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.inter.ConvertI;
import com.tingfeng.util.java.base.common.inter.returnfunction.Function2;
import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVOne;
import com.tingfeng.util.java.base.common.utils.BeanUtils;
import com.tingfeng.util.java.base.common.utils.ObjectUtils;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * CSV读写工具类
 *
 */
public class CSVUtil {
	
	public static final String  V_COMMON = ",";
	public static final String V_NEW_LINE = "\r";
	public static final String V_NULL_STRING = "";


    /**
     * 默认英文逗号分隔,
     * 当写入内容为空时，可以写入一个字符串""（表示csv的内容是空串），防止csv读取软件读取了csv编码内容
     * 错误会抛出异常
     * @param out 给定内容的输出流
     * @param csvWriter com.tingfeng.util.java.base.file.csv.CSVWriter 实例
     */
    public static void writeCsv(OutputStream out,CSVWriter csvWriter){
        OutputStreamWriter osw=null;
        BufferedWriter bw=null;
        try {
            osw = new OutputStreamWriter(out);
            bw =new BufferedWriter(osw);
            //UTF-8编码bom头
            byte[] bom ={(byte) 0xEF,(byte) 0xBB,(byte) 0xBF};
            out.write(bom);
            csvWriter.write(bw);
        }catch (Throwable e){
           throw new BaseException(e);
        }finally{
            if(bw!=null){
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            if(osw!=null){
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            } 
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
        BufferedReader br=null;
        try {
            br = new BufferedReader(reader);
            String line = ""; 
            while ((line = br.readLine()) != null) { 
                T t = converter.apply(line);
                functionVOne.accept(t);
            }
        }catch (Throwable e){
            throw new BaseException(e);
        }finally{
            if(br!=null){
                try {
                    br.close();
                    br=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
    	sb.append(line.replaceAll("\"","\"\""));
    	sb.append("\"");
    	return sb.toString();
    }
    
   /**
    * 
    * @param out 输出流 ,此方法关闭输出流
    * @param fileName 导出文件名称
    * @param csvPageWriter
    * @param params 获取数据传入的参数,在每次取得数据后因该对参数内容做调整以做到分批得到数据
    */
    public static <T> void writeCsvByPage(OutputStream out, String fileName, final CSVPageWriter<T> csvPageWriter, final Object... params){
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
            try {
                CSVUtil.writeCsv(out, csvWriter);
                out.flush();
                out.close();
            }catch (Throwable e){
                throw new BaseException(e);
            }
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
        String separator = csvBatchReadParam.getSeparator();
        int batchSize = csvBatchReadParam.getBatchSize();
        String[] headers = csvBatchReadParam.getHeaders();
        boolean firstLineIsHeaders = csvBatchReadParam.isFirstLineIsHeaders();
        Class<T> beanCls = csvBatchReadParam.getBeanCls();
        Supplier<Stream<String>> contentSupplier = csvBatchReadParam.getContentSupplier();
       if(csvBatchReadParam.getBatchSize() <= 0){
           throw new IllegalArgumentException("batch size must over than 0");
       }
       if(ObjectUtils.isAnyEmpty(csvBatchReadParam.getConsumerContentF(),csvBatchReadParam.getContentSupplier(),csvBatchReadParam.getBeanCls())){
           throw new IllegalArgumentException("contentSupplier,consumerContentF,beanCls must not be null");
       }
       if(headers == null || headers.length == 0){
           if(!firstLineIsHeaders){
                throw new IllegalArgumentException("can not read used headers! first line is headers or specified the headers");
           }
           headers = contentSupplier.get()
                   .findFirst()
                   .map(str -> str.split(separator))
                   .orElse(new String[]{});
           if(csvBatchReadParam.getHeaderHandler() != null){
               csvBatchReadParam.getHeaderHandler().accept(headers);
           }
       }
        Function<String[], T> beanConverter = csvBatchReadParam.getBeanConverter();
        if(beanConverter == null){
            if(beanCls.isAssignableFrom(Map.class)){
                beanConverter = (Function<String[], T>) createMapConverter(headers);
            }else {
                beanConverter = BeanUtils.createBeanConverter(headers, beanCls, null);
            }
        }
        //分批次读取每一行的值
        Function<String[], T> finalBeanConverter = beanConverter;
        handleByBatch(csvBatchReadParam.isFirstLineIsHeaders() ? 1 : 0,batchSize,(skip, limit) -> {
            //在这里处理每一列的值
            List<T> contentList = null;
                Stream<String[]> stream = contentSupplier.get()
                        .skip(skip)
                        .limit(limit)
                        .map(str -> str.split(separator));
                if(csvBatchReadParam.getContentHandler() != null){
                    stream = stream.peek(csvBatchReadParam.getContentHandler());
                }
                contentList = stream
                        .map(finalBeanConverter::apply)
                        .collect(Collectors.toList());
                csvBatchReadParam.getConsumerContentF().accept(contentList);
            return contentList;
        });
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

    private static <T> void handleByBatch(int startLine,int batchSize, Function2<List<T>, Integer, Integer> handleContentF){
        int skipSize = startLine;
        while (true) {
            List<T> list = handleContentF.run(skipSize, batchSize);
            if(ObjectUtils.isEmpty(list) || list.size() < batchSize){
                break;
            }
            skipSize += batchSize;
        }
    }

    /**
     * 分批读取CSV内容并转为指定对象
     * @param batchSize
     * @param beanCls
     * @param contentSupplier
     * @param consumerContentF
     * @param <T>
     */
    public static <T> void readCSVInBatch(int batchSize,Class<T> beanCls,Supplier<Stream<String>> contentSupplier, Consumer<List<T>> consumerContentF){
        readCSVInBatch(new CSVBatchReadParam<>(batchSize,beanCls,contentSupplier,consumerContentF));
    }

    /**
     * 分批读取CSV内容并转为指定对象,通过map方式读取内容
     * @param batchSize
     * @param contentSupplier
     * @param consumerContentF
     */
    public static void readCSVInBatchToMap(int batchSize,Supplier<Stream<String>> contentSupplier, Consumer<List<Map>> consumerContentF){
        readCSVInBatch(new CSVBatchReadParam<>(batchSize, Map.class, contentSupplier, consumerContentF));
    }
}
