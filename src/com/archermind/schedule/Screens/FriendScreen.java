package com.archermind.schedule.Screens;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Utils.AScheduleBroadcast;

public class FriendScreen extends Screen {
	
	private AScheduleBroadcast ContactCheckReceiver;
	private AlarmManager AScheduleAM;
	private static final int CONTACT_SYNC_INTERVAL = 60 * 60 * 1000;			/* 1个小时检测一次联系人是否有变化 */
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		
		PendingIntent contactcheckintent = PendingIntent.getBroadcast(ScheduleApplication.getContext(), 
										0, new Intent(AScheduleBroadcast.ASCHEDULE_CHECK_CONTACT), 1);
		AScheduleAM = (AlarmManager)ScheduleApplication.getContext().getSystemService(ALARM_SERVICE);
		AScheduleAM.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 3000, CONTACT_SYNC_INTERVAL, contactcheckintent);
		ContactCheckReceiver = new AScheduleBroadcast();
		
	 }
	 
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		unregisterReceiver(ContactCheckReceiver);
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		IntentFilter intentfilter = new IntentFilter(AScheduleBroadcast.ASCHEDULE_CHECK_CONTACT);
		registerReceiver(ContactCheckReceiver, intentfilter);
	}

	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		PendingIntent contactcheckintent = PendingIntent.getBroadcast(ScheduleApplication.getContext(), 
										0, new Intent(AScheduleBroadcast.ASCHEDULE_CHECK_CONTACT), 1);
		AScheduleAM.cancel(contactcheckintent);
		
		super.onDestroy();
	}
	
}