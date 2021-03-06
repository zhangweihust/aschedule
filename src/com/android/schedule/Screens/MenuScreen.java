
package com.android.schedule.Screens;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.schedule.ScheduleApplication;
import com.android.schedule.Model.UserInfoData;
import com.android.schedule.Services.ServiceManager;
import com.android.schedule.Utils.DeviceInfo;
import com.android.schedule.Utils.MyProgressDialog;
import com.archermind.schedule.R;

public class MenuScreen extends Activity implements OnClickListener {

    private static final int LOGIN_STATUS_FAILED = 0;

    private static final int LOGIN_STATUS_SUCCESS = 1;

    private static final int LOGIN_STATUS_UNBIND = 2;

    private Button gotonext;

    private Button menu_account;

    private Button menu_weather;

    private Button menu_setting;

    private Button menu_feedback;

    private Button menu_about;

    private Handler handler;

    private boolean account_btn_flag = false;

    private ProgressDialog mpDialog;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.settingmenu_layout);

        gotonext = (Button)findViewById(R.id.gotonext);
        menu_account = (Button)findViewById(R.id.menu_account_btn);
        menu_weather = (Button)findViewById(R.id.menu_weather_btn);
        menu_setting = (Button)findViewById(R.id.menu_setting_btn);
        menu_feedback = (Button)findViewById(R.id.menu_feedback_btn);
        menu_about = (Button)findViewById(R.id.menu_about_btn);

        gotonext.setOnClickListener(this);
        menu_account.setOnClickListener(this);
        menu_weather.setOnClickListener(this);
        menu_setting.setOnClickListener(this);
        menu_feedback.setOnClickListener(this);
        menu_about.setOnClickListener(this);

        mpDialog = MyProgressDialog.getProgressDialog(MenuScreen.this);

        handler = new Handler() {
            public void handleMessage(Message msg) {
            	try {
            		Intent it;
            		if (mpDialog != null) {
            			mpDialog.dismiss();
            		}
            		switch (msg.what) {
            			case LOGIN_STATUS_FAILED:
            				it = new Intent(MenuScreen.this, LoginScreen.class);
            				break;
            				
            			case LOGIN_STATUS_SUCCESS:
            				it = new Intent(MenuScreen.this, AccountSettingScreen.class);
            				break;
            				
            			case LOGIN_STATUS_UNBIND:
            				ServiceManager.setBindFlag(false);
            				it = new Intent(MenuScreen.this, TelephoneBindScreen.class);
            				break;
            				
            			default:
            				it = new Intent(MenuScreen.this, LoginScreen.class);
            				break;
            		}
            		startActivity(it);
            		account_btn_flag = false;
            		overridePendingTransition(R.anim.right_in, R.anim.right_out);
				} catch (Exception e) {
					ScheduleApplication.logException(getClass(),e);
				}
            };
        };
    }

    public void onClick(View v) {
        try {
        	switch (v.getId()) {
        		case R.id.gotonext:
        			// MenuScreen.this.finish();
        			// overridePendingTransition(R.anim.left_in,R.anim.left_out);
        			onBackPressed();
        			break;
        		case R.id.menu_account_btn:
        			
        			mpDialog.show();
        			if (!account_btn_flag) {
        				account_btn_flag = true;
        				new Thread() {
        					public void run() {
        						if (ServiceManager.isUserLogining(ServiceManager.getUserId())) {
        							if (ServiceManager.getBindFlag()) {
        								handler.sendEmptyMessage(LOGIN_STATUS_SUCCESS);
        							} else {
        								handler.sendEmptyMessage(LOGIN_STATUS_UNBIND);
        							}
        						} else {
        							handler.sendEmptyMessage(LOGIN_STATUS_FAILED);
        						}
        					};
        				}.start();
        			}
        			break;
        			
        		case R.id.menu_weather_btn:
        			startActivity(new Intent(MenuScreen.this, WeatherScreen.class));
        			break;
        		case R.id.menu_setting_btn:
        			startActivity(new Intent(MenuScreen.this, SettingScreen.class));
        			break;
        		case R.id.menu_feedback_btn:
        			startActivity(new Intent(MenuScreen.this, FeedbackScreen.class));
        			break;
        		case R.id.menu_about_btn:
        			startActivity(new Intent(MenuScreen.this, AboutScreen.class));
        			break;
        			
        		default:
        			break;
        	}
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(),e);
		}
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.left_out);
    }

}
