package com.archermind.schedule.Events;

public interface IEventDispatcher {
	boolean add(IEventHandler handler);
	boolean remove(IEventHandler handler);
}
