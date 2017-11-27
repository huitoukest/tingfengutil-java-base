package  com.tingfeng.util.java.base.file.csv;

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
     * 错误会抛出异常
     * @param dataList 数据
     * @return
	 * @throws IOException 
     */
    public static void writeCsv(OutputStream out,CSVWriter csvWriter) throws IOException{
        OutputStreamWriter osw=null;
        BufferedWriter bw=null;
        try {
            osw = new OutputStreamWriter(out);
            bw =new BufferedWriter(osw);
            byte[] bom ={(byte) 0xEF,(byte) 0xBB,(byte) 0xBF};//UTF-8编码bom头
            out.write(bom);
            csvWriter.write(bw);
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
     * 
     * @param file csv文件(路径+文件)
     * @return
     * @throws IOException 
     */
    public static <T> void readCsv(Reader reader,CSVReader<T> csvReader) throws IOException{      
        BufferedReader br=null;
        try { 
            br = new BufferedReader(reader);
            String line = ""; 
            while ((line = br.readLine()) != null) { 
                T t =csvReader.getObject(line);
                csvReader.read(t);
            }
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
    * @param csvDbWriter
    * @param params 获取数据传入的参数,在每次取得数据后因该对参数内容做调整以做到分批得到数据
    * @throws Exception  当导出的记录数量超过最大导出数量的时候,抛出传入的异常
    */
    public static <T> void writeCsvFromDb(OutputStream out, String fileName,final CSVStreamWriter<T> csvDbWriter,final Object... params) throws Exception{
    		final long count = csvDbWriter.getTotalCount();
            if (count > csvDbWriter.getMaxExportCount()) {
    			throw csvDbWriter.getOverMaxExportCountException();
    		}    		
    		CSVWriter csvWriter = new CSVWriter() {
    			StringBuilder sb = new StringBuilder(150);
    			@Override
    			public boolean write(BufferedWriter bufferedWriter) throws IOException {
    				List<T> susbs = csvDbWriter.getList(params);	
    				String content = null;
    				Object[]  data = null;
    				for (int i = 0; i < susbs.size(); i++) {
    					if (i == 0) {
    						data = csvDbWriter.getTableHearder();
    						content = getCSVLineData(data,sb);
    						bufferedWriter.append(content).append(V_NEW_LINE);
    					}else if(i > 0){
    						bufferedWriter.append(V_NEW_LINE);
    					}
    					T lineData = susbs.get(i);
    					data = csvDbWriter.getTableLineData(lineData);
    					content = getCSVLineData(data,sb);
    					bufferedWriter.append(content);
    				}
    				if (susbs.size() >= csvDbWriter.getCountOfPerExport()) {
    					return this.write(bufferedWriter);
    				}
    				return true;
    			}
    		};

    		CSVUtil.writeCsv(out, csvWriter);
    		out.flush();
    		out.close();
    		out = null;
      
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
