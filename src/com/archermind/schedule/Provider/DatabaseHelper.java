package com.archermind.schedule.Provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String NAME = "schedule.db";
	private static final int version = 1;
	public static final String TAB_SCHEDULE = "schedule";

	public static final String COLUMN_SCHEDULE_ID = "_id";//日程ID
	public static final String COLUMN_SCHEDULE_USER_ID = "user_id";//用户ID
	public static final String COLUMN_SCHEDULE_SHARE = "share";//是否分享
	public static final String COLUMN_SCHEDULE_TYPE = "type";//日程类别
	public static final String COLUMN_SCHEDULE_START_TIME = "start_time";//日程开始时间
	public static final String COLUMN_SCHEDULE_OPER_FLAG = "oper_flag";//日程操作标志
	public static final String COLUMN_SCHEDULE_UPDATE_TIME = "update_time";//日程更新时间
	public static final String COLUMN_SCHEDULE_NOTICE_TIME = "notice_time";//
	public static final String COLUMN_SCHEDULE_NOTICE_PERIOD = "notice_period";
	public static final String COLUMN_SCHEDULE_NOTICE_WEEK = "notice_week";
	public static final String COLUMN_SCHEDULE_NOTICE_START = "notice_start";//日程开始时间
	public static final String COLUMN_SCHEDULE_NOTICE_END = "notice_end";//日程结束时间
	public static final String COLUMN_SCHEDULE_NOTICE_CONTENT = "content";//日程内容
	private static final String CRETAE_TAB_SCHEDULE = " CREATE TABLE IF NOT EXISTS "
			+ TAB_SCHEDULE
			+ " ( "
			+ COLUMN_SCHEDULE_ID
			+ " INTEGER PRIMARY KEY , "
			+ COLUMN_SCHEDULE_USER_ID
			+ " INTEGER, "
			+ COLUMN_SCHEDULE_SHARE
			+ " INTEGER, "
			+ COLUMN_SCHEDULE_TYPE
			+ " INTEGER, "
			+ COLUMN_SCHEDULE_START_TIME
			+ " TEXT, "
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
			+ COLUMN_SCHEDULE_NOTICE_CONTENT
			+ " TEXT "
			+ ")";

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
	}

}
