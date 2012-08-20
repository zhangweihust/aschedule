
package com.archermind.schedule.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.EventTypes;
import com.archermind.schedule.Model.UserInfoData;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Screens.FriendsDyamicScreen;
import com.archermind.schedule.Screens.HomeScreen;
import com.archermind.schedule.Screens.RegisterScreen;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.Contact;
import com.archermind.schedule.Utils.NetworkUtils;
import com.archermind.schedule.Utils.ServerInterface;
import com.archermind.schedule.Utils.SyncDataUtil;

public class ServiceManager extends Service implements OnClickListener{

    private static final EventService eventService = new EventService();

    private static boolean started;

    private static Timer mTimer;

    private static DatabaseManager dbManager = new DatabaseManager(ScheduleApplication.getContext());

    private static ServerInterface serverInerface = new ServerInterface();
    
    private static HomeScreen homeScreen;

    private static Contact contact = new Contact();

    private static int user_id = 0;
    private static String cookie = "";
    private static String avator_url = "";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor spEditor;

    private static Toast toast;

    private static long mGetDataTime = 5 * 60 * 1000;

    private static long mTaskTime = 5 * 60 * 1000;

    private String TAG = "ServiceManager";

    private MyTimerTask myTask;

    private List<String> msg_adds = new ArrayList<String>();
    
    private List<String> msg_refuses = new ArrayList<String>();
    
    private List<String> msg_accepets = new ArrayList<String>();
    
    private List<String> msg_sys = new ArrayList<String>();
    
    private Handler handler = new Handler(){
    	public void handleMessage(Message msg) {
    		for(int i = 0; i < msg_adds.size(); i++){
    			showDialog(msg_adds.get(i));
    		}
    		for(int i = 0; i < msg_accepets.size(); i++){
    			makeFriendFromInet(msg_accepets.get(i),Constant.FriendType.friend_yes);
    			serverInerface.acceptConfirm(String.valueOf(getUserId()), msg_accepets.get(i));
    		}
    	};
    };
    
	   private Dialog dialog;
	    public void showDialog(String id){
	    	dialog = new Dialog(homeScreen,R.style.WeatherDialog);
			dialog.setContentView(R.layout.friend_dialog);
			Button accept_friend = (Button) dialog.findViewById(R.id.accept_friend);
			Button refuse_friend = (Button) dialog.findViewById(R.id.refuse_friend);
			accept_friend.setOnClickListener(this);
			refuse_friend.setOnClickListener(this);
			accept_friend.setTag(id);
			refuse_friend.setTag(id);
			
			Display display = homeScreen.getWindowManager().getDefaultDisplay();
			int screenWidth = display.getWidth();
			Window window = dialog.getWindow(); // 得到对话框
			WindowManager.LayoutParams wl = window.getAttributes();
			wl.width = screenWidth * 7 / 8;
			wl.gravity = Gravity.CENTER; // 设置重力
			window.setAttributes(wl);
			dialog.show();
	    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            // TODO Auto-generated method stub

            if (mTaskTime != mGetDataTime) {
                mTimer.cancel();
                Log.i(TAG, "handlemessage mTaskTime is " + mTaskTime + " mGetDataTime is "
                        + mGetDataTime);
                mTimer = new Timer();
                mTaskTime = mGetDataTime;
                myTask = new MyTimerTask();
                mTimer.schedule(myTask, mTaskTime, mTaskTime);
            }
            SyncDataUtil.getSchedulesFromWeb(String.valueOf(ServiceManager.getUserId()), true);        
            makeFriendFromInet();
            eventService.onUpdateEvent(new EventArgs(EventTypes.CONTACT_SYNC_SUCCESS));
            Log.i(TAG, "get data in service!the time is " + mTaskTime);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        toast = Toast.makeText(getApplicationContext(), "service start", Toast.LENGTH_SHORT);
        // toast.show();
        sharedPreferences = getSharedPreferences(UserInfoData.USER_INFO,
                Context.MODE_WORLD_READABLE);
        spEditor = sharedPreferences.edit();
        user_id = Integer.parseInt(sharedPreferences.getString(UserInfoData.USER_ID, "0"));
        cookie = sharedPreferences.getString(UserInfoData.COOKIE, "");
        avator_url = sharedPreferences.getString(UserInfoData.PHOTO_URL, "");
        mTimer = new Timer();
        myTask = new MyTimerTask();
        mTimer.schedule(myTask, mTaskTime, mTaskTime);

        Log.i(TAG, "oncreate set time is " + mTaskTime);
    }

    public static void setGetDataTime(long getDataTime) {

        Log.i("ServiceManager ", " setGetDataTime is  " + getDataTime);
        mGetDataTime = getDataTime;

        // mTimer.schedule(mTimerTask, mTaskTime, mTaskTime);
    }

    public static boolean start() {

        if (ServiceManager.started) {
            return true;
        }

        // start Android service
        ScheduleApplication.getContext().startService(
                new Intent(ScheduleApplication.getContext(), ServiceManager.class));

        boolean success = true;

        success &= eventService.start();
        dbManager.open();
        if (!success) {
            ScheduleApplication.LogD(ServiceManager.class, "Failed to start services");
            return false;
        }

        ServiceManager.started = true;

        return true;
    }

    public static boolean stop() {
        if (!ServiceManager.started) {
            return true;
        }

        // stops Android service
        ScheduleApplication.getContext().stopService(
                new Intent(ScheduleApplication.getContext(), ServiceManager.class));
        boolean success = true;
        success &= eventService.stop();
        dbManager.close();
        if (!success) {
            ScheduleApplication.LogD(ServiceManager.class, "Failed to stop services");
        }
        ServiceManager.started = false;
        return success;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        exit();
        super.onDestroy();
    }

