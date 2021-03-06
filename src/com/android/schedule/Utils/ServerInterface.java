package com.android.schedule.Utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.amtcloud.mobile.android.business.AmtApplication;
import com.amtcloud.mobile.android.business.AmtUserObj;
import com.amtcloud.mobile.android.business.MessageTypes;
import com.android.schedule.ScheduleApplication;
import com.android.schedule.Provider.DatabaseHelper;
import com.android.schedule.Services.ServiceManager;
import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Person;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.calendar.CalendarFeed;
import com.google.gdata.data.calendar.EventWho;
import com.google.gdata.data.extensions.When;
import com.google.gdata.data.extensions.Where;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class ServerInterface {

	public static final int SUCCESS = 0;

	public static final int ERROR_ACCOUNT_OR_PASSWORD_EMPTY = -1;

	public static final int ERROR_ACCOUNT_EXIST = -2;

	public static final int ERROR_SYNC_FAILED = -3;

	public static final int ERROR_UPLOAD_FAILED = -4;

	public static final int ERROR_DATABASE_INTERNAL = -5;

	public static final int ERROR_EMAIL_INVALID = -6;

	public static final int ERROR_PASSWORD_INVALID = -7;

	public static final int ERROR_NICKNAME_INVALID = -9;

	public static final int ERROR_TEL_INVALID = -10;

	public static final int ERROR_WEB_ERROR = -8;

	public static final int ERROR_SERVER_N0_REPLY = -100;

	public static final int ERROR_SERVER_INTERNAL = -101;

	public static final int ERROR_SERVER_REFUSE = -102;

	public static final int ERROR_PASSWORD_WRONG = -201;

	public static final int ERROR_USER_NOT_EXIST = -202;

	public static final int ERROR_USER_NOT_BIND = -203;

	public static final int ERROR_HTTP_UNKNOW = -500;

	public static final String app_id = "957ce0a81a0d4b4793c39471950ad298";

	public static final String app_secret = "94165c45d0664e68b7f2a7712734e957";

	// public static DatabaseManager mDatabaseManager= new
	// DatabaseManager(ServerInterfaceActivity.getContext());;

	// ----------------- 静 态 方 法、工 具 函 数 -----------------

	/**************************************
	 * 判断Email地址合法性 (32 byte)@(31 byte).(6 byte) true : 合法 false: 非法
	 ***************************************/

	public boolean isEmail(String email) {
		String emailPattern = "[a-zA-Z0-9][a-zA-Z0-9._-]{2,30}[a-zA-Z0-9]@[a-zA-Z0-9]{2,31}.[a-zA-Z0-9]{3,4}";
		boolean result = Pattern.matches(emailPattern, email);
		try {
			Pattern p1 = Pattern.compile("[\u4E00-\u9FB0]");
			Matcher m = p1.matcher(email);
			while (m.find()) {
				return false;
			}
			String regEx = "[`~!#$%^&*()+=|{}':;',\\[\\]<>/?~！#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
			Pattern p2 = Pattern.compile(regEx);
			Matcher m2 = p2.matcher(email);
			while (m2.find()) {
				return false;
			}
		} catch (Exception e) {
			ScheduleApplication.logException(ServerInterface.class, e);
		}
		return result;
	}

	/**************************************
	 * 判断密码合法性 长度与复杂度判断,特殊字符过滤 true : 合法 false: 非法
	 ***************************************/
	public boolean isPswdValid(String pass) {
		if(pass == null){
			return false;
		}
		if (pass.length() < 6
				|| (pass.length() > 15)
				|| !(Pattern.matches(".*[0-9]+.*", pass) && Pattern.matches(
						".*[a-zA-Z]+.*", pass))) {
			return false;
		} else {
			return true;
		}
	}

	/**************************************
	 * 判断isNickName合法性 长度小于10，且没有特殊字符
	 ***************************************/
	public boolean isNickName(String nickName) {
		try {
			String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
			Pattern p2 = Pattern.compile(regEx);
			Matcher m2 = p2.matcher(nickName);
			while (m2.find()) {
				return false;
			}
		} catch (Exception e) {
			ScheduleApplication.logException(ServerInterface.class, e);
		}
		if (nickName.length() == 0 || nickName.length() > 10) {
			return false;
		}
		return true;
	}

	// ------------------ 获取天气信息 -------------------
	// 传入参数 ：pro 省 city 市
	// 返回值 ：json的字符串，包含了城市4天的天气预报,或者为空
	public String getWeather(String prov, String city) {
		if (city == null || city.length() == 0) {
			return "";
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("city", city);
		map.put("prov", prov);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/getWeather");
		return ret;
	}

	// ----------------- 帐号注册、登录、管理、绑定 -----------------
	/**************************************
	 * 用户注册 return: 0 : success n : ...... ......
	 ***************************************/
	// public int login(String email,String pass){
	// return SUCCESS;
	// }
	//
	// public int register(String email,String pass/*,String imsi,...*/){
	// return SUCCESS;
	// }
	//
	// public int pswdModify(String oldpass,String newpass){
	// return SUCCESS;
	// }
	// 注意有新浪邮箱绑定的操作
	/*
	 * 用户注册函数 输入参数：登录的帐号类型（人人，腾讯，sina等等），登录的帐号 返回值 // 0 —— success
	 * //ERROR_USER_NOT_BIND —— 没有记录，需要绑定 //ERROR_WEB_ERROR ——
	 * 网络异常或其他原因注册失败，让用户重新尝试
	 */
	public int checkBinding(String type, String username) {
		// 查询数据库看是否有绑定的帐号，数据库表为登录类型，登录帐号，我们的注册帐号；我们的注册帐号栏如果不为空，则说明已经绑定，否则要进行绑定
		return SUCCESS;
	}

	/*
	 * 用户注册函数 输入参数：用户名，用户密码，imsi号，手机号，图像url，用户详细信息，注册时间 返回值 // 0 —— success
	 * //ERROR_ACCOUNT_OR_PASSWORD_EMPTY —— empty account or pswd
	 * //ERROR_ACCOUNT_EXIST —— account already exist //ERROR_WEB_ERROR ——
	 * 网络异常或其他原因注册失败
	 */
	public String register(String username, String password, String nickname,
			String imsi, String tel, String photo_id, String info, String type,
			String user_acc) {
		String ret = "";
		try {
			username = username.replace(" ", "");
			password = password.replace(" ", "");
			nickname = nickname.replace(" ", "");

			if (username.length() == 0 || password.length() == 0) {
				return String.valueOf(ERROR_ACCOUNT_OR_PASSWORD_EMPTY);// 帐号或密码为空
			}
			if (!isEmail(username)) {
				return String.valueOf(ERROR_EMAIL_INVALID);
			}
			if (!isPswdValid(password)) {
				return String.valueOf(ERROR_PASSWORD_INVALID);
			}
			if (!isNickName(nickname)) {
				return String.valueOf(ERROR_NICKNAME_INVALID);
			}

			String passwordCrypt = ServiceManager.enCrypt(password);
			Map<String, String> map = new HashMap<String, String>();
			map.put("user", username);
			map.put("password", passwordCrypt);
			map.put("nick", nickname);
			map.put("type", type);
			map.put("user_acc", user_acc);

			ret = HttpUtils.doPost(map,
					"http://arc.archermind.com/ci/index.php/aschedule/register");
		} catch (Exception e) {
			ScheduleApplication.logException(ServerInterface.class, e);
		}
		return ret;
	}

	/*
	 * 用户修改密码函数 输入参数：用户名，用户旧密码，新密码 返回值 // 0 —— success //ERROR_PASSWORD_WRONG ——
	 * check oldpswd is wrong //ERROR_WEB_ERROR —— 网络异常或其他原因修改密码失败
	 */
	public int modifyPassword(String username, String oldpassword,
			String newpassword) {
		if (username != null && oldpassword != null && newpassword != null) {
			username = username.replace(" ", "");
			oldpassword = oldpassword.replace(" ", "");
			newpassword = newpassword.replace(" ", "");
		}
		// 查询数据库判断旧密码是否正确，查询的时候如果异常返回-2
		if (!isPswdValid(newpassword)) {
			return ERROR_PASSWORD_INVALID;
		}
		if (newpassword.length() == 0 || oldpassword.length() == 0 /* ||旧密码不对 */) {
			return ERROR_PASSWORD_INVALID;
		}
		// 向数据库中插入更新的信息，并判断是否插入成功
		// if(不成功){
		// return ERROR_PASSWORD_WRONG;
		// }
		Map<String, String> map = new HashMap<String, String>();
		map.put("user", username);
		map.put("password", oldpassword);
		map.put("newpass", newpassword);;
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/pswdModify");
		if (ret.equals("0")) {
			return SUCCESS;
		} else if (ret.equals("-1") || ret.equals("-2")) {
			return ERROR_PASSWORD_WRONG;
		} else {
			return ERROR_HTTP_UNKNOW;
		}
	}

	/*
	 * 用户登录函数 输入参数：用户名，用户密码，imsi号 返回值 // 0 —— success //ERROR_USER_NOT_EXIST ——
	 * check user is not exist //ERROR_PASSWORD_WRONG —— check pswd is wrong
	 * //ERROR_WEB_ERROR —— 网络异常或其他原因注册失败
	 */
	public String login(String username, String password, String imsi) {
		if (username != null && password != null && imsi != null) {
			username = username.replace(" ", "");
			password = password.replace(" ", "");
			imsi = imsi.replace(" ", "");
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("user", username);
		map.put("password", password);
		// HttpUtils mhttp =new HttpUtils();
		// mhttp.SetMap(map);
		// mhttp.Seturl("http://player.archermind.com/ci/index.php/aschedule/login");
		// new Thread(mhttp).start();
		// 查询数据库判断用户是否存在，密码是否正确，imsi是否改变，查询的时候如果异常返回-3
		// if(用户不存在){
		// return ERROR_USER_NOT_EXIST;
		// }
		// if(密码不对){
		// return ERROR_PASSWORD_WRONG;
		// }
		// if(imsi改变){
		// 记录一个状态，插入数据库，通知做其他操作；
		// }
		// 向数据库中插入更新的信息，并判断是否插入成功
		// if(不成功){
		// return ERROR_WEB_ERROR;
		// }
		String ret = HttpUtils.doPost(map,
				"http://arc.archermind.com/ci/index.php/aschedule/login");
		// if (ret.equals("0") || ret.length() > 20) {
		// return SUCCESS;
		// } else {
		// System.out.println("login-----" + ret);
		// return -1;
		// }
		return ret;
		// return SUCCESS;
	}

	public String logout(String user_id) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		String ret = HttpUtils.doPost(map,
				"http://arc.archermind.com/ci/index.php/aschedule/logout");
		return ret;
	}

	/**************************************
	 * 找回密码 调用SDK发送密码至指定邮箱 0 : 已发送至指定邮箱 n : 错误码
	 ***************************************/
	public int findPswd(String email) {
		return SUCCESS;
	}

	/**************************************
	 * 绑定手机号 包含解除绑定(将手机号转换成16进制数： 15927130379 ——> 3B554B90B) 0 : 已绑定 n : 错误码
	 ***************************************/
	public int telBind(String user_id, String tel) {
		return SUCCESS;
	}

	public int QQBind() {
		return SUCCESS;
	}

	public int SinaBind() {
		return SUCCESS;
	}

	// ---------------------- 好友管理(邀请、删除) ----------------------
	// 获取系统消息
	public String getMessage(String user_id) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/getMessage");
		return ret;
	}

	// 根据user_id查询此用户的基本信息
	public String findUserInfobyUserId(String user_id) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/findUserInfobyUserId");
		return ret;
	}

	// 添加好友
	public int inviteFriend(String user_id, String duser_id) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("duser_id", duser_id);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/inviteFriend");
		// System.out.println("zhangguopeng+++++++"+ret);
		if (ret.equals("-2")) {
			return -2; // 已经是好友了
		} else if (ret.equals("-3")) {
			return -3; // 已经发过消息了
		} else if (ret.equals("0")) {
			return 0; // 消息发送成功
		} else {
			return -1; // 消息发送失败
		}
	}

	// 拒绝信息回执
	public int refuseConfirm(String user_id, String duser_id) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("duser_id", duser_id);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/refuseConfirm");
		if (ret.equals("0")) {
			return 0; // 消息发送成功
		} else {
			return -1; // 消息发送失败
		}
	}

	// 拒绝邀请
	public int refuseFriend(String user_id, String duser_id) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("duser_id", duser_id);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/refuseFriend");
		if (ret.equals("0")) {
			return 0; // 消息发送成功
		} else {
			return -1; // 消息发送失败
		}
	}

	// 接受邀请
	public int acceptFriend(String user_id, String duser_id) {
		ScheduleApplication.LogD(getClass(), "********************acceptFriend*********************");
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("duser_id", duser_id);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/acceptFriend");
		if (ret.equals("0")) {
			return 0; // 消息发送成功
		} else {
			return -1; // 消息发送失败
		}
	}

	// 接受信息回执
	public int acceptConfirm(String user_id, String duser_id) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("duser_id", duser_id);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/acceptConfirm");
		if (ret.equals("0")) {
			return 0; // 消息发送成功
		} else {
			return -1; // 消息发送失败
		}
	}

	public int removeFriend(String user_id, String duser_id) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("duser_id", duser_id);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/removeFriend");
		if (ret.equals("0")) {
			return SUCCESS; // 删除成功
		} else {
			return -2; // 删除失败
		}
	}

	public int shieldFriend(String user_id, String duser_id) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("duser_id", duser_id);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/removeFriend");
		if (ret.equals("0")) {
			ret = HttpUtils
					.doPost(map,
							"http://arc.archermind.com/ci/index.php/aschedule/shieldFriend");
			if (ret.equals("0")) {
				return SUCCESS; // 屏蔽成功
			} else {
				return -1;
			}
		} else {
			return -2; // 删除成功
		}
	}

	public String getFriendRel(String userid) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", userid);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/getFriendRel");
		return ret;
	}

	public String isfriendSchedule(String tel) {
		System.out.println("tel====" + tel);
		if (tel.length() != 11) {
			return "-1";
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("tel", tel);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/friendSchedule");
		return ret;
	}

	// 判断是否使用微日程
	public String checkUserSchedule(String user_id) {
		System.out.println("tel====" + user_id);
		// if (tel.length() != 11) {
		// return "-1";
		// }
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/checkUserSchedule");

		return ret;
	}

	// ----------------- 本地数据与服务器同步、消息推拉 -----------------
	/**************************************
	 * 上传联系人 将手机号转换成16进制数： 15927130379 ——> 3B554B90B 0 : 上传成功 n : 错误码
	 ***************************************/
	public int uploadContact(String user_id, String tellist) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("telist", tellist);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/uploadContact");
		if (ret.equals("0")) {
			return SUCCESS;
		} else {
			System.out.println("uploadContact-----" + ret);
			return -4;
		}
		// return (ret != 0) ? ret : SUCCESS;
		// return SUCCESS;
	}

	public String syncContact(String user_id) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/syncContact");
		return ret;
	}

	public static int uploadSchedule(String num, String host
	/*
	 * String userID, int share,type, start_time, update_time, city,
	 * notice_time, notice_period, notice_week, notice_start, notice_end,
	 * content...
	 */) {

		int result = 0;
		int tid = 0;
		int flag = 0;
		try {
			Cursor cursor = ServiceManager.getDbManager().queryLocalSchedules();
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {

						Map<String, String> map = new HashMap<String, String>();
						map.put("user_id",
								Integer.toString(cursor.getInt(cursor
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_USER_ID))));
						map.put("share",
								Integer.toString(cursor.getInt(cursor
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_SHARE))));
						map.put("type",
								Integer.toString(cursor.getInt(cursor
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_TYPE))));
						map.put("start_time",
								cursor.getString(cursor
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME)));
						// map.put("first_flag",
						// cursor.getString(cursor
						// .getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_FIRST_FLAG)));
						// map.put("city", "武汉");
						// notice_time 改为是否有闹钟的标识
						map.put("notice_time",
								Integer.toString(cursor.getInt(cursor
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_FLAG))));
						map.put("notice_period",
								cursor.getString(cursor
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD)));
						map.put("notice_week",
								cursor.getString(cursor
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_WEEK)));
						map.put("notice_end",
								cursor.getString(cursor
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_END)));
						map.put("content",
								cursor.getString(cursor
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT)));
						map.put("tid",
								Integer.toString(cursor.getInt(cursor
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_T_ID))));

						// 回帖。。。。
						map.put("num", num);
						map.put("host", host);
						String text = "";
						text = cursor
								.getString(cursor
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG));
						
						System.out
								.println("+++++++++++++++++++"
										+ cursor.getString(cursor
												.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG)));
						map.put("action",
								cursor.getString(cursor
										.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG)));
						if (text != null && text.equals("A")) {
							Cursor cursor1 = ServiceManager.getDbManager()
									.queryMaxTid();

							if (cursor1 != null) {
								if (cursor1.moveToFirst())
									tid = cursor1.getInt(0);
								// tid =tid +flag;
							}
							map.put("tid", Integer.toString(tid));
							System.out.println("++++++++++++++ tid=" + tid);
							cursor1.close();
							String ret = HttpUtils
									.doPost(map,
											"http://arc.archermind.com/ci/index.php/aschedule/uploadSchedule");
							// ContentValues cv = new ContentValues();
							try {
								result = Integer.parseInt(ret);
							} catch (Exception e) {
								result = -1;
							}
							if (result > 0) {
								ContentValues cv = new ContentValues();
								cv.put(DatabaseHelper.COLUMN_SCHEDULE_T_ID, ret);
								cv.put(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG,
										"N");
								ServiceManager
										.getDbManager()
										.updateScheduleById(
												cursor.getInt(cursor
														.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_ID)),
												cv);
								flag++;
							} else {
								result = -1;
								ServiceManager.ToastShow("同步用户动态失败，请检查网络设置。");
								continue;
							}

						} else if (text != null && text.equals("M")) {
							String ret = HttpUtils
									.doPost(map,
											"http://arc.archermind.com/ci/index.php/aschedule/uploadSchedule");
							try {
								result = Integer.parseInt(ret);
							} catch (Exception e) {
								result = -1;
							}
							if (result >= 0) {

								ContentValues cv = new ContentValues();
								cv.put(DatabaseHelper.COLUMN_SCHEDULE_UPDATE_TIME,
										System.currentTimeMillis());
								cv.put(DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG,
										"N");
								ServiceManager
										.getDbManager()
										.updateScheduleById(
												cursor.getInt(cursor
														.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_USER_ID)),
												cv);

							} else {
								result = -1;
								ServiceManager.ToastShow("同步用户动态失败，请检查网络设置。");
								continue;
							}
						} else if (text != null && text.equals("D")) {
							String ret = HttpUtils
									.doPost(map,
											"http://arc.archermind.com/ci/index.php/aschedule/uploadSchedule");
							try {
								result = Integer.parseInt(ret);
							} catch (Exception e) {
								result = -1;
							}
							if (result >= 0) {

								// 删除当前记录
								ServiceManager
										.getDbManager()
										.deleteScheduleById(
												cursor.getInt(cursor
														.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_ID)));

							} else {
								result = -1;
								ServiceManager.ToastShow("同步用户动态失败，请检查网络设置。");
								continue;
							}
						}
					} while (cursor.moveToNext());
				}
			}
			cursor.close();
		} catch (Exception e) {
			ScheduleApplication.logException(ServerInterface.class,e);
		}
		return result;
	}
	public static int deleteScheduleByTid(int tid) {
		Map<String, String> map = new HashMap<String, String>();
		int result = 0;
		map.put("tid", Integer.toString(tid));
		map.put("action", "D");
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/uploadSchedule");
		try {
			result = Integer.parseInt(ret);
		} catch (Exception e) {
			result = -1;
		}
		if (result >= 0) {
			ServiceManager.getDbManager().deleteScheduleByTid(tid);
		} else {
			result = -1;
		}
		return result;
	}

	public String syncFriendShare(String user_id, String updateTime) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("updateTime", updateTime);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/syncFriendShare");
		return ret;
	}

	public String syncSchedule(String user_id) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/syncSchedule");
		return ret;
	}

	public int syncGmail(/* xxxx */) {
		return SUCCESS;
	}

	/**
	 * 按时间范围查询event find all events in a specified date/time range.
	 * 
	 * @param service
	 *            An authenticated CalendarService object.
	 * @param startTime
	 *            Start time (inclusive) of events to print.
	 * @param endTime
	 *            End time (exclusive) of events to print.
	 * @throws MalformedURLException
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	public static ArrayList<CalendarEventEntry> dateRangeQuery(
			CalendarService service, DateTime startTime, DateTime endTime)
			throws MalformedURLException {

		List<CalendarEventEntry> allevents = new ArrayList<CalendarEventEntry>();
		String surl = "http://www.google.com/calendar/feeds/xiaopashu@gmail.com/private/full";
		URL url1 = new URL(surl);
		CalendarQuery myQuery = new CalendarQuery(url1);
		myQuery.setMinimumStartTime(startTime);
		myQuery.setMaximumStartTime(endTime);

		// Send the request and receive the response:
		CalendarEventFeed resultFeed;
		try {
			resultFeed = service.query(myQuery, CalendarEventFeed.class);

			for (int i = 0; i < resultFeed.getEntries().size(); i++) {
				CalendarEventEntry entry = resultFeed.getEntries().get(i);
				allevents.add(i, entry);
				// System.out.println("\t" + entry.getTitle().getPlainText());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (ArrayList<CalendarEventEntry>) allevents;
	}

	public static DateTime createtime(int year, int month, int day, int hour,
			int minute, int second) {
		Calendar c = new GregorianCalendar();
		c.set(year, month - 1, day, hour, minute, second); // 记住，Calendar类里的month是减1的，0代表一月
		DateTime time = new DateTime(c.getTime(), TimeZone.getDefault()); // 这里设置时区
		return time;
	}

	public static DateTime getInstanceTime() {
		Calendar c = Calendar.getInstance();// 获得系统当前日期
		DateTime time = new DateTime(c.getTime(), TimeZone.getDefault()); // 这里设置时区
		return time;
	}

	// 读取日历数据
	public static String readCalendars(String userName, String userPassword,
			DateTime starttime, DateTime endtime) {
		String res = "";
		String s = "http://www.google.com/calendar/feeds/default";
		URL url = null;
		try {
			url = new URL(s);
			CalendarService myService = null;
			myService = new CalendarService("dcsCalendarServer");
			// CalendarService myService = new CalendarService("Calendar");
			try {
				myService.setUserCredentials(userName, userPassword);
			} catch (AuthenticationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// CalendarFeed resultFeed = myService.getFeed(url,CalendarFeed.class);
			CalendarFeed feeds = null;
			try {
				feeds = myService.getFeed(url, CalendarFeed.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<CalendarEntry> entrys = feeds.getEntries();
			List<String> list = new ArrayList<String>(entrys.size());
			for (CalendarEntry entry : entrys) {
				// 标题
				String title = entry.getTitle().getPlainText();
				// 日历时区
				String tz = entry.getTimeZone().getValue();
				// 显示颜色
				String color = entry.getColor().getValue(); // 访问级别
				String level = entry.getAccessLevel().getValue();
				// 创建者
				List<Person> authors = entry.getAuthors();
				String author = "";
				for (Person p : authors) {
					author += p.getName() + ",";
				}
				list.add(entry.getTitle().getPlainText());
			}
			// 仅简单的返回 标题，其余数据被忽略
			String[] state = new String[list.size()];
			state = list.toArray(state);
			// DateTime starttime;
			// starttime = createtime(2012, 7, 4, 11, 30, 30);
			// System.out.println("++++++++++++++++++++++++"+starttime);
			// DateTime endtime;
			// endtime = createtime(2012, 7, 24, 11, 30, 30);
			// System.out.println("++++++++++++++++++++++++"+endtime);
			if (endtime == null) {
				endtime = getInstanceTime();
			}
			List<CalendarEventEntry> entrys1 = null;
			try {
				entrys1 = dateRangeQuery(myService, starttime, endtime);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (CalendarEventEntry entry1 : entrys1) {
				// 活动事件名称
				String title = entry1.getTitle().getPlainText();
				System.out.println("++++++++++++++++++++++++" + title);
				// 活动描述
				String memo = entry1.getTextContent().getContent().getPlainText();
				System.out.println("++++++++++++++++++++++++" + memo);
				// 活动地点：
				List<Where> wList = entry1.getLocations();
				String where = "";
				for (Where w : wList) {
					where += "," + w.getValueString();
				}
				// 活动时间；
				When when = entry1.getTimes().get(0);
				String time = when.getStartTime() + "～" + when.getEndTime();
				System.out.println("++++++++++++++++++++++++" + time);
				// 参与者
				List<EventWho> whos = entry1.getParticipants();
				String who = "";
				for (EventWho p : whos) {
					who += p.getValueString();
					System.out.println(who);
				}
				if (res.length() == 0) {
					res = time + " " + title + " " + memo + "\r\n";
				} else {
					res = res + time + " " + title + " " + memo + "\r\n";
				}
			}
		} catch (Exception e) {
			ScheduleApplication.logException(ServerInterface.class, e);
		}
		System.out.println(res);
		return res;
	}

	public static void gettest() {
		CalendarService myService = new CalendarService("CalendarTest-1");
		try {
			myService.setUserCredentials("username", "password");
			// System.out.println("Printing all events");

		} catch (AuthenticationException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}
	}

	/* 设置绑定部分 */
	public String get_Bin_Info(String user_id, String type, String user_acc) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("type", type);
		map.put("user_acc", user_acc);
		String ret = HttpUtils.doPost(map,
				"http://arc.archermind.com/ci/index.php/aschedule/BinInfo");
		System.out.println("Bin_Info====" + ret);
		return ret;
	}

	/* 设置绑定部分 */
	public String Bin_login(String type, String user_acc) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("type", type);
		map.put("user_acc", user_acc);
		String ret = HttpUtils.doPost(map,
				"http://arc.archermind.com/ci/index.php/aschedule/Binlogin");
		System.out.println("Bin_Info====" + ret);
		return ret;
	}

	/*
	 * 获取手机验证码 appid 1 爱音乐 2 话费通 3 微日程 4 微笔记 Mobile 手机号 Action：default
	 */
	public String sendSMS(String appid, String Mobile, String Action) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("appid", appid);
		map.put("Mobile", Mobile);
		map.put("Action", Action);
		String ret = HttpUtils.doPost(map,
				"http://arc.archermind.com/ci/index.php/SMSUtils/sendSMS");
		System.out.println("sendSMS====" + ret);
		return ret;
	}

	/*
	 * 验证手机验证码 appid 1 爱音乐 2 话费通 3 微日程 4 微笔记 smsID sendSMS返回的id号，对应一条消息 code 验证码
	 * Action：default
	 */
	public int checkSMS(String appid, String code, String smsID, String Action,
			String user_id, String tel, String imsi) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("appid", appid);
		map.put("code", code);
		map.put("smsID", smsID);
		map.put("Action", Action);
		map.put("user_id", user_id);
		map.put("tel", tel);
		map.put("imsi", imsi);
		System.out.println("smsID = " + smsID + ",tel = " + tel);
		String ret = HttpUtils.doPost(map,
				"http://arc.archermind.com/ci/index.php/SMSUtils/checkSMS");
		System.out.println("sendSMS====" + ret);
		int result = 0;
		try {
			result = Integer.parseInt(ret);
		} catch (Exception e) {
			result = -4;
		}
		return result;
	}

	public int is_tel_bind(String user_id, String tel) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("tel", tel);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/is_tel_bind");
		int result = 0;
		try {
			result = Integer.parseInt(ret);
		} catch (Exception e) {
			result = -4;
		}
		return result;
	}

	/*
	 * 初始化 在主界面启动的时候调用 参数 ：上下文参数(this) 返回值：void
	 */
	public void InitAmtCloud(Context context) {

		AmtApplication.amtAppInitialize(context, app_id, app_secret);
	}

	/**
	 * 上传头像 输入参数：用户id,文件路径，文件名，文件扩展名 返回值： 0 成功 -1 url为空 -2：数据库操作失败
	 */
	public static int uploadPhoto(String user_id, String url) {

		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("photo_url", url);
		String res = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/uploadPhoto");
		int result = 0;

		try {
			result = Integer.parseInt(res);
		} catch (Exception e) {
			result = -3; // 其他异常情况
		}

		return result;
	}

	/**
	 * 获取头像 输入参数：用户id 返回值： json 成功 -1 url为空 -2：数据库操作失败
	 */
	public static String getPhoto(String user_id) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		String res = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/downloadPhoto");
		return res;
	}

	public String session_check(String user_id) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/session_check_2");
		return ret;
	}

	public int suggestionfeedback(String user_id, String tel, String suggestion) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("tel", tel);
		map.put("suggestion", suggestion);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/suggestionfeedback");
		int result = 0;
		try {
			result = Integer.parseInt(ret);
		} catch (Exception e) {
			result = -3;
		}
		return result;
	}

	public int tel_unbind(String user_id) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/tel_unbind");
		int result = 0;
		try {
			result = Integer.parseInt(ret);
		} catch (Exception e) {
			result = -3;
		}
		return result;
	}

	// -1 传入值为空 ，-2 ：帐号不存在，-3 昵称已经存在， -4 其他错误
	public int nickModify(String user_id, String nick) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("user_id", user_id);
		map.put("nick", nick);
		String ret = HttpUtils
				.doPost(map,
						"http://arc.archermind.com/ci/index.php/aschedule/nickModify");
		int result = 0;
		try {
			result = Integer.parseInt(ret);
		} catch (Exception e) {
			result = -4;
		}
		return result;
	}
}
