package com.archermind.schedule.Adapters;

import java.util.ArrayList;
import java.util.Map;

import com.archermind.schedule.Model.Friend;
import com.archermind.schedule.Screens.Screen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

public class FriendAdapter extends BaseAdapter {

	private LayoutInflater inflater;

	public FriendAdapter(Context context, Map<Integer, ArrayList<Friend>> cursorMap, Screen screen) {
		inflater = LayoutInflater.from(context);

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
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return super.getItemViewType(position);
	}
	
	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return super.getViewTypeCount();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}



}
