package com.rd.veuisdk.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

/**
 * 选择资源
 */
public class BounceGridView extends GridView {
    public BounceGridView(Context context) {
        super(context);
        initBounceGridView();
    }

    public BounceGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBounceGridView();
    }

    public BounceGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initBounceGridView();
    }

    @SuppressLint("NewApi")
    private void initBounceGridView() {
        setFadingEdgeLength(0);
        setOverScrollMode(0);
    }

    @SuppressLint("NewApi")
    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
                                   int scrollY, int scrollRangeX, int scrollRangeY,
                                   int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY,
                scrollRangeX, scrollRangeY, maxOverScrollX, Math.abs(scrollY)
                        + 20 / ((Math.abs(scrollY) + 20) / 20), isTouchEvent);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
                                  boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rect,
                                                 boolean immediate) {
        rect.offset(child.getLeft() - child.getScrollX(), child.getTop() - child.getScrollY());
        return super.requestChildRectangleOnScreen(child, rect, immediate);
    }
}
