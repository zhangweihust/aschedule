package com.archermind.schedule.Views;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class MonthCalWidget extends AppWidgetProvider {
	
	public static String WIDGET_CLICK_NEXT   = "com.josegd.monthcalwidget.ACTION_NEXT_MONTH";
	public static String WIDGET_CLICK_PREV   = "com.josegd.monthcalwidget.ACTION_PREV_MONTH";
	public static String WIDGET_CLICK_MYTV   = "com.josegd.monthcalwidget.ACTION_CURRENT_MONTH";
	public static String WIDGET_NEWDAY_ALARM = "com.josegd.monthcalwidget.ACTION_NEW_DAY";
	public static String WIDGET_DATE_CHANGED = "android.intent.action.DATE_CHANGED";

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		initService(context);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			String action = intent.getAction();
			if (action.equals(WIDGET_CLICK_NEXT)) {
				MCWUpdateService.nextMonth();
			} else
				if (action.equals(WIDGET_CLICK_PREV)) {
					MCWUpdateService.previousMonth();
				} else
					if (action.equals(WIDGET_CLICK_MYTV) || action.equals(WIDGET_NEWDAY_ALARM) ||	action.equals(WIDGET_DATE_CHANGED)) {
						MCWUpdateService.initMonthDisplayHelper();
					} else {
						initService(context);
						return;
					  }
			MCWUpdateService.updateCalendar(context);
		} catch (NullPointerException e) {
			initService(context); 
		}
	}

	private void initService(Context context) {
		context.startService(new Intent(context, MCWUpdateService.class));
	}
	
}

