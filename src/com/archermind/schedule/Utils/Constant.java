package com.archermind.schedule.Utils;

public class Constant {
	public static int flagType = 1;
	public static int YEAR;
	public static int MONTH;
	public static int DAY;
	public static int HOUR;
	public static int MIN;

	public static int VARY_YEAR;
	public static int VARY_MONTH;
	public static int VARY_DAY;
	public static int VARY_HOUR;
	public static int VARY_MIN;

	public static String VARY_LUNAR_YEAR;
	public static String VARY_LUNAR_MONTH;
	public static String VARY_LUNAR_DAY;

	public static int wheel_year = 1;
	public static int wheel_month = 2;
	public static int wheel_day = 3;
	public static int wheel_hour = 4;
	public static int wheel_min = 5;

	public final static class FriendType {
		public final static int friend_yes = 0;
		public final static int friend_Ignore = 1;
		public final static int friend_contact_use = 2;
		public final static int friend_contact = 3;
		public final static int friends_friends =4;//是好友的好友，不是自己的好友

		public final static String FRIEND_YES_KEY = "friend_yes_key";
		public final static String FRIEND_IGNORE_KEY = "friend_ingnore_key";
		public final static String FRIEND_CONTACT_USE_KEY = "friend_cantact_use_key";
		public final static String FRIEND_CONTACT_KEY = "friend_contact_key";
	}

	public final static class SendUserInfo {
		public final static String SEND_USER_DEVICE_INFO = "send_user_device_info";
		public final static String SEND_USER_ACTIVITY_INFO_DAYDATE = "send_user_activity_info_dayDate";
		public final static String SEND_USER_ACTIVITY_INFO_DATE = "send_user_activity_info_date";
		public final static String SEND_USER_ACTIVITY_INFO_TIMES = "send_user_activity_info_times";
		public final static String SEND_USER_ACTIVITY_INFO_TIMESTAMP = "send_user_activity_info_timestamp";
	}

	public final static class UrlInfo {
		public final static String USER_DEVICE_INFO_URL = "http://arc.archermind.com/ci/index.php/aschedule/setClientInfo";
		public final static String USER_ACTIVITY_INFO_URL = "http://arc.archermind.com/ci/index.php/aschedule/setUserActionInfo";
	}

	public static boolean refrash = false;
}
