package com.archermind.schedule.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Utils.Contact;

public class ServiceManager extends Service {

	private static final EventService eventService = new EventService();
	private static boolean started;
	private static DatabaseManager dbManager = new DatabaseManager(ScheduleApplication.getContext());
	private static Contact contact = new Contact();
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	public static boolean start() {
		if(ServiceManager.started){
			return true;
		}
		// start Android service
		ScheduleApplication.getContext().startService(
				new Intent(ScheduleApplication.getContext(), ServiceManager.class));
		
		
		boolean success = true;

		success &= eventService.start();
		dbManager.open();
		if(!success){
			ScheduleApplication.LogD(ServiceManager.class, "Failed to start services");
			return false;
		}
		
		ServiceManager.started = true;
		
		return true;
	}
	
	public static boolean stop() {
		if(!ServiceManager.started){
			return true;
		}
		
		// stops Android service
		ScheduleApplication.getContext().stopService(
				new Intent(ScheduleApplication.getContext(), ServiceManager.class));
		
		boolean success = true;

		success &= eventService.stop();
		dbManager.close();
		if(!success){
			ScheduleApplication.LogD(ServiceManager.class, "Failed to stop services");
		}
		ServiceManager.started = false;
		return success;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		exit();
		super.onDestroy();
	}
	
	public static EventService getEventservice() {
		return eventService;
	}
	
	public static DatabaseManager getDbManager() {
		return dbManager;
	}

	public static Contact getContact()
	{
		return contact;
	}
	
	public static boolean isStarted() {
		return started;
	}

	
	public static void exit() {
		stop();
		//mainActivity.finish();
		System.exit(0);
	}
}