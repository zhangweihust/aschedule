package com.archermind.schedule.Adapters;

import java.util.ArrayList;
import java.util.List;

import com.archermind.schedule.R;
import com.archermind.schedule.Adapters.FriendContactAdapter.ContentHolderView;
import com.archermind.schedule.Model.Friend;
import com.archermind.schedule.Utils.Constant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FriendAdapter extends BaseAdapter implements OnClickListener{
	private List<Friend> friends = new ArrayList<Friend>();
	private LayoutInflater layoutInflater;
	private Context context;
	
	public FriendAdapter(Context context, List<Friend>friends){
		this.context = context;
		this.friends = friends;
		this.layoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return friends.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return friends.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		HolderView holderView = null;
		if(convertView == null){
			holderView = new HolderView();
			convertView = layoutInflater.inflate(R.layout.friend_item, null);
			holderView.friend_layout = (LinearLayout) convertView.findViewById(R.id.friend_layout);
			holderView.friend_layout.setBackgroundColor(0xebeaea);
			holderView.headImg = (ImageView) convertView.findViewById(R.id.head_image);
			holderView.name = (TextView) convertView.findViewById(R.id.name);
			holderView.friend_button1 = (Button) convertView.findViewById(R.id.friend_button1);
			holderView.friend_button2 = (Button) convertView.findViewById(R.id.friend_button2);
			convertView.setTag(holderView);
		}else{
			holderView = (HolderView) convertView.getTag();
		}
		Friend friend = friends.get(position);
		if(friend != null){
			holderView.name.setText(friend.getName());
			holderView.friend_button1.setText(context.getResources().getString(R.string.friend_add));
			holderView.friend_button2.setText(context.getResources().getString(R.string.friend_add));
			
		}
		return convertView;
	}
	
	public class HolderView{
		private LinearLayout friend_layout;
		private ImageView headImg;
		private TextView name;
		private Button friend_button1;
		private Button friend_button2;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch((Integer) v.getTag()){
		case R.id.friend_button1:
			break;
		case R.id.friend_button2:
			break;
		}
	}

}
