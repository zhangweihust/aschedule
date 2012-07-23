package com.archermind.schedule.Screens;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import com.archermind.schedule.R;

public class DynamicScreen extends TabActivity implements OnTabChangeListener{
	private TabHost mTabHost;
	private RelativeLayout tabSpecView;
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.dynamic_screen);
	        mTabHost = this.getTabHost();
	        mTabHost.addTab(buildTabSpec("my_dynamic", new Intent(this, MyDynamicScreen.class)));
	        mTabHost.addTab(buildTabSpec("friends_dynamic", new Intent(this, FriendsDyamicScreen.class)));
	        mTabHost.setCurrentTab(0);
	        mTabHost.setOnTabChangedListener(this);
	        HomeScreen.setmChildTabHost(mTabHost);
	 }

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		
	}
	
	  private TabSpec buildTabSpec(String tag, Intent intent) {
		  tabSpecView = (RelativeLayout) LayoutInflater.from(this).inflate(
	  				R.layout.tab_item_view, null);
	  		TabSpec tabSpec = this.mTabHost.newTabSpec(tag).setIndicator(tabSpecView).setContent(intent);
	  		return tabSpec;
	  	}
	      
}