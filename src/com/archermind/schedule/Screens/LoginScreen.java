package com.archermind.schedule.Screens;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.archermind.schedule.R;
import com.archermind.schedule.Services.ServiceManager;

public class LoginScreen extends Activity implements OnTouchListener,OnGestureListener,OnClickListener{
	private GestureDetector gd;
	private Handler handler;
	
	private Button login_sina;
	private Button login_tencent;
	private Button login_renren;
	private Button login_submit;
	private EditText login_username;
	private EditText login_password;
	
	private static final int LOGIN_SUCCESS = 1;
	private static final int LOGIN_FAILED = 2;
	private static final int FLING_MIN_DISTANCE = 20;
	private static final int FLING_MIN_VELOCITY = 20;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.login_layout);
        
        login_sina = (Button)findViewById(R.id.login_sina);
        login_tencent = (Button)findViewById(R.id.login_tencent);
        login_renren = (Button)findViewById(R.id.login_renren);
        login_submit = (Button)findViewById(R.id.login_submit);
        login_username = (EditText)findViewById(R.id.login_username);
        login_password = (EditText)findViewById(R.id.login_password);
        
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
					RegisterScreen.ToastShow("登录成功!");
				}
				else if (msg.what == LOGIN_FAILED)
				{
					RegisterScreen.ToastShow("登录失败!");
				}
			}
        };
        
        gd = new GestureDetector(this);
        LinearLayout ll = (LinearLayout) findViewById(R.id.myline_login);  
        ll.setOnTouchListener(this);  
        ll.setLongClickable(true);
    }
    
//	public void onGesture(GestureOverlayView overlay, MotionEvent event) {
//		// TODO Auto-generated method stub
//		
//	}
//	public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
//		// TODO Auto-generated method stub
//		
//	}
//	public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
//		// TODO Auto-generated method stub
//		
//	}
//	public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
//		// TODO Auto-generated method stub
//		
//	}
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return gd.onTouchEvent(event);
	}
	
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,  
			float velocityY) {  
		
		// TODO Auto-generated method stub  
//		if(e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY)  
//		{  
//			Intent intent = new Intent(login.this, DemoTestActivity.class);  
//			startActivity(intent);  
//			overridePendingTransition(R.anim.left_in,R.anim.left_out);
////			this.finish();
////			overridePendingTransition(android.R.anim.slide_out_right,android.R.anim.slide_in_left);
////			Toast.makeText(this, "向左手势", Toast.LENGTH_SHORT).show();   
//
//		}  
		if (e2.getX()-e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) >FLING_MIN_VELOCITY) {  
	              
			//切换Activity  
			Intent intent = new Intent(LoginScreen.this, RegisterScreen.class);  
			startActivity(intent);  
			overridePendingTransition(R.anim.right_in,R.anim.right_out);
//			Toast.makeText(this, "向右手势", Toast.LENGTH_SHORT).show();  
		}  
		
		return false;  
	}
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.login_sina:
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
	
	public void loginSubmit()
	{
		TelephonyManager tm = (TelephonyManager) (LoginScreen.this).getSystemService(Context.TELEPHONY_SERVICE);
		final String imsi = tm.getSubscriberId();
		final String username = login_username.getText().toString();
		final String password = login_password.getText().toString();
		if (username.length() == 0)
		{
			RegisterScreen.ToastShow("用户名不能为空!");
			return;
		}
		if (password.length() == 0)
		{
			RegisterScreen.ToastShow("密码不能为空");
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