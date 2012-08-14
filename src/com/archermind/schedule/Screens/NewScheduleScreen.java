package com.archermind.schedule.Screens;

import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.archermind.schedule.R;
import com.archermind.schedule.Dialog.AlarmPopwindow;
import com.archermind.schedule.Dialog.AlarmPopwindow.OnRemindSelectListener;
import com.archermind.schedule.Dialog.EventTypeDialog;
import com.archermind.schedule.Dialog.EventTypeDialog.OnEventTypeSelectListener;
import com.archermind.schedule.Dialog.TimeSelectorDialog;
import com.archermind.schedule.Dialog.TimeSelectorDialog.OnOkButtonClickListener;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Services.AlarmRecevier;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Utils.ServerInterface;
import com.archermind.schedule.Views.ScheduleEditText;

public class NewScheduleScreen extends Screen implements OnClickListener {
	/** Called when the activity is first created. */
	private static String TAG = "NewScheduleScreen";

	private View dateView, share, remind, event;
	private ImageView shareImg, remindImg, eventImg;
	private ScheduleEditText schedule_text;
	private TextView mScheduleMonthTv, mScheduleYearTv, mScheduleTimeTv;
	private Button saveBtn, backBtn;
	private LinearLayout event_addtion_linear;
	private ImageView remind_selector;
	private ImageView event_selector;
	private LinearLayout schedule_top;

	private boolean mStageRemind = false;
	private boolean mShare = false;
	private boolean mImportant = false;
	private boolean mRemind = false;
	private int mType = -1;
	private long startTime = 0;
	private long endTime = 0;
	private String oper_flag = "N";
	private String remindCycle = "0";
	private String weekType = " ";
	private String monthday = " ";
	private String yearday = " ";
	private long flagAlarm;

	private boolean remind_flag = false;
	private int bottom;
	private ServerInterface si;
	private TimeSelectorDialog timeselectordialog;
	private AlarmPopwindow alarmPopwindow;
	private EventTypeDialog eventTypeDialog;
	private long schedule_id = 1;
	private String scheduleText;
	private int screenWidth, screenHeight;
	private TimeSelectorOkListener mTimeSelectorOkListener = new TimeSelectorOkListener();
	private RemindSelectListner mRemindSelectListner = new RemindSelectListner();

	private EventTypeSelectListner mEventTypeSelectListner = new EventTypeSelectListner();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		si = ServiceManager.getServerInterface();
		setContentView(R.layout.schedule_new);

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

		mScheduleYearTv = (TextView) findViewById(R.id.schedule_new_year);
		mScheduleMonthTv = (TextView) findViewById(R.id.schedule_new_month_day);
		mScheduleTimeTv = (TextView) findViewById(R.id.schedule_new_time);

		Calendar mCalendar = Calendar.getInstance();
		long currentTime = mCalendar.getTimeInMillis();
		long calendarTime = ScheduleScreen.getDateMill();
		// 进入新建日程的时间,默认显示当前时间（hh mm）,日期为日历日期
		mCalendar.set(Calendar.YEAR,
				Integer.parseInt(DateTimeUtils.time2String("y", calendarTime)));
		mCalendar.set(Calendar.MONTH,
				Integer.parseInt(DateTimeUtils.time2String("M", calendarTime))-1);
		mCalendar.set(Calendar.DAY_OF_MONTH,
				Integer.parseInt(DateTimeUtils.time2String("d", calendarTime)));
		mCalendar.set(Calendar.HOUR_OF_DAY,
				Integer.parseInt(DateTimeUtils.time2String("h", currentTime)));
		mCalendar.set(Calendar.MINUTE,
				Integer.parseInt(DateTimeUtils.time2String("m", currentTime)));
		mCalendar.set(Calendar.SECOND, 0);
		mCalendar.set(Calendar.MILLISECOND, 0);
		startTime = mCalendar.getTimeInMillis();
		setDisplayTime(calendarTime, startTime);

		// 结束时间默认为无穷大
		mCalendar.set(Calendar.YEAR, 2049);
		endTime = mCalendar.getTimeInMillis();

		// 向edittext中添加图片
		schedule_text = (ScheduleEditText) findViewById(R.id.schedule_note);

		Display display = getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();

		// 设置edittext的高度
		ScheduleEditText.initNoteHight = screenHeight;
//		ScheduleEditText.lineColor = 0xc5c5c5;

		timeselectordialog = new TimeSelectorDialog(this);
		timeselectordialog.setOnOkButtonClickListener(mTimeSelectorOkListener);
		alarmPopwindow = new AlarmPopwindow(this, screenHeight);
		alarmPopwindow.setOnRemindSelectListener(mRemindSelectListner);
		alarmPopwindow.setRemind(mRemind, remindCycle, weekType, mStageRemind,
				startTime, endTime);

