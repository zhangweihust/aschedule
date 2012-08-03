package com.archermind.schedule.Adapters;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Image.SmartImageView;
import com.archermind.schedule.Model.ScheduleBean;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.DateTimeUtils;

public class DynamicScheduleAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private OnClickListener listener;
	private List<ScheduleBean> list;

	public DynamicScheduleAdapter(final Context context, List<ScheduleBean> list) {
		ScheduleApplication.LogD(DynamicScheduleAdapter.class, "DynamicScheduleAdapter");
		inflater = LayoutInflater.from(context);
		listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(context, (String) v.getTag(), Toast.LENGTH_SHORT)
						.show();
				initPopWindow(context, v);
			}
		};
	}
	
	@Override
	public int getCount() {
		return list.size();
	}
	

	public void setList(List<ScheduleBean> list){
		this.list = list;
		this.notifyDataSetChanged();
	}
	
	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ScheduleItem item;
		if (convertView == null) {
			View view = inflater.inflate(R.layout.dynamic_schedule_item, null);
			item = new ScheduleItem();
			item.date = (TextView) view.findViewById(R.id.date);
			item.time = (TextView) view.findViewById(R.id.time);
			item.name = (TextView) view.findViewById(R.id.name);
			item.location = (TextView) view.findViewById(R.id.location);
			item.content = (TextView) view.findViewById(R.id.content);
			item.typeView = (ImageView) view.findViewById(R.id.type);
			item.avatar = (SmartImageView) view.findViewById(R.id.avatar);
			item.avatarLayout = view.findViewById(R.id.avatar_layout);
			item.commentsLayout = (LinearLayout)view.findViewById(R.id.feed_comments_thread);
			view.setTag(R.layout.dynamic_schedule_item, item);
			convertView = view;
		} else {
			item = (ScheduleItem) convertView.getTag(R.layout.dynamic_schedule_item);
		}
		final ScheduleBean data = list.get(position);
		if (data != null) {
			item.content.setText(data.getContent());
			long time = data.getTime();
			item.time.setText(DateTimeUtils.time2String("hh:mm", time));
			item.date.setText(DateTimeUtils.time2String("yyyy/MM/dd", time));
			String amORpm = DateTimeUtils.time2String("a", time);
			if ("上午".equals(amORpm)) {
				item.time.setBackgroundResource(R.drawable.am);
			} else if ("下午".equals(amORpm)) {
				item.time.setBackgroundResource(R.drawable.pm);
			}
			
			item.location.setText(data.getLocation());
			int t_id = data.getT_id();
			Cursor slaveCursor = ServiceManager.getDbManager().querySlaveShareSchedules(t_id);
			if (slaveCursor != null && slaveCursor.getCount() > 0){
				item.commentsLayout.setVisibility(View.VISIBLE);
				item.commentsLayout.removeAllViews();
				for (slaveCursor.moveToFirst(); !slaveCursor.isAfterLast(); slaveCursor.moveToNext()) {
					View commentView = inflater.inflate(R.layout.feed_comments_item, null);
					CommentItem commentItem = new CommentItem();
					commentItem.avatar = (SmartImageView) commentView.findViewById(R.id.comment_profile_photo);
					commentItem.content = (TextView) commentView.findViewById(R.id.comment_body);
					commentItem.time = (TextView) commentView.findViewById(R.id.comment_time);
					commentItem.avatar.setBackgroundResource(R.drawable.avatar);
					commentItem.content.setText(slaveCursor.getString(slaveCursor.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT)));
					long commentTime = slaveCursor.getLong(slaveCursor
							.getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME));
					commentItem.time.setText(DateTimeUtils.time2String("yyyy/MM/dd hh:mm:ss", commentTime));
					item.commentsLayout.addView(commentView);
				}
			} else {
				item.commentsLayout.setVisibility(View.GONE);
			}
			slaveCursor.close();
			item.avatarLayout.setVisibility(View.VISIBLE);
			item.avatar.setImageUrl("http://i0.sinaimg.cn/IT/cr/2010/0120/4105794628.jpg", R.drawable.avatar, R.drawable.avatar);
			switch (data.getType()) {
			case DatabaseHelper.SCHEDULE_EVENT_TYPE_NONE:
				item.typeView.setBackgroundResource(R.drawable.type_notice);
			case DatabaseHelper.SCHEDULE_EVENT_TYPE_NOTICE:
				item.typeView.setBackgroundResource(R.drawable.type_notice);
				break;
			case DatabaseHelper.SCHEDULE_EVENT_TYPE_ACTIVE:
				item.typeView.setBackgroundResource(R.drawable.type_active);
				break;
			case DatabaseHelper.SCHEDULE_EVENT_TYPE_APPOINTMENT:
				item.typeView.setBackgroundResource(R.drawable.type_appointment);
				break;
			case DatabaseHelper.SCHEDULE_EVENT_TYPE_TRAVEL:
				item.typeView.setBackgroundResource(R.drawable.type_travel);
				break;
			case DatabaseHelper.SCHEDULE_EVENT_TYPE_ENTERTAINMENT:
				item.typeView.setBackgroundResource(R.drawable.type_entertainment);
				break;
			case DatabaseHelper.SCHEDULE_EVENT_TYPE_EAT:
				item.typeView.setBackgroundResource(R.drawable.type_eat);
				break;
			case DatabaseHelper.SCHEDULE_EVENT_TYPE_WORK:
				item.typeView.setBackgroundResource(R.drawable.type_work);
				break;
			}
			EventArgs args = new EventArgs();
			args.putExtra("time", time);
			convertView.setTag(args);
		}
		return convertView;
	}

	private void initPopWindow(Context context, View v) {
		// 加载popupWindow的布局文件
		View contentView = LayoutInflater.from(context).inflate(
				R.layout.leave_message, null);
		// 声明一个弹出框
		final PopupWindow popupWindow = new PopupWindow(contentView,
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		// 为弹出框设定自定义的布局
		popupWindow.setOutsideTouchable(true);
		final EditText editText = (EditText) contentView
				.findViewById(R.id.editText1);
		/*
		 * 这个popupWindow.setFocusable(true);非常重要，如果不在弹出之前加上这条语句，你会很悲剧的发现，你是无法在
		 * editText中输入任何东西的
		 * 。该方法可以设定popupWindow获取焦点的能力。当设置为true时，系统会捕获到焦点给popupWindow
		 * 上的组件。默认为false哦.该方法一定要在弹出对话框之前进行调用。
		 */
		popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		popupWindow.setFocusable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		/*
		 * popupWindow.showAsDropDown（View view）弹出对话框，位置在紧挨着view组件
		 * showAsDropDown(View anchor, int xoff, int yoff)弹出对话框，位置在紧挨着view组件，x y
		 * 代表着偏移量 showAtLocation(View parent, int gravity, int x, int y)弹出对话框
		 * parent 父布局 gravity 依靠父布局的位置如Gravity.CENTER x y 坐标值
		 */
		popupWindow.showAsDropDown(v);

		Button joinBtn = (Button) contentView.findViewById(R.id.joinBtn);
		joinBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popupWindow.dismiss();
			}
		});

		Button forwardBtn = (Button) contentView.findViewById(R.id.forwardBtn);
		forwardBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popupWindow.dismiss();
			}
		});

		Button publishBtn = (Button) contentView.findViewById(R.id.publishBtn);
		publishBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popupWindow.dismiss();
			}
		});
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
		@Override
		public void run() {
		InputMethodManager m = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		}
		}, 500);
		
	}
	

	private class ScheduleItem {
		private TextView date;
		private TextView name;
		private TextView location;
		private TextView time;
		private TextView content;
		private ImageView typeView;
		private SmartImageView avatar;
		private int type;
		private View avatarLayout;
		private LinearLayout commentsLayout;
	}
	
	private class CommentItem{
		private SmartImageView avatar;
		private TextView content;
		private TextView time;
	}

}
