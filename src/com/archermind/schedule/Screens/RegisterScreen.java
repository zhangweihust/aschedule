package com.archermind.schedule.Screens;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Model.UserInfoData;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.HttpUtils;

public class RegisterScreen extends Activity implements OnClickListener {
	private Button goback;

	private Button submit;

	private EditText et_username;

	private EditText et_email;

	private EditText et_pswd;
	private EditText et_pswd_confirm;

	private ImageView photoselect;

	private Handler handler;

	private int binType = -1;

	private String binId;

	private static final int REGISTER_SUCCESS = 1;

	private static final int REGISTER_FAILED = 2;

	private boolean registerflag = false;

	private static final int REGISTER_FAILED_USERORPSWD_NULL = -1; // 用户名或密码为空
	private static final int REGISTER_FAILED_ACCOUNT_EXISTS = -2; // 账号或昵称已存在
	private static final int REGISTER_FAILED_BLOG_BINDED = -3; // 微博等账号已绑定
	private static final int REGISTER_FAILED_EMIAL_ERROR = -6; // 注册的邮箱不合法
	private static final int REGISTER_FAILED_PSWD_ERROR = -7; // 密码不合法
	private static final int REGISTER_FAILED_NICK_ERROR = -9; // 昵称含特殊字符，不合法

	// private static final int BIN_SUCCESS = 3;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.register_layout);

		Intent intent = getIntent();

