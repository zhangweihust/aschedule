package com.archermind.schedule.Screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Adapters.DynamicScheduleAdapter;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.EventTypes;
import com.archermind.schedule.Events.IEventHandler;
import com.archermind.schedule.Model.ScheduleBean;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Utils.NetworkUtils;
import com.archermind.schedule.Views.XListView;
import com.archermind.schedule.Views.XListView.IXListViewListener;

public class FriendsDyamicScreen extends Screen implements IXListViewListener, OnItemClickListener,
        IEventHandler {

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
                eventService.onUpdateEvent(new EventArgs(EventTypes.SERVICE_TIP_OFF));
				loading.setVisibility(View.GONE);
				mAdapter.setList(dataArrayList);
				onLoad();
				break;
			case ON_LoadMore:
				loading.setVisibility(View.GONE);
				mAdapter.setList(dataArrayList);
				onLoad();
				break;
			case ON_LoadData:
				loading.setVisibility(View.GONE);
				mAdapter.setList(dataArrayList);
				onLoad();
				break;	
			
			}

		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_dynamic_screen);
        list = (XListView)findViewById(R.id.list);
        list.setPullLoadEnable(true);
        list.setPullRefreshEnable(true);
        list.setOnItemClickListener(this);
        list.setXListViewListener(this);
        dataArrayList = new ArrayList<ScheduleBean>();
        mAdapter = new DynamicScheduleAdapter(FriendsDyamicScreen.this, dataArrayList, list);
        mAdapter.setList(dataArrayList);
        list.setAdapter(mAdapter);
        loading = (RelativeLayout)findViewById(R.id.loading);
        mListFooter = (RelativeLayout)LayoutInflater.from(this).inflate(
                R.layout.dynamic_listview_footer, null);
        loginBtn = (Button)mListFooter.findViewById(R.id.login);
        registerBtn = (Button)mListFooter.findViewById(R.id.register);
        eventService.add(this);
        loginBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FriendsDyamicScreen.this, "login", Toast.LENGTH_SHORT).show();
            }
        });
        registerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FriendsDyamicScreen.this, "register", Toast.LENGTH_SHORT).show();
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadSchedules();
            }
        }).start();
    }

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			HomeScreen.switchActivity();
		}
		return super.onKeyUp(keyCode, event);
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
		new Thread(new Runnable(){
			@Override
			public void run() {
				getSchedulesFromWeb("3", "1343203371");
				dataArrayList.clear();
				c = ServiceManager.getDbManager().queryShareSchedules(start, end);
				cursorToArrayList(c);
				mHandler.sendEmptyMessage(ON_Refresh);
			}}).start();
	}

	@Override
	public void onLoadMore() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				ScheduleBean bean = dataArrayList.get(dataArrayList.size() - 1);
				c = ServiceManager.getDbManager().queryShareSchedules(
						bean.getTime(), LOAD_DATA_SIZE);
				if (c.getCount() != 0) {
					cursorToArrayList(c);
					mHandler.sendEmptyMessage(ON_LoadMore);
				} else {
					FriendsDyamicScreen.this.runOnUiThread(new Runnable(){
						@Override
						public void run() {
							Toast.makeText(FriendsDyamicScreen.this,
									"onLoadMore is lastone", Toast.LENGTH_SHORT).show();
							onLoad();
						}});
				}
			}
		}).start();
	}
	
	private void loadSchedules(){
		getSchedulesFromWeb("3", "1343203369");
		c = ServiceManager.getDbManager().queryShareSchedules(start, end);
		if (c.getCount() == 0) {
				list.addFooterView(mListFooter);
				list.setHeaderGone(false);
				insertDefaultSchedules();
				c.requery();
		} 
		cursorToArrayList(c);
		mHandler.sendEmptyMessage(ON_LoadData);
	}
	

	private void getSchedulesFromWeb(String userId, String time) {
		if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
			
			String jsonString = ServiceManager.getServerInterface()
					.syncFriendShare(userId, time);
			try {
				JSONArray jsonArray = new JSONArray(jsonString);
				ScheduleApplication.LogD(FriendsDyamicScreen.class, jsonString
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
						ScheduleApplication.LogD(FriendsDyamicScreen.class, "重复的TID：" + t_id);
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	private void initPopWindow(Context context, final int t_id) {
		// 加载popupWindow的布局文件
		View contentView = LayoutInflater.from(context).inflate(
				R.layout.leave_message, null);
		// 声明一个弹出框
		final PopupWindow popupWindow = new PopupWindow(contentView,
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		// 为弹出框设定自定义的布局
		popupWindow.setOutsideTouchable(true);
		final EditText editText = (EditText) contentView
				.findViewById(R.id.editText1);
		/*
		 * 这个popupWindow.setFocusable(true);非常重要，如果不在弹出之前加上这条语句，你会很悲剧的发现，你是无法在
		 * editText中输入任何东西的
		 * 。该方法可以设定popupWindow获取焦点的能力。当设置为true时，系统会捕获到焦点给popupWindow
		 * 上的组件。默认为false哦.该方法一定要在弹出对话框之前进行调用。
		 */
		popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		popupWindow.setFocusable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		/*
		 * popupWindow.showAsDropDown（View view）弹出对话框，位置在紧挨着view组件
		 * showAsDropDown(View anchor, int xoff, int yoff)弹出对话框，位置在紧挨着view组件，x y
		 * 代表着偏移量 showAtLocation(View parent, int gravity, int x, int y)弹出对话框
		 * parent 父布局 gravity 依靠父布局的位置如Gravity.CENTER x y 坐标值
		 */
		popupWindow.showAtLocation(list,Gravity.CENTER,0,0);

//		Button joinBtn = (Button) contentView.findViewById(R.id.joinBtn);
//		joinBtn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				popupWindow.dismiss();
//			}
//		});
//
//		Button forwardBtn = (Button) contentView.findViewById(R.id.forwardBtn);
//		forwardBtn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				popupWindow.dismiss();
//			}
//		});

		Button publishBtn = (Button) contentView.findViewById(R.id.publishBtn);
		publishBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(editText.getText().toString().equals("")){
					Toast.makeText(getApplicationContext(), "留言不能为空", Toast.LENGTH_SHORT).show();
				} else {
					popupWindow.dismiss();
					saveScheduleToDb(editText.getText().toString(), t_id);
				}
			}
		});
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
		@Override
		public void run() {
		InputMethodManager m = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		}
		}, 500);
		
	}
	
	
	private void cursorToArrayList(Cursor c){
		if (c != null && c.getCount() > 0){
			ScheduleBean bean;
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				bean = new ScheduleBean();
				bean.setContent(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT)));
				bean.setLocation(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CITY)));
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
				FriendsDyamicScreen.this,
				"position:" + position, Toast.LENGTH_SHORT)
				.show();
		initPopWindow(FriendsDyamicScreen.this, dataArrayList.get(position-1).getT_id());
	}
	
	public void saveScheduleToDb(final String content, final int t_id) {
		new Thread( new Runnable(){
			@Override
			public void run() {
				ScheduleApplication.LogD(FriendsDyamicScreen.class, content + " id = " + t_id + " USER:" + ServiceManager.getUserId());
				ContentValues cv = new ContentValues();
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG, DatabaseHelper.SCHEDULE_OPER_ADD);
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID, t_id);
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_USER_ID, ServiceManager.getUserId());
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_SHARE, true);
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER, 1);
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME, System.currentTimeMillis());
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT, content);
				ServiceManager.getDbManager().insertLocalSchedules(cv, System.currentTimeMillis());
				ServiceManager.getServerInterface().uploadSchedule("1", String.valueOf(t_id));
			}
			
		}).start();
	}

    protected void onDestroy() {

        eventService.remove(this);
        super.onDestroy();
    }

    @Override
    public boolean onEvent(Object sender, EventArgs e) {
        // TODO Auto-generated method stub
        return false;
    }

}
