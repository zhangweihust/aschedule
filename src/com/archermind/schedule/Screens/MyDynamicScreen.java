package com.archermind.schedule.Screens;

import android.database.Cursor;
import android.os.Bundle;

import com.archermind.schedule.R;
import com.archermind.schedule.Adapters.DynamicScheduleAdapter;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Views.XListView;

public class MyDynamicScreen extends Screen {
	private Cursor c;
	private XListView list;
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.friends_dynamic_screen);
	        list = (XListView) findViewById(R.id.list);
	        c = ServiceManager.getDbManager().queryMyShareSchedules();
//	        list.setAdapter(new DynamicScheduleAdapter(
//	        		MyDynamicScreen.this, c));
	 }
}
