package com.archermind.schedule.Screens;


import com.archermind.schedule.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

public class settingmenu extends Activity implements OnTouchListener,OnGestureListener,OnClickListener{
	private Button gotonext;
	private Button menu_account;
	private Button menu_expiredlog;
	private Button menu_weather;
	private Button menu_setting;
	private Button menu_feedback;
	private Button menu_about;
	
	private GestureDetector gd;
	private static final int FLING_MIN_DISTANCE = 20;
	private static final int FLING_MIN_VELOCITY = 20;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.settingmenu_layout);
        
        gd = new GestureDetector(this);
        LinearLayout ll = (LinearLayout) findViewById(R.id.myLinearLayout);  
        ll.setOnTouchListener(this);  
        ll.setLongClickable(true);
        
        gotonext = (Button) findViewById(R.id.gotonext);
        menu_account = (Button) findViewById(R.id.menu_account_btn);
        menu_expiredlog = (Button) findViewById(R.id.menu_expiredlog_btn);
        menu_weather = (Button) findViewById(R.id.menu_weather_btn);
        menu_setting = (Button) findViewById(R.id.menu_setting_btn);
        menu_feedback = (Button) findViewById(R.id.menu_feedback_btn);
        menu_about = (Button) findViewById(R.id.menu_about_btn);
        
        gotonext.setOnClickListener(this);
        menu_account.setOnClickListener(this);
        menu_expiredlog.setOnClickListener(this);
        menu_weather.setOnClickListener(this);
        menu_setting.setOnClickListener(this);
        menu_feedback.setOnClickListener(this);
        menu_about.setOnClickListener(this);
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
		if(e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY)  
		{  
			Intent intent = new Intent(settingmenu.this, register.class);  
			startActivity(intent);  
			overridePendingTransition(R.anim.left_in,R.anim.left_out);
//			this.finish();
//			overridePendingTransition(android.R.anim.slide_out_right,android.R.anim.slide_in_left);
//			Toast.makeText(this, "向左手势", Toast.LENGTH_SHORT).show();   

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
		case R.id.gotonext:
			Intent it = new Intent();
			it.setClass(settingmenu.this, register.class);
			startActivity(it);
			overridePendingTransition(R.anim.left_in,R.anim.left_out);
			break;
		case R.id.menu_account_btn:
			break;
		case R.id.menu_expiredlog_btn:
			break;
		case R.id.menu_weather_btn:
			break;
		case R.id.menu_setting_btn:
			break;
		case R.id.menu_feedback_btn:
			break;
		case R.id.menu_about_btn:
			break;
			
		default:
			break;
		}
	}  
	
}