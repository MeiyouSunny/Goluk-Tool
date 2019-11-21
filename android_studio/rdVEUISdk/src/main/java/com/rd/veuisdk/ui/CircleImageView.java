package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Checkable;

import com.rd.lib.ui.RotateImageView;
import com.rd.veuisdk.utils.GraphicsHelper;


/***
 * 圆形边框，可旋转
 */
public class CircleImageView extends RotateImageView implements Checkable {

    private int mRadius = 40;

    private int mBorderWeight = 3;

    private int mBgColor = 0xff888888;
    private int mBorderColor = 0x00000000;

    private int mDrawBorderColor = mBorderColor;
    private int mDrawBgColor = mBgColor;

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public CircleImageView(Context context) {
        this(context, null);
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
                        w / 2, mBorderWeight, mDrawBorderColor, mDrawBgColor, isChecked(), 100);
                bitmap.recycle();
            }
        } catch (Exception e) {
        }
    }


    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int mRadius) {
        this.mRadius = mRadius;
    }

    public int getBorderWeight() {
        return mBorderWeight;
    }

    public void setBorderWeight(int mBorderWeight) {
        this.mBorderWeight = mBorderWeight;
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setBorderColor(int mBorderColor) {
        this.mBorderColor = mBorderColor;
        this.mDrawBorderColor = mBorderColor;
    }

    public int getBgColor() {
        return mBgColor;
    }

    public void setBgColor(int mBgColor) {
        this.mBgColor = mBgColor;
        this.mDrawBgColor = mBgColor;
    }

    private boolean isChecked = false;

    @Override
    public void setChecked(boolean b) {
        isChecked = b;
        invalidate();
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {

    }
}