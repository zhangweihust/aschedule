package com.archermind.schedule.Task;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Services.UserInfoService;
import com.archermind.schedule.Utils.DeviceInfo;
import com.archermind.schedule.Utils.DeviceInfo.InfoName;
import com.archermind.schedule.Utils.NetworkUtils;

public class DeviceInfoThread extends Thread {
	private int times;
	private UserInfoService countService;
	private boolean stop = false;
	private final DatabaseManager db;
	private int stand = 10;

	@Override
	public void run() {
		while (!stop && times > 0) {
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				int date = Integer.parseInt(format.format(System.currentTimeMillis()));
				if (uploadCountToServer()) {
					db.updateCountUserInfoTask(date, 1);
					break;
				} else {
					db.updateCountUserInfoTask(date, 0);
				}
				sleep(countService.getCountDuration());
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				times--;
			}
		}
	}

	public DeviceInfoThread(int times, UserInfoService countService) {
		this.countService = countService;
		this.times = countService.getCountTimes() - times;
		this.db = ServiceManager.getDbManager();
	}

	private boolean uploadCountToServer() {
		if (NetworkUtils.getNetworkState(ScheduleApplication.getContext()) != NetworkUtils.NETWORN_NONE) {
			try {
				HttpEntityEnclosingRequestBase httpRequest = new HttpPost("");
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair(InfoName.IMEI.toString(), DeviceInfo.getDeviceIMEI()));
				params.add(new BasicNameValuePair(InfoName.CPU_MAX_FREQUENCY.toString(), DeviceInfo.getDeviceCpuMaxFrequency()));
				params.add(new BasicNameValuePair(InfoName.CPU_MODEL.toString(), DeviceInfo.getDeviceCpuModel()));
				params.add(new BasicNameValuePair(InfoName.MEMORY_TOTAL.toString(), DeviceInfo.getDeviceMemoryTotal()));
				params.add(new BasicNameValuePair(InfoName.PHONE_KTV_VERSION.toString(), DeviceInfo.getMyVersionCode()));
				params.add(new BasicNameValuePair(InfoName.PHONE_MODEL.toString(), DeviceInfo.getDeviceModel()));
				params.add(new BasicNameValuePair(InfoName.SCREEN_RESOLUTION.toString(), DeviceInfo.getDeviceScreenResolution()));
				params.add(new BasicNameValuePair(InfoName.SCREEN_DENSITYDPI.toString(), DeviceInfo.getDeviceScreenDensitydpi()));
				params.add(new BasicNameValuePair(InfoName.SYSTEM_VERSION.toString(), DeviceInfo.getDeviceSystemVersion()));
				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public void stopThread() {
		stop = true;
	}
}
