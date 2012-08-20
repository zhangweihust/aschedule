package com.archermind.schedule.Adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.archermind.schedule.R;
import com.archermind.schedule.Adapters.FriendContactAdapter.ContentListElement;
import com.archermind.schedule.Adapters.FriendContactAdapter.ListElement;
import com.archermind.schedule.Image.SmartImageView;
import com.archermind.schedule.Model.Friend;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.ListViewUtil;
import com.archermind.schedule.Utils.ServerInterface;

public class FriendAdapter extends BaseAdapter implements OnClickListener{
	private List<Friend> friends = new ArrayList<Friend>();
	private LayoutInflater layoutInflater;
	private Context context;
	private ServerInterface serverInterface;
	private DatabaseManager database;
	private FriendContactAdapter friendContactAdapter;
	private ListView listView;
	private Dialog dialog;
	
	public FriendAdapter(Context context, List<Friend>friends, ListView listView){
		this.context = context;
		this.friends = friends;
		this.listView = listView;
		this.layoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
		serverInterface = new ServerInterface();
		database = ServiceManager.getDbManager();
	}
	
	public void refresh(){
		notifyDataSetChanged();	
	    ListViewUtil.setListViewHeightBasedOnChildren(listView);
	}
	
	public ListView getListView(){
		return this.listView;
	}
	
	public void setOtherAdapter(FriendContactAdapter friendContactAdapter){
		this.friendContactAdapter = friendContactAdapter;
	}
	
	public void setFriends(List<Friend> friends){
		this.friends = friends;
	}
	
	public List<Friend> getFriends(){
		return this.friends;
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
			holderView.friend_layout.setOnClickListener(this);
			holderView.friend_layout.setBackgroundColor(0xebeaea);
			holderView.headImg = (SmartImageView) convertView.findViewById(R.id.head_image);
			holderView.name = (TextView) convertView.findViewById(R.id.name);
			convertView.setTag(holderView);
		}else{
			holderView = (HolderView) convertView.getTag();
		}
		Friend friend = friends.get(position);
		if(friend != null){
			String nick = friend.getNick();
			if(nick != null && !nick.equals("")){
				holderView.name.setText(nick);
			}else{
				holderView.name.setText(friend.getTelephone());
			}
			holderView.headImg.setImageUrl(friend.getHeadImagePath(),
                    R.drawable.friend_item_img, R.drawable.friend_item_img);
			holderView.friend_layout.setTag(friend);
			
		}
		return convertView;
	}
	
	public class HolderView{
		private LinearLayout friend_layout;
		private SmartImageView headImg;
		private TextView name;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Friend friend = (Friend) v.getTag();
		switch( v.getId()){
		case R.id.friend_layout:
			showDialog(friend);
			break;
		case R.id.friend_shield:
			dialog.dismiss();
			if(0 == serverInterface.shieldFriend(String.valueOf(ServiceManager.getUserId()), friend.getId())){
				database.ignoreFriend(friend.getId());
			}
			break;
		case R.id.friend_delete:
			dialog.dismiss();
			if(0 == serverInterface.removeFriend(String.valueOf(ServiceManager.getUserId()), friend.getId())){
				friends.remove(friend);
				refresh();
				database.deleteFriend(friend.getId());
				database.updateContactType(database.queryContactIdByTel(friend.getTelephone()), Constant.FriendType.friend_contact_use,friend.getId());
				
				FriendContactAdapter.ContentListElement element = friendContactAdapter.new ContentListElement();
				friend.setType(Constant.FriendType.friend_contact_use);
				element.setFriend(friend);
				friendContactAdapter.addFristFriendContactUse(element);
				friendContactAdapter.refresh();
			}
			break;
		}
	}
	
	public void showDialog(Friend friend){
		dialog = new Dialog(context, R.style.CustomDialog);
		dialog.setContentView(R.layout.friend_adapter_dialog);
		dialog.setCanceledOnTouchOutside(true);
		
		Button friend_shield = (Button)dialog.findViewById(R.id.friend_shield);
		Button friend_delete = (Button)dialog.findViewById(R.id.friend_delete);
		
		friend_shield.setOnClickListener(this);
		friend_delete.setOnClickListener(this);
		
		friend_shield.setTag(friend);
		friend_delete.setTag(friend);
		
		windowDeploy(0,0,LayoutParams.FILL_PARENT);
		dialog.show();
		
	}
	
	 private void windowDeploy(int x, int y,int width){
	        Window window = dialog.getWindow(); //得到对话框
	        window.setWindowAnimations(R.style.dialogWindowAnim); //设置窗口弹出动画
	        WindowManager.LayoutParams wl = window.getAttributes();
	        //根据x，y坐标设置窗口需要显示的位置
	        wl.x = x; //x小于0左移，大于0右移
	        wl.y = y; //y小于0上移，大于0下移 
	        wl.width = width;
//	        wl.alpha = 0.6f; //设置透明度
	        wl.gravity = Gravity.BOTTOM; //设置重力
	        window.setAttributes(wl);
	    }

}
