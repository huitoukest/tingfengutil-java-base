package com.tingfeng.util.java.base.database.hbase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class HbaseConnectUtils{
		/**
		 * 
		 * @param host 连接host
		 * @param port 连接端口号,默认10000
		 * @param dbName 连接数据库名称,默认default
		 * @param name 连接用户名,默认""
		 * @param password 连接密码,默认""
		 * @return
		 * @throws ClassNotFoundException
		 * @throws SQLException
		 */
		public static Connection getHiveConnect(String host,String port,String dbName,String name,String password) throws ClassNotFoundException, SQLException{
			String driverName = "org.apache.hadoop.hive.jdbc.HiveDriver";
			if(port==null||port.trim().length()<1){
				port="10000";
			}
			if(dbName==null||dbName.trim().length()<1){
				dbName="default";
			}
			String url = "jdbc:hive://"+host+port+"/"+dbName;  
			Class.forName(driverName);  
            return  DriverManager.getConnection(url, name, password);  
		}
		/**
		 * 
		 * @param host 连接host
		 * @param port 连接端口号,默认10000
		 * @param dbName 连接数据库名称,默认default
		 * @param name 连接用户名,默认""
		 * @param password 连接密码,默认""
		 * @return
		 * @throws ClassNotFoundException
		 * @throws SQLException
		 */
		public static Connection getHive2Connect(String host,String port,String dbName,String name,String password) throws ClassNotFoundException, SQLException{
			String driverName = "org.apache.hadoop.hive.jdbc.HiveDriver";
			if(port==null||port.trim().length()<1){
				port="10000";
			}
			if(dbName==null||dbName.trim().length()<1){
				dbName="default";
			}
			String url = "jdbc:hive2://"+host+port+"/"+dbName;  
			Class.forName(driverName);  
            return  DriverManager.getConnection(url, name, password);  
		}
}
