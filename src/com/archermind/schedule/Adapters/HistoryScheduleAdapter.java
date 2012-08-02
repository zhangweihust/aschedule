package com.archermind.schedule.Adapters;

import java.util.ArrayList;
import java.util.List;

import com.archermind.schedule.R;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Utils.ScheduleData;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HistoryScheduleAdapter extends BaseAdapter{

	private LayoutInflater inflater;
	private List<ScheduleData> schedulelist = new ArrayList<ScheduleData>();
	
	public HistoryScheduleAdapter(Context context, List<ScheduleData> scheduledataset)
	{
		inflater = LayoutInflater.from(context);
		schedulelist.addAll(scheduledataset);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return schedulelist.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return schedulelist.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		ScheduleItem item;
		if (convertView == null)
		{
			convertView = inflater.inflate(R.layout.local_schedule_item, null);
			item = new ScheduleItem();
			item.date = (TextView) convertView.findViewById(R.id.date);
			item.time = (TextView) convertView.findViewById(R.id.time);
			item.week = (TextView) convertView.findViewById(R.id.week);
			item.location = (TextView) convertView.findViewById(R.id.location);
			item.content = (TextView) convertView.findViewById(R.id.content);
			item.weather = (ImageView) convertView.findViewById(R.id.weather);
			item.typeView = (ImageView) convertView.findViewById(R.id.type);
			item.alarm = (ImageView) convertView.findViewById(R.id.alarm);
			item.share = (ImageView) convertView.findViewById(R.id.share);
			item.important = (ImageView) convertView.findViewById(R.id.important);
			item.dateLayout = convertView.findViewById(R.id.date_layout);
			convertView.setTag(R.layout.local_schedule_item,item);
		}
		else
		{
			item = (ScheduleItem) convertView.getTag(R.layout.local_schedule_item);
		}
		
		ScheduleData data = schedulelist.get(position);
		
		item.content.setText(data.content);
		item.time.setText(DateTimeUtils.time2String("hh:mm", data.time));
		String amORpm = DateTimeUtils.time2String("a", data.time);
		if("上午".equals(amORpm)){
			item.time.setBackgroundResource(R.drawable.am);
		} else if("下午".equals(amORpm)){
			item.time.setBackgroundResource(R.drawable.pm);
		}
		if(data.first){
			item.dateLayout.setVisibility(View.VISIBLE);
			item.week.setText(DateTimeUtils.time2String("EEEE", data.time));
			item.date.setText(DateTimeUtils.time2String("dd", data.time));
		} else {
			item.dateLayout.setVisibility(View.INVISIBLE);
		}
		if(data.share){
			item.share.setVisibility(View.VISIBLE);
		} else {
			item.share.setVisibility(View.GONE);
		}
		if(data.important){
			item.important.setVisibility(View.VISIBLE);
		} else {
			item.important.setVisibility(View.INVISIBLE);
		}
		switch (data.type) {
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
		args.putExtra("time", data.time);
		args.putExtra("first", data.first);
		convertView.setTag(args);
		
		return convertView;
	}
	
	public void addPreData(List<ScheduleData> prescheduledata)
	{
		int i = 0;
		int size = prescheduledata.size();
		while (i < size)
		{
			schedulelist.add(i, prescheduledata.get(i));
			i++;
		}
		notifyDataSetChanged();
	}
	
	public void addAfterData(List<ScheduleData> afterscheduledata)
	{
		schedulelist.addAll(afterscheduledata);
		notifyDataSetChanged();
	}
	
	public void setTodayData(List<ScheduleData> prescheduledata)
	{
		schedulelist.clear();
		schedulelist.addAll(prescheduledata);
		notifyDataSetChanged();
	}
	
	public long getEarliestTime()
	{
		long time = 0;
		if (schedulelist.size() > 0)
		{
			time = schedulelist.get(0).time;
		}
		
		return time;
	}
	
	public long getlatestTime()
	{
		long time = 0;
		int size = schedulelist.size();
		if (size > 0)
		{
			time = schedulelist.get(size - 1).time;
		}
		
		return time;
	}
	
	public boolean isEmpty()
	{
		return schedulelist.size() == 0 ? true : false;
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
