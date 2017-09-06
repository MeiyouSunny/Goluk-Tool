package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.rd.veuisdk.R;

/**
 * 自定义listview的item ,支持是否被选中，图片资源
 *
 * @author JIAN
 */
public class ExtItemView extends View {
    private Paint mBorderPaint = new Paint();
    private Rect mBorderRect = new Rect(), mContentDst = new Rect();
    private boolean mIsSelected = false;
    private Drawable mDrawable;
    private int mBorderWidth = 5;

    public ExtItemView(Context context) {
        this(context, null, 0);
    }

    public ExtItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private int[] mBmpSize = new int[2];

    public ExtItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mBorderWidth = getResources().getDimensionPixelSize(
                R.dimen.borderline_width4);
        mBorderPaint.setColor(getResources().getColor(R.color.transparent_black80));
        mBorderPaint.setAntiAlias(true);
//	mBorderPaint.setStyle(Style.STROKE);
//	mBorderPaint.setStrokeWidth(mBorderWidth);

        mDrawable = getResources().getDrawable(R.drawable.media_item_selected);
        mBmpSize[0] = mDrawable.getIntrinsicWidth();
        mBmpSize[1] = mDrawable.getIntrinsicHeight();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
        if (mIsSelected) {
//	    canvas.drawRect(mBorderRect, mBorderPaint);
            mDrawable.setBounds(mContentDst);
            mDrawable.draw(canvas);

        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mBorderRect.set(left - getLeft(), top - getTop(), right - getLeft(),
                bottom - getTop());

        mContentDst.set(right - mBmpSize[0] - 8, 8, right - 8, 8 + mBmpSize[1]);
    }

    /**
     * 设置是否选中
     */
    public void setSelected(boolean isSelected) {
        this.mIsSelected = isSelected;
        this.postDelayed(new Runnable() {

            @Override
            public void run() {
                invalidate();
            }
        }, 200);
    }

    public boolean isSelected() {
        return mIsSelected;
    }


}
