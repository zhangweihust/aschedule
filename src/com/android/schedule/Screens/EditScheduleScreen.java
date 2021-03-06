package com.android.schedule.Screens;

import java.util.Calendar;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.schedule.ScheduleApplication;
import com.android.schedule.Dialog.AlarmPopwindow;
import com.android.schedule.Dialog.EventTypeDialog;
import com.android.schedule.Dialog.TimeSelectorDialog;
import com.android.schedule.Dialog.AlarmPopwindow.OnRemindSelectListener;
import com.android.schedule.Dialog.EventTypeDialog.OnEventTypeSelectListener;
import com.android.schedule.Dialog.TimeSelectorDialog.OnOkButtonClickListener;
import com.android.schedule.Events.EventArgs;
import com.android.schedule.Events.EventTypes;
import com.android.schedule.Provider.DatabaseHelper;
import com.android.schedule.Services.ServiceManager;
import com.android.schedule.Utils.Constant;
import com.android.schedule.Utils.DateTimeUtils;
import com.android.schedule.Utils.ServerInterface;
import com.android.schedule.Views.ScheduleEditText;
import com.archermind.schedule.R;

public class EditScheduleScreen extends Screen implements OnClickListener {
	/** Called when the activity is first created. */
	private static String TAG = "EditScheduleScreen";

	private View dateView, share, remind, event;

	private ImageView shareImg, remindImg, eventImg;

	private ScheduleEditText schedule_text;

	private TextView mScheduleMonthTv, mScheduleYearTv, mScheduleTimeTv;

	private Button saveBtn, backBtn;

	private LinearLayout schedule_top;

	private LinearLayout event_addtion_linear;

	private boolean mStageRemind;

	private boolean mShare;

	private boolean mRemind;

	private int mType = -1;

	private long startTime = 0;

	private long endTime = 0;

	private String oper_flag;

	private String remindCycle = "0";

	private String weekType = " ";

	private String monthday = " ";

	private String yearday = " ";

	private long flagAlarm;

	private long scheduleTime;

	private ServerInterface si;

	private TimeSelectorDialog timeselectordialog;

	private AlarmPopwindow alarmPopwindow;

	private EventTypeDialog eventTypeDialog;

	private int schedule_id = 1;

	private String scheduleText;

	private int screenHeight;

	private TimeSelectorOkListener mTimeSelectorOkListener = new TimeSelectorOkListener();

	private RemindSelectListner mRemindSelectListner = new RemindSelectListner();

	private EventTypeSelectListner mEventTypeSelectListner = new EventTypeSelectListner();

	private ImageView remind_selector;

	private ImageView event_selector;

