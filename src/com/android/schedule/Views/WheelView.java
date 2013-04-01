/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 *  
 *  Copyright 2010 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.android.schedule.Views;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.android.schedule.ScheduleApplication;
import com.android.schedule.Adapters.WheelAdapter;
import com.android.schedule.Calendar.SpecialCalendar;
import com.android.schedule.Utils.Constant;
import com.archermind.schedule.R;

/**
 * Numeric wheel view.
 * 
 * @author Yuri Kanivets
 */
public class WheelView extends View {
	/** Scrolling duration */
	private static final int SCROLLING_DURATION = 400;

	/** Minimum delta for scrolling */
	private static final int MIN_DELTA_FOR_SCROLLING = 1;

	/** Current value & label text color */
	private static final int VALUE_TEXT_COLOR = 0xF0000000;

	/** Items text color */
	private static final int ITEMS_TEXT_COLOR = 0xFF000000;

	/** Top and bottom shadows colors */
	private static final int[] SHADOWS_COLORS = new int[]{0xFF111111,
			0x00AAAAAA, 0x00AAAAAA};

	/** Additional items height (is added to standard text item height) */
	private static final int ADDITIONAL_ITEM_HEIGHT = 26;

	// private static final int ITEM_HEIGHT = 26;

	/** Text size */
	private static int TEXT_SIZE = 24;

	private static int LABEL_TEXT_SIZE = 14;

	/** Top and bottom items offset (to hide that) */
	private static final int ITEM_OFFSET = /* TEXT_SIZE / 5 */0;

	/** Additional width for items layout */
	private static final int ADDITIONAL_ITEMS_SPACE = 10;

	/** Label offset */
	private static final int LABEL_OFFSET = 8;

	/** Left and right padding value */
	private static final int PADDING = 10;

	/** Default count of visible items */
	private static final int DEF_VISIBLE_ITEMS = 5;

	// Wheel Values
	private WheelAdapter adapter = null;
	private int currentItem = 0;

	// Widths
	private int itemsWidth = 0;
	private int labelWidth = 0;

	// Count of visible items
	private int visibleItems = DEF_VISIBLE_ITEMS;

	// Item height
	private int itemHeight = 0;

	private int wheel_type = 0;

	// Text paints
	private TextPaint itemsPaint;
	private TextPaint valuePaint;
	private TextPaint labelPaint;

	// Layouts
	private StaticLayout itemsLayout;
	private StaticLayout labelLayout;
	private StaticLayout valueLayout;

	// Label & background
	private String label;
	private Drawable centerDrawable;

	// Shadows drawables
	private GradientDrawable topShadow;
	private GradientDrawable bottomShadow;

	// Scrolling
	private boolean isScrollingPerformed;
	private int scrollingOffset;

	// Scrolling animation
	private GestureDetector gestureDetector;
	private Scroller scroller;
	private int lastScrollY;

	// Cyclic
	boolean isCyclic = true;

	// Listeners
	private List<OnWheelChangedListener> changingListeners = new LinkedList<OnWheelChangedListener>();
	private List<OnWheelScrollListener> scrollingListeners = new LinkedList<OnWheelScrollListener>();

	private boolean isHadScroll = false;

	private int labelOff = 0;

