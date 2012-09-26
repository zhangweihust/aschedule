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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Adapters.FriendAdapter;
import com.archermind.schedule.Adapters.FriendContactAdapter;
import com.archermind.schedule.Adapters.FriendContactAdapter.ListElement;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.IEventHandler;
import com.archermind.schedule.Model.Friend;
import com.archermind.schedule.Model.ScheduleData;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.AScheduleBroadcast;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.Contact;
import com.archermind.schedule.Utils.ListViewUtil;
import com.archermind.schedule.Utils.NetworkUtils;
import com.archermind.schedule.Utils.ServerInterface;

public class FriendScreen extends Screen
		implements
			OnClickListener,
			IEventHandler {

	private ListView friend_listView;
	private ListView friend_contact_listView;
	private Button friend_button_state;
	private Button friend_contact_button_state;
	private AScheduleBroadcast ContactCheckReceiver;
	private AlarmManager AScheduleAM;

	private static final int CONTACT_SYNC_INTERVAL = 60 * 60 * 1000; // 1个小时检测一次联系人是否有变化
	private static final int CONTACT_SYNC_SUCCESS = 0x101;
	private static final int CONTACT_SYNC_ERROR = 0x102;
	private static final int CONTACT_SYNC_CANCEL = 0x103;

	private DatabaseManager database;
	private ServerInterface serverInterface;
	private HashMap<String, List<Friend>> hashMap = new HashMap<String, List<Friend>>();
	private RelativeLayout loading;
	private SharedPreferences sp;
	private String friendId = null;

	private LinearLayout myFriendLayout;
	private LinearLayout myContactFriendLayout;
	private LinearLayout bindLayout;
	private LinearLayout loginLayout;
	private Button loginFriendBtn;
	private Button bindFriendBtn;
	
	public FriendScreen() {
		database = ServiceManager.getDbManager();
		serverInterface = new ServerInterface();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_screen);
		eventService.add(this);
		initView();

		PendingIntent contactcheckintent = PendingIntent.getBroadcast(
				ScheduleApplication.getContext(), 0, new Intent(
						AScheduleBroadcast.ASCHEDULE_CHECK_CONTACT), 1);
		AScheduleAM = (AlarmManager) ScheduleApplication.getContext()
				.getSystemService(ALARM_SERVICE);
		AScheduleAM.setRepeating(AlarmManager.RTC,
				System.currentTimeMillis() + 3000, CONTACT_SYNC_INTERVAL,
				contactcheckintent);
		ContactCheckReceiver = new AScheduleBroadcast();
		IntentFilter intentfilter = new IntentFilter(
				AScheduleBroadcast.ASCHEDULE_CHECK_CONTACT);
		registerReceiver(ContactCheckReceiver, intentfilter);
		sp = getSharedPreferences("Data", Context.MODE_PRIVATE);

		loginFriendBtn.setOnClickListener(this);
		bindFriendBtn.setOnClickListener(this);
		showData();
	}

	public void initView() {
		myFriendLayout = (LinearLayout) findViewById(R.id.myfriendlayout);
		myContactFriendLayout = (LinearLayout) findViewById(R.id.mycontactfriendlayout);

		bindLayout = (LinearLayout) findViewById(R.id.bindTelfriend);
		loginLayout = (LinearLayout) findViewById(R.id.loginUpfriend);

		friend_listView = (ListView) findViewById(R.id.friend_listView);
		friend_contact_listView = (ListView) findViewById(R.id.friend_contact_listView);
		loading = (RelativeLayout) findViewById(R.id.loading);
		friend_button_state = (Button) findViewById(R.id.friend_button_state);
		friend_contact_button_state = (Button) findViewById(R.id.friend_contact_button_state);
		friend_button_state.setOnClickListener(this);
		friend_contact_button_state.setOnClickListener(this);
		myFriendLayout.setOnClickListener(this);
		myContactFriendLayout.setOnClickListener(this);

		loginFriendBtn = (Button) findViewById(R.id.loginfriend);
		bindFriendBtn = (Button) findViewById(R.id.bindfriend);
	}

	public void initAdapter() {

		List<Friend> friends = hashMap.get(Constant.FriendType.FRIEND_YES_KEY);
		List<Friend> ignores = hashMap
				.get(Constant.FriendType.FRIEND_IGNORE_KEY);
		List<Friend> contact_use = hashMap
				.get(Constant.FriendType.FRIEND_CONTACT_USE_KEY);
		List<Friend> contact = hashMap
				.get(Constant.FriendType.FRIEND_CONTACT_KEY);

		Friend addFriend = null;
		if (friendId != null && !"".equals(friendId)) {
			Cursor cursor = database.queryFriend(Integer.parseInt(friendId));
			if (cursor.moveToNext()) {
				String telephone = cursor.getString(cursor
						.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_NUM));
				String name = cursor.getString(cursor
						.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_NAME));
				String nick = cursor.getString(cursor
						.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_NICK));
				String headImagePath = cursor
						.getString(cursor
								.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_PHOTO_URL));
				addFriend = new Friend();
				addFriend.setId(friendId);
				addFriend.setTelephone(telephone);
				addFriend.setName(name);
				addFriend.setNick(nick);
				addFriend.setHeadImagePath(headImagePath);
				addFriend.setType(Constant.FriendType.friend_yes);
				friends.add(addFriend);
				contact_use.remove(addFriend);
			}
			cursor.close();
		}

		friends.addAll(ignores);
		FriendAdapter friendAdapter = new FriendAdapter(this, friends,
				friend_listView);
		friend_listView.setAdapter(friendAdapter);
		ListViewUtil.setListViewHeightBasedOnChildren(friend_listView);

		FriendContactAdapter friendContactAdapter = new FriendContactAdapter(
				this, friend_contact_listView);
		friendContactAdapter.addTitleHeaderItem(getResources().getString(
				R.string.contact_use_show));
		ArrayList<ListElement> elements = new ArrayList<ListElement>();
		for (Friend friend : contact_use) {
			FriendContactAdapter.ContentListElement element = friendContactAdapter.new ContentListElement();
			element.setFriend(friend);
			elements.add(element);
		}
		friendContactAdapter.addList(elements);

		friendContactAdapter.setFriendContactUseIndex(friendContactAdapter
				.getCount());
		friendContactAdapter.addTitleHeaderItem(getResources().getString(
				R.string.contact_show));
		elements = new ArrayList<ListElement>();
		for (Friend friend : contact) {
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
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.myfriendlayout :
			case R.id.friend_button_state :
				if (friend_listView.getVisibility() == View.VISIBLE) {
					friend_listView.setVisibility(View.GONE);
					friend_button_state
							.setBackgroundResource(R.drawable.friend_group_shrink);
				} else {
					friend_listView.setVisibility(View.VISIBLE);
					friend_button_state
							.setBackgroundResource(R.drawable.friend_group_expand);
				}
				break;
			case R.id.mycontactfriendlayout :
			case R.id.friend_contact_button_state :
				if (friend_contact_listView.getVisibility() == View.VISIBLE) {
					friend_contact_listView.setVisibility(View.GONE);
					friend_contact_button_state
							.setBackgroundResource(R.drawable.friend_group_shrink);
				} else {
					friend_contact_listView.setVisibility(View.VISIBLE);
					friend_contact_button_state
							.setBackgroundResource(R.drawable.friend_group_expand);
				}
				break;

			case R.id.bindfriend :
				Intent it = new Intent(FriendScreen.this,
						TelephoneBindScreen.class);
				startActivity(it);
				break;

			case R.id.loginfriend :
				Intent itlogin = new Intent(FriendScreen.this,
						LoginScreen.class);
				startActivity(itlogin);
				break;

		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (ServiceManager.getUserId() == 0) {// 用户没有登录或已退出
			bindLayout.setVisibility(View.GONE);
			loginLayout.setVisibility(View.VISIBLE);
		} else {
		
			if (ServiceManager.getBindFlag()) {
				loginLayout.setVisibility(View.GONE);
				bindLayout.setVisibility(View.GONE);
				
				showData();
				
			} else {
				loginLayout.setVisibility(View.GONE);
				bindLayout.setVisibility(View.VISIBLE);
			}
		}
	}

	private void showData() {

		boolean sync = sp.getBoolean("sync", false);
		if (sync) {
			new Thread() {
				public void run() {
					getData();
				};
			}.start();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		PendingIntent contactcheckintent = PendingIntent.getBroadcast(
				ScheduleApplication.getContext(), 0, new Intent(
						AScheduleBroadcast.ASCHEDULE_CHECK_CONTACT), 1);
		AScheduleAM.cancel(contactcheckintent);
		eventService.remove(this);
		unregisterReceiver(ContactCheckReceiver);
		super.onDestroy();
	}

	@Override
	public boolean onEvent(Object sender, EventArgs e) {
		friendId = (String) e.getExtra("friend_id");
		switch (e.getType()) {
			case CONTACT_SYNC_SUCCESS :
				getData();
				sp = getSharedPreferences("Data", Context.MODE_PRIVATE);
				Editor editor = sp.edit();
				editor.putBoolean("sync", true);
				editor.commit();
				break;
			case CONTACT_SYNC_FAILED :
				handler.sendEmptyMessage(CONTACT_SYNC_ERROR);
				break;
			case CONTACT_SYNC_CANCEL :
				handler.sendEmptyMessage(CONTACT_SYNC_CANCEL);
				break;
			case ADD_FRIEND :
				FriendScreen.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						initAdapter();
					}
				});
				break;
			case LOGIN_SUCCESS :
				this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (ServiceManager.getBindFlag()) {
							ServiceManager.getContact().checkSync(
									FriendScreen.this);
						}
					}
				});
				break;
			case TELEPHONE_BIND_SUCCESS :
				ServiceManager.getContact().checkSync(FriendScreen.this);
				break;
		}
		return true;
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case CONTACT_SYNC_SUCCESS :
					initAdapter();
					break;
				case CONTACT_SYNC_ERROR :
					Toast.makeText(FriendScreen.this, "同步失败！",
							Toast.LENGTH_SHORT).show();
					break;
				case CONTACT_SYNC_CANCEL :
					break;
			}
			loading.setVisibility(View.GONE);
		};
	};

	private boolean makeFriendContactUseFromInet(List<Friend> friendContactUs,
			List<Friend> friends, List<Friend> ignores, List<String> tempList,
			List<String> friendList, List<String> ignoreList) {
		if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {

			String jsonString = ServiceManager.getServerInterface()
					.checkUserSchedule(
							String.valueOf(ServiceManager.getUserId()));
			ScheduleApplication.LogD(getClass(), "获取我的好友信息：" + jsonString);
			ContentValues values = null;
			if (jsonString != null && !"".equals(jsonString)) {
				if (jsonString.indexOf("user_id") >= 0) {// 防止返回错误码
					try {
						String[] stringArray = jsonString.split("####");
						for (int i = 0; i < stringArray.length; i++) {
							JSONArray jsonArray = new JSONArray(stringArray[i]);
							JSONObject jsonObject = (JSONObject) jsonArray
									.opt(0);
							String user_id = jsonObject.getString("user_id");
							String nick = jsonObject.getString("nick");
							String photo_url = jsonObject
									.getString("photo_url");
							String tel = jsonObject.getString("tel");

							tempList.add(tel);

							if (user_id.equals(ServiceManager.getUserId())) {// 屏蔽自己
								continue;
							}
							Friend friend = new Friend();
							if (friendList.contains(user_id)) {// 更新好友信息
								ScheduleApplication.LogD(getClass(), "我的好友:"
										+ nick);
								friend.setId(user_id);
								friend.setTelephone(tel);
								friend.setType(Constant.FriendType.friend_yes);
								friend.setNick(nick);
								friend.setHeadImagePath(photo_url);
								friend.setName(database.queryNameByTel(tel));
								friends.add(friend);

								Cursor cursor = database.queryFriend(Integer
										.parseInt(user_id));
								values = new ContentValues();
								values.put(
										DatabaseHelper.ASCHEDULE_FRIEND_TYPE,
										Constant.FriendType.friend_yes);
								values.put(DatabaseHelper.ASCHEDULE_FRIEND_NUM,
										tel);
								values.put(
										DatabaseHelper.ASCHEDULE_FRIEND_NICK,
										nick);
								values.put(
										DatabaseHelper.ASCHEDULE_FRIEND_PHOTO_URL,
										photo_url);
								if (cursor.moveToNext()) {
									database.updateFriend(user_id, values);
								} else {
									values.put(
											DatabaseHelper.ASCHEDULE_FRIEND_ID,
											user_id);
									database.addFriend(values);
								}
								cursor.close();
								continue;
							}

							if (ignoreList.contains(user_id)) {// 更新好友屏蔽信息
								ScheduleApplication.LogD(getClass(), "屏蔽的好友:"
										+ nick);
								friend.setId(user_id);
								friend.setTelephone(tel);
								friend.setType(Constant.FriendType.friend_Ignore);
								friend.setNick(nick);
								friend.setHeadImagePath(photo_url);
								ignores.add(friend);

								Cursor cursor = database.queryFriend(Integer
										.parseInt(user_id));
								values = new ContentValues();
								values.put(
										DatabaseHelper.ASCHEDULE_FRIEND_TYPE,
										Constant.FriendType.friend_Ignore);
								values.put(DatabaseHelper.ASCHEDULE_FRIEND_NUM,
										tel);
								values.put(
										DatabaseHelper.ASCHEDULE_FRIEND_NICK,
										nick);
								values.put(
										DatabaseHelper.ASCHEDULE_FRIEND_PHOTO_URL,
										photo_url);
								if (cursor.moveToNext()) {
									database.updateFriend(user_id, values);
								} else {
									values.put(
											DatabaseHelper.ASCHEDULE_FRIEND_ID,
											user_id);
									database.addFriend(values);
								}
								cursor.close();
								continue;

							}
							ScheduleApplication.LogD(getClass(), "正在使用微日程的:"
									+ nick);
							friend.setId(user_id);
							friend.setTelephone(tel);
							friend.setType(Constant.FriendType.friend_contact_use);
							friend.setNick(nick);
							friend.setHeadImagePath(photo_url);
							friend.setName(database.queryNameByTel(tel));

							friendContactUs.add(friend);
							database.updateContactType(
									database.queryContactIdByTel(tel),
									Constant.FriendType.friend_contact_use,
									user_id);
						}
						return true;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}
		return false;
	}

	protected HashMap<String, List<Friend>> getData() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				loading.setVisibility(View.VISIBLE);
			}
		});
		List<String> contactToalList = new ArrayList<String>();
		List<String> friendList = new ArrayList<String>();
		List<String> ignoreList = new ArrayList<String>();
