package com.android.schedule.Screens;

import android.app.Activity;

import com.android.schedule.Provider.DatabaseManager;
import com.android.schedule.Services.EventService;
import com.android.schedule.Services.ServiceManager;

public class Screen extends Activity {
	protected DatabaseManager database;
	protected EventService eventService;
	
	public Screen() {
		database = ServiceManager.getDbManager();
		eventService = ServiceManager.getEventservice();
	}
}
