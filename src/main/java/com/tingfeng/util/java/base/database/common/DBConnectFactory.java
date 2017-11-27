package com.tingfeng.util.java.base.database.common;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnectFactory {
	/**
	 * 
	 * @param host 连接host
	 * @param port 连接端口号,传入null或者""将会使用默认端口号
	 * @param dbName 连接数据库名称,传入null或者""将会使用默认数据库
	 * @param name 连接用户名
	 * @param password 连接密码
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static Connection getConnection(String host,String port,String dbName,String name,String password,DBType type) throws ClassNotFoundException, SQLException{
		switch (type) {
		case  HIVE:
			  return getHiveConnect(host, port, dbName, name, password);
		case HIVE2:
			  return getHive2Connect(host, port, dbName, name, password);
		default:
			break;
		}
		return null;
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
	public static Connection getHiveConnect(String host,String port,String dbName,String name,String password) throws ClassNotFoundException, SQLException{
		String driverName = "org.apache.hadoop.hive.jdbc.HiveDriver";
		DBConnBean dBean=new DBConnBean("jdbc:hive:", host, port, dbName, name, password, driverName, "10000", "defalut");
        return  dBean.getConnection();
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
		DBConnBean dBean=new DBConnBean("jdbc:hive2:", host, port, dbName, name, password, driverName, "10000", "defalut");
        return  dBean.getConnection();
	}
}
