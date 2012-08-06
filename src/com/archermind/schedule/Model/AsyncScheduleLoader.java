
package com.archermind.schedule.Model;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
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

    // private HashMap<String, SoftReference<Drawable>> imageCache;
    public AsyncScheduleLoader() {

    }

    private class CommentItem {
        private SmartImageView avatar;

        private TextView content;

        private TextView time;
    }

    public Cursor loadSchedule(final LayoutInflater inflater,
            LinearLayout llayout, final int t_id, final ScheduleCallback scheduleCallback) {


        final LinearLayout commentsLayout = llayout;

        // if (imageCache.containsKey(imageUrl)) {
        // SoftReference<Drawable> softReference = imageCache.get(imageUrl);
        // Drawable drawable = softReference.get();
        // if (drawable != null) {
        // return drawable;
        // }
        // }

        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                
                scheduleCallback.scheduleCallback((ArrayList<View>)message.obj, t_id);                
            }
        };

        new Thread() {
            @Override
            public void run() {

                Cursor slaveCursors = ServiceManager.getDbManager().querySlaveShareSchedules(t_id);
                Log.i(TAG, "loadSchedule" + t_id);
                
                ArrayList<View> tArrayList = new ArrayList<View>();
                if ((commentsLayout != null) && (slaveCursors != null)
                        && (slaveCursors.getCount() > 0)) {

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
                        tArrayList.add(commentView);

                    }
                }

                Message message = handler.obtainMessage(0, tArrayList);
                handler.sendMessage(message);

                slaveCursors.close();
            }

            // Drawable drawable = loadImageFromUrl(imageUrl);
            // imageCache.put(imageUrl, new
            // SoftReference<Drawable>(drawable));
            // Message message = handler.obtainMessage(0, slaveCursor);
            // handler.sendMessage(message);

        }.start();

        return null;
    }

    // public static Drawable loadImageFromUrl(String url) {
    // URL m;
    // InputStream i = null;
    //
    // try {
    // m = new URL(url);
    // i = (InputStream)m.getContent();
    // } catch (MalformedURLException e1) {
    // e1.printStackTrace();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    //
    // Drawable d = Drawable.createFromStream(i, "src");
    //
    // return d;
    // }

    public interface ScheduleCallback {

        public void scheduleCallback(ArrayList<View> listView, int t_id);
        
    }

}
