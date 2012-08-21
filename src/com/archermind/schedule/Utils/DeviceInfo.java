package com.archermind.schedule.Utils;

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
	
}
