package com.android.schedule.Screens;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amtcloud.mobile.android.business.AmtAlbumObj;
import com.amtcloud.mobile.android.business.AmtApplication;
import com.amtcloud.mobile.android.business.AmtUserObj;
import com.amtcloud.mobile.android.business.MessageTypes;
import com.android.schedule.ScheduleApplication;
import com.android.schedule.Dialog.ModifyNickDialog;
import com.android.schedule.Events.EventArgs;
import com.android.schedule.Events.EventTypes;
import com.android.schedule.Image.SmartImageView;
import com.android.schedule.Model.UserInfoData;
import com.android.schedule.Services.ServiceManager;
import com.android.schedule.Utils.AlbumInfoUtil;
import com.android.schedule.Utils.MyProgressDialog;
import com.android.schedule.Utils.NetworkUtils;
import com.archermind.schedule.R;

public class AccountSettingScreen extends Screen implements OnClickListener {

	private static final int LOGOUT_FAILED = 0;

	public static final String ALBUMNAME_AVATAR = "avatar";// 相册的名字

	private static final int LOGOUT_SUCCESS = 1;

	private SmartImageView headImage;

	private LinearLayout bindTelephone;

	private LinearLayout headImageSetLayout;

	private LinearLayout nickNameSetLayout;

	private Button logout;

	private String mUserName = "";

	private Button goback;

	private TextView loginNick;

	private ProgressDialog mpDialog;

	// url http://api.amtbaas.com/0/services
	// /showPictrue?appId=462b39f3eb7c4fb9a8e027473a6cd322&appSecret=482a4afe2f0e4020832078a4b4eeeae4
	//  &username=liqifan@163.com&filename=17_20120910_155620.jpg&album=myalbumname
	private String mUpPhotoUrl = "http://api.amtbaas.com/0/services/showPictrue?appId=957ce0a81a0d4b4793c39471950ad298&appSecret=94165c45d0664e68b7f2a7712734e957";

	private ModifyNickDialog modifyNickDialog;

	private AmtAlbumObj mAlbumObj;

	private Handler handler;

	private String dictionry = Environment.getExternalStorageDirectory().getPath() + "/"
			+ ScheduleApplication.getContext().getPackageName() + "/schedule";

	private String fileName = null;

	private String headImagePath = null;

	private boolean logoutflag = false;
	
