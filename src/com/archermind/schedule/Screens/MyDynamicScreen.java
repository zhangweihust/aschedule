
package com.archermind.schedule.Screens;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Adapters.DynamicScheduleAdapter;
import com.archermind.schedule.Model.ScheduleBean;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Utils.NetworkUtils;
import com.archermind.schedule.Views.XListView;
import com.archermind.schedule.Views.XListView.IXListViewListener;

public class MyDynamicScreen extends Screen implements IXListViewListener, OnItemClickListener {
	private XListView list;
	private RelativeLayout mListFooter;
	private Button loginBtn, registerBtn;
	private RelativeLayout loading;
	private Cursor c;
	protected static final int ON_Refresh = 0x101;
	protected static final int ON_LoadMore = 0x102;
	protected static final int ON_LoadData = 0x103;
	private int end = 10;
	private int start = 0;
	private List<ScheduleBean> dataArrayList;
	private DynamicScheduleAdapter mAdapter;
	private final int LOAD_DATA_SIZE = 20;


    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ON_Refresh:
                    loading.setVisibility(View.GONE);
                    mAdapter.setList(dataArrayList);
                    break;
                case ON_LoadMore:
                    loading.setVisibility(View.GONE);
                    mAdapter.setList(dataArrayList);
                    break;
                case ON_LoadData:
                    loading.setVisibility(View.GONE);
                    mAdapter.setList(dataArrayList);
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
		list.setOnItemClickListener(this);
		list.setXListViewListener(this);
		dataArrayList = new ArrayList<ScheduleBean>();
		mAdapter = new DynamicScheduleAdapter(MyDynamicScreen.this, dataArrayList);
		mAdapter.setList(dataArrayList);
		list.setAdapter(mAdapter);
		loading = (RelativeLayout) findViewById(R.id.loading);
		mListFooter = (RelativeLayout) LayoutInflater.from(this).inflate(
				R.layout.dynamic_listview_footer, null);
		loginBtn = (Button) mListFooter.findViewById(R.id.login);
		registerBtn = (Button) mListFooter.findViewById(R.id.register);
		loginBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MyDynamicScreen.this, "login",
						Toast.LENGTH_SHORT).show();
			}
		});
		registerBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MyDynamicScreen.this, "register",
						Toast.LENGTH_SHORT).show();
			}
		});
		new Thread(new Runnable() {
			@Override
			public void run() {
				loadSchedules();
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
		Toast.makeText(MyDynamicScreen.this, "onRefresh",
				Toast.LENGTH_SHORT).show();
		getSchedulesFromWeb("1343203371");
		dataArrayList.clear();
		c = ServiceManager.getDbManager().queryLocalSchedules(start, end);
		cursorToArrayList(c);
		mHandler.sendEmptyMessage(ON_Refresh);
		onLoad();
	}

	@Override
	public void onLoadMore() {
		ScheduleBean bean = dataArrayList.get(dataArrayList.size() -1);
		c = ServiceManager.getDbManager().queryLocalSchedules(bean.getTime(), LOAD_DATA_SIZE);
		if(c.getCount() != 0){
			cursorToArrayList(c);
			mHandler.sendEmptyMessage(ON_LoadMore);
		} else {
			Toast.makeText(MyDynamicScreen.this, "onLoadMore is lastone" ,
					Toast.LENGTH_SHORT).show();
		}
		onLoad();
	}
	
	private void loadSchedules(){
		getSchedulesFromWeb("1343203369");
		c = ServiceManager.getDbManager().queryLocalSchedules(start, end);
		if (c.getCount() == 0) {
				list.addFooterView(mListFooter);
				list.setHeaderGone(false);
				insertDefaultSchedules();
				c.requery();
		} 
		cursorToArrayList(c);
		mHandler.sendEmptyMessage(ON_LoadData);
	}
	

	private void getSchedulesFromWeb(String time) {
		if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
			
			String jsonString = ServiceManager.getServerInterface()
					.syncFriendShare("3", time);
			try {
				JSONArray jsonArray = new JSONArray(jsonString);
				ScheduleApplication.LogD(MyDynamicScreen.class, jsonString
						+ jsonArray.length());
				ContentValues contentvalues;
				//ScheduleBean bean = null;
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
//					bean = new ScheduleBean();
//					bean.setContent(jsonObject.getString("content"));
//					bean.setLocation(jsonObject.getString("city"));
//					bean.setT_id(Integer.parseInt(t_id));
//					bean.setTime(Long.parseLong(jsonObject.getString("start_time")));
//					bean.setType(Integer.parseInt(jsonObject.getString("type")));
//					dataArrayList.add(0,bean);
					if (!ServiceManager.getDbManager().isInShareSchedules(t_id)) {
						ServiceManager.getDbManager().insertShareSchedules(
								contentvalues);
					} else {
						ServiceManager.getDbManager().updateShareSchedules(
								contentvalues, t_id);
						ScheduleApplication.LogD(MyDynamicScreen.class, "重复的TID：" + t_id);
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void cursorToArrayList(Cursor c){
		if (c != null && c.getCount() > 0){
			ScheduleBean bean;
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				bean = new ScheduleBean();
				bean.setContent(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT)));
//				bean.setLocation(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CITY)));
				bean.setLocation("武汉");
				bean.setT_id(c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_T_ID)));
				bean.setTime(c.getLong(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME)));
				bean.setType(c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_TYPE)));
				dataArrayList.add(bean);
			}
			c.close();
		} 
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


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Toast.makeText(
				MyDynamicScreen.this,
				"position:" + position, Toast.LENGTH_SHORT)
				.show();
	}

}
