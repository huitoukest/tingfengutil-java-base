package com.tingfeng.util.java.base.common.helper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 采用map缓存取到过的属性
 * @author huitoukest
 *
 */
@SuppressWarnings("static-access")
public class PropertyHelper {
	private static Map<String, String> propertyMap = new HashMap<String, String>();  
	
    private String fileName;  
    public static String PROPERTIES_PATH = "";  
    
	public PropertyHelper(String fileName) {  
    	this.PROPERTIES_PATH = this.getClass().getResource("/").getPath()+"/";
        this.fileName = PROPERTIES_PATH + fileName;  
    }  
      /**
       * 此方法默认会从缓存中获取数据，当缓存中不存在数据的时候在文件读取数据
       * @param key
       * @return
     * @throws IOException 
     * @throws FileNotFoundException 
       */
	 public String getProperty(String key) throws FileNotFoundException, IOException{
		return this.getProperty(key, true);
	 }
    public String getProperty(String key,boolean useCache) throws FileNotFoundException, IOException {  
    	String value = null;
    	if(useCache==true)
    	{
    		value = PropertyHelper.propertyMap.get(key);
    	}
        Properties property = new Properties();  
        FileInputStream inputFile = null;  
          
        if (value == null) {  
            // 实例化inputFile,如config.properties文件的位置   
            inputFile = new FileInputStream(this.fileName);  
            // 装载配置文件   
            property.load(inputFile);  
            value = property.getProperty(key);  
            PropertyHelper.propertyMap.put(key, value);  
        }  
        return value;  
    }  
    
    public Map<String,String>  getAllProperty() throws IOException{ 	
    	// 定义Map用于存放结果   
        Map<String,String> propertyMap = PropertyHelper.propertyMap;  
        // 定义Properties property = new Properties();   
        Properties property = new Properties();  
        // 定义FileInputStream inputFile = null;     
        FileInputStream inputFile = null;          
        try {  
            // 实例化inputFile   
            inputFile = new FileInputStream(this.fileName);  
            // 装载配置文件   
            property.load(inputFile);  
            Set<Object> keys=property.keySet();
            for (Object ss :keys) {  
                // 从配置文件中获取属性存入map中   
                String data = property.getProperty(ss.toString());  
                propertyMap.put(ss.toString(), data);  
            }  
          
        } finally {  
            // 关闭输入流   
            if (inputFile != null) {  
                inputFile.close();  
            }  
        }  
          
        return propertyMap;  
    }
    
    public Map<String,String> getProperty(List<String> propertyList) throws FileNotFoundException, IOException {  
        // 定义Map用于存放结果   
        Map<String,String> propertyMap = PropertyHelper.propertyMap;  
        // 定义Properties property = new Properties();   
        Properties property = new Properties();  
        // 定义FileInputStream inputFile = null;     
        FileInputStream inputFile = null;  
          
        try {  
            // 实例化inputFile   
            inputFile = new FileInputStream(this.fileName);  
            // 装载配置文件   
            property.load(inputFile);  
            for (String name : propertyList) {  
                // 从配置文件中获取属性存入map中   
                String data = property.getProperty(name);  
                propertyMap.put(name, data);  
            }  
          
        } finally {  
            // 关闭输入流   
            if (inputFile != null) {  
                inputFile.close();  
            }  
        }  
          
        return propertyMap;  
    }  
}