	private long mSelectTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_edit);

		si = ServiceManager.getServerInterface();
		Intent mIntent = new Intent();
		mIntent = getIntent();

		schedule_id = mIntent.getIntExtra("id", 1);
		scheduleTime = mIntent.getLongExtra("time", System.currentTimeMillis());

		Log.i(TAG,
				" detail startTime = "
						+ DateTimeUtils.time2String("yyyy-MM-dd-HH-mm",
								scheduleTime));

		// setDisplayTime(scheduleTime);
		Log.i(TAG, "------schedule_id=" + schedule_id);
		init();
	}

	public void init() {

		schedule_top = (LinearLayout) findViewById(R.id.schedule_new_top);
		event_addtion_linear = (LinearLayout) findViewById(R.id.schedule_new_addtion);

		saveBtn = (Button) findViewById(R.id.schedule_new_save);
		saveBtn.setOnClickListener(this);
		backBtn = (Button) findViewById(R.id.schedule_new_back);
		backBtn.setOnClickListener(this);

		share = findViewById(R.id.schedule_new_share);
		share.setOnClickListener(this);
		shareImg = (ImageView) findViewById(R.id.schedule_new_share_img);

		remind = findViewById(R.id.schedule_new_remind);
		remind.setOnClickListener(this);
		remindImg = (ImageView) findViewById(R.id.schedule_new_remind_img);
		remind_selector = (ImageView) findViewById(R.id.schedule_share_tringle_img);

		event = findViewById(R.id.schedule_new_event);
		event.setOnClickListener(this);
		eventImg = (ImageView) findViewById(R.id.schedule_new_event_img);
		event_selector = (ImageView) findViewById(R.id.schedule_event_tringle_img);

		dateView = findViewById(R.id.schedule_new_date);
		dateView.setOnClickListener(this);

		mScheduleMonthTv = (TextView) findViewById(R.id.schedule_new_month_day);
		mScheduleYearTv = (TextView) findViewById(R.id.schedule_new_year);
		mScheduleTimeTv = (TextView) findViewById(R.id.schedule_new_time);

		schedule_text = (ScheduleEditText) findViewById(R.id.schedule_note);

		// 查询数据库，获得该日程的相关信息
		readDb();
		Display display = getWindowManager().getDefaultDisplay();
		screenHeight = display.getHeight();

		timeselectordialog = new TimeSelectorDialog(this);
		timeselectordialog.setOnOkButtonClickListener(mTimeSelectorOkListener);
		Log.d(TAG, "=------------startTime=" + startTime);
		alarmPopwindow = new AlarmPopwindow(this, screenHeight);
		alarmPopwindow.setOnRemindSelectListener(mRemindSelectListner);
		eventTypeDialog = new EventTypeDialog(this, R.style.EventTypedialog,
				mType, screenHeight / 8);
		eventTypeDialog.setOnEventTypeSelectListener(mEventTypeSelectListner);

		// 初始化界面
		schedule_text.setText(scheduleText);
		// 设置edittext的高度
		ScheduleEditText.initNoteHight = screenHeight;

		setDisplayTime(scheduleTime);

		mSelectTime = scheduleTime;
		if (mShare) {
			shareImg.setImageResource(R.drawable.schedule_new_share_select);
		}

		setEventTypeImg(mType);

		if (mRemind) {
			remindImg.setImageResource(R.drawable.schedule_new_remind_select);
			Log.d(TAG, "=------------startTime------");
		}
		alarmPopwindow.setRemind(mRemind, remindCycle, weekType, mStageRemind,
				startTime, endTime);

	}

	public void readDb() {
		Log.d(TAG, "readDb");
		Cursor c =null;
		try {
			c = ServiceManager.getDbManager().queryScheduleById(schedule_id);
			
			if (c.moveToFirst()) {
				scheduleText = c.getString(c
						.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT));
				
				scheduleTime = c.getLong(c
						.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME));
				
				mShare = c.getInt(c
						.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_SHARE)) == 1;
				
				mType = c.getInt(c
						.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_TYPE));
				
				mRemind = c
						.getInt(c
								.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_FLAG)) == 1;
				remindCycle = c
						.getString(c
								.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD));
				flagAlarm = c.getLong(c
						.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_ALARM_FLAG));
				weekType = c
						.getString(c
								.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_WEEK));
				mStageRemind = c
						.getInt(c
								.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_STAGE_FLAG)) == 1;
				
				startTime = c.getLong(c
						.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME));
				
				Log.i(TAG,
						" read form database startTime = "
								+ DateTimeUtils.time2String("yyyy-MM-dd-HH-mm",
										startTime));
				
				endTime = Long
						.parseLong(c.getString(c
								.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_END)));
				
			}
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(),e);
		}finally{
			if(c!=null){
				c.close();
			}
		}

	}

	public void setDisplayTime(long displaytime) {
		String amORpm = DateTimeUtils.time2String("a", displaytime);
		if (amORpm.equals("上午")) {
			amORpm = "AM";
		} else if (amORpm.equals("下午")) {
			amORpm = "PM";
		}
		String time = amORpm + " "
				+ DateTimeUtils.time2String("hh:mm", displaytime);
		String monthday = DateTimeUtils.time2String("M月d日", displaytime);
		String year = DateTimeUtils.time2String("y", displaytime);

		mScheduleTimeTv.setText(time);
		mScheduleMonthTv.setText(monthday);
		mScheduleYearTv.setText(year);
	}

	@Override
	public void onClick(View v) {
		try {
			if (v.getId() == backBtn.getId()) {
				
				checkQuit();
				
			} else if (v.getId() == saveBtn.getId()) {
				oper_flag = DatabaseHelper.SCHEDULE_OPER_MODIFY;
				scheduleText = schedule_text.getText().toString();
				// 日程内容为空，则提示用户
				if ("".equals(scheduleText.toString().trim())) {
					Toast.makeText(EditScheduleScreen.this, "内容不能为空",
							Toast.LENGTH_SHORT).show();
				} else {
					// 删除之前的闹钟
					// cancelAlarm();
					
					// 更新到数据库
					updateScheduleToDb();
					this.finish();
				}
				
			} else if (v.getId() == share.getId()) {
				if (mShare == false && ServiceManager.getUserId() == 0) {
					Toast.makeText(EditScheduleScreen.this, "请登录以后再分享日程",
							Toast.LENGTH_SHORT).show();
					return;
				}
				// 判断时间是否可以分享，如果大于当前时间则可以分享
				if (mSelectTime < System.currentTimeMillis()) {
					// 提示不能分享；
					Toast.makeText(EditScheduleScreen.this, "不能分享过去事件",
							Toast.LENGTH_SHORT).show();
				} else {
					if (mShare == false) {
						mShare = true;
						shareImg.setImageResource(R.drawable.schedule_new_share_select);
						
					} else if (mShare == true) {
						mShare = false;
						shareImg.setImageResource(R.drawable.schedule_new_share);
					}
				}
			} else if (v.getId() == remind.getId()) {
				
				alarmPopwindow.show(v);
				
			} else if (v.getId() == event.getId()) {
				
				if (eventTypeDialog.isShowing()) {
					Log.d("eventTypeDialog", "---------showing");
					eventTypeDialog.cancel();
				} else {
					int y = schedule_top.getHeight()
							+ event_addtion_linear.getHeight() + screenHeight / 8
							/ 2 - screenHeight / 2;
					eventTypeDialog.setPosition(0, y);
					eventTypeDialog.setCanceledOnTouchOutside(true);
					eventTypeDialog.show();
					
				}
				
			} else if (v.getId() == dateView.getId()) {
				// 启动时间选择器
				timeselectordialog.setCurrentItem(startTime);
				timeselectordialog.show();
				
			}
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(),e);
		}

	}

	private void checkQuit() {

//		if (!"".equals(schedule_text.getText().toString().trim())) {

			new AlertDialog.Builder(EditScheduleScreen.this)
					.setMessage("是否放弃修改？")
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int whichButton) {

									dialog.dismiss();
								}
							})
					.setPositiveButton("确认",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int whichButton) {
									oper_flag = DatabaseHelper.SCHEDULE_OPER_NOTHING;
									finish();
									
								}
							}).show();
