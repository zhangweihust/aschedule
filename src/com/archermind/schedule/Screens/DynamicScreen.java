package com.archermind.schedule.Screens;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
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
	        mTabHost.addTab(buildTabSpec("friends_dynamic", new Intent(this, FriendsDyamicScreen.class)));
	        mTabHost.addTab(buildTabSpec("my_dynamic", new Intent(this, MyDynamicScreen.class)));
	        mTabHost.setCurrentTab(0);
	        mTabHost.setOnTabChangedListener(this);
	        HomeScreen.setmChildTabHost(mTabHost);
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