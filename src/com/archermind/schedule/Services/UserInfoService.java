package com.archermind.schedule.Services;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.archermind.schedule.Task.DeviceInfoThread;
import com.archermind.schedule.Task.UserActivityInfoThread;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.SharedPreferenceUtil;

public class UserInfoService implements IService {
	private int USER_FREQUENCY = 7;
	private static final long COUNT_DURATION = 10 * 60 * 1000;
	private static final int COUNT_TIMES = 3;
	private DeviceInfoThread deviceInfoThread;
	private UserActivityInfoThread userActivityInfoThread;
	private String startTime;
	private String exitTime;

	@Override
	public boolean start() {

		Date nowDate = new Date();
		SimpleDateFormat formatTime = new SimpleDateFormat("dd:HH:mm");
		startTime = formatTime.format(nowDate);

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		int date = Integer.parseInt(format.format(nowDate));

		SharedPreferenceUtil.setValue(
				Constant.SendUserInfo.SEND_USER_ACTIVITY_INFO_DAYDATE,
				String.valueOf(date));

		if ("false".equals(SharedPreferenceUtil.getValue(
				Constant.SendUserInfo.SEND_USER_DEVICE_INFO, "false"))) {
			// 发送请求
			deviceInfoThread = new DeviceInfoThread(0, this);
			deviceInfoThread.start();
//			SharedPreferenceUtil.setValue(
//					Constant.SendUserInfo.SEND_USER_DEVICE_INFO, "true");
		}

		int oldDate = Integer.parseInt(SharedPreferenceUtil.getValue(
				Constant.SendUserInfo.SEND_USER_ACTIVITY_INFO_DATE, "0"));
		if (date - oldDate > USER_FREQUENCY) {
			// 发送请求
			userActivityInfoThread = new UserActivityInfoThread(0, this);
			userActivityInfoThread.start();
		}
		return true;
	}

	@Override
	public boolean stop() {
		Date nowDate = new Date();
		SimpleDateFormat formatTime = new SimpleDateFormat("dd:HH:mm");
		exitTime = formatTime.format(nowDate);

		SimpleDateFormat formatDayDate = new SimpleDateFormat("yyyyMMdd");
		int dayDate = Integer.parseInt(formatDayDate.format(nowDate));

		int oldDayDate = Integer.parseInt(SharedPreferenceUtil.getValue(
				Constant.SendUserInfo.SEND_USER_ACTIVITY_INFO_DAYDATE, "0"));
		if (dayDate - oldDayDate > 0) {
			int times = Integer.parseInt(SharedPreferenceUtil.getValue(
					Constant.SendUserInfo.SEND_USER_ACTIVITY_INFO_TIMES, "1"));
			times++;
			SharedPreferenceUtil.setValue(
					Constant.SendUserInfo.SEND_USER_ACTIVITY_INFO_DAYDATE,
					String.valueOf(dayDate));
			SharedPreferenceUtil.setValue(
					Constant.SendUserInfo.SEND_USER_ACTIVITY_INFO_TIMES,
					String.valueOf(times));
		}
		int timesTamp = Integer.parseInt(SharedPreferenceUtil.getValue(
				Constant.SendUserInfo.SEND_USER_ACTIVITY_INFO_TIMESTAMP, "0"));
		timesTamp += getTimestamp(startTime, exitTime);
		SharedPreferenceUtil.setValue(
				Constant.SendUserInfo.SEND_USER_ACTIVITY_INFO_TIMESTAMP,
				String.valueOf(timesTamp));

		if (deviceInfoThread != null) {
			deviceInfoThread.stopThread();
		}
		if (userActivityInfoThread != null) {
			userActivityInfoThread.stopThread();
		}
		return true;
	}

	public int getTimestamp(String startTime, String exitTime) {
		String[] startTimes = startTime.split(":");
		String[] exitTimes = exitTime.split(":");
		int startTimesToM = Integer.parseInt(startTimes[0]) * 24 * 60
				+ Integer.parseInt(startTimes[1]) * 60
				+ Integer.parseInt(startTimes[2]);
		int exitTimesToM = Integer.parseInt(exitTimes[0]) * 24 * 60
				+ Integer.parseInt(exitTimes[1]) * 60
				+ Integer.parseInt(exitTimes[2]);
		return (exitTimesToM - startTimesToM);/* / 60.0f; */
	}
	public long getCountDuration() {
		return COUNT_DURATION;
	}

	public int getCountTimes() {
		return COUNT_TIMES;
	}

}
