
package com.archermind.schedule.Dialog;

import java.util.Calendar;
import java.util.HashMap;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import com.archermind.schedule.R;
import com.archermind.schedule.Dialog.SimpleTimeSelectorDialog.SimpleOnOkButtonClickListener;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Screens.NewScheduleScreen;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.DateTimeUtils;

public class AlarmPopwindow implements OnClickListener {

    private PopupWindow alarmPopupWindow;

    // private int screenWidth, screenHeight;

    private RadioButton repeat_remind_none, repeat_remind_day, repeat_remind_month,
            repeat_remind_year;

    private CheckBox mMonday, mTuesday, mWednesday, mThursday, mFriday, mSaturday, mSunday;

    private CheckBox stage_remind_checkbox;

    private View remind_root_view;

    private LinearLayout time_remind_linear, repeat_remind_linear, stage_remind_linear;

    private LinearLayout time_remind_title, repeat_remind_title, stage_remind_title;

    private Button time_remind_option, repeat_remind_option, stage_remind_option;

    private Boolean time_remind_flag = true, repeat_remind_flag = false, stage_remind_flag = false;// 三个阶段的标志

    private TextView stage_remind_start_date, stage_remind_end_date;

    private RadioGroup time_remind_group;

    private RadioButton remind_on, remind_off;

    private SimpleTimeSelectorDialog mSimpleTimeSelectorDialog;

    private SimpleTimeSelectorDialog mSimpleTimeSelectorDialogend;

    private SimpleTimeSelectorOkListener mSimpleTimeSelectorOkListener = new SimpleTimeSelectorOkListener();

    private SimpleTimeSelectorOkListenerend mSimpleTimeSelectorOkListenerend = new SimpleTimeSelectorOkListenerend();

    private RepeatListener repeatListener = new RepeatListener();

    private WeekListener weekListener = new WeekListener();

    private WeekClickListener weekClickListener = new WeekClickListener();

    private boolean mStageRemind;

    private boolean mRemind;

    private String remindCycle = DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_NONE;

    private long startTime;

    private long endTime;

    // private StringBuffer weekType = new StringBuffer();
    private int[] weekvalue = new int[7];

    public interface OnRemindSelectListener {
        void onRemindSelect(AlarmPopwindow alarmPopupWindow);

        void onRemindUnSelect(AlarmPopwindow alarmPopupWindow);

        void onDissmissListener(AlarmPopwindow alarmPopupWindow);

        void onShowListener(AlarmPopwindow alarmPopupWindow);
    }

    private OnRemindSelectListener mOnRemindSelectListener;

    public void setOnRemindSelectListener(OnRemindSelectListener l) {
        mOnRemindSelectListener = l;
    }

    private HashMap<String, String> infoMap = new HashMap<String, String>();

    private Context mContext;

