package com.rd.veuisdk.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class PriviewLinearLayout extends LinearLayout {


    public PriviewLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    private final String TAG = "控制scroll";

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mEnable) {
            return super.onInterceptTouchEvent(ev); // 不做拦截，传递给子 view ，由子 view 的
            // dispatchTouchEvent
            // 再来开始这个事件的分发
        } else {
            return true; // 拦截掉，交由本身的onTouchEvent 处理
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mEnable)
            return super.onTouchEvent(event);
        return false;
    }

    private boolean mEnable = true;

    public void setEnableTouch(boolean enable) {
        mEnable = enable;
    }


}
