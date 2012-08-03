package com.archermind.schedule.Screens;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Dialog.WeatherDialog;
import com.archermind.schedule.Model.ScheduleBean;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Utils.HttpUtils;
import com.archermind.schedule.Utils.NetworkUtils;

import android.content.ContentValues;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;
import android.widget.TextView;

public class WeatherScreen extends Screen {
	private static final String TAG = "WeatherScreen";
	private TextView cityInfoTv;
	private TextView todayCurTemp;
	private TextView todayDate;
	private ImageView todayImg, oneDAfterImg, twoDAfterImg, threeDAfterImg;
	private TextView todayTemp;
	private TextView oneDAfterMinTemp, oneDAfterMaxTemp;

	private TextView twoDAfterMinTemp, twoDAfterMaxTemp;
	private TextView threeDAfterMinTemp, threeDAfterMaxTemp;
	private TextView todayWeather, oneDAfterWeather, twoDAfterWeather,
			threeDAfterWeather;
	private TextView oneDAfterDate, twoDAfterDate, threeDAfterDate;
	private TextView oneDAfterWeek, twoDAfterWeek, threeDAfterWeek;
	private String city, province, cid, weather;
	private Map<String, String> cityInfoMap = new HashMap<String, String>();
	private Map<String, String> itemsmap = new HashMap<String, String>();
	Map<String, Integer> weatherMap = new HashMap<String, Integer>();
	private String[] date = new String[4];
	private String[] week = new String[4];

	private ImageView[] img = new ImageView[4];

	WeatherDialog mwWeatherDialog;

