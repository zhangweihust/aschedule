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
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Utils.DateTimeUtils;

public class LocalScheduleAdapter  extends CursorAdapter {
	private LayoutInflater inflater;
	
	public LocalScheduleAdapter(Context context, Cursor c) {
		super(context, c);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ScheduleItem item = (ScheduleItem) view.getTag(R.layout.local_schedule_item);
		String content = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT));
		long time = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME));
		long noticeTime = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_TIME));
		boolean share = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_SHARE)) == 1;
		boolean important = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_IMPORTANT)) == 1;
		int type = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_TYPE));
		boolean first = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_FIRST_FLAG)) == 1;
		item.content.setText(content);
		item.time.setText(DateTimeUtils.time2String("hh:mm", time));
		String amORpm = DateTimeUtils.time2String("a", time);
		if("上午".equals(amORpm)){
			item.time.setBackgroundResource(R.drawable.am);
		} else if("下午".equals(amORpm)){
			item.time.setBackgroundResource(R.drawable.pm);
		}
		if(first){
			item.dateLayout.setVisibility(View.VISIBLE);
			item.week.setText(DateTimeUtils.time2String("EEEE", time));
			item.date.setText(DateTimeUtils.time2String("dd", time));
		} else {
			item.dateLayout.setVisibility(View.INVISIBLE);
		}
		if(noticeTime == 0){
			item.alarm.setVisibility(View.GONE);
		} else {
			item.alarm.setVisibility(View.VISIBLE);
		}
		if(share){
			item.share.setVisibility(View.VISIBLE);
		} else {
			item.share.setVisibility(View.GONE);
		}
		if(important){
			item.important.setVisibility(View.VISIBLE);
		} else {
			item.important.setVisibility(View.INVISIBLE);
		}
		switch (type) {
		case DatabaseHelper.SCHEDULE_EVENT_TYPE_NONE:
			item.typeView.setBackgroundResource(R.drawable.type_notice);
		case DatabaseHelper.SCHEDULE_EVENT_TYPE_NOTICE:
			item.typeView.setBackgroundResource(R.drawable.type_notice);
			break;
		case DatabaseHelper.SCHEDULE_EVENT_TYPE_ACTIVE:
			item.typeView.setBackgroundResource(R.drawable.type_active);
			break;
		case DatabaseHelper.SCHEDULE_EVENT_TYPE_APPOINTMENT:
			item.typeView.setBackgroundResource(R.drawable.type_appointment);
			break;
		case DatabaseHelper.SCHEDULE_EVENT_TYPE_TRAVEL:
			item.typeView.setBackgroundResource(R.drawable.type_travel);
			break;
		case DatabaseHelper.SCHEDULE_EVENT_TYPE_ENTERTAINMENT:
			item.typeView.setBackgroundResource(R.drawable.type_entertainment);
			break;
		case DatabaseHelper.SCHEDULE_EVENT_TYPE_EAT:
			item.typeView.setBackgroundResource(R.drawable.type_eat);
			break;
		case DatabaseHelper.SCHEDULE_EVENT_TYPE_WORK:
			item.typeView.setBackgroundResource(R.drawable.type_work);
			break;
		}
		EventArgs args = new EventArgs();
		args.putExtra("time", time);
		args.putExtra("first", first);
		view.setTag(args);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.local_schedule_item, null);
		ScheduleItem item = new ScheduleItem();
		item.date = (TextView) view.findViewById(R.id.date);
		item.time = (TextView) view.findViewById(R.id.time);
		item.week = (TextView) view.findViewById(R.id.week);
		item.location = (TextView) view.findViewById(R.id.location);
		item.content = (TextView) view.findViewById(R.id.content);
		item.weather = (ImageView) view.findViewById(R.id.weather);
		item.typeView = (ImageView) view.findViewById(R.id.type);
		item.alarm = (ImageView) view.findViewById(R.id.alarm);
		item.share = (ImageView) view.findViewById(R.id.share);
		item.important = (ImageView) view.findViewById(R.id.important);
		item.dateLayout = view.findViewById(R.id.date_layout);
		view.setTag(R.layout.local_schedule_item,item);
		return view;
	}
	
	private class ScheduleItem{
		private TextView date;
		private TextView week;
		private TextView location;
		private TextView time;
		private TextView content;
		private ImageView weather;
		private ImageView typeView;
		private ImageView alarm;
		private ImageView share;
		private ImageView important;
		private int type;
		private View dateLayout;
	}


}
