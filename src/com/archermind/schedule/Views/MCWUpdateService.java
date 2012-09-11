package com.archermind.schedule.Views;

import java.util.Calendar;

import com.archermind.schedule.R;
import com.archermind.schedule.Calendar.CalendarData;
import com.archermind.schedule.Calendar.LunarCalendar;
import com.archermind.schedule.Utils.Constant;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.widget.RemoteViews;

public class MCWUpdateService extends IntentService {

	private static int yearNow;
	private static int monthNow;
	private static int today;
	
	private static int jumpYear;
	private static int jumpMonth;
	
	private static String[] dayNumber = new String[42];
	
	private static CalendarData calendarData;

	public MCWUpdateService() {
		super("MCWUpdateService");
	}
	
	@Override
	public void onHandleIntent(Intent intent) {
		initMonthDisplayHelper();
		updateCalendar(this);
	}
	
	public static void initMonthDisplayHelper() {
		Calendar cal = Calendar.getInstance();
		yearNow = cal.get(Calendar.YEAR);
		monthNow = cal.get(Calendar.MONTH);
		today = cal.get(Calendar.DATE);
	}

	public static void nextMonth() {
		jumpMonth++;
	}
	
	public static void previousMonth() {
		jumpMonth--;
	}
	
	public static void updateCalendar(Context context) {
		ComponentName widget = new ComponentName(context, MonthCalWidget.class);
		AppWidgetManager mgr = AppWidgetManager.getInstance(context);
		RemoteViews remViews = buildUpdate(context);
		mgr.updateAppWidget(widget, remViews);
	}

	private static RemoteViews buildUpdate(Context context) {
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_layout);
		setViewAction(context, views, MonthCalWidget.WIDGET_CLICK_NEXT, R.id.nextmonth);
		setViewAction(context, views, MonthCalWidget.WIDGET_CLICK_PREV, R.id.prevmonth);
		setViewAction(context, views, MonthCalWidget.WIDGET_CLICK_MYTV, R.id.monthyear);
		setCalendar(context, views);
		return views;
	}
	
	private static void setViewAction(Context cont, RemoteViews rv, String action, int idView) {
		Intent intent = new Intent(cont, MonthCalWidget.class);
		intent.setAction(action);		
		rv.setOnClickPendingIntent(idView, PendingIntent.getBroadcast(cont, 0, intent, 0));
	}

	private static void setCalendar(Context context, RemoteViews rv){
		
		calendarData = new CalendarData(context,jumpMonth, jumpYear, yearNow,monthNow, today,Constant.flagType);
		dayNumber = calendarData.getDayNumber();
		rv.setTextViewText(R.id.monthyear, calendarData.getShowYear() + "." + calendarData.getShowMonth());
		
		int identifier_week = 0;
		int identifier_day = 0;
		int identifier_mark = 0;
		int identifier_holiday = 0;
		for (int i = 0; i < 7; i++) {
			identifier_week = context.getResources().getIdentifier("day" + i, "id", context.getPackageName());
			rv.setTextViewText(identifier_week, calendarData.getWeek()[i]);
		}		
        for (int i = 0; i < dayNumber.length; i++) {
        	
    		identifier_day = context.getResources().getIdentifier("date" + i, "id", context.getPackageName());
    		identifier_mark = context.getResources().getIdentifier("mark" + i, "id", context.getPackageName());
    		identifier_holiday = context.getResources().getIdentifier("holiday" + i, "id", context.getPackageName());
    		
//    		rv.setViewVisibility(identifier_mark, View.GONE);
//    		rv.setViewVisibility(identifier_holiday, View.GONE);
    		
    		String temp = dayNumber[i].split("\\.")[1];
    		if(temp.contains(LunarCalendar.suffix)){
    			rv.setViewVisibility(identifier_holiday, View.VISIBLE);
    			rv.setImageViewResource(identifier_holiday, R.drawable.other_holiday);
    		}
    		
        	if (i < calendarData.getDaysOfWeek()) {
            } else if (i < calendarData.getDayOfWeek() + calendarData.getDaysOfWeek()) { // 前一个月
            	SpannableStringBuilder ssb = new SpannableStringBuilder();
				ssb.append(dayNumber[i].split("\\.")[0]);
            	rv.setTextColor(identifier_day, Color.GRAY);
            	rv.setTextViewText(identifier_day, dayNumber[i].split("\\.")[0]);
            	
            } else if (i < calendarData.getDaysOfMonth() + calendarData.getDayOfWeek() + calendarData.getDaysOfWeek()) { // 本月
            	SpannableStringBuilder ssb = new SpannableStringBuilder();
				ssb.append(dayNumber[i].split("\\.")[0]);
				rv.setTextColor(identifier_day, Color.BLACK);
				if(i == today){
					ssb.setSpan(new BackgroundColorSpan(context.getResources().getColor(R.color.selector)), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				}
				rv.setTextViewText(identifier_day, ssb);
				
	    		if(calendarData.getMarkcount()[i] > 0){
	    			rv.setViewVisibility(identifier_holiday, View.GONE);
	    			rv.setViewVisibility(identifier_mark, View.VISIBLE);
	    			rv.setTextViewText(identifier_mark, calendarData.getMarkcount()[i]+"");
	    		}
				
            } else { // 下一个月
             	SpannableStringBuilder ssb = new SpannableStringBuilder();
				ssb.append(dayNumber[i].split("\\.")[0]);
            	rv.setTextColor(identifier_day, Color.GRAY);
            	rv.setTextViewText(identifier_day, ssb);
            }
        }
	}
}
