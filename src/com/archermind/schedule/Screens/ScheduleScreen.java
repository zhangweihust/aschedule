package com.archermind.schedule.Screens;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.R.integer;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Adapters.CalendarAdapter;
import com.archermind.schedule.Adapters.HistoryScheduleAdapter;
import com.archermind.schedule.Calendar.CalendarData;
import com.archermind.schedule.Calendar.SpecialCalendar;
import com.archermind.schedule.Dialog.ScheduleOperateDialog;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.IEventHandler;
import com.archermind.schedule.Model.ScheduleData;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Utils.LostTime;
import com.archermind.schedule.Views.VerticalScrollView;
import com.archermind.schedule.Views.XListView;
import com.archermind.schedule.Views.XListView.IXListViewListener;
import com.archermind.schedule.Views.XListView.OnXScrollListener;
import com.archermind.schedule.Views.XListViewFooter;

public class ScheduleScreen extends Screen
		implements
			IXListViewListener,
			OnXScrollListener,
			OnItemClickListener,
			OnGestureListener,
			OnClickListener,
			IEventHandler {

	private static final int FRESH_LIMIT_NUM = 30;

	private static final int LOAD_DATA_OVER = 1;

	private static final int LOAD_OVERD_GOTO_TODAY = 2;
	private static final int LOCAL_SCHEDULE_UPDATE_OVER = 3;

	private ImageView mListHeader;

	private View schedulelistHeadView;

	private int headViewHeight = 0;

	private TextView schedule_headview_prompt;

	private VerticalScrollView pager;

	private static boolean isFling = false;

	// private TextView tv1;
	// private TextView tv2;
	// private TextView tv3;
	// private TextView tv4;

	// private FrameLayout layout1;
	private FrameLayout layout2;

	// private FrameLayout layout3;
	// private FrameLayout layout4;

	// private ListView list1;
	private XListView list2;

	// private ListView list3;
	// private ListView list4;

	// private Cursor c1;
	private Cursor TodayScheduleCursor;

	// private Cursor c3;
	// private Cursor c4;

	private HistoryScheduleAdapter hsa;

	private List<ScheduleData> listdata;

	private String curSelectedDate = "";

	private int curScrollYear = 0;

	private int curScrollMonth = 0;
	
	private int listScrollYear = 0;

	private int listScrollMonth = 0;

	private String curDay = "";

	private static int year;

	private static int month;

	private static int day;

	private Handler handler;

	private ViewFlipper flipper = null;

	private GestureDetector gestureDetector = null;

	private CalendarAdapter calV = null;

	private CalendarData calendarData;

	private GridView gridView = null;

	private TextView current_date = null;

	private Drawable draw = null;

	public static int jumpMonth = 0; // 每次滑动，增加或减去一个月，默认为0（即显示当前月）

	public static int jumpYear = 0; // 滑动跨越一年，则增加或者减去一年，默认为0（即当前年）

	// public static int jumpDay = 0;
//	public String currentDay = null;

	private int year_c = 0;

	private int month_c = 0;

	private int day_c = 0;

	private Button previous_year, next_year;

	private Button current_day;

	private EventArgs args;

	private boolean flag = false;

	private TextView gototoday;

	public static boolean isUp = false;

	public ScheduleScreen() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
		String currentDate = sdf.format(date); // 当前日期
		year = Constant.YEAR = year_c = Integer
				.parseInt(currentDate.split("-")[0]);
		month = Constant.MONTH = month_c = Integer.parseInt(currentDate
				.split("-")[1]);
		day = Constant.DAY = day_c = Integer
				.parseInt(currentDate.split("-")[2]);

		curScrollYear = year_c;
		curScrollMonth = month_c;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ServiceManager.setListViewKind(XListViewFooter.SCHEDULE_PROMPT);
		setContentView(R.layout.schedule_screen);

		curDay = DateTimeUtils.time2String("dd", System.currentTimeMillis());

		schedulelistHeadView = LayoutInflater.from(ScheduleScreen.this)
				.inflate(R.layout.schedule_list_headview, null);
		schedule_headview_prompt = (TextView) schedulelistHeadView
				.findViewById(R.id.schedule_headview_prompt);
		Typeface type = Typeface.createFromAsset(getAssets(),"xdxwzt.ttf");
		schedule_headview_prompt.setTypeface(type);
		schedule_headview_prompt.setOnClickListener(this);

		handler = new Handler() {

			public void handleMessage(Message msg) {
				switch (msg.what) {
					case LOAD_DATA_OVER:
						hsa.setData(listdata);
						onLoad();
						break;

					case LOAD_OVERD_GOTO_TODAY :
						// hsa.setData(listdata);
						gototodaypos();
						schedule_headview_prompt
								.setText(getHeadViewText(getDateByMillisTime(System
										.currentTimeMillis())));
						break;
					case LOCAL_SCHEDULE_UPDATE_OVER :
						gridView.setAdapter(calV);
						//hsa.setData(listdata);
						gototoday.setVisibility(View.INVISIBLE);
						schedule_headview_prompt
								.setText(getHeadViewText(curSelectedDate));
						break;
				}
				super.handleMessage(msg);
			}
		};

		gototoday = (TextView) findViewById(R.id.gototoday);
		gototoday.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				int pos = hsa.getTodayPosition(getDateByMillisTime(System
						.currentTimeMillis()));
				list2.setSelection(pos + 1); /* 最上面的上翻更新数据也算一个位置,,所以+1 */

			}
		});

		curSelectedDate = getDateByMillisTime(System.currentTimeMillis());
		pager = (VerticalScrollView) findViewById(R.id.pager);
		flag = true;
		eventService.add(this);
		mListHeader = (ImageView) findViewById(R.id.list_header);
		mListHeader.setBackgroundResource(R.drawable.listview_header_up);
		mListHeader.setTag(R.drawable.listview_header_up);
		mListHeader.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				showListSchedule();
			}
		});
		schedule_headview_prompt
		.setText(getHeadViewText(curSelectedDate));

		pager.addOnScrollListener(new VerticalScrollView.OnScrollListener() {
			public void onScroll(int scrollX) {
			}

			public void onViewScrollFinished(int currentPage) {

			}

			@Override
			public void snapToPage(int whichPage) {
				if (whichPage == 1) {
					LayoutParams lp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.FILL_PARENT, 0);
					schedule_headview_prompt.setLayoutParams(lp);
					schedulelistHeadView.setVisibility(View.GONE);
					gototodaypos();

					mListHeader
							.setBackgroundResource(R.drawable.listview_header_down);
					mListHeader.setTag(R.drawable.listview_header_down);

					// tv2.setTextColor(Color.parseColor("#4f810f"));
					// list2.setHeaderGone(true);
					// tv3.setVisibility(View.VISIBLE);
					// tv4.setVisibility(View.VISIBLE);
				} else {
					LayoutParams lp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.FILL_PARENT,
							headViewHeight);
					schedule_headview_prompt.setLayoutParams(lp);
					schedulelistHeadView.setVisibility(View.VISIBLE);
					list2.setSelection(1);
					gototoday.setVisibility(View.INVISIBLE);

					mListHeader
							.setBackgroundResource(R.drawable.listview_header_up);
					mListHeader.setTag(R.drawable.listview_header_up);
					// tv1.setVisibility(View.GONE);
					// tv2.setVisibility(View.GONE);
					// tv3.setVisibility(View.GONE);
					// tv4.setVisibility(View.GONE);
					// layout1.setVisibility(View.GONE);
					layout2.setVisibility(View.VISIBLE);
					// layout3.setVisibility(View.GONE);
					// layout4.setVisibility(View.GONE);
				}
			}
		});

		setupView();

		hsa = new HistoryScheduleAdapter(this);
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {

		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK :

				showListSchedule();
				return true;

			case KeyEvent.KEYCODE_MENU :

				HomeScreen.switchActivity();
				break;

			default :
				break;
		}

		return super.onKeyUp(keyCode, event);
	}

	private void setupView() {
		layout2 = (FrameLayout) findViewById(R.id.layout2);

		list2 = (XListView) findViewById(R.id.list02);
		list2.setXListViewListener(this);
		list2.setOnScrollListener(this);
		list2.setOnItemClickListener(this);
		list2.setPullLoadEnable(true);
		list2.setPullLoadEnable(true);

		layout2.setVisibility(View.VISIBLE);

		gestureDetector = new GestureDetector(this);
		flipper = (ViewFlipper) findViewById(R.id.flipper);
		flipper.removeAllViews();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (flag) {
			flag = false;

			calendarData = new CalendarData(ScheduleScreen.this, jumpMonth,
					jumpYear, year_c, month_c, day_c, Constant.flagType);
			calV = new CalendarAdapter(this, flipper.getHeight(), calendarData);

			addGridView();
			gridView.setAdapter(calV);

			headViewHeight = (int) (pager.getMeasuredHeight() * 0.25 - mListHeader
					.getMeasuredHeight());
			LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT, headViewHeight);
			schedule_headview_prompt.setLayoutParams(lp);
			list2.addHeaderView(schedulelistHeadView, null, false);

			list2.setAdapter(hsa);
			listdata = calendarData.getMonthSchedule(curScrollYear,
					curScrollMonth);
			listScrollYear = curScrollYear;
			listScrollMonth = curScrollMonth;
			handler.sendEmptyMessage(LOAD_DATA_OVER);

			// flipper.addView(gridView);
			flipper.addView(gridView, 0);
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

	public void cursorToListData(Cursor c, List<ScheduleData> listdata) {
		ScheduleData data;

		if (c == null) {
			return;
		}

		while (c.moveToNext()) {
			data = new ScheduleData();
			data.id = c.getInt(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_ID));
			data.content = c.getString(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT));
			data.time = c.getLong(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME));
			data.share = c.getInt(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_SHARE)) == 1;
			data.notice_flag = c
					.getInt(c
							.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_FLAG)) == 1;
			data.type = c.getInt(c
					.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_TYPE));

			listdata.add(data);
		}
	}

	@Override
	public void onRefresh() {
		// Toast.makeText(ScheduleScreen.this, "up", Toast.LENGTH_SHORT).show();
		// tv1.setVisibility(View.VISIBLE);
		// tv2.setVisibility(View.VISIBLE);
		scrollToPreMonth();
		new Thread() {
			public void run() {
				listdata = calendarData.getMonthSchedule(listScrollYear,
						listScrollMonth);
				handler.sendEmptyMessage(LOAD_DATA_OVER);
			};
		}.start();

		// list2.setHeaderGone(false);
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		scrollToAftMonth();
		new Thread() {
			public void run() {
				listdata = calendarData.getMonthSchedule(listScrollYear,
						listScrollMonth);
				handler.sendEmptyMessage(LOAD_DATA_OVER);
			};
		}.start();

		// list2.setHeaderGone(false);
	}

	private void onLoad() {
		list2.stopRefresh();
		list2.stopLoadMore();
		list2.setRefreshTime(DateTimeUtils.time2String("yyyy-MM-dd hh:mm:ss",
				System.currentTimeMillis()));
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		args = (EventArgs) view.getTag();
		/*
		 * if (parent.getId() == list1.getId()) { Toast.makeText(
		 * ScheduleScreen.this, "list1=" + id + "  tag = " + (Boolean)
		 * args.getExtra("first") + "  time = " + (Long) args.getExtra("time"),
		 * Toast.LENGTH_SHORT) .show(); } else
		 */if (parent.getId() == list2.getId()) {
			// Toast.makeText(
			// ScheduleScreen.this,
			// "list2=" + id + "  tag = "
			// + (Boolean) args.getExtra("first") + "  time = "
			// + (Long) args.getExtra("time"), Toast.LENGTH_SHORT)
			// .show();
		}/*
		 * else if (parent.getId() == list3.getId()) { Toast.makeText(
		 * ScheduleScreen.this, "list3=" + id + "  tag = " + (Boolean)
		 * args.getExtra("first") + "  time = " + (Long) args.getExtra("time"),
		 * Toast.LENGTH_SHORT) .show(); } else if (parent.getId() ==
		 * list4.getId()) { Toast.makeText( ScheduleScreen.this, "list4=" + id +
		 * "  tag = " + (Boolean) args.getExtra("first") + "  time = " + (Long)
		 * args.getExtra("time"), Toast.LENGTH_SHORT) .show(); }
		 */

		Integer itemid = (Integer) args.getExtra("id");
		if (itemid > 0) {
			ScheduleOperateDialog scheduleDialog = new ScheduleOperateDialog(
					this, args);
			scheduleDialog.show(getWindow().getAttributes().width);
		}
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		System.out
				.println("e1.getX() - e2.getX() = " + (e1.getX() - e2.getX()));
		System.out.println("velocityX = " + velocityX + "     velocityY = "
				+ velocityY);
		int gvFlag = 0; // 每次添加gridview到viewflipper中时给的标记
		if (e1.getX() - e2.getX() > 100) {

			long pretime = System.currentTimeMillis();
			// 向左滑动
			addGridView(); // 添加一个gridview
			jumpMonth++; // 下一个月

			calendarData = new CalendarData(ScheduleScreen.this, jumpMonth,
					jumpYear, year_c, month_c, day_c, Constant.flagType);
			ScheduleApplication.LogD(getClass(),
					"获取数据花费时间是：" + (System.currentTimeMillis() - pretime));
			calV = new CalendarAdapter(this, flipper.getHeight(), calendarData);
			gridView.setAdapter(calV);
			ScheduleApplication.LogD(getClass(),
					"设置到view里面的时间：" + (System.currentTimeMillis() - pretime));
			// flipper.addView(gridView);
			gvFlag++;
			flipper.addView(gridView, gvFlag);
			this.flipper.setInAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_left_in));
			this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_left_out));
			this.flipper.showNext();
			flipper.removeViewAt(0);

			setScheduleData();

			ScheduleApplication.LogD(getClass(),
					"总共的时间" + (System.currentTimeMillis() - pretime));

			return true;
		} else if (e1.getX() - e2.getX() < -100) {
			// 向右滑动
			addGridView(); // 添加一个gridview
			jumpMonth--; // 上一个月
			calendarData = new CalendarData(ScheduleScreen.this, jumpMonth,
					jumpYear, year_c, month_c, day_c, Constant.flagType);
			calV = new CalendarAdapter(this, flipper.getHeight(), calendarData);
			gridView.setAdapter(calV);
			gvFlag++;
			// flipper.addView(gridView);
			flipper.addView(gridView, gvFlag);

			this.flipper.setInAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_right_in));
			this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_right_out));
			this.flipper.showPrevious();
			flipper.removeViewAt(0);

			setScheduleData();

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

	// 添加头部的年份 闰哪个月等信息
	public void addTextToTopTextView(TextView view) {
		// StringBuffer textDate = new StringBuffer();
		// draw = getResources().getDrawable(R.drawable.top_day);
		// view.setBackgroundDrawable(draw);
		// textDate.append(calV.getShowYear()).append("年").append(
		// calV.getShowMonth()).append("月").append("\t");
		// if (!calV.getLeapMonth().equals("") && calV.getLeapMonth() != null) {
		// textDate.append("闰").append(calV.getLeapMonth()).append("月")
		// .append("\t");
		// }
		// textDate.append(calV.getAnimalsYear()).append("年").append("(").append(
		// calV.getCyclical()).append("年)");
		// view.setText(textDate);
		if (Integer.parseInt(calendarData.getShowMonth()) < 10) {
			view.setText(calendarData.getShowYear() + ".0"
					+ calendarData.getShowMonth());
		} else {
			view.setText(calendarData.getShowYear() + "."
					+ calendarData.getShowMonth());
		}
		view.setTextColor(Color.BLACK);
		view.setTypeface(Typeface.DEFAULT_BOLD);
	}

	public String getDate(int position) {
		String date = "";
		int month = Integer.parseInt(calendarData.getShowMonth());
		int day = Integer.parseInt(calV.getDateByClickItem(position).split(
				"\\.")[0]);
		this.year = Integer.parseInt(calendarData.getShowYear());
		this.month = month;
		this.day = day;
		if (month < 10) {
			date = calendarData.getShowYear() + ".0" + month;
		} else {
			date = calendarData.getShowYear() + "." + month;
		}
		if (day < 10) {
			date += ".0" + day;
		} else {
			date += "." + day;
		}
		return date;
	}

	public static long getDateMill() {
		String data = year + "." + month + "." + day;
		return getMillisTimeByDate(data);
	}

	public static long getMillisTimeByDate(String date) {
		long time = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		try {
			Date d = sdf.parse(date);
			time = d.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return time;
	}

	public String getDateByMillisTime(long time) {
		String date = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		Date d = new Date(time);
		date = sdf.format(d);

		return date;
	}

	public void scrollToPreMonth() {
		if (listScrollMonth > 1) {
			listScrollMonth--;
		} else {
			listScrollMonth = 12;
			listScrollYear--;
		}
	}

	public void scrollToAftMonth() {
		if (listScrollMonth < 12) {
			listScrollMonth++;
		} else {
			listScrollMonth = 1;
			listScrollYear++;
		}
	}

	// 添加gridview
	private void addGridView() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		// 取得屏幕的宽度和高度
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		int Width = display.getWidth();
		int Height = display.getHeight();

		gridView = new GridView(this);
		gridView.setNumColumns(7);
		gridView.setColumnWidth(Width / 7 + 1);

		gridView.setVerticalScrollBarEnabled(false);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setGravity(Gravity.CENTER);
		gridView.setOnTouchListener(new OnTouchListener() {
			// 将gridview中的触摸时间回传给gestureDetector
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return ScheduleScreen.this.gestureDetector.onTouchEvent(event);
			}
		});

		gridView.setOnItemClickListener(new OnItemClickListener() {
			// gridView中的每一个item的点击事件
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				// 点击任何一个item，得到这个item的日期（排除点击的是周日到周六（点击不响应））
				int startPosition = calV.getStartPositon();
				int endPosition = calV.getEndPosition();
				if (startPosition <= position && position <= endPosition) {
					String scheduleDay = calV.getDateByClickItem(position)
							.split("\\.")[0]; // 这一天的阳历
					// String scheduleLunarDay =
					// calV.getDateByClickItem(position).split("\\.")[1];
					// //这一天的阴历
					String scheduleYear = calendarData.getShowYear();
					String scheduleMonth = calendarData.getShowMonth();
					String week = "";

					int index = calV.getOldposition();
					if (index == -1) {
						// ((RelativeLayout)view).setBackgroundResource(R.drawable.current_day_bg);
						((RelativeLayout) view)
								.setBackgroundColor(getResources().getColor(
										R.color.selector));
						calV.setOldPosition(position);
					} else {
						RelativeLayout layout = (RelativeLayout) arg0
								.getChildAt(index);
						if (layout != null) {
							layout.setBackgroundResource(R.drawable.gridview_bk);
						}
						// ((RelativeLayout)view).setBackgroundResource(R.drawable.current_day_bg);
						((RelativeLayout) view)
								.setBackgroundColor(getResources().getColor(
										R.color.selector));
						calV.setOldPosition(position);
					}

					String date = getDate(position);
					if (!curSelectedDate.equals(date)) {

						curSelectedDate = date;
						ScheduleApplication
								.LogD(ScheduleScreen.class,
										"setOnItemClickListener --->"
												+ curSelectedDate);

						// if (!hsa.isEmpty()) {

						schedule_headview_prompt
								.setText(getHeadViewText(curSelectedDate));
						// }
					}
					gototoday.setVisibility(View.INVISIBLE);
				}
			}
		});
		gridView.setLayoutParams(params);
	}

	private boolean isCurrenDay(int position) {
		int year = Integer.parseInt(calendarData.getShowYear());
		int month = Integer.parseInt(calendarData.getShowMonth());
		int day = Integer.parseInt(calV.getDateByClickItem(position).split(
				"\\.")[0]);
		if (year == year_c && month == month_c && day == day_c) {
			return true;
		}
		return false;
	}

	private void setScheduleData() {
		addTextToTopTextView(current_date);
		curScrollYear = Integer.parseInt(current_date.getText()
				.toString().split("\\.")[0]);
		curScrollMonth = Integer.parseInt(current_date.getText()
				.toString().split("\\.")[1]);
		listdata = null;
		isFling = true;
		gototoday.setVisibility(View.INVISIBLE);
		schedule_headview_prompt
				.setText(getHeadViewText(curSelectedDate));
		isFling = false;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int gvFlag = 0;
		switch (v.getId()) {

			case R.id.schedule_headview_prompt :
				new Thread() {
					public void run() {
	
						if (listScrollYear != curScrollYear 
								|| listScrollMonth != curScrollMonth
								|| listdata == null) {
							listdata = calendarData.getMonthSchedule(curScrollYear,
									curScrollMonth);
							listScrollYear = curScrollYear;
							listScrollMonth = curScrollMonth;
							
						}
						handler.sendEmptyMessage(LOAD_DATA_OVER);
	
					};
				}.start();
				showListSchedule();
				break;

			case R.id.previous_year :// 点击上一个月

				addGridView(); // 添加一个gridview
				jumpMonth--; // 下一年
				calendarData = new CalendarData(ScheduleScreen.this, jumpMonth,
						jumpYear, year_c, month_c, day_c, Constant.flagType);
				calV = new CalendarAdapter(this, flipper.getHeight(),
						calendarData);
				gridView.setAdapter(calV);
				// flipper.addView(gridView);
				
				gvFlag++;
				flipper.addView(gridView, gvFlag);
				this.flipper.setInAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_left_in));
				this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_left_out));
				this.flipper.showNext();
				flipper.removeViewAt(0);


				setScheduleData();


				break;

			case R.id.next_year :// 点击下一个月

				addGridView(); // 添加一个gridview
				jumpMonth++; // 上一年
				calendarData = new CalendarData(ScheduleScreen.this, jumpMonth,
						jumpYear, year_c, month_c, day_c, Constant.flagType);
				calV = new CalendarAdapter(this, flipper.getHeight(),
						calendarData);
				gridView.setAdapter(calV);
				gvFlag++;
				
				// flipper.addView(gridView);
				flipper.addView(gridView, gvFlag);

				this.flipper.setInAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_right_in));
				this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
						R.anim.push_right_out));
				this.flipper.showPrevious();
				flipper.removeViewAt(0);

				setScheduleData();
				

				break;
			case R.id.current_day :

				int xMonth = jumpMonth;
				int xYear = jumpYear;
				jumpMonth = 0;
				jumpYear = 0;
				addGridView(); // 添加一个gridview
				Date date = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
				String currentDate = sdf.format(date); // 当前日期
				this.year = year_c = Integer
						.parseInt(currentDate.split("-")[0]);
				this.month = month_c = Integer
						.parseInt(currentDate.split("-")[1]);
				this.day = day_c = Integer.parseInt(currentDate.split("-")[2]);
				calendarData = new CalendarData(ScheduleScreen.this, jumpMonth,
						jumpYear, year_c, month_c, day_c, Constant.flagType);
				calV = new CalendarAdapter(this, flipper.getHeight(),
						calendarData);
				gridView.setAdapter(calV);
				gvFlag++;
				flipper.addView(gridView, gvFlag);
				if (xMonth == 0 && xYear == 0) {
					// nothing to do
				} else if ((xYear == 0 && xMonth > 0) || xYear > 0) {
					this.flipper.setInAnimation(AnimationUtils.loadAnimation(
							this, R.anim.push_left_in));
					this.flipper.setOutAnimation(AnimationUtils.loadAnimation(
							this, R.anim.push_left_out));
					this.flipper.showNext();
				} else {
					this.flipper.setInAnimation(AnimationUtils.loadAnimation(
							this, R.anim.push_right_in));
					this.flipper.setOutAnimation(AnimationUtils.loadAnimation(
							this, R.anim.push_right_out));
					this.flipper.showPrevious();
				}
				flipper.removeViewAt(0);


						// handler.post(new Runnable() {
						//
						// @Override
						// public void run() {
						// // TODO Auto-generated method stub
						// handler.sendEmptyMessage(LOAD_OVERD_GOTO_TODAY);
						// }
						// });
				setScheduleData();

				// 点击回到今天，把值设置到今天
				curSelectedDate = getDateByMillisTime(System
						.currentTimeMillis());
				curDay = DateTimeUtils.time2String("dd",
						System.currentTimeMillis());

				break;
		}
	}

	private void showListSchedule() {

		Integer tag = (Integer) mListHeader.getTag();
		if (tag != null) {
			if (tag.intValue() == R.drawable.listview_header_up) {
				isUp = true;
				pager.snapToPage(1);
			} else if (tag.intValue() == R.drawable.listview_header_down) {
				isUp = false;
				pager.snapToPage(0);
			}
		}

		ScheduleApplication.LogD(getClass(), " isup= " + isUp);
	}

	@Override
	public boolean onEvent(Object sender, EventArgs e) {
		switch (e.getType()) {
			case LOCAL_SCHEDULE_UPDATE :
				calendarData = new CalendarData(ScheduleScreen.this, jumpMonth,
						jumpYear, year_c, month_c, day_c, Constant.flagType);
				calV = new CalendarAdapter(this, flipper.getHeight(),
						calendarData,curSelectedDate);
				ScheduleApplication.LogD(ScheduleScreen.class,
						"LOCAL_SCHEDULE_UPDATE");

				listdata = calendarData.getMonthSchedule(curScrollYear,
						curScrollMonth);
				handler.sendEmptyMessage(LOCAL_SCHEDULE_UPDATE_OVER);

				// ScheduleScreen.this.runOnUiThread(new Runnable(){
				// @Override
				// public void run() {
				// listdata = calendarData.getMonthSchedule(curScrollYear,
				// curScrollMonth);
				// handler.sendEmptyMessage(LOAD_DATA_OVER);
				// gridView.setAdapter(calV);
				// }});

				break;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		eventService.remove(this);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

		if (firstVisibleItem < 2 || firstVisibleItem > hsa.getCount()) {
			return;
		}

		if (hsa.containTodaySchedule(firstVisibleItem - 2, firstVisibleItem
				+ visibleItemCount - 2,
				getDateByMillisTime(System.currentTimeMillis()))) {
			/* 当前显示中包含今天的日程，要让回今天按钮消失 */
			gototoday.setVisibility(View.INVISIBLE);
		} else {
			/* 当前显示中不包含今天的日程,但是所有已加载的日程中包含今天的日程，显示回今天按钮 */
			if (hsa.containTodaySchedule(0, hsa.getCount(),
					getDateByMillisTime(System.currentTimeMillis()))) {
				gototoday.setVisibility(View.VISIBLE);
			}
		}

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onXScrolling(View view) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	public void gototodaypos() {
		int pos = hsa.getTodayPosition(getDateByMillisTime(System
				.currentTimeMillis()));
		if (pos >= 0) {
			list2.setSelection(pos + 2); /* 最上面的上翻更新数据也算一个位置,再加上一个headview */
		}
	}

	public String getHeadViewText(String date) {
		// String strtext = "";
		// int[] schedulecount = calendarData.getSchDateTagFlag();
		// int i = 0;
		// int k = 0;
		// int count = 32; /*一个月最多31天*/
		//
		// for(i = 0; i < schedulecount.length; i++)
		// {
		// if ((schedulecount[i] - 1) == day)
		// {
		// strtext = "今天有 " + calendarData.getMarkcount()[i] + " 条日程";
		// break;
		// }
		// else if(schedulecount[i] == -1)
		// {
		// if (i == 0)
		// {
		// strtext = String.format("%04d年%02d月没有日程", year,month);
		// break;
		// }
		//
		// for (k = 0; k < i; k++)
		// {
		// if (count > (day - schedulecount[k] + 1))
		// {
		// count = day - schedulecount[k] + 1;
		// }
		// }
		//
		// if ((count > 0) && (count <= 31))
		// {
		// strtext = "已经有 " + count + " 天没写日程了哦";
		// }
		// else
		// {
		// strtext = "已经有 " + day + " 天没写日程了哦";
		// }
		// break;
		// }
		// }
		//
		//
		// return strtext;

		String dataTime = "";
		int count = 0;

		if (curScrollMonth < 10) {

			dataTime = "" + curScrollYear + ".0" + curScrollMonth + ".";
			dataTime += isFling ? curDay : new String(date.toCharArray(), 8, 2);

		} else {

			dataTime = "" + curScrollYear + "." + curScrollMonth + ".";
			dataTime += isFling ? curDay : new String(date.toCharArray(), 8, 2);
		}

		// database.openwithnoservice();

		long timeInMillis = DateTimeUtils.time2Long("yyyy.MM.dd", dataTime);
		int year = Integer.parseInt(DateTimeUtils.time2String("yyyy",
				timeInMillis));
		int month = Integer.parseInt(DateTimeUtils.time2String("M",
				timeInMillis));
		int day = Integer
				.parseInt(DateTimeUtils.time2String("d", timeInMillis));

		String dayOfYear = month + "." + day;
		String dayOfMonth = Integer.toString(day);
		String dayOfWeek = SpecialCalendar.getNumberWeekDay(year, month, day);

		Cursor cursor = database.queryIsMarkWithDay(timeInMillis, dayOfYear,
				dayOfMonth, dayOfWeek);

		if (cursor != null) {

			count = cursor.getCount();
			cursor.close();
		}

		// int count = hsa.getScheduleCountInDay(dataTime);
		ScheduleApplication.LogD(ScheduleScreen.class, " date is " + date
				+ " count is " + count);

		// database.close();
		String strtext = "";
		if (count > 0) {

			strtext = "" + dataTime + " 有 " + count + " 条日程!";
		} else {

			strtext = "" + dataTime + " 没有日程!";
		}

		return strtext;
	}
}
