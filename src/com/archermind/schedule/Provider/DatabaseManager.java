package com.archermind.schedule.Provider;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;

import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.EventTypes;
import com.archermind.schedule.Services.EventService;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.DateTimeUtils;

public class DatabaseManager {
	private Context context;
	private DatabaseHelper databaseHelper;
	private SQLiteDatabase database;
	EventService eventService;
	
	public DatabaseManager(Context context) {
		this.context = context;
	}

	public void open() {
		databaseHelper = new DatabaseHelper(context);
		database = databaseHelper.getWritableDatabase();
		eventService = ServiceManager.getEventservice();
	}

	public void close() {
		databaseHelper.close();
		database.close();
	}

	public long insertLocalSchedules(ContentValues values) {
		long schedule_id= database.insert(DatabaseHelper.TAB_LOCAL_SCHEDULE, null, values);
		eventService.onUpdateEvent(new EventArgs(EventTypes.LOCAL_SCHEDULE_UPDATE));
		return schedule_id;
	}
	
	public void insertShareSchedules(ContentValues values) {
		database.insert(DatabaseHelper.TAB_SHARE_SCHEDULE, null, values);
	}
	
	public void deleteShareDefaultSchedules() {
		database.delete(DatabaseHelper.TAB_SHARE_SCHEDULE, DatabaseHelper.COLUMN_SCHEDULE_DEFAULT+ " =? ", new String[] { String.valueOf(1)});
	}
	
	public boolean isInShareSchedules(String t_id){
		Cursor cursor = database.query(DatabaseHelper.TAB_SHARE_SCHEDULE, new String[] { DatabaseHelper.COLUMN_SCHEDULE_ID },
				DatabaseHelper.COLUMN_SCHEDULE_T_ID + " =? ",  new String[] { t_id }, null, null, null);
		boolean result = false;
		if (cursor.getCount() > 0) {
			result = true;
		}
		cursor.close();
		return result;
	}
	
	public void updateShareSchedules(ContentValues values, String t_id) {
		database.update(DatabaseHelper.TAB_SHARE_SCHEDULE, values,
				DatabaseHelper.COLUMN_SCHEDULE_T_ID + " =? ",
				new String[] { t_id });
	}

	public Cursor queryLocalSchedules() {
		return database.query(DatabaseHelper.TAB_LOCAL_SCHEDULE, null, null, null,
				null, null, null);
	}



	public Cursor queryWeekLocalSchedules(long timeInMillis) {
		return database
				.query(DatabaseHelper.TAB_LOCAL_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " BETWEEN ? AND ? AND " + DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG + " != 'D'",
						new String[] {
								String.valueOf(DateTimeUtils.getDayOfWeek(
										Calendar.MONDAY, timeInMillis)),
								String.valueOf(DateTimeUtils.getDayOfWeek(
										Calendar.SUNDAY, timeInMillis)) },
						null, null, DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " ASC");
	}

	public Cursor queryTodayLocalSchedules(long timeInMillis) {
		return database
				.query(DatabaseHelper.TAB_LOCAL_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " BETWEEN ? AND ?  AND " + DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG + " != 'D' AND "
								+ DatabaseHelper.COLUMN_SCHEDULE_ORDER + " = ? ",
						new String[] { 
								String.valueOf(DateTimeUtils.getToday(
										Calendar.AM, timeInMillis)),
								String.valueOf(DateTimeUtils.getToday(
										Calendar.PM, timeInMillis)),
										"0"}, null,
						null, DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " ASC");
	}
	
	public Cursor queryMonthLocalSchedules(long starTimeInMillis, long endTimeInMillis) {
		return database
				.query(DatabaseHelper.TAB_LOCAL_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " BETWEEN ? AND ?  AND " + DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG + " != 'D'",
						new String[] {
								String.valueOf(DateTimeUtils.getToday(
										Calendar.AM, starTimeInMillis)),
								String.valueOf(DateTimeUtils.getToday(
										Calendar.PM, endTimeInMillis)) }, null,
						null, DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " ASC");
	}


	public Cursor queryTomorrowLocalSchedules(long timeInMillis) {
		return database
				.query(DatabaseHelper.TAB_LOCAL_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " BETWEEN ? AND ? AND " + DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG + " != 'D'",
						new String[] {
								String.valueOf(DateTimeUtils.getTomorrow(
										Calendar.AM, timeInMillis)),
								String.valueOf(DateTimeUtils.getTomorrow(
										Calendar.PM, timeInMillis)) }, null,
						null, DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " ASC");
	}

