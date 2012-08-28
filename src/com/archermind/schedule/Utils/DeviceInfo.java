package com.archermind.schedule.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;

import com.archermind.schedule.ScheduleApplication;

public class DeviceInfo {
	/*
	 * 获取当前程序的版本号
	 */
	public static int getMyVersionCode() {
		PackageInfo pinfo;
		try {
			pinfo = ScheduleApplication
					.getContext()
					.getPackageManager()
					.getPackageInfo(ScheduleApplication.getContext().getPackageName(),
							PackageManager.GET_CONFIGURATIONS);
			return pinfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}



	/**
	 * 获得手机品牌
	 * 
	 * @return
	 */
	public static String getDeviceManufacturer() {
		return android.os.Build.MANUFACTURER;
	}

	/**
	 * 获得手机型号
	 * 
	 * @return
	 */
	public static String getDeviceModel() {
		return android.os.Build.MODEL;
	}

	/**
	 * 获取IMEI
	 * 
	 * @return
	 */
	public static String getDeviceIMEI() {
		TelephonyManager telephonyManager = (TelephonyManager) ScheduleApplication
				.getContext().getSystemService(
						Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}

	/**
	 * 获取手机号
	 * 
	 * @return
	 */
	public static String getDevicePhoneNumber() {
		TelephonyManager telephonyManager = (TelephonyManager) ScheduleApplication
				.getContext().getSystemService(
						Context.TELEPHONY_SERVICE);
		return telephonyManager.getLine1Number();
	}

	/**
	 * 获取运营商名称
	 * 
	 * @return
	 */
	public static String getDeviceOperatorName() {
		TelephonyManager telephonyManager = (TelephonyManager) ScheduleApplication
				.getContext().getSystemService(
						Context.TELEPHONY_SERVICE);
		return telephonyManager.getNetworkOperatorName();
	}
	
	/**
	 * 获取IMSI
	 * 
	 * @return
	 */
	public static String getDeviceIMSI() {
		TelephonyManager telephonyManager = (TelephonyManager) ScheduleApplication
				.getContext().getSystemService(
						Context.TELEPHONY_SERVICE);
		return telephonyManager.getSubscriberId();
	}
	
	/**
	 * 获取系统版本号
	 * 
	 * @return
	 */
	public static String getDeviceSystemVersion() {
		return android.os.Build.VERSION.RELEASE;
	}
	
	/**
	 * 获取CPU型号
	 * 
	 * @return
	 */
	public static String getDeviceCpuModel() {
		String value = null;
		FileReader fr = null;
		BufferedReader localBufferedReader = null;
		String str1 = "/proc/cpuinfo";
		try {
			fr = new FileReader(str1);
			localBufferedReader = new BufferedReader(fr, 8192);
			String str2 = localBufferedReader.readLine();
			String[] arrayOfString = str2.split("\\s+");
			for (int i = 2; i < arrayOfString.length; i++) {
				value += arrayOfString[i] + " ";
			}
		} catch (IOException e) {
		} finally {
			try {
				if (localBufferedReader != null) {
					localBufferedReader.close();
				}
				if (fr != null) {
					fr.close();
				}
			} catch (Exception e) {

			}
		}
		return value;
	}
	
	
	
}
