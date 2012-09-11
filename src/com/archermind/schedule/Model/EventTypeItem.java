package com.archermind.schedule.Model;

public class EventTypeItem {
	private String title;
	private int imageId;

	public EventTypeItem() {
		super();
	}

	public EventTypeItem(String title, int imageId) {
		super();
		this.title = title;
		this.imageId = imageId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getImageId() {
		return imageId;
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}
}
