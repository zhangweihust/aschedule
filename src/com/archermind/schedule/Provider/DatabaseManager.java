package com.archermind.schedule.Provider;

import java.util.Calendar;

import com.archermind.schedule.Utils.DateTimeUtils;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;

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

	public int deleteContact()
	{
		return database.delete(DatabaseHelper.ASCHEDULE_CONTACT, null, null);
	}
	
	public long insertContact(ContentValues values)
	{
		return database.insert(DatabaseHelper.ASCHEDULE_CONTACT, null, values);
	}
	
	public Cursor getAScheduleContacts()
	{
		return database.query(DatabaseHelper.ASCHEDULE_CONTACT, 
											null, 
											null, 
											null, 
											null, 
											null, 
											null);
	}
	
	public Cursor getLocalContacts()
	{
		return context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,  
	    		null,null, null,null);
	}
	
	public boolean hasData(int contactid,String number)
	{
		Cursor cursor = database.query(DatabaseHelper.ASCHEDULE_CONTACT, 
				null, 
				DatabaseHelper.COLUMN_CONTACT_ID + " = ? and " + DatabaseHelper.ASCHEDULE_CONTACT_NUM + " = ?", 
				new String[] {String.valueOf(contactid),number}, 
				null, 
				null, 
				null);
		
		int count = cursor.getCount();
		cursor.close();
		
		return count > 0 ? true:false;
	}
	
}