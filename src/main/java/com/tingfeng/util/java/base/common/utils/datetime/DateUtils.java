package com.tingfeng.util.java.base.common.utils.datetime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tingfeng.util.java.base.common.inter.ConvertI;
import com.tingfeng.util.java.base.common.utils.ArrayUtils;
import com.tingfeng.util.java.base.common.utils.string.StringConvertUtils;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;

/**
 * 关于时间和日期的工具类，包含一些常用处理时间的函数
 * 因为SimpleDateFormat 是线程不安全的所以使用加锁处理
 * 
 */
public class DateUtils {
	/** DateFormat:yyyyMMddHHmmssSSS */
	public static final String FORMATE_YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";

	/** DateFormat:yyyyMMddHHmmss */
	public static final String FORMATE_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	/** DateFormat:yyyyMMdd */
	public static final String FORMATE_YYYYMMDD = "yyyyMMdd";
	/** DateFormat:HH:mm:ss */
	public static final String FORMATE_HHMMSS = "HHmmss";
	public static final String FORMATE_YYYYMM = "yyyyMM";
	public static final String FORMATE_YYYY = "yyyy";

	/** DateFormat:yyyy-MM-dd */
	public static final String FORMATE_YYYYMMDD_UNDERLIN = "yyyy-MM-dd";
	/** DateFormat:yyyy-MM */
	public static final String FORMATE_YYYYMM_UNDERLIN = "yyyy-MM";
	/** DateFormat:yyyy-MM-dd HH:mm:ss */
	public static final String FORMATE_YYYYMMDDHHMMSS_UNDERLIN = "yyyy-MM-dd HH:mm:ss";
	/** DateFormat:yyyy-MM-dd HH:mm:ss.SSS */
	public static final String FORMATE_YYYYMMDDHHMMSSSSS_UNDERLIN = "yyyy-MM-dd HH:mm:ss.SSS";

	/** 年月日格式 */
	public static final String FORMATE_YYYYMMDD_CHN = "yyyy年MM月dd日";
	/** 年月格式 */
	public static final String FORMATE_YYYYMM_CHN = "yyyy年MM月";

	/** 年月日格式时分秒 */
	public static final String FORMATE_YYYYMMDDHHMMSS_CHN = "yyyy年MM月dd日 HH:mm:ss";
	/** 年月日格式 时分秒毫秒 */
	public static final String FORMATE_YYYYMMDDHHMMSSSSS_CHN = "yyyy年MM月dd日 HH:mm:ss.SSS";

	/** yyyy/MM/dd */
	public static final String FORMATE_YYYYMMDD_OBLINE = "yyyy/MM/dd";
	/** yyyy/MM/dd HH:mm:ss */
	public static final String FORMATE_YYYYMMDDHHMMSS_OBLINE = "yyyy/MM/dd HH:mm:ss";
	/** yyyy/MM/dd HH:mm:ss.SSS */
	public static final String FORMATE_YYYYMMDDHHMMSSSSS_OBLINE = "yyyy/MM/dd HH:mm:ss.SSS";

	private static final Map<String,SimpleDateFormat> DATE_FORMATE_MAP = new ConcurrentHashMap<>(25); 
	
