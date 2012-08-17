package com.archermind.schedule.Adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.archermind.schedule.R;
import com.archermind.schedule.Image.SmartImageView;
import com.archermind.schedule.Model.Friend;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Screens.Screen;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.ListViewUtil;
import com.archermind.schedule.Utils.ServerInterface;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class FriendContactAdapter extends BaseAdapter implements OnClickListener{

	private Context context;

	protected ArrayList<ListElement> resultList;

	private LayoutInflater layoutInflater;
	
	private ServerInterface serverInterface;
	private DatabaseManager database;
	private FriendAdapter friendAdapter;
//	private int friendContactIndex;
	private int friendContactUseIndex;
	private ListView listView;
	public FriendContactAdapter(Context context, ListView listView) {
		super();
		this.context = context;
		this.listView = listView;
		this.layoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
		this.resultList = new ArrayList<ListElement>();
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
	
	public void setOtherAdapter(FriendAdapter friendAdapter){
		this.friendAdapter = friendAdapter;
	}
	
//	public void addFriendContact(ListElement element){
//		resultList.add(friendContactIndex, element);
//	}
	
	public void addLastFriendContactUse(ListElement element){
		resultList.add(friendContactUseIndex, element);
		friendContactUseIndex++;
	}
	
	public void addFristFriendContactUse(ListElement element){
		resultList.add(1, element);
		friendContactUseIndex++;
	}
	
	public void removeFriendContactUse(ListElement element){
		resultList.remove(element);
	}
	
	public void setFriendContactUseIndex(int friendContactUseIndex){
		this.friendContactUseIndex = friendContactUseIndex;
	}
	
	public int getFriendContactUseIndex(){
		return this.friendContactUseIndex;
	}
	
	@Override
	public int getCount() {
	return this.resultList.size();
	}

	@Override
	public Object getItem(int position) {
	return this.resultList.get(position);
	}

	@Override
	public long getItemId(int position) {
	return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		return this.resultList.get(position).getViewForListElement(layoutInflater, context, view);
	}

	public void addList(List<ListElement> elements) {
		this.resultList.addAll(elements);
	}

//	@Override
//	public boolean isEnabled(int position) {
//		return this.resultList.get(position).isClickable();
//	}

	public void addTitleHeaderItem(String text) {
		TitleListElement element = new TitleListElement();
		element.setText(text);
		this.resultList.add(element);
	}

	public interface ListElement {
		public int getLayoutId();

		public boolean isClickable();

		public View getViewForListElement(LayoutInflater layoutInflater,Context context, View view);
	}
	
	public class TitleListElement implements ListElement {

		private String title;

		public void setText(String title) {
			this.title = title;
		}

		@Override
		public int getLayoutId() {
			return R.layout.friend_contact_title_item;
		}

		@Override
		public boolean isClickable() {
			return false;
		}

		@Override
		public View getViewForListElement(LayoutInflater layoutInflater,Context context, View view) {
			SectionHolderView sectionHolderView = null;
//			if(view == null){
				sectionHolderView = new SectionHolderView();
				view = layoutInflater.inflate(getLayoutId(), null);
				sectionHolderView.textInfo = (TextView) view.findViewById(R.id.TextInfo);
				view.setTag(sectionHolderView);
//			}else{
//				sectionHolderView = (SectionHolderView) view.getTag();
//			}
			sectionHolderView.textInfo.setText(title);
			return view;
		}
	}
	public class ContentListElement implements ListElement {

		private Friend friend = null;
		public void setFriend(Friend friend) {
			this.friend = friend;
		}
		
		public Friend getFriend(){
			return this.friend;
		}
		
		@Override
		public int getLayoutId() {
			return R.layout.friend_item;
		}

		@Override
		public View getViewForListElement(LayoutInflater layoutInflater,Context context, View view) {
			ContentHolderView contentHolderView = null;
//			if(view == null){
				contentHolderView = new ContentHolderView();
				view = layoutInflater.inflate(getLayoutId(), null);
				contentHolderView.headImg = (SmartImageView) view.findViewById(R.id.head_image);
				contentHolderView.name = (TextView) view.findViewById(R.id.name);
				contentHolderView.friend_button2 = (Button) view.findViewById(R.id.friend_button2);
				contentHolderView.friend_button2.setVisibility(View.VISIBLE);
				contentHolderView.friend_button2.setOnClickListener(FriendContactAdapter.this);
				view.setTag(contentHolderView);
//			}else{
//				System.out.println("tag = "+view.getTag());
//				contentHolderView = (ContentHolderView) view.getTag();
//			}
			if(friend != null){
				contentHolderView.name.setText(friend.getName());
				if(Constant.FriendType.friend_contact_use == friend.getType()){
					contentHolderView.friend_button2.setText(context.getResources().getString(R.string.friend_add));
					contentHolderView.friend_button2.setTag(this);
					contentHolderView.headImg.setImageUrl(friend.getHeadImagePath(), R.drawable.friend_item_img, R.drawable.friend_item_img);
				}else if(Constant.FriendType.friend_contact == friend.getType()){
					contentHolderView.friend_button2.setText(context.getResources().getString(R.string.friend_invite));
					contentHolderView.friend_button2.setTag(this);
				}
			}
			return view;
		}

		@Override
		public boolean isClickable() {
			return true;
		}

	}
	
	public class SectionHolderView{
		private TextView textInfo;
	}
	
	public class ContentHolderView{
		private SmartImageView headImg;
		private TextView name;
		private Button friend_button2;
	}
	 private void goToShare() {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			String textMessage = "邀请好友阿";
			intent.putExtra(Intent.EXTRA_TEXT, textMessage);
			context.startActivity(Intent.createChooser(intent,"邀请好友"));
		}
	 
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		ContentListElement element = (ContentListElement) v.getTag();
		Friend friend = element.getFriend();
		switch(friend.getType()){
		case Constant.FriendType.friend_contact_use:	
			if(0 == serverInterface.inviteFriend(String.valueOf(ServiceManager.getUserId()), friend.getId())){	
				Toast.makeText(context, "正在添加好友...", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(context, "添加好友失败", Toast.LENGTH_LONG).show();
			}
			break;
		case Constant.FriendType.friend_contact:
			goToShare();

			break;
		}
		
//		friendAdapter.getFriends().add(friend);
//		friendAdapter.notifyDataSetChanged();
//		ListViewUtil.setListViewHeightBasedOnChildren(friendAdapter.getListView());
//		
//		removeFriendContactUse(element);
//		System.out.println("count = "+getCount());
//		notifyDataSetChanged();
//		ListViewUtil.setListViewHeightBasedOnChildren(getListView());
	}
}
