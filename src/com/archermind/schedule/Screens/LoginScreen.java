package com.archermind.schedule.Screens;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Services.ServiceManager;

public class LoginScreen extends Activity implements OnClickListener{
	private Handler handler;
	
	private Button login_goback;
	private Button login_sina;
	private Button login_tencent;
	private Button login_renren;
	private Button login_submit;
	private EditText login_username;
	private EditText login_password;
	
	private static final int LOGIN_SUCCESS = 1;
	private static final int LOGIN_FAILED = 2;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.login_layout);
        
        login_goback = (Button)findViewById(R.id.login_goback);
        login_sina = (Button)findViewById(R.id.login_sina);
        login_tencent = (Button)findViewById(R.id.login_tencent);
        login_renren = (Button)findViewById(R.id.login_renren);
        login_submit = (Button)findViewById(R.id.login_submit);
        login_username = (EditText)findViewById(R.id.login_username);
        login_password = (EditText)findViewById(R.id.login_password);
        
        login_goback.setOnClickListener(this);
        login_sina.setOnClickListener(this);
        login_tencent.setOnClickListener(this);
        login_renren.setOnClickListener(this);
        login_submit.setOnClickListener(this);
        
        handler = new Handler()
        {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
				if (msg.what == LOGIN_SUCCESS)
				{
					ServiceManager.ToastShow("登录成功!");
				}
				else if (msg.what == LOGIN_FAILED)
				{
					ServiceManager.ToastShow("登录失败!");
				}
			}
        };
    }
    

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.login_goback:
			onBackPressed();
			break;
		case R.id.login_sina:
			Intent it = new Intent(LoginScreen.this,RegisterScreen.class);
			startActivity(it);
			overridePendingTransition(R.anim.right_in,R.anim.right_out);
			break;
		case R.id.login_tencent:
			break;
		case R.id.login_renren:
			break;
		case R.id.login_submit:
			loginSubmit();
			break;
			
		default:
			break;
		}
	}  
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(R.anim.left_in,R.anim.left_out);
	}
	
	public void loginSubmit()
	{
		TelephonyManager tm = (TelephonyManager) (LoginScreen.this).getSystemService(Context.TELEPHONY_SERVICE);
		final String imsi = tm.getSubscriberId();
		final String username = login_username.getText().toString();
		final String password = login_password.getText().toString();
		if (username.length() == 0)
		{
			ServiceManager.ToastShow("用户名不能为空!");
			return;
		}
		if (password.length() == 0)
		{
			ServiceManager.ToastShow("密码不能为空");
			return;
		}
		new Thread()
		{
			public void run() 
			{
				int ret = ServiceManager.getServerInterface().login(username, password, imsi);
				if (ret == 0)
				{
					handler.sendEmptyMessage(LOGIN_SUCCESS);
				}
				else
				{
					handler.sendEmptyMessage(LOGIN_FAILED);
				}
			};
		}.start();
	}
}