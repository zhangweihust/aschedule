
package com.archermind.schedule.Screens;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;

import com.archermind.schedule.R;
import com.archermind.schedule.ScheduleApplication;
import com.archermind.schedule.Services.AlarmServiceReceiver;
import com.archermind.schedule.Services.ServiceManager;

public class LoadingScreen extends Screen {

    private ImageView mImageView;

    private AnimationDrawable mAnimaition;
    
    private boolean flag = false;

    public LoadingScreen() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
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

        // 下面这一段是当servicemanager被关闭的时候，自动重新启动的
        Intent myIntent = new Intent(this, AlarmServiceReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, myIntent, 0);

        long firstime = SystemClock.elapsedRealtime();
        AlarmManager am = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);

        // 10秒一个周期，不停的发送广播
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime, 10 * 1000, sender);

        ScheduleApplication.LogD(getClass(), "userid = "+ServiceManager.getUserId());
        
//        if (!ServiceManager.isUserLogining(ServiceManager.getUserId())) {
//            
//            ServiceManager.setUserId(0);
//        }

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
                    if(!flag){
                    	 Intent it = new Intent(LoadingScreen.this, HomeScreen.class);
                         startActivity(it);
                         finish();
                    }
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAnimaition.isRunning()) {

            mAnimaition.stop();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        mAnimaition.start();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            return true;
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_HOME){
        	flag = true;
        	this.finish();
        }
        return super.dispatchKeyEvent(event);
    }
    
    @Override
    public void onAttachedToWindow () {
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
        super.onAttachedToWindow();
    }
    

}
