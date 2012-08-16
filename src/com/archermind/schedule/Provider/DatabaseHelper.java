package com.archermind.schedule.Provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String NAME = "schedule.db";
	private static final int version = 1;
	public static final String TAB_LOCAL_SCHEDULE = "local_schedule";

	public final static int SCHEDULE_EVENT_TYPE_NONE = -1;
	public final static int SCHEDULE_EVENT_TYPE_NOTICE = 0;
	public final static int SCHEDULE_EVENT_TYPE_ACTIVE = 1;
	public final static int SCHEDULE_EVENT_TYPE_APPOINTMENT = 2;
	public final static int SCHEDULE_EVENT_TYPE_TRAVEL = 3;
	public final static int SCHEDULE_EVENT_TYPE_ENTERTAINMENT = 4;
	public final static int SCHEDULE_EVENT_TYPE_EAT = 5;
	public final static int SCHEDULE_EVENT_TYPE_WORK = 6;
	
	public final static String SCHEDULE_OPER_ADD = "A";
	public final static String SCHEDULE_OPER_MODIFY = "M";
	public final static String SCHEDULE_OPER_DELETE = "D";
	public final static String SCHEDULE_OPER_NOTHING = "N";

	public final static String SCHEDULE_NOTICE_PERIOD_MODE_NONE = "0";
	public final static String SCHEDULE_NOTICE_PERIOD_MODE_DAY = "D";
	public final static String SCHEDULE_NOTICE_PERIOD_MODE_YEAR = "Y";
	public final static String SCHEDULE_NOTICE_PERIOD_MODE_MONTH = "M";
	public final static String SCHEDULE_NOTICE_PERIOD_MODE_WEEK = "W";

	public static final String COLUMN_SCHEDULE_ID = "_id";// 日程ID
	public static final String COLUMN_SCHEDULE_USER_ID = "user_id";// 用户ID
	public static final String COLUMN_SCHEDULE_T_ID = "tid";// 贴子ID
	public static final String COLUMN_SCHEDULE_SHARE = "share";//是否分享
	public static final String COLUMN_SCHEDULE_TYPE = "type";//日程类别
	public static final String COLUMN_SCHEDULE_START_TIME = "start_time";//日程开始时间
	public static final String COLUMN_SCHEDULE_OPER_FLAG = "oper_flag";//日程操作标志
	public static final String COLUMN_SCHEDULE_UPDATE_TIME = "update_time";//日程更新时间
	public static final String COLUMN_SCHEDULE_ALARM_FLAG = "alarm_flag";//闹钟标志
	public static final String COLUMN_SCHEDULE_NOTICE_FLAG = "notice_flag";//闹钟提醒标志
	public static final String COLUMN_SCHEDULE_NOTICE_PERIOD = "notice_period";//闹钟重复提醒标志
	public static final String COLUMN_SCHEDULE_NOTICE_WEEK = "notice_week";//闹钟按星期重复提醒的星期值
	public static final String COLUMN_SCHEDULE_NOTICE_MONTHDAY="notice_monthday";//闹钟按每月重复提醒的日（本地，取的是notice_day 中的日）
	public static final String COLUMN_SCHEDULE_NOTICE_YEARDAY="notice_yearday";//闹钟按每年重复提醒的月和日（本地，取的是notice_day 中的日，月）
	public static final String COLUMN_SCHEDULE_NOTICE_STAGE_FLAG = "notice_stage_flag";//闹钟阶段提醒标志
	public static final String COLUMN_SCHEDULE_NOTICE_END = "notice_end";// 日程结束时间
	public static final String COLUMN_SCHEDULE_FLAG_OUTDATE = "notice_outdate";//闹钟是否过期
	public static final String COLUMN_SCHEDULE_CONTENT = "content";// 日程内容
	public static final String COLUMN_SCHEDULE_SLAVE_ID = "slave_id";// 隶属于哪个主贴
	public static final String COLUMN_SCHEDULE_ORDER = "queue";// 回帖顺序
	public static final String COLUMN_SCHEDULE_DEFAULT = "default_data";// 好友动态的默认数据标示
	private static final String CRETAE_TAB_LOCAL_SCHEDULE = " CREATE TABLE IF NOT EXISTS "
			+ TAB_LOCAL_SCHEDULE
			+ " ( "
			+ COLUMN_SCHEDULE_ID
			+ " INTEGER PRIMARY KEY , "
			+ COLUMN_SCHEDULE_USER_ID
			+ " INTEGER, "
			+ COLUMN_SCHEDULE_T_ID
			+ " INTEGER, "
			+ COLUMN_SCHEDULE_SHARE
			+ " BOOLEAN, "
			+ COLUMN_SCHEDULE_SLAVE_ID
			+ " INTEGER, "
			+ COLUMN_SCHEDULE_ORDER
			+ " INTEGER, "
			+ COLUMN_SCHEDULE_TYPE
			+ " INTEGER, "
			+ COLUMN_SCHEDULE_START_TIME
			+ " BIGINT, "
			+ COLUMN_SCHEDULE_OPER_FLAG
			+ " TEXT, "
			+ COLUMN_SCHEDULE_ALARM_FLAG
			+" BIGINT, "
			+ COLUMN_SCHEDULE_UPDATE_TIME
			+ " TEXT, "
			+ COLUMN_SCHEDULE_NOTICE_FLAG
			+ " BOOLEAN, "
			+ COLUMN_SCHEDULE_NOTICE_PERIOD
			+ " TEXT, "
			+ COLUMN_SCHEDULE_NOTICE_WEEK
			+ " TEXT, "
			+ COLUMN_SCHEDULE_NOTICE_MONTHDAY
			+ " TEXT, "
			+ COLUMN_SCHEDULE_NOTICE_YEARDAY
			+ " TEXT,"
			+ COLUMN_SCHEDULE_NOTICE_END
			+ " TEXT, "
			+ COLUMN_SCHEDULE_FLAG_OUTDATE
			+ " BOOLEAN, "
			+ COLUMN_SCHEDULE_NOTICE_STAGE_FLAG
			+ " BOOLEAN, "
			+ COLUMN_SCHEDULE_CONTENT + " TEXT " + ")";

	public static final String TAB_SHARE_SCHEDULE = "share_schedule";
	public static final String COLUMN_SCHEDULE_CITY = "city";//用户所在城市
	
	private static final String CRETAE_TAB_SHARE_SCHEDULE = " CREATE TABLE IF NOT EXISTS "
		+ TAB_SHARE_SCHEDULE
		+ " ( "
		+ COLUMN_SCHEDULE_ID
		+ " INTEGER PRIMARY KEY , "
		+ COLUMN_SCHEDULE_USER_ID
		+ " INTEGER, "
		+ COLUMN_SCHEDULE_T_ID
		+ " INTEGER, "
		+ COLUMN_SCHEDULE_SLAVE_ID
		+ " INTEGER, "
		+ COLUMN_SCHEDULE_ORDER
		+ " INTEGER, "
		+ COLUMN_SCHEDULE_TYPE
		+ " INTEGER, "
		+ COLUMN_SCHEDULE_START_TIME
		+ " BIGINT, "
		+ COLUMN_SCHEDULE_UPDATE_TIME
		+ " TEXT, "
		+ COLUMN_SCHEDULE_CITY
		+ " TEXT, "
		+ COLUMN_SCHEDULE_DEFAULT
		+ " BOOLEAN, "
		+ COLUMN_SCHEDULE_CONTENT
		+ " TEXT "
		+ ")";
	
	

	public static final String ASCHEDULE_CONTACT = "schedule_contact";	/* 微日程联系人表 */
	public static final String COLUMN_CONTACT_ID = "contact_id";			/* 微日程联系人表 */
   public static final String COLUMN_FRIEND_ID = "friend_id";		
   public static final String ASCHEDULE_CONTACT_NUM = "number";			/* 微日程联系人表 */
