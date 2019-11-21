package com.rd.veuisdk.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Style item 区域正方形
 *
 * @author JIAN
 */
public class SpecialItemFrameLayout extends FrameLayout {

    public SpecialItemFrameLayout(Context context) {
        this(context, null, 0);
    }

    public SpecialItemFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpecialItemFrameLayout(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int previewWidth = MeasureSpec.getSize(widthMeasureSpec);

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY));
    }

}
