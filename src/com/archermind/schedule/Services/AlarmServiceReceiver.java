package com.archermind.schedule.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (ServiceManager.isStarted()) {

		} else {

			if (!ServiceManager.start()) {
				ServiceManager.exit();
				return;
			}
		}
	}

}
