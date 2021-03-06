package com.android.schedule.Calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.android.schedule.ScheduleApplication;

import android.util.Log;

public class LunarCalendar {
	private int year; // 农历的年份
	private int month;
	private int day;
	private String lunarMonth; // 农历的月份
	private boolean leap;
	public int leapMonth = 0; // 闰的是哪个月
	public static String suffix = "_";

	final static String chineseNumber[] = {"一", "二", "三", "四", "五", "六", "七",
			"八", "九", "十", "十一", "十二"};
	static SimpleDateFormat chineseDateFormat = new SimpleDateFormat(
			"yyyy年MM月dd日");
	final static long[] lunarInfo = new long[]{0x04bd8, 0x04ae0, 0x0a570,
			0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
			0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0,
			0x0ada2, 0x095b0, 0x14977, 0x04970, 0x0a4b0, 0x0b4b5, 0x06a50,
			0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, 0x06566,
			0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0,
			0x1c8d7, 0x0c950, 0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4,
			0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0, 0x0b550,
			0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950,
			0x06aa0, 0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260,
			0x0f263, 0x0d950, 0x05b57, 0x056a0, 0x096d0, 0x04dd5, 0x04ad0,
			0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
			0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40,
			0x0af46, 0x0ab60, 0x09570, 0x04af5, 0x04970, 0x064b0, 0x074a3,
			0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, 0x0c960,
			0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0,
			0x092d0, 0x0cab5, 0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9,
			0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, 0x07954, 0x06aa0,
			0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65,
			0x0d530, 0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0,
			0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, 0x0b5a0, 0x056d0, 0x055b2,
			0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0};

	// 农历部分假日
	final static String[] lunarHoliday = new String[]{"0101 春节", "0115 元宵",
			"0505 端午", "0707 七夕情人", "0715 中元", "0815 中秋", "0909 重阳", "1208 腊八",
			"1224 小年", "0100 除夕"};

	// 公历部分节假日
	final static String[] solarHoliday = new String[]{"0101 元旦", "0214 情人",
			"0308 妇女", "0312 ֲ植树",
			// "0315 消费者权益日",
			"0401 愚人", "0501 劳动", "0504 青年",
			// "0512 护士",
			"0601 儿童", "0701 建党", "0801 建军",
			// "0909 毛泽东逝世纪念日",
			"0910 教师",
			// "0928 孔子诞辰",
			"1001 国庆",
			// "1006 老人",
			// "1024 联合国日",
			// "1112 孙中山诞辰纪念",
			// "1220 澳门回归纪念",
			"1225 圣诞"
	// "1226 毛泽东诞辰纪念�"
	};

	// ====== 传回农历Y年的总天数
	final private int yearDays(int y) {
		int i, sum = 348;
		for (i = 0x8000; i > 0x8; i >>= 1) {
			if ((lunarInfo[y - 1900] & i) != 0)
				sum += 1;
		}
		return (sum + leapDays(y));
	}

	// ====== 传回农历Y年闰月的天数
	final private int leapDays(int y) {
		if (leapMonth(y) != 0) {
			if ((lunarInfo[y - 1900] & 0x10000) != 0)
				return 30;
			else
				return 29;
		} else
			return 0;
	}

	// ====== 传回农历y年闰哪个月1-12，没闰传回0
	final private int leapMonth(int y) {
		return (int) (lunarInfo[y - 1900] & 0xf);
	}

	// ====== 传回农历y年m月的总天数
	final private int monthDays(int y, int m) {
		if ((lunarInfo[y - 1900] & (0x10000 >> m)) == 0)
			return 29;
		else
			return 30;
	}

	// ====== 传回农历y年的生肖
	final public String animalsYear(int year) {
		final String[] Animals = new String[]{"鼠", "牛", "虎", "兔", "龙", "蛇",
				"马", "羊", "猴", "鸡", "狗", "猪"};
		return Animals[(year - 4) % 12];
	}

