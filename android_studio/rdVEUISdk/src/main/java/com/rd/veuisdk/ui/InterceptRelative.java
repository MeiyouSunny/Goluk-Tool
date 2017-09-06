package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.rd.lib.ui.RotateRelativeLayout;
import com.rd.lib.utils.CoreUtils;

/**
 * 录制界面拦截焦点
 * @author JIAN
 * @date 2017-4-28 下午2:20:26
 */
public class InterceptRelative extends RotateRelativeLayout {
    private int mHeight;

    public InterceptRelative(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHeight = CoreUtils.getMetrics().heightPixels;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean re = super.onInterceptTouchEvent(ev);
        return false;
    }

    private View mView;
    private Rect mRect = new Rect();

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean re = super.onTouchEvent(event);

        mView = getChildAt(0);
        if (null != mView) {
            mView.getLocalVisibleRect(mRect);
        }
        if (event.getY() > mHeight - mRect.height()) {// 点击人脸控制栏
            return true;
        } else { // 点击上半部分UI延时等。。
            return false;
        }

    }
}
