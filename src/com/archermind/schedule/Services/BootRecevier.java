package com.archermind.schedule.Services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Utils.DateTimeUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

public class BootRecevier extends BroadcastReceiver {
	private static final String TAG = "BootRecevier";
	private String remindCycle;
	private long scheduleTime;
	private long aheadTime;
	private String weekValue;
	private String timeValue;
	private String dateValue;
	private long startTime;
	private long endTime;
	private long flagAlarm;
	private long schedule_id;
	private static final int DATE_MODE_NONE = 0;// 0
	private static final int DATE_MODE_DAY = 1;// 1

	private static final int DATE_MODE_WEEK = 2;// 2

	private static final int DATE_MODE_MONTH = 3;// 3

	private static final int DATE_MODE_YEAR = 4;// 4
	private int dateMode;
	private long nextTime;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "--------------BOOT receiver");
		Cursor c = ServiceManager.getDbManager().queryNotOutdateschedule();
		Log.d(TAG, "-------------not outdate count :"+ c.getCount());
		if (c != null) {
			if (c.moveToFirst()) {
				do {
                    
					remindCycle = c
							.getString(c
									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD));
					scheduleTime = c
							.getLong(c
									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME));
					aheadTime = Long
							.valueOf(c.getString(c
									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_TIME)));
					weekValue = c
							.getString(c
									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_WEEK));
					startTime = Long
							.valueOf(c.getString(c
									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_START)));
					endTime = Long
							.valueOf(c.getString(c
									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_END)));
					flagAlarm = c
							.getLong(c
									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_FLAG));
					schedule_id =c
					.getLong(c
							.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_ID));
					

					if (remindCycle.equals("0")) {
						dateMode = DATE_MODE_NONE;
					} else if (remindCycle.equals("D")) {
						dateMode = DATE_MODE_DAY;
					} else if (remindCycle.equals("W")) {
						dateMode = DATE_MODE_WEEK;
					} else if (remindCycle.equals("M")) {
						dateMode = DATE_MODE_MONTH;
					} else if (remindCycle.equals("Y")) {
						dateMode = DATE_MODE_YEAR;
					}

					nextTime = getNextTime(dateMode, weekValue, scheduleTime);
					Log.d(TAG,
							"----------now="
									+ DateTimeUtils.time2String("yyyy-MM-dd-hh-mm", nextTime));
					// 设置闹钟过期
					if (nextTime == 0) {
						ContentValues cv = new ContentValues();
						cv.put("notice_outdate", true);
						ServiceManager.getDbManager().updateScheduleById((int) c
								.getLong(c
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_ID)),
								cv);

					} else {
						// 设置闹钟
						Intent alarmIntent = new Intent(context, AlarmRecevier.class);
						alarmIntent.setAction("" + flagAlarm);
						alarmIntent.putExtra("schedule_id", c
								.getLong(c
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_ID)));
						PendingIntent pi = PendingIntent.getBroadcast(context, 1,
								alarmIntent, 0);
						AlarmManager am = (AlarmManager) context
								.getSystemService(context.ALARM_SERVICE);
						am.set(AlarmManager.RTC_WAKEUP, nextTime, pi);
					}
				} while (c.moveToNext());

			}

		}

	}
	
	
	
	
	

	private long getNextTime(int dateMode, String weekValue, long time) {

		final SimpleDateFormat fmt = new SimpleDateFormat();
		final Calendar c = Calendar.getInstance();// 获取的是当前的时间
		Log.d(TAG,
				"----------c="
						+ DateTimeUtils.time2String("yyyy-MM-dd-hh-mm",
								c.getTimeInMillis()));
		// Log.d(TAG, "----------c=" + c.getTimeInMillis());
		long nextTime = 0; // 需要计算的下一次闹钟的时间
		final long now = System.currentTimeMillis(); // 当前的毫秒数

		Log.d(TAG,
				"----------now="
						+ DateTimeUtils.time2String("yyyy-MM-dd-hh-mm", now));
		// Log.d(TAG, "----------now=" + now);

		switch (dateMode) {

		// 不指定
		case DATE_MODE_NONE:

			if (now < scheduleTime) {

				nextTime = scheduleTime;
			} else {

				nextTime = 0;
			}

			break;

		// 每天
		// 获取当前系统时间T1，然后设置时间为触发时间（几点几分），并获取时间为T2。
		// 判断如果T1<T2,那么设置T2为闹钟的时间，反之，则把当前时间加上一天，然后获取时间T3并设置其为触发时间。
		case DATE_MODE_DAY:

			// 把时间移到当天的闹钟的时间
			c.set(Calendar.HOUR_OF_DAY,
					Integer.parseInt(DateTimeUtils.time2String("H", time)));
			c.set(Calendar.MINUTE,
					Integer.parseInt(DateTimeUtils.time2String("m", time)));
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);

			nextTime = c.getTimeInMillis();
			// Log.d(TAG, "----------nextTime1=" +
			// DateTimeUtils.time2String("yyyy-MM-dd-hh-mm", nextTime));
			// Log.d(TAG, "----------nextTime1=" + nextTime);

			if (now >= nextTime) // 准时闹，或者上一次没有触发，且还没有到下一次触发时间
			{

				c.add(Calendar.DAY_OF_MONTH, 1);// 往后面推迟一天
				nextTime = c.getTimeInMillis();
				// Log.d(TAG, "----------nextTime2=" +
				// DateTimeUtils.time2String("yyyy-MM-dd-hh-mm", nextTime));
			}

			nextTime = checkAlive(nextTime);

			break;

		case DATE_MODE_MONTH:

			c.set(Calendar.DAY_OF_MONTH,
					Integer.parseInt(DateTimeUtils.time2String("d", time)));
			Log.d(TAG,
					"----------c="
							+ DateTimeUtils.time2String("yyyy-MM-dd-hh-mm",
									c.getTimeInMillis()));
			Log.d(TAG, "----------c=" + c.getTimeInMillis());
			// 把时间移到当天的闹钟的时间
			c.set(Calendar.HOUR_OF_DAY,
					Integer.parseInt(DateTimeUtils.time2String("H", time)));
			c.set(Calendar.MINUTE,
					Integer.parseInt(DateTimeUtils.time2String("m", time)));
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			nextTime = c.getTimeInMillis();
			Log.d(TAG,
					"----------setTime="
							+ DateTimeUtils.time2String("yyyy-MM-dd-hh-mm",
									nextTime));
			Log.d(TAG, "----------setTime=" + nextTime);

			if (now >= nextTime) // 准时闹，或者上一次没有触发，且还没有到下一次触发时间
			{

				c.add(Calendar.MONTH, 1);// 往后面推迟一个月
				nextTime = c.getTimeInMillis();
				Log.d(TAG,
						"----------nextTime2="
								+ DateTimeUtils.time2String("yyyy-MM-dd-hh-mm",
										nextTime));
			}

			nextTime = checkAlive(nextTime);

			break;

		// 首先获得今天是星期几,
		case DATE_MODE_WEEK:

			final int[] checkedWeeks = parseDateWeeks(weekValue);
			if (null != checkedWeeks) {

				for (int week : checkedWeeks) {

					c.set(Calendar.DAY_OF_WEEK, (int) (week + 1));
					// 把时间移到当天的闹钟的时间
					c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(DateTimeUtils
							.time2String("H", time)));
					c.set(Calendar.MINUTE, Integer.parseInt(DateTimeUtils
							.time2String("m", time)));
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);

					long triggerAtTime = c.getTimeInMillis();

					if (triggerAtTime <= now) { // 下周
						triggerAtTime += AlarmManager.INTERVAL_DAY * 7;
					}

					// 保存最近闹钟时间
					if (0 == nextTime) {

						nextTime = triggerAtTime;

					} else {

						nextTime = Math.min(triggerAtTime, nextTime);
					}
				}

			}

			nextTime = checkAlive(nextTime);
			break;

		case DATE_MODE_YEAR:

			c.set(Calendar.MONTH,
					Integer.parseInt(DateTimeUtils.time2String("M", time)));
			c.set(Calendar.DAY_OF_MONTH,
					Integer.parseInt(DateTimeUtils.time2String("d", time)));
			// 把时间移到当天的闹钟的时间
			c.set(Calendar.HOUR_OF_DAY,
					Integer.parseInt(DateTimeUtils.time2String("H", time)));
			c.set(Calendar.MINUTE,
					Integer.parseInt(DateTimeUtils.time2String("m", time)));
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			nextTime = c.getTimeInMillis();

			nextTime = checkAlive(nextTime);
			break;

		default:

			nextTime = 0;
			break;
		}

		// nextTime = nextTime - aheadTime;

		return nextTime;
	}

	private long checkAlive(long nextTime) {
		return nextTime;
		// if (nextTime < startTime) {
		//
		// nextTime = startTime;
		// } else if (nextTime > endTime) {
		//
		// nextTime = 0;
		// }

	}

	private int[] parseDateWeeks(String value) {

		int[] weeks = null;
		ArrayList<Integer> arrayList = new ArrayList<Integer>();

		char[] weekString = value.toCharArray();

		for (int i = 0; i < weekString.length; i++) {

			String weekNumber = new String(weekString, i, 1);
			int number = Integer.parseInt(weekNumber);

			if (number == 1) {

				arrayList.add(i + 1);

			} else {

			}
		}

		weeks = new int[arrayList.size()];

		for (int i = 0; i < arrayList.size(); i++) {

			weeks[i] = arrayList.get(i);
		}

		return weeks;
	}

	public static long[][] parseDateMonthsAndDays(String value) {
		long[][] values = new long[2][];

		try {

			final String[] items = value.split("\\|");
			final String[] monthStrs = items[0].split(",");
			final String[] dayStrs = items[1].split(",");
			values[0] = new long[monthStrs.length];
			values[1] = new long[dayStrs.length];

			int i = 0;
			for (String s : monthStrs) {
				values[0][i++] = Long.valueOf(s);
			}

			i = 0;
			for (String s : dayStrs) {
				values[1][i++] = Long.valueOf(s);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return values;
	}
}
