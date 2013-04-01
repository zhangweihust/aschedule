package com.android.schedule.Services;

import java.util.concurrent.CopyOnWriteArrayList;

import android.util.Log;

import com.android.schedule.Events.EventArgs;
import com.android.schedule.Events.IEventDispatcher;
import com.android.schedule.Events.IEventHandler;

public class EventService implements IEventDispatcher, IService {

	private final CopyOnWriteArrayList<IEventHandler> eventHandlers;

	public EventService() {
		super();
		this.eventHandlers = new CopyOnWriteArrayList<IEventHandler>();
	}

	@Override
	public boolean add(IEventHandler handler) {
		return eventHandlers.add(handler);
	}

	@Override
	public boolean remove(IEventHandler handler) {
		return eventHandlers.remove(handler);
	}

	/* ===================== Dispatch events ======================== */
	public synchronized void onUpdateEvent(final EventArgs eargs) {
		for (final IEventHandler handler : this.eventHandlers) {
			new Thread(new Runnable() {
				public void run() {
					if (!handler.onEvent(this, eargs)) {
						Log.w(handler.getClass().getName(), "onEvent failed");
					}
				}
			}).start();
		}
	}

	@Override
	public boolean start() {
		return true;
	}

	@Override
	public boolean stop() {
		eventHandlers.clear();
		return true;
	}
}
