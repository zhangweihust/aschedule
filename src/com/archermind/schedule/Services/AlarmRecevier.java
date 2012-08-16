package com.archermind.schedule.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.archermind.schedule.R;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Screens.HomeScreen;
import com.archermind.schedule.Utils.DateTimeUtils;

public class AlarmRecevier extends BroadcastReceiver {
	private static final String TAG = "AlarmRecever";
	private NotificationManager mNotificationManager;
	private Notification mNotification;
	private PendingIntent mPendingIntent;
	private String remindCycle;
//	private long scheduleTime;
	private String weekValue;
	private long startTime;
	private long endTime;
	private long flagAlarm;
	private String schedule_content;
	private boolean mStageRemind;

	@Override
	public void onReceive(Context context, Intent intent) {
		long schedule_id = intent.getLongExtra("schedule_id", 1);
		// 读取数据库
		Cursor c = ServiceManager.getDbManager().queryScheduleById(
				(int) schedule_id);
		if(c != null && c.getCount() > 0){			
			c.moveToFirst();
			remindCycle = c.getString(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD));
			startTime = c.getLong(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME));
			weekValue = c.getString(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_WEEK));
			endTime = Long.valueOf(c.getString(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_END)));
			flagAlarm = c.getLong(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_ALARM_FLAG));
			schedule_content=c.getString(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT));	
			mStageRemind= c .getInt(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_STAGE_FLAG)) ==1;
		}
		c.close();
		// 通知栏提示
		mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// 点击通知进入的界面
		Intent mIntent = new Intent(context, HomeScreen.class);
		mIntent.putExtra("notify_id", (int)schedule_id);
		mPendingIntent = PendingIntent.getActivity(context, 0, mIntent, 0);
		mNotification = new Notification();
		mNotification.icon = R.drawable.schedule_icon;
		mNotification.tickerText = "你有一个新日程";
		mNotification.flags = Notification.FLAG_AUTO_CANCEL; 
		// 设置通知为默认声音
		mNotification.defaults = Notification.DEFAULT_SOUND;
		mNotification.setLatestEventInfo(context, "你有一个新日程", schedule_content,
				mPendingIntent);
		mNotificationManager.notify((int)schedule_id, mNotification);
		Log.d(TAG, "-----------shedule_id="+schedule_id);
		long nextTime = DateTimeUtils.getNextAlarmTime(mStageRemind, startTime, endTime, startTime, remindCycle, weekValue);
	    System.out.println("result receiver:"
	                + DateTimeUtils.time2String("yyyy-MM-dd HH:mm:ss", nextTime));
		// 设置闹钟过期
		if (nextTime == 0) {
			ContentValues cv = new ContentValues();
			cv.put(DatabaseHelper.COLUMN_SCHEDULE_FLAG_OUTDATE, true);
			ServiceManager.getDbManager().updateScheduleById(schedule_id,
					cv);
		} else {
			// 设置闹钟
			DateTimeUtils.sendAlarm(nextTime, flagAlarm, schedule_id);
		}

	}

}
