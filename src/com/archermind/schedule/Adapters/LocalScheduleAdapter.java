package com.archermind.schedule.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.archermind.schedule.R;
import com.archermind.schedule.Provider.DatabaseHelper;

public class LocalScheduleAdapter  extends CursorAdapter {
	private LayoutInflater inflater;
	
	public LocalScheduleAdapter(Context context, Cursor c) {
		super(context, c);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ScheduleItem item = (ScheduleItem) view.getTag();
		String content = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_CONTENT));
		int share = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_SHARE));
		int type = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_TYPE));
		item.content.setText(content);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.local_schedule_item, null);
		ScheduleItem item = new ScheduleItem();
		item.date = (TextView) view.findViewById(R.id.date);
		item.week = (TextView) view.findViewById(R.id.week);
		item.location = (TextView) view.findViewById(R.id.location);
		item.content = (TextView) view.findViewById(R.id.content);
		item.weather = (ImageView) view.findViewById(R.id.weather);
		item.alarm = (ImageView) view.findViewById(R.id.alarm);
		item.share = (ImageView) view.findViewById(R.id.share);
		item.important = (ImageView) view.findViewById(R.id.important);
		view.setTag(item);
		return view;
	}
	
	private class ScheduleItem{
		private TextView date;
		private TextView week;
		private TextView location;
		private TextView time;
		private TextView content;
		private ImageView weather;
		private ImageView alarm;
		private ImageView share;
		private ImageView important;
		private int type;
	}


}
