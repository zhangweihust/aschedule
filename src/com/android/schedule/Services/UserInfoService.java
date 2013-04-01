package com.android.schedule.Services;

import com.android.schedule.ScheduleApplication;
import com.android.schedule.Task.DeviceInfoThread;
import com.android.schedule.Task.UserActiveInfoThread;
import com.android.schedule.Utils.Constant;
import com.android.schedule.Utils.DateTimeUtils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserInfoService{
	public static final long COUNT_DURATION = 10 * 60 * 1000;
	public static final int COUNT_TIMES = 3;
	public static final int USER_ACTIVE_DURATION = 7;
	private DeviceInfoThread deviceInfoThread;
	private UserActiveInfoThread userActiveInfoThread;
	private long startTime;

	public boolean start() {
		UserInfoThread userInfoThread = new UserInfoThread();
		userInfoThread.start();
		return true;
	}

	public boolean stop() {
		if (deviceInfoThread != null) {
			deviceInfoThread.stopThread();
		}
		
		if (userActiveInfoThread != null) {
			userActiveInfoThread.stopThread();
		}
		Context context = ScheduleApplication.getContext();
		SharedPreferences spActive = context.getSharedPreferences(UserActiveInfoThread.PREF, 0);
		SharedPreferences.Editor editor = spActive.edit();
		long lastDuration = spActive.getLong(UserActiveInfoThread.PREF_KEY_DURATION, 0);
		long duration = System.currentTimeMillis() - startTime + lastDuration;
		editor.putLong(UserActiveInfoThread.PREF_KEY_DURATION, duration);
		editor.commit();
		return true;
	}
	
	class UserInfoThread extends Thread{
		@Override
		public void run(){
			try {
			System.out.println("===userinfothread== run");
			Context context = ScheduleApplication.getContext();
			SharedPreferences sp = context.getSharedPreferences(DeviceInfoThread.PREF, 0);
		    if(!sp.getBoolean(Constant.SendUserInfo.SEND_USER_DEVICE_INFO, false)){
		    	deviceInfoThread = new DeviceInfoThread();
		    	deviceInfoThread.start();
		    }
		    
		    SharedPreferences spActive = context.getSharedPreferences(UserActiveInfoThread.PREF, 0);    
		    long lastOKDate = spActive.getLong(UserActiveInfoThread.PREF_KEY_OK_LASTDATE, 0);
		    long curDate = Long.parseLong(DateTimeUtils.time2String("yyyyMMdd", System.currentTimeMillis()));
		    if((curDate - lastOKDate) >= USER_ACTIVE_DURATION){
		    	userActiveInfoThread = new UserActiveInfoThread();
		    	userActiveInfoThread.start();
		    }
		    long lastDate = spActive.getLong(UserActiveInfoThread.PREF_KEY_LAST_DATE, 0);
		    if(lastDate < curDate){
		    	SharedPreferences.Editor editor = spActive.edit();
		    	editor.putLong(UserActiveInfoThread.PREF_KEY_LAST_DATE, curDate);
		    	editor.putInt(UserActiveInfoThread.PREF_KEY_TIMES, spActive.getInt(UserActiveInfoThread.PREF_KEY_TIMES, 0)+1);
		    	editor.commit();		    
		    }
		    startTime = System.currentTimeMillis();
			} catch (Exception e) {
				ScheduleApplication.logException(getClass(), e);
			}
		}
	}

}
