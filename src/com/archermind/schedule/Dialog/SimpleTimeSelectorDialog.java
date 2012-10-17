package com.archermind.schedule.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Adapters.NumericWheelAdapter;
import com.archermind.schedule.Calendar.SpecialCalendar;
import com.archermind.schedule.Dialog.TimeSelectorDialog.OnOkButtonClickListener;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Views.WheelView;
import com.archermind.schedule.Views.WheelView.OnWheelChangedListener;
import com.archermind.schedule.Views.WheelView.OnWheelScrollListener;

public class SimpleTimeSelectorDialog implements OnClickListener {
	private WheelView wheelView_day;
	private WheelView wheelView_month;
	private WheelView wheelView_year;
	private boolean initFinished = false;
	private boolean wheelScrolled = false;
	private Dialog timeSelectorDialog;
	private Context context;
	private Button wheelView_cancel, wheelView_ok;
	private Window window = null;

	public interface SimpleOnOkButtonClickListener {
		void onOkButtonClick(SimpleTimeSelectorDialog timeSelectorDialog);
	}

	private SimpleOnOkButtonClickListener mOnOkButtonClickListener;

	public void setOnOkButtonClickListener(SimpleOnOkButtonClickListener l) {
		mOnOkButtonClickListener = l;
	}

	public SimpleTimeSelectorDialog(Context context) {
		timeSelectorDialog = new Dialog(context, R.style.CustomDialog);
		timeSelectorDialog.setContentView(R.layout.simple_time_select);
		timeSelectorDialog.setCanceledOnTouchOutside(true);
		init();
		wheelView_cancel = (Button) timeSelectorDialog
				.findViewById(R.id.wheelView_cancel);
		wheelView_ok = (Button) timeSelectorDialog
				.findViewById(R.id.wheelView_ok);
		wheelView_cancel.setOnClickListener(this);
		wheelView_ok.setOnClickListener(this);
	}

	private void init() {
		initWheel(R.id.wheelView_year);
		initWheel(R.id.wheelView_month);
		initWheel(R.id.wheelView_day);
		initFinished = true;
		updateStatus();
	}
	public void setCurrentItem(long time) {

		Constant.YEAR = Integer.parseInt(DateTimeUtils.time2String("y", time));
		Constant.MONTH = Integer.parseInt(DateTimeUtils.time2String("M", time));
		Constant.DAY = Integer.parseInt(DateTimeUtils.time2String("d", time));
		// wheelView_year.setCurrentItem(Constant.YEAR - 1901);
		// wheelView_month.setCurrentItem(Constant.MONTH - 1);
		// wheelView_day.setCurrentItem(Constant.DAY - 1);

	}

	private void initWheel(int id) {
		WheelView wheel = getWheel(id);
		switch (id) {
			case R.id.wheelView_year :
				wheel.setType(Constant.wheel_year);
				// wheel.setFlag(1);
				wheel.setAdapter(new NumericWheelAdapter(1901, 2048,
						NumericWheelAdapter.DEFAULT_CALENDER));

				wheel.setRealLabel("年");
				wheel.setCurrentItem(Constant.YEAR - 1901);
				wheel.addScrollingListener(scrolledListener);
				wheel.addChangingListener(changedListener);
				wheelView_year = wheel;
				break;
			case R.id.wheelView_month :
				wheel.setType(Constant.wheel_month);
				// wheel.setFlag(1);
				wheel.setAdapter(new NumericWheelAdapter(1, 12,
						NumericWheelAdapter.DEFAULT_CALENDER));

				wheel.setRealLabel("月");
				wheel.setCurrentItem(Constant.MONTH - 1);
				wheel.addScrollingListener(scrolledListener);
				wheel.addChangingListener(changedListener);
				wheelView_month = wheel;
				break;
			case R.id.wheelView_day :
				wheel.setType(Constant.wheel_day);
				// wheel.setFlag(1);
				wheel.setAdapter(new NumericWheelAdapter(1, SpecialCalendar
						.getDaysOfMonth(
								SpecialCalendar.isLeapYear(Constant.YEAR),
								Constant.MONTH),
						NumericWheelAdapter.DEFAULT_CALENDER));
				// wheel.setRealLabel(SpecialCalendar.getCapitelNumberWeekDay(Constant.YEAR,
				// Constant.MONTH, Constant.DAY));
				wheel.setRealLabel("日");
				wheel.setCurrentItem(Constant.DAY - 1);
				wheel.addScrollingListener(scrolledListener);
				wheelView_day = wheel;
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
		init();
		timeSelectorDialog.show();
	}

	public void dismiss() {
		timeSelectorDialog.dismiss();
		initFinished = false;
		if (wheelView_day != null) {
			wheelView_day = null;
		}
		if (wheelView_month != null) {
			wheelView_month = null;
		}
		if (wheelView_year != null) {
			wheelView_year = null;
		}
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
