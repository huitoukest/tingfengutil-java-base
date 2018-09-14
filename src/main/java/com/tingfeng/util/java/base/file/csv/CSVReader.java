package com.tingfeng.util.java.base.file.csv;

/**
 * @author huitoukest
 * CSV文件读取
 * @param <T>
 */
public interface CSVReader<T> {
	/**
	 * 读取行,调用getObject方法,传入t
	 * @param t
	 */
	 void read(T t);
	/**
	 * 将一行的内容转为T
	 * @param line
	 * @return
	 */
	 T getObject(String line);
}
