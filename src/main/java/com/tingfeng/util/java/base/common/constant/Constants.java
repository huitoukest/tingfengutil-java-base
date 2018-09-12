package com.tingfeng.util.java.base.common.constant;
/**
 * 
 * @author huitoukest
 * @describe 常见的一些数据常量
 */
public class Constants {

	/**
	 * 常见的字符和符号
	 */
	public interface Symbol{
		char dot = '.';
		String spliteDot = "\\.";
        String comma = ",";
        String semicolon = ";";
    }

	/*****************************************编码相关Start*****************************************************/
	public interface CharSet{
		String UTF8 = "UTF-8";
		String ISO88591 = "iso8859-1";
		String GBK = "GBK";
		String UNICODE ="Unicode";
		
	}
	/*****************************************编码相关End*****************************************************/
	
	/*****************************************Http相关配置Start*****************************************************/
	public interface HttpConfig{
		String V_SEMICOLON = ";";
		String V_PARAM_JOIN_STRING_AND = "&";
		String V_PARAM_JOIN_STRING_EQ = "=";
		String KEY_CONTENT_TYPE_STREAM = "application/octet-stream;";
		String KYE_CHARSET_EQ = "charset=";
		String HEARDER_CONTENT_DISPOSITION = "Content-Disposition";
		String HEARDER_CONTENT_LENGTH = "Content-Length";
		String KEY_ATTACHMENT = "attachment";
		String KEY_FILENAME_EQ= "filename=";
		String KEY_HTTP = "http://";
		String KEY_HTTPS = "https://";
	}
	/*****************************************Http相关配置End*****************************************************/
	/*****************************************Response关键字Start*****************************************************/
	public interface ResPonse{
		String KEY_RESPCODE = "RespCode";
		String KEY_RESPMSG = "RespDesc";
	}
	/*****************************************Response关键字End*****************************************************/
	
	/*****************************************文件扩展名关键字Start*****************************************************/
	public interface FileExtensionName{
		String CSV = ".csv";
		String PNG = ".png";
		String JPG = ".jpg";
		String JPEG = ".jpeg";
		String DOC = ".doc";
		String XLSX = ".xlsx";
		String TXT = ".txt";
	}
	/*****************************************文件扩展名关键字End*****************************************************/
	
	/*****************************************数据导出相关Start*****************************************************/
	public interface Export{
		/**
		 * 单次最大导出行数50w
		 */
		int MAX_EXPORTCOUNT = 500000;
		/**
		 * 每次从数据库取数据量1w
		 */
		int COUNT_OF_PER_EXPORT = 10000;
	}
	/*****************************************数据导出相关Start*****************************************************/
	/**
	 * java和系统相关的常量
	 */
	public interface JavaSystem{
		String jar = "jar";
		String classStr = "class";
		String javaStr = "java";
	}



}
