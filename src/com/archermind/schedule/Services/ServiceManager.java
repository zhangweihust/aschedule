
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
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.EventTypes;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Screens.FriendsDyamicScreen;
import com.archermind.schedule.Screens.HomeScreen;
import com.archermind.schedule.Screens.RegisterScreen;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.Contact;
import com.archermind.schedule.Utils.NetworkUtils;
import com.archermind.schedule.Utils.ServerInterface;

public class ServiceManager extends Service implements OnClickListener{

    private static final EventService eventService = new EventService();

    private static boolean started;

    private static Timer mTimer;

    private static DatabaseManager dbManager = new DatabaseManager(ScheduleApplication.getContext());

    private static ServerInterface serverInerface = new ServerInterface();
    
    private static HomeScreen homeScreen;

    private static Contact contact = new Contact();

    private static int user_id = 0;

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
            getSchedulesFromWeb(String.valueOf(ServiceManager.getUserId()));        
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
        SharedPreferences sp = getSharedPreferences(RegisterScreen.USER_INFO,
                Context.MODE_WORLD_READABLE);
        user_id = sp.getInt(RegisterScreen.USER_ID, 0);
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
    }

    public static int getUserId() {
        return user_id;
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

    private void getSchedulesFromWeb(String userId) {
        if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
        	Cursor updateTimeCursor = ServiceManager.getDbManager().queryShareSchedules();
			String time;
			if(updateTimeCursor != null && updateTimeCursor.getCount() > 0 ){
				updateTimeCursor.moveToFirst();
				time = updateTimeCursor.getString(updateTimeCursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_UPDATE_TIME));
			} else {
				time = "0";
			}
			updateTimeCursor.close();
            String jsonString = ServiceManager.getServerInterface().syncFriendShare(userId, time);
            try {

                JSONArray jsonArray = new JSONArray(jsonString);
                ScheduleApplication
                        .LogD(FriendsDyamicScreen.class, jsonString + jsonArray.length());
                ContentValues contentvalues;

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject)jsonArray.opt(i);
                    contentvalues = new ContentValues();
                    String t_id = jsonObject.getString("TID");
                    contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_T_ID, t_id);
                    contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER,
                            jsonObject.getString("num"));
                    contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID,
                            jsonObject.getString("host"));
                    contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_USER_ID,
                            jsonObject.getString("user_id"));
                    contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE,
                            jsonObject.getString("type"));
                    contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
                            jsonObject.getString("start_time"));
                    contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_UPDATE_TIME,
                            jsonObject.getString("update_time"));
                    contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CITY,
                            jsonObject.getString("city"));
                    contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT,
                            jsonObject.getString("content"));

                    if (!ServiceManager.getDbManager().isInShareSchedules(t_id)) {

                        eventService.onUpdateEvent(new EventArgs(EventTypes.SERVICE_TIP_ON));
                        ServiceManager.getDbManager().insertShareSchedules(contentvalues);
                    
                    } else {
                    
                        ServiceManager.getDbManager().updateShareSchedules(contentvalues, t_id);
                        ScheduleApplication.LogD(FriendsDyamicScreen.class, "重复的TID：" + t_id);                    
                    }
                }
            } catch (JSONException e) {

                e.printStackTrace();
            }
        }
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
				ServiceManager.getEventservice().onUpdateEvent(new EventArgs(EventTypes.CONTACT_SYNC_SUCCESS));
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


}
