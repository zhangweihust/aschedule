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
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.database.Cursor;

import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Services.UserInfoService;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.DeviceInfo;
import com.archermind.schedule.Utils.DeviceInfo.InfoName;
import com.archermind.schedule.Utils.NetworkUtils;

public class UserActivityInfoThread extends Thread {
	private int times;
	private UserInfoService countService;
	private boolean stop = false;
	private final DatabaseManager db;

	@Override
	public void run() {
		while (!stop && times > 0) {
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				int date = Integer.parseInt(format.format(System.currentTimeMillis()));
				if (uploadCountToServer()) {
					db.updateUserActivityInfoTask(date, 1);
					break;
				} else {
					db.updateUserActivityInfoTask(date, 0);
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
		db = ServiceManager.getDbManager();
	}

	private boolean uploadCountToServer() {
		if (NetworkUtils.getNetworkState(ScheduleApplication.getContext()) != NetworkUtils.NETWORN_NONE) {
			Cursor c = db.queryUserActivityInfo();
			int times = -1;
			int timesTamp = -1;
			if(c.moveToNext()){
				times = c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_TIMES));
				timesTamp = c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_COUNT_USER_INFO_ACTIVITY_CUMULATIVE_TIME));
			}
//			System.out.println("times="+times+" timesTamp="+timesTamp);
			c.close();
			if(times == -1 && timesTamp == -1){
				return true;
			}
			try {
				HttpEntityEnclosingRequestBase httpRequest = new HttpPost("");
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				String value = "{"+DeviceInfo.getDeviceIMEI()+","+times+","+timesTamp+"}";
//				params.add(new BasicNameValuePair(Constant.MediaWithServerConstant.request_parameter, value));
				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
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
