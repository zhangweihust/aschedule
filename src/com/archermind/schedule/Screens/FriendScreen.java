package com.archermind.schedule.Screens;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.archermind.schedule.Utils.AScheduleBroadcast;
import com.archermind.schedule.Utils.Constant;

public class FriendScreen extends Screen implements OnClickListener{
		private ListView friend_listView;
	private ListView friend_contact_listView;
	private Button friend_button_state;
	private Button friend_contact_button_state;
	private AScheduleBroadcast ContactCheckReceiver;
	private AlarmManager AScheduleAM;
	private static final int CONTACT_SYNC_INTERVAL = 60 * 60 * 1000;			/* 1个小时检测一次联系人是否有变化 */
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_screen);
		
		friend_listView = (ListView) findViewById(R.id.friend_listView);
		friend_contact_listView = (ListView) findViewById(R.id.friend_contact_listView);
		
		friend_button_state= (Button) findViewById(R.id.friend_button_state);
		friend_contact_button_state= (Button) findViewById(R.id.friend_contact_button_state);
		friend_button_state.setOnClickListener(this);
		friend_contact_button_state.setOnClickListener(this);
		
		initAdapter();

		PendingIntent contactcheckintent = PendingIntent.getBroadcast(ScheduleApplication.getContext(), 
										0, new Intent(AScheduleBroadcast.ASCHEDULE_CHECK_CONTACT), 1);
		AScheduleAM = (AlarmManager)ScheduleApplication.getContext().getSystemService(ALARM_SERVICE);
		AScheduleAM.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 3000, CONTACT_SYNC_INTERVAL, contactcheckintent);
		ContactCheckReceiver = new AScheduleBroadcast();
		
	 }
	 public void initAdapter(){
//		
		List<Friend> friends = new ArrayList<Friend>();
		for(int i = 0; i < 5; i++){
			Friend f = new Friend(i,"guo:NO"+i,Constant.FriendType.friend_contact);
			friends.add(f);
		}
		FriendAdapter friendAdapter = new FriendAdapter(this, friends);
		friend_listView.setAdapter(friendAdapter);
		
		
		
		
		
	    FriendContactAdapter friendContactAdapter = new FriendContactAdapter(this);

		friendContactAdapter.addTitleHeaderItem(getResources().getString(R.string.contact_use_show));

	    ArrayList<ListElement> elements = new ArrayList<ListElement>();
	    for (int i = 0; i < 10; i++) {
	    	FriendContactAdapter.ContentListElement element = friendContactAdapter.new ContentListElement();
	    	Friend friend = new Friend(i, "zhang:NO"+i,Constant.FriendType.friend_contact);
	    	element.setfriend(friend);
	    	elements.add(element);
	    }
	    friendContactAdapter.addList(elements);

	    friendContactAdapter.addTitleHeaderItem(getResources().getString(R.string.contact_show));

	    elements = new ArrayList<ListElement>();
	    for (int i = 0; i < 10; i++) {
	    	FriendContactAdapter.ContentListElement element = friendContactAdapter.new ContentListElement();
	    	Friend friend = new Friend(i, "peng:NO"+i ,Constant.FriendType.friend_contact);
	    	element.setfriend(friend);
	    	elements.add(element);
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
