package com.archermind.schedule.Screens;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
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
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Views.VerticalScrollView;
import com.archermind.schedule.Views.XListView;
import com.archermind.schedule.Views.XListView.IXListViewListener;

public class ScheduleScreen extends Screen implements IXListViewListener,
		OnItemClickListener ,OnGestureListener,OnClickListener{

	private ImageView mListHeader;
	private TextView tv1;
	private TextView tv2;
	private TextView tv3;
	private TextView tv4;

	private FrameLayout layout1;
	private FrameLayout layout2;
	private FrameLayout layout3;
	private FrameLayout layout4;

	private ListView list1;
	private XListView list2;
	private ListView list3;
	private ListView list4;

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
		
		private DatabaseManager database;
		
		private Button previous_year, next_year;
		private Button current_day;
		private EventArgs args;
		
		public ScheduleScreen() {
			Date date = new Date();
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
	    	currentDate = sdf.format(date);  //当前日期
	    	Constant.YEAR = year_c = Integer.parseInt(currentDate.split("-")[0]);
	    	Constant.MONTH = month_c = Integer.parseInt(currentDate.split("-")[1]);
	    	Constant.DAY = day_c = Integer.parseInt(currentDate.split("-")[2]);
	    	
	    	database = ServiceManager.getDbManager();
	    	
		}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_screen);
		final VerticalScrollView pager = (VerticalScrollView) findViewById(R.id.pager);
//		insert();
		setupView();
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
					tv2.setTextColor(Color.parseColor("#4f810f"));
					list2.setHeaderGone(true);
					tv3.setVisibility(View.VISIBLE);
					tv4.setVisibility(View.VISIBLE);
				} else {
					mListHeader
							.setBackgroundResource(R.drawable.listview_header_up);
					mListHeader.setTag(R.drawable.listview_header_up);
					tv1.setVisibility(View.GONE);
					tv2.setVisibility(View.GONE);
					tv3.setVisibility(View.GONE);
					tv4.setVisibility(View.GONE);
					layout1.setVisibility(View.GONE);
					layout2.setVisibility(View.VISIBLE);
					layout3.setVisibility(View.GONE);
					layout4.setVisibility(View.GONE);
				}
			}
		});
	}

	private void setupView() {
		tv1 = (TextView) findViewById(R.id.tv01);
		tv2 = (TextView) findViewById(R.id.tv02);
		tv3 = (TextView) findViewById(R.id.tv03);
		tv4 = (TextView) findViewById(R.id.tv04);

		layout1 = (FrameLayout) findViewById(R.id.layout1);
		layout2 = (FrameLayout) findViewById(R.id.layout2);
		layout3 = (FrameLayout) findViewById(R.id.layout3);
		layout4 = (FrameLayout) findViewById(R.id.layout4);

		list1 = (ListView) findViewById(R.id.list01);
		list2 = (XListView) findViewById(R.id.list02);
		list3 = (ListView) findViewById(R.id.list03);
		list4 = (ListView) findViewById(R.id.list04);
		list2.setXListViewListener(this);
		list1.setOnItemClickListener(this);
		list2.setOnItemClickListener(this);
		list3.setOnItemClickListener(this);
		list4.setOnItemClickListener(this);

		list1.setAdapter(new LocalScheduleAdapter(this, ServiceManager
				.getDbManager().query3DaysBeforeLocalSchedules(
						System.currentTimeMillis())));
		list2.setAdapter(new LocalScheduleAdapter(this, ServiceManager
				.getDbManager().queryTodayLocalSchedules(
						System.currentTimeMillis())));
		list3.setAdapter(new LocalScheduleAdapter(this, ServiceManager
				.getDbManager().queryTomorrowLocalSchedules(
						System.currentTimeMillis())));
		list4.setAdapter(new LocalScheduleAdapter(this, ServiceManager
				.getDbManager().queryWeekLocalSchedules(
						System.currentTimeMillis())));

		layout1.setVisibility(View.GONE);
		layout2.setVisibility(View.VISIBLE);
		layout3.setVisibility(View.GONE);
		layout4.setVisibility(View.GONE);

		tv1.setVisibility(View.GONE);
		tv2.setVisibility(View.GONE);
		tv3.setVisibility(View.GONE);
		tv4.setVisibility(View.GONE);

		
		gestureDetector = new GestureDetector(this);
        flipper = (ViewFlipper) findViewById(R.id.flipper);
        flipper.removeAllViews();
        System.out.println("height = "+flipper.getHeight());
       
        
       
		tv1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				tv1.setTextColor(Color.parseColor("#4f810f"));
				tv2.setTextColor(Color.parseColor("#252524"));
				tv3.setTextColor(Color.parseColor("#252524"));
				tv4.setTextColor(Color.parseColor("#252524"));
				layout1.setVisibility(View.VISIBLE);
				layout2.setVisibility(View.GONE);
				layout3.setVisibility(View.GONE);
				layout4.setVisibility(View.GONE);
			}
		});

		tv2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				tv2.setTextColor(Color.parseColor("#4f810f"));
				tv1.setTextColor(Color.parseColor("#252524"));
				tv3.setTextColor(Color.parseColor("#252524"));
				tv4.setTextColor(Color.parseColor("#252524"));
				layout1.setVisibility(View.GONE);
				layout2.setVisibility(View.VISIBLE);
				layout3.setVisibility(View.GONE);
				layout4.setVisibility(View.GONE);
			}
		});

		tv3.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				tv3.setTextColor(Color.parseColor("#4f810f"));
				tv1.setTextColor(Color.parseColor("#252524"));
				tv2.setTextColor(Color.parseColor("#252524"));
				tv4.setTextColor(Color.parseColor("#252524"));
				layout1.setVisibility(View.GONE);
				layout2.setVisibility(View.GONE);
				layout3.setVisibility(View.VISIBLE);
				layout4.setVisibility(View.GONE);
				if (!tv2.isShown()) {
					tv2.setVisibility(View.VISIBLE);
				}
			}
		});

		tv4.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				tv4.setTextColor(Color.parseColor("#4f810f"));
				tv1.setTextColor(Color.parseColor("#252524"));
				tv2.setTextColor(Color.parseColor("#252524"));
				tv3.setTextColor(Color.parseColor("#252524"));
				layout1.setVisibility(View.GONE);
				layout2.setVisibility(View.GONE);
				layout3.setVisibility(View.GONE);
				layout4.setVisibility(View.VISIBLE);
				if (!tv2.isShown()) {
					tv2.setVisibility(View.VISIBLE);
				}
			}
		});
	}

