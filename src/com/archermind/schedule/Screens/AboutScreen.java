
package com.archermind.schedule.Screens;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.archermind.schedule.R;
import com.archermind.schedule.Model.UserInfoData;

public class AboutScreen extends Screen implements OnClickListener {

    private Button btnOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        btnOut = (Button)findViewById(R.id.title_bar_about_button);
        btnOut.setOnClickListener(this);
        
    }

    @Override
    public void onClick(View v) {

        finish();
    }

}
