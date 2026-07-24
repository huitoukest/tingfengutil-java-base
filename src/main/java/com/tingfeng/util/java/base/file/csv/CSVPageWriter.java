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
	 * @return 超过最大导出数量时的异常(自定义的)
	 */
	 RuntimeException getOverMaxExportCountException();
	/**
	 * 每次获取数据的传入参数
	 * @param params 参数信息
	 * @return 参数对应的结果数据
	 */
	 List<T> getList(Object ...params);
	/**
	 * 
	 * @return 返回单次获取的数据量
	 */
	 int getCountOfPerExport();
	/**
	 * @return 表头
	 */
	 String[] getTableHeader();
	/**
	 *
	 * @return 单行表格数据
	 */
	 Object[] getTableLineData(T t);
}
