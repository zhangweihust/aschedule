package com.archermind.schedule.Calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.database.Cursor;
import android.util.Log;

import com.archermind.schedule.Calendar.LunarCalendar;
import com.archermind.schedule.Calendar.SpecialCalendar;
import com.archermind.schedule.Model.ScheduleBean;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Services.ServiceManager;

public class CalendarData {
	private DatabaseManager database;
	private boolean isLeapyear = false;  //是否为闰年
	private int daysOfMonth = 0;      //某月的天数
	private int dayOfWeek = 0;        //具体某一天是星期几
	private int lastDaysOfMonth = 0;  //上一个月的总天数
	private int daysOfWeek = 0;
	private String[] dayNumber = new String[42];  //一个gridview中的日期存入此数组中
	private static String[] week = {"周日","周一","周二","周三","周四","周五","周六"};
	private SpecialCalendar sc = null;
	private LunarCalendar lc = null; 
	
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
	private int mark_count[];
	public CalendarData(int jumpMonth,int jumpYear,int year_c,int month_c,int day_c, int mark,int flagType){
		database = ServiceManager.getDbManager();
		Date date = new Date();
		sysDate = sdf.format(date);  //当前日期
		sys_year = sysDate.split("-")[0];
		sys_month = sysDate.split("-")[1];
		sys_day = sysDate.split("-")[2];
		sc = new SpecialCalendar();
		lc = new LunarCalendar();
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
			}
		}else if(mark == 0){
			if(stepMonth > 0){
				//往下一个月滑动
				if(stepMonth%12 == 0){
					stepMonth = 12;
				}else{
					stepMonth = stepMonth%12;
				}
			}else{
				stepMonth = stepMonth%12 + 12;
			}
		}
	
		currentYear = String.valueOf(stepYear);;  //得到当前的年份
		currentMonth = String.valueOf(stepMonth);  //得到本月（jumpMonth为滑动的次数，每滑动一次就增加一月或减一月）
		currentDay = String.valueOf(day_c);  //得到当前日期是哪天
		
		System.out.println("currentYear = " + currentYear);
		getCalendar(Integer.parseInt(currentYear),Integer.parseInt(currentMonth), flagType);
		
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
		Cursor cursor = null;
		int days = SpecialCalendar.getDaysOfMonth(SpecialCalendar.isLeapYear(year),month);
		schDateTagFlag = new int[days];
		mark_count = new int[days];
		for(int i = 0; i < days; i++){
			schDateTagFlag[i] = -1;
		}
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
				
				
				
				
				
				
				
				
				String startData = year+"."+month+"."+day;
				String dayOfYear = month + "." + day;
				String dayOfMonth = day;
				String dayOfWeek = SpecialCalendar.getNumberWeekDay(year,month,Integer.parseInt(day));
				
				System.out.println("*********************dayOfWeek = "+dayOfWeek);
				
				long starTimeInMillis = getMillisTimeByDate(startData);
				ArrayList<ScheduleBean> dateTagList = new ArrayList<ScheduleBean>();
				cursor = database.queryTodayLocalSchedules(starTimeInMillis);

				while(cursor.moveToNext()){
					String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME));
					ScheduleBean scheduleBean = new ScheduleBean();
					scheduleBean.setDate(date);
					dateTagList.add(scheduleBean);
				}
				cursor = database.queryIsMarkWithDay(starTimeInMillis, dayOfYear, dayOfMonth, dayOfWeek);
				while(cursor.moveToNext()){
						ScheduleBean scheduleBean = new ScheduleBean();
						scheduleBean.setDate(String.valueOf(starTimeInMillis));
						dateTagList.add(scheduleBean);
				}
				

				if(dateTagList != null && dateTagList.size() > 0){

				}
				
				
				//对于当前月才去标记当前日期
				if(sys_year.equals(String.valueOf(year)) && sys_month.equals(String.valueOf(month)) && sys_day.equals(day)){
					//笔记当前日期
					currentFlag = i;
				}
				
				//标记日程日期
				if(dateTagList != null && dateTagList.size() > 0){
//					for(int m = 0; m < dateTagList.size(); m++){
//						ScheduleBean scheduleBean = dateTagList.get(m);
//						String date = scheduleBean.getDate();
//						Calendar time = Calendar.getInstance(Locale.CHINA);
//						time.setTimeInMillis(Long.parseLong(date));
//						int matchYear = time.get(Calendar.YEAR);
//						int matchMonth = time.get(Calendar.MONTH) + 1;
//						int matchDay = time.get(Calendar.DAY_OF_MONTH);
//						if(matchYear == year && matchMonth == month && matchDay == Integer.parseInt(day)){
//							schDateTagFlag[flag] = i;
//							mark_count[flag] = dateTagList.size();
//							flag++;
//						}
//					}
					schDateTagFlag[flag] = i;
					mark_count[flag] = dateTagList.size();
					flag++;
				}
				
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
		
		if(cursor != null){
			cursor.close();
		}
        
        String abc = "";
        for(int i = 0; i < dayNumber.length; i++){
        	 abc = abc+dayNumber[i]+":";
        }
        Log.d("DAYNUMBER",abc);


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
	
	public int getDaysOfMonth(){
		return this.daysOfMonth;
	}
	public int getDaysOfWeek(){
		return this.daysOfWeek;
	}
	public int getDayOfWeek(){
		return this.dayOfWeek;
	}
	public int[] getMarkcount(){
		return this.mark_count;
	}
	public int getCurrentFlag(){
		return this.currentFlag;
	}
	public String[] getDayNumber(){
		return this.dayNumber;
	}
	public int[] getSchDateTagFlag(){
		return this.schDateTagFlag;
	}
	
	
}
