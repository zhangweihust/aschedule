package com.archermind.schedule.Screens;

import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;

public class AboutScreen extends Screen implements OnClickListener {

	private Button btnOut;
	private LinearLayout mConnectLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		btnOut = (Button) findViewById(R.id.title_bar_about_button);
		btnOut.setOnClickListener(this);

		mConnectLayout = (LinearLayout) findViewById(R.id.aboutcontentlayout);
		mConnectLayout.setBackgroundResource(R.drawable.aboutconnect);
		mConnectLayout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

			case R.id.aboutcontentlayout :

				mConnectLayout.setBackgroundResource(R.drawable.aboutconnect);
				ScheduleApplication.LogD(getClass(), "about background " + mConnectLayout.getBackground().toString());
				break;

			case R.id.title_bar_about_button :
				
				finish();
				break;

			default :
				break;
		}
	}

}
