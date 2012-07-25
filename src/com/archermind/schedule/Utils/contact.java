package com.archermind.schedule.Utils;

import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Services.ServiceManager;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

public class contact {
	
	private DatabaseManager database;
	
	public contact()
	{
		this.database = ServiceManager.getDbManager();
	}
	
	/* 检测是否需要将联系人与服务器进行同步 */
	public boolean checkIfNeedSync()
	{
		Cursor AscheduleContact = database.getAScheduleContacts();
		int contactid = 0;
		String number = "";
		
		if (AscheduleContact.getCount() <= 0)
		{
			AscheduleContact.close();
			return true;
		}
		
		Cursor LocalContact = database.getLocalContacts();
		if (LocalContact.getCount() != AscheduleContact.getCount())
		{
			AscheduleContact.close();
			LocalContact.close();
			return true;
		}
		
		while (LocalContact.moveToNext())
		{
			contactid = Integer.parseInt(LocalContact.getString(LocalContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
			number = LocalContact.getString(LocalContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			if (!database.hasData(contactid, number))
			{
				AscheduleContact.close();
				LocalContact.close();
				return true;
			}
		}
		
		AscheduleContact.close();
		LocalContact.close();
		return false;
	}
	
	public String getContactString()
	{
//		int contactid = 0;
		String number = "";
		StringBuffer Contactsb = new StringBuffer();
		Cursor LocalContact = database.getLocalContacts();
		
		while (LocalContact.moveToNext())
		{
//			contactid = Integer.parseInt(LocalContact.getString(LocalContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
			number = LocalContact.getString(LocalContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

			if (number.matches("^(\\+86)?1(3[4-9]|47|5[0-27-9]|8[278])\\d{8}$"))
			{
				number = number.replace("+86", "");
				Contactsb.append(number + ",");
			}
		}
		
		LocalContact.close();
		return Contactsb.toString();
	}
	
	public void updateAScheduleContact()
	{
		int contactid = 0;
		String number = "";
		ContentValues cv = new ContentValues();
		Cursor LocalContact = database.getLocalContacts();
		
		database.deleteContact();
		
		while (LocalContact.moveToNext())
		{
			contactid = Integer.parseInt(LocalContact.getString(LocalContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
			number = LocalContact.getString(LocalContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			cv.put(DatabaseHelper.COLUMN_CONTACT_ID,contactid);
			cv.put(DatabaseHelper.ASCHEDULE_CONTACT_NUM, number);
			database.insertContact(cv);
		}
		
		LocalContact.close();
	}
	
	public void ContactSync()
	{
		ServerInterface server = new ServerInterface();
		
		if (checkIfNeedSync())
		{
			Log.e("---lqf---","ContactSync() 联系人有变动，需要更新");
			/* 同步成功了，将通讯录中的数据导入微日程数据库中 */
			if (server.uploadContact("1111",getContactString()) == 0)
			{
				updateAScheduleContact();
			}
		}
		else
		{
			Log.e("---lqf---","ContactSync() 联系人没有变化，不需要更新");
		}
	}
	
//	/* 向服务器传送联系人信息的接口 */
//	public boolean Sync(String contactinfo)
//	{
//		return true;
//	}
	
}
