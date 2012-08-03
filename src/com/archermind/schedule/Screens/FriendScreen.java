package com.archermind.schedule.Screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Adapters.FriendAdapter;
import com.archermind.schedule.Adapters.FriendContactAdapter;
import com.archermind.schedule.Adapters.FriendContactAdapter.ListElement;
import com.archermind.schedule.Model.Friend;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.AScheduleBroadcast;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.Contact;
import com.archermind.schedule.Utils.ServerInterface;
import com.archermind.schedule.task.FriendTask;

public class FriendScreen extends Screen implements OnClickListener{
	private ListView friend_listView;
	private ListView friend_contact_listView;
	private Button friend_button_state;
	private Button friend_contact_button_state;
	private AScheduleBroadcast ContactCheckReceiver;
	private AlarmManager AScheduleAM;
	private static final int CONTACT_SYNC_INTERVAL = 60 * 60 * 1000;			/* 1个小时检测一次联系人是否有变化 */
	private DatabaseManager database;
	private ServerInterface serverInterface;
	public FriendScreen(){
		database = ServiceManager.getDbManager();
		serverInterface = new ServerInterface();
	}
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_screen);
		
		initView();	
		
		PendingIntent contactcheckintent = PendingIntent.getBroadcast(ScheduleApplication.getContext(), 
										0, new Intent(AScheduleBroadcast.ASCHEDULE_CHECK_CONTACT), 1);
		AScheduleAM = (AlarmManager)ScheduleApplication.getContext().getSystemService(ALARM_SERVICE);
		AScheduleAM.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 3000, CONTACT_SYNC_INTERVAL, contactcheckintent);
		ContactCheckReceiver = new AScheduleBroadcast();
		
		loadData();
		
	 }
	
	private void loadData(){
		new FriendTask(this).execute();
	}
	
	public void initView(){
		friend_listView = (ListView) findViewById(R.id.friend_listView);
		friend_contact_listView = (ListView) findViewById(R.id.friend_contact_listView);
		
		friend_button_state= (Button) findViewById(R.id.friend_button_state);
		friend_contact_button_state= (Button) findViewById(R.id.friend_contact_button_state);
		friend_button_state.setOnClickListener(this);
		friend_contact_button_state.setOnClickListener(this);
	}

	 public void initAdapter(HashMap<String, List<Friend>> result){
		 
		 ServiceManager.getContact().updateAScheduleContact();

		List<Friend> friends = result.get(Constant.FriendType.FRIEND_YES_KEY);
		List<Friend> ignores = result.get(Constant.FriendType.FRIEND_IGNORE_KEY);
		List<Friend> contact_use = result.get(Constant.FriendType.FRIEND_CONTACT_USE_KEY);
		List<Friend> contact = result.get(Constant.FriendType.FRIEND_CONTACT_KEY);
		 
	    friends.addAll(ignores);
		FriendAdapter friendAdapter = new FriendAdapter(this, friends);
		friend_listView.setAdapter(friendAdapter);
		
		
	    FriendContactAdapter friendContactAdapter = new FriendContactAdapter(this);
		friendContactAdapter.addTitleHeaderItem(getResources().getString(R.string.contact_use_show));
	    ArrayList<ListElement> elements = new ArrayList<ListElement>();
    	for(Friend friend : contact_use){
    		FriendContactAdapter.ContentListElement element = friendContactAdapter.new ContentListElement();
	    	element.setfriend(friend);
	    	elements.add(element);
    	}
	    friendContactAdapter.addList(elements);

	    friendContactAdapter.addTitleHeaderItem(getResources().getString(R.string.contact_show));
	    elements = new ArrayList<ListElement>();
    	for(Friend friend : contact){
    		FriendContactAdapter.ContentListElement element2 = friendContactAdapter.new ContentListElement();
    		element2.setfriend(friend);
	    	elements.add(element2);
    	}
	    friendContactAdapter.addList(elements);

	    friend_contact_listView.setAdapter(friendContactAdapter);
	}	  	
	
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		unregisterReceiver(ContactCheckReceiver);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.friend_button_state:
			if(friend_listView.getVisibility() == View.VISIBLE){
				friend_listView.setVisibility(View.GONE);
				friend_button_state.setBackgroundResource(R.drawable.friend_group_shrink);
			}else{
				friend_listView.setVisibility(View.VISIBLE);
				friend_button_state.setBackgroundResource(R.drawable.friend_group_expand);
			}
			break;
		case R.id.friend_contact_button_state:
			if(friend_contact_listView.getVisibility() == View.VISIBLE){
				friend_contact_listView.setVisibility(View.GONE);
				friend_contact_button_state.setBackgroundResource(R.drawable.friend_group_shrink);
			}else{
				friend_contact_listView.setVisibility(View.VISIBLE);
				friend_contact_button_state.setBackgroundResource(R.drawable.friend_group_expand);
			}
			break;
		}
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