//		} else {
//
//			finish();
//		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			checkQuit();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	// 更新数据库
	public void updateScheduleToDb() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					ContentValues cv = new ContentValues();
					
					mStageRemind = alarmPopwindow.getStageRemind();
					mStageRemind = alarmPopwindow.getStageRemind();
					if (mStageRemind) {
						
						startTime = alarmPopwindow.getStartTime();
						Log.i(TAG,
								" insert database startTime = "
										+ DateTimeUtils.time2String(
												"yyyy-MM-dd-HH-mm", startTime));
						endTime = alarmPopwindow.getEndTime();
						
					} else {
						if (mSelectTime != 0) {
							startTime = mSelectTime;
						}
						
						Calendar mCalendar = Calendar.getInstance();
						mCalendar.set(Calendar.YEAR, 2049);
						endTime = mCalendar.getTimeInMillis();
					}
					mRemind = alarmPopwindow.getRemind();
					
					weekType = alarmPopwindow.getWeekValue();
					remindCycle = alarmPopwindow.getRepeatType();
					mType = eventTypeDialog.getEventType();
					cv.put(DatabaseHelper.COLUMN_SCHEDULE_USER_ID,
							ServiceManager.getUserId());
					cv.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE, mType);
					cv.put(DatabaseHelper.COLUMN_SCHEDULE_SHARE, mShare);
					cv.put(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG, oper_flag);
					cv.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME, startTime);
					
					Calendar beginTime = Calendar.getInstance(Locale.CHINA);
					beginTime.setTimeInMillis(startTime);
					beginTime.set(Calendar.HOUR_OF_DAY, 0);
					beginTime.set(Calendar.MINUTE, 0);
					beginTime.set(Calendar.SECOND, 0);
					beginTime.set(Calendar.MILLISECOND, 0);
					cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_BEGIN,
							beginTime.getTimeInMillis());
					
					cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_FLAG, mRemind);
					cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD,
							remindCycle);
					cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_STAGE_FLAG,
							mStageRemind);
					// 主贴
					cv.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER, 0);
					cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_END, endTime);
					cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_WEEK, weekType);
					if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_MONTH
							.equals(remindCycle)) {
						monthday = DateTimeUtils.time2String("d", startTime);
					}
					
					cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_MONTHDAY, monthday);
					if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_YEAR
							.equals(remindCycle)) {
						yearday = DateTimeUtils.time2String("M.d", startTime);
					}
					cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_YEARDAY, yearday);
					cv.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT, scheduleText);
					
					// 将修改的数据插入到数据库中
					ServiceManager.getDbManager().updateScheduleById(schedule_id,
							cv);
					
					if (mRemind) {
						long time = DateTimeUtils.getNextAlarmTime(mStageRemind,
								startTime, endTime, startTime, remindCycle,
								weekType);
						System.out.println("result:"
								+ DateTimeUtils.time2String("yyyy-MM-dd HH:mm:ss",
										time));
						if (time != 0) {
							DateTimeUtils.sendAlarm(time, flagAlarm, schedule_id);
							ScheduleApplication.LogD(
									EditScheduleScreen.class,
									" set alarm = "
											+ DateTimeUtils.time2String(
													"yyyy-MM-dd-HH-mm", time));
						} else {
							DateTimeUtils.cancelAlarm(schedule_id);
						}
					} else {
						DateTimeUtils.cancelAlarm(schedule_id);
					}
					
					si.uploadSchedule("0", "1");
					
