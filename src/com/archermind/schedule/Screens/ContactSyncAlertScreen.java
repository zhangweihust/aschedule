package com.archermind.schedule.Screens;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.EventTypes;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.SharedPreferenceUtil;

public class ContactSyncAlertScreen extends Activity implements OnClickListener {

	private Button contactsyncalert_sync;
	private Button contactsyncalert_cancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_sync_alert);
		contactsyncalert_sync = (Button) findViewById(R.id.contactsyncalert_sync);
		contactsyncalert_cancel = (Button) findViewById(R.id.contactsyncalert_cancel);

		contactsyncalert_sync.setOnClickListener(this);
		contactsyncalert_cancel.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		try {
			switch (v.getId()) {
				case R.id.contactsyncalert_sync :
					SharedPreferenceUtil.setValue("sync", Constant.CONTACT_SYNC_ING);
					new Thread() {
						public void run() {
							/* 要获取用户名 */
							if (ServiceManager.getContact().ContactSync(
									String.valueOf(ServiceManager.getUserId()))) {
								ServiceManager.getEventservice().onUpdateEvent(
										new EventArgs(EventTypes.CONTACT_SYNC_SUCCESS));
								SharedPreferenceUtil.setValue("sync", Constant.CONTACT_SYNC_SUCCESS);
							} else {
								ServiceManager.getEventservice().onUpdateEvent(
										new EventArgs(EventTypes.CONTACT_SYNC_FAILED));
								SharedPreferenceUtil.setValue("sync", Constant.CONTACT_SYNC_FAILED);
							}
						};
					}.start();
					break;
				case R.id.contactsyncalert_cancel :
					ServiceManager.getEventservice().onUpdateEvent(
							new EventArgs(EventTypes.CONTACT_SYNC_CANCEL));
					SharedPreferenceUtil.setValue("sync", Constant.CONTACT_SYNC_CANCEL);
					break;
				default :
					break;
			}
			ContactSyncAlertScreen.this.finish();
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(),e);
		}
	}

}
