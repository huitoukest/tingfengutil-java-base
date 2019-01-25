package com.tingfeng.util.java.base.common;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.tingfeng.util.java.base.common.utils.datetime.DateUtils;

public class DateUtilsTest {
	
	@Test
	public void test() {
		String[] testValue = new String[] {"2017年10月20日 14:35:55.777","2017-10:20","2017-10:20 14:35:55.777","2017/1020 14:35:55.777",
				"20171020 143555777","20171020143555777","2017/10/20/14:3555.777","1548400055509"};
		for(String value : testValue) {
			Date date = DateUtils.getDate(value,true);
			String formateString = DateUtils.getDateString(date);
			System.out.println(formateString);		 
		}
		
	}

	@Test
	public void getDayEndTest(){
		Date date = new Date();
		long time  = DateUtils.getDayEnd(date).getTime();
		String str = DateUtils.getDateString(new Date(time),DateUtils.FORMATE_YYYYMMDDHHMMSSSSS_UNDERLIN);
		System.out.println(str);
		Assert.assertTrue(str.endsWith("23:59:59.999"));
	}
}
