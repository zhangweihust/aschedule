package com.archermind.schedule.Screens;

import android.app.Activity;

import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Services.EventService;
import com.archermind.schedule.Services.ServiceManager;

public class Screen extends Activity {
	protected DatabaseManager database;
	protected EventService eventService;

	public Screen() {
		database = ServiceManager.getDbManager();
		eventService = ServiceManager.getEventservice();
	}

}
