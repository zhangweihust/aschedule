package com.android.schedule.Dialog;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.android.schedule.Adapters.EventTypeItemAdapter;
import com.android.schedule.Model.EventTypeItem;
import com.android.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.R;

public class EventTypeDialog extends Dialog implements OnItemClickListener {

	// private Dialog eventTypeDialog;
	private GridView gridview;
	private ImageView img_selector;
	private EventTypeItemAdapter adapter;
	private String[] titles;
	private ArrayList<EventTypeItem> eventItems;
	private int mType;
	private int mCurSelectorIndex = 0;
	private long prePosition = -1;
	private Context mContext;
	private int x;
	private int y;
	private int height;

	private int[] images = new int[]{R.drawable.schedule_new_active,
			R.drawable.schedule_new_appointment,
			R.drawable.schedule_new_travel, R.drawable.type_entertainment,
			R.drawable.schedule_new_eat, R.drawable.schedule_new_work

	};

	public interface OnEventTypeSelectListener {
		void OnEventTypeSelect(EventTypeDialog eventTypeDialog, int mType);

		void onDismissListener(EventTypeDialog eventTypeDialog);

		void onShowListener(EventTypeDialog eventTypeDialog);
	}

	private OnEventTypeSelectListener mOnEventTypeSelectListener;

	public void setOnEventTypeSelectListener(OnEventTypeSelectListener l) {
		mOnEventTypeSelectListener = l;
	}

	public EventTypeDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;

	}

	public EventTypeDialog(Context context, int theme, int mType, int height) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		mContext = context;
		this.mType = mType;
		this.height = height;

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.schedule_event_item);
		img_selector = (ImageView) findViewById(R.id.img_selector);
		img_selector.setVisibility(View.INVISIBLE);
		gridview = (GridView) findViewById(R.id.mygridview);
		gridview.setOnItemClickListener(this);

		titles = mContext.getResources().getStringArray(R.array.schedule);
		eventItems = new ArrayList<EventTypeItem>();
		for (int i = 0; i < titles.length; i++) {
			eventItems.add(new EventTypeItem(titles[i], images[i]));
		}
		adapter = new EventTypeItemAdapter(eventItems, mContext);
		gridview.setAdapter(adapter);

		Window window = getWindow(); // 得到对话框
		// window.setWindowAnimations(R.style.EventdialogWindowAnim); //设置窗口弹出动画
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.height = height;
		window.setAttributes(wl);

	}

	public void setPosition(int x, int y) {

		// this.x =x;
		// this.y =y;
		Window window = getWindow(); // 得到对话框
		// window.setWindowAnimations(R.style.dialogWindowAnim); //设置窗口弹出动画
		WindowManager.LayoutParams wl = window.getAttributes();

		wl.x = x; // x小于0左移，大于0右移
		wl.y = y - 2; // y小于0上移，大于0下移
		// wl.height = height;
		window.setAttributes(wl);
	}

	public int getEventType() {
		return mType;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if (mType != -1) {
			img_selector.setVisibility(View.VISIBLE);
			moveTopSelect(mType - 1);
		}

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

		super.show();
		mOnEventTypeSelectListener.onShowListener(this);
	}

	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
		mOnEventTypeSelectListener.onDismissListener(this);

	}

	public void moveTopSelect(int selectIndex) {

		int startLeft = gridview.getChildAt(mCurSelectorIndex).getLeft()
				+ gridview.getChildAt(mCurSelectorIndex).getWidth() / 2
				- img_selector.getWidth() / 2;
		Log.i("eventType", "-------------");
		int endLeft = gridview.getChildAt(selectIndex).getLeft()
				+ gridview.getChildAt(selectIndex).getWidth() / 2
				- img_selector.getWidth() / 2;
		TranslateAnimation animation = new TranslateAnimation(startLeft,
				endLeft, 0, 0);
		animation.setDuration(100);
		animation.setFillAfter(true);
		img_selector.bringToFront();
		img_selector.startAnimation(animation);
		mCurSelectorIndex = selectIndex;
		Log.i("eventType", "-------------startLeft=" + startLeft + ", endLeft="
				+ endLeft);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if (prePosition == arg2) {
			if (img_selector.isShown()) {
				img_selector.clearAnimation();
				img_selector.setVisibility(View.GONE);
				mType = -1;
				Log.d("event popwindow", "----------INVISIBLE--");
			} else {
				setEventTypeSelector(arg2);
				Log.d("event popwindow", "----------VISIBLE--");
			}

		} else {
			prePosition = arg2;
			setEventTypeSelector(arg2);

		}

		mOnEventTypeSelectListener.OnEventTypeSelect(EventTypeDialog.this,
				mType);
		Log.d("event popwindow", "----------onItemClick---now------="
				+ prePosition);

	}

	private void setEventTypeSelector(int arg) {
		if (arg != -1) {
			// prePosition = mType -1;
			img_selector.setVisibility(View.VISIBLE);
			moveTopSelect(arg);
			switch (arg) {

				case 0 :
					mType = DatabaseHelper.SCHEDULE_EVENT_TYPE_ACTIVE;

					break;
				case 1 :
					mType = DatabaseHelper.SCHEDULE_EVENT_TYPE_APPOINTMENT;

					break;
				case 2 :
					mType = DatabaseHelper.SCHEDULE_EVENT_TYPE_TRAVEL;

					break;
				case 3 :
					mType = DatabaseHelper.SCHEDULE_EVENT_TYPE_ENTERTAINMENT;

					break;
				case 4 :
					mType = DatabaseHelper.SCHEDULE_EVENT_TYPE_EAT;

					break;
				case 5 :
					mType = DatabaseHelper.SCHEDULE_EVENT_TYPE_WORK;

					break;
			}

		}

	}

}
