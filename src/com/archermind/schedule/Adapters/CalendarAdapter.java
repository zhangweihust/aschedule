package com.archermind.schedule.Adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TimeUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Calendar.CalendarData;
import com.archermind.schedule.Calendar.LunarCalendar;
import com.archermind.schedule.Utils.DateTimeUtils;

/**
 * 日历gridview中的每一个item显示的textview
 * 
 * @author jack_peng
 * 
 */
public class CalendarAdapter extends BaseAdapter {

	private Context context;
	private String[] dayNumber = new String[42]; // 一个gridview中的日期存入此数组中
	// private Resources res = null;
	// private Drawable drawable = null;
	//
	// private int[] schDateTagFlag = null; //存储当月所有的日程日期
	//
	private int height;
	private int old_position = -1;

	private int height1;
	private int height2;
	private CalendarData calendarData;
	private String currentDay;
	private String selectedDay;
	private boolean isCurrentMonth = false;
	public CalendarAdapter() {
		// Date date = new Date();
		// sysDate = sdf.format(date); //当前日期
		// sys_year = sysDate.split("-")[0];
		// sys_month = sysDate.split("-")[1];
		// sys_day = sysDate.split("-")[2];

	}

	public CalendarAdapter(Context context, int height,
			CalendarData calendarData) {

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
		String currentDate = sdf.format(date); // 当前日期
		currentDay = currentDate.split("-")[2];
		selectedDay = currentDay;
		this.context = context;
		this.height = height;
		this.calendarData = calendarData;
		this.dayNumber = calendarData.getDayNumber();
		// this.schDateTagFlag = calendarData.getSchDateTagFlag();
		this.isCurrentMonth = String.valueOf(date.getYear()+1900).equals(
				calendarData.getShowYear())
				&& String.valueOf(date.getMonth()+1).equals(
						calendarData.getShowMonth()); 

		if (height % 6 == 0) {
			height1 = height2 = height / 6;
		} else {
			height1 = height / 6;
			height2 = height / 6 + height % 6;
		}

	}
	
	public CalendarAdapter(Context context, int height,
			CalendarData calendarData, String selDay) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
		String currentDate = sdf.format(date); // 当前日期
		currentDay = currentDate.split("-")[2];
		
		ScheduleApplication.LogD(getClass(), "selectDay ="+selDay);
		long time = DateTimeUtils.time2Long("yyyy.MM.dd", selDay);
		ScheduleApplication.LogD(getClass(), "time ="+time);
 		String selday = DateTimeUtils.time2String("d",time ) ;
 		ScheduleApplication.LogD(getClass(), "currenday ="+selday);
 		selectedDay = selday;
		this.context = context;
		this.height = height;
		this.calendarData = calendarData;
		this.dayNumber = calendarData.getDayNumber();
		// this.schDateTagFlag = calendarData.getSchDateTagFlag();
		
		this.isCurrentMonth = String.valueOf(date.getYear()+1900).equals(
				calendarData.getShowYear())
				&& String.valueOf(date.getMonth()+1).equals(
						calendarData.getShowMonth()); 

		if (height % 6 == 0) {
			height1 = height2 = height / 6;
		} else {
			height1 = height / 6;
			height2 = height / 6 + height % 6;
		}

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return dayNumber.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.calendar, null);
		}
		RelativeLayout layout = (RelativeLayout) convertView
				.findViewById(R.id.calendar_layout);
		TextView calendar_number = (TextView) convertView
				.findViewById(R.id.calendar_number);
		if (position < 7) {
			calendar_number.setHeight(height2);
		} else {
			calendar_number.setHeight(height1);
		}
		TextView calendar_schedule_number = (TextView) convertView
				.findViewById(R.id.calendar_schedule_number);
		ImageView holiday = (ImageView) convertView
				.findViewById(R.id.calendar_holiday);
		String d = dayNumber[position].split("\\.")[0];
		String dv = dayNumber[position].split("\\.")[1];
		String temp = dv;
		int length = dayNumber[position].length();
		if (dv.contains(LunarCalendar.suffix)) {
			length -= 1;
			dv = dv.substring(0, dv.indexOf(LunarCalendar.suffix));
			holiday.setImageResource(R.drawable.other_holiday);
		}
		// Typeface typeface = Typeface.createFromAsset(context.getAssets(),
		// "fonts/Helvetica.ttf");
		// textView.setTypeface(typeface);
		SpannableString sp = new SpannableString(d + "\n" + dv);
		sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0,
				d.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		sp.setSpan(new RelativeSizeSpan(1.2f), 0, d.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (dv != null || dv != "") {
			sp.setSpan(new RelativeSizeSpan(0.75f), d.length() + 1, /*
																	 * dayNumber[
																	 * position
																	 * ].
																	 * length()
																	 */length,
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		// sp.setSpan(new ForegroundColorSpan(Color.MAGENTA), 14, 16,
		// Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
		calendar_number.setText(sp);
		calendar_number.setTextColor(Color.GRAY);

		if (position < calendarData.getDaysOfMonth()
				+ calendarData.getDayOfWeek() + calendarData.getDaysOfWeek()
				&& position >= calendarData.getDayOfWeek()
						+ calendarData.getDaysOfWeek()) {
			if (temp.contains(LunarCalendar.suffix)) {
				holiday.setImageResource(R.drawable.current_holiday);
			}
			// 当前月信息显示
			calendar_number.setTextColor(Color.BLACK);// 当月字体设黑

			if (calendarData.getMarkcount()[Integer.parseInt(d)] > 0) {
				// 设置日程标记背景
				calendar_schedule_number.setVisibility(View.VISIBLE);
				calendar_schedule_number
						.setText(calendarData.getMarkcount()[Integer
								.parseInt(d)] + "");
				calendar_schedule_number
						.setBackgroundResource(R.drawable.calendar_schedule_number_bg);
				holiday.setImageDrawable(null);
			}
			if (d.equals(currentDay) && isCurrentMonth) {
				// 设置当天的背景
				calendar_number.setTextColor(context.getResources().getColor(
						R.color.current_day));
			}
			
			if (d.equals(selectedDay)) {
				layout.setBackgroundColor(context.getResources().getColor(
						R.color.selector));
				setOldPosition(position);
			}
		}

		return convertView;
	}
	public long getMillisTimeByDate(String date) {
		long time = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd");
		try {
			Date d = sdf.parse(date);
			time = d.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return time;
	}

	/**
	 * 点击每一个item时返回item中的日期
	 * 
	 * @param position
	 * @return
	 */
	public String getDateByClickItem(int position) {
		return dayNumber[position];
	}

	/**
	 * 在点击gridView时，得到这个月中第一天的位置
	 * 
	 * @return
	 */
	public int getStartPositon() {
		return calendarData.getDayOfWeek() + calendarData.getDaysOfWeek();
	}

	/**
	 * 在点击gridView时，得到这个月中最后一天的位置
	 * 
	 * @return
	 */
	public int getEndPosition() {
		return (calendarData.getDayOfWeek() + calendarData.getDaysOfMonth() + calendarData
				.getDaysOfWeek()) - 1;
	}

	public void setOldPosition(int old_position) {
		this.old_position = old_position;
	}
	public int getOldposition() {
		return this.old_position;
	}
}
