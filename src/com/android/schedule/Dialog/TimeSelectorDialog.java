package com.android.schedule.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;

import com.android.schedule.ScheduleApplication;
import com.android.schedule.Adapters.NumericWheelAdapter;
import com.android.schedule.Calendar.SpecialCalendar;
import com.android.schedule.Utils.Constant;
import com.android.schedule.Utils.DateTimeUtils;
import com.android.schedule.Views.WheelView;
import com.android.schedule.Views.WheelView.OnWheelChangedListener;
import com.android.schedule.Views.WheelView.OnWheelScrollListener;
import com.archermind.schedule.R;

public class TimeSelectorDialog implements OnClickListener {
	private WheelView wheelView_day;
	private WheelView wheelView_month;
	private WheelView wheelView_year;
	private WheelView wheelView_hour;
	private WheelView wheelView_min;
	private boolean initFinished = false;
	private boolean wheelScrolled = false;
	private Dialog timeSelectorDialog;
	private Context context;
	private Button wheelView_cancel, wheelView_ok;
	private Window window = null;

	public interface OnOkButtonClickListener {
		void onOkButtonClick(TimeSelectorDialog timeSelectorDialog);
	}

	private OnOkButtonClickListener mOnOkButtonClickListener;

	public void setOnOkButtonClickListener(OnOkButtonClickListener l) {
		mOnOkButtonClickListener = l;
	}

	public TimeSelectorDialog(Context context) {
		timeSelectorDialog = new Dialog(context, R.style.CustomDialog);
		timeSelectorDialog.setContentView(R.layout.time_select);
		timeSelectorDialog.setCanceledOnTouchOutside(true);
		init();
	}

	private void init() {
		initWheel(R.id.wheelView_year);
		initWheel(R.id.wheelView_month);
		initWheel(R.id.wheelView_day);
		initWheel(R.id.wheelView_hour);
		initWheel(R.id.wheelView_min);

		wheelView_cancel = (Button) timeSelectorDialog
				.findViewById(R.id.wheelView_cancel);
		wheelView_ok = (Button) timeSelectorDialog
				.findViewById(R.id.wheelView_ok);

		wheelView_cancel.setOnClickListener(this);
		wheelView_ok.setOnClickListener(this);
		
		initFinished = true;
		updateStatus();
	}

	public void setCurrentItem(long time) {

		Constant.YEAR = Integer.parseInt(DateTimeUtils.time2String("y", time));
		Constant.MONTH = Integer.parseInt(DateTimeUtils.time2String("M", time));
		Constant.DAY = Integer.parseInt(DateTimeUtils.time2String("d", time));
		Constant.HOUR = Integer.parseInt(DateTimeUtils.time2String("H", time));
		Constant.MIN = Integer.parseInt(DateTimeUtils.time2String("m", time));
		wheelView_year.setCurrentItem(Constant.YEAR - 1901);
		wheelView_month.setCurrentItem(Constant.MONTH - 1);
		wheelView_day.setCurrentItem(Constant.DAY - 1);
		wheelView_hour.setCurrentItem(Constant.HOUR - 0);
		wheelView_min.setCurrentItem(Constant.MIN - 0);
	}

