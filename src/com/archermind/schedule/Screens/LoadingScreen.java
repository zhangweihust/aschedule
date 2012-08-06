
package com.archermind.schedule.Screens;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.archermind.schedule.R;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.IEventHandler;
import com.archermind.schedule.Services.EventService;
import com.archermind.schedule.Services.ServiceManager;

public class LoadingScreen extends Screen implements IEventHandler {
    /** Called when the activity is first created. */
    private Button btn;

    private TextView tv;

    EventService eventService;

    private ImageView mImageView;

    private AnimationDrawable mAnimaition;

    public LoadingScreen() {
        super();
        eventService = ServiceManager.getEventservice();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        eventService.add(this);

        mImageView = (ImageView)findViewById(R.id.ivloadingsthreepoint);

        // 设置动画背景
        mImageView.setBackgroundResource(R.anim.loading_show_hide);
        // 获得动画对象
        mAnimaition = (AnimationDrawable)mImageView.getBackground();

        new Thread() {
            public void run() {

                try {
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                handler.sendEmptyMessage(1);

            }
        }.start();

    }
    
    /**
     * 用Handler来更新UI
     */
    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {

            switch (msg.what) {

                case 1:

                    if (mAnimaition.isRunning()) {

                        mAnimaition.stop();
                    }

                    Intent it = new Intent(LoadingScreen.this, HomeScreen.class);
                    startActivity(it);
                    finish();

                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public boolean onEvent(Object sender, final EventArgs e) {
        switch (e.getType()) {
            case LOCAL_SCHEDULE_UPDATE:
                LoadingScreen.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // tv.setText((String)e.getExtra("message"));
                    }
                });
                break;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAnimaition.isRunning()) {

            mAnimaition.stop();

        }

        eventService.remove(this);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        
        mAnimaition.start();
    }

}
