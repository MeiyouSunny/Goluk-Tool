package com.mobnote.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.TextView;


public class ParallelogramTextView extends TextView {
	Paint mInnerPaint;
	/** This var unit is px */
	private final static int sDelta = 30;

	public ParallelogramTextView(Context context) {
		super(context);
		init();
	}

	public ParallelogramTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ParallelogramTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mInnerPaint = new Paint();
		mInnerPaint.setAntiAlias(true);
		mInnerPaint.setColor(Color.parseColor("#ffffff"));
		mInnerPaint.setStyle(Paint.Style.FILL);
		mInnerPaint.setStrokeJoin(Paint.Join.ROUND);
	}
	

	@Override
	public void draw(Canvas canvas) {
		Path path = new Path();
		path.moveTo(getWidth(), 0);
		path.lineTo(sDelta, 0);
		path.lineTo(0, getHeight());
		path.lineTo(getWidth() - sDelta, getHeight());
		path.lineTo(getWidth(), 0);
		canvas.drawPath(path, mInnerPaint);
		super.draw(canvas);
	}
}
