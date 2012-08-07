package com.archermind.schedule.Screens;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.archermind.schedule.R;
import com.archermind.schedule.Adapters.EventTypeItemAdapter;
import com.archermind.schedule.Dialog.SimpleTimeSelectorDialog;
import com.archermind.schedule.Dialog.TimeSelectorDialog;
import com.archermind.schedule.Dialog.SimpleTimeSelectorDialog.SimpleOnOkButtonClickListener;
import com.archermind.schedule.Dialog.TimeSelectorDialog.OnOkButtonClickListener;
import com.archermind.schedule.Model.EventTypeItem;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Screens.NewScheduleScreen.SimpleTimeSelectorOkListener;
import com.archermind.schedule.Screens.NewScheduleScreen.SimpleTimeSelectorOkListenerend;
import com.archermind.schedule.Screens.NewScheduleScreen.TimeSelectorOkListener;
import com.archermind.schedule.Services.AlarmRecevier;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Utils.ServerInterface;
import com.archermind.schedule.Views.ScheduleEditText;

public class EditScheduleScreen extends Screen implements OnClickListener {
	/** Called when the activity is first created. */
	private static String TAG = "EditScheduleScreen";
	private GridView gridview;
	private View dateView, share, remind, important;
	private ImageView shareImg, remindImg, improtantImg;
	private ImageView img_selector;
	private ScheduleEditText schedule_text;
	private TextView mScheduleMonthTv, mScheduleDayTv, mScheduleWeekTv,
			mScheduleTimeTv;
	private EventTypeItemAdapter adapter;
	private String[] titles;

	private int mCurSelectorIndex = 0;
	private ArrayList<EventTypeItem> eventItems;

	private int[] images = new int[] { R.drawable.schedule_new_notice,
			R.drawable.schedule_new_active,
			R.drawable.schedule_new_appointment,
			R.drawable.schedule_new_travel,
			R.drawable.schedule_new_entertainment, R.drawable.schedule_new_eat,
			R.drawable.schedule_new_work

	};

