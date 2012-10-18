package com.archermind.schedule.Task;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Service;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;

import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Services.ExceptionService;

public class SendCrashReportsTask extends AsyncTask<Void, Void, Integer> {
	/** 错误报告文件的扩展名 */
	private static final String CRASH_REPORTER_EXTENSION = ".log";
	TelephonyManager tm = (TelephonyManager) ScheduleApplication.getContext()
			.getSystemService(Context.TELEPHONY_SERVICE);
	private Service mService;

	public SendCrashReportsTask() {

	}

	public SendCrashReportsTask(Service service) {
		mService = service;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		sendCrashReportsToServer();
		return null;
	}

	/**
	 * 把错误报告发送给服务器,包含新产生的和以前没发送的.
	 * 
	 * @param ctx
	 */
	private void sendCrashReportsToServer() {
		try {
		String[] crFiles = getCrashReportFiles();
		if (crFiles != null && crFiles.length > 0) {
			TreeSet<String> sortedFiles = new TreeSet<String>();
			sortedFiles.addAll(Arrays.asList(crFiles));

			for (String fileName : sortedFiles) {
				File cr = new File(ExceptionService.crashPath, fileName);
				if (postReport(cr)) {
					cr.delete();// 删除已发送的报告
					ScheduleApplication.LogD(SendCrashReportsTask.class,
							"Send OK :" + fileName);
				} else {
					ScheduleApplication.LogD(SendCrashReportsTask.class,
							"Send FAILURE :" + fileName);
				}

			}
		} else {
			ScheduleApplication.LogD(SendCrashReportsTask.class, "本地没有LOG文件");
		}
		if (mService != null)
			mService.stopSelf();
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(), e);
		}
	}

	private boolean postReport(File file) {
		// TODO 使用HTTP Post 发送错误报告到服务器
		// 这里不再详述,开发者可以根据OPhoneSDN上的其他网络操作
		// 教程来提交错误报告
		HttpEntityEnclosingRequestBase httpRequest = new HttpPost(
				ExceptionService.CRASH_UPLOAD_SERVER_URL);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		StringBuffer sb = new StringBuffer();
		try {
			readToBuffer(sb, new FileInputStream(file));
		} catch (FileNotFoundException e1) {
			ScheduleApplication.LogD(SendCrashReportsTask.class,
					"FileNotFoundException");
			return false;
		} catch (IOException e1) {
			ScheduleApplication.LogD(SendCrashReportsTask.class, "IOException");
			return false;
		}
		params.add(new BasicNameValuePair("crashReport", sb.toString()));
		params.add(new BasicNameValuePair("imei", tm.getDeviceId()));
		try {
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);
			ScheduleApplication.LogD(SendCrashReportsTask.class, "StatusCode :"
					+ httpResponse.getStatusLine().getStatusCode());
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				return true;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return false;

	}

	public void readToBuffer(StringBuffer buffer, InputStream is)
			throws IOException {
		String line; // 用来保存每行读取的内容
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		line = reader.readLine(); // 读取第一行
		while (line != null) { // 如果 line 为空说明读完了
			buffer.append(line); // 将读到的内容添加到 buffer 中
			buffer.append("/n"); // 添加换行符
			line = reader.readLine(); // 读取下一行
		}
	}

	/**
	 * 获取错误报告文件名
	 * 
	 * @param ctx
	 * @return
	 */
	private String[] getCrashReportFiles() {
		File filesDir = new File(ExceptionService.crashPath);
		if (!filesDir.exists()) {
			filesDir.mkdirs();
		}
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(CRASH_REPORTER_EXTENSION);
			}
		};
		return filesDir.list(filter);
	}

}