	public Cursor query3DaysBeforeLocalSchedules(long timeInMillis) {
		return database
				.query(DatabaseHelper.TAB_LOCAL_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " BETWEEN ? AND ? AND " + DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG + " != 'D'",
						new String[] {
								String.valueOf(DateTimeUtils
										.getThreeDaysBefore(timeInMillis)),
								String.valueOf(DateTimeUtils
										.getYesterdayEnd(timeInMillis)) },
						null, null, DatabaseHelper.COLUMN_SCHEDULE_START_TIME
								+ " ASC");
	}
	
	
	public Cursor queryIsMarkWithDay(long timeInMillis, String dayOfYear, String dayOfMonth, String dayOfWeek){
		
//		String sql = "select * from "+DatabaseHelper.TAB_LOCAL_SCHEDULE + " where (("
//		+DatabaseHelper.COLUMN_SCHEDULE_NOTICE_YEARDAY+" ="+" '"+dayOfYear+"' or "
//		+DatabaseHelper.COLUMN_SCHEDULE_NOTICE_MONTHDAY+" ="+" '"+dayOfMonth+"' or "
//		+DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD+" ="+" '"+DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_DAY+"' or "
//		+DatabaseHelper.COLUMN_SCHEDULE_NOTICE_WEEK+" like"+" '%"+dayOfWeek+"%' ) and ("
//		+DatabaseHelper.COLUMN_SCHEDULE_START_TIME+" <="+" '"+timeInMillis+"' and "
//		+DatabaseHelper.COLUMN_SCHEDULE_NOTICE_END+" >="+" '"+timeInMillis+"' and "
//		+DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD+" !="+" '"+DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_NONE+"' )) or ("
//		+DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD+" ="+" '"+DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_NONE+"' and "
//		+DatabaseHelper.COLUMN_SCHEDULE_START_TIME+" <="+" '"+String.valueOf(DateTimeUtils.getToday(
//				Calendar.PM, timeInMillis))+"' and "
//		+DatabaseHelper.COLUMN_SCHEDULE_START_TIME+" >="+" '"+String.valueOf(DateTimeUtils.getToday(
//				Calendar.AM, timeInMillis))+"')"
//		;
		
		String sql = "select * from "+DatabaseHelper.TAB_LOCAL_SCHEDULE + " where (("
		+DatabaseHelper.COLUMN_SCHEDULE_NOTICE_YEARDAY+" ="+" '"+dayOfYear+"' or "
		+DatabaseHelper.COLUMN_SCHEDULE_NOTICE_MONTHDAY+" ="+" '"+dayOfMonth+"' or "
		+DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD+" ="+" '"+DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_DAY+"' or "
		+DatabaseHelper.COLUMN_SCHEDULE_NOTICE_WEEK+" like"+" '%"+dayOfWeek+"%' ) and ("
		+DatabaseHelper.COLUMN_SCHEDULE_START_TIME+" <="+" '"+timeInMillis+"' and "
		+DatabaseHelper.COLUMN_SCHEDULE_NOTICE_END+" >="+" '"+timeInMillis+"' and "
		+DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD+" !="+" '"+DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_NONE+"' ) and (" 
		+DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG+" !="+" '"+DatabaseHelper.SCHEDULE_OPER_DELETE+"')) or ("
		+DatabaseHelper.COLUMN_SCHEDULE_NOTICE_PERIOD+" ="+" '"+DatabaseHelper.SCHEDULE_NOTICE_PERIOD_MODE_NONE+"' and "
		+DatabaseHelper.COLUMN_SCHEDULE_START_TIME+" <="+" '"+String.valueOf(DateTimeUtils.getToday(
				Calendar.PM, timeInMillis))+"' and "
		+DatabaseHelper.COLUMN_SCHEDULE_START_TIME+" >="+" '"+String.valueOf(DateTimeUtils.getToday(
				Calendar.AM, timeInMillis))+"' and "
		+DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG+" !="+" '"+DatabaseHelper.SCHEDULE_OPER_DELETE+"')";
		return database.rawQuery(sql, null);
	}
	
	
	
	
	public Cursor querySpecifiedNumPreSchedules(long timeInMillis,int limitnum)
	{
		return database.query(false, 
								DatabaseHelper.TAB_LOCAL_SCHEDULE, 
								null, 
								DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " < ? and "  + DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG + " != 'D'", 
								new String[] {String.valueOf(timeInMillis)}, 
								null, 
								null, 
								DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " DESC", 
								String.valueOf(limitnum));
	}
	
	public Cursor querySpecifiedNumAftSchedules(long timeInMillis,int limitnum)
	{
		return database.query(false, 
								DatabaseHelper.TAB_LOCAL_SCHEDULE, 
								null, 
								DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " > ? and "  + DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG + " != 'D'", 
								new String[] {String.valueOf(timeInMillis)}, 
								null, 
								null, 
								DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " ASC", 
								String.valueOf(limitnum));
	}
	