	// ====== 传入月日的offset 传回干支，0=甲子
	final private String cyclicalm(int num) {
		final String[] Gan = new String[]{"甲", "乙", "丙", "丁", "戊", "己", "庚",
				"辛", "壬", "癸"};
		final String[] Zhi = new String[]{"子", "丑", "寅", "卯", "辰", "巳", "午",
				"未", "申", "酉", "戌", "亥"};
		return (Gan[num % 10] + Zhi[num % 12]);
	}

	// ====== 传入offset 传回干支，0=甲子
	final public String cyclical(int year) {
		int num = year - 1900 + 36;
		return (cyclicalm(num));
	}

	public String getChinaDayString(int day) {
		String chineseTen[] = {"初", "十", "廿", "卅"};
		int n = day % 10 == 0 ? 9 : day % 10 - 1;
		if (day > 30)
			return "";
		if (day == 10)
			return "初十";
		else
			try {
				return chineseTen[day / 10] + chineseNumber[n];
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("day = " + day + "  n = " + n);
				return chineseTen[1] + chineseNumber[5];
			}

	}

	/** */
	/**
	 * 
	 * isday: 这个参数为false---日期韦节假日时，阴历日期就返回节假日，true---不管日期是否为节假日依然返回这天对应的阴历日期
	 * 
	 * @param cal
	 * @return
	 */
	public synchronized String getLunarDate(int year_log, int month_log,
			int day_log, boolean isday) {
		// @SuppressWarnings("unused")
		int yearCyl, monCyl, dayCyl;
		// int leapMonth = 0;
		String nowadays;
		Date baseDate = null;
		Date nowaday = null;
		try {
			baseDate = chineseDateFormat.parse("1900年1月31日");
		} catch (ParseException e) {
			e.printStackTrace(); // To change body of catch statement use
			// Options | File Templates.
		}

		nowadays = year_log + "年" + month_log + "月" + day_log + "日";
		try {
			nowaday = chineseDateFormat.parse(nowadays);
		} catch (ParseException e) {
			e.printStackTrace(); // To change body of catch statement use
			// Options | File Templates.
		}

		int offset = (int) ((nowaday.getTime() - baseDate.getTime()) / 86400000L);
		dayCyl = offset + 40;
		monCyl = 14;

		// i最终结果是农历的年份
		// offset是当年的第几天
		int iYear, daysOfYear = 0;
		for (iYear = 1900; iYear < 2049 && offset > 0; iYear++) {
			daysOfYear = yearDays(iYear);
			offset -= daysOfYear;
			monCyl += 12;
		}
		if (offset < 0) {
			offset += daysOfYear;
			iYear--;
			monCyl -= 12;
		}
		// 农历年份
		year = iYear;
		setYear(year); // 设置公历对应的农历年份

		yearCyl = iYear - 1864;
		try {
			leapMonth = leapMonth(iYear);
		} catch (ArrayIndexOutOfBoundsException e) {
			leapMonth = 0;
			System.out.println("**************** iYear = " + iYear);
		}
		leap = false;

		int iMonth, daysOfMonth = 0;
		for (iMonth = 1; iMonth < 13 && offset > 0; iMonth++) {
			// 闰月
			if (leapMonth > 0 && iMonth == (leapMonth + 1) && !leap) {
				--iMonth;
				leap = true;
				daysOfMonth = leapDays(year);
			} else
				daysOfMonth = monthDays(year, iMonth);

			offset -= daysOfMonth;
			if (leap && iMonth == (leapMonth + 1))
				leap = false;
			if (!leap)
				monCyl++;
		}
		if (offset == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
			if (leap) {
				leap = false;
			} else {
				leap = true;
				--iMonth;
				--monCyl;
			}
		}
		if (offset < 0) {
			offset += daysOfMonth;
			--iMonth;
			--monCyl;
		}
		month = iMonth;
		try {
			setLunarMonth(chineseNumber[month - 1] + "月");
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("**************** month = " + month);
			setLunarMonth(chineseNumber[0] + "月");
		}
		day = offset + 1;

		if (!isday) {
			// 如果日期为节假日则阴历日期返回节假日
			// setLeapMonth(leapMonth);
			for (int i = 0; i < solarHoliday.length; i++) {
				// 返回公历节假日名称
				String sd = solarHoliday[i].split(" ")[0]; // 节假日的日期
				String sdv = solarHoliday[i].split(" ")[1]; // 节假日的名称
				String smonth_v = month_log + "";
				String sday_v = day_log + "";
				String smd = "";
				if (month_log < 10) {
					smonth_v = "0" + month_log;
				}
				if (day_log < 10) {
					sday_v = "0" + day_log;
				}
				smd = smonth_v + sday_v;
				if (sd.trim().equals(smd.trim())) {
					return sdv + suffix;
				}
			}

			for (int i = 0; i < lunarHoliday.length; i++) {
				// 返回农历节假日名称
				String ld = lunarHoliday[i].split(" ")[0]; // 节假日的日期
				String ldv = lunarHoliday[i].split(" ")[1]; // 节假日的名称
				String lmonth_v = month + "";
				String lday_v = day + "";
				String lmd = "";
				if (month < 10) {
					lmonth_v = "0" + month;
				}
				if (day < 10) {
					lday_v = "0" + day;
				}
				lmd = lmonth_v + lday_v;
				if (ld.trim().equals(lmd.trim())) {
					return ldv + suffix;
				}
			}
		}
		if (day == 1)
			try {
				return chineseNumber[month - 1] + "月";
			} catch (Exception e) {
				// TODO: handle exception
				return chineseNumber[0] + "月";
			}
		else
			return getChinaDayString(day);

	}

