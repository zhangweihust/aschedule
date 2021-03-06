package com.android.schedule.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.android.schedule.ScheduleApplication;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferenceUtil {
	private static SharedPreferences sp;
	static {
		sp = ScheduleApplication.getContext().getSharedPreferences("Data",
				ScheduleApplication.getContext().MODE_WORLD_WRITEABLE);
	}
	public static String getValue(String key, String defValue) {
		return sp.getString(key, defValue);
	}
	public static int getValue(String key, int defValue) {
		return sp.getInt(key, defValue);
	}
	public static void setValue(String key, String value) {
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}
	public static void setValue(String key, boolean value) {
		Editor editor = sp.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	public static void setValue(String key, int value) {
		Editor editor = sp.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	public static void setValues(HashMap<String, String> values) {
		Iterator<Entry<String, String>> iterator = values.entrySet().iterator();
		Entry<String, String> entry = null;
		Editor editor = sp.edit();
		while (iterator.hasNext()) {
			entry = iterator.next();
			editor.putString(entry.getKey(), entry.getValue());
		}
		editor.commit();
	}

}
