package com.android.schedule.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.android.schedule.ScheduleApplication;

public class TimeTickService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();

		registerReceiver(this.mTimerTick, new IntentFilter(
				Intent.ACTION_TIME_TICK));
		ScheduleApplication.LogD(getClass(), "oncreate");
	}

	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

	// 声明一个广播接受对象，用接受时间的变化
	private BroadcastReceiver mTimerTick = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {

			ScheduleApplication.LogD(TimeTickService.class, "mTimerTick  "
					+ intent.getAction());

			// 在这里直接修改widget
			if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {

				Intent timeIntent = new Intent(
						"com.archermind.TimeTickService.tick");
				context.sendBroadcast(timeIntent);

			}
		}
	};

}
