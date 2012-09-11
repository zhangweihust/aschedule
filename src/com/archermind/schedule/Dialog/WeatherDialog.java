package com.archermind.schedule.Dialog;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.archermind.schedule.R;
import com.archermind.schedule.Screens.WeatherScreen;
import com.archermind.schedule.Utils.DateTimeUtils;

public class WeatherDialog implements OnClickListener {
	private static final String TAG = "WeatherDialog";
	private Dialog weatherDialog;
	private TextView cityInfoTv;
	private TextView todayCurTemp;
	private TextView todayDate;
	private TextView todayWeek;
	private ImageView todayImg, oneDAfterImg, twoDAfterImg, threeDAfterImg;
	private TextView todayTemp;
	private TextView oneDAfterMinTemp, oneDAfterMaxTemp;
	private LinearLayout weatherInfo;

	private TextView twoDAfterMinTemp, twoDAfterMaxTemp;
	private TextView threeDAfterMinTemp, threeDAfterMaxTemp;
	private TextView todayWeather, oneDAfterWeather, twoDAfterWeather,
			threeDAfterWeather;
	private TextView oneDAfterDate, twoDAfterDate, threeDAfterDate;
	private TextView oneDAfterWeek, twoDAfterWeek, threeDAfterWeek;

	private String[] date = new String[4];
	private String[] week = new String[4];

	private Window window = null;
	private int screenWidth, screenHeight;
	private Button cancelBtn;

	private String mWeather1 = "", mWeather2 = "", mWeather3 = "",
			mWeather4 = "";
	private String[] mTemp1, mTemp2, mTemp3, mTemp4;
	private String maxtemp1, mintemp1;
	private String maxtemp2, mintemp2;
	private String maxtemp3, mintemp3;
	private String maxtemp4, mintemp4;

	private Context context;
	
	private CitySettingDialog citySettingDialog;
	
	private String province;
	
	private String city;
	
	public interface OnCancelButtonClickListener {
		void onCancelButtonClick(WeatherDialog mweatherDialog);
	}