	private View remind_root_view;
	private LayoutInflater inflater;
	private LinearLayout time_remind_linear, repeat_remind_linear,
			stage_remind_linear;
	private Button time_remind_option, repeat_remind_option,
			stage_remind_option;
	private Boolean time_remind_flag = true, repeat_remind_flag = false,
			stage_remind_flag = false;
	View repeat_remind_view;
	private PopupWindow popupWindow;
	private int screenWidth, screenHeight;
	private LinearLayout event_addtion_linear;
	private RelativeLayout event_item_relative;
	private Button remind_ok, remind_cancel;
	private RadioGroup time_remind_radiogroup;
	private RadioButton time_remind_on_time, time_remind_five_min,
			time_remind_fifteen_min, time_remind_one_hour;
	private RadioButton repeat_remind_none, repeat_remind_day,
			repeat_remind_month, repeat_remind_year;
	private CheckBox mMonday, mTuesday, mWednesday, mThursday, mFriday,
			mSaturday, mSunday;
	private CheckBox stage_remind_checkbox;
	private TextView stage_remind_start_date, stage_remind_end_date;
	String text, text1;
	private int temp = -1;
	// private List<HashMap<String, Object>> remind_radiogroup;
	private Button saveBtn, backBtn, deleteBtn;
	private NotificationManager mNotificationManager;
	private Notification mNotification;
	private Intent mIntent;
	private PendingIntent mPendingIntent;
	private boolean mShare = false;
	private boolean mImportant = false;
	private boolean mRemind = false;
	private int mType;
	private long aHeadTime;
	private long scheduleTime;
	private String oper_flag = "N";
	private String remindCycle = "0";
	private String weekType = "";
	private Calendar mCalendar = Calendar.getInstance();
	// private int currentYear,currentMonth, currentDay, currentWeek,
	// currentTime;
	private ServerInterface si = new ServerInterface();
	private int schedule_id = 1;
	private boolean firstFlag = false;
	private String schedule_Content;
	private int[] weekvalue = new int[7];
	private TimeSelectorDialog timeselectordialog;
	private TimeSelectorOkListener mTimeSelectorOkListener = new TimeSelectorOkListener();
	private SimpleTimeSelectorDialog mSimpleTimeSelectorDialog;
	private SimpleTimeSelectorDialog mSimpleTimeSelectorDialogend;
	private SimpleTimeSelectorOkListener mSimpleTimeSelectorOkListener = new SimpleTimeSelectorOkListener();
    private SimpleTimeSelectorOkListenerend mSimpleTimeSelectorOkListenerend =new SimpleTimeSelectorOkListenerend();
	private long displayTime;
	private long flagAlarm;
	private AlarmManager am;
	private long startTime = 0, endTime = 0;
	private boolean mStageRemind=false ;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_edit);
		Intent mIntent = new Intent();
		mIntent = getIntent();
		si = ServiceManager.getServerInterface();
		schedule_id = mIntent.getIntExtra("id", 1);
		firstFlag = mIntent.getBooleanExtra("first", false);		
		scheduleTime = mIntent.getLongExtra("time", 1);
		Log.i(TAG, "------schedule_id=" + schedule_id);
		init();
	}

	public void init() {
		saveBtn = (Button) findViewById(R.id.schedule_edit_save);
		saveBtn.setOnClickListener(this);
		backBtn = (Button) findViewById(R.id.schedule_edit_back);
		backBtn.setOnClickListener(this);
		deleteBtn = (Button) findViewById(R.id.schedule_edit_delete);
		deleteBtn.setOnClickListener(this);
		share = findViewById(R.id.schedule_new_share);
		share.setOnClickListener(this);
		shareImg = (ImageView) findViewById(R.id.schedule_new_share_img);
		remind = findViewById(R.id.schedule_new_remind);
		remind.setOnClickListener(this);
		remindImg = (ImageView) findViewById(R.id.schedule_new_remind_img);
		important = findViewById(R.id.schedule_new_important);
		important.setOnClickListener(this);
		improtantImg = (ImageView) findViewById(R.id.schedule_new_important_img);
		dateView = findViewById(R.id.schedule_new_date);
		dateView.setOnClickListener(this);
		mScheduleMonthTv = (TextView) findViewById(R.id.schedule_new_month);
		mScheduleDayTv = (TextView) findViewById(R.id.schedule_new_day);
		mScheduleWeekTv = (TextView) findViewById(R.id.schedule_new_week);
		mScheduleTimeTv = (TextView) findViewById(R.id.schedule_new_time);

		img_selector = (ImageView) findViewById(R.id.img_selector);
		gridview = (GridView) findViewById(R.id.editgridview);

		titles = this.getResources().getStringArray(R.array.schedule);
		eventItems = new ArrayList<EventTypeItem>();
		for (int i = 0; i < titles.length; i++) {
			eventItems.add(new EventTypeItem(titles[i], images[i]));
			// Log.i("newshedule",
			// "-------eventitems["+i+"]="+eventItems.get(i).getImageId());
		}
		adapter = new EventTypeItemAdapter(eventItems, this);
		gridview.setAdapter(adapter);

		// 向edittext中添加图片
		schedule_text = (ScheduleEditText) findViewById(R.id.schedule_note);
        
		
		//查询数据库，获得该日程的相关信息
		Cursor c = ServiceManager.getDbManager().queryScheduleById(schedule_id);
		if (c.moveToFirst()){
			schedule_Content = c.getString(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT));
		    scheduleTime = c.getLong(c
				.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME));
