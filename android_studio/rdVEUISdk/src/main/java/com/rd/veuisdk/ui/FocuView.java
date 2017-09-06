package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;

/**
 * 摄像头聚焦焦点圆
 */

class FocuView extends View {
    private Paint mPaint;
    private boolean mCanDraw = false;
    private int mXPosition, mYPosition;
    private int mRadius = 25;

    public FocuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.transparent_white));
        mRadius = CoreUtils.dpToPixel(35);
        mPaint.setStrokeWidth(2);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
    }


    void setLocation(int mXPosition, int mYPosition) {
        this.mXPosition = mXPosition;
        this.mYPosition = mYPosition;
        FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(mRadius, mRadius);
        fp.leftMargin = mXPosition - (mRadius / 2);
        fp.topMargin = mYPosition - (mRadius / 2);
        fp.rightMargin = 0;
        fp.bottomMargin = 0;
        setLayoutParams(fp);
        FocuView.this.startAnimation(AnimationUtils.loadAnimation(getContext(),
                R.anim.alpha_in));
        setVisibility(View.VISIBLE);
        removeCallbacks(mRunnable);
        this.postDelayed(mRunnable, 800);
    }

    void removeAll() {
        this.removeCallbacks(mRunnable);
        alphaGone();
    }

    private void alphaGone() {
        if (getVisibility() == View.VISIBLE) {
            FocuView.this.setVisibility(View.GONE);
            FocuView.this.startAnimation(AnimationUtils.loadAnimation(
                    getContext(), R.anim.alpha_out));
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            alphaGone();
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mXPosition, mYPosition, mRadius, mPaint);
        canvas.drawCircle(mXPosition, mYPosition, 15, mPaint);
    }
}