	public synchronized String getLunarDate(long millistime, boolean isday) {
		int year;
		int month;
		int day;

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millistime);
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH) + 1;
		day = cal.get(Calendar.DAY_OF_MONTH);

		return getLunarDate(year, month, day, isday);
	}

	public String toString() {
		if (chineseNumber[month - 1] == "一" && getChinaDayString(day) == "初一")
			return "农历" + year + "年";
		else if (getChinaDayString(day) == "初一")
			return chineseNumber[month - 1] + "月";
		else
			return getChinaDayString(day);
		// return year + "��" + (leap ? "��" : "") + chineseNumber[month - 1] +
		// "��" + getChinaDayString(day);
	}

	public int getLeapMonth() {
		return leapMonth;
	}

	public void setLeapMonth(int leapMonth) {
		this.leapMonth = leapMonth;
	}

	/**
	 * 得到当前日期对应的农历月份
	 * 
	 * @return
	 */
	public String getLunarMonth() {
		return lunarMonth;
	}

	public void setLunarMonth(String lunarMonth) {
		this.lunarMonth = lunarMonth;
	}

	/**
	 * 得到当前年对应的农历年份
	 * 
	 * @return
	 */
	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public synchronized String getHolidays(long times) {
		String holiday = "";
		String strdate = "";
		int year = 0;
		int month = 0;
		int day = 0;
		Date date = new Date(times);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		strdate = sdf.format(date);
		year = Integer.parseInt(strdate.split("-")[0]);
		month = Integer.parseInt(strdate.split("-")[1]);
		day = Integer.parseInt(strdate.split("-")[2]);

		holiday = getLunarDate(year, month, day, false);

		if (!isHolidays(holiday)) {
			holiday = "false";
		} else {
			holiday = holiday.replace("_", "");
		}

		return holiday;
	}

	public boolean isHolidays(String lunar) {
		if (lunar.contains("初") || lunar.contains("十") || lunar.contains("廿")
				|| lunar.contains("卅") || lunar.contains("月")) {
			return false;
		}

		return true;
	}
}
