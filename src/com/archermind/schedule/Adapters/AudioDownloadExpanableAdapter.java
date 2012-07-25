package com.archermind.schedule.Adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.archermind.schedule.R;
import com.archermind.schedule.Model.Friend;
import com.archermind.schedule.Screens.Screen;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class AudioDownloadExpanableAdapter extends BaseExpandableListAdapter {

	private final int GROUP_ID_FRIEND = 0;
	private final int GROUP_ID_CONTACT_FRIEND = 1;
	private LayoutInflater inflater;

	List<String> group; // 组列表
	List<ArrayList<Friend>> child; // 子列表
	private Map<Integer, ArrayList<Friend>> childMap;

	public AudioDownloadExpanableAdapter(Context context, Map<Integer, ArrayList<Friend>> cursorMap, Screen screen) {
		inflater = LayoutInflater.from(context);
		setchildCursors(cursorMap);
		initializeData(context);
		childMap = new HashMap<Integer, ArrayList<Friend>>();
	}

	public void setchildCursors(Map<Integer, ArrayList<Friend>> cursorMap) {
		childMap = cursorMap;
	}


	/**
	 * 初始化组、子列表数据
	 */
	public void initializeData(Context context) {
		group = new ArrayList<String>();
		child = new ArrayList<ArrayList<Friend>>();
		String groupName = "";
		for (Integer groupId : childMap.keySet()) {
			ArrayList<Friend> child = childMap.get(groupId);
			if (GROUP_ID_FRIEND == groupId) {
//				groupName = context.getString(R.string.screen_download_tab_unfinished);
			} else if (GROUP_ID_CONTACT_FRIEND == groupId) {
//				groupName = context.getString(R.string.screen_download_tab_finished);
			}
			addAudioInfo(groupName, child);
		}

	}

	private void addAudioInfo(String g, ArrayList<Friend> valueItem) {
		group.add(g);
		child.add(valueItem);
	}

	private class ViewHolder {
		private TextView title;
		private TextView status;
		private TextView size;
		private TextView speed;
		private SeekBar downloadProgress;
		private View layout;
	}


	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		String string = group.get(groupPosition);
		View view = inflater.inflate(R.layout.friend_title_item, null);
//		TextView downGroupInfoView = (TextView) view.findViewById(R.id.screen_search_download_above);
		return view;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return child.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		final Friend childItem = child.get(groupPosition).get(childPosition);
		final ViewHolder viewHolder;
		//if (null == convertView) {
			convertView = inflater.inflate(R.layout.friend_item, null);
			viewHolder = new ViewHolder();
//			viewHolder.title = (TextView) convertView.findViewById(R.id.download_title);
//			convertView.setTag(R.layout.screen_search_download_child, audioDownloadItem);
//	    } else {
//	    	audioDownloadItem = (AudioDownloadItem) convertView.getTag(R.layout.screen_search_download_child);
//	    }
		BaseAdapter a;
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return child.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return group.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return group.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	

}
