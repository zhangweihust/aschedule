package com.archermind.schedule.Task;

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
import com.archermind.schedule.Services.UserInfoService;
import com.archermind.schedule.Utils.DeviceInfo;
import com.archermind.schedule.Utils.DeviceInfo.InfoName;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.NetworkUtils;


import android.content.Context;
import android.content.SharedPreferences;

public class DeviceInfoThread extends Thread {
	private boolean stop = false;
	public static final String PREF = "DeviceInfo";
//	public static final String PREF_KEY_CONTENT = "content";
//	public static final String PREF_KEY_VERSION = "version";
//	public static final String PREF_KEY_OK_TIME = "ok_time";
	private Context context;

	@Override
	public void run() {
		int times = UserInfoService.COUNT_TIMES;
		while (!stop && times > 0) {
			System.out.println("===send deviceInfo===" + times);
			try {
				if (uploadCountToServer()) {
					SharedPreferences sp = context.getSharedPreferences(PREF, 0);
					SharedPreferences.Editor editor = sp.edit();
			    	editor.putBoolean(Constant.SendUserInfo.SEND_USER_DEVICE_INFO, true);
			    	editor.commit();
					break;
				} 
				sleep(UserInfoService.COUNT_DURATION);
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				times--;
			}
		}
	}

	public DeviceInfoThread() {
		context = ScheduleApplication.getContext();
	}

	private boolean uploadCountToServer() {
		System.out.println("+++has network+++");
		if(NetworkUtils.getNetworkState(context) != NetworkUtils.NETWORN_NONE){
			System.out.println("+++has network+++");
			try {
				HttpEntityEnclosingRequestBase httpRequest = new HttpPost(Constant.UrlInfo.USER_DEVICE_INFO_URL);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair(InfoName.IMEI.toString(), DeviceInfo.getDeviceIMEI()));
				params.add(new BasicNameValuePair(InfoName.CPU_MAX_FREQUENCY.toString(), DeviceInfo.getDeviceCpuMaxFrequency()));
				params.add(new BasicNameValuePair(InfoName.CPU_MODEL.toString(), DeviceInfo.getDeviceCpuModel()));
				params.add(new BasicNameValuePair(InfoName.MEMORY_TOTAL.toString(), DeviceInfo.getDeviceMemoryTotal()));
				params.add(new BasicNameValuePair(InfoName.SOFT_VERSION.toString(), DeviceInfo.getMyVersionCode()));
				params.add(new BasicNameValuePair(InfoName.PHONE_MODEL.toString(), DeviceInfo.getDeviceModel()));
				params.add(new BasicNameValuePair(InfoName.SCREEN_RESOLUTION.toString(), DeviceInfo.getDeviceScreenResolution()));
				params.add(new BasicNameValuePair(InfoName.SCREEN_DENSITYDPI.toString(), DeviceInfo.getDeviceScreenDensitydpi()));
				params.add(new BasicNameValuePair(InfoName.SYSTEM_VERSION.toString(), DeviceInfo.getDeviceSystemVersion()));
				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				System.out.println("+++http request+++========");
				System.out.println(httpRequest.toString());
				HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
				System.out.println("+++return code+++" + httpResponse.getStatusLine().getStatusCode());
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					return true;
				}
			} catch (Exception e) {
				System.out.println("=== exception ===");
				e.printStackTrace();
			}
		}
		return false;
	}

	public void stopThread() {
		stop = true;
	}
}
