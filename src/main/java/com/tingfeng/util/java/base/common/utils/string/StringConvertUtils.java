package com.tingfeng.util.java.base.common.utils.string;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串的转换工具,主要实现将字符串转换为其它格式/对象;
 * @author huitoukest
 *
 */
public class StringConvertUtils {
	

	
	final static int BUFFER_SIZE = 4096;
	/**
	 * 将InputStream转换成String
	 * 
	 * @param in
	 *            InputStream
	 * @return String
	 * @throws Exception
	 */
	public static String getStringByStream(InputStream in) {

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] data = new byte[BUFFER_SIZE];
		String string = null;
		int count = 0;
		try {
			while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
				outStream.write(data, 0, count);
		} catch (IOException e) {
			e.printStackTrace();
		}

		data = null;
		try {
			string = new String(outStream.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return string;
	}

	/**
	 * 将InputStream转换成某种字符编码的String
	 * 
	 * @param in
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public static String getStringByStream(InputStream in, String encoding) {
		String string = null;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] data = new byte[BUFFER_SIZE];
		int count = -1;
		try {
			while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
				outStream.write(data, 0, count);
		} catch (IOException e) {
			e.printStackTrace();
		}

		data = null;
		try {
			string = new String(outStream.toByteArray(), encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return string;
	}
	
	/**
	 * 将byte数组转换成String
	 * 
	 * @param in
	 * @param charEncode
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws Exception
	 */
	public static String getStringByBytes(byte[] in,String charEncode) throws UnsupportedEncodingException {
		String string=new String(in,charEncode);
		return string;
	}
	public static String getStringByBytes(byte[] in) throws UnsupportedEncodingException {
		return getStringByBytes(in, "UTF-8");
	}
	

	
	
	/**如果使用的是逗号作为分隔符号,使用逗号分隔符会更好
	 * 将souceString用指定的splite分割,自动去除首位空格和首位分隔符号,自动去掉首位多余的分割符号;
	 * @param souceString
	 * @param splite
	 * @return  a,b,c这种格式,方便在sql中用in语句直接使用;转换失败会抛出异常
	 */
	public static String toSqlColumnString(String souceString,String splite){
		if(splite.equals(","))
			return toSqlColumnString(souceString);
		souceString=souceString.trim();
		if(souceString.length()<1) return souceString;
		if(souceString.indexOf(splite)==0)
		{
			souceString=souceString.substring(splite.length());
		}
		if(souceString.lastIndexOf(splite)+splite.length()==souceString.length()){
			souceString=souceString.substring(0,souceString.length()-splite.length());
		}
		String[] tdsStrings=souceString.split(splite);
		StringBuffer sb=new StringBuffer();
		
		for(int i=0;i<tdsStrings.length;i++){
			if(i>0)
			sb.append(",");
			sb.append(tdsStrings[i]);
		}
		return sb.toString();
		
	}
	
	/**souceString默认使用的是逗号作为分隔符号
	 * @param souceString
	 * @return  a,b,c这种格式,方便在sql中用in语句直接使用;转换失败会抛出异常
	 */
	public static String toSqlColumnString(String souceString){
		souceString=souceString.trim();
		if(souceString.length()<1) return souceString;
		if(souceString.indexOf(",")==0)
		{
			souceString=souceString.substring(1);
		}
		if(souceString.lastIndexOf(",")==souceString.length()-1){
			souceString=souceString.substring(0,souceString.length()-1);
		}
		return souceString;		
	}

	
	
	public static String toFirstLowerLetterString(String srcString) {
		StringBuilder sb = new StringBuilder();
		sb.append(Character.toLowerCase(srcString.charAt(0)));
		sb.append(srcString.substring(1));
		return sb.toString();
	}
	   	
	
	/*******************************************************************************
	 * 一些基础类型数据的转换
	 *******************************************************************************/
	public static Integer getInteger(String value,Integer nullValue){
		if(value==null)
			return nullValue;
		return Integer.parseInt(value);
	}
	public static Integer getInteger(String value){
		return getInteger(value,null);
	}
	
	public static Long getLong(String value,Long nullValue){
		if(value==null)
			return nullValue;
		return Long.parseLong(value);
	}
	public static Long getLong(String value){
		return getLong(value,null);
	}
	
	public static Double getDouble(String value,Double nullValue){
		if(value==null)
			return nullValue;
		return Double.parseDouble(value);
	}
	public static Double getDouble(String value){
		return getDouble(value,null);
	}
	
	public static Float getFloat(String value,Float nullValue){
		if(value==null)
			return nullValue;
		return Float.parseFloat(value);
	}
	public static Float getFloat(String value){
		return getFloat(value,null);
	}
	
	public static Short getShort(String value,Short nullValue){
		if(value==null)
			return nullValue;
		return Short.parseShort(value);
	}
	public static Short getShort(String value){
		return getShort(value,null);
	}
	public static Byte getByte(String value,Byte nullValue){
		if(value==null)
			return nullValue;
		return Byte.parseByte(value);
	}
	public static Byte getByte(String value){
		return getByte(value,null);
	}
	public static Boolean getBoolean(String value,Boolean nullValue){
		if(value==null)
			return nullValue;
		return Boolean.parseBoolean(value);
	}
	public static Boolean getBoolean(String value){
		return getBoolean(value,null);
	}
		
	/**
	 * 去除字符串中非Number的部分
	 * @param content
	 * @return
	 */
	public String getNumberString(String content) {
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			return matcher.group(0);
		}
		return "";
	}

