package com.rd.veuisdk.ui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Checkable;

import com.rd.lib.ui.RotateImageView;
import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.utils.GraphicsHelper;

/**
 * 矩形圆角可选
 */
public class ExtSquareImageView extends RotateImageView implements Checkable {

    private int mBorderWidth = 3;//边框线的宽度px
    private int mBorderColorN = Color.BLACK;//未选中矩形边框线颜色
    private int mBorderColorP = Color.GREEN;//选中矩形边框线颜色
    private int mDrawBgColor = Color.TRANSPARENT;
    private boolean mIsChecked = false;
    private int mCornersRadius = 20;//矩形圆角size
    private final int max = 100;
    private int progress = 0;

    public ExtSquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray tArray = context.obtainStyledAttributes(attrs,
                R.styleable.ExtSquare);

        mIsChecked = tArray.getBoolean(R.styleable.ExtSquare_squareChecked,
                false);

        Resources res = getResources();

        mDrawBgColor = tArray.getInt(R.styleable.ExtSquare_squareBgColor, Color.TRANSPARENT);
        mBorderColorP = tArray.getInt(R.styleable.ExtSquare_squareBorderColorP, Color.GREEN);
        mBorderColorN = tArray.getInt(R.styleable.ExtSquare_squareBorderColorN, Color.BLACK);
        mBorderWidth = (int) (tArray.getDimension(R.styleable.ExtSquare_squareBorderWidth, CoreUtils.dip2px(context, 2)));
        mCornersRadius = (int) (tArray.getDimension(R.styleable.ExtSquare_squareCornersRadius, CoreUtils.dip2px(context, 10)));
        tArray.recycle();
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

                GraphicsHelper.drawRoundedSquareCornerBitmap(canvas, w, h, bitmap,
                        mBorderWidth, (isChecked() ? mBorderColorP : mBorderColorN), mDrawBgColor,
                        mCornersRadius);

                bitmap.recycle();
            }
        } catch (Exception e) {
        }
    }


    public void setBorderWidth(int mBorderWidth) {
        this.mBorderWidth = mBorderWidth;
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