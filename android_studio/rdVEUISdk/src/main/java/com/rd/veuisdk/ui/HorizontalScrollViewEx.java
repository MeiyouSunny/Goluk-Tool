package com.rd.veuisdk.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * 拦截焦点
 */
public class HorizontalScrollViewEx extends HorizontalScrollView {

    private boolean mEnableScroll = true;

    public HorizontalScrollViewEx(Context context) {
        super(context);
    }

    public HorizontalScrollViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalScrollViewEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mEnableScroll) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }

    public void enableScroll(boolean enable) {
        mEnableScroll = enable;
    }
}
