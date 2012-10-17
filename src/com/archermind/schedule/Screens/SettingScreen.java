package com.archermind.schedule.Screens;

import android.content.Intent;
import android.content.UriMatcher;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Model.UserInfoData;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Views.SettingSlipSwitch;

public class SettingScreen extends Screen implements OnClickListener {

	private Button mBtnOut;

//	private SettingSlipSwitch mSlipSwitch;

	private final int SMS_RINGTONE_PICKED = 0;

	// private Object mMmsSoundsPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);

		mBtnOut = (Button) findViewById(R.id.title_bar_setting_btn);
		mBtnOut.setOnClickListener(this);

		// LinearLayout llayout = (LinearLayout)findViewById(R.id.datatime);
		// llayout.setOnClickListener(this);

		LinearLayout llayoutremind = (LinearLayout) findViewById(R.id.setting_remind);
		llayoutremind.setOnClickListener(this);
		//
		// mSlipSwitch =
		// (SettingSlipSwitch)findViewById(R.id.settingslipswitch);
		// mSlipSwitch.setImageResource(R.drawable.settingonandoff,
		// R.drawable.settingonandoff,
		// R.drawable.settingcover);
		// mSlipSwitch.setSwitchState(true);

	}

	@Override
	public void onClick(View v) {
		try {
			switch (v.getId()) {
				
				case R.id.setting_remind :
					
					doPickSmsRingtone();
					break;
					
				case R.id.title_bar_setting_btn :
					
					finish();
					break;
					
				default :
					
					break;
					
			}
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(),e);
		}
	}

	private void doPickSmsRingtone() {

		String tempRing = ServiceManager
				.getSPUserSetting(UserInfoData.SETTING_SOUND_REMIND);
		String notificationStr = TextUtils.isEmpty(tempRing) ? null : tempRing;

		Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
				RingtoneManager.TYPE_NOTIFICATION);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
				RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);

		Uri notificationUri;
		if (notificationStr != null) {

			notificationUri = Uri.parse(notificationStr);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
					notificationUri);
		} else { // 如果没有值，则显示默认的铃声

			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
					RingtoneManager
							.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		}

		startActivityForResult(intent, SMS_RINGTONE_PICKED);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != RESULT_OK) {

			return;
		}

		switch (requestCode) {
			case SMS_RINGTONE_PICKED : {

				Uri pickedUri = data
						.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				if (null == pickedUri) {

					ServiceManager.setSPUserSetting(
							UserInfoData.SETTING_SOUND_REMIND, "slient");
				} else {

					ServiceManager.setSPUserSetting(
							UserInfoData.SETTING_SOUND_REMIND,
							pickedUri.toString());
				}
			}
				break;

			default :
				break;
		}
	}

}
