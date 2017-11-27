package com.tingfeng.util.java.base.file.csv;
import java.util.List;

/**
 * 流式读取导出CSV格式
 * @author huitoukest
 * @param T 读取的数据返回的domain类型
 */
public interface CSVStreamWriter<T> {
	public long getTotalCount();	
	public long getMaxExportCount();
	/**
	 * 当导出的记录数量超过最大导出数量的时候,抛出此异常
	 * @param e
	 * @return
	 * @throws E
	 */
	public Exception getOverMaxExportCountException();
	/**
	 * 每次获取数据的传入参数
	 * @param params
	 * @return
	 */
	public List<T> getList(Object ...params);
	/**
	 * 
	 * @return 返回单次获取的数据量
	 */
	public int getCountOfPerExport();
	/**
	 * 返回表头
	 * @param sb
	 */
	public String[] getTableHearder();
	/**
	 * 返回单行表格数据
	 * @param sb
	 * @return
	 */
	public Object[] getTableLineData(T t);
}
