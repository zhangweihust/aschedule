/*
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

package com.archermind.schedule.Adapters;

/**
 * Numeric Wheel adapter.
 */
public class NumericWheelAdapter implements WheelAdapter {

	/** The default min value */
	public static final int DEFAULT_MAX_VALUE = 9;

	/** The default max value */
	private static final int DEFAULT_MIN_VALUE = 0;

	// Values
	private int minValue;
	private int maxValue;

	public int type;

	public static int DEFAULT_CALENDER = 0;
	public static int LUNAR_CALENDER_MONTH = 1;
	public static int LUNAR_CALENDER_DAY = 2;

	// format
	private String format;

	/**
	 * Default constructor
	 */
	public NumericWheelAdapter() {
		this(DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE, 0);
	}

	/**
	 * Constructor
	 * 
	 * @param minValue
	 *            the wheel min value
	 * @param maxValue
	 *            the wheel max value
	 */
	public NumericWheelAdapter(int minValue, int maxValue, int type) {
		this(minValue, maxValue, null, type);
	}

	/**
	 * Constructor
	 * 
	 * @param minValue
	 *            the wheel min value
	 * @param maxValue
	 *            the wheel max value
	 * @param format
	 *            the format string
	 */
	public NumericWheelAdapter(int minValue, int maxValue, String format,
			int type) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.format = format;
		this.type = type;
	}

	public String getItem(int index) {

		if (index >= 0 && index < getItemsCount()) {
			int value = minValue + index;
			if (type == DEFAULT_CALENDER) {
				return value < 10 ? "0" + value : Integer.toString(value);
			} else if (type == LUNAR_CALENDER_MONTH) {
				String[] month = {"一", "二", "三", "四", "五", "六", "七", "八", "九",
						"十", "十一", "十二"};
				return month[value - 1];
			} else if (type == LUNAR_CALENDER_DAY) {
				String[] day = {"初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八",
						"初九", "初十", "十一", "十二", "十三", "十四", "十五", "十六", "十七",
						"十八", "十九", "廿十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六",
						"廿七", "廿八", "廿九", "卅十", "卅一"};
				return day[value - 1];
			}
		}

		return null;
	}

	public int getItemsCount() {
		return maxValue - minValue + 1;
	}

	public int getMaximumLength() {
		int max = Math.max(Math.abs(maxValue), Math.abs(minValue));
		int maxLen = Integer.toString(max).length();
		if (minValue < 0) {
			maxLen++;
		}
		return maxLen;
	}
}
