
package com.archermind.schedule.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

public class FeedBackEditText extends EditText {

    private Rect mRect;

    private Paint mPaint;

    /**
     * 字体大小
     */
    public static int fontSize = 20;

    /**
     * 字体颜色
     */
    public static int fontColor = Color.BLACK;

    /**
     * 文本初始高度
     */
    public static int initNoteHight = 480;

    /**
     * 分割线颜色
     */
    public static int lineColor = 0x800000FF;

    /**
     * 文本追加高度
     */
    public static int append = 240;

    public FeedBackEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
       
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(lineColor);
        setTextSize(fontSize);
        setTextColor(fontColor);
        
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Rect r = mRect;
        Paint paint = mPaint;
        int lineHeight = getLineHeight();
        int height = getLineBounds(getLineCount() - 1, r);
        if (height >= initNoteHight - lineHeight) {
            initNoteHight += append;
        }
        int count = (int)(initNoteHight / lineHeight + 2);
        int baseline = 0;
        for (int i = 0; i < count; i++) {
            baseline += lineHeight;
            canvas.drawLine(r.left, baseline+8 , r.right, baseline+8, paint);
        }
        super.onDraw(canvas);
    }

}
