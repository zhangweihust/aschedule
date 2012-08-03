package com.archermind.schedule.Adapters;

import java.util.ArrayList;
import java.util.List;

import com.archermind.schedule.R;
import com.archermind.schedule.Model.EventTypeItem;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EventTypeItemAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<EventTypeItem> eventItems = new ArrayList<EventTypeItem>();

	public EventTypeItemAdapter(ArrayList<EventTypeItem> eventItems, Context context) {
		super();
		this.eventItems = eventItems;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		if (null != eventItems) {
			return eventItems.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		return eventItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.schedule_new_item, null);
			viewHolder = new ViewHolder();
			viewHolder.title = (TextView) convertView.findViewById(R.id.title);
			viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		EventTypeItem eventItem = eventItems.get(position);
		viewHolder.title.setText(eventItem.getTitle());
		// Log.i("eventitemAdapter", "--------imageid="+eventItem.getImageId());
		viewHolder.image.setImageResource(eventItem.getImageId());
		return convertView;
	}

	class ViewHolder {
		public TextView title;
		public ImageView image;
	}
}
