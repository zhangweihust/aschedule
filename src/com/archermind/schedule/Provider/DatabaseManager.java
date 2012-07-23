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

	public boolean deleteLocalSchedules(int id) {
		return database.delete(DatabaseHelper.TAB_SCHEDULE,
				DatabaseHelper.COLUMN_SCHEDULE_ID + " =? ",
				new String[] { String.valueOf(id) }) >= 0;
	}

	public Cursor queryWeekLocalSchedules() {
		return database
				.query(DatabaseHelper.TAB_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getDayOfWeek(Calendar.MONDAY)), String.valueOf(DateTimeUtils.getDayOfWeek(Calendar.SUNDAY)) },
						null, null, DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " ASC");
	}
	
	public Cursor queryTodayLocalSchedules() {
		return database
				.query(DatabaseHelper.TAB_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getToday(Calendar.AM)), String.valueOf(DateTimeUtils.getToday(Calendar.PM)) },
						null, null, null);
	}
	
	public Cursor queryTomorrowLocalSchedules() {
		return database
				.query(DatabaseHelper.TAB_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getTomorrow(Calendar.AM)), String.valueOf(DateTimeUtils.getTomorrow(Calendar.PM)) },
						null, null, null);
	}

	public Cursor query3DaysBeforeLocalSchedules() {
		return database
				.query(DatabaseHelper.TAB_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " BETWEEN ? AND ? ",
						new String[] { String.valueOf(DateTimeUtils.getThreeDaysBefore()), String.valueOf(DateTimeUtils.getYesterdayEnd()) },
						null, null, DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " ASC");
	}

}
