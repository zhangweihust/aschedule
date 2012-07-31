package com.archermind.schedule.Adapters;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.archermind.schedule.R;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.calendar.LunarCalendar;
import com.archermind.schedule.calendar.SpecialCalendar;

/**
 * 日历gridview中的每一个item显示的textview
 * @author jack_peng
 *
 */
public class CalendarAdapter extends BaseAdapter {

	private DatabaseManager database;
	private boolean isLeapyear = false;  //是否为闰年
	private int daysOfMonth = 0;      //某月的天数
	private int dayOfWeek = 0;        //具体某一天是星期几
	private int lastDaysOfMonth = 0;  //上一个月的总天数
	private Context context;
	private int daysOfWeek = 0;
	private String[] dayNumber = new String[42];  //一个gridview中的日期存入此数组中
	private static String[] week = {"周日","周一","周二","周三","周四","周五","周六"};
	private SpecialCalendar sc = null;
	private LunarCalendar lc = null; 
	private Resources res = null;
	private Drawable drawable = null;
	
	private String currentYear = "";
	private String currentMonth = "";
	private String currentDay = "";
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
	private int currentFlag = -1;     //用于标记当天
	private int[] schDateTagFlag = null;  //存储当月所有的日程日期
	
	private String showYear = "";   //用于在头部显示的年份
	private String showMonth = "";  //用于在头部显示的月份
	private String animalsYear = ""; 
	private String leapMonth = "";   //闰哪一个月
	private String cyclical = "";   //天干地支
	//系统当前时间
	private String sysDate = "";  
	private String sys_year = "";
	private String sys_month = "";
	private String sys_day = "";
	
	//日程时间（需要标记的日程日期）
	private String sch_year = "";
	private String sch_month = "";
	private String sch_day = "";
	private int height;
	
	public CalendarAdapter(){
		Date date = new Date();
		sysDate = sdf.format(date);  //当前日期
		sys_year = sysDate.split("-")[0];
		sys_month = sysDate.split("-")[1];
		sys_day = sysDate.split("-")[2];
		
	}
	
	public CalendarAdapter(Context context,Resources rs,int jumpMonth,int jumpYear,int year_c,int month_c,int day_c, int mark, int height, int flagType){
		this();
		this.context= context;
		this.height = height;
		sc = new SpecialCalendar();
		lc = new LunarCalendar();
		this.res = rs;
		System.out.println("jumpYear = " + jumpYear);
		int stepYear = year_c+jumpYear;
		int stepMonth = month_c+jumpMonth ;
		if(mark == 1){
			if(stepMonth > 0){
				//往下一个月滑动
				if(stepMonth%12 == 0){
					stepYear = /*year_c*/stepYear + stepMonth/12 -1;
					stepMonth = 12;
				}else{
					stepYear = /*year_c*/stepYear + stepMonth/12;
					stepMonth = stepMonth%12;
				}
			}else{
				//往上一个月滑动
				stepYear = /*year_c*/stepYear - 1 + stepMonth/12;
				stepMonth = stepMonth%12 + 12;
				if(stepMonth%12 == 0){
					
				}
			}
		}
	
		currentYear = String.valueOf(stepYear);;  //得到当前的年份
		currentMonth = String.valueOf(stepMonth);  //得到本月（jumpMonth为滑动的次数，每滑动一次就增加一月或减一月）
		currentDay = String.valueOf(day_c);  //得到当前日期是哪天
		
		System.out.println("currentYear = " + currentYear);
		getCalendar(Integer.parseInt(currentYear),Integer.parseInt(currentMonth), flagType);
		
	}
	
	public CalendarAdapter(Context context,Resources rs,int year, int month, int day, int flagType){
		this();
		this.context= context;
		sc = new SpecialCalendar();
		lc = new LunarCalendar();
		this.res = rs;
		currentYear = String.valueOf(year);;  //得到跳转到的年份
		currentMonth = String.valueOf(month);  //得到跳转到的月份
		currentDay = String.valueOf(day);  //得到跳转到的月份
		
		getCalendar(Integer.parseInt(currentYear),Integer.parseInt(currentMonth), flagType);
		
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
		TextView calendar_number = (TextView) convertView.findViewById(R.id.calendar_number);
		calendar_number.setHeight(height / 6 + 1);
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
		if(position<daysOfWeek){
			//设置周
			calendar_number.setTextColor(Color.BLACK);
			drawable = res.getDrawable(R.drawable.week_top);
			calendar_number.setBackgroundDrawable(drawable);
		}
		
		if (position < daysOfMonth + dayOfWeek+daysOfWeek && position >= dayOfWeek+daysOfWeek) {
			if(temp.contains(LunarCalendar.suffix)){
				holiday.setImageResource(R.drawable.current_holiday);
			}
			// 当前月信息显示
			calendar_number.setTextColor(Color.BLACK);// 当月字体设黑
			drawable = res.getDrawable(R.drawable.item);
			//textView.setBackgroundDrawable(drawable);
			//textView.setBackgroundColor(Color.WHITE);

		}
		if(schDateTagFlag != null && schDateTagFlag.length >0){
			for(int i = 0; i < schDateTagFlag.length; i++){
				if(schDateTagFlag[i] == position){
					//设置日程标记背景
					calendar_schedule_number.setVisibility(View.VISIBLE);
					calendar_schedule_number.setBackgroundResource(R.drawable.calendar_schedule_number_bg);
				}
			}
		}
		if(currentFlag == position){ 
			//设置当天的背景
			drawable = res.getDrawable(R.drawable.current_day_bg);
			calendar_number.setBackgroundDrawable(drawable);
			calendar_number.setTextColor(Color.WHITE);
		}
		return convertView;
	}
	
