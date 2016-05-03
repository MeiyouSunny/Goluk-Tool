package com.mobnote.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
public class ClipView extends View {

	private Paint mPaint;
	private Context mContext;

	public ClipView(Context context) {
		this(context, null);
		mContext = context;
	}

	public ClipView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		mContext = context;
	}

	public ClipView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mPaint = new Paint();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width = this.getWidth();
		int height = this.getHeight();
		//内圈
		int innerCircle = dip2px(mContext, 100); 
		int ringWidth = height; 

		mPaint.setColor(Color.WHITE);
		mPaint.setStrokeWidth(2);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setAntiAlias(true);
		canvas.drawCircle(width / 2, height / 2, innerCircle, mPaint);
		//将圆以外的部分设置成半透明
		mPaint.setColor(0xaa000000);
		mPaint.setStrokeWidth(ringWidth);
		canvas.drawCircle(width / 2, height / 2, innerCircle + 1 + ringWidth
				/ 2, mPaint);

	}
	//屏幕密度转换成像素  dp转换成px
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

}
