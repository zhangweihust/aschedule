package com.archermind.schedule.Screens;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.archermind.schedule.R;
import com.archermind.schedule.Adapters.CalendarAdapter;
import com.archermind.schedule.Adapters.LocalScheduleAdapter;
import com.archermind.schedule.Adapters.HistoryScheduleAdapter;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.IEventHandler;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.ScheduleData;
import com.archermind.schedule.Views.VerticalScrollView;
import com.archermind.schedule.Views.XListView;
import com.archermind.schedule.Views.XListView.IXListViewListener;

public class ScheduleScreen extends Screen implements IXListViewListener,
		OnItemClickListener ,OnGestureListener, OnClickListener, IEventHandler{

	private static final int FRESH_LIMIT_NUM = 30;
	
	private ImageView mListHeader;
//	private TextView tv1;
//	private TextView tv2;
//	private TextView tv3;
//	private TextView tv4;

//	private FrameLayout layout1;
	private FrameLayout layout2;
//	private FrameLayout layout3;
//	private FrameLayout layout4;

//	private ListView list1;
	private XListView list2;
//	private ListView list3;
//	private ListView list4;
	
//	private Cursor c1;
	private Cursor TodayScheduleCursor;
//	private Cursor c3;
//	private Cursor c4;
	
	private HistoryScheduleAdapter hsa;
	private List<ScheduleData> listdata = new ArrayList<ScheduleData>();
	private String curSelectedDate = "";
	

private ViewFlipper flipper = null;
		private GestureDetector gestureDetector = null;
		private CalendarAdapter calV = null;
		private GridView gridView = null;
		private TextView current_date = null;
		private Drawable draw = null;
		public static int jumpMonth = 0;      //每次滑动，增加或减去一个月，默认为0（即显示当前月）
		public static int jumpYear = 0;       //滑动跨越一年，则增加或者减去一年，默认为0（即当前年）
		public static int jumpDay = 0; 
		private int year_c = 0;
		private int month_c = 0;
		private int day_c = 0;
		private String currentDate = "";
		
		private Button previous_year, next_year;
		private Button current_day;
		private EventArgs args;
		private boolean flag = false;
		
		public ScheduleScreen() {
			Date date = new Date();
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
	    	currentDate = sdf.format(date);  //当前日期
	    	Constant.YEAR = year_c = Integer.parseInt(currentDate.split("-")[0]);
	    	Constant.MONTH = month_c = Integer.parseInt(currentDate.split("-")[1]);
	    	Constant.DAY = day_c = Integer.parseInt(currentDate.split("-")[2]);
		}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_screen);
		final VerticalScrollView pager = (VerticalScrollView) findViewById(R.id.pager);
		setupView();
		flag = true;
		eventService.add(this);
		mListHeader = (ImageView) findViewById(R.id.list_header);
		mListHeader.setBackgroundResource(R.drawable.listview_header_up);
		mListHeader.setTag(R.drawable.listview_header_up);
		mListHeader.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Integer tag = (Integer) mListHeader.getTag();
				if (tag != null) {
					if (tag.intValue() == R.drawable.listview_header_up) {
						pager.snapToPage(1);
					} else if (tag.intValue() == R.drawable.listview_header_down) {
						pager.snapToPage(0);
					}
				}
			}
		});
		pager.addOnScrollListener(new VerticalScrollView.OnScrollListener() {
			public void onScroll(int scrollX) {
			}

			public void onViewScrollFinished(int currentPage) {
			}

			@Override
			public void snapToPage(int whichPage) {
				if (whichPage == 1) {
					mListHeader
							.setBackgroundResource(R.drawable.listview_header_down);
					mListHeader.setTag(R.drawable.listview_header_down);
//					tv2.setTextColor(Color.parseColor("#4f810f"));
//					list2.setHeaderGone(true);
//					tv3.setVisibility(View.VISIBLE);
//					tv4.setVisibility(View.VISIBLE);
				} else {
					mListHeader
							.setBackgroundResource(R.drawable.listview_header_up);
					mListHeader.setTag(R.drawable.listview_header_up);
//					tv1.setVisibility(View.GONE);
//					tv2.setVisibility(View.GONE);
//					tv3.setVisibility(View.GONE);
//					tv4.setVisibility(View.GONE);
//					layout1.setVisibility(View.GONE);
					layout2.setVisibility(View.VISIBLE);
//					layout3.setVisibility(View.GONE);
//					layout4.setVisibility(View.GONE);
				}
			}
		});
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			HomeScreen.switchActivity();
		}
		return super.onKeyUp(keyCode, event);
	}
	
	private void setupView() {
//		tv1 = (TextView) findViewById(R.id.tv01);
//		tv2 = (TextView) findViewById(R.id.tv02);
//		tv3 = (TextView) findViewById(R.id.tv03);
//		tv4 = (TextView) findViewById(R.id.tv04);

//		layout1 = (FrameLayout) findViewById(R.id.layout1);
		layout2 = (FrameLayout) findViewById(R.id.layout2);
//		layout3 = (FrameLayout) findViewById(R.id.layout3);
//		layout4 = (FrameLayout) findViewById(R.id.layout4);

//		list1 = (ListView) findViewById(R.id.list01);
		list2 = (XListView) findViewById(R.id.list02);
//		list3 = (ListView) findViewById(R.id.list03);
//		list4 = (ListView) findViewById(R.id.list04);
		list2.setXListViewListener(this);
//		list1.setOnItemClickListener(this);
		list2.setOnItemClickListener(this);
//		list3.setOnItemClickListener(this);
//		list4.setOnItemClickListener(this);

//		c1 = ServiceManager.getDbManager().query3DaysBeforeLocalSchedules(
//				System.currentTimeMillis());
		
		TodayScheduleCursor = ServiceManager.getDbManager().queryTodayLocalSchedules(
				System.currentTimeMillis());
		
//		c3 = ServiceManager.getDbManager().queryTomorrowLocalSchedules(
//				System.currentTimeMillis());
//		
//		c4 = ServiceManager.getDbManager().queryWeekLocalSchedules(
//				System.currentTimeMillis());
		
//		list1.setAdapter(new LocalScheduleAdapter(this, c1));
		ScheduleData data;
		while (TodayScheduleCursor.moveToNext())
		{
			data = new ScheduleData();
			data.content = TodayScheduleCursor.getString(TodayScheduleCursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT));
			data.time = TodayScheduleCursor.getLong(TodayScheduleCursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME));
			data.share = TodayScheduleCursor.getInt(TodayScheduleCursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_SHARE)) == 1;
			data.important = TodayScheduleCursor.getInt(TodayScheduleCursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_IMPORTANT)) == 1;
			data.type = TodayScheduleCursor.getInt(TodayScheduleCursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_TYPE));
			data.first = TodayScheduleCursor.getInt(TodayScheduleCursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_FIRST_FLAG)) == 1;
			
			listdata.add(data);
		}
		TodayScheduleCursor.close();
		hsa = new HistoryScheduleAdapter(this, listdata);
		list2.setAdapter(hsa);
