
package com.archermind.schedule.Screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import com.archermind.schedule.Model.ScheduleBean;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Utils.SyncDataUtil;
import com.archermind.schedule.Views.XListView;
import com.archermind.schedule.Views.XListView.IXListViewListener;

public class MyDynamicScreen extends Screen implements IXListViewListener, OnItemClickListener {
	private XListView list;

    private RelativeLayout mListFooter;

    private Button loginBtn;

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
        list.setOnItemClickListener(this);
        list.setXListViewListener(this);
        dataArrayList = new ArrayList<ScheduleBean>();
        mAdapter = new DynamicScheduleAdapter(MyDynamicScreen.this, dataArrayList, list);
        mAdapter.setList(dataArrayList);
        list.setAdapter(mAdapter);
        loading = (RelativeLayout)findViewById(R.id.loading);
        mListFooter = (RelativeLayout)LayoutInflater.from(this).inflate(
                R.layout.dynamic_listview_footer, null);
        loginBtn = (Button)mListFooter.findViewById(R.id.login);
        loginBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MyDynamicScreen.this, "login", Toast.LENGTH_SHORT).show();
                Intent it = new Intent(MyDynamicScreen.this,LoginScreen.class);
				startActivity(it);
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
		if(ServiceManager.getUserId() == 0){
			Toast.makeText(MyDynamicScreen.this, "请登录以后再刷新",
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(MyDynamicScreen.this, "onRefresh",
					Toast.LENGTH_SHORT).show();
			new Thread(new Runnable(){
				@Override
				public void run() {
					SyncDataUtil.getSchedulesFromWeb(String.valueOf(ServiceManager.getUserId()));
					dataArrayList.clear();
					c = ServiceManager.getDbManager().queryLocalSchedules(start, end);
					cursorToArrayList(c);
					mHandler.sendEmptyMessage(ON_Refresh);
				}}).start();
		}
	}

	@Override
	public void onLoadMore() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (dataArrayList.size() != 0){
					ScheduleBean bean = dataArrayList.get(dataArrayList.size() - 1);
					c = ServiceManager.getDbManager().queryLocalSchedules(
							bean.getTime(), LOAD_DATA_SIZE);
					if (c.getCount() != 0) {
						cursorToArrayList(c);
						mHandler.sendEmptyMessage(ON_LoadMore);
						return;
					} 
				}
				MyDynamicScreen.this.runOnUiThread(new Runnable(){
						@Override
						public void run() {
							Toast.makeText(MyDynamicScreen.this,
									"onLoadMore is lastone", Toast.LENGTH_SHORT).show();
							onLoad();
				}});
			}
		}).start();
	}
	
	private void loadSchedules(){
		SyncDataUtil.getSchedulesFromWeb(String.valueOf(ServiceManager.getUserId()));
		c = ServiceManager.getDbManager().queryLocalSchedules(start, end);
//		if (c.getCount() == 0) {
//			    SyncDataUtil.insertDefaultSchedules();
//				c.requery();
//		} 
		cursorToArrayList(c);
		mHandler.sendEmptyMessage(ON_LoadData);
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


		Button publishBtn = (Button) contentView.findViewById(R.id.publishBtn);
		publishBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(editText.getText().toString().equals("")){
					Toast.makeText(getApplicationContext(), "留言不能为空", Toast.LENGTH_SHORT).show();
				} else {
					if(saveScheduleToDb(editText.getText().toString(), t_id)){
						Toast.makeText(getApplicationContext(), "留言提交成功", Toast.LENGTH_SHORT).show();
						onRefresh();
					} else {
						Toast.makeText(getApplicationContext(), "留言提交失败", Toast.LENGTH_SHORT).show();
					}
					popupWindow.dismiss();
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
				bean.setT_id(c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_T_ID)));
				bean.setTime(c.getLong(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME)));
				bean.setType(c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_TYPE)));
				bean.setUser_id(c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_USER_ID)));
				dataArrayList.add(bean);
			}
		} 
		c.close();
	}





	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Toast.makeText(
				MyDynamicScreen.this,
				"position:" + position, Toast.LENGTH_SHORT)
				.show();
		if(!dataArrayList.get(position-1).isDefault_data()){
			initPopWindow(MyDynamicScreen.this, dataArrayList.get(position-1).getT_id());
		}
	}
	
	public boolean saveScheduleToDb(final String content, final int t_id) {
		ScheduleApplication.LogD(FriendsDyamicScreen.class, content + " id = " + t_id + " USER:" + ServiceManager.getUserId());
		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG, DatabaseHelper.SCHEDULE_OPER_ADD);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID, t_id);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_USER_ID, ServiceManager.getUserId());
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_SHARE, true);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER, 1);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME, System.currentTimeMillis());
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT, content);
		ServiceManager.getDbManager().insertLocalSchedules(cv);
		int result = ServiceManager.getServerInterface().uploadSchedule("1", String.valueOf(t_id));
		if(result > 0){
			return true;
		}
		return false;
}


    protected void onDestroy() {

        super.onDestroy();
    }


	@Override
	protected void onResume() {
		super.onResume();
		if (ServiceManager.getUserId() == 0) {
			if(!list.isFooterViewAdd()){
				ScheduleApplication.LogD(MyDynamicScreen.class, " NOT list.isFooterViewAdd()");
				list.addFooterView(mListFooter);
				list.setFooterViewAdd(true);
				list.setHeaderGone(false);
				list.setPullRefreshEnable(false);
				list.setPullLoadEnable(false);
			} else {
				ScheduleApplication.LogD(MyDynamicScreen.class, "list.isFooterViewAdd()");
			}
		} else {
			//if(list.isFooterViewAdd()){
				list.removeFooterView(mListFooter);
				list.setFooterViewAdd(false);
				list.setHeaderGone(true);
				list.setPullRefreshEnable(true);
				list.setPullLoadEnable(true);
		//	}
		}
	}
    

}
