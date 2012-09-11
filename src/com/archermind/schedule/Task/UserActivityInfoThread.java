package com.archermind.schedule.Task;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Services.UserInfoService;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.DeviceInfo;
import com.archermind.schedule.Utils.NetworkUtils;
import com.archermind.schedule.Utils.SharedPreferenceUtil;

public class UserActivityInfoThread extends Thread {
	private int times;
	private UserInfoService countService;
	private boolean stop = false;

	@Override
	public void run() {
		while (!stop && times > 0) {
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				int date = Integer.parseInt(format.format(System
						.currentTimeMillis()));
				if (uploadCountToServer()) {
					SharedPreferenceUtil.setValue(
							Constant.SendUserInfo.SEND_USER_ACTIVITY_INFO_DATE,
							String.valueOf(date));
					break;
				}
				times--;
				sleep(countService.getCountDuration());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public UserActivityInfoThread(int times, UserInfoService countService) {
		this.countService = countService;
		this.times = countService.getCountTimes() - times;
	}

	private boolean uploadCountToServer() {
		if (NetworkUtils.getNetworkState(ScheduleApplication.getContext()) != NetworkUtils.NETWORN_NONE) {
			try {
				HttpEntityEnclosingRequestBase httpRequest = new HttpPost(
						Constant.UrlInfo.USER_ACTIVITY_INFO_URL);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				int times = Integer.parseInt(SharedPreferenceUtil.getValue(
						Constant.SendUserInfo.SEND_USER_ACTIVITY_INFO_TIMES,
						"1"));
				int timesTamp = Integer
						.parseInt(SharedPreferenceUtil
								.getValue(
										Constant.SendUserInfo.SEND_USER_ACTIVITY_INFO_TIMESTAMP,
										"0"));
				String value = "{" + DeviceInfo.getDeviceIMEI() + "," + times
						+ "," + timesTamp + "}";
				// params.add(new
				// BasicNameValuePair(Constant.MediaWithServerConstant.request_parameter,
				// value));
				httpRequest.setEntity(new UrlEncodedFormEntity(params,
						HTTP.UTF_8));
				HttpResponse httpResponse = new DefaultHttpClient()
						.execute(httpRequest);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					return true;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public void stopThread() {
		stop = true;
	}
}
