package com.tingfeng.util.java.base.file.csv;
import java.util.List;

/**
 * 分页读取导出CSV格式
 * @author huitoukest
 * @param <T> 读取的数据返回的domain类型
 */
public interface CSVPageWriter<T> {
	 long getTotalCount();
	 long getMaxExportCount();
	/**
	 * 当导出的记录数量超过最大导出数量的时候,抛出此异常
	 * @return
	 */
	 RuntimeException getOverMaxExportCountException();
	/**
	 * 每次获取数据的传入参数
	 * @param params
	 * @return
	 */
	 List<T> getList(Object ...params);
	/**
	 * 
	 * @return 返回单次获取的数据量
	 */
	 int getCountOfPerExport();
	/**
	 * 返回表头
	 */
	 String[] getTableHearder();
	/**
	 * 返回单行表格数据
	 * @return
	 */
	 Object[] getTableLineData(T t);
}
