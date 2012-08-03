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
import com.archermind.schedule.Adapters.NumericWheelAdapter;
import com.archermind.schedule.Calendar.SpecialCalendar;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Views.WheelView;
import com.archermind.schedule.Views.WheelView.OnWheelScrollListener;

public class SimpleTimeSelectorDialog implements OnClickListener {
	private WheelView wheelView_day;
	private WheelView wheelView_month;
	private WheelView wheelView_year;
	private boolean wheelScrolled = false;
	private Dialog timeSelectorDialog;
	private Context context;
	private Button wheelView_cancel,wheelView_ok;
	private Window window = null;

	public SimpleTimeSelectorDialog(Context context) {
		timeSelectorDialog = new Dialog(context, R.style.CustomDialog);
		timeSelectorDialog.setContentView(R.layout.simple_time_select);
		timeSelectorDialog.setCanceledOnTouchOutside(true);
		init();
	}

	private void init() {
		initWheel(R.id.wheelView_year);
		initWheel(R.id.wheelView_month);
		initWheel(R.id.wheelView_day);
		
		wheelView_cancel = (Button) timeSelectorDialog.findViewById(R.id.wheelView_cancel);
		wheelView_ok = (Button) timeSelectorDialog.findViewById(R.id.wheelView_ok);
		
		wheelView_cancel.setOnClickListener(this);
		wheelView_ok.setOnClickListener(this);


	}

	private void initWheel(int id) {
		WheelView wheel = getWheel(id);
		wheel.setCyclic(false);
		switch (id) {
		case R.id.wheelView_year:
			wheel.setType(Constant.wheel_year);
//			wheel.setFlag(1);
			wheel.setAdapter(new NumericWheelAdapter(1901, 2048,
					NumericWheelAdapter.DEFAULT_CALENDER));

			wheel.setLabel("年");
			wheel.setCurrentItem(Constant.YEAR - 1901);
			wheel.addScrollingListener(scrolledListener);
			wheelView_year = wheel;
			break;
		case R.id.wheelView_month:
			wheel.setType(Constant.wheel_month);
//			wheel.setFlag(1);
			wheel.setAdapter(new NumericWheelAdapter(1, 12,
					NumericWheelAdapter.DEFAULT_CALENDER));
			
			wheel.setLabel("月");
			wheel.setCurrentItem(Constant.MONTH - 1);
			wheel.addScrollingListener(scrolledListener);
			wheelView_month = wheel;
			break;
		case R.id.wheelView_day:
			wheel.setType(Constant.wheel_day);
//			wheel.setFlag(1);
			wheel.setAdapter(new NumericWheelAdapter(1, SpecialCalendar
					.getDaysOfMonth(SpecialCalendar.isLeapYear(Constant.YEAR),
							Constant.MONTH),
					NumericWheelAdapter.DEFAULT_CALENDER));
			
			wheel.setLabel(SpecialCalendar.getWeekDay(Constant.YEAR,
					Constant.MONTH, Constant.DAY));
			wheel.setCurrentItem(Constant.DAY - 1);
			wheel.addScrollingListener(scrolledListener);
			wheelView_day = wheel;
			break;
		}
		wheel.setInterpolator(new AnticipateOvershootInterpolator());
	}
    public void windowDeploy(int x, int y){
        window = timeSelectorDialog.getWindow(); //得到对话框
        window.setWindowAnimations(R.style.dialogWindowAnim); //设置窗口弹出动画
        WindowManager.LayoutParams wl = window.getAttributes();
        //根据x，y坐标设置窗口需要显示的位置
        wl.x = x; //x小于0左移，大于0右移
        wl.y = y; //y小于0上移，大于0下移 
//        wl.alpha = 0.6f; //设置透明度
        wl.gravity = Gravity.BOTTOM; //设置重力
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
			updateStatus();
		}
	};

	private void updateStatus() {
		wheelView_day.setAdapter(new NumericWheelAdapter(1, SpecialCalendar
				.getDaysOfMonth(SpecialCalendar.isLeapYear(Constant.VARY_YEAR),
						Constant.VARY_MONTH),
				NumericWheelAdapter.DEFAULT_CALENDER));
		wheelView_day.setCurrentItem(wheelView_day.getCurrentItem());
		wheelView_day.setItemsLayout(null);
		wheelView_day.setLabelLayout(null);
	}

	public void show() {
		windowDeploy(0,0);
		timeSelectorDialog.show();
	}

	public void dismiss() {
		timeSelectorDialog.dismiss();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.wheelView_cancel:
			dismiss();
			break;
		case R.id.wheelView_ok:
			dismiss();
			break;
		}
	}

}
