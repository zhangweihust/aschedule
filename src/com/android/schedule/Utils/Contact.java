package com.android.schedule.Utils;

import com.android.schedule.ScheduleApplication;
import com.android.schedule.Events.EventArgs;
import com.android.schedule.Events.EventTypes;
import com.android.schedule.Provider.DatabaseHelper;
import com.android.schedule.Screens.ContactSyncAlertScreen;
import com.android.schedule.Services.ServiceManager;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;

public class Contact {
	private static boolean isSyncing = false;
	/* 检测是否需要将联系人与服务器进行同步 */
	public boolean checkIfNeedSync() {
		try {
			ScheduleApplication.LogD(getClass(), "检测是否需要将联系人与服务器进行同步");
			Cursor AscheduleContact = ServiceManager.getDbManager()
					.getAScheduleContacts();
			int contactid = 0;
			String number = "";
			if (AscheduleContact.getCount() <= 0) {
				AscheduleContact.close();
				ScheduleApplication.LogD(getClass(), "应用的联系人为空，需要重新同步");
				return true;
			}

			Cursor LocalContact = ServiceManager.getDbManager().getLocalContacts();
			if (LocalContact.getCount() != AscheduleContact.getCount()) {
				ScheduleApplication.LogD(getClass(), "应用的联系人与系统的联系人数量不一致，需要重新同步 LocalContact.getCount() =" 
						+ LocalContact.getCount() + "  AscheduleContact.getCount() = " + AscheduleContact.getCount());
				AscheduleContact.close();
				LocalContact.close();
				return true;
			}

			while (LocalContact.moveToNext()) {
				contactid = Integer
						.parseInt(LocalContact.getString(LocalContact
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
				number = LocalContact
						.getString(LocalContact
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				if (!ServiceManager.getDbManager().hasData(contactid, number)) {
					AscheduleContact.close();
					LocalContact.close();
					ScheduleApplication.LogD(getClass(),
							"应用的联系人中的数据内容与系统联系人不一致，需要重新同步");
					return true;
				}
			}
			ScheduleApplication.LogD(getClass(), "应用的联系人中的数据与系统联系人一致，不需要重新同步");
			AscheduleContact.close();
			LocalContact.close();
		} catch (Exception e) {
			ScheduleApplication.logException(Contact.class, e);
		}
		return false;
	}
    
	public void checkSync(final Context context) {
		new Thread() {
			public void run() {
				if (isSyncing) /* 正在同步 */
				{
					ScheduleApplication.LogD(getClass(), "正在同步中....");
					return;
				}
				try {
					if (NetworkUtils.getNetworkState(context) != NetworkUtils.NETWORN_NONE) {
						/* 还要判断用户是否已登录 */
						if (ServiceManager.isUserLogining(ServiceManager
								.getUserId())) {
							if (ServiceManager.getBindFlag()) {// 判断用户是否绑定
								if (checkIfNeedSync()) {
									ScheduleApplication.LogD(getClass(),
											"需要同步，弹出用户确认同步对话框");
									Intent it = new Intent(context,
											ContactSyncAlertScreen.class);
									context.startActivity(it);
								} else {
									ScheduleApplication.LogD(getClass(),
											"联系人数据一致，不需要同步");
									ServiceManager
											.getEventservice()
											.onUpdateEvent(
													new EventArgs(
															EventTypes.CONTACT_SYNC_CANCEL));
								}
							} else {
								ScheduleApplication.LogD(getClass(), "用户未绑定，不需要同步");
								ServiceManager.getEventservice().onUpdateEvent(
										new EventArgs(
												EventTypes.CONTACT_SYNC_CANCEL));
							}
						} else {
							ScheduleApplication.LogD(getClass(), "用户还未登录，不需要同步");
							ServiceManager.getEventservice().onUpdateEvent(
									new EventArgs(EventTypes.CONTACT_SYNC_CANCEL));
						}
					} else {
						ScheduleApplication.LogD(getClass(), "没有网络，不需要同步");
						ServiceManager.getEventservice().onUpdateEvent(
								new EventArgs(EventTypes.CONTACT_SYNC_CANCEL));
					}
				} catch (Exception e) {
					ScheduleApplication.logException(Contact.class, e);
				}
			};
		}.start();
	}

	public String getContactString() {
		// 将系统联系人拼成字符串上传给服务器
		String number = "";
		StringBuffer Contactsb = new StringBuffer();
		Cursor LocalContact = ServiceManager.getDbManager().getLocalContacts();
		while (LocalContact.moveToNext()) {
			number = LocalContact
					.getString(LocalContact
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			if (number.matches("^(\\+86)?1[3|4|5|8][0-9]\\d{8}$")) {
				number = number.replace("+86", "");
				if(Contactsb.indexOf(number)==-1){
					Contactsb.append(number + ",");
				}
			}
		}
		LocalContact.close();
		return Contactsb.toString();
	}

	public void updateAScheduleContact() {
		int contactid = 0;
		String number = "";
		String name = "";
		ContentValues cv = new ContentValues();
		Cursor LocalContact = ServiceManager.getDbManager().getLocalContacts();
		ServiceManager.getDbManager().deleteContact();
		while (LocalContact.moveToNext()) {
			contactid = Integer
					.parseInt(LocalContact.getString(LocalContact
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
			number = LocalContact
					.getString(LocalContact
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			name = LocalContact
					.getString(LocalContact
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			cv.put(DatabaseHelper.COLUMN_CONTACT_ID, contactid);
			cv.put(DatabaseHelper.ASCHEDULE_CONTACT_NUM, number);
			cv.put(DatabaseHelper.ASCHEDULE_CONTACT_NAME, name);
			cv.put(DatabaseHelper.ASCHEDULE_CONTACT_TYPE, Constant.FriendType.friend_contact);
			if(ServiceManager.getDbManager().insertContact(cv) == -1){
				ScheduleApplication.LogD(getClass(), "插入联系人失败：" + name + "  contactid:" + contactid);
			} 
		}
		LocalContact.close();
	}

	public boolean ContactSync(String username) {
		ServerInterface server = new ServerInterface();
		boolean ret = false;
		isSyncing = true;
		/* 同步成功了，将通讯录中的数据导入微日程数据库中 */
		if (server.uploadContact(username, getContactString()) == 0) {
			updateAScheduleContact();// 上传服务器成功将本地自己的联系人数据库替换为系统的联系人数据
			ret = true;
		}
		isSyncing = false;
		return ret;
	}
}
