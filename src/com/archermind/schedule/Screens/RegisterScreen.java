package com.archermind.schedule.Screens;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class RegisterScreen extends Activity implements OnTouchListener,OnGestureListener,OnClickListener {
	private GestureDetector gd;
	private LinearLayout ll;
	
	private Button goback;
	private Button submit;
	private EditText et_username;
	private EditText et_email;
	private EditText et_pswd;
	private ImageView photoselect;
	private Handler handler;
	private static Toast toast;
	
	private static final int REGISTER_SUCCESS = 1;
	private static final int REGISTER_FAILED = 2;
	public static final String REGISTER_SERVER_ADDR = "http://player.archermind.com/ci/index.php/ktvphone/getUinfo";
	
	private static final int FLING_MIN_DISTANCE = 20;
	private static final int FLING_MIN_VELOCITY = 20;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.register_layout);
        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
        
        handler = new Handler()
        {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
				if (msg.what == REGISTER_SUCCESS)
				{
					ToastShow("注册成功!");
				}
				else if (msg.what == REGISTER_FAILED)
				{
					ToastShow("注册失败!");
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
        
        ll = (LinearLayout) findViewById(R.id.myline);
        ll.setOnTouchListener(this);  
        ll.setLongClickable(true);
        gd = new GestureDetector(this);
        
//      //创建电话管理
//        TelephonyManager tm = (TelephonyManager) (nextone.this).getSystemService(Context.TELEPHONY_SERVICE);
//    //与手机建立连接
//   //获取手机号码
//       String phoneId = tm.getLine1Number();
//       Toast.makeText(getApplicationContext(), phoneId, Toast.LENGTH_LONG).show();
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
	
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		if (e2.getX()-e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) >FLING_MIN_VELOCITY) {  
            
		//切换Activity  
		Intent intent = new Intent(RegisterScreen.this, MenuScreen.class);  
		startActivity(intent);  
//		overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
		overridePendingTransition(R.anim.right_in,R.anim.right_out);
//		this.finish();
//		Toast.makeText(this, "向右手势", Toast.LENGTH_SHORT).show();
		}
		else if (e1.getX()-e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) >FLING_MIN_VELOCITY) 
		{  
			//切换Activity  
			Intent intent = new Intent(RegisterScreen.this, LoginScreen.class);  
			startActivity(intent);  
			overridePendingTransition(R.anim.left_in,R.anim.left_out);
		} 
		
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
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return gd.onTouchEvent(event);
	}
	
	public static void ToastShow(String message)
	{
		if (toast != null)
		{
			toast.cancel();
		}
		toast.setText(message);
		toast.show();
	}
	
	/* 验证填写的信息合法性 */
	public void Register()
	{
		final String username = et_username.getText().toString();
		if (username.length() <= 0)
		{
			ToastShow("昵称不能为空!");
			return;
		}
		else if (username.length() > 20)
		{
			ToastShow("昵称不能大于20位!");
			return;
		}
		
		final String email = et_email.getText().toString();
		if (email.length() <= 0)
		{
			ToastShow("邮箱不能为空!");
			return;
		}
		else if (!email.matches("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"))
		{
			ToastShow("邮箱格式不正确");
			return;
		}
		
		final String pswd = et_pswd.getText().toString();
		if (pswd.length() < 6)
		{
			ToastShow("密码长度小于6位!");
			return;
		}
		
		TelephonyManager tm = (TelephonyManager) (RegisterScreen.this).getSystemService(Context.TELEPHONY_SERVICE);
		final String imsi = tm.getSubscriberId();
		
		new Thread()
		{
			public void run() 
			{
				int ret = ((ScheduleApplication)getApplication()).getServerInterface().register(email,pswd,imsi,null,null,null);
				Log.e("---lqf---","ret = " + ret);
				if (ret == 0)
				{
					handler.sendEmptyMessage(REGISTER_SUCCESS);
				}
				else
				{
					handler.sendEmptyMessage(REGISTER_FAILED);
				}
			};
		}.start();
//		Toast.makeText(this, "imsi = " + imsi, Toast.LENGTH_SHORT).show();
	}
	
}