		eventTypeDialog = new EventTypeDialog(this, R.style.EventTypedialog,
				mType, screenHeight / 8);
		eventTypeDialog.setOnEventTypeSelectListener(mEventTypeSelectListner);


	}

	public void setDisplayTime(long calendarTime, long displaytime) {
		String amORpm = DateTimeUtils.time2String("a", displaytime);
		if (amORpm.equals("上午")) {
			amORpm = "AM";
		} else if (amORpm.equals("下午")) {
			amORpm = "PM";
		}
		String time = amORpm + " "
				+ DateTimeUtils.time2String("hh:mm", displaytime);
		String monthday = DateTimeUtils.time2String("M月d日", calendarTime);

		String year = DateTimeUtils.time2String("y", calendarTime);

		mScheduleTimeTv.setText(time);
		mScheduleMonthTv.setText(monthday);
		mScheduleYearTv.setText(year);
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
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		bottom = remind_selector.getBottom();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == backBtn.getId()) {
			oper_flag = DatabaseHelper.SCHEDULE_OPER_NOTHING;
			this.finish();

		} else if (v.getId() == saveBtn.getId()) {
			oper_flag = DatabaseHelper.SCHEDULE_OPER_ADD;
			scheduleText = schedule_text.getText().toString();
			// 日程内容为空，则提示用户
			if ("".equals(scheduleText.toString().trim())) {
				Toast.makeText(NewScheduleScreen.this, "内容不能为空",
						Toast.LENGTH_SHORT).show();
			} else {

				// 将日程保存到数据库中
				saveScheduleToDb();
				this.finish();
			}

		} else if (v.getId() == share.getId()) {

			// 判断时间是否可以分享，如果大于当前时间则可以分享
			if (startTime < System.currentTimeMillis()) {
				// 提示不能分享；
				Toast.makeText(NewScheduleScreen.this, "不能分享过去事件",
						Toast.LENGTH_SHORT).show();
			} else {
				if (mShare == false) {
					mShare = true;
					shareImg.setImageResource(R.drawable.schedule_new_share_select);

				} else {
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
			Constant.wheelViewFlag = 0;
			timeselectordialog.setCurrentItem(startTime);
			timeselectordialog.show();

		}

	}

	public void saveScheduleToDb() {
		Intent intent = new Intent();
	    intent.setAction("android.appwidget.action.LOCAL_SCHEDULE_UPDATE");  
	    sendBroadcast(intent);
		new Thread(new Runnable() {
			@Override
			public void run() {
				ContentValues cv = new ContentValues();
				startTime = alarmPopwindow.getStartTime();
				endTime = alarmPopwindow.getEndTime();
				mRemind = alarmPopwindow.getRemind();
				mStageRemind = alarmPopwindow.getStageRemind();
				weekType = alarmPopwindow.getWeekValue();
				remindCycle = alarmPopwindow.getRepeatType();
				mType = eventTypeDialog.getEventType();
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE, mType);
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_SHARE, mShare);
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG, oper_flag);
				flagAlarm = System.currentTimeMillis();
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_ALARM_FLAG, flagAlarm);
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME, startTime);

				cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_FLAG, mRemind);
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD,
						remindCycle);
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_STAGE_FLAG,
						mStageRemind);
				// 主贴
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER, 0);
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_END, endTime);
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_FLAG_OUTDATE, false);
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_WEEK, weekType);
				if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_MONTH
						.equals(remindCycle)) {
					monthday = DateTimeUtils.time2String("d", startTime);
				}

				cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_MONTHDAY, monthday);
				if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_YEAR
						.equals(remindCycle)) {
					yearday = DateTimeUtils.time2String("d", startTime);
				}
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_YEARDAY, yearday);
				cv.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT, scheduleText);
				Log.d(TAG, "--------mRemind=" + mRemind);
				Log.d(TAG,
						"--------startTime="
								+ DateTimeUtils.time2String(
										"yyyy-MM-dd-hh-mm-ss", startTime));
				Log.d(TAG,
						"--------endTime="
								+ DateTimeUtils.time2String(
										"yyyy-MM-dd-hh-mm-ss", endTime));

				schedule_id = ServiceManager.getDbManager()
						.insertLocalSchedules(cv, startTime);
				// 闹钟提醒
				// sendAlarm(startTime);
				// 同步新建日程到服务器
				si.uploadSchedule("0", "1");

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

	// 发送闹钟事件
	public void sendAlarm(Long time) {
		// Cursor c = ServiceManager.getDbManager().queryNotOutdateschedule();
		// Log.d(TAG, "-------NOT out date count = " + c.getCount());
		// c.close();
		Intent alarmIntent = new Intent(NewScheduleScreen.this,
				AlarmRecevier.class);
		alarmIntent.setAction("" + flagAlarm);
		alarmIntent.putExtra("schedule_id", schedule_id);
		PendingIntent pi = PendingIntent.getBroadcast(NewScheduleScreen.this,
				1, alarmIntent, 0);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, time, pi);
	}

	// 监听时间选择器的“完成”按钮事件
	class TimeSelectorOkListener implements OnOkButtonClickListener {

		public void onOkButtonClick(TimeSelectorDialog timeSelectorDialog) {
			// Constant.

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
			startTime = c.getTimeInMillis();
			setDisplayTime(startTime);
			alarmPopwindow.setStartTime(startTime);
			// 此时重复提醒默认为无，设置阶段提醒开始时间,结束时间显默认显示新建日程时间,；

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

			switch (mType) {
			case -1:
				eventImg.setImageResource(R.drawable.schedule_new_add_event);
				break;
			case 1:
				eventImg.setImageResource(R.drawable.schedule_new_active);
				break;
			case 2:
				eventImg.setImageResource(R.drawable.schedule_new_appointment);
				break;
			case 3:
				eventImg.setImageResource(R.drawable.schedule_new_travel);
				break;
			case 4:
				eventImg.setImageResource(R.drawable.schedule_new_entertainment);
				break;
			case 5:
				eventImg.setImageResource(R.drawable.schedule_new_eat);
				break;
			case 6:
				eventImg.setImageResource(R.drawable.schedule_new_work);
				break;
			}

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

}
