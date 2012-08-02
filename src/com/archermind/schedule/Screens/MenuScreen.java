package com.archermind.schedule.Screens;


import com.archermind.schedule.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class MenuScreen extends Activity implements OnClickListener{
	
	private Button gotonext;
	private Button menu_account;
	private Button menu_weather;
	private Button menu_setting;
	private Button menu_feedback;
	private Button menu_about;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.settingmenu_layout);
        
        
        gotonext = (Button) findViewById(R.id.gotonext);
        menu_account = (Button) findViewById(R.id.menu_account_btn);
        menu_weather = (Button) findViewById(R.id.menu_weather_btn);
        menu_setting = (Button) findViewById(R.id.menu_setting_btn);
        menu_feedback = (Button) findViewById(R.id.menu_feedback_btn);
        menu_about = (Button) findViewById(R.id.menu_about_btn);
        
        gotonext.setOnClickListener(this);
        menu_account.setOnClickListener(this);
        menu_weather.setOnClickListener(this);
        menu_setting.setOnClickListener(this);
        menu_feedback.setOnClickListener(this);
        menu_about.setOnClickListener(this);
    }
    
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.gotonext:
//			MenuScreen.this.finish();
//			overridePendingTransition(R.anim.left_in,R.anim.left_out);
			onBackPressed();
			break;
		case R.id.menu_account_btn:
			Intent it = new Intent(MenuScreen.this,LoginScreen.class);
			startActivity(it);
			overridePendingTransition(R.anim.right_in,R.anim.right_out);
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
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(R.anim.left_in,R.anim.left_out);
	}

	
}