//		list3.setAdapter(new LocalScheduleAdapter(this, c3));
//		list4.setAdapter(new LocalScheduleAdapter(this, c4));

//		layout1.setVisibility(View.GONE);
		layout2.setVisibility(View.VISIBLE);
//		layout3.setVisibility(View.GONE);
//		layout4.setVisibility(View.GONE);

//		tv1.setVisibility(View.GONE);
//		tv2.setVisibility(View.GONE);
//		tv3.setVisibility(View.GONE);
//		tv4.setVisibility(View.GONE);

		
		gestureDetector = new GestureDetector(this);
        flipper = (ViewFlipper) findViewById(R.id.flipper);
        flipper.removeAllViews();
        System.out.println("height = "+flipper.getHeight());
       
        
       
//		tv1.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				tv1.setTextColor(Color.parseColor("#4f810f"));
//				tv2.setTextColor(Color.parseColor("#252524"));
//				tv3.setTextColor(Color.parseColor("#252524"));
//				tv4.setTextColor(Color.parseColor("#252524"));
//				layout1.setVisibility(View.VISIBLE);
//				layout2.setVisibility(View.GONE);
//				layout3.setVisibility(View.GONE);
//				layout4.setVisibility(View.GONE);
//			}
//		});

//		tv2.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				tv2.setTextColor(Color.parseColor("#4f810f"));
////				tv1.setTextColor(Color.parseColor("#252524"));
//				tv3.setTextColor(Color.parseColor("#252524"));
//				tv4.setTextColor(Color.parseColor("#252524"));
////				layout1.setVisibility(View.GONE);
//				layout2.setVisibility(View.VISIBLE);
//				layout3.setVisibility(View.GONE);
//				layout4.setVisibility(View.GONE);
//			}
//		});

