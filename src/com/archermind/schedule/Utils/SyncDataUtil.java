package com.archermind.schedule.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.EventTypes;
import com.archermind.schedule.Model.Friend;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Screens.FriendsDyamicScreen;
import com.archermind.schedule.Services.ServiceManager;

public class SyncDataUtil {

	public static boolean getSchedulesFromWeb(String userId) {
		return getSchedulesFromWeb(userId, false);
	}

	public static boolean getSchedulesFromWeb(String userId, boolean isService) {
		boolean flag = false;
		if (NetworkUtils.getNetworkState(ScheduleApplication.getContext()) != NetworkUtils.NETWORN_NONE
				&& ServiceManager.getUserId() != 0) {
			Cursor updateTimeCursor = ServiceManager.getDbManager()
					.queryShareSchedules();
			String time;
			if (updateTimeCursor != null && updateTimeCursor.getCount() > 0) {
				updateTimeCursor.moveToFirst();
				time = updateTimeCursor
						.getString(updateTimeCursor
								.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_UPDATE_TIME));
			} else {
				time = "0";
			}
			updateTimeCursor.close();
			String jsonString = ServiceManager.getServerInterface()
					.syncFriendShare(userId, time);
			ScheduleApplication.LogD(SyncDataUtil.class, "userid:" + userId);
			ScheduleApplication.LogD(
					SyncDataUtil.class,
					"time:"
							+ time
							+ DateTimeUtils.time2String("yyyy.MM.dd HH:mm:ss",
									Long.parseLong(time)));
			ScheduleApplication.LogD(SyncDataUtil.class, "jsonString:"
					+ jsonString);
			
			if (jsonString.contains("host")) {
				try {
					JSONArray jsonArray = new JSONArray(jsonString);
					ContentValues contentvalues;
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
						contentvalues = new ContentValues();
						String t_id = jsonObject.getString("TID");
						contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_T_ID,
								t_id);
						String order = jsonObject.getString("num");
						contentvalues.put(DatabaseHelper.COLUMN_SCHEDULE_ORDER,
								order);
						contentvalues.put(
								DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID,
								jsonObject.getString("host"));
						String user_id = jsonObject.getString("user_id");
						contentvalues
								.put(DatabaseHelper.COLUMN_SCHEDULE_USER_ID,
										user_id);
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
						contentvalues.put(
								DatabaseHelper.COLUMN_SCHEDULE_CONTENT,
								jsonObject.getString("content"));
						ScheduleApplication.LogD(SyncDataUtil.class, "user_id:"
								+ user_id + " order:" + order);
						// if (user_id.equals(userId) && "0".equals(order)) {
						// ScheduleApplication.LogD(
						// SyncDataUtil.class,
						// "该帖子是自己发的主贴，在好友动态里面不要显示:"
						// + jsonObject.getString("content"));
						// continue;
						// }
						if (!ServiceManager.getDbManager().isInShareSchedules(
								t_id)) {
							if (isService) {
								ServiceManager
										.getEventservice()
										.onUpdateEvent(
												new EventArgs(
														EventTypes.SERVICE_TIP_ON));
							}
							ServiceManager.getDbManager().insertShareSchedules(
									contentvalues);
							flag = true;
						} else {
							ServiceManager.getDbManager().updateShareSchedules(
									contentvalues, t_id);
							ScheduleApplication.LogD(SyncDataUtil.class,
									"重复的TID：" + t_id);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}else {
				
				ScheduleApplication.LogD(SyncDataUtil.class, "数据下载失败");
			}
		}
		return flag;
	}

	public static Friend makeFriendFromInet(String id, int type) {
		if (NetworkUtils.getNetworkState(ScheduleApplication.getContext()) != NetworkUtils.NETWORN_NONE) {
			String jsonString = ServiceManager.getServerInterface()
					.findUserInfobyUserId(id);
			ContentValues values = null;
			Friend friend = null;
			if (jsonString != null && !"".equals(jsonString)) {
				if (jsonString.indexOf("tel") >= 0) {// 防止返回错误码
					try {
						JSONArray jsonArray = new JSONArray(jsonString);
						ScheduleApplication.LogD(FriendsDyamicScreen.class,
								jsonString + jsonArray.length());
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsonObject = (JSONObject) jsonArray
									.opt(i);
							String tel = jsonObject.getString("tel");
							String nick = jsonObject.getString("nick");
							String photo_url = jsonObject
									.getString("photo_url");
							values = new ContentValues();
							values.put(DatabaseHelper.ASCHEDULE_FRIEND_ID, id);
							values.put(DatabaseHelper.ASCHEDULE_FRIEND_TYPE,
									type);
							values.put(DatabaseHelper.ASCHEDULE_FRIEND_NUM, tel);
							values.put(DatabaseHelper.ASCHEDULE_FRIEND_NICK,
									nick);
							values.put(
									DatabaseHelper.ASCHEDULE_FRIEND_PHOTO_URL,
									photo_url);
							values.put(DatabaseHelper.ASCHEDULE_FRIEND_NAME,
									ServiceManager.getDbManager()
											.queryNameByTel(tel));
							ServiceManager.getDbManager().addFriend(values);
							friend = new Friend();
							friend.setNick(nick);
							friend.setHeadImagePath(photo_url);
							return friend;
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						return null;
					}
				}
			}
		}
		return null;
	}
}
