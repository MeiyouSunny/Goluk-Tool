package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.rd.veuisdk.R;

/**
 * 自定义listview的item ,支持是否被选中，图片资源
 *
 * @author JIAN
 */
public class ExtListItemView extends View {
    private Paint mCheckedBorderPaint = new Paint(), mNormalBorderPaint = new Paint(),
            mTransPaint = new Paint();
    private Rect mBorderRect = new Rect(), mContentDst = new Rect();
    public boolean mIsSelected = false;
    private int mBorderWidth = 2;
    private Bitmap mBitmap;

    public ExtListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBorderWidth = getResources().getDimensionPixelSize(
                R.dimen.borderline_width2);
        mCheckedBorderPaint.setColor(getResources().getColor(R.color.white));
        mCheckedBorderPaint.setAntiAlias(true);
        mCheckedBorderPaint.setStyle(Style.STROKE);
        mCheckedBorderPaint.setStrokeWidth(mBorderWidth);

        mNormalBorderPaint.setColor(getResources().getColor(R.color.border_no_checked));
        mNormalBorderPaint.setAntiAlias(true);
        mNormalBorderPaint.setStyle(Style.STROKE);
        mNormalBorderPaint.setStrokeWidth(mBorderWidth);

        mTransPaint.setColor(getResources().getColor(R.color.transparent_orange));
        mTransPaint.setAntiAlias(true);
        mTransPaint.setStyle(Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
        if (null != mBitmap && !mBitmap.isRecycled()) {
            canvas.drawBitmap(mBitmap, null, mContentDst, null);
        }
        if (mIsSelected) {
            canvas.drawRect(mContentDst, mTransPaint);
        }


    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mBorderRect.set(left, top, getWidth() - left, getHeight() - top);

        int newb = mBorderWidth - 2;// -2防止有边框
        mContentDst.set(mBorderRect.left + newb, mBorderRect.top + newb,
                mBorderRect.right - newb, mBorderRect.bottom - newb);

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
        }, 100);
    }

    public void onCheckDelItem() {
        mCheckedBorderPaint.setColor(getResources().getColor(R.color.red));
        invalidate();
    }

    public void onResetDelItem() {
        mCheckedBorderPaint.setColor(getResources().getColor(R.color.white));
        invalidate();
    }

    public boolean isSelected() {
        return mIsSelected;
    }


    /**
     * 设置图片资源
     *
     * @param mBitmap
     */
    public void setbitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
        invalidate();
    }

    public Bitmap getBmpCache() {
        this.buildDrawingCache();
        return super.getDrawingCache();

    }
}
