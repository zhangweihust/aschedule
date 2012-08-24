package com.archermind.schedule.Adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.archermind.schedule.R;
import com.archermind.schedule.Calendar.CalendarData;
import com.archermind.schedule.Calendar.LunarCalendar;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Model.ScheduleData;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Utils.DateTimeUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HistoryScheduleAdapter extends BaseAdapter{

	private LayoutInflater inflater;
	private List<ScheduleData> schedulelist = new ArrayList<ScheduleData>();
	private boolean existsPrompt = false;
	private LunarCalendar lc = new LunarCalendar();
	
	public HistoryScheduleAdapter(Context context)
	{
		inflater = LayoutInflater.from(context);
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
//		String str = "";
		String holiday = "";
		String[] weeks = new String[] {
				"周日","周一", "周二", "周三", "周四", "周五", "周六"
        };
		if (convertView == null)
		{
			convertView = inflater.inflate(R.layout.local_schedule_item, null);
			item = new ScheduleItem();
			item.date = (TextView) convertView.findViewById(R.id.date);
//			item.yearandmonth = (TextView) convertView.findViewById(R.id.yearandmonth);
			item.time = (TextView) convertView.findViewById(R.id.time);
//			item.week = (TextView) convertView.findViewById(R.id.week);
			item.location = (TextView) convertView.findViewById(R.id.location);
			item.content = (TextView) convertView.findViewById(R.id.content);
			item.weather = (ImageView) convertView.findViewById(R.id.weather);
			item.typeView = (ImageView) convertView.findViewById(R.id.type);
			item.alarm = (ImageView) convertView.findViewById(R.id.alarm);
			item.share = (ImageView) convertView.findViewById(R.id.share);
			item.important = (ImageView) convertView.findViewById(R.id.important);
//			item.dateLayout = convertView.findViewById(R.id.date_layout);
			
			item.title_date = (TextView) convertView.findViewById(R.id.schedule_date);
			item.title_week = (TextView) convertView.findViewById(R.id.schedule_week);
			item.title_lunar = (TextView) convertView.findViewById(R.id.schedule_lunar);
			item.title_year_month = (TextView) convertView.findViewById(R.id.schedule_year_month);
			item.titleLayout = convertView.findViewById(R.id.date_title_layout);
			item.scheduleinfo = convertView.findViewById(R.id.local_schedule_item_info);
			convertView.setTag(R.layout.local_schedule_item,item);
		}
		else
		{
			item = (ScheduleItem) convertView.getTag(R.layout.local_schedule_item);
		}
		
		ScheduleData data = schedulelist.get(position);
		
		if (data.id == -1)
		{
			item.titleLayout.setVisibility(View.VISIBLE);
			item.scheduleinfo.setVisibility(View.GONE);
			item.title_year_month.setVisibility(View.INVISIBLE);
			
			item.title_date.setText(DateTimeUtils.time2String("dd", data.time));
			item.title_week.setText(weeks[getWeek(data.time)]);
//			str = lc.getLunarDate(data.time, true);
//			if (str.matches("[\\s\\S]{1,2}月"))
//			{
//				str += "初一";
//			}
//			item.title_lunar.setText(str);
			
//			if (position == 0)
//			{
			item.title_lunar.setText(lc.getLunarMonth() + lc.getLunarDate(data.time, true));
			holiday = lc.getHolidays(data.time);
			if (!holiday.equals("false"))
			{
				item.title_year_month.setVisibility(View.VISIBLE);
				item.title_year_month.setText(holiday);
			}
				
//			}
		}
		else
		{
			item.titleLayout.setVisibility(View.GONE);
			item.scheduleinfo.setVisibility(View.VISIBLE);
			
			item.content.setText(data.content);
			item.time.setText(DateTimeUtils.time2String("hh:mm", data.time));
			String amORpm = DateTimeUtils.time2String("a", data.time);
			if("上午".equals(amORpm) || "AM".equals(amORpm)){
				item.time.setBackgroundResource(R.drawable.am);
			} else if("下午".equals(amORpm) || "PM".equals(amORpm)){
				item.time.setBackgroundResource(R.drawable.pm);
			}
//			if(data.first){
//				item.dateLayout.setVisibility(View.VISIBLE);
//				item.yearandmonth.setVisibility(View.VISIBLE);
//				item.week.setText(DateTimeUtils.time2String("EEEE", data.time));
//				item.date.setText(DateTimeUtils.time2String("dd", data.time));
//				item.yearandmonth.setText(DateTimeUtils.time2String("yyyy年MM月", data.time));
//			} else {
//				item.dateLayout.setVisibility(View.INVISIBLE);
//				item.yearandmonth.setVisibility(View.GONE);
//			}
			if(data.notice_flag){
				item.alarm.setVisibility(View.VISIBLE);
			} else {
				item.alarm.setVisibility(View.GONE);
			}
			if(data.share){
				item.share.setVisibility(View.VISIBLE);
			} else {
				item.share.setVisibility(View.GONE);
			}
			item.important.setVisibility(View.INVISIBLE);
			switch (data.type) {
			case DatabaseHelper.SCHEDULE_EVENT_TYPE_NONE:
				item.typeView.setBackgroundResource(R.drawable.type_none);
				break;
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
		}
		
		EventArgs args = new EventArgs();
		args.putExtra("id", data.id);
		args.putExtra("time", data.time);
		convertView.setTag(args);
		
		return convertView;
	}
	
	public void addPreData(List<ScheduleData> prescheduledata)
	{
		int i = 0;
		int size = prescheduledata.size();
		if (size <= 0)
		{
			return;
		}
		if (existsPrompt)
		{
			schedulelist.clear();
			existsPrompt = false;
		}
		while (i < size)
		{
			schedulelist.add(i, prescheduledata.get(i));
			i++;
		}
//		notifyDataSetChanged();
	}
	
	public void addAfterData(List<ScheduleData> afterscheduledata)
	{
		if (afterscheduledata.size() <= 0)
		{
			return;
		}
		if (existsPrompt)
		{
			schedulelist.clear();
			existsPrompt = false;
		}
		schedulelist.addAll(afterscheduledata);
//		notifyDataSetChanged();
	}
	
	public void setData(List<ScheduleData> prescheduledata)
	{
		existsPrompt = false;
		schedulelist.clear();
		schedulelist.addAll(prescheduledata);
		notifyDataSetChanged();
	}
	
	public void setNoSchedulePrompt(long time)
	{
		ScheduleData data = new ScheduleData();
		data.id = -1;
		data.content = "日程是空的~点击右上角的+让您的生活变得有序";
 		data.time = time;
		data.type = DatabaseHelper.SCHEDULE_EVENT_TYPE_NONE;
		
		schedulelist.clear();
		schedulelist.add(data);
		existsPrompt = true;
//		notifyDataSetChanged();
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
	
	public boolean containTodaySchedule(int begin,int end,String date)
	{
		boolean ret = false;
		int i;
		String today = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		Date d;
		for (i = begin; i < end; i++)
		{
			if (i >= 0 && i < schedulelist.size())
			{
				d = new Date(schedulelist.get(i).time);
				today = sdf.format(d);
				if (today.equals(date))
				{
					ret = true;
					break;
				}
			}
		}
		
		return ret;
	}
	
	public int getScheduleCountInDay(String date)
	{
		int i = 0;
		int count = 0;
		int size = schedulelist.size();
		String today = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		Date d;
		for (i = 0; i < size; i++)
		{
			d = new Date(schedulelist.get(i).time);
			today = sdf.format(d);
			if (today.equals(date) && schedulelist.get(i).id > 0)
			{
				count++;
			}
		}
		
		return count;
	}
	
	public int getTodayPosition(String date)
	{
		int i = 0;
		int size = schedulelist.size();
		String today = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		Date d;
		for (i = 0; i < size; i++)
		{
			d = new Date(schedulelist.get(i).time);
			today = sdf.format(d);
			if (today.equals(date))
			{
				break;
			}
		}
		
		return i;
	}
	
	public int getWeek(long millstime)
	{
		int ret;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(millstime);
		ret = c.get(Calendar.DAY_OF_WEEK);
		return ret - 1;
	}
	
	private class ScheduleItem{
		private TextView date;
//		private TextView week;
		private TextView location;
//		private TextView yearandmonth;
		private TextView time;
		private TextView content;
		private ImageView weather;
		private ImageView typeView;
		private ImageView alarm;
		private ImageView share;
		private ImageView important;
		private int type;
//		private View dateLayout;
		
		private TextView title_date;
		private TextView title_week;
		private TextView title_lunar;
		private TextView title_year_month;
		private View titleLayout;
		private View scheduleinfo;
	}
	
}