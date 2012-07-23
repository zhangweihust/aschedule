package com.archermind.schedule.Screens;

import android.content.ContentValues;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.archermind.schedule.R;
import com.archermind.schedule.Adapters.LocalScheduleAdapter;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Views.VerticalScrollView;
import com.archermind.schedule.Views.XListView;
import com.archermind.schedule.Views.XListView.IXListViewListener;

public class ScheduleScreen extends Screen implements IXListViewListener, OnItemClickListener {
	
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
		
		

	@Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.schedule_screen);
	        final VerticalScrollView pager = (VerticalScrollView) findViewById(R.id.pager);
	        insert();
	        setupView();
	        mListHeader = (ImageView) findViewById(R.id.list_header);
	        mListHeader.setBackgroundResource(R.drawable.listview_header_up);
	        mListHeader.setTag(R.drawable.listview_header_up);
	        mListHeader.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Integer tag = (Integer) mListHeader.getTag();
					if(tag != null){
						if(tag.intValue() == R.drawable.listview_header_up){
							pager.snapToPage(1);
						} else if(tag.intValue() == R.drawable.listview_header_down) {
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
		            	if(whichPage  == 1){
		            		mListHeader.setBackgroundResource(R.drawable.listview_header_down);
		            		 mListHeader.setTag(R.drawable.listview_header_down);
		            		 tv2.setTextColor(Color.parseColor("#4f810f"));
		            		 list2.setHeaderGone(true);
		            		 tv3.setVisibility(View.VISIBLE);
		            		 tv4.setVisibility(View.VISIBLE);
		            	} else {
		            		mListHeader.setBackgroundResource(R.drawable.listview_header_up);
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
		tv1 =(TextView) findViewById(R.id.tv01);
		tv2 =(TextView) findViewById(R.id.tv02);
		tv3 =(TextView) findViewById(R.id.tv03);
		tv4 =(TextView) findViewById(R.id.tv04);
		
		layout1 = (FrameLayout)findViewById(R.id.layout1);
		layout2 = (FrameLayout)findViewById(R.id.layout2);
		layout3 = (FrameLayout)findViewById(R.id.layout3);
		layout4 = (FrameLayout)findViewById(R.id.layout4);
		
		list1 = (ListView)findViewById(R.id.list01);
		list2 = (XListView)findViewById(R.id.list02);
		list3 = (ListView)findViewById(R.id.list03);
		list4 = (ListView)findViewById(R.id.list04);
		list2.setXListViewListener(this);
		list1.setOnItemClickListener(this);
		list2.setOnItemClickListener(this);
		list3.setOnItemClickListener(this);
		list4.setOnItemClickListener(this);
		
		list1.setAdapter(new LocalScheduleAdapter(this, ServiceManager.getDbManager().query3DaysBeforeLocalSchedules()));
		list2.setAdapter(new LocalScheduleAdapter(this, ServiceManager.getDbManager().queryTodayLocalSchedules())); 
		list3.setAdapter(new LocalScheduleAdapter(this, ServiceManager.getDbManager().queryTomorrowLocalSchedules())); 
		list4.setAdapter(new LocalScheduleAdapter(this, ServiceManager.getDbManager().queryWeekLocalSchedules()));

		layout1.setVisibility(View.GONE);
		layout2.setVisibility(View.VISIBLE);
		layout3.setVisibility(View.GONE);
		layout4.setVisibility(View.GONE);
		
		tv1.setVisibility(View.GONE);
		tv2.setVisibility(View.GONE);
		tv3.setVisibility(View.GONE);
		tv4.setVisibility(View.GONE);
		
		
		
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
				if(!tv2.isShown()){
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
				if(!tv2.isShown()){
					tv2.setVisibility(View.VISIBLE);
				}
			}
		});
	}
	
	
	private void insert(){
		//DateTimeUtils.getDayOfWeek(Calendar.SUNDAY);
		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_CONTENT, "happy new year!");
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME, System.currentTimeMillis());
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_FIRST_FLAG, true);
		ServiceManager.getDbManager().insertLocalSchedules(contentValues);
		contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_CONTENT, "hello everyone 如果背景的大小不一样，一般需要为每种大小都 制作一张图片，这在button中尤为明显。当然我们也可以一小块一小块水平重复的画，也可 以垂直的话。在android中专门有一种叫nine patch图片（以 9.png结尾）来解决背景大小不一样时，只用一张背景图片");
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME, System.currentTimeMillis());
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_FIRST_FLAG, false);
		ServiceManager.getDbManager().insertLocalSchedules(contentValues);
		contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_NOTICE_CONTENT, "you are a bad men!");
		contentValues.put(DatabaseHelper.COLUMN_SCHEDULE_START_TIME, System.currentTimeMillis());
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(parent.getId() == list1.getId()){
			Toast.makeText(ScheduleScreen.this, "list1=" + id, Toast.LENGTH_SHORT).show();
		} else if(parent.getId() == list2.getId()){
			Toast.makeText(ScheduleScreen.this, "list2=" + id, Toast.LENGTH_SHORT).show();
		} else if(parent.getId() == list3.getId()){
			Toast.makeText(ScheduleScreen.this, "list3=" + id, Toast.LENGTH_SHORT).show();
		} else if(parent.getId() == list4.getId()){
			Toast.makeText(ScheduleScreen.this, "list4=" + id, Toast.LENGTH_SHORT).show();
		}
	}
	
}