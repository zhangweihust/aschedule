package com.android.schedule.Utils;

public class StackTraceHelper {
	public static void callStackPrint() {
		try {
			throw new RuntimeException();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
}
