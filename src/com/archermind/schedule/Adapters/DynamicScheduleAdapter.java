package com.archermind.schedule.Adapters;

import java.util.ArrayList;
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
import com.archermind.schedule.Model.AsyncScheduleLoader;
import com.archermind.schedule.Model.AsyncScheduleLoader.ScheduleCallback;
import com.archermind.schedule.Model.ScheduleBean;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.DateTimeUtils;
import com.archermind.schedule.Views.XListView;

public class DynamicScheduleAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<ScheduleBean> list;

    private XListView mXList;

    private   AsyncScheduleLoader asyncScheduleLoader;

    private String TAG = "DynamicScheduleAdapter";
    
    public DynamicScheduleAdapter(final Context context, List<ScheduleBean> list, XListView xlist) {
        ScheduleApplication.LogD(DynamicScheduleAdapter.class, "DynamicScheduleAdapter");
        inflater = LayoutInflater.from(context);

        mXList = xlist;
        asyncScheduleLoader = new AsyncScheduleLoader();
     
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

        Log.i(TAG, "scheduleCallback" + "position is " + position);

        if (convertView == null) {

            Log.i(TAG, "scheduleCallback" + "create View is " + position);

            View view = inflater.inflate(R.layout.dynamic_schedule_item, null);
            item = new ScheduleItem();
            item.date = (TextView)view.findViewById(R.id.date);
            item.time = (TextView)view.findViewById(R.id.time);
            item.name = (TextView)view.findViewById(R.id.name);
            item.location = (TextView)view.findViewById(R.id.location);
            item.content = (TextView)view.findViewById(R.id.content);
            item.typeView = (ImageView)view.findViewById(R.id.type);
            item.avatar = (SmartImageView)view.findViewById(R.id.avatar);
            item.avatarLayout = view.findViewById(R.id.avatar_layout);
            item.commentsLayout = (LinearLayout)view.findViewById(R.id.feed_comments_thread);
            view.setTag(R.layout.dynamic_schedule_item, item);
            convertView = view;

        } else {

            item = (ScheduleItem)convertView.getTag(R.layout.dynamic_schedule_item);
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

            // Cursor slaveCursor =
            // ServiceManager.getDbManager().querySlaveShareSchedules(t_id);
            // mXList.setTag(item);
            item.commentsLayout.setTag(t_id);

            // Cursor slaveCursor = null;
            Cursor slaveCursor = (Cursor)asyncScheduleLoader.loadSchedule(inflater,
                    item.commentsLayout, t_id, new ScheduleCallback() {

                        public void scheduleCallback(ArrayList<View> listViews, int t_id) {

                            if (listViews != null) {
                                Log.i(TAG, "scheduleCallback" + t_id + "number of cursor is "
                                        + listViews.size());

                            } else {
                                Log.i(TAG, "scheduleCallback" + t_id + "number of cursor is null");

                            }

                            LinearLayout commentsLayout = (LinearLayout)mXList
                                    .findViewWithTag(t_id);

                            if (commentsLayout != null) {
                                Log.i(TAG, "scheduleCallback" + t_id + "commentsLayout is "
                                        + commentsLayout.getId());

                            } else {
                                Log.i(TAG, "scheduleCallback" + t_id + "commentsLayout is null");

                            }

                            // commentsLayout.getId();
                            if ((commentsLayout != null) && (listViews != null)
                                    && (listViews.size() > 0)) {
                                commentsLayout.setVisibility(View.VISIBLE);
                                commentsLayout.removeAllViews();

                                for (int i = 0; i < listViews.size(); i++) {

                                    commentsLayout.addView(listViews.get(i));
                                }

                            }
                        }
                    });

            if (slaveCursor != null && slaveCursor.getCount() > 0) {
                // item.commentsLayout.setVisibility(View.VISIBLE);
                // item.commentsLayout.removeAllViews();
                // for (slaveCursor.moveToFirst(); !slaveCursor.isAfterLast();
                // slaveCursor
                // .moveToNext()) {
                // View commentView =
                // inflater.inflate(R.layout.feed_comments_item, null);
                // CommentItem commentItem = new CommentItem();
                // commentItem.avatar = (SmartImageView)commentView
                // .findViewById(R.id.comment_profile_photo);
                // commentItem.content =
                // (TextView)commentView.findViewById(R.id.comment_body);
                // commentItem.time =
                // (TextView)commentView.findViewById(R.id.comment_time);
                // commentItem.avatar.setBackgroundResource(R.drawable.avatar);
                // commentItem.content.setText(slaveCursor.getString(slaveCursor
                // .getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT)));
                // long commentTime = slaveCursor.getLong(slaveCursor
                // .getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME));
                // commentItem.time.setText(DateTimeUtils.time2String("yyyy/MM/dd hh:mm:ss",
                // commentTime));
                // item.commentsLayout.addView(commentView);
                // }
            } else {
                item.commentsLayout.setVisibility(View.GONE);
            }

            if (slaveCursor != null) {
                slaveCursor.close();
            }

            item.avatarLayout.setVisibility(View.VISIBLE);
            item.avatar.setImageUrl("http://i0.sinaimg.cn/IT/cr/2010/0120/4105794628.jpg",
                    R.drawable.avatar, R.drawable.avatar);
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