    public AlarmPopwindow(Context context, int popWindowHeight) {

        mContext = context;

        LayoutInflater layoutInflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (alarmPopupWindow == null) {
            remind_root_view = layoutInflater.inflate(R.layout.schedule_alarm_remind, null);
            alarmPopupWindow = new PopupWindow(remind_root_view, LayoutParams.FILL_PARENT,
                    LayoutParams.FILL_PARENT);

        }
        alarmPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                // TODO Auto-generated method stub
                mOnRemindSelectListener.onDissmissListener(AlarmPopwindow.this);
            }
        });

        mSimpleTimeSelectorDialog = new SimpleTimeSelectorDialog(context);
        mSimpleTimeSelectorDialog.setOnOkButtonClickListener(mSimpleTimeSelectorOkListener);
        mSimpleTimeSelectorDialogend = new SimpleTimeSelectorDialog(context);
        mSimpleTimeSelectorDialogend.setOnOkButtonClickListener(mSimpleTimeSelectorOkListenerend);
        init();

    }

    private void init() {

        // 可见，不可见
        time_remind_linear = (LinearLayout)remind_root_view.findViewById(R.id.time_remind_content);
        repeat_remind_linear = (LinearLayout)remind_root_view
                .findViewById(R.id.repeat_remind_content);
        stage_remind_linear = (LinearLayout)remind_root_view
                .findViewById(R.id.stage_remind_content);

        // 点击
        time_remind_title = (LinearLayout)remind_root_view
                .findViewById(R.id.schedule_alarm_time_remind_title);
        repeat_remind_title = (LinearLayout)remind_root_view
                .findViewById(R.id.schedule_alarm_reapeat_remind_title);
        stage_remind_title = (LinearLayout)remind_root_view
                .findViewById(R.id.schedule_alarm_stage_remind_title);

        time_remind_option = (Button)remind_root_view.findViewById(R.id.time_remind_option);
        repeat_remind_option = (Button)remind_root_view.findViewById(R.id.repeat_remind_option);
        stage_remind_option = (Button)remind_root_view.findViewById(R.id.stage_remind_option);

        // 提醒，重复提醒，阶段提醒控件的展开和收起 month in year
        time_remind_title.setOnClickListener(this);
        repeat_remind_title.setOnClickListener(this);
        stage_remind_title.setOnClickListener(this);

        // 是否提醒
        time_remind_group = (RadioGroup)remind_root_view.findViewById(R.id.time_remind_group);
        remind_on = (RadioButton)remind_root_view.findViewById(R.id.time_remind_on);
        remind_off = (RadioButton)remind_root_view.findViewById(R.id.time_remind_off);

        // 重复提醒
        repeat_remind_day = (RadioButton)remind_root_view.findViewById(R.id.repeat_remind_day);
        repeat_remind_none = (RadioButton)remind_root_view.findViewById(R.id.repeat_remind_none);
        repeat_remind_month = (RadioButton)remind_root_view.findViewById(R.id.repeat_remind_month);
        repeat_remind_year = (RadioButton)remind_root_view.findViewById(R.id.repeat_remind_year);

        repeat_remind_none.setOnCheckedChangeListener(repeatListener);
        repeat_remind_day.setOnCheckedChangeListener(repeatListener);
        repeat_remind_month.setOnCheckedChangeListener(repeatListener);
        repeat_remind_year.setOnCheckedChangeListener(repeatListener);

        mMonday = (CheckBox)remind_root_view.findViewById(R.id.repeat_remind_monday);
        mTuesday = (CheckBox)remind_root_view.findViewById(R.id.repeat_remind_tuesday);
        mWednesday = (CheckBox)remind_root_view.findViewById(R.id.repeat_remind_wednesday);
        mThursday = (CheckBox)remind_root_view.findViewById(R.id.repeat_remind_thursday);
        mFriday = (CheckBox)remind_root_view.findViewById(R.id.repeat_remind_friday);
        mSaturday = (CheckBox)remind_root_view.findViewById(R.id.repeat_remind_saturday);
        mSunday = (CheckBox)remind_root_view.findViewById(R.id.repeat_remind_sunday);

        mMonday.setOnCheckedChangeListener(weekListener);
        mTuesday.setOnCheckedChangeListener(weekListener);
        mWednesday.setOnCheckedChangeListener(weekListener);
        mThursday.setOnCheckedChangeListener(weekListener);
        mFriday.setOnCheckedChangeListener(weekListener);
        mSaturday.setOnCheckedChangeListener(weekListener);
        mSunday.setOnCheckedChangeListener(weekListener);

        mMonday.setOnClickListener(weekClickListener);
        mTuesday.setOnClickListener(weekClickListener);
        mWednesday.setOnClickListener(weekClickListener);
        mThursday.setOnClickListener(weekClickListener);
        mFriday.setOnClickListener(weekClickListener);
        mSaturday.setOnClickListener(weekClickListener);
        mSunday.setOnClickListener(weekClickListener);

        stage_remind_checkbox = (CheckBox)remind_root_view.findViewById(R.id.stage_remind_checkbox);

        stage_remind_start_date = (TextView)remind_root_view
                .findViewById(R.id.stage_remind_start_date);
        stage_remind_end_date = (TextView)remind_root_view.findViewById(R.id.stage_remind_end_date);
        stage_remind_start_date.setOnClickListener(this);
        stage_remind_end_date.setOnClickListener(this);

        // if
        // (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_NONE.equals(remindCycle))
        // {
        // setWeekDayFalse();
        // stage_remind_checkbox.setEnabled(false);
        // stage_remind_start_date.setEnabled(false);
        // stage_remind_end_date.setEnabled(false);
        // }

        // 是否提醒
        time_remind_group.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                if (checkedId == remind_on.getId()) {

                    mRemind = true;
                    setRepeatRemindTrue();
                    mOnRemindSelectListener.onRemindSelect(AlarmPopwindow.this);

                    repeat_remind_option
                            .setBackgroundResource(R.drawable.schedule_alarm_remind_option_select);
                    repeat_remind_linear.setVisibility(View.VISIBLE);
                    repeat_remind_flag = true;

                } else if (checkedId == remind_off.getId()) {

                    mRemind = false;
                    Log.d("alarmpopwindow", "---------off_checked");
                    setRepeatRemindfalse();
                    mOnRemindSelectListener.onRemindUnSelect(AlarmPopwindow.this);

                    repeat_remind_option
                            .setBackgroundResource(R.drawable.schedule_alarm_remind_option_unselect);
                    repeat_remind_linear.setVisibility(View.GONE);
                    repeat_remind_flag = false;

                }
            }
        });

        stage_remind_checkbox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // TODO Auto-generated method stub
                        if (isChecked) {
                            mStageRemind = true;
                            stage_remind_start_date.setEnabled(true);
                            stage_remind_end_date.setEnabled(true);

                            Calendar mCalendar = Calendar.getInstance();
                            mCalendar.setTimeInMillis(startTime);

                            if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_DAY.equals(remindCycle)) {
                                mCalendar.add(Calendar.DAY_OF_MONTH, 5);
                            } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_MONTH
                                    .equals(remindCycle)) {
                                mCalendar.add(Calendar.MONTH, 3);
                            } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_YEAR
                                    .equals(remindCycle)) {
                                mCalendar.add(Calendar.YEAR, 2);
                            } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_WEEK
                                    .equals(remindCycle)) {
                                mCalendar.add(Calendar.WEEK_OF_MONTH, 3);
                            }
                            // startTime = scheduleTime;
                            endTime = mCalendar.getTimeInMillis();
                            stage_remind_start_date.setText(DateTimeUtils.time2String("yyyy-MM-dd",
                                    startTime));
                            stage_remind_end_date.setText(DateTimeUtils.time2String("yyyy-MM-dd",
                                    endTime));
                        } else {

                            mStageRemind = false;
                            Log.d("alarmpopwindow", "---------stage checked false");

                            stage_remind_start_date.setEnabled(false);
                            stage_remind_end_date.setEnabled(false);
                            stage_remind_start_date.setText(DateTimeUtils.time2String("yyyy-MM-dd",
                                    startTime));
                            stage_remind_end_date.setText(DateTimeUtils.time2String("yyyy-MM-dd",
                                    startTime));

                        }
                    }
                });
    }

    public void show(View parent) {

        // alarmPopupWindow.setAnimationStyle(R.style.EventdialogWindowAnim);
        alarmPopupWindow.setFocusable(true);
        alarmPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        int xoff = alarmPopupWindow.getWidth();
        alarmPopupWindow.showAsDropDown(parent, -xoff, 0);
        // popupWindow.showAtLocation(parent,Gravity.CENTER, 0, 0);
        // Log.i("ScreenEqualizer",
        // "------------------------------------------show");
        mOnRemindSelectListener.onShowListener(AlarmPopwindow.this);
    }

    public void setStartTime(long startPopTime) {

        if (mStageRemind) {

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(startTime);
            c.set(Calendar.HOUR_OF_DAY,
                    Integer.parseInt(DateTimeUtils.time2String("H", startPopTime)));
            c.set(Calendar.MINUTE, Integer.parseInt(DateTimeUtils.time2String("m", startPopTime)));

            startTime = c.getTimeInMillis();
            Log.i("pop NewScheduleScreen",
                    "true  popSetStartTime = "
                            + DateTimeUtils.time2String("yyyy-MM-dd-HH-mm", startTime));

        } else {

            startTime = startPopTime;
            Log.i("pop NewScheduleScreen",
                    "false  popSetStartTime = "
                            + DateTimeUtils.time2String("yyyy-MM-dd-HH-mm", startTime));
        }

    }

    public void setRemind(boolean mRemind, String remindCycle, String weekType,
            boolean mStageRemind, long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.remindCycle = remindCycle;
        this.mStageRemind = mStageRemind;
        this.mRemind = mRemind;

        if (!mRemind) {
            // 默认为不提醒
            remind_off.setChecked(true);
            setRepeatRemindfalse();
            stage_remind_checkbox.setChecked(false);
            stage_remind_checkbox.setEnabled(false);
            stage_remind_start_date.setEnabled(false);
            stage_remind_end_date.setEnabled(false);
            stage_remind_start_date.setText(DateTimeUtils.time2String("yyyy-MM-dd", startTime));
            stage_remind_end_date.setText(DateTimeUtils.time2String("yyyy-MM-dd", startTime));
        } else {
            remind_on.setChecked(true);
            if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_NONE.equals(remindCycle)) {
                repeat_remind_none.setChecked(true);
                stage_remind_checkbox.setChecked(false);
                stage_remind_checkbox.setEnabled(false);
                stage_remind_start_date.setEnabled(false);
                stage_remind_end_date.setEnabled(false);
                stage_remind_start_date.setText(DateTimeUtils.time2String("yyyy-MM-dd", startTime));
                stage_remind_end_date.setText(DateTimeUtils.time2String("yyyy-MM-dd", startTime));

            } else {
                if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_DAY.equals(remindCycle)) {
                    repeat_remind_day.setChecked(true);
                } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_MONTH.equals(remindCycle)) {
                    repeat_remind_month.setChecked(true);
                } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_WEEK.equals(remindCycle)) {
                    if (!"".equals(weekType)) {
                        setweekvalue(weekType);
                    }
                } else if (DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_YEAR.equals(remindCycle)) {
                    repeat_remind_year.setChecked(true);
                }
                stage_remind_start_date.setText(DateTimeUtils.time2String("yyyy-MM-dd", startTime));
                if (mStageRemind) {
                    stage_remind_checkbox.setChecked(true);
                    stage_remind_end_date.setText(DateTimeUtils.time2String("yyyy-MM-dd", endTime));
                } else {
                    stage_remind_end_date.setText(DateTimeUtils
                            .time2String("yyyy-MM-dd", startTime));
                }

            }

        }

    }

    private void setweekvalue(String weekType) {
        char[] week = weekType.toCharArray();
        for (int i = 0; i < week.length; i++) {
            switch (week[i] - 48) {
                case 1:
                    mMonday.setChecked(true);
                    break;
                case 2:
                    mTuesday.setChecked(true);
                    break;
                case 3:
                    mWednesday.setChecked(true);
                    break;
                case 4:
                    mThursday.setChecked(true);
                    break;
                case 5:
                    mFriday.setChecked(true);
                    break;
                case 6:
                    mSaturday.setChecked(true);
                    break;
                case 7:
                    mSunday.setChecked(true);
                    break;
            }
            // Log.d("ALarmpopWindow", String.valueOf(week[i]));

        }
    }

    public long getStartTime() {

        return startTime;
    }

    public long getEndTime() {

        return endTime;
    }

    public boolean getRemind() {
        return mRemind;
    }

    public String getWeekValue() {
        StringBuffer weekType = new StringBuffer();
        for (int i = 0; i < weekvalue.length; i++) {
            if (weekvalue[i] == 1) {
                weekType.append(i + 1);
            }

        }
        return weekType.toString();
    }

    public String getRepeatType() {

        return remindCycle;
    }

    public boolean getStageRemind() {

        return mStageRemind;
    }

    private void setRepeatRemindfalse() {
        repeat_remind_none.setChecked(true);

        repeat_remind_day.setChecked(false);
        repeat_remind_month.setChecked(false);
        repeat_remind_year.setChecked(false);
        setWeekDayFalse();
        // stage_remind_checkbox.setChecked(false);
        repeat_remind_day.setEnabled(false);
        repeat_remind_month.setEnabled(false);
        repeat_remind_year.setEnabled(false);
        mMonday.setEnabled(false);
        mTuesday.setEnabled(false);
        mWednesday.setEnabled(false);
        mThursday.setEnabled(false);
        mFriday.setEnabled(false);
        mSaturday.setEnabled(false);
        mSunday.setEnabled(false);
        // stage_remind_checkbox.setEnabled(false);

    }

    private void setRepeatRemindTrue() {

        repeat_remind_day.setEnabled(true);
        repeat_remind_month.setEnabled(true);
        repeat_remind_year.setEnabled(true);

        mMonday.setEnabled(true);
        mTuesday.setEnabled(true);
        mWednesday.setEnabled(true);
        mThursday.setEnabled(true);
        mFriday.setEnabled(true);
        mSaturday.setEnabled(true);
        mSunday.setEnabled(true);

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

    private long getStageTime(long time) {

        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(time);
        mCalendar.set(Calendar.HOUR_OF_DAY,
                Integer.valueOf(DateTimeUtils.time2String("H", startTime)));
        mCalendar.set(Calendar.MINUTE, Integer.valueOf(DateTimeUtils.time2String("m", startTime)));
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);
        long mTime = mCalendar.getTimeInMillis();
        return mTime;
    }

    class SimpleTimeSelectorOkListener implements SimpleOnOkButtonClickListener {

        @Override
        public void onOkButtonClick(SimpleTimeSelectorDialog timeSelectorDialog) {
            // TODO Auto-generated method stub

            // 获取开始时间选择器的值
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(startTime);

            c.set(Calendar.YEAR, Constant.VARY_YEAR);
            c.set(Calendar.MONTH, Constant.VARY_MONTH - 1);
            c.set(Calendar.DAY_OF_MONTH, Constant.VARY_DAY);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            startTime = c.getTimeInMillis();

            stage_remind_start_date.setText(DateTimeUtils.time2String("yyyy-MM-dd", startTime));

            Log.i("pop NewScheduleScreen",
                    " select startTime = "
                            + DateTimeUtils.time2String("yyyy-MM-dd-HH-mm", startTime));

        }
    }

    class SimpleTimeSelectorOkListenerend implements SimpleOnOkButtonClickListener {

        public void onOkButtonClick(SimpleTimeSelectorDialog timeSelectorDialog) {

            // 获取结束时间选择器的值
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, Constant.VARY_YEAR);
            c.set(Calendar.MONTH, Constant.VARY_MONTH - 1);
            c.set(Calendar.DAY_OF_MONTH, Constant.VARY_DAY);
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            c.set(Calendar.MILLISECOND, 0);

            endTime = c.getTimeInMillis();
            endTime = getStageTime(endTime);

            if (startTime > endTime) {

                Toast.makeText(mContext, "结束时间不能晚于开始时间，请重新设置！", 1).show();
            } else {

                stage_remind_end_date.setText(DateTimeUtils.time2String("yyyy-MM-dd", endTime));
            }
        }
    }

    class RepeatListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // TODO Auto-generated method stub

            if (repeat_remind_none.getId() == buttonView.getId()) {
                if (isChecked) {

                    remindCycle = DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_NONE;
                    Log.d("alarmpopwindow", "---------none_checked");
                    repeat_remind_day.setChecked(false);
                    repeat_remind_month.setChecked(false);
                    repeat_remind_year.setChecked(false);
                    setWeekDayFalse();
                    stage_remind_checkbox.setChecked(false);
                    stage_remind_checkbox.setEnabled(false);
                    hideStageBar();
                    
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

                    showStageBar();
                    setWeekDayFalse();
                }

            } else if (repeat_remind_month.getId() == buttonView.getId()) {

                if (isChecked) {
                    remindCycle = DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_MONTH;

                    repeat_remind_none.setChecked(false);
                    repeat_remind_day.setChecked(false);
                    repeat_remind_year.setChecked(false);
                    setWeekDayFalse();
                    showStageBar();
                }
            } else if (repeat_remind_year.getId() == buttonView.getId()) {
                if (isChecked) {
                    remindCycle = DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_YEAR;

                    repeat_remind_none.setChecked(false);
                    repeat_remind_month.setChecked(false);
                    repeat_remind_day.setChecked(false);
                    setWeekDayFalse();
                    showStageBar();
                }

            }
            
            stage_remind_checkbox.setChecked(false);
            stage_remind_start_date.setText(DateTimeUtils.time2String("yyyy-MM-dd", startTime));
            stage_remind_end_date.setText(DateTimeUtils.time2String("yyyy-MM-dd", startTime));
        }
    }

    class WeekListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // TODO Auto-generated method stub
            // Toast.makeText(NewScheduleScreen.this, "week",
            // Toast.LENGTH_SHORT)
            // .show();
            if (isChecked) {
                remindCycle = DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_WEEK;
                repeat_remind_day.setChecked(false);
                repeat_remind_none.setChecked(false);
                repeat_remind_month.setChecked(false);
                repeat_remind_year.setChecked(false);
                showStageBar();
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

    class WeekClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (!(mMonday.isChecked() || mTuesday.isChecked() || mWednesday.isChecked()
                    || mThursday.isChecked() || mFriday.isChecked() || mSaturday.isChecked()
                    || mSunday.isChecked() || repeat_remind_none.isChecked()
                    || repeat_remind_day.isChecked() || repeat_remind_month.isChecked() || repeat_remind_year
                        .isChecked())) {

                repeat_remind_none.setChecked(true);

            }
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == time_remind_title.getId()) {

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

        } else if (v.getId() == repeat_remind_title.getId()) {

            if (mRemind) {

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
            }

        } else if (v.getId() == stage_remind_title.getId()) {

            if (!repeat_remind_none.isChecked()) {

                if (stage_remind_flag == false) {

                    showStageBar();

                } else {

                    hideStageBar();

                }
            }

        } else if (v.getId() == stage_remind_end_date.getId()) {

            // 启动时间选择器
            mSimpleTimeSelectorDialogend.setCurrentItem(endTime);
            mSimpleTimeSelectorDialogend.show();
        } else if (v.getId() == stage_remind_start_date.getId()) {

            // 启动时间选择器
            mSimpleTimeSelectorDialog.setCurrentItem(startTime);
            mSimpleTimeSelectorDialog.show();
        }

    }

    private void hideStageBar() {

        stage_remind_option.setBackgroundResource(R.drawable.schedule_alarm_remind_option_unselect);
        stage_remind_linear.setVisibility(View.GONE);
        stage_remind_flag = false;
    }

    private void showStageBar() {

        stage_remind_option.setBackgroundResource(R.drawable.schedule_alarm_remind_option_select);
        stage_remind_linear.setVisibility(View.VISIBLE);
        stage_remind_flag = true;
    }
}
