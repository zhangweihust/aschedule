package com.archermind.schedule;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.archermind.schedule.Services.ServiceManager;

public class ScheduleApplication extends Application{
	
	private static ScheduleApplication instance;
	private final static String TAG = "Schedule";
	
	public ScheduleApplication(){
		ScheduleApplication.instance = this;
	}
	
	
	 public static Context getContext() {
	        return ScheduleApplication.instance;
	    }
	 
	
	 @Override
		public void onCreate() {
			super.onCreate();
			 if (ServiceManager.isStarted()) {
				} else {
					if (!ServiceManager.start()) {
						ServiceManager.exit();
						return;
					}
				}
		}
	 
	 @SuppressWarnings("rawtypes")
		public static void LogD(Class classz, String str){
			Log.d(TAG, classz.getCanonicalName() + "--->" + str);
		}
}