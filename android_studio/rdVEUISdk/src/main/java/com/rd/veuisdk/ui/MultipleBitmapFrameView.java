package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.PaintUtils;
import com.rd.veuisdk.R;

import java.util.ArrayList;

/**
 * 多个bitmap ，糅在一起显示 （拼接同时播放）
 *
 * @create 2019/6/5
 */
public class MultipleBitmapFrameView extends View {


    public MultipleBitmapFrameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(true);
        mShadowColor = getResources().getColor(R.color.transparent_black);
        mTextColor = getResources().getColor(R.color.transparent_white);
    }

    public void setList(ArrayList<Bitmap> list) {
        mList = list;
    }

    private ArrayList<Bitmap> mList;

    private int maxPx = CoreUtils.dpToPixel(5);

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mShadowColor = Color.TRANSPARENT;
    private int mTextColor = Color.WHITE;
    private int mTextSize = CoreUtils.dpToPixel(16);

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null != mList) {
            //只显示3张图
            int len = Math.min(3, mList.size());
            int itemMaxSize = Math.min(getWidth(), getHeight()) - maxPx;
            int itemPx = maxPx / len;
            Bitmap bmp;
            Rect dst;
            for (int i = len - 1; i >= 0; i--) {
                int left = itemPx * i;
                int bottom = getBottom() - itemPx * i;
                dst = new Rect(left, bottom - itemMaxSize, left + itemMaxSize, bottom);
                bmp = mList.get(i);
                canvas.drawBitmap(bmp, new Rect(0, 0, bmp.getWidth(), bmp.getHeight()), dst, null);
                if (i == 0) { //绘制文字
                    paint.reset();
                    paint.setColor(mShadowColor);
                    canvas.drawRect(dst, paint);
                    String text = "X" + mList.size();
                    paint.setColor(mTextColor);
                    paint.setTextSize(mTextSize);
                    float textWidth = paint.measureText(text);
                    int[] arr = PaintUtils.getHeight(paint);
                    canvas.drawText(text, dst.centerX() - (textWidth / 2), dst.centerY() + (arr[0] / 2) - arr[1], paint);
                }
            }
        }
    }

    public void recycle() {
        if (null != mList) {
            mList.clear();
            mList = null;
        }
    }
}
