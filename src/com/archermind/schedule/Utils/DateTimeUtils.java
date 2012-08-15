package com.archermind.schedule.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Services.AlarmRecevier;
import com.archermind.schedule.Services.ServiceManager;

public class DateTimeUtils {
	public static String time2String(String formatter, long date) {
		Date d = new Date(date);
		SimpleDateFormat dateFormat = new SimpleDateFormat(formatter);
		String dateTime = dateFormat.format(d);
		// ScheduleApplication.LogD(DateTimeUtils.class, "time=" + dateTime);
		return dateTime;
	}
	
	public static long time2Long(String formatter, String date) {
		Date d = null; 
		SimpleDateFormat sdf = new SimpleDateFormat(formatter);
		try {
			d = sdf.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return d.getTime();
	}


	public static long getDayOfWeek(int week, long timeInMillis) {
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd hh:mm:ss a EEEE");
		Calendar time = Calendar.getInstance(Locale.CHINA);
		time.setTimeInMillis(timeInMillis);
		// ScheduleApplication.LogD(DateTimeUtils.class,
		// "当前时间:"+sdf.format(time.getTime()));
		if (week == Calendar.SUNDAY) {
			int nowDay = time.get(Calendar.DAY_OF_MONTH);
			time.set(Calendar.DATE, nowDay + 7);
			time.set(Calendar.HOUR_OF_DAY, 23);
			time.set(Calendar.MINUTE, 59);
			time.set(Calendar.SECOND, 59);
			// ScheduleApplication.LogD(DateTimeUtils.class,
			// "本周日时间:"+sdf.format(time.getTime()));
		} else if (week == Calendar.MONDAY) {
			time.set(Calendar.HOUR_OF_DAY, 0);
			time.set(Calendar.MINUTE, 0);
			time.set(Calendar.SECOND, 0);
		}
		time.set(Calendar.DAY_OF_WEEK, week);
		// ScheduleApplication.LogD(DateTimeUtils.class,
		// "时间:"+sdf.format(time.getTime()));
		return time.getTimeInMillis();
	}

	public static long getThreeDaysBefore(long timeInMillis) {
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd hh:mm:ss a EEEE");
		Calendar time = Calendar.getInstance(Locale.CHINA);
		time.setTimeInMillis(timeInMillis);
		// ScheduleApplication.LogD(DateTimeUtils.class,
		// "当前时间:"+sdf.format(time.getTime()));
		int nowDay = time.get(Calendar.DAY_OF_MONTH);
		time.set(Calendar.DATE, nowDay - 3);
		time.set(Calendar.HOUR_OF_DAY, 0);
		time.set(Calendar.MINUTE, 0);
		time.set(Calendar.SECOND, 0);
		// ScheduleApplication.LogD(DateTimeUtils.class,
		// "3天前开始时间:"+sdf.format(time.getTime()));
		return time.getTimeInMillis();
	}

	public static long getYesterdayEnd(long timeInMillis) {
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd hh:mm:ss a EEEE");
		Calendar time = Calendar.getInstance(Locale.CHINA);
		time.setTimeInMillis(timeInMillis);
		// ScheduleApplication.LogD(DateTimeUtils.class,
		// "当前时间:"+sdf.format(time.getTime()));
		int nowDay = time.get(Calendar.DAY_OF_MONTH);
		time.set(Calendar.DATE, nowDay - 1);
		time.set(Calendar.HOUR_OF_DAY, 23);
		time.set(Calendar.MINUTE, 59);
		time.set(Calendar.SECOND, 59);
		// ScheduleApplication.LogD(DateTimeUtils.class,
		// "昨天结束时间:"+sdf.format(time.getTime()));
		return time.getTimeInMillis();
	}

	public static long getToday(int amORpm, long timeInMillis) {
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd hh:mm:ss a EEEE");
		Calendar time = Calendar.getInstance(Locale.CHINA);
		time.setTimeInMillis(timeInMillis);
		// ScheduleApplication.LogD(DateTimeUtils.class,
		// "当前时间:"+sdf.format(time.getTime()));
		if (amORpm == Calendar.AM) {
			time.set(Calendar.HOUR_OF_DAY, 0);
			time.set(Calendar.MINUTE, 0);
			time.set(Calendar.SECOND, 0);
			// ScheduleApplication.LogD(DateTimeUtils.class,
			// "今天开始时间:"+sdf.format(time.getTime()));
		} else {
			time.set(Calendar.HOUR_OF_DAY, 23);
			time.set(Calendar.MINUTE, 59);
			time.set(Calendar.SECOND, 59);
			// ScheduleApplication.LogD(DateTimeUtils.class,
			// "今天结束时间:"+sdf.format(time.getTime()));
		}
		return time.getTimeInMillis();
	}

	public static long getTomorrow(int amORpm, long timeInMillis) {
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd hh:mm:ss a EEEE");
		Calendar time = Calendar.getInstance(Locale.CHINA);
		time.setTimeInMillis(timeInMillis);
		// ScheduleApplication.LogD(DateTimeUtils.class,
		// "当前时间:"+sdf.format(time.getTime()));
		int nowDay = time.get(Calendar.DAY_OF_MONTH);
		time.set(Calendar.DATE, nowDay + 1);
		if (amORpm == Calendar.AM) {
			time.set(Calendar.HOUR_OF_DAY, 0);
			time.set(Calendar.MINUTE, 0);
			time.set(Calendar.SECOND, 0);
			// ScheduleApplication.LogD(DateTimeUtils.class,
			// "明天开始时间:"+sdf.format(time.getTime()));
		} else {
			time.set(Calendar.HOUR_OF_DAY, 23);
			time.set(Calendar.MINUTE, 59);
			time.set(Calendar.SECOND, 59);
			// ScheduleApplication.LogD(DateTimeUtils.class,
			// "明天结束时间:"+sdf.format(time.getTime()));
		}
		return time.getTimeInMillis();
	}

	 /*
     * isStage:是否设置阶段提醒 startStage：阶段提醒开始时间 endStage：阶段提醒结束时间 setTime：闹钟设置的时间
     * mode：闹钟重复的类型 week：闹钟按周重复里面包含的星期几
     */
    public static long getNextAlarmTime(boolean isStage, long startStage,
            long endStage, long setTime, String mode, String week) {
        Calendar time = Calendar.getInstance(Locale.CHINA);
        long currentTime = System.currentTimeMillis();
        System.out
                .println("currentTime:"
                        + DateTimeUtils.time2String("yyyy-MM-dd hh:mm:ss",
                                currentTime));
        if (isStage) {// 设置了闹钟的区间段，开始时间startStage，结束时间endStage
            if (currentTime < startStage) {
                time.setTimeInMillis(setTime);// 取得闹钟设置的时刻，并换算为时间段开始的那一天
                time.set(Calendar.YEAR, Integer.parseInt(DateTimeUtils
                        .time2String("yyyy", startStage)));
                time.set(Calendar.MONTH, Integer.parseInt(DateTimeUtils
                        .time2String("MM", startStage)) - 1);
                time.set(Calendar.DAY_OF_MONTH, Integer.parseInt(DateTimeUtils
                        .time2String("dd", startStage)));
                if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_NONE
                        .equals(mode)) {// 闹钟没有重复
                    return time.getTimeInMillis();
                } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_DAY
                        .equals(mode)) {// 闹钟按天重复
                    return time.getTimeInMillis();
                } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_YEAR
                        .equals(mode)) {// 闹钟按年重复
                    time.add(Calendar.YEAR, 1);
                    if (time.getTimeInMillis() > endStage) {// 一年后的时间超出了范围
                        return 0;
                    } else {
                        return time.getTimeInMillis();
                    }
                } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_MONTH
                        .equals(mode)) {// 闹钟按月重复
                    time.add(Calendar.MONTH, 1);
                    if (time.getTimeInMillis() > endStage) {// 一个月后的时间超出了范围
                        return 0;
                    } else {
                        return time.getTimeInMillis();
                    }
                } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_WEEK
                        .equals(mode)) {// 闹钟按周重复
                    int weekDay = getWeekDay(time);
                    if (week.contains(String.valueOf(weekDay))) {// 周重复里面包含了阶段第一天
                        return time.getTimeInMillis();
                    } else {
                        boolean flag = true;
                        while (flag) {// 循环往后遍历每一天
                            time.add(Calendar.DAY_OF_MONTH, 1);
                            weekDay = getWeekDay(time);
                            if (week.contains(String.valueOf(weekDay))) {
                                flag = false;
                                if (time.getTimeInMillis() > endStage) {// 最近的一个周几已经超出范围
                                    return 0;
                                } else {
                                    return time.getTimeInMillis();
                                }
                            } else {
                                if (time.getTimeInMillis() > endStage) {// 遍历的已经超出范围
                                    return 0;
                                }
                            }
                        }
                    }
                }
            } else if (startStage < currentTime && currentTime < endStage) {// 当前事件在时间阶段内
                time.setTimeInMillis(setTime);// 取得闹钟设置的时刻，并换算为时间段开始的那一天
                time.set(Calendar.YEAR, Integer.parseInt(DateTimeUtils
                        .time2String("yyyy", currentTime)));
                time.set(Calendar.MONTH, Integer.parseInt(DateTimeUtils
                        .time2String("MM", currentTime)) - 1);
                time.set(Calendar.DAY_OF_MONTH, Integer.parseInt(DateTimeUtils
                        .time2String("dd", currentTime)));
                String tmp = DateTimeUtils.time2String("yyyy-MM-dd HH:mm:ss",
                        time.getTimeInMillis());
                if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_NONE
                        .equals(mode)) {// 闹钟没有重复
                    if (time.getTimeInMillis() > currentTime) {// 今天的闹钟时间还没有错过
                        return time.getTimeInMillis();
                    } else {
                        time.add(Calendar.DAY_OF_MONTH, 1);
                        tmp = DateTimeUtils.time2String("yyyy-MM-dd HH:mm:ss",
                                time.getTimeInMillis());
                        if (time.getTimeInMillis() > endStage) {// 往后移动一天超出范围
                            return 0;
                        } else {
                            return time.getTimeInMillis();
                        }
                    }
                } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_DAY
                        .equals(mode)) {
                    if (time.getTimeInMillis() > currentTime) {// 今天的闹钟时间还没有错过
                        return time.getTimeInMillis();
                    } else {
                        time.add(Calendar.DAY_OF_MONTH, 1);
                        tmp = DateTimeUtils.time2String("yyyy-MM-dd HH:mm:ss",
                                time.getTimeInMillis());
                        if (time.getTimeInMillis() > endStage) {// 往后移动一天超出范围
                            return 0;
                        } else {
                            return time.getTimeInMillis();
                        }
                    }
                } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_YEAR
                        .equals(mode)) {
                    time.set(Calendar.YEAR, Integer.parseInt(DateTimeUtils
                            .time2String("yyyy", startStage)));
                    time.set(Calendar.MONTH, Integer.parseInt(DateTimeUtils
                            .time2String("MM", startStage)) - 1);
                    time.set(Calendar.DAY_OF_MONTH, Integer
                            .parseInt(DateTimeUtils.time2String("dd",
                                    startStage)));
                    time.add(Calendar.YEAR, 1);
                    if (time.getTimeInMillis() > endStage) {// 一年后的时间超出了范围
                        return 0;
                    } else {
                        return time.getTimeInMillis();
                    }
                } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_MONTH
                        .equals(mode)) {
                    time.set(Calendar.YEAR, Integer.parseInt(DateTimeUtils
                            .time2String("yyyy", startStage)));
                    time.set(Calendar.MONTH, Integer.parseInt(DateTimeUtils
                            .time2String("MM", startStage)) - 1);
                    time.set(Calendar.DAY_OF_MONTH, Integer
                            .parseInt(DateTimeUtils.time2String("dd",
                                    startStage)));
                    time.add(Calendar.MONTH, 1);
                    if (time.getTimeInMillis() > endStage) {// 一个月后的时间超出了范围
                        return 0;
                    } else {
                        return time.getTimeInMillis();
                    }
                } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_WEEK
                        .equals(mode)) {
                    int weekDay = getWeekDay(time);
                    if (week.contains(String.valueOf(weekDay))) {// 周重复里面包含了阶段第一天
                        if (time.getTimeInMillis() > currentTime) {
                            return time.getTimeInMillis();
                        }
                    }
                    boolean flag = true;
                    while (flag) {// 循环往后遍历每一天
                        time.add(Calendar.DAY_OF_MONTH, 1);
                        weekDay = getWeekDay(time);
                        if (week.contains(String.valueOf(weekDay))) {
                            flag = false;
                            if (time.getTimeInMillis() > endStage) {// 最近的一个周几已经超出范围
                                return 0;
                            } else {
                                return time.getTimeInMillis();
                            }
                        } else {
                            if (time.getTimeInMillis() > endStage) {// 遍历的已经超出范围
                                return 0;
                            }
                        }
                    }

                }
            } else if (currentTime > endStage) {// 当前时间已经超出范围，说明已经过期日志
                return 0;
            }
        } else {// 没有设置阶段提醒的
            time.setTimeInMillis(setTime);// 取得闹钟设置的时刻，并换算为时间段开始的那一天
            String tmp = DateTimeUtils.time2String("yyyy-MM-dd HH:mm:ss",
                    time.getTimeInMillis());
            tmp = DateTimeUtils.time2String("yyyy-MM-dd HH:mm:ss", currentTime);
            if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_NONE.equals(mode)) {// 闹钟没有重复
                if (time.getTimeInMillis() > currentTime) {// 闹钟设置时间大于当前时间，可以设置
                    return time.getTimeInMillis();
                } else {
                    return 0;
                }
            } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_DAY
                    .equals(mode)) {
                if (time.getTimeInMillis() > currentTime) {// 闹钟设置时间大于当前时间，可以设置
                    return time.getTimeInMillis();
                } else {// 闹钟设置时间小于当前时间，那么取当前时间设置闹钟
                    time.set(Calendar.YEAR, Integer.parseInt(DateTimeUtils
                            .time2String("yyyy", currentTime)));
                    time.set(Calendar.MONTH, Integer.parseInt(DateTimeUtils
                            .time2String("MM", currentTime)) - 1);
                    time.set(Calendar.DAY_OF_MONTH, Integer
                            .parseInt(DateTimeUtils.time2String("dd",
                                    currentTime)));
                    if (time.getTimeInMillis() > currentTime) {// 今天的闹钟时间没有绰过
                        return time.getTimeInMillis();
                    } else {// 错过了今天，设置明天
                        time.add(Calendar.DAY_OF_MONTH, 1);
                        return time.getTimeInMillis();
                    }
                }
            } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_YEAR
                    .equals(mode)) {
                if (time.getTimeInMillis() > currentTime) {// 闹钟设置时间大于当前时间，可以设置
                    return time.getTimeInMillis();
                } else {// 今天的闹钟错过了，设置明年的闹钟
                    time.add(Calendar.YEAR, 1);
                    return time.getTimeInMillis();
                }
            } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_MONTH
                    .equals(mode)) {
                if (time.getTimeInMillis() > currentTime) {// 闹钟设置时间大于当前时间，可以设置
                    return time.getTimeInMillis();
                } else {// 今天的闹钟错过了，设置下个月的闹钟
                    time.add(Calendar.MONTH, 1);
                    return time.getTimeInMillis();
                }
            } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_WEEK
                    .equals(mode)) {
                int weekDay = getWeekDay(time);
                if (time.getTimeInMillis() > currentTime) {// 闹钟设置时间大于当前时间，可以设置
                    boolean flag = true;
                    if (week.contains(String.valueOf(weekDay))) {
                        return time.getTimeInMillis();
                    }
                    while (flag) {// 循环往后遍历每一天
                        time.add(Calendar.DAY_OF_MONTH, 1);
                        weekDay = getWeekDay(time);
                        if (week.contains(String.valueOf(weekDay))) {
                            return time.getTimeInMillis();
                        }
                    }

                } else {// 今天的闹钟错过了，设置下一次的闹钟
                    boolean flag = true;
                    time.set(Calendar.YEAR, Integer.parseInt(DateTimeUtils
                            .time2String("yyyy", currentTime)));
                    time.set(Calendar.MONTH, Integer.parseInt(DateTimeUtils
                            .time2String("MM", currentTime)) - 1);
                    time.set(Calendar.DAY_OF_MONTH, Integer
                            .parseInt(DateTimeUtils.time2String("dd",
                                    currentTime)));
                    if (time.getTimeInMillis() > currentTime) {// 今天的闹钟时间没有绰过
                        if (week.contains(String.valueOf(getWeekDay(time)))) {
                            return time.getTimeInMillis();
                        }
                    }
                    while (flag) {// 循环往后遍历每一天
                        time.add(Calendar.DAY_OF_MONTH, 1);
                        weekDay = getWeekDay(time);
                        if (week.contains(String.valueOf(weekDay))) {
                            return time.getTimeInMillis();
                        }
                    }
                }
            }
        }
        return 0;
    }
    
    public static void cancelAlarm(long schedule_id) {
		Cursor c = ServiceManager.getDbManager().queryScheduleById(schedule_id);
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			long flagAlarm = c.getLong(c
							.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_FLAG));
			Intent alarmIntent = new Intent(ScheduleApplication.getContext(),
					AlarmRecevier.class);
			alarmIntent.setAction("" + flagAlarm);
			alarmIntent.putExtra("schedule_id", schedule_id);
			ScheduleApplication.LogD(DateTimeUtils.class, "cancel schedule_id:" + schedule_id + " flagAlarm:" + flagAlarm);
			AlarmManager am = (AlarmManager) ScheduleApplication.getContext().getSystemService(Context.ALARM_SERVICE);
			PendingIntent pi = PendingIntent.getBroadcast(
					ScheduleApplication.getContext(), 1, alarmIntent, 0);
			am.cancel(pi);
		}
		c.close();
	}

	public static void sendAlarm(Long time, long flagAlarm, long schedule_id) {
		Intent alarmIntent = new Intent(ScheduleApplication.getContext(),
				AlarmRecevier.class);
		alarmIntent.setAction("" + flagAlarm);
		alarmIntent.putExtra("schedule_id", schedule_id);
		ScheduleApplication.LogD(DateTimeUtils.class, "set schedule_id:" + schedule_id + " flagAlarm:" + flagAlarm);
		PendingIntent pi = PendingIntent.getBroadcast(ScheduleApplication.getContext(),
				1, alarmIntent, 0);
		AlarmManager am = (AlarmManager) ScheduleApplication.getContext().getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, time, pi);
	}
	
	public static int getWeekDay(Calendar time){//因为API里面获得的星期天是1，星期六是7，所以这里要做个转换
		int weekDay = time.get(Calendar.DAY_OF_WEEK);
		weekDay = weekDay - 1;
		if (weekDay == 0){
			weekDay = 7;
		}
		return weekDay;
	}
}
