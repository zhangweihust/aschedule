package com.archermind.schedule.Calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SpecialCalendar {

	private static int daysOfMonth = 0; // 某月的天数
	private static int dayOfWeek = 0; // 具体某一天是星期几

	// 判断是否为闰年
	public static boolean isLeapYear(int year) {
		if (year % 100 == 0 && year % 400 == 0) {
			return true;
		} else if (year % 100 != 0 && year % 4 == 0) {
			return true;
		}
		return false;
	}

	// 得到某月又多少天
	public static int getDaysOfMonth(boolean isLeapyear, int month) {
		
		switch (month) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			daysOfMonth = 31;
			break;
		case 4:
		case 6:
		case 9:
		case 11:
			daysOfMonth = 30;
			break;
		case 2:
			if (isLeapyear) {
				daysOfMonth = 29;
			} else {
				daysOfMonth = 28;
			}

		}
		return daysOfMonth;
	}

	//指定某年中的某月的第一天是星期几
	public int getWeekdayOfMonth(int year, int month) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, 1);
		dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
		return dayOfWeek;
	}

	// 实现给定某日期，判断是星期几
	public static String getWeekDay(int year,int month,int day) {// 必须yyyy-MM-dd
		String date = year+"-"+month+"-"+day;
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdw = new SimpleDateFormat("E");
		Date d = null;
		try {
			d = sd.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		System.out.println(sdw.format(d));
		return /*getRealWeekDay(Integer.parseInt(sdw.format(d)) - 1)*/sdw.format(d);
	}
	
	private static String getRealWeekDay(int weekDay){
		String[] week = {"周日","周一","周二","周三","周四","周五","周六"};
		return week[weekDay];
	}

}