	private Uri imageFilePath;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try {
				// 处理图片上传过程发送的消息
				switch (msg.what) {
					
					case MessageTypes.ERROR_MESSAGE :
						
						ScheduleApplication.LogD(AccountSettingScreen.class, " 请求失败！ "
								+ (String) msg.obj);
						if (((String) msg.obj).contains("相册名称已经存在")) {
							
							mAlbumObj.requestAlbumidInfo(mUserName);
						}
						break;
						
						// 创建相册成功
					case MessageTypes.MESSAGE_CREATEALBUM :
						
						ScheduleApplication.LogD(AccountSettingScreen.class, "创建相册成功");
						mAlbumObj.requestAlbumidInfo(mUserName);
						break;
						
						// 获取相册的信息包括id的值
					case MessageTypes.MESSAGE_GETALBUM :
						
						ScheduleApplication.LogD(getClass(), "获取相册详细信息成功");
						int albumid = -1;
						albumid = AlbumInfoUtil.getAlbumIdByName(mAlbumObj, msg.obj, ALBUMNAME_AVATAR);
						
						if (albumid == 0) {
							
							mAlbumObj.createAlbum(mUserName, ALBUMNAME_AVATAR);
						} else {
							
							ScheduleApplication.LogD(AccountSettingScreen.class, "获取相册详细信息成功");
							
							ArrayList<String> picPath = new ArrayList<String>();
							picPath.add(headImagePath);
							ArrayList<String> picNames = new ArrayList<String>();
							picNames.add(headImagePath.substring(headImagePath.lastIndexOf("/") + 1));
							
							mAlbumObj.uploadPicFiles(picPath, picNames, albumid);
							
							ScheduleApplication.LogD(AccountSettingScreen.class, "albumid：" + albumid);
						}
						break;
						
					case MessageTypes.MESSAGE_UPLOADPIC :
						// 上传头像文件成功，开始执行插入数据库操作
						ScheduleApplication.LogD(AccountSettingScreen.class, "图片上传成功");
						String filename = headImagePath.substring(headImagePath.lastIndexOf("/") + 1);
						
						mUpPhotoUrl = mUpPhotoUrl + "&username=" + mUserName + "&filename=" + filename
								+ "&album=" + ALBUMNAME_AVATAR;
						
						if (0 == ServiceManager.getServerInterface().uploadPhoto(
								String.valueOf(ServiceManager.getUserId()), mUpPhotoUrl)) {
							
							ScheduleApplication.LogD(AccountSettingScreen.class, "上传图片url成功 url = "
									+ mUpPhotoUrl);
							ServiceManager.setAvator_url(mUpPhotoUrl);
							Toast.makeText(getApplicationContext(), "上传图片成功！", Toast.LENGTH_LONG)
							.show();
							
							String uri = getUriFormWeb();
							headImage.setImageUrl(uri, R.drawable.friend_item_img,
									R.drawable.friend_item_img);
							
						} else {
							
							ScheduleApplication.LogD(AccountSettingScreen.class, "上传图片url失败");
							Toast.makeText(getApplicationContext(), "上传图片失败！", Toast.LENGTH_LONG)
							.show();
						}
						
						break;
					default :
						break;
				}
			} catch (Exception e) {
				ScheduleApplication.logException(getClass(),e);
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.account_setting);
		modifyNickDialog = new ModifyNickDialog(this);
		modifyNickDialog.getDialog().setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				loginNick.setText(ServiceManager.getSPUserInfo(UserInfoData.NICK));
			}
		});

		mpDialog = MyProgressDialog.getProgressDialog(AccountSettingScreen.this);
		headImage = (SmartImageView) findViewById(R.id.headImage);
		loginNick = (TextView) findViewById(R.id.login_nick);
		bindTelephone = (LinearLayout) findViewById(R.id.bindTelephone);
		headImageSetLayout = (LinearLayout) findViewById(R.id.headimageset);
		nickNameSetLayout = (LinearLayout) findViewById(R.id.login_nick_set);
		logout = (Button) findViewById(R.id.logout);
		goback = (Button) findViewById(R.id.title_bar_setting_btn);

		// loginNick.setOnClickListener(this);
		// headImage.setOnClickListener(this);
		bindTelephone.setOnClickListener(this);
		logout.setOnClickListener(this);
		goback.setOnClickListener(this);
		headImageSetLayout.setOnClickListener(this);
		nickNameSetLayout.setOnClickListener(this);

		loginNick.setText(ServiceManager.getSPUserInfo(UserInfoData.NICK));
		mUserName = ServiceManager.getSPUserInfo(UserInfoData.EMAIL);
		String uri = getUriFormWeb();
		ScheduleApplication.LogD(getClass(), "uri =" + uri);

		Pattern p = Pattern.compile("(filename=)([^<].*?)\\&");// 正则表达式，

		if (!TextUtils.isEmpty(uri)) {

			Matcher m = p.matcher(uri);
			while (m.find()) {
				fileName = m.group(2);
				ScheduleApplication.LogD(getClass(), "urifile =" + fileName);
			}
		}

		ServiceManager.setAvator_url(uri);
		System.out.println("***********uri = " + uri);

		fileName = ServiceManager.getSPUserInfo(UserInfoData.USER_ID) + "_"
				+ System.currentTimeMillis() + ".jpg";
		headImagePath = dictionry + "/" + fileName;

		// if (fileName != null) {
		//
		// headImagePath = dictionry + "/" + fileName;
		// if (new File(headImagePath).exists()) {
		//
		// ScheduleApplication.LogD(getClass(), "本地有头像，使用本地头像");
		// headImage.setImageURI(Uri.parse(headImagePath));
		//
		// } else {
		//
		// fileName = ServiceManager.getSPUserInfo(UserInfoData.USER_ID) + "_"
		// + System.currentTimeMillis() + ".jpg";
		// headImagePath = dictionry + "/" + fileName;
		//
		// ScheduleApplication.LogD(getClass(), "本地没有头像，从网上获取头像");
		// headImage.setImageUrl(uri, R.drawable.friend_item_img,
		// R.drawable.friend_item_img);
		// }
		// } else {
		//
		// fileName = ServiceManager.getSPUserInfo(UserInfoData.USER_ID) + "_"
		// + System.currentTimeMillis() + ".jpg";
		// }

		headImage.setImageUrl(uri, R.drawable.friend_item_img, R.drawable.friend_item_img);

		handler = new Handler() {
			public void handleMessage(Message msg) {
				try {
					if (mpDialog != null) {
						mpDialog.dismiss();
					}
					switch (msg.what) {
						case LOGOUT_FAILED :
							// String res = "";
							// switch (msg.arg1) {
							// case -1:
							// finish();
							// res += "密码错误";
							// break;
							//
							// case -600:
							// res += "没有登录";
							// finish();
							// break;
							// default:
							// res += "未知";
							// break;
							// }
							// res += " 返回";
							// Toast.makeText(getApplicationContext(), res,
							// Toast.LENGTH_SHORT).show();
							Toast.makeText(getApplicationContext(),
									R.string.check_new_version_no_network, Toast.LENGTH_SHORT).show();
							break;
							
						case LOGOUT_SUCCESS :
							ServiceManager.ToastShow("注销成功");
							ServiceManager.setUserId(0);
							Intent it = new Intent(AccountSettingScreen.this, LoginScreen.class);
							startActivity(it);
							finish();
							break;
					}
					logoutflag = false;
				} catch (Exception e) {
					ScheduleApplication.logException(getClass(),e);
				}
			};
		};
	}

	public void onClick(View v) {
		try {
			switch (v.getId()) {
				case R.id.login_nick_set :
					modifyNickDialog.show();
					break;
				case R.id.headimageset :
					ShowPickDialog();
					break;
				case R.id.bindTelephone :
					Intent it = new Intent(AccountSettingScreen.this, TelephoneBindScreen.class);
					startActivity(it);
					break;
				case R.id.logout :
					if (!logoutflag) {
						logoutflag = true;
						mpDialog.show();
						new Thread() {
							public void run() {
								int ret = Integer.parseInt(ServiceManager.getServerInterface().logout(
										String.valueOf(ServiceManager.getUserId())));
								if (ret == -101) {
//								ScheduleApplication
//										.LogD(getClass(),
//												"ret = "
//														+ ServiceManager.getServerInterface()
//																.logout(String
//																		.valueOf(ServiceManager
//																				.getUserId())));
//								Message msg = new Message();
//								msg.what = LOGOUT_FAILED;
//								msg.arg1 = ret;
//								handler.sendMessage(msg);
									handler.sendEmptyMessage(LOGOUT_FAILED);
								} else {
									ScheduleApplication.LogD(getClass(),"logout success, ret = "+ret);
									ServiceManager.setCookie("");
									handler.sendEmptyMessage(LOGOUT_SUCCESS);
									eventService
									.onUpdateEvent(new EventArgs(EventTypes.LOGOUT_SUCCESS));
								}
							};
						}.start();
					}
					break;
					
				case R.id.title_bar_setting_btn :
					Intent intent = new Intent(this, MenuScreen.class);
					startActivity(intent);
					finish();
					break;
			}
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(),e);
		}
	}

