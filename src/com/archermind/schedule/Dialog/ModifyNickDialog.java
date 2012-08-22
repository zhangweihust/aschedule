package com.archermind.schedule.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Adapters.NumericWheelAdapter;
import com.archermind.schedule.Calendar.SpecialCalendar;
import com.archermind.schedule.Dialog.TimeSelectorDialog.OnOkButtonClickListener;
import com.archermind.schedule.Model.UserInfoData;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Views.WheelView;
import com.archermind.schedule.Views.WheelView.OnWheelScrollListener;

public class ModifyNickDialog implements OnClickListener {
	private Dialog modifyNickDialog;
	private Context context;
	private Button modify_nick_cancel,modify_nick__ok;
	private EditText nick;
	private Window window = null;

	public ModifyNickDialog(Context context) {
		modifyNickDialog = new Dialog(context, R.style.CustomDialog);
		modifyNickDialog.setContentView(R.layout.modify_nick);
		modifyNickDialog.setCanceledOnTouchOutside(true);
		initView();
	}

	private void initView() {
		nick = (EditText) modifyNickDialog.findViewById(R.id.nick);
		modify_nick_cancel = (Button) modifyNickDialog.findViewById(R.id.modify_nick_cancel);
		modify_nick__ok = (Button) modifyNickDialog.findViewById(R.id.modify_nick__ok);
		modify_nick_cancel.setOnClickListener(this);
		modify_nick__ok.setOnClickListener(this);
	}
	
	public Dialog getDialog(){
		return modifyNickDialog;
	}

	public void show() {
		   DisplayMetrics dm = ScheduleApplication.getContext().getResources().getDisplayMetrics();
			int screenWidth = dm.widthPixels;
			Window window = modifyNickDialog.getWindow(); // 得到对话框
			WindowManager.LayoutParams wl = window.getAttributes();
			wl.width = screenWidth * 7 / 8;
			wl.gravity = Gravity.CENTER; // 设置重力
			window.setAttributes(wl);
			modifyNickDialog.show();
	}

	public void dismiss() {
		modifyNickDialog.dismiss();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.modify_nick_cancel:
			ServiceManager.setSPUserInfo(UserInfoData.NICK,nick.getText().toString());
			break;
		case R.id.modify_nick__ok:
			break;
		}
		dismiss();
	}

}
