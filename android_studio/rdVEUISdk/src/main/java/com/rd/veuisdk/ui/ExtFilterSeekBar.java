package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.rd.veuisdk.R;

/**
 * @author JIAN
 * @create 2018/12/6
 * @Describe
 */
public class ExtFilterSeekBar extends SeekBar {
    private Drawable mThumb_n, mThumb_p;
    private int thumbW, thumbH;
    private int mProgressDrawableMargin = 0;
    private Paint bgPaint, progressPaint;

    /**
     * @param changedByHand
     */
    public void setChangedByHand(boolean changedByHand) {
        isChangedByHand = changedByHand;
        invalidate();
    }

    private boolean isChangedByHand = false;

    public ExtFilterSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);


        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(getResources().getColor(R.color.config_titlebar_bg));
        mThumb_n = getResources().getDrawable(R.drawable.config_sbar_thumb_n);
        mThumb_p = getResources().getDrawable(R.drawable.config_sbar_thumb_p);

        thumbW = mThumb_n.getIntrinsicWidth();
        thumbH = mThumb_n.getIntrinsicHeight();
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setColor(getResources().getColor(R.color.main_orange));
        mProgressDrawableMargin = (int) (thumbW * 1.05);
    }

    private final int RADIUS = 4;



    public void setDefaultValue(int defaultValue) {
        this.defaultValue = defaultValue;
    }

    private int defaultValue=0;




    @Override
    protected synchronized void onDraw(Canvas canvas) {

        int cp = getProgress();
        int width = getWidth();
        int paddingLeft = mProgressDrawableMargin / 2;
        int px = ((width - mProgressDrawableMargin) * cp / getMax()) + paddingLeft;


        int nTop = (getHeight() / 2) - RADIUS;

        RectF bgRectF = new RectF(paddingLeft, nTop, (width - paddingLeft), nTop + RADIUS * 2);

        //?????????
        canvas.drawRoundRect(bgRectF, RADIUS, RADIUS, bgPaint);

        RectF progressRectF;

        int beginPx=((width - mProgressDrawableMargin) *defaultValue/getMax())+paddingLeft;


        progressRectF=new RectF(beginPx,bgRectF.top,px,bgRectF.bottom);

        //????????????
        canvas.drawRoundRect(progressRectF, RADIUS, RADIUS, progressPaint);

        int baseY = (int) bgRectF.centerY();
        Rect thumbRect = new Rect(px - (thumbW / 2), baseY - (thumbH / 2), px + (thumbW / 2), baseY + (thumbH / 2));
        if (isChangedByHand) {
            mThumb_p.setBounds(thumbRect);
            mThumb_p.draw(canvas);
        } else {
            mThumb_n.setBounds(thumbRect);
            mThumb_n.draw(canvas);

        }
    }


}
