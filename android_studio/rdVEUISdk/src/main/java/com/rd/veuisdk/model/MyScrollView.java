package com.rd.veuisdk.model;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class MyScrollView extends HorizontalScrollView {

    public MyScrollView(Context context) {
	super(context);
	// TODO Auto-generated constructor stub
    }

    public MyScrollView(Context context, AttributeSet attrs) {
	super(context, attrs);
	// TODO Auto-generated constructor stub
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
	// TODO Auto-generated method stub
	// return super.onTouchEvent(ev);
	return false;
    }

}
