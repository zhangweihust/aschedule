package com.android.schedule.Calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Text;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;

import com.android.schedule.ScheduleApplication;
import com.android.schedule.Model.ScheduleData;
import com.android.schedule.Model.UserInfoData;
import com.android.schedule.Provider.DatabaseHelper;
import com.android.schedule.Provider.DatabaseManager;
import com.android.schedule.Provider.LunarDatesDatabaseHelper;
import com.android.schedule.Services.ServiceManager;
import com.android.schedule.Utils.LostTime;

public class CalendarData {
	private static int BASE_YEAR = 1900;
	private static int LAST_YEAR = 2049;
	private static LunarMonthDates[] lunarMonthDates = new LunarMonthDates[(LAST_YEAR - BASE_YEAR + 1) * 12];
	private DatabaseManager database;

	private boolean isLeapyear = false; // 是否为闰年

	private int daysOfMonth = 0; // 某月的天数

	private int dayOfWeek = 0; // 具体某一天是星期几

	private int lastDaysOfMonth = 0; // 上一个月的总天数

	private int daysOfWeek = 0;

	private String[] dayNumber = new String[42]; // 一个gridview中的日期存入此数组中

	private String[] week = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

	private SpecialCalendar sc = null;

	private LunarCalendar lc = null;

	private String currentYear = "";

	private String currentMonth = "";

	private String currentDay = "";

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");

	private int currentFlag = -1; // 用于标记当天

	private String showYear = ""; // 用于在头部显示的年份

	private String showMonth = ""; // 用于在头部显示的月份

	private String animalsYear = "";

	private String leapMonth = ""; // 闰哪一个月

	private String cyclical = ""; // 天干地支

	// 系统当前时间
	private String sysDate = "";

	private String sys_year = "";

	private String sys_month = "";

	private String sys_day = "";

	// 日程时间（需要标记的日程日期）
	private String sch_year = "";

	private String sch_month = "";

	private String sch_day = "";

	private int mark_count[];

	private List<ScheduleData> scheduleList = new ArrayList<ScheduleData>();

	private Context mContext;

