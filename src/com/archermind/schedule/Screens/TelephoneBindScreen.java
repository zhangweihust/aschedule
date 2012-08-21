package com.archermind.schedule.Screens;

import org.json.JSONException;
import org.json.JSONObject;

import com.archermind.schedule.R;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.DeviceInfo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TelephoneBindScreen extends Activity implements OnClickListener{
	
	private final static String SCHEDULE_APP_ID = "3";
	private final static int GET_VERIFICATION_CODE_FAILED = 0;
	private final static int GET_VERIFICATION_CODE_SUCCESS = 1;
	private final static int TELEPHONE_BIND_FAILED = 2;
	private final static int TELEPHONE_BIND_SUCCESS = 3;
	
	private EditText telephone_bind_tel_et;
	private EditText telephone_bind_verification_et;
	private Button telephone_bind_btn;
	private Button telephone_bind_verification_btn;
	private TextView telephone_bind_prompt;
	private LinearLayout telephone_bind_verification;
	
	private String tel = "";
	private String smsID = "";
	private boolean canRequestVerification = true;
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.telephone_bind);
        
        telephone_bind_tel_et = (EditText)findViewById(R.id.telephone_bind_tel_et);
        telephone_bind_verification_et = (EditText)findViewById(R.id.telephone_bind_verification_et);
        telephone_bind_btn = (Button)findViewById(R.id.telephone_bind_btn);
        telephone_bind_verification_btn = (Button)findViewById(R.id.telephone_bind_verification_btn);
        telephone_bind_prompt = (TextView)findViewById(R.id.telephone_bind_prompt);
        telephone_bind_verification = (LinearLayout)findViewById(R.id.telephone_bind_verification);
        
        telephone_bind_btn.setOnClickListener(this);
        telephone_bind_verification_btn.setOnClickListener(this);
        
        handler = new Handler()
        {
        	@Override
        	public void handleMessage(Message msg) {
        		// TODO Auto-generated method stub
        		super.handleMessage(msg);
        		
        		switch(msg.what)
        		{
        		case GET_VERIFICATION_CODE_FAILED:
        			ServiceManager.ToastShow("获取验证码失败!");
        			break;
        		case GET_VERIFICATION_CODE_SUCCESS:
        			telephone_bind_prompt.setVisibility(View.VISIBLE);
        			telephone_bind_verification.setVisibility(View.VISIBLE);
        			canRequestVerification = false;
        			handler.postDelayed(new Runnable()
        			{
        				@Override
        				public void run() {
        					// TODO Auto-generated method stub
        					canRequestVerification = true;
        				}
        			}, 60000);
        			break;
        		case TELEPHONE_BIND_FAILED:
        			ServiceManager.ToastShow("绑定失败!");
        			break;
        		case TELEPHONE_BIND_SUCCESS:
        			telephone_bind_prompt.setVisibility(View.INVISIBLE);
        			ServiceManager.ToastShow("绑定成功!");
        			finish();
        			break;
    			default:
    				break;
        		}
        	}
        };
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		String inputstr = "";
		switch(v.getId())
		{
		case R.id.telephone_bind_btn:
			if (canRequestVerification)
			{
				inputstr = telephone_bind_tel_et.getText().toString();
				if (inputstr.matches("^(\\+86)?1(3[0-9]|47|5[0-37-9]|8[278])\\d{8}$"))
				{
					inputstr = inputstr.replace("+86", "");
					final String requestTel = inputstr;
					
					new Thread()
					{
						public void run() 
						{
							if (0 == ServiceManager.getServerInterface().is_tel_bind((String.valueOf(ServiceManager.getUserId())), requestTel,DeviceInfo.getDeviceIMSI()))
							{
								String ret = ServiceManager.getServerInterface().sendSMS(SCHEDULE_APP_ID,requestTel,"default");
								String smsid = parseJason(ret,"smsID");
								if (!smsid.equals(""))
								{
									if (Integer.parseInt(smsid) > 0)
									{
										tel = requestTel;
										smsID = smsid;
										handler.sendEmptyMessage(GET_VERIFICATION_CODE_SUCCESS);
									}
									else
									{
										handler.sendEmptyMessage(GET_VERIFICATION_CODE_FAILED);
									}
								}
								else
								{
									handler.sendEmptyMessage(GET_VERIFICATION_CODE_FAILED);
								}
							}
						};
					}.start();
					
				}
				else
				{
					ServiceManager.ToastShow("手机号码不合法!");
				}
			}
			else
			{
				ServiceManager.ToastShow("您获取验证码的频率过高,请过一分钟后再试!");
			}
			
			break;
		case R.id.telephone_bind_verification_btn:
			inputstr = telephone_bind_verification_et.getText().toString();
			if (!inputstr.equals(""))
			{
				final String verficationcode = inputstr;
				new Thread(){
					public void run() 
					{
						int ret = ServiceManager.getServerInterface().checkSMS(
								 SCHEDULE_APP_ID, 
								 verficationcode, 
								 smsID, 
								 "bind", 
								 String.valueOf(ServiceManager.getUserId()), 
								 tel);
						if (ret == 0)
						{
							handler.sendEmptyMessage(TELEPHONE_BIND_SUCCESS);
						}
						else
						{
							handler.sendEmptyMessage(TELEPHONE_BIND_FAILED);
						}
					};
				}.start();
				 
			}
			break;
		default:
			break;
		}
	}
	
	public String parseJason(String jason,String feild)
	{
		String ret = "";
		try {
            JSONObject jsonObject = new JSONObject(jason);
            ret = jsonObject.getString(feild);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
}
