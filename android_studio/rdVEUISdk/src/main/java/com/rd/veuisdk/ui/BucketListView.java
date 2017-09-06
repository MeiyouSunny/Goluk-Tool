package com.rd.veuisdk.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ListView;

/**
 *  选择资源->图库选择图片
 */
public class BucketListView extends ListView {
    private int mWidth;
    private int mHeight;

    public BucketListView(Context context) {
        super(context);
    }

    public BucketListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BucketListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    private void init() {
        DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();
        mWidth = dm.widthPixels / 2;
        mHeight = dm.heightPixels / 2;

    }

    public void setSize(int mWidth, int mHeight) {
        this.mWidth = mWidth;
        if (mHeight != 0) {
            this.mHeight = mHeight;
        }
    }

    // 设置listview的最大宽高
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mHeight > -1) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeight - 100,
                    MeasureSpec.AT_MOST);

        }
        if (mWidth > -1) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mWidth - 50,
                    MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
