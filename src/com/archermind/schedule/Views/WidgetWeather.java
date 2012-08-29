
package com.archermind.schedule.Views;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Screens.HomeScreen;
import com.archermind.schedule.Screens.WeatherScreen;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Utils.NetworkUtils;
import com.archermind.schedule.Utils.ServerInterface;

public class WidgetWeather extends AppWidgetProvider {

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        ScheduleApplication.LogD(getClass(), "onEnabled");
    }

    private static final String ACTION_TIME_TICK = "com.archermind.widgetweather.timetick";

    private static final String TAG = "WeatherWidget";

    private String mCurrentTemp = null;

    private static final int TIMECHANGE = 1;

    private static final int SCHEDULE = 2;

    private String mMax = null;

    private String mMin = null;

    private String mWeather = null;

    private String mCity = null;

    private String mDateTime = null;// 周三 10/12/11

    private String mTimeHour = null;

    private String mTimeMinute = null;

    private DatabaseManager dbManager;

    private String mAmOrPm = null;

    private String mContent = null;

    private Context mContext = null;

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {

        super.onDeleted(context, appWidgetIds);
        Log.i(TAG, "OnDeleted");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Log.i(WidgetWeather.class.getCanonicalName(), "onUpdate");
        Intent it = new Intent(ACTION_TIME_TICK);
        context.startService(it);
        
        mContext = context;
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
       
    }

    /**
     * Update widget. This method can be called inside the same package.
     * 
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     */
    void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // getWeatherFromWeb();
        getDateTime();
        getLocalSchedule();
        showData(appWidgetId);

    }

    private void showDateTime(Context context, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather_home);

        setRemoteView(views, R.id.widgetweatheramorpm, mAmOrPm);

        setRemoteView(views, R.id.widgetweatherhour, mTimeHour);

        setRemoteView(views, R.id.widgetweatherminute, mTimeMinute);

        setRemoteView(views, R.id.widgetweathertime, mDateTime);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void getDateTime() {

        long currentTime = System.currentTimeMillis();
        mDateTime = DateTimeUtils.time2String("E", currentTime) + " ";
        mDateTime = mDateTime + DateTimeUtils.time2String("d/M/yy", currentTime);
        mTimeHour = DateTimeUtils.time2String("h", currentTime);
        mTimeMinute = DateTimeUtils.time2String("m", currentTime);

        mAmOrPm = DateTimeUtils.time2String("a", currentTime);
        if (mAmOrPm.equals("上午")) {

            mAmOrPm = "AM";
        } else if (mAmOrPm.equals("下午")) {

            mAmOrPm = "PM";
        }
    }

    private void getWeatherFromWeb() {
        // 如果联网了，则从服务器获取数据
        if (NetworkUtils.getNetworkState(mContext) != NetworkUtils.NETWORN_NONE) {

            ServerInterface serverInerface = new ServerInterface();
            SharedPreferences sp = mContext.getSharedPreferences(
                    "com.archermind.schedule_preferences", Context.MODE_WORLD_WRITEABLE);
            mCity = sp.getString("city", "北京");
            Log.i(TAG, "getDataFromWeb");

            String strResult = serverInerface.getWeather(sp.getString("province", "北京"), mCity);
            Log.i(TAG, strResult);
            // [{"city":"武汉","province":"湖北","cid":"101200101","weather":"\"st1 \":\"33\",\"temp1\":\"33℃~27℃\",\"weather1\":\"多云\",\"temp2\": \"34℃~28℃\",\"weather2\":\"多云\",\"temp3\":\"35℃~28℃\",\"weather3 \":\"多云\",\"temp4\":\"34℃~24℃\",\"weather4\":\"多云\""}]
            if (strResult != null && !strResult.equals("")) {
                if (strResult.indexOf("city") >= 0) {
                    String city, province, cid, weather = null;
                    String weatherDate;
                    try {

                        JSONArray jsonArray = new JSONArray(strResult);
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = (JSONObject)jsonArray.opt(i);
                            weatherDate = jsonObject.getString("date");
                            city = jsonObject.getString("city");
                            province = jsonObject.getString("province");
                            cid = jsonObject.getString("cid");
                            weather = jsonObject.getString("weather");
                        }

                    } catch (JSONException e) {

                        e.printStackTrace();
                    }

                    // weather="st1":"34","temp1":"34℃~26℃","weather1":"多云",
                    weather = weather.replace("\"", "");
                    String[] parts = weather.split(",");
                    String[] datas = null;
                    String[] tag = {
                            "st1", "temp1", "weather1"
                    };

                    Map<String, String> itemsmap = new HashMap<String, String>();
                    for (String part : parts) {
                        datas = part.split(":");
                        if (datas != null) {

                            for (int i = 0; i < tag.length; i++) {
                                if (datas[0].equalsIgnoreCase(tag[i])) {
                                    itemsmap.put(tag[i], datas[1]);
                                }
                            }

                        }
                    }

                    if (itemsmap.get("weather1").contains("转")) {
                        String[] weatherarray = itemsmap.get("weather1").split("转");
                        itemsmap.put("weather1", weatherarray[0]);
                    }
                    mCurrentTemp = itemsmap.get("st1");
                    String temp_rage = itemsmap.get("temp1");
                    String[] mTemp2 = temp_rage.split("~");
                    mMax = mTemp2[0];
                    mMin = mTemp2[1];
                    mWeather = itemsmap.get("weather1");

                }
            }
        }
    }

    private void getLocalSchedule() {

        ScheduleApplication.LogD(WidgetProvider.class, "readlocalschedule");
        Calendar mCalendar = Calendar.getInstance();

        dbManager = new DatabaseManager(mContext);
        dbManager.open();
        Cursor cursor = dbManager.queryTodayLocalSchedules(mCalendar.getTimeInMillis());

        if (cursor != null) {

            try {
                if (cursor.getCount() != 0) {
                    if (cursor.moveToFirst()) {

                        mContent = cursor.getString(cursor
                                .getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT));

                    }
                }
            } catch (Exception e) {

                e.printStackTrace();
            } finally {

                cursor.close();
            }
        }

        dbManager.close();
    }

    private void showData(int appWidgetId) {

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_weather_home);
        Intent intent = new Intent(mContext, HomeScreen.class);
        PendingIntent pIntentHomeScreen = PendingIntent.getActivity(mContext, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widgetweatherlayoutschedule, pIntentHomeScreen);

        Intent intentweather = new Intent(mContext, WeatherScreen.class);
        PendingIntent pIntentweatherScreen = PendingIntent.getActivity(mContext, 0, intentweather,
                0);
        views.setOnClickPendingIntent(R.id.widgetweatherlayouttime, pIntentweatherScreen);
        views.setOnClickPendingIntent(R.id.widgetweatherlayoutcity, pIntentweatherScreen);

        Log.i(TAG, "mTemp = " + mCurrentTemp);

        setRemoteView(views, R.id.widgetweathertemp, mCurrentTemp + "°");

        setRemoteView(views, R.id.widgetweatherMaxTemp, mMax + "℃");

        setRemoteView(views, R.id.widgetweatherMinTemp, mMin + "℃");

        setRemoteView(views, R.id.widgetweathershow, mWeather);

        setRemoteView(views, R.id.widgetweatheramorpm, mAmOrPm);

        setRemoteView(views, R.id.widgetweatherhour, mTimeHour);

        setRemoteView(views, R.id.widgetweatherminute, mTimeMinute);

        setRemoteView(views, R.id.widgetweathertime, mDateTime);

        setRemoteView(views, R.id.widgetweathercity, mCity);

        if ((mContent != null) && (!"".equals(mContent))) {

            views.setTextViewText(R.id.widgetweathercontent, mContent);

        } else {

            views.setTextViewText(R.id.widgetweathercontent, "您今天没有日程！");
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    void setRemoteView(RemoteViews rv, int id, String content) {

        if (!TextUtils.isEmpty(content)) {

            rv.setTextViewText(id, content);
        }

    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        ScheduleApplication.LogD(WidgetWeather.class, "onReceive :" + intent.getAction());
        AppWidgetManager gm = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = gm.getAppWidgetIds(new ComponentName(context, WidgetWeather.class));

        if (action.equals(Intent.ACTION_TIME_CHANGED) 
                || action.equals(Intent.ACTION_DATE_CHANGED)
                || action.equals(Intent.ACTION_TIMEZONE_CHANGED)
                || action.equals("com.archermind.TimeTickService.tick")) {

            for (int i = 0; i < appWidgetIds.length; i++) {

                updateAppWidget(context, gm, appWidgetIds[i], TIMECHANGE);
            }
        }
        
        if (action.equals(Intent.ACTION_LOCALE_CHANGED)
        || action.equals("android.appwidget.action.LOCAL_SCHEDULE_UPDATE")) {

            for (int i = 0; i < appWidgetIds.length; i++) {

                updateAppWidget(context, gm, appWidgetIds[i], SCHEDULE);
            }

        } else {

            super.onReceive(context, intent);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager gm, int appWidgetId, int flag) {

        switch (flag) {

            case TIMECHANGE:

                getDateTime();
                showDateTime(context, appWidgetId);
                break;

            case SCHEDULE:

                getLocalSchedule();
                showLocalSchedule(appWidgetId);

                break;

            default:
                break;
        }

    }

    private void showLocalSchedule(int appWidgetId) {

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_weather_home);

        if ((mContent != null) && (!"".equals(mContent))) {

            views.setTextViewText(R.id.widgetweathercontent, mContent);

        } else {

            views.setTextViewText(R.id.widgetweathercontent, "您今天没有日程！");
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

}
