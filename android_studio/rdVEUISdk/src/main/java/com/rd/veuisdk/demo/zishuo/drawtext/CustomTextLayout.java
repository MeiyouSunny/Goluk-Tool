package com.rd.veuisdk.demo.zishuo.drawtext;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.rd.vecore.graphics.Matrix;
import com.rd.vecore.models.CanvasObject;
import com.rd.vecore.models.TextLayout;
import com.rd.veuisdk.demo.zishuo.TextNode;

public class CustomTextLayout {

    private TextNode mTextNode;//文字节点
    private boolean vertical;//是否竖排
    private Matrix mMatrix;//文字运动矩阵
    private Paint mPaint, strokPaint = null;
    private int mWidth, mHeight;
    private TextLayout mTextLayout;

    public CustomTextLayout(int width, int height, Rect padding, TextLayout.Alignment algin, TextNode textNode) {
        this.mTextNode = textNode;
        this.mWidth = width;
        this.mHeight = height;
        mTextLayout = new TextLayout(mWidth, mHeight, mTextNode.getText(), padding, algin);
        //画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setAlpha(0);
        if (!TextUtils.isEmpty(mTextNode.getFont())) {
            mPaint.setTypeface(Typeface.createFromFile(mTextNode.getFont()));
        } else {
            mPaint.setTypeface(Typeface.create(Typeface.DEFAULT,
                    Typeface.NORMAL));
        }
        mPaint.setColor(Color.parseColor(mTextNode.getColor()));

        if (mTextNode.getStrokeWidth() != 0) {
            strokPaint = new Paint();
            strokPaint.setAntiAlias(true);
            strokPaint.setTypeface(mPaint.getTypeface());
            strokPaint.setFlags(mPaint.getFlags());
            strokPaint.setAlpha(mPaint.getAlpha());

            strokPaint.setStyle(Paint.Style.STROKE);
            strokPaint.setColor(Color.parseColor(mTextNode.getStrokeColor()));
            strokPaint.setStrokeWidth(mTextNode.getStrokeWidth() * 2);
        }
        mPaint.setShadowLayer(mTextNode.getShadowAlpha() * 5, 2, 2, Color.parseColor(mTextNode.getStrokeColor()));
    }

    public void draw(CanvasObject canvas, int left, int top) {
        canvas.drawText(mTextLayout, left, top, mPaint, strokPaint);
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public void setVertical() {
        vertical = true;
        mTextLayout.setVertical(vertical);
    }

    public boolean isVertical() {
        return vertical;
    }

    public TextNode getTextNode() {
        return mTextNode;
    }

    public Matrix getMatrix() {
        return mMatrix;
    }

    public void setMatrix(Matrix matrix) {
        mMatrix = matrix;
    }

    public Rect getPadding() {
        return mTextLayout.getPadding();
    }

    public TextLayout.Alignment getAlignment() {
        return mTextLayout.getAlignment();
    }

}