//		List<String> contactList = new ArrayList<String>();
		List<Friend> friends = new ArrayList<Friend>();
		List<Friend> ignores = new ArrayList<Friend>();
		List<Friend> contact_use = new ArrayList<Friend>();
		List<Friend> contact = new ArrayList<Friend>();
       boolean getFriendsOK = false;
		if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
			//查询好友关系
			String jsonString = ServiceManager.getServerInterface()
					.getFriendRel(String.valueOf(ServiceManager.getUserId()));
			ScheduleApplication.LogD(getClass(), "好友界面获取服务器返回的联系人信息："
					+ jsonString);
			if (jsonString != null && !"".equals(jsonString)) {
				if (jsonString.indexOf("user_id") >= 0) {// 防止返回错误码
					try {
						JSONArray jsonArray = new JSONArray(jsonString);
						ScheduleApplication.LogD(FriendsDyamicScreen.class,
								jsonString + jsonArray.length());
						JSONObject jsonObject = (JSONObject) jsonArray.opt(0);
						String user_id = jsonObject.getString("user_id");
						String contact_list = jsonObject
								.getString("contact_list");
						String friends_list = jsonObject
								.getString("friends_list");
						String shield_list = jsonObject
								.getString("shield_list");

						String[] contacts = contact_list.split(",");
						for (int i = 0; i < contacts.length; i++) {
							contactToalList.add(contacts[i]);
						}

						String[] friendes = friends_list.split(",");
						for (int i = 0; i < friendes.length; i++) {
							friendList.add(friendes[i]);
						}

						String[] shields = shield_list.split(",");
						for (int i = 0; i < shields.length; i++) {
							ignoreList.add(shields[i]);
						}
						List<String> tempList = new ArrayList<String>();
						//查询好友信息
						getFriendsOK = makeFriendContactUseFromInet(contact_use, friends, ignores,
								tempList, friendList, ignoreList);
						for (String tel : tempList) {
							contactToalList.remove(tel);
						}
						Cursor cursor = null;
						for (String tel : contactToalList) {
							cursor = database.queryContactIdByTel(tel);
							if (cursor != null) {
								if (cursor.moveToNext()) {
									String id = cursor
											.getString(cursor
													.getColumnIndex(DatabaseHelper.COLUMN_CONTACT_ID));
									String telephone = cursor
											.getString(cursor
													.getColumnIndex(DatabaseHelper.ASCHEDULE_CONTACT_NUM));
									String name = cursor
											.getString(cursor
													.getColumnIndex(DatabaseHelper.ASCHEDULE_CONTACT_NAME));
									String headImagePath = cursor
											.getString(cursor
													.getColumnIndex(DatabaseHelper.ASCHEDULE_CONTACT_IMGPATH));
									Friend friend = new Friend();
									friend.setId(id);
									friend.setTelephone(telephone);
									friend.setName(name);
									friend.setHeadImagePath(headImagePath);
									friend.setType(Constant.FriendType.friend_contact);
									contact.add(friend);
								}
								cursor.close();
							}
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}

		} 
		if(!getFriendsOK){
			Cursor cursor = database.queryFriendYes();
			if(cursor!=null && cursor.moveToFirst()){
				while (!cursor.isAfterLast()) {
					String id = cursor.getString(cursor
							.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_ID));
					String telephone = cursor.getString(cursor
							.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_NUM));
					String name = cursor.getString(cursor
							.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_NAME));
					String headImagePath = cursor
							.getString(cursor
									.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_PHOTO_URL));
					Friend friend = new Friend();
					friend.setId(id);
					friend.setTelephone(telephone);
					friend.setName(name);
					friend.setHeadImagePath(headImagePath);
					friend.setType(Constant.FriendType.friend_yes);
					
					friends.add(friend);
					cursor.moveToNext();
				}
			}
			if (cursor!=null) {
				cursor.close();
			}
			
			cursor = database.queryFriendIgnore();
			if(cursor!=null && cursor.moveToFirst()){
				while (!cursor.isAfterLast()) {
					String id = cursor.getString(cursor
							.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_ID));
					String telephone = cursor.getString(cursor
							.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_NUM));
					String name = cursor.getString(cursor
							.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_NAME));
					String headImagePath = cursor
							.getString(cursor
									.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_PHOTO_URL));
					Friend friend = new Friend();
					friend.setId(id);
					friend.setTelephone(telephone);
					friend.setName(name);
					friend.setHeadImagePath(headImagePath);
					friend.setType(Constant.FriendType.friend_Ignore);
					
					ignores.add(friend);
					cursor.moveToNext();
				}
			}
			if (cursor!=null) {
				cursor.close();
			}

			cursor = database.queryContactUse();
			if(cursor!=null && cursor.moveToFirst()){
				while (!cursor.isAfterLast()) {
					String id = cursor.getString(cursor
							.getColumnIndex(DatabaseHelper.COLUMN_CONTACT_ID));
					String telephone = cursor.getString(cursor
							.getColumnIndex(DatabaseHelper.ASCHEDULE_CONTACT_NUM));
					String name = cursor.getString(cursor
							.getColumnIndex(DatabaseHelper.ASCHEDULE_CONTACT_NAME));
					String headImagePath = cursor
							.getString(cursor
									.getColumnIndex(DatabaseHelper.ASCHEDULE_CONTACT_IMGPATH));
					Friend friend = new Friend();
					friend.setId(id);
					friend.setTelephone(telephone);
					friend.setName(name);
					friend.setHeadImagePath(headImagePath);
					friend.setType(Constant.FriendType.friend_contact_use);
					
					contact_use.add(friend);
					cursor.moveToNext();
				}
			}
			if (cursor!=null) {
				cursor.close();
			}
			
			cursor = database.queryContact();
			if(cursor!=null && cursor.moveToFirst()){
				while (!cursor.isAfterLast()) {
					String id = cursor.getString(cursor
							.getColumnIndex(DatabaseHelper.COLUMN_CONTACT_ID));
					String telephone = cursor.getString(cursor
							.getColumnIndex(DatabaseHelper.ASCHEDULE_CONTACT_NUM));
					String name = cursor.getString(cursor
							.getColumnIndex(DatabaseHelper.ASCHEDULE_CONTACT_NAME));
					String headImagePath = cursor
							.getString(cursor
									.getColumnIndex(DatabaseHelper.ASCHEDULE_CONTACT_IMGPATH));
					Friend friend = new Friend();
					friend.setId(id);
					friend.setTelephone(telephone);
					friend.setName(name);
					friend.setHeadImagePath(headImagePath);
					friend.setType(Constant.FriendType.friend_contact);
					
					contact.add(friend);
					cursor.moveToNext();
				}
			}
			if (cursor!=null) {
				cursor.close();
			}
		}

		hashMap.put(Constant.FriendType.FRIEND_YES_KEY, friends);
		hashMap.put(Constant.FriendType.FRIEND_IGNORE_KEY, ignores);
		hashMap.put(Constant.FriendType.FRIEND_CONTACT_USE_KEY, contact_use);
		hashMap.put(Constant.FriendType.FRIEND_CONTACT_KEY, contact);
		handler.sendEmptyMessage(CONTACT_SYNC_SUCCESS);
		return hashMap;
	}

}