	// private String
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weather_main);
		// setContentView(R.layout.weather);
		// init();
		// 获取屏幕宽度
		Display display = getWindowManager().getDefaultDisplay();
		int screenWidth = display.getWidth();
		int screenHeight = display.getHeight();

		// 获取天气图片地址
		weatherMap = getWeathermap();

		// 获取四天的日期
		Calendar mCalendar = Calendar.getInstance();
		for (int i = 0; i < date.length; i++) {
			date[i] = DateTimeUtils.time2String("M月d日",
					mCalendar.getTimeInMillis());
			week[i] = DateTimeUtils.time2String("E",
					mCalendar.getTimeInMillis());
			mCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}

		// String cityInfo = "null";
		//
		// // 如果未设置城市，则默认显示北京天气
		// if (cityInfo == null) {
		//
		//
		// }
		cityInfoMap.put("province", "湖北");
		cityInfoMap.put("city", "武汉");
		// new Thread(new HttpUtils()).start();

		// 如果联网了，则从服务器获取数据
		//
		 if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE)
		 {
		
		 String strResult = HttpUtils
		 .doPost(cityInfoMap,
		 "http://player.archermind.com/ci/index.php/aschedule/getWeather");
		 Log.d(TAG, "-------strResult:" + strResult);

//		 [{"city":"武汉","province":"湖北","cid":"101200101","weather":"\"st1 \":\"33\",\"temp1\":\"33℃~27℃\",\"weather1\":\"多云\",\"temp2\": \"34℃~28℃\",\"weather2\":\"多云\",\"temp3\":\"35℃~28℃\",\"weather3 \":\"多云\",\"temp4\":\"34℃~24℃\",\"weather4\":\"多云\""}]
		 if (!strResult.equals("-1")) {
		 itemsmap = parseJson(strResult);
		 mwWeatherDialog = new WeatherDialog(this, screenWidth,
		 screenHeight, cityInfoMap, weatherMap, itemsmap);
		 mwWeatherDialog.show();
		 saveToDb(itemsmap);
		 }
		
		 } else {
		// 没有联网，则读取本地数据库
		Cursor c = ServiceManager.getDbManager().queryScheduleWeather(date[0]);
		// 如果不存在当天天气，则什么都不显示
		if (c.getCount() != 0) {

			if (c.moveToFirst()) {

				itemsmap.put("str1", c.getString(c
						.getColumnIndex(DatabaseHelper.COLUMN_WEATHER_TEMP)));
				itemsmap.put(
						"temp1",
						c.getString(c
								.getColumnIndex(DatabaseHelper.COLUMN_WEATHER_TEMP_RANGE)));
				itemsmap.put("weather1", c.getString(c
						.getColumnIndex(DatabaseHelper.COLUMN_WEATHER_WEATHER)));
				c.getString(c
						.getColumnIndex(DatabaseHelper.COLUMN_WEATHER_TEMP));
				c.close();
				
				Cursor cursor1 = ServiceManager.getDbManager()
						.queryScheduleWeather(date[1]);
				if (cursor1.moveToFirst()) {
					itemsmap.put(
							"temp2",
							cursor1.getString(cursor1
									.getColumnIndex(DatabaseHelper.COLUMN_WEATHER_TEMP_RANGE)));
					itemsmap.put(
							"weather2",
							cursor1.getString(cursor1
									.getColumnIndex(DatabaseHelper.COLUMN_WEATHER_WEATHER)));

				} else {
					itemsmap.put("temp2", "");
					itemsmap.put("weather2", "");

				}

				cursor1.close();

				Cursor cursor2 = ServiceManager.getDbManager()
						.queryScheduleWeather(date[2]);
				if(cursor2.moveToFirst()){
					
					itemsmap.put(
							"temp3",
							cursor2.getString(cursor2
									.getColumnIndex(DatabaseHelper.COLUMN_WEATHER_TEMP_RANGE)));
					itemsmap.put("weather3", cursor2.getString(cursor2
							.getColumnIndex(DatabaseHelper.COLUMN_WEATHER_WEATHER)));
					cursor2.close();
								
				}else {
					itemsmap.put("temp3", "");
					itemsmap.put("weather3", "");

				}

				Cursor cursor3 = ServiceManager.getDbManager()
						.queryScheduleWeather(date[3]);
				if(cursor3.moveToFirst()){
					itemsmap.put(
							"temp3",
							cursor3.getString(cursor3
									.getColumnIndex(DatabaseHelper.COLUMN_WEATHER_TEMP_RANGE)));
					itemsmap.put("weather3", cursor3.getString(cursor3
							.getColumnIndex(DatabaseHelper.COLUMN_WEATHER_WEATHER)));
					cursor3.close();
					
				}else {
					itemsmap.put("temp4", "");
					itemsmap.put("weather4", "");

				}
				
				Log.d(TAG, "-------" + itemsmap.get("str1"));
				Log.d(TAG, "-------" + itemsmap.get("temp1"));
				Log.d(TAG, "-------" + itemsmap.get("weather1"));
				Log.d(TAG, "-------" + itemsmap.get("temp2"));
				Log.d(TAG, "-------" + itemsmap.get("weather2"));
				Log.d(TAG, "-------" + itemsmap.get("temp3"));
				Log.d(TAG, "-------" + itemsmap.get("weather3"));
				Log.d(TAG, "-------" + itemsmap.get("temp4"));
				Log.d(TAG, "-------" + itemsmap.get("weather4"));

			}
			mwWeatherDialog = new WeatherDialog(this, screenWidth,
					screenHeight, cityInfoMap, weatherMap, itemsmap);
			mwWeatherDialog.show();

		}

		 }

	}

	public Map<String, Integer> getWeathermap() {
		Map<String, Integer> weathermap = new HashMap<String, Integer>();
		weathermap.put("晴", R.drawable.clear);
		weathermap.put("多云", R.drawable.cloudy);
		weathermap.put("阴", R.drawable.shade);
		weathermap.put("阵雨", R.drawable.shower);
		weathermap.put("雷阵雨", R.drawable.thundershowers);
		weathermap.put("雷阵雨伴有冰雹", R.drawable.thundershowers_hail);
		weathermap.put("雨夹雪", R.drawable.sleet);
		weathermap.put("小雨", R.drawable.light_rain);
		weathermap.put("中雨", R.drawable.moderate_rain);
		weathermap.put("大雨", R.drawable.heavy_rain);
		weathermap.put("暴雨", R.drawable.rainstorm);
		weathermap.put("大暴雨", R.drawable.downpour);
		weathermap.put("特大暴雨", R.drawable.heavy_rainfall);
		weathermap.put("阵雪", R.drawable.shower_snow);
		weathermap.put("小雪", R.drawable.slight_snow);
		weathermap.put("中雪", R.drawable.moderate_snow);
		weathermap.put("大雪", R.drawable.heavy_snow);
		weathermap.put("暴雪", R.drawable.blizzard);
		weathermap.put("雾", R.drawable.fog);
		weathermap.put("冻雨", R.drawable.freezing_rain);

		weathermap.put("小雨-中雨", R.drawable.moderate_rain);
		weathermap.put("中雨-大雨", R.drawable.heavy_rain);
		weathermap.put("大雨-暴雨", R.drawable.rainstorm);
		weathermap.put("暴雨-大暴雨", R.drawable.downpour);
		weathermap.put("大暴雨-特大暴雨", R.drawable.heavy_rainfall);
		weathermap.put("小雪-中雪", R.drawable.moderate_snow);
		weathermap.put("中雪-大雪", R.drawable.heavy_snow);
		weathermap.put("大雪-暴雪", R.drawable.blizzard);

		weathermap.put("沙城暴", R.drawable.sand_storm);
		weathermap.put("强沙尘暴", R.drawable.sand_storm);
		weathermap.put("浮尘", R.drawable.sand);
		weathermap.put("扬沙", R.drawable.sand);

		return weathermap;
	}

	// 从服务器获取数据
	public Map<String, String> parseJson(String strResult) {
		try {

			JSONArray jsonArray = new JSONArray(strResult);
			for (int i = 0; i < jsonArray.length(); i++) {

				JSONObject jsonObject = (JSONObject) jsonArray.opt(i);
				city = jsonObject.getString("city");
				Log.d(TAG, "----province=" + city);
				province = jsonObject.getString("province");
				Log.d(TAG, "----province=" + province);
				cid = jsonObject.getString("cid");
				Log.d(TAG, "----cid=" + cid);
				weather = jsonObject.getString("weather");
				Log.d(TAG, "----weather=" + weather);

			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// weather="st1":"34","temp1":"34℃~26℃","weather1":"多云",
		// "temp2":"33℃~26℃","weather2":"多云转阵雨",
		// "temp3":"31℃~26℃","weather3":"中雨转大雨",
		// "temp4":"33℃~27℃","weather4":"阵雨转多云"
		weather = weather.replace("\"", "");
		String[] parts = weather.split(",");
		String[] datas = null;
		String[] tag = { "st1", "temp1", "weather1", "temp2", "weather2",
				"temp3", "weather3", "temp4", "weather4" };
		Map<String, String> itemsmap = new HashMap<String, String>();
		for (String part : parts) {
			datas = part.split(":");
			if (datas != null) {

				for (int i = 0; i < tag.length; i++) {
					if (datas[0].equalsIgnoreCase(tag[i])) {
						itemsmap.put(tag[i], datas[1]);
						Log.d(TAG, "-----" + tag[i] + "------" + datas[1]);

					}
				}

			}

		}
		if (itemsmap.get("weather1").contains("转")) {
			String[] weather = itemsmap.get("weather1").split("转");
			itemsmap.put("weather1", weather[0]);
		}
		if (itemsmap.get("weather2").contains("转")) {
			String[] weather = itemsmap.get("weather2").split("转");
			itemsmap.put("weather2", weather[0]);
		}
		if (itemsmap.get("weather3").contains("转")) {
			String[] weather = itemsmap.get("weather3").split("转");
			itemsmap.put("weather3", weather[0]);
		}
		if (itemsmap.get("weather4").contains("转")) {
			String[] weather = itemsmap.get("weather4").split("转");
			itemsmap.put("weather4", weather[0]);
		}

		// for (int i = 0; i < 3; i++) {
		// if (itemsmap.get("weather" + i).contains("转")) {
		// String[] weather = itemsmap.get("weather" + i).split("转");
		// itemsmap.put("weather" + i, weather[0]);
		// }
		//
		// }

		return itemsmap;

	}

	public void saveToDb(Map<String, String> itemsmap) {

		// 删除之前的数据
		ServiceManager.getDbManager().deleteScheduleWeather();
		// 保存到数据库
		ContentValues cv1 = new ContentValues();
		cv1.put(DatabaseHelper.COLUMN_WEATHER_DATE, date[0]);
		cv1.put(DatabaseHelper.COLUMN_WEATHER_TEMP, itemsmap.get("st1"));
		cv1.put(DatabaseHelper.COLUMN_WEATHER_TEMP_RANGE, itemsmap.get("temp1"));
		cv1.put(DatabaseHelper.COLUMN_WEATHER_WEATHER, itemsmap.get("weather1"));
		ServiceManager.getDbManager().insertScheduleWeather(cv1);

		ContentValues cv2 = new ContentValues();
		cv2.put(DatabaseHelper.COLUMN_WEATHER_DATE, date[1]);
		cv2.put(DatabaseHelper.COLUMN_WEATHER_TEMP_RANGE, itemsmap.get("temp2"));
		cv2.put(DatabaseHelper.COLUMN_WEATHER_WEATHER, itemsmap.get("weather2"));
		ServiceManager.getDbManager().insertScheduleWeather(cv2);

		ContentValues cv3 = new ContentValues();
		cv3.put(DatabaseHelper.COLUMN_WEATHER_DATE, date[2]);
		cv3.put(DatabaseHelper.COLUMN_WEATHER_TEMP_RANGE, itemsmap.get("temp3"));
		cv3.put(DatabaseHelper.COLUMN_WEATHER_WEATHER, itemsmap.get("weather3"));
		ServiceManager.getDbManager().insertScheduleWeather(cv3);

		ContentValues cv4 = new ContentValues();
		cv4.put(DatabaseHelper.COLUMN_WEATHER_DATE, date[3]);
		cv4.put(DatabaseHelper.COLUMN_WEATHER_TEMP_RANGE, itemsmap.get("temp4"));
		cv4.put(DatabaseHelper.COLUMN_WEATHER_WEATHER, itemsmap.get("weather4"));
		ServiceManager.getDbManager().insertScheduleWeather(cv4);
	}
}
