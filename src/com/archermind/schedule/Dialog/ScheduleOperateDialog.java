package com.archermind.schedule.Dialog;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.archermind.schedule.R;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.EventTypes;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Screens.EditScheduleScreen;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.DateTimeUtils;

public class ScheduleOperateDialog implements OnClickListener {

	private Dialog dialog;
	private Button schedule_operate_modify;
	private Button schedule_operate_delete;
	private Button schedule_operate_goback;
	private Window window = null;
	Context context;

	private EventArgs args;

	public ScheduleOperateDialog(Context context, EventArgs args) {
		this.context = context;
		this.args = args;
		dialog = new Dialog(context, R.style.CustomDialog);
		dialog.setContentView(R.layout.schedule_operate_dialog);
		dialog.setCanceledOnTouchOutside(true);

		schedule_operate_modify = (Button) dialog
				.findViewById(R.id.schedule_operate_modify);
		schedule_operate_delete = (Button) dialog
				.findViewById(R.id.schedule_operate_delete);
		schedule_operate_goback = (Button) dialog
				.findViewById(R.id.schedule_operate_goback);

		schedule_operate_modify.setOnClickListener(this);
		schedule_operate_delete.setOnClickListener(this);
		schedule_operate_goback.setOnClickListener(this);
	}

	public void windowDeploy(int x, int y, int width) {
		window = dialog.getWindow(); // 得到对话框
		window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画
		WindowManager.LayoutParams wl = window.getAttributes();
		// 根据x，y坐标设置窗口需要显示的位置
		wl.x = x; // x小于0左移，大于0右移
		wl.y = y; // y小于0上移，大于0下移
		wl.width = width;
		// wl.alpha = 0.6f; //设置透明度
		wl.gravity = Gravity.BOTTOM; // 设置重力
		window.setAttributes(wl);
	}

	public void show(int width) {
		windowDeploy(0, 0, width);
		dialog.show();
	}

	public void dismiss() {
		dialog.dismiss();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		dismiss();
		switch (v.getId()) {
			case R.id.schedule_operate_modify :
				Intent mIntent = new Intent(context, EditScheduleScreen.class);
				mIntent.putExtra("id", (Integer) args.getExtra("id"));
				mIntent.putExtra("first", (Boolean) args.getExtra("first"));
				mIntent.putExtra("time", (Long) args.getExtra("time"));
				context.startActivity(mIntent);
				break;
			case R.id.schedule_operate_delete :
				new Thread(new Runnable() {
					@Override
					public void run() {
						ContentValues contentvalues = new ContentValues();
						contentvalues.put(
								DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG,
								DatabaseHelper.SCHEDULE_OPER_DELETE);
						ServiceManager.getDbManager().updateScheduleById(
								(Integer) args.getExtra("id"), contentvalues);
						DateTimeUtils.cancelAlarm((Integer) args.getExtra("id"));
						ServiceManager.getServerInterface().uploadSchedule("0",
								"1");
						ServiceManager
								.getEventservice()
								.onUpdateEvent(
										new EventArgs(
												EventTypes.LOCAL_SCHEDULE_UPDATE));
					}
				}).start();
				break;
			case R.id.schedule_operate_goback :
				break;
		}
	}

}
