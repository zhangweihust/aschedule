package com.archermind.schedule.Model;

import com.archermind.schedule.Utils.Constant;

public class Friend {
	private int id;
	private String telephone;
	private String name;
	private String headImagePath;
	private int type = Constant.FriendType.friend_contact;

	public Friend(int id, String telephone, String name, String headImagePath, int type){
		this.id = id;
		this.telephone = telephone;
		this.name = name;
		this.telephone = telephone;
		this.headImagePath = headImagePath;
		this.type = type;
	}
	public Friend(int id, String telephone, int type){
		this.id = id;
		this.telephone = telephone;
		this.type = type;
	}
	public int getId(){
		return this.id;
	}
	public void setId(int id){
		this.id = id;
	}
	public String getTelephone(){
		return this.telephone;
	}
	public void setTelephone(String telephone){
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
}