	public Cursor queryShareSchedules(int start, int end) {
		return database
				.query(DatabaseHelper.TAB_SHARE_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_ORDER + " =? ",
						new String[] { "0", String.valueOf(start), String.valueOf(end) }, 
						null,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " DESC " + " LIMIT ? , ? ");
	}
	
	public Cursor queryShareSchedules() {
		return database
				.query(DatabaseHelper.TAB_SHARE_SCHEDULE,
						new String[] { DatabaseHelper.COLUMN_SCHEDULE_UPDATE_TIME },
						null,
						null, 
						null,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_UPDATE_TIME + " DESC ");
	}
	
	public Cursor queryShareSchedules(long time, int size) {
		return database
				.query(DatabaseHelper.TAB_SHARE_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_ORDER + " =? AND " + DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " < ? ",
						new String[] { "0", String.valueOf(time), String.valueOf(size) }, 
						null,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " DESC " + " LIMIT ? ");
	}
	
	  public Cursor queryLocalSchedules(int start, int end) {

        return database.query(DatabaseHelper.TAB_LOCAL_SCHEDULE, null,
               DatabaseHelper.COLUMN_SCHEDULE_SHARE + " =? AND " +   DatabaseHelper.COLUMN_SCHEDULE_ORDER + " =?", new String[] {
                       "1", "0",  String.valueOf(start), String.valueOf(end)
                }, null, null, DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " DESC "
                        + " LIMIT ? , ? ");

    }
    
    public Cursor queryLocalSchedules(long time, int size) {

        return database.query(DatabaseHelper.TAB_LOCAL_SCHEDULE, null,
                DatabaseHelper.COLUMN_SCHEDULE_SHARE + " =? AND "
                        + DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " < ? AND " +   DatabaseHelper.COLUMN_SCHEDULE_ORDER + " =?", new String[] {
                        "1", String.valueOf(time), "0", String.valueOf(size)
                }, null, null, DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " DESC "
                        + " LIMIT ? ");

    }
	
	public Cursor queryMyShareSchedules() {
		return database
				.query(DatabaseHelper.TAB_LOCAL_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_SHARE + " =? ",
						new String[] { "1", "0", "20" }, 
						null,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " ASC " + " LIMIT ? , ? ");
	}
	
	public Cursor querySlaveShareSchedules(int t_id) {
		return database
				.query(DatabaseHelper.TAB_SHARE_SCHEDULE,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_SLAVE_ID + " =? AND " 
						+ DatabaseHelper.COLUMN_SCHEDULE_ORDER +" >? ",
						new String[] {String.valueOf(t_id),"0"}, 
						null,
						null,
						DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " ASC ");
	}
	

	public int deleteContact() {
		return database.delete(DatabaseHelper.ASCHEDULE_CONTACT, null, null);
	}

	public long insertContact(ContentValues values) {
		return database.insert(DatabaseHelper.ASCHEDULE_CONTACT, null, values);
	}

	public Cursor getAScheduleContacts() {
		return database.query(DatabaseHelper.ASCHEDULE_CONTACT, null, null,
				null, null, null, null);
	}

	public Cursor getLocalContacts() {
		return context.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
				null, null);
	}
	public Cursor queryContactIdByTel(String tel){
		return database.query(DatabaseHelper.ASCHEDULE_CONTACT, null, DatabaseHelper.ASCHEDULE_CONTACT_NUM + " =? ", new String[]{tel}, null, null, null);
	}
	public void updateContactType(Cursor cursor, int type, String useId){
		while(cursor.moveToNext()){
			String id = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACT_ID));
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.ASCHEDULE_CONTACT_TYPE, type);
			values.put(DatabaseHelper.COLUMN_FRIEND_ID, useId);
			database.update(DatabaseHelper.ASCHEDULE_CONTACT, values, DatabaseHelper.COLUMN_CONTACT_ID + " =? ", new String[] { String.valueOf(id)});
		}
		cursor.close();
	}
	public Cursor queryContactUse(){
		return database.query(DatabaseHelper.ASCHEDULE_CONTACT, null, DatabaseHelper.ASCHEDULE_CONTACT_TYPE + " =? ", new String[] { String.valueOf(Constant.FriendType.friend_contact_use)}, null, null, null);
	}

	public boolean hasData(int contactid, String number) {
		Cursor cursor = database.query(DatabaseHelper.ASCHEDULE_CONTACT, null,
				DatabaseHelper.COLUMN_CONTACT_ID + " = ? and "
						+ DatabaseHelper.ASCHEDULE_CONTACT_NUM + " = ?",
				new String[] { String.valueOf(contactid), number }, null, null,
				null);

		int count = cursor.getCount();
		cursor.close();

		return count > 0 ? true : false;
	}

	public void addFriend(ContentValues values){
		database.insert(DatabaseHelper.ASCHEDULE_FRIEND, null,values);
	}
