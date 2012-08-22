package com.archermind.schedule.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.EventTypes;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Screens.FriendsDyamicScreen;
import com.archermind.schedule.Services.ServiceManager;

public class SyncDataUtil {
	
	
	public static void getSchedulesFromWeb(String userId) {
		getSchedulesFromWeb(userId, false);
	}
	
	public static void getSchedulesFromWeb(String userId, boolean isService) {
		if (NetworkUtils.getNetworkState(ScheduleApplication.getContext()) != NetworkUtils.NETWORN_NONE && ServiceManager.getUserId() != 0) {
			Cursor updateTimeCursor = ServiceManager.getDbManager().queryShareSchedules();
			String time;
			if(updateTimeCursor != null && updateTimeCursor.getCount() > 0 ){
				updateTimeCursor.moveToFirst();
				time = updateTimeCursor.getString(updateTimeCursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_UPDATE_TIME));
			} else {
				time = "0";
			}
			updateTimeCursor.close();
			String jsonString = ServiceManager.getServerInterface()
					.syncFriendShare(userId, time);
			ScheduleApplication.LogD(FriendsDyamicScreen.class, "userid:" + userId);
			ScheduleApplication.LogD(FriendsDyamicScreen.class, "time:" +  time + DateTimeUtils.time2String("yyyy.MM.dd HH:mm:ss", Long.parseLong(time)));
			ScheduleApplication.LogD(FriendsDyamicScreen.class, "jsonString:" + jsonString);
			try {
				JSONArray jsonArray = new JSONArray(jsonString);
				ScheduleApplication.LogD(FriendsDyamicScreen.class, jsonString
						+ jsonArray.length());
				ContentValues contentvalues;
				if(jsonArray.length() > 0){
					ScheduleApplication.LogD(FriendsDyamicScreen.class, jsonArray.length() + "清除本地的默认初始数据");
					ServiceManager.getDbManager().deleteShareDefaultSchedules();
				}
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
					contentvalues = new ContentValues();
					String t_id = jsonObject.getString("TID");
					contentvalues
							.put(DatabaseHelper.COLUMN_SCHEDULE_T_ID, t_id);
					String order = jsonObject.getString("num");
					contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER, order);
					contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID,
							jsonObject.getString("host"));
					String user_id = jsonObject.getString("user_id");
					contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_USER_ID, user_id);
					contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE,
							jsonObject.getString("type"));
					contentvalues.put(
							DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
							jsonObject.getString("start_time"));
					contentvalues.put(
							DatabaseHelper.COLUMN_SCHEDULE_UPDATE_TIME,
							jsonObject.getString("update_time"));
					contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CITY,
							jsonObject.getString("city"));
					contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT,
							jsonObject.getString("content"));
					ScheduleApplication.LogD(FriendsDyamicScreen.class, "user_id:" + user_id + " order:" + order);
					if(user_id.equals(userId) && "0".equals(order)){
						continue;
					}
					if (!ServiceManager.getDbManager().isInShareSchedules(t_id)) {
						if(isService){
							ServiceManager.getEventservice().onUpdateEvent(new EventArgs(EventTypes.SERVICE_TIP_ON));
						}
						ServiceManager.getDbManager().insertShareSchedules(
								contentvalues);
					} else {
						ServiceManager.getDbManager().updateShareSchedules(
								contentvalues, t_id);
						ScheduleApplication.LogD(FriendsDyamicScreen.class, "重复的TID：" + t_id);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static  void insertDefaultSchedules() {
		long time = 0;
		ContentValues contentvalues = new ContentValues();
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_T_ID, -1);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID, -100);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER, 0);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE, 1);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_DEFAULT,true);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
				System.currentTimeMillis());
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_UPDATE_TIME,
				time);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CITY, "武汉");
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT,
				"hi all 让我们一起微日程吧");
		ServiceManager.getDbManager().insertShareSchedules(contentvalues);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_T_ID, -4);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID, -1);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER, 1);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE, 2);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_DEFAULT,true);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
				System.currentTimeMillis());
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_UPDATE_TIME,
				time);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CITY, "北京");
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT, "恩，大家聚会方便多了");
		ServiceManager.getDbManager().insertShareSchedules(contentvalues);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_T_ID, -5);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID, -1);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER, 2);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_TYPE, 2);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_DEFAULT,true);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
				System.currentTimeMillis());
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_UPDATE_TIME,
				time);
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CITY, "北京");
		contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_CONTENT, "哈哈，这东东蛮好用的");
		ServiceManager.getDbManager().insertShareSchedules(contentvalues);
	}
}
