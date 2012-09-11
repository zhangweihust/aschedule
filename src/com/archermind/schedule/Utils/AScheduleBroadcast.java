package com.archermind.schedule.Utils;

import com.archermind.schedule.Services.ServiceManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AScheduleBroadcast extends BroadcastReceiver {

	public static final String ASCHEDULE_CHECK_CONTACT = "aschedule_check_contact";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if (action.equals(ASCHEDULE_CHECK_CONTACT)) {
			ServiceManager.getContact().checkSync(context);
		}
	}

}
