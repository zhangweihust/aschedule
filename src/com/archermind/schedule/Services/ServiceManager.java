package com.archermind.schedule.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.widget.Toast;

import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Screens.RegisterScreen;
import com.archermind.schedule.Utils.Contact;
import com.archermind.schedule.Utils.ServerInterface;

public class ServiceManager extends Service {

	private static final EventService eventService = new EventService();
	private static boolean started;
	private static DatabaseManager dbManager = new DatabaseManager(ScheduleApplication.getContext());
	private static  ServerInterface serverInerface = new ServerInterface();
    private static Contact contact = new Contact();
    private static int user_id = 0;
    private static Toast toast;
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
		SharedPreferences sp = getSharedPreferences(RegisterScreen.USER_INFO, Context.MODE_WORLD_READABLE);
		user_id = sp.getInt(RegisterScreen.USER_ID, 0);
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
	
	public static  ServerInterface getServerInterface(){
		return serverInerface;
	}

	public static void setUserId(int userid)
	{
		user_id = userid;
	}
	
	public static int getUserId()
	{
		return user_id;
	}
	
	public static void ToastShow(String message)
	{
		if (toast != null)
		{
			toast.cancel();
		}
		toast.setText(message);
		toast.show();
	}
	
	public static void exit() {
		stop();
		//mainActivity.finish();
		System.exit(0);
	}

}
