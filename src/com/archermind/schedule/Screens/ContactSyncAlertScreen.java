package com.archermind.schedule.Screens;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.archermind.schedule.R;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.EventTypes;
import com.archermind.schedule.Services.ServiceManager;

public class ContactSyncAlertScreen extends Activity implements OnClickListener{

	private Button contactsyncalert_sync;
	private Button contactsyncalert_cancel;
	private Handler handler;
	private static final int SUCCESS = 0;
	private static final int FAILED = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_sync_alert);
		
		handler = new Handler()
		{
			public void handleMessage(android.os.Message msg) 
			{
				String str = "";
				if (msg.what == FAILED)
				{
					str = "同步失败!";
				}
				else if (msg.what == SUCCESS)
				{
					str = "同步成功!";
				}
				Toast.makeText(ContactSyncAlertScreen.this, str, Toast.LENGTH_SHORT).show();
			};
		};
		
		contactsyncalert_sync = (Button)findViewById(R.id.contactsyncalert_sync);
		contactsyncalert_cancel = (Button)findViewById(R.id.contactsyncalert_cancel);
		
		contactsyncalert_sync.setOnClickListener(this);
		contactsyncalert_cancel.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.contactsyncalert_sync:
			new Thread()
			{
				public void run() 
				{
					/* 要获取用户名 */
					if (ServiceManager.getContact().ContactSync(String.valueOf(ServiceManager.getUserId())))
					{
						handler.sendEmptyMessage(SUCCESS);
					}
					else
					{
						handler.sendEmptyMessage(SUCCESS);
					}
					ServiceManager.getEventservice().onUpdateEvent(new EventArgs(EventTypes.CONTACT_SYNC_SUCCESS));
					
				};
			}.start();
			break;
		case R.id.contactsyncalert_cancel:
			break;
		default:
			break;
		}
		ContactSyncAlertScreen.this.finish();
	}
	
}