	public WeatherDialog(final Context context, int screenWidth, int screenHeight,
			Map<String, String> cityInfoMap, Map<String, Integer> weathermap,
			Map<String, String> itemsmap) {

        this.context = context;
		weatherDialog = new Dialog(context, R.style.WeatherDialog);
		weatherDialog.setContentView(R.layout.weather);
		citySettingDialog = new CitySettingDialog(context);
		citySettingDialog.getDialog().setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				WeatherScreen WeatherScreen = (WeatherScreen)context;
				SharedPreferences sp = context.getSharedPreferences("com.archermind.schedule_preferences",Context.MODE_WORLD_WRITEABLE);
				String mProvince = sp.getString("province", "北京");
				String mCity = sp.getString("city", "北京");
				if(mProvince.equals(province) && mCity.equals(city)){
					return;
				}
				WeatherScreen.getWeatherData(mProvince,mCity);
				displayWeather(WeatherScreen.getCityInfoMap(), WeatherScreen.getWeatherMap(), WeatherScreen.getItemsmap());
				province = mProvince;
				city = mCity;
			}
		});
		weatherDialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				
		        if (WeatherScreen.isChangeCity) {
		            
		            Intent it = new Intent("com.archermind.action.PLACECHANGED");
		            context.sendBroadcast(it);
		        }
		        
			    ((Activity)context).finish();

			}
		});
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		init();
		province = cityInfoMap.get("province");
		city = cityInfoMap.get("city");
		displayWeather(cityInfoMap, weathermap, itemsmap);

	}

	// 初始化
	public void init() {

		cityInfoTv = (TextView) weatherDialog.findViewById(R.id.city_info);	
		todayImg = (ImageView) weatherDialog.findViewById(R.id.today_image);
		todayCurTemp = (TextView) weatherDialog
				.findViewById(R.id.today_temperature);
		todayTemp = (TextView) weatherDialog
				.findViewById(R.id.today_temperature_range);
		todayWeather = (TextView) weatherDialog
				.findViewById(R.id.today_weather_condition);
		todayDate = (TextView) weatherDialog.findViewById(R.id.today_date);
		todayWeek = (TextView) weatherDialog.findViewById(R.id.today_week);
		
		weatherInfo = (LinearLayout) weatherDialog.findViewById(R.id.today_weather_info);

		oneDAfterDate = (TextView) weatherDialog
				.findViewById(R.id.one_day_after_date);
		oneDAfterWeek = (TextView) weatherDialog
				.findViewById(R.id.one_day_after_week);
		oneDAfterImg = (ImageView) weatherDialog
				.findViewById(R.id.one_day_after_temperature_img);
		oneDAfterMinTemp = (TextView) weatherDialog
				.findViewById(R.id.one_day_after_min_temperature);
		oneDAfterMaxTemp = (TextView) weatherDialog
				.findViewById(R.id.one_day_after_max_temperature);
		oneDAfterWeather = (TextView) weatherDialog
				.findViewById(R.id.one_day_after_condition);

		twoDAfterDate = (TextView) weatherDialog
				.findViewById(R.id.two_day_after_date);
		twoDAfterWeek = (TextView) weatherDialog
				.findViewById(R.id.two_day_after_week);
		twoDAfterImg = (ImageView) weatherDialog
				.findViewById(R.id.two_day_after_temperature_img);
		twoDAfterMinTemp = (TextView) weatherDialog
				.findViewById(R.id.two_day_after_min_temperature);
		twoDAfterMaxTemp = (TextView) weatherDialog
				.findViewById(R.id.two_day_after_max_temperature);
		twoDAfterWeather = (TextView) weatherDialog
				.findViewById(R.id.two_day_after_condition);

		threeDAfterDate = (TextView) weatherDialog
				.findViewById(R.id.three_day_after_date);
		threeDAfterWeek = (TextView) weatherDialog
				.findViewById(R.id.three_day_after_week);
		threeDAfterImg = (ImageView) weatherDialog
				.findViewById(R.id.three_day_after_temperature_img);
		threeDAfterMinTemp = (TextView) weatherDialog
				.findViewById(R.id.three_day_after_min_temperature);
		threeDAfterMaxTemp = (TextView) weatherDialog
				.findViewById(R.id.three_day_after_max_temperature);
		threeDAfterWeather = (TextView) weatherDialog
				.findViewById(R.id.three_day_after_condition);

		cancelBtn = (Button) weatherDialog
				.findViewById(R.id.weather_dialog_cancel);
		cancelBtn.setOnClickListener(this);
		weatherInfo.setOnClickListener(this);

	}

	public void displayWeather(Map<String, String> cityInfoMap,
			Map<String, Integer> weathermap, Map<String, String> itemsmap) {
		// 获取四天的日期
		Calendar mCalendar = Calendar.getInstance();

		for (int i = 0; i < date.length; i++) {
			date[i] = DateTimeUtils.time2String("M月d日",
					mCalendar.getTimeInMillis());
			week[i] = DateTimeUtils.time2String("E",
					mCalendar.getTimeInMillis());
			mCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}

		// 设置城市信息
		if("北京".equals(cityInfoMap.get("city"))||"上海".equals(cityInfoMap.get("city"))||"重庆".equals(cityInfoMap.get("city"))||"天津".equals(cityInfoMap.get("city"))){
			cityInfoTv.setText(cityInfoMap.get("city") + "市");
			
		}else{
			cityInfoTv.setText(cityInfoMap.get("province") + "省"
					+ cityInfoMap.get("city") + "市");
		}
		
		// 设置今天当前温度
		Log.i(TAG, "-------st1-" + itemsmap.get("st1"));
		if(itemsmap.get("st1")!=null){
			todayCurTemp.setText(itemsmap.get("st1") + "℃");
		}else{
			todayCurTemp.setText(itemsmap.get("st1"));			
		}
		
		todayTemp.setText(itemsmap.get("temp1"));
		todayDate.setText(date[0]);
		todayWeek.setText(week[0]);
		
		mWeather1 = itemsmap.get("weather1");
		mWeather1 = getweatherMap(mWeather1, weathermap);
		if (mWeather1 == null) {
			mWeather1 = "  ";
		}
		
		todayWeather.setText(mWeather1);
		Log.i(TAG, "-------weather1-" + mWeather1);
		Log.i(TAG, "-------weather1-" + weathermap.get(mWeather1));
		if (itemsmap.get("weather1") !=null &&!itemsmap.get("weather1").equals("")) {
			todayImg.setImageResource(weathermap.get(mWeather1));
		}

		// 设置一天后
		if (itemsmap.get("temp2") !=null &&!itemsmap.get("temp2").equals("")) {
			mTemp2 = itemsmap.get("temp2").split("~");
			maxtemp2=mTemp2[0]+"~";
			mintemp2=mTemp2[1];
		}else{
			maxtemp2=" ";
			mintemp2=" ";
		}
	
		oneDAfterMinTemp.setText(maxtemp2);
		oneDAfterMaxTemp.setText(mintemp2);

		mWeather2 = itemsmap.get("weather2");
		mWeather2 = getweatherMap(mWeather2, weathermap);
		if (mWeather2 == null) {
			mWeather2 = "  ";
		}
		oneDAfterWeather.setText(mWeather2);
		Log.i(TAG, "------weather2-" + mWeather2);
		Log.i(TAG, "-------weather2-" + weathermap.get(mWeather2));
		if (itemsmap.get("weather2") !=null &&!itemsmap.get("weather2").equals("")) {
			oneDAfterImg.setImageResource(weathermap.get(mWeather2));
		}

		// 设置两天后
		
		if (itemsmap.get("temp3") !=null &&!itemsmap.get("temp3").equals("")) {
			mTemp3 = itemsmap.get("temp3").split("~");
			maxtemp3=mTemp3[0]+"~";
			mintemp3=mTemp3[1];
		}else{
			maxtemp3=" ";
			mintemp3=" ";
		}		
		twoDAfterMinTemp.setText(maxtemp3);
		twoDAfterMaxTemp.setText(mintemp3);
		mWeather3 = itemsmap.get("weather3");
		mWeather3 = getweatherMap(mWeather3, weathermap);
		if (mWeather3 == null) {
			mWeather3 = " ";
		   }
		twoDAfterWeather.setText(mWeather3);
		Log.i(TAG, "-------weather3---" + mWeather3);
		Log.i(TAG, "-------weather3-" + weathermap.get(mWeather3));
		if (itemsmap.get("weather3") !=null &&!itemsmap.get("weather3").equals("")) {
			twoDAfterImg.setImageResource(weathermap.get(mWeather3));
		}

		// 设置三天后
		if (itemsmap.get("temp4") !=null &&!itemsmap.get("temp4").equals("")) {
			mTemp4 = itemsmap.get("temp4").split("~");
			maxtemp4=mTemp4[0]+"~";
			mintemp4=mTemp4[1];
		}else{
			maxtemp4="  ";
			mintemp4="  ";
		}
		
		threeDAfterMinTemp.setText(maxtemp4 );
		threeDAfterMaxTemp.setText(mintemp4);
		mWeather4 = itemsmap.get("weather4");
		mWeather4 = getweatherMap(mWeather4, weathermap);
		threeDAfterWeather.setText(mWeather4);
		Log.i(TAG, "----weather4---" + mWeather4);
		Log.i(TAG, "-------weather4-" + weathermap.get(mWeather4));
		if (itemsmap.get("weather4") !=null &&!itemsmap.get("weather4").equals("")) {
			threeDAfterImg.setImageResource(weathermap.get(mWeather4));
		}

		// 设置后三天的日期
		oneDAfterDate.setText(date[1]);
		twoDAfterDate.setText(date[2]);
		threeDAfterDate.setText(date[3]);

		// 设置星期
		oneDAfterWeek.setText(week[1]);
		twoDAfterWeek.setText(week[2]);
		threeDAfterWeek.setText(week[3]);
	}

	public void dismiss() {
		weatherDialog.dismiss();
	}

	public void show() {
		Log.i("free", "show");
		window = weatherDialog.getWindow(); // 得到对话框
//		window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画
		WindowManager.LayoutParams wl = window.getAttributes();
		// //根据x，y坐标设置窗口需要显示的位置
		// wl.x = x; //x小于0左移，大于0右移
		// wl.y = y; //y小于0上移，大于0下移
		// wl.alpha = 0.6f; //设置透明度
		wl.height = screenHeight * 7 / 8;
		wl.width = screenWidth * 7 / 8;
		wl.gravity = Gravity.CENTER; // 设置重力
		window.setAttributes(wl);
		weatherDialog.show();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.today_weather_info:
			citySettingDialog.show();
			break;
		case R.id.weather_dialog_cancel:
			weatherDialog.dismiss();
			break;
		}
	}

	public String getweatherMap(String key, Map<String, Integer> weathermap) {
		String retkey;
		Iterator<Entry<String, Integer>> iterator = weathermap.entrySet()
				.iterator();
		Entry<String, Integer> entry = null;
		
		while (iterator.hasNext()) {
			entry = iterator.next();
			Log.i("free", "entry.getKey()"+entry.getKey());
			Log.i("free", "key"+key);
			if (key.contains(entry.getKey())) {
				retkey = entry.getKey();
				return retkey;
			}		
		}
		
		return null;
	}
	
}