	private void initWheel(int id) {
		WheelView wheel = getWheel(id);
		switch (id) {
			case R.id.wheelView_year :
				wheel.setAdapter(new NumericWheelAdapter(1901, 2048,
						NumericWheelAdapter.DEFAULT_CALENDER));
				wheel.setType(Constant.wheel_year);
				wheel.setRealLabel("年");
				wheel.setCurrentItem(Constant.YEAR - 1901);
				wheel.addScrollingListener(scrolledListener);
				wheel.addChangingListener(changedListener);
				wheelView_year = wheel;
				break;
			case R.id.wheelView_month :
				wheel.setAdapter(new NumericWheelAdapter(1, 12,
						NumericWheelAdapter.DEFAULT_CALENDER));
				wheel.setType(Constant.wheel_month);
				wheel.setRealLabel("月");
				wheel.setCurrentItem(Constant.MONTH - 1);
				wheel.addScrollingListener(scrolledListener);
				wheel.addChangingListener(changedListener);
				wheelView_month = wheel;
				break;
			case R.id.wheelView_day :
				wheel.setAdapter(new NumericWheelAdapter(1, SpecialCalendar
						.getDaysOfMonth(
								SpecialCalendar.isLeapYear(Constant.YEAR),
								Constant.MONTH),
						NumericWheelAdapter.DEFAULT_CALENDER));
				wheel.setType(Constant.wheel_day);
				// wheel.setRealLabel(SpecialCalendar.getCapitelNumberWeekDay(Constant.YEAR,
				// Constant.MONTH, Constant.DAY));
				wheel.setRealLabel("日");
				wheel.setCurrentItem(Constant.DAY - 1);
				wheel.addScrollingListener(scrolledListener);
				wheelView_day = wheel;
				break;
			case R.id.wheelView_hour :
				wheel.setAdapter(new NumericWheelAdapter(0, 23,
						NumericWheelAdapter.DEFAULT_CALENDER));
				wheel.setType(Constant.wheel_hour);
				wheel.setRealLabel("时");
				wheel.setCurrentItem(Constant.HOUR - 0);
				wheelView_hour = wheel;
				break;
			case R.id.wheelView_min :
				wheel.setAdapter(new NumericWheelAdapter(0, 59,
						NumericWheelAdapter.DEFAULT_CALENDER));
				wheel.setType(Constant.wheel_min);
				wheel.setRealLabel("分");
				wheel.setCurrentItem(Constant.MIN - 0);
				wheelView_min = wheel;
				break;
		}
		wheel.setInterpolator(new AnticipateOvershootInterpolator());
	}
	public void windowDeploy(int x, int y) {
		window = timeSelectorDialog.getWindow(); // 得到对话框
		window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画
		WindowManager.LayoutParams wl = window.getAttributes();
		// 根据x，y坐标设置窗口需要显示的位置
		wl.x = x; // x小于0左移，大于0右移
		wl.y = y; // y小于0上移，大于0下移
		// wl.alpha = 0.6f; //设置透明度
		wl.gravity = Gravity.BOTTOM; // 设置重力
		window.setAttributes(wl);
	}
	private WheelView getWheel(int id) {
		return (WheelView) timeSelectorDialog.findViewById(id);
	}

	OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
		public void onScrollingStarted(WheelView wheel) {
			wheelScrolled = true;
		}
		
		public void onScrollingFinished(WheelView wheel) {
			wheelScrolled = false;
			// updateStatus();
		}
	};
	
	OnWheelChangedListener changedListener = new OnWheelChangedListener() {

		public void onChanged(WheelView wheel, int oldValue, int newValue) {
			// TODO Auto-generated method stub
			updateStatus();
		}
	
	};

	private void updateStatus() {
		if (initFinished == false)
			return ;
		
		try {
			String text = wheelView_year.getAdapter() != null ? wheelView_year
					.getAdapter().getItem(wheelView_year.getCurrentItem())
					: null;
			Constant.VARY_YEAR = Integer.valueOf(text);
			
			text = wheelView_month.getAdapter() != null ? wheelView_month
					.getAdapter().getItem(wheelView_month.getCurrentItem())
					: null;
			Constant.VARY_MONTH = Integer.valueOf(text);

			wheelView_day.setAdapter(new NumericWheelAdapter(1, SpecialCalendar
					.getDaysOfMonth(SpecialCalendar.isLeapYear(Constant.VARY_YEAR),
							Constant.VARY_MONTH),
					NumericWheelAdapter.DEFAULT_CALENDER));
			wheelView_day.setCurrentItem(wheelView_day.getCurrentItem());
			wheelView_day.setItemsLayout(null);
			wheelView_day.setLabelLayout(null);
		} catch (Exception e) {
			ScheduleApplication.logException(SimpleTimeSelectorDialog.class, e);
		}
	}

	public void show() {
		windowDeploy(0, 0);
		timeSelectorDialog.show();
	}

	public void dismiss() {
		timeSelectorDialog.dismiss();
		initFinished = false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.wheelView_cancel :
				dismiss();
				break;
			case R.id.wheelView_ok :
				dismiss();
				mOnOkButtonClickListener.onOkButtonClick(this);
				break;
		}
	}

}
