package com.rd.veuisdk.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.rd.veuisdk.R;

/**
 * 自定义listview的item ,支持是否被选中，图片资源
 *
 * @author JIAN
 */
public class ExtListItemStyle extends ImageView {
    private Paint mBorderPaint = new Paint();
    private Rect mBorderRect = new Rect(), mContentDst = new Rect();
    public boolean mIsSelected = false;
    private int mBorderWidth = 2;
    private int mBorderRoundRadius = 6;
    private Bitmap mBitmap;

    public ExtListItemStyle(Context context) {
        this(context, null, 0);
    }

    public ExtListItemStyle(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private Drawable mDrawableSrc;

    public ExtListItemStyle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray ta = context.obtainStyledAttributes(attrs,
                R.styleable.ExtImage);
        if (null != ta) {
            mDrawableSrc = ta.getDrawable(R.styleable.ExtImage_extSrc);
            mBorderWidth = ta.getDimensionPixelSize(
                    R.styleable.ExtImage_extBorderLineWidth, 2);
            mBorderRoundRadius = ta.getDimensionPixelSize(R.styleable.ExtImage_extBorderRoundRadius, 6);
            ta.recycle();
        } else {
            mBorderWidth = getResources().getDimensionPixelSize(
                    R.dimen.borderline_width2);
        }
        mBorderPaint.setColor(getResources().getColor(R.color.main_orange));
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderWidth);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
        if (mIsSelected) {
            canvas.drawRoundRect(new RectF(mBorderRect), mBorderRoundRadius, mBorderRoundRadius, mBorderPaint);
        }
        if (null != mBitmap && !mBitmap.isRecycled()) {
            canvas.drawBitmap(mBitmap, null, mContentDst, null);

        } else if (null != mDrawableSrc) {
            mDrawableSrc.setBounds(mContentDst);
            mDrawableSrc.draw(canvas);

        }


    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mBorderRect.set(mBorderWidth, mBorderWidth, getWidth() - mBorderWidth,
                getHeight() - mBorderWidth);
        mContentDst.set(mBorderRect.left + mBorderWidth, mBorderRect.top
                        + mBorderWidth, mBorderRect.right - mBorderWidth,
                mBorderRect.bottom - mBorderWidth);
    }

    /**
     * 设置是否选中
     */
    public void setSelected(boolean mIsSelected) {

        this.mIsSelected = mIsSelected;

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

}
