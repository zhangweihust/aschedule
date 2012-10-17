package com.archermind.schedule.Screens;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.IEventHandler;
import com.archermind.schedule.Model.UserInfoData;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.ServerInterface;


public class FeedbackScreen extends Screen
		implements
			IEventHandler,
			OnClickListener {

	private Button mBtnOut;

	private Button mBtnSend;

	private EditText etContentSend;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_back);

		mBtnOut = (Button) findViewById(R.id.title_feedback_button_out);
		mBtnOut.setOnClickListener(this);

		mBtnSend = (Button) findViewById(R.id.title_feedback_button_send);
		mBtnSend.setOnClickListener(this);

		etContentSend = (EditText) findViewById(R.id.etfeed_back_content);
		etContentSend.setHint(R.string.feedbackhintcontent);
	}

	public boolean onEvent(Object sender, EventArgs e) {

		return false;
	}

	@Override
	public void onClick(View v) {
		try {
			switch (v.getId()) {
				
				case R.id.title_feedback_button_out :
					
					finish();
					break;
					
				case R.id.title_feedback_button_send :
					
					String telephone = ServiceManager
					.getSPUserInfo(UserInfoData.TEL);
					String userid = ServiceManager
							.getSPUserInfo(UserInfoData.USER_ID);
					String suggestion = etContentSend.getText().toString().trim();
					
					ServerInterface sfInterface = new ServerInterface();
					
					if (suggestion.length() > 160) {
						
						Toast.makeText(getApplicationContext(), "字数超过160，请减少字数", 1)
						.show();
						
					} else if (suggestion.length() == 0) {
						
						Toast.makeText(getApplicationContext(), "请输入反馈内容", 1)
						.show();
						
					} else {
						
						int result = sfInterface.suggestionfeedback(userid,
								telephone, suggestion);
						
						if (0 == result) {
							
							Toast.makeText(getApplicationContext(), "反馈成功", 1)
							.show();
							finish();
							
						} else {
							
							Toast.makeText(getApplicationContext(), "反馈失败，请稍后重试", 1)
							.show();
						}
					}
					
					break;
					
				default :
					break;
			}
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(),e);
		}
	}
}
