package com.archermind.schedule.Dialog;

import java.util.Calendar;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.archermind.schedule.R;
import com.archermind.schedule.Utils.DateTimeUtils;

public class WeatherDialog implements OnClickListener {
	private static final String TAG = "WeatherDialog";
	private Dialog weatherDialog;
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

	
	
	public interface OnCancelButtonClickListener {
		void onCancelButtonClick(WeatherDialog mweatherDialog);
	}

	private OnCancelButtonClickListener mOnCanceButtonClickListener;

	public void setOnCancelButtonClickListener(OnCancelButtonClickListener l) {
		mOnCanceButtonClickListener = l;
	}
	public WeatherDialog(Context context, int screenWidth, int screenHeight,
			Map<String, String> cityInfoMap, Map<String, Integer> weathermap,
			Map<String, String> itemsmap) {

		weatherDialog = new Dialog(context, R.style.WeatherDialog);
		weatherDialog.setContentView(R.layout.weather);
		// weatherDialog.setCanceledOnTouchOutside(true);

		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		init();
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
		cityInfoTv.setText(cityInfoMap.get("province") + "省"
				+ cityInfoMap.get("city") + "市");

		// 设置今天当前温度
		Log.i(TAG, "-------st1-" + itemsmap.get("st1"));
		if(itemsmap.get("st1")!=null){
			todayCurTemp.setText(itemsmap.get("st1") + "℃");
		}else{
			todayCurTemp.setText(itemsmap.get("st1"));			
		}
		
		todayTemp.setText(itemsmap.get("temp1"));
		todayDate.setText(date[0] + week[0]);
		mWeather1 = itemsmap.get("weather1");
		if (mWeather1 == null) {
			mWeather1 = "  ";
		}
		todayWeather.setText(mWeather1);
		Log.i(TAG, "-------weather1-" + mWeather1);
		Log.i(TAG, "-------weather1-" + weathermap.get(mWeather1));
		if (weathermap.get(mWeather1) != null) {
			todayImg.setImageResource(weathermap.get(mWeather1));
		}

		// 设置一天后
		if (itemsmap.get("temp2") != null) {
			mTemp2 = itemsmap.get("temp2").split("~");
			oneDAfterMinTemp.setText(mTemp2[0] + "~");
			oneDAfterMaxTemp.setText(mTemp2[1]);
		}
		mTemp2 = itemsmap.get("temp2").split("~");
		oneDAfterMinTemp.setText(mTemp2[0] + "~");
		oneDAfterMaxTemp.setText(mTemp2[1]);

		mWeather2 = itemsmap.get("weather2");
		if (mWeather2 == null) {
			mWeather2 = "  ";
		}
		oneDAfterWeather.setText(mWeather2);
		Log.i(TAG, "------weather2-" + mWeather2);
		Log.i(TAG, "-------weather2-" + weathermap.get(mWeather2));
		if (weathermap.get(mWeather2) != null) {
			oneDAfterImg.setImageResource(weathermap.get(mWeather2));
		}

		// 设置两天后
		
		if (itemsmap.get("temp3") != null) {
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
		if (mWeather3 == null) {
			mWeather3 = " ";
		   }
		twoDAfterWeather.setText(mWeather3);
		Log.i(TAG, "-------weather3---" + mWeather3);
		Log.i(TAG, "-------weather3-" + weathermap.get(mWeather3));
		if (weathermap.get(mWeather3) != null) {
			twoDAfterImg.setImageResource(weathermap.get(mWeather3));
		}

		// 设置三天后
		if (itemsmap.get("temp4") != null) {
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
		threeDAfterWeather.setText(mWeather4);
		Log.i(TAG, "----weather4---" + mWeather4);
		Log.i(TAG, "-------weather4-" + weathermap.get(mWeather4));
		if (weathermap.get(mWeather4) != null) {
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
		window = weatherDialog.getWindow(); // 得到对话框
		window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画
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
		weatherDialog.dismiss();
		mOnCanceButtonClickListener.onCancelButtonClick(this);
	}

}