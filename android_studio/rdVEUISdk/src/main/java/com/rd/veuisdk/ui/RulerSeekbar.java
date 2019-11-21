package com.rd.veuisdk.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;

/**
 * 卡尺横向拖动view
 */
public class RulerSeekbar extends View {
    private int SCALE_WIDTH = 4;
    private int SCALE_LONG_HEIGHT = 35;
    private int SCALE_SHORT_HEIGHT = 20;
    private int THUMB_RADIUS = 35;

    private RectF[] rects;
    private RectF HoriRect;
    private int longScaleCount = 5;
    private int shortScaleCount = 4;
    private Paint mPaint = new Paint();
    private Paint mThumbPaint = new Paint();
    private Paint mTextPaint = new TextPaint();
    private CharSequence[] mCharArrays = null;
    private OnSeekListener mListener;
    private int max = 100;
    private float progress;

    public RulerSeekbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray tA = context.obtainStyledAttributes(attrs,
                R.styleable.extdragarray);
        mCharArrays = tA.getTextArray(R.styleable.extdragarray_sArrays);
        if (mCharArrays != null && mCharArrays.length > 0) {
            longScaleCount = mCharArrays.length;
        }
        tA.recycle();
        mPaint.setAntiAlias(true);
        mThumbPaint.setAntiAlias(true);
        mThumbPaint.setColor(getResources().getColor(R.color.white));
        mTextPaint.setColor(getResources().getColor(R.color.ruler_text_color));
        mTextPaint.setTextSize(CoreUtils.dpToPixel(14));
        mGesDetector = new GestureDetector(context, new pressGestureListener());
        SCALE_WIDTH = CoreUtils.dpToPixel(1.5f);
        SCALE_SHORT_HEIGHT = CoreUtils.dpToPixel(7);
        SCALE_LONG_HEIGHT = CoreUtils.dpToPixel(14);
        THUMB_RADIUS = CoreUtils.dpToPixel(13);
    }


    private GestureDetector mGesDetector;

    public RulerSeekbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RulerSeekbar(Context context) {
        this(context, null, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
        canvas.drawRect(HoriRect, mPaint);
        mPaint.setColor(getResources().getColor(R.color.white));
        for (int n = 0; n < rects.length; n++) {
            //190625  全部绘制成底部灰白色
            mPaint.setColor(getResources().getColor(R.color.ruler_background_color));
            canvas.drawRect(rects[n], mPaint);
        }

        canvas.drawCircle(mXPosition, HoriRect.centerY(), THUMB_RADIUS, mThumbPaint);
        for (int n = 0; n < mCharArrays.length; n++) {
            String text = mCharArrays[n].toString();
            Rect textRect = new Rect();
            mTextPaint.getTextBounds(text, 0, text.length(), textRect);
            float x = rects[n * longScaleCount].centerX() - textRect.centerX();
            x = Math.max(x, 0);
            if (n == longScaleCount - 1) {
                if (x + textRect.centerX() > getWidth()) {
                    x = getWidth() - textRect.centerX();
                }
            }
            canvas.drawText(mCharArrays[n].toString(), x, HoriRect.centerY() - CoreUtils.dpToPixel(25) - textRect.centerY(), mTextPaint);
        }
    }

    private void resetRect() {
        mXPosition = Math.max(Math.min(mXPosition, getWidth() - THUMB_RADIUS), THUMB_RADIUS);
        progress = max * (mXPosition - HoriRect.left) / HoriRect.width();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int size = shortScaleCount * (longScaleCount - 1) + longScaleCount;
        rects = new RectF[size];
        HoriRect = new RectF(THUMB_RADIUS, (getHeight() - SCALE_WIDTH) / 2,
                getWidth() - THUMB_RADIUS, (getHeight() + SCALE_WIDTH) / 2);
        float intervalWidth = (float) (getWidth() - SCALE_WIDTH - THUMB_RADIUS * 2) / (size - 1);
        float rectLeft = THUMB_RADIUS;
        for (int n = 0; n < size; n++) {
            float rectTop, rectBottom;
            if (n % longScaleCount == 0) {
                rectTop = (getHeight() - SCALE_LONG_HEIGHT) / 2;
                rectBottom = rectTop + SCALE_LONG_HEIGHT;
            } else {
                rectTop = (getHeight() - SCALE_SHORT_HEIGHT) / 2;
                rectBottom = rectTop + SCALE_SHORT_HEIGHT;
            }
            RectF rectF = new RectF(rectLeft, rectTop,
                    rectLeft + SCALE_WIDTH, rectBottom);
            rects[n] = rectF;
            rectLeft += intervalWidth;
        }
        if (mXPosition == -1) {
            mXPosition = getWidth() / 2;
        }
    }

    public void setMax(int max) {
        this.max = max;
    }


    public int getMax() {
        return max;
    }

    private boolean mIsForced = false;

    private float mXPosition = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGesDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mIsForced) {
                mXPosition = event.getX();
                onActionUp();
            }
        }
        invalidate();
        return true;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        mXPosition = HoriRect.left + HoriRect.width() * progress / max;
        invalidate();
    }

    public float getProgress() {
        return progress;
    }

    public void setOnSeekListener(OnSeekListener listener) {
        mListener = listener;
    }

    public interface OnSeekListener {
        void onSeekStart(float progress, int max);

        void onSeek(float progress, int max);

        void onSeekEnd(float progress, int max);
    }

    private class pressGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            mXPosition = e2.getX();
            resetRect();
            if (mListener != null) {
                mListener.onSeek(progress, max);
            }
            return true;

        }

        @Override
        public boolean onDown(MotionEvent e) {
            mIsForced = true;
            mXPosition = e.getX();
            resetRect();
            invalidate();
            if (mListener != null) {
                mListener.onSeekStart(progress, max);
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float veSlocityX, float velocityY) {
            onActionUp();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            mXPosition = e.getX();
            onActionUp();
            return false;
        }
    }


    private void onActionUp() {
        resetRect();
        mIsForced = false;
        if (mListener != null) {
            mListener.onSeekEnd(progress, max);
        }
    }


}
