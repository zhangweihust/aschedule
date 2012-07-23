package com.archermind.schedule.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.archermind.schedule.ScheduleApplication;

public class DateTimeUtils {
	public static String time2String(String formatter, long date) {
		Date d = new Date(date);
		SimpleDateFormat dateFormat = new SimpleDateFormat(formatter);
		String dateTime = dateFormat.format(d);
		ScheduleApplication.LogD(DateTimeUtils.class, "time=" + dateTime);
		return dateTime;
	}
	
	public static long getDayOfWeek(int week) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a EEEE"); 
		Calendar time = Calendar.getInstance(Locale.CHINA); 
		time.setTimeInMillis(System.currentTimeMillis());
		ScheduleApplication.LogD(DateTimeUtils.class, "当前时间:"+sdf.format(time.getTime()));
		if(week == Calendar.SUNDAY){
			int nowDay = time.get(Calendar.DAY_OF_MONTH); 
			time.set(Calendar.DATE, nowDay+7);
			time.set(Calendar.HOUR_OF_DAY,23);
			time.set(Calendar.MINUTE,59);
			time.set(Calendar.SECOND,59);
			ScheduleApplication.LogD(DateTimeUtils.class, "本周日时间:"+sdf.format(time.getTime()));
		} else if(week == Calendar.MONDAY){
			time.set(Calendar.HOUR_OF_DAY,0);
			time.set(Calendar.MINUTE,0);
			time.set(Calendar.SECOND,0);
		}
		time.set(Calendar.DAY_OF_WEEK, week);
		ScheduleApplication.LogD(DateTimeUtils.class, "时间:"+sdf.format(time.getTime()));
		return time.getTimeInMillis();
	}
	
	public static long getThreeDaysBefore() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a EEEE"); 
		Calendar time = Calendar.getInstance(Locale.CHINA); 
		time.setTimeInMillis(System.currentTimeMillis());
		ScheduleApplication.LogD(DateTimeUtils.class, "当前时间:"+sdf.format(time.getTime()));
		int nowDay = time.get(Calendar.DAY_OF_MONTH);
		time.set(Calendar.DATE, nowDay - 3);
		time.set(Calendar.HOUR_OF_DAY,0);
		time.set(Calendar.MINUTE,0);
		time.set(Calendar.SECOND,0);
		ScheduleApplication.LogD(DateTimeUtils.class, "3天前开始时间:"+sdf.format(time.getTime()));
		return time.getTimeInMillis();
	}
	
	public static long getYesterdayEnd() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a EEEE"); 
		Calendar time = Calendar.getInstance(Locale.CHINA); 
		time.setTimeInMillis(System.currentTimeMillis());
		ScheduleApplication.LogD(DateTimeUtils.class, "当前时间:"+sdf.format(time.getTime()));
		int nowDay = time.get(Calendar.DAY_OF_MONTH);
		time.set(Calendar.DATE, nowDay - 1);
		time.set(Calendar.HOUR_OF_DAY,23);
		time.set(Calendar.MINUTE,59);
		time.set(Calendar.SECOND,59);
		ScheduleApplication.LogD(DateTimeUtils.class, "昨天结束时间:"+sdf.format(time.getTime()));
		return time.getTimeInMillis();
	}
	
	
	public static long getToday(int amORpm) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a EEEE"); 
		Calendar time = Calendar.getInstance(Locale.CHINA); 
		time.setTimeInMillis(System.currentTimeMillis());
		ScheduleApplication.LogD(DateTimeUtils.class, "当前时间:"+sdf.format(time.getTime()));
		if(amORpm == Calendar.AM){
			time.set(Calendar.HOUR_OF_DAY,0);
			time.set(Calendar.MINUTE,0);
			time.set(Calendar.SECOND,0);
			ScheduleApplication.LogD(DateTimeUtils.class, "今天开始时间:"+sdf.format(time.getTime()));
		} else {
			time.set(Calendar.HOUR_OF_DAY,23);
			time.set(Calendar.MINUTE,59);
			time.set(Calendar.SECOND,59);
			ScheduleApplication.LogD(DateTimeUtils.class, "今天结束时间:"+sdf.format(time.getTime()));
		}
		return time.getTimeInMillis();
	}
	
	public static long getTomorrow(int amORpm) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a EEEE"); 
		Calendar time = Calendar.getInstance(Locale.CHINA); 
		time.setTimeInMillis(System.currentTimeMillis());
		ScheduleApplication.LogD(DateTimeUtils.class, "当前时间:"+sdf.format(time.getTime()));
		int nowDay = time.get(Calendar.DAY_OF_MONTH);
		time.set(Calendar.DATE, nowDay + 1);
		if(amORpm == Calendar.AM){
			time.set(Calendar.HOUR_OF_DAY,0);
			time.set(Calendar.MINUTE,0);
			time.set(Calendar.SECOND,0);
			ScheduleApplication.LogD(DateTimeUtils.class, "明天开始时间:"+sdf.format(time.getTime()));
		} else {
			time.set(Calendar.HOUR_OF_DAY,23);
			time.set(Calendar.MINUTE,59);
			time.set(Calendar.SECOND,59);
			ScheduleApplication.LogD(DateTimeUtils.class, "明天结束时间:"+sdf.format(time.getTime()));
		}
		return time.getTimeInMillis();
	}
	
	
}
