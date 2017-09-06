package com.rd.veuisdk.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * 分割删除
 */
public class PriviewLayout extends FrameLayout {

    public PriviewLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PriviewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PriviewLayout(Context context) {
        this(context, null, 0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (setForceTarget) {
            View mTarget = getChildAt(1);
            if (mTarget instanceof DraggedTrashLayout) {
                return ((DraggedTrashLayout) mTarget).dispatchTouchEvent(ev);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean setForceTarget = false;

    public void setForceToTarget(boolean isset) {
        setForceTarget = isset;
    }
}
