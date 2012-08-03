package com.archermind.schedule.Adapters;

import java.util.ArrayList;
import java.util.List;

import com.archermind.schedule.R;
import com.archermind.schedule.Adapters.FriendContactAdapter.ContentHolderView;
import com.archermind.schedule.Model.Friend;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.ServerInterface;

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
	private ServerInterface serverInterface;
	private DatabaseManager database;
	
	public FriendAdapter(Context context, List<Friend>friends){
		this.context = context;
		this.friends = friends;
		this.layoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
		serverInterface = new ServerInterface();
		database = ServiceManager.getDbManager();
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
			holderView.friend_button1.setOnClickListener(this);
			holderView.friend_button2.setOnClickListener(this);
			convertView.setTag(holderView);
		}else{
			holderView = (HolderView) convertView.getTag();
		}
		Friend friend = friends.get(position);
		if(friend != null){
			holderView.name.setText(friend.getTelephone());
			if(Constant.FriendType.friend_Ignore == friend.getType()){
				holderView.friend_button1.setEnabled(false);
			}
			holderView.friend_button1.setText("屏蔽");
			holderView.friend_button1.setTag(friend);
			holderView.friend_button2.setText("删除");
			holderView.friend_button2.setTag(friend);
			
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
		Friend friend = (Friend) v.getTag();
		switch( v.getId()){
		case R.id.friend_button1:
			database.ignoreFriend(friend.getId());
			v.setEnabled(false);	
			serverInterface.shieldFriend("3", friend.getId().replace("\"", ""));					
			break;
		case R.id.friend_button2:
			friends.remove(friend);
			database.deleteFriend(friend.getId());
			notifyDataSetChanged();			
			serverInterface.removeFriend("3", friend.getId().replace("\"", ""));
			break;
		}
	}

}
