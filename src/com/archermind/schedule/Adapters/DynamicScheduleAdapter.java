package com.archermind.schedule.Adapters;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Image.SmartImageView;
import com.archermind.schedule.Model.ScheduleBean;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Task.AsyncScheduleLoader;
import com.archermind.schedule.Task.AsyncScheduleLoader.ScheduleCallback;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Views.XListView;

public class DynamicScheduleAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<ScheduleBean> list;

	private XListView mXList;

	private AsyncScheduleLoader asyncScheduleLoader;

	private String TAG = "DynamicScheduleAdapter";

	private Context mContext;

	public DynamicScheduleAdapter(final Context context,
			List<ScheduleBean> list, XListView xlist) {
		ScheduleApplication.LogD(DynamicScheduleAdapter.class,
				"DynamicScheduleAdapter");
		inflater = LayoutInflater.from(context);
		mContext = context;
		mXList = xlist;
		asyncScheduleLoader = new AsyncScheduleLoader();
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
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
			Log.i(TAG, "scheduleCallback" + "create View is " + position);
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
			item.commentsLayout = (LinearLayout) view
					.findViewById(R.id.feed_comments_thread);
			view.setTag(R.layout.dynamic_schedule_item, item);
			convertView = view;

		} else {

			item = (ScheduleItem) convertView
					.getTag(R.layout.dynamic_schedule_item);
		}

		final ScheduleBean data = list.get(position);
		if (data != null) {
			item.content.setText(data.getContent());
			//long time = data.getTime()*1000+1;
			long time = data.getTime();
			item.time.setText(DateTimeUtils.time2String("hh:mm", time));
			item.date.setText(DateTimeUtils.time2String("yyyy.MM.dd", time));
			String amORpm = DateTimeUtils.time2String("a", time);
			if ("上午".equals(amORpm)) {
				item.time.setBackgroundResource(R.drawable.am);
			} else if ("下午".equals(amORpm)) {
				item.time.setBackgroundResource(R.drawable.pm);
			}

			item.location.setText(data.getLocation());
			int t_id = data.getT_id();

			item.commentsLayout.setTag(t_id);

			asyncScheduleLoader.loadSchedule(mContext, inflater, t_id,
					new ScheduleCallback() {

						public void scheduleCallback(LinearLayout listViews,
								int t_id) {
							LinearLayout commentsLayout = (LinearLayout) mXList
									.findViewWithTag(t_id);
							if (commentsLayout != null) {
								Log.i(TAG,
										"scheduleCallback" + t_id
												+ "commentsLayout is "
												+ commentsLayout.getId());

							} else {
								Log.i(TAG, "scheduleCallback" + t_id
										+ "commentsLayout is null");

							}
							if ((commentsLayout != null) && (listViews != null)
									&& (listViews.getChildCount() > 0)) {
								commentsLayout.setVisibility(View.VISIBLE);
								commentsLayout.removeAllViews();
								commentsLayout.addView(listViews);
							}
						}
					});

			item.commentsLayout.setVisibility(View.GONE);
			item.avatarLayout.setVisibility(View.VISIBLE);
			Cursor friendCursor = ServiceManager.getDbManager().queryFriend(
					data.getUser_id());
			if (friendCursor != null && friendCursor.getCount() > 0) {
				friendCursor.moveToFirst();
				String nick = friendCursor.getString(friendCursor
						.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_NICK));
				item.name.setText(nick);
				item.avatar
						.setImageUrl(
								friendCursor
										.getString(friendCursor
												.getColumnIndex(DatabaseHelper.ASCHEDULE_FRIEND_PHOTO_URL)),
								R.drawable.avatar, R.drawable.avatar);
			}
			friendCursor.close();
			if (data.getUser_id() == ServiceManager.getUserId()) {
				ScheduleApplication.LogD(getClass(), "自己发的主贴，头像是自己"
						+ ServiceManager.getAvator_url());
				item.avatar.setImageUrl(ServiceManager.getAvator_url(),
						R.drawable.avatar, R.drawable.avatar);
			}
			switch (data.getType()) {
				case DatabaseHelper.SCHEDULE_EVENT_TYPE_NONE :
					item.typeView.setBackgroundResource(R.drawable.type_none);
					break;
//				case DatabaseHelper.SCHEDULE_EVENT_TYPE_NOTICE :
//					item.typeView.setBackgroundResource(R.drawable.type_notice);
//					break;
				case DatabaseHelper.SCHEDULE_EVENT_TYPE_ACTIVE :
					item.typeView.setBackgroundResource(R.drawable.type_active);
					break;
				case DatabaseHelper.SCHEDULE_EVENT_TYPE_APPOINTMENT :
					item.typeView
							.setBackgroundResource(R.drawable.type_appointment);
					break;
				case DatabaseHelper.SCHEDULE_EVENT_TYPE_TRAVEL :
					item.typeView.setBackgroundResource(R.drawable.type_travel);
					break;
				case DatabaseHelper.SCHEDULE_EVENT_TYPE_ENTERTAINMENT :
					item.typeView
							.setBackgroundResource(R.drawable.type_entertainment);
					break;
				case DatabaseHelper.SCHEDULE_EVENT_TYPE_EAT :
					item.typeView.setBackgroundResource(R.drawable.type_eat);
					break;
				case DatabaseHelper.SCHEDULE_EVENT_TYPE_WORK :
					item.typeView.setBackgroundResource(R.drawable.type_work);
					break;
			}
			EventArgs args = new EventArgs();
			args.putExtra("time", time);
			convertView.setTag(args);
		}
		return convertView;
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

	private class CommentItem {
		private SmartImageView avatar;
		private TextView content;
		private TextView time;
	}

}