	public CalendarData(int jumpMonth, int jumpYear, int year_c, int month_c,
			int day_c, int flagType) {

		try {
		database = ServiceManager.getDbManager();
		Date date = new Date();
		sysDate = sdf.format(date); // 当前日期
		sys_year = sysDate.split("-")[0];
		sys_month = sysDate.split("-")[1];
		sys_day = sysDate.split("-")[2];
		sc = new SpecialCalendar();
		lc = new LunarCalendar();
		int stepYear = year_c + jumpYear;
		int stepMonth = month_c + jumpMonth;
		if (stepMonth > 0) {
			// 往下一个月滑动
			if (stepMonth % 12 == 0) {
				stepYear = stepYear + stepMonth / 12 - 1;
				stepMonth = 12;
			} else {
				stepYear = stepYear + stepMonth / 12;
				stepMonth = stepMonth % 12;
			}
		} else {
			// 往上一个月滑动
			stepYear = stepYear - 1 + stepMonth / 12;
			stepMonth = stepMonth % 12 + 12;
			if (stepMonth % 12 == 0) {

			}
		}

		currentYear = String.valueOf(stepYear);; // 得到当前的年份
		currentMonth = String.valueOf(stepMonth); // 得到本月（jumpMonth为滑动的次数，每滑动一次就增加一月或减一月）
		currentDay = String.valueOf(day_c); // 得到当前日期是哪天
		getCalendar(Integer.parseInt(currentYear),
				Integer.parseInt(currentMonth), flagType);
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(), e);
		}
	}

	public CalendarData(final Context context, int jumpMonth, int jumpYear,
			int year_c, int month_c, int day_c, int flagType) {
		try {
		mContext = context;
		database = new DatabaseManager(context);
		database.openwithnoservice();

		Date date = new Date();
		sysDate = sdf.format(date); // 当前日期
		sys_year = sysDate.split("-")[0];
		sys_month = sysDate.split("-")[1];
		sys_day = sysDate.split("-")[2];
		sc = new SpecialCalendar();
		lc = new LunarCalendar();
		int stepYear = year_c + jumpYear;
		int stepMonth = month_c + jumpMonth;
		if (stepMonth > 0) {
			// 往下一个月滑动
			if (stepMonth % 12 == 0) {
				stepYear = stepYear + stepMonth / 12 - 1;
				stepMonth = 12;
			} else {
				stepYear = stepYear + stepMonth / 12;
				stepMonth = stepMonth % 12;
			}
		} else {
			// 往上一个月滑动
			stepYear = stepYear - 1 + stepMonth / 12;
			stepMonth = stepMonth % 12 + 12;
			if (stepMonth % 12 == 0) {

			}
		}

		currentYear = String.valueOf(stepYear);; // 得到当前的年份
		currentMonth = String.valueOf(stepMonth); // 得到本月（jumpMonth为滑动的次数，每滑动一次就增加一月或减一月）
		currentDay = String.valueOf(day_c); // 得到当前日期是哪天
		
		getCalendar(Integer.parseInt(currentYear),
				Integer.parseInt(currentMonth), flagType);

		database.close();
		
//Thread t = new Thread(new Runnable() {
//
//	@Override
//	public void run() {
//		// TODO Auto-generated method stub
//		database = new DatabaseManager(context);
//		database.openwithnoservice();
//        	for (int i=1900; i<=2049; i++) {
//        		for (int j=1; j<=12; j++) {
//        			getCalendar(i, j, 3);
//        		}
//        	}
//
//        database.close();
//	}}) {
//	
//};t.start();
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(), e);
		}
	}

	public long getMillisTimeByDate(String date) {
		long time = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd");
		try {
			Date d = sdf.parse(date);
			time = d.getTime() - ServiceManager.getTimeDifference();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return time;
	}

	// 得到某年的某月的天数且这月的第一天是星期几
	public void getCalendar(int year, int month, int flagType) {
		isLeapyear = sc.isLeapYear(year); // 是否为闰年
		daysOfMonth = sc.getDaysOfMonth(isLeapyear, month); // 某月的总天数
		dayOfWeek = sc.getWeekdayOfMonth(year, month); // 某月第一天为星期几
		lastDaysOfMonth = sc.getDaysOfMonth(isLeapyear, month - 1); // 上一个月的总天数
		Log.d("DAY", isLeapyear + " ======  " + daysOfMonth
				+ "  ============  " + dayOfWeek + "  =========   "
				+ lastDaysOfMonth);
		getweek(year, month, flagType);
	}
	
	private boolean loadLunarMonthDatesOnYear(int year) {
    	//System.out.println("=CCC= loadLunarMonthDatesOnYear");
		if (year <= BASE_YEAR || year > LAST_YEAR)
			return false;

		boolean loadSuccess = false;
		try {
		int monthPos = (year - BASE_YEAR) * 12;
		Cursor cursor = database.queryLunarDatesOnYear(String.valueOf(year));
		if (cursor != null) {
			if (cursor.getCount() == 12) {
				while (cursor.moveToNext()) {
					lunarMonthDates[monthPos] = new LunarMonthDates();
					lunarMonthDates[monthPos].lunarDates = cursor
							.getString(cursor
									.getColumnIndex(LunarDatesDatabaseHelper.COLUMN_CALENDAR_LUNARDATE)).split(",");
					lunarMonthDates[monthPos].dayOfWeeks = cursor
							.getString(cursor
									.getColumnIndex(LunarDatesDatabaseHelper.COLUMN_CALENDAR_DAYOFWEEK)).split(",");
					monthPos++;
				}
				loadSuccess = true;
			}
			cursor.close();
			}
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(), e);
		}
		return loadSuccess;
	}

