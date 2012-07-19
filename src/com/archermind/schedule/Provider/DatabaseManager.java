package com.archermind.schedule.Provider;

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
		return database.query(DatabaseHelper.TAB_SCHEDULE, null, null, null, null, null, null);
	}
}
