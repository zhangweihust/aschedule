package com.android.schedule.Events;

public interface IEventDispatcher {
	boolean add(IEventHandler handler);
	boolean remove(IEventHandler handler);
}
