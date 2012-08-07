
package com.archermind.schedule.Task;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.archermind.schedule.R;
import com.archermind.schedule.Image.SmartImageView;
import com.archermind.schedule.Provider.DatabaseHelper;
import com.archermind.schedule.Services.ServiceManager;
import com.archermind.schedule.Utils.DateTimeUtils;

public class AsyncScheduleLoader {

    String TAG = "AsyncScheduleLoader";
    public AsyncScheduleLoader() {

    }

    private class CommentItem {
        private SmartImageView avatar;

        private TextView content;

        private TextView time;
    }

    public void loadSchedule(final Context context, final LayoutInflater inflater,
    		final int t_id, final ScheduleCallback scheduleCallback) {

        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                
                scheduleCallback.scheduleCallback((LinearLayout)message.obj, t_id);                
            }
        };

        new Thread() {
            @Override
            public void run() {

                Cursor slaveCursors = ServiceManager.getDbManager().querySlaveShareSchedules(t_id);
                Log.i(TAG, "loadSchedule" + t_id);
                
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                if ((slaveCursors != null) && (slaveCursors.getCount() > 0)) {
                    for (slaveCursors.moveToFirst(); !slaveCursors.isAfterLast(); slaveCursors
                            .moveToNext()) {
                        View commentView = inflater.inflate(R.layout.feed_comments_item, null);
                        CommentItem commentItem = new CommentItem();
                        commentItem.avatar = (SmartImageView)commentView
                                .findViewById(R.id.comment_profile_photo);
                        commentItem.content = (TextView)commentView.findViewById(R.id.comment_body);
                        commentItem.time = (TextView)commentView.findViewById(R.id.comment_time);
                        commentItem.avatar.setBackgroundResource(R.drawable.avatar);
                        commentItem.content.setText(slaveCursors.getString(slaveCursors
                                .getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_CONTENT)));
                        long commentTime = slaveCursors.getLong(slaveCursors
                                .getColumnIndex(DatabaseHelper.COLUMN_SCHEDULE_START_TIME));
                        commentItem.time.setText(DateTimeUtils.time2String("yyyy/MM/dd hh:mm:ss",
                                commentTime));
                        layout.addView(commentView);
                    }
                }

                Message message = handler.obtainMessage(0, layout);
                handler.sendMessage(message);

                slaveCursors.close();
            }

        }.start();
    }

    public interface ScheduleCallback {

        public void scheduleCallback(LinearLayout listView, int t_id);
        
    }

}