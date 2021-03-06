
package com.android.schedule.Screens;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.schedule.ScheduleApplication;
import com.android.schedule.Events.EventArgs;
import com.android.schedule.Events.EventTypes;
import com.android.schedule.Model.UserInfoData;
import com.android.schedule.Services.ServiceManager;
import com.android.schedule.Utils.AlbumInfoUtil;
import com.android.schedule.Utils.DeviceInfo;
import com.android.schedule.Utils.HttpUtils;
import com.android.schedule.Utils.MyProgressDialog;
import com.android.schedule.Utils.NetworkUtils;
import com.archermind.schedule.R;
import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.exception.RenrenAuthError;
import com.renren.api.connect.android.view.RenrenAuthListener;
import com.tencent.weibo.api.UserAPI;
import com.tencent.weibo.constants.OAuthConstants;
import com.tencent.weibo.oauthv1.OAuthV1;
import com.tencent.weibo.oauthv1.OAuthV1Client;
import com.tencent.weibo.utils.QHttpClient;
import com.tencent.weibo.webview.OAuthV1AuthorizeWebView;
import com.weibo.net.DialogError;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;

public class LoginScreen extends Screen implements OnClickListener {
    private Handler handler;

    private String mFisrtKey;

    private Button login_goback;

    private Button login_sina;

    private Button login_tencent;

    private Button login_renren;

    private Button login_submit;

    private EditText login_username;

    private EditText login_password;

    private static final String APP_RENREN_ID = "207648";

    private static final String API_RENREN_KEY = "0549e85e0bdc4a468d3e2bac9eead851";

    private static final String SECRET_RENREN_KEY = "58eee644a21549d1b4177e3c1983760f";

    private Renren renren;

    private OAuthV1 oAuth;

    private static final int LOGIN_SUCCESS = 1;

    private static final int LOGIN_FAILED = 2;

    private static final int TAG_SINA = 1;

    private static final int TAG_RENREN = 2;

    private static final int TAG_TENGXUN = 3;

    private boolean loginflag = false;

    private ProgressDialog mpDialog;

    private static final int LOGIN_FAILED_EMAILORPSWD_NULL = -1; // 邮箱或密码为空

    private static final int LOGIN_FAILED_EMAILORPSWD_ERROR = -2; // 邮箱或密码错误

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

        login_username.setText(ServiceManager.getSPUserInfo(UserInfoData.EMAIL));
        login_password.setText(ServiceManager.deCrypt(ServiceManager
                .getSPUserInfo(UserInfoData.PSWD)));

        login_goback.setOnClickListener(this);
        login_sina.setOnClickListener(this);
        login_tencent.setOnClickListener(this);
        login_renren.setOnClickListener(this);
        login_submit.setOnClickListener(this);

        mpDialog = MyProgressDialog.getProgressDialog(LoginScreen.this);
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
					
                	if (mpDialog!=null) {
                		mpDialog.dismiss();
                	}
                	
