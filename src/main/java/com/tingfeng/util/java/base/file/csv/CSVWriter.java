package  com.tingfeng.util.java.base.file.csv;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * 写入CSV文件的工具类
 */
public interface CSVWriter {
	
	/**
	 * 默认字段之间以英文逗号分隔,字符串内容需要转移
	 * @param bufferedWriter bufferedWriter.append(line).append("\r");
	 * @return
	 * @throws IOException 
	 */
	public boolean write(BufferedWriter bufferedWriter) throws IOException;
}