//		    mRemind
		    mShare = c.getInt(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_SHARE)) ==1 ;
		    mImportant = c.getInt(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_IMPORTANT)) ==1;
		   
		    remindCycle= c.getString(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD));
		    
		    weekType = c.getString(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD));
		    
		    startTime = Integer.parseInt(c.getString(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_START)));
		    
		    endTime =   Integer.parseInt(c.getString(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_START)));
		    
		    mType = c.getInt(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_TYPE));
		}
			
		schedule_text.setText(schedule_Content);
		c.close();

		setDisplayTime(scheduleTime);
		
		
		// ImageGetter imageGetter = new ImageGetter() {
		// public Drawable getDrawable(String source) {
		// int id = Integer.parseInt(source);
		// Drawable d = getResources().getDrawable(id);
		// d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		// return d;
		// }
		// };
		// schedule_text
		// .append(Html.fromHtml("<img src='"
		// + R.drawable.schedule_new_text_edit + "'/>",
		// imageGetter, null));

		// 动态获取高度和分辨率
		event_item_relative = (RelativeLayout) findViewById(R.id.event_item);
		event_addtion_linear = (LinearLayout) findViewById(R.id.event_addtion);

		Display display = getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();

		// 设置edittext的高度
		ScheduleEditText.initNoteHight = screenWidth;

		gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				img_selector.setVisibility(View.VISIBLE);
				moveTopSelect(position);
				switch (position) {
				case 0:
					mType = DatabaseHelper.SCHEDULE_EVENT_TYPE_NOTICE;
					break;
				case 1:
					mType = DatabaseHelper.SCHEDULE_EVENT_TYPE_ACTIVE;
					break;
				case 2:
					mType = DatabaseHelper.SCHEDULE_EVENT_TYPE_APPOINTMENT;
					break;
				case 3:
					mType = DatabaseHelper.SCHEDULE_EVENT_TYPE_TRAVEL;
					break;
				case 4:
					mType = DatabaseHelper.SCHEDULE_EVENT_TYPE_ENTERTAINMENT;
					break;
				case 5:
					mType = DatabaseHelper.SCHEDULE_EVENT_TYPE_EAT;
					break;
				case 6:
					mType = DatabaseHelper.SCHEDULE_EVENT_TYPE_WORK;
					break;
				}
			}
		});
		timeselectordialog = new TimeSelectorDialog(this);
		timeselectordialog.setOnOkButtonClickListener(mTimeSelectorOkListener);

		mSimpleTimeSelectorDialog = new SimpleTimeSelectorDialog(this);
		mSimpleTimeSelectorDialog
				.setOnOkButtonClickListener(mSimpleTimeSelectorOkListener);
		mSimpleTimeSelectorDialogend = new SimpleTimeSelectorDialog(this);
		mSimpleTimeSelectorDialogend
				.setOnOkButtonClickListener(mSimpleTimeSelectorOkListenerend);
		
		
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
	}

	public void setDisplayTime(long displaytime) {
		String amORpm = DateTimeUtils.time2String("a", displaytime);
		if(amORpm.equals("上午")){
			amORpm="AM";
		}else if(amORpm.equals("下午")){
			amORpm="PM";		
		}
		String time = amORpm + " "
				+ DateTimeUtils.time2String("hh:mm", displaytime);
		String month = DateTimeUtils.time2String("M", displaytime);
		String day = DateTimeUtils.time2String("d", displaytime);
		String week = DateTimeUtils.time2String("E", displaytime);
		// week = weekConvert(week);
		mScheduleTimeTv.setText(time);
		mScheduleMonthTv.setText(month);
		mScheduleDayTv.setText(day);
		mScheduleWeekTv.setText(week);
	}


	public String weekConvert(String week) {
		String displayweek = "";
		switch (Integer.parseInt(week)) {
		case 1:
			displayweek = getResources().getString(R.string.sunday);
			break;
		case 2:
			displayweek = getResources().getString(R.string.monday);
			break;
		case 3:
			displayweek = getResources().getString(R.string.tuesday);
			break;
		case 4:
			displayweek = getResources().getString(R.string.wednesday);
			break;
		case 5:
			displayweek = getResources().getString(R.string.thursday);
			break;
		case 6:
			displayweek = getResources().getString(R.string.friday);
			break;
		case 7:
			displayweek = getResources().getString(R.string.saturday);
			break;
		}
		return displayweek;
	}

	public void showRemindWindow(View parent) {
		RepeatListener repeatListener = new RepeatListener();
		WeekListener weekListener = new WeekListener();
		RemindTitleListener titleListener = new RemindTitleListener();
		if (popupWindow == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			remind_root_view = layoutInflater.inflate(
					R.layout.schedule_alarm_remind, null);
			int popwindowheight = screenHeight
					- event_addtion_linear.getHeight()
					- event_item_relative.getHeight();
			int popwindowidth = screenWidth;
			// Log.i("equalizer",
			// "-----------------popwindowheight:"+popwindowheight);
			popupWindow = new PopupWindow(remind_root_view, popwindowidth,
					popwindowheight);
		}
		// popupWindow.setAnimationStyle(R.style.AnimationPreview);
		popupWindow.setFocusable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		int xoff = popupWindow.getWidth();
		popupWindow.showAsDropDown(parent, -xoff, 0);
		// popupWindow.showAtLocation(parent,Gravity.CENTER, 0, 0);
		// Log.i("ScreenEqualizer",
		// "------------------------------------------show");

		time_remind_linear = (LinearLayout) remind_root_view
				.findViewById(R.id.time_remind_content);
		repeat_remind_linear = (LinearLayout) remind_root_view
				.findViewById(R.id.repeat_remind_content);
		stage_remind_linear = (LinearLayout) remind_root_view
				.findViewById(R.id.stage_remind_content);

		time_remind_option = (Button) remind_root_view
				.findViewById(R.id.time_remind_option);
		repeat_remind_option = (Button) remind_root_view
				.findViewById(R.id.repeat_remind_option);
		stage_remind_option = (Button) remind_root_view
				.findViewById(R.id.stage_remind_option);
		// 提醒，重复提醒，阶段提醒控件的展开和收起
		time_remind_option.setOnClickListener(titleListener);
		repeat_remind_option.setOnClickListener(titleListener);
		stage_remind_option.setOnClickListener(titleListener);

		time_remind_radiogroup = (RadioGroup) remind_root_view
				.findViewById(R.id.time_remind_group);
		time_remind_on_time = (RadioButton) remind_root_view
				.findViewById(R.id.time_remind_on_time);
		time_remind_five_min = (RadioButton) remind_root_view
				.findViewById(R.id.time_remind_five_min);
		time_remind_fifteen_min = (RadioButton) remind_root_view
				.findViewById(R.id.time_remind_fifteen_min);
		time_remind_one_hour = (RadioButton) remind_root_view
				.findViewById(R.id.time_remind_one_hour);

		repeat_remind_day = (RadioButton) remind_root_view
				.findViewById(R.id.repeat_remind_day);
		repeat_remind_none = (RadioButton) remind_root_view
				.findViewById(R.id.repeat_remind_none);
		repeat_remind_month = (RadioButton) remind_root_view
				.findViewById(R.id.repeat_remind_month);
		repeat_remind_year = (RadioButton) remind_root_view
				.findViewById(R.id.repeat_remind_year);

		repeat_remind_none.setOnCheckedChangeListener(repeatListener);
		repeat_remind_day.setOnCheckedChangeListener(repeatListener);
		repeat_remind_month.setOnCheckedChangeListener(repeatListener);
		repeat_remind_year.setOnCheckedChangeListener(repeatListener);

		mMonday = (CheckBox) remind_root_view
				.findViewById(R.id.repeat_remind_monday);
		mTuesday = (CheckBox) remind_root_view
				.findViewById(R.id.repeat_remind_tuesday);
		mWednesday = (CheckBox) remind_root_view
				.findViewById(R.id.repeat_remind_wednesday);
		mThursday = (CheckBox) remind_root_view
				.findViewById(R.id.repeat_remind_thursday);
		mFriday = (CheckBox) remind_root_view
				.findViewById(R.id.repeat_remind_friday);
		mSaturday = (CheckBox) remind_root_view
				.findViewById(R.id.repeat_remind_saturday);
		mSunday = (CheckBox) remind_root_view
				.findViewById(R.id.repeat_remind_sunday);

		setWeekDayFalse();
		
		mMonday.setOnCheckedChangeListener(weekListener);
		mTuesday.setOnCheckedChangeListener(weekListener);
		mWednesday.setOnCheckedChangeListener(weekListener);
		mThursday.setOnCheckedChangeListener(weekListener);
		mFriday.setOnCheckedChangeListener(weekListener);
		mSaturday.setOnCheckedChangeListener(weekListener);
		mSunday.setOnCheckedChangeListener(weekListener);
		
		stage_remind_checkbox = (CheckBox) remind_root_view
				.findViewById(R.id.stage_remind_checkbox);
		stage_remind_checkbox.setEnabled(false);
		stage_remind_start_date = (TextView) remind_root_view
				.findViewById(R.id.stage_remind_start_date);
		stage_remind_end_date = (TextView) remind_root_view
				.findViewById(R.id.stage_remind_end_date);
		stage_remind_start_date.setEnabled(false);
		stage_remind_end_date.setEnabled(false);

		// 此时重复提醒默认为无，设置阶段提醒开始时间,结束时间显默认显示新建日程时间,；
		if(DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_NONE.equals(remindCycle)){
			startTime = scheduleTime;
			stage_remind_start_date.setText(DateTimeUtils.time2String("yyyy-MM-dd",
					startTime));
			stage_remind_end_date.setText(" ");

		}
		
		remind_ok = (Button) remind_root_view.findViewById(R.id.remind_ok);
		remind_cancel = (Button) remind_root_view
				.findViewById(R.id.remind_cancel);

		remind_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Toast.makeText(EditScheduleScreen.this, "ok",
				// Toast.LENGTH_SHORT)
				// .show();
				if (popupWindow != null) {
					popupWindow.dismiss();
				}
				mRemind = true;

			}
		});

		remind_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Toast.makeText(EditScheduleScreen.this, "cancel",
				// Toast.LENGTH_SHORT).show();
				if (popupWindow != null) {
					popupWindow.dismiss();
				}

			}
		});

		time_remind_radiogroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub

						if (checkedId == time_remind_on_time.getId()) {
							// text = "on time";
							aHeadTime = 0;
						} else if (checkedId == time_remind_five_min.getId()) {
							// text = "five min";
							aHeadTime = 5;
						} else if (checkedId == time_remind_fifteen_min.getId()) {
							// text = "fifteen mind";
							aHeadTime = 15;
						} else if (checkedId == time_remind_one_hour.getId()) {
							// text = "one hour";
							aHeadTime = 60;
						}

						// Toast.makeText(NewScheduleScreen.this, text,
						// Toast.LENGTH_SHORT).show();
					}
				});

		stage_remind_checkbox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						if (isChecked) {
							mStageRemind = true;
							stage_remind_start_date.setEnabled(true);
							stage_remind_end_date.setEnabled(true);
							
							Calendar mCalendar = Calendar.getInstance();
							mCalendar.setTimeInMillis(scheduleTime);

							if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_DAY
									.equals(remindCycle)) {
								mCalendar.add(Calendar.DAY_OF_MONTH, 1);
							} else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_MONTH
									.equals(remindCycle)) {
								mCalendar.add(Calendar.MONTH, 1);
							} else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_YEAR
									.equals(remindCycle)) {
								mCalendar.add(Calendar.YEAR, 1);
							} else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_WEEK
									.equals(remindCycle)) {
								mCalendar.add(Calendar.WEEK_OF_MONTH, 1);
							}
							startTime = scheduleTime;
							endTime = mCalendar.getTimeInMillis();
							stage_remind_start_date.setText(DateTimeUtils
									.time2String("yyyy-MM-dd", startTime));
							stage_remind_end_date.setText(DateTimeUtils
									.time2String("yyyy-MM-dd", endTime));
						} else {
							mStageRemind = false;
							stage_remind_start_date.setEnabled(false);
							stage_remind_end_date.setEnabled(false);
							stage_remind_start_date.setText(DateTimeUtils
									.time2String("yyyy-MM-dd", startTime));
							stage_remind_end_date.setText(" ");
						}
					}
				});

		stage_remind_start_date.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Toast.makeText(EditScheduleScreen.this,
				// stage_remind_start_date.getText(), Toast.LENGTH_SHORT)
				// .show();
				// 启动时间选择器
				mSimpleTimeSelectorDialog.setCurrentItem(startTime);
				mSimpleTimeSelectorDialog.show();

			}
		});

		stage_remind_end_date.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Toast.makeText(EditScheduleScreen.this,
				// stage_remind_end_date.getText(), Toast.LENGTH_SHORT)
				// .show();
				// 启动时间选择器
				mSimpleTimeSelectorDialogend.setCurrentItem(endTime);
				mSimpleTimeSelectorDialogend.show();
			}
		});
	}

	public void moveTopSelect(int selectIndex) {

		int startLeft = gridview.getChildAt(mCurSelectorIndex).getLeft()
				+ gridview.getChildAt(mCurSelectorIndex).getWidth() / 2
				- img_selector.getWidth() / 2;
		int endLeft = gridview.getChildAt(selectIndex).getLeft()
				+ gridview.getChildAt(selectIndex).getWidth() / 2
				- img_selector.getWidth() / 2;
		TranslateAnimation animation = new TranslateAnimation(startLeft,
				endLeft, 0, 0);
		animation.setDuration(200);
		animation.setFillAfter(true);
		img_selector.bringToFront();
		img_selector.startAnimation(animation);
		mCurSelectorIndex = selectIndex;
		// Log.i("tabtest", "-------------startLeft=" + startLeft + ", endLeft="
		// + endLeft);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == backBtn.getId()) {
			oper_flag = "N";
			this.finish();
		} else if (v.getId() == saveBtn.getId()) {
			oper_flag = "M";

			// 删除之前的闹钟
			cancelAlarm();
			// 将日程更新到数据库中
			updateScheduleToDb();
			// 重新设置闹钟提醒
			scheduleTime = scheduleTime + aHeadTime * 3600 * 1000;
			sendAlarm(scheduleTime);
			this.finish();

		} else if (v.getId() == deleteBtn.getId()) {
			oper_flag = "D";
			
			//取消闹钟
			
            //同步服务器删除日程
			deleteScheduleFromDb();

			this.finish();
		}

		else if (v.getId() == share.getId()) {
			if (mShare == false) {
				mShare = true;
				shareImg.setImageResource(R.drawable.schedule_new_share_select);
			} else if (mShare == true) {
				mShare = false;
				shareImg.setImageResource(R.drawable.schedule_new_share);
			}

		} else if (v.getId() == remind.getId()) {
			// 显示闹钟提醒对话框
			showRemindWindow(v);
			remindImg.setImageResource(R.drawable.schedule_new_remind_select);
		} else if (v.getId() == important.getId()) {
			if (mImportant == false) {
				mImportant = true;
				improtantImg
						.setImageResource(R.drawable.schedule_new_improtant_select);
			} else if (mImportant == true) {
				mImportant = false;
				improtantImg
						.setImageResource(R.drawable.schedule_new_important);
			}

		} else if (v.getId() == dateView.getId()) {
			// 启动时间选择器
			timeselectordialog.setCurrentItem(scheduleTime);
			timeselectordialog.show();

		}
		// Toast.makeText(NewScheduleScreen.this, (CharSequence) v.getTag(),
		// Toast.LENGTH_SHORT).show();
	}

	public void deleteScheduleFromDb() {

//		ContentValues contentvalues = new ContentValues();
//		contentvalues.put("oper_flag", oper_flag);
//		ServiceManager.getDbManager().updateScheduleById((int) schedule_id,
//				contentvalues);
		ServiceManager.getDbManager().deleteLocalSchedules(schedule_id, firstFlag, scheduleTime);
		si.uploadSchedule("0","1");

	}

	public void updateScheduleToDb() {

		Log.d("EditSheduleScreen", "----save to schedule");
		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_USER_ID, 1);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE, mType);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_SHARE, mShare);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_IMPORTANT, mImportant);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG, oper_flag);
		// 此为时间选择器的时间，暂设置为当前时间
		long scheduleTime = System.currentTimeMillis();
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME, scheduleTime);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_TIME, aHeadTime);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD, remindCycle);
		// cv.put(DatabaseHelper.COLUMN_SCHEDULE_UPDATE_TIME, scheduleTime);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_START, startTime);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_END, endTime);
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_STAGE_FLAG, mStageRemind);
		//主贴
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER,0);
		weekType = "1100000";
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_WEEK, weekType);
		String scheduleText = schedule_text.getText().toString();
		cv.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT, scheduleText);
		ServiceManager.getDbManager().updateScheduleById(schedule_id, cv);
		// ServiceManager.getDbManager().insertLocalSchedules(cv);
		si.uploadSchedule("0","1");
	}

	public void cancelAlarm() {
		Cursor c = ServiceManager.getDbManager().queryScheduleById(
				(int) schedule_id);
		if (c.moveToFirst()) {			
			long flagAlarmb = c
					.getLong(c
							.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_FLAG));

			Intent alarmIntent = new Intent(EditScheduleScreen.this,
					AlarmRecevier.class);
			alarmIntent.setAction("" + flagAlarmb);
			alarmIntent.putExtra("schedule_id", schedule_id);
			PendingIntent pi = PendingIntent.getBroadcast(
					EditScheduleScreen.this, 1, alarmIntent, 0);
			am.cancel(pi);
		}
		c.close();

	}

	public void sendAlarm(Long time) {
//		Cursor c = ServiceManager.getDbManager().queryNotOutdateschedule();
//		Log.d(TAG, "-------NOT out date count = " + c.getCount());
//		c.close();
		Intent alarmIntent = new Intent(EditScheduleScreen.this,
				AlarmRecevier.class);
		alarmIntent.setAction("" + flagAlarm);
		alarmIntent.putExtra("schedule_id", schedule_id);
		PendingIntent pi = PendingIntent.getBroadcast(EditScheduleScreen.this,
				1, alarmIntent, 0);

		am.set(AlarmManager.RTC_WAKEUP, time, pi);
	}

	private void setWeekDayFalse() {
		mMonday.setChecked(false);
		mTuesday.setChecked(false);
		mWednesday.setChecked(false);
		mThursday.setChecked(false);
		mFriday.setChecked(false);
		mSaturday.setChecked(false);
		mSunday.setChecked(false);
	}
   
	private int[] parseDateWeeks(String value) {

		int[] weeks = null;
		ArrayList<Integer> arrayList = new ArrayList<Integer>();

		char[] weekString = value.toCharArray();

		for (int i = 0; i < weekString.length; i++) {

			String weekNumber = new String(weekString, i, 1);
			int number = Integer.parseInt(weekNumber);

			if (number == 1) {

				arrayList.add(i + 1);

			} else {

			}
		}

		weeks = new int[arrayList.size()];

		for (int i = 0; i < arrayList.size(); i++) {

			weeks[i] = arrayList.get(i);
		}

		return weeks;
	}
	// 监听时间选择器的“完成”按钮事件
	class TimeSelectorOkListener implements OnOkButtonClickListener {

		public void onOkButtonClick(TimeSelectorDialog timeSelectorDialog) {
			// Constant.

			// 获取时间选择器的值
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, Constant.VARY_YEAR);
			c.set(Calendar.MONTH, Constant.VARY_MONTH - 1);
			Log.i(TAG, "-------MONTH" + Constant.VARY_MONTH);
			c.set(Calendar.DAY_OF_MONTH, Constant.VARY_DAY);
			c.set(Calendar.HOUR_OF_DAY, Constant.VARY_HOUR);
			c.set(Calendar.MINUTE, Constant.VARY_MIN);

			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			scheduleTime = c.getTimeInMillis();
			setDisplayTime(scheduleTime);
			// Log.d(TAG,
			// "----------huuujhhjhj     "+DateTimeUtils.time2String("yyyy-mm-dd",
			// scheduleTime));
		}

	}
	
	class SimpleTimeSelectorOkListener implements SimpleOnOkButtonClickListener {

		@Override
		public void onOkButtonClick(SimpleTimeSelectorDialog timeSelectorDialog) {
			// TODO Auto-generated method stub

			// 获取时间选择器的值
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, Constant.VARY_YEAR);
			c.set(Calendar.MONTH, Constant.VARY_MONTH - 1);
			c.set(Calendar.DAY_OF_MONTH, Constant.VARY_DAY);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			startTime = c.getTimeInMillis();
			stage_remind_start_date.setText(DateTimeUtils.time2String(
					"yyyy-MM-dd", startTime));

		}
	}
	
	class SimpleTimeSelectorOkListenerend implements SimpleOnOkButtonClickListener {

		@Override
		public void onOkButtonClick(SimpleTimeSelectorDialog timeSelectorDialog) {
			// TODO Auto-generated method stub

			// 获取时间选择器的值
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, Constant.VARY_YEAR);
			c.set(Calendar.MONTH, Constant.VARY_MONTH - 1);
			c.set(Calendar.DAY_OF_MONTH, Constant.VARY_DAY);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			endTime = c.getTimeInMillis();
			stage_remind_end_date.setText(DateTimeUtils.time2String(
					"yyyy-MM-dd", endTime));

		}
	}
	
	
	
	
	class RepeatListener implements CompoundButton.OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub

			if (repeat_remind_none.getId() == buttonView.getId()) {
				if (isChecked) {
					remindCycle = DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_NONE;
					repeat_remind_day.setChecked(false);

					repeat_remind_month.setChecked(false);
					repeat_remind_year.setChecked(false);

					stage_remind_checkbox.setChecked(false);
					stage_remind_checkbox.setEnabled(false);

					setWeekDayFalse();
				} else {
					stage_remind_checkbox.setEnabled(true);
				}
			} else if (repeat_remind_day.getId() == buttonView.getId()) {

				if (isChecked) {
					remindCycle = DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_DAY;

					repeat_remind_none.setChecked(false);
					repeat_remind_month.setChecked(false);
					repeat_remind_year.setChecked(false);
					// stage_remind_start_date.getText()

					// stage_remind_start_date.setText(text)

					setWeekDayFalse();

				}

			} else if (repeat_remind_month.getId() == buttonView.getId()) {

				if (isChecked) {
					remindCycle = DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_MONTH;

					repeat_remind_none.setChecked(false);
					repeat_remind_day.setChecked(false);
					repeat_remind_year.setChecked(false);

					setWeekDayFalse();
				}
			} else if (repeat_remind_year.getId() == buttonView.getId()) {
				if (isChecked) {
					remindCycle = DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_YEAR;

					repeat_remind_none.setChecked(false);
					repeat_remind_month.setChecked(false);
					repeat_remind_day.setChecked(false);

					setWeekDayFalse();
				}
			}
			stage_remind_checkbox.setChecked(false);
			Calendar mCalendar = Calendar.getInstance();
			mCalendar.setTimeInMillis(scheduleTime);
			startTime = scheduleTime;
			endTime = mCalendar.getTimeInMillis();
			stage_remind_start_date.setText(DateTimeUtils.time2String(
					"yyyy-MM-dd", startTime));
			stage_remind_end_date.setText(" ");
		}
	}

	class WeekListener implements CompoundButton.OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			// Toast.makeText(EditScheduleScreen.this, "week",
			// Toast.LENGTH_SHORT).show();
			if (isChecked) {
				remindCycle = DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_WEEK;
				repeat_remind_day.setChecked(false);
				repeat_remind_none.setChecked(false);
				repeat_remind_month.setChecked(false);
				repeat_remind_year.setChecked(false);
			}
			if (mMonday.getId() == buttonView.getId()) {
				if (isChecked) {
					weekvalue[0] = 1;
				} else {
					weekvalue[0] = 0;
				}
			} else if (mTuesday.getId() == buttonView.getId()) {
				if (isChecked) {
					weekvalue[1] = 1;
				} else {
					weekvalue[1] = 0;
				}
			} else if (mWednesday.getId() == buttonView.getId()) {
				if (isChecked) {
					weekvalue[2] = 1;
				} else {
					weekvalue[2] = 0;
				}
			} else if (mThursday.getId() == buttonView.getId()) {
				if (isChecked) {

					weekvalue[3] = 1;
				} else {

					weekvalue[3] = 0;
				}
			} else if (mFriday.getId() == buttonView.getId()) {
				if (isChecked) {
					weekvalue[4] = 1;
				} else {
					weekvalue[4] = 0;
				}
			} else if (mSaturday.getId() == buttonView.getId()) {
				if (isChecked) {
					weekvalue[5] = 1;
				} else {
					weekvalue[5] = 0;
				}
			} else if (mSunday.getId() == buttonView.getId()) {
				if (isChecked) {
					weekvalue[6] = 1;
				} else {
					weekvalue[6] = 0;
				}
			}

		}
	}

	class RemindTitleListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			if (v.getId() == time_remind_option.getId()) {
				if (time_remind_flag == false) {
					time_remind_option
							.setBackgroundResource(R.drawable.schedule_alarm_remind_option_select);
					time_remind_linear.setVisibility(View.VISIBLE);
					time_remind_flag = true;
				}

				else {
					time_remind_option
							.setBackgroundResource(R.drawable.schedule_alarm_remind_option_unselect);
					time_remind_linear.setVisibility(View.GONE);
					time_remind_flag = false;
				}

			} else if (v.getId() == repeat_remind_option.getId()) {
				if (repeat_remind_flag == false) {
					repeat_remind_option
							.setBackgroundResource(R.drawable.schedule_alarm_remind_option_select);
					repeat_remind_linear.setVisibility(View.VISIBLE);
					repeat_remind_flag = true;
				}

				else {
					repeat_remind_option
							.setBackgroundResource(R.drawable.schedule_alarm_remind_option_unselect);
					repeat_remind_linear.setVisibility(View.GONE);
					repeat_remind_flag = false;
				}

			} else if (v.getId() == stage_remind_option.getId()) {
				if (stage_remind_flag == false) {
					stage_remind_option
							.setBackgroundResource(R.drawable.schedule_alarm_remind_option_select);
					stage_remind_linear.setVisibility(View.VISIBLE);
					stage_remind_flag = true;
				} else {
					stage_remind_option
							.setBackgroundResource(R.drawable.schedule_alarm_remind_option_unselect);
					stage_remind_linear.setVisibility(View.GONE);
					stage_remind_flag = false;
				}

			}

		}
	}
}
