
package com.archermind.schedule.Services;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Views.WidgetWeather;

public class TimeTickService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver(this.mTimerTick, new IntentFilter(Intent.ACTION_TIME_TICK));
        ScheduleApplication.LogD(getClass(), "oncreate");
    }

    @Override
    public IBinder onBind(Intent arg0) {

        return null;
    }

    // 声明一个广播接受对象，用接受时间的变化
    private BroadcastReceiver mTimerTick = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            
            ScheduleApplication.LogD(TimeTickService.class, "mTimerTick  " + intent.getAction());
            
            // 在这里直接修改widget
            if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                
                Intent timeIntent = new Intent("com.archermind.TimeTickService.tick");
                context.sendBroadcast(timeIntent);
                
            }
        }
    };

    private String mDateTime;

    private String mTimeHour;

    private String mTimeMinute;

    private String mAmOrPm;

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

    private void showDateTime(Context context, int appWidgetId) {

        ScheduleApplication.LogD(getClass(), "showDateTime");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather_home);
        setRemoteView(views, R.id.widgetweatheramorpm, mAmOrPm);
        setRemoteView(views, R.id.widgetweatherhour, mTimeHour);
        setRemoteView(views, R.id.widgetweatherminute, mTimeMinute);
        setRemoteView(views, R.id.widgetweathertime, mDateTime);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    void setRemoteView(RemoteViews rv, int id, String content) {

        if (!TextUtils.isEmpty(content)) {

            rv.setTextViewText(id, content);
        }
    }

}
