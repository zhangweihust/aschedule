package com.archermind.schedule.Screens;

import org.json.JSONException;
import org.json.JSONObject;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Model.UserInfoData;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.DeviceInfo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TelephoneBindScreen extends Activity implements OnClickListener{
	
	private final static String SCHEDULE_APP_ID = "3";
	private final static int GET_VERIFICATION_CODE_FAILED = 0;
	private final static int GET_VERIFICATION_CODE_SUCCESS = 1;
	private final static int TELEPHONE_BIND_FAILED = 2;
	private final static int TELEPHONE_BIND_SUCCESS = 3;
	private final static int TELEPHONE_BIND_ALREADY = 4;
	private final static int TELEPHONE_SIM_NONE = 5;
	
	private final static int TELEPHONE_BIND_FAILED_USERORTEL_NULL = -3; 			//用户名或手机号为空
	private final static int TELEPHONE_BIND_FAILED_USER_NOTEXISTS = -4; 			//用户不存在
	private final static int TELEPHONE_BIND_FAILED_TEL_BINDED = -5; 				//该电话已经绑定
	private final static int TELEPHONE_BIND_FAILED_VERIFICATION_ERROR = -6; 		//验证码不正确
	
	private EditText telephone_bind_tel_et;
	private EditText telephone_bind_verification_et;
	private Button telephone_bind_btn;
	private Button telephone_bind_verification_btn;
	private Button telephone_bind_goback;
	private TextView telephone_bind_prompt;
	private LinearLayout telephone_bind_verification;
	private LinearLayout telephone_bind_get_verification;
	private LinearLayout telephone_bind_is_bind;
	private Button bind_again;
	private TextView is_bind_prompt;
	
	private String tel = "";
	private String smsID = "";
	private boolean canRequestVerification = true;
	private boolean canBingFlag = true;
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.telephone_bind);
        
        telephone_bind_tel_et = (EditText)findViewById(R.id.telephone_bind_tel_et);
        telephone_bind_verification_et = (EditText)findViewById(R.id.telephone_bind_verification_et);
        telephone_bind_btn = (Button)findViewById(R.id.telephone_bind_btn);
        telephone_bind_goback = (Button)findViewById(R.id.telephone_bind_goback);
        telephone_bind_verification_btn = (Button)findViewById(R.id.telephone_bind_verification_btn);
        telephone_bind_prompt = (TextView)findViewById(R.id.telephone_bind_prompt);
        telephone_bind_verification = (LinearLayout)findViewById(R.id.telephone_bind_verification);
        telephone_bind_get_verification = (LinearLayout)findViewById(R.id.telephone_bind_get_verification);
        bind_again = (Button)findViewById(R.id.bind_again);
        is_bind_prompt = (TextView)findViewById(R.id.is_bind_prompt);
        telephone_bind_is_bind = (LinearLayout)findViewById(R.id.telephone_bind_is_bind);
        
        telephone_bind_btn.setOnClickListener(this);
        telephone_bind_verification_btn.setOnClickListener(this);
        telephone_bind_goback.setOnClickListener(this);
        bind_again.setOnClickListener(this);
        
        if (ServiceManager.getBindFlag())
        {
        	telephone_bind_is_bind.setVisibility(View.VISIBLE);
        	String promptstr = "您的帐号 " + ServiceManager.getSPUserInfo(UserInfoData.EMAIL) + " 已经与手机号 "
        	 					+ ServiceManager.getSPUserInfo(UserInfoData.TEL) + " 绑定!";
        	is_bind_prompt.setText(promptstr);
        	telephone_bind_get_verification.setVisibility(View.INVISIBLE);
        }
        else
        {
        	telephone_bind_is_bind.setVisibility(View.GONE);
        	telephone_bind_get_verification.setVisibility(View.VISIBLE);
        }
        
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
        			canRequestVerification = true;
        			break;
        		case GET_VERIFICATION_CODE_SUCCESS:
        			telephone_bind_prompt.setVisibility(View.VISIBLE);
        			telephone_bind_verification.setVisibility(View.VISIBLE);
        			telephone_bind_btn.setEnabled(false);
        			handler.postDelayed(new Runnable()
        			{
        				@Override
        				public void run() {
        					// TODO Auto-generated method stub
        					canRequestVerification = true;
        					telephone_bind_btn.setEnabled(true);
        				}
        			}, 60000);
        			break;
        		case TELEPHONE_BIND_FAILED:
        			int reason = (Integer) msg.obj;
        			String failedReason = "";
        			switch(reason)
        			{
        			case TELEPHONE_BIND_FAILED_USERORTEL_NULL:
        				failedReason = "用户名或手机号为空";
        				break;
        			case TELEPHONE_BIND_FAILED_USER_NOTEXISTS:
        				failedReason = "用户不存在";
        				break;
        			case TELEPHONE_BIND_FAILED_TEL_BINDED:
        				failedReason = "该电话已经绑定";
        				break;
        			case TELEPHONE_BIND_FAILED_VERIFICATION_ERROR:
        				failedReason = "验证码不正确";
        				break;
    				default:
    					failedReason = String.valueOf(reason);
    					break;
        			}
        			ServiceManager.ToastShow("绑定失败 : " + failedReason);
        			break;
        		case TELEPHONE_BIND_SUCCESS:
        			telephone_bind_prompt.setVisibility(View.INVISIBLE);
        			ServiceManager.ToastShow("绑定成功!");
        			finish();
        			break;
        		case TELEPHONE_BIND_ALREADY:
        			Toast.makeText(TelephoneBindScreen.this, "这个手机号已经绑定!", Toast.LENGTH_SHORT).show();
        			finish();
        			break;
        		case TELEPHONE_SIM_NONE:
        			ServiceManager.ToastShow("请插入SIM卡");
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
		String imsi = DeviceInfo.getDeviceIMSI();
		
		if (imsi == null)
		{
			imsi = "";
//			handler.sendEmptyMessage(TELEPHONE_SIM_NONE);
//			return;
		}
		switch(v.getId())
		{
		case R.id.telephone_bind_btn:
			if (canRequestVerification)
			{
				inputstr = telephone_bind_tel_et.getText().toString();
				if (inputstr.matches("^(\\+86)?1[3|4|5|8][0-9]\\d{8}$"))
				{
					inputstr = inputstr.replace("+86", "");
					final String requestTel = inputstr;
					
					new Thread()
					{
						public void run() 
						{
							canRequestVerification = false;
							int is_tel_bindret = ServiceManager.getServerInterface().is_tel_bind((String.valueOf(ServiceManager.getUserId())), requestTel);
							ScheduleApplication.LogD(TelephoneBindScreen.class, "is_tel_bindret = " + is_tel_bindret);
							if (0 == is_tel_bindret)
							{
								String ret = ServiceManager.getServerInterface().sendSMS(SCHEDULE_APP_ID,requestTel,"default");
								ScheduleApplication.LogD(TelephoneBindScreen.class,"ret = " + ret);
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
							else
							{
								handler.sendEmptyMessage(TELEPHONE_BIND_ALREADY);
								ScheduleApplication.LogD(TelephoneBindScreen.class,"这个手机号已经绑定");
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
				if (canBingFlag)
				{
					canBingFlag = false;
					final String finalimsi = imsi;
					new Thread(){
						public void run() 
						{
							int ret = ServiceManager.getServerInterface().checkSMS(
									 SCHEDULE_APP_ID, 
									 verficationcode, 
									 smsID, 
									 "bind", 
									 String.valueOf(ServiceManager.getUserId()), 
									 tel,
									 finalimsi);
							if (ret == 0)
							{
								handler.sendEmptyMessage(TELEPHONE_BIND_SUCCESS);
								ServiceManager.setSPUserInfo(UserInfoData.TEL, tel);
								ServiceManager.setSPUserInfo(UserInfoData.IMSI, finalimsi);
								ServiceManager.setBindFlag(true);
							}
							else
							{
								Message msg = new Message();
								msg.what = TELEPHONE_BIND_FAILED;
								msg.obj = ret;
								handler.sendMessage(msg);
								ServiceManager.setBindFlag(false);
							}
							canBingFlag = true;
						};
					}.start();
				}
				 
			}
			break;
		case R.id.bind_again:
			telephone_bind_is_bind.setVisibility(View.GONE);
			telephone_bind_get_verification.setVisibility(View.VISIBLE);
			break;
		case R.id.telephone_bind_goback:
			finish();
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
