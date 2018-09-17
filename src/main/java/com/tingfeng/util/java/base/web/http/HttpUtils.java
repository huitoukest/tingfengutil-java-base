package com.tingfeng.util.java.base.web.http;

import com.tingfeng.util.java.base.common.bean.HttpResponseInfo;
import com.tingfeng.util.java.base.common.constant.Constants;
import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.utils.RegExpUtils;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 待测试,目前不支持Https
 * @author huitoukest
 *
 */
@Slf4j
public class HttpUtils {

    /**
     * 向指定URL发送GET方法的请求
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     * String sr=HttpRequest.sendPost("http://localhost:6144/Home/RequestPostString", "key=123&v=456");
     */
    public static HttpResponseInfo sendGet(String url, Map<String,Object> param) {
           String getUrl = toGetUrl(url,param);
           String getParams  = null;
           return sendGet(getUrl,getParams);
    }

	/**
     * 向指定URL发送GET方法的请求
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     * String sr=HttpRequest.sendPost("http://localhost:6144/Home/RequestPostString", "key=123&v=456");
     */
    public static HttpResponseInfo sendGet(String url, String param) {
        HttpResponseInfo responseInfo = new HttpResponseInfo();
		String result = "";
	    BufferedReader in = null;
        URLConnection connection = null;
	    try {
	        String urlNameString = url ;
	        if(StringUtils.isNotEmpty(param)) {
              urlNameString += "?" + param;
            }
	        URL realUrl = new URL(urlNameString);
	        // 打开和URL之间的连接
            connection = realUrl.openConnection();
	        // 设置通用的请求属性
	        connection.setRequestProperty("accept", "*/*");
	        connection.setRequestProperty("connection", "Keep-Alive");
	        connection.setRequestProperty("user-agent",
	                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
	        // 建立实际的连接
	        connection.connect();
	        // 获取所有响应头字段
	        Map<String, List<String>> map = connection.getHeaderFields();
	        // 遍历所有的响应头字段
            responseInfo.setHeaders(map);
            List<String> statusList = map.get(null);
            if (map != null && statusList != null){
                if(statusList != null && !statusList.isEmpty()){
                    String status = statusList.get(0);
                    if(null != status){
                        Pattern pattern = RegExpUtils.getPattern(RegExpUtils.PATTERN_STR_HTTP_STATUS);
                        Matcher m = pattern.matcher(status);
                        if(m.find()) {
                            status = m.group(1);
                        }
                        Integer s = StringUtils.getInteger(status,0);
                        responseInfo.setStatus(s);
                    }
                }
            }else{
                responseInfo.setStatus(200);
            }
	        // 定义 BufferedReader输入流来读取URL的响应
	       in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	       if(null != in) {
               final BufferedReader appendIn = in;
               result = StringUtils.doAppend(sb -> {
                   String line = null;
                   while ((line = appendIn.readLine()) != null) {
                       sb.append(line);
                   }
                return sb.toString();
               });
           }
            responseInfo.setBody(result);
	    } catch (Exception e) {
	        throw new BaseException(e);
	    }
	    // 使用finally块来关闭输入流
	    finally {
	        try {
	            if (in != null) {
	                in.close();
	            }
	        } catch (Throwable e) {
	            log.error("close stream error",e);
	        }
	    }
	    return responseInfo;
    }

    public static String sendPostByJson(String url,String jsonObject,String charSet){
        return sendPost(url,jsonObject,"application/json",charSet);
    }
    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     * String sr=HttpRequest.sendPost("http://localhost:6144/Home/RequestPostString", "key=123&v=456");
     */
    public static String sendPost(String url, String param, String contentType, String reqCharset) {
        OutputStreamWriter out = null;
       // OutputStreamWriter out=null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection)realUrl.openConnection();
            // 设置通用的请求属性
            if(null != reqCharset) {
                conn.setRequestProperty("Charset", "charset=" + reqCharset);
            }
            // 设置文件类型:
            conn.setRequestProperty("Content-Type",contentType);
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            // 获取URLConnection对象对应的输出流
            out = new  OutputStreamWriter(conn.getOutputStream(),reqCharset);
            // 发送请求参数
            out.write(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 把 名=值 参数表转换成字符串 (url + ?a=1&b=2)
     *
     * @param url    url可以为null，为null则返回参数组成的字符串a=1&b=2
     * @param params url中的参数
     * @return
     */
    public static String toGetUrl(String url, Map<String, Object> params) {
        return StringUtils.doAppend(sb->{
                if (null != url) {
                    sb.append(url);
                }
                if (null != params && !params.isEmpty()) {
                    int i = 0;
                    for (String key : params.keySet()) {
                        Object value = params.get(key);
                        if (value != null) {
                            if (i == 0 && null != url) {
                                sb.append("?");
                            } else {
                                sb.append("&");
                            }
                            sb.append(key);
                            sb.append("=");
                            sb.append(value);
                        }
                        i++;
                    }
                }
                return sb.toString();
         });
    }

    /**
     * 解析字符串返回 名称=值的参数表 (a=1&b=2 => a=1,b=2)
     * 通过解析Get的参数url来得到参数
     * @param str
     * @return 如果str是null或者空串，返回空的Map
     */
    public static HashMap<String, String> parseGetParams(String str) {
        HashMap<String, String> result = new HashMap<String, String>();
        if (str != null && !str.equals("") && str.indexOf("=") > 0) {
            String name = null;
            String value = null;
            int i = 0;
            while (i < str.length()) {
                char c = str.charAt(i);
                switch (c) {
                    case 61: // =
                        value = "";
                        break;
                    case 38: // &
                        if (name != null && value != null && !name.equals("")) {
                            result.put(name, value);
                        }
                        name = null;
                        value = null;
                        break;
                    default:
                        if (value != null) {
                            value = (value != null) ? (value + c) : "" + c;
                        } else {
                            name = (name != null) ? (name + c) : "" + c;
                        }
                }
                i++;
            }
            if (name != null && value != null && !name.equals("")) {
                result.put(name, value);
            }
        }
        return result;
    }

    /**
     * 得到处理后的可用url,如果relativeUrl是http或https开头的连接
     * 那么不处理，否者返回urlPrefix + relativeUrl.trim()的值
     * @param urlPrefix
     * @param relativeUrl
     * @return
     */
    public static String getFileUrl(String urlPrefix,String relativeUrl){
        if(StringUtils.isEmpty(relativeUrl)){
            return "";
        }

        if(StringUtils.isEmpty(urlPrefix)){
            return relativeUrl;
        }

        relativeUrl = relativeUrl.trim();

        if(relativeUrl.startsWith(Constants.HttpConfig.KEY_HTTPS) || relativeUrl.startsWith(Constants.HttpConfig.KEY_HTTP)){
            return relativeUrl;
        }

        return urlPrefix + relativeUrl;
    }
}
