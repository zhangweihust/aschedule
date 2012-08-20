package com.archermind.schedule.Screens;


import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Image.SmartImageView;
import com.archermind.schedule.Model.UserInfoData;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.NetworkUtils;
import com.archermind.schedule.Utils.ServerInterface;

public class AccountSettingScreen extends Activity implements OnClickListener{
	
	private static final int LOGOUT_FAILED = 0;
	private static final int LOGOUT_SUCCESS = 1;
	
	private SmartImageView headImage;
	private String headImagePath;
	private LinearLayout bindTelephone;
	private Button logout;
	private Button goback;
	private EditText loginNick;
	private Handler handler;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.account_setting);
        
        headImage = (SmartImageView)findViewById(R.id.headImage);
        loginNick = (EditText)findViewById(R.id.login_nick);
        bindTelephone = (LinearLayout)findViewById(R.id.bindTelephone);
        logout = (Button)findViewById(R.id.logout);
        goback = (Button)findViewById(R.id.title_bar_setting_btn);
        
        headImage.setOnClickListener(this);
        bindTelephone.setOnClickListener(this);
        logout.setOnClickListener(this);
        goback.setOnClickListener(this);
        
        
        loginNick.setText(ServiceManager.getSPUserInfo(UserInfoData.NICK));
		headImage.setImageUrl(getUriFormWeb(),
                R.drawable.friend_item_img, R.drawable.friend_item_img);
        
        handler = new Handler()
        {
        	public void handleMessage(Message msg) 
        	{
        		switch(msg.what)
        		{
        		case LOGOUT_FAILED:
        			ServiceManager.ToastShow("注销失败");
        			break;
        		case LOGOUT_SUCCESS:
        			ServiceManager.ToastShow("注销成功");
        			onBackPressed();
        			break;
        		}
        	};
        };
    }

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.headImage:
			ShowPickDialog();
			break;
		case R.id.bindTelephone:
			Intent it = new Intent(AccountSettingScreen.this,TelephoneBindScreen.class);
			startActivity(it); 
			break;
		case R.id.logout:
			new Thread()
			{
				public void run() 
				{
					if (ServiceManager.getServerInterface().logout(String.valueOf(ServiceManager.getUserId())).equals("0"))
					{
						ServiceManager.setCookie("");
						handler.sendEmptyMessage(LOGOUT_SUCCESS);
					}
					else
					{
						handler.sendEmptyMessage(LOGOUT_FAILED);
					}
				};
			}.start();
			break;
		}
	}  
	
	
	private String getFilepathFromUri(Uri uri) {
		ContentResolver mContentResolver = getContentResolver();
		Cursor cursor = mContentResolver.query(uri, null,   
                null, null, null);   
		cursor.moveToFirst();   
		String filepath = cursor.getString(1);
		cursor.close();
		return filepath;
	}
	
	/**
	 * 选择提示对话框
	 */
	private void ShowPickDialog() {
		new AlertDialog.Builder(this)
				.setTitle("设置头像...")
				.setNegativeButton("相册", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent intent = new Intent(Intent.ACTION_PICK, null);
						intent.setDataAndType(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								"image/*");
						startActivityForResult(intent, 1);

					}
				})
				.setPositiveButton("拍照", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						Intent intent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri
								.fromFile(new File(Environment
										.getExternalStorageDirectory(),
										"headImage.jpg")));
						startActivityForResult(intent, 2);
					}
				}).show();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(R.anim.left_in,R.anim.left_out);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Uri uri = null;
		switch (requestCode) {
		case 1:
			if(data != null){
				uri = data.getData();
				startPhotoZoom(uri);
				headImagePath = getFilepathFromUri(uri);
			}
			break;
		case 2:
			File temp = new File(Environment.getExternalStorageDirectory()
					+ "/headImage.jpg");
			uri = Uri.fromFile(temp);
			startPhotoZoom(uri);
			headImagePath = uri.getPath();
			break;
		case 3:
			if(data != null){
				setPicToView(data);
			}
			break;
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	 private String getUriFormWeb(){
			if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {
				
				String jsonString = ServiceManager.getServerInterface().findUserInfobyUserId(String.valueOf(ServiceManager.getUserId()));
				if(jsonString != null && !"".equals(jsonString)){
					if(jsonString.indexOf("photo_url") >= 0){//防止返回错误码
						try {
							JSONArray jsonArray = new JSONArray(jsonString);
							ScheduleApplication.LogD(FriendsDyamicScreen.class, jsonString
									+ jsonArray.length());
							JSONObject jsonObject = (JSONObject) jsonArray.opt(0);
							return jsonObject.getString("photo_url");																 							

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			}
			return null;
		}

	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");

		intent.putExtra("crop", "true");

		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);

		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, 3);
	}
	
	private void setPicToView(Intent picdata) {
		Bundle extras = picdata.getExtras();
		if (extras != null) {
			ServerInterface serverInterface = ServiceManager.getServerInterface();
			serverInterface.InitAmtCloud(this);
			String filename = headImagePath.substring(headImagePath.lastIndexOf("/") + 1, headImagePath.lastIndexOf("."));
			String expandname = headImagePath.substring(headImagePath.lastIndexOf(".") + 1, headImagePath.length());
			System.out.println("***********  expandname = "+ expandname);
			if(0 == serverInterface.uploadPhoto(this, String.valueOf(ServiceManager.getUserId()), headImagePath, filename, expandname)){
				Bitmap photo = extras.getParcelable("data");
				Drawable drawable = new BitmapDrawable(photo);			
				headImage.setImageDrawable(drawable);
				Toast.makeText(this, "上传图片成功！", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(this, "上传图片失败！", Toast.LENGTH_LONG).show();
			}
		}
	}
}
