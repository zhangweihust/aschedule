package com.archermind.schedule.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.EventTypes;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Screens.EditScheduleScreen;
import com.archermind.schedule.Screens.MyDynamicScreen;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Utils.ServerInterface;

public class MyDynamicOperateDialog implements OnClickListener {

	private Dialog dialog;
	private Button my_dynamic_operate_reply;
	private Button my_dynamic_operate_delete;
	private Button my_dynamic_operate_goback;
	private Window window = null;
	Context context;

	private EventArgs args;
	private Handler handler;

	public MyDynamicOperateDialog(Context context, EventArgs args, Handler handler) {
		this.context = context;
		this.args = args;
		this.handler = handler;
		dialog = new Dialog(context, R.style.CustomDialog);
		dialog.setContentView(R.layout.my_dynamic_operate_dialog);
		dialog.setCanceledOnTouchOutside(true);

		my_dynamic_operate_reply = (Button) dialog
				.findViewById(R.id.my_dynamic_operate_reply);
		my_dynamic_operate_delete = (Button) dialog
				.findViewById(R.id.my_dynamic_operate_delete);
		my_dynamic_operate_goback = (Button) dialog
				.findViewById(R.id.my_dynamic_operate_goback);

		my_dynamic_operate_reply.setOnClickListener(this);
		my_dynamic_operate_delete.setOnClickListener(this);
		my_dynamic_operate_goback.setOnClickListener(this);
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
		case R.id.my_dynamic_operate_reply:
			Message msg = new Message();
			msg.what = MyDynamicScreen.ON_REPLY;
			msg.getData().putInt("t_id", (Integer) args.getExtra("t_id"));
			handler.sendMessage(msg);
			break;
		case R.id.my_dynamic_operate_delete:
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.prompt)
					.setMessage(R.string.content)
					.setNegativeButton(android.R.string.cancel,null)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									deleteSchedule();
								}
							}).show();
			break;
		case R.id.my_dynamic_operate_goback:
			break;
		}
	}

	private void deleteSchedule() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
				int tid = (Integer) args.getExtra("t_id");
				int id = -1;
				Cursor c =  ServiceManager.getDbManager().queryScheduleByTid(tid);
				if (c != null) {
					if (c.moveToFirst()) {
						id = c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_ID));
					}
					c.close();
				}
				
				if (id >= 0) {
					ContentValues contentvalues = new ContentValues();
					contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG,
							DatabaseHelper.SCHEDULE_OPER_DELETE);
					ServiceManager.getDbManager().updateScheduleById(id, contentvalues);
					DateTimeUtils.cancelAlarm(id);
				}
				ServiceManager.getServerInterface().deleteScheduleByTid(tid);
				ServiceManager.getEventservice().onUpdateEvent(
						new EventArgs(EventTypes.LOCAL_SCHEDULE_UPDATE));
				ServiceManager.sendBroadcastForUpdateSchedule(context);
				
				ServiceManager.getDbManager().deleteShareScheduleByTid(Integer.toString(tid));
				} catch (Exception e) {
					ScheduleApplication.logException(MyDynamicOperateDialog.class,e);
				}
			}
		}).start();
	}
}
