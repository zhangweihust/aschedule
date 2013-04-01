package com.android.schedule.Services;
import com.android.schedule.ScheduleApplication;
import com.android.schedule.Task.SendCrashReportsTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CrashReportService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ScheduleApplication.LogD(CrashReportService.class,
				"CrashReportService onCreate");
		try {
		SendCrashReportsTask task = new SendCrashReportsTask(this);
		task.execute();
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(), e);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ScheduleApplication.LogD(CrashReportService.class,
				"CrashReportService onDestroy");
	}

}
