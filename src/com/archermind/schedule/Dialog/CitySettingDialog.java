package com.archermind.schedule.Dialog;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Screens.EditScheduleScreen;
import com.archermind.schedule.Screens.ScheduleScreen;
import com.archermind.schedule.Screens.WeatherScreen;

import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CitySettingDialog implements OnClickListener {

	private Dialog dialog;

	private Button province_button;

	private Button city_button;

	private TextView province_text;

	private TextView city_text;

	// private Button city_setting_ok;
	// private Button city_setting_cancel;
	private Button city_setting_close;

	private Context context;

	// 定义相关的字符串存放信息
	private String provinceStr = "";

	private String cityStr = "";

	// xml文件中对应的省份，城市的数组
	private int[] arrays = {R.array.beijing, R.array.shanghai, R.array.tianjin,
			R.array.chongqing, R.array.anhui, R.array.fujian, R.array.gansu,
			R.array.guangdong, R.array.guangxi, R.array.guizhou,
			R.array.hainan, R.array.hebei, R.array.henan, R.array.heilongjiang,
			R.array.hubei, R.array.hunan, R.array.jilin, R.array.jiangsu,
			R.array.jiangxi, R.array.liaoning, R.array.neimenggu,
			R.array.ningxia, R.array.qinghai, R.array.shandong,
			R.array.shanxi_01, R.array.shanxi_02, R.array.sichuan,
			R.array.xizang, R.array.xinjiang, R.array.yunnan, R.array.zhejiang,};

	// 存放省份的数组
	private String[] provinces;

	String[] two_char_provinces;

	int pro_position = 0;

	// 定义一个数组存放arrays中对应的城市数组
	String[] cities;

	final int SELECT_PROVINCES = 0x111;

	final int SELECT_OPERATOR = 0x112;

	int cityarrayId = 0;

	int cityId = 0;

	boolean provinceClickOrNot = false;

	boolean cityClickOrNot = false;

	private SharedPreferences sp;

	public CitySettingDialog(Context context) {
		this.context = context;
		dialog = new Dialog(context, R.style.CustomDialog);
		dialog.setContentView(R.layout.city_setting_dialog);
		dialog.setCanceledOnTouchOutside(true);

		province_button = (Button) dialog.findViewById(R.id.province_button);
		city_button = (Button) dialog.findViewById(R.id.city_button);
		city_setting_close = (Button) dialog
				.findViewById(R.id.city_setting_dialog_close);
		// city_setting_ok = (Button)dialog.findViewById(R.id.city_setting_ok);
		// city_setting_cancel =
		// (Button)dialog.findViewById(R.id.city_setting_cancel);

		province_text = (TextView) dialog.findViewById(R.id.province_text);
		city_text = (TextView) dialog.findViewById(R.id.city_text);

		province_button.setOnClickListener(this);
		city_button.setOnClickListener(this);
		city_setting_close.setOnClickListener(this);
		// city_setting_ok.setOnClickListener(this);
		// city_setting_cancel.setOnClickListener(this);

		sp = context.getSharedPreferences(
				"com.archermind.schedule_preferences",
				Context.MODE_WORLD_WRITEABLE);
	}

	public Dialog getDialog() {
		return dialog;
	}

	public void show() {
		initPersonal();

		DisplayMetrics dm = ScheduleApplication.getContext().getResources()
				.getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		Window window = dialog.getWindow(); // 得到对话框
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.width = screenWidth * 7 / 8;
		wl.gravity = Gravity.CENTER; // 设置重力
		window.setAttributes(wl);
		dialog.show();
	}

	public void dismiss() {
		dialog.dismiss();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.province_button :
				showDialog();
				break;
			case R.id.city_button :
				showDialog(true);
				break;
			case R.id.city_setting_dialog_close :
				dismiss();

				Editor editor = sp.edit();
				editor.putString("province", province_text.getText().toString());
				editor.putString("city", city_text.getText().toString());
				editor.putInt("pro_position", pro_position);
				editor.commit();
				break;
			// case R.id.city_setting_ok:
			// dismiss();
			//
			// Editor editor = sp.edit();
			// editor.putString("province", provinceStr);
			// editor.putString("city", cityStr);
			// editor.putInt("pro_position", pro_position);
			// editor.commit();
			// break;
			// case R.id.city_setting_cancel:
			// dismiss();
			// break;
		}
	}

	private void initPersonal() {
		provinces = context.getResources().getStringArray(R.array.provinces);
		two_char_provinces = context.getResources().getStringArray(
				R.array.two_chars_provinces);
		pro_position = sp.getInt("pro_position", 0);
		provinceStr = sp.getString("province", "北京");
		if (provinceStr != null && !("").equals(provinceStr)) {
			province_text.setText(provinceStr);
		}
		cityStr = sp.getString("city", "北京");
		if (cityStr != null && !("").equals(cityStr)) {
			city_text.setText(cityStr);
		}
	}

	private void showDialog() {

		Builder builder = new android.app.AlertDialog.Builder(context);
		// 设置对话框的标题
		builder.setTitle("选择省份");
		// 0: 默认第一个单选按钮被选中
		builder.setSingleChoiceItems(R.array.provinces,
				getProvincesId(province_text.getText().toString()),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						provinceStr = context.getResources().getStringArray(
								R.array.provinces)[which];

						cityarrayId = which;

						provinceClickOrNot = true;
					}
				});
		// 确定按钮
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				if (provinceClickOrNot) {
					provinceStr = context.getResources().getStringArray(
							R.array.provinces)[cityarrayId];
					province_text.setText(provinceStr);
					cities = (String[]) context.getResources().getStringArray(
							arrays[cityarrayId]);
					cityStr = context.getResources().getStringArray(
							arrays[cityarrayId])[0];
					city_text.setText(cityStr);

					ScheduleApplication.LogD(getClass(), "citydialog"
							+ WeatherScreen.isChangeCity);
					WeatherScreen.isChangeCity = true;
					ScheduleApplication.LogD(getClass(), " after citydialog"
							+ WeatherScreen.isChangeCity);

				} else {

					pro_position = getProvincesId(province_text.getText()
							.toString());

					provinceStr = context.getResources().getStringArray(
							R.array.provinces)[pro_position];
					cities = (String[]) context.getResources().getStringArray(
							arrays[pro_position]);
					cityStr = context.getResources().getStringArray(
							arrays[pro_position])[0];
					province_text.setText(provinceStr);
					city_text.setText(cityStr);
				}
			}
		}).show();
	}

	private void showDialog(final boolean city) {

		pro_position = getProvincesId(province_text.getText().toString());
		final String[] array = (String[]) context.getResources()
				.getStringArray(arrays[pro_position]);

		Builder builder = new android.app.AlertDialog.Builder(context);
		// 设置对话框的标题

		// 0: 默认第一个单选按钮被选中
		if (city) {
			builder.setTitle(province_text.getText().toString());
			String[] pro_city = array;
			for (int i = 0; i < pro_city.length; i++) {
				if (cityStr.equals(pro_city[i])) {
					cityId = i;
				}
			}
			builder.setSingleChoiceItems(array, cityId,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							cityStr = array[which];
							cityClickOrNot = true;
						}
					});
		}
		// 确定按钮
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				if (city) {
					if (cityClickOrNot) {
						city_text.setText(cityStr);

						WeatherScreen.isChangeCity = true;

						// Intent it = new
						// Intent("com.archermind.action.PLACECHANGED");
						// context.sendBroadcast(it);
					} else {
						cityStr = context.getResources().getStringArray(
								arrays[getProvincesId(province_text.getText()
										.toString())])[cityId];
						city_text.setText(cityStr);
					}
				}
			}
		}).show();
	}

	private int getProvincesId(String string) {
		// Log.i("my", "String==========" + string);
		int pro_position = 0;
		for (int i = 0; i < provinces.length; i++) {
			if (string.equals(provinces[i])) {
				pro_position = i;
			}
		}
		return pro_position;
	}

}
