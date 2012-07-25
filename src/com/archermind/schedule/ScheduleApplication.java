package com.archermind.schedule;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.ServerInterface;

public class ScheduleApplication extends Application{
	
	private static ScheduleApplication instance;
	private final static String TAG = "Schedule";
	private ServerInterface serverinterface;
	
	public ScheduleApplication(){
		ScheduleApplication.instance = this;
		serverinterface = new ServerInterface();
	}
	
	 public static Context getContext() {
	        return ScheduleApplication.instance;
	    }
	 
	 public ServerInterface getServerInterface()
	 {
		 return serverinterface;
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