package com.rd.veuisdk.ui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Checkable;

import com.rd.lib.ui.RotateImageView;
import com.rd.veuisdk.R;
import com.rd.veuisdk.utils.GraphicsHelper;

/**
 * 圆形图片,下载进度带边框
 *
 * @author JIAN
 */
public class ExtCircleImageView extends RotateImageView implements Checkable {

    private int mBorderWidth = 4;
    private int mDrawBorderColor = 0;
    private int mDrawBgColor = 0;
    private int mProgress = 100;
    private boolean mIsChecked = false;

    public ExtCircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray tArray = context.obtainStyledAttributes(attrs,
                R.styleable.ExtCircle);

        mIsChecked = tArray.getBoolean(R.styleable.ExtCircle_circleChecked,
                false);

        Resources res = getResources();

        mDrawBgColor = tArray.getInt(R.styleable.ExtCircle_circleBgColor,
                res.getColor(R.color.transparent));
        mDrawBorderColor = tArray.getInt(
                R.styleable.ExtCircle_circleBorderColor,
                res.getColor(R.color.main_orange));

        tArray.recycle();
    }


    /**
     * 设置当前下载进度
     *
     * @param pro 未下载时设置为0
     */
    public void setProgress(int pro) {
        mProgress = Math.min(100, pro);
        if (pro > 0) {
            if (!mIsChecked) {
                mIsChecked = true;
            }
        }
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = this.getDrawable();
        if (drawable == null) {
            super.onDraw(canvas);
            return;
        }
        try {
            Bitmap bitmap = GraphicsHelper.getBitmap(drawable);
            if (null != bitmap) {
                int w = this.getWidth();
                int h = this.getHeight();
                GraphicsHelper.drawRoundedCornerBitmap(canvas, w, h, bitmap,
                        w / 2, mBorderWidth, mDrawBorderColor, mDrawBgColor,
                        mIsChecked, mProgress);
                bitmap.recycle();
            }
        } catch (Exception e) {
        }
    }


    public void setBorderWidth(int mBorderWidth) {
        this.mBorderWidth = mBorderWidth;
    }

    public void setBorderColor(int borderColor) {
        this.mDrawBorderColor = borderColor;
        invalidate();
    }

    public void setBgColor(int bgColor) {
        this.mDrawBgColor = bgColor;
        invalidate();
    }

    @Override
    public boolean isChecked() {
        return mIsChecked;
    }

    @Override
    public void toggle() {
    }


    @Override
    public void setChecked(boolean checked) {
        mIsChecked = checked;
        invalidate();
    }

}