	/**
	 * 去除字符串中的Number部分
	 * @param content
	 * @return
	 */
	public String getNotNumberString(String content) {
		Pattern pattern = Pattern.compile("\\D+");
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			return matcher.group(0);
		}
		return "";
	}
			
	/*******************************************************************************
	 * 和时间与日期转换相关的方法
	 *******************************************************************************/
	
	/**
	 *
	 * @param formatString "yyyy-MM-dd"或者"yyyy-MM-dd HH:mm:ss",自定决定
	 * @return
	 */
	/*public static String getStringByDate(Date date,String formatString){
		if(date == null){
			return "";
		}
		try{
			SimpleDateFormat format = new SimpleDateFormat(formatString,Locale.getDefault());
			return format.format(date);
		}catch(Exception e){
			return "";
		}
	}*/
	
	/**
	 * 
	 * formatString "yyyy-MM-dd"
	 * @return
	 */
	/*public static String getStringByDate(Date date){
		return getStringByDate(date, "yyyy-MM-dd");
	}
	
	*//**
	 * 
	 * formatString "yyyy-MM-dd HH:mm:ss"
	 * @return
	 *//*
	public static String getStringByDateAndTime(Date date){
		return getStringByDate(date, "yyyy-MM-dd HH:mm:ss");
	}*/
	
	/**
	 * 得到指定的毫秒数所代表的日期,yyyy-mm-dd格式
	 * @param time
	 * @param formatString "yyyy-MM-dd"或者"yyyy-MM-dd HH:mm:ss",自定决定
	 * @return
	 */
	public static String getDateString(Long time,String formatString){
		if(time== null){
			return "";
		}
		SimpleDateFormat format = new SimpleDateFormat(formatString,Locale.getDefault());
		return format.format(new Date(time));
	}
	
	/**
	 * 获取当前时间，只是精确到分钟，格式为：yyyy-MM-dd hh:mm
	 * @return
	 */
	public static String getStringByCurrentTimeNoSeconds(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		//设置时间
		format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		Date date = new Date(System.currentTimeMillis());
		return format.format(date);
	}
	
	/**
	 * 获取当前时间，格式为：yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String getStringByCurrentDateAndTime(){
		return getDateString(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss");
	}
	
	/**
	 * 以“yyyy-MM-dd   hh:mm:ss”形式返回格林威治时间，精确到秒
	 * @return  {@link String}
	 */
	public static String getStringByGMT(){
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		//设置时间
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = new Date(System.currentTimeMillis());
		return format.format(date);
	}

	
	/**
	 * 泛型方法(通用)，把list转换成以“,”相隔的字符串 调用时注意类型初始化（申明类型） 如：List<Integer> intList = new ArrayList<Integer>(); 调用方法：StringUtils.listTtoString(intList); 效率：list中4条信息，1000000次调用时间为850ms左右
	 * 
	 * @author fengliang
	 * @serialData 2008-01-09
	 * @param <T>
	 *            泛型
	 * @param list
	 * @params symbol
	 *            list列表
	 * @return 以symbol分隔的字符串
	 */
	public static <T> String toListString(List<T> list,String symbol) {
		if (list == null || list.size() < 1)
			return "";
		Iterator<T> i = list.iterator();
		if (!i.hasNext())
			return "";
		StringBuilder sb = new StringBuilder();
		for (;;) {
			T e = i.next();
			sb.append(e);
			if (!i.hasNext())
				return sb.toString();
			sb.append(symbol);
		}
	}
	/**
	 * 默认返回以,分隔的字符串
	 * @param list
	 * @return
	 */
	public static <T> String toListString(List<T> list) {
		return toListString(list,",");
	}
	/**
	 * 把整形数组转换成以“,”相隔的字符串
	 * 
	 * @author fengliang
	 * @serialData 2008-01-08
	 * @param a
	 *            数组a
	 * @return 以“,”相隔的字符串
	 */
	public static String toListString(Object[] a,String symbol) {
		if (a == null)
			return "";
		int iMax = a.length - 1;
		if (iMax == -1)
			return "";
		StringBuilder b = new StringBuilder();
		for (int i = 0;; i++) {
			b.append(a[i]);
			if (i == iMax)
				return b.toString();
			b.append(symbol);
		}
	}
	
	public static String toListString(Object[] a) {
		return toListString(a, ","); 	
	}
	
	
	 /** 全角字符变半角字符
	 * 
	 * @date 
	 * @param str
	 * @return
	 */
	public static String toSbcCaseByDbcCase(String str) {
		if (str == null || "".equals(str))
			return "";
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);

			if (c >= 65281 && c < 65373)
				sb.append((char) (c - 65248));
			else
				sb.append(str.charAt(i));
		}

		return sb.toString();

	}
	/**
	 * 全角生成半角
	 */
	/*public static String toDbcCaseBySbcCase(String QJstr) {
		String outStr = "";
		String Tstr = "";
		byte[] b = null;
		for (int i = 0; i < QJstr.length(); i++) {
			try {
				Tstr = QJstr.substring(i, i + 1);
				b = Tstr.getBytes("unicode");
			} catch (java.io.UnsupportedEncodingException e) {
				LogUtils.info(StringConvertUtils.class, e);
			}
			if (b[3] == -1) {
				b[2] = (byte) (b[2] + 32);
				b[3] = 0;
				try {
					outStr = outStr + new String(b, "unicode");
				} catch (java.io.UnsupportedEncodingException ex) {
					LogUtils.info(StringConvertUtils.class, ex);
				}
			} else {
				outStr = outStr + Tstr;
			}
		}
		return outStr;
	}*/

	/**
	 * 解析前台encodeURIComponent编码后的参数
	 * 
	 * @param url 前端用urldecoder，编码后的url
	 * @param encoding 编码方式，如"UTF-8"
	 * @return
	 */
	public static String toDecodeStringUrl(String url,String encoding) {
		String trem = "";
		if (StringUtils.isNotEmpty(url)) {
			try {
				trem = URLDecoder.decode(url,encoding);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return trem;
	}
	
	/**
     * 数字转字符串
     * @param num
     * @param minValue 如果小于minValue，则输出""
     * @return
     */
    public static String getStringByNumbernumber(Number num,double minValue) {
        if (num == null) {
            return null;
        } else if (num instanceof Integer && (Integer) num > minValue) {
            return Integer.toString((Integer) num);
        } else if (num instanceof Long && (Long) num > minValue) {
            return Long.toString((Long) num);
        } else if (num instanceof Float && (Float) num > minValue) {
            return Float.toString((Float) num);
        } else if (num instanceof Double && (Double) num > minValue) {
            return Double.toString((Double) num);
        } else {
            return "";
        }
    }
    
    /**
     * 根据传入的分割符号,把传入的字符串分割为List字符串
     * 
     * @param slipSymbol
     *            分隔的字符串
     * @param src
     *            字符串
     * @return 列表
     */
    public static List<String> toStringListBySlip(String slipSymbol, String src) {

        if (src == null)
            return null;
        List<String> list = new ArrayList<String>();
        String[] result = src.split(slipSymbol);
        for (int i = 0; i < result.length; i++) {
            list.add(result[i]);
        }
        return list;
    }

	/**
	 * 驼峰风格字符串转为下划线连接的小写字符串
	 * @param param
	 * @return
	 */
	public static String camelToUnderline(String param) {
        if(StringUtils.isEmpty(param)) {
            return "";
        } else {
            int len = param.length();
            StringBuilder sb = new StringBuilder(len);

            for(int i = 0; i < len; ++i) {
                char c = param.charAt(i);
                if(Character.isUpperCase(c) && i > 0) {
                    sb.append('_');
                }

                sb.append(Character.toLowerCase(c));
            }

            return sb.toString();
        }
    }

	/**
	 * 下划线风格的字符串转为驼峰原则
	 * @param param
	 * @return
	 */
    public static String underlineToCamel(String param) {
        if(StringUtils.isEmpty(param)) {
            return "";
        } else {
            String temp = param.toLowerCase();
            int len = temp.length();
            StringBuilder sb = new StringBuilder(len);

            for(int i = 0; i < len; ++i) {
                char c = temp.charAt(i);
                if(c == 95) {
                    ++i;
                    if(i < len) {
                        sb.append(Character.toUpperCase(temp.charAt(i)));
                    }
                } else {
                    sb.append(c);
                }
            }

            return sb.toString();
        }
    }

	/**
	 * 首字母转小写
	 * @param param
	 * @return
	 */
	public static String firstToLowerCase(String param) {
        if(StringUtils.isEmpty(param)) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder(param.length());
            sb.append(param.substring(0, 1).toLowerCase());
            sb.append(param.substring(1));
            return sb.toString();
        }
    }
}
