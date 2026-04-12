package com.tingfeng.util.java.base.database.common;

public class SqlUtils {
	public static final String badStr = "'|and|exec|execute|insert|select|delete|update|count|drop|*|%|chr|mid|master|truncate|" +
            "char|declare|sitename|net user|xp_cmdshell|;|-|or|+|,|like'|and|exec|execute|insert|create|drop|" +
            "table|from|grant|use|group_concat|column_name|" +
            "information_schema.columns|table_schema|union|where|select|delete|update|order|by|count|*|" +
            "chr|mid|master|truncate|char|declare|or|;|--|+|,|like|//|/|%|#";//过滤掉的sql关键字，可以手动添加
	public SqlUtils() {
		
	}
	/**
	 * 是否包含一个sql关键字
	 * @param str
	 * @return
	 */
	public static boolean sqlValidate(String str) {
        str = str.toLowerCase();//统一转为小写      
        String[] badStrs = badStr.split("\\|");
        String[] values=str.split("\\s");
        for (int i = 0; i < badStrs.length; i++) {
            for(String v:values){
            	if (v.equals(badStrs[i])) {
                    return true;
                }
            }
        	
        }
        return false;
    }
}
