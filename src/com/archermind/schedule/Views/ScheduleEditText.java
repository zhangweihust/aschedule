package com.archermind.schedule.Views;

import com.archermind.schedule.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

public class ScheduleEditText extends EditText {
	private Rect mRect;
	private Paint mPaint;
	/**
	 * 字体大小
	 */
	public static int fontSize = 25;
	/**
	 * 字体颜色
	 */
	public static int fontColor = Color.BLACK;;
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

	// we need this constructor for LayoutInflater
	public ScheduleEditText(Context context, AttributeSet attrs) {
		super(context, attrs);

		mRect = new Rect();
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(context.getResources().getColor(R.color.lineColor));
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
		int count = (int) (initNoteHight / lineHeight + 2);
		setLineSpacing(7f, 1);
		int baseline = 0;
		for (int i = 0; i < count; i++) {
			baseline += lineHeight;
			canvas.drawLine(r.left, baseline + 12, r.right, baseline + 12,
					paint);
		}
		super.onDraw(canvas);
	}
}
