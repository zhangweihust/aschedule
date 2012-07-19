package com.archermind.schedule.Screens;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import com.archermind.schedule.R;


public class HomeScreen extends TabActivity implements OnTabChangeListener {
    /** Called when the activity is first created. */
	private TabHost mTabHost;
	private int mCurSelectTabIndex = 0;
	private final int INIT_SELECT = 0;
	private View tabSpecView;
	private View tabSelect ;
	private boolean flag = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        mTabHost = this.getTabHost();
        mTabHost.addTab(buildTabSpec("schedule", R.drawable.tab_schedule, new Intent(this, ScheduleScreen.class)));
        mTabHost.addTab(buildTabSpec("dynamic", R.drawable.tab_dynamic, new Intent(this, DynamicScreen.class)));
        mTabHost.addTab(buildTabSpec("friend", R.drawable.tab_friend, new Intent(this, FriendScreen.class)));
        mTabHost.setCurrentTab(0);
        mTabHost.setOnTabChangedListener(this);
        tabSelect = (View) findViewById(R.id.tabselect_cursor);
    }
    
      
      private TabSpec buildTabSpec(String tag, int iconId, Intent intent) {
  		tabSpecView = (LinearLayout) LayoutInflater.from(this).inflate(
  				R.layout.tab_item_view, null);
  		ImageView icon = (ImageView) tabSpecView.findViewById(R.id.imageview);
  		icon.setImageResource(iconId);
  		TabSpec tabSpec = this.mTabHost.newTabSpec(tag).setIndicator(tabSpecView).setContent(intent);
  		return tabSpec;
  	}
      
      
      
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(!flag) {
			moveTopSelect(INIT_SELECT);
			flag = true;
		}
	}


	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		if (tabId.equalsIgnoreCase("schedule")) {
			moveTopSelect(0);
		}else if (tabId.equalsIgnoreCase("dynamic")){
			moveTopSelect(1);
		}else if (tabId.equalsIgnoreCase("friend")){
			moveTopSelect(2);
		}
		
	}
	
	 public void moveTopSelect(int selectIndex) {
	        // 起始位置中心点
	        int startMid = ((View) getTabWidget().getChildAt(mCurSelectTabIndex)).getLeft() + ((View) getTabWidget().getChildAt(mCurSelectTabIndex)).getWidth() / 2;
	        // 目标位置中心点
	        int endMid = ((View) getTabWidget().getChildAt(selectIndex)).getLeft() + ((View) getTabWidget().getChildAt(selectIndex)).getWidth() / 2;
	        TranslateAnimation animation = new TranslateAnimation(startMid, endMid, 0, 0);
	        animation.setDuration(200);
	        animation.setFillAfter(true);
	        tabSelect.bringToFront();
	        tabSelect.startAnimation(animation);
	        mCurSelectTabIndex = selectIndex;
	    }
	 
}
