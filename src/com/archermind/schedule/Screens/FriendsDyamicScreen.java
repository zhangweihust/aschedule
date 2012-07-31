package com.archermind.schedule.Screens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Adapters.DynamicScheduleAdapter;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Utils.NetworkUtils;
import com.archermind.schedule.Views.XListView;
import com.archermind.schedule.Views.XListView.IXListViewListener;

public class FriendsDyamicScreen extends Screen implements 
		IXListViewListener {
	private XListView list;
	private RelativeLayout mListFooter;
	private Button loginBtn, registerBtn;
	private RelativeLayout loading;
	private Cursor c;
	protected static final int ON_Refresh = 0x101;
	protected static final int ON_LoadMore = 0x102;
	private int end = 2;
	private int start = 0;

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case ON_Refresh:
				loading.setVisibility(View.GONE);
				list.setAdapter(new DynamicScheduleAdapter(
						FriendsDyamicScreen.this, c));
				break;
			case ON_LoadMore:
				loading.setVisibility(View.GONE);
				list.setAdapter(new DynamicScheduleAdapter(
						FriendsDyamicScreen.this, c));
				list.setSelection(start);
				break;
			}

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_dynamic_screen);
		list = (XListView) findViewById(R.id.list);
		list.setPullLoadEnable(true);
		list.setXListViewListener(this);
		loading = (RelativeLayout) findViewById(R.id.loading);
		mListFooter = (RelativeLayout) LayoutInflater.from(this).inflate(
				R.layout.dynamic_listview_footer, null);
		loginBtn = (Button) mListFooter.findViewById(R.id.login);
		registerBtn = (Button) mListFooter.findViewById(R.id.register);
		loginBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(FriendsDyamicScreen.this, "login",
						Toast.LENGTH_SHORT).show();
			}
		});
		registerBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(FriendsDyamicScreen.this, "register",
						Toast.LENGTH_SHORT).show();
			}
		});
		new Thread(new Runnable() {
			@Override
			public void run() {
				getSchedulesFromWeb();
			}
		}).start();

	}


	private void onLoad() {
		list.stopRefresh();
		list.stopLoadMore();
		list.setRefreshTime(DateTimeUtils.time2String("yyyy-MM-dd hh:mm:ss",
				System.currentTimeMillis()));
	}

	@Override
	public void onRefresh() {
		Toast.makeText(FriendsDyamicScreen.this, "onRefresh",
				Toast.LENGTH_SHORT).show();
		getSchedulesFromWeb();
		onLoad();
	}

	@Override
	public void onLoadMore() {
		Toast.makeText(FriendsDyamicScreen.this, "onLoadMore",
				Toast.LENGTH_SHORT).show();
		end = end + 2;
		start = start + 2;
		c = ServiceManager.getDbManager().queryShareSchedules(end);
		mHandler.sendEmptyMessage(ON_LoadMore);
		onLoad();
	}

	private void getSchedulesFromWeb() {
		if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
			String jsonString = ServiceManager.getServerInterface()
					.syncFriendShare("3", "1343203369");
			try {
				JSONArray jsonArray = new JSONArray(jsonString);
				ScheduleApplication.LogD(FriendsDyamicScreen.class, jsonString
						+ jsonArray.length());
				ContentValues contentvalues;
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
					contentvalues = new ContentValues();
					String t_id = jsonObject.getString("TID");
					contentvalues
							.put(DatabaseHelper.COLUMN_SCHEDULE_T_ID, t_id);
					contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER,
							jsonObject.getString("num"));
					contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID,
							jsonObject.getString("host"));
					contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_USER_ID,
							jsonObject.getString("user_id"));
					contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE,
							jsonObject.getString("type"));
					contentvalues.put(
							DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
							jsonObject.getString("start_time"));
					contentvalues.put(
							DatabaseHelper.COLUMN_SCHEDULE_UPDATE_TIME,
							jsonObject.getString("update_time"));
					contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CITY,
							jsonObject.getString("city"));
					contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT,
							jsonObject.getString("content"));
					if (!ServiceManager.getDbManager().isInShareSchedules(t_id)) {
						ServiceManager.getDbManager().insertShareSchedules(
								contentvalues);
					} else {
						ServiceManager.getDbManager().updateShareSchedules(
								contentvalues, t_id);
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		c = ServiceManager.getDbManager().queryShareSchedules(end);
		if (c.getCount() == 0) {
			list.addFooterView(mListFooter);
			list.setHeaderGone(false);
			insertDefaultSchedules();
			c.requery();
		}
		mHandler.sendEmptyMessage(ON_Refresh);
	}

	private void insertDefaultSchedules() {
		ContentValues contentvalues = new ContentValues();
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_T_ID, 1);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID, -1);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER, 0);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE, 1);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
				System.currentTimeMillis());
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CITY, "武汉");
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT,
				"hi all 让我们一起微日程吧");
		ServiceManager.getDbManager().insertShareSchedules(contentvalues);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_T_ID, 2);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID, -1);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER, 0);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE, 2);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
				System.currentTimeMillis());
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CITY, "北京");
		contentvalues
				.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT, "这周同学聚会大家要来啊");
		ServiceManager.getDbManager().insertShareSchedules(contentvalues);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_T_ID, 3);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID, 2);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER, 1);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE, 3);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
				System.currentTimeMillis());
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CITY, "北京");
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT, "班长组织一定要捧场啊");
		ServiceManager.getDbManager().insertShareSchedules(contentvalues);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_T_ID, 4);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID, 1);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER, 1);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE, 2);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
				System.currentTimeMillis());
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CITY, "北京");
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT, "恩，大家聚会方便多了");
		ServiceManager.getDbManager().insertShareSchedules(contentvalues);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_T_ID, 5);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID, 1);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER, 2);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE, 2);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
				System.currentTimeMillis());
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CITY, "北京");
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT, "哈哈，这东东蛮好用的");
		ServiceManager.getDbManager().insertShareSchedules(contentvalues);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_T_ID, 6);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID, -1);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER, 0);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE, 3);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
				System.currentTimeMillis());
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CITY, "北京");
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT,
				"想吃火锅~各种流口水啊，有木有想吃的同去");
		ServiceManager.getDbManager().insertShareSchedules(contentvalues);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_T_ID, 7);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID, 6);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER, 1);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE, 3);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
				System.currentTimeMillis());
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CITY, "北京");
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT, "大馋猫，下班一起去咯");
		ServiceManager.getDbManager().insertShareSchedules(contentvalues);
	}

	String inputStream2String(InputStream is) {
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer = new StringBuffer();
		String line = "";
		try {
			while ((line = in.readLine()) != null) {
				buffer.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buffer.toString();
	}

}
