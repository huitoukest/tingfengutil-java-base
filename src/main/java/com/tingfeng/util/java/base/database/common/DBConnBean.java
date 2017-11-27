package com.tingfeng.util.java.base.database.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnBean {
	protected String urlPrefix;
	protected String host;
	protected String port;
	protected String dbName;
	protected String name;
	protected String password;
	protected String driverName;
	protected String defalutPort;
	protected String defalutDataBase;

	public DBConnBean(String urlPrefix, String host, String port,
			String dbName, String name, String password, String driverName,
			String defalutPort, String defalutDataBase) {
		super();
		this.urlPrefix = urlPrefix;
		this.host = host;
		this.port = port;
		this.dbName = dbName;
		this.name = name;
		this.password = password;
		this.driverName = driverName;
		this.defalutPort = defalutPort;
		this.defalutDataBase = defalutDataBase;
	}

	public Connection getConnection() throws ClassNotFoundException, SQLException{
		if(port==null||port.trim().length()<1){
			port=defalutPort;
		}
		if(dbName==null||dbName.trim().length()<1){
			dbName=defalutDataBase;
		}
		String url = urlPrefix+"//"+host+port+"/"+dbName;  
		Class.forName(driverName);  
        return  DriverManager.getConnection(url, name, password);  
	}
}
