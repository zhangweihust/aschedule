package com.archermind.schedule.Services;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Task.SendCrashReportsTask;

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
		SendCrashReportsTask task = new SendCrashReportsTask(this);
		task.execute();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ScheduleApplication.LogD(CrashReportService.class,
				"CrashReportService onDestroy");
	}

}
