package com.tingfeng.util.java.base.common.constant;
/**
 * 
 * @author huitoukest
 * @describe 常见的一些数据常量
 */
public class Constants {
	/*****************************************编码相关Start*****************************************************/
	public interface CharSet{
		public static final String UTF8 = "UTF-8";
		public static final String ISO88591 = "iso8859-1";
		public static final String GBK = "GBK";
		public static final String UNICODE ="Unicode";
		
	}
	/*****************************************编码相关End*****************************************************/
	
	/*****************************************Http相关配置Start*****************************************************/
	public interface HttpConfig{
		public static final String V_SEMICOLON = ";";
		public static final String V_PARAM_JOIN_STRING_AND = "&";
		public static final String V_PARAM_JOIN_STRING_EQ = "=";
		public static final String KEY_CONTENT_TYPE_STREAM = "application/octet-stream;";
		public static final String KYE_CHARSET_EQ = "charset=";
		public static final String HEARDER_CONTENT_DISPOSITION = "Content-Disposition";
		public static final String HEARDER_CONTENT_LENGTH = "Content-Length";
		public static final String KEY_ATTACHMENT = "attachment";
		public static final String KEY_FILENAME_EQ= "filename=";
		public static final String KEY_HTTP = "http://";
		public static final String KEY_HTTPS = "https://";
	}
	/*****************************************Http相关配置End*****************************************************/
	/*****************************************Response关键字Start*****************************************************/
	public interface ResPonse{
		public static final String KEY_RESPCODE = "RespCode";
		public static final String KEY_RESPMSG = "RespDesc";
	}
	/*****************************************Response关键字End*****************************************************/
	
	/*****************************************文件扩展名关键字Start*****************************************************/
	public interface FileExtensionName{
		public static final String CSV = ".csv";
		public static final String PNG = ".png";
		public static final String JPG = ".jpg";
		public static final String JPEG = ".jpeg";
		public static final String DOC = ".doc";
		public static final String XLSX = ".xlsx";
		public static final String TXT = ".txt";
	}
	/*****************************************文件扩展名关键字End*****************************************************/
	
	/*****************************************数据导出相关Start*****************************************************/
	public interface Export{
		/**
		 * 单次最大导出行数50w
		 */
		public static final int MAX_EXPORTCOUNT = 500000;
		/**
		 * 每次从数据库取数据量1w
		 */
		public static final int COUNT_OF_PER_EXPORT = 10000;
	}
	/*****************************************数据导出相关Start*****************************************************/
}