	//得到某年的某月的天数且这月的第一天是星期几
	public void getCalendar(int year, int month, int flagType){
		isLeapyear = sc.isLeapYear(year);              //是否为闰年
		daysOfMonth = sc.getDaysOfMonth(isLeapyear, month);  //某月的总天数
		dayOfWeek = sc.getWeekdayOfMonth(year, month);      //某月第一天为星期几
		lastDaysOfMonth = sc.getDaysOfMonth(isLeapyear, month-1);  //上一个月的总天数
		Log.d("DAY", isLeapyear+" ======  "+daysOfMonth+"  ============  "+dayOfWeek+"  =========   "+lastDaysOfMonth);
		getweek(year,month,flagType);
	}
	
	//将一个月中的每一天的值添加入数组dayNuber中
	private void getweek(int year, int month, int flagType) {
		if(flagType == 1){
			week = new String[]{"周一","周二","周三","周四","周五","周六","周日"};
			dayOfWeek -= 1;
			if(dayOfWeek < 0){
				dayOfWeek = 6;
			}
		}
		int j = 1;
		int flag = 0;
		String lunarDay = "";
		//得到当前月的所有日程日期（这些日期需要标记）
//		database = ServiceManager.getDbManager();
//		ArrayList<ScheduleDateTag> dateTagList = database.getTagDate(year,month);
//		if(dateTagList != null && dateTagList.size() > 0){
//			schDateTagFlag = new int[dateTagList.size()];
//		}
		
		for (int i = 0; i < dayNumber.length; i++) {
			int k = 1;
			// 周一
			if(i<daysOfWeek){
				dayNumber[i]=week[i]+"."+" ";
			}
			else if(i < dayOfWeek+daysOfWeek){  //前一个月
				int temp = lastDaysOfMonth - dayOfWeek+k-daysOfWeek;
				lunarDay = lc.getLunarDate(year, month-1, temp+i,false);
				dayNumber[i] = (temp + i)+"."+lunarDay;
			}else if(i < daysOfMonth + dayOfWeek+daysOfWeek){   //本月
				String day = String.valueOf(i-dayOfWeek+k-daysOfWeek);   //得到的日期
				lunarDay = lc.getLunarDate(year, month, i-dayOfWeek+k-daysOfWeek,false);
				dayNumber[i] = i-dayOfWeek+k-daysOfWeek+"."+lunarDay;
				//对于当前月才去标记当前日期
				if(sys_year.equals(String.valueOf(year)) && sys_month.equals(String.valueOf(month)) && sys_day.equals(day)){
					//笔记当前日期
					currentFlag = i;
				}
				
				//标记日程日期
//				if(dateTagList != null && dateTagList.size() > 0){
//					for(int m = 0; m < dateTagList.size(); m++){
//						ScheduleDateTag dateTag = dateTagList.get(m);
//						int matchYear = dateTag.getYear();
//						int matchMonth = dateTag.getMonth();
//						int matchDay = dateTag.getDay();
//						if(matchYear == year && matchMonth == month && matchDay == Integer.parseInt(day)){
//							schDateTagFlag[flag] = i;
//							flag++;
//						}
//					}
//				}
				
				setShowYear(String.valueOf(year));
				setShowMonth(String.valueOf(month));
				setAnimalsYear(lc.animalsYear(year));
				setLeapMonth(lc.leapMonth == 0?"":String.valueOf(lc.leapMonth));
				setCyclical(lc.cyclical(year));
			}else{   //下一个月
				lunarDay = lc.getLunarDate(year, month+1, j,false);
				dayNumber[i] = j+"."+lunarDay;
				j++;
			}
		}
        
        String abc = "";
        for(int i = 0; i < dayNumber.length; i++){
        	 abc = abc+dayNumber[i]+":";
        }
        Log.d("DAYNUMBER",abc);


	}
	
	
	public void matchScheduleDate(int year, int month, int day){
		
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
		return dayOfWeek+daysOfWeek;
	}
	
	/**
	 *  在点击gridView时，得到这个月中最后一天的位置
	 * @return
	 */
	public int getEndPosition(){
		return  (dayOfWeek+daysOfMonth+daysOfWeek)-1;
	}
	
	public String getShowYear() {
		return showYear;
	}

	public void setShowYear(String showYear) {
		this.showYear = showYear;
	}

	public String getShowMonth() {
		return showMonth;
	}

	public void setShowMonth(String showMonth) {
		this.showMonth = showMonth;
	}
	
	public String getAnimalsYear() {
		return animalsYear;
	}

	public void setAnimalsYear(String animalsYear) {
		this.animalsYear = animalsYear;
	}
	
	public String getLeapMonth() {
		return leapMonth;
	}

	public void setLeapMonth(String leapMonth) {
		this.leapMonth = leapMonth;
	}
	
	public String getCyclical() {
		return cyclical;
	}

	public void setCyclical(String cyclical) {
		this.cyclical = cyclical;
	}
}
