package com.archermind.schedule.Screens;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.archermind.schedule.R;


public class HomeScreen extends TabActivity implements OnTabChangeListener {
    /** Called when the activity is first created. */
	private TabHost mTabHost;
	private int mCurSelectTabIndex = 0;
	private final int INIT_SELECT = 0;
	private RelativeLayout tabSpecView;
	private View tabSelect ;
	private boolean flag = false;
	private Button menuBtn, addBtn;
	private RadioButton  myDynamicBtn, friendsDynamicBtn;
	private RadioGroup tabWidget;
	private TextView titleText;
	private ImageView titleImage;
	private static TabHost mChildTabHost;
	private Button titleAddBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        initView();
        mTabHost.addTab(buildTabSpec("schedule", R.drawable.tab_schedule, new Intent(this, ScheduleScreen.class)));
        mTabHost.addTab(buildTabSpec("dynamic", R.drawable.tab_dynamic, new Intent(this, DynamicScreen.class)));
        mTabHost.addTab(buildTabSpec("friend", R.drawable.tab_friend, new Intent(this, FriendScreen.class)));
        mTabHost.setCurrentTab(0);
        mTabHost.setOnTabChangedListener(this);
        tabSelect = (View) findViewById(R.id.tabselect_cursor);
        titleAddBtn= (Button) findViewById(R.id.title_bar_add_button);
        titleAddBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent mIntent = new Intent(HomeScreen.this,NewScheduleScreen.class);
				startActivity(mIntent);
			}
		});
        
    }
    
    private void initView(){
    	mTabHost = this.getTabHost();
        menuBtn = (Button) findViewById(R.id.title_bar_menu_button);
        addBtn = (Button) findViewById(R.id.title_bar_add_button);
        myDynamicBtn = (RadioButton) findViewById(R.id.tab_widget_my_dynamic);
        friendsDynamicBtn = (RadioButton) findViewById(R.id.tab_widget_friends_dynamic);
        titleText = (TextView) findViewById(R.id.title_bar_title_text);
        titleImage = (ImageView) findViewById(R.id.title_bar_title_image);
        tabWidget = (RadioGroup) findViewById(R.id.title_bar_tab_widget);
        tabWidget.setVisibility(View.INVISIBLE);
        titleText.setVisibility(View.INVISIBLE);
        friendsDynamicBtn.setChecked(true);
		tabWidget.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkId) {
				if (checkId == myDynamicBtn.getId()) {
					mChildTabHost.setCurrentTab(1);
				} else if (checkId == friendsDynamicBtn.getId()){
					mChildTabHost.setCurrentTab(0);
				}
			}
		});
		menuBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(HomeScreen.this,RegisterScreen.class));
			}
		});
		
		addBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		
    }  

	public static void setmChildTabHost(TabHost tabHost) {
		   mChildTabHost = tabHost;
	}

	private TabSpec buildTabSpec(String tag, int iconId, Intent intent) {
  		tabSpecView = (RelativeLayout) LayoutInflater.from(this).inflate(
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
			tabWidget.setVisibility(View.INVISIBLE);
			titleText.setVisibility(View.INVISIBLE);
			titleImage.setVisibility(View.VISIBLE);
		}else if (tabId.equalsIgnoreCase("dynamic")){
			moveTopSelect(1);
			tabWidget.setVisibility(View.VISIBLE);
			titleText.setVisibility(View.INVISIBLE);
			titleImage.setVisibility(View.INVISIBLE);
		}else if (tabId.equalsIgnoreCase("friend")){
			moveTopSelect(2);
			tabWidget.setVisibility(View.INVISIBLE);
			titleText.setVisibility(View.VISIBLE);
			titleText.setText(R.string.tab_name_friend);
			titleImage.setVisibility(View.INVISIBLE);
		}
		
	}
	
	 public void moveTopSelect(int selectIndex) {
	        // 起始位置中心点
	        int startMid = ((View) getTabWidget().getChildAt(mCurSelectTabIndex)).getLeft() + ((ViewGroup)getTabWidget().getChildAt(mCurSelectTabIndex)).getChildAt(0).getLeft() + ((ViewGroup)getTabWidget().getChildAt(mCurSelectTabIndex)).getChildAt(0).getWidth()/7;
	        // 目标位置中心点
	        int endMid = ((View) getTabWidget().getChildAt(selectIndex)).getLeft() + ((ViewGroup)getTabWidget().getChildAt(selectIndex)).getChildAt(0).getLeft()+ ((ViewGroup)getTabWidget().getChildAt(selectIndex)).getChildAt(0).getWidth()/7;
	        TranslateAnimation animation = new TranslateAnimation(startMid, endMid, 0, 0);
	        animation.setDuration(200);
	        animation.setFillAfter(true);
	        tabSelect.bringToFront();
	        tabSelect.startAnimation(animation);
	        mCurSelectTabIndex = selectIndex;
	    }
	 
	 
	 
}