//	public static final String ASCHEDULE_CONTACT_FLAG = "flag";			/* 微日程联系人表 */
	public static final String ASCHEDULE_CONTACT_NAME = "name";
	public static final String ASCHEDULE_CONTACT_IMGPATH = "img_path";
	public static final String ASCHEDULE_CONTACT_TYPE = "type";
	private static final String CREATE_CONTACT_TABLE = " CREATE TABLE IF NOT EXISTS "
	+ ASCHEDULE_CONTACT +
	" ( "
	+ COLUMN_CONTACT_ID + " INTEGER PRIMARY KEY, " 
	+ COLUMN_FRIEND_ID + " INTEGER DEFAULT '-1', "
	+ ASCHEDULE_CONTACT_NUM + " TEXT, "
	+ ASCHEDULE_CONTACT_NAME + " TEXT, "
	+ ASCHEDULE_CONTACT_IMGPATH + " TEXT, "
	+ ASCHEDULE_CONTACT_TYPE + " INTEGER DEFAULT '0' "
//	+ ASCHEDULE_CONTACT_FLAG + " INTEGER DEFAULT '0' "
	+ " ); ";
	
	public static final String ASCHEDULE_FRIEND = "schedule_friend";
	public static final String ASCHEDULE_FRIEND_ID = "friend_id";
	public static final String ASCHEDULE_FRIEND_NUM = "number";
	public static final String ASCHEDULE_FRIEND_NAME = "name";
	public static final String ASCHEDULE_FRIEND_NICK = "nick";
	public static final String ASCHEDULE_FRIEND_PHOTO_URL = "photo_url";
	public static final String ASCHEDULE_FRIEND_TYPE = "type";
	
	private static final String CREATE_FRIEND_TABLE = " CREATE TABLE IF NOT EXISTS "
		+ ASCHEDULE_FRIEND +
		" ( "
		+ ASCHEDULE_FRIEND_ID + " INTEGER PRIMARY KEY, " 
		+ ASCHEDULE_FRIEND_NUM + " TEXT, "
		+ ASCHEDULE_FRIEND_NAME + " TEXT, "
		+ ASCHEDULE_FRIEND_NICK + " TEXT, "
		+ ASCHEDULE_FRIEND_PHOTO_URL + " TEXT, "
		+ ASCHEDULE_FRIEND_TYPE + " INTEGER DEFAULT '0' "
		+ " ); ";


	public static final String TAB_SCHEDULE_WEATHER = "schedule_weather";
	public static final String COLUMN_WEATHER_ID = "weather_id";
	public static final String COLUMN_WEATHER_DATE = "weather_date";
	public static final String COLUMN_WEATHER_TEMP = "weather_temp";
	public static final String COLUMN_WEATHER_TEMP_RANGE = "weather_tempRange";
	public static final String COLUMN_WEATHER_WEATHER = "weather_weather";
	private static final String CREATE_TABLE_WEATHER = "CREATE TABLE IF NOT EXISTS "
			+ TAB_SCHEDULE_WEATHER
			+ " ( "
			+ COLUMN_WEATHER_ID
			+ " INTEGER PRIMARY KEY, "
			+ COLUMN_WEATHER_DATE
			+ " TEXT, "
			+ COLUMN_WEATHER_TEMP
			+ " TEXT, "
			+ COLUMN_WEATHER_TEMP_RANGE
			+ " TEXT, " + COLUMN_WEATHER_WEATHER + " TEXT " + " ); ";
	public DatabaseHelper(Context context) {
		super(context, NAME, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTabs(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	private void createTabs(SQLiteDatabase db) {
		db.execSQL(CRETAE_TAB_LOCAL_SCHEDULE);
		db.execSQL(CRETAE_TAB_SHARE_SCHEDULE);
		db.execSQL(CREATE_CONTACT_TABLE);	
        db.execSQL(CREATE_FRIEND_TABLE);	
         db.execSQL(CREATE_TABLE_WEATHER);
	}

}
