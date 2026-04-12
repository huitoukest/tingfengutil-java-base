package com.tingfeng.util.java.base.file.csv;

/**
 * @author huitoukest
 * CSV文件读取
 * @param <T>
 */
public interface CSVReader<T> {
	/**
	 * 读取行,调用getObject方法,传入t
	 * @param t 读取道的单行数据
	 */
	 void read(T t);
	/**
	 * 将一行的内容转为T
	 * @param line 当前行的内容
	 * @return 需要的 T 类型 对象
	 */
	 T getObject(String line);
}
