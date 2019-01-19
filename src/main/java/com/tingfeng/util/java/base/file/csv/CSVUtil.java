package  com.tingfeng.util.java.base.file.csv;

import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.inter.ConvertI;
import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVOne;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.List;

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
     * @param out
     * @param csvWriter
     * @throws IOException
     */
    public static void writeCsv(OutputStream out,CSVWriter csvWriter){
        OutputStreamWriter osw=null;
        BufferedWriter bw=null;
        try {
            osw = new OutputStreamWriter(out);
            bw =new BufferedWriter(osw);
            byte[] bom ={(byte) 0xEF,(byte) 0xBB,(byte) 0xBF};//UTF-8编码bom头
            out.write(bom);
            csvWriter.write(bw);
        }catch (Throwable e){
           throw new BaseException(e);
        }finally{
            if(bw!=null){
                try {
                    bw.close();
                    bw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            if(osw!=null){
                try {
                    osw.close();
                    osw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            } 
        }
    }

    /**
     * 导入
     * @param reader
     * @param converter 将读取的csv单行数据转换为T类型
     * @Param functionVOne
     * @param <T>
     * @throws IOException
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
     * @param line
     * @return
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
    * @throws Exception  当导出的记录数量超过最大导出数量的时候,抛出传入的异常
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
                                data = csvPageWriter.getTableHearder();
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
                out = null;
            }catch (Throwable e){
                throw new BaseException(e);
            }
    }
    /**
     * 
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
}
