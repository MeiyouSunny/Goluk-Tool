package com.rd.veuisdk.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.rd.veuisdk.adapter.HorizontalListAdapter;
import com.rd.veuisdk.model.HorizontalListItem;

/**
 * 
 * @author jeck
 * 
 */
@SuppressLint("NewApi")
public class HorizontalListViewEx extends HorizontalScrollView {
	/** 标示 */
	@SuppressWarnings("unused")
	private static final String TAG = "HorizontalListViewEx";

	/** 布局容器 */
	private LinearLayout mContainerLayout;

	/** 链表适配器 */
	private HorizontalListAdapter mHorizontalListAdapter;

	public HorizontalListViewEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setContainerLayout(LinearLayout layout) {

		this.mContainerLayout = layout;
	}

	/**
	 * 
	 * @param adapter
	 */
	public void setAdapter(HorizontalListAdapter adapter) {

		mHorizontalListAdapter = adapter;

		mHorizontalListAdapter.setScrollView(this);

		mContainerLayout.removeAllViews();

		for (HorizontalListItem item : mHorizontalListAdapter) {

			mContainerLayout.addView(item.getContentView());
		}
	}

	/**
	 * 滚动到指定项
	 * 
	 * @param item
	 */
	public void scrollByItem(HorizontalListItem item) {
		int nCurrentScrollX = this.getScrollX();
		View clickItem = item.getContentView();
		int nScrollLeft = item.getContentView().getLeft()
				- mContainerLayout.getPaddingLeft();
		int nScrollRight = clickItem.getRight() - this.getWidth()
				+ mContainerLayout.getPaddingRight();
		if (nScrollLeft < nCurrentScrollX) {
			this.smoothScrollTo(nScrollLeft, this.getScrollY());
		} else if (nScrollRight > nCurrentScrollX) {
			this.smoothScrollTo(nScrollRight, this.getScrollY());
		}
	}

	@Override
	public void onScreenStateChanged(int screenState) {
		super.onScreenStateChanged(screenState);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.onTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.dispatchTouchEvent(ev);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
