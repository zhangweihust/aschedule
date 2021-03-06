package com.android.schedule.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.android.schedule.ScheduleApplication;
import com.android.schedule.Provider.DatabaseHelper;
import com.android.schedule.Provider.DatabaseManager;
import com.android.schedule.Utils.DateTimeUtils;

public class BootRecevier extends BroadcastReceiver {
	private String remindCycle;

	private String weekValue;

	private long flagAlarm;

	private long startTime;

	private long endTime;

	private boolean mStageRemind;

	private long schedule_id;

	private Context mContext;

	public void onReceive(Context context, Intent intent) {
		ScheduleApplication.LogD(BootRecevier.class,
				"action:" + intent.getAction());
		mContext = context;

		new Thread() {
			public void run() {
				try {
				DatabaseManager dbManager = new DatabaseManager(mContext);
				dbManager.openwithnoservice();

				Cursor c = dbManager.queryNotOutdateschedule(System
						.currentTimeMillis());
				ScheduleApplication.LogD(BootRecevier.class,
					" 需要修改日程闹钟的个数是 " + c.getCount());
				if (c != null) {
					for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
						schedule_id = c
								.getLong(c
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_ID));
						remindCycle = c
								.getString(c
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD));
						startTime = c
								.getLong(c
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME));
						weekValue = c
								.getString(c
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_WEEK));
						endTime = Long
								.valueOf(c.getString(c
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_END)));
						flagAlarm = c
								.getLong(c
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_ALARM_FLAG));
						mStageRemind = c
								.getInt(c
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_STAGE_FLAG)) == 1;
						long nextTime = DateTimeUtils.getNextAlarmTime(
								mStageRemind, startTime, endTime, startTime,
								remindCycle, weekValue);
						ScheduleApplication.LogD(
								BootRecevier.class,
								"nextTime: "
										+ DateTimeUtils.time2String(
												"yyyy-MM-dd-HH-mm", nextTime));
						DateTimeUtils.sendAlarm(nextTime, flagAlarm,
								schedule_id);
					}
					
					c.close();
				}

				dbManager.close();
				} catch (Exception e) {
					ScheduleApplication.logException(getClass(), e);
				}
			}
		}.start();
	}

}
