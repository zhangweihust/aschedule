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
	
	public static final String COLUMN_SCHEDULE_ID = "_id";//日程ID
	public static final String COLUMN_SCHEDULE_USER_ID = "user_id";//用户ID
	public static final String COLUMN_SCHEDULE_T_ID = "tid";// 贴子ID
	public static final String COLUMN_SCHEDULE_SHARE = "share";//是否分享
	public static final String COLUMN_SCHEDULE_IMPORTANT = "important";//是否重要
	public static final String COLUMN_SCHEDULE_TYPE = "type";//日程类别
	public static final String COLUMN_SCHEDULE_START_TIME = "start_time";//日程开始时间
	public static final String COLUMN_SCHEDULE_OPER_FLAG = "oper_flag";//日程操作标志
	public static final String COLUMN_SCHEDULE_UPDATE_TIME = "update_time";//日程更新时间
	public static final String COLUMN_SCHEDULE_NOTICE_TIME = "notice_time";//
	public static final String COLUMN_SCHEDULE_NOTICE_PERIOD = "notice_period";
	public static final String COLUMN_SCHEDULE_NOTICE_WEEK = "notice_week";
	public static final String COLUMN_SCHEDULE_NOTICE_START = "notice_start";//日程开始时间
	public static final String COLUMN_SCHEDULE_NOTICE_END = "notice_end";//日程结束时间
	public static final String COLUMN_SCHEDULE_CONTENT = "content";//日程内容
	public static final String COLUMN_SCHEDULE_SLAVE_ID = "slave_id";//隶属于哪个主贴
	public static final String COLUMN_SCHEDULE_ORDER = "queue";//回帖顺序
	public static final String COLUMN_SCHEDULE_FIRST_FLAG = "first_flag";//一天的第一条日程
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
			+ COLUMN_SCHEDULE_IMPORTANT
			+ " BOOLEAN, "
			+ COLUMN_SCHEDULE_TYPE
			+ " INTEGER, "
			+ COLUMN_SCHEDULE_START_TIME
			+ " BIGINT, "
			+ COLUMN_SCHEDULE_OPER_FLAG
			+ " TEXT, "
			+ COLUMN_SCHEDULE_UPDATE_TIME
			+ " TEXT, "
			+ COLUMN_SCHEDULE_NOTICE_TIME
			+ " TEXT, "
			+ COLUMN_SCHEDULE_NOTICE_PERIOD
			+ " TEXT, "
			+ COLUMN_SCHEDULE_NOTICE_WEEK
			+ " TEXT, "
			+ COLUMN_SCHEDULE_NOTICE_START
			+ " TEXT, "
			+ COLUMN_SCHEDULE_NOTICE_END
			+ " TEXT, "
			+ COLUMN_SCHEDULE_FIRST_FLAG
			+ " BOOLEAN, "
			+ COLUMN_SCHEDULE_CONTENT
			+ " TEXT "
			+ ")";
	
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
	public static final String ASCHEDULE_FRIEND_NIKE = "nike";
	public static final String ASCHEDULE_FRIEND_TYPE = "type";
	
	private static final String CREATE_FRIEND_TABLE = " CREATE TABLE IF NOT EXISTS "
		+ ASCHEDULE_FRIEND +
		" ( "
		+ ASCHEDULE_FRIEND_ID + " INTEGER PRIMARY KEY, " 
		+ ASCHEDULE_FRIEND_NUM + " TEXT, "
		+ ASCHEDULE_FRIEND_NAME + " TEXT, "
		+ ASCHEDULE_FRIEND_NIKE + " TEXT, "
		+ ASCHEDULE_FRIEND_TYPE + " INTEGER DEFAULT '0' "
		+ " ); ";
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
	}

}