    public static EventService getEventservice() {
        return eventService;
    }

    public static DatabaseManager getDbManager() {
        return dbManager;
    }

    public static Contact getContact() {
        return contact;
    }

    public static boolean isStarted() {
        return started;
    }

    public static ServerInterface getServerInterface() {
        return serverInerface;
    }

    public static void setUserId(int userid) {
        user_id = userid;
        setSPUserInfo(UserInfoData.USER_ID,String.valueOf(userid));
    }

    public static int getUserId() {
        return user_id;
    }
    
    public static void setCookie(String cookiestr)
    {
    	cookie = cookiestr;
    	setSPUserInfo(UserInfoData.COOKIE,cookiestr);
    }
    
    public static String getCookie()
    {
    	return cookie;
    }

    public static void ToastShow(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast.setText(message);
        toast.show();
    }

    public static void exit() {

        stop();
        System.exit(0);
    }
    
    public static void setHomeScreen(HomeScreen mHomeScreen){
    	homeScreen = mHomeScreen;
    }

    
    private void makeFriendFromInet(){
		if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
			
			String jsonString = ServiceManager.getServerInterface().getMessage(String.valueOf(ServiceManager.getUserId()));
		
			if(jsonString != null && !"".equals(jsonString)){
				if(jsonString.indexOf("user_id") >= 0){//防止返回错误码
					
					try {
						JSONArray jsonArray = new JSONArray(jsonString);
						ScheduleApplication.LogD(FriendsDyamicScreen.class, jsonString
								+ jsonArray.length());
							JSONObject jsonObject = (JSONObject) jsonArray.opt(0);
							String user_id = jsonObject.getString("user_id");
							String msg_add = jsonObject.getString("msg_add");
							String msg_refuse = jsonObject.getString("msg_refuse");
							String msg_accepet = jsonObject.getString("msg_accept");
							String msg_sy = jsonObject.getString("msg_sys");
							
							if(msg_add.length() == 0 && msg_refuse.length() == 0 && msg_sy.length() == 0)
								return ;
								
							String[] msg_add_array = msg_add.split(",");
							for(int i = 0; i < msg_add_array.length; i++){
								msg_adds.add(msg_add_array[i]);
							}
							
							String[] msg_refuse_array = msg_refuse.split(",");
							for(int i = 0; i < msg_refuse_array.length; i++){
								msg_refuses.add(msg_refuse_array[i]);
							}
							
							String[] msg_accepet_array = msg_accepet.split(",");
							for(int i = 0; i < msg_accepet_array.length; i++){
								msg_accepets.add(msg_accepet_array[i]);
							}
							
							String[] msg_sys_array = msg_sy.split(",");
							for(int i = 0; i < msg_sys_array.length; i++){
								msg_sys.add(msg_sys_array[i]);
							}
							handler.sendEmptyMessage(0);
							
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}																		
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		String id = (String) v.getTag();
		switch(v.getId()){
		case R.id.accept_friend:
			if(0 == serverInerface.acceptFriend(String.valueOf(ServiceManager.getUserId()), id)){
				//成功添加好r友
				makeFriendFromInet(id,Constant.FriendType.friend_yes);
				eventService.onUpdateEvent(new EventArgs(EventTypes.CONTACT_SYNC_SUCCESS));
			}
			
			break;
		case R.id.refuse_friend:
			if(0 == serverInerface.refuseFriend(String.valueOf(ServiceManager.getUserId()), id)){
				//拒绝好友
			}
			break;
		}
		dialog.dismiss();
	}
	
	
	   private void makeFriendFromInet(String id, int type){
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				
				String jsonString = ServiceManager.getServerInterface().findUserInfobyUserId(id);
				ContentValues values = null;
				if(jsonString != null && !"".equals(jsonString)){
					if(jsonString.indexOf("tel") >= 0){//防止返回错误码
						try {
							JSONArray jsonArray = new JSONArray(jsonString);
							ScheduleApplication.LogD(FriendsDyamicScreen.class, jsonString
									+ jsonArray.length());
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
								String tel = jsonObject.getString("tel");
								String nick = jsonObject.getString("nick");
								String photo_url = jsonObject.getString("photo_url");
								
									 
								 values = new ContentValues();
								 values.put(DatabaseHelper.ASCHEDULE_FRIEND_ID, id);
								 values.put(DatabaseHelper.ASCHEDULE_FRIEND_TYPE, type);
								 values.put(DatabaseHelper.ASCHEDULE_FRIEND_NUM, tel);
								 values.put(DatabaseHelper.ASCHEDULE_FRIEND_NICK, nick);
								 values.put(DatabaseHelper.ASCHEDULE_FRIEND_PHOTO_URL, photo_url);
								 values.put(DatabaseHelper.ASCHEDULE_FRIEND_PHOTO_URL, dbManager.queryNameByTel(tel));
								 dbManager.addFriend(values);								
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			}
		}


	   public static void setSPUserInfo(String feild,String data)
	   {
		   spEditor.putString(feild, data);       
           spEditor.commit();
	   }
	   
	   public static String getSPUserInfo(String feild)
	   {
	       return sharedPreferences.getString(feild,"");
	   }
	   
	   public static boolean isUserLogining(int userid)
	   {
		   boolean ret = false;
		   if (NetworkUtils.getNetworkState(ScheduleApplication.getContext()) != NetworkUtils.NETWORN_NONE)
		   {
			   /* 发送userid给服务器，服务器判断userid的用户是否登录 */
			   if (serverInerface.session_check(String.valueOf(user_id)).equals("0"))
			   {
				   ret = true;
			   }
		   }
		   
		   return ret;
	   }
	   
	public static String getAvator_url() {
		return avator_url;
	}
	   
	   
}
