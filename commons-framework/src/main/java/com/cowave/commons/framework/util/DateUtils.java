/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils{

    public static final String YYYY = "yyyy";

    public static final String YYYY_MM = "yyyy-MM";

    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static final String[] PARSE_PATTERNS = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

	/**
	 * 格式化当前时间
	 */
	public static String format(DateFormat format) {
		return format.format(new Date());
	}

    /**
     * 格式化当前时间
     */
    public static String format(String format) {
    	return new SimpleDateFormat(format).format(new Date());
    }

	/**
	 * 格式化时间
	 */
	public static String format(Date date) {
		return new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS).format(date);
	}

    /**
     * 格式化时间
     */
    public static String format(Date date, DateFormat format) {
    	return format.format(date);
    }

    /**
     * 格式化时间
     */
    public static String format(Date date, String format) {
    	return new SimpleDateFormat(format).format(date);
    }

    /**
     * 反格式化日期
     */
    public static Date parse(String date, DateFormat format) {
    	try {
			return format.parse(date);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
    }

    /**
     * 反格式化日期
     */
    public static Date parse(String date, String format) {
    	try {
			return new SimpleDateFormat(format).parse(date);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
    }

    /**
     * 反格式化日期
     */
    public static Date parse(String date){
        if (date == null){
            return null;
        }
        try{
            return parseDate(date, PARSE_PATTERNS);
        }catch (ParseException e){
        	throw new IllegalArgumentException(e);
        }
    }

    /**
     * 相对当前时间，根据偏移量计算日期
     */
    public Date shift(int shift, int calendarField) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(new Date());
    	calendar.add(calendarField, shift);
    	return calendar.getTime();
    }

    /**
     * 相对当前时间，根据偏移量计算日期，并设置时分秒
     */
    public Date shiftAndSet(int shift, int calendarField, Integer hour, Integer minute, Integer second) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(new Date());
    	calendar.add(calendarField, shift);

    	if(hour != null) {
    		calendar.set(Calendar.HOUR_OF_DAY, hour);
    	}
    	if(minute != null) {
    		calendar.set(Calendar.MINUTE, minute);
    	}
    	if(second != null) {
    		calendar.set(Calendar.SECOND, second);
    	}
    	return calendar.getTime();
    }

    /**
     * 相对当前时间，根据偏移量获取格式化时间列表
     */
    public List<String> getFormatListWithShift(int shift, int calendarField, String format) {
    	return getFormatListWithShift(shift, calendarField, format, new Date());
    }

    /**
     * 相对指定时间，根据偏移量获取格式化时间列表
     */
    public List<String> getFormatListWithShift(int shift, int calendarField, String format, Date date) {
    	DateFormat dateFormat = new SimpleDateFormat(format);
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	String now = dateFormat.format(calendar.getTime());

    	List<String> list = new ArrayList<>();
    	if(shift > 0) {
    		list.add(now);
    		for(int i = 1; i <= shift; i++) {
    			calendar.add(calendarField, 1);
    			list.add(dateFormat.format(calendar.getTime()));
    		}
    	}else if(shift < 0) {
    		calendar.add(calendarField, shift);
    		list.add(dateFormat.format(calendar.getTime()));
    		for(int i = shift + 1; i <= -1; i++) {
    			calendar.add(calendarField, 1);
    			list.add(dateFormat.format(calendar.getTime()));
    		}
    		list.add(now);
    	}else {
    		list.add(now);
    	}
    	return list;
    }

    /**
     * 指定区间内，获取格式化时间列表
     */
    public List<String> getFormatListWithShift(String format, String beginDate, String endDate) {
    	List<String> list = new ArrayList<>();
    	list.add(beginDate);

    	try {
    		DateFormat dateFormat = new SimpleDateFormat(format);
    		Date begin = dateFormat.parse(beginDate);
    		Date end = dateFormat.parse(endDate);
    		if(!begin.before(end) && !begin.equals(end)) {
    			throw new IllegalArgumentException("endDate cann't be less than beginDate");
    		}

    		Calendar calendar = Calendar.getInstance();
        	calendar.setTime(begin);
    		while(begin.before(end)) {
    			calendar.add(Calendar.DAY_OF_MONTH, 1);
    			list.add(dateFormat.format(calendar.getTime()));
    		}
    	}catch(ParseException e) {
    		throw new IllegalArgumentException(e);
    	}
    	return list;
    }
}
