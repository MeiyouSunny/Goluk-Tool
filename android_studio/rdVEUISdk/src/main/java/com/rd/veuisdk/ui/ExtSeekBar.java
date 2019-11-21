package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.PaintUtils;
import com.rd.veuisdk.R;

/**
 * @author JIAN
 * @create 2018/12/3
 * @Describe
 */
public class ExtSeekBar extends SeekBar {
    private Paint paint = null;
    private Drawable mDrawable;
    private Drawable mThumb;
    private int dw = 0, dh = 0;
    private int thumbW = 0, thumbH = 0;
    private int mProgressDrawableMargin = 0;
    private Paint progressPaint;
    private Paint bgPaint;

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    private int minValue = 400;

    public ExtSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        int color = getResources().getColor(R.color.main_orange);
        paint.setColor(color);
        paint.setTextSize(CoreUtils.dpToPixel(14));
        mDrawable = getResources().getDrawable(R.drawable.config_sbar_text_bg);
        mThumb = getResources().getDrawable(R.drawable.config_sbar_thumb_p);
        dw = mDrawable.getIntrinsicWidth();
        dh = mDrawable.getIntrinsicHeight();

        thumbW = mThumb.getIntrinsicWidth();
        thumbH = mThumb.getIntrinsicHeight();
        mProgressDrawableMargin = CoreUtils.dpToPixel(20 * 2);
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setColor(color);
        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(getResources().getColor(R.color.config_titlebar_bg));
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {

        int cp = getProgress();
        int px = ((getWidth() - mProgressDrawableMargin) * cp / getMax()) + (mProgressDrawableMargin / 2);
        int top = 10;
        Rect rect = new Rect(px - (dw / 2), top, px + (dw / 2), top + dh);
        int nTop = rect.bottom + 25;

        RectF progressRectF = new RectF(mProgressDrawableMargin / 2, nTop, px, nTop + 10);
        canvas.drawRoundRect(new RectF((mProgressDrawableMargin / 2), progressRectF.top, (getWidth() - mProgressDrawableMargin / 2), progressRectF.bottom), 5, 5, bgPaint);


        canvas.drawRoundRect(progressRectF, 5, 5, progressPaint);


        mDrawable.setBounds(rect);
        mDrawable.draw(canvas);


        int baseY = (int) progressRectF.centerY();
        Rect thumbRect = new Rect(px - (thumbW / 2), baseY - (thumbH / 2), px + (thumbW / 2), baseY + (thumbH / 2));

        mThumb.setBounds(thumbRect);
        mThumb.draw(canvas);


        String text = Integer.toString(minValue + getProgress());
        px = px - PaintUtils.getWidth(paint, text) / 2;
        int[] textHArr = PaintUtils.getHeight(paint);


        //完全保留图标底部
        canvas.drawText(text, px, top + ((dh - 6) / 2) + textHArr[1], paint);


        canvas.drawText(text, px, top + ((dh - 6) / 2) + textHArr[1], paint);


    }
}
