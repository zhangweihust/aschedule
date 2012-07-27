package com.archermind.schedule.Provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String NAME = "schedule.db";
	private static final int version = 1;
	public static final String TAB_SCHEDULE = "schedule";

	public static final String COLUMN_SCHEDULE_ID = "_id";// 日程ID
	public static final String COLUMN_SCHEDULE_USER_ID = "user_id";// 用户ID
	public static final String COLUMN_SCHEDULE_T_ID = "tid";// 贴子ID
	public static final String COLUMN_SCHEDULE_SHARE = "share";// 是否分享
	public static final String COLUMN_SCHEDULE_IMPORTANT = "important";// 是否重要
	public static final String COLUMN_SCHEDULE_TYPE = "type";// 日程类别
	public static final String COLUMN_SCHEDULE_START_TIME = "start_time";// 日程开始时间
	public static final String COLUMN_SCHEDULE_OPER_FLAG = "oper_flag";// 日程操作标志
	public static final String COLUMN_SCHEDULE_UPDATE_TIME = "update_time";// 日程更新同步时间
	public static final String COLUMN_SCHEDULE_NOTICE_TIME = "notice_time";// 提醒时间(提前分钟)
	public static final String COLUMN_SCHEDULE_NOTICE_PERIOD = "notice_period";// 提醒周期（日）
	public static final String COLUMN_SCHEDULE_NOTICE_WEEK = "notice_week";// 提醒周期（星期）
	public static final String COLUMN_SCHEDULE_NOTICE_START = "notice_start";// 提醒开始时间
	public static final String COLUMN_SCHEDULE_NOTICE_END = "notice_end";// 提醒结束时间
	public static final String COLUMN_SCHEDULE_NOTICE_CONTENT = "content";// 日程内容
	public static final String COLUMN_SCHEDULE_FIRST_FLAG = "first_flag";// 一天的第一条日程
	private static final String CRETAE_TAB_SCHEDULE = " CREATE TABLE IF NOT EXISTS "
			+ TAB_SCHEDULE
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
			+ COLUMN_SCHEDULE_NOTICE_CONTENT + " TEXT " + ")";

	public static final String ASCHEDULE_CONTACT = "schedule_contact"; /* 微日程联系人表 */
	public static final String COLUMN_CONTACT_ID = "contact_id"; /* 微日程联系人表 */
	public static final String ASCHEDULE_CONTACT_NUM = "number"; /* 微日程联系人表 */
	// public static final String ASCHEDULE_CONTACT_FLAG = "flag"; /* 微日程联系人表 */
	public static final String ASCHEDULE_CONTACT_NAME = "name";
	public static final String ASCHEDULE_CONTACT_TYPE = "type";
	private static final String CREATE_CONTACT_TABLE = " CREATE TABLE IF NOT EXISTS "
	+ ASCHEDULE_CONTACT +
	" ( "
	+ COLUMN_CONTACT_ID + " INTEGER PRIMARY KEY, " 
	+ ASCHEDULE_CONTACT_NUM + " TEXT "
	+ ASCHEDULE_CONTACT_NAME + " TEXT "
	+ ASCHEDULE_CONTACT_TYPE + " INTEGER DEFAULT '0' "
//	+ ASCHEDULE_CONTACT_FLAG + " INTEGER DEFAULT '0' "
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
		db.execSQL(CRETAE_TAB_SCHEDULE);
		db.execSQL(CREATE_CONTACT_TABLE);		
	}
}