//		tv3.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				tv3.setTextColor(Color.parseColor("#4f810f"));
////				tv1.setTextColor(Color.parseColor("#252524"));
////				tv2.setTextColor(Color.parseColor("#252524"));
//				tv4.setTextColor(Color.parseColor("#252524"));
////				layout1.setVisibility(View.GONE);
//				layout2.setVisibility(View.GONE);
//				layout3.setVisibility(View.VISIBLE);
//				layout4.setVisibility(View.GONE);
////				if (!tv2.isShown()) {
////					tv2.setVisibility(View.VISIBLE);
////				}
//			}
//		});
//
//		tv4.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				tv4.setTextColor(Color.parseColor("#4f810f"));
////				tv1.setTextColor(Color.parseColor("#252524"));
////				tv2.setTextColor(Color.parseColor("#252524"));
//				tv3.setTextColor(Color.parseColor("#252524"));
////				layout1.setVisibility(View.GONE);
//				layout2.setVisibility(View.GONE);
//				layout3.setVisibility(View.GONE);
//				layout4.setVisibility(View.VISIBLE);
////				if (!tv2.isShown()) {
////					tv2.setVisibility(View.VISIBLE);
////				}
//			}
//		});
	}

@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(flag){
			flag = false;
			 calV = new CalendarAdapter(this, getResources(),jumpMonth,jumpYear,year_c,month_c,day_c,1,flipper.getHeight(), Constant.flagType);
		        
		        addGridView();
		        gridView.setAdapter(calV);
		        //flipper.addView(gridView);
		        flipper.addView(gridView,0);
		        current_date = (TextView) findViewById(R.id.current_date);
				addTextToTopTextView(current_date);
			
				previous_year = (Button) findViewById(R.id.previous_year);
				next_year = (Button) findViewById(R.id.next_year);
				current_day = (Button) findViewById(R.id.current_day);
				
				previous_year.setOnClickListener(this);
				next_year.setOnClickListener(this);
				current_day.setOnClickListener(this);
		}
	}

	public void cursorToListData(Cursor c,List<ScheduleData> listdata)
	{
		ScheduleData data;
		
		if (c == null)
		{
			return;
		}
		
		while (c.moveToNext())
		{
			data = new ScheduleData();
			data.id = c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_ID));
			data.content = c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT));
			data.time = c.getLong(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME));
			data.share = c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_SHARE)) == 1;
			data.important = c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_IMPORTANT)) == 1;
			data.type = c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_TYPE));
			data.first = c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_FIRST_FLAG)) == 1;
			
			listdata.add(data);
		}
	}

	@Override
	public void onRefresh() {
//		Toast.makeText(ScheduleScreen.this, "up", Toast.LENGTH_SHORT).show();
//		tv1.setVisibility(View.VISIBLE);
//		tv2.setVisibility(View.VISIBLE);
		
		Cursor c;
		if (hsa.isEmpty())
		{
			c = ServiceManager.getDbManager().querySpecifiedNumPreSchedules(
					getMillisTimeByDate(curSelectedDate),FRESH_LIMIT_NUM);
		}
		else
		{
			c = ServiceManager.getDbManager().querySpecifiedNumPreSchedules(
					hsa.getEarliestTime(),FRESH_LIMIT_NUM);
		}
		List<ScheduleData> listdata = new ArrayList<ScheduleData>();
		cursorToListData(c,listdata);
		Collections.reverse(listdata);		/* 数据库查询时按照降序排列，因此此处需要将listdata中数据倒序 */
		c.close();
		hsa.addPreData(listdata);
		
		onLoad();
//		list2.setHeaderGone(false);
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		Cursor c;
		if (hsa.isEmpty())
		{
			c = ServiceManager.getDbManager().querySpecifiedNumAftSchedules(
					getMillisTimeByDate(curSelectedDate),FRESH_LIMIT_NUM);
		}
		else
		{
			c = ServiceManager.getDbManager().querySpecifiedNumAftSchedules(
					hsa.getlatestTime(),FRESH_LIMIT_NUM);
		}
		List<ScheduleData> listdata = new ArrayList<ScheduleData>();
		cursorToListData(c,listdata);
		c.close();
		hsa.addAfterData(listdata);
		
		onLoad();
//		list2.setHeaderGone(false);
	}

	private void onLoad() {
		list2.stopRefresh();
		list2.stopLoadMore();
		list2.setRefreshTime("刚刚");
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		args = (EventArgs) view.getTag();
		/*if (parent.getId() == list1.getId()) {
			Toast.makeText(
					ScheduleScreen.this,
					"list1=" + id + "  tag = "
							+ (Boolean) args.getExtra("first") + "  time = "
							+ (Long) args.getExtra("time"), Toast.LENGTH_SHORT)
					.show();
		} else */if (parent.getId() == list2.getId()) {
//			Toast.makeText(
//					ScheduleScreen.this,
//					"list2=" + id + "  tag = "
//							+ (Boolean) args.getExtra("first") + "  time = "
//							+ (Long) args.getExtra("time"), Toast.LENGTH_SHORT)
//					.show();
		}/* else if (parent.getId() == list3.getId()) {
			Toast.makeText(
					ScheduleScreen.this,
					"list3=" + id + "  tag = "
							+ (Boolean) args.getExtra("first") + "  time = "
							+ (Long) args.getExtra("time"), Toast.LENGTH_SHORT)
					.show();
		} else if (parent.getId() == list4.getId()) {
			Toast.makeText(
					ScheduleScreen.this,
					"list4=" + id + "  tag = "
							+ (Boolean) args.getExtra("first") + "  time = "
							+ (Long) args.getExtra("time"), Toast.LENGTH_SHORT)
					.show();
		}*/
		Intent mIntent =new Intent(ScheduleScreen.this,EditScheduleScreen.class);
		mIntent.putExtra("id", (Integer) args.getExtra("id"));
		mIntent.putExtra("first", (Boolean) args.getExtra("first"));
		mIntent.putExtra("time", (Long) args.getExtra("time"));
		ScheduleScreen.this.startActivity(mIntent);
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		System.out.println("e1.getX() - e2.getX() = "+ (e1.getX() - e2.getX()));
		System.out.println("velocityX = "+ velocityX + "     velocityY = " + velocityY);
		int gvFlag = 0;         //每次添加gridview到viewflipper中时给的标记
		if (e1.getX() - e2.getX() > 0) {
            //向左滑动
			addGridView();   //添加一个gridview
			jumpMonth++;     //下一个月
			
			calV = new CalendarAdapter(this, getResources(),jumpMonth,jumpYear,year_c,month_c,day_c,1,flipper.getHeight(),Constant.flagType);
	        gridView.setAdapter(calV);
	        //flipper.addView(gridView);
	        addTextToTopTextView(current_date);
	        gvFlag++;
	        flipper.addView(gridView, gvFlag);
			this.flipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.push_left_in));
			this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.push_left_out));
			this.flipper.showNext();
			flipper.removeViewAt(0);
			return true;
		} else if (e1.getX() - e2.getX() < 0) {
            //向右滑动
			addGridView();   //添加一个gridview
			jumpMonth--;     //上一个月
			
			calV = new CalendarAdapter(this, getResources(),jumpMonth,jumpYear,year_c,month_c,day_c,1,flipper.getHeight(),Constant.flagType);
	        gridView.setAdapter(calV);
	        gvFlag++;
	        addTextToTopTextView(current_date);
	        //flipper.addView(gridView);
	        flipper.addView(gridView,gvFlag);
	        
			this.flipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.push_right_in));
			this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.push_right_out));
			this.flipper.showPrevious();
			flipper.removeViewAt(0);
			return true;
		}
		return false;
	}
	

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	
	//添加头部的年份  闰哪个月等信息
	public void addTextToTopTextView(TextView view){
//		StringBuffer textDate = new StringBuffer();
//		draw = getResources().getDrawable(R.drawable.top_day);
//		view.setBackgroundDrawable(draw);
//		textDate.append(calV.getShowYear()).append("年").append(
//				calV.getShowMonth()).append("月").append("\t");
//		if (!calV.getLeapMonth().equals("") && calV.getLeapMonth() != null) {
//			textDate.append("闰").append(calV.getLeapMonth()).append("月")
//					.append("\t");
//		}
//		textDate.append(calV.getAnimalsYear()).append("年").append("(").append(
//				calV.getCyclical()).append("年)");
//		view.setText(textDate);
		if(Integer.parseInt(calV.getShowMonth()) < 10){
			view.setText(calV.getShowYear()+".0"+calV.getShowMonth());
		}else{
			view.setText(calV.getShowYear()+"."+calV.getShowMonth());
		}
		view.setTextColor(Color.BLACK);
		view.setTypeface(Typeface.DEFAULT_BOLD);
	}
	
	public String getDate(int position){
		String date = "";
		int month = Integer.parseInt(calV.getShowMonth());
		int day = Integer.parseInt(calV.getDateByClickItem(position).split("\\.")[0]);
		
		if(month < 10){
			date = calV.getShowYear() + ".0" + month;
		}else{
			date = calV.getShowYear() + "." + month;
		}
		if (day < 10){
			date += ".0" + day;
		}
		else{
			date += "." + day;
		}
		return date;
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
	
	//添加gridview
	private void addGridView() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		//取得屏幕的宽度和高度
		WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int Width = display.getWidth(); 
        int Height = display.getHeight();
        
		gridView = new GridView(this);
		gridView.setNumColumns(7);
		gridView.setColumnWidth(Width/7 + 1);
		
//		if(Width == 480 && Height == 800){
//			gridView.setColumnWidth(69);
//		}
      gridView.setSelector(new ColorDrawable(Color.TRANSPARENT)); 
		gridView.setGravity(Gravity.CENTER);
		gridView.setOnTouchListener(new OnTouchListener() {
            //将gridview中的触摸时间回传给gestureDetector
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return ScheduleScreen.this.gestureDetector
						.onTouchEvent(event);
			}
		});

		
		gridView.setOnItemClickListener(new OnItemClickListener() {
            //gridView中的每一个item的点击事件
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				  //点击任何一个item，得到这个item的日期（排除点击的是周日到周六（点击不响应））
				  int startPosition = calV.getStartPositon();
				  int endPosition = calV.getEndPosition();
				  if(startPosition <= position  && position <= endPosition){
					  String scheduleDay = calV.getDateByClickItem(position).split("\\.")[0];  //这一天的阳历
					  //String scheduleLunarDay = calV.getDateByClickItem(position).split("\\.")[1];  //这一天的阴历
	                  String scheduleYear = calV.getShowYear();
	                  String scheduleMonth = calV.getShowMonth();
	                  String week = "";
	                  
                  int index = calV.getOldposition();
	                  System.out.println("index = "+index);
	                  if(index == -1){
	                	  ((RelativeLayout)view).setBackgroundResource(R.drawable.current_day_bg);
	                	  calV.setOldPosition(position);
	                  }else{
	                	  RelativeLayout layout = (RelativeLayout) arg0.getChildAt(index);
	                	  if(layout != null){
	                		  layout.setBackgroundResource(R.drawable.gridview_bk);  
//	                		  layout.setBackgroundDrawable(null);
	                	  }
		                  ((RelativeLayout)view).setBackgroundResource(R.drawable.current_day_bg);
		                  System.out.println("position = "+position);
		                  calV.setOldPosition(position);
	                  }
				String date = getDate(position);
				if (!curSelectedDate.equals(date))	
				{
					curSelectedDate = date;

					Cursor c = ServiceManager.getDbManager().queryTodayLocalSchedules(getMillisTimeByDate(curSelectedDate));
					List<ScheduleData> listdata = new ArrayList<ScheduleData>();
					cursorToListData(c,listdata);
					c.close();
					hsa.setTodayData(listdata);

				}
			}
        }
		});
		gridView.setLayoutParams(params);


	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int gvFlag = 0; 
		switch(v.getId()){
		case R.id.previous_year:
            //向左滑动
			addGridView();   //添加一个gridview
			jumpYear--;     //下一年
			
			calV = new CalendarAdapter(this, getResources(),jumpMonth,jumpYear,year_c,month_c,day_c,0,flipper.getHeight(),Constant.flagType);
	        gridView.setAdapter(calV);
	        //flipper.addView(gridView);
	        addTextToTopTextView(current_date);
	        gvFlag++;
	        flipper.addView(gridView, gvFlag);
			this.flipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.push_left_in));
			this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.push_left_out));
			this.flipper.showNext();
			flipper.removeViewAt(0);
			break;
		case R.id.next_year:
			 //向右滑动
			addGridView();   //添加一个gridview
			jumpYear++;     //上一年
			
			calV = new CalendarAdapter(this, getResources(),jumpMonth,jumpYear,year_c,month_c,day_c,0,flipper.getHeight(),Constant.flagType);
	        gridView.setAdapter(calV);
	        gvFlag++;
	        addTextToTopTextView(current_date);
	        //flipper.addView(gridView);
	        flipper.addView(gridView,gvFlag);
	        
			this.flipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.push_right_in));
			this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.push_right_out));
			this.flipper.showPrevious();
			flipper.removeViewAt(0);
			break;
		case R.id.current_day:
			int xMonth = jumpMonth;
        	int xYear = jumpYear;
        	jumpMonth = 0;
        	jumpYear = 0;
        	addGridView();   //添加一个gridview
        	year_c = Integer.parseInt(currentDate.split("-")[0]);
        	month_c = Integer.parseInt(currentDate.split("-")[1]);
        	day_c = Integer.parseInt(currentDate.split("-")[2]);
        	calV = new CalendarAdapter(this, getResources(),jumpMonth,jumpYear,year_c,month_c,day_c,1,flipper.getHeight(),Constant.flagType);
	        gridView.setAdapter(calV);
	        addTextToTopTextView(current_date);
	        gvFlag++;
	        flipper.addView(gridView,gvFlag);
	        if(xMonth == 0 && xYear == 0){
	        	//nothing to do
	        }else if((xYear == 0 && xMonth >0) || xYear >0){
	        	this.flipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.push_left_in));
				this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.push_left_out));
				this.flipper.showNext();
	        }else{
	        	this.flipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.push_right_in));
				this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.push_right_out));
				this.flipper.showPrevious();
	        }
			flipper.removeViewAt(0);
			break;
		}
		
	}
	@Override
	public boolean onEvent(Object sender, EventArgs e) {
		switch(e.getType()){
		case LOCAL_SCHEDULE_UPDATE:
			ScheduleScreen.this.runOnUiThread(new Runnable(){
				@Override
				public void run() {
//				    c1.requery();
					Cursor c = ServiceManager.getDbManager().queryTodayLocalSchedules(System.currentTimeMillis());
					List<ScheduleData> listdata = new ArrayList<ScheduleData>();
					cursorToListData(c,listdata);
					c.close();
					hsa.setTodayData(listdata);
//				    c3.requery();
//				    c4.requery();
				}});
			break;
		}
		return true;
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		eventService.remove(this);
	}	
	
}

