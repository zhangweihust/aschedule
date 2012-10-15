package com.archermind.schedule.Views;

import java.util.Calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.widget.RemoteViews;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Screens.HomeScreen;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.DateTimeUtils;

public class WidgetProvider extends AppWidgetProvider {

	private static DatabaseManager dbManager;

	private DatabaseHelper databaseHelper;

	private SQLiteDatabase database;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
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

	public void onDeleted(Context context, int[] appWidgetIds) {
		// Called when one or more instance of this appWidget is destroyed
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onDisabled(Context context) {
		// Called when last instance of this appWidget is destroyed
		super.onDisabled(context);
	}

	@Override
	public void onEnabled(Context context) {
		// Called when widget is instantiated
		super.onEnabled(context);
	}

	/**
	 * Update widget. This method can be called inside the same package.
	 * 
	 * @param context
	 * @param appWidgetManager
	 * @param appWidgetId
	 */
	void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId) {
		ScheduleApplication.LogD(WidgetProvider.class, "updateAppWidget ");
		Intent intent = new Intent(context, HomeScreen.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);

		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);
		views.setOnClickPendingIntent(R.id.widget, pendingIntent);
		views.setTextViewText(
				R.id.title,
				DateTimeUtils.time2String("MM-dd EEEE",
						System.currentTimeMillis()));
		views.removeAllViews(R.id.schedule_list);

		Cursor c = queryTodayLocalSchedules(System.currentTimeMillis());
		if (c != null) {

			if (c.getCount() > 0) {
				RemoteViews view;
				String content, time;
				int i = 0;
				ScheduleApplication.LogD(WidgetProvider.class, "c.getCount() :"
						+ c.getCount());
				for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
					ScheduleApplication.LogD(WidgetProvider.class,
							"updateAppWidget :" + appWidgetId);
					if (i >= 5) {
						view = new RemoteViews(context.getPackageName(),
								R.layout.widget_item);
						view.setTextViewText(R.id.schedule_title, "点击查看更多...");
						views.addView(R.id.schedule_list, view);
						break;
					}
					view = new RemoteViews(context.getPackageName(),
							R.layout.widget_item);
					content = c
							.getString(c
									.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT));
					time = DateTimeUtils
							.time2String(
									"hh:mm",
									c.getLong(c
											.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME)));
					view.setTextViewText(R.id.schedule_title, time + "-"
							+ content);
					views.addView(R.id.schedule_list, view);
					i++;
				}
			}
			c.close();
		}
		// Tell the widget manager to update
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		ScheduleApplication.LogD(WidgetProvider.class,
				"onReceive :" + intent.getAction());
		if (action.equals(Intent.ACTION_LOCALE_CHANGED)
				|| action.equals(Intent.ACTION_TIME_CHANGED)
				|| action.equals(Intent.ACTION_DATE_CHANGED)
				|| action.equals(Intent.ACTION_TIMEZONE_CHANGED)
				|| action
						.equals("android.appwidget.action.LOCAL_SCHEDULE_UPDATE")) {
			AppWidgetManager gm = AppWidgetManager.getInstance(context);
			int[] appWidgetIds = gm.getAppWidgetIds(new ComponentName(context,
					WidgetProvider.class));

			// dbManager = new DatabaseManager(context);
			// dbManager.open();

			databaseHelper = new DatabaseHelper(context);
			database = databaseHelper.getReadableDatabase();

			for (int i = 0; i < appWidgetIds.length; i++) {
				updateAppWidget(context, gm, appWidgetIds[i]);
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

	public Cursor queryTodayLocalSchedules(long timeInMillis) {
		return database.query(
				DatabaseHelper.TAB_LOCAL_SCHEDULE,
				null,
				DatabaseHelper.COLUMN_SCHEDULE_START_TIME
						+ " BETWEEN ? AND ?  AND "
						+ DatabaseHelper.COLUMN_SCHEDULE_OPER_FLAG
						+ " != 'D' AND " + DatabaseHelper.COLUMN_SCHEDULE_ORDER
						+ " = ? ",
				new String[]{
						String.valueOf(DateTimeUtils.getToday(Calendar.AM,
								timeInMillis)),
						String.valueOf(DateTimeUtils.getToday(Calendar.PM,
								timeInMillis)), "0"}, null, null,
				DatabaseHelper.COLUMN_SCHEDULE_START_TIME + " ASC");
	}

}
