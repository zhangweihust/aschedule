package com.archermind.schedule.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.archermind.schedule.Model.Friend;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Screens.FriendScreen;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.ServerInterface;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

public class FriendTask extends AsyncTask<Void, Integer, HashMap<String, List<Friend>>> {
	private ServerInterface serverInterface;
	private DatabaseManager database;
	private FriendScreen friendScreen;
	private ProgressDialog dialog;
	public FriendTask(FriendScreen friendScreen){
		this.friendScreen = friendScreen;
		database = ServiceManager.getDbManager();
		serverInterface = new ServerInterface();
		dialog = new ProgressDialog(friendScreen);
	}
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		dialog.show();
	}
	
	   private void makeList(String str, List<String> list){
		   str.replaceAll("\"", "");
		   System.out.println("^^^^^^^^^^^^^^^^^^^^^^^   str = "+str);
			 if(str != null && !str.equals("")){
				 String[] str1 = str.split(",");
				 if(str1.length > 0){
					 for(int k = 0 ;k < str1.length; k++){
						 String str11 = str1[k];
						 if(str11.matches("[0-9]+")){
							 list.add(str11);
						 }
					 }
				 }else{
					 if(str.matches("[0-9]+")){
						 list.add(str);
					 }
				 }
			 }
	   }
	   private Friend makeFriend1(String str,int type){
		   str.replaceAll("\"", "");
			if(str != null && !"".equals(str)){
				if(str.indexOf("tel") >= 0){//防止返回错误码
					str = str.replace("[{", "").replace("}]", "");
					String[] strs = str.split(",");
					
					
					String[] str1 = strs[0].split(":");
					
					System.out.println("0000  = "+ strs[0]);
					
					String tel = str1[1];
					
					String[] str2 = strs[1].split(":");
					String nike_name = str2[1];
					
					String[] str3 = strs[2].split(":");
					String photo_url = str2[1];
					Friend friend = new Friend();
					friend.setTelephone(tel);
					friend.setType(type);
					friend.setName(nike_name);
					return friend;
				}
			}
			
			return null;
		}
	   private Friend makeFriend(String str){
			str.replaceAll("\"", "");
			if(str != null && !"".equals(str)){
				if(str.indexOf("user_id") >= 0){//防止返回错误码
					str = str.replace("[{", "").replace("}]", "");
					String[] strs = str.split(",");
					
					
					String[] str1 = strs[0].split(":");
					
					System.out.println("0000  = "+ strs[0]);
					
					String id = str1[1];
					
					String[] str2 = strs[1].split(":");
					String nike_name = str2[1];
					
					String[] str3 = strs[2].split(":");
					String photo_url = str2[1];
					return new Friend(id,nike_name,Constant.FriendType.friend_contact_use);
				}
			}
			
			return null;
		}
	@Override
	protected HashMap<String, List<Friend>> doInBackground(Void... params) {
		// TODO Auto-generated method stub
		 List<String> contactToalList = new ArrayList<String>();
		 List<String> friendList = new ArrayList<String>();
		 List<String> ignoreList = new ArrayList<String>();
		 List<String> contactList = new ArrayList<String>();
		 
		 
		 String str = serverInterface.getFriendRel("3");
		 System.out.println("str = " + str);
		 if(str != null && !str.equals("")){
			 String[] strs = str.split(":");
			 if(strs.length > 0){
				 for(int i = 0 ;i < strs.length ; i++){
					 if(i == 0){
						 makeList(strs[i], new ArrayList<String>());
					 }else if(i == 1){
						 makeList(strs[i], new ArrayList<String>());
					 }else if(i == 2){
						 //联系人
						 makeList(strs[i], contactToalList);
					 }else if(i == 3){
						 //好友
						 makeList(strs[i], friendList);
					 }else if(i == 4){
						 //屏蔽
						 makeList(strs[i], ignoreList);
					 }
				 }
			 }
		 }
		 System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% size = "+friendList.size());
		 List<Friend> friends = new ArrayList<Friend>();
		 Cursor cursor = null;
		 for(String id : friendList){
			 cursor = database.queryFriendTel(Integer.parseInt(id));
			 if(cursor.moveToNext()){
				 String telephone = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_NUM));
				 contactToalList.remove(telephone);
				 Friend friend = new Friend(id,telephone,Constant.FriendType.friend_yes);
				 friends.add(friend);
				 contactToalList.remove(friend.getTelephone().replace("\"", ""));
			 }else{
				 //向服务器查询数据(构造Friend,向本地数据库插入数据)
				 String result = serverInterface.findUserInfobyUserId(id);
				 Friend friend = makeFriend1(result,Constant.FriendType.friend_yes);
				 if(friend != null){
					 friends.add(friend);
					 ContentValues values = new ContentValues();
					 values.put(DatabaseHelper.ASCHEDULE_FRIEND_ID, id);
					 values.put(DatabaseHelper.ASCHEDULE_FRIEND_TYPE, Constant.FriendType.friend_yes);
					 values.put(DatabaseHelper.ASCHEDULE_FRIEND_NUM, friend.getTelephone().replace("\"", ""));
					 database.addFriend(values);
					 System.out.println("&&&&&&&&&&&&&&&&&&&&&& getTelephone = "+friend.getTelephone()+ "   name = "+friend.getName());
					 contactToalList.remove(friend.getTelephone().replace("\"", ""));
				 }
			 }
		 }
		 List<Friend> ignores = new ArrayList<Friend>();
		 for(String id : ignoreList){
			 cursor = database.queryFriendTel(Integer.parseInt(id));
			 if(cursor.moveToNext()){
				 String telephone = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_NUM));
				 contactToalList.remove(telephone);
				 Friend friend = new Friend(id,telephone,Constant.FriendType.friend_Ignore);
				 ignores.add(friend);
				 contactToalList.remove(friend.getTelephone());
			 }else{
				//向服务器查询数据(构造Friend,向本地数据库插入数据)
				 String result = serverInterface.findUserInfobyUserId(id);
				 Friend friend = makeFriend1(result,Constant.FriendType.friend_Ignore);
				 if(friend != null){
					 friends.add(friend);
					 ContentValues values = new ContentValues();
					 values.put(DatabaseHelper.ASCHEDULE_FRIEND_ID, id);
					 values.put(DatabaseHelper.ASCHEDULE_FRIEND_TYPE, Constant.FriendType.friend_Ignore);
					 values.put(DatabaseHelper.ASCHEDULE_FRIEND_NUM, friend.getTelephone());
					 database.addFriend(values);
					 contactToalList.remove(friend.getTelephone());
				 }
			 }
		 }

		 
		 
		 List<Friend> contact_use = new ArrayList<Friend>();
		 cursor = database.queryContactUse();
		 if(cursor.getCount() > 0){
			 while(cursor.moveToNext()){
				 String id = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACT_ID));
				 String tel = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ASCHEDULE_CONTACT_NUM));
				 String imgPath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACT_ID));
				 Friend friend = new Friend();	
				 friend.setId(id);
				 friend.setHeadImagePath(imgPath);
				 friend.setTelephone(tel);
				 friend.setType(Constant.FriendType.friend_contact);
				 contact_use.add(friend);
				 contactToalList.remove(tel);
			 }
		 }else{
			 List<String> tempList = new ArrayList<String>();
			 for(String tel : contactToalList){
				 String result = serverInterface.isfriendSchedule(tel);
				 System.out.println("result = "+result);
				 Friend friend = makeFriend(result);
				 if(friend != null){
					 tempList.add(tel);
					 contact_use.add(friend);
//					 ContentValues values = new ContentValues();
//					 values.put(DatabaseHelper.ASCHEDULE_CONTACT_TYPE, Constant.FriendType.friend_contact_use);
//					 values.put(DatabaseHelper.ASCHEDULE_CONTACT_NUM, friend.getTelephone());
//					 values.put(DatabaseHelper.COLUMN_CONTACT_ID, value);
//					 database.insertContact(values);
					 cursor = database.queryContactIdByTel(tel);
					 database.updateContactType(cursor, Constant.FriendType.friend_contact_use,friend.getId().replace("\"",""));
				 }
			 }
			 for(String tel : tempList){
				 contactToalList.remove(tel);
			 }
		 }		 		 		 
			 
		 List<Friend> contact = new ArrayList<Friend>();
		 for(String tel : contactToalList){
			 cursor = database.queryContactIdByTel(tel);
			 if(cursor.moveToNext()){
				 String id = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACT_ID));
				 String imgPath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACT_ID));
				 Friend friend = new Friend();	
				 friend.setId(id);
				 friend.setHeadImagePath(imgPath);
				 friend.setTelephone(tel);
				 friend.setType(Constant.FriendType.friend_contact);
				 contact.add(friend);
				 System.out.println("********************id = "+id);
			 }
		 }
		 
		 if(cursor != null){
			 cursor.close();
		 }
		 
		 HashMap<String, List<Friend>> hashMap = new HashMap<String, List<Friend>>();
		 hashMap.put(Constant.FriendType.FRIEND_YES_KEY, friends);
		 hashMap.put(Constant.FriendType.FRIEND_IGNORE_KEY, ignores);
		 hashMap.put(Constant.FriendType.FRIEND_CONTACT_USE_KEY, contact_use);
		 hashMap.put(Constant.FriendType.FRIEND_CONTACT_KEY, contact);
		return hashMap;
	}
	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}
	@Override
	protected void onPostExecute(HashMap<String, List<Friend>> result) {
		// TODO Auto-generated method stub
		if(dialog.isShowing()){
			dialog.dismiss();
		}
		friendScreen.initAdapter(result);
	}
	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		super.onCancelled();
	}

}