//				Intent intent = new Intent();
//				intent.setAction("android.appwidget.action.LOCAL_SCHEDULE_UPDATE");
//				EditScheduleScreen.this.sendBroadcast(intent);
					
					
					ServiceManager.sendBroadcastForUpdateSchedule(EditScheduleScreen.this);
				} catch (Exception e) {
					ScheduleApplication.logException(getClass(),e);
				}
				
			}
		}).start();
	}

	public long getStageTime(long time) {

		Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		mCalendar.set(Calendar.HOUR_OF_DAY,
				Integer.valueOf(DateTimeUtils.time2String("H", startTime)));
		mCalendar.set(Calendar.MINUTE,
				Integer.valueOf(DateTimeUtils.time2String("m", startTime)));
		mCalendar.set(Calendar.SECOND, 0);
		mCalendar.set(Calendar.MILLISECOND, 0);
		long mTime = mCalendar.getTimeInMillis();
		return mTime;

	}

	// 监听时间选择器的“完成”按钮事件
	class TimeSelectorOkListener implements OnOkButtonClickListener {

		public void onOkButtonClick(TimeSelectorDialog timeSelectorDialog) {

			// 获取时间选择器的值
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, Constant.VARY_YEAR);
			c.set(Calendar.MONTH, Constant.VARY_MONTH - 1);
			c.set(Calendar.DAY_OF_MONTH, Constant.VARY_DAY);
			c.set(Calendar.HOUR_OF_DAY, Constant.VARY_HOUR);
			Log.i(TAG, "---------HOUR=" + c.get(Calendar.HOUR_OF_DAY));
			c.set(Calendar.MINUTE, Constant.VARY_MIN);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			mSelectTime = c.getTimeInMillis();
			setDisplayTime(mSelectTime);
			alarmPopwindow.setStartTime(mSelectTime);

			eventService.onUpdateEvent(new EventArgs(
					EventTypes.LOCAL_SCHEDULE_UPDATE));

		}
	}

	class RemindSelectListner implements OnRemindSelectListener {

		@Override
		public void onRemindSelect(AlarmPopwindow alarmPopupWindow) {
			// TODO Auto-generated method stub
			remindImg.setImageResource(R.drawable.schedule_new_remind_select);
		}

		@Override
		public void onRemindUnSelect(AlarmPopwindow alarmPopupWindow) {
			// TODO Auto-generated method stub
			remindImg.setImageResource(R.drawable.schedule_new_remind);
		}

		@Override
		public void onDissmissListener(AlarmPopwindow alarmPopupWindow) {
			// TODO Auto-generated method stub
			remind_selector.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onShowListener(AlarmPopwindow alarmPopupWindow) {
			// TODO Auto-generated method stub
			remind_selector.setVisibility(View.VISIBLE);
		}

	}

	class EventTypeSelectListner implements OnEventTypeSelectListener {

		@Override
		public void OnEventTypeSelect(EventTypeDialog eventTypeDialog, int mType) {
			// TODO Auto-generated method stub
			setEventTypeImg(mType);
		}

		@Override
		public void onDismissListener(EventTypeDialog eventTypeDialog) {
			// TODO Auto-generated method stub
			event_selector.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onShowListener(EventTypeDialog eventTypeDialog) {
			// TODO Auto-generated method stub
			event_selector.setVisibility(View.VISIBLE);
		}

	}

	private void setEventTypeImg(int mType) {
		if(eventImg!=null){
			switch (mType) {
				case -1 :
					eventImg.setImageResource(R.drawable.schedule_new_add_event);
					break;
					
				case 1 :
					eventImg.setImageResource(R.drawable.schedule_new_active);
					break;
				case 2 :
					eventImg.setImageResource(R.drawable.schedule_new_appointment);
					break;
				case 3 :
					eventImg.setImageResource(R.drawable.schedule_new_travel);
					break;
				case 4 :
					eventImg.setImageResource(R.drawable.schedule_new_entertainment);
					break;
				case 5 :
					eventImg.setImageResource(R.drawable.schedule_new_eat);
					break;
				case 6 :
					eventImg.setImageResource(R.drawable.schedule_new_work);
					break;
			}
		}
	}
}