                	if (msg != null && msg.obj != null) {
                		String retValue = (String)msg.obj;
                		String prompt = "";
                		ScheduleApplication.LogD(LoginScreen.class, "retValue = " + retValue);
                		if (retValue.contains("user_id")) {
                			ServiceManager.ToastShow("登录成功");
                			RegisterScreen.writeUserinfo(retValue, HttpUtils.GetCookie());
                			
                			if (ServiceManager.getSPUserInfo(UserInfoData.TEL).equals("")
                					|| ServiceManager.getSPUserInfo(UserInfoData.TEL).equals("null")) {
                				ServiceManager.ToastShow("您的帐号尚未绑定手机号,请进行绑定!");
                				startActivity(new Intent(LoginScreen.this, TelephoneBindScreen.class));
                			} else {
                				startActivity(new Intent(LoginScreen.this, AccountSettingScreen.class));
                			}
                			eventService.onUpdateEvent(new EventArgs(EventTypes.LOGIN_SUCCESS));
                			finish();
                			
                		} else {
                			int ret = Integer.parseInt(retValue);
                			switch (ret) {
                				
                				case LOGIN_FAILED_EMAILORPSWD_NULL:
                					prompt = "：邮箱或密码为空";
                					break;
                					
                				case LOGIN_FAILED_EMAILORPSWD_ERROR:
                					prompt = "：邮箱或密码错误";
                					break;
                					
                				case -101:// 程序中偶尔出现
                					
                					prompt = "请10秒后重试！";
                					break;
                					
                				default:
                					prompt = "请稍候重试！";
                					break;
                			}
                			prompt = "登录失败 " + prompt;
                			
                			ServiceManager.ToastShow(prompt);
                		}
                	}
                	loginflag = false;
				} catch (Exception e) {
					ScheduleApplication.logException(getClass(),e);
				}
            }
        };
    }

    public void onClick(View v) {
    	try {
    		if (!loginflag) {
    			loginflag = true;
    			switch (v.getId()) {
    				case R.id.login_goback:
    					onBackPressed();
    					break;
    				case R.id.login_sina:
    					
    					loginSina();
    					break;
    				case R.id.login_tencent:
    					
    					loginTencent();
    					break;
    				case R.id.login_renren:
    					
    					loginRenren();
    					break;
    				case R.id.login_submit:
    					loginSubmit();
    					break;
    					
    				default:
    					break;
    			}
    		}
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(),e);
		}
    }

    private void loginSina() {

        Weibo weibo = Weibo.getInstance();
        weibo.setupConsumerConfig("2178333014", "9027b1d8aaf67311bb41c0edf0147acd");

        // Oauth2.0
        // 隐式授权认证方式
        weibo.setRedirectUrl("https://api.weibo.com/oauth2/default.html");// 此处回调页内容应该替换为与appkey对应的应用回调页

        // 对应的应用回调页可在开发者登陆新浪微博开发平台之后，
        // 进入我的应用--应用详情--应用信息--高级信息--授权设置--应用回调页进行设置和查看，
        // 应用回调页不可为空
        weibo.authorize(LoginScreen.this, new AuthDialogListener());
        loginflag = false;
    }

    private void loginRenren() {

        renren = new Renren(API_RENREN_KEY, SECRET_RENREN_KEY, APP_RENREN_ID, LoginScreen.this);

        final RenrenAuthListener listener = new RenrenAuthListener() {

            public void onComplete(Bundle values) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (renren.isSessionKeyValid()) {

                            long uid = renren.getCurrentUid();

                            if (renren.getCurrentUid() > 0) {

                                binAccount(Long.toString(uid), TAG_RENREN);
                            }

                        } else {

                            ServiceManager.ToastShow("false");
                        }
                    }
                });
            }

            @Override
            public void onRenrenAuthError(RenrenAuthError renrenAuthError) {

            }

            @Override
            public void onCancelLogin() {

            }

            @Override
            public void onCancelAuth(Bundle values) {

            }
        };

        renren.logout(getApplicationContext());
        renren.authorize(LoginScreen.this, listener);
        loginflag = false;
    }

    private void loginTencent() {
        String oauthCallback = "null";
        String oauthConsumeKey = "801211475";
        String oauthConsumerSecret = "dcc70ad93fa9963bd7619279ef8bf3e5";

        oAuth = new OAuthV1(oauthCallback);
        oAuth.setOauthConsumerKey(oauthConsumeKey);
        oAuth.setOauthConsumerSecret(oauthConsumerSecret);

        // 关闭OAuthV1Client中的默认开启的QHttpClient。
        OAuthV1Client.getQHttpClient().shutdownConnection();

        // 为OAuthV1Client配置自己定义QHttpClient。
        OAuthV1Client.setQHttpClient(new QHttpClient());

        try {

            oAuth = OAuthV1Client.requestToken(oAuth);
            mFisrtKey = oAuth.getOauthTokenSecret();
        } catch (Exception e1) {

            e1.printStackTrace();
        }

        if (mFisrtKey.length() > 2) {

            Intent intent = new Intent(LoginScreen.this, OAuthV1AuthorizeWebView.class);// 创建Intent，使用WebView让用户授权
            intent.putExtra("oauth", oAuth);
            startActivityForResult(intent, 1);
        } else {
            Toast.makeText(getApplicationContext(), "登录腾讯出错", 1).show();
        }
        loginflag = false;
    }

    class AuthDialogListener implements WeiboDialogListener {

        @Override
        public void onComplete(Bundle values) {

            String uid = values.getString("uid");
            binAccount(uid, TAG_SINA);
        }

        @Override
        public void onError(DialogError e) {

        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onWeiboException(WeiboException e) {

        }
    }

    /*
     * 腾讯的返回操作 通过读取OAuthV1AuthorizeWebView返回的Intent，获取用户授权后的验证码
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == OAuthV1AuthorizeWebView.RESULT_CODE) {

				// 从返回的Intent中获取验证码
				oAuth = (OAuthV1) data.getExtras().getSerializable("oauth");

				try {

					oAuth = OAuthV1Client.accessToken(oAuth);

					UserAPI userAPI = new UserAPI(OAuthConstants.OAUTH_VERSION_1);
					String response = userAPI.info(oAuth, "json");// 获取用户信息
					JSONObject jsonObject = new JSONObject(response).getJSONObject("data");
					String openid = null;
					openid = jsonObject.getString("openid");

					if (openid.length() > 5) {

						binAccount(openid, TAG_TENGXUN);

					} else {

						ServiceManager.ToastShow("腾讯授权失败");
					}
					Log.i("tenxun", openid);

					/*
					 * 注意：此时oauth中的Oauth_token和Oauth_token_secret将发生变化，用新获取到的
					 * 已授权的access_token和access_token_secret替换之前存储的未授权的request_token
					 * 和request_token_secret.
					 */
				} catch (Exception e) {
					ScheduleApplication.logException(getClass(),e);
				}

				OAuthV1Client.getQHttpClient().shutdownConnection();// 腾讯微博关闭连接
			}
		}
    }

    // uid 表示要绑定的帐号的id，tag 表示哪一种帐号：新浪，人人，腾讯
    public void binAccount(String uid, int tag) {

        String isBin = ServiceManager.getServerInterface().Bin_login(Integer.toString(tag), uid);
        if (isBin.contains("user_id")) {
            Message msg = new Message();
            msg.obj = isBin;
            handler.sendMessage(msg);
            // handler.sendEmptyMessage(LOGIN_SUCCESS);
            RegisterScreen.writeUserinfo(isBin, HttpUtils.GetCookie());

        } else {

            Intent intent = new Intent();
            intent.setClass(LoginScreen.this, RegisterScreen.class);
            intent.putExtra("uid", uid);
            intent.putExtra("type", tag);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.left_out);
    }

    public void loginSubmit() {

        TelephonyManager tm = (TelephonyManager)(LoginScreen.this)
                .getSystemService(Context.TELEPHONY_SERVICE);
        final String imsi = tm.getSubscriberId();
        final String username = login_username.getText().toString();
        final String password = login_password.getText().toString();

        if (username.length() == 0) {
            ServiceManager.ToastShow("用户名不能为空!");
            loginflag = false;
            return;
        }

        if (password.length() == 0) {
            ServiceManager.ToastShow("密码不能为空");
            loginflag = false;
            return;
        }

        ScheduleApplication.LogD(LoginScreen.class, "loginSubmit start ");

        // 判断网络是否链接
        if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
            ScheduleApplication.LogD(LoginScreen.class, "loginSubmit ok ");

            mpDialog.show();
            ScheduleApplication.LogD(getClass(), "isshowing  " + mpDialog.isShowing());

            new Thread() {
                public void run() {
                    String passwordCrypt = ServiceManager.enCrypt(password);
                    String ret = ServiceManager.getServerInterface().login(username, passwordCrypt,
                            imsi);
                    ScheduleApplication.LogD(LoginScreen.class, "run() ret = " + ret);

                    Message msg = new Message();
                    msg.obj = ret;
                    handler.sendMessage(msg);
                };
            }.start();
        } else {

            loginflag = false;
            ScheduleApplication.LogD(LoginScreen.class, "loginSubmit off ");
            Toast.makeText(getApplicationContext(), "链接网络失败，请检查网络", 1).show();
        }
        ScheduleApplication.LogD(LoginScreen.class, "loginSubmit end ");
    }

}