public void deleteFriend(String id){
		database.delete(DatabaseHelper.ASCHEDULE_FRIEND, DatabaseHelper.COLUMN_FRIEND_ID+ " =? ", new String[] { String.valueOf(id)});
	}
	public void ignoreFriend(String id){
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.ASCHEDULE_FRIEND_TYPE, Constant.FriendType.friend_Ignore);
		values.put(DatabaseHelper.ASCHEDULE_FRIEND_ID, id);
		database.update(DatabaseHelper.ASCHEDULE_FRIEND, values, DatabaseHelper.COLUMN_FRIEND_ID+ " =? ", new String[] { String.valueOf(id)});
	}
	public void UpdateContactUse(int id){
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.ASCHEDULE_CONTACT_TYPE, Constant.FriendType.friend_contact_use);
		database.update(DatabaseHelper.ASCHEDULE_CONTACT, values, DatabaseHelper.COLUMN_CONTACT_ID + " =? ", new String[] { String.valueOf(id)});
	}
	public void UpdateContactFriend(int id){
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.ASCHEDULE_CONTACT_TYPE, Constant.FriendType.friend_contact);
		database.update(DatabaseHelper.ASCHEDULE_CONTACT, values, DatabaseHelper.COLUMN_CONTACT_ID + " =? ", new String[] { String.valueOf(id)});
	}

	public Cursor queryFriendTel(int id){
		return database.query(DatabaseHelper.ASCHEDULE_FRIEND, null, DatabaseHelper.ASCHEDULE_FRIEND_ID + " =? ", new String[] { String.valueOf(id)}, null, null, null);
	}

	
	public String queryNameByTel(String tel){
		String name = null;
		Cursor cursor = database.query(DatabaseHelper.ASCHEDULE_CONTACT, null, DatabaseHelper.ASCHEDULE_CONTACT_NUM+ " =? ", new String[] {tel}, null, null, null);
		if(cursor.moveToNext()){
			name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ASCHEDULE_CONTACT_NAME));
		}
		cursor.close();
		return name;
	}	

	public void updateScheduleById(long id, ContentValues cv) {
		 database.update(DatabaseHelper.TAB_LOCAL_SCHEDULE, cv,
				DatabaseHelper.COLUMN_SCHEDULE_ID + " =? ",
				new String[] { String.valueOf(id) });
	}

	public Cursor queryScheduleById(long id) {
		return database.query(DatabaseHelper.TAB_LOCAL_SCHEDULE, null,
				DatabaseHelper.COLUMN_SCHEDULE_ID + " = ?",
				new String[] { String.valueOf(id) }, null, null, null);

	}

	public boolean deleteScheduleById(int id) {
		return database.delete(DatabaseHelper.TAB_LOCAL_SCHEDULE,
				DatabaseHelper.COLUMN_SCHEDULE_ID + " =? ",
				new String[] { String.valueOf(id) }) > 0;

	}

	public Cursor queryMaxTid() {
		return database.rawQuery("SELECT MAX(tid) FROM "
				+ DatabaseHelper.TAB_LOCAL_SCHEDULE, null);
	}


	public boolean insertScheduleWeather(ContentValues cv) {
		return database.insert(DatabaseHelper.TAB_SCHEDULE_WEATHER, null, cv) > 0;
	}


	public Cursor queryNotOutdateschedule( ){
		return database.query(DatabaseHelper.TAB_LOCAL_SCHEDULE, null, DatabaseHelper.COLUMN_SCHEDULE_FLAG_OUTDATE + " = ? AND " + DatabaseHelper.COLUMN_SCHEDULE_NOTICE_FLAG + " =?",
				new String[] { String.valueOf(0) , String.valueOf(1)}, null, null, null);
		
	}
	public long insertSchedules(ContentValues values) {
		return database.insert(DatabaseHelper.TAB_LOCAL_SCHEDULE, null, values) ;
	}
	public int deleteScheduleWeather() {
		return database.delete(DatabaseHelper.TAB_SCHEDULE_WEATHER, null, null);
	}

public Cursor queryScheduleWeather(String date){
		return database.query(DatabaseHelper.TAB_SCHEDULE_WEATHER, null,
				DatabaseHelper.COLUMN_WEATHER_DATE + " = ?",
				new String[] { String.valueOf(date) }, null, null, null);
		
	}
}