//	private String getFilepathFromUri(Uri uri) {
//		ContentResolver mContentResolver = getContentResolver();
//		Cursor cursor = mContentResolver.query(uri, null, null, null, null);
//		cursor.moveToFirst();
//		String filepath = cursor.getString(1);
//		cursor.close();
//		return filepath;
//	}

	/**
	 * 选择提示对话框
	 */
	private void ShowPickDialog() {
		new AlertDialog.Builder(this).setTitle("设置头像...")
				.setNegativeButton("相册", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent intent = new Intent(Intent.ACTION_PICK, null);
						intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								"image/*");
						startActivityForResult(intent, 1);
						ScheduleApplication.LogD(getClass(), "从相册选择头像");

					}
				}).setPositiveButton("拍照", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						ContentValues values = new ContentValues(3);   
						values.put(MediaStore.Images.Media.DISPLAY_NAME, "head");   
						values.put(MediaStore.Images.Media.DESCRIPTION, "this is description");   
						values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");   
						imageFilePath = AccountSettingScreen.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);   
		              intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFilePath); //这样就将文件的存储方式和uri指定到了Camera应用中   
		                   
						startActivityForResult(intent, 2);
						ScheduleApplication.LogD(getClass(), "拍照");
					}
				}).show();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent i = new Intent(this, MenuScreen.class);
		startActivity(i);
		super.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Uri uri = null;
		switch (requestCode) {
			case 1 :
				if (data != null) {
					ScheduleApplication.LogD(getClass(), "1 相册返回的数据 ");
					uri = data.getData();
					startPhotoZoom(uri);
					// headImagePath = getFilepathFromUri(uri);
				}
				break;

			case 2 :
				if (resultCode == Activity.RESULT_OK) {
					ScheduleApplication.LogD(getClass(), "1 拍照返回的数据 ");
					
					startPhotoZoom(imageFilePath);
				} else {

					ScheduleApplication.LogD(getClass(), "1 拍照返回的数据 取消拍照 ");
				}
				break;

			case 3 :
				if (data != null) {
					setPicToView(data);
				}
				break;

			default :
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private String getUriFormWeb() {
		if (NetworkUtils.getNetworkState(this) != NetworkUtils.NETWORN_NONE) {

			String jsonString = ServiceManager.getServerInterface().findUserInfobyUserId(
					String.valueOf(ServiceManager.getUserId()));
			if (jsonString != null && !"".equals(jsonString)) {
				if (jsonString.indexOf("photo_url") >= 0) {// 防止返回错误码
					try {
						JSONArray jsonArray = new JSONArray(jsonString);
						ScheduleApplication.LogD(FriendsDyamicScreen.class,
								jsonString + jsonArray.length());
						JSONObject jsonObject = (JSONObject) jsonArray.opt(0);

						ScheduleApplication.LogD(getClass(), "地址为 url = " + jsonObject.toString());
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

	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
		if (bitmap == null)
			return null;

		Bitmap output = Bitmap
				.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
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

	private void writePhoto(byte[] bytes) {
		// String dictionry = Environment.getExternalStorageDirectory()
		// + ScheduleApplication.getContext().getPackageName()+"/image";
		File dir = new File(dictionry);
		File file = null;
		if (!dir.exists()) {

			ScheduleApplication.LogD(getClass(), "文件夹不存在");
			if (dir.mkdirs()) {

				ScheduleApplication.LogD(getClass(), "创建文件夹成功" + dir.getPath());
			} else {
				ScheduleApplication.LogD(getClass(), "创建文件夹失败" + dir.getPath());
			}

			file = new File(dir, fileName);
		} else {
			file = new File(dir, fileName);
			if (file.exists()) {
				file.delete();
				try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		OutputStream os;
		try {
			os = new FileOutputStream(file);
			try {
				os.write(bytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void setPicToView(Intent picdata) {
		try {
			Bundle extras = picdata.getExtras();
			if (extras != null) {
				
				// 显示头像
				Bitmap photo = extras.getParcelable("data");
				photo = toRoundCorner(photo, 15);
				Drawable drawable = new BitmapDrawable(photo);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				photo.compress(Bitmap.CompressFormat.PNG, 60, stream);
				byte[] b = stream.toByteArray();
				writePhoto(b);
				// headImage.setImageDrawable(drawable);// 显示头像
				ScheduleApplication.LogD(getClass(), " setPicToView ");
				// 给用户创建相册用于上传头像
				ServiceManager.getServerInterface().InitAmtCloud(this);
				AmtApplication.setAmtUserName(mUserName);
				
				AmtUserObj userObj = new AmtUserObj(handler);
				mAlbumObj = new AmtAlbumObj();
				mAlbumObj.setHandler(mHandler);
				mAlbumObj.createAlbum(mUserName, ALBUMNAME_AVATAR);
				
				ScheduleApplication.LogD(getClass(), "开始上传图像");
			}
		} catch (Exception e) {
			ScheduleApplication.logException(getClass(),e);
		}
	}

}
