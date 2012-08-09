
package com.archermind.schedule.Screens;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.archermind.schedule.R;
import com.archermind.schedule.Events.EventArgs;
import com.archermind.schedule.Events.IEventHandler;

public class Feedback extends Screen implements IEventHandler, OnClickListener {

    private Button mBtnOut;

    private Button mBtnSend;

    private EditText etContentSend;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_back);

        mBtnOut = (Button)findViewById(R.id.title_feedback_button_out);
        mBtnOut.setOnClickListener(this);

        mBtnSend = (Button)findViewById(R.id.title_feedback_button_send);
        mBtnSend.setOnClickListener(this);

        etContentSend = (EditText)findViewById(R.id.etfeed_back_content);
        etContentSend.setHint(R.string.feedbackhintcontent);
    }

    public boolean onEvent(Object sender, EventArgs e) {

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.title_feedback_button_out:

                finish();
                break;

            case R.id.title_feedback_button_send:

                Toast.makeText(getApplicationContext(),
                        "等待发送接口！！！" + etContentSend.getText().toString().trim(), 1).show();
                break;

            default:
                break;
        }
    }
}
