
package com.archermind.schedule.Services;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.EventTypes;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Screens.FriendsDyamicScreen;
import com.archermind.schedule.Screens.RegisterScreen;
import com.archermind.schedule.Utils.Contact;
import com.archermind.schedule.Utils.NetworkUtils;
import com.archermind.schedule.Utils.ServerInterface;

public class ServiceManager extends Service {

    private static final EventService eventService = new EventService();

    private static boolean started;

    private static Timer mTimer;

    private static DatabaseManager dbManager = new DatabaseManager(ScheduleApplication.getContext());

    private static ServerInterface serverInerface = new ServerInterface();

    private static Contact contact = new Contact();

    private static int user_id = 0;

    private static Toast toast;

    private static long mGetDataTime = 5 * 60 * 1000;

    private static long mTaskTime = 5 * 60 * 1000;

    private String TAG = "ServiceManager";

    private MyTimerTask myTask;

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
            getSchedulesFromWeb("3", "1343203371");

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

    private void getSchedulesFromWeb(String userId, String time) {
        if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {

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

}
