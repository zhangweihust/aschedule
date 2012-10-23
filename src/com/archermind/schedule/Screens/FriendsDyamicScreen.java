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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Adapters.DynamicScheduleAdapter;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.EventTypes;
import com.archermind.schedule.Events.IEventHandler;
import com.archermind.schedule.Model.ScheduleBean;
import com.archermind.schedule.Model.UserInfoData;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Utils.DeviceInfo;
import com.archermind.schedule.Utils.SyncDataUtil;
import com.archermind.schedule.Views.XListView;
import com.archermind.schedule.Views.XListView.IXListViewListener;
import com.archermind.schedule.Views.XListViewFooter;

public class FriendsDyamicScreen extends Screen
		implements
			IXListViewListener,
			OnItemClickListener,
			IEventHandler {

	private XListView list = null;

	private Button loginBtn, bindBtn, refreshBtn;

	private TextView noteTv;

	private RelativeLayout loading;

	private LinearLayout bindLayout, loginLayout, refreshLayout;

	private Cursor c;

	protected static final int ON_Refresh = 0x101;

	protected static final int ON_LoadMore = 0x102;

	protected static final int ON_LoadData = 0x103;

	protected static final int RefreshLayout_Gone = 0x104;

	protected static final int RefreshLayout_Visible = 0x105;

	private int end = 10;

	private int start = 0;

	private List<ScheduleBean> dataArrayList;

	private DynamicScheduleAdapter mAdapter;

	private final int LOAD_DATA_SIZE = 20;

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try {
				switch (msg.what) {
					case ON_Refresh :
						eventService.onUpdateEvent(new EventArgs(
								EventTypes.SERVICE_TIP_OFF));
						loading.setVisibility(View.GONE);
						dataArrayList.clear();
						cursorToArrayList((Cursor)msg.obj);
						mAdapter.notifyDataSetChanged();
						onLoad();
						break;
					case ON_LoadMore :
						loading.setVisibility(View.GONE);
						cursorToArrayList((Cursor)msg.obj);
						mAdapter.notifyDataSetChanged();
						onLoad();
						break;
					case ON_LoadData :
						loading.setVisibility(View.GONE);
						cursorToArrayList((Cursor)msg.obj);
						mAdapter.notifyDataSetChanged();
						onLoad();
						break;
					case RefreshLayout_Gone :
						refreshLayout.setVisibility(View.GONE);
						break;
					case RefreshLayout_Visible :
						refreshLayout.setVisibility(View.VISIBLE);
						break;
						
				}
				if (!dataArrayList.isEmpty()) {
					list.setXListViewListener(FriendsDyamicScreen.this);
					list.setPullRefreshEnable(true);
					list.setPullLoadEnable(true);
				}
			} catch (Exception e) {
				ScheduleApplication.logException(getClass(),e);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ServiceManager.setListViewKind(XListViewFooter.DYNAMIC_PROMPT);
		setContentView(R.layout.friends_dynamic_screen);
		list = (XListView) findViewById(R.id.list);
		list.setOnItemClickListener(this);
		dataArrayList = new ArrayList<ScheduleBean>();
		mAdapter = new DynamicScheduleAdapter(FriendsDyamicScreen.this, dataArrayList, list);
		list.setAdapter(mAdapter);
		loading = (RelativeLayout) findViewById(R.id.loading);
		bindLayout = (LinearLayout) findViewById(R.id.bindTel);
		loginLayout = (LinearLayout) findViewById(R.id.loginUp);
		refreshLayout = (LinearLayout) findViewById(R.id.nullDynamic);
		noteTv = (TextView) findViewById(R.id.note);
		noteTv.setText("还没有好友分享动态，赶紧叫好友分享吧");
		loginBtn = (Button) findViewById(R.id.login);
		bindBtn = (Button) findViewById(R.id.bind);
		refreshBtn = (Button) findViewById(R.id.refresh);
		eventService.add(this);
		loginBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(FriendsDyamicScreen.this,
						LoginScreen.class);
				startActivity(it);
			}
		});
		bindBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(FriendsDyamicScreen.this,
						TelephoneBindScreen.class);
				startActivity(it);
			}
		});
		refreshBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onRefresh();
			}
		});

		if (ServiceManager.getUserId() == 0) {

			ScheduleApplication.LogD(getClass(), " onCreate 未登录 userid "
					+ ServiceManager.getUserId());
			if (!dataArrayList.isEmpty()) {

				dataArrayList.clear();
				mAdapter.notifyDataSetChanged();
			}
			loginLayout.setVisibility(View.VISIBLE);
			list.setPullRefreshEnable(false);
			list.setPullLoadEnable(false);

		} else {	    
            if (ServiceManager.getBindFlag())
              {
				ScheduleApplication.LogD(
						getClass(),
						" onCreate 已登录且已绑定 userid "
								+ ServiceManager.getUserId());
				loginLayout.setVisibility(View.GONE);
				bindLayout.setVisibility(View.GONE);
				new Thread(new Runnable() {
					@Override
					public void run() {
						loadSchedules();
					}
				}).start();
			} else {
				ScheduleApplication.LogD(getClass(), " onCreate 未绑定  ");
				loginLayout.setVisibility(View.GONE);
				bindLayout.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (keyCode == KeyEvent.KEYCODE_MENU) {
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
		if (ServiceManager.getUserId() == 0) {
			Toast.makeText(FriendsDyamicScreen.this, "请登录以后再刷新",
					Toast.LENGTH_SHORT).show();
		} else {
			if (dataArrayList.isEmpty())
				loading.setVisibility(View.VISIBLE);
			new Thread(new Runnable() {
				@Override
				public void run() {
					SyncDataUtil.getSchedulesFromWeb(String
							.valueOf(ServiceManager.getUserId()));
					dataArrayList.clear();
					c = ServiceManager.getDbManager().queryShareSchedules(
							start, end);
					if (c != null) {
						if (c.getCount() == 0) {
							mHandler.sendEmptyMessage(RefreshLayout_Visible);
							c.close();
						} else {
							mHandler.sendEmptyMessage(RefreshLayout_Gone);
						}
					}
					Message msg = new Message();
                  msg.what = ON_Refresh;
                  msg.obj = c;
                  Bundle bundle = new Bundle();
                  msg.setData(bundle);
                  mHandler.sendMessage(msg);
				}
			}).start();
		}
	}

	@Override
	public void onLoadMore() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (dataArrayList.size() != 0) {
						ScheduleBean bean = dataArrayList.get(dataArrayList.size() - 1);
						c = ServiceManager.getDbManager().queryShareSchedules(
								bean.getTime(), LOAD_DATA_SIZE);
						if (c != null && c.getCount() != 0) {
							Message msg = new Message();
							msg.what = ON_LoadMore;
							msg.obj = c;
							Bundle bundle = new Bundle();
							msg.setData(bundle);
							mHandler.sendMessage(msg); 
							return;
						} else {
							c.close();
						}
					}
					FriendsDyamicScreen.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(FriendsDyamicScreen.this, "历史记录加载完毕",
									Toast.LENGTH_SHORT).show();
							onLoad();
						}
					});
				} catch (Exception e) {
					ScheduleApplication.logException(getClass(),e);
				}
			}
		}).start();
	}

	private void loadSchedules() {
		SyncDataUtil.getSchedulesFromWeb(String.valueOf(ServiceManager
				.getUserId()));
		c = ServiceManager.getDbManager().queryShareSchedules(start, end);
		if (c != null) {
			if (c.getCount() == 0) {
				mHandler.sendEmptyMessage(RefreshLayout_Visible);
				c.close();
			} else {
				mHandler.sendEmptyMessage(RefreshLayout_Gone);
			}
		}
		 Message msg = new Message();
        msg.what = ON_LoadData;
        msg.obj = c;
        Bundle bundle = new Bundle();
        msg.setData(bundle);
        mHandler.sendMessage(msg);
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
		popupWindow
				.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		popupWindow.setFocusable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		/*
		 * popupWindow.showAsDropDown（View view）弹出对话框，位置在紧挨着view组件
		 * showAsDropDown(View anchor, int xoff, int yoff)弹出对话框，位置在紧挨着view组件，x y
		 * 代表着偏移量 showAtLocation(View parent, int gravity, int x, int y)弹出对话框
		 * parent 父布局 gravity 依靠父布局的位置如Gravity.CENTER x y 坐标值
		 */
		popupWindow.showAtLocation(list, Gravity.CENTER, 0, 0);

		Button publishBtn = (Button) contentView.findViewById(R.id.publishBtn);
		publishBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (editText.getText().toString().equals("")) {
					Toast.makeText(getApplicationContext(), "留言不能为空",
							Toast.LENGTH_SHORT).show();
				} else {
					if (saveScheduleToDb(editText.getText().toString(), t_id)) {
						Toast.makeText(getApplicationContext(), "留言提交成功",
								Toast.LENGTH_SHORT).show();
						onRefresh();
					} else {
						Toast.makeText(getApplicationContext(), "留言提交失败",
								Toast.LENGTH_SHORT).show();
					}
					popupWindow.dismiss();
				}
			}
		});

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				InputMethodManager m = (InputMethodManager) editText
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 500);
	}

	private void cursorToArrayList(Cursor c) {
		if (c != null && c.getCount() >= 0) {
			ScheduleBean bean;
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				bean = new ScheduleBean();
				bean.setContent(c.getString(c
						.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT)));
				bean.setLocation(c.getString(c
						.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CITY)));
				bean.setT_id(c.getInt(c
						.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_T_ID)));
				bean.setUser_id(c.getInt(c
						.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_USER_ID)));
				bean.setTime(c.getLong(c
						.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME)));
				bean.setType(c.getInt(c
						.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_TYPE)));
				bean.setDefault_data(c.getInt(c
						.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_DEFAULT)) == 1);
				dataArrayList.add(bean);
			}
			c.close();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		try {
			if (dataArrayList.size() != 0
					&& !dataArrayList.get(position - 1).isDefault_data()) {
				initPopWindow(FriendsDyamicScreen.this,
						dataArrayList.get(position - 1).getT_id());
			}
		} catch (Exception e) {
			ScheduleApplication.LogD(getClass(), "onItemClick error");
		}
	}

	public boolean saveScheduleToDb(final String content, final int t_id) {
		ScheduleApplication.LogD(FriendsDyamicScreen.class, content + " id = "
				+ t_id + " USER:" + ServiceManager.getUserId());
		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG,
				DatabaseHelper.SCHEDULE_OPER_ADD);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID, t_id);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_USER_ID,
				ServiceManager.getUserId());
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_SHARE, true);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER, 1);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
				System.currentTimeMillis());
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT, content);
		ServiceManager.getDbManager().insertLocalSchedules(cv);
		int result = ServiceManager.getServerInterface().uploadSchedule("1",
				String.valueOf(t_id));
		if (result > 0) {
			return true;
		}
		return false;
	}

	protected void onDestroy() {
		eventService.remove(this);
		super.onDestroy();
	}

	// @Override
	// protected void onResume() {
	// super.onResume();
	// if (ServiceManager.getUserId() == 0) {
	// if (!dataArrayList.isEmpty()) {
	//
	// dataArrayList.clear();
	// mAdapter.setList(dataArrayList);
	// }
	//
	// loginLayout.setVisibility(View.VISIBLE);
	// list.setPullRefreshEnable(false);
	// list.setPullLoadEnable(false);
	//
	// } else {
	// if (ServiceManager.getBindFlag()) {
	// loginLayout.setVisibility(View.GONE);
	// bindLayout.setVisibility(View.GONE);
	// new Thread(new Runnable() {
	// @Override
	// public void run() {
	// loadSchedules();
	// }
	// }).start();
	// } else {
	// loginLayout.setVisibility(View.GONE);
	// bindLayout.setVisibility(View.VISIBLE);
	// }
	// }
	// }

	@Override
	public boolean onEvent(Object sender, EventArgs e) {
		try {
			switch (e.getType()) {
				
				case LOGIN_SUCCESS :
					this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							
							if (ServiceManager.getBindFlag()) {
								loginLayout.setVisibility(View.GONE);
								bindLayout.setVisibility(View.GONE);
								new Thread(new Runnable() {
									@Override
									public void run() {
										
										loadSchedules();
									}
								}).start();
							} else {
								loginLayout.setVisibility(View.GONE);
								bindLayout.setVisibility(View.VISIBLE);
							}
						}
					});
					break;
					
				case TELEPHONE_BIND_SUCCESS :
					this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							loginLayout.setVisibility(View.GONE);
							bindLayout.setVisibility(View.GONE);
							loadSchedules();
						}
					});
					break;
					
				case LOGOUT_SUCCESS :
					this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							
							if (!dataArrayList.isEmpty()) {
								
								dataArrayList.clear();
								mAdapter.notifyDataSetChanged();
							}
							loginLayout.setVisibility(View.VISIBLE);
							list.setPullRefreshEnable(false);
							list.setPullLoadEnable(false);
						}
					});
					break;
			}
		} catch (Exception e2) {
			ScheduleApplication.logException(getClass(),e2);
		}
		return true;
	}
}
