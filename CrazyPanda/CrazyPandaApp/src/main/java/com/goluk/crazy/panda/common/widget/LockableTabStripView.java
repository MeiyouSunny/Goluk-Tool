package com.goluk.crazy.panda.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.astuetz.PagerSlidingTabStrip;

/**
 * Created by leege100 on 2016/8/19.
 * basedOn PagerSlidingTabStrip  [https://github.com/jpardogo/PagerSlidingTabStrip]
 */
public class LockableTabStripView extends PagerSlidingTabStrip {
    public LockableTabStripView(Context context) {
        super(context);
    }

    public LockableTabStripView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockableTabStripView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    private boolean mScrollable = true;

    public void setScrollingEnabled(boolean enabled) {
        mScrollable = enabled;
    }

    public boolean isScrollable() {
        return mScrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // if we can scroll pass the event to the superclass
                if (mScrollable) return super.onTouchEvent(ev);
                // only continue to handle the touch event if scrolling enabled
                return mScrollable; // mScrollable is always false at this point
            default:
                return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't do anything with intercepted touch events if
        // we are not scrollable
        if (!mScrollable) return false;
        else return super.onInterceptTouchEvent(ev);
    }

}
