package com.rd.veuisdk.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RadioButton;

import com.rd.veuisdk.R;

/**
 * 描边颜色显示器 正方形
 *
 * @author JIAN
 */
public class SquareView extends RadioButton {
    private Paint mPText = new Paint(), mPborder = new Paint();

    private String mText = "";
    private int mBgColor;
    private Rect rect = new Rect();

    public SquareView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray arr = context.obtainStyledAttributes(attrs,
                R.styleable.squareview);
        mText = arr.getString(R.styleable.squareview_square_text);
        mBgColor = arr.getColor(R.styleable.squareview_square_bg, R.color.white);

        arr.recycle();
        mPText.setAntiAlias(true);
        mPText.setColor(Color.BLACK);
        mPText.setTextSize(getResources().getDimension(R.dimen.text_size_16));

        mPborder.setAntiAlias(true);
        mPborder.setColor(getResources().getColor(R.color.special_checked));
        mPborder.setStrokeWidth(4f);
        mPborder.setStyle(Style.STROKE);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));

        canvas.drawColor(mBgColor);
        if (isChecked()) {
            canvas.drawRect(rect, mPborder);
        }
        if (!TextUtils.isEmpty(mText)) {
            canvas.drawColor(getResources().getColor(R.color.transparent_white));
            int strWidth = (int) mPText.measureText(mText);
            FontMetrics fm = mPText.getFontMetrics();

            int mleft = (getWidth() - strWidth) / 2;

            int mtop = (int) ((int) (getHeight() / 2 + Math.abs(fm.ascent) / 2) - Math
                    .abs(fm.descent)) + 2;

            canvas.drawText(mText, mleft, rect.top + mtop, mPText);

        }

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rect.set(2, 2, getWidth() - 2, getHeight() - 2);

    }

}