	private String realLabel;
	/**
	 * Constructor
	 */
	public WheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initData(context);
	}

	/**
	 * Constructor
	 */
	public WheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initData(context);
	}

	/**
	 * Constructor
	 */
	public WheelView(Context context) {
		super(context);
		initData(context);
	}

	/**
	 * Initializes class data
	 * 
	 * @param context
	 *            the context
	 */
	private void initData(Context context) {
		gestureDetector = new GestureDetector(context, gestureListener);
		gestureDetector.setIsLongpressEnabled(false);

		scroller = new Scroller(context);

	}

	/**
	 * Gets wheel adapter
	 * 
	 * @return the adapter
	 */
	public WheelAdapter getAdapter() {
		return adapter;
	}

	/**
	 * Sets wheel adapter
	 * 
	 * @param adapter
	 *            the new wheel adapter
	 */
	public void setAdapter(WheelAdapter adapter) {
		this.adapter = adapter;
		DisplayMetrics dm = ScheduleApplication.getContext().getResources()
				.getDisplayMetrics();
		int width = dm.widthPixels;
		if (width <= 300) {
			TEXT_SIZE = 14;
			LABEL_TEXT_SIZE = 8;
		}
		invalidateLayouts();
		invalidate();
	}

	/**
	 * Set the the specified scrolling interpolator
	 * 
	 * @param interpolator
	 *            the interpolator
	 */
	public void setInterpolator(Interpolator interpolator) {
		scroller.forceFinished(true);
		scroller = new Scroller(getContext(), interpolator);
	}

	/**
	 * Gets count of visible items
	 * 
	 * @return the count of visible items
	 */
	public int getVisibleItems() {
		return visibleItems;
	}

	/**
	 * Sets count of visible items
	 * 
	 * @param count
	 *            the new count
	 */
	public void setVisibleItems(int count) {
		visibleItems = count;
		invalidate();
	}

	/**
	 * Gets label
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets label
	 * 
	 * @param newLabel
	 *            the label to set
	 */
	public void setLabel(String newLabel) {
		if (label == null || !label.equals(newLabel)) {
			label = newLabel;
			labelLayout = null;
			invalidate();
		}
	}

	public void setRealLabel(String newLabel) {
		this.realLabel = newLabel;
	}

	public void setType(int wheel_type) {
		this.wheel_type = wheel_type;
	}

	/**
	 * Adds wheel changing listener
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addChangingListener(OnWheelChangedListener listener) {
		changingListeners.add(listener);
	}

	/**
	 * Removes wheel changing listener
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeChangingListener(OnWheelChangedListener listener) {
		changingListeners.remove(listener);
	}

	/**
	 * Notifies changing listeners
	 * 
	 * @param oldValue
	 *            the old wheel value
	 * @param newValue
	 *            the new wheel value
	 */
	protected void notifyChangingListeners(int oldValue, int newValue) {
		for (OnWheelChangedListener listener : changingListeners) {
			listener.onChanged(this, oldValue, newValue);
		}
	}

	/**
	 * Adds wheel scrolling listener
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addScrollingListener(OnWheelScrollListener listener) {
		scrollingListeners.add(listener);
	}

	/**
	 * Removes wheel scrolling listener
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeScrollingListener(OnWheelScrollListener listener) {
		scrollingListeners.remove(listener);
	}

	/**
	 * Notifies listeners about starting scrolling
	 */
	protected void notifyScrollingListenersAboutStart() {
		for (OnWheelScrollListener listener : scrollingListeners) {
			listener.onScrollingStarted(this);
		}
	}

	/**
	 * Notifies listeners about ending scrolling
	 */
	protected void notifyScrollingListenersAboutEnd() {
		for (OnWheelScrollListener listener : scrollingListeners) {
			listener.onScrollingFinished(this);
		}
	}

	/**
	 * Gets current value
	 * 
	 * @return the current value
	 */
	public int getCurrentItem() {
		return currentItem;
	}

	/**
	 * Sets the current item. Does nothing when index is wrong.
	 * 
	 * @param index
	 *            the item index
	 * @param animated
	 *            the animation flag
	 */
	public void setCurrentItem(int index, boolean animated) {
		if (adapter == null || adapter.getItemsCount() == 0) {
			return; // throw?
		}
		if (index < 0 || index >= adapter.getItemsCount()) {
			if (isCyclic) {
				while (index < 0) {
					index += adapter.getItemsCount();
				}
				index %= adapter.getItemsCount();
			} else {
				return; // throw?
			}
		}
		if (index != currentItem) {
			if (animated) {
				scroll(index - currentItem, SCROLLING_DURATION);
			} else {
				invalidateLayouts();

				int old = currentItem;
				currentItem = index;

				notifyChangingListeners(old, currentItem);

				invalidate();
			}
		}
	}

	/**
	 * Sets the current item w/o animation. Does nothing when index is wrong.
	 * 
	 * @param index
	 *            the item index
	 */
	public void setCurrentItem(int index) {
		setCurrentItem(index, false);
	}

	/**
	 * Tests if wheel is cyclic. That means before the 1st item there is shown
	 * the last one
	 * 
	 * @return true if wheel is cyclic
	 */
	public boolean isCyclic() {
		return isCyclic;
	}

	/**
	 * Set wheel cyclic flag
	 * 
	 * @param isCyclic
	 *            the flag to set
	 */
	public void setCyclic(boolean isCyclic) {
		this.isCyclic = isCyclic;

		invalidate();
		invalidateLayouts();
	}

	/**
	 * Invalidates layouts
	 */
	private void invalidateLayouts() {
		itemsLayout = null;
		valueLayout = null;
		scrollingOffset = 0;
	}

	/**
	 * Initializes resources
	 */
	private void initResourcesIfNecessary() {
		if (itemsPaint == null) {
			itemsPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG
					| Paint.FAKE_BOLD_TEXT_FLAG);
			// itemsPaint.density = getResources().getDisplayMetrics().density;
			itemsPaint.setTextSize(TEXT_SIZE);
		}

		if (valuePaint == null) {
			valuePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG
					| Paint.FAKE_BOLD_TEXT_FLAG | Paint.DITHER_FLAG);
			// valuePaint.density = getResources().getDisplayMetrics().density;
			valuePaint.setTextSize(TEXT_SIZE);
			valuePaint.setShadowLayer(0.1f, 0, 0.1f, 0xFFC0C0C0);
		}

		if (labelPaint == null) {
			labelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG
					| Paint.FAKE_BOLD_TEXT_FLAG);
			// itemsPaint.density = getResources().getDisplayMetrics().density;
			labelPaint.setTextSize(LABEL_TEXT_SIZE);
		}

		if (centerDrawable == null) {
			centerDrawable = getContext().getResources().getDrawable(
					R.drawable.wheel_val);
		}

		if (topShadow == null) {
			topShadow = new GradientDrawable(Orientation.TOP_BOTTOM,
					SHADOWS_COLORS);
		}

		if (bottomShadow == null) {
			bottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP,
					SHADOWS_COLORS);
		}

		setBackgroundResource(R.drawable.wheel_bg);
	}

	/**
	 * Calculates desired height for layout
	 * 
	 * @param layout
	 *            the source layout
	 * @return the desired layout height
	 */
	private int getDesiredHeight(Layout layout) {
		if (layout == null) {
			return 0;
		}

		int desired = getItemHeight() * visibleItems - ITEM_OFFSET * 2
				- ADDITIONAL_ITEM_HEIGHT;

		// Check against our minimum height
		desired = Math.max(desired, getSuggestedMinimumHeight());

		return desired;
	}

	/**
	 * Returns text item by index
	 * 
	 * @param index
	 *            the item index
	 * @return the item or null
	 */
	private String getTextItem(int index) {
		if (adapter == null || adapter.getItemsCount() == 0) {
			return null;
		}
		int count = adapter.getItemsCount();
		if ((index < 0 || index >= count) && !isCyclic) {
			return null;
		} else {
			while (index < 0) {
				index = count + index;
			}
		}

		index %= count;
		return adapter.getItem(index);
	}

	/**
	 * Builds text depending on current value
	 * 
	 * @param useCurrentValue
	 * @return the text
	 */
	private String buildText(boolean useCurrentValue) {
		StringBuilder itemsText = new StringBuilder();
		int addItems = visibleItems / 2 + 1;

		for (int i = currentItem - addItems; i <= currentItem + addItems; i++) {
			if (useCurrentValue || i != currentItem) {
				String text = getTextItem(i);
				if (text != null) {
					itemsText.append(text);
				}
			}
			if (i < currentItem + addItems) {
				itemsText.append("\n");
			}
		}

		return itemsText.toString();
	}

	/**
	 * Returns the max item length that can be present
	 * 
	 * @return the max length
	 */
	private int getMaxTextLength() {
		WheelAdapter adapter = getAdapter();
		if (adapter == null) {
			return 0;
		}

		int adapterLength = adapter.getMaximumLength();
		if (adapterLength > 0) {
			return adapterLength;
		}

		String maxText = null;
		int addItems = visibleItems / 2;
		for (int i = Math.max(currentItem - addItems, 0); i < Math.min(
				currentItem + visibleItems, adapter.getItemsCount()); i++) {
			String text = adapter.getItem(i);
			if (text != null
					&& (maxText == null || maxText.length() < text.length())) {
				maxText = text;
			}
		}

		return maxText != null ? maxText.length() : 0;
	}

	/**
	 * Returns height of wheel item
	 * 
	 * @return the item height
	 */
	private int getItemHeight() {
		if (itemHeight != 0) {
			return itemHeight;
		} else if (itemsLayout != null && itemsLayout.getLineCount() > 2) {
			itemHeight = itemsLayout.getLineTop(2) - itemsLayout.getLineTop(1);
			return itemHeight;
		}

		return getHeight() / visibleItems;
	}

	/**
	 * Calculates control width and creates text layouts
	 * 
	 * @param widthSize
	 *            the input layout width
	 * @param mode
	 *            the layout mode
	 * @return the calculated control width
	 */
	private int calculateLayoutWidth(int widthSize, int mode) {
		initResourcesIfNecessary();

		int width = widthSize;

		int maxLength = getMaxTextLength();
		if (maxLength > 0) {
			float textWidth = FloatMath.ceil(Layout.getDesiredWidth("0",
					itemsPaint));
			itemsWidth = (int) (maxLength * textWidth);
		} else {
			itemsWidth = 0;
		}
		itemsWidth += ADDITIONAL_ITEMS_SPACE; // make it some more

		labelWidth = 0;
		if (label != null && label.length() > 0) {
			labelWidth = (int) FloatMath.ceil(Layout.getDesiredWidth(label,
					valuePaint));
		}

		boolean recalculate = false;
		if (mode == MeasureSpec.EXACTLY) {
			width = widthSize;
			recalculate = true;
		} else {
			width = itemsWidth + labelWidth + 2 * PADDING;
			if (labelWidth > 0) {
				width += LABEL_OFFSET;
			}

			// Check against our minimum width
			width = Math.max(width, getSuggestedMinimumWidth());

			if (mode == MeasureSpec.AT_MOST && widthSize < width) {
				width = widthSize;
				recalculate = true;
			}
		}

		if (recalculate) {
			// recalculate width
			int pureWidth = width - LABEL_OFFSET - 2 * PADDING;
			if (pureWidth <= 0) {
				itemsWidth = labelWidth = 0;
			}
			if (labelWidth > 0) {
				double newWidthItems = (double) itemsWidth * pureWidth
						/ (itemsWidth + labelWidth);
				itemsWidth = (int) newWidthItems;
				labelWidth = pureWidth - itemsWidth;
			} else {
				itemsWidth = pureWidth + LABEL_OFFSET; // no label
			}
		}

		if (itemsWidth > 0) {
			createLayouts(itemsWidth, labelWidth);
		}

		return width;
	}
	private static int solar_calendar_flag = 1;
	private static int lunar_calendar_flag = 2;

	private int calendar_flag = solar_calendar_flag;
	public void setCalendarFlag(int calendar_flag) {
		this.calendar_flag = calendar_flag;
	}
	/**
	 * Creates layouts
	 * 
	 * @param widthItems
	 *            width of items layout
	 * @param widthLabel
	 *            width of label layout
	 */
	private void createLayouts(int widthItems, int widthLabel) {
		if (wheel_type == Constant.wheel_day) {
			widthItems += labelOff;
		}
		if (itemsLayout == null || itemsLayout.getWidth() > widthItems) {
			itemsLayout = new StaticLayout(buildText(isScrollingPerformed),
					itemsPaint, widthItems, widthLabel > 0
							? Layout.Alignment.ALIGN_OPPOSITE
							: Layout.Alignment.ALIGN_CENTER, 1,
					ADDITIONAL_ITEM_HEIGHT, false);
		} else {
			itemsLayout.increaseWidthTo(widthItems);
		}
		String text = null;
		if (!isScrollingPerformed
				&& (valueLayout == null || valueLayout.getWidth() > widthItems)) {
			text = getAdapter() != null
					? getAdapter().getItem(currentItem)
					: null;
			if (wheel_type == Constant.wheel_year) {
				if (calendar_flag == solar_calendar_flag) {
					Constant.VARY_YEAR = Integer.valueOf(text);
					// LunarCalendar.getLunarDate(Constant.VARY_YEAR,
					// Constant.VARY_MONTH, Constant.VARY_DAY, true);
					// Constant.VARY_LUNAR_YEAR =
					// LunarCalendar.getLunarYear()+"";
				} else if (calendar_flag == lunar_calendar_flag) {
					// Constant.VARY_LUNAR_YEAR = text;
					// String solarDate =
					// LunarCalendar.getSolar(Constant.VARY_LUNAR_YEAR,
					// Constant.VARY_LUNAR_MONTH, Constant.VARY_LUNAR_DAY);
					// Constant.VARY_YEAR =
					// Integer.parseInt(solarDate.split(",")[0]);
				}

			} else if (wheel_type == Constant.wheel_month) {
				try {
					Constant.VARY_MONTH = Integer.valueOf(text);
					// LunarCalendar.getLunarDate(Constant.VARY_YEAR,
					// Constant.VARY_MONTH, Constant.VARY_DAY, true);
					// Constant.VARY_LUNAR_MONTH =
					// LunarCalendar.getLunarMonthNumber()+"";
				} catch (Exception e) {
					// if(text != null){
					// String[] months =
					// {"一","二","三","四","五","六","七","八","九","十","十一","十二"};
					// String month = null;
					// for(int i = 0 ; i < months.length; i++){
					// if(text.equals(months[i])){
					// month = String.valueOf(i + 1);
					// break;
					// }
					// }
					// Constant.VARY_LUNAR_MONTH = month;
					// String solarDate =
					// LunarCalendar.getSolar(Constant.VARY_LUNAR_YEAR, month,
					// Constant.VARY_LUNAR_DAY);
					// Constant.VARY_MONTH =
					// Integer.parseInt(solarDate.split(",")[1]);
					// }
				}
			} else if (wheel_type == Constant.wheel_day) {
				try {
					Constant.VARY_DAY = Integer.valueOf(text);
					// LunarCalendar.getLunarDate(Constant.VARY_YEAR,
					// Constant.VARY_MONTH, Constant.VARY_DAY, true);
					// Constant.VARY_LUNAR_DAY =
					// LunarCalendar.getLunarDayNumber()+"";
				} catch (Exception e) {
					// if(text != null){
					// String[] days =
					// {"初一","初二","初三","初四","初五","初六","初七","初八","初九","初十","十一","十二",
					// "十三","十四","十五","十六","十七","十八","十九","廿十","廿一","廿二",
					// "廿三","廿四","廿五","廿六","廿七","廿八","廿九","卅十","卅一"};
					// String day = null;
					// for(int i = 0 ; i < days.length; i++){
					// if(text.equals(days[i])){
					// day = String.valueOf(i + 1);
					// break;
					// }
					// }
					// Constant.VARY_LUNAR_DAY = day;
					// String solarDate =
					// LunarCalendar.getSolar(Constant.VARY_LUNAR_YEAR,
					// Constant.VARY_LUNAR_MONTH, day);
					// Constant.VARY_DAY =
					// Integer.parseInt(solarDate.split(",")[2]);
					// }
				}
			} else if (wheel_type == Constant.wheel_hour) {
				Constant.VARY_HOUR = Integer.valueOf(text);
			} else if (wheel_type == Constant.wheel_min) {
				Constant.VARY_MIN = Integer.valueOf(text);
			}
			valueLayout = new StaticLayout(text != null ? text : "",
					valuePaint, widthItems, widthLabel > 0
							? Layout.Alignment.ALIGN_OPPOSITE
							: Layout.Alignment.ALIGN_CENTER, 1,
					ADDITIONAL_ITEM_HEIGHT, false);
		} else if (isScrollingPerformed) {
			valueLayout = null;
		} else {
			valueLayout.increaseWidthTo(widthItems);
		}

		// if (widthLabel > 0) {
		if (!isScrollingPerformed
				&& (labelLayout == null || labelLayout.getWidth() > widthLabel)) {
			// if(wheel_type == Constant.wheel_day){
			// if(text == null){
			// if(isHadScroll){
			// realLabel = "日";
			// }else{
			// realLabel =
			// SpecialCalendar.getCapitelNumberWeekDay(Constant.YEAR,Constant.MONTH,
			// Constant.DAY);
			// }
			// }else{
			// realLabel =
			// SpecialCalendar.getCapitelNumberWeekDay(Constant.VARY_YEAR,
			// Constant.VARY_MONTH, Constant.VARY_DAY);
			// }
			// }
			labelLayout = new StaticLayout(realLabel, labelPaint, widthItems,
					Layout.Alignment.ALIGN_CENTER, 0.5f,
					ADDITIONAL_ITEM_HEIGHT, false);

		} else if (isScrollingPerformed) {
			labelLayout = null;
		} else {
			labelLayout.increaseWidthTo(widthItems);
		}
		// }
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width = calculateLayoutWidth(widthSize, widthMode);

		int height;
		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else {
			height = getDesiredHeight(itemsLayout);

			if (heightMode == MeasureSpec.AT_MOST) {
				height = Math.min(height, heightSize);
			}
		}

		setMeasuredDimension(width, height);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (itemsLayout == null) {
			if (itemsWidth == 0) {
				calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);
			} else {
				createLayouts(itemsWidth, labelWidth);
			}
		}

		if (itemsWidth > 0) {
			canvas.save();
			// Skip padding space and hide a part of top and bottom items
			canvas.translate(PADDING, -ITEM_OFFSET);
			drawItems(canvas);
			drawValue(canvas);
			canvas.restore();
		}

		drawCenterRect(canvas);
		drawShadows(canvas);
	}

	/**
	 * Draws shadows on top and bottom of control
	 * 
	 * @param canvas
	 *            the canvas for drawing
	 */
	private void drawShadows(Canvas canvas) {
		topShadow.setBounds(0, 0, getWidth(), getHeight() / visibleItems);
		topShadow.draw(canvas);

		bottomShadow.setBounds(0, getHeight() - getHeight() / visibleItems,
				getWidth(), getHeight());
		bottomShadow.draw(canvas);
	}

	/**
	 * Draws value and label layout
	 * 
	 * @param canvas
	 *            the canvas for drawing
	 */
	private void drawValue(Canvas canvas) {
		valuePaint.setColor(VALUE_TEXT_COLOR);
		valuePaint.drawableState = getDrawableState();

		Rect bounds = new Rect();
		itemsLayout.getLineBounds(visibleItems / 2, bounds);

		int start = 0;
		if (wheel_type == Constant.wheel_day) {
			start += labelOff / 2;
		}

		// draw label
		// draw label
		if (labelLayout != null) {
			canvas.save();
			canvas.translate(2 * start, bounds.top + ADDITIONAL_ITEM_HEIGHT * 5
					/ 6);
			labelLayout.draw(canvas);
			canvas.restore();
		}

		// draw current value
		if (valueLayout != null) {
			canvas.save();
			canvas.translate(start, bounds.top + scrollingOffset);
			valueLayout.draw(canvas);
			canvas.restore();
		}
	}

	/**
	 * Draws items
	 * 
	 * @param canvas
	 *            the canvas for drawing
	 */
	private void drawItems(Canvas canvas) {
		canvas.save();
		int start = 0;
		if (wheel_type == Constant.wheel_day) {
			start += labelOff / 2;
		}
		int top = itemsLayout.getLineTop(1);
		canvas.translate(start, -top + scrollingOffset);

		itemsPaint.setColor(ITEMS_TEXT_COLOR);
		itemsPaint.drawableState = getDrawableState();
		itemsLayout.draw(canvas);

		canvas.restore();
	}
	/**
	 * Draws rect for current value
	 * 
	 * @param canvas
	 *            the canvas for drawing
	 */
	private void drawCenterRect(Canvas canvas) {
		int center = getHeight() / 2;
		int offset = getItemHeight() / 2;
		centerDrawable.setBounds(0, center - offset, getWidth(), center
				+ offset);
		centerDrawable.draw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		WheelAdapter adapter = getAdapter();
		if (adapter == null) {
			return true;
		}

		if (!gestureDetector.onTouchEvent(event)
				&& event.getAction() == MotionEvent.ACTION_UP) {
			justify();
		}
		return true;
	}

	/**
	 * Scrolls the wheel
	 * 
	 * @param delta
	 *            the scrolling value
	 */
	private void doScroll(int delta) {
		scrollingOffset += delta;

		int count = scrollingOffset / getItemHeight();
		int pos = currentItem - count;
		if (isCyclic && adapter.getItemsCount() > 0) {
			// fix position by rotating
			while (pos < 0) {
				pos += adapter.getItemsCount();
			}
			pos %= adapter.getItemsCount();
		} else if (isScrollingPerformed) {
			//
			if (pos < 0) {
				count = currentItem;
				pos = 0;
			} else if (pos >= adapter.getItemsCount()) {
				count = currentItem - adapter.getItemsCount() + 1;
				pos = adapter.getItemsCount() - 1;
			}
		} else {
			// fix position
			pos = Math.max(pos, 0);
			pos = Math.min(pos, adapter.getItemsCount() - 1);
		}

		int offset = scrollingOffset;
		if (pos != currentItem) {
			setCurrentItem(pos, false);
		} else {
			invalidate();
		}

		// update offset
		scrollingOffset = offset - count * getItemHeight();
		if (scrollingOffset > getHeight()) {
			scrollingOffset = scrollingOffset % getHeight() + getHeight();
		}
	}

	// gesture listener
	private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
		public boolean onDown(MotionEvent e) {
			if (isScrollingPerformed) {
				scroller.forceFinished(true);
				clearMessages();
				return true;
			}
			return false;
		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			isHadScroll = true;
			startScrolling();
			doScroll((int) -distanceY);
			return true;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			lastScrollY = currentItem * getItemHeight() + scrollingOffset;
			int maxY = isCyclic ? 0x7FFFFFFF : adapter.getItemsCount()
					* getItemHeight();
			int minY = isCyclic ? -maxY : 0;
			scroller.fling(0, lastScrollY, 0, (int) -velocityY / 2, 0, 0, minY,
					maxY);
			setNextMessage(MESSAGE_SCROLL);
			return true;
		}
	};

	// Messages
	private final int MESSAGE_SCROLL = 0;
	private final int MESSAGE_JUSTIFY = 1;

	/**
	 * Set next message to queue. Clears queue before.
	 * 
	 * @param message
	 *            the message to set
	 */
	private void setNextMessage(int message) {
		clearMessages();
		animationHandler.sendEmptyMessage(message);
	}

	/**
	 * Clears messages from queue
	 */
	private void clearMessages() {
		animationHandler.removeMessages(MESSAGE_SCROLL);
		animationHandler.removeMessages(MESSAGE_JUSTIFY);
	}

	// animation handler
	private Handler animationHandler = new Handler() {
		public void handleMessage(Message msg) {
			scroller.computeScrollOffset();
			int currY = scroller.getCurrY();
			int delta = lastScrollY - currY;
			lastScrollY = currY;
			if (delta != 0) {
				doScroll(delta);
			}

			// scrolling is not finished when it comes to final Y
			// so, finish it manually
			if (Math.abs(currY - scroller.getFinalY()) < MIN_DELTA_FOR_SCROLLING) {
				currY = scroller.getFinalY();
				scroller.forceFinished(true);
			}
			if (!scroller.isFinished()) {
				animationHandler.sendEmptyMessage(msg.what);
			} else if (msg.what == MESSAGE_SCROLL) {
				justify();
			} else {
				finishScrolling();
			}
		}
	};

	/**
	 * Justifies wheel
	 */
	private void justify() {
		if (adapter == null) {
			return;
		}

		lastScrollY = 0;
		int offset = scrollingOffset;
		int itemHeight = getItemHeight();
		boolean needToIncrease = offset > 0 ? currentItem < adapter
				.getItemsCount() : currentItem > 0;
		if ((isCyclic || needToIncrease)
				&& Math.abs((float) offset) > (float) itemHeight / 2) {
			if (offset < 0)
				offset += itemHeight + MIN_DELTA_FOR_SCROLLING;
			else
				offset -= itemHeight + MIN_DELTA_FOR_SCROLLING;
		}
		if (Math.abs(offset) > MIN_DELTA_FOR_SCROLLING) {
			scroller.startScroll(0, 0, 0, offset, SCROLLING_DURATION);
			setNextMessage(MESSAGE_JUSTIFY);
		} else {
			finishScrolling();
		}
	}

	/**
	 * Starts scrolling
	 */
	private void startScrolling() {
		if (!isScrollingPerformed) {
			isScrollingPerformed = true;
			notifyScrollingListenersAboutStart();
		}
	}

	/**
	 * Finishes scrolling
	 */
	void finishScrolling() {
		if (isScrollingPerformed) {
			notifyScrollingListenersAboutEnd();
			isScrollingPerformed = false;
		}
		invalidateLayouts();
		invalidate();
	}

	public StaticLayout getItemsLayout() {
		return this.itemsLayout;
	}
	public StaticLayout getLabelLayout() {
		return this.labelLayout;
	}
	public void setItemsLayout(StaticLayout itemsLayout) {
		this.itemsLayout = itemsLayout;
	}
	public void setLabelLayout(StaticLayout labelLayout) {
		this.labelLayout = labelLayout;
	}

	/**
	 * Scroll the wheel
	 * 
	 * @param itemsToSkip
	 *            items to scroll
	 * @param time
	 *            scrolling duration
	 */
	public void scroll(int itemsToScroll, int time) {
		scroller.forceFinished(true);

		lastScrollY = scrollingOffset;
		int offset = itemsToScroll * getItemHeight();

		scroller.startScroll(0, lastScrollY, 0, offset - lastScrollY, time);
		setNextMessage(MESSAGE_SCROLL);

		startScrolling();
	}

	/**
	 * Wheel changed listener interface.
	 * <p>
	 * The currentItemChanged() method is called whenever current wheel
	 * positions is changed:
	 * <li>New Wheel position is set
	 * <li>Wheel view is scrolled
	 */
	public interface OnWheelChangedListener {
		/**
		 * Callback method to be invoked when current item changed
		 * 
		 * @param wheel
		 *            the wheel view whose state has changed
		 * @param oldValue
		 *            the old value of current item
		 * @param newValue
		 *            the new value of current item
		 */
		void onChanged(WheelView wheel, int oldValue, int newValue);
	}

	/**
	 * Wheel scrolled listener interface.
	 */
	public interface OnWheelScrollListener {
		/**
		 * Callback method to be invoked when scrolling started.
		 * 
		 * @param wheel
		 *            the wheel view whose state has changed.
		 */
		void onScrollingStarted(WheelView wheel);

		/**
		 * Callback method to be invoked when scrolling ended.
		 * 
		 * @param wheel
		 *            the wheel view whose state has changed.
		 */
		void onScrollingFinished(WheelView wheel);
	}

}
