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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.archermind.schedule.R;
import com.archermind.schedule.Calendar.CalendarData;
import com.archermind.schedule.Calendar.LunarCalendar;

/**
 * 日历gridview中的每一个item显示的textview
 * @author jack_peng
 *
 */
public class CalendarAdapter extends BaseAdapter {

	private Context context;
	private String[] dayNumber = new String[42];  //一个gridview中的日期存入此数组中
//	private Resources res = null;
//	private Drawable drawable = null;
//	
	private int[] schDateTagFlag = null;  //存储当月所有的日程日期
//	
	private int height;
	private int old_position = -1;
	
	private int height1;
	private int height2;
	private CalendarData calendarData;
	public CalendarAdapter(){
//		Date date = new Date();
//		sysDate = sdf.format(date);  //当前日期
//		sys_year = sysDate.split("-")[0];
//		sys_month = sysDate.split("-")[1];
//		sys_day = sysDate.split("-")[2];
		
	}
	
	public CalendarAdapter(Context context, int height, CalendarData calendarData){
		this.context= context;
		this.height = height;
		this.calendarData = calendarData;
		this.dayNumber = calendarData.getDayNumber();
		this.schDateTagFlag = calendarData.getSchDateTagFlag();
		
		if(height % 6 == 0){
			height1 = height2 = height / 6;
		}else{
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

		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.calendar, null);
		 }
		RelativeLayout layout = (RelativeLayout) convertView.findViewById(R.id.calendar_layout);
		TextView calendar_number = (TextView) convertView.findViewById(R.id.calendar_number);
//		calendar_number.setTextSize(height1/4);
		if(position < 35){
			calendar_number.setHeight(height1);
		}else{
			calendar_number.setHeight(height2);
		}
		TextView calendar_schedule_number = (TextView) convertView.findViewById(R.id.calendar_schedule_number);
		ImageView holiday = (ImageView) convertView.findViewById(R.id.calendar_holiday);
		String d = dayNumber[position].split("\\.")[0];
		String dv = dayNumber[position].split("\\.")[1];
		String temp = dv;
		int length = dayNumber[position].length();
		if(dv.contains(LunarCalendar.suffix)){
			length -= 1;
			dv = dv.substring(0, dv.indexOf(LunarCalendar.suffix));
			holiday.setImageResource(R.drawable.other_holiday);
		}
		//Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica.ttf");
		//textView.setTypeface(typeface);
		SpannableString sp = new SpannableString(d+"\n"+dv);
		sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, d.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		sp.setSpan(new RelativeSizeSpan(1.2f) , 0, d.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		if(dv != null || dv != ""){
            sp.setSpan(new RelativeSizeSpan(0.75f), d.length()+1, /*dayNumber[position].length()*/length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		//sp.setSpan(new ForegroundColorSpan(Color.MAGENTA), 14, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
		calendar_number.setText(sp);
		calendar_number.setTextColor(Color.GRAY);
		
//		if(dv.contains(LunarCalendar.suffix)){
//			holiday.setImageResource(R.drawable.other_holiday);
//		}
		
		if (position < calendarData.getDaysOfMonth() + calendarData.getDayOfWeek()+calendarData.getDaysOfWeek() && position >= calendarData.getDayOfWeek()+calendarData.getDaysOfWeek()) {
			if(temp.contains(LunarCalendar.suffix)){
				holiday.setImageResource(R.drawable.current_holiday);
			}
			// 当前月信息显示
			calendar_number.setTextColor(Color.BLACK);// 当月字体设黑
//			drawable = res.getDrawable(R.drawable.item);
			//textView.setBackgroundDrawable(drawable);
			//textView.setBackgroundColor(Color.WHITE);

		}
		if(schDateTagFlag != null && schDateTagFlag.length >0){
			for(int i = 0; i < schDateTagFlag.length; i++){
				if(schDateTagFlag[i] == position){
					//设置日程标记背景
					calendar_schedule_number.setVisibility(View.VISIBLE);
					calendar_schedule_number.setText(calendarData.getMarkcount()+"");
					calendar_schedule_number.setBackgroundResource(R.drawable.calendar_schedule_number_bg);
				}
			}
		}
		if(calendarData.getCurrentFlag() == position){ 
			//设置当天的背景
//			calendar_number.setBackgroundDrawable(drawable);
			layout.setBackgroundResource(R.drawable.current_day_bg);
			calendar_number.setTextColor(Color.WHITE);
			setOldPosition(position);
		}
		return convertView;
	}
	public long getMillisTimeByDate(String date)
	{
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
	 * @param position
	 * @return
	 */
	public String getDateByClickItem(int position){
		return dayNumber[position];
	}
	
	/**
	 * 在点击gridView时，得到这个月中第一天的位置
	 * @return
	 */
	public int getStartPositon(){
		return calendarData.getDayOfWeek()+calendarData.getDaysOfWeek();
	}
	
	/**
	 *  在点击gridView时，得到这个月中最后一天的位置
	 * @return
	 */
	public int getEndPosition(){
		return  (calendarData.getDayOfWeek()+calendarData.getDaysOfMonth()+calendarData.getDaysOfWeek())-1;
	}
	
	public void setOldPosition(int old_position){
		this.old_position = old_position;
	}
	public int getOldposition(){
		return this.old_position;
	}
}


