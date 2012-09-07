package com.archermind.schedule.Utils;

import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Screens.ContactSyncAlertScreen;
import com.archermind.schedule.Services.ServiceManager;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;

public class Contact {
	private boolean isSyncing = false;
	
//	private static DatabaseManager database;
//	
//	public Contact()
//	{
//		database = ServiceManager.getDbManager();
//	}
	
	/* 检测是否需要将联系人与服务器进行同步 */
	public boolean checkIfNeedSync()
	{
		Cursor AscheduleContact = ServiceManager.getDbManager().getAScheduleContacts();
		int contactid = 0;
		String number = "";
		
		if (AscheduleContact.getCount() <= 0)
		{
			AscheduleContact.close();
			return true;
		}
		
		Cursor LocalContact = ServiceManager.getDbManager().getLocalContacts();
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
			if (!ServiceManager.getDbManager().hasData(contactid, number))
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
	
	public void checkSync(final Context context)
	{
		new Thread()
		{
			public void run() 
			{
				if (isSyncing)		/*正在同步*/
				{
					return;
				}
				if (NetworkUtils.getNetworkState(context) != NetworkUtils.NETWORN_NONE)
				{
					/* 还要判断用户是否已登录 */
					if (ServiceManager.isUserLogining(ServiceManager.getUserId()))
					{
						if (checkIfNeedSync())
						{
							Intent it = new Intent(context,ContactSyncAlertScreen.class);
							context.startActivity(it);
						}
					}
				}
			};
		}.start();
	}
	
	public String getContactString()
	{
//		int contactid = 0;
		String number = "";
		StringBuffer Contactsb = new StringBuffer();
		Cursor LocalContact = ServiceManager.getDbManager().getLocalContacts();
		
		while (LocalContact.moveToNext())
		{
//			contactid = Integer.parseInt(LocalContact.getString(LocalContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
			number = LocalContact.getString(LocalContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

			if (number.matches("^(\\+86)?1[3|4|5|8][0-9]\\d{8}$"))
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
		String name = "";
		ContentValues cv = new ContentValues();
		Cursor LocalContact = ServiceManager.getDbManager().getLocalContacts();
		
		ServiceManager.getDbManager().deleteContact();
		
		while (LocalContact.moveToNext())
		{
			contactid = Integer.parseInt(LocalContact.getString(LocalContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
			number = LocalContact.getString(LocalContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			name = LocalContact.getString(LocalContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			cv.put(DatabaseHelper.COLUMN_CONTACT_ID,contactid);
			cv.put(DatabaseHelper.ASCHEDULE_CONTACT_NUM, number);
			cv.put(DatabaseHelper.ASCHEDULE_CONTACT_NAME, name);
			ServiceManager.getDbManager().insertContact(cv);
		}
		
		LocalContact.close();
	}
	
	public boolean ContactSync(String username)
	{
		ServerInterface server = new ServerInterface();
		boolean ret = false;
		
		isSyncing = true;
		/* 同步成功了，将通讯录中的数据导入微日程数据库中 */
		if (server.uploadContact(username,getContactString()) == 0)
		{
			updateAScheduleContact();
			ret = true;
		}
		
		isSyncing = false;
		
		return ret;
	}
	
//	/* 向服务器传送联系人信息的接口 */
//	public boolean Sync(String contactinfo)
//	{
//		return true;
//	}
	
//	/* 判断网络是否连通 */
//    public static boolean isNetAvailable(Context context)
//	{
//		boolean ret = false;
//		
//		ret = isWiFiActive(context) || isNetworkAvailable(context);
//		
//		return ret;
//	}
//	
//	public static boolean isWiFiActive(Context inContext) {
//		WifiManager mWifiManager = (WifiManager) inContext.getSystemService(Context.WIFI_SERVICE);
//		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
//		int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
//		if (mWifiManager.isWifiEnabled() && ipAddress != 0) 
//		{
//			return true;
//		} 
//		
//		return false;   
//	}
//	
//	public static boolean isNetworkAvailable(Context context) {
//        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (connectivity == null) 
//        {
//            return false;
//        } 
//        else 
//        {
//            NetworkInfo info = connectivity.getActiveNetworkInfo();
//            if(info == null)
//            {
//				return false;
//            }
//            else
//            {
//                if(info.isAvailable())
//                {
//                    return true;
//                }
//                  
//            }
//        }
//        
//        return false;
//    }
}