	//初始化,对常用的格式进行缓存
	static {
	    DATE_FORMATE_MAP.put(FORMATE_YYYYMMDDHHMMSSSSS,new SimpleDateFormat(FORMATE_YYYYMMDDHHMMSSSSS));
	    DATE_FORMATE_MAP.put(FORMATE_YYYYMMDDHHMMSS,new SimpleDateFormat(FORMATE_YYYYMMDDHHMMSS));
	    DATE_FORMATE_MAP.put(FORMATE_YYYYMMDD,new SimpleDateFormat(FORMATE_YYYYMMDD));
	    DATE_FORMATE_MAP.put(FORMATE_HHMMSS,new SimpleDateFormat(FORMATE_HHMMSS));
	    DATE_FORMATE_MAP.put(FORMATE_YYYYMM,new SimpleDateFormat(FORMATE_YYYYMM));
	    DATE_FORMATE_MAP.put(FORMATE_YYYY,new SimpleDateFormat(FORMATE_YYYY));
	    
	    DATE_FORMATE_MAP.put(FORMATE_YYYYMMDD_UNDERLIN,new SimpleDateFormat(FORMATE_YYYYMMDD_UNDERLIN));
	    DATE_FORMATE_MAP.put(FORMATE_YYYYMM_UNDERLIN,new SimpleDateFormat(FORMATE_YYYYMM_UNDERLIN));
	    DATE_FORMATE_MAP.put(FORMATE_YYYYMMDDHHMMSS_UNDERLIN,new SimpleDateFormat(FORMATE_YYYYMMDDHHMMSS_UNDERLIN));
	    DATE_FORMATE_MAP.put(FORMATE_YYYYMMDDHHMMSSSSS_UNDERLIN,new SimpleDateFormat(FORMATE_YYYYMMDDHHMMSSSSS_UNDERLIN));
	    
	    DATE_FORMATE_MAP.put(FORMATE_YYYYMMDD_CHN,new SimpleDateFormat(FORMATE_YYYYMMDD_CHN));
	    DATE_FORMATE_MAP.put(FORMATE_YYYYMM_CHN,new SimpleDateFormat(FORMATE_YYYYMM_CHN));
	    DATE_FORMATE_MAP.put(FORMATE_YYYYMMDDHHMMSS_CHN,new SimpleDateFormat(FORMATE_YYYYMMDDHHMMSS_CHN));
	    DATE_FORMATE_MAP.put(FORMATE_YYYYMMDDHHMMSSSSS_CHN,new SimpleDateFormat(FORMATE_YYYYMMDDHHMMSSSSS_CHN));
	    
	    DATE_FORMATE_MAP.put(FORMATE_YYYYMMDD_OBLINE,new SimpleDateFormat(FORMATE_YYYYMMDD_OBLINE));
	    DATE_FORMATE_MAP.put(FORMATE_YYYYMMDDHHMMSS_OBLINE,new SimpleDateFormat(FORMATE_YYYYMMDDHHMMSS_OBLINE));
	    DATE_FORMATE_MAP.put(FORMATE_YYYYMMDDHHMMSSSSS_OBLINE,new SimpleDateFormat(FORMATE_YYYYMMDDHHMMSSSSS_OBLINE));
	}
	
	private static SimpleDateFormat getSimpleDateFormat(String formateString) {
	    SimpleDateFormat simpleDateFormat = DATE_FORMATE_MAP.get(formateString);
	    if( simpleDateFormat == null) {
	        return new SimpleDateFormat(formateString);
	    }
	    return simpleDateFormat;
	}
	
	/*************************************
	 * 时间获得与计算
	 **************************************/

	/**
	 * 根据当前时间设定相关参数值
	 * 
	 * @param hour
	 * @param minute
	 * @param second
	 * @param milliSecond
	 * @return
	 */
	public static Calendar getInitCalendar(int hour, int minute, int second, int milliSecond) {
		Calendar calendar = Calendar.getInstance();
		return getInitCalendar(calendar, hour, minute, second, milliSecond);
	}

