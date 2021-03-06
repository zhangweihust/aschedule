package com.android.schedule.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.TextUtils;

import com.android.schedule.ScheduleApplication;
import com.android.schedule.Model.UserInfoData;
import com.android.schedule.Provider.DatabaseHelper;
import com.android.schedule.Provider.DatabaseManager;
import com.android.schedule.Screens.HomeScreen;
import com.android.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.R;

public class AlarmRecevier extends BroadcastReceiver {

	private NotificationManager mNotificationManager;

	private Notification mNotification;

	private PendingIntent mPendingIntent;

	private String remindCycle;

	private String weekValue;

	private long startTime;

	private long endTime;

	private long flagAlarm;

	private String schedule_content;

	private boolean mStageRemind;

	private Context mContext;

	private long schedule_id;

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
		mContext = context;
		schedule_id = intent.getLongExtra("schedule_id", 1);
		long alarmTime = intent.getLongExtra("alarmtime", 1);
		ScheduleApplication.LogD(AlarmRecevier.class, " 闹钟设置的时间是 alarmTime = "
				+ DateTimeUtils.time2String("yyyy-MM-dd-HH-mm", alarmTime));
		long currentTime = System.currentTimeMillis();
		ScheduleApplication.LogD(AlarmRecevier.class, " 当前时间是 currentTime = "
				+ DateTimeUtils.time2String("yyyy-MM-dd-HH-mm", currentTime));

		if (currentTime - alarmTime < 59 * 1000) {// 说明是准时触发，通知栏提示

			ScheduleApplication.LogD(AlarmRecevier.class, "时间在误差之内闹钟被触发");

			mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			// 点击通知进入的界面
			Intent mIntent = new Intent(context, HomeScreen.class);
			mIntent.putExtra("notify_id", (int) schedule_id);
			mPendingIntent = PendingIntent.getActivity(context, 0, mIntent, 0);
			mNotification = new Notification();
			mNotification.icon = R.drawable.schedule_icon;
			mNotification.tickerText = "你有一个新日程";
			mNotification.flags = Notification.FLAG_AUTO_CANCEL;
			SharedPreferences spSetting = context.getSharedPreferences(
					UserInfoData.USER_SETTING, Context.MODE_WORLD_READABLE);

			String notificationStr = spSetting.getString(
					UserInfoData.SETTING_SOUND_REMIND, "");
			if (notificationStr.equals("slient")) {// 说明用户选择了静音

				mNotification.sound = null;
			} else { // 没有选择，使用默认的铃声。有选择，使用选择了的铃声

				mNotification.sound = TextUtils.isEmpty(notificationStr)
						? RingtoneManager
								.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
						: Uri.parse(notificationStr);
			}

			mNotification.setLatestEventInfo(context, "你有一个新日程",
					schedule_content, mPendingIntent);
			mNotificationManager.notify((int) schedule_id, mNotification);

		} else { // 说明是系统自动触发已过期的闹钟，不提示

			ScheduleApplication.LogD(AlarmRecevier.class, "时间在误差之外,闹钟已过期");
		}

		new Thread() {
			public void run() {
				try {
				DatabaseManager dbManager = new DatabaseManager(mContext);
				dbManager.openwithnoservice();
				long nextTime = 0;
				// 读取数据库
				Cursor c = dbManager.queryScheduleById((int) schedule_id);
				if (c != null) {

					if (c.getCount() > 0) {

						c.moveToFirst();
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
						schedule_content = c
								.getString(c
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT));
						mStageRemind = c
								.getInt(c
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_STAGE_FLAG)) == 1;

						nextTime = DateTimeUtils.getNextAlarmTime(mStageRemind,
								startTime, endTime, startTime, remindCycle,
								weekValue);
						ScheduleApplication.LogD(
								AlarmRecevier.class,
								"nextTime: "
										+ DateTimeUtils.time2String(
												"yyyy-MM-dd-HH-mm", nextTime));
					}
					
					c.close();
				}

				if (nextTime != 0) {

					// 设置闹钟
					DateTimeUtils.sendAlarm(nextTime, flagAlarm, schedule_id);
					ScheduleApplication.LogD(AlarmRecevier.class, "日程的下一次时间是："
							+ DateTimeUtils.time2String(
									"yyyy-MM-dd-HH-mm", nextTime));

				} else {

					ScheduleApplication.LogD(AlarmRecevier.class,
							"这是本日程的最后一个提醒，没有下一次的时间");
				}

				dbManager.close();
				} catch (Exception e) {
					ScheduleApplication.logException(getClass(), e);
				}
			}
		}.start();
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(), e);
		}
	}
}
