package com.rd.veuisdk.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/***
 * 防止单个界面中，部分UI有滑动事件的干扰
 */
public class ExtViewPagerNoScroll extends ViewPager {

    private boolean mEnableScroll = false;
    private int mHalfHeight = 300;

    public ExtViewPagerNoScroll(Context context) {
        super(context);
        init(context);

    }

    public ExtViewPagerNoScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {

        mHalfHeight = context.getResources().getDisplayMetrics().heightPixels / 2;

    }

    public void setHandledHeight(int handlerHeight) {
        mHalfHeight = handlerHeight;
    }

    public void enableScroll(boolean enable) {
        mEnableScroll = enable;
    }

    /**
     * 只争对广场 ，网页中含有viewpager
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {


        if (mEnableScroll) {
            return super.onInterceptTouchEvent(ev);
        }
        return false;
    }


}
