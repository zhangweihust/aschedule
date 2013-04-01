package com.android.schedule;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class ScheduleApplication extends Application {

	private static ScheduleApplication instance;
	private final static String TAG = "Schedule";

	public ScheduleApplication() {
		ScheduleApplication.instance = this;
	}

	public static Context getContext() {
		return ScheduleApplication.instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public static void LogD(Class classz, String str) {
		Log.d(TAG, classz.getCanonicalName() + "--->" + str);
	}

	public static void LogI(Class classz, String str) {
		Log.i(TAG, classz.getCanonicalName() + "--->" + str);
	}

	public static void LogE(Class classz, String str) {
		Log.e(TAG, classz.getCanonicalName() + "--->" + str);
	}

	public static void LogV(Class classz, String str) {
		Log.v(TAG, classz.getCanonicalName() + "--->" + str);
	}

	public static void logException(Class c, Throwable e) {
		try {
			StringBuilder exceptionInfo = new StringBuilder();
			if (e == null) {
				exceptionInfo.append("Exception:"
						+ "e is null,probably null pointer exception" + "\n");
			} else {
				e.printStackTrace();
				exceptionInfo.append(e.getClass().getCanonicalName() + ":"
						+ e.getMessage() + "\n");
				StackTraceElement[] stes = e.getStackTrace();
				for (StackTraceElement ste : stes) {
					exceptionInfo.append("at " + ste.getClassName() + "$"
							+ ste.getMethodName() + "$" + ste.getFileName()
							+ ":" + ste.getLineNumber() + "\n");
				}
			}

			LogE(c, exceptionInfo.toString());
		} catch (Exception ex) {
			LogE(c, ex.getMessage());
		}
	}
}