	/**
	 * 根据指定时间设定相关参数值
	 * 
	 * @param calendar
	 * @param hour
	 * @param minute
	 * @param second
	 * @param milliSecond
	 * @return
	 */
	public static Calendar getInitCalendar(Calendar calendar, int hour, int minute, int second, int milliSecond) {
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, milliSecond);
		return calendar;
	}

	/**
	 * 根据指定时间设定相关参数值
	 * 
	 * @param date
	 * @param hour
	 * @param minute
	 * @param second
	 * @param milliSecond
	 * @return
	 */
	public static Calendar getInitCalendar(Date date, int hour, int minute, int second, int milliSecond) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return getInitCalendar(calendar,hour,minute,second,milliSecond);
	}

	/**
	 * 得到当日凌晨
	 * 
	 * @addTime 返回结果的时候增加的毫秒数;
	 * @return
	 */
	public static Date getDayBegin(Date date, int addTime) {
		Calendar calendar = getInitCalendar(date, 0, 0, 0, 0);
		calendar.add(Calendar.MILLISECOND, addTime);
		return calendar.getTime();
	}

	/**
	 * 得到下一日的首日凌晨
	 * 
	 * @addTime 返回结果的时候增加的毫秒数;
	 * @return
	 */
	public Date getNextDayBegin(Date date, int addTime) {
		Calendar calendar = getInitCalendar(date, 0, 0, 0, 0);
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		calendar.add(Calendar.MILLISECOND, addTime);
		return calendar.getTime();
	}

	/**
	 * 得到一天的开始时间
	 */
	public static Date getDayBegin(Date date) {
		return getDayBegin(date, 0);
	}

	/**
	 * 得到一天的结束时间
	 * 
	 * @param date
	 * @return
	 */
	public static Date getDayEnd(Date date) {
		return getInitCalendar(date, 23, 59, 59, 999).getTime();
	}

	/**
	 * 获取指定时间周开始时间
	 * 
	 * @return
	 */
	public static Date getBeginDayOfWeek(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			calendar.add(Calendar.DAY_OF_MONTH, -7);
		}
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		Date monDayTime = getDayBegin(calendar.getTime());
		return monDayTime;

	}

	/**
	 * 获取指定时间周结束时间
	 * 
	 * @return
	 */
	public static Date getEndDayOfWeek(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
			calendar.add(Calendar.DAY_OF_MONTH, 7);
		}
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		Date sundayTime = getDayEnd(calendar.getTime());
		return sundayTime;
	}

	/**
	 * 得到下个月的首日凌晨
	 * 
	 * @addTime 返回结果的时候增加的毫秒数;
	 * @return
	 */
	public static Date getNextMonthFirstTime(Date date, int addTime) {
		Calendar calendar = getInitCalendar(0, 0, 0, 0);
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.MILLISECOND, addTime);
		Date dateTmp = calendar.getTime();
		return dateTmp;
	}

	/**
	 * 两个时间相差的年份; 其中小于一年的将会0; 主要可以用于生日等;精确到每一日;
	 * 
	 * @return
	 */
	public static int getYearCountBetweenTwoDate(Date dateA, Date dateB) {
		int count = 0;
		String formatString = "yyyy:MM:dd";
		String[] sa, sb;
		// 包装sa的值大于sb;
		if (dateA.getTime() < dateB.getTime()) {
			sb = StringConvertUtils.getStringByDate(dateA, formatString).split(":");
			sa = StringConvertUtils.getStringByDate(dateB, formatString).split(":");
		} else {
			sa = StringConvertUtils.getStringByDate(dateA, formatString).split(":");
			sb = StringConvertUtils.getStringByDate(dateB, formatString).split(":");
		}
		ConvertI<Integer, Object> convertI = new ConvertI<Integer, Object>() {
			@Override
			public Integer convert(Object s) {
				return Integer.parseInt(s.toString());
			}
		};
		Integer[] arrayA = ArrayUtils.getArray(sa, convertI);
		Integer[] arrayB = ArrayUtils.getArray(sb, convertI);
		count = arrayA[1] - arrayB[1];
		// 比较月份;
		if (count > 0 && arrayA[2] < arrayB[2]) {
			return count - 1;
		}
		// 比较日期
		if (count > 0 && arrayA[3] < arrayB[3]) {
			return count - 1;
		}
		return count;
	}

	public static int getYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	public static int getMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH) + 1;
	}

	public static int getDayOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * @param date
	 * @return 返回一个长度为7的一维数组,索引0到索引6依次保存，当前周的周一到周日的时间;
	 * @throws ParseException
	 */
	public static Date[] getRecentlyWeekDate(Date date) {
		Date[] weekDates = new Date[7];
		int week = getDayOfWeek(date);// 获取周几,1表示星期天、2表示星期一、7表示星期六
		int maxWeek = 7;
		/**
		 * position 当期是星期几
		 */
		int position = 1;
		if (week == 1) {
			position = 7;
		} else {
			position = week - 1;
		}
		// 计算从周一到当前的日期
		for (int i = 1; i < position; i++) {
			weekDates[position - i - 1] = getDateAdd(date, -i);
		}
		// 计算当前到周日的日期
		for (int i = 0; i <= maxWeek - position; i++) {
			weekDates[position + i - 1] = getDateAdd(date, i);
		}

		return weekDates;
	}

	/**
	 * 得到从当前开始的之后一周的时间的数组;
	 * 
	 * @param date
	 * @return
	 */
	public static Date[] getWeekDatesFromNow(Date date) {
		Date[] weekDates = new Date[7];
		for (int i = 0; i < 7; i++) {
			weekDates[i] = getDateAdd(date, i);
		}
		return weekDates;
	}

	/**
	 * 根据指定的年、月、日返回当前是星期几。1表示星期天、2表示星期一、7表示星期六。
	 * 
	 * @param year
	 * @param month
	 *            month是从1开始的12结束
	 * @param day
	 * @return 返回一个代表当期日期是星期几的数字。1表示星期天、2表示星期一、7表示星期六。
	 */
	public static int getDayOfWeek(String year, String month, String day) {
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.set(new Integer(year).intValue(), new Integer(month).intValue() - 1, new Integer(day).intValue());
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * 根据指定的年、月、日返回当前是星期几。1表示星期天、2表示星期一、7表示星期六。
	 * @return 返回一个代表当期日期是星期几的数字。1表示星期天、2表示星期一、7表示星期六。
	 */
	public static int getDayOfWeek(Date date) {
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * 取得给定日期加上(减去)一定天数后的日期对象.
	 * 
	 * @param date
	 *            给定的日期对象
	 * @param amount
	 *            需要添加的天数，如果是向前的天数，使用负数就可以.
	 * @param format
	 *            格式 "yyyy-MM-dd HH:mm:ss" 输出格式.
	 * @return Date 加上一定天数以后的Date对象.
	 */
	public static String getDateString(Date date, int amount, String format) {
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.setTime(date);
		cal.add(Calendar.DATE, amount);
		return getDateString(cal.getTime(), format);
	}

	/**
	 * 取得给定日期加上(减去)一定天数后的日期对象.
	 * 
	 * @param date
	 *            给定的日期对象
	 * @param amount
	 *            需要添加的天数，如果是向前的天数，使用负数就可以.
	 */
	public static Date getDateAdd(Date date, int amount) {
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.setTime(date);
		cal.add(Calendar.DATE, amount);
		return cal.getTime();
	}

	/*************************************
	 * 时间获得与计算
	 **************************************/

	/*************************************
	 * 时间转为字符串
	 ***************************************/
	/**
	 * 得到年月日字符串
	 * 
	 * @needDay 是否需要日字符串
	 * @return 如20170816
	 */
	public static String getDateString(Date date, boolean needDay) {
		StringBuilder sb = new StringBuilder();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar = getInitCalendar(calendar, 0, 0, 0, 0);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;

		sb.append(year);
		if (month < 10) {
			sb.append("0");
		}
		sb.append(month);

		if (needDay) {
			if (calendar.get(Calendar.DAY_OF_MONTH) < 10) {
				sb.append("0");
			}
			sb.append(calendar.get(Calendar.DAY_OF_MONTH));
		}
		return sb.toString();
	}

	/**
	 * 根据给定的格式与时间(Date类型的)，返回时间字符串。最为通用。<br>
	 * 
	 * @param date
	 *            指定的日期
	 * @param format
	 *            日期格式字符串
	 * @return String 指定格式的日期字符串.
	 */
	public synchronized static String getDateString(Date date, String format) {
		SimpleDateFormat sdf = getSimpleDateFormat(format);
		return sdf.format(date);
	}

	/**
	 * 默认格式化 yyyy-MM-dd HH:mm:ss
	 * 
	 * @param date
	 * @return
	 */
	public static String getDateString(Date date) {
		return getDateString(date, DateUtils.FORMATE_YYYYMMDDHHMMSS_UNDERLIN);
	}

	/*************************************
	 * 时间转为字符串
	 ***************************************/

	/*************************************
	 * 字符串转为时间
	 **************************************/

	/**
	 * 
	 * @param value
	 * @param formateString
	 *            "yyyy-MM-dd HH:mm:ss"等支持的格式
	 * @param defaultValue
	 *            异常或者value是null时返回默认值
	 * @return 默认先用Long来获取毫秒数,失败后再用时间格式来获取相应的时间;
	 */
	public synchronized static Date getDate(String value, String formateString, Date defaultValue) {
		SimpleDateFormat sdf = getSimpleDateFormat(formateString);
		if (value == null)
			return defaultValue;
		Date date = null;
		try {
			date = sdf.parse(value);
		} catch (Exception e) {
			date = defaultValue;
		}
		return date;
	}

	/**
	 * "yyyy-MM-dd HH:mm:ss"支持的格式
	 * 
	 * @param value
	 * @param defaultValue
	 * @return
	 */
	public static Date getDate(String value, Date defaultValue) {
		return getDate(value, FORMATE_YYYYMMDDHHMMSS_UNDERLIN, defaultValue);
	}

	/**
	 * 
	 * @param value
	 * @param formateString
	 *            "yyyy-MM-dd HH:mm:ss"等支持的格式
	 * @return
	 */
	public static Date getDate(String value, String formateString) {
		return getDate(value, formateString, null);
	}

	/** isAutoConvert是false时返回,"yyyy-MM-dd HH:mm:ss"支持的格式
	 * @param value
	 * @param isAutoConvert 是否根据输入的值,自动猜测时间格式并转换 ,支持:DateUtils中所列出的常量格式类型的自动猜测转换
	 * @return 
	 */
	public static Date getDate(String value,boolean isAutoConvert) {
		if(isAutoConvert) {
			if(StringUtils.isEmpty(value)) {
				return null;
			}
			value = value.replaceAll("[^\\d]","");
			if(value.length() == 13){//默认识别为毫秒数量
				return new Date(Long.parseLong(value));
			}
			StringBuilder sb = new StringBuilder(30);
			sb.append(value);
			for(int  i = value.length() + 1 ; i < 18; i++) {//初始化数据
				if(i == 5 || i == 7 || i >= 9){
					sb.append(0);
				}
				if(i == 6 || i == 8){
					sb.append(1);
				}

			}
			return getDate(sb.toString(), FORMATE_YYYYMMDDHHMMSSSSS);
		}else {
			return getDate(value, FORMATE_YYYYMMDDHHMMSS_UNDERLIN, null);
		}	
	}

	/** 默认转换自动转换日期格式支持的格式
	 * @param value
	 * @return
	 */
	public static Date getDate(String value) {
		return getDate(value,true);
	}

	/*************************************
	 * 字符串转为时间
	 **************************************/

}
