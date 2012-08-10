package com.archermind.schedule.Screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Adapters.FriendAdapter;
import com.archermind.schedule.Adapters.FriendContactAdapter;
import com.archermind.schedule.Adapters.FriendContactAdapter.ListElement;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.IEventHandler;
import com.archermind.schedule.Model.Friend;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.AScheduleBroadcast;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.Contact;
import com.archermind.schedule.Utils.ListViewUtil;
import com.archermind.schedule.Utils.NetworkUtils;
import com.archermind.schedule.Utils.ScheduleData;
import com.archermind.schedule.Utils.ServerInterface;

public class FriendScreen extends Screen implements OnClickListener, IEventHandler{
	private ListView friend_listView;
	private ListView friend_contact_listView;
	private Button friend_button_state;
	private Button friend_contact_button_state;
	private AScheduleBroadcast ContactCheckReceiver;
	private AlarmManager AScheduleAM;
	private static final int CONTACT_SYNC_INTERVAL = 60 * 60 * 100010;			/* 1个小时检测一次联系人是否有变化 */
	private DatabaseManager database;
	private ServerInterface serverInterface;
	private HashMap<String, List<Friend>> hashMap = new HashMap<String, List<Friend>>();
	private RelativeLayout loading;
	private SharedPreferences sp;
	
//	private ProgressDialog dialog;
	public FriendScreen(){
		database = ServiceManager.getDbManager();
		serverInterface = new ServerInterface();
	}
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_screen);
		eventService.add(this);
		initView();	
		
		PendingIntent contactcheckintent = PendingIntent.getBroadcast(ScheduleApplication.getContext(), 
										0, new Intent(AScheduleBroadcast.ASCHEDULE_CHECK_CONTACT), 1);
		AScheduleAM = (AlarmManager)ScheduleApplication.getContext().getSystemService(ALARM_SERVICE);
		AScheduleAM.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 3000, CONTACT_SYNC_INTERVAL, contactcheckintent);
		ContactCheckReceiver = new AScheduleBroadcast();
		
		sp = getSharedPreferences("Data", Context.MODE_PRIVATE);
		boolean sync = sp.getBoolean("sync", false);
		if(sync){
			new Thread(){
				public void run() {
					getData();
				};
			}.start();
		}
		
	 }	
	
	public void initView(){
		friend_listView = (ListView) findViewById(R.id.friend_listView);
		friend_contact_listView = (ListView) findViewById(R.id.friend_contact_listView);
		loading = (RelativeLayout) findViewById(R.id.loading);
//		dialog = new ProgressDialog(this, R.style.rotateProgress);
//		dialog.show();
		friend_button_state= (Button) findViewById(R.id.friend_button_state);
		friend_contact_button_state= (Button) findViewById(R.id.friend_contact_button_state);
		friend_button_state.setOnClickListener(this);
		friend_contact_button_state.setOnClickListener(this);
	}

	 public void initAdapter(){		 

		List<Friend> friends = hashMap.get(Constant.FriendType.FRIEND_YES_KEY);
		List<Friend> ignores = hashMap.get(Constant.FriendType.FRIEND_IGNORE_KEY);
		List<Friend> contact_use = hashMap.get(Constant.FriendType.FRIEND_CONTACT_USE_KEY);
		List<Friend> contact = hashMap.get(Constant.FriendType.FRIEND_CONTACT_KEY);
		 
	    friends.addAll(ignores);
		FriendAdapter friendAdapter = new FriendAdapter(this, friends, friend_listView);
		friend_listView.setAdapter(friendAdapter);
		ListViewUtil.setListViewHeightBasedOnChildren(friend_listView);
		
	    FriendContactAdapter friendContactAdapter = new FriendContactAdapter(this, friend_contact_listView);
		friendContactAdapter.addTitleHeaderItem(getResources().getString(R.string.contact_use_show));
	    ArrayList<ListElement> elements = new ArrayList<ListElement>();
    	for(Friend friend : contact_use){
    		FriendContactAdapter.ContentListElement element = friendContactAdapter.new ContentListElement();
	    	element.setFriend(friend);
	    	elements.add(element);
    	}
	    friendContactAdapter.addList(elements);

	    
	    friendContactAdapter.setFriendContactUseIndex(friendContactAdapter.getCount());
	    friendContactAdapter.addTitleHeaderItem(getResources().getString(R.string.contact_show));
	    elements = new ArrayList<ListElement>();
    	for(Friend friend : contact){
    		FriendContactAdapter.ContentListElement element2 = friendContactAdapter.new ContentListElement();
    		element2.setFriend(friend);
	    	elements.add(element2);
    	}
	    friendContactAdapter.addList(elements);

	    friend_contact_listView.setAdapter(friendContactAdapter);
	    ListViewUtil.setListViewHeightBasedOnChildren(friend_contact_listView);
	    
	    friendAdapter.setOtherAdapter(friendContactAdapter);
	    friendContactAdapter.setOtherAdapter(friendAdapter);
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
		eventService.remove(this);
		super.onDestroy();
	}
	@Override
	public boolean onEvent(Object sender, EventArgs e) {
		switch(e.getType()){
		case CONTACT_SYNC_SUCCESS:
			getData();
			sp = getSharedPreferences("Data", Context.MODE_PRIVATE);
			Editor editor = sp.edit();
			editor.putBoolean("sync", true);
			editor.commit();
			break;
		case CONTACT_SYNC_FAILED:
		}
		return true;
	}
	
	
	
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			loading.setVisibility(View.GONE);
//			dialog.dismiss();
			initAdapter();
		};
	};
	
	   
	   private void makeFriendFromInet(List<Friend> friends,String id, int type, List<String> toalList){
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				
				String jsonString = ServiceManager.getServerInterface().findUserInfobyUserId(id);
				ContentValues values = null;
				if(jsonString != null && !"".equals(jsonString)){
					if(jsonString.indexOf("tel") >= 0){//防止返回错误码
						try {
							JSONArray jsonArray = new JSONArray(jsonString);
							ScheduleApplication.LogD(FriendsDyamicScreen.class, jsonString
									+ jsonArray.length());
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
								String tel = jsonObject.getString("tel");
								String nick = jsonObject.getString("nick");
								String photo_url = jsonObject.getString("photo_url");
								
								Friend friend = new Friend();
								friend.setTelephone(tel);
								friend.setType(type);
								friend.setNick(nick);
								friend.setHeadImagePath(photo_url);
								friends.add(friend);
									 
								 values = new ContentValues();
								 values.put(DatabaseHelper.ASCHEDULE_FRIEND_ID, id);
								 values.put(DatabaseHelper.ASCHEDULE_FRIEND_TYPE, type);
								 values.put(DatabaseHelper.ASCHEDULE_FRIEND_NUM, tel);
								 values.put(DatabaseHelper.ASCHEDULE_FRIEND_NICK, nick);
								 values.put(DatabaseHelper.ASCHEDULE_FRIEND_PHOTO_URL, photo_url);
								 database.addFriend(values);
								 
								 toalList.remove(tel);
								
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			}
		}
	   
	   
	   private void makeFriendContactUseFromInet(List<Friend> friendContactUs,String tel, int type, List<String> tempList,List<String> friendList){
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				
				String jsonString = ServiceManager.getServerInterface().isfriendSchedule(tel);
				ContentValues values = null;
				if(jsonString != null && !"".equals(jsonString)){
					if(jsonString.indexOf("user_id") >= 0){//防止返回错误码
						try {
							JSONArray jsonArray = new JSONArray(jsonString);
							ScheduleApplication.LogD(FriendsDyamicScreen.class, jsonString
									+ jsonArray.length());
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
								String user_id = jsonObject.getString("user_id");
								String nick = jsonObject.getString("nick");
								String photo_url = jsonObject.getString("photo_url");
								
								tempList.add(tel);

								if(friendList.contains(user_id)){
									break;
								}
								Friend friend = new Friend();
								friend.setTelephone(tel);
								friend.setType(type);
								friend.setNick(nick);
								friend.setHeadImagePath(photo_url);
								
								friendContactUs.add(friend);
									 
								database.updateContactType(database.queryContactIdByTel(tel), type,user_id);
								
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			}
		}

	
	protected HashMap<String, List<Friend>> getData() {
		// TODO Auto-generated method stub

		 List<String> contactToalList = new ArrayList<String>();
		 List<String> friendList = new ArrayList<String>();
		 List<String> ignoreList = new ArrayList<String>();
		 List<String> contactList = new ArrayList<String>();	 
		 
		 if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				
				String jsonString = ServiceManager.getServerInterface().getFriendRel("3");
				
				System.out.println("result = "+jsonString);
				if(jsonString != null && !"".equals(jsonString)){
					if(jsonString.indexOf("user_id") >= 0){//防止返回错误码
						
						try {
							JSONArray jsonArray = new JSONArray(jsonString);
							ScheduleApplication.LogD(FriendsDyamicScreen.class, jsonString
									+ jsonArray.length());
								JSONObject jsonObject = (JSONObject) jsonArray.opt(0);
								String user_id = jsonObject.getString("user_id");
								String contact_list = jsonObject.getString("contact_list");
								String friends_list = jsonObject.getString("friends_list");
								String shield_list = jsonObject.getString("shield_list");
								
								
								String[] contacts = contact_list.split(",");
								for(int i = 0; i < contacts.length; i++){
									contactToalList.add(contacts[i]);
								}
								
								String[] friends = friends_list.split(",");
								for(int i = 0; i < friends.length; i++){
									friendList.add(friends[i]);
								}
								
								String[] shields = shield_list.split(",");
								for(int i = 0; i < shields.length; i++){
									ignoreList.add(shields[i]);
								}
								
								
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}																		
						
					}
				}
				
		 }

		 List<Friend> friends = new ArrayList<Friend>();
		 Cursor cursor = null;
		 for(String id : friendList){
			 if(id.matches("[0-9]+")){
				 cursor = database.queryFriendTel(Integer.parseInt(id));
				 if(cursor.moveToNext()){
					 String telephone = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_NUM));
					 contactToalList.remove(telephone);
					 Friend friend = new Friend(id,telephone,Constant.FriendType.friend_yes);
					 friends.add(friend);
				 }else{
					 //向服务器查询数据(构造Friend,向本地数据库插入数据)
					 makeFriendFromInet(friends, id, Constant.FriendType.friend_yes, contactToalList);
				 }
			 }
			 
		 }
		 List<Friend> ignores = new ArrayList<Friend>();
		 for(String id : ignoreList){
			 if(id.matches("[0-9]+")){
				 cursor = database.queryFriendTel(Integer.parseInt(id));
				 if(cursor.moveToNext()){
					 String telephone = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_NUM));
					 contactToalList.remove(telephone);
					 Friend friend = new Friend(id,telephone,Constant.FriendType.friend_Ignore);
					 ignores.add(friend);
				 }else{
					//向服务器查询数据(构造Friend,向本地数据库插入数据)
					 makeFriendFromInet(ignores, id, Constant.FriendType.friend_Ignore, contactToalList);
				 }
			 }

		 }

		 
		 
		 List<Friend> contact_use = new ArrayList<Friend>();
		 cursor = database.queryContactUse();
		 if(cursor.getCount() > 0){
			 while(cursor.moveToNext()){
				 String id = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACT_ID));
				 String tel = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ASCHEDULE_CONTACT_NUM));
				 String imgPath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACT_ID));
				 Friend friend = new Friend();	
				 friend.setId(id);
				 friend.setHeadImagePath(imgPath);
				 friend.setTelephone(tel);
				 friend.setType(Constant.FriendType.friend_contact_use);
				 contact_use.add(friend);
				 contactToalList.remove(tel);
			 }
		 }else{
			 List<String> tempList = new ArrayList<String>();
			 for(String tel : contactToalList){
				 makeFriendContactUseFromInet(contact_use, tel, Constant.FriendType.friend_contact_use, tempList, friendList);
			 }
			 for(String tel : tempList){
				 contactToalList.remove(tel);
			 }
		 }		 		 		 
			 
		 List<Friend> contact = new ArrayList<Friend>();
		 for(String tel : contactToalList){
			 cursor = database.queryContactIdByTel(tel);
			 if(cursor.moveToNext()){
				 String id = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACT_ID));
				 String imgPath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACT_ID));
				 Friend friend = new Friend();	
				 friend.setId(id);
				 friend.setHeadImagePath(imgPath);
				 friend.setTelephone(tel);
				 friend.setType(Constant.FriendType.friend_contact);
				 contact.add(friend);
			 }
		 }
		 
		 if(cursor != null){
			 cursor.close();
		 }
		 
		 hashMap.put(Constant.FriendType.FRIEND_YES_KEY, friends);
		 hashMap.put(Constant.FriendType.FRIEND_IGNORE_KEY, ignores);
		 hashMap.put(Constant.FriendType.FRIEND_CONTACT_USE_KEY, contact_use);
		 hashMap.put(Constant.FriendType.FRIEND_CONTACT_KEY, contact);
		 
		 handler.sendEmptyMessage(0);
		return hashMap;
	}
	
	
}
