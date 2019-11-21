package com.rd.veuisdk.demo.zishuo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import com.rd.lib.utils.CoreUtils;

/**
 * 文字描边输入框
 */
@SuppressLint("AppCompatCustomView")
public class StrokeEditView extends EditText {

    private TextPaint mTextPaint;
    private int mLeft = 0;//距离左边的位置

    private float mStrokeWidth = 0;
    private String mStrokeColor = "#000000";

    public StrokeEditView(Context context) {
        super(context);
        init(context);
    }

    public StrokeEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StrokeEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        mTextPaint = new TextPaint();
        mLeft = CoreUtils.dip2px(context, 5);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //复制原来TextViewg画笔中的一些参数
        if (mStrokeWidth != 0) {
            if (mTextPaint == null) {
                mTextPaint = new TextPaint();
            }
            Paint paint = getPaint();
            mTextPaint.setTextSize(paint.getTextSize());
            mTextPaint.setTypeface(paint.getTypeface());
            mTextPaint.setFlags(paint.getFlags());
            mTextPaint.setAlpha(paint.getAlpha());

            //自定义描边效果
            mTextPaint.setStyle(Paint.Style.STROKE);
            mTextPaint.setColor(Color.parseColor(mStrokeColor));
            mTextPaint.setStrokeWidth(mStrokeWidth * 5);
            String text = getText().toString();

            //在文本底层画出带描边的文本
            canvas.drawText(text, mLeft, getBaseline(), mTextPaint);
        }
        super.onDraw(canvas);
    }

    public void setStrokeWidth(float strokeWidth) {
        mStrokeWidth = strokeWidth;
        invalidate();
    }

    public void setStrokeColor(String strokeColor) {
        mStrokeColor = strokeColor;
        invalidate();
    }

}
