package com.archermind.schedule.Screens;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.archermind.schedule.R;
import com.archermind.schedule.Services.ServiceManager;

public class RegisterScreen extends Activity implements OnClickListener {
	private Button goback;
	private Button submit;
	private EditText et_username;
	private EditText et_email;
	private EditText et_pswd;
	private ImageView photoselect;
	private Handler handler;
	
	
	private static final int REGISTER_SUCCESS = 1;
	private static final int REGISTER_FAILED = 2;
	public static final String USER_INFO = "userinfo";
	public static final String USER_ID = "userid";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.register_layout);
       
        
        handler = new Handler()
        {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
				if (msg.what == REGISTER_SUCCESS)
				{
					ServiceManager.ToastShow("注册成功!");
					onBackPressed();
				}
				else if (msg.what == REGISTER_FAILED)
				{
					ServiceManager.ToastShow("注册失败!");
				}
			}
        };
        
        goback = (Button)findViewById(R.id.register_goback);
        submit = (Button)findViewById(R.id.register_submit);
        photoselect = (ImageView)findViewById(R.id.register_photoselect);
        et_username = (EditText)findViewById(R.id.register_username);
        et_email = (EditText)findViewById(R.id.register_email);
        et_pswd = (EditText)findViewById(R.id.register_pswd);
        
        goback.setOnClickListener(this);
        submit.setOnClickListener(this);
        photoselect.setOnClickListener(this);
        
    }
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.register_goback:
			onBackPressed();
			break;
		case R.id.register_submit:
			Register();
			break;
		case R.id.register_photoselect:
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
	
	
	/* 验证填写的信息合法性 */
	public void Register()
	{
		final String username = et_username.getText().toString();
		if (username.length() <= 0)
		{
			ServiceManager.ToastShow("昵称不能为空!");
			return;
		}
		else if (!ServiceManager.getServerInterface().isNickName(username))
		{
			ServiceManager.ToastShow("昵称太长或者包含非法字符!");
			return;
		}
		
		final String email = et_email.getText().toString();
		if (email.length() <= 0)
		{
			ServiceManager.ToastShow("邮箱不能为空!");
			return;
		}
		else if (!ServiceManager.getServerInterface().isEmail(email))
		{
			ServiceManager.ToastShow("邮箱格式不正确");
			return;
		}
		
		final String pswd = et_pswd.getText().toString();
		if (pswd.length() < 6 ||  pswd.length() > 15)
		{
			ServiceManager.ToastShow("密码长度应该在6-15个字符之间!");
			return;
		}
		else if (!ServiceManager.getServerInterface().isPswdValid(pswd))
		{
			ServiceManager.ToastShow("密码中必须同时包含数字和字母!");
			return;
		}
			
		
		TelephonyManager tm = (TelephonyManager) (RegisterScreen.this).getSystemService(Context.TELEPHONY_SERVICE);
		final String imsi = tm.getSubscriberId();
		
		new Thread()
		{
			public void run() 
			{
				int ret = ServiceManager.getServerInterface().register(email,pswd,username,imsi,null,null,null);
				Log.e("RegisterScreen","ret = " + ret);
				if (ret <= 0)
				{
					handler.sendEmptyMessage(REGISTER_FAILED);
				}
				else
				{
					handler.sendEmptyMessage(REGISTER_SUCCESS);
					ServiceManager.setUserId(ret);		/* 设置服务器返回的Userid */
					SharedPreferences.Editor editor = getSharedPreferences(USER_INFO, Context.MODE_WORLD_WRITEABLE).edit();
					editor.putInt(USER_ID, ret);
					editor.commit();
				}
			};
		}.start();
	}
	
}
