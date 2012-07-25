package com.archermind.schedule.Provider;

import java.util.ArrayList;
import java.util.Calendar;

import com.archermind.schedule.Model.ScheduleDateTag;
import com.archermind.schedule.Model.ScheduleVO;
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
	
	/**
	 * �����ճ���Ϣ
	 * @param scheduleVO
	 */
	public int save(ScheduleVO scheduleVO){
		//dbOpenHelper = new DBOpenHelper(context, "schedules.db");
		ContentValues values = new ContentValues();
		values.put("scheduleTypeID", scheduleVO.getScheduleTypeID());
		values.put("remindID", scheduleVO.getRemindID());
		values.put("scheduleContent", scheduleVO.getScheduleContent());
		values.put("scheduleDate", scheduleVO.getScheduleDate());
		database.beginTransaction();
		int scheduleID = -1;
		try{
			database.insert("schedule_test", null, values);
		    Cursor cursor = database.rawQuery("select max(scheduleID) from schedule_test", null);
		    if(cursor.moveToFirst()){
		    	scheduleID = (int) cursor.getLong(0);
		    }
		    cursor.close();
		    database.setTransactionSuccessful();
		}finally{
			database.endTransaction();
		}
	    return scheduleID;
	}
	
	/**
	 * ��ѯĳһ���ճ���Ϣ
	 * @param scheduleID
	 * @return
	 */
	public ScheduleVO getScheduleByID(int scheduleID){
		//dbOpenHelper = new DBOpenHelper(context, "schedules.db");
		Cursor cursor = database.query("schedule_test", new String[]{"scheduleID","scheduleTypeID","remindID","scheduleContent","scheduleDate"}, "scheduleID=?", new String[]{String.valueOf(scheduleID)}, null, null, null);
		if(cursor.moveToFirst()){
			int schID = cursor.getInt(cursor.getColumnIndex("scheduleID"));
			int scheduleTypeID = cursor.getInt(cursor.getColumnIndex("scheduleTypeID"));
			int remindID = cursor.getInt(cursor.getColumnIndex("remindID"));
			String scheduleContent = cursor.getString(cursor.getColumnIndex("scheduleContent"));
			String scheduleDate = cursor.getString(cursor.getColumnIndex("scheduleDate"));
			cursor.close();
			return new ScheduleVO(schID,scheduleTypeID,remindID,scheduleContent,scheduleDate);
		}
		cursor.close();
		return null;
		
	}
	
	/**
	 * ��ѯ���е��ճ���Ϣ
	 * @return
	 */
	public ArrayList<ScheduleVO> getAllSchedule(){
		ArrayList<ScheduleVO> list = new ArrayList<ScheduleVO>();
		Cursor cursor = database.query("schedule_test", new String[]{"scheduleID","scheduleTypeID","remindID","scheduleContent","scheduleDate"}, null, null, null, null, "scheduleID desc");
		while(cursor.moveToNext()){
			int scheduleID = cursor.getInt(cursor.getColumnIndex("scheduleID")); 
			int scheduleTypeID = cursor.getInt(cursor.getColumnIndex("scheduleTypeID"));
			int remindID = cursor.getInt(cursor.getColumnIndex("remindID"));
			String scheduleContent = cursor.getString(cursor.getColumnIndex("scheduleContent"));
			String scheduleDate = cursor.getString(cursor.getColumnIndex("scheduleDate"));
			ScheduleVO vo = new ScheduleVO(scheduleID,scheduleTypeID,remindID,scheduleContent,scheduleDate);
			list.add(vo);
		}
		cursor.close();
		if(list != null && list.size() > 0){
			return list;
		}
		return null;
		
	}
	
	/**
	 * ɾ���ճ�
	 * @param scheduleID
	 */
	public void delete(int scheduleID){
		database.beginTransaction();
		try{
			database.delete("schedule_test", "scheduleID=?", new String[]{String.valueOf(scheduleID)});
			database.delete("scheduletagdate", "scheduleID=?", new String[]{String.valueOf(scheduleID)});
			database.setTransactionSuccessful();
		}finally{
			database.endTransaction();
		}
	}
	
	/**
	 * �����ճ�
	 * @param vo
	 */
	public void update(ScheduleVO vo){
		ContentValues values = new ContentValues();
		values.put("scheduleTypeID", vo.getScheduleTypeID());
		values.put("remindID", vo.getRemindID());
		values.put("scheduleContent", vo.getScheduleContent());
		values.put("scheduleDate", vo.getScheduleDate());
		database.update("schedule_test", values, "scheduleID=?", new String[]{String.valueOf(vo.getScheduleID())});
	}
	
	/**
	 * ���ճ̱�־���ڱ��浽��ݿ���
	 * @param dateTagList
	 */
	public void saveTagDate(ArrayList<ScheduleDateTag> dateTagList){
		ScheduleDateTag dateTag = new ScheduleDateTag();
		for(int i = 0; i < dateTagList.size(); i++){
			dateTag = dateTagList.get(i);
			ContentValues values = new ContentValues();
			values.put("year", dateTag.getYear());
			values.put("month", dateTag.getMonth());
			values.put("day", dateTag.getDay());
			values.put("scheduleID", dateTag.getScheduleID());
			database.insert("scheduletagdate", null, values);
		}
	}
	
	/**
	 * ֻ��ѯ����ǰ�µ��ճ�����
	 * @param currentYear
	 * @param currentMonth
	 * @return
	 */
	public ArrayList<ScheduleDateTag> getTagDate(int currentYear, int currentMonth){
		ArrayList<ScheduleDateTag> dateTagList = new ArrayList<ScheduleDateTag>();
		Cursor cursor = database.query("scheduletagdate", new String[]{"tagID","year","month","day","scheduleID"}, "year=? and month=?", new String[]{String.valueOf(currentYear),String.valueOf(currentMonth)}, null, null, null);
		while(cursor.moveToNext()){
			int tagID = cursor.getInt(cursor.getColumnIndex("tagID"));
			int year = cursor.getInt(cursor.getColumnIndex("year"));
			int month = cursor.getInt(cursor.getColumnIndex("month"));
			int day = cursor.getInt(cursor.getColumnIndex("day"));
			int scheduleID = cursor.getInt(cursor.getColumnIndex("scheduleID"));
			ScheduleDateTag dateTag = new ScheduleDateTag(tagID,year,month,day,scheduleID);
			dateTagList.add(dateTag);
			}
		cursor.close();
		if(dateTagList != null && dateTagList.size() > 0){
			return dateTagList;
		}
		return null;
	}
	
	/**
	 * �����ÿһ��gridview��itemʱ,��ѯ�������������е��ճ̱��(scheduleID)
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public String[] getScheduleByTagDate(int year, int month, int day){
		ArrayList<ScheduleVO> scheduleList = new ArrayList<ScheduleVO>();
		//���ʱ���ѯ���ճ�ID��scheduleID����һ�����ڿ��ܶ�Ӧ����ճ�ID
		Cursor cursor = database.query("scheduletagdate", new String[]{"scheduleID"}, "year=? and month=? and day=?", new String[]{String.valueOf(year),String.valueOf(month),String.valueOf(day)}, null, null, null);
		String scheduleIDs[] = null;
		scheduleIDs = new String[cursor.getCount()];
		int i = 0;
		while(cursor.moveToNext()){
			String scheduleID = cursor.getString(cursor.getColumnIndex("scheduleID"));
			scheduleIDs[i] = scheduleID;
			i++;
		}
		cursor.close();
		
		return scheduleIDs;
		
		
	}
	
}