
package com.archermind.schedule.Screens;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.archermind.schedule.R;
import com.archermind.schedule.Views.SettingSlipSwitch;
import com.archermind.schedule.Views.SettingSlipSwitch.OnSwitchListener;

public class SettingScreen extends Screen implements OnClickListener {

    private Button mBtnOut;

    private SettingSlipSwitch mSlipSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        mBtnOut = (Button)findViewById(R.id.title_bar_setting_btn);
        mBtnOut.setOnClickListener(this);

        LinearLayout llayout = (LinearLayout)findViewById(R.id.datatime);
        llayout.setOnClickListener(this);

        mSlipSwitch = (SettingSlipSwitch)findViewById(R.id.settingslipswitch);
        mSlipSwitch.setImageResource(R.drawable.settingonandoff, R.drawable.settingonandoff,
                R.drawable.settingcover);
        mSlipSwitch.setSwitchState(true);
        mSlipSwitch.setOnSwitchListener(new OnSwitchListener() {

            @Override
            public void onSwitched(boolean isSwitchOn) {
                // TODO Auto-generated method stub
                if (isSwitchOn) {
                    Toast.makeText(SettingScreen.this, "开关已经开启", 300).show();
                } else {
                    Toast.makeText(SettingScreen.this, "开关已经关闭", 300).show();
                }
            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.datatime:

                Toast.makeText(getApplicationContext(), "click datetime", 1).show();
                break;

            case R.id.title_bar_setting_btn:

                finish();
                break;

            default:
                break;
        }

    }
}