@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
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
	private void insert() {
		// DateTimeUtils.getDayOfWeek(Calendar.SUNDAY);
		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_CONTENT,
				"happy new year!");
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
				System.currentTimeMillis());
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_FIRST_FLAG, true);
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_SHARE, true);
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_IMPORTANT, true);
		ServiceManager.getDbManager().insertLocalSchedules(contentValues);
		contentValues = new ContentValues();
		contentValues
				.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_CONTENT,
						"hello everyone 如果背景的大小不一样，一般需要为每种大小都 制作一张图片，这在button中尤为明显。当然我们也可以一小块一小块水平重复的画，也可 以垂直的话。在android中专门有一种叫nine patch图片（以 9.png结尾）来解决背景大小不一样时，只用一张背景图片");
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
				System.currentTimeMillis());
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_FIRST_FLAG, false);
		ServiceManager.getDbManager().insertLocalSchedules(contentValues);
		contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_CONTENT,
				"you are a bad men!");
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME,
				System.currentTimeMillis());
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_FIRST_FLAG, false);
		ServiceManager.getDbManager().insertLocalSchedules(contentValues);
	}

	@Override
	public void onRefresh() {
		Toast.makeText(ScheduleScreen.this, "up", Toast.LENGTH_SHORT).show();
		tv1.setVisibility(View.VISIBLE);
		tv2.setVisibility(View.VISIBLE);
		onLoad();
		list2.setHeaderGone(false);
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub

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
		if (parent.getId() == list1.getId()) {
			Toast.makeText(
					ScheduleScreen.this,
					"list1=" + id + "  tag = "
							+ (Boolean) args.getExtra("first") + "  time = "
							+ (Long) args.getExtra("time"), Toast.LENGTH_SHORT)
					.show();
		} else if (parent.getId() == list2.getId()) {
			Toast.makeText(
					ScheduleScreen.this,
					"list2=" + id + "  tag = "
							+ (Boolean) args.getExtra("first") + "  time = "
							+ (Long) args.getExtra("time"), Toast.LENGTH_SHORT)
					.show();
		} else if (parent.getId() == list3.getId()) {
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
		}
		Intent mIntent =new Intent(ScheduleScreen.this,EditScheduleScreen.class);
		mIntent.putExtra("id", id);
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
	
	/**
	 * 创建菜单
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, menu.FIRST, menu.FIRST, "今天");
		menu.add(0, menu.FIRST+1, menu.FIRST+1, "跳转");
		menu.add(0, menu.FIRST+2, menu.FIRST+2, "日程");
		menu.add(0, menu.FIRST+3, menu.FIRST+3, "日期转换");
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * 选择菜单
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()){
        case Menu.FIRST:
//        	跳转到今天
        	int xMonth = jumpMonth;
        	int xYear = jumpYear;
        	int gvFlag =0;
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
        case Menu.FIRST+1:
        	
        	new DatePickerDialog(this, new OnDateSetListener() {
				
				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear,
						int dayOfMonth) {
					//1901-1-1 ----> 2049-12-31
					if(year < 1901 || year > 2049){
						//不在查询范围内
						new AlertDialog.Builder(ScheduleScreen.this).setTitle("错误日期").setMessage("跳转日期范围(1901/1/1-2049/12/31)").setPositiveButton("确认", null).show();
					}else{
						int gvFlag = 0;
						addGridView();   //添加一个gridview
			        	calV = new CalendarAdapter(ScheduleScreen.this, ScheduleScreen.this.getResources(),year,monthOfYear+1,dayOfMonth,Constant.flagType);
				        gridView.setAdapter(calV);
				        addTextToTopTextView(current_date);
				        gvFlag++;
				        flipper.addView(gridView,gvFlag);
				        if(year == year_c && monthOfYear+1 == month_c){
				        	//nothing to do
				        }
				        if((year == year_c && monthOfYear+1 > month_c) || year > year_c ){
				        	ScheduleScreen.this.flipper.setInAnimation(AnimationUtils.loadAnimation(ScheduleScreen.this,R.anim.push_left_in));
				        	ScheduleScreen.this.flipper.setOutAnimation(AnimationUtils.loadAnimation(ScheduleScreen.this,R.anim.push_left_out));
				        	ScheduleScreen.this.flipper.showNext();
				        }else{
				        	ScheduleScreen.this.flipper.setInAnimation(AnimationUtils.loadAnimation(ScheduleScreen.this,R.anim.push_right_in));
				        	ScheduleScreen.this.flipper.setOutAnimation(AnimationUtils.loadAnimation(ScheduleScreen.this,R.anim.push_right_out));
				        	ScheduleScreen.this.flipper.showPrevious();
				        }
				        flipper.removeViewAt(0);
				        //跳转之后将跳转之后的日期设置为当前日期
				        year_c = year;
						month_c = monthOfYear+1;
						day_c = dayOfMonth;
						jumpMonth = 0;
						jumpYear = 0;
					}
				}
			},year_c, month_c-1, day_c).show();
        	break;
        case Menu.FIRST+2:
        	Intent intent = new Intent();
			intent.setClass(ScheduleScreen.this, ScheduleAllScreen.class);
			startActivity(intent);
        	break;
        case Menu.FIRST+3:
        	Intent intent1 = new Intent();
        	intent1.setClass(ScheduleScreen.this, CalendarConvertScreen.class);
        	intent1.putExtra("date", new int[]{year_c,month_c,day_c});
        	startActivity(intent1);
        	break;
        }
		return super.onMenuItemSelected(featureId, item);
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
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
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
	                  
	                  //ͨ通过日期查询这一天是否被标记，如果标记了日程就查询出这天的所有日程信息
	                  String[] scheduleIDs = database.getScheduleByTagDate(Integer.parseInt(scheduleYear), Integer.parseInt(scheduleMonth), Integer.parseInt(scheduleDay));
	                  if(scheduleIDs != null && scheduleIDs.length > 0){
	                	  for(String str : scheduleIDs){
	                		  System.out.print("scheduleID = "+str+", ");
	                	  }
	                	  //跳转到显示这一天的所有日程信息界面
		  				  Intent intent = new Intent();
		  				  intent.setClass(ScheduleScreen.this, ScheduleInfoScreen.class);
		                  intent.putExtra("scheduleID", scheduleIDs);
		  				  startActivity(intent);  
		  				  
	                  }else{
	                  //ֱ直接跳转到需要添加日程的界面
	                	  
		                  //得到这一天是星期几
		                  switch(position%7){
		                  case 0:
		                	  week = "星期日";
		                	  break;
		                  case 1:
		                	  week = "星期一";
		                	  break;
		                  case 2:
		                	  week = "星期二";
		                	  break;
		                  case 3:
		                	  week = "星期三";
		                	  break;
		                  case 4:
		                	  week = "星期四";
		                	  break;
		                  case 5:
		                	  week = "星期五";
		                	  break;
		                  case 6:
		                	  week = "星期六";
		                	  break;
		                  }
						 
		                  ArrayList<String> scheduleDate = new ArrayList<String>();
		                  scheduleDate.add(scheduleYear);
		                  scheduleDate.add(scheduleMonth);
		                  scheduleDate.add(scheduleDay);
		                  scheduleDate.add(week);
		                  //scheduleDate.add(scheduleLunarDay);
		                  
		                  
		                  Intent intent = new Intent();
		                  intent.putStringArrayListExtra("scheduleDate", scheduleDate);
		                  intent.setClass(ScheduleScreen.this, ScheduleAddScreen.class);
		                  startActivity(intent);
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
	
}

