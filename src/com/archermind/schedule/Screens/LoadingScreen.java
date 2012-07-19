package com.archermind.schedule.Screens;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.archermind.schedule.R;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.EventTypes;
import com.archermind.schedule.Events.IEventHandler;
import com.archermind.schedule.Services.EventService;
import com.archermind.schedule.Services.ServiceManager;

public class LoadingScreen extends Screen implements IEventHandler{
    /** Called when the activity is first created. */
	private Button btn;
	private TextView tv;
	EventService eventService;
	
	public LoadingScreen(){
		super();
		eventService = ServiceManager.getEventservice();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        eventService.add(this);
        btn = (Button) findViewById(R.id.button1);
        tv = (TextView) findViewById(R.id.text1);
        btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				eventService.onUpdateEvent(new EventArgs(EventTypes.EVENT_TEST).putExtra("message", "TEST"));
			}
		});
    }
    
	@Override
	public boolean onEvent(Object sender, final EventArgs e) {
		switch(e.getType()){
		case EVENT_TEST:
			LoadingScreen.this.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					tv.setText((String)e.getExtra("message"));
				}});
			break;
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		eventService.remove(this);
	}
	
}
