package com.archermind.schedule.Adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.archermind.schedule.R;
import com.archermind.schedule.Model.Friend;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Provider.DatabaseManager;
import com.archermind.schedule.Screens.Screen;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.Constant;
import com.archermind.schedule.Utils.ServerInterface;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class FriendContactAdapter extends BaseAdapter implements OnClickListener{

	private Context context;

	protected ArrayList<ListElement> resultList;

	private LayoutInflater layoutInflater;
	
	private ServerInterface serverInterface;
	private DatabaseManager database;
	public FriendContactAdapter(Context context) {
		super();
		this.context = context;
		this.layoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
		this.resultList = new ArrayList<ListElement>();
		serverInterface = new ServerInterface();
		database = ServiceManager.getDbManager();
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
		public void setfriend(Friend friend) {
			this.friend = friend;
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
				contentHolderView.headImg = (ImageView) view.findViewById(R.id.head_image);
				contentHolderView.name = (TextView) view.findViewById(R.id.name);
				contentHolderView.friend_button1 = (Button) view.findViewById(R.id.friend_button1);
				contentHolderView.friend_button1.setVisibility(View.GONE);
				contentHolderView.friend_button2 = (Button) view.findViewById(R.id.friend_button2);
				contentHolderView.friend_button2.setOnClickListener(FriendContactAdapter.this);
				view.setTag(contentHolderView);
//			}else{
//				System.out.println("tag = "+view.getTag());
//				contentHolderView = (ContentHolderView) view.getTag();
//			}
			if(friend != null){
				contentHolderView.name.setText(friend.getTelephone());
				if(Constant.FriendType.friend_contact_use == friend.getType()){
					System.out.println("friend_contact_use");
					contentHolderView.friend_button2.setText(context.getResources().getString(R.string.friend_add));
					contentHolderView.friend_button2.setTag(friend);
				}else if(Constant.FriendType.friend_contact == friend.getType()){
					System.out.println("friend_contact");
					contentHolderView.friend_button2.setText(context.getResources().getString(R.string.friend_invite));
					contentHolderView.friend_button2.setTag(friend);
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
		private ImageView headImg;
		private TextView name;
		private Button friend_button1;
		private Button friend_button2;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
//		System.out.println("*********************8");
		Friend friend = (Friend) v.getTag();
		switch(friend.getType()){
		case Constant.FriendType.friend_contact_use:	
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&friend.getId() = "+friend.getId());
			if(serverInterface.inviteFriend("3", "4") == 0){	
			}
			break;
		case Constant.FriendType.friend_contact:
			String result = serverInterface.getMessage("4");//返回主动加人的信息
			Dialog dialog = new Dialog(context);
			dialog.setContentView(R.layout.dialog);
			Button btn1 = (Button) dialog.findViewById(R.id.button1);
			Button btn2 = (Button) dialog.findViewById(R.id.button2);
			btn1.setTag(friend);
			btn2.setTag(friend);
			btn1.setOnClickListener(listen);
			btn2.setOnClickListener(listen);
			dialog.show();
			break;
		}
	}
	
	OnClickListener listen = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Friend friend = (Friend) v.getTag();
			switch(v.getId()){
				case R.id.button1:
					serverInterface.acceptFriend("4", "3");
					 ContentValues values = new ContentValues();
					 values.put(DatabaseHelper.ASCHEDULE_FRIEND_ID, "4");
					 values.put(DatabaseHelper.ASCHEDULE_FRIEND_TYPE, Constant.FriendType.friend_yes);
					 values.put(DatabaseHelper.ASCHEDULE_FRIEND_NUM, friend.getTelephone().replace("\"", ""));
					 database.addFriend(values);
					break;
				case R.id.button2:
					serverInterface.refuseFriend("4", "3");
					break;
				}
			}
	};
}
