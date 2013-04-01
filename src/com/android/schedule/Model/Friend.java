package com.android.schedule.Model;

import com.android.schedule.Utils.Constant;

public class Friend {
	private String id;
	private String telephone;
	private String name;
	private String headImagePath;
	private String nick;
	private String from;
	private int type = Constant.FriendType.friend_contact;

	public Friend() {

	}
	public Friend(String telephone, String name, String headImagePath, int type) {
		this.telephone = telephone;
		this.name = name;
		this.telephone = telephone;
		this.headImagePath = headImagePath;
		this.type = type;
	}
	public Friend(String id, String telephone, int type) {
		this.id = id;
		this.telephone = telephone;
		this.type = type;
	}
	public String getId() {
		return this.id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTelephone() {
		return this.telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHeadImagePath() {
		return headImagePath;
	}

	public void setHeadImagePath(String headImagePath) {
		this.headImagePath = headImagePath;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getNick() {
		return this.nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}
	
	public String getFrom() {
		return this.from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String toString() {
		return "id =" + id + " telephone = " + telephone + " name = " + name
				+ " nike = " + nick;
	}
}
