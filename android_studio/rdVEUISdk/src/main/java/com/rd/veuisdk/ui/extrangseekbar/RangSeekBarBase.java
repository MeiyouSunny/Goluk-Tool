package com.rd.veuisdk.ui.extrangseekbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class RangSeekBarBase extends View {

	protected final int MAX_PHANDTOUCH = 120; // 触摸区域与把手的最大相距100px

	protected final int MIN_THUMB = 5;// 视频区域(起止两点间的px)最少120px

	/**
	 * 未知thumb标识值
	 */
	public static final int NONE_THUMB_PRESSED = 0;
	/**
	 * 选择范围最小时，thumb标识值
	 */
	public static final int MIN_THUMB_PRESSED = 1;
	/**
	 * 选择范围最大时，thumb标识值
	 */
	public static final int MAX_THUMB_PRESSED = 2;
	/**
	 * 指定当前值时，thumb标识值
	 */
	public static final int CURRENT_THUMB_PRESSED = 3;

	public RangSeekBarBase(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mPadding = MAX_PHANDTOUCH / 4;
	}

	public RangSeekBarBase(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Callback listener interface to notify about changed range values.
	 * 
	 */
	public interface OnRangeSeekBarChangeListener {
		/**
		 * 响应thumb按下时
		 * 
		 * @param thumbPressed
		 * @return
		 */
		boolean beginTouch(int thumbPressed);

		/**
		 * seek bar响应值发生改变完成后
		 * 
		 * @param minValue
		 * @param maxValue
		 * @param currentValue
		 */
		void rangeSeekBarValuesChanged(long minValue, long maxValue,
				long currentValue);

		/**
		 * seek bar响应值改变时
		 * 
		 * @param setValue
		 */
		void rangeSeekBarValuesChanging(long setValue);


	}

	protected int mPadding = 5;

	public int getpadding() {
		return mPadding;
	}

	/**
	 * 是否在把手区域内
	 * 
	 * @param touchX
	 * @param handx
	 * @return
	 */
	protected boolean isInHand(float touchX, int handx) {
		return Math.abs(touchX - handx) < 50;
	}
}
