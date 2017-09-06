package com.rd.veuisdk.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.rd.veuisdk.R;

/**
 * 自定义listview的item ,支持是否被选中，图片资源，被选中顺序
 *
 * @author Scott
 */
public class NumItemView extends View {
    private final int DEFAULT_TEXTSIZE = 16;
    private final int SCALE_TEXTSIZE = 13;

    private Paint mPborder = new Paint();
    private Rect borderRect = new Rect(), contentDst = new Rect();
    private boolean isSelected = false;
    private Bitmap mBitmap;
    private int bmpWidth;
    private int bmpHeight;
    private float textScale;
    private int position;

    public NumItemView(Context context) {
        this(context, null, 0);
    }

    public NumItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPborder.setColor(getResources().getColor(R.color.transparent_black80));
        mPborder.setAntiAlias(true);
        mBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.media_item_selected_bg).copy(
                Bitmap.Config.ARGB_8888, true);
        bmpWidth = mBitmap.getWidth();
        bmpHeight = mBitmap.getHeight();
        Resources resources = getContext().getResources();
        textScale = resources.getDisplayMetrics().density;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
        if (isSelected) {
            drawTextToBitmap();
            canvas.drawBitmap(mBitmap, contentDst.left, contentDst.top,
                    new Paint());
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        borderRect.set(left - getLeft(), top - getTop(), right - getLeft(),
                bottom - getTop());

        contentDst.set(right - bmpWidth - 8, 8, right - 8, 8 + bmpHeight);
    }

    /**
     * 设置是否选中
     */
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        this.postDelayed(new Runnable() {

            @Override
            public void run() {
                invalidate();
            }
        }, 100);
    }

    public boolean isSelected() {
        return isSelected;
    }

    /**
     * 设置position
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * 在bitmap上显示文字
     */
    private void drawTextToBitmap() {
        mBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.media_item_selected_bg).copy(
                Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mBitmap);
        TextPaint textpaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textpaint.setColor(Color.WHITE);
        textpaint.setTextAlign(Paint.Align.CENTER);
        textpaint.setTextSize(DEFAULT_TEXTSIZE * textScale);
        if (position >= 99) {
            textpaint.setTextSize(SCALE_TEXTSIZE * textScale);
        }

        String string = "" + (position + 1);
        Rect bounds = new Rect();
        textpaint.getTextBounds(string, 0, string.length(), bounds);
        FontMetricsInt fontMetrics = textpaint.getFontMetricsInt();
        int baseline = (bounds.height() - fontMetrics.bottom + fontMetrics.top)
                / 2 - fontMetrics.top;

        int x = mBitmap.getWidth() / 2;
        int y = mBitmap.getHeight() / 2 + baseline / 2;

        canvas.drawText(string, x, y, textpaint);
    }

}
