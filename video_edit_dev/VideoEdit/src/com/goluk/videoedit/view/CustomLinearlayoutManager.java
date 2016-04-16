package com.goluk.videoedit.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

public class CustomLinearlayoutManager extends LinearLayoutManager {
    private int mPaddingTop;
    private int mPaddingBottom;

    public CustomLinearlayoutManager(Context context, int paddingTop, int paddingBottom) {
        super(context);
        mPaddingTop = paddingTop;
        mPaddingBottom = paddingBottom;
    }

    @Override
    public int getPaddingTop() {
        return mPaddingTop;
    }

    @Override
    public int getPaddingBottom() {
        return mPaddingBottom;
    }
}