//	static SimpleDateFormat chineseDateFormat = new SimpleDateFormat(
//	"yyyy年MM月dd日");
	
	private LunarDateInfo getLunarDateInfo(int year_log, int month_log,
			int day_log) {
		LunarDateInfo info = new LunarDateInfo();
		
		if (month_log <= 0) {
			month_log += 12;
			year_log -= 1;
		}
		
		if (month_log > 12) {
			month_log -= 12;
			year_log += 1;
		}
		
//		Date nowaday = null;
//    	String nowadays = year_log + "年" + month_log + "月" + day_log + "日";
//		try {
//			nowaday = chineseDateFormat.parse(nowadays);
//		} catch (ParseException e) {
//			e.printStackTrace(); // To change body of catch statement use
//			// Options | File Templates.
//		}
//		
//		year_log = nowaday.getYear() + BASE_YEAR;
//		month_log = nowaday.getMonth() + 1;
//		day_log = nowaday.getDate();
		
		int monthPos = (year_log - BASE_YEAR) * 12 + month_log - 1;
		boolean hasLunarMonthDates = (lunarMonthDates[monthPos] != null);
		if (!hasLunarMonthDates) {
			hasLunarMonthDates = loadLunarMonthDatesOnYear(year_log);
		}
		
		if (hasLunarMonthDates && (lunarMonthDates[monthPos] != null)) {
			try {
				LunarMonthDates monthDates = lunarMonthDates[monthPos];
				info.lunarDate = monthDates.lunarDates[day_log-1];
				info.dayOfWeek = monthDates.dayOfWeeks[day_log-1 ];
			} catch (ArrayIndexOutOfBoundsException e) {
				info.lunarDate = day_log+"."+lc.getLunarDate(year_log, month_log, day_log, false);
				info.dayOfWeek = SpecialCalendar.getNumberWeekDay(year_log, month_log, day_log);
				//System.out.println("=CCC=1 "+ year_log+"."+month_log+"."+day_log + "==="+info.lunarDate+"==="+info.dayOfWeek);
			}
		} else {
			info.lunarDate = day_log+"."+lc.getLunarDate(year_log, month_log, day_log, false);
			info.dayOfWeek = SpecialCalendar.getNumberWeekDay(year_log, month_log, day_log);
			//System.out.println("=CCC=2 "+ year_log+"."+month_log+"."+day_log + "==="+info.lunarDate+"==="+info.dayOfWeek+"==="+hasLunarMonthDates);
		}
		
		return info;
	} 

    // 将一个月中的每一天的值添加入数组dayNuber中
    private synchronized void getweek(int year, int month, int flagType) {
    	try {
    	//LostTime.cast("getweek 1");
        String date = year + "." + month;// 表示查询哪个月

        if (flagType == 1) {
            week = new String[] {
                    "周一", "周二", "周三", "周四", "周五", "周六", "周日"
            };
            dayOfWeek -= 1;
            if (dayOfWeek < 0) {
                dayOfWeek = 6;
            }
        }

        //LostTime.cast("getweek 2");
		
		//LostTime.cast("getweek 3");
		
		//LostTime.sum_reset();
        	
		String data0 = year + "." + month;
		String data1 = "";
		String data2 = "";
		
		
		String[] dayOfWeeks = new String[42];
        int j = 1;
        String lunarDay = "";
        mark_count = new int[42];
        for (int i = 0; i < dayNumber.length; i++) {
        	int k = 1;

            // 周一
            if (i < daysOfWeek) {
            	dayNumber[i] = week[i] + "." + " ";
            } else if (i < dayOfWeek + daysOfWeek) { // 前一个月
				int temp = lastDaysOfMonth - dayOfWeek + k - daysOfWeek;
//				lunarDay = lc.getLunarDate(year, month - 1, temp + i, false);
//				dayNumber[i] = (temp + i) + "." + lunarDay;
            	dayNumber[i] = getLunarDateInfo(year, month - 1, temp + i).lunarDate;
            } else if (i < daysOfMonth + dayOfWeek + daysOfWeek) { // 本月
            	String day = String.valueOf(i - dayOfWeek + k - daysOfWeek); // 得到的日期
            	String startData = year + "." + month + "." + day;
                String dayOfYear = month + "." + day;
                String dayOfMonth = day;
                //LostTime.sum_mark_start();
                
//				lunarDay = lc.getLunarDate(year, month, i - dayOfWeek + k
//						- daysOfWeek, false);
//				dayNumber[i] = i - dayOfWeek + k - daysOfWeek + "."
//						+ lunarDay;
//				dayOfWeeks[i] = SpecialCalendar.getNumberWeekDay(year,
//						month, Integer.parseInt(day));
				
                
//                if (flagType == 3) {
//					data1 = data1 + dayNumber[i] + ",";
//					data2 = data2 + dayOfWeeks[i] + ",";
//				}
				
                LunarDateInfo info = getLunarDateInfo(year, month, i
						- dayOfWeek + k - daysOfWeek);
				dayNumber[i] = info.lunarDate;
				dayOfWeeks[i] = info.dayOfWeek;
                
                Cursor cursor = null;

                long starTimeInMillis = getMillisTimeByDate(startData);
                int count = 0;
                cursor = database.queryIsMarkWithDay(starTimeInMillis, dayOfYear, dayOfMonth,
                		dayOfWeeks[i]);
                
                count = cursor.getCount();
                
                if (cursor != null) {
                    cursor.close();
                }

                // 对于当前月才去标记当前日期
                if (sys_year.equals(String.valueOf(year))
                        && sys_month.equals(String.valueOf(month)) && sys_day.equals(day)) {
                    // 笔记当前日期
                    currentFlag = i;
                }

                // 标记日程日期
                if (count > 0) {
                    mark_count[Integer.parseInt(day)] = count;
                }

                setShowYear(String.valueOf(year));
                setShowMonth(String.valueOf(month));
                setAnimalsYear(lc.animalsYear(year));
                setLeapMonth(lc.leapMonth == 0 ? "" : String.valueOf(lc.leapMonth));
                //LostTime.sum_mark_end();
                setCyclical(lc.cyclical(year));

            } else { // 下一个月
				//lunarDay = lc.getLunarDate(year, month + 1, j, false);
				//dayNumber[i] = j + "." + lunarDay;
            	dayNumber[i] = getLunarDateInfo(year, month + 1, j).lunarDate;
                j++;
            }
        }
        //LostTime.sum_cast("getNumberWeekDay");
        //LostTime.cast("getweek 4");
        
//        if (flagType == 3) {
//			ContentValues contentvalues = new ContentValues();
//			contentvalues.put(DatabaseHelper.COLUMN_CALENDAR_MONTH,
//					data0);
//			contentvalues.put(DatabaseHelper.COLUMN_CALENDAR_LUNARDATE,
//					data1);
//			contentvalues.put(DatabaseHelper.COLUMN_CALENDAR_DAYOFWEEK,
//					data2);
//			ServiceManager.getDbManager().insertCalendarMap(
//					contentvalues);
//		}
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(), e);
		}
    }

	public synchronized List<ScheduleData> getMonthSchedule(int year, int month) {
		try {
		database.openwithnoservice();
		int i;
		int days = SpecialCalendar.getDaysOfMonth(
				SpecialCalendar.isLeapYear(year), month);
		String startData = "";
		String dayOfYear = "";
		String dayOfMonth = "";
		String dayOfWeek = "";
		String lunar = "";
		List<ScheduleData> todayscheduleList = new ArrayList<ScheduleData>();
		long starTimeInMillis = 0;
		LunarCalendar lunarcalendar = new LunarCalendar();
		scheduleList.clear();
		for (i = 1; i <= days; i++) {
			lunar = lunarcalendar.getLunarDate(year, month, i, false);

			startData = year + "." + month + "." + i;
			dayOfYear = month + "." + i;
			dayOfMonth = String.valueOf(i);
			dayOfWeek = SpecialCalendar.getNumberWeekDay(year, month, i);

			Cursor cursor = null;

			starTimeInMillis = getMillisTimeByDate(startData);
			cursor = database.queryIsMarkWithDay(starTimeInMillis, dayOfYear,
					dayOfMonth, dayOfWeek);

			todayscheduleList.clear();
			while (cursor.moveToNext()) {
				ScheduleData scheduledata = new ScheduleData();
				scheduledata.id = cursor.getInt(cursor
						.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_ID));
				scheduledata.content = cursor
						.getString(cursor
								.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT));
				scheduledata.time = cursor
						.getLong(cursor
								.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME));
				scheduledata.share = cursor.getInt(cursor
						.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_SHARE)) == 1;
				scheduledata.notice_flag = cursor
						.getInt(cursor
								.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_FLAG)) == 1;
				scheduledata.type = cursor.getInt(cursor
						.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_TYPE));
				scheduledata.pastsecomds = scheduledata.time
						% (24 * 3600 * 1000);
				scheduledata.time = starTimeInMillis + scheduledata.pastsecomds;
				todayscheduleList.add(scheduledata);
			}
			Collections.sort(todayscheduleList, new SortByPastsecond());

			if (lunarcalendar.isHolidays(lunar) || todayscheduleList.size() > 0) {
				ScheduleData scheduledata = new ScheduleData();
				scheduledata.id = -1;
				scheduledata.time = starTimeInMillis;
				scheduleList.add(scheduledata);
				scheduleList.addAll(todayscheduleList);
			}

			if (cursor != null) {
				cursor.close();
			}
		}
		database.close();
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(), e);
		}
		return scheduleList;
	}

	public String getShowYear() {
		return showYear;
	}

	public void setShowYear(String showYear) {
		this.showYear = showYear;
	}

	public String getShowMonth() {
		return showMonth;
	}

	public void setShowMonth(String showMonth) {
		this.showMonth = showMonth;
	}

	public String getAnimalsYear() {
		return animalsYear;
	}

	public void setAnimalsYear(String animalsYear) {
		this.animalsYear = animalsYear;
	}

	public String getLeapMonth() {
		return leapMonth;
	}

	public void setLeapMonth(String leapMonth) {
		this.leapMonth = leapMonth;
	}

	public String getCyclical() {
		return cyclical;
	}

	public void setCyclical(String cyclical) {
		this.cyclical = cyclical;
	}

	public int getDaysOfMonth() {
		return this.daysOfMonth;
	}

	public int getDaysOfWeek() {
		return this.daysOfWeek;
	}

	public int getDayOfWeek() {
		return this.dayOfWeek;
	}

	public int[] getMarkcount() {
		return this.mark_count;
	}

	public int getCurrentFlag() {
		return this.currentFlag;
	}

	public String[] getDayNumber() {
		return this.dayNumber;
	}

	public String[] getWeek() {
		return this.week;
	}

	class SortByPastsecond implements Comparator {

		@Override
		public int compare(Object object1, Object object2) {
			// TODO Auto-generated method stub
			long time1 = ((ScheduleData) object1).pastsecomds;
			long time2 = ((ScheduleData) object2).pastsecomds;
			if (time1 > time2) {
				return 1;
			} else if (time1 == time2) {
				return 0;
			}
			return -1;
		}
	}
	
    class LunarMonthDates {
    	String []lunarDates;
    	String []dayOfWeeks;
    }

    class LunarDateInfo {
    	String lunarDate;
    	String dayOfWeek;
    }
}
