package com.archermind.schedule.Task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Services.UserInfoService;
import com.archermind.schedule.Utils.DeviceInfo;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Utils.NetworkUtils;

import android.content.Context;
import android.content.SharedPreferences;


public class UserActiveInfoThread extends Thread {

	private boolean stop = false;
	public static String PREF = "UserActive";
	public static String PREF_KEY_DURATION= "duration";
	public static String PREF_KEY_TIMES = "times";
	public static String PREF_KEY_OK_LASTDATE = "ok_last_date";
	public static String PREF_KEY_LAST_DATE = "last_date";
	private Context context;
	
	@Override
	public void run() {
		int times = UserInfoService.COUNT_TIMES;
		while (!stop && times > 0) {
			try {
				if (uploadCountToServer()) {
				    SharedPreferences spActive = context.getSharedPreferences(PREF, 0);
				    SharedPreferences.Editor editor = spActive.edit();
					long curDate = Long.parseLong(DateTimeUtils.time2String("yyyyMMdd", System.currentTimeMillis()));
			    	editor.putLong(UserActiveInfoThread.PREF_KEY_OK_LASTDATE, curDate);
				    editor.commit();
					break;
				} 
				times--;
				sleep(UserInfoService.COUNT_DURATION);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public UserActiveInfoThread() {
		context = ScheduleApplication.getContext();
	}

	private boolean uploadCountToServer() {
		if(NetworkUtils.getNetworkState(context) != NetworkUtils.NETWORN_NONE){
			System.out.println("===========network ok==============");
			SharedPreferences spActive = context.getSharedPreferences(PREF, 0);
			int times = spActive.getInt(PREF_KEY_TIMES, 0);
			long duration = spActive.getLong(PREF_KEY_DURATION, 0);
			try {
				
				HttpEntityEnclosingRequestBase httpRequest = new HttpPost(Constant.UrlInfo.USER_ACTIVITY_INFO_URL);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				String value = "{"+DeviceInfo.getDeviceIMEI()+","+times+","+duration+"}";
				
				System.out.println("=====" + value);
				params.add(new BasicNameValuePair("dbstr", value));
				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
				System.out.println("===========response==============" + httpResponse.getStatusLine().getStatusCode());
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
