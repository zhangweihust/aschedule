package com.archermind.schedule.Events;

public interface IEventHandler {
	boolean onEvent(Object sender, EventArgs e);
}