		binType = intent.getIntExtra("type", 0);
		binId = intent.getStringExtra("uid");

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);

				if (msg != null && msg.obj != null) {
					String retValue = (String) msg.obj;
					String prompt = "";

					ScheduleApplication.LogD(RegisterScreen.class,
							"retValue = " + retValue);

					if (retValue.contains("user_id")) {
						prompt = "注册成功";
						writeUserinfo(retValue, HttpUtils.GetCookie());
						ScheduleApplication
								.LogD(RegisterScreen.class,
										"服务器返回信息写入SharedPrefences成功! ret = "
												+ retValue);
						startActivity(new Intent(RegisterScreen.this,
								TelephoneBindScreen.class));
						finish();
					} else {
						int ret = Integer.parseInt(retValue);
						switch (ret) {
							case REGISTER_FAILED_USERORPSWD_NULL :
								prompt = "用户名或密码为空";
								break;
							case REGISTER_FAILED_ACCOUNT_EXISTS :
								prompt = "账号或昵称已存在";
								break;
							case REGISTER_FAILED_BLOG_BINDED :
								prompt = "微博等账号已绑定";
								break;
							case REGISTER_FAILED_EMIAL_ERROR :
								prompt = "注册的邮箱不合法";
								break;
							case REGISTER_FAILED_PSWD_ERROR :
								prompt = "密码不合法";
								break;
							case REGISTER_FAILED_NICK_ERROR :
								prompt = "昵称含特殊字符，不合法";
								break;
							default :
								prompt = retValue;
								break;
						}
						prompt = "注册失败 : " + prompt;
					}

					ServiceManager.ToastShow(prompt);
				}

				registerflag = false;
			}
		};

		goback = (Button) findViewById(R.id.register_goback);
		submit = (Button) findViewById(R.id.register_submit);
		photoselect = (ImageView) findViewById(R.id.register_photoselect);
		et_username = (EditText) findViewById(R.id.register_username);
		et_email = (EditText) findViewById(R.id.register_email);
		et_pswd = (EditText) findViewById(R.id.register_pswd);
		et_pswd_confirm = (EditText) findViewById(R.id.register_pswd_confirm);

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

		if (!registerflag) {
			switch (v.getId()) {
				case R.id.register_goback :
					onBackPressed();
					break;
				case R.id.register_submit :
					Register();
					break;
				case R.id.register_photoselect :
					break;

				default :
					break;
			}
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(R.anim.left_in, R.anim.left_out);
	}

	/* 验证填写的信息合法性 */
	public void Register() {
		final String username = et_username.getText().toString();
		if (username.length() <= 0) {
			ServiceManager.ToastShow("昵称不能为空!");
			return;
		} else if (!ServiceManager.getServerInterface().isNickName(username)) {
			ServiceManager.ToastShow("昵称太长或者包含非法字符!");
			return;
		}

		final String email = et_email.getText().toString();
		if (email.length() <= 0) {
			ServiceManager.ToastShow("邮箱不能为空!");
			return;
		} else if (!ServiceManager.getServerInterface().isEmail(email)) {
			ServiceManager.ToastShow("邮箱格式不正确");
			return;
		}

		final String pswd = et_pswd.getText().toString();
		if (!pswd.equals(et_pswd_confirm.getText().toString())) {
			ServiceManager.ToastShow("两次输入的密码不一致");
			et_pswd.setText("");
			et_pswd_confirm.setText("");
			return;
		}
		if (pswd.length() < 6 || pswd.length() > 15) {
			ServiceManager.ToastShow("密码长度应该在6-15个字符之间!");
			return;
		} else if (!ServiceManager.getServerInterface().isPswdValid(pswd)) {
			ServiceManager.ToastShow("密码中必须同时包含数字和字母!");
			return;
		}

		TelephonyManager tm = (TelephonyManager) (RegisterScreen.this)
				.getSystemService(Context.TELEPHONY_SERVICE);
		final String imsi = tm.getSubscriberId();

		new Thread() {
			public void run() {
				registerflag = true;
				String ret = ServiceManager.getServerInterface().register(
						email, pswd, username, imsi, null, null, null,
						Integer.toString(binType), binId);
				Message msg = new Message();
				msg.obj = ret;
				handler.sendMessage(msg);
			};
		}.start();
	}

	/* 登录成功后将服务器返回的信息写入SharedPrefences，并在程序中保存userid和cookie */
	public static void writeUserinfo(String userinfo, String cookie) {
		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(userinfo);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = (JSONObject) jsonArray.opt(i);

				/* 将服务器返回的信息存到SharedPrefences中 */
				ServiceManager.setSPUserInfo(UserInfoData.USER_ID,
						jsonObject.getString(UserInfoData.USER_ID));
				ServiceManager.setSPUserInfo(UserInfoData.EMAIL,
						jsonObject.getString(UserInfoData.EMAIL));
				ServiceManager.setSPUserInfo(UserInfoData.PSWD,
						jsonObject.getString(UserInfoData.PSWD));
				ServiceManager.setSPUserInfo(UserInfoData.REGDATE,
						jsonObject.getString(UserInfoData.REGDATE));
				ServiceManager.setSPUserInfo(UserInfoData.LGDATE,
						jsonObject.getString(UserInfoData.LGDATE));
				ServiceManager.setSPUserInfo(UserInfoData.IMSI,
						jsonObject.getString(UserInfoData.IMSI));
				ServiceManager.setSPUserInfo(UserInfoData.NICK,
						jsonObject.getString(UserInfoData.NICK));
				ServiceManager.setSPUserInfo(UserInfoData.TEL,
						jsonObject.getString(UserInfoData.TEL));
				ServiceManager.setSPUserInfo(UserInfoData.PHOTO_URL,
						jsonObject.getString(UserInfoData.PHOTO_URL));
				ServiceManager.setSPUserInfo(UserInfoData.COOKIE, cookie);

				/* 保存userid和cookie */
				ServiceManager.setUserId(jsonObject
						.getInt(UserInfoData.USER_ID));
				ServiceManager.setCookie(cookie);

				if (jsonObject.getString(UserInfoData.TEL).equals("")
						|| jsonObject.getString(UserInfoData.TEL) == "null") {
					ServiceManager.setBindFlag(false);
				} else {
					ServiceManager.setBindFlag(true);
				}

			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
