package com.android.schedule.Utils;

import java.util.Calendar;

public class LostTime {
	private static long startT = -1;
	private static long sumStartT = -1;
	private static long sumSumT = -1;
	public static void reset() {
		startT = System.currentTimeMillis(); // 排序前取得当前时间
	}
	
	public static void cast(String info) {
		long t2 = System.currentTimeMillis(); // 排序后取得当前时间

		if (startT == -1) {
			startT = t2;
		}
		
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(t2 - startT);

		System.out.println("=CCC= " + info + " 耗时: " + c.get(Calendar.MINUTE) + "分 "
				+ c.get(Calendar.SECOND) + "秒 " + c.get(Calendar.MILLISECOND)
				+ " 毫秒");
	}
	
	public static void sum_reset() {
		sumSumT = 0; 
	}
	
	public static void sum_mark_start() {
		sumStartT = System.currentTimeMillis(); // 排序前取得当前时间
	}
	
	public static void sum_mark_end() {
		sumSumT += System.currentTimeMillis() - sumStartT; 
	}
	
	public static void sum_cast(String info) {	
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(sumSumT);

		System.out.println("=CCC= sum " + info + " 耗时: " + c.get(Calendar.MINUTE) + "分 "
				+ c.get(Calendar.SECOND) + "秒 " + c.get(Calendar.MILLISECOND)
				+ " 毫秒");
	}
}
