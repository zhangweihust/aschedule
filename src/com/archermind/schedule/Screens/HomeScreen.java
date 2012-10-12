package com.archermind.schedule.Screens;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.IEventHandler;
import com.archermind.schedule.Model.UserInfoData;
import com.archermind.schedule.Services.AlarmServiceReceiver;
import com.archermind.schedule.Services.EventService;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.upgrade.DownloadManager;
import com.archermind.upgrade.MessageTypes;
import com.archermind.upgrade.Update;
import com.archermind.upgrade.UpgradeManager;

public class HomeScreen extends TabActivity
		implements
			OnTabChangeListener,
			IEventHandler {


	/** Called when the activity is first created. */
	private TabHost mTabHost;

	private int mCurSelectTabIndex = 0;

	private final int INIT_SELECT = 0;

	private RelativeLayout tabSpecView;

	private View tabSelect;

	private boolean flag = false;

	private Button menuBtn, addBtn;

	private RadioButton myDynamicBtn, friendsDynamicBtn;

	private RadioGroup tabWidget;

	private TextView titleText;

	private ImageView titleImage;

	private static TabHost mChildTabHost;

	private Button titleAddBtn;

	private NotificationManager mNotificationManager;

	private ImageView tipsImageView;

	private EventService eventService = ServiceManager.getEventservice();
	
	private final static String URL = "http://arc.archermind.com/ci/index.php/AppUpdate/getUpdateInfo";
	private final static String PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/"
            + ScheduleApplication.getContext().getPackageName() + "/update/";
	private static final String XML_KEY_TIME = "last_update_time";
	public static final int mNotificationId = 100;
	SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private Notification mNotification = null;
	private PendingIntent mPendingIntent = null;
	// 通知对话框
	private Dialog noticeDialog;

	private SharedPreferences sharedPreferences;
	protected static Context mContext;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_screen);
		ServiceManager.setHomeScreen(this);
		if (ServiceManager.isStarted()) {
		} else {
			if (!ServiceManager.start()) {
				ServiceManager.exit();
				return;
			}
		}
		mContext = HomeScreen.this;
		initNotification();
		initView();
		sharedPreferences = getSharedPreferences(UserInfoData.USER_SETTING,
				Context.MODE_WORLD_READABLE);
		mTabHost.addTab(buildTabSpec("schedule", R.drawable.tab_schedule,
				new Intent(this, ScheduleScreen.class)));
		mTabHost.addTab(buildTabSpecAndTips("dynamic", R.drawable.tab_dynamic,
				new Intent(this, DynamicScreen.class)));
		mTabHost.addTab(buildTabSpec("friend", R.drawable.tab_friend,
				new Intent(this, FriendScreen.class)));

		mTabHost.setCurrentTab(0);
		mTabHost.setOnTabChangedListener(this);
		tabSelect = (View) findViewById(R.id.tabselect_cursor);
		titleAddBtn = (Button) findViewById(R.id.title_bar_add_button);
		titleAddBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent mIntent = new Intent(HomeScreen.this,
						NewScheduleScreen.class);
				startActivity(mIntent);
			}
		});

		eventService.add(this);

		mNotificationManager = (NotificationManager) this
				.getSystemService(NOTIFICATION_SERVICE);
		int id = getIntent().getIntExtra("notify_id", 1);
		mNotificationManager.cancel(id);

		//注册监听联系人变化
		ContentResolver localResolver = getContentResolver();
		Uri localUri1 = ContactsContract.Data.CONTENT_URI;
		localResolver.registerContentObserver(localUri1, true, mObserver);
		checkUpdate();
	}

	private static final int CONTACT_CHANGED = 1;
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case CONTACT_CHANGED:
					//检测到联系人变化就进行同步
					ServiceManager.getContact().checkSync(HomeScreen.this);
					return;
			}
		};
	};
	
	private ContentObserver mObserver = new AScheduleObserver(handler);
    
	class AScheduleObserver extends ContentObserver{

		private Handler mHandler;
		
		public AScheduleObserver(Handler handler) {
			super(handler);
			this.mHandler = handler;
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			mHandler.sendEmptyMessage(CONTACT_CHANGED);
		}
	}
	
	private void initView() {
		mTabHost = this.getTabHost();
		menuBtn = (Button) findViewById(R.id.title_bar_menu_button);
		addBtn = (Button) findViewById(R.id.title_bar_add_button);
		myDynamicBtn = (RadioButton) findViewById(R.id.tab_widget_my_dynamic);
		friendsDynamicBtn = (RadioButton) findViewById(R.id.tab_widget_friends_dynamic);
		titleText = (TextView) findViewById(R.id.title_bar_title_text);
		titleImage = (ImageView) findViewById(R.id.title_bar_title_image);
		tabWidget = (RadioGroup) findViewById(R.id.title_bar_tab_widget);
		tabWidget.setVisibility(View.INVISIBLE);
		titleText.setVisibility(View.INVISIBLE);
		friendsDynamicBtn.setChecked(true);
		tabWidget.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkId) {
				if (mChildTabHost != null ) {
					if (checkId == myDynamicBtn.getId()) {
						mChildTabHost.setCurrentTab(1);
					} else if (checkId == friendsDynamicBtn.getId()) {
						mChildTabHost.setCurrentTab(0);
					}
				}
			}
		});
		menuBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(HomeScreen.this, MenuScreen.class));
				overridePendingTransition(R.anim.right_in, R.anim.right_out);
			}
		});

		addBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});

	}

	public static void setmChildTabHost(TabHost tabHost) {
		mChildTabHost = tabHost;
	}

	private TabSpec buildTabSpec(String tag, int iconId, Intent intent) {
		tabSpecView = (RelativeLayout) LayoutInflater.from(this).inflate(
				R.layout.tab_item_view, null);
		ImageView icon = (ImageView) tabSpecView.findViewById(R.id.imageview);
		icon.setImageResource(iconId);

		TabSpec tabSpec = this.mTabHost.newTabSpec(tag)
				.setIndicator(tabSpecView).setContent(intent);
		return tabSpec;
	}

	private TabSpec buildTabSpecAndTips(String tag, int iconId, Intent intent) {
		tabSpecView = (RelativeLayout) LayoutInflater.from(this).inflate(
				R.layout.tab_item_view, null);
		ImageView icon = (ImageView) tabSpecView.findViewById(R.id.imageview);
		icon.setImageResource(iconId);

		tipsImageView = (ImageView) tabSpecView.findViewById(R.id.tab_icon_num);

		TabSpec tabSpec = this.mTabHost.newTabSpec(tag)
				.setIndicator(tabSpecView).setContent(intent);
		return tabSpec;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (!flag) {

			moveTopSelect(INIT_SELECT);
			flag = true;
		}
	}

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		if (tabId.equalsIgnoreCase("schedule")) {
			moveTopSelect(0);
			tabWidget.setVisibility(View.INVISIBLE);
			titleText.setVisibility(View.INVISIBLE);
			titleImage.setVisibility(View.VISIBLE);

		} else if (tabId.equalsIgnoreCase("dynamic")) {
			moveTopSelect(1);
			tabWidget.setVisibility(View.VISIBLE);
			titleText.setVisibility(View.INVISIBLE);
			titleImage.setVisibility(View.INVISIBLE);
			tipsImageView.setVisibility(View.GONE);

		} else if (tabId.equalsIgnoreCase("friend")) {
			moveTopSelect(2);
			tabWidget.setVisibility(View.INVISIBLE);
			titleText.setVisibility(View.VISIBLE);
			titleText.setText(R.string.tab_name_friend);
			titleImage.setVisibility(View.INVISIBLE);
		}
	}

	public void moveTopSelect(int selectIndex) {
		// 起始位置中心点
		int startMid = ((View) getTabWidget().getChildAt(mCurSelectTabIndex))
				.getLeft()
				+ ((ViewGroup) getTabWidget().getChildAt(mCurSelectTabIndex))
						.getChildAt(0).getLeft()
				+ ((ViewGroup) getTabWidget().getChildAt(mCurSelectTabIndex))
						.getChildAt(0).getWidth() / 7;
		// 目标位置中心点
		int endMid = ((View) getTabWidget().getChildAt(selectIndex)).getLeft()
				+ ((ViewGroup) getTabWidget().getChildAt(selectIndex))
						.getChildAt(0).getLeft()
				+ ((ViewGroup) getTabWidget().getChildAt(selectIndex))
						.getChildAt(0).getWidth() / 7;
		TranslateAnimation animation = new TranslateAnimation(startMid, endMid,
				0, 0);
		animation.setDuration(200);
		animation.setFillAfter(true);
		tabSelect.bringToFront();
		tabSelect.startAnimation(animation);
		mCurSelectTabIndex = selectIndex;
	}

	public static void switchActivity() {
		mContext.startActivity(new Intent(mContext, MenuScreen.class));
		((Activity) mContext).overridePendingTransition(R.anim.right_in,
				R.anim.right_out);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		long id = intent.getIntExtra("notify_id", 1);
		mNotificationManager.cancel((int) id);
	}

	private boolean mExit_Flag;// 退出标记
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		ScheduleApplication.LogD(HomeScreen.class,
				"onKeyDown" + event.getKeyCode());
		if ((mCurSelectTabIndex != 0) || (!ScheduleScreen.isUp)) {

			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
					&& event.getAction() == KeyEvent.ACTION_UP) {
				if (mExit_Flag) {
					CancelRestartService();
					ScheduleApplication.LogD(HomeScreen.class, "mExit_Flag");
					this.finish();
					ServiceManager.exit();
				} else {
					Toast.makeText(this, getString(R.string.exit),
							Toast.LENGTH_SHORT).show();
					mExit_Flag = true;
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							mExit_Flag = false;
						}
					}, 10000);
				}
				return true;
			}
		}

		return super.dispatchKeyEvent(event);
	}

	// 关闭每10秒发送一次启动servicemanager的消息
	private void CancelRestartService() {

		Intent myIntent = new Intent(this, AlarmServiceReceiver.class);
		PendingIntent senderIntent = PendingIntent.getBroadcast(this, 0,
				myIntent, 0);
		AlarmManager am = (AlarmManager) HomeScreen.this
				.getSystemService(Context.ALARM_SERVICE);
		am.cancel(senderIntent);

	}

	@Override
	public boolean onEvent(Object sender, EventArgs e) {

		switch (e.getType()) {

			case SERVICE_TIP_ON : {

				HomeScreen.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {

						tipsImageView.setVisibility(View.VISIBLE);
					}
				});
			}
				break;

			case SERVICE_TIP_OFF : {

				HomeScreen.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {

						tipsImageView.setVisibility(View.GONE);
					}
				});
			}
				break;

			case IMSI_CHANGED :
				ServiceManager.ToastShow("检测到您的手机号发生变化,请重新绑定!");
				startActivity(new Intent(HomeScreen.this,
						TelephoneBindScreen.class));
				ServiceManager.getContact().checkSync(mContext);

				break;
			default :
				break;
		}

		return false;
	}

	protected void onDestroy() {
		super.onDestroy();
		getContentResolver().unregisterContentObserver(mObserver);
		eventService.remove(this);
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageTypes.DOWN_DATA_CHANGED:
				updateNotification((Integer) msg.obj);
				break;
			case MessageTypes.FILE_ALREADY_DOWNLOADED:
				if (msg.obj != null) {
					Log.e("UpgradeDemoActivity", "地址:" + msg.obj);
					UpgradeManager.getInstance().installApk(mContext,
							(String) msg.obj);
				}
				ScheduleApplication.LogD(getClass(), "文件已经下载完毕");
				break;
			case MessageTypes.DOWN_SUCCESS:
				if (msg.obj != null) {
					Log.e("UpgradeDemoActivity", "地址:" + msg.obj);
					UpgradeManager.getInstance().installApk(mContext,
							(String) msg.obj);
				}
				mNotificationManager.cancel(mNotificationId);
				ScheduleApplication.LogD(getClass(), "下载成功");
				break;
			case MessageTypes.DOWN_FAIL:
				ScheduleApplication.LogD(getClass(), "下载失败");
				mNotificationManager.cancel(mNotificationId);
				break;
			case MessageTypes.NO_NEED_TO_UPGRADE:
				ScheduleApplication.LogD(getClass(), "不需要更新");
				sharedPreferences.edit().putString(XML_KEY_TIME, sDateFormat.format(new java.util.Date())).commit();
				break;
			case MessageTypes.NEED_TO_UPGRADE:
				ScheduleApplication.LogD(getClass(), "需要更新");
				showNoticeDialog((Update) msg.obj);
				sharedPreferences.edit().putString(XML_KEY_TIME, sDateFormat.format(new java.util.Date())).commit();
				break;
			case MessageTypes.ERROR:
				ScheduleApplication.LogD(getClass(), "有异常");
				switch ((Integer) msg.obj) {
				case MessageTypes.ERROR_NO_SDCARD:
					ScheduleApplication.LogD(getClass(), "没有SD卡");
					break;
				case MessageTypes.ERROR_IO_ERROR:
					ScheduleApplication.LogD(getClass(), "IO异常");
					break;
				case MessageTypes.ERROR_PARSE_JSON_ERROR:
					ScheduleApplication.LogD(getClass(), "JSON解析异常");
					break;
				case MessageTypes.ERROR_HTTP_DATA_ERROR:
					ScheduleApplication.LogD(getClass(), "网络数据交互异常");
					break;
				case MessageTypes.ERROR_FILE_ERROR:
					ScheduleApplication.LogD(getClass(), "文件操作异常");
					break;
				}
				break;
			}
			super.handleMessage(msg);
		};
		
	};
	
	private void showNoticeDialog(final Update update) {
		if (update == null) {
			return;
		}
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("软件版本更新");
		builder.setMessage(update.getVersionInfo());
		builder.setPositiveButton("立即更新",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						DownloadManager.getInstance().downloadApk(update, true,
								PATH, mHandler);
					}
				});
		builder.setNegativeButton("以后再说",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

					}
				});
		noticeDialog = builder.create();
		noticeDialog.show();
	}

	private void updateNotification(int progress) {
		mNotification.contentView.setProgressBar(R.id.app_upgrade_progressbar,
				100, progress, false);
		mNotification.contentView.setTextViewText(
				R.id.app_upgrade_progresstext, progress + "%");
		mNotificationManager.notify(mNotificationId, mNotification);
	}
	
	private void initNotification(){
		mNotification = new Notification(android.R.drawable.stat_sys_download, "开始下载", System
                .currentTimeMillis());
		mNotification.flags = Notification.FLAG_ONGOING_EVENT;
		mNotification.contentView = new RemoteViews(getApplication()
				.getPackageName(), R.layout.app_upgrade_notification);
		Intent completingIntent = new Intent();
		completingIntent.setClass(this, HomeScreen.class);
		mPendingIntent = PendingIntent.getActivity(this,
				0, completingIntent, 0);
		mNotification.contentIntent = mPendingIntent;
		mNotification.contentView.setProgressBar(R.id.app_upgrade_progressbar,
				100, 0, false);
		mNotification.contentView.setTextViewText(
				R.id.app_upgrade_progresstext, 0 + "%");
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}
	public void checkUpdate(){
		String saveTime =sharedPreferences.getString(XML_KEY_TIME, null);
		if(saveTime != null && sDateFormat.format(new java.util.Date()).equals(saveTime)){
			ScheduleApplication.LogD(getClass(), "不需要检测新版本");
			return;
		} else {
			ScheduleApplication.LogD(getClass(), "开始检测新版本");
			UpgradeManager.getInstance().checkAppUpdate(
					HomeScreen.this, URL, UpgradeManager.A_SCHEDULE,
					mHandler);
		}
	}
}
