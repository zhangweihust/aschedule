package com.archermind.schedule.Provider;

import java.util.Calendar;

import com.archermind.schedule.Utils.DateTimeUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseManager {
	private Context context;
	private DatabaseHelper databaseHelper;
	private SQLiteDatabase database;

	public DatabaseManager(Context context) {
		this.context = context;
	}

	public void open() {
		databaseHelper = new DatabaseHelper(context);
		database = databaseHelper.getWritableDatabase();
	}

	public void close() {
		databaseHelper.close();
		database.close();
	}

	public boolean insertLocalSchedules(ContentValues values) {
		return database.insert(DatabaseHelper.TAB_SCHEDULE, null, values) > 0;
	}

	public Cursor queryLocalSchedules() {
		return database.query(DatabaseHelper.TAB_SCHEDULE, null, null, null,
				null, null, null);
	}

	public void deleteLocalSchedules(int id, boolean firstFlag , long timeInMillis) {
	   database.delete(DatabaseHelper.TAB_SCHEDULE,
				DatabaseHelper.COLUMN_SCHEDULE_ID + " =? ",
				new String[] { String.valueOf(id) });
	   if(firstFlag){//如果该日程是一天的第一条日程，则修改该天的第二条日程的标志位
			Cursor c = queryTodayLocalSchedules(timeInMillis);
			if(c.getCount() > 0){
				c.moveToFirst();
				int _id = c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_ID));
				ContentValues values = new ContentValues();
				values.put(DatabaseHelper.COLUMN_SCHEDULE_FIRST_FLAG, true);
				updateLocalSchedules(values, _id);
				c.close();
			}
		}
	}
	
	public void updateLocalSchedules(ContentValues values, int id){
	    database.update(DatabaseHelper.TAB_SCHEDULE, values, DatabaseHelper.COLUMN_SCHEDULE_ID + " =? ", new String[] { String.valueOf(id)});
	}

	public Cursor queryWeekLocalSchedules(long timeInMillis) {
		return database
				.query(DatabaseHelper.TAB_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getDayOfWeek(Calendar.MONDAY, timeInMillis)), String.valueOf(DateTimeUtils.getDayOfWeek(Calendar.SUNDAY, timeInMillis)) },
						null, null, DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " ASC");
	}
	
	public Cursor queryTodayLocalSchedules(long timeInMillis) {
		return database
				.query(DatabaseHelper.TAB_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getToday(Calendar.AM, timeInMillis)), String.valueOf(DateTimeUtils.getToday(Calendar.PM, timeInMillis)) },
						null, null, DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " ASC");
	}
	
	public Cursor queryTomorrowLocalSchedules(long timeInMillis) {
		return database
				.query(DatabaseHelper.TAB_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getTomorrow(Calendar.AM, timeInMillis)), String.valueOf(DateTimeUtils.getTomorrow(Calendar.PM, timeInMillis)) },
						null, null, DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " ASC");
	}

	public Cursor query3DaysBeforeLocalSchedules(long timeInMillis) {
		return database
				.query(DatabaseHelper.TAB_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getThreeDaysBefore(timeInMillis)), String.valueOf(DateTimeUtils.getYesterdayEnd(timeInMillis)) },
						null, null, DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " ASC");
	}

}
