
package com.archermind.schedule.Views;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Screens.HomeScreen;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Utils.NetworkUtils;
import com.archermind.schedule.Utils.ServerInterface;

public class WidgetWeather extends AppWidgetProvider {

    private static final String ACTION_TIME_TICK = "com.archermind.widgetweather.timetick";

    private static final String TAG = "WeatherWidget";

    private String mCurrentTemp = null;

    private static final int TIMECHANGE = 1;

    private static final int SCHEDULE = 2;

    private static final int PLACECHANGE = 3;

    private String mMax = null;

    private String mMin = null;

    private String mWeather = null;

    private String mCity = null;

    private String mDateTime = null;// 周三 10/12/11

    private String mTimeHour = null;

    private String mTimeMinute = null;

    private DatabaseManager dbManager;

    private boolean isAm = true;

    private String mContent = null;

    private DatabaseHelper databaseHelper = null;

    private SQLiteDatabase database = null;

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {

        super.onDeleted(context, appWidgetIds);
        Log.i(TAG, "OnDeleted");
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        ScheduleApplication.LogD(getClass(), "onEnabled");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Log.i(WidgetWeather.class.getCanonicalName(), "onUpdate");
        Intent it = new Intent(ACTION_TIME_TICK);
        context.startService(it);

        final int N = appWidgetIds.length;

        databaseHelper = new DatabaseHelper(context);
        database = databaseHelper.getWritableDatabase();

        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

        if (database != null) {
            database.close();
        }

        if (databaseHelper != null) {

            databaseHelper.close();
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

        getWeatherFromWeb(context);
        getDateTime();
        getLocalSchedule();
        showData(context, appWidgetId);

    }

    private void showDateTime(Context context, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather_home);

        setViewDateTime(views);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void setViewDateTime(RemoteViews views) {

        if (isAm) {

            views.setViewVisibility(R.id.widgetweatheram, View.VISIBLE);
            views.setViewVisibility(R.id.widgetweatherpm, View.INVISIBLE);
        } else {

            views.setViewVisibility(R.id.widgetweatheram, View.INVISIBLE);
            views.setViewVisibility(R.id.widgetweatherpm, View.VISIBLE);
        }

        setRemoteView(views, R.id.widgetweatherhour, mTimeHour);
        setRemoteView(views, R.id.widgetweatherminute, mTimeMinute);
        setRemoteView(views, R.id.widgetweathertime, mDateTime);
    }

    private void getDateTime() {

        long currentTime = System.currentTimeMillis();
        mDateTime = DateTimeUtils.time2String("E", currentTime) + " ";
        mDateTime = mDateTime + DateTimeUtils.time2String("dd/MM/yy", currentTime);
        mTimeHour = " " + DateTimeUtils.time2String("hh", currentTime);
        mTimeMinute = DateTimeUtils.time2String("mm", currentTime) + " ";

        String AmOrPm = DateTimeUtils.time2String("a", currentTime);
        if (AmOrPm.equals("上午")) {

            isAm = true;
        } else {

            isAm = false;
        }
    }

    private void getWeatherFromWeb(Context context) {
        Log.i(TAG, "getDataFromWeb1");
        // 如果联网了，则从服务器获取数据
        if (NetworkUtils.getNetworkState(context) != NetworkUtils.NETWORN_NONE) {

            Log.i(TAG, "getDataFromWeb2");

            ServerInterface serverInerface = new ServerInterface();
            SharedPreferences sp = context.getSharedPreferences(
                    "com.archermind.schedule_preferences", Context.MODE_WORLD_WRITEABLE);
            mCity = sp.getString("city", "北京");
            Log.i(TAG, "getDataFromWeb3");

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
        Cursor cursor = null;
        if ((database != null) && (database.isOpen())) {

            cursor = queryTodayLocalSchedules(mCalendar.getTimeInMillis());
        }
        if (cursor != null) {
            try {
                if (cursor.getCount() != 0) {
                    if (cursor.moveToLast()) {

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
    }

    private void showData(Context context, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather_home);
        Intent intent = new Intent(context, HomeScreen.class);
        PendingIntent pIntentHomeScreen = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widgetweatherlayoutschedule, pIntentHomeScreen);

        // Intent intentweather = new Intent(context, WeatherScreen.class);
        // PendingIntent pIntentweatherScreen = PendingIntent
        // .getActivity(context, 0, intentweather, 0);
        // views.setOnClickPendingIntent(R.id.widgetweatherlayouttime,
        // pIntentweatherScreen);
        // views.setOnClickPendingIntent(R.id.widgetweatherlayoutcity,
        // pIntentweatherScreen);

        setViewDateTime(views);
        setViewCityandWeather(views);
        setViewLocalSchedule(views);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    void setRemoteView(RemoteViews rv, int id, String content) {

        if (!TextUtils.isEmpty(content)) {

            rv.setTextViewText(id, content);
        }
    }

    public Cursor queryTodayLocalSchedules(long timeInMillis) {
        return database.query(
                DatabaseHelper.TAB_LOCAL_SCHEDULE,
                null,
                DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " BETWEEN ? AND ?  AND "
                        + DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG + " != 'D' AND "
                        + DatabaseHelper.COLUMN_SCHEDULE_ORDER + " = ? ",
                new String[] {
                        String.valueOf(DateTimeUtils.getToday(Calendar.AM, timeInMillis)),
                        String.valueOf(DateTimeUtils.getToday(Calendar.PM, timeInMillis)), "0"
                }, null, null, DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " ASC");
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        ScheduleApplication.LogD(WidgetWeather.class, "onReceive :" + intent.getAction());
        AppWidgetManager gm = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = gm.getAppWidgetIds(new ComponentName(context, WidgetWeather.class));

        if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_DATE_CHANGED)
                || action.equals(Intent.ACTION_TIMEZONE_CHANGED)
                || action.equals("com.archermind.TimeTickService.tick")) {

            for (int i = 0; i < appWidgetIds.length; i++) {

                updateAppWidget(context, gm, appWidgetIds[i], TIMECHANGE);
            }
        }

        if (action.equals("com.archermind.action.PLACECHANGED")) {

            getWeatherFromWeb(context);

            for (int i = 0; i < appWidgetIds.length; i++) {

                updateAppWidget(context, gm, appWidgetIds[i], PLACECHANGE);
            }
        }

        if (action.equals(Intent.ACTION_LOCALE_CHANGED)
                || action.equals("android.appwidget.action.LOCAL_SCHEDULE_UPDATE")) {
            databaseHelper = new DatabaseHelper(context);
            if (databaseHelper != null) {

                database = databaseHelper.getWritableDatabase();
            }
            for (int i = 0; i < appWidgetIds.length; i++) {

                updateAppWidget(context, gm, appWidgetIds[i], SCHEDULE);
            }
            if (database != null) {

                database.close();
            }
            if (databaseHelper != null) {

                databaseHelper.close();
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
                showLocalSchedule(context, appWidgetId);
                break;

            case PLACECHANGE:
                showCityandWeather(context, appWidgetId);
                break;

            default:
                break;
        }
    }

    private void showCityandWeather(Context context, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather_home);
        setViewCityandWeather(views);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void setViewCityandWeather(RemoteViews views) {

        setRemoteView(views, R.id.widgetweathertemp, mCurrentTemp + "°");
        setRemoteView(views, R.id.widgetweatherMaxTemp, mMax);
        setRemoteView(views, R.id.widgetweatherMinTemp, mMin);
        setRemoteView(views, R.id.widgetweathercity, mCity);

        Map<String, Integer> weathermap = getWeathermap();
        if (!TextUtils.isEmpty(mWeather)) {
            int icon = getweatherMap(mWeather, weathermap);

            if (icon == 0) {

                icon = R.drawable.noweather_100;
            }

            views.setImageViewResource(R.id.widgetweathershow, icon);
        }
    }

    private void showLocalSchedule(Context context, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather_home);
        setViewLocalSchedule(views);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void setViewLocalSchedule(RemoteViews views) {

        if (!TextUtils.isEmpty(mContent)) {

            views.setTextViewText(R.id.widgetweathercontent, mContent);
        } else {

            views.setTextViewText(R.id.widgetweathercontent, "您今天没有日程！");
        }
    }

    public Map<String, Integer> getWeathermap() {
        Map<String, Integer> weathermap = new HashMap<String, Integer>();
        weathermap.put("晴", R.drawable.clear_100);
        weathermap.put("多云", R.drawable.cloudy_100);
        weathermap.put("阴", R.drawable.shade_100);
        weathermap.put("阵雨", R.drawable.shower_100);
        // weathermap.put("雷阵雨", R.drawable.thundershowers_100);
        // weathermap.put("雷阵雨伴有冰雹", R.drawable.thundershowers_hail_100);
        weathermap.put("雨夹雪", R.drawable.sleet_100);
        weathermap.put("小雨", R.drawable.light_rain_100);
        weathermap.put("中雨", R.drawable.moderate_rain_100);
        weathermap.put("大雨", R.drawable.heavy_rain_100);

        weathermap.put("暴雨", R.drawable.rainstorm_100);
        // weathermap.put("大暴雨", R.drawable.downpour_100);
        // weathermap.put("特大暴雨", R.drawable.heavy_rainfall_100);
        weathermap.put("阵雪", R.drawable.shower_snow_100);
        weathermap.put("小雪", R.drawable.slight_snow_100);
        weathermap.put("中雪", R.drawable.moderate_snow_100);
        weathermap.put("大雪", R.drawable.heavy_snow_100);
        weathermap.put("暴雪", R.drawable.blizzard_100);
        weathermap.put("雾", R.drawable.fog_100);
        weathermap.put("冻雨", R.drawable.freezing_rain_100);

        // weathermap.put("小雨-中雨", R.drawable.moderate_rain_100);
        // weathermap.put("中雨-大雨", R.drawable.heavy_rain_100);
        // weathermap.put("大雨-暴雨", R.drawable.rainstorm_100);
        // weathermap.put("暴雨-大暴雨", R.drawable.downpour_100);
        // weathermap.put("大暴雨-特大暴雨", R.drawable.heavy_rainfall_100);
        // weathermap.put("小雪-中雪", R.drawable.moderate_snow_100);
        // weathermap.put("中雪-大雪", R.drawable.heavy_snow_100);
        // weathermap.put("大雪-暴雪", R.drawable.blizzard_100);
        weathermap.put("沙城暴", R.drawable.sand_storm_100);
        // weathermap.put("强沙尘暴", R.drawable.sand_storm_100);

        weathermap.put("浮尘", R.drawable.sand_100);
        weathermap.put("扬沙", R.drawable.sand_100);

        return weathermap;
    }

    public Integer getweatherMap(String key, Map<String, Integer> weathermap) {

        Iterator<Entry<String, Integer>> iterator = weathermap.entrySet().iterator();
        Entry<String, Integer> entry = null;

        while (iterator.hasNext()) {

            entry = iterator.next();
            if (key.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return 0;
    }

}
