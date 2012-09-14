package com.archermind.schedule.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Image.SmartImageView;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Screens.FriendsDyamicScreen;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Utils.ServerInterface;

public class AsyncScheduleLoader {

	String TAG = "AsyncScheduleLoader";

	public AsyncScheduleLoader() {

	}

	private class CommentItem {
		private SmartImageView avatar;

		private TextView content;

		private TextView time;
	}

	public void loadSchedule(final Context context,
			final LayoutInflater inflater, final int t_id,
			final ScheduleCallback scheduleCallback) {

		final Handler handler = new Handler() {
			public void handleMessage(Message message) {

				scheduleCallback.scheduleCallback((LinearLayout) message.obj,
						t_id);
			}
		};

		new Thread() {
			@Override
			public void run() {

				Cursor slaveCursors = ServiceManager.getDbManager()
						.querySlaveShareSchedules(t_id);
				Log.i(TAG, "loadSchedule" + t_id);
				LinearLayout layout = new LinearLayout(context);
				layout.setOrientation(LinearLayout.VERTICAL);
				if ((slaveCursors != null) && (slaveCursors.getCount() > 0)) {
					for (slaveCursors.moveToFirst(); !slaveCursors
							.isAfterLast(); slaveCursors.moveToNext()) {
						String nick = "";
						View commentView = inflater.inflate(
								R.layout.feed_comments_item, null);
						final CommentItem commentItem = new CommentItem();
						commentItem.avatar = (SmartImageView) commentView
								.findViewById(R.id.comment_profile_photo);
						commentItem.avatar
								.setBackgroundResource(R.drawable.avatar);
						final int friend_id = slaveCursors
								.getInt(slaveCursors
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_USER_ID));

						int loop = 0;
						while (loop < 2) {

							final Cursor friendCursor = ServiceManager
									.getDbManager().queryFriend(friend_id);

							if (friendCursor != null
									&& friendCursor.getCount() > 0) {
								loop = 2;
								friendCursor.moveToFirst();
								if (friend_id == ServiceManager.getUserId()) {
									nick = "我";
								} else {
									nick = friendCursor
											.getString(friendCursor
													.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_NICK));
								}

								handler.post(new Runnable() {
									@Override
									public void run() {
										if (friend_id == ServiceManager
												.getUserId()) {
											commentItem.avatar.setImageUrl(
													ServiceManager
															.getAvator_url(),
													R.drawable.avatar,
													R.drawable.avatar);
										} else {
											commentItem.avatar.setImageUrl(
													friendCursor
															.getString(friendCursor
																	.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_PHOTO_URL)),
													R.drawable.avatar,
													R.drawable.avatar);
										}
										friendCursor.close();
									}
								});

							} else {

								ScheduleApplication.LogD(AsyncScheduleLoader.class, " 是好友的好友 friend_id = "+friend_id);								
								loop++;
								// 本地不存在，去服务器下载数据，获取好友的好友的头像以及昵称，插入本地
								String jsonString = ServiceManager
										.getServerInterface()
										.findUserInfobyUserId(
												Integer.toString(friend_id));
								ContentValues values = null;
								ScheduleApplication.LogD(AsyncScheduleLoader.class, " 是好友的好友 friend_id = "+friend_id +"data is "+jsonString);								
								
								if (jsonString != null
										&& !"".equals(jsonString)) {
									if (jsonString.indexOf("tel") >= 0) {// 防止返回错误码
										try {
											JSONArray jsonArray = new JSONArray(
													jsonString);
											ScheduleApplication.LogD(
													FriendsDyamicScreen.class,
													jsonString
															+ jsonArray
																	.length());
											for (int i = 0; i < jsonArray
													.length(); i++) {
												JSONObject jsonObject = (JSONObject) jsonArray
														.opt(i);
												String friend_tel = jsonObject
														.getString("tel");
												String friend_nick = jsonObject
														.getString("nick");
												String photo_url = jsonObject
														.getString("photo_url");

												values = new ContentValues();
												values.put(
														DatabaseHelper.ASCHEDULE_FRIEND_ID,
														friend_id);
												values.put(
														DatabaseHelper.ASCHEDULE_FRIEND_TYPE,
														Constant.FriendType.friends_friends);
												values.put(
														DatabaseHelper.ASCHEDULE_FRIEND_NUM,
														friend_tel);
												values.put(
														DatabaseHelper.ASCHEDULE_FRIEND_NICK,
														friend_nick);
												values.put(
														DatabaseHelper.ASCHEDULE_FRIEND_PHOTO_URL,
														photo_url);
												values.put(
														DatabaseHelper.ASCHEDULE_FRIEND_NAME,
														ServiceManager
																.getDbManager()
																.queryNameByTel(
																		friend_tel));
												ServiceManager.getDbManager()
														.addFriend(values);
												ScheduleApplication.LogD(AsyncScheduleLoader.class, "插入数据成功");								
												
											}
										} catch (JSONException e) {
											// TODO Auto-generated catch block

										}
									}

								}
								friendCursor.close();
							}

						}

						if (friend_id == ServiceManager.getUserId()) {
							nick = "我";
							handler.post(new Runnable() {
								@Override
								public void run() {
									commentItem.avatar.setImageUrl(
											ServiceManager.getAvator_url(),
											R.drawable.avatar,
											R.drawable.avatar);
								}
							});
						}
						commentItem.content = (TextView) commentView
								.findViewById(R.id.comment_body);
						commentItem.time = (TextView) commentView
								.findViewById(R.id.comment_time);
						commentItem.content
								.setText(nick
										+ ":"
										+ slaveCursors.getString(slaveCursors
												.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT)));
						long commentTime = slaveCursors
								.getLong(slaveCursors
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME));
						commentItem.time.setText(DateTimeUtils.time2String(
								"yyyy.MM.dd hh:mm:ss", commentTime));
						layout.addView(commentView);
					}
				}

				Message message = handler.obtainMessage(0, layout);
				handler.sendMessage(message);

				slaveCursors.close();
			}

		}.start();
	}

	public interface ScheduleCallback {

		public void scheduleCallback(LinearLayout listView, int t_id);

	}

}
