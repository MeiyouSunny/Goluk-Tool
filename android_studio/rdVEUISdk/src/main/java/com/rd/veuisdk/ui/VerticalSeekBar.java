package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * 编辑界面控制配乐比
 */
public class VerticalSeekBar extends SeekBar {

    private VerticalSeekBarProgressListener listener;

    public VerticalSeekBar(Context context) {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec,
                                          int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    protected void onDraw(Canvas canvas) {
        canvas.rotate(-90);
        canvas.translate(-getHeight(), 0);
        Rect progressRect = getProgressDrawable().getBounds();
        Drawable thumbDrawable = getThumb();
        Rect thumbRect = thumbDrawable.getBounds();
        float left = (progressRect.right - progressRect.left) * getProgress()
                / getMax();
        thumbDrawable.setBounds((int) left, thumbRect.top,
                (int) left + thumbDrawable.getIntrinsicWidth(),
                thumbRect.bottom);
        super.onDraw(canvas);
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(listener!=null){
                    listener.onStartTouch();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int i = 0;
                i = getMax() - (int) (getMax() * event.getY() / getHeight());

                if (i >= 100) {
                    i = 100;
                }
                if (i <= 0) {
                    i = 0;
                }
                progress(i);
                break;
            case MotionEvent.ACTION_UP:
                if(listener!=null){
                    listener.onStopTouch();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    public void progress(int progress) {
        setProgress(progress);
        if (listener != null) {
            listener.onProgress(progress);
        }
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }

    public void setOnProgressListener(VerticalSeekBarProgressListener listener) {
        this.listener = listener;
    }

    public interface VerticalSeekBarProgressListener {
        void onProgress(int progress);

        void onStartTouch();

        void onStopTouch();
    }

}